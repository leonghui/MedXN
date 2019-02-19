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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.ohnlp.medxn.fhir.FhirQueryUtils;
import org.ohnlp.medxn.type.Drug;
import org.ohnlp.medxn.type.Ingredient;
import org.ohnlp.medxn.type.LookupWindow;
import org.ohnlp.medxn.type.MedAttr;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class FhirMedStrengthAnnotator extends JCasAnnotator_ImplBase {
    private final Pattern digitsWithComma = Pattern.compile("(?<!\\S)\\d[\\d,.]*");

    @Override
    public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
        super.initialize(uimaContext);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void process(JCas jcas) {
        jcas.getAnnotationIndex(LookupWindow.type).forEach(window ->
                jcas.getAnnotationIndex(Drug.type).subiterator(window).forEachRemaining(drugAnnotation -> {
                    Drug drug = (Drug) drugAnnotation;

                    ImmutableList<Ingredient> sortedIngredients = ImmutableList.of();

                    if (drug.getIngredients() != null) {
                        sortedIngredients = Streams.stream(drug.getIngredients())
                                .map(Ingredient.class::cast)
                                .sorted(Comparator.comparingInt(Ingredient::getBegin)
                                        .thenComparingInt(Ingredient::getEnd))
                                .collect(ImmutableList.toImmutableList());
                    }

                    ImmutableList<MedAttr> sortedStrengths = ImmutableList.of();

                    if (drug.getAttrs() != null) {
                        sortedStrengths = Streams.stream(drug.getAttrs())
                                .map(MedAttr.class::cast)
                                .filter(attribute ->
                                        attribute.getTag()
                                                .contentEquals(FhirQueryUtils.MedAttrConstants.STRENGTH))
                                .sorted(Comparator.comparingInt(MedAttr::getBegin)
                                        .thenComparingInt(MedAttr::getEnd))
                                .collect(ImmutableList.toImmutableList());
                    }

                    AtomicBoolean assumeIngredientStrengthPairs = new AtomicBoolean(
                            !sortedIngredients.isEmpty() &&
                                    sortedIngredients.size() == sortedStrengths.size());

                    if (assumeIngredientStrengthPairs.get()) {
                        getContext().getLogger().log(Level.INFO, "Assuming ingredient-strength pairs for:"
                                + drug.getCoveredText());
                    }

                    sortedStrengths.forEach(strength -> {

                        Matcher matcher = digitsWithComma.matcher(strength.getCoveredText());

                        if (matcher.find()) {

                            double value = Double.parseDouble(matcher.group(0).replaceAll(",", ""));
                            String unit = matcher.replaceFirst("").trim();

                            // if there are no ingredients, create a new array of size 1 to hold the strength
                            // resize the array if there are additional strengths later
                            if (drug.getIngredients() == null) {
                                FSArray newIngredientArray = new FSArray(jcas, 1);
                                Ingredient newIngredient = new Ingredient(jcas, drug.getBegin(), drug.getEnd());

                                newIngredient.setAmountValue(value);
                                newIngredient.setAmountUnit(unit);
                                newIngredient.setItem(""); // avoid null ingredients

                                newIngredientArray.set(0, newIngredient);

                                drug.setIngredients(newIngredientArray);

                                getContext().getLogger().log(Level.INFO, "Associating first strength: " +
                                        value + " : " + unit +
                                        " with drug: " + drug.getCoveredText()
                                );

                            } else {
                                Optional<Ingredient> matchedIngredient;

                                Stream<Ingredient> candidateIngredients = Streams.stream(drug.getIngredients())
                                        .map(Ingredient.class::cast)
                                        .filter(ingredient -> ingredient.getEnd() < strength.getBegin())
                                        .filter(ingredient -> ingredient.getAmountUnit() == null
                                                || ingredient.getAmountValue() == 0.0);

                                if (!assumeIngredientStrengthPairs.get()) {
                                    // SCENARIO 1: drug strengths are always written after drug names
                                    // look backward and assign strength to the closest ingredient
                                    matchedIngredient = candidateIngredients
                                            .max(Comparator.comparingInt(Ingredient::getEnd));
                                } else {
                                    // SCENARIO 2: assuming ingredient-strength pairs
                                    // look backward and assign strength to the furthest ingredient
                                    matchedIngredient = candidateIngredients
                                            .min(Comparator.comparingInt(Ingredient::getEnd));
                                }

                                if (matchedIngredient.isPresent()) {
                                    matchedIngredient.get().setAmountValue(value);
                                    matchedIngredient.get().setAmountUnit(unit);

                                    getContext().getLogger().log(Level.INFO, "Associating matched strength: " +
                                            value + " : " + unit +
                                            " with ingredient: " + matchedIngredient.get().getCoveredText() +
                                            " for drug: " + drug.getCoveredText()
                                    );
                                } else {
                                    Ingredient newIngredient = new Ingredient(jcas, drug.getBegin(), drug.getEnd());
                                    newIngredient.setAmountValue(value);
                                    newIngredient.setAmountUnit(unit);
                                    newIngredient.setItem(""); // avoid null ingredients

                                    int arraySize = drug.getIngredients().size();

                                    // resize internal array
                                    FSArray newArray = new FSArray(jcas, arraySize + 1);
                                    newArray.copyFromArray(
                                            drug.getIngredients().toArray(), 0, 0, arraySize);
                                    newArray.set(arraySize, newIngredient);

                                    drug.setIngredients(newArray);

                                    getContext().getLogger().log(Level.INFO, "Associating additional strength: " +
                                            value + " : " + unit +
                                            " with drug: " + drug.getCoveredText()
                                    );
                                }
                            }
                        }
                    });
                })
        );
    }
}