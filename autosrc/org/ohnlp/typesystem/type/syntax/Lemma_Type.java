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
import org.apache.uima.jcas.cas.TOP_Type;

/** Stores a lemma (canonical form of a token).  Inherits from uima.cas.TOP, allowing for reuse of standardized forms across the CAS. 
Equivalent to Mayo cTAKES version 2.5: edu.mayo.bmi.uima.core.type.Lemma
 * Updated by JCasGen Sun Feb 03 22:13:25 SGT 2019
 * @generated */
public class Lemma_Type extends TOP_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Lemma.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.ohnlp.typesystem.type.syntax.Lemma");
 
  /** @generated */
  final Feature casFeat_key;
  /** @generated */
  final int     casFeatCode_key;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getKey(int addr) {
        if (featOkTst && casFeat_key == null)
      jcas.throwFeatMissing("key", "org.ohnlp.typesystem.type.syntax.Lemma");
    return ll_cas.ll_getStringValue(addr, casFeatCode_key);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setKey(int addr, String v) {
        if (featOkTst && casFeat_key == null)
      jcas.throwFeatMissing("key", "org.ohnlp.typesystem.type.syntax.Lemma");
    ll_cas.ll_setStringValue(addr, casFeatCode_key, v);}
    
  
 
  /** @generated */
  final Feature casFeat_posTag;
  /** @generated */
  final int     casFeatCode_posTag;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getPosTag(int addr) {
        if (featOkTst && casFeat_posTag == null)
      jcas.throwFeatMissing("posTag", "org.ohnlp.typesystem.type.syntax.Lemma");
    return ll_cas.ll_getStringValue(addr, casFeatCode_posTag);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setPosTag(int addr, String v) {
        if (featOkTst && casFeat_posTag == null)
      jcas.throwFeatMissing("posTag", "org.ohnlp.typesystem.type.syntax.Lemma");
    ll_cas.ll_setStringValue(addr, casFeatCode_posTag, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Lemma_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_key = jcas.getRequiredFeatureDE(casType, "key", "uima.cas.String", featOkTst);
    casFeatCode_key  = (null == casFeat_key) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_key).getCode();

 
    casFeat_posTag = jcas.getRequiredFeatureDE(casType, "posTag", "uima.cas.String", featOkTst);
    casFeatCode_posTag  = (null == casFeat_posTag) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_posTag).getCode();

  }
}



    