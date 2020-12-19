package com.japagram.utilitiesCrossPlatform;

import android.content.Context;

import com.japagram.data.InputQuery;
import com.japagram.data.Word;
import com.japagram.utilitiesPlatformOverridable.OvUtilsDb;
import com.japagram.utilitiesPlatformOverridable.OvUtilsGeneral;
import com.japagram.utilitiesPlatformOverridable.OvUtilsResources;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UtilitiesDictSearch {
    public static List<Word> getMatchingWords(boolean roomExtendedDbIsAvailable,
                                              boolean roomNamesDatabaseIsAvailable,
                                              boolean roomNamesDatabasesFinishedLoading,
                                              InputQuery mQuery,
                                              String language,
                                              @NotNull Context context,
                                              boolean mShowNames) {
        List<Word> localMatchingWordsList;

        Object[] matchingWordIds = UtilitiesDb.getMatchingWordIdsAndDoBasicFiltering(
                roomExtendedDbIsAvailable,
                roomNamesDatabaseIsAvailable,
                roomNamesDatabasesFinishedLoading,
                mQuery,
                language,
                mShowNames,
                context);

        List<Long> matchingWordIdsCentral = (List<Long>) matchingWordIds[0];
        List<Long> matchingWordIdsExtended = (List<Long>) matchingWordIds[1];
        List<Long> matchingWordIdsNames = (List<Long>) matchingWordIds[2];
        OvUtilsGeneral.printLog(Globals.DEBUG_TAG, "LocalSearchAsyncTask - Got matching word ids");

        localMatchingWordsList = OvUtilsDb.getWordListByWordIds(matchingWordIdsCentral, context, Globals.DB_CENTRAL, language);
        OvUtilsGeneral.printLog(Globals.DEBUG_TAG, "LocalSearchAsyncTask - Got matching words");

        if (roomExtendedDbIsAvailable) {
            List<Word> extendedWordsList = OvUtilsDb.getWordListByWordIds(matchingWordIdsExtended, context, Globals.DB_EXTENDED, language);
            localMatchingWordsList.addAll(extendedWordsList);
        }
        OvUtilsGeneral.printLog(Globals.DEBUG_TAG, "LocalSearchAsyncTask - Added matching extended words");

        if (roomNamesDatabaseIsAvailable) {
        List<Word> originalNames = OvUtilsDb.getWordListByWordIds(matchingWordIdsNames, context, Globals.DB_NAMES, language);
            OvUtilsGeneral.printLog(Globals.DEBUG_TAG, "LocalSearchAsyncTask - Added matching names");
            List<Word> condensedNames = new ArrayList<>();
            boolean foundName;
            for (Word name : originalNames) {
                foundName = false;
                for (Word condensedName : condensedNames) {
                    if (name.getRomaji().equals(condensedName.getRomaji())
                            && name.getMeaningsEN().get(0).getType().equals(condensedName.getMeaningsEN().get(0).getType())) {
                        foundName = true;
                        condensedName.setKanji(OvUtilsGeneral.concat(new String[]{condensedName.getKanji(), "・", name.getKanji()}));
                        break;
                    }
                }
                if (!foundName) {
                    condensedNames.add(name);
                }
            }
            localMatchingWordsList.addAll(condensedNames);
        }
        return localMatchingWordsList;
    }

    public static @NotNull List<String> prepareSource(String inputQuery,
                                                      int inputQueryTextType,
                                                      String inputQueryFirstLetter,
                                                      String inputQueryLatin,
                                                      String inputQueryNoSpaces,
                                                      @NotNull String romajiAndKanji,
                                                      @NotNull Word word,
                                                      @NotNull List<Word.Meaning> meanings,
                                                      boolean typeIsVerb,
                                                      @NotNull String language,
                                                      Context context) {

        String romajiAndKanjiNoSpaces = romajiAndKanji.replace(" ","");
        String romaji = word.getRomaji();
        String kanji = word.getKanji();
        String altSpellings = word.getAltSpellings();
        String matchingConj = word.getMatchingConj() == null? "" : word.getMatchingConj();
        String keywords = word.getExtraKeywordsEN();
        switch (language) {
            case Globals.LANG_STR_EN:
                keywords = word.getExtraKeywordsEN();
                break;
            case Globals.LANG_STR_FR:
                keywords = word.getExtraKeywordsFR();
                break;
            case Globals.LANG_STR_ES:
                keywords = word.getExtraKeywordsES();
                break;
        }
        List<String> meaningStrings = new ArrayList<>();
        for (int j = 0; j< meanings.size(); j++) {
            meaningStrings.add(meanings.get(j).getMeaning());
        }
        String combinedMeanings = OvUtilsGeneral.joinList(", ", meaningStrings);

        List<String> sourceInfo = new ArrayList<>();
        String from;
        String text;
        if (!romajiAndKanji.contains(inputQuery)
                && !romajiAndKanji.contains(inputQueryNoSpaces)
                && !romajiAndKanjiNoSpaces.contains(inputQuery)
                && !romajiAndKanjiNoSpaces.contains(inputQueryNoSpaces)
                && !romajiAndKanjiNoSpaces.contains(inputQueryLatin)) {

            String latin = UtilitiesQuery.getWaapuroHiraganaKatakana(romaji).get(Globals.TEXT_TYPE_LATIN);
            String hiragana = UtilitiesQuery.getWaapuroHiraganaKatakana(romaji).get(Globals.TEXT_TYPE_HIRAGANA);
            String katakana = UtilitiesQuery.getWaapuroHiraganaKatakana(romaji).get(Globals.TEXT_TYPE_KATAKANA);

            if (!OvUtilsGeneral.isEmptyString(altSpellings) && altSpellings.contains(inputQuery)) {
                String[] altSpellingElements = altSpellings.split(",");
                boolean isExactMatch = false;
                for (String element : altSpellingElements) {
                    if (inputQuery.equals(element.trim())) {
                        isExactMatch = true;
                        break;
                    }
                }
                from = OvUtilsResources.getString(isExactMatch ? "from_alt_form" : "from_alt_form_containing", context, Globals.RESOURCE_MAP_GENERAL, language);
                text = OvUtilsGeneral.concat(new String[]{from, " \"", inputQuery, "\"."});
                sourceInfo.add(text);
            }
            else if (combinedMeanings.contains(inputQuery) || combinedMeanings.contains(latin)) {
                //Ignore words where the input query is included in the meaning
            }
            else if (keywords != null && (keywords.contains(inputQuery) || keywords.contains(inputQueryLatin))) {
                String[] keywordList = keywords.split(",");
                for (String element : keywordList) {
                    String keyword = element.trim();
                    if (!romaji.contains(keyword) && !kanji.contains(keyword) && !altSpellings.contains(keyword)
                            && !combinedMeanings.contains(keyword)) {
                        from = OvUtilsResources.getString("from_associated_word", context, Globals.RESOURCE_MAP_GENERAL, language);
                        text = OvUtilsGeneral.concat(new String[]{from, " \"", keyword, "\"."});
                        sourceInfo.add(text);
                        break;
                    }
                }
            }
            else if (!OvUtilsGeneral.isEmptyString(matchingConj)
                    && word.getVerbConjMatchStatus() == Word.CONJ_MATCH_EXACT
                    || word.getVerbConjMatchStatus() == Word.CONJ_MATCH_CONTAINED
                    && matchingConj.contains(inputQuery)
                    || matchingConj.contains(inputQueryNoSpaces)
                    || matchingConj.contains(inputQueryLatin)) {
                from = OvUtilsResources.getString(typeIsVerb? "from_conjugated_form" : "from_associated_word", context, Globals.RESOURCE_MAP_GENERAL, language);
                text = OvUtilsGeneral.concat(new String[]{from, " \"", matchingConj, "\"."});
                sourceInfo.add(text);
            }
            else if ((inputQueryTextType == Globals.TEXT_TYPE_KANJI
                    && kanji.length() > 0 && !kanji.substring(0, 1).equals(inputQueryFirstLetter))
                    || (inputQueryTextType == Globals.TEXT_TYPE_LATIN
                    && romaji.length() > 0 && !romaji.substring(0, 1).equals(inputQueryFirstLetter))
                    || (inputQueryTextType == Globals.TEXT_TYPE_HIRAGANA
                    && hiragana.length() > 0 && !hiragana.substring(0, 1).equals(inputQueryFirstLetter))
                    || (inputQueryTextType == Globals.TEXT_TYPE_KATAKANA
                    && katakana.length() > 0 && !katakana.substring(0, 1).equals(inputQueryFirstLetter))
            ) {
                from = OvUtilsResources.getString("derived_from", context, Globals.RESOURCE_MAP_GENERAL, language);
                text = OvUtilsGeneral.concat(new String[]{from, " \"", inputQuery, "\"."});
                sourceInfo.add(text);
            }
        }
        return sourceInfo;
    }

    public static String getRomajiAndKanji(boolean @NotNull [] types, @NotNull Word word, Context context, String language) {
        String romaji = word.getRomaji();
        String kanji = word.getKanji();
        String parentRomaji;
        String placeholder;
        if (types[Globals.WORD_TYPE_VERB_CONJ] && romaji.length()>3 && romaji.startsWith("(o)")) {
            placeholder = OvUtilsResources.getString("verb", context, Globals.RESOURCE_MAP_GENERAL, language);
            parentRomaji = OvUtilsGeneral.concat(new String[]{"(o)[", placeholder, "] + ", romaji.substring(3)});
        }
        else if (types[Globals.WORD_TYPE_VERB_CONJ] && romaji.length()>3 && !romaji.startsWith("(o)")) {
            placeholder = OvUtilsResources.getString("verb", context, Globals.RESOURCE_MAP_GENERAL, language);
            parentRomaji = OvUtilsGeneral.concat(new String[]{"[", placeholder, "] + ", romaji});
        }
        else if (types[Globals.WORD_TYPE_I_ADJ_CONJ]) {
            placeholder = OvUtilsResources.getString("i_adj", context, Globals.RESOURCE_MAP_GENERAL, language);
            parentRomaji = OvUtilsGeneral.concat(new String[]{"[", placeholder, "] + ", romaji});
        }
        else if (types[Globals.WORD_TYPE_NA_ADJ_CONJ]) {
            placeholder = OvUtilsResources.getString("na_adj", context, Globals.RESOURCE_MAP_GENERAL, language);
            parentRomaji = OvUtilsGeneral.concat(new String[]{"[", placeholder, "] + ", romaji});
        }
        else if (types[Globals.WORD_TYPE_ADVERB] && !types[Globals.WORD_TYPE_NOUN] && romaji.length()>2
                && romaji.endsWith("ni")
                && !romaji.endsWith(" ni")) {
            parentRomaji = OvUtilsGeneral.concat(new String[]{romaji.substring(0,romaji.length()-2), " ni"});
        }
        else parentRomaji = romaji;

        String romajiAndKanji;
        if (romaji.equals("")) romajiAndKanji = kanji;
        else if (kanji.equals("")) romajiAndKanji = romaji;
        else romajiAndKanji = OvUtilsGeneral.concat(new String[]{parentRomaji.toUpperCase(), " (", kanji, ")"});
        return romajiAndKanji;
    }

    public static boolean @NotNull [] getTypesFromWordMeanings(@NotNull List<Word.Meaning> meanings) {
        String type = "";
        boolean wordHasPhraseConstruction = false;
        boolean[] types = new boolean[]{false, false, false, false, false, false};
        for (int j = 0; j< meanings.size(); j++) {
            if (j==0) {
                type = meanings.get(j).getType();
                types[Globals.WORD_TYPE_VERB_CONJ] = type.equals("VC");
                types[Globals.WORD_TYPE_I_ADJ_CONJ] = type.equals("iAC");
                types[Globals.WORD_TYPE_NA_ADJ_CONJ] = type.equals("naAC");
                String[] typeElements = type.split(";");
                types[Globals.WORD_TYPE_VERB] = type.contains("V") && !type.equals("VC") && !Arrays.asList(typeElements).contains("V");
                types[Globals.WORD_TYPE_ADVERB] = type.contains("A");
                types[Globals.WORD_TYPE_NOUN] = type.contains("N");
            }
            if (!wordHasPhraseConstruction) wordHasPhraseConstruction = type.equals("PC");
        }
        return types;
    }

    public static String getFinalWordMeaningsExtract(boolean onlyEnglishMeaningsAvailable, List<Word.Meaning> meanings, Context mContext, String mLanguage, String mLanguageFromResource) {

        String extract = "";
        if (onlyEnglishMeaningsAvailable) {
            extract = OvUtilsGeneral.concat(new String[]{
                    OvUtilsResources.getString("meanings_in", mContext, Globals.RESOURCE_MAP_GENERAL, mLanguage),
                    " ",
                    mLanguageFromResource.toLowerCase(),
                    " ",
                    OvUtilsResources.getString("unavailable_select_word_to_see_meanings", mContext, Globals.RESOURCE_MAP_GENERAL, mLanguage)
            });
        }
        else if (meanings.get(0).getMeaning().equals("*")) {
            String type = meanings.get(0).getType();
            if (Globals.PARTS_OF_SPEECH.containsKey(type)) {
                //String  currentType = Utilities.capitalizeFirstLetter(mContext.getString(GlobalConstants.TYPES.get(element)));
                extract = OvUtilsResources.getString(Globals.PARTS_OF_SPEECH.get(type), mContext, Globals.RESOURCE_MAP_TYPES, mLanguage);
            }
            else {
                extract = "*";
            }
        }
        else {
            extract = UtilitiesGeneral.removeDuplicatesFromCommaList(UtilitiesDb.getMeaningsExtract(meanings, Globals.BALANCE_POINT_REGULAR_DISPLAY));
        }
        return extract;
    }

    @Contract("_, _ -> new")
    public static Object @NotNull [] getDisplayableMeanings(Word word, @NotNull String language) {
        boolean onlyEnglishMeaningsAvailable = false;
        List<Word.Meaning> meanings = new ArrayList<>();
        switch (language) {
            case Globals.LANG_STR_EN:
                meanings = word.getMeaningsEN();
                break;
            case Globals.LANG_STR_FR:
                meanings = word.getMeaningsFR();
                break;
            case Globals.LANG_STR_ES:
                meanings = word.getMeaningsES();
                break;
        }
        if (meanings == null || meanings.size() == 0) {
            //If the French/Spanish meaning is empty, use the english meanings as reference
            meanings = word.getMeaningsEN();
            onlyEnglishMeaningsAvailable = true;
        }
        return new Object[]{meanings, onlyEnglishMeaningsAvailable};
    }
}
