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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import org.ahocorasick.trie.Trie;
import org.apache.uima.jcas.tcas.Annotation;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Medication;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

public class FhirQueryUtils {

    private static Stream<String> getKeyStreamFromKeywordMap(SetMultimap<String, String> keywordMap, String keyword) {
        return keywordMap.entries().parallelStream()
                .filter(entry -> entry.getValue().toLowerCase().contentEquals(keyword.toLowerCase()))
                .map(Map.Entry::getKey);
    }

    public static String getRxCuiFromKeywordMap(SetMultimap<String, String> keywordMap, String keyword) {
        return getKeyStreamFromKeywordMap(keywordMap, keyword)
                .findFirst()
                .orElse(null);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static ImmutableSet<String> getAllRxCuisFromKeywordMap(SetMultimap<String, String> keywordMap, String keyword) {
        return getKeyStreamFromKeywordMap(keywordMap, keyword)
                .collect(ImmutableSet.toImmutableSet());
    }

    @SuppressWarnings("UnstableApiUsage")
    public static ImmutableSet<Medication> getMedicationsFromRxCui(Collection<Medication> allMedications, Collection<String> rxCuis) {
        return allMedications.parallelStream()
                .filter(medication -> {
                    Coding medicationCoding = medication.getCode().getCodingFirstRep();
                    return rxCuis.contains(medicationCoding.getCode());
                })
                .collect(ImmutableSet.toImmutableSet());
    }

    @SuppressWarnings("UnstableApiUsage")
    public static ImmutableSet<String> getIngredientsFromMedication(Medication medication) {
        return medication.getIngredient().parallelStream()
                .map(Medication.MedicationIngredientComponent::getItem)
                .map(CodeableConcept.class::cast)
                .map(CodeableConcept::getCodingFirstRep)
                .map(Coding::getCode)
                .collect(ImmutableSet.toImmutableSet());
    }

    @SuppressWarnings("UnstableApiUsage")
    public static ImmutableSet<String> getIngredientsFromMedications(Collection<Medication> allMedications) {
        return allMedications.parallelStream()
                .flatMap(medication -> medication.getIngredient().parallelStream()
                        .map(Medication.MedicationIngredientComponent::getItem))
                .map(CodeableConcept.class::cast)
                .map(CodeableConcept::getCodingFirstRep)
                .map(Coding::getCode)
                .collect(ImmutableSet.toImmutableSet());
    }

    @SuppressWarnings("UnstableApiUsage")
    public static ImmutableList<String> getCoveredTextFromAnnotations(Collection<? extends Annotation> annotations) {
        return annotations.stream()
                .map(Annotation::getCoveredText)
                .collect(ImmutableList.toImmutableList());
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
        // rxCui, keyword
        public final SetMultimap<String, String> keywordMap = LinkedHashMultimap.create();

        // Data structure to store the trie
        public Trie trie;
    }
}
