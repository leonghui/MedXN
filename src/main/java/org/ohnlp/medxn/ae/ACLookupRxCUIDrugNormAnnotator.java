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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashMap;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceAccessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.ohnlp.medxn.type.Drug;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Table;

/**
 * AhoCorasick string matching algorithm to find normalized medication form
 * using RxCUI. Exact string matching.
 * 
 * @author Hongfang Liu, Sunghwan Sohn, Leong Hui Wong
 *
 */
public class ACLookupRxCUIDrugNormAnnotator extends JCasAnnotator_ImplBase {

	// Data structure to store keywords
	// keyword, rxCui
	private LinkedHashMap<String, String> keywordMap = new LinkedHashMap<>();

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
			// LH: Build Aho-Corasick trie
			// TODO Switch to FHIR query
			URI dictUri = aContext.getResourceURI("RxCUI");
			Path dictionary = Paths.get(dictUri);

			try {
				Files.lines(dictionary, Charset.forName("Cp1252")).forEach(line -> {
					String[] parts = line.split("\\|"); // escape twice for pipe separator
					String keyword = parts[0].replaceAll("\\s+", " ");
					String rxCui = parts[1];
					String tty = parts[2];
					String term = parts[3];

					keywordMap.put(keyword, rxCui);
					conceptTable.put(rxCui, tty, term);
				});
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			trie = Trie.builder().ignoreCase().onlyWholeWordsWhiteSpaceSeparated() // exact match
					.addKeywords(keywordMap.keySet()).build();

		} catch (ResourceAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void process(JCas jCas) {
		JFSIndexRepository indexes = jCas.getJFSIndexRepository();

		for (Annotation annotation : indexes.getAnnotationIndex(Drug.type)) {
			Drug med = (Drug) annotation;

			String text = med.getNormDrug2().replaceAll("<.*?>", " ").replaceAll("\\s+", " ").trim();

			// LH: Populate CAS with matched tokens
			// TODO Retrieve code attributes from FHIR
			// TODO Handle more than one Emit

			Collection<Emit> emits = trie.parseText(text);

			for (Emit emit : emits) {

				String rxCui = keywordMap.get(emit.getKeyword());

				if (!conceptTable.row(rxCui).isEmpty()) {
					med.setNormRxCui2(rxCui); // RxCUI

					Collection<String> terms = conceptTable.row(rxCui).values();

					// feature-parity: get longest term
					String longestTerm = "";
					for (String term : terms) {
						if (term.length() > longestTerm.length()) {
							longestTerm = term;
						}
					}

					BiMap<String, String> ttyMap = HashBiMap.create(conceptTable.row(rxCui));

					med.setNormRxType2(ttyMap.inverse().get(longestTerm)); // Term type
					med.setNormRxName2(longestTerm); // RxNorm name
				}
			}
		}
	}
}
