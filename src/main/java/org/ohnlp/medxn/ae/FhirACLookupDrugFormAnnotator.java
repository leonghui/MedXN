/*******************************************************************************
 * Copyright: (c)  2013  Mayo Foundation for Medical Education and
 * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 * triple-shield Mayo logo are trademarks and service marks of MFMER.
 *
 * Except as contained in the copyright notice above, or as used to identify
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 *
 * Copyright (c) 2018-2019. Leong Hui Wong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.ohnlp.medxn.ae;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
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
import java.util.stream.IntStream;

public class FhirACLookupDrugFormAnnotator extends JCasAnnotator_ImplBase {

    private final LookupTable dosageForms = new LookupTable();
    private final Pattern punctuationsOrWhitespaces = Pattern.compile("((\\p{Punct}|\\s)+)");
    private FhirQueryClient queryClient;

    @Override
    public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
        super.initialize(uimaContext);

        // Get config parameter values
        String url = (String) uimaContext.getConfigParameterValue("FHIR_SERVER_URL");
        int timeout = (int) uimaContext.getConfigParameterValue("TIMEOUT_SEC");
        queryClient = FhirQueryClient.createFhirQueryClient(url, timeout);

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
        for (Annotation annotation : jcas.getAnnotationIndex(Drug.type)) {
            Drug drug = (Drug) annotation;
            FSArray attributes = drug.getAttrs();

//            List<String> strengths = new ArrayList<>();
            List<String> forms = new ArrayList<>();
            List<String> routes = new ArrayList<>();
//            List<String> times = new ArrayList<>();
//            List<String> volumes = new ArrayList<>();

            IntStream.range(0, attributes.size()).forEach(index -> {

                MedAttr attribute = (MedAttr) attributes.get(index);

                switch (attribute.getTag()) {
//                    case MedAttrConstants.STRENGTH:
//                        strengths.add(attribute.getCoveredText());
//                        break;
                    case MedAttrConstants.FORM:
                        forms.add(attribute.getCoveredText());
                        break;
                    case MedAttrConstants.ROUTE:
                        routes.add(attribute.getCoveredText());
                        break;
//                    case MedAttrConstants.TIME:
//                        times.add(attribute.getCoveredText());
//                        break;
//                    case MedAttrConstants.VOLUME:
//                        volumes.add(attribute.getCoveredText());
//                        break;
                }

                AtomicBoolean oralContextFlag = new AtomicBoolean(false);
                AtomicBoolean vaginalContextFlag = new AtomicBoolean(false);
                AtomicBoolean rectalContextFlag = new AtomicBoolean(false);
                AtomicBoolean topicalContextFlag = new AtomicBoolean(false);

                if (!routes.isEmpty()) {
                    routes.forEach(route -> {
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
                            .max(Comparator.comparingInt(String::length))
                            .get();
                } else if (forms.size() == 1) {
                    dosageForm = forms.get(0);
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
        }
    }

    class MedAttrConstants {
        static final String STRENGTH = "strength";
        static final String FORM = "form";
        static final String ROUTE = "route";
        static final String TIME = "time";
        static final String VOLUME = "volume";
    }

    class LookupTable {
        // Data structure to store keywords
        // rxCui, keyword
        private final SetMultimap<String, String> keywordMap = LinkedHashMultimap.create();

        // Data structure to store the trie
        private Trie trie;
    }
}