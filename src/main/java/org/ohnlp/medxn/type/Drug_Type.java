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

/** 
 * Updated by JCasGen Mon Feb 04 00:02:11 SGT 2019
 * @generated */
public class Drug_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Drug.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.ohnlp.medxn.type.Drug");
 
  /** @generated */
  final Feature casFeat_name;
  /** @generated */
  final int     casFeatCode_name;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getName(int addr) {
        if (featOkTst && casFeat_name == null)
      jcas.throwFeatMissing("name", "org.ohnlp.medxn.type.Drug");
    return ll_cas.ll_getRefValue(addr, casFeatCode_name);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setName(int addr, int v) {
        if (featOkTst && casFeat_name == null)
      jcas.throwFeatMissing("name", "org.ohnlp.medxn.type.Drug");
    ll_cas.ll_setRefValue(addr, casFeatCode_name, v);}
    
  
 
  /** @generated */
  final Feature casFeat_attrs;
  /** @generated */
  final int     casFeatCode_attrs;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getAttrs(int addr) {
        if (featOkTst && casFeat_attrs == null)
      jcas.throwFeatMissing("attrs", "org.ohnlp.medxn.type.Drug");
    return ll_cas.ll_getRefValue(addr, casFeatCode_attrs);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setAttrs(int addr, int v) {
        if (featOkTst && casFeat_attrs == null)
      jcas.throwFeatMissing("attrs", "org.ohnlp.medxn.type.Drug");
    ll_cas.ll_setRefValue(addr, casFeatCode_attrs, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public int getAttrs(int addr, int i) {
        if (featOkTst && casFeat_attrs == null)
      jcas.throwFeatMissing("attrs", "org.ohnlp.medxn.type.Drug");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_attrs), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_attrs), i);
	return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_attrs), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setAttrs(int addr, int i, int v) {
        if (featOkTst && casFeat_attrs == null)
      jcas.throwFeatMissing("attrs", "org.ohnlp.medxn.type.Drug");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_attrs), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_attrs), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_attrs), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_normDrug;
  /** @generated */
  final int     casFeatCode_normDrug;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getNormDrug(int addr) {
        if (featOkTst && casFeat_normDrug == null)
      jcas.throwFeatMissing("normDrug", "org.ohnlp.medxn.type.Drug");
    return ll_cas.ll_getStringValue(addr, casFeatCode_normDrug);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setNormDrug(int addr, String v) {
        if (featOkTst && casFeat_normDrug == null)
      jcas.throwFeatMissing("normDrug", "org.ohnlp.medxn.type.Drug");
    ll_cas.ll_setStringValue(addr, casFeatCode_normDrug, v);}
    
  
 
  /** @generated */
  final Feature casFeat_normRxType;
  /** @generated */
  final int     casFeatCode_normRxType;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getNormRxType(int addr) {
        if (featOkTst && casFeat_normRxType == null)
      jcas.throwFeatMissing("normRxType", "org.ohnlp.medxn.type.Drug");
    return ll_cas.ll_getStringValue(addr, casFeatCode_normRxType);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setNormRxType(int addr, String v) {
        if (featOkTst && casFeat_normRxType == null)
      jcas.throwFeatMissing("normRxType", "org.ohnlp.medxn.type.Drug");
    ll_cas.ll_setStringValue(addr, casFeatCode_normRxType, v);}
    
  
 
  /** @generated */
  final Feature casFeat_normRxCui;
  /** @generated */
  final int     casFeatCode_normRxCui;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getNormRxCui(int addr) {
        if (featOkTst && casFeat_normRxCui == null)
      jcas.throwFeatMissing("normRxCui", "org.ohnlp.medxn.type.Drug");
    return ll_cas.ll_getStringValue(addr, casFeatCode_normRxCui);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setNormRxCui(int addr, String v) {
        if (featOkTst && casFeat_normRxCui == null)
      jcas.throwFeatMissing("normRxCui", "org.ohnlp.medxn.type.Drug");
    ll_cas.ll_setStringValue(addr, casFeatCode_normRxCui, v);}
    
  
 
  /** @generated */
  final Feature casFeat_normRxName;
  /** @generated */
  final int     casFeatCode_normRxName;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getNormRxName(int addr) {
        if (featOkTst && casFeat_normRxName == null)
      jcas.throwFeatMissing("normRxName", "org.ohnlp.medxn.type.Drug");
    return ll_cas.ll_getStringValue(addr, casFeatCode_normRxName);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setNormRxName(int addr, String v) {
        if (featOkTst && casFeat_normRxName == null)
      jcas.throwFeatMissing("normRxName", "org.ohnlp.medxn.type.Drug");
    ll_cas.ll_setStringValue(addr, casFeatCode_normRxName, v);}
    
  
 
  /** @generated */
  final Feature casFeat_normDrug2;
  /** @generated */
  final int     casFeatCode_normDrug2;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getNormDrug2(int addr) {
        if (featOkTst && casFeat_normDrug2 == null)
      jcas.throwFeatMissing("normDrug2", "org.ohnlp.medxn.type.Drug");
    return ll_cas.ll_getStringValue(addr, casFeatCode_normDrug2);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setNormDrug2(int addr, String v) {
        if (featOkTst && casFeat_normDrug2 == null)
      jcas.throwFeatMissing("normDrug2", "org.ohnlp.medxn.type.Drug");
    ll_cas.ll_setStringValue(addr, casFeatCode_normDrug2, v);}
    
  
 
  /** @generated */
  final Feature casFeat_normRxType2;
  /** @generated */
  final int     casFeatCode_normRxType2;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getNormRxType2(int addr) {
        if (featOkTst && casFeat_normRxType2 == null)
      jcas.throwFeatMissing("normRxType2", "org.ohnlp.medxn.type.Drug");
    return ll_cas.ll_getStringValue(addr, casFeatCode_normRxType2);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setNormRxType2(int addr, String v) {
        if (featOkTst && casFeat_normRxType2 == null)
      jcas.throwFeatMissing("normRxType2", "org.ohnlp.medxn.type.Drug");
    ll_cas.ll_setStringValue(addr, casFeatCode_normRxType2, v);}
    
  
 
  /** @generated */
  final Feature casFeat_normRxCui2;
  /** @generated */
  final int     casFeatCode_normRxCui2;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getNormRxCui2(int addr) {
        if (featOkTst && casFeat_normRxCui2 == null)
      jcas.throwFeatMissing("normRxCui2", "org.ohnlp.medxn.type.Drug");
    return ll_cas.ll_getStringValue(addr, casFeatCode_normRxCui2);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setNormRxCui2(int addr, String v) {
        if (featOkTst && casFeat_normRxCui2 == null)
      jcas.throwFeatMissing("normRxCui2", "org.ohnlp.medxn.type.Drug");
    ll_cas.ll_setStringValue(addr, casFeatCode_normRxCui2, v);}
    
  
 
  /** @generated */
  final Feature casFeat_normRxName2;
  /** @generated */
  final int     casFeatCode_normRxName2;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getNormRxName2(int addr) {
        if (featOkTst && casFeat_normRxName2 == null)
      jcas.throwFeatMissing("normRxName2", "org.ohnlp.medxn.type.Drug");
    return ll_cas.ll_getStringValue(addr, casFeatCode_normRxName2);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setNormRxName2(int addr, String v) {
        if (featOkTst && casFeat_normRxName2 == null)
      jcas.throwFeatMissing("normRxName2", "org.ohnlp.medxn.type.Drug");
    ll_cas.ll_setStringValue(addr, casFeatCode_normRxName2, v);}
    
  
 
  /** @generated */
  final Feature casFeat_form;
  /** @generated */
  final int     casFeatCode_form;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getForm(int addr) {
        if (featOkTst && casFeat_form == null)
      jcas.throwFeatMissing("form", "org.ohnlp.medxn.type.Drug");
    return ll_cas.ll_getStringValue(addr, casFeatCode_form);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setForm(int addr, String v) {
        if (featOkTst && casFeat_form == null)
      jcas.throwFeatMissing("form", "org.ohnlp.medxn.type.Drug");
    ll_cas.ll_setStringValue(addr, casFeatCode_form, v);}
    
  
 
  /** @generated */
  final Feature casFeat_brand;
  /** @generated */
  final int     casFeatCode_brand;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getBrand(int addr) {
        if (featOkTst && casFeat_brand == null)
      jcas.throwFeatMissing("brand", "org.ohnlp.medxn.type.Drug");
    return ll_cas.ll_getStringValue(addr, casFeatCode_brand);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setBrand(int addr, String v) {
        if (featOkTst && casFeat_brand == null)
      jcas.throwFeatMissing("brand", "org.ohnlp.medxn.type.Drug");
    ll_cas.ll_setStringValue(addr, casFeatCode_brand, v);}
    
  
 
  /** @generated */
  final Feature casFeat_ingredients;
  /** @generated */
  final int     casFeatCode_ingredients;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getIngredients(int addr) {
        if (featOkTst && casFeat_ingredients == null)
      jcas.throwFeatMissing("ingredients", "org.ohnlp.medxn.type.Drug");
    return ll_cas.ll_getRefValue(addr, casFeatCode_ingredients);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setIngredients(int addr, int v) {
        if (featOkTst && casFeat_ingredients == null)
      jcas.throwFeatMissing("ingredients", "org.ohnlp.medxn.type.Drug");
    ll_cas.ll_setRefValue(addr, casFeatCode_ingredients, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public int getIngredients(int addr, int i) {
        if (featOkTst && casFeat_ingredients == null)
      jcas.throwFeatMissing("ingredients", "org.ohnlp.medxn.type.Drug");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_ingredients), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_ingredients), i);
	return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_ingredients), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setIngredients(int addr, int i, int v) {
        if (featOkTst && casFeat_ingredients == null)
      jcas.throwFeatMissing("ingredients", "org.ohnlp.medxn.type.Drug");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_ingredients), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_ingredients), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_ingredients), i, v);
  }
 



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Drug_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_name = jcas.getRequiredFeatureDE(casType, "name", "org.ohnlp.medtagger.type.ConceptMention", featOkTst);
    casFeatCode_name  = (null == casFeat_name) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_name).getCode();

 
    casFeat_attrs = jcas.getRequiredFeatureDE(casType, "attrs", "uima.cas.FSArray", featOkTst);
    casFeatCode_attrs  = (null == casFeat_attrs) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_attrs).getCode();

 
    casFeat_normDrug = jcas.getRequiredFeatureDE(casType, "normDrug", "uima.cas.String", featOkTst);
    casFeatCode_normDrug  = (null == casFeat_normDrug) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_normDrug).getCode();

 
    casFeat_normRxType = jcas.getRequiredFeatureDE(casType, "normRxType", "uima.cas.String", featOkTst);
    casFeatCode_normRxType  = (null == casFeat_normRxType) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_normRxType).getCode();

 
    casFeat_normRxCui = jcas.getRequiredFeatureDE(casType, "normRxCui", "uima.cas.String", featOkTst);
    casFeatCode_normRxCui  = (null == casFeat_normRxCui) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_normRxCui).getCode();

 
    casFeat_normRxName = jcas.getRequiredFeatureDE(casType, "normRxName", "uima.cas.String", featOkTst);
    casFeatCode_normRxName  = (null == casFeat_normRxName) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_normRxName).getCode();

 
    casFeat_normDrug2 = jcas.getRequiredFeatureDE(casType, "normDrug2", "uima.cas.String", featOkTst);
    casFeatCode_normDrug2  = (null == casFeat_normDrug2) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_normDrug2).getCode();

 
    casFeat_normRxType2 = jcas.getRequiredFeatureDE(casType, "normRxType2", "uima.cas.String", featOkTst);
    casFeatCode_normRxType2  = (null == casFeat_normRxType2) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_normRxType2).getCode();

 
    casFeat_normRxCui2 = jcas.getRequiredFeatureDE(casType, "normRxCui2", "uima.cas.String", featOkTst);
    casFeatCode_normRxCui2  = (null == casFeat_normRxCui2) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_normRxCui2).getCode();

 
    casFeat_normRxName2 = jcas.getRequiredFeatureDE(casType, "normRxName2", "uima.cas.String", featOkTst);
    casFeatCode_normRxName2  = (null == casFeat_normRxName2) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_normRxName2).getCode();

 
    casFeat_form = jcas.getRequiredFeatureDE(casType, "form", "uima.cas.String", featOkTst);
    casFeatCode_form  = (null == casFeat_form) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_form).getCode();

 
    casFeat_brand = jcas.getRequiredFeatureDE(casType, "brand", "uima.cas.String", featOkTst);
    casFeatCode_brand  = (null == casFeat_brand) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_brand).getCode();

 
    casFeat_ingredients = jcas.getRequiredFeatureDE(casType, "ingredients", "uima.cas.FSArray", featOkTst);
    casFeatCode_ingredients  = (null == casFeat_ingredients) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_ingredients).getCode();

  }
}



    