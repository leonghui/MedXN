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
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** Concept mention stands for concepts detected by the NLP system
 * Updated by JCasGen Sun Feb 03 22:13:24 SGT 2019
 * @generated */
public class ConceptMention_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = ConceptMention.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.ohnlp.medtagger.type.ConceptMention");
 
  /** @generated */
  final Feature casFeat_detectionMethod;
  /** @generated */
  final int     casFeatCode_detectionMethod;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getDetectionMethod(int addr) {
        if (featOkTst && casFeat_detectionMethod == null)
      jcas.throwFeatMissing("detectionMethod", "org.ohnlp.medtagger.type.ConceptMention");
    return ll_cas.ll_getStringValue(addr, casFeatCode_detectionMethod);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setDetectionMethod(int addr, String v) {
        if (featOkTst && casFeat_detectionMethod == null)
      jcas.throwFeatMissing("detectionMethod", "org.ohnlp.medtagger.type.ConceptMention");
    ll_cas.ll_setStringValue(addr, casFeatCode_detectionMethod, v);}
    
  
 
  /** @generated */
  final Feature casFeat_normTarget;
  /** @generated */
  final int     casFeatCode_normTarget;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getNormTarget(int addr) {
        if (featOkTst && casFeat_normTarget == null)
      jcas.throwFeatMissing("normTarget", "org.ohnlp.medtagger.type.ConceptMention");
    return ll_cas.ll_getStringValue(addr, casFeatCode_normTarget);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setNormTarget(int addr, String v) {
        if (featOkTst && casFeat_normTarget == null)
      jcas.throwFeatMissing("normTarget", "org.ohnlp.medtagger.type.ConceptMention");
    ll_cas.ll_setStringValue(addr, casFeatCode_normTarget, v);}
    
  
 
  /** @generated */
  final Feature casFeat_Certainty;
  /** @generated */
  final int     casFeatCode_Certainty;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getCertainty(int addr) {
        if (featOkTst && casFeat_Certainty == null)
      jcas.throwFeatMissing("Certainty", "org.ohnlp.medtagger.type.ConceptMention");
    return ll_cas.ll_getStringValue(addr, casFeatCode_Certainty);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setCertainty(int addr, String v) {
        if (featOkTst && casFeat_Certainty == null)
      jcas.throwFeatMissing("Certainty", "org.ohnlp.medtagger.type.ConceptMention");
    ll_cas.ll_setStringValue(addr, casFeatCode_Certainty, v);}
    
  
 
  /** @generated */
  final Feature casFeat_semGroup;
  /** @generated */
  final int     casFeatCode_semGroup;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getSemGroup(int addr) {
        if (featOkTst && casFeat_semGroup == null)
      jcas.throwFeatMissing("semGroup", "org.ohnlp.medtagger.type.ConceptMention");
    return ll_cas.ll_getStringValue(addr, casFeatCode_semGroup);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSemGroup(int addr, String v) {
        if (featOkTst && casFeat_semGroup == null)
      jcas.throwFeatMissing("semGroup", "org.ohnlp.medtagger.type.ConceptMention");
    ll_cas.ll_setStringValue(addr, casFeatCode_semGroup, v);}
    
  
 
  /** @generated */
  final Feature casFeat_status;
  /** @generated */
  final int     casFeatCode_status;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getStatus(int addr) {
        if (featOkTst && casFeat_status == null)
      jcas.throwFeatMissing("status", "org.ohnlp.medtagger.type.ConceptMention");
    return ll_cas.ll_getStringValue(addr, casFeatCode_status);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setStatus(int addr, String v) {
        if (featOkTst && casFeat_status == null)
      jcas.throwFeatMissing("status", "org.ohnlp.medtagger.type.ConceptMention");
    ll_cas.ll_setStringValue(addr, casFeatCode_status, v);}
    
  
 
  /** @generated */
  final Feature casFeat_sentence;
  /** @generated */
  final int     casFeatCode_sentence;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getSentence(int addr) {
        if (featOkTst && casFeat_sentence == null)
      jcas.throwFeatMissing("sentence", "org.ohnlp.medtagger.type.ConceptMention");
    return ll_cas.ll_getRefValue(addr, casFeatCode_sentence);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSentence(int addr, int v) {
        if (featOkTst && casFeat_sentence == null)
      jcas.throwFeatMissing("sentence", "org.ohnlp.medtagger.type.ConceptMention");
    ll_cas.ll_setRefValue(addr, casFeatCode_sentence, v);}
    
  
 
  /** @generated */
  final Feature casFeat_experiencer;
  /** @generated */
  final int     casFeatCode_experiencer;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getExperiencer(int addr) {
        if (featOkTst && casFeat_experiencer == null)
      jcas.throwFeatMissing("experiencer", "org.ohnlp.medtagger.type.ConceptMention");
    return ll_cas.ll_getStringValue(addr, casFeatCode_experiencer);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setExperiencer(int addr, String v) {
        if (featOkTst && casFeat_experiencer == null)
      jcas.throwFeatMissing("experiencer", "org.ohnlp.medtagger.type.ConceptMention");
    ll_cas.ll_setStringValue(addr, casFeatCode_experiencer, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public ConceptMention_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_detectionMethod = jcas.getRequiredFeatureDE(casType, "detectionMethod", "uima.cas.String", featOkTst);
    casFeatCode_detectionMethod  = (null == casFeat_detectionMethod) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_detectionMethod).getCode();

 
    casFeat_normTarget = jcas.getRequiredFeatureDE(casType, "normTarget", "uima.cas.String", featOkTst);
    casFeatCode_normTarget  = (null == casFeat_normTarget) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_normTarget).getCode();

 
    casFeat_Certainty = jcas.getRequiredFeatureDE(casType, "Certainty", "uima.cas.String", featOkTst);
    casFeatCode_Certainty  = (null == casFeat_Certainty) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_Certainty).getCode();

 
    casFeat_semGroup = jcas.getRequiredFeatureDE(casType, "semGroup", "uima.cas.String", featOkTst);
    casFeatCode_semGroup  = (null == casFeat_semGroup) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_semGroup).getCode();

 
    casFeat_status = jcas.getRequiredFeatureDE(casType, "status", "uima.cas.String", featOkTst);
    casFeatCode_status  = (null == casFeat_status) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_status).getCode();

 
    casFeat_sentence = jcas.getRequiredFeatureDE(casType, "sentence", "org.ohnlp.typesystem.type.textspan.Sentence", featOkTst);
    casFeatCode_sentence  = (null == casFeat_sentence) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_sentence).getCode();

 
    casFeat_experiencer = jcas.getRequiredFeatureDE(casType, "experiencer", "uima.cas.String", featOkTst);
    casFeatCode_experiencer  = (null == casFeat_experiencer) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_experiencer).getCode();

  }
}



    