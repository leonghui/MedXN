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

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.ohnlp.medxn.fhir.FhirQueryClient;
import org.ohnlp.medxn.fhir.FhirQueryUtils;
import org.ohnlp.medxn.type.Drug;
import org.ohnlp.medxn.type.MedAttr;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class FhirACLookupDrugFormAnnotator extends JCasAnnotator_ImplBase {

    private final FhirQueryUtils.LookupTable dosageForms = new FhirQueryUtils.LookupTable();
    private final Pattern punctuationsOrWhitespaces = Pattern.compile("((\\p{Punct}|\\s)+)");

    @Override
    public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
        super.initialize(uimaContext);

        // Get config parameter values
        String url = (String) uimaContext.getConfigParameterValue("FHIR_SERVER_URL");
        int timeout = (int) uimaContext.getConfigParameterValue("TIMEOUT_SEC");
        FhirQueryClient queryClient = FhirQueryClient.createFhirQueryClient(url, timeout);

        queryClient.getDosageFormMap().forEach((rxCui, term) -> {

            dosageForms.keywordMap.put(rxCui, term.toLowerCase());

            // enrich keyword map with common abbreviations
            dosageForms.keywordMap.put(rxCui, term.replaceAll("(?i)Tablet", "Tab").toLowerCase());
            dosageForms.keywordMap.put(rxCui, term.replaceAll("(?i)Capsule", "Cap").toLowerCase());
            dosageForms.keywordMap.put(rxCui, term.replaceAll("(?i)Injection|Injectable", "Inj").toLowerCase());
            dosageForms.keywordMap.put(rxCui, term.replaceAll("(?i)Topical", "Top").toLowerCase());
            dosageForms.keywordMap.put(rxCui, term.replaceAll("(?i)Cream", "Crm").toLowerCase());
            dosageForms.keywordMap.put(rxCui, term.replaceAll("(?i)Ointment", "Oint").toLowerCase());
            dosageForms.keywordMap.put(rxCui, term.replaceAll("(?i)Suppository", "Supp").toLowerCase());
        });

        dosageForms.trie = Trie.builder()
                .ignoreCase()
                .ignoreOverlaps()
                .onlyWholeWords()
                .addKeywords(dosageForms.keywordMap.values())
                .build();
    }

    @Override
    public void process(JCas jcas) {
        jcas.getAnnotationIndex(Drug.type).forEach(annotation -> {
            Drug drug = (Drug) annotation;
            FSArray attributeArray = drug.getAttrs();

            List<MedAttr> forms = new ArrayList<>();
            List<MedAttr> routes = new ArrayList<>();

            attributeArray.forEach(featureStructure -> {
                MedAttr attribute = (MedAttr) featureStructure;
                switch (attribute.getTag()) {
                    case FhirQueryUtils.MedAttrConstants.FORM:
                        forms.add(attribute);
                        break;
                    case FhirQueryUtils.MedAttrConstants.ROUTE:
                        routes.add(attribute);
                        break;
                }

                AtomicBoolean oralContextFlag = new AtomicBoolean(false);
                AtomicBoolean vaginalContextFlag = new AtomicBoolean(false);
                AtomicBoolean rectalContextFlag = new AtomicBoolean(false);
                AtomicBoolean topicalContextFlag = new AtomicBoolean(false);

                if (!routes.isEmpty()) {
                    routes.forEach(routeAttr -> {
                        String route = routeAttr.getCoveredText();
                        if (route.equalsIgnoreCase("mouth") ||
                                route.equalsIgnoreCase("oral") ||
                                route.equalsIgnoreCase("orally") ||
                                route.equalsIgnoreCase("po") ||
                                route.equalsIgnoreCase("p.o.")) {
                            oralContextFlag.compareAndSet(false, true);
                        }
                        if (route.equalsIgnoreCase("vaginally") ||
                                route.equalsIgnoreCase("pv")) {
                            vaginalContextFlag.compareAndSet(false, true);
                        }
                        if (route.equalsIgnoreCase("rectally") ||
                                route.equalsIgnoreCase("anally") ||
                                route.equalsIgnoreCase("pr") ||
                                route.equalsIgnoreCase("p.r.")) {
                            rectalContextFlag.compareAndSet(false, true);
                        }
                        if (route.equalsIgnoreCase("skin") ||
                                route.equalsIgnoreCase("topical") ||
                                route.equalsIgnoreCase("topically")) {
                            topicalContextFlag.compareAndSet(false, true);
                        }
                    });
                }

                // if more than 1 dose form is found, use the longest dose form
                // if 1 dose form is found, check against known dosage forms
                // if no matches are found, use route contexts to attempt inference

                String dosageForm = "";

                if (forms.size() > 1) {
                    dosageForm = forms.stream()
                            .map(MedAttr::getCoveredText)
                            .max(Comparator.comparingInt(String::length))
                            .get();
                } else if (forms.size() == 1) {
                    dosageForm = forms.get(0).getCoveredText();
                }

                if (!dosageForm.equals("")) {
                    String sanitizedDosageForm = dosageForm
                            .replaceAll(punctuationsOrWhitespaces.toString(), " ")
                            .trim();

                    Emit matchedForm = null;

                    if (dosageForms.trie.containsMatch(sanitizedDosageForm)) {
                        matchedForm = dosageForms.trie.firstMatch(sanitizedDosageForm);

                    } else {
                        if (oralContextFlag.get() &&
                                dosageForms.trie.containsMatch("oral " + sanitizedDosageForm)) {
                            matchedForm = dosageForms.trie.firstMatch("oral " + sanitizedDosageForm);
                        } else if (vaginalContextFlag.get() &&
                                dosageForms.trie.containsMatch("vaginal " + sanitizedDosageForm)) {
                            matchedForm = dosageForms.trie.firstMatch("vaginal " + sanitizedDosageForm);
                        } else if (rectalContextFlag.get() &&
                                dosageForms.trie.containsMatch("rectal " + sanitizedDosageForm)) {
                            matchedForm = dosageForms.trie.firstMatch("rectal " + sanitizedDosageForm);
                        } else if (topicalContextFlag.get() &&
                                dosageForms.trie.containsMatch("topical " + sanitizedDosageForm)) {
                            matchedForm = dosageForms.trie.firstMatch("topical " + sanitizedDosageForm);
                        }
                    }

                    if (matchedForm != null) {
                        String rxCui = FhirQueryUtils
                                .getRxCuiFromKeywordMap(dosageForms.keywordMap, matchedForm.getKeyword());
                        drug.setForm(rxCui);
                    }
                }
            });
        });
    }
}