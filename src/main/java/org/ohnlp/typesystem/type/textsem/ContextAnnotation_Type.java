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
package org.ohnlp.typesystem.type.textsem;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** Contextual information of an entity. Equivalent to Mayo cTAKES version 2.5: edu.mayo.bmi.uima.context.type.ContextAnnotation
 * Updated by JCasGen Sun Feb 03 22:13:25 SGT 2019
 * @generated */
public class ContextAnnotation_Type extends IdentifiedAnnotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = ContextAnnotation.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.ohnlp.typesystem.type.textsem.ContextAnnotation");
 
  /** @generated */
  final Feature casFeat_FocusText;
  /** @generated */
  final int     casFeatCode_FocusText;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getFocusText(int addr) {
        if (featOkTst && casFeat_FocusText == null)
      jcas.throwFeatMissing("FocusText", "org.ohnlp.typesystem.type.textsem.ContextAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_FocusText);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setFocusText(int addr, String v) {
        if (featOkTst && casFeat_FocusText == null)
      jcas.throwFeatMissing("FocusText", "org.ohnlp.typesystem.type.textsem.ContextAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_FocusText, v);}
    
  
 
  /** @generated */
  final Feature casFeat_Scope;
  /** @generated */
  final int     casFeatCode_Scope;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getScope(int addr) {
        if (featOkTst && casFeat_Scope == null)
      jcas.throwFeatMissing("Scope", "org.ohnlp.typesystem.type.textsem.ContextAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_Scope);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setScope(int addr, String v) {
        if (featOkTst && casFeat_Scope == null)
      jcas.throwFeatMissing("Scope", "org.ohnlp.typesystem.type.textsem.ContextAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_Scope, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public ContextAnnotation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_FocusText = jcas.getRequiredFeatureDE(casType, "FocusText", "uima.cas.String", featOkTst);
    casFeatCode_FocusText  = (null == casFeat_FocusText) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_FocusText).getCode();

 
    casFeat_Scope = jcas.getRequiredFeatureDE(casType, "Scope", "uima.cas.String", featOkTst);
    casFeatCode_Scope  = (null == casFeat_Scope) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_Scope).getCode();

  }
}



    