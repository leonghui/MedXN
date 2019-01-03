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

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.ohnlp.typesystem.type.textspan.Segment;
import org.ohnlp.typesystem.type.textspan.Sentence;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Table;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.resource.ResourceAccessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.ohnlp.medtagger.type.ConceptMention;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;

/**
 * AhoCorasick string matching algorithm to find medication name Lower-cased
 * exact matching
 * 
 * @author Hongfang Liu, Sunghwan Sohn, Leong Hui Wong
 */
public class ACLookupDrugAnnotator extends JCasAnnotator_ImplBase {

	// Data structure to store keywords
	// keyword, rxCui
	private SetMultimap<String, String> keywordMap = LinkedHashMultimap.create();

	// Data structure to store concept terms
	// rxCui, tty, term
	private Table<String, String, String> conceptTable = HashBasedTable.create();

	// LOG4J logger based on class name
	private Logger logger = Logger.getLogger(getClass().getName());

	// data structure that stores the TRIE
	private Trie trie;

	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);
		logger.setLevel(Level.DEBUG);

		try {
			// LH: Build Aho-Corasick trie using IN and BN
			// TODO Switch to FHIR query
			URI dictUri = aContext.getResourceURI("RxNorm_BNIN");
			Path dictionary = Paths.get(dictUri);

			try {
				Files.lines(dictionary).forEach(line -> {
					String[] parts = line.split("\\|"); // escape twice for pipe separator
					String keyword = parts[0].replaceAll("\\s+", " ");
					String rxCui = parts[1];
					String tty = parts[2];
					String term = parts[3];

					keywordMap.put(rxCui, keyword);
					conceptTable.put(rxCui, tty, term);
				});
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			trie = Trie.builder().ignoreCase().onlyWholeWordsWhiteSpaceSeparated() // exact match
					.addKeywords(keywordMap.values()).build();

		} catch (ResourceAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		JFSIndexRepository indexes = jCas.getJFSIndexRepository();
		Iterator<?> segItr = indexes.getAnnotationIndex(Segment.type).iterator();

		while (segItr.hasNext()) {
			Segment seg = (Segment) segItr.next();

			Iterator<?> sentItr = indexes.getAnnotationIndex(Sentence.type).subiterator(seg);

			while (sentItr.hasNext()) {

				Sentence sent = (Sentence) sentItr.next();

				// LH: Populate CAS with matched tokens
				// TODO Retrieve code attributes from FHIR
				// TODO Investigate downstream use of "::" and other separators

				String sentText = sent.getCoveredText().toLowerCase().replaceAll("\\s+", " ") // replace all whitespace characters with a space
						.replaceAll("(\\p{Punct})", " "); // replace all punctuations with a space
				//		.replaceAll("(?<=\\w+)(\\p{Punct})", " "); // replace all punctuations after a word character with a space

				Collection<Emit> sentEmits = trie.parseText(sentText);

				for (Emit emit : sentEmits) {
					for (Map.Entry<String, String> entry : keywordMap.entries()) {
						if (entry.getValue().contentEquals(emit.getKeyword())) {
							String rxCui = entry.getKey();

							int begin = sent.getBegin() + emit.getStart();
							int end = sent.getBegin() + emit.getEnd() + 1;

							ConceptMention neAnnot = new ConceptMention(jCas, begin, end);

							Collection<String> terms = conceptTable.row(rxCui).values();

							BiMap<String, String> ttyMap = HashBiMap.create(conceptTable.row(rxCui));

							for (String term : terms) {
								neAnnot.setNormTarget(term); // Preferred Term
								neAnnot.setSemGroup(rxCui + "::" + ttyMap.inverse().get(term)); // RxCUI::TermType
								neAnnot.setSentence(sent);
								neAnnot.addToIndexes();
							}
						}
					}
				}
			}
		}
	}
}