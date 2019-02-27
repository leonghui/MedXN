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
import org.ohnlp.medxn.fhir.FhirQueryFilters;
import org.ohnlp.medxn.fhir.FhirQueryUtils;
import org.ohnlp.medxn.type.Drug;
import org.ohnlp.medxn.type.Ingredient;
import org.ohnlp.medxn.type.MedAttr;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class FhirMedNormAnnotator extends JCasAnnotator_ImplBase {
    private Map<String, Medication> allMedications;
    private Map<String, MedicationKnowledge> allMedicationKnowledge;
    private String url;
    private Comparator<Medication> byDisplayLength = Comparator.comparingInt((Medication m) ->
            m.getCode().getCodingFirstRep().getDisplay().length());
    private FhirQueryClient queryClient;
    
    @Override
    public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
        super.initialize(uimaContext);

        // Get config parameter values
        url = (String) uimaContext.getConfigParameterValue("FHIR_SERVER_URL");
        int timeout = (int) uimaContext.getConfigParameterValue("TIMEOUT_SEC");
        queryClient = FhirQueryClient.createFhirQueryClient(url, timeout);

        allMedications = queryClient.getAllMedications();
        allMedicationKnowledge = queryClient.getAllMedicationKnowledge();
    }

    @Override
    public void process(JCas jcas) {
        jcas.getAnnotationIndex(Drug.type).forEach(drugAnnotation -> {

            Drug drug = (Drug) drugAnnotation;

            FhirQueryFilters queryFilters = FhirQueryFilters.createQueryFilter(getContext(), queryClient, jcas, drug);

            String brand = Optional.ofNullable(drug.getBrand()).orElse("");

            Set<String> candidateBrands = ImmutableSet.copyOf(brand.split(","));

            Set<Medication> candidateMedications = candidateBrands.isEmpty() ? findGenericMedications(jcas, drug) :
                    FhirQueryUtils.getMedicationsFromCode(allMedications, candidateBrands);

            Set<Medication> initialResults = ImmutableSet.copyOf(candidateMedications);

            Set<Medication> formResults = queryFilters.byDoseForm(initialResults);

            Set<Medication> strengthResults = queryFilters.byStrength(initialResults);

            Set<Medication> combinedResults = queryFilters.byDoseFormAndStrength(initialResults);

            Set<Set<Medication>> resultSets = ImmutableSet.of(
                    initialResults, strengthResults, formResults, combinedResults);

            Set<Medication> routeInferenceResults = queryFilters.byRouteInference(initialResults);

            Set<Medication> strengthInferenceResults = queryFilters.byStrengthInference(initialResults);

            Set<Medication> associationInferenceResults = queryFilters.byAssociationInference(initialResults);

            Set<Set<Medication>> inferenceResultSets = ImmutableSet.of(
                    routeInferenceResults, strengthInferenceResults, associationInferenceResults);

            resultSets.stream()
                    .filter(set -> !set.isEmpty())
                    .min(Comparator.comparingInt(Set::size))
                    .ifPresent(set -> {
                        if (set.size() == 1) {
                            Medication medication = set.iterator().next();

                            drug.setNormRxName(medication.getCode().getCodingFirstRep().getDisplay());
                            drug.setNormRxCui(medication.getCode().getCodingFirstRep().getCode());

                            getContext().getLogger().log(Level.INFO, "Tagging " +
                                    medication.getCode().getCodingFirstRep().getDisplay() + " to drug: " +
                                    drug.getCoveredText()
                            );

                        } else {
                            getContext().getLogger().log(Level.INFO, "Remaining matches for drug: " +
                                    drug.getCoveredText() + " : " +
                                    FhirQueryUtils.getDisplayNameFromMedications(set)
                            );
                        }
                    });

            inferenceResultSets.stream()
                    .filter(set -> !set.isEmpty())
                    .min(Comparator.comparingInt(Set::size))
                    .ifPresent(set -> {
                        if (set.size() == 1) {
                            Medication medication = set.iterator().next();

                            drug.setNormRxName2(medication.getCode().getCodingFirstRep().getDisplay());
                            drug.setNormRxCui2(medication.getCode().getCodingFirstRep().getCode());

                            getContext().getLogger().log(Level.INFO, "Tagging inferred " +
                                    medication.getCode().getCodingFirstRep().getDisplay() + " to drug: " +
                                    drug.getCoveredText()
                            );

                        } else {
                            getContext().getLogger().log(Level.INFO, "Remaining inferred matches for drug: " +
                                    drug.getCoveredText() + " : " +
                                    FhirQueryUtils.getDisplayNameFromMedications(set)
                            );
                        }
                    });
        });
    }

    private boolean tagParentMedication(JCas jcas, Drug drug, Medication specificMedication) {
        FSArray attributeArray = Optional.ofNullable(drug.getAttrs()).orElse(new FSArray(jcas, 0));

        List<MedAttr> attributes = Streams.stream(attributeArray)
                .map(MedAttr.class::cast)
                .collect(ImmutableList.toImmutableList());

        boolean hasStrength = attributes.stream()
                .map(MedAttr::getTag)
                .anyMatch(tag -> tag.contentEquals(FhirQueryUtils.MedAttrConstants.STRENGTH));

        boolean hasBrand = !Optional.ofNullable(drug.getBrand()).orElse("").isEmpty();

        Set<String> parentCodes = allMedicationKnowledge.get(specificMedication.getCode().getCodingFirstRep().getCode())
                .getAssociatedMedication()
                .stream()
                .map(Reference::getReference)
                .map(string -> string.split("/")[1].split("rxNorm-")[1])
                .collect(ImmutableSet.toImmutableSet());

        Stream<Medication> medicationStream = FhirQueryUtils
                .getMedicationsFromCode(allMedications, parentCodes)
                .stream();

        if (hasStrength) {
            medicationStream = medicationStream
                    .filter(medication -> medication.getIngredient().stream()
                            .allMatch(component -> component.getStrength().getNumerator().getUnit() != null &&
                                    component.getStrength().getNumerator().getValue() != null));
        } else {
            medicationStream = medicationStream
                    .filter(medication -> medication.getIngredient().stream()
                            .allMatch(component -> component.getStrength().getNumerator().getUnit() == null &&
                                    component.getStrength().getNumerator().getValue() == null));
        }

        if (hasBrand) {
            medicationStream = medicationStream
                    .filter(medication -> !medication.getExtensionsByUrl(url + "StructureDefinition/brand").isEmpty());
        } else {
            medicationStream = medicationStream
                    .filter(medication -> medication.getExtensionsByUrl(url + "StructureDefinition/brand").isEmpty());
        }

        AtomicBoolean isTagged = new AtomicBoolean(false);

        medicationStream.max(byDisplayLength).ifPresent(medication -> {
            drug.setNormRxName(medication.getCode().getCodingFirstRep().getDisplay());
            drug.setNormRxCui(medication.getCode().getCodingFirstRep().getCode());
            isTagged.set(true);
        });

        return isTagged.get();
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