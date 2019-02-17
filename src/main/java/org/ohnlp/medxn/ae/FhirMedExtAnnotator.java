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
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.hl7.fhir.dstu3.model.Medication;
import org.ohnlp.medxn.fhir.FhirQueryClient;
import org.ohnlp.medxn.fhir.FhirQueryUtils;
import org.ohnlp.medxn.type.Drug;
import org.ohnlp.medxn.type.Ingredient;
import org.ohnlp.medxn.type.LookupWindow;
import org.ohnlp.medxn.type.MedAttr;
import org.ohnlp.typesystem.type.textspan.Sentence;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class FhirMedExtAnnotator extends JCasAnnotator_ImplBase {

    private ImmutableList<MedAttr> attributes;
    private ImmutableList<Sentence> sortedSentences;
    private ImmutableList<MedAttr> formsRoutesFrequencies;
    private List<Medication> allMedications;

    @Override
    public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
        super.initialize(uimaContext);

        // Get config parameter values
        String url = (String) uimaContext.getConfigParameterValue("FHIR_SERVER_URL");
        int timeout = (int) uimaContext.getConfigParameterValue("TIMEOUT_SEC");
        FhirQueryClient queryClient = FhirQueryClient.createFhirQueryClient(url, timeout);

        allMedications = queryClient.getAllMedications();
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

        mergeBrandAndGenerics(jcas);
        createLookupWindows(jcas);
        associateAttributesAndIngredients(jcas);
    }

    private void mergeBrandAndGenerics(JCas jcas) {

        AtomicBoolean drugsModified = new AtomicBoolean(false);

        do {
            drugsModified.set(false);
            // always generate a new instance of drug list because the index has been updated
            ImmutableList<Drug> sortedDrugs = ImmutableList.sortedCopyOf(
                    Comparator.comparingInt(Drug::getBegin).thenComparingInt(Drug::getEnd),
                    jcas.getAnnotationIndex(Drug.type)
            );

            IntStream.range(0, sortedDrugs.size()).forEach(drugIndex -> {
                Drug drug = sortedDrugs.get(drugIndex);


                if (drug.getBrand() != null) {
                    if (drugIndex + 1 < sortedDrugs.size()) {
                        Drug nextDrug = sortedDrugs.get(drugIndex + 1);

                        getContext().getLogger().log(Level.INFO, "Looking ahead: " +
                                nextDrug.getCoveredText() +
                                " with drug: " + drug.getCoveredText()
                        );

                        // SCENARIO 1: Brand name is followed by ingredient name
                        if (nextDrug.getBrand() == null) {
                            drugsModified.set(compareIngredientsAndMergeDrugs(jcas, nextDrug, drug));
                        }

                    } else if (drugIndex > 0) {
                        Drug prevDrug = sortedDrugs.get(drugIndex - 1);

                        getContext().getLogger().log(Level.INFO, "Looking backward: " +
                                prevDrug.getCoveredText() +
                                " with drug: " + drug.getCoveredText()
                        );

                        // SCENARIO 2: Ingredient name is followed by brand name
                        if (prevDrug.getBrand() == null) {
                            drugsModified.set(compareIngredientsAndMergeDrugs(jcas, prevDrug, drug));
                        }
                    }
                }
            });
        } while (drugsModified.get());

    }

    private void createLookupWindows(JCas jcas) {
        // always generate a new instance of drug list because the index has been updated
        ImmutableList<Drug> sortedDrugs = ImmutableList.sortedCopyOf(
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
                            .filter(medAttr -> medAttr.getBegin() > drug.getEnd() &&
                                    medAttr.getEnd() < nextDrugBegin)
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
        windows.forEach(TOP::addToIndexes);
    }

    private void associateAttributesAndIngredients(JCas jcas) {
        jcas.getAnnotationIndex(LookupWindow.type).forEach(window ->
                jcas.getAnnotationIndex(Drug.type).subiterator(window).forEachRemaining(annotation -> {

                    Drug drug = (Drug) annotation;

                    // attributes are assumed not to be contained in drug names
                    List<MedAttr> filteredAttributes = attributes.stream()
                            .filter(attribute ->
                                    attribute.getBegin() >= window.getBegin() &&
                                            attribute.getEnd() <= window.getEnd())
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
    private boolean compareIngredientsAndMergeDrugs(JCas jcas, Drug sourceDrug, Drug targetDrug) {
        boolean drugsMerged = false;

        ImmutableList<String> rxCuis = ImmutableList.copyOf(targetDrug.getBrand().split(","));

        Set<Medication> targetMedications = FhirQueryUtils.getMedicationsFromRxCui(allMedications, rxCuis);

        ArrayList<String> candidateIngredients = new ArrayList<>(FhirQueryUtils.getIngredientsFromMedications(targetMedications));

        // don't add ingredients that are already found in the drug
        if (targetDrug.getIngredients() != null) {
            ImmutableList<String> targetIngredients = Streams.stream(targetDrug.getIngredients())
                    .map(Ingredient.class::cast)
                    .map(Ingredient::getItem)
                    .collect(ImmutableList.toImmutableList());

            candidateIngredients.removeAll(targetIngredients);
        }

        if (sourceDrug.getIngredients() != null) {
            ImmutableList<String> sourceIngredients = Streams.stream(sourceDrug.getIngredients())
                    .map(Ingredient.class::cast)
                    .map(Ingredient::getItem)
                    .collect(ImmutableList.toImmutableList());

            if (candidateIngredients.containsAll(sourceIngredients)) {
                mergeDrugs(jcas, sourceDrug, targetDrug);

                getContext().getLogger().log(Level.INFO, "Merged drug: " +
                        sourceDrug.getCoveredText() +
                        " with drug: " + targetDrug.getCoveredText()
                );

                drugsMerged = true;
            }
        }

        return drugsMerged;
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

        ImmutableSet<FeatureStructure> mergedAttributes = null;

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

        ImmutableSet<FeatureStructure> mergedIngredients = null;

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