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


package polishnlg.orthography;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import polishnlg.framework.*;
import polishnlg.features.*;


/**
 * <p>
 * This processing module deals with punctuation when applied to
 * <code>DocumentElement</code>s. The punctuation currently handled by this
 * processor includes the following:
 * <ul>
 * <li>Capitalisation of the first letter in sentences.</li>
 * <li>Termination of sentences with a period if not interrogative.</li>
 * <li>Termination of sentences with a question mark if they are interrogative.</li>
 * <li>Replacement of multiple conjunctions with a comma. For example,
 * <em>John and Peter and Simon</em> becomes <em>John, Peter and Simon</em>.</li>
 * <li>Comma placement for subordinate clauses, e.g. Peter isst einen Apfel, während er nachhause geht.</li>
 * </ul>
 * </p>
 */

public class OrthographyProcessor extends NLGModule {

	private boolean commaSepPremodifiers; // set whether to separate
	// premodifiers using commas

	private boolean commaSepCuephrase;   // set whether to include a comma after a
	// cue phrase (if marked by the
	// CUE_PHRASE=true) feature.
	
	private boolean subordinateCommaSet;
	
	//POLISH MORPHOLOGY CODE

	/** The list of conjunctions which need a comma before. */
	private static final List<String> CONJUNCTIONS_COMMA = Arrays.asList(
			"aby", "acz", "aczkolwiek", "albowiem", "azali", "aż", "ażeby", "bo", "boć", "bowiem", 
			"by", "byle", "byleby", "chociaż", "chociażby", "choć", "choćby", "chybaby", "chyba że", 
			"co", "cokolwiek", "czy", "czyj", "dlaczego", "dlatego", "dlatego że", "dokąd", "dokądkolwiek", 
			"dopiero", "dopiero gdy", "dopóki", "gdy", "gdyby", "gdyż", "gdzie", "gdziekolwiek", "ile", 
			"ilekolwiek", "ilekroć", "ile razy", "ile że", "im", "iż", "iżby", "jak", "jakby", "jak gdyby", 
			"jaki", "jakikolwiek", "jakkolwiek", "jako", "jakoby", "jako że", "jakżeby", "jeśli", "jeśliby", 
			"jeżeli", "jeżeliby", "kędy", "kiedy", "kiedykolwiek", "kiedyż", "kim", "kogo", "komu", "kto", 
			"ktokolwiek", "którędy", "który", "ledwie", "ledwo", "mimo iż", "mimo że", "na co", "niech", 
			"nim", "odkąd", "o ile", "po co", "po czym", "podczas gdy", "pomimo iż", "pomimo że", 
			"ponieważ", "póki", "przy czym", "skąd", "skądkolwiek", "skoro", "tak jak", "tylko że", 
			"tym bardziej że", "w miarę jak", "wprzód nim", "w razie gdyby", "za co", "zaledwie", 
			"zanim", "zwłaszcza gdy", "zwłaszcza jeżeli", "zwłaszcza kiedy", "zwłaszcza że", "że", 
			"że aż", "żeby");
	
	//END

	@Override
	public void initialise() {
		this.commaSepPremodifiers = false;
		this.commaSepCuephrase = false;
		this.subordinateCommaSet = false;
	}

	/**
	 * Check whether this processor separates premodifiers using a comma.
	 *
	 * @return <code>true</code> if premodifiers in the noun phrase are
	 *         comma-separated.
	 */
	public boolean isCommaSepPremodifiers() {
		return commaSepPremodifiers;
	}

	/**
	 * Set whether to separate premodifiers using a comma. If <code>true</code>,
	 * premodifiers will be comma-separated, as in <i>the long, dark road</i>.
	 * If <code>false</code>, they won't.
	 *
	 * @param commaSepPremodifiers
	 *            the commaSepPremodifiers to set
	 */
	public void setCommaSepPremodifiers(boolean commaSepPremodifiers) {
		this.commaSepPremodifiers = commaSepPremodifiers;
	}


