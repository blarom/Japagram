package com.japagram.resources;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;
import com.japagram.BuildConfig;
import com.japagram.data.ConjugationTitle;
import com.japagram.data.IndexEnglish;
import com.japagram.data.IndexFrench;
import com.japagram.data.IndexKanji;
import com.japagram.data.IndexRomaji;
import com.japagram.data.IndexSpanish;
import com.japagram.data.InputQuery;
import com.japagram.data.RoomCentralDatabase;
import com.japagram.data.RoomExtendedDatabase;
import com.japagram.data.RoomNamesDatabase;
import com.japagram.data.Verb;
import com.japagram.data.Word;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import androidx.appcompat.widget.ActivityChooserView;

public class UtilitiesDb {
    private static final int WORD_SEARCH_CHAR_COUNT_THRESHOLD = 3;
    private static FirebaseDatabase mDatabase;
    public static final String firebaseEmail = BuildConfig.firebaseEmail;
    public static final String firebasePass = BuildConfig.firebasePass;
    //Database operations utilities
    public static List<Word> getMergedWordsList(List<Word> localWords, List<Word> asyncWords, String languageCode) {

        List<Word> finalWordsList = new ArrayList<>();
        List<Word> finalAsyncWords = new ArrayList<>(asyncWords);
        boolean asyncMeaningFoundLocally;

        for (int j = 0; j < localWords.size(); j++) {
            Word currentLocalWord = localWords.get(j);
            Word finalWord = new Word();

            //Copying basic properties
            finalWord.setRomaji(currentLocalWord.getRomaji());
            finalWord.setKanji(currentLocalWord.getKanji());
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
            if (TextUtils.isEmpty(currentLocalWord.getAltSpellings())) finalAltSpellings = new ArrayList<>();
            else {
                finalAltSpellings = new ArrayList<>(Arrays.asList(currentLocalWord.getAltSpellings().split(",")));
                for (int i = 0; i < finalAltSpellings.size(); i++) finalAltSpellings.set(i, finalAltSpellings.get(i).trim());
            }
            finalWord.setAltSpellings(TextUtils.join(", ", finalAltSpellings));

            //Updating and copying meanings/alt spellings from the async word
            List<Word.Meaning> currentLocalMeanings = currentLocalWord.getMeaningsEN();
            List<Word.Meaning> currentFinalMeanings = new ArrayList<>(currentLocalMeanings);

            int currentIndex = finalAsyncWords.size() - 1;
            while (currentIndex >= 0 && finalAsyncWords.size() != 0) {

                if (currentIndex > finalAsyncWords.size() - 1) break;
                Word currentAsyncWord = finalAsyncWords.get(currentIndex);

                if (currentAsyncWord.getRomaji().replace(" ", "")
                        .equals(currentLocalWord.getRomaji().replace(" ", ""))
                        && currentAsyncWord.getKanji().equals(currentLocalWord.getKanji())) {

                    //Setting the altSpellings
                    String finalAsyncWordAltSpellings = finalAsyncWords.get(currentIndex).getAltSpellings();
                    if (!TextUtils.isEmpty(finalAsyncWordAltSpellings)) {
                        for (String altSpelling : finalAsyncWordAltSpellings.split(",")) {
                            if (!finalAltSpellings.contains(altSpelling.trim())) {
                                finalAltSpellings.add(altSpelling.trim());
                            }
                        }
                    }
                    finalWord.setAltSpellings(TextUtils.join(", ", finalAltSpellings));

                    //Setting the meanings
                    List<Word.Meaning> currentAsyncMeanings = currentAsyncWord.getMeaningsEN();
                    for (int m = 0; m < currentAsyncMeanings.size(); m++) {

                        asyncMeaningFoundLocally = false;
                        for (int k = 0; k < currentLocalMeanings.size(); k++) {

                            if (currentLocalMeanings.get(k).getMeaning()
                                    .contains(currentAsyncMeanings.get(m).getMeaning())) {
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

    public static List<Word> getDifferentAsyncWords(List<Word> localWords, List<Word> asyncWords) {

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
                    if (!TextUtils.isEmpty(asyncWord.getAltSpellings())) {
                        if (TextUtils.isEmpty(localWord.getAltSpellings())) altSpellingNotFoundInLocalWord = true;
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

    public static List<Word> getCommonWords(List<Word> wordsList) {
        List<Word> commonWords = new ArrayList<>();
        for (Word word : wordsList) {
            if (word.getIsCommon()) commonWords.add(word);
        }
        return commonWords;
    }

    public static List<long[]> bubbleSortForThreeIntegerList(List<long[]> MatchList) {

        // Sorting the results according to the shortest keyword as found in the above search

        // Computing the value length
        int list_size = MatchList.size();
        long[][] matches = new long[list_size][3];
        for (int i = 0; i < list_size; i++) {
            matches[i][0] = MatchList.get(i)[0];
            matches[i][1] = MatchList.get(i)[1];
            matches[i][2] = MatchList.get(i)[2];
        }

        // Sorting
        long tempVar0;
        long tempVar1;
        long tempVar2;
        for (int i = 0; i < list_size; i++) { //Bubble sort
            for (int t = 1; t < list_size - i; t++) {
                if (matches[t - 1][1] > matches[t][1]) {
                    tempVar0 = matches[t - 1][0];
                    tempVar1 = matches[t - 1][1];
                    tempVar2 = matches[t - 1][2];
                    matches[t - 1][0] = matches[t][0];
                    matches[t - 1][1] = matches[t][1];
                    matches[t - 1][2] = matches[t][2];
                    matches[t][0] = tempVar0;
                    matches[t][1] = tempVar1;
                    matches[t][2] = tempVar2;
                }
            }
        }

        List<long[]> sortedMatchList = new ArrayList<>();
        long[] element;
        for (int i = 0; i < list_size; i++) {
            element = new long[3];
            element[0] = matches[i][0];
            element[1] = matches[i][1];
            element[2] = matches[i][2];
            sortedMatchList.add(element);
        }

        return sortedMatchList;
    }

    public static int getRankingFromWordAttributes(Word currentWord, String mInputQuery, String queryWordWithoutTo, boolean queryIsVerbWithTo, String language) {

        String inputQueryLatin = InputQuery.getWaapuroHiraganaKatakana(mInputQuery).get(Globals.TYPE_LATIN);
        int ranking;
        String romaji_value = currentWord.getRomaji();
        String kanji_value = currentWord.getKanji();
        String altSpellings_value = currentWord.getAltSpellings();
        String kwJap_value = currentWord.getExtraKeywordsJAP() == null ? "" : currentWord.getExtraKeywordsJAP();
        String kwLat_value = "";
        String type = currentWord.getMeaningsEN().get(0).getType();
        boolean currentWordIsAVerb = type.length() > 0 && type.substring(0, 1).equals("V") && !type.equals("VC") && !type.equals("NV");

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
        switch (language) {
            case "en":
                currentMeanings = currentWord.getMeaningsEN();
                kwLat_value = currentWord.getExtraKeywordsEN() == null ? "" : currentWord.getExtraKeywordsEN();
                break;
            case "fr":
                currentMeanings = currentWord.getMeaningsFR();
                kwLat_value = currentWord.getExtraKeywordsFR() == null ? "" : currentWord.getExtraKeywordsFR();
                break;
            case "es":
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

            //region If the current word is not a verb
            if (!currentWordIsAVerb) {
                foundMeaningLength = false;
                inputQuery = mInputQuery;

                //If meaning has the exact word, get the length as follows
                if (currentMeaning.contains("three flat objects")) {
                    currentMeaning = currentMeanings.get(j).getMeaning();
                }
                String[] currentMeaningIndividualElements = Utilities.splitAtCommasOutsideParentheses(currentMeaning);
                lateHitInMeaningPenalty = 0;
                cumulativeMeaningLength = 0;
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
                    String[] currentMeaningIndividualWordsWithoutParentheses = trimmedElement.replace("(", "").replace(")", "").split(" ");
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
                foundMeaningLength = false;

                String[] currentMeaningIndividualElements = Utilities.splitAtCommasOutsideParentheses(currentMeaning);
                if (!queryIsVerbWithTo) {

                    //Calculate the length first by adding "to " to the input query. If it leads to a hit, that means that this verb is relevant
                    inputQuery = "to " + mInputQuery;

                    lateHitInMeaningPenalty = 0;
                    cumulativeMeaningLength = 0;
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
                    if (foundMeaningLength) {
                        ranking += lateMeaningPenalty + lateHitInMeaningPenalty + cumulativeMeaningLength;
                        break;
                    }

                    //Otherwise, use the original query to get the length
                    inputQuery = mInputQuery;
                    lateHitInMeaningPenalty = 0;
                    cumulativeMeaningLength = 0;
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
                    if (foundMeaningLength) {
                        ranking += lateMeaningPenalty + lateHitInMeaningPenalty + cumulativeMeaningLength;
                        break;
                    }
                } else {
                    inputQuery = mInputQuery;

                    //Get the length according to the position of the verb in the meanings list
                    lateHitInMeaningPenalty = 0;
                    cumulativeMeaningLength = 0;
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
                    if (foundMeaningLength) {
                        ranking += lateMeaningPenalty + lateHitInMeaningPenalty + cumulativeMeaningLength;
                        break;
                    }
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
                || (romaji_value.length() >= inputQueryLatin.length() && romaji_value.substring(0, inputQueryLatin.length()).equals(inputQueryLatin))
                || (kanji_value.length() >= mInputQuery.length()      && kanji_value.substring(0, mInputQuery.length()     ).equals(mInputQuery))
                || romajiNoSpaces.equals(mInputQuery) || romajiNoSpaces.equals(inputQueryLatin)
        ) {
            ranking -= 1000;
        }

        //Otherwise, if the romaji or Kanji value contains the search word, then it must appear near the start of the list
        else if (romaji_value.contains(mInputQuery) || kanji_value.contains(mInputQuery)) ranking -= 300;

        //If the word is a name, the ranking worsens
        if (Globals.NAMES_LIST.contains(currentWord.getMeaningsEN().get(0).getType())) ranking += 5000;

        //If the word is common, the ranking improves
        if (currentWord.getIsCommon()) ranking -= 50;

        //If the word is a verb and one of its conjugations is a perfect match, the ranking improves on a level similar to meaning bonuses
        if (currentWord.getVerbConjMatchStatus() == Word.CONJ_MATCH_EXACT) ranking -= 1400;

        //If the word is a verb and one of its conjugations is a partial match, the ranking improves a bit less than for perfect matches
        if (currentWord.getVerbConjMatchStatus() == Word.CONJ_MATCH_CONTAINED) ranking -= 1000;

        //If one of the elements in altSpellings is a perfect match, the ranking improves
        for (String element : altSpellings_value.split(",")) {
            if (mInputQuery.equals(element.trim()) || inputQueryLatin.equals(element.trim())) {
                ranking -= 70;
                break;
            }
        }

        //If one of the elements in the Japanese Keywords is a perfect match, the ranking improves
        for (String element : kwJap_value.split(",")) {
            if (mInputQuery.equals(element.trim()) || inputQueryLatin.equals(element.trim())) {
                ranking -= 70;
                break;
            }
        }

        //If one of the elements in the Latin Keywords is a perfect match, the ranking improves
        for (String element : kwLat_value.split(",")) {
            if (mInputQuery.equals(element.trim()) || inputQueryLatin.equals(element.trim())) {
                ranking -= 40;
                break;
            }
        }

        //If the romaji or Kanji value is an exact match to the search word, then it must appear at the start of the list
        if (romaji_value.equals(mInputQuery) || kanji_value.equals(mInputQuery)) ranking = 0;

        ranking += missingLanguagePenalty;

        return ranking;
    }

    public static Object[] getMatchingWordIdsAndDoBasicFiltering(InputQuery query, String language, boolean showNames, Context context) {

        //region Initializations
        List<Long> matchingWordIdsCentral = new ArrayList<>();
        List<Long> matchingWordIdsExtended = new ArrayList<>();
        List<Long> matchingWordIdsNames = new ArrayList<>();
        int queryType = query.getType();
        if (queryType == Globals.TYPE_INVALID) return new Object[]{new ArrayList<>(), new ArrayList<>()};
        //endregion

        //region Getting the matches
        matchingWordIdsCentral = addNormalMatchesToMatchesList(query, matchingWordIdsCentral, language, false, context);

        matchingWordIdsCentral = addConjugatedAdjectivesToMatchesList(query, matchingWordIdsCentral, context);
        matchingWordIdsCentral = addCountersToMatchesList(query, matchingWordIdsCentral, context);

        if (RoomExtendedDatabase.getInstance(context) != null)
            matchingWordIdsExtended = addNormalMatchesToMatchesList(query, new ArrayList<>(), language, true, context);

        if (RoomNamesDatabase.getInstance(context) != null && showNames) {
            matchingWordIdsNames = addNamesToMatchesList(query, context);
        }

        //endregion

        return new Object[]{matchingWordIdsCentral, matchingWordIdsExtended, matchingWordIdsNames};
    }

    private static List<Long> getMatchingWordIdsForOriginalInputQuery(boolean forceExactSearch, InputQuery query, String language, boolean use_extended_db, Context context) {

        String searchWord = query.getOriginalCleaned();
        int inputTextType = query.getType();
        List<String> possibleInterpretations = query.getWaapuroConversions();

        List<Long> matchingWordIds = new ArrayList<>();
        String searchWordNoSpaces = searchWord.replaceAll("\\s","");

        //region Search for the matches in the indexed keywords
        List<String> searchResultIndexesArray = new ArrayList<>();
        List<Object> latinIndices;
        List<IndexKanji> kanjiIndices;
        if (inputTextType == Globals.TYPE_LATIN || inputTextType == Globals.TYPE_HIRAGANA
                || inputTextType == Globals.TYPE_KATAKANA || inputTextType == Globals.TYPE_NUMBER) {

            //If the input is a verb in "to " form, remove the "to " for the search only (results will be filtered later on)
            String inputWord = Utilities.removeNonSpaceSpecialCharacters(searchWord);
            if (searchWord.length() > 3) {
                if (searchWord.substring(0, 3).equals("to ")) {
                    inputWord = searchWordNoSpaces.substring(2);
                }
            }
            String inputWordNoSpaces = Utilities.removeSpecialCharacters(inputWord);

            boolean exactSearch = inputWordNoSpaces.length() < 3 || forceExactSearch;
            latinIndices = findQueryInLatinIndices(inputWordNoSpaces, possibleInterpretations, exactSearch, new String[]{"romaji", language}, use_extended_db, context);

            if (latinIndices.size() == 0) return matchingWordIds;

            // If the entered word is Latin and only has up to WORD_SEARCH_CHAR_COUNT_THRESHOLD characters, limit the word keywords to be checked later
            if (((inputTextType == Globals.TYPE_LATIN
                    || inputTextType == Globals.TYPE_HIRAGANA
                    || inputTextType == Globals.TYPE_KATAKANA)
                    && searchWordNoSpaces.length() < WORD_SEARCH_CHAR_COUNT_THRESHOLD)
                    || (inputTextType == Globals.TYPE_NUMBER
                    && searchWordNoSpaces.length() < WORD_SEARCH_CHAR_COUNT_THRESHOLD - 1)) {

                for (Object indexLatin : latinIndices) {
                    if (indexLatin instanceof IndexRomaji && ((IndexRomaji) indexLatin).getValue().length() < WORD_SEARCH_CHAR_COUNT_THRESHOLD) {
                        searchResultIndexesArray.add(((IndexRomaji) indexLatin).getWordIds());
                        break;
                    } else if (indexLatin instanceof IndexEnglish && ((IndexEnglish) indexLatin).getValue().length() < WORD_SEARCH_CHAR_COUNT_THRESHOLD) {
                        searchResultIndexesArray.add(((IndexEnglish) indexLatin).getWordIds());
                        break;
                    } else if (indexLatin instanceof IndexFrench && ((IndexFrench) indexLatin).getValue().length() < WORD_SEARCH_CHAR_COUNT_THRESHOLD) {
                        searchResultIndexesArray.add(((IndexFrench) indexLatin).getWordIds());
                        break;
                    } else if (indexLatin instanceof IndexSpanish && ((IndexSpanish) indexLatin).getValue().length() < WORD_SEARCH_CHAR_COUNT_THRESHOLD) {
                        searchResultIndexesArray.add(((IndexSpanish) indexLatin).getWordIds());
                        break;
                    }
                }
            } else {
                for (Object indexLatin : latinIndices) {
                    if (indexLatin instanceof IndexRomaji) searchResultIndexesArray.add(((IndexRomaji) indexLatin).getWordIds());
                    else if (indexLatin instanceof IndexEnglish) searchResultIndexesArray.add(((IndexEnglish) indexLatin).getWordIds());
                    else if (indexLatin instanceof IndexFrench) searchResultIndexesArray.add(((IndexFrench) indexLatin).getWordIds());
                    else if (indexLatin instanceof IndexSpanish) searchResultIndexesArray.add(((IndexSpanish) indexLatin).getWordIds());
                }
            }

        } else if (inputTextType == Globals.TYPE_KANJI) {
            kanjiIndices = findQueryInKanjiIndex(searchWordNoSpaces, forceExactSearch, use_extended_db, context);
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
                matchingWordIds.add(Long.valueOf(indexList.get(j)));
            }
        }
        //endregion

        return matchingWordIds;
    }

    private static List<Long> getMatchingWordIdsForQueryWithoutExtraIng(boolean forceExactSearch, List<Long> matchingWordIdsFromIndex, String inglessVerb, boolean use_extended_db, Context context) {

        List<Long> newMatchingWordIdsFromIndex = new ArrayList<>(matchingWordIdsFromIndex);

        boolean exactSearch = inglessVerb.length() < 3 || forceExactSearch;
        List<Object> latinIndices = findQueryInLatinIndices(inglessVerb, new ArrayList<>(), exactSearch, new String[]{Globals.LANG_STR_EN}, use_extended_db, context);

        for (Object indexLatin : latinIndices) {
            List<String> indexList = new ArrayList<>();
            if (indexLatin instanceof IndexRomaji) indexList = Arrays.asList(((IndexRomaji) indexLatin).getWordIds().split(Globals.DB_ELEMENTS_DELIMITER));
            else if (indexLatin instanceof IndexEnglish) indexList = Arrays.asList(((IndexEnglish) indexLatin).getWordIds().split(Globals.DB_ELEMENTS_DELIMITER));
            else if (indexLatin instanceof IndexFrench) indexList = Arrays.asList(((IndexFrench) indexLatin).getWordIds().split(Globals.DB_ELEMENTS_DELIMITER));
            else if (indexLatin instanceof IndexSpanish) indexList = Arrays.asList(((IndexSpanish) indexLatin).getWordIds().split(Globals.DB_ELEMENTS_DELIMITER));

            for (int j = 0; j < indexList.size(); j++) {
                newMatchingWordIdsFromIndex.add(Long.valueOf(indexList.get(j)));
            }
        }

        return newMatchingWordIdsFromIndex;
    }

    private static List<Long> getMatchingWordIds(InputQuery query, String language, boolean use_extended_db, Context context) {

        String searchWord = query.getOriginalCleaned();
        String inglessVerb = query.getIngless();
        int inputTextType = query.getType();

        List<Long> matchingWordIds = getMatchingWordIdsForOriginalInputQuery(false, query, language, use_extended_db, context);

        //If the number of matching ids is larger than MAX_SQL_VARIABLES_FOR_QUERY, perform an exact search
        if (matchingWordIds.size() > Globals.MAX_SQL_VARIABLES_FOR_QUERY) {
            matchingWordIds = getMatchingWordIdsForOriginalInputQuery(true, query, language, use_extended_db, context);
        }

        //If the number of matching ids is still larger than MAX_SQL_VARIABLES_FOR_QUERY, limit the list length to MAX_SQL_VARIABLES_FOR_QUERY
        if (matchingWordIds.size() > Globals.MAX_SQL_VARIABLES_FOR_QUERY) {
            matchingWordIds = matchingWordIds.subList(0, Globals.MAX_SQL_VARIABLES_FOR_QUERY);
        }

        //Adding search results where the "ing" is removed from an "ing" verb in english
        if (language.equals(Globals.LANG_STR_EN) && !inglessVerb.equals(searchWord)
                && (inputTextType == Globals.TYPE_LATIN || inputTextType == Globals.TYPE_HIRAGANA
                || inputTextType == Globals.TYPE_KATAKANA || inputTextType == Globals.TYPE_NUMBER)) {

            List<Long> newMatchingWordIds = getMatchingWordIdsForQueryWithoutExtraIng(false, matchingWordIds, inglessVerb, use_extended_db, context);

            if (matchingWordIds.size() + newMatchingWordIds.size() > Globals.MAX_SQL_VARIABLES_FOR_QUERY) {
                newMatchingWordIds = getMatchingWordIdsForQueryWithoutExtraIng(true, matchingWordIds, inglessVerb, use_extended_db, context);
            }

            if (matchingWordIds.size() + newMatchingWordIds.size() <= Globals.MAX_SQL_VARIABLES_FOR_QUERY) {
                matchingWordIds.addAll(newMatchingWordIds);
            }
        }

        //Removing duplicates while keeping the list order (https://stackoverflow.com/questions/19511797/remove-duplicates-in-an-array-without-changing-order-of-elements)
        matchingWordIds = new ArrayList<>(new LinkedHashSet<>(matchingWordIds));

        return matchingWordIds;
    }

    private static List<Long> addNormalMatchesToMatchesList(InputQuery query, List<Long> matchingWordIds, String language, boolean use_extended_db, Context context) {

        //region Initializations
        List<long[]> MatchList = new ArrayList<>();
        String keywords;
        long[] current_match_values;
        boolean foundMatch;
        boolean queryIsVerbWithTo = query.getIsVerbWithTo();
        String searchWordWithoutTo = query.getWithoutTo();
        String searchWord = query.getOriginalCleaned();
        String inglessVerb = query.getIngless();
        int inputTextType = query.getType();
        List<String> possibleInterpretations = query.getWaapuroConversions();
        //endregion

        List<Long> matchingWordIdsFromIndex = getMatchingWordIds(query, language, use_extended_db, context);

        //region Limiting the database query if there are too many results (prevents long query times)
        boolean onlyRetrieveShortRomajiWords = false;
        if ((inputTextType == Globals.TYPE_LATIN || inputTextType == Globals.TYPE_HIRAGANA
                || inputTextType == Globals.TYPE_KATAKANA) && searchWord.length() < 3) {
            onlyRetrieveShortRomajiWords = true;
        }
        //endregion

        //region Filtering the matches
        List<Word> matchingWordList = use_extended_db ? RoomExtendedDatabase.getInstance(context).getWordListByWordIds(matchingWordIdsFromIndex)
                : RoomCentralDatabase.getInstance(context).getWordListByWordIds(matchingWordIdsFromIndex);
        String romaji;
        String altSpellings;
        boolean isExactMeaningWordsMatch;
        boolean isRomajiMatch;
        boolean isAltSpellingsMatch;
        int searchWordLength = searchWord.length();
        int romajiLength;
        List<String> keywordsList;
        for (Word word : matchingWordList) {

            foundMatch = false;
            isRomajiMatch = false;
            isAltSpellingsMatch = false;

            //region Handling short words
            if ((inputTextType == Globals.TYPE_LATIN || inputTextType == Globals.TYPE_HIRAGANA
                    || inputTextType == Globals.TYPE_KATAKANA) && onlyRetrieveShortRomajiWords) {

                //Checking if the word is an exact match to one of the words in the meanings
                isExactMeaningWordsMatch = getMeaningsContainingExactQueryMatch(searchWord, word, language);

                if (!isExactMeaningWordsMatch) {
                    //Checking if the romaji is a match
                    romaji = word.getRomaji();
                    romajiLength = romaji.length();
                    isRomajiMatch = romajiLength <= searchWordLength + 1 && romaji.contains(searchWord);

                }

                if (!isExactMeaningWordsMatch && !isRomajiMatch) {

                    //Checking if one of the elements of altSpellings is a match
                    altSpellings = word.getAltSpellings();
                    isAltSpellingsMatch = false;
                    for (String altSpelling : altSpellings.split(",")) {
                        if (altSpelling.trim().contains(searchWord)) {
                            isAltSpellingsMatch = true;
                            break;
                        }
                    }
                }

                //Setting foundMatch
                if (isExactMeaningWordsMatch || isRomajiMatch || isAltSpellingsMatch) {
                    foundMatch = true;
                } else continue;

            }
            //endregion

            //Otherwise, handling longer words
            if (!foundMatch) {

                keywordsList = new ArrayList<>();
                keywordsList.add(word.getRomaji());
                keywordsList.add(word.getKanji());
                keywordsList.add(word.getAltSpellings());
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
                keywordsList.add(getConcatenatedMeanings(word));
                keywords = TextUtils.join(", ", keywordsList).replace(" ", "").toLowerCase();

                for (String interpretation : possibleInterpretations) {
                    if (keywords.contains(interpretation.replace(" ", ""))) {
                        foundMatch = true;
                        break;
                    }
                }
                foundMatch |= keywords.contains(inglessVerb);
                foundMatch |= queryIsVerbWithTo && keywords.contains(searchWordWithoutTo);
            }

            if (foundMatch) {
                current_match_values = new long[2];
                current_match_values[0] = word.getWordId();
                //current_match_values[1] = (long) match_length;
                MatchList.add(current_match_values);
            }
        }

        for (int i = 0; i < MatchList.size(); i++) {
            matchingWordIds.add(MatchList.get(i)[0]);
        }
        //endregion

        return matchingWordIds;
    }

    private static List<Long> addNamesToMatchesList(InputQuery query, Context context) {

        String searchWord = query.getSearchQuery();
        List<Long> matchingWordIdsNames = findQueryInNameIndices(searchWord, false, query.getType(), context);

        //If the number of matching ids is larger than MAX_SQL_VARIABLES_FOR_QUERY, perform an exact search
        if (matchingWordIdsNames.size() > Globals.MAX_SQL_VARIABLES_FOR_QUERY) {
            matchingWordIdsNames = findQueryInNameIndices(searchWord, true, query.getType(), context);
        }

        //If the number of matching ids is still larger than MAX_SQL_VARIABLES_FOR_QUERY, limit the list length to MAX_SQL_VARIABLES_FOR_QUERY
        if (matchingWordIdsNames.size() > Globals.MAX_SQL_VARIABLES_FOR_QUERY) {
            matchingWordIdsNames = matchingWordIdsNames.subList(0, Globals.MAX_SQL_VARIABLES_FOR_QUERY);
        }

        return matchingWordIdsNames;
    }

    private static boolean getMeaningsContainingExactQueryMatch(String searchWord, Word word, String language) {

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

        StringBuilder builder = new StringBuilder();
        for (Word.Meaning meaning : meanings) {
            builder.append(" ");
            builder.append(meaning.getMeaning().replace(", ", " ").replace("(", " ").replace(")", " "));
        }
        String meaningsString = builder.toString();
        String[] meaningSet = meaningsString.split(" ");
        boolean isExactMeaningWordsMatch = false;
        for (String meaningSetElement : meaningSet) {
            if (meaningSetElement.equals(searchWord)) {
                isExactMeaningWordsMatch = true;
                break;
            }
        }
        return isExactMeaningWordsMatch;
    }

    private static String getConcatenatedMeanings(Word word) {
        List<String> meanings = new ArrayList<>();
        for (Word.Meaning meaning : word.getMeaningsEN()) {
            meanings.add(meaning.getMeaning());
        }
        for (Word.Meaning meaning : word.getMeaningsFR()) {
            meanings.add(meaning.getMeaning());
        }
        for (Word.Meaning meaning : word.getMeaningsES()) {
            meanings.add(meaning.getMeaning());
        }
        return TextUtils.join(", ", meanings);
    }

    private static List<Long> addConjugatedAdjectivesToMatchesList(InputQuery query, List<Long> matchingWordIds, Context context) {

        String searchWord = query.getSearchQuery();
        //Adding relevant adjectives to the list of matches if the input query is an adjective conjugation
        List<Long> matchingWordIdsFromIndex = getmatchingWordIdsFromIndexForAdjectiveSearch(searchWord, query.getType(), matchingWordIds, false, context);

        if (matchingWordIdsFromIndex.size() > Globals.MAX_SQL_VARIABLES_FOR_QUERY) {
            matchingWordIdsFromIndex = getmatchingWordIdsFromIndexForAdjectiveSearch(searchWord, query.getType(), matchingWordIds, true, context);
        }

        if (matchingWordIdsFromIndex.size() > Globals.MAX_SQL_VARIABLES_FOR_QUERY) {
            Log.i(Globals.DEBUG_TAG, "WARNING: exceeded MAX_SQL_VARIABLES_FOR_QUERY when searching for " + searchWord + " in adjectives search, but prevented crash.");
        } else {
            List<Word> matchingPotentialAdjectives = RoomCentralDatabase.getInstance(context).getWordListByWordIds(matchingWordIdsFromIndex);
            boolean isAlreadyInList;
            for (Word word : matchingPotentialAdjectives) {
                isAlreadyInList = false;

                List<String> typesList = new ArrayList<>();
                for (Word.Meaning meaning : word.getMeaningsEN()) {
                    typesList.add(meaning.getType());
                }
                typesList = Arrays.asList(TextUtils.join(Globals.DB_ELEMENTS_DELIMITER, typesList).split(Globals.DB_ELEMENTS_DELIMITER));
                if (typesList.contains("Ai") || typesList.contains("Ana")) {
                    for (long id : matchingWordIds) {
                        if (id == word.getWordId()) {
                            isAlreadyInList = true;
                            break;
                        }
                    }
                    if (!isAlreadyInList) matchingWordIds.add(word.getWordId());
                }
            }
        }

        return matchingWordIds;
    }

    private static List<Long> addCountersToMatchesList(InputQuery query, List<Long> matchingWordIds, Context context) {

        String searchWord = query.getSearchQuery();
        if (query.getType() == Globals.TYPE_KANJI && searchWord.length() == 2 && "何一二三四五六七八九十".contains(searchWord.substring(0, 1))) {

            List<IndexKanji> kanjiIndicesForCounter = findQueryInKanjiIndex(searchWord.substring(1, 2), true, false, context);

            if (kanjiIndicesForCounter.size() == 0) return matchingWordIds;

            List<String> searchResultIndexesArray = new ArrayList<>();
            for (IndexKanji indexKanji : kanjiIndicesForCounter) {
                searchResultIndexesArray.add(indexKanji.getWordIds());
            }

            List<String> indexList;
            List<Long> matchingWordIdsFromIndex = new ArrayList<>();
            for (String searchResultIndexes : searchResultIndexesArray) {
                indexList = Arrays.asList(searchResultIndexes.split(Globals.DB_ELEMENTS_DELIMITER));
                for (int j = 0; j < indexList.size(); j++) {
                    matchingWordIdsFromIndex.add(Long.valueOf(indexList.get(j)));
                }
            }

            if (matchingWordIdsFromIndex.size() > Globals.MAX_SQL_VARIABLES_FOR_QUERY) {
                Log.i(Globals.DEBUG_TAG, "WARNING: exceeded MAX_SQL_VARIABLES_FOR_QUERY when searching for " + searchWord + " in counter search, but prevented crash.");
            } else {
                List<Word> matchingPotentialCounters = RoomCentralDatabase.getInstance(context).getWordListByWordIds(matchingWordIdsFromIndex);
                boolean isAlreadyInList;
                for (Word word : matchingPotentialCounters) {
                    isAlreadyInList = false;
                    List<String> typesList = new ArrayList<>();
                    for (Word.Meaning meaning : word.getMeaningsEN()) {
                        typesList.add(meaning.getType());
                    }
                    typesList = Arrays.asList(TextUtils.join(Globals.DB_ELEMENTS_DELIMITER, typesList).split(Globals.DB_ELEMENTS_DELIMITER));
                    if (typesList.contains("C")) {
                        for (long id : matchingWordIds) {
                            if (id == word.getWordId()) {
                                isAlreadyInList = true;
                                break;
                            }
                        }
                        if (!isAlreadyInList) matchingWordIds.add(word.getWordId());
                    }
                }
            }
        }

        return matchingWordIds;
    }

    private static List<Long> getmatchingWordIdsFromIndexForAdjectiveSearch(String searchWord, int inputTextType, List<Long> matchingWordIds, boolean forceExactSearch, Context context) {

        List<Long> matchingWordIdsFromIndex = new ArrayList<>();
        String input_word = Utilities.removeNonSpaceSpecialCharacters(searchWord);
        String adjectiveConjugation;
        String baseAdjective = "";
        boolean isPotentialAdjective = false;
        List<String> searchResultIndexesArray = new ArrayList<>();

        if (inputTextType == Globals.TYPE_LATIN || inputTextType == Globals.TYPE_HIRAGANA || inputTextType == Globals.TYPE_KATAKANA) {

            input_word = InputQuery.getWaapuroHiraganaKatakana(input_word).get(Globals.TYPE_LATIN);

            if (input_word.length() > 9) {
                adjectiveConjugation = input_word.substring(input_word.length() - 9);
                baseAdjective = input_word.substring(0, input_word.length() - 9) + "i";
                if (adjectiveConjugation.equals("kunakatta")) isPotentialAdjective = true;
            }
            if (!isPotentialAdjective && input_word.length() > 6) {
                adjectiveConjugation = input_word.substring(input_word.length() - 6);
                baseAdjective = input_word.substring(0, input_word.length() - 6) + "i";
                if (adjectiveConjugation.equals("kereba")) isPotentialAdjective = true;
            }
            if (!isPotentialAdjective && input_word.length() > 5) {
                adjectiveConjugation = input_word.substring(input_word.length() - 5);
                baseAdjective = input_word.substring(0, input_word.length() - 5) + "i";
                if (adjectiveConjugation.equals("kunai")
                        || adjectiveConjugation.equals("katta")
                        || adjectiveConjugation.equals("karou")) isPotentialAdjective = true;
            }
            if (!isPotentialAdjective && input_word.length() > 4) {
                adjectiveConjugation = input_word.substring(input_word.length() - 4);
                baseAdjective = input_word.substring(0, input_word.length() - 4) + "i";
                if (adjectiveConjugation.equals("kute")) isPotentialAdjective = true;
            }
            if (!isPotentialAdjective && input_word.length() > 2) {
                adjectiveConjugation = input_word.substring(input_word.length() - 2);
                if (adjectiveConjugation.equals("mi") || adjectiveConjugation.equals("ku")) {
                    isPotentialAdjective = true;
                    baseAdjective = input_word.substring(0, input_word.length() - 2) + "i";
                } else if (adjectiveConjugation.equals("ni") || adjectiveConjugation.equals("na") || adjectiveConjugation.equals("de")) {
                    isPotentialAdjective = true;
                    baseAdjective = input_word.substring(0, input_word.length() - 2);
                }
            }

            if (!isPotentialAdjective) return matchingWordIds;

            boolean exactSearch = baseAdjective.length() < 3 || forceExactSearch;

            List<String> possibleInterpretations = InputQuery.getWaapuroRomanizationsFromLatinText(baseAdjective);
            List<Object> latinIndicesForAdjective = findQueryInLatinIndices(baseAdjective, possibleInterpretations, exactSearch, new String[]{"romaji"}, false, context);

            if (latinIndicesForAdjective.size() == 0) return matchingWordIds;

            for (Object indexLatin : latinIndicesForAdjective) {
                if (indexLatin instanceof IndexRomaji) searchResultIndexesArray.add(((IndexRomaji) indexLatin).getWordIds());
                else if (indexLatin instanceof IndexEnglish) searchResultIndexesArray.add(((IndexEnglish) indexLatin).getWordIds());
                else if (indexLatin instanceof IndexFrench) searchResultIndexesArray.add(((IndexFrench) indexLatin).getWordIds());
                else if (indexLatin instanceof IndexSpanish) searchResultIndexesArray.add(((IndexSpanish) indexLatin).getWordIds());
            }

        } else if (inputTextType == Globals.TYPE_KANJI) {

            if (input_word.length() > 5) {
                adjectiveConjugation = input_word.substring(input_word.length() - 5);
                baseAdjective = input_word.substring(0, input_word.length() - 5) + "い";
                if (adjectiveConjugation.equals("くなかった")) isPotentialAdjective = true;
            }
            if (!isPotentialAdjective && input_word.length() > 3) {
                adjectiveConjugation = input_word.substring(input_word.length() - 3);
                baseAdjective = input_word.substring(0, input_word.length() - 3) + "い";
                if (adjectiveConjugation.equals("くない")
                        || adjectiveConjugation.equals("ければ")
                        || adjectiveConjugation.equals("かった")
                        || adjectiveConjugation.equals("かろう")) isPotentialAdjective = true;
            }
            if (!isPotentialAdjective && input_word.length() > 2) {
                adjectiveConjugation = input_word.substring(input_word.length() - 2);
                baseAdjective = input_word.substring(0, input_word.length() - 2) + "い";
                if (adjectiveConjugation.equals("くて")) isPotentialAdjective = true;
            }
            if (!isPotentialAdjective && input_word.length() > 1) {
                adjectiveConjugation = input_word.substring(input_word.length() - 1);
                if (adjectiveConjugation.equals("み") || adjectiveConjugation.equals("く")) {
                    isPotentialAdjective = true;
                    baseAdjective = input_word.substring(0, input_word.length() - 1) + "";
                } else if (adjectiveConjugation.equals("に") || adjectiveConjugation.equals("な") || adjectiveConjugation.equals("で")) {
                    isPotentialAdjective = true;
                    baseAdjective = input_word.substring(0, input_word.length() - 1);
                }
            }

            if (!isPotentialAdjective) return matchingWordIds;

            List<IndexKanji> kanjiIndicesForAdjective = findQueryInKanjiIndex(baseAdjective, forceExactSearch, false, context);

            if (kanjiIndicesForAdjective.size() == 0) return matchingWordIds;

            for (IndexKanji indexKanji : kanjiIndicesForAdjective) {
                searchResultIndexesArray.add(indexKanji.getWordIds());
            }

        } else {
            return matchingWordIds;
        }

        List<String> indexList;
        for (String searchResultIndexes : searchResultIndexesArray) {
            indexList = Arrays.asList(searchResultIndexes.split(Globals.DB_ELEMENTS_DELIMITER));
            for (int j = 0; j < indexList.size(); j++) {
                matchingWordIdsFromIndex.add(Long.valueOf(indexList.get(j)));
            }
        }

        return matchingWordIdsFromIndex;
    }

    public static String replaceInvalidKanjisWithValidOnes(String input, List<String[]> mSimilarsDatabase) {
        StringBuilder output = new StringBuilder();
        char currentChar;
        boolean found;
        for (int i = 0; i < input.length(); i++) {
            currentChar = input.charAt(i);
            found = false;
            for (int j = 0; j < mSimilarsDatabase.size(); j++) {
                if (mSimilarsDatabase.get(j).length > 0 && mSimilarsDatabase.get(j)[0].charAt(0) == currentChar) {
                    output.append(mSimilarsDatabase.get(j)[1].charAt(0));
                    found = true;
                    break;
                }
            }
            if (!found) output.append(currentChar);
        }
        return output.toString();
    }



    private static List<Long> findQueryInNameIndices(String concatenated_word, boolean exactSearch, int inputTextType, Context context) {

        List<String> matchingIndices = new ArrayList<>();
        RoomNamesDatabase namesRoomDatabase = RoomNamesDatabase.getInstance(context);
        if (exactSearch) {
            //Preventing the index search from returning too many results and crashing the app

            if (inputTextType == Globals.TYPE_KANJI) {
                IndexKanji indexKanji = namesRoomDatabase.getKanjiIndexForExactWord(concatenated_word);
                if (indexKanji != null) {
                    matchingIndices.addAll(Arrays.asList(indexKanji.getWordIds().split(Globals.DB_ELEMENTS_DELIMITER)));
                }
            } else {
                if (inputTextType == Globals.TYPE_HIRAGANA || inputTextType == Globals.TYPE_KATAKANA) {
                    concatenated_word = InputQuery.getWaapuroHiraganaKatakana(concatenated_word).get(Globals.TYPE_LATIN);
                }
                IndexRomaji indexRomaji = namesRoomDatabase.getRomajiIndexForExactWord(concatenated_word);
                if (indexRomaji != null) {
                    matchingIndices.addAll(Arrays.asList(indexRomaji.getWordIds().split(Globals.DB_ELEMENTS_DELIMITER)));
                }
            }

        } else {
            if (inputTextType == Globals.TYPE_KANJI) {
                List<IndexKanji> indexesKanji = namesRoomDatabase.getKanjiIndexesListForStartingWord(concatenated_word);
                if (indexesKanji != null && indexesKanji.size() > 0) {
                    for (IndexKanji indexKanji : indexesKanji) {
                        matchingIndices.addAll(Arrays.asList(indexKanji.getWordIds().split(Globals.DB_ELEMENTS_DELIMITER)));
                    }
                }
            } else {
                if (inputTextType == Globals.TYPE_HIRAGANA || inputTextType == Globals.TYPE_KATAKANA) {
                    concatenated_word = InputQuery.getWaapuroHiraganaKatakana(concatenated_word).get(Globals.TYPE_LATIN);
                }
                List<IndexRomaji> indexesRomaji = namesRoomDatabase.getRomajiIndexesListForStartingWord(concatenated_word);
                if (indexesRomaji != null && indexesRomaji.size() > 0) {
                    for (IndexRomaji indexRomaji : indexesRomaji) {
                        matchingIndices.addAll(Arrays.asList(indexRomaji.getWordIds().split(Globals.DB_ELEMENTS_DELIMITER)));
                    }
                }
            }

        }
        matchingIndices = Utilities.removeDuplicatesFromList(matchingIndices);
        List<Long> finalIndexList = new ArrayList<>();
        for (String element : matchingIndices) {
            finalIndexList.add(Long.parseLong(element));
        }
        return finalIndexList;
    }

    private static List<Object> findQueryInLatinIndices(String concatenated_word, List<String> possibleInterpretations, boolean exactSearch, String[] searchType,
                                                        boolean use_extended_db, Context context) {

        //Exact search Prevents the index search from returning too many results and crashing the app

        RoomCentralDatabase roomCentralDatabase = RoomCentralDatabase.getInstance(context);
        RoomExtendedDatabase extendedRoomDatabase = RoomExtendedDatabase.getInstance(context);
        List<Object> matchingIndices = new ArrayList<>();
        if (exactSearch) {

            if (Arrays.asList(searchType).contains("romaji")) {
                List<IndexRomaji> indexesRomaji = use_extended_db ? extendedRoomDatabase.getRomajiIndexForExactWordsList(possibleInterpretations)
                        : roomCentralDatabase.getRomajiIndexForExactWordsList(possibleInterpretations);
                if (indexesRomaji != null && indexesRomaji.size() > 0) matchingIndices.addAll(indexesRomaji);
            }

            if (Arrays.asList(searchType).contains(Globals.LANG_STR_EN)) {
                IndexEnglish indexEnglish = use_extended_db ? extendedRoomDatabase.getEnglishIndexForExactWord(concatenated_word)
                        : roomCentralDatabase.getEnglishIndexForExactWord(concatenated_word);
                if (indexEnglish != null) matchingIndices.add(indexEnglish); //Only add the index if the word was found in the index
            } else if (Arrays.asList(searchType).contains(Globals.LANG_STR_FR)) {
                IndexFrench indexFrench = use_extended_db ? extendedRoomDatabase.getFrenchIndexForExactWord(concatenated_word)
                        : roomCentralDatabase.getFrenchIndexForExactWord(concatenated_word);
                if (indexFrench != null) matchingIndices.add(indexFrench); //Only add the index if the word was found in the index
                IndexEnglish indexEnglish = use_extended_db ? extendedRoomDatabase.getEnglishIndexForExactWord(concatenated_word)
                        : roomCentralDatabase.getEnglishIndexForExactWord(concatenated_word);
                if (indexEnglish != null) matchingIndices.add(indexEnglish); //Only add the index if the word was found in the index
            } else if (Arrays.asList(searchType).contains(Globals.LANG_STR_ES)) {
                IndexSpanish indexSpanish = use_extended_db ? extendedRoomDatabase.getSpanishIndexForExactWord(concatenated_word)
                        : roomCentralDatabase.getSpanishIndexForExactWord(concatenated_word);
                if (indexSpanish != null) matchingIndices.add(indexSpanish); //Only add the index if the word was found in the index
                IndexEnglish indexEnglish = use_extended_db ? extendedRoomDatabase.getEnglishIndexForExactWord(concatenated_word)
                        : roomCentralDatabase.getEnglishIndexForExactWord(concatenated_word);
                if (indexEnglish != null) matchingIndices.add(indexEnglish); //Only add the index if the word was found in the index
            }

        } else {
            if (Arrays.asList(searchType).contains("romaji")) {
                List<IndexRomaji> indexesRomaji = use_extended_db ? extendedRoomDatabase.getRomajiIndexesListForStartingWordsList(possibleInterpretations)
                        : roomCentralDatabase.getRomajiIndexesListForStartingWordsList(possibleInterpretations);
                if (indexesRomaji != null && indexesRomaji.size() > 0) matchingIndices.addAll(indexesRomaji);
            }

            if (Arrays.asList(searchType).contains(Globals.LANG_STR_EN)) {
                List<IndexEnglish> indexesEnglish = use_extended_db ? extendedRoomDatabase.getEnglishIndexesListForStartingWord(concatenated_word)
                        : roomCentralDatabase.getEnglishIndexesListForStartingWord(concatenated_word);
                if (indexesEnglish != null && indexesEnglish.size() > 0) matchingIndices.addAll(indexesEnglish);
            } else if (Arrays.asList(searchType).contains(Globals.LANG_STR_FR)) {
                List<IndexFrench> indexesFrench = use_extended_db ? extendedRoomDatabase.getFrenchIndexesListForStartingWord(concatenated_word)
                        : roomCentralDatabase.getFrenchIndexesListForStartingWord(concatenated_word);
                if (indexesFrench != null && indexesFrench.size() > 0) matchingIndices.addAll(indexesFrench);
                List<IndexEnglish> indexesEnglish = use_extended_db ? extendedRoomDatabase.getEnglishIndexesListForStartingWord(concatenated_word)
                        : roomCentralDatabase.getEnglishIndexesListForStartingWord(concatenated_word);
                if (indexesEnglish != null && indexesEnglish.size() > 0) matchingIndices.addAll(indexesEnglish);
            } else if (Arrays.asList(searchType).contains(Globals.LANG_STR_ES)) {
                List<IndexSpanish> indexesSpanish = use_extended_db ? extendedRoomDatabase.getSpanishIndexesListForStartingWord(concatenated_word)
                        : roomCentralDatabase.getSpanishIndexesListForStartingWord(concatenated_word);
                if (indexesSpanish != null && indexesSpanish.size() > 0) matchingIndices.addAll(indexesSpanish);
                List<IndexEnglish> indexesEnglish = use_extended_db ? extendedRoomDatabase.getEnglishIndexesListForStartingWord(concatenated_word)
                        : roomCentralDatabase.getEnglishIndexesListForStartingWord(concatenated_word);
                if (indexesEnglish != null && indexesEnglish.size() > 0) matchingIndices.addAll(indexesEnglish);
            }
        }
        return matchingIndices;
    }

    private static List<IndexKanji> findQueryInKanjiIndex(String concatenated_word, boolean exactSearch, boolean use_extended_db, Context context) {

        // Prepare the input word to be used in the following algorithm: the word is converted to its hex utf-8 value as a string, in fractional form
        //String prepared_word = convertToUTF8Index(concatenated_word);
        String prepared_word = concatenated_word;

        RoomCentralDatabase roomCentralDatabase = RoomCentralDatabase.getInstance(context);
        RoomExtendedDatabase japaneseToolboxExtendedRoomDatabase = RoomExtendedDatabase.getInstance(context);
        List<IndexKanji> matchingIndexKanjis;
        if (exactSearch) {
            //Preventing the index search from returning too many results and crashing the app
            matchingIndexKanjis = new ArrayList<>();
            IndexKanji index = use_extended_db ? japaneseToolboxExtendedRoomDatabase.getKanjiIndexForExactWord(prepared_word)
                    : roomCentralDatabase.getKanjiIndexForExactWord(prepared_word);
            if (index != null) matchingIndexKanjis.add(index); //Only add the index if the word was found in the index
            return matchingIndexKanjis;
        } else {
            matchingIndexKanjis = use_extended_db ? japaneseToolboxExtendedRoomDatabase.getKanjiIndexesListForStartingWord(prepared_word)
                    : roomCentralDatabase.getKanjiIndexesListForStartingWord(prepared_word);
            return matchingIndexKanjis;
        }
    }

    public static String getRomajiNoSpacesForSpecialPartsOfSpeech(String romaji) {
        return romaji.replace(" ni", "ni")
                .replace(" de", "de")
                .replace(" wo", "wo")
                .replace(" to", "to")
                .replace(" na", "na");
    }

    public static boolean wordsAreEquivalent(Word wordA, Word wordB) {
        return wordA.getRomaji().trim().equals(wordB.getRomaji().trim()) && wordA.getKanji().trim().equals(wordB.getKanji().trim());
    }

    public static boolean wordsAreSimilar(Word wordA, String wordB) {
        return wordA.getRomaji().trim().equals(wordB) || wordA.getKanji().trim().equals(wordB);
    }

    //Database creation utilities
    public static FirebaseDatabase getDatabase() {
        //inspired by: https://github.com/firebase/quickstart-android/issues/15
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase;
    }

    public static String cleanIdentifierForFirebase(String string) {
        if (TextUtils.isEmpty(string)) return "";
        string = string.replaceAll("\\.", "*");
        string = string.replaceAll("#", "*");
        string = string.replaceAll("\\$", "*");
        string = string.replaceAll("\\[", "*");
        string = string.replaceAll("]", "*");
        //string = string.replaceAll("\\{","*");
        //string = string.replaceAll("}","*");
        return string;
    }

    public static void checkDatabaseStructure(List<String[]> databaseFromCsv, String databaseName, int numColumns) {
        for (String[] line : databaseFromCsv) {
            if (line.length < numColumns) {
                Log.v("JapaneseToolbox", "Serious error in row [" + line[0] + "] in " + databaseName + ": CSV file row has less columns than expected! Check for accidental line breaks.");
                break;
            }
        }
    }

    public static Word createWordFromCsvDatabases(List<String[]> centralDatabase,
                                                  List<String[]> meaningsENDatabase,
                                                  List<String[]> meaningsFRDatabase,
                                                  List<String[]> meaningsESDatabase,
                                                  List<String[]> multExplENDatabase,
                                                  List<String[]> multExplFRDatabase,
                                                  List<String[]> multExplESDatabase,
                                                  List<String[]> examplesDatabase, int centralDbRowIndex) {

        Word word = new Word();

        //Getting the index value
        int matchingWordId = Integer.parseInt(centralDatabase.get(centralDbRowIndex)[Globals.COLUMN_WORD_ID]);
        word.setWordId(matchingWordId);

        //Getting the Romaji value
        String matchingWordRomaji = centralDatabase.get(centralDbRowIndex)[Globals.COLUMN_ROMAJI];
        word.setRomaji(matchingWordRomaji);

        //Getting the Kanji value
        String matchingWordKanji = centralDatabase.get(centralDbRowIndex)[Globals.COLUMN_KANJI];
        word.setKanji(matchingWordKanji);

        //Setting the unique identifier
        word.setUniqueIdentifier(matchingWordRomaji + "-" + matchingWordKanji);

        //Getting the Jap Keywords
        word.setExtraKeywordsJAP(centralDatabase.get(centralDbRowIndex)[Globals.COLUMN_EXTRA_KEYWORDS_JAP]);

        //Setting the flags
        word.setIsCommon(centralDatabase.get(centralDbRowIndex)[Globals.COLUMN_COMMON].equals("1"));
        word.setIsLocal(true);

        //Getting the AltSpellings value
        String matchingWordAltSpellings = centralDatabase.get(centralDbRowIndex)[Globals.COLUMN_ALT_SPELLINGS];
        word.setAltSpellings(matchingWordAltSpellings);

        //Getting the Meanings
        word.setMeaningsEN(getMeanings(centralDatabase, meaningsENDatabase, Globals.COLUMN_MEANING_EN_INDEXES,
                multExplENDatabase, examplesDatabase, centralDbRowIndex, "en"));
        if (word.getMeaningsEN().size() == 0) return new Word();

        word.setMeaningsFR(getMeanings(centralDatabase, meaningsFRDatabase, Globals.COLUMN_MEANING_FR_INDEXES,
                multExplFRDatabase, examplesDatabase, centralDbRowIndex, "fr"));
        word.setMeaningsES(getMeanings(centralDatabase, meaningsESDatabase, Globals.COLUMN_MEANING_ES_INDEXES,
                multExplESDatabase, examplesDatabase, centralDbRowIndex, "es"));

        //Setting the keywords value
        word.setExtraKeywordsEN(centralDatabase.get(centralDbRowIndex)[Globals.COLUMN_EXTRA_KEYWORDS_EN]);
        word.setExtraKeywordsFR(centralDatabase.get(centralDbRowIndex)[Globals.COLUMN_EXTRA_KEYWORDS_FR]);
        word.setExtraKeywordsES(centralDatabase.get(centralDbRowIndex)[Globals.COLUMN_EXTRA_KEYWORDS_ES]);

        return word;
    }

    static Word createWordFromExtendedDatabase(String[] extendedDatabaseRow) {

        Word word = new Word();
        word.setWordId(Long.parseLong(extendedDatabaseRow[Globals.XDB_COL_INDEX]));
        word.setRomaji(extendedDatabaseRow[Globals.XDB_COL_ROMAJI]);
        word.setKanji(extendedDatabaseRow[Globals.XDB_COL_KANJI]);
        word.setAltSpellings(extendedDatabaseRow[Globals.XDB_COL_ALTS].replace("#", ", "));
        word.setExtraKeywordsJAP("");
        word.setExtraKeywordsEN("");
        word.setExtraKeywordsFR("");
        word.setExtraKeywordsES("");

        String[] POS_list = extendedDatabaseRow[Globals.XDB_COL_POS].split("#", -1);
        String[] meaningsEN_list = extendedDatabaseRow[Globals.XDB_COL_MEANINGS_EN].split("#", -1);
        String[] meaningsFR_list = extendedDatabaseRow[Globals.XDB_COL_MEANINGS_FR].split("#", -1);
        String[] meaningsES_list = extendedDatabaseRow[Globals.XDB_COL_MEANINGS_ES].split("#", -1);

        List<Word.Meaning> meaningsEN = new ArrayList<>();
        List<Word.Meaning> meaningsFR = new ArrayList<>();
        List<Word.Meaning> meaningsES = new ArrayList<>();

        if (!meaningsEN_list[0].equals("")) {
            for (int i = 0; i < meaningsEN_list.length; i++) {
                Word.Meaning meaning = new Word.Meaning();
                meaning.setMeaning(meaningsEN_list[i].replace("@@@@", "#").replace("$$$$", "\""));
                meaning.setType(POS_list[i]);
                meaningsEN.add(meaning);
            }
        }
        if (!meaningsFR_list[0].equals("")) {
            for (int i = 0; i < meaningsFR_list.length; i++) {
                Word.Meaning meaning = new Word.Meaning();
                meaning.setMeaning(meaningsFR_list[i].replace("@@@@", "#").replace("$$$$", "\""));
                meaning.setType(POS_list[0]);
                meaningsFR.add(meaning);
            }
        }
        if (!meaningsES_list[0].equals("")) {
            for (int i = 0; i < meaningsES_list.length; i++) {
                Word.Meaning meaning = new Word.Meaning();
                meaning.setMeaning(meaningsES_list[i].replace("@@@@", "#").replace("$$$$", "\""));
                meaning.setType(POS_list[0]);
                meaningsES.add(meaning);
            }
        }

        word.setMeaningsEN(meaningsEN);
        word.setMeaningsFR(meaningsFR);
        word.setMeaningsES(meaningsES);

        return word;
    }

    static Word createWordFromNamesDatabase(String[] namesDatabaseRow) {

        Word word = new Word();
        word.setWordId(Long.parseLong(namesDatabaseRow[Globals.NDB_COL_INDEX]));
        word.setRomaji(namesDatabaseRow[Globals.NDB_COL_ROMAJI]);
        word.setKanji(namesDatabaseRow[Globals.NDB_COL_KANJI]);
        word.setAltSpellings("");
        word.setExtraKeywordsJAP("");
        word.setExtraKeywordsEN("");
        word.setExtraKeywordsFR("");
        word.setExtraKeywordsES("");

        String[] POS_list = namesDatabaseRow[Globals.NDB_COL_POS].split("#", -1);

        List<Word.Meaning> meaningsEN = new ArrayList<>();

        /*
        String[] meaningsEN_list = namesDatabaseRow[GlobalConstants.NDB_COL_MEANINGS_EN].split("#", -1);
        if (!meaningsEN_list[0].equals("")) {
            for (int i = 0; i < meaningsEN_list.length; i++) {
                Word.Meaning meaning = new Word.Meaning();
                meaning.setMeaning(meaningsEN_list[i].replace("@@@@", "#").replace("$$$$", "\""));
                meaning.setType(POS_list[i]);
                meaningsEN.add(meaning);
            }
        }*/
        Word.Meaning meaning = new Word.Meaning();
        meaning.setMeaning("*");
        meaning.setType(POS_list[0]);
        meaningsEN.add(meaning);

        word.setMeaningsEN(meaningsEN);
        word.setMeaningsFR(new ArrayList<>());
        word.setMeaningsES(new ArrayList<>());

        return word;
    }

    private static List<Word.Meaning> getMeanings(List<String[]> centralDatabase, List<String[]> meaningsDatabase, int meaningsColumn,
                                                  List<String[]> multExplDatabase, List<String[]> examplesDatabase, int centralDbRowIndex, String language) {

        //Initializations
        int example_index;
        List<String> parsed_example_list;
        String matchingWordMeaning;
        String matchingWordType;
        String matchingWordAntonym;
        String matchingWordSynonym;
        String matchingWordExplanation;
        String matchingWordRules;
        String matchingWordExampleList;
        String[] current_meaning_characteristics;
        boolean has_multiple_explanations;
        String ME_index;
        int exampleLatinColumn;
        switch (language) {
            case "en":
                exampleLatinColumn = Globals.COLUMN_EXAMPLES_ENGLISH;
                break;
            case "fr":
                exampleLatinColumn = Globals.COLUMN_EXAMPLES_FRENCH;
                break;
            case "es":
                exampleLatinColumn = Globals.COLUMN_EXAMPLES_SPANISH;
                break;
            default:
                exampleLatinColumn = Globals.COLUMN_EXAMPLES_ENGLISH;
                break;
        }

        //Finding the meanings using the supplied index
        String MM_indexEN = centralDatabase.get(centralDbRowIndex)[meaningsColumn];
        List<String> MM_index_list = Arrays.asList(MM_indexEN.split(Globals.DB_ELEMENTS_DELIMITER));
        if (MM_index_list.size() == 0 || MM_index_list.get(0).equals("")) {
            return new ArrayList<>();
        }

        List<Word.Meaning> meaningsList = new ArrayList<>();
        int current_MM_index;
        for (int i = 0; i < MM_index_list.size(); i++) {

            Word.Meaning meaning = new Word.Meaning();
            current_MM_index = Integer.parseInt(MM_index_list.get(i)) - 1;
            current_meaning_characteristics = meaningsDatabase.get(current_MM_index);

            //Getting the Meaning value
            matchingWordMeaning = current_meaning_characteristics[Globals.COLUMN_MEANINGS_MEANING];
            if (matchingWordMeaning.equals("")) continue;

            //Getting the Type value
            matchingWordType = current_meaning_characteristics[Globals.COLUMN_MEANINGS_TYPE];

            //Adding "to " to the meaning values if the hit is a verb and these are english meanings
            if (language.equals("en") && matchingWordType.contains("V") && !matchingWordType.equals("VC") && !matchingWordType.equals("NV") && !matchingWordType.equals("VdaI")) {

                List<String> meaningElements = Arrays.asList(matchingWordMeaning.split(","));
                StringBuilder meaningFixed = new StringBuilder();
                boolean valueIsInParentheses = false;
                for (int k = 0; k < meaningElements.size(); k++) {
                    if (valueIsInParentheses) meaningFixed.append(meaningElements.get(k).trim());
                    else meaningFixed.append("to ").append(meaningElements.get(k).trim());

                    if (k < meaningElements.size() - 1) meaningFixed.append(", ");

                    if (meaningElements.get(k).contains("(") && !meaningElements.get(k).contains(")")) valueIsInParentheses = true;
                    else if (!meaningElements.get(k).contains("(") && meaningElements.get(k).contains(")")) valueIsInParentheses = false;
                }
                matchingWordMeaning = meaningFixed.toString();
            }

            //Setting the Meaning and Type values in the returned list
            meaning.setMeaning(matchingWordMeaning);
            meaning.setType(matchingWordType);

            //Getting the Opposite value
            matchingWordAntonym = current_meaning_characteristics[Globals.COLUMN_MEANINGS_ANTONYM];
            meaning.setAntonym(matchingWordAntonym);

            //Getting the Synonym value
            matchingWordSynonym = current_meaning_characteristics[Globals.COLUMN_MEANINGS_SYNONYM];
            meaning.setSynonym(matchingWordSynonym);

            //Getting the set of Explanations
            has_multiple_explanations = false;
            ME_index = "";
            if (current_meaning_characteristics[Globals.COLUMN_MEANINGS_EXPLANATION].length() > 3) {
                if (current_meaning_characteristics[Globals.COLUMN_MEANINGS_EXPLANATION].substring(0, 3).equals("ME#")) {
                    has_multiple_explanations = true;
                    ME_index = current_meaning_characteristics[Globals.COLUMN_MEANINGS_EXPLANATION].substring(3);
                }
            }

            List<Word.Meaning.Explanation> explanationList = new ArrayList<>();
            if (has_multiple_explanations) {
                List<String> ME_index_list = Arrays.asList(ME_index.split(Globals.DB_ELEMENTS_DELIMITER));
                int current_ME_index;
                for (int j = 0; j < ME_index_list.size(); j++) {

                    Word.Meaning.Explanation explanation = new Word.Meaning.Explanation();

                    current_ME_index = Integer.parseInt(ME_index_list.get(j)) - 1;

                    //Getting the Explanation value
                    matchingWordExplanation = multExplDatabase.get(current_ME_index)[Globals.COLUMN_MULT_EXPLANATIONS_ITEM];
                    explanation.setExplanation(matchingWordExplanation);

                    //Getting the Rules value
                    matchingWordRules = multExplDatabase.get(current_ME_index)[Globals.COLUMN_MULT_EXPLANATIONS_RULE];
                    explanation.setRules(matchingWordRules);

                    //Getting the Examples
                    matchingWordExampleList = multExplDatabase.get(current_ME_index)[Globals.COLUMN_MULT_EXPLANATIONS_EXAMPLES];
                    List<Word.Meaning.Explanation.Example> exampleList = new ArrayList<>();
                    if (!matchingWordExampleList.equals("") && !matchingWordExampleList.contains("Example")) {
                        parsed_example_list = Arrays.asList(matchingWordExampleList.split(Globals.DB_ELEMENTS_DELIMITER));
                        for (int t = 0; t < parsed_example_list.size(); t++) {
                            Word.Meaning.Explanation.Example example = new Word.Meaning.Explanation.Example();
                            example_index = Integer.parseInt(parsed_example_list.get(t)) - 1;
                            example.setLatinSentence(examplesDatabase.get(example_index)[exampleLatinColumn]);
                            example.setRomajiSentence(examplesDatabase.get(example_index)[Globals.COLUMN_EXAMPLES_ROMAJI]);
                            example.setKanjiSentence(examplesDatabase.get(example_index)[Globals.COLUMN_EXAMPLES_KANJI]);
                            exampleList.add(example);
                        }
                    }
                    explanation.setExamples(exampleList);
                    explanationList.add(explanation);
                }
            } else {
                Word.Meaning.Explanation explanation = new Word.Meaning.Explanation();

                //Getting the Explanation value
                matchingWordExplanation = meaningsDatabase.get(current_MM_index)[Globals.COLUMN_MEANINGS_EXPLANATION];
                explanation.setExplanation(matchingWordExplanation);

                //Getting the Rules value
                matchingWordRules = meaningsDatabase.get(current_MM_index)[Globals.COLUMN_MEANINGS_RULES];
                explanation.setRules(matchingWordRules);

                //Getting the Examples
                matchingWordExampleList = meaningsDatabase.get(current_MM_index)[Globals.COLUMN_MEANINGS_EXAMPLES];
                List<Word.Meaning.Explanation.Example> exampleList = new ArrayList<>();
                if (!matchingWordExampleList.equals("") && !matchingWordExampleList.contains("Example")) {
                    parsed_example_list = Arrays.asList(matchingWordExampleList.split(Globals.DB_ELEMENTS_DELIMITER));
                    for (int t = 0; t < parsed_example_list.size(); t++) {
                        Word.Meaning.Explanation.Example example = new Word.Meaning.Explanation.Example();
                        example_index = Integer.parseInt(parsed_example_list.get(t)) - 1;
                        example.setLatinSentence(examplesDatabase.get(example_index)[exampleLatinColumn]);
                        example.setRomajiSentence(examplesDatabase.get(example_index)[Globals.COLUMN_EXAMPLES_ROMAJI]);
                        example.setKanjiSentence(examplesDatabase.get(example_index)[Globals.COLUMN_EXAMPLES_KANJI]);
                        exampleList.add(example);
                    }
                }
                explanation.setExamples(exampleList);
                explanationList.add(explanation);
            }
            meaning.setExplanations(explanationList);
            meaningsList.add(meaning);

        }

        return meaningsList;
    }

    public static Verb createVerbFromCsvDatabase(List<String[]> verbDatabase, List<String[]> meaningsDatabase, int verbDbRowIndex) {

        // Value Initializations
        Verb verb = new Verb();
        boolean foundVerbType;
        String[] types;
        int lastCharIndex;
        String[] currentMeaningCharacteristics;

        verb.setVerbId(Integer.parseInt(verbDatabase.get(verbDbRowIndex)[Globals.COLUMN_WORD_ID]));
        verb.setPreposition(verbDatabase.get(verbDbRowIndex)[Globals.COLUMN_PREPOSITION]);
        verb.setKanjiRoot(verbDatabase.get(verbDbRowIndex)[Globals.COLUMN_KANJI_ROOT]);
        verb.setLatinRoot(verbDatabase.get(verbDbRowIndex)[Globals.COLUMN_LATIN_ROOT]);
        verb.setExceptionIndex(verbDatabase.get(verbDbRowIndex)[Globals.COLUMN_EXCEPTION_INDEX]);
        verb.setRomaji(verbDatabase.get(verbDbRowIndex)[Globals.COLUMN_ROMAJI]);
        verb.setKanji(verbDatabase.get(verbDbRowIndex)[Globals.COLUMN_KANJI]);
        verb.setAltSpellings(verbDatabase.get(verbDbRowIndex)[Globals.COLUMN_ALT_SPELLINGS]);
        verb.setHiraganaFirstChar(InputQuery.getWaapuroHiraganaKatakana(verb.getRomaji()).get(Globals.TYPE_HIRAGANA).substring(0, 1));

        //Setting the family
        String MM_index = verbDatabase.get(verbDbRowIndex)[Globals.COLUMN_MEANING_EN_INDEXES];
        List<String> MM_index_list = Arrays.asList(MM_index.split(Globals.DB_ELEMENTS_DELIMITER));
        if (MM_index_list.size() == 0) {
            return verb;
        }

        int current_MM_index;
        List<String> trans = new ArrayList<>();
        for (int i = 0; i < MM_index_list.size(); i++) {

            current_MM_index = Integer.parseInt(MM_index_list.get(i)) - 1;
            currentMeaningCharacteristics = meaningsDatabase.get(current_MM_index);

            //Getting the Family value
            types = currentMeaningCharacteristics[2].split(Globals.DB_ELEMENTS_DELIMITER);

            foundVerbType = false;
            for (String type : types) {
                lastCharIndex = type.length() - 1;
                if (type.substring(0, 1).equals("V") && (type.substring(lastCharIndex).equals("T") || type.substring(lastCharIndex).equals("I"))) {
                    trans.add(String.valueOf(type.charAt(lastCharIndex)));
                    if (i == 0) verb.setFamily(type.substring(1, lastCharIndex)); //only keeping the verb itself
                    foundVerbType = true;
                    break;
                }
            }

            if (!foundVerbType) {
                Log.i(Globals.DEBUG_TAG, "Warning! No VxxxT/I type found for verb " + verb.getRomaji() + " (Meaning index:" + current_MM_index + ")");
                //No exception catching is made here, in order to make sure that database errors are caught before production
            }
        }

        //Setting the transitive/intransitive flag
        for (int i = 0; i < trans.size(); i++) {
            if (i > 0 && !trans.get(i - 1).equals(trans.get(i))) {
                verb.setTrans("T/I");
                break;
            }
            if (i == trans.size() - 1) verb.setTrans(trans.get(i));
        }


        return verb;
    }

    //Conjugator Module utilities
    public static List<ConjugationTitle> getConjugationTitles(List<String[]> verbLatinConjDatabase, Context context) {

        String[] titlesRow = verbLatinConjDatabase.get(0);
        String[] subtitlesRow = verbLatinConjDatabase.get(1);
        String[] endingsRow = verbLatinConjDatabase.get(2);
        int sheetLength = titlesRow.length;
        List<ConjugationTitle> conjugationTitles = new ArrayList<>();
        List<ConjugationTitle.Subtitle> subtitles = new ArrayList<>();
        ConjugationTitle conjugationTitle = new ConjugationTitle();

        for (int col = 0; col < sheetLength; col++) {

            if (col == 0) {
                int titleRef = Globals.VERB_CONJUGATION_TITLES.get(titlesRow[col]);
                conjugationTitle.setTitle(context.getString(titleRef));
                conjugationTitle.setTitleIndex(col);

                ConjugationTitle.Subtitle subtitle = new ConjugationTitle.Subtitle();
                int subtitleRef = Globals.VERB_CONJUGATION_TITLES.get(subtitlesRow[col]);
                subtitle.setSubtitle(context.getString(subtitleRef));
                subtitle.setSubtitleIndex(col);
                subtitles.add(subtitle);
            } else if (col == sheetLength - 1) {
                conjugationTitle.setSubtitles(subtitles);
                conjugationTitles.add(conjugationTitle);
            } else {
                if (!titlesRow[col].equals("")) {

                    conjugationTitle.setSubtitles(subtitles);
                    conjugationTitles.add(conjugationTitle);

                    conjugationTitle = new ConjugationTitle();
                    subtitles = new ArrayList<>();

                    int titleRef = Globals.VERB_CONJUGATION_TITLES.get(titlesRow[col]);
                    conjugationTitle.setTitle(context.getString(titleRef));
                    conjugationTitle.setTitleIndex(col);

                }

                ConjugationTitle.Subtitle subtitle = new ConjugationTitle.Subtitle();
                int subtitleRef = Globals.VERB_CONJUGATION_TITLES.get(subtitlesRow[col]);
                subtitle.setSubtitle(context.getString(subtitleRef));
                subtitle.setEnding((col <= Globals.COLUMN_VERB_MASUSTEM) ? "" : endingsRow[col]);
                subtitle.setSubtitleIndex(col);
                subtitles.add(subtitle);
            }
        }

        return conjugationTitles;
    }

    @NotNull public static List<String[]> removeSpacesFromConjDb(List<String[]> db) {
        List<String[]> newDb = new ArrayList<>();
        String[] currentItems;
        int length = db.get(0).length;
        for (int i = 0; i< db.size(); i++) {
            currentItems = new String[length];
            if (Globals.COLUMN_VERB_ISTEM >= 0) System.arraycopy(db.get(i), 0, currentItems, 0, Globals.COLUMN_VERB_ISTEM);
            for (int j=Globals.COLUMN_VERB_ISTEM; j<length; j++) {
                currentItems[j] = db.get(i)[j].replace(" ", "");
            }
            newDb.add(currentItems);
        }
        return newDb;
    }
}
