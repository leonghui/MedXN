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

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.ohnlp.medxn.type.Drug;
import org.ohnlp.medxn.type.LookupWindow;
import org.ohnlp.medxn.type.MedAttr;
import org.ohnlp.typesystem.type.textspan.Sentence;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FhirMedExtAnnotator extends JCasAnnotator_ImplBase {

    @Override
    public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
        super.initialize(uimaContext);
    }

    @Override
    public void process(JCas jcas) {
        List<Sentence> sentences = new ArrayList<>();
        jcas.getAnnotationIndex(Sentence.type).forEach(annotation -> {
            Sentence sentence = (Sentence) annotation;
            sentences.add(sentence);
        });

        List<Drug> drugs = new ArrayList<>();
        jcas.getAnnotationIndex(Drug.type).forEach(annotation -> {
            Drug drug = (Drug) annotation;
            drugs.add(drug);
        });

        List<MedAttr> attributes = new ArrayList<>();
        jcas.getAnnotationIndex(MedAttr.type).forEach(annotation -> {
            MedAttr attribute = (MedAttr) annotation;
            attributes.add(attribute);
        });

        List<Drug> sortedDrugs = drugs.stream()
                .sorted(Comparator.comparingInt(Drug::getBegin).thenComparingInt(Drug::getEnd))
                .collect(Collectors.toList());

        IntStream.range(0, sortedDrugs.size()).forEach(index -> {
                    LookupWindow window = new LookupWindow(jcas);

                    Drug drug = sortedDrugs.get(index);
                    window.setBegin(drug.getBegin());

                    if (index < sortedDrugs.size() - 1) {
                        Drug nextDrug = sortedDrugs.get(index + 1);
                        window.setEnd(nextDrug.getBegin() - 1);
                    } else {
                        int end = sentences.stream()
                                .filter(sentence ->
                                        drug.getBegin() >= sentence.getBegin() &&
                                                drug.getEnd() <= sentence.getEnd())
                                .map(Sentence::getEnd)
                                .findFirst()
                                .orElse(Integer.MAX_VALUE);

                        window.setEnd(end);
                    }

                    window.addToIndexes();

                    List<MedAttr> filteredAttributes = attributes.stream()
                            .filter(attribute ->
                                    attribute.getBegin() >= window.getBegin() &&
                                            attribute.getEnd() <= window.getEnd())
                            .collect(Collectors.toList());

                    FSArray attributesArray = new FSArray(jcas, filteredAttributes.size());

                    IntStream.range(0, filteredAttributes.size())
                            .forEach(attributeIndex ->
                                    attributesArray.set(attributeIndex, filteredAttributes.get(attributeIndex))
                            );

                    drug.setAttrs(attributesArray);
                }
        );
    }
}
