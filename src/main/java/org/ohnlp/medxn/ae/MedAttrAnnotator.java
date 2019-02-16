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
import org.ahocorasick.trie.Trie;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceAccessException;
import org.apache.uima.resource.ResourceInitializationException;
import org.ohnlp.medxn.fhir.FhirQueryClient;
import org.ohnlp.medxn.fhir.FhirQueryUtils;
import org.ohnlp.medxn.type.MedAttr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 *
 * @author Sunghwan Sohn, Leong Hui Wong
 * Extract medication attributes defined in regExPatterns
 */
public class MedAttrAnnotator extends JCasAnnotator_ImplBase {
	private final FhirQueryUtils.LookupTable doseForms = new FhirQueryUtils.LookupTable();

	class Attribute {
		String tag;
		String text;
		int begin;
		int end;
	}

	private Map< String, List<String> > regExPat;

	public void initialize(UimaContext uimaContext) throws ResourceInitializationException {
		super.initialize(uimaContext);
		regExPat = new HashMap<>();

		// Get config parameter values
		String url = (String) uimaContext.getConfigParameterValue("FHIR_SERVER_URL");
		int timeout = (int) uimaContext.getConfigParameterValue("TIMEOUT_SEC");
		FhirQueryClient queryClient = FhirQueryClient.createFhirQueryClient(url, timeout);

		queryClient.getDosageFormMap().forEach((rxCui, term) -> {

			doseForms.keywordMap.put(rxCui, term.toLowerCase());

			// enrich keyword map with common abbreviations
			doseForms.keywordMap.put(rxCui, term.replaceAll("(?i)Tablet", "Tab").toLowerCase());
			doseForms.keywordMap.put(rxCui, term.replaceAll("(?i)Capsule", "Cap").toLowerCase());
			doseForms.keywordMap.put(rxCui, term.replaceAll("(?i)Injection|Injectable", "Inj").toLowerCase());
			doseForms.keywordMap.put(rxCui, term.replaceAll("(?i)Topical", "Top").toLowerCase());
			doseForms.keywordMap.put(rxCui, term.replaceAll("(?i)Cream", "Crm").toLowerCase());
			doseForms.keywordMap.put(rxCui, term.replaceAll("(?i)Ointment", "Oint").toLowerCase());
			doseForms.keywordMap.put(rxCui, term.replaceAll("(?i)Suppository", "Supp").toLowerCase());
		});

		List<String> commonOmittedWords = Arrays.asList(
				"Oral",
				"Topical",
				"Ophthalmic",
				"Otic",
				"Rectal",
				"Vaginal",
				"Nasal",
				"Sublingual",
				"Dry Powder",
				"Metered Dose"
		);

		SetMultimap<String, String> additionalKeywords = LinkedHashMultimap.create();

		doseForms.keywordMap.entries().forEach(
				entry -> commonOmittedWords.forEach(word -> {
					Pattern pattern = Pattern.compile(word, Pattern.CASE_INSENSITIVE);
					Matcher matcher = pattern.matcher(entry.getValue());
					if (matcher.find()) {
						additionalKeywords.put(entry.getKey(), matcher.replaceAll("").trim());
					}
				}));

		// enrich keyword map with plural terms
		doseForms.keywordMap.entries().forEach(entry -> {
			Pattern endsWithS = Pattern.compile("s$");
			Matcher endsWithSMatcher = endsWithS.matcher(entry.getValue());

			Pattern endsWithLyOrRy = Pattern.compile("(?<=[l|r])(y$)");
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

		try {
			InputStream in = getContext().getResourceAsStream("regExPatterns");
			regExPat = getRegEx(in);
		} catch (ResourceAccessException e) {
			e.printStackTrace();
		}
	}

	public void process(JCas jcas) {
		//String docName = DocumentIDAnnotationUtil.getDocumentID(jcas);
		//System.out.println("---"+docName+" MedAttrAnnotator processed---");

		String docText = jcas.getDocumentText();
		addToJCas2(jcas, removeOverlap(getAttribute2(docText)));
	}

	private void addToJCas2(JCas jcas, List<Attribute> annot) {
		for(Attribute attr : annot) {
			MedAttr ma = new MedAttr(jcas);
			ma.setTag(attr.tag);
			ma.setBegin(attr.begin);
			ma.setEnd(attr.end);
			ma.addToIndexes();
		}
	}

	/**
	 * Find and return medication attributes in text
	 * @param text String to extract attributes
	 * @return List of Attribute classes
	 */
	private List<Attribute> getAttribute2(String text) {
		List<Attribute> ret = new ArrayList<>();

		for(String tag : regExPat.keySet()) {
			int groupNumber = 0; //group number in regex
			String aTag = tag;
			if(tag.contains("%")) {
				String [] tokens = tag.split("%");
				aTag = tokens[0];
				groupNumber = Integer.parseInt(tokens[1]);
			}
			for(String regex : regExPat.get(tag)) {
				if (!tag.contentEquals("form")) {	// override form matching
					Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
					Matcher m = p.matcher(text);
					while (m.find()) {
						Attribute attr = new Attribute();
						attr.tag = aTag; //w/o group number
						attr.text = m.group(groupNumber);
						attr.begin = m.start(groupNumber);
						attr.end = m.end(groupNumber);
						ret.add(attr);
					}
				}
			}

			String sanitizedText = text
					.replaceAll("\\s+", " ") // replace all whitespace characters with a space
					.replaceAll("(\\p{Punct})", " "); // replace all punctuations with a space

			doseForms.trie.parseText(sanitizedText).forEach(emit -> {
				Attribute attr = new Attribute();
				attr.tag = "form";
				attr.text = emit.getKeyword();
				attr.begin = emit.getStart();
				attr.end = emit.getEnd() + 1;
				ret.add(attr);
			});

		}

		return ret;
	}

	/**
	 * Remove duplicates or take a longer attribute if subsumed
	 * and return the updated list of Attribute
	 * @param attr List of Attribute class
	 * @return List of Attribute class without duplicates/overlaps
	 */
	private List<Attribute> removeOverlap(List<Attribute> attr) {
		List<Attribute> ret = new ArrayList<>();
		List<Attribute> tmp = new ArrayList<>();
		Set<String> spans = new HashSet<>();

		//remove duplicates 
		for(Attribute a : attr) {
			String span = a.tag+"|"+a.begin+"|"+a.end;
			if(spans.contains(span)) continue;
			spans.add(span);
			tmp.add(a);
		}

		//if one is subsumed by another, use a longer one 
		//(CAUTION: duplicated instances will be removed all)
		//duplicates must be removed before this step
		boolean isOverlap;
		for(int i=0; i<tmp.size(); i++) {
			isOverlap = false;
			for(int j=0; j<tmp.size(); j++) {
				if(i==j) continue;
				if(tmp.get(i).tag.equals(tmp.get(j).tag) &&
						tmp.get(i).begin>=tmp.get(j).begin &&
						tmp.get(i).end<=tmp.get(j).end) {
					isOverlap = true;
					break;
				}
			}
			if(!isOverlap) ret.add(tmp.get(i));
		}

		return ret;
	}

	/**
	 * Return regular expression patterns for med attributes
	 * @param input file name of the regEx file
	 * @return Map of attribute regular expression (key:tag, val:List of regular expression patterns)
	 */
	private Map< String, List<String> > getRegEx(InputStream input) {
		//key:tag, val:List of regular expression patterns
		Map< String, List<String> > regexMap = new HashMap<>();
		if(input!=null) {
			try {
				BufferedReader fin = new BufferedReader(new InputStreamReader(input));
				String line;
				List<String> regexList; //regular expression patterns for attributes
				Map<String,String> varMap = new HashMap<>();
				while((line = fin.readLine())!= null) {
					if( line.startsWith("#")
							|| line.length()==0
							|| Character.isWhitespace(line.charAt(0)) )
						continue;

					//get variable definitions (MUST BE before regEx patterns in the file) 
					//eg) @STRENGTH_UNIT::mg/dl|mg/ml|g/l|milligrams
					if(line.startsWith("@")) {
						String [] tokens = line.split("::");
						String var = tokens[0].trim();
						String val = tokens[1].trim();
						varMap.put(var, val);
					}
					//get regEx patterns
					//eg) strength::\b(@DECIMAL_NUM/)?(@DECIMAL_NUM)(\s|-)?(@STRENGTH_UNIT)\b
					else {
						String [] strings = line.split("::");
						String tag = strings[0].trim();
						String patStr = strings[1].trim();

						for(String s : varMap.keySet()) {
							patStr = patStr.replaceAll(s,varMap.get(s));
						}

						regexList = regexMap.get(tag);
						if(regexList==null) regexList = new ArrayList<>();
						regexList.add(patStr);
						regexMap.put(tag, regexList);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return regexMap;
	}
}
