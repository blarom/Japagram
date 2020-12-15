package com.japagram.utilitiesCrossPlatform;

import com.japagram.utilitiesPlatformOverridable.OverridableUtilitiesGeneral;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class UtilitiesQuery {
    @NotNull
    @Contract("_ -> new")
    public static String[] getOfficialKana(String input) {
        if (Globals.GLOBAL_ROMANIZATIONS == null) {
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
        String currentChar;
        int[] romajiTypes = new int[]{
                Globals.ROM_COL_WAAPURO,
                Globals.ROM_COL_MOD_HEPBURN,
                Globals.ROM_COL_NIHON_SHIKI,
                Globals.ROM_COL_KUNREI_SHIKI
        };
        int romanizationsLength = Globals.GLOBAL_ROMANIZATIONS[Globals.ROM_COL_HIRAGANA].length;

        //Translating from Katakana to Hiragana
        for (int i = 1; i < romanizationsLength; i++) {
            currentChar = Globals.GLOBAL_ROMANIZATIONS[Globals.ROM_COL_KATAKANA][i];
            if (currentChar.equals("")) continue;
            transliteratedToHiragana = transliteratedToHiragana.replace(currentChar, Globals.GLOBAL_ROMANIZATIONS[Globals.ROM_COL_HIRAGANA][i]);
        }

        //Translating from from Hiragana to Katakana
        for (int i = 1; i < romanizationsLength; i++) {
            currentChar = Globals.GLOBAL_ROMANIZATIONS[Globals.ROM_COL_HIRAGANA][i];
            if (currentChar.equals("")) continue;
            transliteratedToKatakana = transliteratedToKatakana.replace(currentChar, Globals.GLOBAL_ROMANIZATIONS[Globals.ROM_COL_KATAKANA][i]);
        }

        //Translating from Romaji to Kana
        for (int type : romajiTypes) {

            for (int i = 1; i < romanizationsLength; i++) {
                currentChar = Globals.GLOBAL_ROMANIZATIONS[type][i];
                if (currentChar.equals("")
                        || currentChar.equals("aa")
                        || currentChar.equals("ii")
                        || currentChar.equals("uu")
                        || currentChar.equals("ee")
                        || currentChar.equals("oo"))
                    continue;
                transliteratedToHiragana = transliteratedToHiragana.replace(currentChar, Globals.GLOBAL_ROMANIZATIONS[Globals.ROM_COL_HIRAGANA][i]);
                transliteratedToKatakana = transliteratedToKatakana.replace(currentChar, Globals.GLOBAL_ROMANIZATIONS[Globals.ROM_COL_KATAKANA][i]);
            }

            //If there are leftover double-vowels, only then should they be replaced
            for (int i = 1; i < romanizationsLength; i++) {
                currentChar = Globals.GLOBAL_ROMANIZATIONS[type][i];
                if (currentChar.equals("")
                        || !(currentChar.equals("aa")
                        || currentChar.equals("ii")
                        || currentChar.equals("uu")
                        || currentChar.equals("ee")
                        || currentChar.equals("oo")))
                    continue;
                transliteratedToHiragana = transliteratedToHiragana.replace(currentChar, Globals.GLOBAL_ROMANIZATIONS[Globals.ROM_COL_HIRAGANA][i]);
                transliteratedToKatakana = transliteratedToKatakana.replace(currentChar, Globals.GLOBAL_ROMANIZATIONS[Globals.ROM_COL_KATAKANA][i]);
            }
        }

        //If there are leftover consonants, replace them by a japanized version
        String oldTransliteration = transliteratedToHiragana;
        transliteratedToHiragana = transliteratedToHiragana.replaceAll("([bdfghjkmnprstvz])","$1u");
        transliteratedToKatakana = transliteratedToKatakana.replaceAll("([bdfghjkmnprstvz])","$1u");

        if (!oldTransliteration.equals(transliteratedToHiragana)) {
            for (int i = 1; i < romanizationsLength; i++) {
                currentChar = Globals.GLOBAL_ROMANIZATIONS[Globals.ROM_COL_WAAPURO][i];
                if (currentChar.equals("")
                        || currentChar.equals("aa")
                        || currentChar.equals("ii")
                        || currentChar.equals("uu")
                        || currentChar.equals("ee")
                        || currentChar.equals("oo"))
                    continue;
                transliteratedToHiragana = transliteratedToHiragana.replace(currentChar, Globals.GLOBAL_ROMANIZATIONS[Globals.ROM_COL_HIRAGANA][i]);
                transliteratedToKatakana = transliteratedToKatakana.replace(currentChar, Globals.GLOBAL_ROMANIZATIONS[Globals.ROM_COL_KATAKANA][i]);
            }
        }

        //Cleaning the leftovers
        transliteratedToHiragana = transliteratedToHiragana.replaceAll("[a-z]", "*");
        transliteratedToKatakana = transliteratedToKatakana.replaceAll("[a-z]", "*");
        transliteratedToHiragana = transliteratedToHiragana.replace("#", "");
        transliteratedToKatakana = transliteratedToKatakana.replace("#", "");

        return new String[]{transliteratedToHiragana, transliteratedToKatakana};
    }

    @NotNull
    @Contract("_ -> new")
    public static String[] getOfficialRomanizations(String kana) {
        if (Globals.GLOBAL_ROMANIZATIONS == null) {
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
        String currentKana;
        int romanizationsLength = Globals.GLOBAL_ROMANIZATIONS[0].length;
        for (int i = 1; i < romanizationsLength; i++) {
            currentKana = Globals.GLOBAL_ROMANIZATIONS[Globals.ROM_COL_HIRAGANA][i];
            if (!currentKana.equals("")) {
                romanizedKanaWaapuro = romanizedKanaWaapuro.replace(currentKana, Globals.GLOBAL_ROMANIZATIONS[Globals.ROM_COL_WAAPURO][i]);
                romanizedKanaModHepburn = romanizedKanaModHepburn.replace(currentKana, Globals.GLOBAL_ROMANIZATIONS[Globals.ROM_COL_MOD_HEPBURN][i]);
                romanizedKanaNihonShiki = romanizedKanaNihonShiki.replace(currentKana, Globals.GLOBAL_ROMANIZATIONS[Globals.ROM_COL_NIHON_SHIKI][i]);
                romanizedKanaKunreiShiki = romanizedKanaKunreiShiki.replace(currentKana, Globals.GLOBAL_ROMANIZATIONS[Globals.ROM_COL_KUNREI_SHIKI][i]);
            }

            currentKana = Globals.GLOBAL_ROMANIZATIONS[Globals.ROM_COL_KATAKANA][i];
            if (!currentKana.equals("")) {
                romanizedKanaWaapuro = romanizedKanaWaapuro.replace(currentKana, Globals.GLOBAL_ROMANIZATIONS[Globals.ROM_COL_WAAPURO][i]);
                romanizedKanaModHepburn = romanizedKanaModHepburn.replace(currentKana, Globals.GLOBAL_ROMANIZATIONS[Globals.ROM_COL_MOD_HEPBURN][i]);
                romanizedKanaNihonShiki = romanizedKanaNihonShiki.replace(currentKana, Globals.GLOBAL_ROMANIZATIONS[Globals.ROM_COL_NIHON_SHIKI][i]);
                romanizedKanaKunreiShiki = romanizedKanaKunreiShiki.replace(currentKana, Globals.GLOBAL_ROMANIZATIONS[Globals.ROM_COL_KUNREI_SHIKI][i]);
            }
        }

        return new String[]{romanizedKanaWaapuro, romanizedKanaModHepburn, romanizedKanaNihonShiki, romanizedKanaKunreiShiki};
    }

    @NotNull
    public static List<String> getWaapuroRomanizationsFromLatinText(String text) {

        text = text.toLowerCase();
        List<String> finalStrings = new ArrayList<>();

        //Reverting directly from Nihon/Kunrei-Shiki special forms to Waapuro
        text = text.replace("sy","sh");
        text = text.replace("ty","ch");

        //Phonemes that can have multiple Waapuro equivalents are prepared here
        //Note that wi we wo (ゐ ゑ を - i e o in MH/NH/KH Globals.ROMANIZATIONS) are not handled here since they could lead to too many false positives
        text = text.replace("j","A");
        text = text.replace("zy","B");
        text = text.replace("ts","C");
        text = text.replace("zu","D");
        text = text.replace("du","O");
        text = text.replace("wê","E");
        text = text.replace("dû","F");
        text = text.replace("si","G");
                //.replace("ti","H")
        text = text.replaceAll("([^sc])hu","$1I");
        text = text.replaceAll("([^sc])hû","$1J");
        text = text.replace("tû","K");
                //.replace("tu","L")
        text = text.replace("zû","M");
        text = text.replace("n'","N");
        text = text.replaceAll("t$","to");
        text = text.replaceAll("c([aeiou])","k$1");
        text = text.replaceAll("l([aeiou])","r$1");
        text = text.replaceAll("q([aeiou])","k$1");

        //Replacing relevant phonemes with the Waapuro equivalent
        List<List<String>> possibleInterpretations = new ArrayList<>();
        String[] newPhonemes;
        for (String character : OverridableUtilitiesGeneral.splitToChars(text)) {
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
            finalStrings.add(OverridableUtilitiesGeneral.joinList("", possibleInterpretations.get(i)));
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

    public static @NotNull List<String> extractKanjiChars(@NotNull String input) {

        List<String> mInputQueryKanjis = new ArrayList<>();
        for (int i=0; i<input.length(); i++) {
            String currentChar = input.substring(i,i+1);
            if (getTextType(currentChar) == Globals.TEXT_TYPE_KANJI) {
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
        List<String> conversionsMH = new ArrayList<>();
        List<String> conversionsNS = new ArrayList<>();
        List<String> conversionsKS = new ArrayList<>();
        for (String conversion : waapuroRomanizations) {
            String[] hiraKata = getOfficialKana(conversion);
            String hiragana = hiraKata[Globals.ROM_COL_HIRAGANA];
            String katakana = hiraKata[Globals.ROM_COL_KATAKANA];
            String[] romanizations = getOfficialRomanizations(hiragana);
            hiraganaConversions.add(hiragana);
            katakanaConversions.add(katakana);
            waapuroConversions.add(romanizations[Globals.ROM_WAAPURO]);
            conversionsMH.add(romanizations[Globals.ROM_MOD_HEPBURN]);
            conversionsNS.add(romanizations[Globals.ROM_NIHON_SHIKI]);
            conversionsKS.add(romanizations[Globals.ROM_KUNREI_SHIKI]);
        }

        return new Object[]{
                hiraganaConversions,
                katakanaConversions,
                waapuroConversions,
                conversionsMH,
                conversionsNS,
                conversionsKS
        };
    }

    @NotNull
    public static List<String> getWaapuroHiraganaKatakana(String text) {

        List<String> conversionsFirstElement = new ArrayList<>();

        if (OverridableUtilitiesGeneral.isEmptyString(text)) {
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

            hasIngEndingLocal = originalType == Globals.TEXT_TYPE_LATIN;
        }

        return new Object[]{returnVerb, hasIngEndingLocal};

    }

    public static int getTextType(@NotNull String input_value) {

        if (input_value.contains("*") || input_value.contains("＊") || input_value.equals("") || input_value.equals("-") ) { return Globals.TEXT_TYPE_INVALID;}

        input_value = UtilitiesGeneral.removeSpecialCharacters(input_value);
        String character;
        int text_type = Globals.TEXT_TYPE_INVALID;

        String hiraganaAlphabet = "あいうえおかきくけこがぎぐげごさしすせそざじずぜぞたてとだでどちつづなぬねのんにはひふへほばびぶべぼぱぴぷぺぽまみむめもやゆよらりるれろわをゔっゐゑぢぁゃゅぅょぉぇぃ";
        String katakanaAlphabet = "アイウエオカキクケコガギグゲゴサシスセソザジズゼゾタテトダデドチツヅナニヌネノンハヒフヘホバビブベボパピプポペマミムメモヤユヨラリルレロワヲヴーッヰヱァャュゥォョェィ";
        String latinAlphabet = "aāáÀÂÄÆbcÇdeēÈÉÊËfghiíÎÏjklmnñoōóÔŒpqrstuūúÙÛÜvwxyz'".toLowerCase();
        String latinAlphabetCap = latinAlphabet.toUpperCase();
        String numberAlphabet = "0123456789";

        if (!input_value.equals("")) {
            for (int i=0; i<input_value.length();i++) {

                if (text_type == Globals.TEXT_TYPE_KANJI) { break;}

                character = Character.toString(input_value.charAt(i));

                if (hiraganaAlphabet.contains(character)) {
                    text_type = Globals.TEXT_TYPE_HIRAGANA;
                } else if (katakanaAlphabet.contains(character)) {
                    text_type = Globals.TEXT_TYPE_KATAKANA;
                } else if (latinAlphabet.contains(character) || latinAlphabetCap.contains(character)) {
                    text_type = Globals.TEXT_TYPE_LATIN;
                } else if (numberAlphabet.contains(character)) {
                    text_type = Globals.TEXT_TYPE_NUMBER;
                } else {
                    text_type = Globals.TEXT_TYPE_KANJI;
                }
            }
        } else {
            return text_type;
        }

        return text_type;
    }

    @Contract("_ -> new")
    public static Object @NotNull [] prepareInputQueryFields(@NotNull String input) {
        String original = input.toLowerCase(Locale.ENGLISH); //Converting the word to lowercase (the search algorithm is not efficient if needing to search both lower and upper case)
        int originalType = getTextType(original);
        List<String> kanjiChars = extractKanjiChars(original);
        String originalCleaned = UtilitiesGeneral.removeNonSpaceSpecialCharacters(original);
        Object[] results = getInglessVerb(originalCleaned.replace("'",""), originalType);
        String ingless = (String) results[0];
        boolean hasIngEnding = (boolean) results[1];
        String originalNoIng = hasIngEnding? "to " + ingless : original;
        String originalCleanedNoSpaces = originalCleaned.replace("\\s","");
        boolean isVerbWithTo = false;
        boolean isTooShort = false;
        String originalWithoutTo = "";
        int searchType = Globals.TEXT_TYPE_LATIN;
        List<String> searchQueriesNonJapanese = new ArrayList<>();
        List<String> searchQueriesRomaji = new ArrayList<>();
        List<String> searchQueriesKanji = new ArrayList<>();

        if (originalCleaned.length() > 3 && originalCleaned.startsWith("to ")) {
            isVerbWithTo = true;
            originalWithoutTo = originalCleaned.substring(3);
        }

        Object[] conversions = getTransliterationsAsLists(originalCleaned.replaceAll("\\s",""));
        List<String> hiraganaConversions = (List<String>)conversions[Globals.ROM_COL_HIRAGANA];
        List<String> katakanaConversions = (List<String>)conversions[Globals.ROM_COL_KATAKANA];
        List<String> waapuroConversions = (List<String>)conversions[Globals.ROM_COL_WAAPURO];
        List<String> conversionsMH = (List<String>)conversions[Globals.ROM_COL_MOD_HEPBURN];
        List<String> conversionsNS = (List<String>)conversions[Globals.ROM_COL_NIHON_SHIKI];
        List<String> conversionsKS = (List<String>)conversions[Globals.ROM_COL_KUNREI_SHIKI];

        List<String> hiraganaUniqueConversions = UtilitiesGeneral.removeDuplicatesFromStringList(hiraganaConversions);
        List<String> katakanaUniqueConversions = UtilitiesGeneral.removeDuplicatesFromStringList(katakanaConversions);
        List<String> waapuroUniqueConversions = UtilitiesGeneral.removeDuplicatesFromStringList(waapuroConversions);
        List<String> uniqueConversionsMH = UtilitiesGeneral.removeDuplicatesFromStringList(conversionsMH);
        List<String> uniqueConversionsNS = UtilitiesGeneral.removeDuplicatesFromStringList(conversionsNS);
        List<String> uniqueConversionsKS = UtilitiesGeneral.removeDuplicatesFromStringList(conversionsKS);

        String romajiSingleElement = waapuroConversions.get(0);
        String hiraganaSingleElement = hiraganaConversions.get(0);
        String katakanaSingleElement = katakanaConversions.get(0);

        if (originalType == Globals.TEXT_TYPE_LATIN) {
            boolean isEnglishWord = false;
            searchQueriesNonJapanese.add(originalCleaned);
            if (originalCleaned.length() > 3 && originalCleaned.endsWith("ing")) {
                //this.searchQueriesNonJapanese.add(ingless);
                isEnglishWord = true;
            }
            if (originalCleaned.length() > 3 && originalCleaned.startsWith("to ")) {
                searchQueriesNonJapanese.add(originalWithoutTo);
                isEnglishWord = true;
            }
            if (!isEnglishWord) {
                searchQueriesRomaji.add(originalCleaned);
                for (String conversion : waapuroUniqueConversions) {
                    if (!conversion.contains("*") && !conversion.equals(originalCleaned)) searchQueriesRomaji.add(conversion);
                }
            }
            isTooShort = originalCleanedNoSpaces.length() < Globals.SMALL_WORD_LENGTH;
        }
        else if (originalType == Globals.TEXT_TYPE_HIRAGANA || originalType == Globals.TEXT_TYPE_KATAKANA) {
            searchQueriesRomaji.addAll(waapuroUniqueConversions);
            isTooShort = searchQueriesRomaji.get(0).length() < Globals.SMALL_WORD_LENGTH;
        }
        else if (originalType == Globals.TEXT_TYPE_NUMBER) {
            searchQueriesNonJapanese.add(originalCleaned);
            isTooShort = originalCleanedNoSpaces.length() < Globals.SMALL_WORD_LENGTH - 1;
        }
        else if (originalType == Globals.TEXT_TYPE_KANJI) {
            searchType = Globals.TEXT_TYPE_KANJI;
            searchQueriesKanji.add(UtilitiesDb.replaceInvalidKanjisWithValidOnes(originalCleaned));
            isTooShort = false;
        }

        return new Object[] {
            original, //0
            originalCleaned,
            originalNoIng,
            romajiSingleElement,
            hiraganaSingleElement,
            katakanaSingleElement, //5
            ingless,

            originalType,
            searchType,

            hasIngEnding,
            isVerbWithTo, //10
            isTooShort,

            searchQueriesNonJapanese,
            searchQueriesRomaji,
            searchQueriesKanji,
            kanjiChars, //15
            hiraganaConversions,
            katakanaConversions,
            waapuroConversions ,
            conversionsMH,
            conversionsNS, //20
            conversionsKS,
            hiraganaUniqueConversions,
            katakanaUniqueConversions,
            waapuroUniqueConversions,
            uniqueConversionsMH, //25
            uniqueConversionsNS,
            uniqueConversionsKS
        };
    }
}
