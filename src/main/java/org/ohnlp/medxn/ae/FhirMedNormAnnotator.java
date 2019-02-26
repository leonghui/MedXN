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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationKnowledge;
import org.hl7.fhir.r4.model.Reference;
import org.ohnlp.medxn.fhir.FhirQueryClient;
import org.ohnlp.medxn.fhir.FhirQueryUtils;
import org.ohnlp.medxn.type.Drug;
import org.ohnlp.medxn.type.Ingredient;
import org.ohnlp.medxn.type.LookupWindow;
import org.ohnlp.medxn.type.MedAttr;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class FhirMedNormAnnotator extends JCasAnnotator_ImplBase {
    private List<Medication> allMedications;
    private List<MedicationKnowledge> allMedicationKnowledge;
    private FhirQueryClient queryClient;

    @Override
    public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
        super.initialize(uimaContext);

        // Get config parameter values
        String url = (String) uimaContext.getConfigParameterValue("FHIR_SERVER_URL");
        int timeout = (int) uimaContext.getConfigParameterValue("TIMEOUT_SEC");
        queryClient = FhirQueryClient.createFhirQueryClient(url, timeout);

        allMedications = queryClient.getAllMedications();
        allMedicationKnowledge = queryClient.getAllMedicationKnowledge();
    }

    @Override
    public void process(JCas jcas) {

        jcas.getAnnotationIndex(LookupWindow.type).forEach(window -> {
            jcas.getAnnotationIndex(Drug.type).subiterator(window).forEachRemaining(drugAnnotation -> {
                Drug drug = (Drug) drugAnnotation;

                Set<Medication> candidateMedications;

                if (drug.getBrand() != null) {
                    Set<String> candidateBrands = ImmutableSet.copyOf(drug.getBrand().split(","));

                    candidateMedications = FhirQueryUtils.getMedicationsFromRxCui(allMedications, candidateBrands);
                } else {
                    candidateMedications = findGenericMedications(jcas, drug);
                }

                getContext().getLogger().log(Level.INFO, "Found " +
                        candidateMedications.size() + " matches with the same ingredient(s) for drug: " +
                        drug.getCoveredText()
                );

                Set<Medication> results = candidateMedications;

                if (results.size() == 1) {
                    Medication medication = results.iterator().next();

                    drug.setNormRxName2(medication.getCode().getCodingFirstRep().getDisplay());
                    drug.setNormRxCui2(medication.getCode().getCodingFirstRep().getCode());
                } else {
                    results = filterByDoseFormOrRoute(jcas, drug, candidateMedications);

                    // TODO introduce recursion later
                    if (results.size() == 1) {
                        Medication medication = results.iterator().next();

                        drug.setNormRxName2(medication.getCode().getCodingFirstRep().getDisplay());
                        drug.setNormRxCui2(medication.getCode().getCodingFirstRep().getCode());
                    } else {
                        results = filterByStrength(jcas, drug, results);

                        if (results.size() == 1) {
                            Medication medication = results.iterator().next();

                            drug.setNormRxName2(medication.getCode().getCodingFirstRep().getDisplay());
                            drug.setNormRxCui2(medication.getCode().getCodingFirstRep().getCode());
                        }
                    }
                }
            });
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

        return allMedications.parallelStream()
                .filter(medication -> medication.getExtensionsByUrl(
                        queryClient.getServerUrl() + "StructureDefinition/brand"
                ).isEmpty())
                .filter(medication -> {
                    List<String> fhirIngredients = FhirQueryUtils.getIngredientIdsFromMedication(medication);

                    return fhirIngredients.size() == annotationIngredientIds.size() &&
                            fhirIngredients.containsAll(annotationIngredientIds);
                })
                .collect(ImmutableSet.toImmutableSet());
    }

    @SuppressWarnings("UnstableApiUsage")
    private Set<Medication> filterByDoseFormOrRoute(JCas jcas, Drug drug, Set<Medication> medications) {

        // CRITERION 2a: Include only medications with the same dose form
        String doseForm = Optional.ofNullable(drug.getForm()).orElse("");

        Set<Medication> fhirMedicationsDoseForm = medications.stream()
                .filter(medication -> medication.getForm().getCodingFirstRep().getCode() != null)
                .filter(medication ->
                        doseForm.contentEquals(medication
                                .getForm()
                                .getCodingFirstRep()
                                .getCode()
                        )
                )
                .collect(ImmutableSet.toImmutableSet());

        getContext().getLogger().log(Level.INFO, "Found " +
                fhirMedicationsDoseForm.size() + " matches with the same dose form for drug: " +
                drug.getCoveredText()
        );

        // CRITERION 2b: Include only medications with the same route, if dose form is not found or unavailable
        FSArray attributeArray = Optional.ofNullable(drug.getAttrs()).orElse(new FSArray(jcas, 0));

        List<MedAttr> routes =
                Streams.stream(attributeArray)
                        .map(MedAttr.class::cast)
                        .filter(attribute -> attribute.getTag().contentEquals(FhirQueryUtils.MedAttrConstants.ROUTE))
                        .collect(ImmutableList.toImmutableList());

        Set<Medication> fhirMedicationsRoute = routes.stream()
                .flatMap(route -> medications.stream()
                        .filter(medication -> medication.getForm().getCodingFirstRep().getCode() != null)
                        .filter(medication -> {
                            String routeNormText;

                            // route normalization adapted from FhirACLookupDrugFormAnnotator
                            switch (route.getCoveredText().toLowerCase()) {
                                case "mouth":
                                case "oral":
                                case "orally":
                                case "po":
                                case "p.o.":
                                    routeNormText = "oral";
                                    break;
                                case "vaginally":
                                case "pv":
                                    routeNormText = "vaginal";
                                    break;
                                case "rectally":
                                case "anally":
                                case "pr":
                                case "p.r.":
                                    routeNormText = "rectal";
                                    break;
                                case "skin":
                                case "topical":
                                case "topically":
                                    routeNormText = "topical";
                                    break;
                                default:
                                    routeNormText = route.getCoveredText().toLowerCase();
                                    break;
                            }

                            return medication
                                    .getForm()
                                    .getCodingFirstRep()
                                    .getDisplay()
                                    .toLowerCase()
                                    .contains(routeNormText);
                        })
                )
                .collect(ImmutableSet.toImmutableSet());

        getContext().getLogger().log(Level.INFO, "Found " +
                fhirMedicationsRoute.size() + " matches with the same route for drug: " +
                drug.getCoveredText()
        );

        // prefer concepts with dose form
        return !fhirMedicationsDoseForm.isEmpty() ? fhirMedicationsDoseForm :
                !fhirMedicationsRoute.isEmpty() ? fhirMedicationsRoute :
                        medications;
    }

    @SuppressWarnings("UnstableApiUsage")
    private Set<Medication> filterByStrength(JCas jcas, Drug drug, Set<Medication> medications) {

        FSArray ingredientArray = Optional.ofNullable(drug.getIngredients()).orElse(new FSArray(jcas, 0));

        List<IngredientCommons> annotationIngredients = Streams.stream(ingredientArray)
                .map(Ingredient.class::cast)
                .map(IngredientCommons::new)
                .collect(ImmutableList.toImmutableList());

        // CRITERION 3a: Include only medications with the same ingredient-strength pairs
        Set<Medication> fhirMedicationsStrength = medications.stream()
                .filter(medication ->
                        medication.getIngredient().stream()
                                .map(Medication.MedicationIngredientComponent.class::cast)
                                .map(MedicationIngredientComponentCommons::new)
                                .allMatch(component ->
                                        annotationIngredients.stream().allMatch(ingredient ->
                                                component.getItemCode().contentEquals(ingredient.getItemCode()) &&
                                                        component.getStrengthNumeratorValue().equals(
                                                                ingredient.getStrengthNumeratorValue()) &&
                                                        component.getStrengthNumeratorUnit().contentEquals(
                                                                ingredient.getStrengthNumeratorUnit())
                                        )
                                )
                )
                .collect(ImmutableSet.toImmutableSet());

        getContext().getLogger().log(Level.INFO, "Found " +
                fhirMedicationsStrength.size() + " matches with the same ingredient-strength pairs for drug: " +
                drug.getCoveredText()
        );


        // CRITERION 3b: Include only medications with the same strengths, if ingredient is not found
        Set<Medication> fhirMedicationsAnonStrength = medications.stream()
                .filter(medication ->
                        medication.getIngredient().stream()
                                .map(Medication.MedicationIngredientComponent.class::cast)
                                .map(MedicationIngredientComponentCommons::new)
                                .allMatch(component ->
                                        annotationIngredients.stream().allMatch(ingredient ->
                                                component.getStrengthNumeratorValue().equals(
                                                        ingredient.getStrengthNumeratorValue()) &&
                                                        component.getStrengthNumeratorUnit().contentEquals(
                                                                ingredient.getStrengthNumeratorUnit())
                                        )
                                )
                )
                .collect(ImmutableSet.toImmutableSet());

        getContext().getLogger().log(Level.INFO, "Found " +
                fhirMedicationsAnonStrength.size() + " matches with the same strength for drug: " +
                drug.getCoveredText()
        );

        // prefer concepts with ingredient-strength pairs
        return !fhirMedicationsStrength.isEmpty() ? fhirMedicationsStrength :
                !fhirMedicationsAnonStrength.isEmpty() ? fhirMedicationsAnonStrength :
                medications;
    }

    // use Adapter pattern to simplify comparisons
    class MedicationIngredientComponentCommons {
        private final Medication.MedicationIngredientComponent component;

        MedicationIngredientComponentCommons(Medication.MedicationIngredientComponent component) {
            this.component = component;
        }

        String getItemCode() {
            Reference substanceReference = (Reference) component.getItem();

            return substanceReference.getReference().split("/")[1].split("rxNorm-")[1];
        }

        BigDecimal getStrengthNumeratorValue() {
            return Optional.ofNullable(component.getStrength().getNumerator().getValue())
                    .orElse(BigDecimal.valueOf(0));
        }

        String getStrengthNumeratorUnit() {
            return Optional.of(component.getStrength().getNumerator().getUnit().toLowerCase())
                    .orElse("");
        }
    }

    class IngredientCommons {
        private final Ingredient ingredient;

        IngredientCommons(Ingredient ingredient) {
            this.ingredient = ingredient;
        }

        String getItemCode() {
            return ingredient.getItem();
        }

        BigDecimal getStrengthNumeratorValue() {
            return BigDecimal.valueOf(ingredient.getAmountValue());
        }

        String getStrengthNumeratorUnit() {
            return ingredient.getAmountUnit().toLowerCase();
        }
    }
}