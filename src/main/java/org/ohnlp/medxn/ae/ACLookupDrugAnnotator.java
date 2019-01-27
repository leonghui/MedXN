/*******************************************************************************
 * Copyright: (c)  2013  Mayo Foundation for Medical Education and
 *  Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 *  triple-shield Mayo logo are trademarks and service marks of MFMER.
 *
 *  Except as contained in the copyright notice above, or as used to identify
 *  MFMER as the author of this software, the trade names, trademarks, service
 *  marks, or product names of the copyright holder shall not be used in
 *  advertising, promotion or otherwise in connection with this software without
 *  prior written authorization of the copyright holder.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *******************************************************************************/
package org.ohnlp.medxn.ae;

import com.google.common.collect.*;
import org.ahocorasick.trie.Trie;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Extension;
import org.ohnlp.medtagger.type.ConceptMention;
import org.ohnlp.medxn.fhir.FhirQueryClient;
import org.ohnlp.typesystem.type.textspan.Segment;
import org.ohnlp.typesystem.type.textspan.Sentence;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * AhoCorasick string matching algorithm to find medication name Lower-cased
 * exact matching
 *
 * @author Hongfang Liu, Sunghwan Sohn, Leong Hui Wong
 */
public class ACLookupDrugAnnotator extends JCasAnnotator_ImplBase {

    // Data structure to store keywords
    // rxCui, keyword
    private final SetMultimap<String, String> keywordMap = LinkedHashMultimap.create();

    // Data structure to store concept terms
    // rxCui, tty, term
    private final Table<String, String, String> conceptTable = HashBasedTable.create();

    // LOG4J logger based on class name
    private final Logger logger = LogManager.getLogger(getClass().getName());

    // data structure that stores the TRIE
    private Trie trie;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);
        Configurator.setLevel(logger.getName(), Level.DEBUG);

        // Build Aho-Corasick trie using IN and BN
        FhirQueryClient queryClient = FhirQueryClient.createFhirQueryClient();

        queryClient
            .getAllSubstances()
            .forEach( substance -> {
            Coding coding = substance.getCode().getCodingFirstRep();
                keywordMap.put(coding.getCode(), coding.getDisplay().toLowerCase());
                conceptTable.put(coding.getCode(), "IN", coding.getDisplay());
            });

        queryClient
            .getAllMedications()
            .forEach( medication -> {
                List<Extension> brandExtensions = medication
                    .getExtensionsByUrl(queryClient.getFhirServerUrl() + "/StructureDefinition/brand");
                brandExtensions.forEach( brandExtension -> {
                    CodeableConcept brandName = (CodeableConcept) brandExtension.getValue();
                    String brandTerm = brandName.getCodingFirstRep().getDisplay();
                    String brandCode = brandName.getCodingFirstRep().getCode();
                    keywordMap.put(brandCode, brandTerm.toLowerCase());
                    conceptTable.put(brandCode, "BN", brandTerm);
                });
            });

        trie = Trie.builder().ignoreCase().onlyWholeWordsWhiteSpaceSeparated() // exact match
                .addKeywords(keywordMap.values()).build();

    }

    @Override
    public void process(JCas jCas) {
        JFSIndexRepository indexes = jCas.getJFSIndexRepository();

        for (Annotation annotation : indexes.getAnnotationIndex(Segment.type)) {
            Segment seg = (Segment) annotation;

            Iterator<?> sentItr = indexes.getAnnotationIndex(Sentence.type).subiterator(seg);

            while (sentItr.hasNext()) {

                Sentence sent = (Sentence) sentItr.next();

                // LH: Populate CAS with matched tokens
                // TODO Retrieve code attributes from FHIR
                // TODO Investigate downstream use of "::" and other separators

                String sentText = sent.getCoveredText().toLowerCase()
                        .replaceAll("\\s+", " ") // replace all whitespace characters with a space
                        .replaceAll("(\\p{Punct})", " "); // replace all punctuations with a space
                //		.replaceAll("(?<=\\w+)(\\p{Punct})", " "); // replace all punctuations after a word character with a space

                trie.parseText(sentText)
                    .forEach( emit -> {
                        String rxCui = keywordMap.entries()
                                .stream()
                                .filter(entry -> entry.getValue().contentEquals(emit.getKeyword()))
                                .map(Map.Entry::getKey)
                                .findFirst()
                                .orElse(null);

                        if (rxCui != null && !conceptTable.row(rxCui).isEmpty()) {

                            int begin = sent.getBegin() + emit.getStart();
                            int end = sent.getBegin() + emit.getEnd() + 1;

                            ConceptMention neAnnot = new ConceptMention(jCas, begin, end);

                            BiMap<String, String> ttyMap = HashBiMap.create(conceptTable.row(rxCui));

                            conceptTable
                                    .row(rxCui)
                                    .values()
                                    .forEach(term -> {
                                        neAnnot.setNormTarget(term); // Preferred Term
                                        neAnnot.setSemGroup(rxCui + "::" + ttyMap.inverse().get(term)); // RxCUI::TermType
                                        neAnnot.setSentence(sent);
                                        neAnnot.addToIndexes();
                                    });
                        }

                    });
            }
        }
    }
}