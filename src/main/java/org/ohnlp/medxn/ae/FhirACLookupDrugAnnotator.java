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
import org.hl7.fhir.r4.model.Coding;
import org.ohnlp.medtagger.type.ConceptMention;
import org.ohnlp.medxn.fhir.FhirQueryClient;
import org.ohnlp.medxn.fhir.FhirQueryUtils;
import org.ohnlp.medxn.type.Drug;
import org.ohnlp.medxn.type.Ingredient;
import org.ohnlp.typesystem.type.textspan.Segment;
import org.ohnlp.typesystem.type.textspan.Sentence;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FhirACLookupDrugAnnotator extends JCasAnnotator_ImplBase {

    private final FhirQueryUtils.LookupTable ingredients = new FhirQueryUtils.LookupTable();
    private final FhirQueryUtils.LookupTable brands = new FhirQueryUtils.LookupTable();
    private FhirQueryClient queryClient;
    private final Pattern punctuationOrWhitespace = Pattern.compile("\\p{Punct}|\\s");
    private final Pattern digitsSlashDigits = Pattern.compile(" \\d+/\\d+");
    private final Pattern doublePunctOrWhitespace = Pattern.compile("(\\p{Punct}|\\s){2}");
    private final Pattern htmlEntities = Pattern.compile("&\\S+;");

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
                    Coding coding = substance.getCode().getCodingFirstRep();

                    ingredients.keywordMap.put(coding.getCode(),
                            coding.getDisplay());

                    substance.getExtensionsByUrl(queryClient.getServerUrl() + "StructureDefinition/synonym")
                            .forEach(synonymExtension ->
                                    ingredients.keywordMap.put(coding.getCode(),
                                            synonymExtension.getValue().toString()
                                                    .replaceAll(punctuationOrWhitespace.toString(), " ")
                                                    .replaceAll(doublePunctOrWhitespace.toString(), " ")
                                    ));
                });

        ingredients.trie = Trie.builder()
                .ignoreCase()
                .ignoreOverlaps()
                .onlyWholeWords()
                .addKeywords(ingredients.keywordMap.values())
                .build();

        getContext().getLogger().log(Level.INFO, "Built ingredient trie using "
                + ingredients.getKeywordSize() + " keywords against "
                + ingredients.getConceptSize() + " concepts."
        );

        queryClient
                .getAllMedications()
                .forEach(medication -> medication
                        .getExtensionsByUrl(queryClient.getServerUrl() + "StructureDefinition/brand")
                        .stream()
                        .findFirst()
                        .ifPresent(brandExtension -> {
                            Coding productCoding = medication.getCode().getCodingFirstRep();

                            String brand = brandExtension.getValue().toString();

                            Matcher brandWithMultipleStrength = digitsSlashDigits.matcher(brand);

                            // enrich keyword map with trade name root without multiple strengths
                            if (brandWithMultipleStrength.find()) {
                                brands.keywordMap.put(productCoding.getCode(),
                                        brandWithMultipleStrength.replaceAll(""));
                            }

                            brands.keywordMap.put(productCoding.getCode(),
                                    brand.replaceAll(punctuationOrWhitespace.toString(), " ")
                                            .replaceAll(doublePunctOrWhitespace.toString(), " ")
                            );
                        })
                );

        brands.trie = Trie.builder()
                .ignoreCase()
                .ignoreOverlaps()
                .onlyWholeWords()
                .addKeywords(brands.keywordMap.values())
                .build();

        getContext().getLogger().log(Level.INFO, "Built brand trie using "
                + brands.getKeywordSize() + " keywords against "
                + brands.getConceptSize() + " concepts."
        );
    }

    @Override
    public void process(JCas jcas) {
        jcas.getAnnotationIndex(Segment.type).forEach(segment ->
                jcas.getAnnotationIndex(Sentence.type).subiterator(segment).forEachRemaining(sentence -> {

                    // note that org.ahocorasick.ahocorasick does not support multiple whitespaces between words
                    String sentText = sentence.getCoveredText().toLowerCase();

                    Matcher httpEntitiesMatcher = htmlEntities.matcher(sentText);

                    while (httpEntitiesMatcher.find()) {
                        String substringToRemove = httpEntitiesMatcher.group(0);
                        sentText = httpEntitiesMatcher.replaceFirst(
                                new String(new char[substringToRemove.length()]).replace("\0", "")
                        );
                    }

                    // replace single punctuations and whitespaces with a single space
                    sentText = sentText.replaceAll(punctuationOrWhitespace.toString(), " ");

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