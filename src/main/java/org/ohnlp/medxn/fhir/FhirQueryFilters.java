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
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FhirQueryFilters {
    private final Map<String, Medication> allMedications;
    private final Map<String, MedicationKnowledge> allMedicationKnowledge;
    private final UimaContext context;
    private final String url;
    private final JCas jcas;
    private final Drug drug;

    private final BiConsumer<Medication, String> logDisplayTerm = (Medication medication, String label) ->
            getContext().getLogger().log(Level.INFO, label + " : " +
                    medication.getCode().getCodingFirstRep().getDisplay());

    private Comparator<Medication> byDisplayLength = Comparator.comparingInt((Medication medication) ->
            medication.getCode().getCodingFirstRep().getDisplay().length());

    private FhirQueryFilters(UimaContext uimaContext, JCas parentJcas, Drug subjectDrug, Map<String, Medication> parentAllMedications, Map<String, MedicationKnowledge> parentAllMedicationKnowledge) {
        context = uimaContext;
        url = (String) context.getConfigParameterValue("FHIR_SERVER_URL");
        jcas = parentJcas;
        drug = subjectDrug;

        allMedications = parentAllMedications;
        allMedicationKnowledge = parentAllMedicationKnowledge;
    }

    public static FhirQueryFilters createQueryFilter(UimaContext uimaContext, JCas parentJcas, Drug subjectDrug, Map<String, Medication> parentAllMedications, Map<String, MedicationKnowledge> parentAllMedicationKnowledge) {
        return new FhirQueryFilters(uimaContext, parentJcas, subjectDrug, parentAllMedications, parentAllMedicationKnowledge);
    }

    @SuppressWarnings("UnstableApiUsage")
    private Stream<Medication> validateDoseFormOrRoute(Stream<Medication> medicationStream, boolean isExcludeAll) {

        String doseForm = Optional.ofNullable(drug.getForm()).orElse("");

        FSArray attributeArray = Optional.ofNullable(drug.getAttrs()).orElse(new FSArray(jcas, 0));

        List<MedAttr> attributes = Streams.stream(attributeArray)
                .map(MedAttr.class::cast)
                .collect(ImmutableList.toImmutableList());

        boolean hasRoute = attributes.stream()
                .map(MedAttr::getTag)
                .anyMatch(tag -> tag.contentEquals(FhirQueryUtils.MedAttrConstants.ROUTE));

        if (doseForm.contentEquals("") && !hasRoute && isExcludeAll) {
            medicationStream = Stream.empty();
        } else if (doseForm.contentEquals("") && !hasRoute) {
            medicationStream = medicationStream.filter(medication -> !medication.hasForm());
        } else {
            medicationStream = medicationStream.filter(Medication::hasForm);
        }

        return medicationStream;
    }

    @SuppressWarnings("UnstableApiUsage")
    private Stream<Medication> validateStrength(Stream<Medication> medicationStream, boolean isExcludeAll) {
        FSArray attributeArray = Optional.ofNullable(drug.getAttrs()).orElse(new FSArray(jcas, 0));

        List<MedAttr> attributes = Streams.stream(attributeArray)
                .map(MedAttr.class::cast)
                .collect(ImmutableList.toImmutableList());

        boolean hasStrength = attributes.stream()
                .map(MedAttr::getTag)
                .anyMatch(tag -> tag.contentEquals(FhirQueryUtils.MedAttrConstants.STRENGTH));

        if (!hasStrength && isExcludeAll) {
            medicationStream = Stream.empty();
        } else if (!hasStrength) {
            medicationStream = medicationStream.filter(medication ->
                    medication.getIngredient().isEmpty() ||
                            medication.getIngredient().stream()
                                    .allMatch(component ->
                                            component.getStrength().getNumerator().getUnit() == null &&
                                                    component.getStrength().getNumerator().getValue() == null
                                    ));
        } else {
            medicationStream = medicationStream.filter(medication ->
                    !medication.getIngredient().isEmpty() &&
                            medication.getIngredient().stream()
                                    .allMatch(component ->
                                            component.getStrength().getNumerator().getUnit() != null &&
                                                    component.getStrength().getNumerator().getValue() != null
                                    ));
        }

        return medicationStream;
    }

    @SuppressWarnings("SameParameterValue")
    private Stream<Medication> validateBrand(Stream<Medication> medicationStream, boolean isExcludeAll) {
        boolean hasBrand = !Optional.ofNullable(drug.getBrand()).orElse("").isEmpty();

        if (!hasBrand && isExcludeAll) {
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

    public Set<Medication> getValidatedSet(Set<Medication> medications) {
        Stream<Medication> medicationStream = validateBrand(medications.stream(), false);

        medicationStream = validateDoseFormOrRoute(medicationStream, false);

        medicationStream = validateStrength(medicationStream, false);

        return medicationStream.collect(Collectors.toSet());
    }

    @SuppressWarnings("UnstableApiUsage")
    public Set<Medication> byDoseForm(Set<Medication> medications, boolean isSilent) {

        // CRITERION 2a: Include only medications with the same dose form
        String doseForm = Optional.ofNullable(drug.getForm()).orElse("");

        Set<Medication> results = validateDoseFormOrRoute(medications.stream(), true)
                .filter(medication -> medication
                        .getForm()
                        .getCodingFirstRep()
                        .getCode()
                        .contentEquals(doseForm))
                .collect(ImmutableSet.toImmutableSet());

        logResults("dose form", drug, results, isSilent);

        return results;
    }

    @SuppressWarnings("UnstableApiUsage")
    public Set<Medication> byRouteInference(Set<Medication> medications, boolean isSilent) {

        // CRITERION 2b: Include only medications with the same route, if dose form is not found or unavailable
        FSArray attributeArray = Optional.ofNullable(drug.getAttrs()).orElse(new FSArray(jcas, 0));

        List<MedAttr> routes = Streams.stream(attributeArray)
                .map(MedAttr.class::cast)
                .filter(attribute -> attribute.getTag().contentEquals(FhirQueryUtils.MedAttrConstants.ROUTE))
                .collect(ImmutableList.toImmutableList());

        Set<Medication> results = routes.stream()
                .flatMap(route -> medications.stream()
                        .filter(Medication::hasForm)
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

        logResults("route", drug, results, isSilent);

        return results;
    }

    @SuppressWarnings("UnstableApiUsage")
    public Set<Medication> byStrength(Set<Medication> medications, boolean isSilent) {

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
                                        annotationIngredients.stream()
                                                .anyMatch(ingredient ->
                                                        component.getCell().equals(ingredient.getCell()))))
                .collect(ImmutableSet.toImmutableSet());

        logResults("ingredient-strength pair(s)", drug, results, isSilent);

        return results;
    }

    @SuppressWarnings("UnstableApiUsage")
    public Set<Medication> byStrengthInference(Set<Medication> medications, boolean isSilent) {

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
                                        annotationIngredients.stream()
                                                .anyMatch(ingredient -> ingredient.getAnonEntry().equals(
                                                        component.getAnonEntry()))))
                .collect(ImmutableSet.toImmutableSet());

        logResults("unpaired strength(s)", drug, results, isSilent);

        return results;
    }

    @SuppressWarnings("UnstableApiUsage")
    public Set<Medication> byAssociationInference(Set<Medication> medications, boolean isSilent) {

        Stream<Medication> childStream = medications.stream();

        // CRITERION 4: Include common associations (parent concepts)
        List<Set<String>> listOfParents = childStream
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

        if (!listOfParents.isEmpty()) {
            // Intersection of stream of sets into new set
            // https://stackoverflow.com/a/38266681
            Set<String> commonParentCodes = listOfParents.stream()
                    .filter(set -> !set.isEmpty())
                    .collect(() -> new HashSet<>(listOfParents.get(0)), Set::retainAll, Set::retainAll);

            Map<String, Long> frequencyTable = listOfParents.stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

            Set<String> mostFrequentCodes = frequencyTable.entrySet().stream()
                    .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                    .limit(listOfParents.stream()
                            .map(Set::size)
                            .max(Integer::compare)
                            .orElse(0))
                    .map(Map.Entry::getKey)
                    .collect(ImmutableSet.toImmutableSet());

            Set<String> codesToRetrieve = !commonParentCodes.isEmpty() ? commonParentCodes : mostFrequentCodes;

            Stream<Medication> parentStream = FhirQueryUtils
                    .getMedicationsFromCode(allMedications, codesToRetrieve).stream();

            Set<Medication> results = parentStream
                    .collect(ImmutableSet.toImmutableSet());

            logResults("associations", drug, results, isSilent);

            return results;
        } else {
            return ImmutableSet.of();
        }
    }

    private void logResults(String label, Drug drug, Set<Medication> results, boolean isSilent) {
        String logText = "Found " + results.size() +
                " matches with the same " + label +
                " for drug: " + drug.getCoveredText();

        if (!isSilent) {
            context.getLogger().log(Level.INFO, logText + " = " +
                    FhirQueryUtils.getDisplayNameFromMedications(results));
        }
    }

    private UimaContext getContext() {
        return context;
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