	@Override
	public NLGElement realise(NLGElement element) {
		NLGElement realisedElement = null;
		Object function = null; //the element's discourse function
		Object clauseStatus = null; //the element's clause status

		//get the element's function first
		if(element instanceof ListElement) {
			List<NLGElement> children = element.getChildren();
			if(!children.isEmpty()) {
				NLGElement firstChild = children.get(0);
				function = firstChild.getFeature(InternalFeature.DISCOURSE_FUNCTION);
				if(element.hasFeature(InternalFeature.CLAUSE_STATUS)) {
					clauseStatus = element.getFeature(InternalFeature.CLAUSE_STATUS);
				}
			}
		} else {
			if(element != null) {
				function = element.getFeature(InternalFeature.DISCOURSE_FUNCTION);
			}
		}
		

		if(element != null) {
			ElementCategory category = element.getCategory();

			if(category instanceof DocumentCategory && element instanceof DocumentElement) {
				List<NLGElement> components = ((DocumentElement) element).getComponents();

				switch((DocumentCategory) category){

				case SENTENCE :
					subordinateCommaSet = false;
					realisedElement = realiseSentence(components, element);
					break;

				case LIST_ITEM :
					if(components != null && components.size() > 0) {
						// recursively realise whatever is in the list item
						// NB: this will realise embedded lists within list
						// items
						realisedElement = new ListElement(realise(components));
						realisedElement.setParent(element.getParent());
					}
					break;

				default :
					((DocumentElement) element).setComponents(realise(components));
					realisedElement = element;
				}

			} else if(element instanceof ListElement) {
				// AG: changes here: if we have a premodifier, then we ask the
				// realiseList method to separate with a comma.
				// if it's a postmod, we need commas at the start and end only
				// if it's appositive
				StringBuffer buffer = new StringBuffer();

				if(DiscourseFunction.PRE_MODIFIER.equals(function)) {

					boolean all_appositives = true;
					for(NLGElement child : element.getChildren()){
						all_appositives = all_appositives && child.getFeatureAsBoolean(Feature.APPOSITIVE);
					}

					if(all_appositives){
						buffer.append(", ");
					}
					realiseList(buffer, element.getChildren(), this.commaSepPremodifiers ? "," : "");
					if(all_appositives){
						buffer.append(", ");
					}
				
				} else if(DiscourseFunction.POST_MODIFIER.equals(function) || DiscourseFunction.MODIFIER.equals(function) || true) {
					boolean adjModifiers = false;
					List<NLGElement> postmods = element.getChildren();
					int len = postmods.size();

					for(int i = 0; i < len; i++ ) {
						if(postmods.get(i).getCategory() != null && (postmods.get(i).getCategory().equals(LexicalCategory.ADJECTIVE))
								&& postmods.get(i).hasFeature(InternalFeature.COMPOSITE) && !postmods.get(i).getFeatureAsBoolean(InternalFeature.COMPOSITE)) {
							adjModifiers = true;
						} else {
							adjModifiers = false;
							break;
						}
					}
					//POLISH MORPHOLOGY CODE
					if(postmods.size() > 1) {
						StringBuffer realisation = new StringBuffer();
						NLGElement realisedChild = null;
						int length = postmods.size();
						if(length == 2) {
							realisation.append(realise(postmods.get(0)));
							realisation.append(" i ");
							realisation.append(realise(postmods.get(1)));
						} else {
							
						for(int index = 0; index < length; index++ ) {
							realisedChild = postmods.get(index);
							if(index < length - 1) {
								realisation.append(realise(realisedChild));
								realisation.append(", ");
							} else {
								realisation.setLength(realisation.length() - 2);
								realisation.append(" oraz ");
								realisedChild = realise(realisedChild);
								realisation.append(realisedChild).append(' ');
							}
						} 
					}
						//END
						realisation.toString().replace(" ,", ",");
						buffer.append(realisation);
					} else {
						for(int i = 0; i < len; i++ ) {
							// for(NLGElement postmod: element.getChildren()) {
							NLGElement postmod = postmods.get(i);

							// if the postmod is appositive, it's sandwiched in
							// commas
							if(postmod.getFeatureAsBoolean(Feature.APPOSITIVE)) {
								buffer.append(", ");
								buffer.append(realise(postmod));
								buffer.append(", ");
							} else {
								buffer.append(" ");
								buffer.append(realise(postmod));
								if(postmod instanceof ListElement
										|| (postmod.getRealisation() != null && !postmod.getRealisation().equals(""))) {
									buffer.append(" ");
								}
							}
						}
					}

				} else if((DiscourseFunction.CUE_PHRASE.equals(function) || DiscourseFunction.FRONT_MODIFIER.equals(function))
						&& this.commaSepCuephrase){
					realiseList(buffer, element.getChildren(), this.commaSepCuephrase ? "," : "");

				} else {
					if(clauseStatus != null && clauseStatus.equals(ClauseStatus.SUBORDINATE) && !subordinateCommaSet) {
						if(element != null &&  (((ListElement) element).getFirst().getRealisation().startsWith("i ")) || (((ListElement) element).getFirst().getRealisation().startsWith(" "))) {
							subordinateCommaSet = true;
						} else if (element != null && ((ListElement) element).getFirst() instanceof ListElement
								&& (((ListElement) ((ListElement) element).getFirst()).getFirst().getRealisation().startsWith("i ") || ((ListElement) ((ListElement) element).getFirst()).getFirst().getRealisation().startsWith("oraz "))) {
							subordinateCommaSet = true;
						}
						else {
							buffer.append(", ");
							subordinateCommaSet = true;
						}
					}
					realiseList(buffer, element.getChildren(), "");
				}
				realisedElement = new StringElement(buffer.toString());

			} else if(element instanceof CoordinatedPhraseElement) {
				realisedElement = realiseCoordinatedPhrase(element.getChildren());
			} else {
				realisedElement = element;
			}

			// make the realised element inherit the original category
			// essential if list items are to be properly formatted later
			if(realisedElement != null) {
				realisedElement.setCategory(category);
			}

			//check if this is a cue phrase; if param is set, postfix a comma
			if((DiscourseFunction.CUE_PHRASE.equals(function) || DiscourseFunction.FRONT_MODIFIER.equals(function))
					&& this.commaSepCuephrase) {
				String realisation = realisedElement.getRealisation();

				if(!realisation.endsWith(",")) {
					realisation = realisation + ",";
				}

				realisedElement.setRealisation(realisation);
			}
		}

		//remove preceding and trailing whitespace from internal punctuation
		removePunctSpace(realisedElement);
		return realisedElement;
	}

