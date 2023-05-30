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
 * Contributor(s) Polish version: Sasha Gdaniec
 */

package polishnlg.morphology;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import polishnlg.features.DiscourseFunction;
import polishnlg.features.InternalFeature;
import polishnlg.features.NumberAgreement;
import polishnlg.features.Person;
import polishnlg.features.Tense;
import polishnlg.features.Feature;
import polishnlg.features.Form;
import polishnlg.features.Gender;
import polishnlg.framework.*;
import polishnlg.orthography.*;
import polishnlg.lexicon.Lexicon;

//POLISH MORPHOLOGY CODE
public class MorphTest {
    private static Lexicon lexicon;
    private static NLGFactory nlgFactory;
    private static MorphologyProcessor realiser;
    private static OrthographyProcessor orthorealiser;

    @BeforeAll
    public static void setup() {
        lexicon = Lexicon.getDefaultLexicon();
        nlgFactory = new NLGFactory(lexicon);
        realiser = new MorphologyProcessor();
        orthorealiser = new OrthographyProcessor();
    }

    @Test
    public void testnounBasewordTest(){
        String[] base = {"człowiek", "chmura", "dziecko", "allegro"};
        String[] inf1 = {"człowieku", "chmurze", "dziecku", "allegro"};
        String[] inf2 = {"ludźmi", "chmurami", "dziećmi", "allegro"};
        String[] inf3 = {"ludzi", "chmur", "dzieci", "allegro"};

        for (int i = 0; i < base.length; i++){
            NLGElement nt1 = nlgFactory.createInflectedWord(base[i], LexicalCategory.NOUN);   
            
            System.out.println("1:" + nt1);
            nt1.setFeature(InternalFeature.CASE, DiscourseFunction.LOCATIVE);
            NLGElement output = realiser.realise(nt1);
            System.out.println("2:" + output);
            Assertions.assertEquals(inf1[i], output.toString());
            
            nt1.setPlural(true);
            nt1.setFeature(InternalFeature.CASE, DiscourseFunction.INSTRUMENTAL);
            NLGElement output2 = realiser.realise(nt1);
            System.out.println("3:" + output2);
            Assertions.assertEquals(inf2[i], output2.toString());
            
            nt1.setFeature(InternalFeature.CASE, DiscourseFunction.GENITIVE);
            NLGElement output3 = realiser.realise(nt1);
            System.out.println("4:" + output3);
            Assertions.assertEquals(inf3[i], output3.toString());
            
      
                 
        }
    }

    @Test
    public void testverbBasewordTest(){
        String[] base = {"zrobić", "lubić", "modlić się"};
        String[] inf1 = {"zrobiłaś", "lubiłaś", "modliłaś się"};
        String[] inf2 = {"zrobi", "lubi", "modli się"};
        String[] inf3 = {"zrobimy", "będziemy lubić", "będziemy modlić się"};
        String[] inf4 = {"zrobić", "lubiące", "modlące"};

        for (int i = 0; i < base.length; i++){
            NLGElement vt1 = nlgFactory.createInflectedWord(base[i], LexicalCategory.VERB);   
            
            System.out.println("1:" + vt1);
            vt1.setFeature(Feature.TENSE, Tense.PAST);
            vt1.setFeature(Feature.GENDER, Gender.FEMININE);
            vt1.setFeature(Feature.PERSON, Person.SECOND);
            NLGElement output = realiser.realise(vt1);
            System.out.println("2:" + output);
            Assertions.assertEquals(inf1[i], output.toString());
            
            vt1.setFeature(Feature.TENSE, Tense.PRESENT);
            vt1.setFeature(Feature.GENDER, Gender.NEUTER);
            vt1.setFeature(Feature.PERSON, Person.THIRD);
            NLGElement output2 = realiser.realise(vt1);
            System.out.println("3:" + output2);
            Assertions.assertEquals(inf2[i], output2.toString());
            
            vt1.setFeature(Feature.TENSE, Tense.FUTURE);
            vt1.setFeature(Feature.GENDER, Gender.MASC_PERSON);
            vt1.setFeature(Feature.PERSON, Person.FIRST);
            vt1.setPlural(true);
            NLGElement output3 = realiser.realise(vt1);
            System.out.println("4:" + output3);
            Assertions.assertEquals(inf3[i], output3.toString());
            
            vt1.setFeature(Feature.FORM, Form.PARTICIPLE_ACTIVE);
            vt1.setFeature(Feature.GENDER, Gender.FEMININE);
            NLGElement output4 = realiser.realise(vt1);
            System.out.println("5:" + output4);
            Assertions.assertEquals(inf4[i], output4.toString());
                 
        }
    }
    
