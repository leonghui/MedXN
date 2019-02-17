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

import org.ahocorasick.trie.Trie;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Substance;
import org.ohnlp.medtagger.type.ConceptMention;
import org.ohnlp.medxn.fhir.FhirQueryClient;
import org.ohnlp.medxn.fhir.FhirQueryUtils;
import org.ohnlp.medxn.type.Drug;
import org.ohnlp.medxn.type.Ingredient;
import org.ohnlp.typesystem.type.textspan.Segment;
import org.ohnlp.typesystem.type.textspan.Sentence;

import java.util.regex.Pattern;

public class FhirACLookupDrugAnnotator extends JCasAnnotator_ImplBase {

    private final FhirQueryUtils.LookupTable ingredients = new FhirQueryUtils.LookupTable();
    private final FhirQueryUtils.LookupTable brands = new FhirQueryUtils.LookupTable();
    private FhirQueryClient queryClient;
    private final Pattern punctuationOrWhitespace = Pattern.compile("\\p{Punct}|\\s");

    @Override
    public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
        super.initialize(uimaContext);

        // Get config parameter values
        String url = (String) uimaContext.getConfigParameterValue("FHIR_SERVER_URL");
        int timeout = (int) uimaContext.getConfigParameterValue("TIMEOUT_SEC");
        queryClient = FhirQueryClient.createFhirQueryClient(url, timeout);

        queryClient
                .getAllSubstances()
                .stream()
                .map(Substance::getCode)
                .map(CodeableConcept::getCodingFirstRep)
                .forEach(coding -> ingredients.keywordMap.put(coding.getCode(), coding.getDisplay().toLowerCase()));

        ingredients.trie = Trie.builder()
                .ignoreCase()
                .ignoreOverlaps()
                .onlyWholeWords()
                .addKeywords(ingredients.keywordMap.values())
                .build();

        queryClient
                .getAllMedications()
                .forEach(medication -> medication
                        .getExtensionsByUrl(queryClient.getFhirServerUrl() + "/StructureDefinition/brand")
                        .stream()
                        .findFirst()
                        .ifPresent(brandExtension -> {
                            Coding productCoding = medication.getCode().getCodingFirstRep();

                            brands.keywordMap.put(productCoding.getCode(),
                                    brandExtension.getValue().toString()
                                            .replaceAll(punctuationOrWhitespace.toString(), " ")
                            );
                        }));

        brands.trie = Trie.builder()
                .ignoreCase()
                .ignoreOverlaps()
                .onlyWholeWords()
                .addKeywords(brands.keywordMap.values())
                .build();

    }

    @Override
    public void process(JCas jcas) {
        jcas.getAnnotationIndex(Segment.type).forEach(segment ->
                jcas.getAnnotationIndex(Sentence.type).subiterator(segment).forEachRemaining(sentence -> {

                    // note that org.ahocorasick.ahocorasick does not support multiple whitespaces between words
                    String sentText = sentence.getCoveredText().toLowerCase()
                            // replace single punctuations and whitespaces with a single space
                            .replaceAll(punctuationOrWhitespace.toString(), " ");

                    // create Drug for each brand found
                    brands.trie.parseText(sentText).forEach(emit -> {
                        int begin = sentence.getBegin() + emit.getStart();
                        int end = sentence.getBegin() + emit.getEnd() + 1;

                        Drug drug = new Drug(jcas, begin, end);
                        String brand = String.join(",",
                                FhirQueryUtils.getAllRxCuisFromKeywordMap(brands.keywordMap, emit.getKeyword()));
                        drug.setBrand(brand);
                        drug.addToIndexes(jcas);

                        getContext().getLogger().log(Level.INFO, "Found brand: " + drug.getCoveredText());
                    });

                    // create ConceptMention and Ingredient for each ingredient found
                    ingredients.trie.parseText(sentText).forEach(emit -> {
                        int begin = sentence.getBegin() + emit.getStart();
                        int end = sentence.getBegin() + emit.getEnd() + 1;

                        Ingredient ingredient = new Ingredient(jcas, begin, end);
                        String rxCui = FhirQueryUtils.getRxCuiFromKeywordMap(ingredients.keywordMap, emit.getKeyword());
                        ingredient.setItem(rxCui);
                        ingredient.addToIndexes(jcas);

                        ConceptMention concept = new ConceptMention(jcas, begin, end);

                        String ingredientTerm = ingredients.keywordMap.get(rxCui)
                                .stream().findFirst().orElse("");

                        concept.setNormTarget(ingredientTerm);
                        concept.setSemGroup(rxCui + "::IN");
                        concept.setSentence((Sentence) sentence);
                        concept.addToIndexes(jcas);

                        Drug drug = new Drug(jcas, begin, end);

                        FSArray ingredientArray = new FSArray(jcas, 1);
                        ingredientArray.set(0, ingredient);
                        drug.setIngredients(ingredientArray);

                        drug.addToIndexes(jcas);

                        getContext().getLogger().log(Level.INFO, "Found ingredient: " + concept.getCoveredText());
                    });
                })
        );
    }
}