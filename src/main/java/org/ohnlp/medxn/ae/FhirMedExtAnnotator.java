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
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceInitializationException;
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

    @Override
    public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
        super.initialize(uimaContext);
    }

    @Override
    public void process(JCas jcas) {
        convertConceptMentions(jcas);
        createLookupWindows(jcas);
        associateAttributesAndIngredients(jcas);
    }

    @SuppressWarnings("UnstableApiUsage")
    private void convertConceptMentions(JCas jcas) {
        List<ConceptMention> conceptMentions = new ArrayList<>();
        jcas.getAnnotationIndex(ConceptMention.type).forEach(annotation -> {
            ConceptMention conceptMention = (ConceptMention) annotation;
            conceptMentions.add(conceptMention);
        });

        List<MedAttr> attributes = new ArrayList<>();
        jcas.getAnnotationIndex(MedAttr.type).forEach(annotation -> {
            MedAttr attribute = (MedAttr) annotation;
            attributes.add(attribute);
        });

        List<Drug> drugs = new ArrayList<>();

        ImmutableList<MedAttr> formsRoutesFrequencies = attributes.stream()
                .filter(attribute -> attribute.getTag().contentEquals(FhirQueryUtils.MedAttrConstants.FORM)
                        ||
                        attribute.getTag().contentEquals(FhirQueryUtils.MedAttrConstants.ROUTE)
                        ||
                        attribute.getTag().equals(FhirQueryUtils.MedAttrConstants.FREQUENCY))
                .collect(ImmutableList.toImmutableList());

        // SCENARIO 1: drugs are always ordered with form or route or frequency
        SetMultimap<MedAttr, ConceptMention> conceptMentionsByNearestMedAttrMap = LinkedHashMultimap.create();

        conceptMentions.forEach(conceptMention -> formsRoutesFrequencies.stream()
                .filter(attribute ->
                        attribute.getBegin() > conceptMention.getEnd())
                .min(Comparator.comparingInt(MedAttr::getBegin))
                .ifPresent(closestAttribute ->
                        conceptMentionsByNearestMedAttrMap.put(closestAttribute, conceptMention)
                ));

        conceptMentionsByNearestMedAttrMap.asMap().forEach((key, value) -> value.stream()
                .min(Comparator.comparingInt(ConceptMention::getBegin))
                .ifPresent(conceptMention -> {
                            Drug drug = new Drug(jcas, conceptMention.getBegin(), conceptMention.getEnd());
                            drugs.add(drug);
                        }
                ));

        drugs.forEach(TOP::addToIndexes);
    }

    @SuppressWarnings("UnstableApiUsage")
    private void createLookupWindows(JCas jcas) {
        List<Sentence> sentences = new ArrayList<>();
        jcas.getAnnotationIndex(Sentence.type).forEach(annotation -> {
            Sentence sentence = (Sentence) annotation;
            sentences.add(sentence);
        });

        // TODO switch to ImmutableList.sortedCopyOf
        ImmutableList<Sentence> sortedSentences = sentences.stream()
                .sorted(Comparator.comparingInt(Sentence::getBegin).thenComparingInt(Sentence::getEnd))
                .collect(ImmutableList.toImmutableList());

        List<Drug> drugs = new ArrayList<>();
        jcas.getAnnotationIndex(Drug.type).forEach(annotation -> {
            Drug drug = (Drug) annotation;
            drugs.add(drug);
        });

        ImmutableList<Drug> sortedDrugs = drugs.stream()
                .sorted(Comparator.comparingInt(Drug::getBegin).thenComparingInt(Drug::getEnd))
                .collect(ImmutableList.toImmutableList());

        IntStream.range(0, sortedSentences.size()).forEach(sentenceIndex -> {
            Sentence sentence = sortedSentences.get(sentenceIndex);

            IntStream.range(0, sortedDrugs.size()).forEach(drugIndex -> {

                Drug drug = sortedDrugs.get(drugIndex);

                // only consider drugs that begin, but not necessarily end, in the same sentence
                if (drug.getBegin() >= sentence.getBegin() && drug.getBegin() <= sentence.getEnd()) {

                    LookupWindow window = new LookupWindow(jcas);

                    window.setBegin(drug.getBegin());

                    // if we have not reached the last drug or last sentence
                    if (drugIndex < sortedDrugs.size() - 1 && sentenceIndex < sortedSentences.size() - 1) {
                        Drug nextDrug = sortedDrugs.get(drugIndex + 1);
                        Sentence nextSentence = sortedSentences.get(sentenceIndex + 1);

                        // end the drug in the next sentence if the next drug begins in further sentences
                        if (nextDrug.getBegin() > nextSentence.getEnd()) {
                            window.setEnd(nextSentence.getEnd());
                        } else {
                            window.setEnd(nextDrug.getBegin() - 1);
                        }
                    } else {
                        window.setEnd(sentence.getEnd());
                    }

                    window.addToIndexes();
                }
            });
        });
    }

    private void associateAttributesAndIngredients(JCas jcas) {

        // TODO switch to ImmutableList.copyOf
        List<MedAttr> attributes = new ArrayList<>();
        jcas.getAnnotationIndex(MedAttr.type).forEach(annotation -> {
            MedAttr attribute = (MedAttr) annotation;
            attributes.add(attribute);
        });

        List<Ingredient> ingredients = new ArrayList<>();
        jcas.getAnnotationIndex(Ingredient.type).forEach(annotation -> {
            Ingredient ingredient = (Ingredient) annotation;
            ingredients.add(ingredient);
        });

        jcas.getAnnotationIndex(LookupWindow.type).forEach(window ->
                jcas.getAnnotationIndex(Drug.type).subiterator(window).forEachRemaining(annotation -> {
                    Drug drug = (Drug) annotation;

                    List<MedAttr> filteredAttributes = attributes.stream()
                            .filter(attribute ->
                                    attribute.getBegin() >= window.getBegin() &&
                                            attribute.getEnd() <= window.getEnd())
                            .collect(Collectors.toList());

                    FSArray attributesArray = new FSArray(jcas, filteredAttributes.size());

                    IntStream.range(0, filteredAttributes.size())
                            .forEach(index ->
                                    attributesArray.set(index, filteredAttributes.get(index))
                            );

                    drug.setAttrs(attributesArray);

                    List<Ingredient> filteredIngredients = ingredients.stream()
                            .filter(ingredient ->
                                    ingredient.getBegin() >= window.getBegin() &&
                                            ingredient.getEnd() <= window.getEnd())
                            .collect(Collectors.toList());

                    FSArray ingredientArray = new FSArray(jcas, filteredIngredients.size());

                    IntStream.range(0, filteredIngredients.size())
                            .forEach(index ->
                                    filteredIngredients.set(index, filteredIngredients.get(index))
                            );

                    drug.setIngredients(ingredientArray);

                })
        );
    }
}