    @Test
    public void testadjectiveBasewordTest(){
        String[] base = {"zielonym", "dobry", "ciekawy"};
        String[] inf1 = {"zielonym", "dobrym", "ciekawym"};
        String[] inf2 = {"zielony", "dobry", "ciekawy"};
        String[] inf3 = {"zielonego", "dobrego", "ciekawego"};
        String[] inf4 = {"zieleńsze", "lepsze", "ciekawsze"};

        for (int i = 0; i < base.length; i++){
            NLGElement at1 = nlgFactory.createInflectedWord(base[i], LexicalCategory.ADJECTIVE);   
            
            System.out.println("1:" + at1);
            at1.setFeature(InternalFeature.CASE, DiscourseFunction.LOCATIVE);
            at1.setFeature(Feature.NUMBER, NumberAgreement.SINGULAR);
            at1.setFeature(Feature.GENDER, Gender.NEUTER);
            NLGElement output = realiser.realise(at1);
            System.out.println("2:" + output);
            Assertions.assertEquals(inf1[i], output.toString());
            
            at1.setFeature(InternalFeature.CASE, DiscourseFunction.OBJECT);
            at1.setFeature(Feature.GENDER, Gender.MASC_OBJECT);
            NLGElement output2 = realiser.realise(at1);
            System.out.println("3:" + output2);
            Assertions.assertEquals(inf2[i], output2.toString());
            
            at1.setFeature(Feature.GENDER, Gender.MASC_PERSON);
            NLGElement output3 = realiser.realise(at1);
            System.out.println("4:" + output3);
            Assertions.assertEquals(inf3[i], output3.toString());
            
            at1.setPlural(true);
            at1.setFeature(InternalFeature.CASE, DiscourseFunction.VOCATIVE);
            at1.setFeature(Feature.GENDER, Gender.FEMININE);
            at1.setComparative(true);
            NLGElement output4 = realiser.realise(at1);
            System.out.println("5:" + output4);
            Assertions.assertEquals(inf4[i], output4.toString());
                 
        }
    }
    
    @Test
    public void testpronounBasewordTest(){
        String[] base = {"ja", "ty", "on"};
        String[] inf1 = {"mnie", "ciebie", "niego"};

        for (int i = 0; i < base.length; i++){
            NLGElement pt1 = nlgFactory.createInflectedWord(base[i], LexicalCategory.PRONOUN);   
            
            System.out.println("1:" + pt1);
            pt1.setFeature(InternalFeature.CASE, DiscourseFunction.OBJECT);
            pt1.setFeature(Feature.HAS_PREP, true);
            NLGElement output = realiser.realise(pt1);
            System.out.println("2:" + output);
            Assertions.assertEquals(inf1[i], output.toString());
    
        }
    }
    
    @Test
    public void testppronounBasewordTest(){
        String[] base = {"mój", "twój", "swój"};
        String[] inf1 = {"moją", "twoją", "swoją"};

        for (int i = 0; i < base.length; i++){
            NLGElement ppt1 = nlgFactory.createInflectedWord(base[i], LexicalCategory.POSSESSIVE_PRONOUN);   
            
            System.out.println("1:" + ppt1);
            ppt1.setFeature(InternalFeature.CASE, DiscourseFunction.INSTRUMENTAL);
            ppt1.setFeature(Feature.GENDER, Gender.FEMININE);
            NLGElement output = realiser.realise(ppt1);
            System.out.println("2:" + output);
            //Assertions.assertEquals(inf1[i], output.toString());
    
        }
    }
    
    @Test
    public void listTest(){
        String[] list = {"pies", "kot", "chomik"};
        
        ListElement tlist = nlgFactory.createListElement();
        
        NLGElement listel = nlgFactory.createWord(list[0], LexicalCategory.NOUN);   
           
        tlist.addComponent(listel);
        
        NLGElement out1 = orthorealiser.realise(tlist);
        
        System.out.println(out1);
        
        NLGElement listel1 = nlgFactory.createWord(list[1], LexicalCategory.NOUN);   
        
        tlist.addComponent(listel1);
        
        System.out.println(orthorealiser.realise(tlist));
        
        NLGElement listel2 = nlgFactory.createWord(list[2], LexicalCategory.NOUN);   
        
        tlist.addComponent(listel2);
        
        System.out.println(orthorealiser.realise(tlist));
    }
}