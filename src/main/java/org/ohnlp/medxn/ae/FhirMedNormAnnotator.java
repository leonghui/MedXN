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
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationKnowledge;
import org.ohnlp.medxn.fhir.FhirQueryClient;
import org.ohnlp.medxn.fhir.FhirQueryFilters;
import org.ohnlp.medxn.fhir.FhirQueryUtils;
import org.ohnlp.medxn.type.Drug;
import org.ohnlp.medxn.type.Ingredient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class FhirMedNormAnnotator extends JCasAnnotator_ImplBase {
    private Map<String, Medication> allMedications;
    private Map<String, MedicationKnowledge> allMedicationKnowledge;
    private String url;

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

            getContext().getLogger().log(Level.INFO, "Found " + candidateMedications.size() +
                    " matches with the same brand/ingredient(s)" +
                    " for drug: " + drug.getCoveredText()
            );

            // Filters for direct matching (normRxCui and normDrugName)
            Set<Medication> validatedSet = queryFilters.getFullyValidatedSet(candidateMedications);

            Set<Medication> formResults = queryFilters.byDoseForm(validatedSet, false);

            Set<Medication> strengthResults = queryFilters.byStrength(validatedSet, false);

            Set<Medication> inferredStrengthResults = queryFilters.byStrengthInference(validatedSet, false);

            // prefer results with matching ingredients
            Set<Medication> strengthOrInferredResults = !strengthResults.isEmpty() ? strengthResults : inferredStrengthResults;

            Set<Medication> formStrengthResults = Sets.intersection(formResults, strengthOrInferredResults);

            Set<Set<Medication>> resultSets = Stream.of(
                    formResults, strengthOrInferredResults, formStrengthResults)
                    .filter(set -> !set.isEmpty())
                    .collect(ImmutableSet.toImmutableSet());

            Set<Medication> uniqueResults = resultSets.stream()
                    .filter(set -> set.size() == 1)
                    .flatMap(Set::stream)
                    .collect(Collectors.toSet());

            if (uniqueResults.size() == 1) {
                Medication medication = uniqueResults.iterator().next();

                Coding medicationCoding = medication.getCode().getCodingFirstRep();

                drug.setNormRxName(medicationCoding.getDisplay());
                drug.setNormRxCui(medicationCoding.getCode());

                getContext().getLogger().log(Level.INFO, "Tagging " +
                        medication.getCode().getCodingFirstRep().getDisplay() + " to drug: " +
                        drug.getCoveredText());
            }


            // Filters for inferred matching (normRxCui2 and normDrugName2)
            Set<Medication> partialValidatedSet = queryFilters.getValidatedSetExceptDf(candidateMedications);

            Set<Medication> strengthResultsWithDf = queryFilters.byStrength(partialValidatedSet, false);

            Set<Medication> inferredStrengthWithDfResults = queryFilters.byStrengthInference(partialValidatedSet, false);

            // prefer results with matching ingredients
            Set<Medication> strengthOrInferredWithDfResults = !strengthResults.isEmpty() ? strengthResultsWithDf : inferredStrengthWithDfResults;

            Set<Medication> routeInferenceResults = queryFilters.byRouteInference(partialValidatedSet, false);

            // prefer results with matching dose forms
            Set<Medication> formOrRouteResults = !formResults.isEmpty() ? formResults : routeInferenceResults;

            Set<Medication> inferredFormStrengthResults = Sets.intersection(formOrRouteResults, strengthOrInferredWithDfResults);

            Set<Set<Medication>> inferredResultSets = Stream.of(
                    strengthOrInferredWithDfResults, formOrRouteResults, inferredFormStrengthResults)
                    .filter(set -> !set.isEmpty())
                    .collect(ImmutableSet.toImmutableSet());

            Set<Medication> uniqueInferredResults = inferredResultSets.stream()
                    .filter(set -> set.size() == 1)
                    .flatMap(Set::stream)
                    .collect(Collectors.toSet());

            if (uniqueInferredResults.size() == 1) {
                Medication medication = uniqueInferredResults.iterator().next();

                Coding medicationCoding = medication.getCode().getCodingFirstRep();

                drug.setNormRxName2(medicationCoding.getDisplay());
                drug.setNormRxCui2(medicationCoding.getCode());

                getContext().getLogger().log(Level.INFO, "Tagging inferred " +
                        medication.getCode().getCodingFirstRep().getDisplay() + " to drug: " +
                        drug.getCoveredText());
            }

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