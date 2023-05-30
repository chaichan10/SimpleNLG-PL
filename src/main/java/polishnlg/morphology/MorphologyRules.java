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

package polishnlg.morphology;

import polishnlg.framework.*;
import polishnlg.features.*;

import java.util.Set;

/**
 * <p>
 * This abstract class contains a number of rules for doing simple inflection.
 * </p>
 *
 * <p>
 * As a matter of course, the processor will first use any user-defined
 * inflection for the word. If no inflection is provided then the lexicon, if
 * it exists, will be examined for the correct inflection. Failing this a set of
 * very basic rules will be examined to inflect the word.
 * </p>
 *
 * All processing modules perform realisation on a tree of
 * <code>NLGElement</code>s. The modules can alter the tree in whichever way
 * they wish. For example, the syntax processor replaces phrase elements with
 * list elements consisting of inflected words while the morphology processor
 * replaces inflected words with string elements.
 * </p>
 *
 * <p>
 * <b>N.B.</b> the use of <em>module</em>, <em>processing module</em> and
 * <em>processor</em> is interchangeable. They all mean an instance of this
 * class.
 * </p>
 */
public abstract class MorphologyRules extends NLGModule {

	/**
	 * This method is the main method to perform the morphology for nouns.
	 *
	 * @param element  the <code>InflectedWordElement</code>.
	 * @param baseWord the <code>WordElement</code> as created from the lexicon
	 *                 entry.
	 * @return a <code>StringElement</code> representing the word after
	 * inflection.
	 */
	//POLISH MORPHOLOGY CODE
	protected static StringElement doNounMorphology(InflectedWordElement element, WordElement baseWord) {
		StringBuffer realised = new StringBuffer();
		String baseForm = getBaseForm(element, baseWord);
		String inflectedForm = baseForm;
		String genus = element.getFeatureAsString(LexicalFeature.GENDER);
		
		DiscourseFunction grammCase = DiscourseFunction.SUBJECT;

		// only if an element has no own grammatical case, use that of parent element
		if (element.hasFeature(InternalFeature.CASE_PARENT) &&
				element.getFeature(InternalFeature.CASE_PARENT) instanceof DiscourseFunction) {
			grammCase = (DiscourseFunction) element.getFeature(InternalFeature.CASE_PARENT);
		}
		if (element.hasFeature(InternalFeature.CASE) &&
				element.getFeature(InternalFeature.CASE) instanceof DiscourseFunction) {
			grammCase = (DiscourseFunction) element.getFeature(InternalFeature.CASE);
		}

		// get all other features of nouns from lexicon entry
		Set<String> features = baseWord.getAllFeatureNames();

		// do morphology
		if (!element.isPlural() && !element.getFeatureAsBoolean(LexicalFeature.PROPER).booleanValue()) {
			inflectedForm = doNounMorphologySingular(element, inflectedForm, baseForm, genus, grammCase, features);
		} else if (element.isPlural() && !element.getFeatureAsBoolean(LexicalFeature.PROPER).booleanValue()) {
			inflectedForm = doNounMorphologyPlural(element, baseWord, baseForm, genus, grammCase, features);
		}
		// if lexicon returned "-", e.g. for words which have no plural
		// keep the word in its base form
		if (inflectedForm.equals("-") || inflectedForm.length()<2) {
			inflectedForm = baseForm;
		}

		realised.append(inflectedForm);
		StringElement realisedElement = new StringElement(realised.toString());
		realisedElement.setFeature(InternalFeature.DISCOURSE_FUNCTION,
				element.getFeature(InternalFeature.DISCOURSE_FUNCTION));

		return realisedElement;
	}

