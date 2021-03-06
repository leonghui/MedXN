/*******************************************************************************
 * Copyright (c) 2018-2019. Leong Hui Wong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** Identifies a particular constituent of interest in the product (FHIR STU3)
 * Updated by JCasGen Mon Feb 04 00:02:11 SGT 2019
 * XML source: /medxn/src/main/resources/org/ohnlp/medxn/types/MedXNTypes.xml
 * @generated */
public class Ingredient extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Ingredient.class);
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
  protected Ingredient() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Ingredient(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Ingredient(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Ingredient(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
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
  //* Feature: amountValue

  /** getter for amountValue - gets Stores a value that corresponds to Medication.ingredient.amount (FHIR STU3)
   * @generated
   * @return value of the feature 
   */
  public double getAmountValue() {
    if (Ingredient_Type.featOkTst && ((Ingredient_Type)jcasType).casFeat_amountValue == null)
      jcasType.jcas.throwFeatMissing("amountValue", "org.ohnlp.medxn.type.Ingredient");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((Ingredient_Type)jcasType).casFeatCode_amountValue);}
    
  /** setter for amountValue - sets Stores a value that corresponds to Medication.ingredient.amount (FHIR STU3) 
   * @generated
   * @param v value to set into the feature 
   */
  public void setAmountValue(double v) {
    if (Ingredient_Type.featOkTst && ((Ingredient_Type)jcasType).casFeat_amountValue == null)
      jcasType.jcas.throwFeatMissing("amountValue", "org.ohnlp.medxn.type.Ingredient");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((Ingredient_Type)jcasType).casFeatCode_amountValue, v);}    
   
    
  //*--------------*
  //* Feature: amountUnit

  /** getter for amountUnit - gets Stores a unit that corresponds to Medication.ingredient.amount (FHIR STU3)
   * @generated
   * @return value of the feature 
   */
  public String getAmountUnit() {
    if (Ingredient_Type.featOkTst && ((Ingredient_Type)jcasType).casFeat_amountUnit == null)
      jcasType.jcas.throwFeatMissing("amountUnit", "org.ohnlp.medxn.type.Ingredient");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Ingredient_Type)jcasType).casFeatCode_amountUnit);}
    
  /** setter for amountUnit - sets Stores a unit that corresponds to Medication.ingredient.amount (FHIR STU3) 
   * @generated
   * @param v value to set into the feature 
   */
  public void setAmountUnit(String v) {
    if (Ingredient_Type.featOkTst && ((Ingredient_Type)jcasType).casFeat_amountUnit == null)
      jcasType.jcas.throwFeatMissing("amountUnit", "org.ohnlp.medxn.type.Ingredient");
    jcasType.ll_cas.ll_setStringValue(addr, ((Ingredient_Type)jcasType).casFeatCode_amountUnit, v);}    
   
    
  //*--------------*
  //* Feature: item

  /** getter for item - gets Stores a code that corresponds to Medication.ingredient.item (FHIR STU3)
   * @generated
   * @return value of the feature 
   */
  public String getItem() {
    if (Ingredient_Type.featOkTst && ((Ingredient_Type)jcasType).casFeat_item == null)
      jcasType.jcas.throwFeatMissing("item", "org.ohnlp.medxn.type.Ingredient");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Ingredient_Type)jcasType).casFeatCode_item);}
    
  /** setter for item - sets Stores a code that corresponds to Medication.ingredient.item (FHIR STU3) 
   * @generated
   * @param v value to set into the feature 
   */
  public void setItem(String v) {
    if (Ingredient_Type.featOkTst && ((Ingredient_Type)jcasType).casFeat_item == null)
      jcasType.jcas.throwFeatMissing("item", "org.ohnlp.medxn.type.Ingredient");
    jcasType.ll_cas.ll_setStringValue(addr, ((Ingredient_Type)jcasType).casFeatCode_item, v);}    
  }

    