	/**
	 * removes extra spaces preceding punctuation from a realised element
	 *
	 * @param realisedElement
	 */
	private void removePunctSpace(NLGElement realisedElement) {

		if(realisedElement != null) {

			String realisation = realisedElement.getRealisation();

			if(realisation != null) {
				realisation = realisation.replaceAll(" ,", ",");
				realisation = realisation.replaceAll(",,+", ",");
				realisation = realisation.replaceAll("  +", " ");
				realisedElement.setRealisation(realisation);
			}

		}
	}

	/**
	 * Performs the realisation on a sentence. This includes adding the
	 * terminator and capitalising the first letter.
	 *
	 * @param components
	 *            the <code>List</code> of <code>NLGElement</code>s representing
	 *            the components that make up the sentence.
	 * @param element
	 *            the <code>NLGElement</code> representing the sentence.
	 * @return the realised element as an <code>NLGElement</code>.
	 */
	private NLGElement realiseSentence(List<NLGElement> components, NLGElement element) {

		NLGElement realisedElement = null;
		if(components != null && components.size() > 0) {
			StringBuffer realisation = new StringBuffer();
			realiseList(realisation, components, "");

			stripLeadingCommas(realisation);
			capitaliseFirstLetter(realisation);
			terminateSentence(realisation, element.getFeatureAsBoolean(InternalFeature.INTERROGATIVE).booleanValue());

			((DocumentElement) element).clearComponents();
			// realisation.append(' ');
			element.setRealisation(realisation.toString());
			realisedElement = element;
		}

		return realisedElement;
	}

	/**
	 * Adds the sentence terminator to the sentence. This is a period ('.') for
	 * normal sentences or a question mark ('?') for interrogatives.
	 *
	 * @param realisation
	 *            the <code>StringBuffer<code> containing the current
	 * realisation of the sentence.
	 * @param interrogative
	 *            a <code>boolean</code> flag showing <code>true</code> if the
	 *            sentence is an interrogative, <code>false</code> otherwise.
	 */
	private void terminateSentence(StringBuffer realisation, boolean interrogative) {
		char character = realisation.charAt(realisation.length() - 1);
		if(character != '.' && character != '?') {
			if(interrogative) {
				realisation.append('?');
			} else {
				realisation.append('.');
			}
		}
	}

