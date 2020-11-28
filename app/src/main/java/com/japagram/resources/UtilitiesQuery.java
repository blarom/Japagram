package com.japagram.resources;

import android.text.TextUtils;

import com.japagram.data.InputQuery;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class UtilitiesQuery {
    @NotNull
    @Contract("_ -> new")
    public static String[] getOfficialKana(String input) {

        if (Globals.Romanizations == null) {
            return new String[]{"", "", "", ""};
        }
        //Transliterations performed according to https://en.wikipedia.org/wiki/Romanization_of_Japanese
        /*
        Rules:
        The combination o + u is written ou if they are in two adjacent syllables or it is the end part of terminal form of a verb
        The combination u + u is written uu if they are in two adjacent syllables or it is the end part of terminal form of a verb

         */
        String transliteratedToHiragana = input;
        String transliteratedToKatakana = input;
        String[] currentRow;
        String currentChar;
        int[] romajiTypes = new int[]{
                Globals.ROM_COL_WAAPURO,
                Globals.ROM_COL_MOD_HEPBURN,
                Globals.ROM_COL_NIHON_SHIKI,
                Globals.ROM_COL_KUNREI_SHIKI
        };

        //Translating from Katakana to Hiragana
        for (int i = 1; i < Globals.Romanizations.size(); i++) {
            currentRow = Globals.Romanizations.get(i);
            if (currentRow.length < 6) break;
            currentChar = currentRow[Globals.ROM_COL_KATAKANA];
            if (currentChar.equals("")) continue;
            transliteratedToHiragana = transliteratedToHiragana.replace(currentChar, currentRow[Globals.ROM_COL_HIRAGANA]);
        }

        //Translating from from Hiragana to Katakana
        for (int i = 1; i < Globals.Romanizations.size(); i++) {
            currentRow = Globals.Romanizations.get(i);
            if (currentRow.length < 6) break;
            currentChar = currentRow[Globals.ROM_COL_HIRAGANA];
            if (currentChar.equals("")) continue;
            transliteratedToKatakana = transliteratedToKatakana.replace(currentChar, currentRow[Globals.ROM_COL_KATAKANA]);
        }

        //Translating from Romaji to Kana
        for (int type : romajiTypes) {

            for (int i = 1; i < Globals.Romanizations.size(); i++) {
                currentRow = Globals.Romanizations.get(i);
                if (currentRow.length < 6) break;

                currentChar = currentRow[type];
                if (currentChar.equals("")
                        || currentChar.equals("aa")
                        || currentChar.equals("ii")
                        || currentChar.equals("uu")
                        || currentChar.equals("ee")
                        || currentChar.equals("oo"))
                    continue;
                transliteratedToHiragana = transliteratedToHiragana.replace(currentChar, currentRow[Globals.ROM_COL_HIRAGANA]);
                transliteratedToKatakana = transliteratedToKatakana.replace(currentChar, currentRow[Globals.ROM_COL_KATAKANA]);
            }

            //If there are leftover double-vowels, only then should they be replaced
            for (int i = 1; i < Globals.Romanizations.size(); i++) {
                currentRow = Globals.Romanizations.get(i);
                if (currentRow.length < 6) break;

                currentChar = currentRow[type];
                if (currentChar.equals("")
                        || !(currentChar.equals("aa")
                        || currentChar.equals("ii")
                        || currentChar.equals("uu")
                        || currentChar.equals("ee")
                        || currentChar.equals("oo")))
                    continue;
                transliteratedToHiragana = transliteratedToHiragana.replace(currentChar, currentRow[Globals.ROM_COL_HIRAGANA]);
                transliteratedToKatakana = transliteratedToKatakana.replace(currentChar, currentRow[Globals.ROM_COL_KATAKANA]);
            }
        }

        //If there are leftover consonants, replace them by a japanized version
        String oldTransliteration = transliteratedToHiragana;
        transliteratedToHiragana = transliteratedToHiragana.replaceAll("([bdfghjkmnprstvz])","$1u");
        transliteratedToKatakana = transliteratedToKatakana.replaceAll("([bdfghjkmnprstvz])","$1u");

        if (!oldTransliteration.equals(transliteratedToHiragana)) {
            for (int i = 1; i < Globals.Romanizations.size(); i++) {
                currentRow = Globals.Romanizations.get(i);
                if (currentRow.length < 6) break;

                currentChar = currentRow[Globals.ROM_COL_WAAPURO];
                if (currentChar.equals("")
                        || currentChar.equals("aa")
                        || currentChar.equals("ii")
                        || currentChar.equals("uu")
                        || currentChar.equals("ee")
                        || currentChar.equals("oo"))
                    continue;
                transliteratedToHiragana = transliteratedToHiragana.replace(currentChar, currentRow[Globals.ROM_COL_HIRAGANA]);
                transliteratedToKatakana = transliteratedToKatakana.replace(currentChar, currentRow[Globals.ROM_COL_KATAKANA]);
            }
        }

        //Cleaning the leftovers
        transliteratedToHiragana = transliteratedToHiragana.replaceAll("[a-z]", "*");
        transliteratedToKatakana = transliteratedToKatakana.replaceAll("[a-z]", "*");

        return new String[]{transliteratedToHiragana, transliteratedToKatakana};
    }

    @NotNull
    @Contract("_ -> new")
    public static String[] getOfficialRomanizations(String kana) {

        if (Globals.Romanizations == null) {
            return new String[]{"", "", "", ""};
        }
        //Transliterations performed according to https://en.wikipedia.org/wiki/Romanization_of_Japanese
        /*
        Rules:
        The combination o + u is written ou if they are in two adjacent syllables or it is the end part of terminal form of a verb
        The combination u + u is written uu if they are in two adjacent syllables or it is the end part of terminal form of a verb

         */
        String romanizedKanaWaapuro = kana;
        String romanizedKanaModHepburn = kana;
        String romanizedKanaNihonShiki = kana;
        String romanizedKanaKunreiShiki = kana;
        String[] currentRow;
        String currentKana;
        for (int i = 1; i< Globals.Romanizations.size(); i++) {
            currentRow = Globals.Romanizations.get(i);
            if (currentRow.length < 6) break;

            currentKana = currentRow[Globals.ROM_COL_HIRAGANA];
            if (!currentKana.equals("")) {
                romanizedKanaWaapuro = romanizedKanaWaapuro.replace(currentKana, currentRow[Globals.ROM_COL_WAAPURO]);
                romanizedKanaModHepburn = romanizedKanaModHepburn.replace(currentKana, currentRow[Globals.ROM_COL_MOD_HEPBURN]);
                romanizedKanaNihonShiki = romanizedKanaNihonShiki.replace(currentKana, currentRow[Globals.ROM_COL_NIHON_SHIKI]);
                romanizedKanaKunreiShiki = romanizedKanaKunreiShiki.replace(currentKana, currentRow[Globals.ROM_COL_KUNREI_SHIKI]);
            }

            currentKana = currentRow[Globals.ROM_COL_KATAKANA];
            if (!currentKana.equals("")) {
                romanizedKanaWaapuro = romanizedKanaWaapuro.replace(currentKana, currentRow[Globals.ROM_COL_WAAPURO]);
                romanizedKanaModHepburn = romanizedKanaModHepburn.replace(currentKana, currentRow[Globals.ROM_COL_MOD_HEPBURN]);
                romanizedKanaNihonShiki = romanizedKanaNihonShiki.replace(currentKana, currentRow[Globals.ROM_COL_NIHON_SHIKI]);
                romanizedKanaKunreiShiki = romanizedKanaKunreiShiki.replace(currentKana, currentRow[Globals.ROM_COL_KUNREI_SHIKI]);
            }
        }

        return new String[]{romanizedKanaWaapuro, romanizedKanaModHepburn, romanizedKanaNihonShiki, romanizedKanaKunreiShiki};
    }

    @NotNull
    public static List<String> getWaapuroRomanizationsFromLatinText(String text) {

        text = text.toLowerCase();
        List<String> finalStrings = new ArrayList<>();

        //Reverting directly from Nihon/Kunrei-Shiki special forms to Waapuro
        text = text.replace("sy","sh")
                .replace("ty","ch");

        //Phonemes that can have multiple Waapuro equivalents are prepared here
        //Note that wi we wo (ゐ ゑ を - i e o in MH/NH/KH romanizations) are not handled here since they could lead to too many false positives
        text = text.replace("j","A")
                .replace("zy","B")
                .replace("ts","C")
                .replace("zu","D")
                .replace("du","O")
                .replace("wê","E")
                .replace("dû","F")
                .replace("si","G")
                //.replace("ti","H")
                .replaceAll("([^sc])hu","$1I")
                .replaceAll("([^sc])hû","$1J")
                .replace("tû","K")
                //.replace("tu","L")
                .replace("zû","M")
                .replace("n\'","N")
                .replaceAll("t$","to")
                .replaceAll("c([aeiou])","k$1")
                .replaceAll("l([aeiou])","r$1")
                .replaceAll("q([aeiou])","k$1");

        //Replacing relevant phonemes with the Waapuro equivalent
        List<List<String>> possibleInterpretations = new ArrayList<>();
        String[] newPhonemes;
        for (String character : text.split("(?!^)")) {
            switch (character) {
                case "ō":
                case "ô":
                    newPhonemes = new String[]{"ou", "oo"};
                    break;
                case "A":
                case "B":
                    newPhonemes = new String[]{"j", "dy"};
                    break;
                case "C":
                    newPhonemes = new String[]{"ts"};
                    break;
                case "D":
                case "O":
                    newPhonemes = new String[]{"zu", "du"};
                    break;
                case "E":
                case "ē":
                case "ê":
                    newPhonemes = new String[]{"ee"};
                    break;
                case "F":
                    newPhonemes = new String[]{"zuu"};
                    break;
                case "G":
                    newPhonemes = new String[]{"shi"};
                    break;
//                case "H":
//                    newPhonemes = new String[]{"chi"};
//                    break;
                case "I":
                    newPhonemes = new String[]{"hu", "fu"};
                    break;
                case "J":
                    newPhonemes = new String[]{"fuu"};
                    break;
                case "K":
                    newPhonemes = new String[]{"tsuu"};
                    break;
//                case "L":
//                    newPhonemes = new String[]{"tsu"};
//                    break;
                case "M":
                    newPhonemes = new String[]{"duu", "zuu"};
                    break;
                case "N":
                    newPhonemes = new String[]{"n'"};
                    break;
                case "ā":
                case "â":
                    newPhonemes = new String[]{"aa"};
                    break;
                case "ū":
                case "û":
                    newPhonemes = new String[]{"uu"};
                    break;
                default:
                    newPhonemes = new String[]{character};
                    break;
            }
            possibleInterpretations = addPhonemesToInterpretations(possibleInterpretations, newPhonemes);
        }
        for (int i=0; i<possibleInterpretations.size(); i++) {
            finalStrings.add(TextUtils.join("", possibleInterpretations.get(i)));
        }
        return finalStrings;
    }

    @NotNull
    private static List<List<String>> addPhonemesToInterpretations(List<List<String>> possibleInterpretations, @NotNull String[] phonemes) {
        List<String> newCharacterList;
        if (phonemes.length == 2) {
            if (possibleInterpretations.size() == 0) {
                newCharacterList = new ArrayList<>();
                newCharacterList.add(phonemes[0]);
                possibleInterpretations.add(newCharacterList);
                newCharacterList = new ArrayList<>();
                newCharacterList.add(phonemes[1]);
                possibleInterpretations.add(newCharacterList);
            } else {
                int initialSize = possibleInterpretations.size();
                for (int i = 0; i < initialSize; i++) {
                    newCharacterList = new ArrayList<>(possibleInterpretations.get(i));
                    newCharacterList.add(phonemes[0]);
                    possibleInterpretations.get(i).add(phonemes[1]);
                    possibleInterpretations.add(newCharacterList);
                }
            }
        }
        else if (phonemes.length == 1) {
            if (possibleInterpretations.size() == 0) {
                newCharacterList = new ArrayList<>();
                newCharacterList.add(phonemes[0]);
                possibleInterpretations.add(newCharacterList);
            } else {
                for (int i = 0; i < possibleInterpretations.size(); i++) {
                    possibleInterpretations.get(i).add(phonemes[0]);
                }
            }
        }
        else return new ArrayList<>();

        return possibleInterpretations;
    }

    public static List<String> extractKanjiChars(@NotNull String input) {

        List<String> mInputQueryKanjis = new ArrayList<>();
        for (int i=0; i<input.length(); i++) {
            String currentChar = input.substring(i,i+1);
            if (InputQuery.getTextType(currentChar) == Globals.TYPE_KANJI) {
                mInputQueryKanjis.add(currentChar);
            }
        }
        return mInputQueryKanjis;
    }

    @NotNull
    @Contract("_ -> new")
    public static Object[] getTransliterationsAsLists(String inputQuery) {

        List<String> waapuroRomanizations = getWaapuroRomanizationsFromLatinText(inputQuery);
        List<String> hiraganaConversions = new ArrayList<>();
        List<String> katakanaConversions = new ArrayList<>();
        List<String> waapuroConversions = new ArrayList<>();
        List<String> MHConversions = new ArrayList<>();
        List<String> NSConversions = new ArrayList<>();
        List<String> KSConversions = new ArrayList<>();
        for (String conversion : waapuroRomanizations) {
            String[] HK = getOfficialKana(conversion);
            String hiragana = HK[Globals.ROM_COL_HIRAGANA];
            String katakana = HK[Globals.ROM_COL_KATAKANA];
            String[] romanizations = getOfficialRomanizations(hiragana);
            hiraganaConversions.add(hiragana);
            katakanaConversions.add(katakana);
            waapuroConversions.add(romanizations[Globals.ROM_WAAPURO]);
            MHConversions.add(romanizations[Globals.ROM_MOD_HEPBURN]);
            NSConversions.add(romanizations[Globals.ROM_NIHON_SHIKI]);
            KSConversions.add(romanizations[Globals.ROM_KUNREI_SHIKI]);
        }

        return new Object[]{
                hiraganaConversions,
                katakanaConversions,
                waapuroConversions,
                MHConversions,
                NSConversions,
                KSConversions
        };
    }

    @NotNull
    public static List<String> getWaapuroHiraganaKatakana(String text) {

        List<String> conversionsFirstElement = new ArrayList<>();

        if (TextUtils.isEmpty(text)) {
            conversionsFirstElement.add("");
            conversionsFirstElement.add("");
            conversionsFirstElement.add("");
        } else {
            Object[] conversions = getTransliterationsAsLists(text);
            conversionsFirstElement.add(((List<String>) conversions[Globals.ROM_COL_WAAPURO]).get(0));
            conversionsFirstElement.add(((List<String>) conversions[Globals.ROM_COL_HIRAGANA]).get(0));
            conversionsFirstElement.add(((List<String>) conversions[Globals.ROM_COL_KATAKANA]).get(0));
        }

        return conversionsFirstElement;
    }

    public static Boolean isOfTypeIngIng(@NotNull String verb) {
        boolean answer = false;
        if (verb.equals("accinging") || verb.equals("astringing") || verb.equals("befringing") || verb.equals("besinging") ||
                verb.equals("binging") || verb.equals("boinging") || verb.equals("bowstringing") || verb.equals("bringing") ||
                verb.equals("clinging") || verb.equals("constringing") || verb.equals("cringing") || verb.equals("dinging") ||
                verb.equals("enringing") || verb.equals("flinging") || verb.equals("folksinging") || verb.equals("fringing") ||
                verb.equals("gunslinging") || verb.equals("hamstringing") || verb.equals("handwringing") || verb.equals("hinging") ||
                verb.equals("impinging") || verb.equals("inbringing") || verb.equals("infringing") || verb.equals("kinging") ||
                verb.equals("minging") || verb.equals("mudslinging") || verb.equals("outringing") || verb.equals("outsinging") ||
                verb.equals("outspringing") || verb.equals("outswinging") || verb.equals("outwinging") || verb.equals("overswinging") ||
                verb.equals("overwinging") || verb.equals("perstringing") || verb.equals("pinging") || verb.equals("refringing") ||
                verb.equals("rehinging") || verb.equals("respringing") || verb.equals("restringing") || verb.equals("ringing") ||
                verb.equals("singing") || verb.equals("slinging") || verb.equals("springing") || verb.equals("stinging") ||
                verb.equals("stringing") || verb.equals("swinging") || verb.equals("syringing") || verb.equals("twinging") ||
                verb.equals("unhinging") || verb.equals("unkinging") || verb.equals("unslinging") || verb.equals("unstringing") ||
                verb.equals("upbringing") || verb.equals("upflinging") || verb.equals("upspringing") || verb.equals("upswinging") ||
                verb.equals("whinging") || verb.equals("winging") || verb.equals("wringing") || verb.equals("zinging")) {
            answer = true;
        }
        return answer;
    }

    public static Object[] getInglessVerb(@NotNull String input, int originalType) {

        String returnVerb = input;
        boolean hasIngEndingLocal = false;
        if (input.length() > 2 && input.endsWith("ing")) {

            String inputWithoutIng = input.substring(0, input.length() - 3);
            if (input.length() > 5 && input.endsWith("inging")) {
                if ( (input.startsWith("to ") && isOfTypeIngIng(input.substring(3))) || (!input.startsWith("to ") && isOfTypeIngIng(input)) ) {
                    // If the verb ends with "inging" then remove the the second "ing"
                    returnVerb = inputWithoutIng;
                }
            }
            else {
                returnVerb = inputWithoutIng;
            }

            hasIngEndingLocal = originalType == Globals.TYPE_LATIN;
        }

        return new Object[]{returnVerb, hasIngEndingLocal};

    }
}
