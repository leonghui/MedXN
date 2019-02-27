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
import org.ahocorasick.trie.Trie;
import org.apache.uima.jcas.tcas.Annotation;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Reference;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FhirQueryUtils {

    private static Stream<String> getKeyStreamFromKeywordMap(SetMultimap<String, String> keywordMap, String keyword) {
        return keywordMap.entries().parallelStream()
                .filter(entry -> entry.getValue().toLowerCase().contentEquals(keyword.toLowerCase()))
                .map(Map.Entry::getKey);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static Set<String> getCodeFromKeywordMap(SetMultimap<String, String> keywordMap, String keyword) {
        return getKeyStreamFromKeywordMap(keywordMap, keyword)
                .collect(ImmutableSet.toImmutableSet());
    }

    @SuppressWarnings("UnstableApiUsage")
    public static Set<String> getAllCodesFromKeywordMap(SetMultimap<String, String> keywordMap, String keyword) {
        return getKeyStreamFromKeywordMap(keywordMap, keyword)
                .collect(ImmutableSet.toImmutableSet());
    }

    @SuppressWarnings("UnstableApiUsage")
    public static Set<Medication> getMedicationsFromCode(
            Map<String, Medication> medications, Collection<String> codes) {
        return codes.stream()
                .map(medications::get)
                .collect(ImmutableSet.toImmutableSet());
    }

    @SuppressWarnings("UnstableApiUsage")
    public static List<String> getIngredientIdsFromMedication(Medication medication) {
        return medication.getIngredient().stream()
                .map(Medication.MedicationIngredientComponent::getItem)
                .filter(Objects::nonNull)
                .map(Reference.class::cast)
                .map(Reference::getReference)
                .map(string -> string.split("/")[1])
                .map(string -> string.split("rxNorm-")[1])
                .collect(ImmutableList.toImmutableList());
    }

    @SuppressWarnings("UnstableApiUsage")
    public static List<String> getIngredientIdsFromMedications(Collection<Medication> medications) {
        return medications.parallelStream()
                .flatMap(medication -> medication.getIngredient().stream()
                        .map(Medication.MedicationIngredientComponent::getItem)
                        .filter(Objects::nonNull)
                        .map(Reference.class::cast)
                        .map(Reference::getReference)
                        .map(string -> string.split("/")[1]))
                        .map(string -> string.split("rxNorm-")[1])
                .collect(ImmutableList.toImmutableList());
    }

    @SuppressWarnings("UnstableApiUsage")
    public static List<String> getCoveredTextFromAnnotations(Collection<? extends Annotation> annotations) {
        return annotations.stream()
                .map(Annotation::getCoveredText)
                .collect(ImmutableList.toImmutableList());
    }

    @SuppressWarnings("UnstableApiUsage")
    public static String getDisplayNameFromMedications(Collection<? extends Medication> medications) {
        return medications.stream()
                .map(Medication::getCode)
                .map(CodeableConcept::getCodingFirstRep)
                .map(Coding::getDisplay)
                .collect(Collectors.joining(", "));
    }

    @SuppressWarnings("UnstableApiUsage")
    public static Map<String, String> getDosageFormMap(FhirQueryClient queryClient) {

        return queryClient.getAllMedications().values().parallelStream()
                .map(Medication::getForm)
                .map(CodeableConcept::getCodingFirstRep)
                .filter(coding -> coding.getCode() != null)
                .collect(ImmutableMap.toImmutableMap(
                        Coding::getCode,
                        Coding::getDisplay,
                        (c1, c2) -> c1
                ));
    }

    public static class MedAttrConstants {
        public static final String STRENGTH = "strength";
        public static final String FORM = "form";
        public static final String ROUTE = "route";
        public static final String TIME = "time";
        public static final String VOLUME = "volume";
        public static final String FREQUENCY = "frequency";
    }

    public static class LookupTable {
        // Data structure to store keywords
        // code, keyword
        public final SetMultimap<String, String> keywordMap = HashMultimap.create();

        // Data structure to store the trie
        public Trie trie;

        public int getConceptSize() {
            return keywordMap.keySet().size();
        }

        public int getKeywordSize() {
            return keywordMap.values().size();
        }
    }
}
