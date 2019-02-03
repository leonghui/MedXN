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

import org.ohnlp.typesystem.type.relation.TemporalRelation;
import org.ohnlp.typesystem.type.relation.LocationOf;


/** This is an Event from the UMLS semantic group of Procedures (except that Laboratory procedures are separate).  Based on generic Clinical Element Models (CEMs)
 * Updated by JCasGen Sun Feb 03 22:13:25 SGT 2019
 * XML source: /medxn/src/main/resources/org/ohnlp/medtagger/types/MedTaggerTypes.xml
 * @generated */
public class Procedure extends Event {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Procedure.class);
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
  protected Procedure() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Procedure(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Procedure(JCas jcas) {
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
  //* Feature: bodyLaterality

  /** getter for bodyLaterality - gets 
   * @generated
   * @return value of the feature 
   */
  public BodyLaterality getBodyLaterality() {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_bodyLaterality == null)
      jcasType.jcas.throwFeatMissing("bodyLaterality", "org.ohnlp.typesystem.type.refsem.Procedure");
    return (BodyLaterality)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_bodyLaterality)));}
    
  /** setter for bodyLaterality - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setBodyLaterality(BodyLaterality v) {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_bodyLaterality == null)
      jcasType.jcas.throwFeatMissing("bodyLaterality", "org.ohnlp.typesystem.type.refsem.Procedure");
    jcasType.ll_cas.ll_setRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_bodyLaterality, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: bodySide

  /** getter for bodySide - gets 
   * @generated
   * @return value of the feature 
   */
  public BodySide getBodySide() {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_bodySide == null)
      jcasType.jcas.throwFeatMissing("bodySide", "org.ohnlp.typesystem.type.refsem.Procedure");
    return (BodySide)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_bodySide)));}
    
  /** setter for bodySide - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setBodySide(BodySide v) {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_bodySide == null)
      jcasType.jcas.throwFeatMissing("bodySide", "org.ohnlp.typesystem.type.refsem.Procedure");
    jcasType.ll_cas.ll_setRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_bodySide, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: bodyLocation

  /** getter for bodyLocation - gets 
   * @generated
   * @return value of the feature 
   */
  public LocationOf getBodyLocation() {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_bodyLocation == null)
      jcasType.jcas.throwFeatMissing("bodyLocation", "org.ohnlp.typesystem.type.refsem.Procedure");
    return (LocationOf)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_bodyLocation)));}
    
  /** setter for bodyLocation - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setBodyLocation(LocationOf v) {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_bodyLocation == null)
      jcasType.jcas.throwFeatMissing("bodyLocation", "org.ohnlp.typesystem.type.refsem.Procedure");
    jcasType.ll_cas.ll_setRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_bodyLocation, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: procedureDevice

  /** getter for procedureDevice - gets 
   * @generated
   * @return value of the feature 
   */
  public ProcedureDevice getProcedureDevice() {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_procedureDevice == null)
      jcasType.jcas.throwFeatMissing("procedureDevice", "org.ohnlp.typesystem.type.refsem.Procedure");
    return (ProcedureDevice)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_procedureDevice)));}
    
  /** setter for procedureDevice - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setProcedureDevice(ProcedureDevice v) {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_procedureDevice == null)
      jcasType.jcas.throwFeatMissing("procedureDevice", "org.ohnlp.typesystem.type.refsem.Procedure");
    jcasType.ll_cas.ll_setRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_procedureDevice, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: duration

  /** getter for duration - gets 
   * @generated
   * @return value of the feature 
   */
  public TemporalRelation getDuration() {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_duration == null)
      jcasType.jcas.throwFeatMissing("duration", "org.ohnlp.typesystem.type.refsem.Procedure");
    return (TemporalRelation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_duration)));}
    
  /** setter for duration - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setDuration(TemporalRelation v) {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_duration == null)
      jcasType.jcas.throwFeatMissing("duration", "org.ohnlp.typesystem.type.refsem.Procedure");
    jcasType.ll_cas.ll_setRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_duration, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: endTime

  /** getter for endTime - gets 
   * @generated
   * @return value of the feature 
   */
  public Time getEndTime() {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_endTime == null)
      jcasType.jcas.throwFeatMissing("endTime", "org.ohnlp.typesystem.type.refsem.Procedure");
    return (Time)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_endTime)));}
    
  /** setter for endTime - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setEndTime(Time v) {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_endTime == null)
      jcasType.jcas.throwFeatMissing("endTime", "org.ohnlp.typesystem.type.refsem.Procedure");
    jcasType.ll_cas.ll_setRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_endTime, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: method

  /** getter for method - gets 
   * @generated
   * @return value of the feature 
   */
  public ProcedureMethod getMethod() {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_method == null)
      jcasType.jcas.throwFeatMissing("method", "org.ohnlp.typesystem.type.refsem.Procedure");
    return (ProcedureMethod)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_method)));}
    
  /** setter for method - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setMethod(ProcedureMethod v) {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_method == null)
      jcasType.jcas.throwFeatMissing("method", "org.ohnlp.typesystem.type.refsem.Procedure");
    jcasType.ll_cas.ll_setRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_method, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: startTime

  /** getter for startTime - gets 
   * @generated
   * @return value of the feature 
   */
  public Time getStartTime() {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_startTime == null)
      jcasType.jcas.throwFeatMissing("startTime", "org.ohnlp.typesystem.type.refsem.Procedure");
    return (Time)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_startTime)));}
    
  /** setter for startTime - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setStartTime(Time v) {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_startTime == null)
      jcasType.jcas.throwFeatMissing("startTime", "org.ohnlp.typesystem.type.refsem.Procedure");
    jcasType.ll_cas.ll_setRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_startTime, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: relativeTemporalContext

  /** getter for relativeTemporalContext - gets 
   * @generated
   * @return value of the feature 
   */
  public TemporalRelation getRelativeTemporalContext() {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_relativeTemporalContext == null)
      jcasType.jcas.throwFeatMissing("relativeTemporalContext", "org.ohnlp.typesystem.type.refsem.Procedure");
    return (TemporalRelation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_relativeTemporalContext)));}
    
  /** setter for relativeTemporalContext - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setRelativeTemporalContext(TemporalRelation v) {
    if (Procedure_Type.featOkTst && ((Procedure_Type)jcasType).casFeat_relativeTemporalContext == null)
      jcasType.jcas.throwFeatMissing("relativeTemporalContext", "org.ohnlp.typesystem.type.refsem.Procedure");
    jcasType.ll_cas.ll_setRefValue(addr, ((Procedure_Type)jcasType).casFeatCode_relativeTemporalContext, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    