	/**
	 * This method performs the morphology for nouns in plural.
	 *
	 * @param element  the <code>InflectedWordElement</code>.
	 * @param baseWord the <code>WordElement</code> as created from the lexicon
	 *                 entry.
	 * @param baseForm  the <code>String</code> representing the baseform as retrieved from the lexicon entry.
	 * @param genus  the <code>String</code> representing the gender of the noun.
	 * @param grammCase  the <code>DiscourseFunction</code> representing grammatical case.
	 * @param features  the <code>Set<String></code> list of features as retrieved from the lexicon entry.
	 * @return a <code>StringElement</code> representing the word after
	 * inflection.
	 */
	private static String doNounMorphologyPlural(InflectedWordElement element, WordElement baseWord,
												 String baseForm, String genus, DiscourseFunction grammCase,
												 Set<String> features) {
		String inflectedForm = null;
		String x = "";
		
		if (LexicalCategory.PRONOUN == element.getCategory()) {
			Boolean has_prep = element.hasFeature(Feature.HAS_PREP)
					&& element.getFeatureAsBoolean(Feature.HAS_PREP);
			if(has_prep) {
				x = "_p";
			}
		}
		if (element.hasFeature(LexicalFeature.PLURAL)) {
			inflectedForm = element.getFeatureAsString(LexicalFeature.PLURAL);
		}
		if (inflectedForm == null && baseWord != null) {
			String baseDefaultInfl = null;
			if (features.contains("c_pl" + x)) {
				baseDefaultInfl = baseWord.getFeatureAsString("c_pl" + x);
			}
			if (baseDefaultInfl != null && baseDefaultInfl.equals("\u2014"))
				inflectedForm = baseForm;
			else
				inflectedForm = baseWord.getFeatureAsString(LexicalFeature.PLURAL);
		}
		// Do dative inflection from lexicon
		if (grammCase == DiscourseFunction.INDIRECT_OBJECT) {
			if (features.contains("c_pl" + x)) {
				String c_pl = element.getFeatureAsString("c_pl" + x);
				if (!c_pl.equals("\u2014")) {
					inflectedForm = c_pl;
				}
			} 
		}
		// Do genitive inflection from lexicon
		else if (grammCase == DiscourseFunction.GENITIVE && features.contains("d_pl" + x)
				&& element.getFeatureAsString("d_pl" + x) != "—") {
			inflectedForm = element.getFeatureAsString("d_pl" + x);
		}
		// Do accusative inflection from lexicon
		else if (grammCase == DiscourseFunction.OBJECT && features.contains("b_pl" + x)
				&& element.getFeatureAsString("b_pl" + x) != "-") {
			inflectedForm = element.getFeatureAsString("b_pl" + x);
		}
		else if (grammCase == DiscourseFunction.LOCATIVE && features.contains("msc_pl" + x)
				&& element.getFeatureAsString("msc_pl" + x) != "-") {
			inflectedForm = element.getFeatureAsString("msc_pl" + x);
		}
		else if (grammCase == DiscourseFunction.INSTRUMENTAL && features.contains("n_pl" + x)
				&& element.getFeatureAsString("n_pl" + x) != "-") {
			inflectedForm = element.getFeatureAsString("n_pl" + x);
		}
		else if (grammCase == DiscourseFunction.VOCATIVE && features.contains("w_pl" + x)
				&& element.getFeatureAsString("w_pl" + x) != "-") {
			inflectedForm = element.getFeatureAsString("w_pl" + x);
		}
		else {
			inflectedForm = element.getFeatureAsString("m_pl" + x);
		}
		return inflectedForm;
	}

	/**
	 * This method performs the morphology for nouns in singular.
	 *
	 * @param element  the <code>InflectedWordElement</code>.
	 * @param inflectedForm the <code>String</code> representing the inflected form.
	 * @param baseForm  the <code>String</code> representing the base form as retrieved from the lexicon entry.
	 * @param genus  the <code>String</code> representing the gender of the noun.
	 * @param grammCase  the <code>DiscourseFunction</code> representing grammatical case.
	 * @param features  the <code>Set<String></code> list of features as retrieved from the lexicon entry.
	 * @return a <code>StringElement</code> representing the word after
	 * inflection.
	 */
	private static String doNounMorphologySingular(InflectedWordElement element, String inflectedForm, String baseForm,
												   String genus, DiscourseFunction grammCase, Set<String> features) {
		
		String x = "";
		if (LexicalCategory.PRONOUN == element.getCategory()) {
			Boolean has_prep = element.hasFeature(Feature.HAS_PREP)
					&& element.getFeatureAsBoolean(Feature.HAS_PREP);
			if(has_prep && features.contains("c_sin_p")) {
				x = "_p";
			}
		}
		if (grammCase == DiscourseFunction.INDIRECT_OBJECT) {
			if (features.contains("c_sin" + x) && element.getFeatureAsString("c_sin" + x) != "-") {
				inflectedForm = element.getFeatureAsString("c_sin" + x);
			} 
		} else if (grammCase == DiscourseFunction.OBJECT) {
			if (features.contains("b_sin" + x) && !element.getFeatureAsString("b_sin" + x).equals("\u2014")) {
				inflectedForm = element.getFeatureAsString("b_sin" + x);
			} 
		} else if (grammCase == DiscourseFunction.GENITIVE) {
			// Check for special genitive forms in lexicon
			if (features.contains("d_sin" + x) && element.getFeatureAsString("d_sin" + x) != "-") {
				inflectedForm = element.getFeatureAsString("d_sin" + x);
			} 
		} else if (grammCase == DiscourseFunction.LOCATIVE) {
			// Check for special genitive forms in lexicon
			if (features.contains("msc_sin" + x) && element.getFeatureAsString("msc_sin" + x) != "-") {
				inflectedForm = element.getFeatureAsString("msc_sin" + x);
			} 
		} else if (grammCase == DiscourseFunction.INSTRUMENTAL) {
			// Check for special genitive forms in lexicon
			if (features.contains("n_sin" + x) && element.getFeatureAsString("n_sin" + x) != "-") {
				inflectedForm = element.getFeatureAsString("n_sin" + x);
			} 
		} else if (grammCase == DiscourseFunction.VOCATIVE) {
			// Check for special genitive forms in lexicon
			if (features.contains("w_sin" + x) && element.getFeatureAsString("w_sin" + x) != "-") {
				inflectedForm = element.getFeatureAsString("w_sin" + x);
			} 
		}
		else {
			if (features.contains("m_sin" + x) && element.getFeatureAsString("m_sin" + x) != "-") {
				inflectedForm = element.getFeatureAsString("m_sin" + x);
			} 
		}
		return inflectedForm;
	}

