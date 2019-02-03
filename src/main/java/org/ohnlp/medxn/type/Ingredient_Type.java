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

/* First created by JCasGen Mon Feb 04 00:02:11 SGT 2019 */
package org.ohnlp.medxn.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** Identifies a particular constituent of interest in the product (FHIR STU3)
 * Updated by JCasGen Mon Feb 04 00:02:11 SGT 2019
 * @generated */
public class Ingredient_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Ingredient.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.ohnlp.medxn.type.Ingredient");
 
  /** @generated */
  final Feature casFeat_amountValue;
  /** @generated */
  final int     casFeatCode_amountValue;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public double getAmountValue(int addr) {
        if (featOkTst && casFeat_amountValue == null)
      jcas.throwFeatMissing("amountValue", "org.ohnlp.medxn.type.Ingredient");
    return ll_cas.ll_getDoubleValue(addr, casFeatCode_amountValue);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setAmountValue(int addr, double v) {
        if (featOkTst && casFeat_amountValue == null)
      jcas.throwFeatMissing("amountValue", "org.ohnlp.medxn.type.Ingredient");
    ll_cas.ll_setDoubleValue(addr, casFeatCode_amountValue, v);}
    
  
 
  /** @generated */
  final Feature casFeat_amountUnit;
  /** @generated */
  final int     casFeatCode_amountUnit;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getAmountUnit(int addr) {
        if (featOkTst && casFeat_amountUnit == null)
      jcas.throwFeatMissing("amountUnit", "org.ohnlp.medxn.type.Ingredient");
    return ll_cas.ll_getStringValue(addr, casFeatCode_amountUnit);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setAmountUnit(int addr, String v) {
        if (featOkTst && casFeat_amountUnit == null)
      jcas.throwFeatMissing("amountUnit", "org.ohnlp.medxn.type.Ingredient");
    ll_cas.ll_setStringValue(addr, casFeatCode_amountUnit, v);}
    
  
 
  /** @generated */
  final Feature casFeat_item;
  /** @generated */
  final int     casFeatCode_item;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getItem(int addr) {
        if (featOkTst && casFeat_item == null)
      jcas.throwFeatMissing("item", "org.ohnlp.medxn.type.Ingredient");
    return ll_cas.ll_getStringValue(addr, casFeatCode_item);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setItem(int addr, String v) {
        if (featOkTst && casFeat_item == null)
      jcas.throwFeatMissing("item", "org.ohnlp.medxn.type.Ingredient");
    ll_cas.ll_setStringValue(addr, casFeatCode_item, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Ingredient_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_amountValue = jcas.getRequiredFeatureDE(casType, "amountValue", "uima.cas.Double", featOkTst);
    casFeatCode_amountValue  = (null == casFeat_amountValue) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_amountValue).getCode();

 
    casFeat_amountUnit = jcas.getRequiredFeatureDE(casType, "amountUnit", "uima.cas.String", featOkTst);
    casFeatCode_amountUnit  = (null == casFeat_amountUnit) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_amountUnit).getCode();

 
    casFeat_item = jcas.getRequiredFeatureDE(casType, "item", "uima.cas.String", featOkTst);
    casFeatCode_item  = (null == casFeat_item) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_item).getCode();

  }
}



    