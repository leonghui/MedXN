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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Streams;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.ohnlp.medxn.fhir.FhirQueryClient;
import org.ohnlp.medxn.fhir.FhirQueryUtils;
import org.ohnlp.medxn.type.Drug;
import org.ohnlp.medxn.type.MedAttr;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FhirACLookupDrugFormAnnotator extends JCasAnnotator_ImplBase {

    private final FhirQueryUtils.LookupTable doseForms = new FhirQueryUtils.LookupTable();
    private final Pattern punctuationOrWhitespace = Pattern.compile("\\p{Punct}|\\s");
    private final Pattern endsWithS = Pattern.compile("s$");
    private final Pattern endsWithLyOrRy = Pattern.compile("(?<=[l|r])(y$)");

    @Override
    public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
        super.initialize(uimaContext);

        // Get config parameter values
        String url = (String) uimaContext.getConfigParameterValue("FHIR_SERVER_URL");
        int timeout = (int) uimaContext.getConfigParameterValue("TIMEOUT_SEC");
        FhirQueryClient queryClient = FhirQueryClient.createFhirQueryClient(url, timeout);

        FhirQueryUtils.getDosageFormMap(queryClient).forEach((code, term) -> {

            doseForms.keywordMap.put(code, term.toLowerCase());

            // enrich keyword map with common abbreviations
            doseForms.keywordMap.put(code, term.replaceAll("(?i)Tablet", "Tab").toLowerCase());
            doseForms.keywordMap.put(code, term.replaceAll("(?i)Capsule", "Cap").toLowerCase());
            doseForms.keywordMap.put(code, term.replaceAll("(?i)Injection|Injectable", "Inj").toLowerCase());
            doseForms.keywordMap.put(code, term.replaceAll("(?i)Topical", "Top").toLowerCase());
            doseForms.keywordMap.put(code, term.replaceAll("(?i)Cream", "Crm").toLowerCase());
            doseForms.keywordMap.put(code, term.replaceAll("(?i)Ointment", "Oint").toLowerCase());
            doseForms.keywordMap.put(code, term.replaceAll("(?i)Suppository", "Supp").toLowerCase());
            doseForms.keywordMap.put(code, term.replaceAll("(?i)Inhaler", "Inh").toLowerCase());

            // enrich keyword map with common synonyms
            doseForms.keywordMap.put(code, term.replaceAll("(?i)Ophthalmic", "Eye").toLowerCase());
            doseForms.keywordMap.put(code, term.replaceAll("(?i)Otic", "Ear").toLowerCase());
        });

        queryClient.destroy();

        SetMultimap<String, String> additionalKeywords = HashMultimap.create();

        // enrich keyword map with plural terms
        doseForms.keywordMap.entries().forEach(entry -> {
            Matcher endsWithSMatcher = endsWithS.matcher(entry.getValue());
            Matcher endsWithLyOrRyMatcher = endsWithLyOrRy.matcher(entry.getValue());

            if (!endsWithSMatcher.find()) {
                if (endsWithLyOrRyMatcher.find()) {
                    additionalKeywords.put(entry.getKey(), endsWithLyOrRyMatcher.replaceFirst("ies"));
                } else {
                    additionalKeywords.put(entry.getKey(), entry.getValue().concat("s"));
                }
            }
        });

        doseForms.keywordMap.putAll(additionalKeywords);

        doseForms.trie = Trie.builder()
                .ignoreCase()
                .ignoreOverlaps()
                .onlyWholeWords()
                .addKeywords(doseForms.keywordMap.values())
                .build();

        getContext().getLogger().log(Level.INFO, "Built dose form trie using "
                + doseForms.getKeywordSize() + " keywords against "
                + doseForms.getConceptSize() + " concepts."
        );
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void process(JCas jcas) {
        jcas.getAnnotationIndex(Drug.type).forEach(annotation -> {
            Drug drug = (Drug) annotation;

            if (drug.getAttrs() != null) {

                List<MedAttr> forms = Streams.stream(drug.getAttrs())
                        .map(MedAttr.class::cast)
                        .filter(attribute -> attribute.getTag().contentEquals(FhirQueryUtils.MedAttrConstants.FORM))
                        .collect(ImmutableList.toImmutableList());

                List<MedAttr> routes = Streams.stream(drug.getAttrs())
                        .map(MedAttr.class::cast)
                        .filter(attribute -> attribute.getTag().contentEquals(FhirQueryUtils.MedAttrConstants.ROUTE))
                        .collect(ImmutableList.toImmutableList());

                AtomicBoolean oralContextFlag = new AtomicBoolean(false);
                AtomicBoolean vaginalContextFlag = new AtomicBoolean(false);
                AtomicBoolean rectalContextFlag = new AtomicBoolean(false);
                AtomicBoolean topicalContextFlag = new AtomicBoolean(false);

                if (!routes.isEmpty()) {
                    routes.forEach(routeAttr -> {
                        // route normalization
                        switch (routeAttr.getCoveredText().toLowerCase()) {
                            case "mouth":
                            case "oral":
                            case "orally":
                            case "po":
                            case "p.o.":
                                oralContextFlag.compareAndSet(false, true);
                                break;
                            case "vaginally":
                            case "pv":
                                vaginalContextFlag.compareAndSet(false, true);
                                break;
                            case "rectally":
                            case "anally":
                            case "pr":
                            case "p.r.":
                                rectalContextFlag.compareAndSet(false, true);
                                break;
                            case "skin":
                            case "topical":
                            case "topically":
                                topicalContextFlag.compareAndSet(false, true);
                                break;
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
                            .replaceAll(punctuationOrWhitespace.toString(), " ");

                    String inferredDosageForm = sanitizedDosageForm;

                    Emit formEmit;

                    if (doseForms.trie.containsMatch(sanitizedDosageForm)) {
                        formEmit = doseForms.trie.firstMatch(sanitizedDosageForm);

                    } else {

                        if (oralContextFlag.get()) {
                            inferredDosageForm = "oral " + inferredDosageForm;
                        } else if (vaginalContextFlag.get()) {
                            inferredDosageForm = "vaginal " + inferredDosageForm;
                        } else if (rectalContextFlag.get()) {
                            inferredDosageForm = "rectal " + inferredDosageForm;
                        } else if (topicalContextFlag.get()) {
                            inferredDosageForm = "topical " + inferredDosageForm;
                        }

                        formEmit = doseForms.trie.firstMatch(inferredDosageForm);
                    }

                    if (formEmit != null) {
                        String code = FhirQueryUtils
                                .getCodeFromKeywordMap(doseForms.keywordMap, formEmit.getKeyword());
                        drug.setForm(code);
                    }
                }
            }
        });
    }
}