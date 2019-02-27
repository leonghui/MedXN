/*******************************************************************************
 * Copyright: (c)  2013  Mayo Foundation for Medical Education and
 * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 * triple-shield Mayo logo are trademarks and service marks of MFMER.
 *
 * Except as contained in the copyright notice above, or as used to identify
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 *
 * Copyright (c) 2018-2019. Leong Hui Wong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.ohnlp.medxn.ae;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceAccessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Substance;
import org.ohnlp.medxn.fhir.FhirQueryClient;
import org.ohnlp.medxn.fhir.FhirQueryUtils;
import org.ohnlp.medxn.type.Drug;
import org.ohnlp.medxn.type.Ingredient;
import org.ohnlp.medxn.type.LookupWindow;
import org.ohnlp.medxn.type.MedAttr;
import org.ohnlp.typesystem.type.textspan.Sentence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class FhirMedExtAnnotator extends JCasAnnotator_ImplBase {

    private List<MedAttr> attributes;
    private List<Sentence> sortedSentences;
    private List<MedAttr> formsRoutesFrequencies;
    private Map<String, Medication> allMedications;
    private Map<String, Substance> allSubstances;
    private List<String> falseMedications;

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
        super.initialize(uimaContext);

        // Get config parameter values
        String url = (String) uimaContext.getConfigParameterValue("FHIR_SERVER_URL");
        int timeout = (int) uimaContext.getConfigParameterValue("TIMEOUT_SEC");
        FhirQueryClient queryClient = FhirQueryClient.createFhirQueryClient(url, timeout);

        allMedications = queryClient.getAllMedications();
        allSubstances = queryClient.getAllSubstances();

        try {
            Path filePath = Paths.get(getContext().getResourceURI("falseMedDict"));

            falseMedications = Files.lines(filePath)
                    .filter(line -> !line.startsWith("#"))
                    .filter(line -> line.length() != 0)
                    .filter(line -> !Character.isWhitespace(line.charAt(0)))
                    .map(String::toLowerCase)
                    .map(String::trim)
                    .collect(ImmutableList.toImmutableList());

        } catch (IOException | ResourceAccessException ex) {
            System.out.println(ex.getClass());
            System.out.println("ERROR: Reading falseMedDict.txt " + ex.toString());
        }

    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void process(JCas jcas) {
        attributes = ImmutableList.copyOf(jcas.getAnnotationIndex(MedAttr.type));
        sortedSentences = ImmutableList.sortedCopyOf(
                Comparator.comparingInt(Sentence::getBegin).thenComparingInt(Sentence::getEnd),
                jcas.getAnnotationIndex(Sentence.type)
        );
        formsRoutesFrequencies = attributes.stream()
                .filter(attribute -> attribute.getTag().contentEquals(FhirQueryUtils.MedAttrConstants.FORM)
                        ||
                        attribute.getTag().contentEquals(FhirQueryUtils.MedAttrConstants.ROUTE)
                        ||
                        attribute.getTag().equals(FhirQueryUtils.MedAttrConstants.FREQUENCY))
                .sorted(Comparator.comparingInt(MedAttr::getBegin).thenComparingInt(MedAttr::getEnd))
                .collect(ImmutableList.toImmutableList());

        removeOverlappingDrugs(jcas);
        removeFalseDrugs(jcas);
        mergeBrandsAndGenerics(jcas);
        createLookupWindows(jcas);
        associateAttributesAndIngredients(jcas);
    }

    @SuppressWarnings("UnstableApiUsage")
    private void removeOverlappingDrugs(JCas jcas) {
        List<Drug> drugs = ImmutableList.copyOf(jcas.getAnnotationIndex(Drug.type));

        // remove drugs that overlap with attributes
        // remove drugs that overlap other drugs and has shorter ingredient names
        drugs.stream()
                .filter(drug -> {
                    String drugName = Streams.stream(Optional.ofNullable(drug.getIngredients())
                            .orElse(new FSArray(jcas, 0)))
                            .map(Ingredient.class::cast)
                            .map(Ingredient::getItem)
                            .map(allSubstances::get)
                            .map(Substance::getCode)
                            .map(CodeableConcept::getCodingFirstRep)
                            .map(Coding::getDisplay)
                            .collect(Collectors.joining(" + "));

                    return attributes.stream()
                            .filter(attribute -> attribute.getBegin() <= drug.getBegin())
                            .anyMatch(attribute -> attribute.getEnd() >= drug.getEnd()) ||
                            drugs.stream()
                                    .filter(drug2 -> drug2.getBegin() <= drug.getBegin())
                                    .filter(drug2 -> {
                                        String drug2Name = Streams.stream(Optional.ofNullable(drug2.getIngredients())
                                                .orElse(new FSArray(jcas, 0)))
                                                .map(Ingredient.class::cast)
                                                .map(Ingredient::getItem)
                                                .map(allSubstances::get)
                                                .map(Substance::getCode)
                                                .map(CodeableConcept::getCodingFirstRep)
                                                .map(Coding::getDisplay)
                                                .collect(Collectors.joining(" + "));
                                        return drug2Name.length() > drugName.length();
                                    })
                                    .anyMatch(drug2 -> drug2.getEnd() >= drug.getEnd());
                })
                .forEach(Drug::removeFromIndexes);
    }

    // Remove false drugs using Mayo Clinic's falseMedDict.txt
    private void removeFalseDrugs(JCas jcas) {
        List<Drug> drugs = ImmutableList.copyOf(jcas.getAnnotationIndex(Drug.type));

        drugs.stream()
                .filter(drug ->
                        falseMedications.contains(
                                drug.getCoveredText().toLowerCase().replaceAll("\\W", " ")
                        ))
                .forEach(drug -> {
                    drug.removeFromIndexes(jcas);

                    getContext().getLogger().log(Level.INFO, "Removed drug: " + drug.getCoveredText());
                });
    }

    private void mergeBrandsAndGenerics(JCas jcas) {

        AtomicBoolean drugsModified = new AtomicBoolean(false);
        AtomicBoolean mergeAhead = new AtomicBoolean(false);

        // scan through all drugs and select a merge behaviour based on the most number of merges possible
        int count = scanAndMergeDrugs(jcas, false);

        if (count >= 0) {
            mergeAhead.set(true);
            getContext().getLogger().log(Level.INFO, "Merge ahead selected");
        } else {
            getContext().getLogger().log(Level.INFO, "Merge backward selected");
        }

        do {
            drugsModified.set(false);
            int numOfDrugsBefore = jcas.getAnnotationIndex(Drug.type).size();

            scanAndMergeDrugs(jcas, true);

            int numOfDrugsAfter = jcas.getAnnotationIndex(Drug.type).size();

            if (numOfDrugsBefore != numOfDrugsAfter) {
                drugsModified.set(true);
            }

        } while (drugsModified.get());
    }

    private int scanAndMergeDrugs(JCas jcas, boolean isMerge) {
        AtomicInteger count = new AtomicInteger(0);
        AtomicBoolean drugsModified = new AtomicBoolean(false);

        List<Drug> sortedDrugs = ImmutableList.sortedCopyOf(
                Comparator.comparingInt(Drug::getBegin).thenComparingInt(Drug::getEnd),
                jcas.getAnnotationIndex(Drug.type)
        );

        IntStream.range(0, sortedDrugs.size()).forEach(drugIndex -> {
            Drug drug = sortedDrugs.get(drugIndex);

            if (drug.getBrand() != null) {
                if (drugIndex + 1 < sortedDrugs.size()) {
                    Drug nextDrug = sortedDrugs.get(drugIndex + 1);

                    if (isMerge) {
                        getContext().getLogger().log(Level.INFO, "Looking ahead: " +
                                nextDrug.getCoveredText() +
                                " with drug: " + drug.getCoveredText()
                        );
                    }

                    // SCENARIO 1: Brand name is followed by ingredient name
                    if (nextDrug.getBrand() == null && compareIngredients(nextDrug, drug)) {
                        count.getAndIncrement();

                        if (isMerge) {
                            mergeDrugs(jcas, nextDrug, drug);

                            getContext().getLogger().log(Level.INFO, "Merged drug: " +
                                    nextDrug.getCoveredText() +
                                    " with drug: " + drug.getCoveredText()
                            );

                            drugsModified.set(true);
                        }
                    }

                } else if (drugIndex > 0) {
                    Drug prevDrug = sortedDrugs.get(drugIndex - 1);

                    if (isMerge) {
                        getContext().getLogger().log(Level.INFO, "Looking backward: " +
                                prevDrug.getCoveredText() +
                                " with drug: " + drug.getCoveredText()
                        );
                    }

                    // SCENARIO 2: Ingredient name is followed by brand name
                    if (prevDrug.getBrand() == null && compareIngredients(prevDrug, drug)) {
                        count.getAndDecrement();

                        if (isMerge) {
                            mergeDrugs(jcas, prevDrug, drug);

                            getContext().getLogger().log(Level.INFO, "Merged drug: " +
                                    prevDrug.getCoveredText() +
                                    " with drug: " + drug.getCoveredText()
                            );

                            drugsModified.set(true);
                        }
                    }
                }
            }
        });

        return count.get();
    }

    private void createLookupWindows(JCas jcas) {
        // always generate a new instance of drug list because the index has been updated
        List<Drug> sortedDrugs = ImmutableList.sortedCopyOf(
                Comparator.comparingInt(Drug::getBegin).thenComparingInt(Drug::getEnd),
                jcas.getAnnotationIndex(Drug.type)
        );

        List<LookupWindow> windows = new ArrayList<>();

        IntStream.range(0, sortedSentences.size()).forEach(sentenceIndex -> {
            Sentence sentence = sortedSentences.get(sentenceIndex);

            IntStream.range(0, sortedDrugs.size()).forEach(drugIndex -> {

                Drug drug = sortedDrugs.get(drugIndex);

                // only consider drugs that begin, but not necessarily end, in the same sentence
                if (drug.getBegin() >= sentence.getBegin() && drug.getBegin() <= sentence.getEnd()) {

                    LookupWindow window = new LookupWindow(jcas);

                    window.setBegin(drug.getBegin());

                    int nextDrugBegin = drugIndex + 1 < sortedDrugs.size() ?
                            sortedDrugs.get(drugIndex + 1).getBegin() : Integer.MAX_VALUE;

                    int nextSentenceEnd = sentenceIndex < sortedSentences.size() - 1 ?
                            sortedSentences.get(sentenceIndex + 1).getEnd() : sentence.getEnd();

                    AtomicInteger firstFormTerminator = new AtomicInteger(0);
                    AtomicInteger firstRouteTerminator = new AtomicInteger(0);
                    AtomicInteger firstFrequencyTerminator = new AtomicInteger(0);

                    formsRoutesFrequencies.stream()
                            .filter(medAttr -> medAttr.getBegin() > drug.getEnd())
                            .filter(medAttr -> medAttr.getEnd() < nextDrugBegin)
                            .forEach(MedAttr -> {
                                switch (MedAttr.getTag()) {
                                    case FhirQueryUtils.MedAttrConstants.FORM:
                                        firstFormTerminator.compareAndSet(0, MedAttr.getEnd());
                                        break;
                                    case FhirQueryUtils.MedAttrConstants.ROUTE:
                                        firstRouteTerminator.compareAndSet(0, MedAttr.getEnd());
                                        break;
                                    case FhirQueryUtils.MedAttrConstants.FREQUENCY:
                                        firstFrequencyTerminator.compareAndSet(0, MedAttr.getEnd());
                                        break;
                                }
                            });

                    // choose the largest value out of the 3 terminators (each the first of its kind)
                    int nextTerminatorEnd = Math.max(firstFormTerminator.get(),
                            Math.max(firstRouteTerminator.get(), firstFrequencyTerminator.get()));

                    int windowEnd;

                    if (nextTerminatorEnd != 0) {
                        // choose the smallest value out of the 3 criteria
                        windowEnd = Math.min(nextDrugBegin, Math.min(nextSentenceEnd, nextTerminatorEnd));
                    } else {
                        windowEnd = Math.min(nextDrugBegin, nextSentenceEnd);
                    }

                    window.setEnd(windowEnd);

                    windows.add(window);
                }
            });
        });
        windows.forEach(LookupWindow::addToIndexes);
    }

    private void associateAttributesAndIngredients(JCas jcas) {
        jcas.getAnnotationIndex(LookupWindow.type).forEach(window ->
                jcas.getAnnotationIndex(Drug.type).subiterator(window).forEachRemaining(annotation -> {

                    Drug drug = (Drug) annotation;

                    // attributes are assumed not to be contained in drug names
                    List<MedAttr> filteredAttributes = attributes.stream()
                            .filter(attribute -> attribute.getBegin() >= window.getBegin())
                            .filter(attribute -> attribute.getEnd() <= window.getEnd())
                            .collect(Collectors.toList());

                    if (filteredAttributes.size() > 0) {
                        FSArray attributesArray = new FSArray(jcas, filteredAttributes.size());

                        attributesArray.copyFromArray(
                                filteredAttributes.toArray(
                                        new FeatureStructure[0]), 0, 0, filteredAttributes.size());

                        drug.setAttrs(attributesArray);

                        getContext().getLogger().log(Level.INFO, "Associating attributes: " +
                                FhirQueryUtils.getCoveredTextFromAnnotations(filteredAttributes) +
                                " with drug: " + drug.getCoveredText()
                        );
                    }
                })
        );
    }

    @SuppressWarnings("UnstableApiUsage")
    private boolean compareIngredients(Drug sourceDrug, Drug targetDrug) {
        boolean canBeMerged = false;

        List<String> codes = ImmutableList.copyOf(targetDrug.getBrand().split(","));

        Set<Medication> targetMedications = FhirQueryUtils.getMedicationsFromCode(allMedications, codes);

        ArrayList<String> candidateIngredients = new ArrayList<>(
                FhirQueryUtils.getIngredientIdsFromMedications(targetMedications));

        // don't add ingredients that are already found in the drug
        if (targetDrug.getIngredients() != null) {
            List<String> targetIngredients = Streams.stream(targetDrug.getIngredients())
                    .map(Ingredient.class::cast)
                    .map(Ingredient::getItem)
                    .collect(ImmutableList.toImmutableList());

            candidateIngredients.removeAll(targetIngredients);
        }

        if (sourceDrug.getIngredients() != null) {
            List<String> sourceIngredients = Streams.stream(sourceDrug.getIngredients())
                    .map(Ingredient.class::cast)
                    .map(Ingredient::getItem)
                    .collect(ImmutableList.toImmutableList());

            if (candidateIngredients.containsAll(sourceIngredients)) {
                canBeMerged = true;
            }
        }

        return canBeMerged;
    }

    @SuppressWarnings("UnstableApiUsage")
    private void mergeDrugs(JCas jcas, Drug sourceDrug, Drug targetDrug) {

        // expand interval to cover both drugs
        int begin = sourceDrug.getBegin() < targetDrug.getBegin() ? sourceDrug.getBegin() : targetDrug.getBegin();
        int end = sourceDrug.getEnd() > targetDrug.getEnd() ? sourceDrug.getEnd() : targetDrug.getEnd();

        Drug mergedDrug = new Drug(jcas, begin, end);

        // prefer attributes from source drug
        String form = sourceDrug.getForm() != null ? sourceDrug.getForm() : targetDrug.getForm();
        String brand = sourceDrug.getBrand() != null ? sourceDrug.getBrand() : targetDrug.getBrand();

        mergedDrug.setForm(form);
        mergedDrug.setBrand(brand);

        Set<FeatureStructure> mergedAttributes = null;

        if (sourceDrug.getAttrs() != null && targetDrug.getAttrs() != null) {
            mergedAttributes = Stream.concat(
                    Streams.stream(sourceDrug.getAttrs()),
                    Streams.stream(targetDrug.getAttrs()))
                    .map(MedAttr.class::cast)
                    .sorted(Comparator.comparingInt(MedAttr::getBegin))
                    .collect(ImmutableSet.toImmutableSet());
        } else if (sourceDrug.getAttrs() != null) {
            mergedAttributes = ImmutableSet.copyOf(sourceDrug.getAttrs());
        } else if (targetDrug.getAttrs() != null) {
            mergedAttributes = ImmutableSet.copyOf(targetDrug.getAttrs());
        }

        if (mergedAttributes != null && mergedAttributes.size() > 0) {
            FSArray medAttrs = new FSArray(jcas, mergedAttributes.size());

            medAttrs.copyFromArray(mergedAttributes
                    .toArray(new FeatureStructure[0]), 0, 0, mergedAttributes.size());

            mergedDrug.setAttrs(medAttrs);
        }

        Set<FeatureStructure> mergedIngredients = null;

        if (sourceDrug.getIngredients() != null && targetDrug.getIngredients() != null) {
            mergedIngredients = Stream.concat(
                    Streams.stream(sourceDrug.getIngredients()),
                    Streams.stream(targetDrug.getIngredients()))
                    .map(Ingredient.class::cast)
                    .sorted(Comparator.comparingInt(Ingredient::getBegin))
                    .collect(ImmutableSet.toImmutableSet());
        } else if (sourceDrug.getIngredients() != null) {
            mergedIngredients = ImmutableSet.copyOf(sourceDrug.getIngredients());
        } else if (targetDrug.getIngredients() != null) {
            mergedIngredients = ImmutableSet.copyOf(targetDrug.getIngredients());
        }

        if (mergedIngredients != null && mergedIngredients.size() > 0) {
            FSArray medIngredients = new FSArray(jcas, mergedIngredients.size());

            medIngredients.copyFromArray(mergedIngredients
                    .toArray(new FeatureStructure[0]), 0, 0, mergedIngredients.size());

            mergedDrug.setIngredients(medIngredients);
        }

        sourceDrug.removeFromIndexes(jcas);
        targetDrug.removeFromIndexes(jcas);
        mergedDrug.addToIndexes(jcas);

    }
}