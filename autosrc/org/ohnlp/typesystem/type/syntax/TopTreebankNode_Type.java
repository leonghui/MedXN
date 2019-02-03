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
package org.ohnlp.typesystem.type.syntax;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;

/** A Penn Treebank Node; as the top node, this stores the whole subsumed sentence's parse tree as a string.  It also stores all the terminals, allowing for traversal of the tree bottom-up (top-down is implied through the TreebankNode's 'children' attribute).
 * Updated by JCasGen Sun Feb 03 22:13:25 SGT 2019
 * @generated */
public class TopTreebankNode_Type extends TreebankNode_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = TopTreebankNode.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("org.ohnlp.typesystem.type.syntax.TopTreebankNode");
 
  /** @generated */
  final Feature casFeat_treebankParse;
  /** @generated */
  final int     casFeatCode_treebankParse;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getTreebankParse(int addr) {
        if (featOkTst && casFeat_treebankParse == null)
      jcas.throwFeatMissing("treebankParse", "org.ohnlp.typesystem.type.syntax.TopTreebankNode");
    return ll_cas.ll_getStringValue(addr, casFeatCode_treebankParse);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setTreebankParse(int addr, String v) {
        if (featOkTst && casFeat_treebankParse == null)
      jcas.throwFeatMissing("treebankParse", "org.ohnlp.typesystem.type.syntax.TopTreebankNode");
    ll_cas.ll_setStringValue(addr, casFeatCode_treebankParse, v);}
    
  
 
  /** @generated */
  final Feature casFeat_terminals;
  /** @generated */
  final int     casFeatCode_terminals;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getTerminals(int addr) {
        if (featOkTst && casFeat_terminals == null)
      jcas.throwFeatMissing("terminals", "org.ohnlp.typesystem.type.syntax.TopTreebankNode");
    return ll_cas.ll_getRefValue(addr, casFeatCode_terminals);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setTerminals(int addr, int v) {
        if (featOkTst && casFeat_terminals == null)
      jcas.throwFeatMissing("terminals", "org.ohnlp.typesystem.type.syntax.TopTreebankNode");
    ll_cas.ll_setRefValue(addr, casFeatCode_terminals, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public int getTerminals(int addr, int i) {
        if (featOkTst && casFeat_terminals == null)
      jcas.throwFeatMissing("terminals", "org.ohnlp.typesystem.type.syntax.TopTreebankNode");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_terminals), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_terminals), i);
	return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_terminals), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setTerminals(int addr, int i, int v) {
        if (featOkTst && casFeat_terminals == null)
      jcas.throwFeatMissing("terminals", "org.ohnlp.typesystem.type.syntax.TopTreebankNode");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_terminals), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_terminals), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_terminals), i, v);
  }
 



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public TopTreebankNode_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_treebankParse = jcas.getRequiredFeatureDE(casType, "treebankParse", "uima.cas.String", featOkTst);
    casFeatCode_treebankParse  = (null == casFeat_treebankParse) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_treebankParse).getCode();

 
    casFeat_terminals = jcas.getRequiredFeatureDE(casType, "terminals", "uima.cas.FSArray", featOkTst);
    casFeatCode_terminals  = (null == casFeat_terminals) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_terminals).getCode();

  }
}



    