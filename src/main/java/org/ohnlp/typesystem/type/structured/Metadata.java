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
package org.ohnlp.typesystem.type.structured;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.LongArray;
import org.apache.uima.jcas.cas.TOP;


/** Structured data that captures information about the document, patient, or context of the clinical text.
 * Updated by JCasGen Sun Feb 03 22:13:25 SGT 2019
 * XML source: /medxn/src/main/resources/org/ohnlp/medtagger/types/MedTaggerTypes.xml
 * @generated */
public class Metadata extends TOP {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Metadata.class);
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
  protected Metadata() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Metadata(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Metadata(JCas jcas) {
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
  //* Feature: patientID

  /** getter for patientID - gets 
   * @generated
   * @return value of the feature 
   */
  public long getPatientID() {
    if (Metadata_Type.featOkTst && ((Metadata_Type)jcasType).casFeat_patientID == null)
      jcasType.jcas.throwFeatMissing("patientID", "org.ohnlp.typesystem.type.structured.Metadata");
    return jcasType.ll_cas.ll_getLongValue(addr, ((Metadata_Type)jcasType).casFeatCode_patientID);}
    
  /** setter for patientID - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPatientID(long v) {
    if (Metadata_Type.featOkTst && ((Metadata_Type)jcasType).casFeat_patientID == null)
      jcasType.jcas.throwFeatMissing("patientID", "org.ohnlp.typesystem.type.structured.Metadata");
    jcasType.ll_cas.ll_setLongValue(addr, ((Metadata_Type)jcasType).casFeatCode_patientID, v);}    
   
    
  //*--------------*
  //* Feature: providerID

  /** getter for providerID - gets 
   * @generated
   * @return value of the feature 
   */
  public LongArray getProviderID() {
    if (Metadata_Type.featOkTst && ((Metadata_Type)jcasType).casFeat_providerID == null)
      jcasType.jcas.throwFeatMissing("providerID", "org.ohnlp.typesystem.type.structured.Metadata");
    return (LongArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Metadata_Type)jcasType).casFeatCode_providerID)));}
    
  /** setter for providerID - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setProviderID(LongArray v) {
    if (Metadata_Type.featOkTst && ((Metadata_Type)jcasType).casFeat_providerID == null)
      jcasType.jcas.throwFeatMissing("providerID", "org.ohnlp.typesystem.type.structured.Metadata");
    jcasType.ll_cas.ll_setRefValue(addr, ((Metadata_Type)jcasType).casFeatCode_providerID, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for providerID - gets an indexed value - 
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public long getProviderID(int i) {
    if (Metadata_Type.featOkTst && ((Metadata_Type)jcasType).casFeat_providerID == null)
      jcasType.jcas.throwFeatMissing("providerID", "org.ohnlp.typesystem.type.structured.Metadata");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Metadata_Type)jcasType).casFeatCode_providerID), i);
    return jcasType.ll_cas.ll_getLongArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Metadata_Type)jcasType).casFeatCode_providerID), i);}

  /** indexed setter for providerID - sets an indexed value - 
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setProviderID(int i, long v) { 
    if (Metadata_Type.featOkTst && ((Metadata_Type)jcasType).casFeat_providerID == null)
      jcasType.jcas.throwFeatMissing("providerID", "org.ohnlp.typesystem.type.structured.Metadata");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Metadata_Type)jcasType).casFeatCode_providerID), i);
    jcasType.ll_cas.ll_setLongArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Metadata_Type)jcasType).casFeatCode_providerID), i, v);}
   
    
  //*--------------*
  //* Feature: sourceData

  /** getter for sourceData - gets 
   * @generated
   * @return value of the feature 
   */
  public SourceData getSourceData() {
    if (Metadata_Type.featOkTst && ((Metadata_Type)jcasType).casFeat_sourceData == null)
      jcasType.jcas.throwFeatMissing("sourceData", "org.ohnlp.typesystem.type.structured.Metadata");
    return (SourceData)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Metadata_Type)jcasType).casFeatCode_sourceData)));}
    
  /** setter for sourceData - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSourceData(SourceData v) {
    if (Metadata_Type.featOkTst && ((Metadata_Type)jcasType).casFeat_sourceData == null)
      jcasType.jcas.throwFeatMissing("sourceData", "org.ohnlp.typesystem.type.structured.Metadata");
    jcasType.ll_cas.ll_setRefValue(addr, ((Metadata_Type)jcasType).casFeatCode_sourceData, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: demographics

  /** getter for demographics - gets 
   * @generated
   * @return value of the feature 
   */
  public Demographics getDemographics() {
    if (Metadata_Type.featOkTst && ((Metadata_Type)jcasType).casFeat_demographics == null)
      jcasType.jcas.throwFeatMissing("demographics", "org.ohnlp.typesystem.type.structured.Metadata");
    return (Demographics)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Metadata_Type)jcasType).casFeatCode_demographics)));}
    
  /** setter for demographics - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setDemographics(Demographics v) {
    if (Metadata_Type.featOkTst && ((Metadata_Type)jcasType).casFeat_demographics == null)
      jcasType.jcas.throwFeatMissing("demographics", "org.ohnlp.typesystem.type.structured.Metadata");
    jcasType.ll_cas.ll_setRefValue(addr, ((Metadata_Type)jcasType).casFeatCode_demographics, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    