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
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.ohnlp.medtagger.type.ConceptMention;
import org.ohnlp.medxn.fhir.FhirQueryUtils;
import org.ohnlp.medxn.type.Drug;
import org.ohnlp.medxn.type.Ingredient;
import org.ohnlp.medxn.type.LookupWindow;
import org.ohnlp.medxn.type.MedAttr;
import org.ohnlp.typesystem.type.textspan.Sentence;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FhirMedExtAnnotator extends JCasAnnotator_ImplBase {
    private ImmutableList<ConceptMention> concepts;
    private ImmutableList<MedAttr> attributes;
    private ImmutableList<Ingredient> ingredients;
    private ImmutableList<Sentence> sortedSentences;
    private ImmutableList<MedAttr> formsRoutesFrequencies;

    @Override
    public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
        super.initialize(uimaContext);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void process(JCas jcas) {
        concepts = ImmutableList.copyOf(jcas.getAnnotationIndex(ConceptMention.type));
        attributes = ImmutableList.copyOf(jcas.getAnnotationIndex(MedAttr.type));
        ingredients = ImmutableList.copyOf(jcas.getAnnotationIndex(Ingredient.type));
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
                .collect(ImmutableList.toImmutableList());

        convertConceptMentions(jcas);
        createLookupWindows(jcas);
        associateAttributesAndIngredients(jcas);
    }

    private void convertConceptMentions(JCas jcas) {
        List<Drug> drugs = new ArrayList<>();

        // TODO merge concepts and brands

        // SCENARIO 1: if ingredients are ordered with form or route or frequency towards the end (terminator)
        SetMultimap<MedAttr, ConceptMention> conceptsByClosestTerminator = LinkedHashMultimap.create();

        concepts.forEach(concept -> formsRoutesFrequencies.stream()
                .filter(attribute ->
                        attribute.getBegin() > concept.getEnd())
                .min(Comparator.comparingInt(MedAttr::getBegin))
                .ifPresent(closestAttribute ->
                        conceptsByClosestTerminator.put(closestAttribute, concept)
                ));

        // SCENARIO 1.1: prioritize the ingredient closest to the terminator
        conceptsByClosestTerminator.asMap().forEach((key, value) -> value.stream()
                .max(Comparator.comparingInt(ConceptMention::getBegin))
                .ifPresent(concept -> {
                            Drug drug = new Drug(jcas, concept.getBegin(), concept.getEnd());
                            drugs.add(drug);
                        }
                ));

        // SCENARIO 2: ingredients ordered without terminators are standalone drugs
        IntStream.range(0, concepts.size()).forEach(index -> {
            ConceptMention concept = concepts.get(index);

            if (index < concepts.size() - 1) {
                ConceptMention nextConcept = concepts.get(index + 1);

                boolean terminatorFound = formsRoutesFrequencies.stream().anyMatch(attribute ->
                        attribute.getBegin() > concept.getEnd() &&
                                attribute.getEnd() < nextConcept.getBegin());

                if (!terminatorFound) {
                    Drug drug = new Drug(jcas, concept.getBegin(), concept.getEnd());
                    drugs.add(drug);
                }
            }
        });

        drugs.forEach(TOP::addToIndexes);
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

                    int nextDrugBegin = drugIndex < sortedDrugs.size() - 1 ?
                            sortedDrugs.get(drugIndex + 1).getBegin() : Integer.MAX_VALUE;

                    int nextSentenceEnd = sentenceIndex < sortedSentences.size() - 1 ?
                            sortedSentences.get(sentenceIndex + 1).getEnd() : sentence.getEnd();

                    int windowEnd = nextDrugBegin < nextSentenceEnd ? nextDrugBegin : nextSentenceEnd;

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

                                IntStream.range(0, filteredAttributes.size())
                                        .forEach(index ->
                                                attributesArray.set(index, filteredAttributes.get(index))
                                        );

                    drug.setAttrs(attributesArray);

                    getContext().getLogger().log(Level.INFO, "Associating attributes: " +
                            FhirQueryUtils.getCoveredTextFromAnnotations(filteredAttributes) +
                            " with drug: " + drug.getCoveredText()
                    );

                    // ingredients are assumed to be contained in drug names
                    List<Ingredient> filteredIngredients = ingredients.stream()
                            .filter(ingredient ->
                                    ingredient.getBegin() >= drug.getBegin() &&
                                            ingredient.getEnd() <= window.getEnd())
                            .collect(Collectors.toList());

                    if (filteredIngredients.size() > 0) {

                        FSArray ingredientArray = new FSArray(jcas, filteredIngredients.size());

                        IntStream.range(0, filteredIngredients.size())
                                .forEach(index ->
                                        ingredientArray.set(index, filteredIngredients.get(index))
                                );

                        drug.setIngredients(ingredientArray);

                        getContext().getLogger().log(Level.INFO, "Associating ingredients: " +
                                FhirQueryUtils.getCoveredTextFromAnnotations(filteredIngredients) +
                                " with drug: " + drug.getCoveredText()
                        );
                    }
                })
        );
    }
}