	/**
	 * return the base form of a word
	 *
	 * @param element
	 * @param baseWord
	 * @return
	 */
	private static String getBaseForm(InflectedWordElement element, WordElement baseWord) {
		if (LexicalCategory.VERB == element.getCategory()) {
			if (baseWord != null && baseWord.getDefaultSpellingVariant() != null)
				return baseWord.getDefaultSpellingVariant();
			else
				return element.getBaseForm();
		} else {
			if (element.getBaseForm() != null)
				return element.getBaseForm();
			else if (baseWord == null)
				return null;
			else
				return baseWord.getDefaultSpellingVariant();
		}
	}

	/**
	 * This method performs the morphology for verbs.
	 *
	 * @param element  the <code>InflectedWordElement</code>.
	 * @param baseWord the <code>WordElement</code> as created from the lexicon
	 *                 entry.
	 * @return a <code>StringElement</code> representing the word after
	 * inflection.
	 */
	protected static NLGElement doVerbMorphology(InflectedWordElement element, WordElement baseWord) {
		String realised = null;
		Object numberValue = element.getFeature(Feature.NUMBER);
		Object personValue = element.getFeature(Feature.PERSON);
		Object form = element.getFeature(Feature.FORM);
		Object tense = element.getFeature(Feature.TENSE);
		Object gender = element.getFeature(Feature.GENDER);
		Tense tenseValue;
		Gender genderValue;
		Form formValue;
		Boolean modal = false;
		Boolean initiated_subord = false;

		// verbs in combination with modal verbs are kept in infinitive
		if (element.hasFeature(Feature.CONTAINS_MODAL)) {
			modal = element.getFeatureAsBoolean(Feature.CONTAINS_MODAL);
		}
		
		if (tense instanceof Tense) {
			tenseValue = (Tense) tense;
			if(tenseValue == Tense.FUTURE && element.hasFeature("ipu")) {
				tenseValue = Tense.PRESENT;
			}
		} else {
			tenseValue = Tense.PRESENT;
		}
		if (form instanceof Form) {
			formValue = (Form) form;
		} else {
			formValue = Form.NORMAL;
		}
		if (gender instanceof Gender) {
			genderValue = (Gender) gender;
		} else {
			genderValue = Gender.FEMININE;
		}

		// base form from baseWord if it exists, otherwise from element
		String baseForm = getBaseForm(element, baseWord);

		if (modal && !baseForm.matches("mieć|musieć|móc|potrafić|chcieć|zechcieć|raczyć|pragnąć|zapragnąć|zamierzać|postanawiać|postanowić|usiłować|woleć|lubić|polubić|pozwalać|pozwolić|zachcieć")
				&& tenseValue.equals(Tense.PRESENT)) {
			// if there is a modal verb in the phrase, following verbs are in infinitive
			StringElement realisedElement = new StringElement(baseForm);
			realisedElement.setFeature(InternalFeature.DISCOURSE_FUNCTION, element.getFeature(InternalFeature.DISCOURSE_FUNCTION));
			return realisedElement;
		}

		// get all features of verb
		Set<String> features = baseWord.getAllFeatureNames();

		if (formValue.equals(Form.PARTICIPLE_ACTIVE)) {
			if ((numberValue == null || NumberAgreement.SINGULAR.equals(numberValue))) {
				if ((Gender.MASC_PERSON.equals(gender) || Gender.MASC_OBJECT.equals(gender) || Gender.MASC_ANIMAL.equals(gender)) && features.contains("ipc_sin_m")) {
					realised = baseWord.getFeatureAsString("ipc_sin_m");
				}
				else if (Gender.FEMININE.equals(gender) && features.contains("ipc_sin_f")) {
					realised = baseWord.getFeatureAsString("ipc_sin_f");
				}
				else if (Gender.NEUTER.equals(gender) && features.contains("ipc_sin_n")) {
					realised = baseWord.getFeatureAsString("ipc_sin_n");
				}
				else {
					realised = baseForm;
				}
			}
			else {
				if ((Gender.MASC_PERSON.equals(gender) || Gender.MASC_OBJECT.equals(gender) || Gender.MASC_ANIMAL.equals(gender)) && features.contains("ipc_pl_m")) {
					realised = baseWord.getFeatureAsString("ipc_pl_m");
				}
				else if (Gender.FEMININE.equals(gender) && features.contains("ipc_pl_f")) {
					realised = baseWord.getFeatureAsString("ipc_pl_f");
				}
				else if (Gender.NEUTER.equals(gender) && features.contains("ipc_pl_m")) {
					realised = baseWord.getFeatureAsString("ipc_pl_m");
				}
				else {
					realised = baseForm;
				}
			}
		} else if (formValue.equals(Form.PARTICIPLE_PASSIVE)) {
			if ((numberValue == null || NumberAgreement.SINGULAR.equals(numberValue))) {
				if ((Gender.MASC_PERSON.equals(genderValue) || Gender.MASC_OBJECT.equals(genderValue) || Gender.MASC_ANIMAL.equals(genderValue)) && features.contains("ipb_sin_m")) {
					realised = baseWord.getFeatureAsString("ipb_sin_m");
				}
				else if (Gender.FEMININE.equals(genderValue) && features.contains("ipb_sin_f")) {
					realised = baseWord.getFeatureAsString("ipb_sin_f");
				}
				else if (Gender.NEUTER.equals(genderValue) && features.contains("ipb_sin_n")) {
					realised = baseWord.getFeatureAsString("ipb_sin_n");
				}
				else {
					realised = baseForm;
				}
			}
			else {
				if ((Gender.MASC_PERSON.equals(genderValue) || Gender.MASC_OBJECT.equals(genderValue) || Gender.MASC_ANIMAL.equals(genderValue)) && features.contains("ipb_pl_m")) {
					realised = baseWord.getFeatureAsString("ipb_pl_m");
				}
				else if (Gender.FEMININE.equals(genderValue) && features.contains("ipb_pl_f")) {
					realised = baseWord.getFeatureAsString("ipb_pl_f");
				}
				else if (Gender.NEUTER.equals(genderValue) && features.contains("ipb_pl_m")) {
					realised = baseWord.getFeatureAsString("ipb_pl_m");
				}
				else {
					realised = baseForm;
				}
			}
		} else if (formValue.equals(Form.PAST_PARTICIPLE)) {
			if (features.contains("ipu")) {
				realised = baseWord.getFeatureAsString("ipu");
			}
			else {
				realised = baseForm;
			}
		} else if (formValue.equals(Form.TRANSGRESSIVE)) {
			if (features.contains("transgressive")) {
				realised = baseWord.getFeatureAsString("transgressive");
			}
			else {
				realised = baseForm;
			}
		} else if (formValue.equals(Form.IMPERATIVE)) {
			if ((numberValue == null || NumberAgreement.SINGULAR.equals(numberValue))) {
				if (Person.FIRST.equals(personValue) && features.contains("command_sin_1")) {
					realised = baseWord.getFeatureAsString("command_sin_1");
				} else if (Person.SECOND.equals(personValue) && features.contains("command_sin_2")) {
					realised = baseWord.getFeatureAsString("command_sin_2");
				} else if (Person.THIRD.equals(personValue) && features.contains("command_sin_3")) {
					realised = baseWord.getFeatureAsString("command_sin_3");
				} else {
					realised = baseForm;
				}
			}
			else {
				if (Person.FIRST.equals(personValue) && features.contains("command_pl_1")) {
					realised = baseWord.getFeatureAsString("command_pl_1");
				} else if (Person.SECOND.equals(personValue) && features.contains("command_pl_2")) {
					realised = baseWord.getFeatureAsString("command_pl_2");
				} else if (Person.THIRD.equals(personValue) && features.contains("command_pl_3")) {
					realised = baseWord.getFeatureAsString("command_pl_3");
				} else {
					realised = baseForm;
				}
			}
		} else if (formValue.equals(Form.CONDITIONAL)) {
			if ((numberValue == null || NumberAgreement.SINGULAR.equals(numberValue))) {
				if (Person.FIRST.equals(personValue)) {
					if ((Gender.MASC_PERSON.equals(genderValue) || Gender.MASC_OBJECT.equals(genderValue) || Gender.MASC_ANIMAL.equals(genderValue)) && features.contains("past_sin_1_m")) {
						realised = baseWord.getFeatureAsString("past_sin_1_m") + "bym";
					}
					else if (Gender.FEMININE.equals(genderValue) && features.contains("past_sin_1_f")) {
						realised = baseWord.getFeatureAsString("past_sin_1_f") + "bym";
					}
					else if (Gender.NEUTER.equals(genderValue) && features.contains("past_sin_1_m")) {
						realised = baseWord.getFeatureAsString("past_sin_1_m") + "bym";
					}
				} else if (Person.SECOND.equals(personValue)) {
					if ((Gender.MASC_PERSON.equals(genderValue) || Gender.MASC_OBJECT.equals(genderValue) || Gender.MASC_ANIMAL.equals(genderValue)) && features.contains("past_sin_2_m")) {
						realised = baseWord.getFeatureAsString("past_sin_2_m") + "byś";
					}
					else if (Gender.FEMININE.equals(genderValue) && features.contains("past_sin_2_f")) {
						realised = baseWord.getFeatureAsString("past_sin_2_f") + "byś";
					}
					else if (Gender.NEUTER.equals(genderValue) && features.contains("past_sin_2_m")) {
						realised = baseWord.getFeatureAsString("past_sin_2_m") + "byś";
					}
				} else if (Person.THIRD.equals(personValue)) {
					if ((Gender.MASC_PERSON.equals(genderValue) || Gender.MASC_OBJECT.equals(genderValue) || Gender.MASC_ANIMAL.equals(genderValue)) && features.contains("past_sin_3_m")) {
						realised = baseWord.getFeatureAsString("past_sin_3_m") + "by";
					}
					else if (Gender.FEMININE.equals(genderValue) && features.contains("past_sin_3_f")) {
						realised = baseWord.getFeatureAsString("past_sin_3_f") + "by";
					}
					else if (Gender.NEUTER.equals(genderValue) && features.contains("past_sin_3_n")) {
						realised = baseWord.getFeatureAsString("past_sin_3_n") + "by";
					}
				} else {
					realised = "będzie " + baseForm;
				}
			} else {
				if (Person.FIRST.equals(personValue)) {
					if ((Gender.MASC_PERSON.equals(genderValue) || Gender.MASC_OBJECT.equals(genderValue) || Gender.MASC_ANIMAL.equals(genderValue)) && features.contains("past_pl_1_m")) {
						realised = baseWord.getFeatureAsString("past_pl_1_m") + "byśmy";
					}
					else if (Gender.FEMININE.equals(genderValue) && features.contains("past_pl_1_f")) {
						realised = baseWord.getFeatureAsString("past_pl_1_f") + "byśmy";
					}
					else if (Gender.NEUTER.equals(genderValue) && features.contains("past_pl_1_m")) {
						realised = baseWord.getFeatureAsString("past_pl_1_m") + "byśmy";
					}
				} else if (Person.SECOND.equals(personValue)) {
					if ((Gender.MASC_PERSON.equals(genderValue) || Gender.MASC_OBJECT.equals(genderValue) || Gender.MASC_ANIMAL.equals(genderValue)) && features.contains("past_pl_2_m")) {
						realised = baseWord.getFeatureAsString("past_pl_2_m") + "byście";
					}
					else if (Gender.FEMININE.equals(genderValue) && features.contains("past_pl_2_f")) {
						realised = baseWord.getFeatureAsString("past_pl_2_f") + "byście";
					}
					else if (Gender.NEUTER.equals(genderValue) && features.contains("past_pl_2_m")) {
						realised = baseWord.getFeatureAsString("past_pl_2_m") + "byście";
					}
				} else if (Person.THIRD.equals(personValue)) {
					if ((Gender.MASC_PERSON.equals(genderValue) || Gender.MASC_OBJECT.equals(genderValue) || Gender.MASC_ANIMAL.equals(genderValue)) && features.contains("past_pl_3_m")) {
						realised = baseWord.getFeatureAsString("past_pl_3_m") + "by";
					}
					else if (Gender.FEMININE.equals(genderValue) && features.contains("past_pl_3_f")) {
						realised = baseWord.getFeatureAsString("past_pl_3_f") + "by";
					}
					else if (Gender.NEUTER.equals(genderValue) && features.contains("past_pl_3_m")) {
						realised = baseWord.getFeatureAsString("past_pl_3_m") + "by";
					}
				} else {
					realised = baseForm;
				}
			}
		} else {
			if (tenseValue == null || Tense.PRESENT.equals(tenseValue)) {
					// Singular
					if ((numberValue == null || NumberAgreement.SINGULAR.equals(numberValue))) {
						if (Person.FIRST.equals(personValue) && features.contains("present_sin_1")) {
							realised = baseWord.getFeatureAsString("present_sin_1");
						} else if (Person.SECOND.equals(personValue) && features.contains("present_sin_2")) {
							realised = baseWord.getFeatureAsString("present_sin_2");
						} else if (Person.THIRD.equals(personValue) && features.contains("present_sin_3")) {
							realised = baseWord.getFeatureAsString("present_sin_3");
						} else {
							realised = baseForm;
						}
					} else {
						if (Person.FIRST.equals(personValue) && features.contains("present_pl_1")) {
							realised = baseWord.getFeatureAsString("present_pl_1");
						} else if (Person.SECOND.equals(personValue) && features.contains("present_pl_2")) {
							realised = baseWord.getFeatureAsString("present_pl_2");
						} else if (Person.THIRD.equals(personValue) && features.contains("present_pl_3")) {
							realised = baseWord.getFeatureAsString("present_pl_3");
						} else {
							realised = baseForm;
						}
					}

				} else if (Tense.PAST.equals(tenseValue)) {
					if ((numberValue == null || NumberAgreement.SINGULAR.equals(numberValue))) {
						if (Person.FIRST.equals(personValue)) {
							if ((Gender.MASC_PERSON.equals(genderValue) || Gender.MASC_OBJECT.equals(genderValue) || Gender.MASC_ANIMAL.equals(genderValue)) && features.contains("past_sin_1_m")) {
								realised = baseWord.getFeatureAsString("past_sin_1_m");
							}
							else if (Gender.FEMININE.equals(genderValue) && features.contains("past_sin_1_f")) {
								realised = baseWord.getFeatureAsString("past_sin_1_f");
							}
							else if (Gender.NEUTER.equals(genderValue) && features.contains("past_sin_1_m")) {
								realised = baseWord.getFeatureAsString("past_sin_1_m");
							}
						} else if (Person.SECOND.equals(personValue)) {
							if ((Gender.MASC_PERSON.equals(genderValue) || Gender.MASC_OBJECT.equals(genderValue) || Gender.MASC_ANIMAL.equals(genderValue)) && features.contains("past_sin_2_m")) {
								realised = baseWord.getFeatureAsString("past_sin_2_m");
							}
							else if (Gender.FEMININE.equals(genderValue) && features.contains("past_sin_2_f")) {
								realised = baseWord.getFeatureAsString("past_sin_2_f");
							}
							else if (Gender.NEUTER.equals(genderValue) && features.contains("past_sin_2_m")) {
								realised = baseWord.getFeatureAsString("past_sin_2_m");
							}
						} else if (Person.THIRD.equals(personValue)) {
							if ((Gender.MASC_PERSON.equals(genderValue) || Gender.MASC_OBJECT.equals(genderValue) || Gender.MASC_ANIMAL.equals(genderValue)) && features.contains("past_sin_3_m")) {
								realised = baseWord.getFeatureAsString("past_sin_3_m");
							}
							else if (Gender.FEMININE.equals(genderValue) && features.contains("past_sin_3_f")) {
								realised = baseWord.getFeatureAsString("past_sin_3_f");
							}
							else if (Gender.NEUTER.equals(genderValue) && features.contains("past_sin_3_n")) {
								realised = baseWord.getFeatureAsString("past_sin_3_n");
							}
						} else {
							realised = baseForm;
						}
					} else {
						if (Person.FIRST.equals(personValue)) {
							if ((Gender.MASC_PERSON.equals(genderValue) || Gender.MASC_OBJECT.equals(genderValue) || Gender.MASC_ANIMAL.equals(genderValue)) && features.contains("past_pl_1_m")) {
								realised = baseWord.getFeatureAsString("past_pl_1_m");
							}
							else if (Gender.FEMININE.equals(genderValue) && features.contains("past_pl_1_f")) {
								realised = baseWord.getFeatureAsString("past_pl_1_f");
							}
							else if (Gender.NEUTER.equals(genderValue) && features.contains("past_pl_1_m")) {
								realised = baseWord.getFeatureAsString("past_pl_1_m");
							}
						} else if (Person.SECOND.equals(personValue)) {
							if ((Gender.MASC_PERSON.equals(genderValue) || Gender.MASC_OBJECT.equals(genderValue) || Gender.MASC_ANIMAL.equals(genderValue)) && features.contains("past_pl_2_m")) {
								realised = baseWord.getFeatureAsString("past_pl_2_m");
							}
							else if (Gender.FEMININE.equals(genderValue) && features.contains("past_pl_2_f")) {
								realised = baseWord.getFeatureAsString("past_pl_2_f");
							}
							else if (Gender.NEUTER.equals(genderValue) && features.contains("past_pl_2_m")) {
								realised = baseWord.getFeatureAsString("past_pl_2_m");
							}
						} else if (Person.THIRD.equals(personValue)) {
							if ((Gender.MASC_PERSON.equals(genderValue) || Gender.MASC_OBJECT.equals(genderValue) || Gender.MASC_ANIMAL.equals(genderValue)) && features.contains("past_pl_3_m")) {
								realised = baseWord.getFeatureAsString("past_pl_3_m");
							}
							else if (Gender.FEMININE.equals(genderValue) && features.contains("past_pl_3_f")) {
								realised = baseWord.getFeatureAsString("past_pl_3_f");
							}
							else if (Gender.NEUTER.equals(genderValue) && features.contains("past_pl_3_m")) {
								realised = baseWord.getFeatureAsString("past_pl_3_m");
							}
						} else {
							realised = baseForm;
						}
					}
				} else if (tenseValue == null || Tense.FUTURE.equals(tenseValue)) {
					if ((numberValue == null || NumberAgreement.SINGULAR.equals(numberValue))) {
						if (Person.FIRST.equals(personValue)) {
							realised = "będę " + baseForm;
						} else if (Person.SECOND.equals(personValue)) {
							realised = "będziesz " + baseForm;
						} else if (Person.THIRD.equals(personValue)) {
							realised = "będzie " + baseForm;
						} else {
							realised = "będzie " + baseForm;
						}
					} else {
						if (Person.FIRST.equals(personValue)) {
							realised = "będziemy " + baseForm;
						} else if (Person.SECOND.equals(personValue)) {
							realised = "będziecie " + baseForm;
						} else if (Person.THIRD.equals(personValue)) {
							realised = "będą " + baseForm;
						} else {
							realised = baseForm;
						}
					}
				} 
			}
		if (realised == null && baseForm != null) {
			realised = baseForm;
		}
		StringElement realisedElement = new StringElement(realised);
		// as adverbs can be placed inside a separable verb, e.g. "schneidet gut ab",
		// the separable feature needs to be passed further to change word order
		realisedElement.setFeature(InternalFeature.DISCOURSE_FUNCTION,
				element.getFeature(InternalFeature.DISCOURSE_FUNCTION));
		return realisedElement;
	}

