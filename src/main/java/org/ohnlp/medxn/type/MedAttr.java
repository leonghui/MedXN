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
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** Medication name and attributes that simply hold string and offset
 * Updated by JCasGen Mon Feb 04 00:02:11 SGT 2019
 * XML source: /medxn/src/main/resources/org/ohnlp/medxn/types/MedXNTypes.xml
 * @generated */
public class MedAttr extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(MedAttr.class);
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
  protected MedAttr() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public MedAttr(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public MedAttr(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public MedAttr(JCas jcas, int begin, int end) {
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
  //* Feature: tag

  /** getter for tag - gets semantic tag - eg, med, freq,...
   * @generated
   * @return value of the feature 
   */
  public String getTag() {
    if (MedAttr_Type.featOkTst && ((MedAttr_Type)jcasType).casFeat_tag == null)
      jcasType.jcas.throwFeatMissing("tag", "org.ohnlp.medxn.type.MedAttr");
    return jcasType.ll_cas.ll_getStringValue(addr, ((MedAttr_Type)jcasType).casFeatCode_tag);}
    
  /** setter for tag - sets semantic tag - eg, med, freq,... 
   * @generated
   * @param v value to set into the feature 
   */
  public void setTag(String v) {
    if (MedAttr_Type.featOkTst && ((MedAttr_Type)jcasType).casFeat_tag == null)
      jcasType.jcas.throwFeatMissing("tag", "org.ohnlp.medxn.type.MedAttr");
    jcasType.ll_cas.ll_setStringValue(addr, ((MedAttr_Type)jcasType).casFeatCode_tag, v);}    
  }

    