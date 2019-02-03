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
package org.ohnlp.typesystem.type.refsem;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.ohnlp.typesystem.type.relation.DegreeOf;


/** This is an Event from the UMLS semantic group of Laboratory Procedures.  Based on generic Clinical Element Models (CEMs)
 * Updated by JCasGen Sun Feb 03 22:13:24 SGT 2019
 * XML source: /medxn/src/main/resources/org/ohnlp/medtagger/types/MedTaggerTypes.xml
 * @generated */
public class Lab extends Event {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Lab.class);
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
  protected Lab() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Lab(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Lab(JCas jcas) {
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
  //* Feature: abnormalInterpretation

  /** getter for abnormalInterpretation - gets 
   * @generated
   * @return value of the feature 
   */
  public DegreeOf getAbnormalInterpretation() {
    if (Lab_Type.featOkTst && ((Lab_Type)jcasType).casFeat_abnormalInterpretation == null)
      jcasType.jcas.throwFeatMissing("abnormalInterpretation", "org.ohnlp.typesystem.type.refsem.Lab");
    return (DegreeOf)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Lab_Type)jcasType).casFeatCode_abnormalInterpretation)));}
    
  /** setter for abnormalInterpretation - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setAbnormalInterpretation(DegreeOf v) {
    if (Lab_Type.featOkTst && ((Lab_Type)jcasType).casFeat_abnormalInterpretation == null)
      jcasType.jcas.throwFeatMissing("abnormalInterpretation", "org.ohnlp.typesystem.type.refsem.Lab");
    jcasType.ll_cas.ll_setRefValue(addr, ((Lab_Type)jcasType).casFeatCode_abnormalInterpretation, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: deltaFlag

  /** getter for deltaFlag - gets 
   * @generated
   * @return value of the feature 
   */
  public LabDeltaFlag getDeltaFlag() {
    if (Lab_Type.featOkTst && ((Lab_Type)jcasType).casFeat_deltaFlag == null)
      jcasType.jcas.throwFeatMissing("deltaFlag", "org.ohnlp.typesystem.type.refsem.Lab");
    return (LabDeltaFlag)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Lab_Type)jcasType).casFeatCode_deltaFlag)));}
    
  /** setter for deltaFlag - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setDeltaFlag(LabDeltaFlag v) {
    if (Lab_Type.featOkTst && ((Lab_Type)jcasType).casFeat_deltaFlag == null)
      jcasType.jcas.throwFeatMissing("deltaFlag", "org.ohnlp.typesystem.type.refsem.Lab");
    jcasType.ll_cas.ll_setRefValue(addr, ((Lab_Type)jcasType).casFeatCode_deltaFlag, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: labValue

  /** getter for labValue - gets 
   * @generated
   * @return value of the feature 
   */
  public LabValue getLabValue() {
    if (Lab_Type.featOkTst && ((Lab_Type)jcasType).casFeat_labValue == null)
      jcasType.jcas.throwFeatMissing("labValue", "org.ohnlp.typesystem.type.refsem.Lab");
    return (LabValue)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Lab_Type)jcasType).casFeatCode_labValue)));}
    
  /** setter for labValue - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setLabValue(LabValue v) {
    if (Lab_Type.featOkTst && ((Lab_Type)jcasType).casFeat_labValue == null)
      jcasType.jcas.throwFeatMissing("labValue", "org.ohnlp.typesystem.type.refsem.Lab");
    jcasType.ll_cas.ll_setRefValue(addr, ((Lab_Type)jcasType).casFeatCode_labValue, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: ordinalInterpretation

  /** getter for ordinalInterpretation - gets 
   * @generated
   * @return value of the feature 
   */
  public DegreeOf getOrdinalInterpretation() {
    if (Lab_Type.featOkTst && ((Lab_Type)jcasType).casFeat_ordinalInterpretation == null)
      jcasType.jcas.throwFeatMissing("ordinalInterpretation", "org.ohnlp.typesystem.type.refsem.Lab");
    return (DegreeOf)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Lab_Type)jcasType).casFeatCode_ordinalInterpretation)));}
    
  /** setter for ordinalInterpretation - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setOrdinalInterpretation(DegreeOf v) {
    if (Lab_Type.featOkTst && ((Lab_Type)jcasType).casFeat_ordinalInterpretation == null)
      jcasType.jcas.throwFeatMissing("ordinalInterpretation", "org.ohnlp.typesystem.type.refsem.Lab");
    jcasType.ll_cas.ll_setRefValue(addr, ((Lab_Type)jcasType).casFeatCode_ordinalInterpretation, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: referenceRangeNarrative

  /** getter for referenceRangeNarrative - gets 
   * @generated
   * @return value of the feature 
   */
  public LabReferenceRange getReferenceRangeNarrative() {
    if (Lab_Type.featOkTst && ((Lab_Type)jcasType).casFeat_referenceRangeNarrative == null)
      jcasType.jcas.throwFeatMissing("referenceRangeNarrative", "org.ohnlp.typesystem.type.refsem.Lab");
    return (LabReferenceRange)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Lab_Type)jcasType).casFeatCode_referenceRangeNarrative)));}
    
  /** setter for referenceRangeNarrative - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setReferenceRangeNarrative(LabReferenceRange v) {
    if (Lab_Type.featOkTst && ((Lab_Type)jcasType).casFeat_referenceRangeNarrative == null)
      jcasType.jcas.throwFeatMissing("referenceRangeNarrative", "org.ohnlp.typesystem.type.refsem.Lab");
    jcasType.ll_cas.ll_setRefValue(addr, ((Lab_Type)jcasType).casFeatCode_referenceRangeNarrative, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    