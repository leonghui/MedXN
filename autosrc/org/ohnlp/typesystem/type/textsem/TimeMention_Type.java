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

/* First created by JCasGen Sun Feb 03 22:13:26 SGT 2019 */
package org.ohnlp.typesystem.type.textsem;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** A text string (IdentifiedAnnotation) that refers to a Time (i.e., TIMEX3).
 * Updated by JCasGen Sun Feb 03 22:13:26 SGT 2019
 * @generated */
public class TimeMention_Type extends IdentifiedAnnotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = TimeMention.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.ohnlp.typesystem.type.textsem.TimeMention");
 
  /** @generated */
  final Feature casFeat_time;
  /** @generated */
  final int     casFeatCode_time;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getTime(int addr) {
        if (featOkTst && casFeat_time == null)
      jcas.throwFeatMissing("time", "org.ohnlp.typesystem.type.textsem.TimeMention");
    return ll_cas.ll_getRefValue(addr, casFeatCode_time);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setTime(int addr, int v) {
        if (featOkTst && casFeat_time == null)
      jcas.throwFeatMissing("time", "org.ohnlp.typesystem.type.textsem.TimeMention");
    ll_cas.ll_setRefValue(addr, casFeatCode_time, v);}
    
  
 
  /** @generated */
  final Feature casFeat_timeClass;
  /** @generated */
  final int     casFeatCode_timeClass;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getTimeClass(int addr) {
        if (featOkTst && casFeat_timeClass == null)
      jcas.throwFeatMissing("timeClass", "org.ohnlp.typesystem.type.textsem.TimeMention");
    return ll_cas.ll_getStringValue(addr, casFeatCode_timeClass);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setTimeClass(int addr, String v) {
        if (featOkTst && casFeat_timeClass == null)
      jcas.throwFeatMissing("timeClass", "org.ohnlp.typesystem.type.textsem.TimeMention");
    ll_cas.ll_setStringValue(addr, casFeatCode_timeClass, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public TimeMention_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_time = jcas.getRequiredFeatureDE(casType, "time", "org.ohnlp.typesystem.type.refsem.Time", featOkTst);
    casFeatCode_time  = (null == casFeat_time) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_time).getCode();

 
    casFeat_timeClass = jcas.getRequiredFeatureDE(casType, "timeClass", "uima.cas.String", featOkTst);
    casFeatCode_timeClass  = (null == casFeat_timeClass) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_timeClass).getCode();

  }
}



    