	/**
	 * This method extracts the stem of a verb.
	 *
	 * @param baseForm the <code>base Form of a word</code>.
	 * @return a <code>String</code> representing the word after
	 * stemming.
	 */

	protected static NLGElement doAdjectiveMorphology(InflectedWordElement element, WordElement baseWord) {
		String realised = getBaseForm(element, baseWord);
		Object numberValue = element.getFeature(Feature.NUMBER);
		String genus = element.getFeatureAsString(LexicalFeature.GENDER);
		Set<String> features = baseWord.getAllFeatureNames();
		
		Boolean is_comparative = element.hasFeature(Feature.IS_COMPARATIVE)
				&& element.getFeatureAsBoolean(Feature.IS_COMPARATIVE);
		Boolean is_superlative = element.hasFeature(Feature.IS_SUPERLATIVE)
				&& element.getFeatureAsBoolean(Feature.IS_SUPERLATIVE);

		ArticleForm articleForm = ArticleForm.NONE;	
		if(element.getFeature(Feature.ARTICLE_FORM) instanceof ArticleForm) {
			articleForm = (ArticleForm) element.getFeature(Feature.ARTICLE_FORM);
		}

		//default grammatical case
		String grammCase = "SUBJECT";
		if (element.hasFeature(InternalFeature.CASE_PARENT) &&
				element.getFeature(InternalFeature.CASE_PARENT) instanceof DiscourseFunction) {
			grammCase = element.getFeatureAsString(InternalFeature.CASE_PARENT);
		}
		if (element.hasFeature(InternalFeature.CASE) &&
				element.getFeature(InternalFeature.CASE) instanceof DiscourseFunction) {
			grammCase = element.getFeatureAsString(InternalFeature.CASE);
		}
		// default genus: most nouns are feminine according to Duden
		if(genus == null) {
			genus = "FEMININE";
		}
		String baseForm = getBaseForm(element, baseWord);
		String cstring = "";
		String nstring = "";
		String gstring = "";
		String csstring = "";
		if(grammCase == "SUBJECT") {
			cstring = "m_";
		}
		else if(grammCase == "INDIRECT_OBJECT") {
			cstring = "c_";
		}
		else if(grammCase == "GENITIVE") {
			cstring = "d_";
		}
		else if(grammCase == "OBJECT") {
			cstring = "b_";
		}
		else if(grammCase == "LOCATIVE") {
			cstring = "msc_";
		}
		else if(grammCase == "INSTRUMENTAL") {
			cstring = "n_";
		}
		else if(grammCase == "VOCATIVE") {
			cstring = "w_";
		}
		else {
			realised = baseForm;
		}
		if (NumberAgreement.PLURAL.equals(numberValue)) {
			nstring = "pl";
			if(grammCase == "SUBJECT" || grammCase == "OBJECT" || grammCase == "VOCATIVE") {
				if(genus == "MASC_PERSON" || genus == "MASC_OBJECT" || genus == "MASC_ANIMAL") {
					gstring = "_m";
				}
				else if(genus == "FEMININE") {
					gstring = "_f";
				}
				else if(genus == "NEUTER") {
					gstring = "_m";
				}
				else {
					realised = baseForm;
				}
			}
		} else {
			nstring = "sin_";
			if(genus == "MASC_PERSON" || genus == "MASC_ANIMAL") {
				if(grammCase == "OBJECT") {
					gstring = "m_p";
				}
				else {
					gstring = "m";
				}
			}
			else if(genus == "MASC_OBJECT") {
				if(grammCase == "OBJECT") {
					gstring = "m_o";
				}
				else {
					gstring = "m";
				}
			}
			else if(genus == "FEMININE") {
				gstring = "f";
			}
			else if(genus == "NEUTER") {
				gstring = "n";
			}
			else {
				realised = baseForm;
			}
		}
		
		if(is_comparative) {
			csstring = "_comp";
		} 
		else if(is_superlative) {
			csstring = "_sup";
		}
		String specialstring = cstring + nstring + gstring + csstring;
		if(element.hasFeature(specialstring)) {
			realised = baseWord.getFeatureAsString(specialstring);
		}
		else {
			realised =  baseForm;
		}
			
		StringElement realisedElement = new StringElement(realised);
		realisedElement.setFeature(InternalFeature.DISCOURSE_FUNCTION,
				element.getFeature(InternalFeature.DISCOURSE_FUNCTION));
		return realisedElement;
	}
	
