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

import org.apache.uima.jcas.cas.TOP;


/** Information about the caregiving setting for the clinical document.  Typically comes from structured metadata.
 * Updated by JCasGen Sun Feb 03 22:13:25 SGT 2019
 * XML source: /medxn/src/main/resources/org/ohnlp/medtagger/types/MedTaggerTypes.xml
 * @generated */
public class SourceData extends TOP {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(SourceData.class);
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
  protected SourceData() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public SourceData(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public SourceData(JCas jcas) {
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
  //* Feature: noteTypeCode

  /** getter for noteTypeCode - gets 
   * @generated
   * @return value of the feature 
   */
  public String getNoteTypeCode() {
    if (SourceData_Type.featOkTst && ((SourceData_Type)jcasType).casFeat_noteTypeCode == null)
      jcasType.jcas.throwFeatMissing("noteTypeCode", "org.ohnlp.typesystem.type.structured.SourceData");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SourceData_Type)jcasType).casFeatCode_noteTypeCode);}
    
  /** setter for noteTypeCode - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setNoteTypeCode(String v) {
    if (SourceData_Type.featOkTst && ((SourceData_Type)jcasType).casFeat_noteTypeCode == null)
      jcasType.jcas.throwFeatMissing("noteTypeCode", "org.ohnlp.typesystem.type.structured.SourceData");
    jcasType.ll_cas.ll_setStringValue(addr, ((SourceData_Type)jcasType).casFeatCode_noteTypeCode, v);}    
   
    
  //*--------------*
  //* Feature: noteSubTypeCode

  /** getter for noteSubTypeCode - gets 
   * @generated
   * @return value of the feature 
   */
  public String getNoteSubTypeCode() {
    if (SourceData_Type.featOkTst && ((SourceData_Type)jcasType).casFeat_noteSubTypeCode == null)
      jcasType.jcas.throwFeatMissing("noteSubTypeCode", "org.ohnlp.typesystem.type.structured.SourceData");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SourceData_Type)jcasType).casFeatCode_noteSubTypeCode);}
    
  /** setter for noteSubTypeCode - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setNoteSubTypeCode(String v) {
    if (SourceData_Type.featOkTst && ((SourceData_Type)jcasType).casFeat_noteSubTypeCode == null)
      jcasType.jcas.throwFeatMissing("noteSubTypeCode", "org.ohnlp.typesystem.type.structured.SourceData");
    jcasType.ll_cas.ll_setStringValue(addr, ((SourceData_Type)jcasType).casFeatCode_noteSubTypeCode, v);}    
   
    
  //*--------------*
  //* Feature: authorSpecialty

  /** getter for authorSpecialty - gets 
   * @generated
   * @return value of the feature 
   */
  public String getAuthorSpecialty() {
    if (SourceData_Type.featOkTst && ((SourceData_Type)jcasType).casFeat_authorSpecialty == null)
      jcasType.jcas.throwFeatMissing("authorSpecialty", "org.ohnlp.typesystem.type.structured.SourceData");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SourceData_Type)jcasType).casFeatCode_authorSpecialty);}
    
  /** setter for authorSpecialty - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setAuthorSpecialty(String v) {
    if (SourceData_Type.featOkTst && ((SourceData_Type)jcasType).casFeat_authorSpecialty == null)
      jcasType.jcas.throwFeatMissing("authorSpecialty", "org.ohnlp.typesystem.type.structured.SourceData");
    jcasType.ll_cas.ll_setStringValue(addr, ((SourceData_Type)jcasType).casFeatCode_authorSpecialty, v);}    
   
    
  //*--------------*
  //* Feature: documentStandard

  /** getter for documentStandard - gets 
   * @generated
   * @return value of the feature 
   */
  public String getDocumentStandard() {
    if (SourceData_Type.featOkTst && ((SourceData_Type)jcasType).casFeat_documentStandard == null)
      jcasType.jcas.throwFeatMissing("documentStandard", "org.ohnlp.typesystem.type.structured.SourceData");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SourceData_Type)jcasType).casFeatCode_documentStandard);}
    
  /** setter for documentStandard - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setDocumentStandard(String v) {
    if (SourceData_Type.featOkTst && ((SourceData_Type)jcasType).casFeat_documentStandard == null)
      jcasType.jcas.throwFeatMissing("documentStandard", "org.ohnlp.typesystem.type.structured.SourceData");
    jcasType.ll_cas.ll_setStringValue(addr, ((SourceData_Type)jcasType).casFeatCode_documentStandard, v);}    
   
    
  //*--------------*
  //* Feature: sourceInstanceId

  /** getter for sourceInstanceId - gets 
   * @generated
   * @return value of the feature 
   */
  public String getSourceInstanceId() {
    if (SourceData_Type.featOkTst && ((SourceData_Type)jcasType).casFeat_sourceInstanceId == null)
      jcasType.jcas.throwFeatMissing("sourceInstanceId", "org.ohnlp.typesystem.type.structured.SourceData");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SourceData_Type)jcasType).casFeatCode_sourceInstanceId);}
    
  /** setter for sourceInstanceId - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSourceInstanceId(String v) {
    if (SourceData_Type.featOkTst && ((SourceData_Type)jcasType).casFeat_sourceInstanceId == null)
      jcasType.jcas.throwFeatMissing("sourceInstanceId", "org.ohnlp.typesystem.type.structured.SourceData");
    jcasType.ll_cas.ll_setStringValue(addr, ((SourceData_Type)jcasType).casFeatCode_sourceInstanceId, v);}    
   
    
  //*--------------*
  //* Feature: sourceRevisionNbr

  /** getter for sourceRevisionNbr - gets 
   * @generated
   * @return value of the feature 
   */
  public int getSourceRevisionNbr() {
    if (SourceData_Type.featOkTst && ((SourceData_Type)jcasType).casFeat_sourceRevisionNbr == null)
      jcasType.jcas.throwFeatMissing("sourceRevisionNbr", "org.ohnlp.typesystem.type.structured.SourceData");
    return jcasType.ll_cas.ll_getIntValue(addr, ((SourceData_Type)jcasType).casFeatCode_sourceRevisionNbr);}
    
  /** setter for sourceRevisionNbr - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSourceRevisionNbr(int v) {
    if (SourceData_Type.featOkTst && ((SourceData_Type)jcasType).casFeat_sourceRevisionNbr == null)
      jcasType.jcas.throwFeatMissing("sourceRevisionNbr", "org.ohnlp.typesystem.type.structured.SourceData");
    jcasType.ll_cas.ll_setIntValue(addr, ((SourceData_Type)jcasType).casFeatCode_sourceRevisionNbr, v);}    
   
    
  //*--------------*
  //* Feature: sourceRevisionDate

  /** getter for sourceRevisionDate - gets 
   * @generated
   * @return value of the feature 
   */
  public String getSourceRevisionDate() {
    if (SourceData_Type.featOkTst && ((SourceData_Type)jcasType).casFeat_sourceRevisionDate == null)
      jcasType.jcas.throwFeatMissing("sourceRevisionDate", "org.ohnlp.typesystem.type.structured.SourceData");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SourceData_Type)jcasType).casFeatCode_sourceRevisionDate);}
    
  /** setter for sourceRevisionDate - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSourceRevisionDate(String v) {
    if (SourceData_Type.featOkTst && ((SourceData_Type)jcasType).casFeat_sourceRevisionDate == null)
      jcasType.jcas.throwFeatMissing("sourceRevisionDate", "org.ohnlp.typesystem.type.structured.SourceData");
    jcasType.ll_cas.ll_setStringValue(addr, ((SourceData_Type)jcasType).casFeatCode_sourceRevisionDate, v);}    
   
    
  //*--------------*
  //* Feature: sourceOriginalDate

  /** getter for sourceOriginalDate - gets 
   * @generated
   * @return value of the feature 
   */
  public String getSourceOriginalDate() {
    if (SourceData_Type.featOkTst && ((SourceData_Type)jcasType).casFeat_sourceOriginalDate == null)
      jcasType.jcas.throwFeatMissing("sourceOriginalDate", "org.ohnlp.typesystem.type.structured.SourceData");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SourceData_Type)jcasType).casFeatCode_sourceOriginalDate);}
    
  /** setter for sourceOriginalDate - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSourceOriginalDate(String v) {
    if (SourceData_Type.featOkTst && ((SourceData_Type)jcasType).casFeat_sourceOriginalDate == null)
      jcasType.jcas.throwFeatMissing("sourceOriginalDate", "org.ohnlp.typesystem.type.structured.SourceData");
    jcasType.ll_cas.ll_setStringValue(addr, ((SourceData_Type)jcasType).casFeatCode_sourceOriginalDate, v);}    
   
    
  //*--------------*
  //* Feature: sourceInstitution

  /** getter for sourceInstitution - gets 
   * @generated
   * @return value of the feature 
   */
  public String getSourceInstitution() {
    if (SourceData_Type.featOkTst && ((SourceData_Type)jcasType).casFeat_sourceInstitution == null)
      jcasType.jcas.throwFeatMissing("sourceInstitution", "org.ohnlp.typesystem.type.structured.SourceData");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SourceData_Type)jcasType).casFeatCode_sourceInstitution);}
    
  /** setter for sourceInstitution - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSourceInstitution(String v) {
    if (SourceData_Type.featOkTst && ((SourceData_Type)jcasType).casFeat_sourceInstitution == null)
      jcasType.jcas.throwFeatMissing("sourceInstitution", "org.ohnlp.typesystem.type.structured.SourceData");
    jcasType.ll_cas.ll_setStringValue(addr, ((SourceData_Type)jcasType).casFeatCode_sourceInstitution, v);}    
   
    
  //*--------------*
  //* Feature: sourceEncounterId

  /** getter for sourceEncounterId - gets 
   * @generated
   * @return value of the feature 
   */
  public String getSourceEncounterId() {
    if (SourceData_Type.featOkTst && ((SourceData_Type)jcasType).casFeat_sourceEncounterId == null)
      jcasType.jcas.throwFeatMissing("sourceEncounterId", "org.ohnlp.typesystem.type.structured.SourceData");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SourceData_Type)jcasType).casFeatCode_sourceEncounterId);}
    
  /** setter for sourceEncounterId - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSourceEncounterId(String v) {
    if (SourceData_Type.featOkTst && ((SourceData_Type)jcasType).casFeat_sourceEncounterId == null)
      jcasType.jcas.throwFeatMissing("sourceEncounterId", "org.ohnlp.typesystem.type.structured.SourceData");
    jcasType.ll_cas.ll_setStringValue(addr, ((SourceData_Type)jcasType).casFeatCode_sourceEncounterId, v);}    
   
    
  //*--------------*
  //* Feature: sourceApplication

  /** getter for sourceApplication - gets 
   * @generated
   * @return value of the feature 
   */
  public String getSourceApplication() {
    if (SourceData_Type.featOkTst && ((SourceData_Type)jcasType).casFeat_sourceApplication == null)
      jcasType.jcas.throwFeatMissing("sourceApplication", "org.ohnlp.typesystem.type.structured.SourceData");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SourceData_Type)jcasType).casFeatCode_sourceApplication);}
    
  /** setter for sourceApplication - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSourceApplication(String v) {
    if (SourceData_Type.featOkTst && ((SourceData_Type)jcasType).casFeat_sourceApplication == null)
      jcasType.jcas.throwFeatMissing("sourceApplication", "org.ohnlp.typesystem.type.structured.SourceData");
    jcasType.ll_cas.ll_setStringValue(addr, ((SourceData_Type)jcasType).casFeatCode_sourceApplication, v);}    
  }

    