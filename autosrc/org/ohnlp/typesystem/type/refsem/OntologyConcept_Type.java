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
package org.ohnlp.typesystem.type.refsem;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.cas.TOP_Type;

/** Ontologies (e.g., SNOMED-CT) provide an expert semantic representation for concepts. They typically assign a code to a concept and normalize across various textual representations of that concept.  
IdentifiedAnnotation and Elements may point to these normalized concept representations to indicate clinical concepts.
Equivalent to Mayo cTAKES version 2.5: edu.mayo.bmi.uima.core.type.OntologyConcept
 * Updated by JCasGen Sun Feb 03 22:13:25 SGT 2019
 * @generated */
public class OntologyConcept_Type extends TOP_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = OntologyConcept.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.ohnlp.typesystem.type.refsem.OntologyConcept");
 
  /** @generated */
  final Feature casFeat_codingScheme;
  /** @generated */
  final int     casFeatCode_codingScheme;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getCodingScheme(int addr) {
        if (featOkTst && casFeat_codingScheme == null)
      jcas.throwFeatMissing("codingScheme", "org.ohnlp.typesystem.type.refsem.OntologyConcept");
    return ll_cas.ll_getStringValue(addr, casFeatCode_codingScheme);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setCodingScheme(int addr, String v) {
        if (featOkTst && casFeat_codingScheme == null)
      jcas.throwFeatMissing("codingScheme", "org.ohnlp.typesystem.type.refsem.OntologyConcept");
    ll_cas.ll_setStringValue(addr, casFeatCode_codingScheme, v);}
    
  
 
  /** @generated */
  final Feature casFeat_code;
  /** @generated */
  final int     casFeatCode_code;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getCode(int addr) {
        if (featOkTst && casFeat_code == null)
      jcas.throwFeatMissing("code", "org.ohnlp.typesystem.type.refsem.OntologyConcept");
    return ll_cas.ll_getStringValue(addr, casFeatCode_code);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setCode(int addr, String v) {
        if (featOkTst && casFeat_code == null)
      jcas.throwFeatMissing("code", "org.ohnlp.typesystem.type.refsem.OntologyConcept");
    ll_cas.ll_setStringValue(addr, casFeatCode_code, v);}
    
  
 
  /** @generated */
  final Feature casFeat_oid;
  /** @generated */
  final int     casFeatCode_oid;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getOid(int addr) {
        if (featOkTst && casFeat_oid == null)
      jcas.throwFeatMissing("oid", "org.ohnlp.typesystem.type.refsem.OntologyConcept");
    return ll_cas.ll_getStringValue(addr, casFeatCode_oid);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setOid(int addr, String v) {
        if (featOkTst && casFeat_oid == null)
      jcas.throwFeatMissing("oid", "org.ohnlp.typesystem.type.refsem.OntologyConcept");
    ll_cas.ll_setStringValue(addr, casFeatCode_oid, v);}
    
  
 
  /** @generated */
  final Feature casFeat_oui;
  /** @generated */
  final int     casFeatCode_oui;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getOui(int addr) {
        if (featOkTst && casFeat_oui == null)
      jcas.throwFeatMissing("oui", "org.ohnlp.typesystem.type.refsem.OntologyConcept");
    return ll_cas.ll_getStringValue(addr, casFeatCode_oui);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setOui(int addr, String v) {
        if (featOkTst && casFeat_oui == null)
      jcas.throwFeatMissing("oui", "org.ohnlp.typesystem.type.refsem.OntologyConcept");
    ll_cas.ll_setStringValue(addr, casFeatCode_oui, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public OntologyConcept_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_codingScheme = jcas.getRequiredFeatureDE(casType, "codingScheme", "uima.cas.String", featOkTst);
    casFeatCode_codingScheme  = (null == casFeat_codingScheme) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_codingScheme).getCode();

 
    casFeat_code = jcas.getRequiredFeatureDE(casType, "code", "uima.cas.String", featOkTst);
    casFeatCode_code  = (null == casFeat_code) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_code).getCode();

 
    casFeat_oid = jcas.getRequiredFeatureDE(casType, "oid", "uima.cas.String", featOkTst);
    casFeatCode_oid  = (null == casFeat_oid) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_oid).getCode();

 
    casFeat_oui = jcas.getRequiredFeatureDE(casType, "oui", "uima.cas.String", featOkTst);
    casFeatCode_oui  = (null == casFeat_oui) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_oui).getCode();

  }
}



    