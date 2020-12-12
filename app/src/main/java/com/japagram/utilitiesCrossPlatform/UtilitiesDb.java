package com.japagram.utilitiesCrossPlatform;

import android.content.Context;

import com.japagram.data.ConjugationTitle;
import com.japagram.data.GenericIndex;
import com.japagram.data.IndexEnglish;
import com.japagram.data.IndexFrench;
import com.japagram.data.IndexKanji;
import com.japagram.data.IndexRomaji;
import com.japagram.data.IndexSpanish;
import com.japagram.data.InputQuery;
import com.japagram.data.Word;
import com.japagram.utilitiesPlatformOverridable.OverridableUtilitiesGeneral;
import com.japagram.utilitiesPlatformOverridable.OverridableUtilitiesDb;
import com.japagram.utilitiesPlatformOverridable.OverridableUtilitiesResources;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

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
            return OverridableUtilitiesGeneral.joinList(", ", totalMeaningElements);
        } else if (meanings.size() > balancePoint || balancePoint > 6) {
            for (Word.Meaning meaning : meanings) {
                totalMeaningElements = addMeaningElementsToListUpToMaxNumber(
                        totalMeaningElements, meaning.getMeaning(), 1);
            }
            return OverridableUtilitiesGeneral.joinList(", ", totalMeaningElements);
        } else return "";
    }

    @NotNull
    @Contract("_, _, _ -> param1")
    private static List<String> addMeaningElementsToListUpToMaxNumber(List<String> totalList, String meaning, int maxNumber) {
        String[] meaningElements = UtilitiesGeneral.splitAtCommasOutsideParentheses(meaning);
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

        if (type == Globals.TYPE_LATIN) {
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
        List<Word> matchingWordList = OverridableUtilitiesDb.getWordListByWordIds(matchingWordIdsFromIndex, context, db);
        //endregion

        //region Filtering the matches
        for (Word word : matchingWordList) {
            foundMatch = stringContainsItemFromList(word.getRomaji(), romajiQueries, true) ||
                    stringContainsItemFromList(word.getKanji(), kanjiQueries, false) ||
                    meaningsContainExactQueryMatch(nonJapQueries, word, language) ||
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
                keywords = OverridableUtilitiesGeneral.joinList(", ", keywordsList).toLowerCase();

                for (String latinQuery : latinQueries) {
                    if (keywords.contains(latinQuery)) {
                        foundMatch = true;
                        break;
                    }
                }
            }

            if (foundMatch) {
                matchingWordIds.add(word.getWordId());
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
        String meaningsString = OverridableUtilitiesGeneral.joinList(" ", meaningsPrepared);
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
        return OverridableUtilitiesGeneral.joinList(", ", meanings);
    }

    @NotNull
    public static List<Long> getAdjectivesFromConjugation(InputQuery query, Context context) {

        List<Long> matchingWordIds = new ArrayList<>();
        //Adding relevant adjectives to the list of matches if the input query is an adjective conjugation
        List<Long> matchingWordIdsFromIndex = getMatchingAdjectiveIdsWithLimits(query, context);
        if (matchingWordIdsFromIndex.size() == 0) return new ArrayList<>();

        List<Word> matchingPotentialAdjectives = OverridableUtilitiesDb.getWordListByWordIds(matchingWordIdsFromIndex, context, Globals.DB_CENTRAL);
        List<String> typesList;
        for (Word word : matchingPotentialAdjectives) {
            typesList = new ArrayList<>();
            for (Word.Meaning meaning : word.getMeaningsEN()) {
                typesList.add(meaning.getType());
            }
            typesList = Arrays.asList(OverridableUtilitiesGeneral.joinList(Globals.DB_ELEMENTS_DELIMITER, typesList).split(Globals.DB_ELEMENTS_DELIMITER));
            if (typesList.contains("Ai") || typesList.contains("Ana")) {
                if (!matchingWordIds.contains(word.getWordId())) matchingWordIds.add(word.getWordId());
            }
        }

        return matchingWordIds;
    }

    @NotNull
    public static List<Long> addCountersToMatchesList(@NotNull InputQuery query, Context context) {
        if (query.getSearchType() != Globals.TYPE_KANJI) return new ArrayList<>();
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
            OverridableUtilitiesGeneral.printLog(Globals.DEBUG_TAG, "WARNING: exceeded MAX_SQL_VARIABLES_FOR_QUERY in counter search, but prevented crash.");
        } else {
            List<Word> matchingPotentialCounters = OverridableUtilitiesDb.getWordListByWordIds(matchingWordIds, context, Globals.DB_CENTRAL);
            for (Word word : matchingPotentialCounters) {
                List<String> typesList = new ArrayList<>();
                for (Word.Meaning meaning : word.getMeaningsEN()) {
                    typesList.add(meaning.getType());
                }
                typesList = Arrays.asList(OverridableUtilitiesGeneral.joinList(Globals.DB_ELEMENTS_DELIMITER, typesList).split(Globals.DB_ELEMENTS_DELIMITER));
                if (typesList.contains("C")) {
                    if (!matchingWordIds.contains(word.getWordId())) matchingWordIds.add(word.getWordId());
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

        if (searchType == Globals.TYPE_LATIN) {

            //regon Getting the base adjectives
            for (String word : query.getSearchQueriesRomaji()) {
                length = word.length();
                if (length > 9) {
                    adjectiveConjugation = word.substring(length - 9);
                    baseAdjective = word.substring(0, length - 9) + "i";
                    if (adjectiveConjugation.equals("kunakatta")) isPotentialAdjective = true;
                }
                if (!isPotentialAdjective && length > 6) {
                    adjectiveConjugation = word.substring(length - 6);
                    baseAdjective = word.substring(0, length - 6) + "i";
                    if (adjectiveConjugation.equals("kereba")) isPotentialAdjective = true;
                }
                if (!isPotentialAdjective && length > 5) {
                    adjectiveConjugation = word.substring(length - 5);
                    baseAdjective = word.substring(0, length - 5) + "i";
                    if (adjectiveConjugation.equals("kunai")
                            || adjectiveConjugation.equals("katta")
                            || adjectiveConjugation.equals("karou")) isPotentialAdjective = true;
                }
                if (!isPotentialAdjective && length > 4) {
                    adjectiveConjugation = word.substring(length - 4);
                    baseAdjective = word.substring(0, length - 4) + "i";
                    if (adjectiveConjugation.equals("kute")) isPotentialAdjective = true;
                }
                if (!isPotentialAdjective && length > 2) {
                    adjectiveConjugation = word.substring(length - 2);
                    if (adjectiveConjugation.equals("mi") || adjectiveConjugation.equals("ku")) {
                        isPotentialAdjective = true;
                        baseAdjective = word.substring(0, length - 2) + "i";
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
                OverridableUtilitiesGeneral.printLog(Globals.DEBUG_TAG, "WARNING: exceeded MAX_SQL_VARIABLES_FOR_QUERY in adjectives search, but prevented crash.");
                return new ArrayList<>();
            }
            //endregion

        } else if (searchType == Globals.TYPE_KANJI) {

            for (String word : query.getSearchQueriesKanji()) {
                length = word.length();
                if (length > 5) {
                    adjectiveConjugation = word.substring(length - 5);
                    baseAdjective = word.substring(0, length - 5) + "い";
                    if (adjectiveConjugation.equals("くなかった")) isPotentialAdjective = true;
                }
                if (!isPotentialAdjective && length > 3) {
                    adjectiveConjugation = word.substring(word.length() - 3);
                    baseAdjective = word.substring(0, length - 3) + "い";
                    if (adjectiveConjugation.equals("くない")
                            || adjectiveConjugation.equals("ければ")
                            || adjectiveConjugation.equals("かった")
                            || adjectiveConjugation.equals("かろう")) isPotentialAdjective = true;
                }
                if (!isPotentialAdjective && length > 2) {
                    adjectiveConjugation = word.substring(length - 2);
                    baseAdjective = word.substring(0, length - 2) + "い";
                    if (adjectiveConjugation.equals("くて")) isPotentialAdjective = true;
                }
                if (!isPotentialAdjective && word.length() > 1) {
                    adjectiveConjugation = word.substring(length - 1);
                    if (adjectiveConjugation.equals("み") || adjectiveConjugation.equals("く")) {
                        isPotentialAdjective = true;
                        baseAdjective = word.substring(0, length - 1) + "";
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
                OverridableUtilitiesGeneral.printLog(Globals.DEBUG_TAG, "WARNING: exceeded MAX_SQL_VARIABLES_FOR_QUERY in adjectives search, but prevented crash.");
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

            if (inputTextType == Globals.TYPE_KANJI) {
                IndexKanji indexKanji = OverridableUtilitiesDb.getKanjiIndexForExactWord(concatenated_word, context);
                if (indexKanji != null) {
                    matchingIndices.addAll(Arrays.asList(indexKanji.getWordIds().split(Globals.DB_ELEMENTS_DELIMITER)));
                }
            } else {
                if (inputTextType == Globals.TYPE_HIRAGANA || inputTextType == Globals.TYPE_KATAKANA) {
                    concatenated_word = UtilitiesQuery.getWaapuroHiraganaKatakana(concatenated_word).get(Globals.TYPE_LATIN);
                }
                IndexRomaji indexRomaji = OverridableUtilitiesDb.getRomajiIndexForExactWord(concatenated_word, context);
                if (indexRomaji != null) {
                    matchingIndices.addAll(Arrays.asList(indexRomaji.getWordIds().split(Globals.DB_ELEMENTS_DELIMITER)));
                }
            }

        } else {
            if (inputTextType == Globals.TYPE_KANJI) {
                List<IndexKanji> indexesKanji = OverridableUtilitiesDb.getKanjiIndexesListForStartingWord(concatenated_word, context);
                if (indexesKanji != null && indexesKanji.size() > 0) {
                    for (IndexKanji indexKanji : indexesKanji) {
                        matchingIndices.addAll(Arrays.asList(indexKanji.getWordIds().split(Globals.DB_ELEMENTS_DELIMITER)));
                    }
                }
            } else {
                if (inputTextType == Globals.TYPE_HIRAGANA || inputTextType == Globals.TYPE_KATAKANA) {
                    concatenated_word = UtilitiesQuery.getWaapuroHiraganaKatakana(concatenated_word).get(Globals.TYPE_LATIN);
                }
                List<IndexRomaji> indexesRomaji = OverridableUtilitiesDb.getRomajiIndexesListForStartingWord(concatenated_word, context);
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
            List<IndexRomaji> indexes = OverridableUtilitiesDb.getRomajiIndexForExactWordsList(searchQueries, context, db);
            if (indexes != null && indexes.size() > 0) matchingIndices.addAll(indexes);

        } else {
            List<IndexRomaji> indexesRomaji = OverridableUtilitiesDb.getRomajiIndexesListForStartingWordsList(searchQueries, context, db);
            if (indexesRomaji != null && indexesRomaji.size() > 0) matchingIndices.addAll(indexesRomaji);
        }
        return matchingIndices;
    }

    public static List<IndexKanji> findQueryInKanjiIndex(List<String> searchQueries, boolean exactSearch, int db, Context context) {
        List<IndexKanji> matchingIndexKanjis;
        if (exactSearch) {
            //Preventing the index search from returning too many results and crashing the app
            matchingIndexKanjis = new ArrayList<>();
            List<IndexKanji> indexes = OverridableUtilitiesDb.getKanjiIndexForExactWordsList(searchQueries, context, db);
            if (indexes != null && indexes.size() > 0) matchingIndexKanjis.addAll(indexes); //Only add the index if the word was found in the index
        } else {
            matchingIndexKanjis = OverridableUtilitiesDb.getKanjiIndexesListForStartingWordsList(searchQueries, context, db);
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
        if (query.getSearchType() == Globals.TYPE_LATIN) {

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
        } else if (query.getSearchType() == Globals.TYPE_KANJI) {
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
                    List<IndexEnglish> indexesEnglish = OverridableUtilitiesDb.getEnglishIndexForExactWordsList(searchQueries, context, db);
                    if (indexesEnglish != null && indexesEnglish.size() > 0) matchingIndices.addAll(indexesEnglish); //Only add the index if the word was found in the index
                    break;
                }
                case Globals.LANG_STR_FR: {
                    List<IndexFrench> indexesFrench = OverridableUtilitiesDb.getFrenchIndexForExactWordsList(searchQueries, context, db);
                    if (indexesFrench != null && indexesFrench.size() > 0) matchingIndices.addAll(indexesFrench); //Only add the index if the word was found in the index
                    List<IndexEnglish> indexesEnglish = OverridableUtilitiesDb.getEnglishIndexForExactWordsList(searchQueries, context, db);
                    if (indexesEnglish != null && indexesEnglish.size() > 0) matchingIndices.addAll(indexesEnglish); //Only add the index if the word was found in the index
                    break;
                }
                case Globals.LANG_STR_ES: {
                    List<IndexSpanish> indexesSpanish = OverridableUtilitiesDb.getSpanishIndexForExactWordsList(searchQueries, context, db);
                    if (indexesSpanish != null && indexesSpanish.size() > 0) matchingIndices.addAll(indexesSpanish); //Only add the index if the word was found in the index
                    List<IndexEnglish> indexesEnglish = OverridableUtilitiesDb.getEnglishIndexForExactWordsList(searchQueries, context, db);
                    if (indexesEnglish != null && indexesEnglish.size() > 0) matchingIndices.addAll(indexesEnglish); //Only add the index if the word was found in the index
                    break;
                }
            }

        } else {
            switch (language) {
                case Globals.LANG_STR_EN: {
                    List<IndexEnglish> indexesEnglish = OverridableUtilitiesDb.getEnglishIndexesListForStartingWordsList(searchQueries, context, db);
                    if (indexesEnglish != null && indexesEnglish.size() > 0) matchingIndices.addAll(indexesEnglish);
                    break;
                }
                case Globals.LANG_STR_FR: {
                    List<IndexFrench> indexesFrench = OverridableUtilitiesDb.getFrenchIndexesListForStartingWordsList(searchQueries, context, db);
                    if (indexesFrench != null && indexesFrench.size() > 0) matchingIndices.addAll(indexesFrench);
                    List<IndexEnglish> indexesEnglish = OverridableUtilitiesDb.getEnglishIndexesListForStartingWordsList(searchQueries, context, db);
                    if (indexesEnglish != null && indexesEnglish.size() > 0) matchingIndices.addAll(indexesEnglish);
                    break;
                }
                case Globals.LANG_STR_ES: {
                    List<IndexSpanish> indexesSpanish = OverridableUtilitiesDb.getSpanishIndexesListForStartingWordsList(searchQueries, context, db);
                    if (indexesSpanish != null && indexesSpanish.size() > 0) matchingIndices.addAll(indexesSpanish);
                    List<IndexEnglish> indexesEnglish = OverridableUtilitiesDb.getEnglishIndexesListForStartingWordsList(searchQueries, context, db);
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
        if (queryType == Globals.TYPE_INVALID) return new Object[]{new ArrayList<>(), new ArrayList<>()};
        //endregion

        //region Getting the matches
        matchingWordIdsCentral = getNormalMatches(query, language, Globals.DB_CENTRAL, context);
        matchingWordIdsCentral.addAll(getAdjectivesFromConjugation(query, context));
        matchingWordIdsCentral.addAll(addCountersToMatchesList(query, context));
        matchingWordIdsCentral = UtilitiesGeneral.removeDuplicatesFromLongList(matchingWordIdsCentral);

        if (roomExtendedDbIsAvailable) matchingWordIdsExtended = getNormalMatches(query, language, Globals.DB_EXTENDED, context);

        if (showNames && roomNamesDatabasesFinishedLoading && roomNamesDatabaseIsAvailable) {
            matchingWordIdsNames = addNamesToMatchesList(query, context);
        }
        //endregion

        return new Object[]{matchingWordIdsCentral, matchingWordIdsExtended, matchingWordIdsNames};
    }

    @NotNull
    public static List<Word> getMergedWordsList(@NotNull List<Word> localWords, List<Word> asyncWords, String languageCode) {

        List<Word> finalWordsList = new ArrayList<>();
        List<Word> finalAsyncWords = new ArrayList<>(asyncWords);
        boolean asyncMeaningFoundLocally;

        for (int j = 0; j < localWords.size(); j++) {
            Word currentLocalWord = localWords.get(j);
            Word finalWord = new Word();

            //Copying basic properties
            finalWord.setRomaji(currentLocalWord.getRomaji());
            finalWord.setKanji(currentLocalWord.getKanji());
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
            if (OverridableUtilitiesGeneral.isEmptyString(currentLocalWord.getAltSpellings())) finalAltSpellings = new ArrayList<>();
            else {
                finalAltSpellings = new ArrayList<>(Arrays.asList(currentLocalWord.getAltSpellings().split(",")));
                String element;
                for (int i = 0; i < finalAltSpellings.size(); i++) {
                    element = finalAltSpellings.get(i).trim();
                    finalAltSpellings.set(i, element);
                }
            }
            finalWord.setAltSpellings(OverridableUtilitiesGeneral.joinList(", ", finalAltSpellings));

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
                    String finalAsyncWordAltSpellings = finalAsyncWords.get(currentIndex).getAltSpellings();
                    if (!OverridableUtilitiesGeneral.isEmptyString(finalAsyncWordAltSpellings)) {
                        for (String altSpelling : finalAsyncWordAltSpellings.split(",")) {
                            if (!finalAltSpellings.contains(altSpelling.trim())) {
                                finalAltSpellings.add(altSpelling.trim());
                            }
                        }
                    }
                    finalWord.setAltSpellings(OverridableUtilitiesGeneral.joinList(", ", finalAltSpellings));

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
                    if (!OverridableUtilitiesGeneral.isEmptyString(asyncWord.getAltSpellings())) {
                        if (OverridableUtilitiesGeneral.isEmptyString(localWord.getAltSpellings())) altSpellingNotFoundInLocalWord = true;
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

        int ranking;
        String romaji_value = currentWord.getRomaji();
        String kanji_value = currentWord.getKanji();
        String altSpellings_value = currentWord.getAltSpellings();
        String kwJap_value = currentWord.getExtraKeywordsJAP() == null ? "" : currentWord.getExtraKeywordsJAP();
        String kwLat_value = "";
        String trimmedValue;
        String type = currentWord.getMeaningsEN().get(0).getType();
        boolean currentWordIsAVerb = type.length() > 0 && type.startsWith("V") && !type.equals("VC") && !type.equals("NV");

        int EXACT_MEANING_MATCH_BONUS = 1000;
        int EXACT_WORD_MATCH_BONUS = 500;
        int WORD_MATCH_IN_SENTENCE_BONUS = 300;
        int WORD_MATCH_IN_PARENTHESES_BONUS = 50;
        int LATE_HIT_IN_SENTENCE_PENALTY = 25;
        int LATE_HIT_IN_MEANING_ELEMENTS_PENALTY = 50;
        int LATE_MEANING_MATCH_PENALTY = 100;

        // Getting ranking according to meaning string length
        // with penalties depending on the lateness of the word in the meanings
        // and the exactness of the match
        List<Word.Meaning> currentMeanings = currentWord.getMeaningsEN();
        if (currentMeanings == null) return 0;
        switch (language) {
            case Globals.LANG_STR_EN:
                currentMeanings = currentWord.getMeaningsEN();
                kwLat_value = currentWord.getExtraKeywordsEN() == null ? "" : currentWord.getExtraKeywordsEN();
                break;
            case Globals.LANG_STR_FR:
                currentMeanings = currentWord.getMeaningsFR();
                kwLat_value = currentWord.getExtraKeywordsFR() == null ? "" : currentWord.getExtraKeywordsFR();
                break;
            case Globals.LANG_STR_ES:
                currentMeanings = currentWord.getMeaningsES();
                kwLat_value = currentWord.getExtraKeywordsES() == null ? "" : currentWord.getExtraKeywordsES();
                break;
        }
        int missingLanguagePenalty = 0;
        if (currentMeanings == null || currentMeanings.size() == 0) {
            missingLanguagePenalty = 10000;
            currentMeanings = currentWord.getMeaningsEN();
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
                String[] currentMeaningIndividualElements = UtilitiesGeneral.splitAtCommasOutsideParentheses(currentMeaning);
                lateHitInMeaningPenalty = 0;
                cumulativeMeaningLength = 0;
                if (currentMeaning.equals(inputQuery)) {
                    ranking -= EXACT_MEANING_MATCH_BONUS;
                    foundMeaningLength = true;
                } else {
                    for (String currentMeaningElement : currentMeaningIndividualElements) {
                        String trimmedElement = currentMeaningElement.trim().toLowerCase();

                        //If there's an exact match, push the word up in ranking
                        if (trimmedElement.equals(inputQuery)) {
                            ranking -= EXACT_WORD_MATCH_BONUS;
                            foundMeaningLength = true;
                        }
                        if (foundMeaningLength) break;

                        String[] currentMeaningIndividualWords = trimmedElement.split(" ");
                        for (String word : currentMeaningIndividualWords) {
                            cumulativeMeaningLength += word.length() + 2; //Added 2 to account for missing ", " instances in loop
                            if (word.equals(inputQuery)) {
                                ranking -= WORD_MATCH_IN_SENTENCE_BONUS;
                                foundMeaningLength = true;
                                break;
                            }
                            lateHitInMeaningPenalty += LATE_HIT_IN_SENTENCE_PENALTY;
                        }
                        if (foundMeaningLength) break;

                        //If meaning has the exact word but maybe in parentheses, get the length as follows
                        String preparedTrimmedElement = trimmedElement.replaceAll("([()])", "");
                        String[] currentMeaningIndividualWordsWithoutParentheses = preparedTrimmedElement.split(" ");
                        for (String word : currentMeaningIndividualWordsWithoutParentheses) {
                            cumulativeMeaningLength += word.length() + 2; //Added 2 to account for missing ", " instances in loop
                            if (word.equals(inputQuery)) {
                                ranking -= WORD_MATCH_IN_PARENTHESES_BONUS;
                                foundMeaningLength = true;
                                break;
                            }
                            lateHitInMeaningPenalty += LATE_HIT_IN_SENTENCE_PENALTY;
                        }
                        if (foundMeaningLength) break;

                        lateHitInMeaningPenalty += LATE_HIT_IN_MEANING_ELEMENTS_PENALTY;
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

                String[] currentMeaningIndividualElements = UtilitiesGeneral.splitAtCommasOutsideParentheses(currentMeaning);
                if (!queryIsVerbWithTo) {

                    //Calculate the length first by adding "to " to the input query. If it leads to a hit, that means that this verb is relevant
                    inputQuery = "to " + mInputQuery;

                    lateHitInMeaningPenalty = 0;
                    cumulativeMeaningLength = 0;
                    if (currentMeaning.equals(inputQuery)) {
                        ranking -= EXACT_MEANING_MATCH_BONUS;
                        foundMeaningLength = true;
                    } else {
                        for (String element : currentMeaningIndividualElements) {

                            String trimmedElement = element.trim();

                            //If there's an exact match, push the word up in ranking
                            if (trimmedElement.equals(inputQuery)) {
                                ranking -= EXACT_WORD_MATCH_BONUS;
                                foundMeaningLength = true;
                            }
                            if (foundMeaningLength) break;

                            cumulativeMeaningLength += element.length() + 2; //Added 2 to account for missing ", " instances in loop
                            if (trimmedElement.contains(inputQuery)) {
                                ranking -= WORD_MATCH_IN_SENTENCE_BONUS;
                                foundMeaningLength = true;
                                break;
                            }
                            lateHitInMeaningPenalty += LATE_HIT_IN_SENTENCE_PENALTY;
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
                        ranking -= EXACT_MEANING_MATCH_BONUS;
                        foundMeaningLength = true;
                    } else {
                        for (String element : currentMeaningIndividualElements) {

                            String trimmedElement = element.trim();

                            //If there's an exact match, push the word up in ranking
                            if (trimmedElement.equals(inputQuery)) {
                                ranking -= EXACT_WORD_MATCH_BONUS;
                                foundMeaningLength = true;
                            }
                            if (foundMeaningLength) break;

                            if (trimmedElement.contains(inputQuery)) {
                                ranking -= WORD_MATCH_IN_SENTENCE_BONUS;
                                foundMeaningLength = true;
                                break;
                            }
                            cumulativeMeaningLength += element.length() + 2; //Added 2 to account for missing ", " instances in loop
                            lateHitInMeaningPenalty += LATE_HIT_IN_SENTENCE_PENALTY;
                        }
                    }
                } else {
                    inputQuery = mInputQuery;

                    //Get the length according to the position of the verb in the meanings list
                    lateHitInMeaningPenalty = 0;
                    cumulativeMeaningLength = 0;
                    if (currentMeaning.equals(inputQuery)) {
                        ranking -= EXACT_MEANING_MATCH_BONUS;
                        foundMeaningLength = true;
                    } else {
                        for (String element : currentMeaningIndividualElements) {

                            String trimmedElement = element.trim();

                            //If there's an exact match, push the word up in ranking
                            if (trimmedElement.equals(inputQuery)) {
                                ranking -= EXACT_WORD_MATCH_BONUS;
                                foundMeaningLength = true;
                            }
                            if (foundMeaningLength) break;

                            cumulativeMeaningLength += element.length() + 2; //Added 2 to account for missing ", " instances in loop
                            if (trimmedElement.contains(inputQuery)) {
                                ranking -= WORD_MATCH_IN_SENTENCE_BONUS;
                                foundMeaningLength = true;
                                break;
                            }
                            lateHitInMeaningPenalty += LATE_HIT_IN_SENTENCE_PENALTY;
                        }
                    }
                }

                if (foundMeaningLength) {
                    ranking += lateMeaningPenalty + lateHitInMeaningPenalty + cumulativeMeaningLength;
                    break;
                }

            }
            //endregion

            lateMeaningPenalty += LATE_MEANING_MATCH_PENALTY;

        }

        //Adding the romaji and kanji lengths to the ranking (ie. shorter is better)
        ranking += 2 * (romaji_value.length() + kanji_value.length());

        //If the word starts with the inputQuery, its ranking improves
        String romajiNoSpaces = getRomajiNoSpacesForSpecialPartsOfSpeech(romaji_value);
        if (       (romaji_value.length() >= mInputQuery.length()     && romaji_value.substring(0, mInputQuery.length()    ).equals(mInputQuery))
                || (kanji_value.length() >= mInputQuery.length()      && kanji_value.substring(0, mInputQuery.length()     ).equals(mInputQuery))
                || romajiNoSpaces.equals(mInputQuery)
        ) {
            ranking -= 1000;
        }

        //Otherwise, if the romaji or Kanji value contains the search word, then it must appear near the start of the list
        else if (romaji_value.contains(mInputQuery) || kanji_value.contains(mInputQuery)) ranking -= 300;

        //If the word is a name, the ranking worsens
        Word.Meaning wordFirstMeaning = currentWord.getMeaningsEN().get(0);
        if (Globals.NAMES_LIST.contains(wordFirstMeaning.getType())) ranking += 5000;

        //If the word is common, the ranking improves
        if (currentWord.getIsCommon()) ranking -= 50;

        //If the word is a verb and one of its conjugations is a perfect match, the ranking improves on a level similar to meaning bonuses
        if (currentWord.getVerbConjMatchStatus() == Word.CONJ_MATCH_EXACT) ranking -= 1400;

        //If the word is a verb and one of its conjugations is a partial match, the ranking improves a bit less than for perfect matches
        if (currentWord.getVerbConjMatchStatus() == Word.CONJ_MATCH_CONTAINED) ranking -= 1000;

        //If one of the elements in altSpellings is a perfect match, the ranking improves
        for (String element : altSpellings_value.split(Globals.DB_ELEMENTS_DELIMITER)) {
            trimmedValue = element.trim();
            if (mInputQuery.equals(trimmedValue)) {
                ranking -= 70;
                break;
            }
        }

        //If one of the elements in the Japanese Keywords is a perfect match, the ranking improves
        for (String element : kwJap_value.split(",")) {
            trimmedValue = element.trim();
            if (mInputQuery.equals(trimmedValue)) {
                ranking -= 70;
                break;
            }
        }

        //If one of the elements in the Latin Keywords is a perfect match, the ranking improves
        for (String element : kwLat_value.split(",")) {
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
        if (romaji_value.equals(mInputQuery) || kanji_value.equals(mInputQuery)) ranking = 0;
        else {
            String cleanValue = romaji_value.replace(" ", "");
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
    public static List<ConjugationTitle> getConjugationTitles(@NotNull List<String[]> verbLatinConjDatabase, Context context) {

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
                conjugationTitle.setTitle(OverridableUtilitiesResources.getString(titleRef, context, Globals.RESOURCE_MAP_VERB_CONJ_TITLES));
                conjugationTitle.setTitleIndex(col);

                subtitle = new ConjugationTitle.Subtitle();
                subtitleRef = Globals.VERB_CONJUGATION_TITLES.get(subtitlesRow[col]);
                subtitle.setSubtitle(OverridableUtilitiesResources.getString(subtitleRef, context, Globals.RESOURCE_MAP_VERB_CONJ_TITLES));
                subtitle.setSubtitleIndex(col);
                subtitles.add(subtitle);
            } else if (col == sheetLength - 1) {
               subtitle = new ConjugationTitle.Subtitle();
                subtitleRef = Globals.VERB_CONJUGATION_TITLES.get(subtitlesRow[col]);
                subtitle.setSubtitle(OverridableUtilitiesResources.getString(subtitleRef, context, Globals.RESOURCE_MAP_VERB_CONJ_TITLES));
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
                    conjugationTitle.setTitle(OverridableUtilitiesResources.getString(titleRef, context, Globals.RESOURCE_MAP_VERB_CONJ_TITLES));
                    conjugationTitle.setTitleIndex(col);

                }

                subtitle = new ConjugationTitle.Subtitle();
                subtitleRef = Globals.VERB_CONJUGATION_TITLES.get(subtitlesRow[col]);
                subtitle.setSubtitle(OverridableUtilitiesResources.getString(subtitleRef, context, Globals.RESOURCE_MAP_VERB_CONJ_TITLES));
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
            for (int j = 0; j < Globals.SIMILARS_DATABASE.size(); j++) {
                String char0 = Globals.SIMILARS_DATABASE.get(j)[0].substring(0, 1);
                if (Globals.SIMILARS_DATABASE.get(j).length > 0 && char0.equals(currentChar)) {
                    String char1 = Globals.SIMILARS_DATABASE.get(j)[1].substring(0, 1);
                    output.add(char1);
                    found = true;
                    break;
                }
            }
            if (!found) output.add(currentChar);
        }
        return OverridableUtilitiesGeneral.joinList(" ", output);
    }
}
