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
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.TOP;


/** Ontologies (e.g., SNOMED-CT) provide an expert semantic representation for concepts. They typically assign a code to a concept and normalize across various textual representations of that concept.  
IdentifiedAnnotation and Elements may point to these normalized concept representations to indicate clinical concepts.
Equivalent to Mayo cTAKES version 2.5: edu.mayo.bmi.uima.core.type.OntologyConcept
 * Updated by JCasGen Sun Feb 03 22:13:25 SGT 2019
 * XML source: /medxn/src/main/resources/org/ohnlp/medtagger/types/MedTaggerTypes.xml
 * @generated */
public class OntologyConcept extends TOP {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(OntologyConcept.class);
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
  protected OntologyConcept() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public OntologyConcept(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public OntologyConcept(JCas jcas) {
    super(jcas);
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
  //* Feature: codingScheme

  /** getter for codingScheme - gets 
   * @generated
   * @return value of the feature 
   */
  public String getCodingScheme() {
    if (OntologyConcept_Type.featOkTst && ((OntologyConcept_Type)jcasType).casFeat_codingScheme == null)
      jcasType.jcas.throwFeatMissing("codingScheme", "org.ohnlp.typesystem.type.refsem.OntologyConcept");
    return jcasType.ll_cas.ll_getStringValue(addr, ((OntologyConcept_Type)jcasType).casFeatCode_codingScheme);}
    
  /** setter for codingScheme - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setCodingScheme(String v) {
    if (OntologyConcept_Type.featOkTst && ((OntologyConcept_Type)jcasType).casFeat_codingScheme == null)
      jcasType.jcas.throwFeatMissing("codingScheme", "org.ohnlp.typesystem.type.refsem.OntologyConcept");
    jcasType.ll_cas.ll_setStringValue(addr, ((OntologyConcept_Type)jcasType).casFeatCode_codingScheme, v);}    
   
    
  //*--------------*
  //* Feature: code

  /** getter for code - gets 
   * @generated
   * @return value of the feature 
   */
  public String getCode() {
    if (OntologyConcept_Type.featOkTst && ((OntologyConcept_Type)jcasType).casFeat_code == null)
      jcasType.jcas.throwFeatMissing("code", "org.ohnlp.typesystem.type.refsem.OntologyConcept");
    return jcasType.ll_cas.ll_getStringValue(addr, ((OntologyConcept_Type)jcasType).casFeatCode_code);}
    
  /** setter for code - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setCode(String v) {
    if (OntologyConcept_Type.featOkTst && ((OntologyConcept_Type)jcasType).casFeat_code == null)
      jcasType.jcas.throwFeatMissing("code", "org.ohnlp.typesystem.type.refsem.OntologyConcept");
    jcasType.ll_cas.ll_setStringValue(addr, ((OntologyConcept_Type)jcasType).casFeatCode_code, v);}    
   
    
  //*--------------*
  //* Feature: oid

  /** getter for oid - gets 
   * @generated
   * @return value of the feature 
   */
  public String getOid() {
    if (OntologyConcept_Type.featOkTst && ((OntologyConcept_Type)jcasType).casFeat_oid == null)
      jcasType.jcas.throwFeatMissing("oid", "org.ohnlp.typesystem.type.refsem.OntologyConcept");
    return jcasType.ll_cas.ll_getStringValue(addr, ((OntologyConcept_Type)jcasType).casFeatCode_oid);}
    
  /** setter for oid - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setOid(String v) {
    if (OntologyConcept_Type.featOkTst && ((OntologyConcept_Type)jcasType).casFeat_oid == null)
      jcasType.jcas.throwFeatMissing("oid", "org.ohnlp.typesystem.type.refsem.OntologyConcept");
    jcasType.ll_cas.ll_setStringValue(addr, ((OntologyConcept_Type)jcasType).casFeatCode_oid, v);}    
   
    
  //*--------------*
  //* Feature: oui

  /** getter for oui - gets holds the rxnorm unique identifier for a given drug
							mention.
							TODO: change this attribute because of name clash (UMLS/SNOMED)
   * @generated
   * @return value of the feature 
   */
  public String getOui() {
    if (OntologyConcept_Type.featOkTst && ((OntologyConcept_Type)jcasType).casFeat_oui == null)
      jcasType.jcas.throwFeatMissing("oui", "org.ohnlp.typesystem.type.refsem.OntologyConcept");
    return jcasType.ll_cas.ll_getStringValue(addr, ((OntologyConcept_Type)jcasType).casFeatCode_oui);}
    
  /** setter for oui - sets holds the rxnorm unique identifier for a given drug
							mention.
							TODO: change this attribute because of name clash (UMLS/SNOMED) 
   * @generated
   * @param v value to set into the feature 
   */
  public void setOui(String v) {
    if (OntologyConcept_Type.featOkTst && ((OntologyConcept_Type)jcasType).casFeat_oui == null)
      jcasType.jcas.throwFeatMissing("oui", "org.ohnlp.typesystem.type.refsem.OntologyConcept");
    jcasType.ll_cas.ll_setStringValue(addr, ((OntologyConcept_Type)jcasType).casFeatCode_oui, v);}    
  }

    