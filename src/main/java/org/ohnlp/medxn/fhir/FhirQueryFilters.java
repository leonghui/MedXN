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

package org.ohnlp.medxn.fhir;

import com.google.common.collect.*;
import org.apache.uima.UimaContext;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.util.Level;
import org.hl7.fhir.r4.model.*;
import org.ohnlp.medxn.type.Drug;
import org.ohnlp.medxn.type.Ingredient;
import org.ohnlp.medxn.type.MedAttr;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Stream;

public class FhirQueryFilters {
    private final Map<String, Medication> allMedications;
    private final Map<String, MedicationKnowledge> allMedicationKnowledge;
    private final UimaContext context;
    private final String url;
    private JCas jcas;
    private Drug drug;
    private Comparator<Medication> byDisplayLength = Comparator.comparingInt((Medication m) ->
            m.getCode().getCodingFirstRep().getDisplay().length());

    private FhirQueryFilters(UimaContext uimaContext, FhirQueryClient queryClient, JCas jcas, Drug drug) {
        context = uimaContext;
        url = (String) uimaContext.getConfigParameterValue("FHIR_SERVER_URL");

        allMedications = queryClient.getAllMedications();
        allMedicationKnowledge = queryClient.getAllMedicationKnowledge();
    }

    public static FhirQueryFilters createQueryFilter(
            UimaContext context, FhirQueryClient queryClient, JCas jcas, Drug drug) {
        return new FhirQueryFilters(context, queryClient, jcas, drug);
    }

    private Stream<Medication> validateDoseForm(Stream<Medication> medicationStream, boolean isStrict) {

        String doseForm = Optional.ofNullable(drug.getForm()).orElse("");

        if (doseForm.contentEquals("") && isStrict) {
            medicationStream = Stream.empty();
        } else if (doseForm.contentEquals("")) {
            medicationStream = medicationStream.filter(medication -> !medication.hasForm());
        } else {
            medicationStream = medicationStream.filter(Medication::hasForm);
        }

        return medicationStream;
    }

    @SuppressWarnings("UnstableApiUsage")
    private Stream<Medication> validateStrength(Stream<Medication> medicationStream, boolean isStrict) {
        FSArray attributeArray = Optional.ofNullable(drug.getAttrs()).orElse(new FSArray(jcas, 0));

        List<MedAttr> attributes = Streams.stream(attributeArray)
                .map(MedAttr.class::cast)
                .collect(ImmutableList.toImmutableList());

        boolean hasStrength = attributes.stream()
                .map(MedAttr::getTag)
                .anyMatch(tag -> tag.contentEquals(FhirQueryUtils.MedAttrConstants.STRENGTH));

        if (!hasStrength && isStrict) {
            medicationStream = Stream.empty();
        } else if (!hasStrength) {
            medicationStream = medicationStream.filter(medication ->
                    medication.getIngredient().stream()
                            .allMatch(component ->
                                    component.getStrength().getNumerator().getUnit() == null &&
                                            component.getStrength().getNumerator().getValue() == null
                            ));
        } else {
            medicationStream = medicationStream.filter(medication ->
                    medication.getIngredient().stream()
                            .allMatch(component ->
                                    component.getStrength().getNumerator().getUnit() != null &&
                                            component.getStrength().getNumerator().getValue() != null
                            ));
        }

        return medicationStream;
    }

    @SuppressWarnings("SameParameterValue")
    private Stream<Medication> validateBrand(Stream<Medication> medicationStream, boolean isStrict) {
        boolean hasBrand = !Optional.ofNullable(drug.getBrand()).orElse("").isEmpty();

        if (!hasBrand && isStrict) {
            medicationStream = Stream.empty();
        } else if (!hasBrand) {
            medicationStream = medicationStream
                    .filter(medication ->
                            medication.getExtensionsByUrl(url + "StructureDefinition/brand").isEmpty());
        } else {
            medicationStream = medicationStream
                    .filter(medication ->
                            !medication.getExtensionsByUrl(url + "StructureDefinition/brand").isEmpty());
        }

        return medicationStream;
    }

    @SuppressWarnings("UnstableApiUsage")
    public Set<Medication> byDoseForm(Set<Medication> medications) {

        // CRITERION 2a: Include only medications with the same dose form
        String doseForm = Optional.ofNullable(drug.getForm()).orElse("");

        Set<Medication> results = validateDoseForm(medications.stream(), true)
                .filter(medication -> medication
                        .getForm()
                        .getCodingFirstRep()
                        .getCode()
                        .contentEquals(doseForm))
                .collect(ImmutableSet.toImmutableSet());

        logResults("dose form", drug, results);

        return results;
    }

    @SuppressWarnings("UnstableApiUsage")
    public Set<Medication> byRouteInference(Set<Medication> medications) {

        // CRITERION 2b: Include only medications with the same route, if dose form is not found or unavailable
        FSArray attributeArray = Optional.ofNullable(drug.getAttrs()).orElse(new FSArray(jcas, 0));

        List<MedAttr> routes = Streams.stream(attributeArray)
                .map(MedAttr.class::cast)
                .filter(attribute -> attribute.getTag().contentEquals(FhirQueryUtils.MedAttrConstants.ROUTE))
                .collect(ImmutableList.toImmutableList());

        Set<Medication> results = routes.stream()
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
                                case "subcutaneously":
                                case "sq":
                                case "intervenous":
                                case "intervenously":
                                case "injected":
                                case "iv":
                                case "intramuscular":
                                    routeNormText = "inject";
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

        logResults("route", drug, results);

        return results;
    }

