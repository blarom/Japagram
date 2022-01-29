package com.japagram.utilitiesCrossPlatform;

import android.content.Context;

import androidx.annotation.NonNull;

import com.japagram.data.ConjugationTitle;
import com.japagram.data.GenericIndex;
import com.japagram.data.IndexEnglish;
import com.japagram.data.IndexFrench;
import com.japagram.data.IndexKanji;
import com.japagram.data.IndexRomaji;
import com.japagram.data.IndexSpanish;
import com.japagram.data.InputQuery;
import com.japagram.data.Word;
import com.japagram.utilitiesPlatformOverridable.OvUtilsGeneral;
import com.japagram.utilitiesPlatformOverridable.OvUtilsDb;
import com.japagram.utilitiesPlatformOverridable.OvUtilsResources;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class UtilitiesDb {
    @Contract("null, _ -> !null")
    public static String getMeaningsExtract(List<Word.Meaning> meanings, int balancePoint) {
        if (meanings == null) return "";
        List<String> totalMeaningElements = new ArrayList<>();

        if (meanings.size() == 1 || balancePoint < 2) return meanings.get(0).getMeaning();
        else if (meanings.size() >= 2 && meanings.size() <= balancePoint) {
            for (Word.Meaning meaning : meanings) {
                totalMeaningElements = addMeaningElementsToListUpToMaxNumber(
                        totalMeaningElements, meaning.getMeaning(), balancePoint + 1 - meanings.size());
            }
            return OvUtilsGeneral.joinList(", ", totalMeaningElements);
        } else if (meanings.size() > balancePoint || balancePoint > 6) {
            for (Word.Meaning meaning : meanings) {
                totalMeaningElements = addMeaningElementsToListUpToMaxNumber(
                        totalMeaningElements, meaning.getMeaning(), 1);
            }
            return OvUtilsGeneral.joinList(", ", totalMeaningElements);
        } else return "";
    }

    @NotNull
    @Contract("_, _, _ -> param1")
    private static List<String> addMeaningElementsToListUpToMaxNumber(List<String> totalList, String meaning, int maxNumber) {
        String[] meaningElements = OvUtilsGeneral.splitAtCommasOutsideParentheses(meaning);
        if (meaningElements.length <= maxNumber) totalList.addAll(Arrays.asList(meaningElements));
        else totalList.addAll(Arrays.asList(meaningElements).subList(0, maxNumber));
        return totalList;
    }

    public static boolean wordsAreEquivalent(@NotNull Word wordA, @NotNull Word wordB) {
        return wordA.getRomaji().trim().equals(wordB.getRomaji().trim()) && wordA.getKanji().trim().equals(wordB.getKanji().trim());
    }

    @Contract("null, _, _ -> !null")
    public static String getVerbRoot(String verb, String family, int type) {
        String root;
        if (verb == null || verb.length() == 0 || family == null || family.length() == 0) {
            return "";
        }

        if (type == Globals.TEXT_TYPE_LATIN) {
            switch (family) {
                case Globals.VERB_FAMILY_BU_GODAN:
                case Globals.VERB_FAMILY_GU_GODAN:
                case Globals.VERB_FAMILY_KU_GODAN:
                case Globals.VERB_FAMILY_IKU_SPECIAL:
                case Globals.VERB_FAMILY_YUKU_SPECIAL:
                case Globals.VERB_FAMILY_MU_GODAN:
                case Globals.VERB_FAMILY_NU_GODAN:
                case Globals.VERB_FAMILY_RU_GODAN:
                case Globals.VERB_FAMILY_ARU_SPECIAL:
                case Globals.VERB_FAMILY_SU_GODAN:
                case Globals.VERB_FAMILY_RU_ICHIDAN:
                    root = verb.substring(0, verb.length() - 2);
                    break;
                case Globals.VERB_FAMILY_TSU_GODAN:
                    root = verb.substring(0, verb.length() - 3);
                    break;
                case Globals.VERB_FAMILY_U_GODAN:
                case Globals.VERB_FAMILY_U_SPECIAL:
                    root = verb.substring(0, verb.length() - 1);
                    break;
                case Globals.VERB_FAMILY_SURU:
                case Globals.VERB_FAMILY_KURU:
                    root = verb.substring(0, verb.length() - 4);
                    break;
                default:
                    root = verb;
                    break;
            }
        } else {
            switch (family) {
                case Globals.VERB_FAMILY_SURU:
                case Globals.VERB_FAMILY_KURU:
                    root = verb.substring(0, verb.length() - 2);
                    break;
                case Globals.VERB_FAMILY_BU_GODAN:
                case Globals.VERB_FAMILY_GU_GODAN:
                case Globals.VERB_FAMILY_KU_GODAN:
                case Globals.VERB_FAMILY_IKU_SPECIAL:
                case Globals.VERB_FAMILY_YUKU_SPECIAL:
                case Globals.VERB_FAMILY_MU_GODAN:
                case Globals.VERB_FAMILY_NU_GODAN:
                case Globals.VERB_FAMILY_RU_GODAN:
                case Globals.VERB_FAMILY_ARU_SPECIAL:
                case Globals.VERB_FAMILY_SU_GODAN:
                case Globals.VERB_FAMILY_RU_ICHIDAN:
                case Globals.VERB_FAMILY_TSU_GODAN:
                case Globals.VERB_FAMILY_U_GODAN:
                case Globals.VERB_FAMILY_U_SPECIAL:
                    root = verb.substring(0, verb.length() - 1);
                    break;
                default:
                    root = verb;
                    break;
            }
        }

        return root;
    }

    @NotNull
    public static List<Long> getNormalMatches(@NotNull InputQuery query, String language, int db, Context context) {

        //region Initializations
        List<Long> matchingWordIds = new ArrayList<>();
        String keywords;
        boolean foundMatch;
        List<String> keywordsList;
        List<String> romajiQueries = query.getSearchQueriesRomaji();
        List<String> nonJapQueries = query.getSearchQueriesNonJapanese();
        List<String> latinQueries = UtilitiesGeneral.combineLists(romajiQueries, nonJapQueries);
        List<String> kanjiQueries = query.getSearchQueriesKanji();
        List<String> combinedQueries = UtilitiesGeneral.combineLists(latinQueries, kanjiQueries);
        boolean queryIsTooShort = query.isTooShort();
        //endregion

        //region Getting the words
        List<Long> matchingWordIdsFromIndex = getMatchingWordIdsWithLimits(query, language, db, context);
        List<Word> matchingWordList = OvUtilsDb.getWordListByWordIds(matchingWordIdsFromIndex, context, db, language);
        //endregion

        //region Filtering the matches
        for (Word word : matchingWordList) {
            foundMatch = stringContainsItemFromList(word.getRomaji(), romajiQueries, true) ||
                    stringContainsItemFromList(word.getKanji(), kanjiQueries, false) ||
                    ( meaningsContainExactQueryMatch(nonJapQueries, word, language)) ||
                    listContainsItemFromList(Arrays.asList(word.getAltSpellings().split(Globals.DB_ELEMENTS_DELIMITER)), combinedQueries);

            if (!foundMatch && !queryIsTooShort) {
                keywordsList = new ArrayList<>();
                keywordsList.add(word.getExtraKeywordsJAP());
                switch (language) {
                    case Globals.LANG_STR_EN:
                        keywordsList.add(word.getExtraKeywordsEN());
                        break;
                    case Globals.LANG_STR_FR:
                        keywordsList.add(word.getExtraKeywordsFR());
                        break;
                    case Globals.LANG_STR_ES:
                        keywordsList.add(word.getExtraKeywordsES());
                        break;
                }
                keywordsList.add(getCombinedMeanings(word, language));
                keywords = OvUtilsGeneral.joinList(", ", keywordsList).toLowerCase();

                for (String latinQuery : latinQueries) {
                    if (keywords.contains(latinQuery)) {
                        foundMatch = true;
                        break;
                    }
                }
            }

            if (foundMatch) {
                matchingWordIds.add(word.getId());
            }
        }
        //endregion

        return matchingWordIds;
    }

    @NotNull
    public static List<Long> addNamesToMatchesList(@NotNull InputQuery query, Context context) {

        List<Long> matchingWordIdsNames = findQueryInNameIndices(query.getOriginalCleaned(), false, query.getOriginalType(), context);

        //If the number of matching ids is larger than MAX_SQL_VARIABLES_FOR_QUERY, perform an exact search
        if (matchingWordIdsNames.size() > Globals.MAX_SQL_VARIABLES_FOR_QUERY) {
            matchingWordIdsNames = findQueryInNameIndices(query.getOriginalCleaned(), true, query.getOriginalType(), context);
        }

        //If the number of matching ids is still larger than MAX_SQL_VARIABLES_FOR_QUERY, limit the list length to MAX_SQL_VARIABLES_FOR_QUERY
        if (matchingWordIdsNames.size() > Globals.MAX_SQL_VARIABLES_FOR_QUERY) {
            matchingWordIdsNames = matchingWordIdsNames.subList(0, Globals.MAX_SQL_VARIABLES_FOR_QUERY);
        }

        return matchingWordIdsNames;
    }

    private static boolean meaningsContainExactQueryMatch(List<String> searchWords, Word word, @NotNull String language) {

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

        List<String> meaningsPrepared = new ArrayList<>();
        for (Word.Meaning meaning : meanings) {
            meaningsPrepared.add(meaning.getMeaning().replaceAll("(, |\\(|\\))", " "));
        }
        String meaningsString = OvUtilsGeneral.joinList(" ", meaningsPrepared);
        List<String> meaningSet = Arrays.asList(meaningsString.split(" "));
        boolean hasIntersection = UtilitiesGeneral.getIntersectionOfLists(meaningSet, searchWords).size() > 0;
        return hasIntersection;
    }

    private static String getCombinedMeanings(Word word, @NotNull String language) {
        List<String> meanings = new ArrayList<>();
        switch (language) {
            case Globals.LANG_STR_EN:
                for (Word.Meaning meaning : word.getMeaningsEN()) {
                    meanings.add(meaning.getMeaning());
                }
                break;
            case Globals.LANG_STR_FR:
                for (Word.Meaning meaning : word.getMeaningsFR()) {
                    meanings.add(meaning.getMeaning());
                }
                for (Word.Meaning meaning : word.getMeaningsEN()) {
                    meanings.add(meaning.getMeaning());
                }
                break;
            case Globals.LANG_STR_ES:
                for (Word.Meaning meaning : word.getMeaningsES()) {
                    meanings.add(meaning.getMeaning());
                }
                for (Word.Meaning meaning : word.getMeaningsEN()) {
                    meanings.add(meaning.getMeaning());
                }
                break;
        }
        return OvUtilsGeneral.joinList(", ", meanings);
    }

    @NotNull
    public static List<Long> getAdjectivesFromConjugation(InputQuery query, Context context, String language) {

        List<Long> matchingWordIds = new ArrayList<>();
        //Adding relevant adjectives to the list of matches if the input query is an adjective conjugation
        List<Long> matchingWordIdsFromIndex = getMatchingAdjectiveIdsWithLimits(query, context);
        if (matchingWordIdsFromIndex.size() == 0) return new ArrayList<>();

        List<Word> matchingPotentialAdjectives = OvUtilsDb.getWordListByWordIds(matchingWordIdsFromIndex, context, Globals.DB_CENTRAL, language);
        List<String> typesList;
        for (Word word : matchingPotentialAdjectives) {
            typesList = new ArrayList<>();
            for (Word.Meaning meaning : word.getMeaningsEN()) {
                typesList.add(meaning.getType());
            }
            typesList = Arrays.asList(OvUtilsGeneral.joinList(Globals.DB_ELEMENTS_DELIMITER, typesList).split(Globals.DB_ELEMENTS_DELIMITER));
            if (typesList.contains("Ai") || typesList.contains("Ana")) {
                if (!matchingWordIds.contains(word.getId())) matchingWordIds.add(word.getId());
            }
        }

        return matchingWordIds;
    }

    @NotNull
    public static List<Long> addCountersToMatchesList(@NotNull InputQuery query, Context context, String language) {
        if (query.getSearchType() != Globals.TEXT_TYPE_KANJI) return new ArrayList<>();
        List<String> potentialCounters = new ArrayList<>();
        String firstChar;
        for (String word : query.getSearchQueriesKanji()) {
            firstChar = word.substring(0,1);
            if ("何一二三四五六七八九十".contains(firstChar)) potentialCounters.add(word.substring(1));
        }

        List<Long> matchingWordIds;

        List<IndexKanji> kanjiIndicesForCounter = findQueryInKanjiIndex(potentialCounters, true, Globals.DB_CENTRAL, context);
        matchingWordIds = getWordIdsFromSearchResults(kanjiIndicesForCounter);
        if (matchingWordIds.size() == 0) return new ArrayList<>();

        if (matchingWordIds.size() > Globals.MAX_SQL_VARIABLES_FOR_QUERY) {
            OvUtilsGeneral.printLog(Globals.DEBUG_TAG, "WARNING: exceeded MAX_SQL_VARIABLES_FOR_QUERY in counter search, but prevented crash.");
        } else {
            List<Word> matchingPotentialCounters = OvUtilsDb.getWordListByWordIds(matchingWordIds, context, Globals.DB_CENTRAL, language);
            for (Word word : matchingPotentialCounters) {
                List<String> typesList = new ArrayList<>();
                for (Word.Meaning meaning : word.getMeaningsEN()) {
                    typesList.add(meaning.getType());
                }
                typesList = Arrays.asList(OvUtilsGeneral.joinList(Globals.DB_ELEMENTS_DELIMITER, typesList).split(Globals.DB_ELEMENTS_DELIMITER));
                if (typesList.contains("C")) {
                    if (!OvUtilsGeneral.listContains(matchingWordIds, word.getId())) matchingWordIds.add(word.getId());
                }
            }
        }

        return matchingWordIds;
    }

    @NotNull
    private static List<Long> getMatchingAdjectiveIdsWithLimits(@NotNull InputQuery query, Context context) {

        List<Long> matchingWordIdsFromIndex;
        String adjectiveConjugation;
        String baseAdjective = "";
        List<String> baseAdjectives = new ArrayList<>();
        int length;
        int searchType = query.getSearchType();
        boolean isPotentialAdjective = false;

        if (searchType == Globals.TEXT_TYPE_LATIN) {

            //regon Getting the base adjectives
            for (String word : query.getSearchQueriesRomaji()) {
                length = word.length();
                if (length > 9) {
                    adjectiveConjugation = word.substring(length - 9);
                    baseAdjective = OvUtilsGeneral.concat(new String[]{word.substring(0, length - 9), "i"});
                    if (adjectiveConjugation.equals("kunakatta")) isPotentialAdjective = true;
                }
                if (!isPotentialAdjective && length > 6) {
                    adjectiveConjugation = word.substring(length - 6);
                    baseAdjective = OvUtilsGeneral.concat(new String[]{word.substring(0, length - 6), "i"});
                    if (adjectiveConjugation.equals("kereba")) isPotentialAdjective = true;
                }
                if (!isPotentialAdjective && length > 5) {
                    adjectiveConjugation = word.substring(length - 5);
                    baseAdjective = OvUtilsGeneral.concat(new String[]{word.substring(0, length - 5), "i"});
                    if (adjectiveConjugation.equals("kunai")
                            || adjectiveConjugation.equals("katta")
                            || adjectiveConjugation.equals("karou")) isPotentialAdjective = true;
                }
                if (!isPotentialAdjective && length > 4) {
                    adjectiveConjugation = word.substring(length - 4);
                    baseAdjective = OvUtilsGeneral.concat(new String[]{word.substring(0, length - 4), "i"});
                    if (adjectiveConjugation.equals("kute")) isPotentialAdjective = true;
                }
                if (!isPotentialAdjective && length > 2) {
                    adjectiveConjugation = word.substring(length - 2);
                    if (adjectiveConjugation.equals("mi") || adjectiveConjugation.equals("ku")) {
                        isPotentialAdjective = true;
                        baseAdjective = OvUtilsGeneral.concat(new String[]{word.substring(0, length - 2), "i"});
                    } else if (adjectiveConjugation.equals("ni") || adjectiveConjugation.equals("na") || adjectiveConjugation.equals("de")) {
                        isPotentialAdjective = true;
                        baseAdjective = word.substring(0, length - 2);
                    }
                }

                if (isPotentialAdjective) baseAdjectives.add(baseAdjective);
            }
            //endregion

            //region Getting the matching word indexes
            boolean exactSearch = false;
            for (String word : baseAdjectives) {
                if (word.length() < Globals.SMALL_WORD_LENGTH) {
                    exactSearch = true;
                    break;
                }
            }

            List<Object> latinIndicesForAdjective = findQueryInRomajiIndex(baseAdjectives, exactSearch, Globals.DB_CENTRAL, context);
            matchingWordIdsFromIndex = getWordIdsFromSearchResults(latinIndicesForAdjective);
            if (!exactSearch && matchingWordIdsFromIndex.size() > Globals.MAX_SQL_VARIABLES_FOR_QUERY) {
                latinIndicesForAdjective = findQueryInRomajiIndex(baseAdjectives, true, Globals.DB_CENTRAL, context);
            }
            matchingWordIdsFromIndex = getWordIdsFromSearchResults(latinIndicesForAdjective);
            if (matchingWordIdsFromIndex.size() > Globals.MAX_SQL_VARIABLES_FOR_QUERY) {
                OvUtilsGeneral.printLog(Globals.DEBUG_TAG, "WARNING: exceeded MAX_SQL_VARIABLES_FOR_QUERY in adjectives search, but prevented crash.");
                return new ArrayList<>();
            }
            //endregion

        } else if (searchType == Globals.TEXT_TYPE_KANJI) {

            for (String word : query.getSearchQueriesKanji()) {
                length = word.length();
                if (length > 5) {
                    adjectiveConjugation = word.substring(length - 5);
                    baseAdjective = OvUtilsGeneral.concat(new String[]{word.substring(0, length - 5), "い"});
                    if (adjectiveConjugation.equals("くなかった")) isPotentialAdjective = true;
                }
                if (!isPotentialAdjective && length > 3) {
                    adjectiveConjugation = word.substring(word.length() - 3);
                    baseAdjective = OvUtilsGeneral.concat(new String[]{word.substring(0, length - 3), "い"});
                    if (adjectiveConjugation.equals("くない")
                            || adjectiveConjugation.equals("ければ")
                            || adjectiveConjugation.equals("かった")
                            || adjectiveConjugation.equals("かろう")) isPotentialAdjective = true;
                }
                if (!isPotentialAdjective && length > 2) {
                    adjectiveConjugation = word.substring(length - 2);
                    baseAdjective = OvUtilsGeneral.concat(new String[]{word.substring(0, length - 2), "い"});
                    if (adjectiveConjugation.equals("くて")) isPotentialAdjective = true;
                }
                if (!isPotentialAdjective && word.length() > 1) {
                    adjectiveConjugation = word.substring(length - 1);
                    if (adjectiveConjugation.equals("み") || adjectiveConjugation.equals("く")) {
                        isPotentialAdjective = true;
                        baseAdjective = OvUtilsGeneral.concat(new String[]{word.substring(0, length - 1), "い"});
                    } else if (adjectiveConjugation.equals("に") || adjectiveConjugation.equals("な") || adjectiveConjugation.equals("で")) {
                        isPotentialAdjective = true;
                        baseAdjective = word.substring(0, length - 1);
                    }
                }

                if (isPotentialAdjective) baseAdjectives.add(baseAdjective);
            }

            List<IndexKanji> kanjiIndicesForAdjective = findQueryInKanjiIndex(baseAdjectives, false, Globals.DB_CENTRAL, context);
            matchingWordIdsFromIndex = getWordIdsFromSearchResults(kanjiIndicesForAdjective);
            if (matchingWordIdsFromIndex.size() > Globals.MAX_SQL_VARIABLES_FOR_QUERY) {
                kanjiIndicesForAdjective = findQueryInKanjiIndex(baseAdjectives, true, Globals.DB_CENTRAL, context);
                matchingWordIdsFromIndex = getWordIdsFromSearchResults(kanjiIndicesForAdjective);
            }
            if (matchingWordIdsFromIndex.size() > Globals.MAX_SQL_VARIABLES_FOR_QUERY) {
                OvUtilsGeneral.printLog(Globals.DEBUG_TAG, "WARNING: exceeded MAX_SQL_VARIABLES_FOR_QUERY in adjectives search, but prevented crash.");
                return new ArrayList<>();
            }

        } else {
            return new ArrayList<>();
        }

        return matchingWordIdsFromIndex;
    }

    @NotNull @SuppressWarnings("unchecked")
    public static List<Long> getWordIdsFromSearchResults(Object indexes) {
        List<String> searchResultIndexesArray = new ArrayList<>();
        List<GenericIndex> genericIndexes = (List<GenericIndex>) indexes;
        List<Long> matchingWordIdsFromIndex = new ArrayList<>();
        List<String> indexList;
        for (GenericIndex index : genericIndexes) {
            searchResultIndexesArray.add(index.getWordIds());
        }

        for (String searchResultIndexes : searchResultIndexesArray) {
            indexList = Arrays.asList(searchResultIndexes.split(Globals.DB_ELEMENTS_DELIMITER));
            for (int j = 0; j < indexList.size(); j++) {
                long value = Long.parseLong(indexList.get(j));
                matchingWordIdsFromIndex.add(value);
            }
        }
        return matchingWordIdsFromIndex;
    }

    @NotNull
    private static List<Long> findQueryInNameIndices(String concatenated_word, boolean exactSearch, int inputTextType, Context context) {

        List<String> matchingIndices = new ArrayList<>();
        if (exactSearch) {
            //Preventing the index search from returning too many results and crashing the app

            if (inputTextType == Globals.TEXT_TYPE_KANJI) {
                IndexKanji indexKanji = OvUtilsDb.getKanjiIndexForExactWord(concatenated_word, context);
                if (indexKanji != null) {
                    matchingIndices.addAll(Arrays.asList(indexKanji.getWordIds().split(Globals.DB_ELEMENTS_DELIMITER)));
                }
            } else {
                if (inputTextType == Globals.TEXT_TYPE_HIRAGANA || inputTextType == Globals.TEXT_TYPE_KATAKANA) {
                    concatenated_word = UtilitiesQuery.getWaapuroHiraganaKatakana(concatenated_word).get(Globals.TEXT_TYPE_LATIN);
                }
                IndexRomaji indexRomaji = OvUtilsDb.getRomajiIndexForExactWord(concatenated_word, context);
                if (indexRomaji != null) {
                    matchingIndices.addAll(Arrays.asList(indexRomaji.getWordIds().split(Globals.DB_ELEMENTS_DELIMITER)));
                }
            }

        } else {
            if (inputTextType == Globals.TEXT_TYPE_KANJI) {
                List<IndexKanji> indexesKanji = OvUtilsDb.getKanjiIndexesListForStartingWord(concatenated_word, context);
                if (indexesKanji != null && indexesKanji.size() > 0) {
                    for (IndexKanji indexKanji : indexesKanji) {
                        matchingIndices.addAll(Arrays.asList(indexKanji.getWordIds().split(Globals.DB_ELEMENTS_DELIMITER)));
                    }
                }
            } else {
                if (inputTextType == Globals.TEXT_TYPE_HIRAGANA || inputTextType == Globals.TEXT_TYPE_KATAKANA) {
                    concatenated_word = UtilitiesQuery.getWaapuroHiraganaKatakana(concatenated_word).get(Globals.TEXT_TYPE_LATIN);
                }
                List<IndexRomaji> indexesRomaji = OvUtilsDb.getRomajiIndexesListForStartingWord(concatenated_word, context);
                if (indexesRomaji != null && indexesRomaji.size() > 0) {
                    for (IndexRomaji indexRomaji : indexesRomaji) {
                        matchingIndices.addAll(Arrays.asList(indexRomaji.getWordIds().split(Globals.DB_ELEMENTS_DELIMITER)));
                    }
                }
            }

        }
        matchingIndices = UtilitiesGeneral.removeDuplicatesFromStringList(matchingIndices);
        List<Long> finalIndexList = new ArrayList<>();
        for (String element : matchingIndices) {
            finalIndexList.add(Long.parseLong(element));
        }
        return finalIndexList;
    }

    @NotNull
    public static List<Object> findQueryInRomajiIndex(List<String> searchQueries, boolean exactSearch, int db, Context context) {

        //Exact search Prevents the index search from returning too many results and crashing the app
        List<Object> matchingIndices = new ArrayList<>();
        if (exactSearch) {
            List<IndexRomaji> indexes = OvUtilsDb.getRomajiIndexForExactWordsList(searchQueries, context, db);
            if (indexes != null && indexes.size() > 0) matchingIndices.addAll(indexes);

        } else {
            List<IndexRomaji> indexesRomaji = OvUtilsDb.getRomajiIndexesListForStartingWordsList(searchQueries, context, db);
            if (indexesRomaji != null && indexesRomaji.size() > 0) matchingIndices.addAll(indexesRomaji);
        }
        return matchingIndices;
    }

    public static List<IndexKanji> findQueryInKanjiIndex(List<String> searchQueries, boolean exactSearch, int db, Context context) {
        List<IndexKanji> matchingIndexKanjis;
        if (exactSearch) {
            //Preventing the index search from returning too many results and crashing the app
            matchingIndexKanjis = new ArrayList<>();
            List<IndexKanji> indexes = OvUtilsDb.getKanjiIndexForExactWordsList(searchQueries, context, db);
            if (indexes != null && indexes.size() > 0) matchingIndexKanjis.addAll(indexes); //Only add the index if the word was found in the index
        } else {
            matchingIndexKanjis = OvUtilsDb.getKanjiIndexesListForStartingWordsList(searchQueries, context, db);
        }
        return matchingIndexKanjis;
    }

    @NotNull
    private static List<Long> getMatchingWordIdsWithLimits(InputQuery query, String language, int db, Context context) {

        List<Long> matchingWordIds = getMatchingWordIds(false, query, language, db, context);

        //If the number of matching ids is larger than MAX_SQL_VARIABLES_FOR_QUERY, perform an exact search
        if (matchingWordIds.size() > Globals.MAX_SQL_VARIABLES_FOR_QUERY) {
            matchingWordIds = getMatchingWordIds(true, query, language, db, context);
        }

        //If the number of matching ids is still larger than MAX_SQL_VARIABLES_FOR_QUERY, limit the list length to MAX_SQL_VARIABLES_FOR_QUERY
        if (matchingWordIds.size() > Globals.MAX_SQL_VARIABLES_FOR_QUERY) {
            matchingWordIds = matchingWordIds.subList(0, Globals.MAX_SQL_VARIABLES_FOR_QUERY);
        }

        //Removing duplicates while keeping the list order (https://stackoverflow.com/questions/19511797/remove-duplicates-in-an-array-without-changing-order-of-elements)
        matchingWordIds = new ArrayList<>(new LinkedHashSet<>(matchingWordIds));

        return matchingWordIds;
    }

    @NotNull
    private static List<Long> getMatchingWordIds(boolean forceExactSearch, @NotNull InputQuery query, String language, int db, Context context) {

        List<Long> matchingWordIds = new ArrayList<>();
        List<String> searchResultIndexesArray = new ArrayList<>();
        List<IndexKanji> kanjiIndices;

        //region Search for the matches in the indexed keywords
        if (query.getSearchType() == Globals.TEXT_TYPE_LATIN) {

            boolean exactSearch = query.isTooShort() || forceExactSearch;

            List<Object> indexes = findQueryInRomajiIndex(query.getSearchQueriesRomaji(), exactSearch, db, context);
            indexes.addAll(findQueryInNonJapIndices(query.getSearchQueriesNonJapanese(), exactSearch, language, db, context));

            if (indexes.size() == 0) return matchingWordIds;

            // If the entered word is Latin and only has up to SMALL_WORD_LENGTH, limit the word keywords to be checked (prevents matches overflow))
            List<GenericIndex> genericIndexes = (List<GenericIndex>)(Object) indexes;
            if (query.isTooShort()) {
                for (GenericIndex genericIndex : genericIndexes) {
                    if (genericIndex.getValue().length() < Globals.SMALL_WORD_LENGTH) {
                        searchResultIndexesArray.add(genericIndex.getWordIds());
                        break;
                    }
                }
            } else {
                for (GenericIndex genericIndex : genericIndexes) {
                    searchResultIndexesArray.add(genericIndex.getWordIds());
                }
            }
        } else if (query.getSearchType() == Globals.TEXT_TYPE_KANJI) {
            kanjiIndices = findQueryInKanjiIndex(query.getSearchQueriesKanji(), forceExactSearch, db, context);
            if (kanjiIndices.size() == 0) return matchingWordIds;
            for (IndexKanji indexKanji : kanjiIndices) {
                searchResultIndexesArray.add(indexKanji.getWordIds());
            }
        } else {
            return new ArrayList<>();
        }
        //endregion

        //region Get the indexes of all of the results that were found
        List<String> indexList;
        for (String searchResultIndexes : searchResultIndexesArray) {
            indexList = Arrays.asList(searchResultIndexes.split(Globals.DB_ELEMENTS_DELIMITER));
            for (int j = 0; j < indexList.size(); j++) {
                matchingWordIds.add(Long.parseLong(indexList.get(j)));
            }
        }
        //endregion

        return matchingWordIds;
    }

    @NotNull
    private static List<Object> findQueryInNonJapIndices(List<String> searchQueries, boolean exactSearch, String language, int db, Context context) {

        //Exact search Prevents the index search from returning too many results and crashing the app

        List<Object> matchingIndices = new ArrayList<>();
        if (exactSearch) {
            switch (language) {
                case Globals.LANG_STR_EN: {
                    List<IndexEnglish> indexesEnglish = OvUtilsDb.getEnglishIndexForExactWordsList(searchQueries, context, db);
                    if (indexesEnglish != null && indexesEnglish.size() > 0) matchingIndices.addAll(indexesEnglish); //Only add the index if the word was found in the index
                    break;
                }
                case Globals.LANG_STR_FR: {
                    List<IndexFrench> indexesFrench = OvUtilsDb.getFrenchIndexForExactWordsList(searchQueries, context, db);
                    if (indexesFrench != null && indexesFrench.size() > 0) matchingIndices.addAll(indexesFrench); //Only add the index if the word was found in the index
                    List<IndexEnglish> indexesEnglish = OvUtilsDb.getEnglishIndexForExactWordsList(searchQueries, context, db);
                    if (indexesEnglish != null && indexesEnglish.size() > 0) matchingIndices.addAll(indexesEnglish); //Only add the index if the word was found in the index
                    break;
                }
                case Globals.LANG_STR_ES: {
                    List<IndexSpanish> indexesSpanish = OvUtilsDb.getSpanishIndexForExactWordsList(searchQueries, context, db);
                    if (indexesSpanish != null && indexesSpanish.size() > 0) matchingIndices.addAll(indexesSpanish); //Only add the index if the word was found in the index
                    List<IndexEnglish> indexesEnglish = OvUtilsDb.getEnglishIndexForExactWordsList(searchQueries, context, db);
                    if (indexesEnglish != null && indexesEnglish.size() > 0) matchingIndices.addAll(indexesEnglish); //Only add the index if the word was found in the index
                    break;
                }
            }

        } else {
            switch (language) {
                case Globals.LANG_STR_EN: {
                    List<IndexEnglish> indexesEnglish = OvUtilsDb.getEnglishIndexesListForStartingWordsList(searchQueries, context, db);
                    if (indexesEnglish != null && indexesEnglish.size() > 0) matchingIndices.addAll(indexesEnglish);
                    break;
                }
                case Globals.LANG_STR_FR: {
                    List<IndexFrench> indexesFrench = OvUtilsDb.getFrenchIndexesListForStartingWordsList(searchQueries, context, db);
                    if (indexesFrench != null && indexesFrench.size() > 0) matchingIndices.addAll(indexesFrench);
                    List<IndexEnglish> indexesEnglish = OvUtilsDb.getEnglishIndexesListForStartingWordsList(searchQueries, context, db);
                    if (indexesEnglish != null && indexesEnglish.size() > 0) matchingIndices.addAll(indexesEnglish);
                    break;
                }
                case Globals.LANG_STR_ES: {
                    List<IndexSpanish> indexesSpanish = OvUtilsDb.getSpanishIndexesListForStartingWordsList(searchQueries, context, db);
                    if (indexesSpanish != null && indexesSpanish.size() > 0) matchingIndices.addAll(indexesSpanish);
                    List<IndexEnglish> indexesEnglish = OvUtilsDb.getEnglishIndexesListForStartingWordsList(searchQueries, context, db);
                    if (indexesEnglish != null && indexesEnglish.size() > 0) matchingIndices.addAll(indexesEnglish);
                    break;
                }
            }
        }
        return matchingIndices;
    }

    @Contract(pure = true)
    private static boolean stringContainsItemFromList(String inputStr, @NotNull List<String> items, boolean noSpaces) {
        if (noSpaces) {
            for (String item : items) {
                if (inputStr.replace(" ","").contains(item.replace(" ",""))) {
                    return true;
                }
            }
        } else {
            for (String item : items) {
                if (inputStr.contains(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Contract(pure = true)
    private static boolean listContainsItemFromList(@NotNull List<String> list1, List<String> list2) {
        for (String item1 : list1) {
            for (String item : list2) {
                if (item1.contains(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    @NotNull
    public static Object[] getMatchingWordIdsAndDoBasicFiltering(boolean roomExtendedDbIsAvailable,
                                                                 boolean roomNamesDatabaseIsAvailable,
                                                                 boolean roomNamesDatabasesFinishedLoading,
                                                                 @NotNull InputQuery query,
                                                                 String language,
                                                                 boolean showNames,
                                                                 Context context) {

        //region Initializations
        List<Long> matchingWordIdsCentral;
        List<Long> matchingWordIdsExtended = new ArrayList<>();
        List<Long> matchingWordIdsNames = new ArrayList<>();
        int queryType = query.getOriginalType();
        if (queryType == Globals.TEXT_TYPE_INVALID) return new Object[]{new ArrayList<>(), new ArrayList<>()};
        //endregion

        //region Getting the matches
        matchingWordIdsCentral = getNormalMatches(query, language, Globals.DB_CENTRAL, context);
        matchingWordIdsCentral.addAll(getAdjectivesFromConjugation(query, context, language));
        matchingWordIdsCentral.addAll(addCountersToMatchesList(query, context, language));
        matchingWordIdsCentral = UtilitiesGeneral.removeDuplicatesFromLongList(matchingWordIdsCentral);

        if (roomExtendedDbIsAvailable) matchingWordIdsExtended = getNormalMatches(query, language, Globals.DB_EXTENDED, context);

        if (showNames && roomNamesDatabasesFinishedLoading && roomNamesDatabaseIsAvailable) {
            matchingWordIdsNames = addNamesToMatchesList(query, context);
        }
        //endregion

        return new Object[]{matchingWordIdsCentral, matchingWordIdsExtended, matchingWordIdsNames};
    }

    @NotNull
    public static List<Word> getMergedWordsList(@NotNull List<Word> localWords, List<Word> asyncWords) {

        List<Word> finalWordsList = new ArrayList<>();
        List<Word> finalAsyncWords = new ArrayList<>(asyncWords);
        boolean asyncMeaningFoundLocally;

        for (int j = 0; j < localWords.size(); j++) {
            Word currentLocalWord = localWords.get(j);
            Word finalWord = new Word(currentLocalWord.getRomaji(), currentLocalWord.getKanji());

            //Copying basic properties
            finalWord.setFrequency(currentLocalWord.getFrequency());
            finalWord.setExtraKeywordsEN(currentLocalWord.getExtraKeywordsEN());
            finalWord.setExtraKeywordsFR(currentLocalWord.getExtraKeywordsFR());
            finalWord.setExtraKeywordsES(currentLocalWord.getExtraKeywordsES());
            finalWord.setExtraKeywordsJAP(currentLocalWord.getExtraKeywordsJAP());
            finalWord.setIsCommon(currentLocalWord.getIsCommon());
            finalWord.setIsLocal(currentLocalWord.getIsLocal());
            finalWord.setVerbConjMatchStatus(currentLocalWord.getVerbConjMatchStatus());
            finalWord.setMatchingConj(currentLocalWord.getMatchingConj());

            //Adjusting and copying alt spellings
            List<String> finalAltSpellings;
            String currentLocalWordAltSpellings = currentLocalWord.getAltSpellings();
            finalWord.setAltSpellings(currentLocalWordAltSpellings);
            if (currentLocalWordAltSpellings.equals("")) finalAltSpellings = new ArrayList<>();
            else {
                finalAltSpellings = new ArrayList<>(Arrays.asList(currentLocalWordAltSpellings.split(Globals.DB_ELEMENTS_DELIMITER)));
            }

            //Updating and copying meanings/alt spellings from the async word
            List<Word.Meaning> currentLocalMeanings = currentLocalWord.getMeaningsEN();
            List<Word.Meaning> currentFinalMeanings = new ArrayList<>(currentLocalMeanings);

            int currentIndex = finalAsyncWords.size() - 1;
            String concatRomajiAsyncword;
            String concatRomajiLocalword;
            while (currentIndex >= 0 && finalAsyncWords.size() != 0) {

                if (currentIndex > finalAsyncWords.size() - 1) break;
                Word currentAsyncWord = finalAsyncWords.get(currentIndex);

                concatRomajiAsyncword = currentAsyncWord.getRomaji().replace(" ", "");
                concatRomajiLocalword = currentLocalWord.getRomaji().replace(" ", "");
                if (concatRomajiAsyncword.equals(concatRomajiLocalword)
                        && currentAsyncWord.getKanji().equals(currentLocalWord.getKanji())) {

                    //Setting the altSpellings
                    String currentAsyncWordAltSpellings = currentAsyncWord.getAltSpellings();
                    if (!currentAsyncWordAltSpellings.equals("")) {
                        String[] finalAsyncWordAltSpellings = currentAsyncWordAltSpellings.split(Globals.DB_ELEMENTS_DELIMITER);
                        for (String altSpelling : finalAsyncWordAltSpellings) {
                            String trimmed = altSpelling.trim();
                            if (!OvUtilsGeneral.listContains(finalAltSpellings, trimmed)) {
                                finalAltSpellings.add(trimmed);
                            }
                        }
                        finalWord.setAltSpellings(OvUtilsGeneral.joinList(", ", finalAltSpellings));
                    }

                    //Setting the meanings
                    List<Word.Meaning> currentAsyncMeanings = currentAsyncWord.getMeaningsEN();
                    String currentLocalMeaning;
                    String currentAsyncMeaning;
                    for (int m = 0; m < currentAsyncMeanings.size(); m++) {

                        asyncMeaningFoundLocally = false;
                        for (int k = 0; k < currentLocalMeanings.size(); k++) {
                            currentLocalMeaning = currentLocalMeanings.get(k).getMeaning();
                            currentAsyncMeaning = currentAsyncMeanings.get(m).getMeaning();
                            if (currentLocalMeaning.contains(currentAsyncMeaning)) {
                                asyncMeaningFoundLocally = true;
                                break;
                            }
                        }
                        if (!asyncMeaningFoundLocally) {
                            currentFinalMeanings.add(currentAsyncMeanings.get(m));
                        }
                    }
                    finalAsyncWords.remove(currentIndex);
                    if (currentIndex == 0) break;
                } else {
                    currentIndex -= 1;
                }
            }
            finalWord.setMeaningsEN(currentFinalMeanings);
            finalWord.setMeaningsFR(currentLocalWord.getMeaningsFR());
            finalWord.setMeaningsES(currentLocalWord.getMeaningsES());

            finalWordsList.add(finalWord);
        }

        //Once all async words have been merged with the local words, set isLocal=false to the remaining async words and add them to the list
        finalWordsList.addAll(finalAsyncWords);

        return finalWordsList;
    }

    @NotNull
    public static List<Word> getDifferentAsyncWords(List<Word> localWords, @NotNull List<Word> asyncWords) {

        List<Word> differentAsyncWords = new ArrayList<>();
        List<Word> remainingLocalWords = new ArrayList<>(localWords);
        List<Word.Meaning> localMeanings;
        List<Word.Meaning> asyncMeanings;
        List<Word.Meaning> remainingLocalMeanings;
        boolean foundMatchingLocalWord;
        int localMeaningIndex;
        int localWordIndex;
        String asyncRomaji;
        String localRomaji;
        String asyncKanji;
        String localKanji;

        Word localWord;

        for (Word asyncWord : asyncWords) {

            foundMatchingLocalWord = false;
            localWordIndex = 0;

            asyncRomaji = asyncWord.getRomaji();
            asyncKanji = asyncWord.getKanji();

            while (localWordIndex < remainingLocalWords.size()) {

                localWord = remainingLocalWords.get(localWordIndex);
                localRomaji = localWord.getRomaji().replace(" ", "");
                localKanji = localWord.getKanji();

                if (asyncRomaji.equals(localRomaji) && asyncKanji.equals(localKanji)) {

                    foundMatchingLocalWord = true;

                    localMeanings = localWord.getMeaningsEN();
                    asyncMeanings = asyncWord.getMeaningsEN();


                    //If non-identical meanings remain, it is possible that a Jisho meaning was split by types in the local database, therefore check the following:
                    StringBuilder allLocalMeanings = new StringBuilder();
                    for (Word.Meaning meaning : localMeanings) {
                        allLocalMeanings.append(meaning.getMeaning());
                        allLocalMeanings.append(", ");
                    }

                    List<String> allAsyncMeaningElements = new ArrayList<>();
                    boolean isInParenthesis;
                    StringBuilder currentElement;
                    String currentAsyncMeaning;
                    String currentAsyncMeaningChar;
                    for (Word.Meaning asyncWordMeaning : asyncMeanings) {
                        isInParenthesis = false;
                        currentElement = new StringBuilder();
                        currentAsyncMeaning = asyncWordMeaning.getMeaning();
                        for (int i = 0; i < asyncWordMeaning.getMeaning().length(); i++) {
                            currentAsyncMeaningChar = currentAsyncMeaning.substring(i, i + 1);
                            if (currentAsyncMeaningChar.equals("(")) isInParenthesis = true;
                            else if (currentAsyncMeaningChar.equals(")")) isInParenthesis = false;

                            if (isInParenthesis || !currentAsyncMeaningChar.equals(",")) currentElement.append(currentAsyncMeaningChar);

                            if (currentAsyncMeaningChar.equals(",") && !isInParenthesis || i == asyncWordMeaning.getMeaning().length() - 1) {
                                allAsyncMeaningElements.add(currentElement.toString().trim());
                                currentElement = new StringBuilder();
                            }
                        }
                    }

                    String allLocalMeaningsAsString = allLocalMeanings.toString();
                    boolean meaningNotFoundInLocalWord = false;
                    for (String element : allAsyncMeaningElements) {
                        if (!allLocalMeaningsAsString.contains(element)) {
                            meaningNotFoundInLocalWord = true;
                            break;
                        }
                    }

                    boolean altSpellingNotFoundInLocalWord = false;
                    if (!OvUtilsGeneral.isEmptyString(asyncWord.getAltSpellings())) {
                        if (OvUtilsGeneral.isEmptyString(localWord.getAltSpellings())) altSpellingNotFoundInLocalWord = true;
                        else {
                            for (String asyncAltSpelling : asyncWord.getAltSpellings().split(",")) {
                                if (!localWord.getAltSpellings().contains(asyncAltSpelling.trim())) {
                                    altSpellingNotFoundInLocalWord = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (meaningNotFoundInLocalWord || altSpellingNotFoundInLocalWord) differentAsyncWords.add(asyncWord);

                    remainingLocalWords.remove(localWord);
                    break;
                } else {
                    localWordIndex++;
                }
            }

            if (!foundMatchingLocalWord) differentAsyncWords.add(asyncWord);

        }

        for (Word word : differentAsyncWords) {
            if (word.getKanji().equals("為る")) {
                differentAsyncWords.remove(word);
                break;
            }
        }

        return differentAsyncWords;
    }

    @NotNull
    public static List<Word> getCommonWords(@NotNull List<Word> wordsList) {
        List<Word> commonWords = new ArrayList<>();
        for (Word word : wordsList) {
            if (word.getIsCommon()) commonWords.add(word);
        }
        return commonWords;
    }

    public static int getRankingFromWordAttributes(@NotNull Word currentWord, String mInputQuery, boolean queryIsVerbWithTo, String language) {

        //FIXME: build table of cases so that ranking can be decided more easily
        int ranking = Globals.STARTING_RANK_VALUE;
        String romajiValue = currentWord.getRomaji();
        String kanjiValue = currentWord.getKanji();
        String altSValue = currentWord.getAltSpellings();
        String kwJapValue = currentWord.getExtraKeywordsJAP() == null ? "" : currentWord.getExtraKeywordsJAP();
        String kwLatin = "";
        List<Word.Meaning> currentMeanings = currentWord.getMeaningsEN();
        if (currentMeanings == null) return 0;
        switch (language) {
            case Globals.LANG_STR_EN:
                currentMeanings = currentWord.getMeaningsEN();
                kwLatin = currentWord.getExtraKeywordsEN() == null ? "" : currentWord.getExtraKeywordsEN();
                break;
            case Globals.LANG_STR_FR:
                currentMeanings = currentWord.getMeaningsFR();
                kwLatin = currentWord.getExtraKeywordsFR() == null ? "" : currentWord.getExtraKeywordsFR();
                break;
            case Globals.LANG_STR_ES:
                currentMeanings = currentWord.getMeaningsES();
                kwLatin = currentWord.getExtraKeywordsES() == null ? "" : currentWord.getExtraKeywordsES();
                break;
        }
        if (currentMeanings == null || currentMeanings.size() == 0) {
            currentMeanings = currentWord.getMeaningsEN();
        }
        List<List<String>> altSpellingsLatinKanji = getLatinKanjiWords(altSValue.split(Globals.DB_ELEMENTS_DELIMITER));
        List<String> keywords = new ArrayList<>(Arrays.asList(kwJapValue.split(Globals.DB_ELEMENTS_DELIMITER)));
        if (!kwLatin.equals("")) keywords.addAll(Arrays.asList(kwLatin.split(Globals.DB_ELEMENTS_DELIMITER)));
        String type = currentWord.getMeaningsEN().get(0).getType();

        List<String> effectiveMeanings = new ArrayList<>();
        int openParenthesisCounter = 0;
        for (Word.Meaning meaning : currentMeanings) {
            String meaningStr = meaning.getMeaning();
            List<String> chars = new ArrayList<>();
            for (int i = 0;i < meaningStr.length(); i++) {
                String meaningChar = meaningStr.substring(i, i+1);
                if (".;,!?".contains(meaningChar) && openParenthesisCounter == 0) {
                    effectiveMeanings.add(OvUtilsGeneral.joinList("", chars));
                    chars = new ArrayList<>();
                } else {
                    if ("([{".contains(meaningChar)) openParenthesisCounter++;
                    else if (")]}".contains(meaningChar)) openParenthesisCounter--;
                    chars.add(meaningChar);
                }
            }
            if (chars.size()>0) effectiveMeanings.add(OvUtilsGeneral.joinList("", chars));
        }
        List<List<String>> meaningsAsWords = new ArrayList<>();
        for (String effectiveMeaning : effectiveMeanings) {
            String[] splitBySpace = effectiveMeaning.split(" ");
            List<String> parsedWords = new ArrayList<>();
            for (String item : splitBySpace) {
                List<String> chars = new ArrayList<>();
                for (int i = 0;i < item.length(); i++) {
                    String meaningChar = item.substring(i, i+1);
                    if ("([{)]}".contains(meaningChar)) {
                        parsedWords.add(OvUtilsGeneral.joinList("", chars));
                        chars = new ArrayList<>();
                        parsedWords.add(meaningChar);
                    } else {
                        chars.add(meaningChar);
                    }
                }
                if (chars.size()>0) parsedWords.add(OvUtilsGeneral.joinList("", chars));
            }
            meaningsAsWords.add(parsedWords);
        }

        boolean currentWordIsAVerb = type.length() > 0 && type.startsWith("V") && !type.equals("VC") && !type.equals("NV");


        boolean foundCondition = false;
        for (String key : Globals.SORTED_RANK_CONDITIONS) {
            switch (key) {
                case Globals.KJ_EX_MATCH:
                    if (kanjiValue.equals(mInputQuery)) {
                        ranking = Globals.RANKINGS.get(key); foundCondition = true;
                    }
                    break;
                case Globals.KJ_ALTS_EX_MATCH:
                    for (String word : altSpellingsLatinKanji.get(Globals.KANJI_WORDS)) {
                        if (word.equals(mInputQuery)) {
                            ranking = Globals.RANKINGS.get(key); foundCondition = true;
                            break;
                        }
                    }
                    break;
                case Globals.R_EX_MATCH:
                    if (romajiValue.equals(mInputQuery)) {
                        ranking = Globals.RANKINGS.get(key); foundCondition = true;
                    }
                    break;
                case Globals.R_ALTS_EX_MATCH:
                    for (String word : altSpellingsLatinKanji.get(Globals.LATIN_WORDS)) {
                        if (word.equals(mInputQuery)) {
                            ranking = Globals.RANKINGS.get(key); foundCondition = true;
                            break;
                        }
                    }
                    break;
                case Globals.FIRST_MEANING_EX_PHRASE_MATCH:
                    if (currentMeanings.get(0).getMeaning().equals(mInputQuery)) {
                        ranking = Globals.RANKINGS.get(key); foundCondition = true;
                    }
                    break;
                case Globals.FIRST_MEANING_TO_EX_PHRASE_MATCH:
                    if (OvUtilsGeneral.concat(new String[]{"to ", currentMeanings.get(0).getMeaning()}).equals(mInputQuery)) {
                        ranking = Globals.RANKINGS.get(key); foundCondition = true;
                    }
                    break;
                case Globals.SECOND_MEANING_EX_PHRASE_MATCH:
                    for (int i=1; i<currentMeanings.size(); i++) {
                        if (currentMeanings.get(i).getMeaning().equals(mInputQuery)) {
                            ranking = Globals.RANKINGS.get(key); foundCondition = true;
                            break;
                        }
                    }
                    break;
                case Globals.SECOND_MEANING_TO_EX_PHRASE_MATCH:
                    for (int i=1; i<currentMeanings.size(); i++) {
                        if (OvUtilsGeneral.concat(new String[]{"to ", currentMeanings.get(i).getMeaning()}).equals(mInputQuery)) {
                            ranking = Globals.RANKINGS.get(key); foundCondition = true;
                            break;
                        }
                    }
                    break;
                case Globals.KW_EX_MATCH:
                    for (String keyword : keywords) {
                        if (keyword.equals(mInputQuery)) {
                            ranking = Globals.RANKINGS.get(key); foundCondition = true;
                            break;
                        }
                    }
                    break;
                case Globals.KJ_PART_START_MATCH:
                    if (kanjiValue.startsWith(mInputQuery)) {
                        ranking = Globals.RANKINGS.get(key); foundCondition = true;
                    }
                    break;
                case Globals.KJ_ALTS_PART_START_MATCH:
                    for (String word : altSpellingsLatinKanji.get(Globals.KANJI_WORDS)) {
                        if (word.startsWith(mInputQuery)) {
                            ranking = Globals.RANKINGS.get(key); foundCondition = true;
                            break;
                        }
                    }
                    break;
                case Globals.R_PART_START_MATCH:
                    if (romajiValue.startsWith(mInputQuery)) {
                        ranking = Globals.RANKINGS.get(key); foundCondition = true;
                    }
                    break;
                case Globals.R_ALTS_PART_START_MATCH:
                    for (String word : altSpellingsLatinKanji.get(Globals.LATIN_WORDS)) {
                        if (word.startsWith(mInputQuery)) {
                            ranking = Globals.RANKINGS.get(key); foundCondition = true;
                            break;
                        }
                    }
                    break;
                case Globals.FIRST_MEANING_EX_WORD_MATCH:
                    for (String word : meaningsAsWords.get(0)) {
                        if (word.equals(mInputQuery)) {
                            ranking = Globals.RANKINGS.get(key); foundCondition = true;
                            break;
                        }
                    }
                    break;
                case Globals.FIRST_MEANING_TO_EX_WORD_MATCH:
                    for (String word : meaningsAsWords.get(0)) {
                        if (OvUtilsGeneral.concat(new String[]{"to ", word}).equals(mInputQuery)) {
                            ranking = Globals.RANKINGS.get(key); foundCondition = true;
                        }
                        break;
                    }
                case Globals.SECOND_MEANING_EX_WORD_MATCH:
                    for (int i=1; i<currentMeanings.size(); i++) {
                        for (String word : meaningsAsWords.get(i)) {
                            if (word.equals(mInputQuery)) {
                                ranking = Globals.RANKINGS.get(key); foundCondition = true;
                                break;
                            }
                        }
                        if (foundCondition) break;
                    }
                    break;
                case Globals.SECOND_MEANING_TO_EX_WORD_MATCH:
                    for (int i=1; i<currentMeanings.size(); i++) {
                        for (String word : meaningsAsWords.get(i)) {
                            if (OvUtilsGeneral.concat(new String[]{"to ", word}).equals(mInputQuery)) {
                                ranking = Globals.RANKINGS.get(key); foundCondition = true;
                                break;
                            }
                        }
                        if (foundCondition) break;
                    }
                    break;
                case Globals.KJ_PART_MATCH:
                    if (kanjiValue.contains(mInputQuery)) {
                        ranking = Globals.RANKINGS.get(key); foundCondition = true;
                    }
                    break;
                case Globals.KANJI_ALTS_PART_MATCH:
                    for (String word : altSpellingsLatinKanji.get(Globals.KANJI_WORDS)) {
                        if (word.contains(mInputQuery)) {
                            ranking = Globals.RANKINGS.get(key); foundCondition = true;
                            break;
                        }
                    }
                    break;
                case Globals.R_PART_MATCH:
                    if (romajiValue.contains(mInputQuery)) {
                        ranking = Globals.RANKINGS.get(key); foundCondition = true;
                    }
                    break;
                case Globals.R_ALTS_PART_MATCH:
                    for (String word : altSpellingsLatinKanji.get(Globals.LATIN_WORDS)) {
                        if (word.contains(mInputQuery)) {
                            ranking = Globals.RANKINGS.get(key); foundCondition = true;
                            break;
                        }
                    }
                    break;
                case Globals.KW_PART_MATCH:
                    for (String keyword : keywords) {
                        if (keyword.contains(mInputQuery)) {
                            ranking = Globals.RANKINGS.get(key); foundCondition = true;
                            break;
                        }
                    }
                    break;
                case Globals.FIRST_MEANING_PART_PHRASE_MATCH:
                    if (currentMeanings.get(0).getMeaning().contains(mInputQuery)) {
                        ranking = Globals.RANKINGS.get(key); foundCondition = true;
                    }
                    break;
                case Globals.FIRST_MEANING_TO_PART_PHRASE_MATCH:
                    if (OvUtilsGeneral.concat(new String[]{"to ", currentMeanings.get(0).getMeaning()}).contains(mInputQuery)) {
                        ranking = Globals.RANKINGS.get(key); foundCondition = true;
                    }
                    break;
                case Globals.SECOND_MEANING_PART_PHRASE_MATCH:
                    for (int i=1; i<currentMeanings.size(); i++) {
                        if (currentMeanings.get(i).getMeaning().contains(mInputQuery)) {
                            ranking = Globals.RANKINGS.get(key); foundCondition = true;
                            break;
                        }
                    }
                    break;
                case Globals.SECOND_MEANING_TO_PART_PHRASE_MATCH:
                    for (int i=1; i<currentMeanings.size(); i++) {
                        if (OvUtilsGeneral.concat(new String[]{"to ", currentMeanings.get(i).getMeaning()}).contains(mInputQuery)) {
                            ranking = Globals.RANKINGS.get(key); foundCondition = true;
                            break;
                        }
                    }
                    break;
                case Globals.FIRST_MEANING_PART_WORD_MATCH:
                    for (String word : meaningsAsWords.get(0)) {
                        if (word.contains(mInputQuery)) {
                            ranking = Globals.RANKINGS.get(key); foundCondition = true;
                            break;
                        }
                    }
                    break;
                case Globals.FIRST_MEANING_TO_PART_WORD_MATCH:
                    for (String word : meaningsAsWords.get(0)) {
                        if (OvUtilsGeneral.concat(new String[]{"to ", word}).contains(mInputQuery)) {
                            ranking = Globals.RANKINGS.get(key); foundCondition = true;
                        }
                        break;
                    }
                case Globals.SECOND_MEANING_PART_WORD_MATCH:
                    for (int i=1; i<currentMeanings.size(); i++) {
                        for (String word : meaningsAsWords.get(i)) {
                            if (word.contains(mInputQuery)) {
                                ranking = Globals.RANKINGS.get(key); foundCondition = true;
                                break;
                            }
                        }
                        if (foundCondition) break;
                    }
                    break;
                case Globals.SECOND_MEANING_TO_PART_WORD_MATCH:
                    for (int i=1; i<currentMeanings.size(); i++) {
                        for (String word : meaningsAsWords.get(i)) {
                            if (OvUtilsGeneral.concat(new String[]{"to ", word}).contains(mInputQuery)) {
                                ranking = Globals.RANKINGS.get(key); foundCondition = true;
                                break;
                            }
                        }
                        if (foundCondition) break;
                    }
                    break;
                default: break;
            }
            if (foundCondition) break;
        }


        return ranking;
    }

    public static int getRankingFromWordAttributesOLD(@NotNull Word currentWord, String mInputQuery, boolean queryIsVerbWithTo, String language) {

        //FIXME: build table of cases so that ranking can be decided more easily
        int ranking;
        String romajiValue = currentWord.getRomaji();
        String kanjiValue = currentWord.getKanji();
        String altSValue = currentWord.getAltSpellings();
        String kwJapValue = currentWord.getExtraKeywordsJAP() == null ? "" : currentWord.getExtraKeywordsJAP();
        String kwLatin = "";
        List<Word.Meaning> currentMeanings = currentWord.getMeaningsEN();
        if (currentMeanings == null) return 0;
        switch (language) {
            case Globals.LANG_STR_EN:
                currentMeanings = currentWord.getMeaningsEN();
                kwLatin = currentWord.getExtraKeywordsEN() == null ? "" : currentWord.getExtraKeywordsEN();
                break;
            case Globals.LANG_STR_FR:
                currentMeanings = currentWord.getMeaningsFR();
                kwLatin = currentWord.getExtraKeywordsFR() == null ? "" : currentWord.getExtraKeywordsFR();
                break;
            case Globals.LANG_STR_ES:
                currentMeanings = currentWord.getMeaningsES();
                kwLatin = currentWord.getExtraKeywordsES() == null ? "" : currentWord.getExtraKeywordsES();
                break;
        }
        if (currentMeanings == null || currentMeanings.size() == 0) {
            currentMeanings = currentWord.getMeaningsEN();
        }
        List<String> keywords = Arrays.asList(kwJapValue.split(Globals.DB_ELEMENTS_DELIMITER));
        keywords.addAll(Arrays.asList(kwLatin.split(Globals.DB_ELEMENTS_DELIMITER)));
        String type = currentWord.getMeaningsEN().get(0).getType();
        boolean currentWordIsAVerb = type.length() > 0 && type.startsWith("V") && !type.equals("VC") && !type.equals("NV");

        // Getting ranking according to meaning string length
        // with penalties depending on the lateness of the word in the meanings
        // and the exactness of the match
        String trimmedValue;
        int missingLanguagePenalty = 0;
        if (currentMeanings == null || currentMeanings.size() == 0) {
            missingLanguagePenalty = 10000;
        }

        String currentMeaning;
        String inputQuery;
        int lateMeaningPenalty = 0;
        boolean foundMeaningLength;
        int lateHitInMeaningPenalty; //Adding a penalty for late hits in the meaning
        int cumulativeMeaningLength; //Using a cumulative meaning length instead of the total length, since if a word is at the start of a meaning it's more important and the hit is more likely to be relevant

        ranking = 10000;
        if (!currentWordIsAVerb || !queryIsVerbWithTo) ranking += 1000;
        else ranking += 200;

        for (int j = 0; j < currentMeanings.size(); j++) {
            currentMeaning = currentMeanings.get(j).getMeaning().toLowerCase();
            foundMeaningLength = false;

            //region If the current word is not a verb
            if (!currentWordIsAVerb) {
                inputQuery = mInputQuery;

                //If meaning has the exact word, get the length as follows
                String[] currentMeaningIndividualElements = OvUtilsGeneral.splitAtCommasOutsideParentheses(currentMeaning);
                lateHitInMeaningPenalty = 0;
                cumulativeMeaningLength = 0;
                if (currentMeaning.equals(inputQuery)) {
                    ranking -= Globals.RANKING_EXACT_MEANING_MATCH_BONUS;
                    foundMeaningLength = true;
                } else {
                    for (String currentMeaningElement : currentMeaningIndividualElements) {
                        String trimmedElement = currentMeaningElement.trim().toLowerCase();

                        //If there's an exact match, push the word up in ranking
                        if (trimmedElement.equals(inputQuery)) {
                            ranking -= Globals.RANKING_EXACT_WORD_MATCH_BONUS;
                            foundMeaningLength = true;
                        }
                        if (foundMeaningLength) break;

                        String[] currentMeaningIndividualWords = trimmedElement.split(" ");
                        for (String word : currentMeaningIndividualWords) {
                            cumulativeMeaningLength += word.length() + 2; //Added 2 to account for missing ", " instances in loop
                            if (word.equals(inputQuery)) {
                                ranking -= Globals.RANKING_WORD_MATCH_IN_SENTENCE_BONUS;
                                foundMeaningLength = true;
                                break;
                            }
                            lateHitInMeaningPenalty += Globals.RANKING_LATE_HIT_IN_SENTENCE_PENALTY;
                        }
                        if (foundMeaningLength) break;

                        //If meaning has the exact word but maybe in parentheses, get the length as follows
                        String preparedTrimmedElement = trimmedElement.replaceAll("([()])", "");
                        String[] currentMeaningIndividualWordsWithoutParentheses = preparedTrimmedElement.split(" ");
                        for (String word : currentMeaningIndividualWordsWithoutParentheses) {
                            cumulativeMeaningLength += word.length() + 2; //Added 2 to account for missing ", " instances in loop
                            if (word.equals(inputQuery)) {
                                ranking -= Globals.RANKING_WORD_MATCH_IN_PARENTHESES_BONUS;
                                foundMeaningLength = true;
                                break;
                            }
                            lateHitInMeaningPenalty += Globals.RANKING_LATE_HIT_IN_SENTENCE_PENALTY;
                        }
                        if (foundMeaningLength) break;

                        lateHitInMeaningPenalty += Globals.RANKING_LATE_HIT_IN_MEANING_ELEMENTS_PENALTY;
                    }
                }
                if (foundMeaningLength) {
                    ranking += lateMeaningPenalty + lateHitInMeaningPenalty + cumulativeMeaningLength;
                    break;
                }

                //If still not found, get the length of the less important results
                if (currentMeaning.contains(inputQuery) && currentMeaning.length() <= ranking) {
                    ranking += currentMeaning.length();
                }
            }
            //endregion

            //region If the current word is a verb
            else {

                String[] currentMeaningIndividualElements = OvUtilsGeneral.splitAtCommasOutsideParentheses(currentMeaning);
                if (!queryIsVerbWithTo) {

                    //Calculate the length first by adding "to " to the input query. If it leads to a hit, that means that this verb is relevant
                    inputQuery = OvUtilsGeneral.concat(new String[]{"to ",mInputQuery});

                    lateHitInMeaningPenalty = 0;
                    cumulativeMeaningLength = 0;
                    if (currentMeaning.equals(inputQuery)) {
                        ranking -= Globals.RANKING_EXACT_MEANING_MATCH_BONUS;
                        foundMeaningLength = true;
                    } else {
                        for (String element : currentMeaningIndividualElements) {

                            String trimmedElement = element.trim();

                            //If there's an exact match, push the word up in ranking
                            if (trimmedElement.equals(inputQuery)) {
                                ranking -= Globals.RANKING_EXACT_WORD_MATCH_BONUS;
                                foundMeaningLength = true;
                            }
                            if (foundMeaningLength) break;

                            cumulativeMeaningLength += element.length() + 2; //Added 2 to account for missing ", " instances in loop
                            if (trimmedElement.contains(inputQuery)) {
                                ranking -= Globals.RANKING_WORD_MATCH_IN_SENTENCE_BONUS;
                                foundMeaningLength = true;
                                break;
                            }
                            lateHitInMeaningPenalty += Globals.RANKING_LATE_HIT_IN_SENTENCE_PENALTY;
                        }
                    }
                    if (foundMeaningLength) {
                        ranking += lateMeaningPenalty + lateHitInMeaningPenalty + cumulativeMeaningLength;
                        break;
                    }

                    //Otherwise, use the original query to get the length
                    inputQuery = mInputQuery;
                    lateHitInMeaningPenalty = 0;
                    cumulativeMeaningLength = 0;
                    if (currentMeaning.equals(inputQuery)) {
                        ranking -= Globals.RANKING_EXACT_MEANING_MATCH_BONUS;
                        foundMeaningLength = true;
                    } else {
                        for (String element : currentMeaningIndividualElements) {

                            String trimmedElement = element.trim();

                            //If there's an exact match, push the word up in ranking
                            if (trimmedElement.equals(inputQuery)) {
                                ranking -= Globals.RANKING_EXACT_WORD_MATCH_BONUS;
                                foundMeaningLength = true;
                            }
                            if (foundMeaningLength) break;

                            if (trimmedElement.contains(inputQuery)) {
                                ranking -= Globals.RANKING_WORD_MATCH_IN_SENTENCE_BONUS;
                                foundMeaningLength = true;
                                break;
                            }
                            cumulativeMeaningLength += element.length() + 2; //Added 2 to account for missing ", " instances in loop
                            lateHitInMeaningPenalty += Globals.RANKING_LATE_HIT_IN_SENTENCE_PENALTY;
                        }
                    }
                } else {
                    inputQuery = mInputQuery;

                    //Get the length according to the position of the verb in the meanings list
                    lateHitInMeaningPenalty = 0;
                    cumulativeMeaningLength = 0;
                    if (currentMeaning.equals(inputQuery)) {
                        ranking -= Globals.RANKING_EXACT_MEANING_MATCH_BONUS;
                        foundMeaningLength = true;
                    } else {
                        for (String element : currentMeaningIndividualElements) {

                            String trimmedElement = element.trim();

                            //If there's an exact match, push the word up in ranking
                            if (trimmedElement.equals(inputQuery)) {
                                ranking -= Globals.RANKING_EXACT_WORD_MATCH_BONUS;
                                foundMeaningLength = true;
                            }
                            if (foundMeaningLength) break;

                            cumulativeMeaningLength += element.length() + 2; //Added 2 to account for missing ", " instances in loop
                            if (trimmedElement.contains(inputQuery)) {
                                ranking -= Globals.RANKING_WORD_MATCH_IN_SENTENCE_BONUS;
                                foundMeaningLength = true;
                                break;
                            }
                            lateHitInMeaningPenalty += Globals.RANKING_LATE_HIT_IN_SENTENCE_PENALTY;
                        }
                    }
                }

                if (foundMeaningLength) {
                    ranking += lateMeaningPenalty + lateHitInMeaningPenalty + cumulativeMeaningLength;
                    break;
                }

            }
            //endregion

            lateMeaningPenalty += Globals.RANKING_LATE_MEANING_MATCH_PENALTY;

        }

        //Adding the romaji and kanji lengths to the ranking (ie. shorter is better)
        ranking += 2 * (romajiValue.length() + kanjiValue.length());

        //If the word starts with the inputQuery, its ranking improves
        String romajiNoSpaces = getRomajiNoSpacesForSpecialPartsOfSpeech(romajiValue);
        if (       (romajiValue.length() >= mInputQuery.length()     && romajiValue.startsWith(mInputQuery))
                || (kanjiValue.length() >= mInputQuery.length()      && kanjiValue.startsWith(mInputQuery))
                || romajiNoSpaces.equals(mInputQuery)
        ) {
            ranking -= 1000;
        }

        //Otherwise, if the romaji or Kanji value contains the search word, then it must appear near the start of the list
        else if (romajiValue.contains(mInputQuery) || kanjiValue.contains(mInputQuery)) ranking -= 300;

        //If the word is a name, the ranking worsens
        Word.Meaning wordFirstMeaning = currentWord.getMeaningsEN().get(0);
        if (OvUtilsGeneral.listContains(Globals.NAMES_LIST, wordFirstMeaning.getType())) ranking += 5000;

        //If the word is common, the ranking improves
        if (currentWord.getIsCommon()) ranking -= 50;

        //If the word is a verb and one of its conjugations is a perfect match, the ranking improves on a level similar to meaning bonuses
        if (currentWord.getVerbConjMatchStatus() == Word.CONJ_MATCH_EXACT) ranking -= 1400;

        //If the word is a verb and one of its conjugations is a partial match, the ranking improves a bit less than for perfect matches
        if (currentWord.getVerbConjMatchStatus() == Word.CONJ_MATCH_CONTAINED) ranking -= 1000;

        //If one of the elements in altSpellings is a perfect match, the ranking improves
        for (String element : altSValue.split(Globals.DB_ELEMENTS_DELIMITER)) {
            trimmedValue = element.trim();
            if (mInputQuery.equals(trimmedValue)) {
                ranking -= 70;
                break;
            }
        }

        //If one of the elements in the Japanese Keywords is a perfect match, the ranking improves
        for (String element : kwJapValue.split(",")) {
            trimmedValue = element.trim();
            if (mInputQuery.equals(trimmedValue)) {
                ranking -= 70;
                break;
            }
        }

        //If one of the elements in the Latin Keywords is a perfect match, the ranking improves
        for (String element : kwLatin.split(",")) {
            trimmedValue = element.trim();
            if (mInputQuery.equals(trimmedValue)) {
                ranking -= 40;
                break;
            } else {
                trimmedValue = trimmedValue.replace(" ","");
                if (mInputQuery.equals(trimmedValue)) {
                    ranking -= 30;
                    break;
                }
            }
        }

        //If the romaji or Kanji value is an exact match to the search word, then it must appear at the start of the list
        if (romajiValue.equals(mInputQuery) || kanjiValue.equals(mInputQuery)) ranking = 0;
        else {
            String cleanValue = romajiValue.replace(" ", "");
            if (cleanValue.equals(mInputQuery)) {
                ranking -= 2000;
            }
        }

        //Penalizing for missing languages
        ranking += missingLanguagePenalty;

        //Adding a bonus for high literary frequency
        int frequency = currentWord.getFrequency();
        ranking -= (20000 - ((frequency==0)? 20000 : frequency)) / 100;

        return ranking;
    }

    @NotNull
    public static String getRomajiNoSpacesForSpecialPartsOfSpeech(@NotNull String romaji) {
        romaji = romaji.replace(" ni", "ni");
        romaji = romaji.replace(" de", "de");
        romaji = romaji.replace(" wo", "wo");
        romaji = romaji.replace(" to", "to");
        romaji = romaji.replace(" na", "na");
        return romaji;
    }

    public static boolean wordsAreSimilar(@NotNull Word wordA, String wordB) {
        String trimmedRomajiA = wordA.getRomaji().trim();
        String trimmedKanjiA = wordA.getKanji().trim();
        return trimmedRomajiA.equals(wordB) || trimmedKanjiA.equals(wordB);
    }

    //Conjugator Module utilities
    @NotNull
    public static List<ConjugationTitle> getConjugationTitles(@NotNull List<String[]> verbLatinConjDatabase, Context context, String language) {

        String[] titlesRow = verbLatinConjDatabase.get(0);
        String[] subtitlesRow = verbLatinConjDatabase.get(1);
        String[] endingsRow = verbLatinConjDatabase.get(2);
        int sheetLength = titlesRow.length;
        List<ConjugationTitle> conjugationTitles = new ArrayList<>();
        List<ConjugationTitle.Subtitle> subtitles = new ArrayList<>();
        ConjugationTitle conjugationTitle = new ConjugationTitle();
        String titleRef;
        String subtitleRef;
        ConjugationTitle.Subtitle subtitle;

        for (int col = 0; col < sheetLength; col++) {

            if (col == 0) {
                titleRef = Globals.VERB_CONJUGATION_TITLES.get(titlesRow[col]);
                conjugationTitle.setTitle(OvUtilsResources.getString(titleRef, context, Globals.RESOURCE_MAP_VERB_CONJ_TITLES, language));
                conjugationTitle.setTitleIndex(col);

                subtitle = new ConjugationTitle.Subtitle();
                subtitleRef = Globals.VERB_CONJUGATION_TITLES.get(subtitlesRow[col]);
                subtitle.setSubtitle(OvUtilsResources.getString(subtitleRef, context, Globals.RESOURCE_MAP_VERB_CONJ_TITLES, language));
                subtitle.setSubtitleIndex(col);
                subtitles.add(subtitle);
            } else if (col == sheetLength - 1) {
               subtitle = new ConjugationTitle.Subtitle();
                subtitleRef = Globals.VERB_CONJUGATION_TITLES.get(subtitlesRow[col]);
                subtitle.setSubtitle(OvUtilsResources.getString(subtitleRef, context, Globals.RESOURCE_MAP_VERB_CONJ_TITLES, language));
                subtitle.setSubtitleIndex(col);
                subtitles.add(subtitle);

                conjugationTitle.setSubtitles(subtitles);
                conjugationTitles.add(conjugationTitle);
            } else {
                if (!titlesRow[col].equals("")) {

                    conjugationTitle.setSubtitles(subtitles);
                    conjugationTitles.add(conjugationTitle);

                    conjugationTitle = new ConjugationTitle();
                    subtitles = new ArrayList<>();

                    titleRef = Globals.VERB_CONJUGATION_TITLES.get(titlesRow[col]);
                    conjugationTitle.setTitle(OvUtilsResources.getString(titleRef, context, Globals.RESOURCE_MAP_VERB_CONJ_TITLES, language));
                    conjugationTitle.setTitleIndex(col);

                }

                subtitle = new ConjugationTitle.Subtitle();
                subtitleRef = Globals.VERB_CONJUGATION_TITLES.get(subtitlesRow[col]);
                subtitle.setSubtitle(OvUtilsResources.getString(subtitleRef, context, Globals.RESOURCE_MAP_VERB_CONJ_TITLES, language));
                String ending = (col <= Globals.COLUMN_VERB_MASUSTEM) ? "" : endingsRow[col];
                subtitle.setEnding(ending);
                subtitle.setSubtitleIndex(col);
                subtitles.add(subtitle);
            }
        }

        return conjugationTitles;
    }

    @NotNull
    public static String replaceInvalidKanjisWithValidOnes(@NotNull String input) {
        List<String> output = new ArrayList<>();
        String currentChar;
        boolean found;
        for (int i = 0; i < input.length(); i++) {
            currentChar = input.substring(i, i+1);
            found = false;
            for (int j = 0; j < Globals.GLOBAL_SIMILARS_DATABASE.size(); j++) {
                String char0 = Globals.GLOBAL_SIMILARS_DATABASE.get(j)[0].substring(0, 1);
                if (Globals.GLOBAL_SIMILARS_DATABASE.get(j).length > 0 && char0.equals(currentChar)) {
                    String char1 = Globals.GLOBAL_SIMILARS_DATABASE.get(j)[1].substring(0, 1);
                    output.add(char1);
                    found = true;
                    break;
                }
            }
            if (!found) output.add(currentChar);
        }
        return OvUtilsGeneral.joinList("", output);
    }

    @NotNull
    public static List<Word> sortWordsAccordingToRanking(List<Word> wordsList, InputQuery mInputQuery, String language) {

        if (wordsList == null || wordsList.size()==0) return new ArrayList<>();

        List<long[]> matchingWordIndexesAndLengths = new ArrayList<>();
        boolean queryIsVerbWithTo = mInputQuery.isVerbWithTo();

        //region Replacing the Kana input word by its romaji equivalent
        String inputQuery = mInputQuery.getOriginal();
        int inputTextType = mInputQuery.getOriginalType();
        if (inputTextType == Globals.TEXT_TYPE_HIRAGANA || inputTextType == Globals.TEXT_TYPE_KATAKANA) {
            inputQuery = mInputQuery.getRomajiSingleElement();
        }
        //endregion

        for (int i = 0; i < wordsList.size(); i++) {

            Word currentWord = wordsList.get(i);
            if (currentWord==null) continue;

            int ranking = getRankingFromWordAttributes(currentWord, inputQuery, queryIsVerbWithTo, language);

            long[] currentMatchingWordIndexAndLength = new long[3];
            currentMatchingWordIndexAndLength[0] = i;
            currentMatchingWordIndexAndLength[1] = ranking;

            matchingWordIndexesAndLengths.add(currentMatchingWordIndexAndLength);
        }

        //Sort the results according to total length
        if (matchingWordIndexesAndLengths.size() != 0) {
            matchingWordIndexesAndLengths = UtilitiesGeneral.bubbleSortForThreeIntegerList(matchingWordIndexesAndLengths);
        }

        //Return the sorted list
        List<Word> sortedWordsList = new ArrayList<>();
        for (int i = 0; i < matchingWordIndexesAndLengths.size(); i++) {
            long sortedIndex = matchingWordIndexesAndLengths.get(i)[0];
            sortedWordsList.add(wordsList.get((int) sortedIndex));
        }

        return sortedWordsList;
    }

    @NotNull
    public static String cleanIdentifier(String string) {
        if (OvUtilsGeneral.isEmptyString(string)) return "";
        string = string.replaceAll("\\.", "*");
        string = string.replaceAll("#", "*");
        string = string.replaceAll("\\$", "*");
        string = string.replaceAll("\\[", "*");
        string = string.replaceAll("]", "*");
        //string = string.replaceAll("\\{","*");
        //string = string.replaceAll("}","*");
        return string;
    }

    @NotNull public static List<String[]> removeSpacesFromConjDb(@NotNull List<String[]> db) {
        List<String[]> newDb = new ArrayList<>();
        String[] currentItems;
        int length = db.get(0).length;
        for (int i = 0; i< db.size(); i++) {
            currentItems = new String[length];
            for (int j=0; j<length; j++) {
                currentItems[j] = (j<Globals.COLUMN_VERB_ISTEM)? db.get(i)[j] : db.get(i)[j].replace(" ", "");
            }
            newDb.add(currentItems);
        }
        return newDb;
    }

    @NotNull public static List<List<String>> getLatinKanjiWords(@NonNull @NotNull String[] words) {
        List<String> latinWords = new ArrayList<>();
        //List<String> kanaWords = new ArrayList<>();
        List<String> kanjiWords = new ArrayList<>();
        for (String word : words) {
            if (word.equals("")) continue;
            if (Globals.LATIN_CHAR_ALPHABET.contains(word.substring(0,1))) latinWords.add(word);
            //else if (Globals.KANA_CHAR_ALPHABET.contains(word.substring(0,1))) kanaWords.add(word);
            else kanjiWords.add(word);
        }
        List<List<String>> allWords = new ArrayList<>();
        allWords.add(latinWords);
        //allWords.add(kanaWords);
        allWords.add(kanjiWords);
        return allWords;
    }
}
