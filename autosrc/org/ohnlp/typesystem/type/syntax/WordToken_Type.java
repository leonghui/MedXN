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

/* First created by JCasGen Sun Feb 03 22:13:25 SGT 2019 */
package org.ohnlp.typesystem.type.syntax;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** Differentiates a token as being a word rather than a punctuation, symbol, newline, contraction, or number.
Equivalent to Mayo cTAKES version 2.5: edu.mayo.bmi.uima.core.type.WordToken
 * Updated by JCasGen Sun Feb 03 22:13:25 SGT 2019
 * @generated */
public class WordToken_Type extends BaseToken_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = WordToken.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.ohnlp.typesystem.type.syntax.WordToken");
 
  /** @generated */
  final Feature casFeat_capitalization;
  /** @generated */
  final int     casFeatCode_capitalization;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getCapitalization(int addr) {
        if (featOkTst && casFeat_capitalization == null)
      jcas.throwFeatMissing("capitalization", "org.ohnlp.typesystem.type.syntax.WordToken");
    return ll_cas.ll_getIntValue(addr, casFeatCode_capitalization);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setCapitalization(int addr, int v) {
        if (featOkTst && casFeat_capitalization == null)
      jcas.throwFeatMissing("capitalization", "org.ohnlp.typesystem.type.syntax.WordToken");
    ll_cas.ll_setIntValue(addr, casFeatCode_capitalization, v);}
    
  
 
  /** @generated */
  final Feature casFeat_numPosition;
  /** @generated */
  final int     casFeatCode_numPosition;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getNumPosition(int addr) {
        if (featOkTst && casFeat_numPosition == null)
      jcas.throwFeatMissing("numPosition", "org.ohnlp.typesystem.type.syntax.WordToken");
    return ll_cas.ll_getIntValue(addr, casFeatCode_numPosition);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setNumPosition(int addr, int v) {
        if (featOkTst && casFeat_numPosition == null)
      jcas.throwFeatMissing("numPosition", "org.ohnlp.typesystem.type.syntax.WordToken");
    ll_cas.ll_setIntValue(addr, casFeatCode_numPosition, v);}
    
  
 
  /** @generated */
  final Feature casFeat_suggestion;
  /** @generated */
  final int     casFeatCode_suggestion;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getSuggestion(int addr) {
        if (featOkTst && casFeat_suggestion == null)
      jcas.throwFeatMissing("suggestion", "org.ohnlp.typesystem.type.syntax.WordToken");
    return ll_cas.ll_getStringValue(addr, casFeatCode_suggestion);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSuggestion(int addr, String v) {
        if (featOkTst && casFeat_suggestion == null)
      jcas.throwFeatMissing("suggestion", "org.ohnlp.typesystem.type.syntax.WordToken");
    ll_cas.ll_setStringValue(addr, casFeatCode_suggestion, v);}
    
  
 
  /** @generated */
  final Feature casFeat_canonicalForm;
  /** @generated */
  final int     casFeatCode_canonicalForm;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getCanonicalForm(int addr) {
        if (featOkTst && casFeat_canonicalForm == null)
      jcas.throwFeatMissing("canonicalForm", "org.ohnlp.typesystem.type.syntax.WordToken");
    return ll_cas.ll_getStringValue(addr, casFeatCode_canonicalForm);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setCanonicalForm(int addr, String v) {
        if (featOkTst && casFeat_canonicalForm == null)
      jcas.throwFeatMissing("canonicalForm", "org.ohnlp.typesystem.type.syntax.WordToken");
    ll_cas.ll_setStringValue(addr, casFeatCode_canonicalForm, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public WordToken_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_capitalization = jcas.getRequiredFeatureDE(casType, "capitalization", "uima.cas.Integer", featOkTst);
    casFeatCode_capitalization  = (null == casFeat_capitalization) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_capitalization).getCode();

 
    casFeat_numPosition = jcas.getRequiredFeatureDE(casType, "numPosition", "uima.cas.Integer", featOkTst);
    casFeatCode_numPosition  = (null == casFeat_numPosition) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_numPosition).getCode();

 
    casFeat_suggestion = jcas.getRequiredFeatureDE(casType, "suggestion", "uima.cas.String", featOkTst);
    casFeatCode_suggestion  = (null == casFeat_suggestion) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_suggestion).getCode();

 
    casFeat_canonicalForm = jcas.getRequiredFeatureDE(casType, "canonicalForm", "uima.cas.String", featOkTst);
    casFeatCode_canonicalForm  = (null == casFeat_canonicalForm) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_canonicalForm).getCode();

  }
}



    