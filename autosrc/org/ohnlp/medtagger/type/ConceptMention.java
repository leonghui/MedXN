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

/* First created by JCasGen Sun Feb 03 22:13:24 SGT 2019 */
package org.ohnlp.medtagger.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.ohnlp.typesystem.type.textspan.Sentence;
import org.apache.uima.jcas.tcas.Annotation;


/** Concept mention stands for concepts detected by the NLP system
 * Updated by JCasGen Sun Feb 03 22:13:24 SGT 2019
 * XML source: /medxn/src/main/resources/org/ohnlp/medtagger/types/MedTaggerTypes.xml
 * @generated */
public class ConceptMention extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(ConceptMention.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected ConceptMention() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public ConceptMention(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public ConceptMention(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public ConceptMention(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: detectionMethod

  /** getter for detectionMethod - gets There are multiple approaches to detect concept mentions including: dictionary lookup, 
  machine learning approaches trained using different training corpus 
  (i2b2 2010 Concept Mention, or SHARE 2013 concept mention corpus).
   * @generated
   * @return value of the feature 
   */
  public String getDetectionMethod() {
    if (ConceptMention_Type.featOkTst && ((ConceptMention_Type)jcasType).casFeat_detectionMethod == null)
      jcasType.jcas.throwFeatMissing("detectionMethod", "org.ohnlp.medtagger.type.ConceptMention");
    return jcasType.ll_cas.ll_getStringValue(addr, ((ConceptMention_Type)jcasType).casFeatCode_detectionMethod);}
    
  /** setter for detectionMethod - sets There are multiple approaches to detect concept mentions including: dictionary lookup, 
  machine learning approaches trained using different training corpus 
  (i2b2 2010 Concept Mention, or SHARE 2013 concept mention corpus). 
   * @generated
   * @param v value to set into the feature 
   */
  public void setDetectionMethod(String v) {
    if (ConceptMention_Type.featOkTst && ((ConceptMention_Type)jcasType).casFeat_detectionMethod == null)
      jcasType.jcas.throwFeatMissing("detectionMethod", "org.ohnlp.medtagger.type.ConceptMention");
    jcasType.ll_cas.ll_setStringValue(addr, ((ConceptMention_Type)jcasType).casFeatCode_detectionMethod, v);}    
   
    
  //*--------------*
  //* Feature: normTarget

  /** getter for normTarget - gets This corresponds to the preferred names of the corresponding concepts. Currently, 
  it is chosen as the most popular synonyms appearing in the clinical corpora.
   * @generated
   * @return value of the feature 
   */
  public String getNormTarget() {
    if (ConceptMention_Type.featOkTst && ((ConceptMention_Type)jcasType).casFeat_normTarget == null)
      jcasType.jcas.throwFeatMissing("normTarget", "org.ohnlp.medtagger.type.ConceptMention");
    return jcasType.ll_cas.ll_getStringValue(addr, ((ConceptMention_Type)jcasType).casFeatCode_normTarget);}
    
  /** setter for normTarget - sets This corresponds to the preferred names of the corresponding concepts. Currently, 
  it is chosen as the most popular synonyms appearing in the clinical corpora. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setNormTarget(String v) {
    if (ConceptMention_Type.featOkTst && ((ConceptMention_Type)jcasType).casFeat_normTarget == null)
      jcasType.jcas.throwFeatMissing("normTarget", "org.ohnlp.medtagger.type.ConceptMention");
    jcasType.ll_cas.ll_setStringValue(addr, ((ConceptMention_Type)jcasType).casFeatCode_normTarget, v);}    
   
    
  //*--------------*
  //* Feature: Certainty

  /** getter for Certainty - gets This refers to the certainty context. The definition is consistent with  
  Context: see http://orbit.nlm.nih.gov/resource/context
   * @generated
   * @return value of the feature 
   */
  public String getCertainty() {
    if (ConceptMention_Type.featOkTst && ((ConceptMention_Type)jcasType).casFeat_Certainty == null)
      jcasType.jcas.throwFeatMissing("Certainty", "org.ohnlp.medtagger.type.ConceptMention");
    return jcasType.ll_cas.ll_getStringValue(addr, ((ConceptMention_Type)jcasType).casFeatCode_Certainty);}
    
  /** setter for Certainty - sets This refers to the certainty context. The definition is consistent with  
  Context: see http://orbit.nlm.nih.gov/resource/context 
   * @generated
   * @param v value to set into the feature 
   */
  public void setCertainty(String v) {
    if (ConceptMention_Type.featOkTst && ((ConceptMention_Type)jcasType).casFeat_Certainty == null)
      jcasType.jcas.throwFeatMissing("Certainty", "org.ohnlp.medtagger.type.ConceptMention");
    jcasType.ll_cas.ll_setStringValue(addr, ((ConceptMention_Type)jcasType).casFeatCode_Certainty, v);}    
   
    
  //*--------------*
  //* Feature: semGroup

  /** getter for semGroup - gets Semantic groups of the corresponding concepts. 
  Adapted from SemGroup defined in the UMLS. See: http://semanticnetwork.nlm.nih.gov/SemGroups/
   * @generated
   * @return value of the feature 
   */
  public String getSemGroup() {
    if (ConceptMention_Type.featOkTst && ((ConceptMention_Type)jcasType).casFeat_semGroup == null)
      jcasType.jcas.throwFeatMissing("semGroup", "org.ohnlp.medtagger.type.ConceptMention");
    return jcasType.ll_cas.ll_getStringValue(addr, ((ConceptMention_Type)jcasType).casFeatCode_semGroup);}
    
  /** setter for semGroup - sets Semantic groups of the corresponding concepts. 
  Adapted from SemGroup defined in the UMLS. See: http://semanticnetwork.nlm.nih.gov/SemGroups/ 
   * @generated
   * @param v value to set into the feature 
   */
  public void setSemGroup(String v) {
    if (ConceptMention_Type.featOkTst && ((ConceptMention_Type)jcasType).casFeat_semGroup == null)
      jcasType.jcas.throwFeatMissing("semGroup", "org.ohnlp.medtagger.type.ConceptMention");
    jcasType.ll_cas.ll_setStringValue(addr, ((ConceptMention_Type)jcasType).casFeatCode_semGroup, v);}    
   
    
  //*--------------*
  //* Feature: status

  /** getter for status - gets This refers to the status context. The definition is consist with  
  Context: see http://orbit.nlm.nih.gov/resource/context
   * @generated
   * @return value of the feature 
   */
  public String getStatus() {
    if (ConceptMention_Type.featOkTst && ((ConceptMention_Type)jcasType).casFeat_status == null)
      jcasType.jcas.throwFeatMissing("status", "org.ohnlp.medtagger.type.ConceptMention");
    return jcasType.ll_cas.ll_getStringValue(addr, ((ConceptMention_Type)jcasType).casFeatCode_status);}
    
  /** setter for status - sets This refers to the status context. The definition is consist with  
  Context: see http://orbit.nlm.nih.gov/resource/context 
   * @generated
   * @param v value to set into the feature 
   */
  public void setStatus(String v) {
    if (ConceptMention_Type.featOkTst && ((ConceptMention_Type)jcasType).casFeat_status == null)
      jcasType.jcas.throwFeatMissing("status", "org.ohnlp.medtagger.type.ConceptMention");
    jcasType.ll_cas.ll_setStringValue(addr, ((ConceptMention_Type)jcasType).casFeatCode_status, v);}    
   
    
  //*--------------*
  //* Feature: sentence

  /** getter for sentence - gets The sentence containing the concept mention
   * @generated
   * @return value of the feature 
   */
  public Sentence getSentence() {
    if (ConceptMention_Type.featOkTst && ((ConceptMention_Type)jcasType).casFeat_sentence == null)
      jcasType.jcas.throwFeatMissing("sentence", "org.ohnlp.medtagger.type.ConceptMention");
    return (Sentence)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((ConceptMention_Type)jcasType).casFeatCode_sentence)));}
    
  /** setter for sentence - sets The sentence containing the concept mention 
   * @generated
   * @param v value to set into the feature 
   */
  public void setSentence(Sentence v) {
    if (ConceptMention_Type.featOkTst && ((ConceptMention_Type)jcasType).casFeat_sentence == null)
      jcasType.jcas.throwFeatMissing("sentence", "org.ohnlp.medtagger.type.ConceptMention");
    jcasType.ll_cas.ll_setRefValue(addr, ((ConceptMention_Type)jcasType).casFeatCode_sentence, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: experiencer

  /** getter for experiencer - gets This refers to the status context. The definition is consist with  
  Context: see http://orbit.nlm.nih.gov/resource/context
   * @generated
   * @return value of the feature 
   */
  public String getExperiencer() {
    if (ConceptMention_Type.featOkTst && ((ConceptMention_Type)jcasType).casFeat_experiencer == null)
      jcasType.jcas.throwFeatMissing("experiencer", "org.ohnlp.medtagger.type.ConceptMention");
    return jcasType.ll_cas.ll_getStringValue(addr, ((ConceptMention_Type)jcasType).casFeatCode_experiencer);}
    
  /** setter for experiencer - sets This refers to the status context. The definition is consist with  
  Context: see http://orbit.nlm.nih.gov/resource/context 
   * @generated
   * @param v value to set into the feature 
   */
  public void setExperiencer(String v) {
    if (ConceptMention_Type.featOkTst && ((ConceptMention_Type)jcasType).casFeat_experiencer == null)
      jcasType.jcas.throwFeatMissing("experiencer", "org.ohnlp.medtagger.type.ConceptMention");
    jcasType.ll_cas.ll_setStringValue(addr, ((ConceptMention_Type)jcasType).casFeatCode_experiencer, v);}    
  }

    