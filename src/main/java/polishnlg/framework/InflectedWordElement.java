/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is "Simplenlg".
 *
 * The Initial Developer of the Original Code is Ehud Reiter, Albert Gatt and Dave Westwater.
 * Portions created by Ehud Reiter, Albert Gatt and Dave Westwater are Copyright (C) 2010-11 The University of Aberdeen. All Rights Reserved.
 *
 * Contributor(s): Ehud Reiter, Albert Gatt, Dave Wewstwater, Roman Kutlak, Margaret Mitchell.
 *
 * Contributor(s) German version: Kira Klimt, Daniel Braun, Technical University of Munich
 * 
 * Contributor(s) Polish version: Sasha Gdaniec
 *
 */

package polishnlg.framework;

import polishnlg.features.*;


import java.util.List;

/**
 * <p>
 * This class defines the <code>NLGElement</code> that is used to represent an
 * word that requires inflection by the morphology. It has convenience methods
 * for retrieving the base form of the word (for example, <em>kiss</em>,
 * <em>eat</em>) and for setting and retrieving the base word. The base word is
 * a <code>WordElement</code> constructed by the lexicon.
 * </p>
 *
 */

public class InflectedWordElement extends NLGElement {

    /**
     * Constructs a new inflected word using the giving word as the base form.
     * Constructing the word also requires a lexical category (such as noun,
     * verb).
     *
     * @param word
     *            the base form for this inflected word.
     * @param category
     *            the lexical category for the word.
     */
    public InflectedWordElement(String word, LexicalCategory category) {
        super();
        setFeature(LexicalFeature.BASE_FORM, word);
        setCategory(category);
    }

    /**
     * Constructs a new inflected word from a WordElement
     *
     * @param word
     *            underlying wordelement
     */
    
    //POLISH MORPHOLOGY CODE
    public InflectedWordElement(WordElement word) {
        super();
        setFeature(InternalFeature.BASE_WORD, word);
        // AG: changed to use the default spelling variant
        // setFeature(LexicalFeature.BASE_FORM, word.getBaseForm());
        String defaultSpelling = word.getDefaultSpellingVariant();
        setFeature(LexicalFeature.BASE_FORM, defaultSpelling);
        setCategory(word.getCategory());
        setFeatures(word);
    }
    
    //Function for inheriting features of the WordElement the InflectedWordElement was creatded from
    public void setFeatures(WordElement currentWord) {
    	if(null != currentWord && null != currentWord.getAllFeatures()) {
    		for(String feature : currentWord.getAllFeatureNames()) {
    			this.setFeature(feature, currentWord.getFeature(feature));
    			// if there is no information in lexicon about separability -> get it through conjugation of 1st person
    			if(feature.equalsIgnoreCase("firstPerPres")) {
    				if (currentWord.getFeature("firstPerPres").toString().contains(" ")) {
    					this.setFeature(LexicalFeature.SEPARABLE, true);
    				}
    			}
    		}
    	} 
    }
    
    //END

    /**
     * This method returns null as the inflected word has no child components.
     */
    @Override
    public List<NLGElement> getChildren() {
        return null;
    }

    @Override
    public String toString() {
        return "InflectedWordElement[" + getBaseForm() + ':'
                + getCategory().toString() + ']';
    }

    @Override
    public String printTree(String indent) {
        StringBuffer print = new StringBuffer();
        print.append("InflectedWordElement: base=").append(getBaseForm())
                .append(", category=").append(getCategory().toString()).append(
                ", ").append(super.toString()).append('\n');
        return print.toString();
    }

    /**
     * Retrieves the base form for this element. The base form is the originally
     * supplied word.
     *
     * @return a <code>String</code> forming the base form of the element.
     */
    public String getBaseForm() {
        return getFeatureAsString(LexicalFeature.BASE_FORM);
    }

    /**
     * Sets the base word for this element.
     *
     * @param word
     *            the <code>WordElement</code> representing the base word as
     *            read from the lexicon.
     */
    public void setBaseWord(WordElement word) {
        setFeature(InternalFeature.BASE_WORD, word);
    }

    /**
     * Retrieves the base word for this element.
     *
     * @return the <code>WordElement</code> representing the base word as read
     *         from the lexicon.
     */
    public WordElement getBaseWord() {
        NLGElement baseWord = this
                .getFeatureAsElement(InternalFeature.BASE_WORD);
        return baseWord instanceof WordElement ? (WordElement) baseWord : null;
    }
}
