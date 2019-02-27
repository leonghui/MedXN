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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationKnowledge;
import org.ohnlp.medxn.fhir.FhirQueryClient;
import org.ohnlp.medxn.fhir.FhirQueryFilters;
import org.ohnlp.medxn.fhir.FhirQueryUtils;
import org.ohnlp.medxn.type.Drug;
import org.ohnlp.medxn.type.Ingredient;

import java.util.*;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class FhirMedNormAnnotator extends JCasAnnotator_ImplBase {
    private Map<String, Medication> allMedications;
    private Map<String, MedicationKnowledge> allMedicationKnowledge;
    private String url;
    private Comparator<Medication> byDisplayLength = Comparator.comparingInt((Medication m) ->
            m.getCode().getCodingFirstRep().getDisplay().length());

    @Override
    public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
        super.initialize(uimaContext);

        // Get config parameter values
        url = (String) uimaContext.getConfigParameterValue("FHIR_SERVER_URL");
        int timeout = (int) uimaContext.getConfigParameterValue("TIMEOUT_SEC");
        FhirQueryClient queryClient = FhirQueryClient.createFhirQueryClient(url, timeout);

        allMedications = queryClient.getAllMedications();
        allMedicationKnowledge = queryClient.getAllMedicationKnowledge();
    }

    @Override
    public void process(JCas jcas) {
        jcas.getAnnotationIndex(Drug.type).forEach(drugAnnotation -> {

            Drug drug = (Drug) drugAnnotation;

            FhirQueryFilters queryFilters = FhirQueryFilters.createQueryFilter(getContext(), jcas, drug, allMedications, allMedicationKnowledge);

            String brand = Optional.ofNullable(drug.getBrand()).orElse("");

            Set<String> candidateBrands = Strings.isNullOrEmpty(brand) ? ImmutableSet.of() : brand.contains(",") ? ImmutableSet.copyOf(brand.split(",")) : ImmutableSet.of(brand);

            Set<Medication> candidateMedications = candidateBrands.isEmpty() ? findGenericMedications(jcas, drug) :
                    FhirQueryUtils.getMedicationsFromCode(allMedications, candidateBrands);

            Set<Medication> initialResults = queryFilters.getValidatedSet(candidateMedications);

            getContext().getLogger().log(Level.INFO, "Found " + initialResults.size() +
                    " matches with the same brand/ingredient(s)" +
                    " for drug: " + drug.getCoveredText()
            );

            Set<Medication> formResults = queryFilters.byDoseForm(initialResults, false);

            Set<Medication> strengthResults = queryFilters.byStrength(initialResults, false);

            Set<Medication> routeInferenceResults = queryFilters.byRouteInference(initialResults, false);

            Set<Medication> strengthInferenceResults = queryFilters.byStrengthInference(initialResults, false);

            // prefer results with dose form match
            Set<Medication> formOrRouteResults = !formResults.isEmpty() ? formResults : routeInferenceResults;

            // prefer results with paired ingredients match
            Set<Medication> strengthOrInferredResults = !strengthResults.isEmpty() ? strengthResults : strengthInferenceResults;

            Set<Set<Medication>> resultSets = Stream.of(
                    initialResults, formOrRouteResults, strengthOrInferredResults)
                    .filter(set -> !set.isEmpty())
                    .collect(ImmutableSet.toImmutableSet());

            Set<Medication> uniqueResults = new HashSet<>();

            Set<Set<Medication>> remainder = new HashSet<>();

            if (resultSets.size() == 1) {
                Set<Medication> finalSet = resultSets.iterator().next();
                if (finalSet.size() == 1) {
                    uniqueResults.add(finalSet.iterator().next());
                } else {
                    remainder.add(finalSet);
                }
            } else {
                for (int i = 2; i <= resultSets.size(); i++) {

                    Sets.combinations(resultSets, i).forEach(set -> {
                        List<Set<Medication>> lists = ImmutableList.copyOf(set);

                        Set<Medication> intersect = lists.get(0);

                        for (int j = 1; j < lists.size(); j++) {
                            intersect = Sets.intersection(lists.get(j), intersect);
                        }

                        if (intersect.size() == 1) {
                            uniqueResults.add(intersect.iterator().next());
                        } else {
                            remainder.add(lists.get(0));
                            remainder.add(lists.get(1));
                        }
                    });
                }
            }

            Set<Medication> allRemainders = remainder.stream()
                    .flatMap(Collection::stream)
                    .collect(ImmutableSet.toImmutableSet());

            if (uniqueResults.size() == 1) {
                Medication medication = uniqueResults.iterator().next();

                drug.setNormRxName(medication.getCode().getCodingFirstRep().getDisplay());
                drug.setNormRxCui(medication.getCode().getCodingFirstRep().getCode());

                getContext().getLogger().log(Level.INFO, "Tagging " +
                        medication.getCode().getCodingFirstRep().getDisplay() + " to drug: " +
                        drug.getCoveredText()
                );

            } else {
                remainder.add(uniqueResults);
                getContext().getLogger().log(Level.INFO, "Remaining matches for drug: " +
                        drug.getCoveredText() + " : " +
                        FhirQueryUtils.getDisplayNameFromMedications(allRemainders)
                );
            }


            Set<Medication> uniqueInferredResults = new HashSet<>();

            Set<Set<Medication>> inferredRemainder = new HashSet<>();

            Set<Set<Medication>> inferenceResultSets = resultSets.stream()
                    .map(set -> queryFilters.byAssociationInference(set, false))
                    .filter(set -> !set.isEmpty())
                    .collect(ImmutableSet.toImmutableSet());

            if (inferenceResultSets.size() == 1) {
                Set<Medication> finalSet = inferenceResultSets.iterator().next();
                if (finalSet.size() == 1) {
                    uniqueInferredResults.add(finalSet.iterator().next());
                } else {
                    inferredRemainder.add(finalSet);
                }
            } else {
                for (int i = 2; i <= inferenceResultSets.size(); i++) {

                    Sets.combinations(inferenceResultSets, i).forEach(set -> {
                        List<Set<Medication>> lists = ImmutableList.copyOf(set);

                        Set<Medication> intersect = ImmutableSet.of();

                        for (int j = 0; j < lists.size(); j++) {
                            intersect = Sets.intersection(lists.get(j), lists.get(j + 1));
                        }

                        if (intersect.size() == 1) {
                            uniqueInferredResults.add(intersect.iterator().next());
                        } else {
                            inferredRemainder.add(lists.get(0));
                            inferredRemainder.add(lists.get(1));
                        }
                    });
                }
            }

            Set<Medication> allInferredRemainders = inferredRemainder.stream()
                    .flatMap(Collection::stream)
                    .collect(ImmutableSet.toImmutableSet());

            if (uniqueInferredResults.size() == 1) {
                Medication medication = uniqueInferredResults.iterator().next();

                drug.setNormRxName2(medication.getCode().getCodingFirstRep().getDisplay());
                drug.setNormRxCui2(medication.getCode().getCodingFirstRep().getCode());

                getContext().getLogger().log(Level.INFO, "Tagging inferred " +
                        medication.getCode().getCodingFirstRep().getDisplay() + " to drug: " +
                        drug.getCoveredText()
                );

            } else {
                inferredRemainder.add(uniqueInferredResults);
                getContext().getLogger().log(Level.INFO, "Remaining inferred matches for drug: " +
                        drug.getCoveredText() + " : " +
                        FhirQueryUtils.getDisplayNameFromMedications(allInferredRemainders)
                );
            }
        });
    }

    @SuppressWarnings("UnstableApiUsage")
    private Set<Medication> findGenericMedications(JCas jcas, Drug drug) {

        FSArray ingredientArray = Optional.ofNullable(drug.getIngredients()).orElse(new FSArray(jcas, 0));

        List<String> annotationIngredientIds = Streams.stream(ingredientArray)
                .map(Ingredient.class::cast)
                .map(Ingredient::getItem)
                .collect(ImmutableList.toImmutableList());

        // CRITERION 1: Consider all medications with the same ingredients

        return allMedications.values().parallelStream()
                .filter(medication -> medication.getExtensionsByUrl(
                        url + "StructureDefinition/brand"
                ).isEmpty())
                .filter(medication -> {
                    List<String> fhirIngredients = FhirQueryUtils.getIngredientIdsFromMedication(medication);

                    return fhirIngredients.size() == annotationIngredientIds.size() &&
                            fhirIngredients.containsAll(annotationIngredientIds);
                })
                .collect(ImmutableSet.toImmutableSet());
    }
}