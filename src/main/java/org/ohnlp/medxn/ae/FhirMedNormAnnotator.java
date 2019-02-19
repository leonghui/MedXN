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
import org.hl7.fhir.dstu3.model.Medication;
import org.ohnlp.medxn.fhir.FhirQueryClient;
import org.ohnlp.medxn.fhir.FhirQueryUtils;
import org.ohnlp.medxn.type.Drug;
import org.ohnlp.medxn.type.Ingredient;
import org.ohnlp.medxn.type.MedAttr;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class FhirMedNormAnnotator extends JCasAnnotator_ImplBase {
    private List<Medication> allMedications;
    private FhirQueryClient queryClient;

    @Override
    public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
        super.initialize(uimaContext);

        // Get config parameter values
        String url = (String) uimaContext.getConfigParameterValue("FHIR_SERVER_URL");
        int timeout = (int) uimaContext.getConfigParameterValue("TIMEOUT_SEC");
        queryClient = FhirQueryClient.createFhirQueryClient(url, timeout);

        allMedications = queryClient.getAllMedications();
    }

    @Override
    public void process(JCas jcas) {
        ImmutableList<Drug> drugs = ImmutableList.copyOf(jcas.getAnnotationIndex(Drug.type));

        drugs.forEach(drug -> {
            Set<Medication> candidateMedications = new LinkedHashSet<>();
            if (drug.getBrand() != null) {
                ImmutableSet<String> candidateBrands = ImmutableSet.copyOf(drug.getBrand().split(","));

                candidateMedications.addAll(FhirQueryUtils.getMedicationsFromRxCui(allMedications, candidateBrands));
            } else {
                candidateMedications.addAll(findGenericMedications(jcas, drug));
            }

            if (candidateMedications.size() == 1) {
                Medication medication = candidateMedications.iterator().next();

                drug.setNormRxName2(medication.getCode().getCodingFirstRep().getDisplay());
                drug.setNormRxCui2(medication.getCode().getCodingFirstRep().getCode());
            }
        });
    }

    @SuppressWarnings("UnstableApiUsage")
    private ImmutableSet<Medication> findGenericMedications(JCas jcas, Drug drug) {

        // all generic Drugs must have at least one Ingredient
        assert drug.getIngredients() != null;

        ImmutableList<IngredientCommons> annotationIngredients = Streams.stream(drug.getIngredients())
                .map(Ingredient.class::cast)
                .map(IngredientCommons::new)
                .collect(ImmutableList.toImmutableList());

        ImmutableList<String> annotationIngredientIds = annotationIngredients.stream()
                .map(IngredientCommons::getItem)
                .collect(ImmutableList.toImmutableList());

        // CRITERION 1: Consider all medications with the same ingredients
        ImmutableSet<Medication> fhirMedicationsIng = allMedications.parallelStream()
                .filter(medication -> {
                    ImmutableList<String> fhirIngredients = FhirQueryUtils.getIngredientsFromMedication(medication);

                    return !medication.getIsBrand() &&
                            fhirIngredients.size() == annotationIngredientIds.size() &&
                            fhirIngredients.containsAll(annotationIngredientIds);
                })
                .collect(ImmutableSet.toImmutableSet());

        // CRITERION 2a: Include only medications with the same dose form
        String doseForm = Optional.ofNullable(drug.getForm()).orElse("");

        ImmutableSet<Medication> fhirMedicationsDoseForm = fhirMedicationsIng.stream()
                .filter(medication ->
                        doseForm.contentEquals(medication
                                .getForm()
                                .getCodingFirstRep()
                                .getCode()
                        )
                )
                .collect(ImmutableSet.toImmutableSet());

        // CRITERION 2b: Include only medications with the same route, if dose form is not found or unavailable
        ImmutableList<MedAttr> routes =
                Streams.stream(Optional.ofNullable(drug.getAttrs()).orElse(new FSArray(jcas, 0)))
                        .map(MedAttr.class::cast)
                        .filter(attribute -> attribute.getTag().contentEquals(FhirQueryUtils.MedAttrConstants.ROUTE))
                        .collect(ImmutableList.toImmutableList());

        ImmutableSet<Medication> fhirMedicationsRoute = routes.stream()
                .flatMap(route -> fhirMedicationsIng.stream()
                        .filter(medication -> {
                            String routeText = route.getCoveredText();

                            // route normalization adapted from FhirACLookupDrugFormAnnotator
                            String routeNormText = routeText;
                            if (routeText.equalsIgnoreCase("mouth") ||
                                    routeText.equalsIgnoreCase("oral") ||
                                    routeText.equalsIgnoreCase("orally") ||
                                    routeText.equalsIgnoreCase("po") ||
                                    routeText.equalsIgnoreCase("p.o.")) {
                                routeNormText = "oral";
                            }
                            if (routeText.equalsIgnoreCase("vaginally") ||
                                    routeText.equalsIgnoreCase("pv")) {
                                routeNormText = "vaginal";
                            }
                            if (routeText.equalsIgnoreCase("rectally") ||
                                    routeText.equalsIgnoreCase("anally") ||
                                    routeText.equalsIgnoreCase("pr") ||
                                    routeText.equalsIgnoreCase("p.r.")) {
                                routeNormText = "rectal";
                            }
                            if (routeText.equalsIgnoreCase("skin") ||
                                    routeText.equalsIgnoreCase("topical") ||
                                    routeText.equalsIgnoreCase("topically")) {
                                routeNormText = "topical";
                            }

                            return medication
                                    .getForm()
                                    .getCodingFirstRep()
                                    .getDisplay()
                                    .toLowerCase()
                                    .contains(routeNormText);
                        }))
                .collect(ImmutableSet.toImmutableSet());

        // prefer concepts with dose form for interim results
        ImmutableSet<Medication> fhirMedicationsDoseFormOrRoute =
                !fhirMedicationsDoseForm.isEmpty() ? fhirMedicationsDoseForm : fhirMedicationsRoute;

        // CRITERION 3: Include only medications with the same strength
        ImmutableSet<Medication> fhirMedicationsStrength = fhirMedicationsDoseFormOrRoute.stream()
                .filter(medication ->
                        medication.getIngredient().stream()
                                .map(Medication.MedicationIngredientComponent.class::cast)
                                .map(MedicationIngredientComponentCommons::new)
                                .allMatch(component ->
                                        annotationIngredients.stream().allMatch(ingredient ->
                                                component.getItemCode().contentEquals(ingredient.getItemCode()) &&
                                                        component.getStrengthNumeratorValue().equals(
                                                                ingredient.getStrengthNumeratorValue()))
                                )
                )
                .collect(ImmutableSet.toImmutableSet());

        return fhirMedicationsStrength;
    }

    // use Adapter pattern to simplify comparisons
    class MedicationIngredientComponentCommons {
        private final Medication.MedicationIngredientComponent component;

        MedicationIngredientComponentCommons(Medication.MedicationIngredientComponent component) {
            this.component = component;
        }

        public String getItemCode() {
            return component.getItemCodeableConcept().getCodingFirstRep().getCode();
        }

        public BigDecimal getStrengthNumeratorValue() {
            return component.getAmount().getNumerator().getValue();
        }

        public String getStrengthNumeratorUnit() {
            return component.getAmount().getNumerator().getUnit().toLowerCase();
        }


    }

    class IngredientCommons {
        private final Ingredient ingredient;

        IngredientCommons(Ingredient ingredient) {
            this.ingredient = ingredient;
        }

        public String getItemCode() {
            return ingredient.getItem();
        }

        public BigDecimal getStrengthNumeratorValue() {
            return BigDecimal.valueOf(ingredient.getAmountValue());
        }

        public String getStrengthNumeratorUnit() {
            return ingredient.getAmountUnit().toLowerCase();
        }

        String getItem() {
            return ingredient.getItem();
        }
    }
}