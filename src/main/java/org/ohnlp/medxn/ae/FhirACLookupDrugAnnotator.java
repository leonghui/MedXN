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

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import org.ahocorasick.trie.Trie;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Medication;
import org.ohnlp.medxn.fhir.FhirQueryClient;
import org.ohnlp.medxn.fhir.FhirQueryUtils;
import org.ohnlp.medxn.type.Drug;
import org.ohnlp.medxn.type.Ingredient;
import org.ohnlp.typesystem.type.textspan.Segment;
import org.ohnlp.typesystem.type.textspan.Sentence;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FhirACLookupDrugAnnotator extends JCasAnnotator_ImplBase {

    private final LookupTable ingredients = new LookupTable();
    private final LookupTable brands = new LookupTable();
    private FhirQueryClient queryClient;
    private final Pattern punctuationsOrWhitespaces = Pattern.compile("((\\p{Punct}|\\s)+)");

    @Override
    public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
        super.initialize(uimaContext);

        // Get config parameter values
        String url = (String) uimaContext.getConfigParameterValue("FHIR_SERVER_URL");
        int timeout = (int) uimaContext.getConfigParameterValue("TIMEOUT_SEC");
        queryClient = FhirQueryClient.createFhirQueryClient(url, timeout);

        queryClient
                .getAllSubstances()
                .forEach(substance -> {
                    Coding substanceCode = substance.getCode().getCodingFirstRep();
                    ingredients.keywordMap.put(substanceCode.getCode(), substanceCode.getDisplay().toLowerCase());
                });

        ingredients.trie = Trie.builder()
                .ignoreCase()
                .ignoreOverlaps()
                .onlyWholeWords()
                .addKeywords(ingredients.keywordMap.values())
                .build();

        queryClient
                .getAllMedications()
                .forEach(medication -> {
                    Extension brandExtension = medication
                            .getExtensionsByUrl(queryClient.getFhirServerUrl() + "/StructureDefinition/brand")
                            .stream()
                            .findFirst()
                            .orElse(null);
                    Coding productCoding = medication.getCode().getCodingFirstRep();

                    if (brandExtension != null) {
                        brands.keywordMap.put(productCoding.getCode(),
                                brandExtension.getValue().toString()
                                        .replaceAll(punctuationsOrWhitespaces.toString(), " ")
                                        .trim()
                        );
                    }
                });

        brands.trie = Trie.builder()
                .ignoreCase()
                .ignoreOverlaps()
                .onlyWholeWords()
                .addKeywords(brands.keywordMap.values())
                .build();

    }

    @Override
    public void process(JCas jcas) {
        jcas.getAnnotationIndex(Segment.type).forEach(segment -> {
            jcas.getAnnotationIndex(Sentence.type).subiterator(segment).forEachRemaining(sentence -> {

                // note that org.ahocorasick.ahocorasick does not support multiple whitespaces between words
                String sentText = sentence.getCoveredText().toLowerCase()
                        // replace single punctuations and whitespaces with a single space
                        .replaceAll(punctuationsOrWhitespaces.toString(), " ")
                        .trim();

                List<Drug> drugs = new ArrayList<>();
                List<Ingredient> ingredientsMentioned = new ArrayList<>();
                List<Ingredient> ingredientsTagged = new ArrayList<>();

                // assume every brand mentioned refers to a drug
                brands.trie.parseText(sentText).forEach(emit -> {
                    int begin = sentence.getBegin() + emit.getStart();
                    int end = sentence.getBegin() + emit.getEnd() + 1;

                    Drug drug = new Drug(jcas, begin, end);
                    drug.setBrand(emit.getKeyword());
                    drugs.add(drug);

                    getContext().getLogger().log(Level.INFO, "Found brand: " + drug.getCoveredText());
                });

                // create type for each ingredient mentioned
                ingredients.trie.parseText(sentText).forEach(emit -> {
                    int begin = sentence.getBegin() + emit.getStart();
                    int end = sentence.getBegin() + emit.getEnd() + 1;

                    Ingredient ingredient = new Ingredient(jcas, begin, end);
                    String rxCui = FhirQueryUtils.getRxCuiFromKeywordMap(ingredients.keywordMap, emit.getKeyword());
                    ingredient.setItem(rxCui);
                    ingredientsMentioned.add(ingredient);
                });

                // associate ingredients with brands if they are found in Medication resources
                drugs.forEach(drug -> {
                    Set<String> productRxCuis = FhirQueryUtils
                            .getAllRxCuisFromKeywordMap(brands.keywordMap, drug.getBrand());

                    Set<Medication> products = FhirQueryUtils
                            .getMedicationsFromRxCui(queryClient.getAllMedications(), productRxCuis);

                    Set<String> productIngredients = FhirQueryUtils.getIngredientsFromMedications(products);

                    List<Ingredient> matchingIngredients = ingredientsMentioned.stream()
                            .filter(ingredient ->
                                    productIngredients.contains(ingredient.getItem()))
                            .sorted(Comparator.comparingInt(Ingredient::getBegin))
                            .collect(Collectors.toList());

                    ingredientsTagged.addAll(matchingIngredients);

                    if (!ingredientsTagged.isEmpty()) {

                        FSArray ingredientArray = new FSArray(jcas, matchingIngredients.size());

                        IntStream.range(0, matchingIngredients.size())
                                .forEach(index -> ingredientArray.set(index, matchingIngredients.get(index)));

                        drug.setIngredients(ingredientArray);

                        getContext().getLogger().log(Level.INFO, "Associating ingredients: " +
                                FhirQueryUtils.getCoveredTextFromAnnotations(matchingIngredients) +
                                " with brand: " + drug.getCoveredText()
                        );
                    }
                });

                // if there are remaining ingredients, assume that they belong to the same drug
                ingredientsMentioned.removeAll(ingredientsTagged);

                if (!ingredientsMentioned.isEmpty()) {

                    //  TODO: Compare all combinations of ingredients against FHIR Medication resources
                    //  Set<Set<Ingredient>> ingredientSets = Sets.powerSet(new LinkedHashSet<>(ingredientsMentioned));

                    FSArray ingredientArray = new FSArray(jcas, ingredientsMentioned.size());

                    IntStream.range(0, ingredientsMentioned.size())
                            .forEach(index -> ingredientArray.set(index, ingredientsMentioned.get(index)));

                    int begin = ingredientsMentioned.stream()
                            .map(Ingredient::getBegin)
                            .min(Integer::compare)
                            .orElse(0);

                    int end = ingredientsMentioned.stream()
                            .map(Ingredient::getEnd)
                            .max(Integer::compare)
                            .orElse(sentence.getEnd());

                    Drug drug = new Drug(jcas, begin, end);

                    drug.setIngredients(ingredientArray);

                    drugs.add(drug);

                    getContext().getLogger().log(Level.INFO, "Found ingredients: " +
                            FhirQueryUtils.getCoveredTextFromAnnotations(ingredientsMentioned));

                }

                drugs.forEach(TOP::addToIndexes);
            });
        });
    }

    class LookupTable {
        // Data structure to store keywords
        // rxCui, keyword
        private final SetMultimap<String, String> keywordMap = LinkedHashMultimap.create();

        // Data structure to store the trie
        private Trie trie;
    }
}