    @SuppressWarnings("UnstableApiUsage")
    public Set<Medication> byStrength(Set<Medication> medications) {

        FSArray ingredientArray = Optional.ofNullable(drug.getIngredients()).orElse(new FSArray(jcas, 0));

        List<IngredientAdapter> annotationIngredients = Streams.stream(ingredientArray)
                .map(Ingredient.class::cast)
                .map(IngredientAdapter::new)
                .collect(ImmutableList.toImmutableList());

        // CRITERION 3a: Include only medications with the same ingredient-strength pairs
        Set<Medication> results = validateStrength(medications.stream(), true)
                .filter(medication ->
                        medication.getIngredient().stream()
                                .map(Medication.MedicationIngredientComponent.class::cast)
                                .map(MedicationIngredientComponentAdapter::new)
                                .allMatch(component ->
                                        annotationIngredients.stream().allMatch(ingredient ->
                                                component.getCell().equals(ingredient.getCell()))
                                )
                )
                .collect(ImmutableSet.toImmutableSet());

        logResults("ingredient-strength pair(s)", drug, results);

        return results;
    }

    @SuppressWarnings("UnstableApiUsage")
    public Set<Medication> byStrengthInference(Set<Medication> medications) {

        FSArray ingredientArray = Optional.ofNullable(drug.getIngredients()).orElse(new FSArray(jcas, 0));

        List<IngredientAdapter> annotationIngredients = Streams.stream(ingredientArray)
                .map(Ingredient.class::cast)
                .map(IngredientAdapter::new)
                .collect(ImmutableList.toImmutableList());

        // CRITERION 3b: Include only medications with the same strengths, if ingredient is not found
        Set<Medication> results = validateStrength(medications.stream(), true)
                .filter(medication ->
                        medication.getIngredient().stream()
                                .map(Medication.MedicationIngredientComponent.class::cast)
                                .map(MedicationIngredientComponentAdapter::new)
                                .allMatch(component ->
                                        annotationIngredients.stream().allMatch(ingredient ->
                                                component.getAnonEntry().equals(ingredient.getAnonEntry()))
                                )
                )
                .collect(ImmutableSet.toImmutableSet());

        logResults("strength(s)", drug, results);

        return results;
    }

    public Set<Medication> byDoseFormAndStrength(Set<Medication> medications) {

        Set<Medication> results = Sets.intersection(
                byDoseForm(medications),
                byStrength(medications));

        logResults("same strength and dose form", drug, results);

        return results;
    }

    @SuppressWarnings("UnstableApiUsage")
    public Set<Medication> byAssociationInference(Set<Medication> medications) {

        // CRITERION 4: Include common associations (parent concepts)
        List<Set<String>> listOfParents = medications.stream()
                .map(Medication::getCode)
                .map(CodeableConcept::getCodingFirstRep)
                .map(Coding::getCode)
                .map(code -> allMedicationKnowledge.get(code)
                        .getAssociatedMedication()
                        .stream()
                        .map(Reference::getReference)
                        .map(string -> string.split("/")[1].split("rxNorm-")[1])
                        .collect(ImmutableSet.toImmutableSet()))
                .collect(ImmutableList.toImmutableList());

        // Intersection of stream of sets into new set
        // https://stackoverflow.com/a/38266681
        Set<String> commonParentCodes = listOfParents.stream().skip(1)
                .collect(() -> new HashSet<>(listOfParents.get(0)), Set::retainAll, Set::retainAll);

        Stream<Medication> medicationStream = FhirQueryUtils
                .getMedicationsFromCode(allMedications, commonParentCodes).stream();

        medicationStream = validateBrand(medicationStream, false);

        medicationStream = validateDoseForm(medicationStream, false);

        medicationStream = validateStrength(medicationStream, false);

        Set<Medication> results = medicationStream
                .collect(ImmutableSet.toImmutableSet());

        logResults("associations", drug, results);

        return results;
    }

    private void logResults(String label, Drug drug, Set<Medication> results) {
        String logText = "Found " + results.size() +
                " matches with the same " + label +
                " for drug: " + drug.getCoveredText();

        context.getLogger().log(Level.INFO, logText);

        context.getLogger().log(Level.FINE, logText + " = " + FhirQueryUtils.getDisplayNameFromMedications(results));
    }

    // use Adapter pattern to simplify comparisons
    private class MedicationIngredientComponentAdapter {
        private final Medication.MedicationIngredientComponent component;

        MedicationIngredientComponentAdapter(Medication.MedicationIngredientComponent component) {
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

    private class IngredientAdapter {
        private final Ingredient ingredient;
        private boolean isMicrogram = false;
        private boolean isGram = false;

        IngredientAdapter(Ingredient ingredient) {
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