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
 * Contributor(s) Polish version: Sasha Gdaniec
 */

package polishnlg.features;

/**
 * <p>
 * An enumeration representing the different forms a verb and its associated
 * phrase can take. The form is recorded under the {@code Feature.FORM} feature
 * and applies to verbs and verb phrases.
 * </p>
 * @author A. Gatt and D. Westwater, University of Aberdeen.
 * @version 4.0
 *
 */
public enum Form {

    /**
     * The bare infinitive is the base form of the verb.
     */
    BARE_INFINITIVE,

    /**
     * In English, the gerund form refers to the usage of a verb as a noun. For
     * example, <em>I like <b>swimming</b></em>. In more general terms, gerunds
     * are usually formed from the base word with <em>-ing</em> added to the
     * end.
     */
    GERUND,

    /**
     * The imperative form of a verb is the one used when the grammatical
     * mood is one of expressing a command or giving a direct request. For example,
     * <em><b>Close</b> the door.</em>
     */
    IMPERATIVE,

    /**
     * The infinitive form represents the base form of the verb, with our
     * without the particle <em>to</em>. For example, <em>do</em> and
     * <em>to do</em> are both infinitive forms of <em>do</em>.
     */
    INFINITIVE,

    /**
     * Normal form represents the base verb. For example, <em>kiss</em>,
     * <em>walk</em>, <em>bark</em>, <em>eat</em>.
     */
    NORMAL,

    /**
     * Most verbs will have only a single form for the past tense. However, some
     * verbs will have two forms, one for the simple past tense and one for the
     * past participle (also knowns as passive participle or perfect
     * participle). The part participle represents the second of these two
     * forms. For example, the verb <em>eat</em> has the simple past form of
     * <em>ate</em> and also the past participle form of <em>eaten</em>. Another
     * example, is <em>write</em>, <em>wrote</em> and <em>written</em>.
     */
    PAST_PARTICIPLE,

    /**
     * The present participle is identical in form to the gerund and is normally
     * used in the active voice. However, the gerund is meant to highlight a
     * verb being used as a noun. The present participle remains as a verb. For
     * example, <em>Jim was <b>sleeping</b></em>.
     * 
     * In Polish, this is used more so as an adjective.
     */
    PARTICIPLE_ACTIVE,
    
    //POLISH MORPHOLOGY CODE
    
    /**
     * This is the passive voice form of the present participle, used in Polish, 
     * like the passive participle, similarly to an adjective
     */
    PARTICIPLE_PASSIVE,
    
    /**
     * Similarly to the active and passive present participles, in Polish, 
     * the transgressive form falls under the same category of 'imiesłów'
     * The transgressive is the verb used like an adverb. It can also be used in the past tense,
     * in which case it's called the 'IPW'
     */
    TRANSGRESSIVE,
    
    /**
     * Name is not formal, I have not been able to find a consistent translation of the Polish linguistic term
     * This is the 'bezosobowa' form, which does not figure in the lexicon, but is created from the singular masculine 
     * form of the passive present participle. Indicates an action that was being done at some point in the past in a commonplace manner, generally
     * Specifically, it is the form of the verb that does not indicate the person of the subject
     */
    PERSONLESS,
    
    /**
     * The conditional form of a verb, indicating that the verb *might* take place
     */
    CONDITIONAL;
}