	protected static NLGElement doAdverbMorphology(InflectedWordElement element, WordElement baseWord) {
		String realised = getBaseForm(element, baseWord);
		Set<String> features = baseWord.getAllFeatureNames();

		Boolean is_comparative = element.hasFeature(Feature.IS_COMPARATIVE)
				&& element.getFeatureAsBoolean(Feature.IS_COMPARATIVE);
		Boolean is_superlative = element.hasFeature(Feature.IS_SUPERLATIVE)
				&& element.getFeatureAsBoolean(Feature.IS_SUPERLATIVE);

		if(is_comparative) {
			realised = baseWord.getFeatureAsString("comp");
		} 
		if(is_superlative) {
			realised = baseWord.getFeatureAsString("sup");
		}

		StringElement realisedElement = new StringElement(realised);
		realisedElement.setFeature(InternalFeature.DISCOURSE_FUNCTION,
				element.getFeature(InternalFeature.DISCOURSE_FUNCTION));
		return realisedElement;
	}


	/**
	 * This method is the main method to perform the morphology for articles.
	 *
	 * @param element  the <code>InflectedWordElement</code>.
	 * @param baseWord the <code>WordElement</code> as created from the lexicon
	 *                 entry.
	 * @return a <code>StringElement</code> representing the word after
	 * inflection.
	 */

	/**
	 * This method is the main method to perform the morphology for indefinite pronouns.
	 *
	 * @param element  the <code>InflectedWordElement</code>.
	 * @param baseWord the <code>WordElement</code> as created from the lexicon
	 *                 entry.
	 * @return a <code>StringElement</code> representing the word after
	 * inflection.
	 */

	/**
	 * The method for getting the number of syllables for a given word
	 *
	 * @param s the given word
	 * @return the number of syllables
	 */
	public static int getNumberOfSyllables(String s) {
		s = s.trim();
		if (s.length() <= 3) {
			return 1;
		}
		s = s.toLowerCase();
		s = s.replaceAll("[aeiouyąęó]+", "a");
		s = "x" + s + "x";
		return s.split("a").length - 1;
	}
	
	//END
	
	//method for pronouns just using the noun method
	//method for possessive pronouns
}
