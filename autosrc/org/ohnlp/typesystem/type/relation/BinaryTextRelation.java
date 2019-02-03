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
package org.ohnlp.typesystem.type.relation;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** A super-type for relationships between two spans of text.
 * Updated by JCasGen Sun Feb 03 22:13:25 SGT 2019
 * XML source: /medxn/src/main/resources/org/ohnlp/medtagger/types/MedTaggerTypes.xml
 * @generated */
public class BinaryTextRelation extends Relation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(BinaryTextRelation.class);
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
  protected BinaryTextRelation() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public BinaryTextRelation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public BinaryTextRelation(JCas jcas) {
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
  //* Feature: arg1

  /** getter for arg1 - gets 
   * @generated
   * @return value of the feature 
   */
  public RelationArgument getArg1() {
    if (BinaryTextRelation_Type.featOkTst && ((BinaryTextRelation_Type)jcasType).casFeat_arg1 == null)
      jcasType.jcas.throwFeatMissing("arg1", "org.ohnlp.typesystem.type.relation.BinaryTextRelation");
    return (RelationArgument)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((BinaryTextRelation_Type)jcasType).casFeatCode_arg1)));}
    
  /** setter for arg1 - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setArg1(RelationArgument v) {
    if (BinaryTextRelation_Type.featOkTst && ((BinaryTextRelation_Type)jcasType).casFeat_arg1 == null)
      jcasType.jcas.throwFeatMissing("arg1", "org.ohnlp.typesystem.type.relation.BinaryTextRelation");
    jcasType.ll_cas.ll_setRefValue(addr, ((BinaryTextRelation_Type)jcasType).casFeatCode_arg1, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: arg2

  /** getter for arg2 - gets 
   * @generated
   * @return value of the feature 
   */
  public RelationArgument getArg2() {
    if (BinaryTextRelation_Type.featOkTst && ((BinaryTextRelation_Type)jcasType).casFeat_arg2 == null)
      jcasType.jcas.throwFeatMissing("arg2", "org.ohnlp.typesystem.type.relation.BinaryTextRelation");
    return (RelationArgument)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((BinaryTextRelation_Type)jcasType).casFeatCode_arg2)));}
    
  /** setter for arg2 - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setArg2(RelationArgument v) {
    if (BinaryTextRelation_Type.featOkTst && ((BinaryTextRelation_Type)jcasType).casFeat_arg2 == null)
      jcasType.jcas.throwFeatMissing("arg2", "org.ohnlp.typesystem.type.relation.BinaryTextRelation");
    jcasType.ll_cas.ll_setRefValue(addr, ((BinaryTextRelation_Type)jcasType).casFeatCode_arg2, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    