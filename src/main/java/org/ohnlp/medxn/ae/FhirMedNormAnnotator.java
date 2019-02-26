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

import com.google.common.collect.*;
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
import java.math.RoundingMode;
import java.util.*;

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

        queryClient.destroy();
    }

    @Override
    public void process(JCas jcas) {

        jcas.getAnnotationIndex(LookupWindow.type).forEach(window -> jcas.getAnnotationIndex(Drug.type).subiterator(window).forEachRemaining(drugAnnotation -> {
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

            FSArray attributeArray = Optional.ofNullable(drug.getAttrs()).orElse(new FSArray(jcas, 0));

            List<MedAttr> attributes = Streams.stream(attributeArray)
                    .map(MedAttr.class::cast)
                    .collect(ImmutableList.toImmutableList());

            boolean hasFormOrRoute = attributes.stream()
                    .map(MedAttr::getTag)
                    .anyMatch(tag -> tag.contentEquals(FhirQueryUtils.MedAttrConstants.FORM) ||
                            tag.contentEquals(FhirQueryUtils.MedAttrConstants.ROUTE));

            boolean hasStrength = attributes.stream()
                    .map(MedAttr::getTag)
                    .anyMatch(tag -> tag.contentEquals(FhirQueryUtils.MedAttrConstants.STRENGTH));

            Set<Medication> initialResults = ImmutableSet.copyOf(candidateMedications);
            Set<Medication> formRouteResults = ImmutableSet.copyOf(candidateMedications);
            Set<Medication> strengthResults = ImmutableSet.copyOf(candidateMedications);
            Set<Medication> combinedResults = ImmutableSet.copyOf(candidateMedications);

            if (hasStrength) {
                initialResults = initialResults.stream()
                        .filter(medication ->
                                medication.getIngredient().stream()
                                        .allMatch(component ->
                                                component.getStrength().getNumerator().getUnit() != null &&
                                                        component.getStrength().getNumerator().getValue() != null
                                        )
                        )
                        .collect(ImmutableSet.toImmutableSet());
            } else {
                initialResults = initialResults.stream()
                        .filter(medication ->
                                medication.getIngredient().stream()
                                        .allMatch(component ->
                                                component.getStrength().getNumerator().getUnit() == null &&
                                                        component.getStrength().getNumerator().getValue() == null
                                        )
                        )
                        .collect(ImmutableSet.toImmutableSet());
            }

            if (hasFormOrRoute) {
                initialResults = initialResults.stream()
                        .filter(Medication::hasForm)
                        .collect(ImmutableSet.toImmutableSet());
            } else {
                initialResults = initialResults.stream()
                        .filter(medication -> !medication.hasForm())
                        .collect(ImmutableSet.toImmutableSet());
            }

            if (hasStrength) {
                strengthResults = filterByStrength(jcas, drug, initialResults);
            }

            if (hasFormOrRoute) {
                formRouteResults = filterByDoseFormOrRoute(jcas, drug, initialResults);
            }

            if (!strengthResults.isEmpty() && !formRouteResults.isEmpty()) {
                combinedResults = Sets.intersection(strengthResults, formRouteResults);

                getContext().getLogger().log(Level.INFO, "Found " +
                        combinedResults.size() + " matches with the same strength and dose form for drug: " +
                        drug.getCoveredText() + " = " +
                        FhirQueryUtils.getDisplayNameFromMedications(combinedResults)
                );
            }

            Set<Set<Medication>> resultSets = ImmutableSet.of(
                    initialResults, strengthResults, formRouteResults, combinedResults);

            resultSets.stream()
                    .filter(set -> !set.isEmpty())
                    .min(Comparator.comparingInt(Set::size))
                    .ifPresent(set -> {
                        if (set.size() == 1) {
                            Medication medication = set.iterator().next();

                            drug.setNormRxName(medication.getCode().getCodingFirstRep().getDisplay());
                            drug.setNormRxCui(medication.getCode().getCodingFirstRep().getCode());

                            drug.setNormRxName2(medication.getCode().getCodingFirstRep().getDisplay());
                            drug.setNormRxCui2(medication.getCode().getCodingFirstRep().getCode());

                            getContext().getLogger().log(Level.INFO, "Tagging " +
                                    medication.getCode().getCodingFirstRep().getDisplay() + " to drug: " +
                                    drug.getCoveredText()
                            );

                        } else {
                            List<Set<String>> listOfParents = set.stream()
                                    .map(medication ->
                                            allMedicationKnowledge.values().stream()
                                                    .filter(medicationKnowledge ->
                                                            medication.getCode().getCodingFirstRep().getCode().contentEquals(
                                                                    medicationKnowledge.getCode().getCodingFirstRep().getCode()))
                                                    .flatMap(medicationKnowledge ->
                                                            medicationKnowledge.getAssociatedMedication()
                                                                    .stream()
                                                                    .map(Reference::getReference))
                                                    .map(string -> string.split("/")[1].split("rxNorm-")[1])
                                                    .collect(ImmutableSet.toImmutableSet())
                                    )
                                    .collect(ImmutableList.toImmutableList());

                            // Intersection of stream of sets into new set
                            // https://stackoverflow.com/a/38266681
                            Set<String> parentCodes = listOfParents.stream().skip(1)
                                    .collect(() -> new HashSet<>(listOfParents.get(0)), Set::retainAll, Set::retainAll);

                            if (parentCodes.size() >= 1) {

                                Comparator<Medication> byDisplayLength = Comparator.comparingInt((Medication m) ->
                                        m.getCode().getCodingFirstRep().getDisplay().length());

                                FhirQueryUtils
                                        .getMedicationsFromRxCui(allMedications, parentCodes)
                                        .stream()
                                        .min(byDisplayLength)
                                        .ifPresent(parentMedication -> {


                                            drug.setNormRxName(parentMedication.getCode().getCodingFirstRep().getDisplay());
                                            drug.setNormRxCui(parentMedication.getCode().getCodingFirstRep().getCode());

                                            drug.setNormRxName2(parentMedication.getCode().getCodingFirstRep().getDisplay());
                                            drug.setNormRxCui2(parentMedication.getCode().getCodingFirstRep().getCode());

                                            getContext().getLogger().log(Level.INFO, "Tagging parent " +
                                                    parentMedication.getCode().getCodingFirstRep().getDisplay() + " to drug: " +
                                                    drug.getCoveredText()
                                            );
                                        });

                            } else {

                                getContext().getLogger().log(Level.INFO, "Remaining matches for drug: " +
                                        drug.getCoveredText() + " : " +
                                        FhirQueryUtils.getDisplayNameFromMedications(set)
                                );
                            }
                        }
                    });

        }));
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

    @SuppressWarnings("UnstableApiUsage")
    private Set<Medication> filterByDoseFormOrRoute(JCas jcas, Drug drug, Set<Medication> medications) {

        // CRITERION 2a: Include only medications with the same dose form
        String doseForm = Optional.ofNullable(drug.getForm()).orElse("");

        Set<Medication> fhirMedicationsDoseForm = medications.stream()
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
                drug.getCoveredText() + " = " +
                FhirQueryUtils.getDisplayNameFromMedications(fhirMedicationsDoseForm)

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
                drug.getCoveredText() + " = " +
                FhirQueryUtils.getDisplayNameFromMedications(fhirMedicationsRoute)
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
                                                component.getCell().equals(ingredient.getCell()))
                                )
                )
                .collect(ImmutableSet.toImmutableSet());

        getContext().getLogger().log(Level.INFO, "Found " +
                fhirMedicationsStrength.size() + " matches with the same ingredient-strength pairs for drug: " +
                drug.getCoveredText() + " = " +
                FhirQueryUtils.getDisplayNameFromMedications(fhirMedicationsStrength)

        );

        // CRITERION 3b: Include only medications with the same strengths, if ingredient is not found
        Set<Medication> fhirMedicationsAnonStrength = medications.stream()
                .filter(medication ->
                        medication.getIngredient().stream()
                                .map(Medication.MedicationIngredientComponent.class::cast)
                                .map(MedicationIngredientComponentCommons::new)
                                .allMatch(component ->
                                        annotationIngredients.stream().allMatch(ingredient ->
                                                component.getAnonEntry().equals(ingredient.getAnonEntry()))
                                )
                )
                .collect(ImmutableSet.toImmutableSet());

        getContext().getLogger().log(Level.INFO, "Found " +
                fhirMedicationsAnonStrength.size() + " matches with the same strength for drug: " +
                drug.getCoveredText() + " = " +
                FhirQueryUtils.getDisplayNameFromMedications(fhirMedicationsAnonStrength)

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
            return component.getStrength().getNumerator().getValue();
        }

        String getStrengthNumeratorUnit() {
            return component.getStrength().getNumerator().getUnit().toLowerCase();
        }

        Table.Cell<String, String, BigDecimal> getCell() {
            return Tables.immutableCell(getItemCode(), getStrengthNumeratorUnit(), getStrengthNumeratorValue());
        }

        Map.Entry<String, BigDecimal> getAnonEntry() {
            return Maps.immutableEntry(getStrengthNumeratorUnit(), getStrengthNumeratorValue());
        }
    }

    class IngredientCommons {
        private final Ingredient ingredient;
        private boolean isMicrogram = false;
        private boolean isGram = false;

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
            return Optional.ofNullable(ingredient.getAmountUnit()).orElse("").toLowerCase();
        }

        String getNormStrengthNumeratorUnit() {
            switch (getStrengthNumeratorUnit()) {
                case "milligrams":
                case "milligram":
                    return "mg";
                case "grams":
                case "gram":
                case "g":
                    isGram = true;
                    return "mg";
                case "micrograms":
                case "microgram":
                case "mcg":
                    isMicrogram = true;
                    return "mg";
                default:
                    return getStrengthNumeratorUnit();
            }
        }

        BigDecimal getNormStrengthNumeratorValue() {
            getNormStrengthNumeratorUnit();
            if (isMicrogram) {
                return getStrengthNumeratorValue().divide(new BigDecimal(1000), RoundingMode.HALF_UP);
            } else if (isGram) {
                return getStrengthNumeratorValue().multiply(new BigDecimal(1000));
            } else {
                return getStrengthNumeratorValue();
            }
        }

        Table.Cell<String, String, BigDecimal> getCell() {
            return Tables.immutableCell(getItemCode(), getNormStrengthNumeratorUnit(), getNormStrengthNumeratorValue());
        }

        Map.Entry<String, BigDecimal> getAnonEntry() {
            return Maps.immutableEntry(getNormStrengthNumeratorUnit(), getNormStrengthNumeratorValue());
        }
    }
}