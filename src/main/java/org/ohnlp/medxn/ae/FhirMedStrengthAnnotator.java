/*******************************************************************************
 * Copyright (c) 2018-2019. Leong Hui Wong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.ohnlp.medxn.ae;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.ohnlp.medxn.fhir.FhirQueryUtils;
import org.ohnlp.medxn.type.Drug;
import org.ohnlp.medxn.type.Ingredient;
import org.ohnlp.medxn.type.MedAttr;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class FhirMedStrengthAnnotator extends JCasAnnotator_ImplBase {
    private final Pattern digitsWithComma = Pattern.compile("(?<!\\S)\\d[\\d,.]*");

    @Override
    public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
        super.initialize(uimaContext);
    }

    @Override
    public void process(JCas jcas) {
        jcas.getAnnotationIndex(Drug.type).forEach(annotation -> {
            Drug drug = (Drug) annotation;
            FSArray attributeArray = drug.getAttrs();

            List<MedAttr> strengths = new ArrayList<>();

            attributeArray.forEach(featureStructure -> {
                MedAttr attribute = (MedAttr) featureStructure;
                if (FhirQueryUtils.MedAttrConstants.STRENGTH.contentEquals(attribute.getTag())) {
                    strengths.add(attribute);
                }
            });

            FSArray ingredientArray = drug.getIngredients();

            if (ingredientArray != null && ingredientArray.size() >= 1) {

                List<Ingredient> ingredients = new ArrayList<>();

                ingredientArray.forEach(featureStructure -> ingredients.add((Ingredient) featureStructure));

                ingredients.forEach(ingredient -> strengths.stream()
                        .filter(strength ->
                                strength.getBegin() > ingredient.getEnd())
                        .min(Comparator.comparingInt(MedAttr::getBegin))
                        .ifPresent(closestStrength -> {
                            Matcher matcher = digitsWithComma.matcher(closestStrength.getCoveredText());

                            if (matcher.find()) {
                                ingredient.setAmountValue(Double
                                        .parseDouble(matcher.group(0)
                                                .replaceAll(",", "")));
                                ingredient.setAmountUnit(matcher.replaceFirst("").trim());
                            }
                        }));
            } else {
                FSArray newIngredientArray = new FSArray(jcas, strengths.size());

                IntStream.range(0, strengths.size()).forEach(index -> {

                    MedAttr strength = strengths.get(index);
                    Ingredient newIngredient = new Ingredient(jcas);
                    Matcher matcher = digitsWithComma.matcher(strength.getCoveredText());

                    if (matcher.find()) {
                        newIngredient.setAmountValue(Double
                                .parseDouble(matcher.group(0)
                                        .replaceAll(",", "")));
                        newIngredient.setAmountUnit(matcher.replaceFirst("").trim());

                        newIngredientArray.set(index, newIngredient);
                    }
                });

                drug.setIngredients(newIngredientArray);
            }
        });
    }
}