	/**
	 * Remove recursively any leading spaces or commas at the start
	 * of a sentence.
	 *
	 * @param realisation
	 *            the <code>StringBuffer<code> containing the current
	 * realisation of the sentence.
	 */
	private void stripLeadingCommas(StringBuffer realisation) {
		char character = realisation.charAt(0);
		if(character == ' ' || character == ',') {
			realisation.deleteCharAt(0);
			stripLeadingCommas(realisation);
		}
	}


	/**
	 * Capitalises the first character of a sentence if it is a lower case
	 * letter.
	 *
	 * @param realisation
	 *            the <code>StringBuffer<code> containing the current
	 * realisation of the sentence.
	 */
	private void capitaliseFirstLetter(StringBuffer realisation) {
		//POLISH MORPHOLOGY CODE
		char character = realisation.charAt(0);
		if((character >= 'a' && character <= 'z') || character == 'ą' || character == 'ć' || character == 'ę' || character == 'ł' || character == 'ń' || character == 'ó' || character == 'ś' || character == 'ż' || character == 'ź') {
			character = Character.toUpperCase(character);
			realisation.setCharAt(0, character);
		}
		//END
	}

	@Override
	public List<NLGElement> realise(List<NLGElement> elements) {
		List<NLGElement> realisedList = new ArrayList<NLGElement>();

		if(elements != null && elements.size() > 0) {
			for(NLGElement eachElement : elements) {
				if(eachElement instanceof DocumentElement) {
					realisedList.add(realise(eachElement));
				} else {
					realisedList.add(eachElement);
				}
			}
		}
		return realisedList;
	}

	/**
	 * Realises a list of elements appending the result to the on-going
	 * realisation.
	 *
	 * @param realisation
	 *            the <code>StringBuffer<code> containing the current
	 * 			  realisation of the sentence.
	 * @param components
	 *            the <code>List</code> of <code>NLGElement</code>s representing
	 *            the components that make up the sentence.
	 * @param listSeparator
	 *            the string to use to separate elements of the list, empty if
	 *            no separator needed
	 */
	private void realiseList(StringBuffer realisation, List<NLGElement> components, String listSeparator) {

		NLGElement realisedChild = null;

		for(int i = 0; i < components.size(); i++ ) {
			NLGElement thisElement = components.get(i);
			realisedChild = realise(thisElement);
			String childRealisation = realisedChild.getRealisation();

			// check that the child realisation is non-empty
			if(childRealisation != null && childRealisation.length() > 0 && !childRealisation.matches("^[\\s\\n]+$")) {
				// before certain conjunctions, such as "während", which introduce a subordinate clause, a comma has to be placed
				if(CONJUNCTIONS_COMMA.contains(childRealisation)) {
					realisation.append(", ");
				}
				realisation.append(realisedChild.getRealisation());

				if(components.size() > 1 && i < components.size() - 1) {
					realisation.append(listSeparator);
				}
				realisation.append(' ');
			}
		}

		if(realisation.length() > 0) {
			realisation.setLength(realisation.length() - 1);
		}
	}

	/**
	 * Realises coordinated phrases. Where there are more than two coordinates,
	 * then a comma replaces the conjunction word between all the coordinates
	 * save the last two. For example, <em>John and Peter and Simon</em> becomes
	 * <em>John, Peter and Simon</em>.
	 *
	 * @param components
	 *            the <code>List</code> of <code>NLGElement</code>s representing
	 *            the components that make up the sentence.
	 * @return the realised element as an <code>NLGElement</code>.
	 */
	private NLGElement realiseCoordinatedPhrase(List<NLGElement> components) {
		StringBuffer realisation = new StringBuffer();
		NLGElement realisedChild = null;

		int length = components.size();

		for(int index = 0; index < length; index++ ) {
			realisedChild = components.get(index);
			if(index < length - 2
					&& DiscourseFunction.CONJUNCTION.equals(realisedChild.getFeature(InternalFeature.DISCOURSE_FUNCTION))) {
				realisation.append(", ");
			} else {
				realisedChild = realise(realisedChild);
				realisation.append(realisedChild.getRealisation()).append(' ');
			}
		}
		realisation.setLength(realisation.length() - 1);
		return new StringElement(realisation.toString().replace(" ,", ","));
	}
}