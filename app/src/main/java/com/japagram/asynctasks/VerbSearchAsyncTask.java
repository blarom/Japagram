package com.japagram.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.japagram.R;
import com.japagram.data.ConjugationTitle;
import com.japagram.data.InputQuery;
import com.japagram.data.RoomCentralDatabase;
import com.japagram.data.Verb;
import com.japagram.data.Word;
import com.japagram.resources.Globals;
import com.japagram.resources.LocaleHelper;
import com.japagram.resources.Utilities;
import com.japagram.resources.UtilitiesDb;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class VerbSearchAsyncTask extends AsyncTask<Void, Void, Object[]> {

    //region Parameters
    private WeakReference<Context> contextRef;
    public VerbSearchAsyncResponseHandler listener;
    private static final int MAX_NUM_RESULTS_FOR_SURU_CONJ_SEARCH = 100;
    public static final int MATCHING_ID = 0;
    public static final int MATCHING_CATEGORY_INDEX = 1;
    public static final int MATCHING_CONJUGATION = 2;
    private List<Verb> mCompleteVerbsList;
    private final List<Word> mWordsFromDictFragment;
    private InputQuery mInputQuery;
    private String mPreparedQuery;
    private int mPreparedQueryTextType;
    private String mPreparedTranslHiragana;
    private String mPreparedTranslRomajiContatenated;
    private String mPreparedTranslHiraganaContatenated;
    private int mPreparedTranslRomajiContatenatedLength;
    private int mPreparedQueryLength;
    private String mPreparedConcatenated;
    private int mPreparedContatenatedLength;
    private RoomCentralDatabase mRoomCentralDatabase;
    private final HashMap<String, Integer> mFamilyConjugationIndexes = new HashMap<>();
    private final static int INDEX_ROMAJI = 0;
    private final static int INDEX_KANJI = 1;
    private final static int INDEX_HIRAGANA_FIRST_CHAR = 2;
    private final static int INDEX_LATIN_ROOT = 3;
    private final static int INDEX_KANJI_ROOT = 4;
    private final static int INDEX_ACTIVE_ALTSPELLING = 5;
    //endregion

    public VerbSearchAsyncTask(Context context, InputQuery inputQuery, List<Word> mWordsFromDictFragment, VerbSearchAsyncResponseHandler listener) {
        contextRef = new WeakReference<>(context);
        String preparedQuery = inputQuery.hasIngEnding()? "to " + inputQuery.getIngless() : inputQuery.getOriginalCleaned();
        this.mInputQuery = new InputQuery(preparedQuery);
        this.mWordsFromDictFragment = mWordsFromDictFragment;
        this.listener = listener;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }
    @Override protected Object[] doInBackground(Void... voids) {

        Log.i(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - Starting");
        mRoomCentralDatabase = RoomCentralDatabase.getInstance(contextRef.get());
        Log.i(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - Loaded Room Verbs Instance");

        if (mCompleteVerbsList == null || mCompleteVerbsList.size()==0) {
            mCompleteVerbsList = mRoomCentralDatabase.getAllVerbs();
        }
        Log.i(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - Loaded Verbs");

        List<Verb> matchingVerbs;
        List<Word> matchingWords;
        List<Verb> matchingVerbsSorted = new ArrayList<>();
        List<Word> matchingWordsSorted = new ArrayList<>();
        List<long[]> mMatchingVerbIdAndColList;
        if (!mInputQuery.isEmpty()) {
            setInputQueryParameters();
            getFamilyConjugationIndexes();
            String language = LocaleHelper.getLanguage(contextRef.get());
            Log.i(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - Initialized parameters");

            mMatchingVerbIdAndColList = getMatchingVerbIdsAndCols(language);
            Log.i(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - Got matchingVerbIdsAndCols");

            List<Long> ids = new ArrayList<>();
            for (long[] idsAndCols : mMatchingVerbIdAndColList) { ids.add(idsAndCols[0]); }
            matchingWords = mRoomCentralDatabase.getWordListByWordIds(ids);
            Log.i(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - Got matchingWords");

            matchingVerbs = getVerbsWithConjugations(mMatchingVerbIdAndColList, matchingWords);
            matchingWords = updateWordsWithConjMatchStatus(matchingWords, matchingVerbs);
            Log.i(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - Updated verbs with conjugations");

            List<long[]> matchingVerbIdColListSortedByLength = sortMatchingVerbIdAndColList(mMatchingVerbIdAndColList, matchingWords);
            for (int i = 0; i < matchingVerbIdColListSortedByLength.size(); i++) {
                for (int j = 0; j < matchingWords.size(); j++) {
                    Word word = matchingWords.get(j);
                    Verb verb = matchingVerbs.get(j);
                    if (word.getWordId() == matchingVerbIdColListSortedByLength.get(i)[0]) {
                        matchingWordsSorted.add(word);
                        matchingVerbsSorted.add(verb);
                        break;
                    }
                }
            }
            Log.i(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - Sorted verbs list");
        }

        List<Object[]> matchingConjugationParameters = new ArrayList<>();
        for (Verb verb : matchingVerbsSorted) {
            Object[] parameters = getConjugationParameters(verb, mPreparedQuery, mInputQuery.getRomajiSingleElement());
            matchingConjugationParameters.add(parameters);
        }

        Log.i(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - Returning objects");
        return new Object[]{matchingVerbsSorted, matchingWordsSorted, matchingConjugationParameters};
    }

    @NotNull @Contract("null, _ -> new; !null, null -> new") private List<Word> updateWordsWithConjMatchStatus(List<Word> matchingWords, List<Verb> matchingVerbs) {
        boolean foundExactMatch;
        boolean foundContainedMatch;
        if (matchingWords == null || matchingVerbs == null) return new ArrayList<>();
        for (Word word : matchingWords) {
            for (Verb verb : matchingVerbs) {
                if (verb.getVerbId() == word.getWordId()) {
                    foundExactMatch = false;
                    for (Verb.ConjugationCategory category : verb.getConjugationCategories()) {
                        for (Verb.ConjugationCategory.Conjugation conjugation : category.getConjugations()) {
                            if (conjugation.getConjugationLatin().equals(mPreparedQuery) || conjugation.getConjugationKanji().equals(mPreparedQuery)
                                    || conjugation.getConjugationLatin().equals(mPreparedConcatenated) || conjugation.getConjugationKanji().equals(mPreparedConcatenated)) {
                                foundExactMatch = true;
                                break;
                            }
                        }
                        if (foundExactMatch) break;
                    }
                    if (foundExactMatch) {
                        word.setVerbConjMatchStatus(Word.CONJ_MATCH_EXACT);
                        break;
                    }

                    foundContainedMatch = false;
                    for (Verb.ConjugationCategory category : verb.getConjugationCategories()) {
                        for (Verb.ConjugationCategory.Conjugation conjugation : category.getConjugations()) {
                            if (conjugation.getConjugationLatin().contains(mPreparedQuery) || conjugation.getConjugationKanji().contains(mPreparedQuery)
                                || conjugation.getConjugationLatin().contains(mPreparedConcatenated) || conjugation.getConjugationKanji().contains(mPreparedConcatenated)) {
                                foundContainedMatch = true;
                                break;
                            }
                        }
                        if (foundContainedMatch) break;
                    }
                    if (foundContainedMatch) {
                        word.setVerbConjMatchStatus(Word.CONJ_MATCH_CONTAINED);
                    }
                    break;
                }
            }
        }
        return matchingWords;
    }

    @NotNull private Object[] getConjugationParameters(@NotNull Verb verb, @NotNull String inputQuery, String inputQueryLatin) {

        List<Verb.ConjugationCategory> conjugationCategories = verb.getConjugationCategories();
        List<Verb.ConjugationCategory.Conjugation> conjugations;
        int matchingConjugationCategoryIndex = 0;
        String matchingConjugation = "";
        boolean foundMatch = false;
        if (!inputQuery.equals(verb.getLatinRoot()) && !inputQuery.equals(verb.getKanjiRoot())) {

            //First pass - checking for conjugations that equal the input query
            for (int i=0; i<conjugationCategories.size(); i++) {
                conjugations = conjugationCategories.get(i).getConjugations();
                for (Verb.ConjugationCategory.Conjugation conjugation : conjugations) {

                    if (mPreparedQueryTextType == Globals.TYPE_LATIN
                            && conjugation.getConjugationLatin().replace(" ", "")
                            .equals(inputQuery.replace(" ", ""))) {
                        matchingConjugation = conjugation.getConjugationLatin();
                        foundMatch = true;
                    }
                    else if ((mPreparedQueryTextType == Globals.TYPE_HIRAGANA || mPreparedQueryTextType == Globals.TYPE_KATAKANA)
                            && conjugation.getConjugationLatin().replace(" ", "")
                            .equals(inputQueryLatin.replace(" ", ""))) {
                        matchingConjugation = conjugation.getConjugationLatin();
                        foundMatch = true;
                    }
                    else if (mPreparedQueryTextType == Globals.TYPE_KANJI && conjugation.getConjugationKanji().equals(inputQuery)) {
                        matchingConjugation = conjugation.getConjugationKanji();
                        foundMatch = true;
                    }

                    if (foundMatch) break;
                }
                if (foundMatch) {
                    matchingConjugationCategoryIndex = i;
                    break;
                }
            }

            //Second pass - if index is still 0, checking for conjugations that contain the input query
            if (matchingConjugationCategoryIndex == 0) {
                for (int i=0; i<conjugationCategories.size(); i++) {
                    conjugations = conjugationCategories.get(i).getConjugations();
                    for (Verb.ConjugationCategory.Conjugation conjugation : conjugations) {

                        if (mPreparedQueryTextType == Globals.TYPE_LATIN && conjugation.getConjugationLatin().contains(inputQuery)) {
                            matchingConjugation = conjugation.getConjugationLatin();
                            foundMatch = true;
                        }
                        else if ((mPreparedQueryTextType == Globals.TYPE_HIRAGANA || mPreparedQueryTextType == Globals.TYPE_KATAKANA)
                                && conjugation.getConjugationLatin().contains(inputQueryLatin)) {
                            matchingConjugation = conjugation.getConjugationLatin();
                            foundMatch = true;
                        }
                        else if (mPreparedQueryTextType == Globals.TYPE_KANJI && conjugation.getConjugationKanji().contains(inputQuery)) {
                            matchingConjugation = conjugation.getConjugationKanji();
                            foundMatch = true;
                        }

                        if (foundMatch) break;
                    }
                    if (foundMatch) {
                        matchingConjugationCategoryIndex = i;
                        break;
                    }
                }
            }
        }

        Object[] parameters = new Object[3];
        parameters[MATCHING_ID] = verb.getVerbId();
        parameters[MATCHING_CATEGORY_INDEX] = matchingConjugationCategoryIndex;
        parameters[MATCHING_CONJUGATION] = matchingConjugation;

        return parameters;
    }
    private void setInputQueryParameters() {
        mPreparedQuery = mInputQuery.getOriginal();
        mPreparedQueryLength = mInputQuery.getOriginal().length();
        mPreparedQueryTextType = mInputQuery.getType();
        mPreparedConcatenated = Utilities.removeSpecialCharacters(mPreparedQuery);
        mPreparedContatenatedLength = mPreparedConcatenated.length();
        mPreparedTranslRomajiContatenated = Utilities.removeSpecialCharacters(mInputQuery.getRomajiSingleElement());
        mPreparedTranslRomajiContatenatedLength = mPreparedTranslRomajiContatenated.length();
        mPreparedTranslHiragana = mInputQuery.getHiraganaSingleElement();
        mPreparedTranslHiraganaContatenated = Utilities.removeSpecialCharacters(mPreparedTranslHiragana);
    }
    private void getFamilyConjugationIndexes() {
        for (int rowIndex = 3; rowIndex < Globals.VerbLatinConjDatabase.size(); rowIndex++) {

            if (Globals.VerbLatinConjDatabase.get(rowIndex)[0].equals("") || !Globals.VerbLatinConjDatabase.get(rowIndex)[1].equals("")) continue;

            if ("su godan".equals(Globals.VerbLatinConjDatabase.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_SU_GODAN)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_SU_GODAN, rowIndex);
            } else if ("ku godan".equals(Globals.VerbLatinConjDatabase.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_KU_GODAN)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_KU_GODAN, rowIndex);
            } else if ("iku special class".equals(Globals.VerbLatinConjDatabase.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_IKU_SPECIAL)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_IKU_SPECIAL, rowIndex);
            } else if ("yuku special class".equals(Globals.VerbLatinConjDatabase.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_YUKU_SPECIAL)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_YUKU_SPECIAL, rowIndex);
            } else if ("gu godan".equals(Globals.VerbLatinConjDatabase.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_GU_GODAN)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_GU_GODAN, rowIndex);
            } else if ("bu godan".equals(Globals.VerbLatinConjDatabase.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_BU_GODAN)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_BU_GODAN, rowIndex);
            } else if ("mu godan".equals(Globals.VerbLatinConjDatabase.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_MU_GODAN)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_MU_GODAN, rowIndex);
            } else if ("nu godan".equals(Globals.VerbLatinConjDatabase.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_NU_GODAN)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_NU_GODAN, rowIndex);
            } else if ("ru godan".equals(Globals.VerbLatinConjDatabase.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_RU_GODAN)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_RU_GODAN, rowIndex);
            } else if ("aru special class".equals(Globals.VerbLatinConjDatabase.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_ARU_SPECIAL)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_ARU_SPECIAL, rowIndex);
            } else if ("tsu godan".equals(Globals.VerbLatinConjDatabase.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_TSU_GODAN)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_TSU_GODAN, rowIndex);
            } else if ("u godan".equals(Globals.VerbLatinConjDatabase.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_U_GODAN)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_U_GODAN, rowIndex);
            } else if ("u special class".equals(Globals.VerbLatinConjDatabase.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_U_SPECIAL)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_U_SPECIAL, rowIndex);
            } else if ("ru ichidan".equals(Globals.VerbLatinConjDatabase.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_RU_ICHIDAN)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_RU_ICHIDAN, rowIndex);
            } else if ("desu copula".equals(Globals.VerbLatinConjDatabase.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_DA)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_DA, rowIndex);
            } else if ("kuru verb".equals(Globals.VerbLatinConjDatabase.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_KURU)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_KURU, rowIndex);
            } else if ("suru verb".equals(Globals.VerbLatinConjDatabase.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_SURU)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_SURU, rowIndex);
            }
        }
    }
    @NotNull private String[] getVerbCharacteristicsFromAltSpelling(String trimmedAltSpelling, Verb verb) {

        String[] characteristics = new String[6];

        int altSpellingType = InputQuery.getTextType(trimmedAltSpelling);

        if (altSpellingType != mPreparedQueryTextType) return new String[]{};

        characteristics[INDEX_ROMAJI] = (altSpellingType == Globals.TYPE_LATIN)? trimmedAltSpelling : verb.getRomaji();
        characteristics[INDEX_KANJI] = (altSpellingType == Globals.TYPE_KANJI)? trimmedAltSpelling : verb.getKanji();
        characteristics[INDEX_HIRAGANA_FIRST_CHAR] =
                (altSpellingType == Globals.TYPE_HIRAGANA) ? trimmedAltSpelling.substring(0,1) :
                        InputQuery.getWaapuroHiraganaKatakana(characteristics[INDEX_ROMAJI]).get(Globals.TYPE_HIRAGANA).substring(0,1);
        characteristics[INDEX_LATIN_ROOT] = Utilities.getVerbRoot(characteristics[INDEX_ROMAJI], verb.getFamily(), Globals.TYPE_LATIN);
        characteristics[INDEX_KANJI_ROOT] = Utilities.getVerbRoot(characteristics[INDEX_KANJI], verb.getFamily(), Globals.TYPE_KANJI);
        characteristics[INDEX_ACTIVE_ALTSPELLING] = trimmedAltSpelling;

        return characteristics;
    }
    @NotNull
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    private List<long[]> getMatchingVerbIdsAndCols(String language) {

        if (mPreparedQueryTextType == Globals.TYPE_INVALID || mCompleteVerbsList==null) return new ArrayList<>();
        Log.i(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - getMatchingVerbIdsAndCols - Starting");

        //region Initializations
        int NumberOfSheetCols = Globals.VerbLatinConjDatabase.get(0).length;
        List<Integer> dilutedConjugationColIndexes = new ArrayList<>();
        boolean queryIsContainedInNormalFamilyConjugation;
        boolean queryIsContainedInAKuruConjugation;
        boolean queryIsContainedInASuruConjugation;
        boolean queryIsContainedInADesuConjugation;
        boolean queryIsContainedInIruVerbConjugation;
        int exceptionIndex;
        String[] currentFamilyConjugations;
        String[] currentConjugations;
        String family;
        String romaji;
        String altSpellingsAsString;
        List<String[]> verbSearchCandidates;
        char hiraganaFirstChar;
        String latinRoot;
        String kanjiRoot;
        String conjugationValue = "";
        boolean foundMatch;
        boolean allowExpandedConjugationsComparison;
        int matchColumn = 0;
        boolean onlyRetrieveShortRomajiVerbs = false;
        boolean onlyRetrieveEnglishWords;
        //endregion

        //region Taking care of the case where the input is a basic conjugation that will cause the app to return too many verbs
        queryIsContainedInNormalFamilyConjugation = false;
        queryIsContainedInASuruConjugation = false;
        queryIsContainedInAKuruConjugation = false;
        queryIsContainedInADesuConjugation = false;
        queryIsContainedInIruVerbConjugation = false;
        int familyIndex;
        String currentFamilyConj;
        for (String key : mFamilyConjugationIndexes.keySet()) {
            familyIndex = mFamilyConjugationIndexes.get(key);
            switch (key) {
                case Globals.VERB_FAMILY_DA:
                    currentFamilyConjugations = Globals.VerbLatinConjDatabase.get(familyIndex);
                    for (int column = 1; column < NumberOfSheetCols; column++) {
                        currentFamilyConj = currentFamilyConjugations[column];
                        if (currentFamilyConj.equals(mPreparedConcatenated) || currentFamilyConj.equals(mPreparedTranslHiraganaContatenated)) {
                            queryIsContainedInADesuConjugation = true;
                            break;
                        }
                    }
                    if (queryIsContainedInADesuConjugation) break;
                    currentFamilyConjugations = Globals.VerbKanjiConjDatabase.get(familyIndex);
                    for (int column = 1; column < NumberOfSheetCols; column++) {
                        currentFamilyConj = currentFamilyConjugations[column];
                        if (currentFamilyConj.equals(mPreparedConcatenated)) {
                            queryIsContainedInADesuConjugation = true;
                            break;
                        }
                    }
                    break;
                case Globals.VERB_FAMILY_KURU:
                    currentFamilyConjugations = Globals.VerbLatinConjDatabase.get(familyIndex);
                    for (int column = 1; column < NumberOfSheetCols; column++) {
                        currentFamilyConj = currentFamilyConjugations[column];
                        if (currentFamilyConj.equals(mPreparedConcatenated) || currentFamilyConj.equals(mPreparedTranslHiraganaContatenated)) {
                            queryIsContainedInAKuruConjugation = true;
                            break;
                        }
                    }
                    if (queryIsContainedInAKuruConjugation) break;
                    currentFamilyConjugations = Globals.VerbKanjiConjDatabase.get(familyIndex);
                    for (int column = 1; column < NumberOfSheetCols; column++) {
                        currentFamilyConj = currentFamilyConjugations[column];
                        if (currentFamilyConj.equals(mPreparedConcatenated)) {
                            queryIsContainedInAKuruConjugation = true;
                            break;
                        }
                    }
                    break;
                case Globals.VERB_FAMILY_SURU:
                    currentFamilyConjugations = Globals.VerbLatinConjDatabase.get(familyIndex);
                    for (int column = 1; column < NumberOfSheetCols; column++) {
                        currentFamilyConj = currentFamilyConjugations[column];
                        if (currentFamilyConj.equals(mPreparedConcatenated) || currentFamilyConj.equals(mPreparedTranslHiraganaContatenated)) {
                            queryIsContainedInASuruConjugation = true;
                            break;
                        }
                    }
                    if (queryIsContainedInASuruConjugation) break;
                    currentFamilyConjugations = Globals.VerbKanjiConjDatabase.get(familyIndex);
                    for (int column = 1; column < NumberOfSheetCols; column++) {
                        currentFamilyConj = currentFamilyConjugations[column];
                        if (currentFamilyConj.equals(mPreparedConcatenated)) {
                            queryIsContainedInASuruConjugation = true;
                            break;
                        }
                    }
                    break;
                case Globals.VERB_FAMILY_RU_ICHIDAN:
                    currentFamilyConjugations = Globals.VerbLatinConjDatabase.get(familyIndex);
                    String currentConjugation;
                    for (int column = Globals.COLUMN_VERB_ISTEM; column < NumberOfSheetCols; column++) {
                        currentConjugation = "i" + currentFamilyConjugations[column];
                        if (currentConjugation.contains(mPreparedConcatenated) || currentConjugation.contains(mPreparedTranslHiraganaContatenated)) {
                            queryIsContainedInIruVerbConjugation = true;
                            break;
                        }
                    }
                    break;
                default:
                    currentFamilyConjugations = Globals.VerbLatinConjDatabase.get(familyIndex);
                    for (int column = Globals.COLUMN_VERB_ISTEM; column < NumberOfSheetCols; column++) {
                        currentFamilyConj = currentFamilyConjugations[column];
                        if (currentFamilyConj.contains(mPreparedConcatenated) || currentFamilyConj.contains(mPreparedTranslHiraganaContatenated)) {
                            queryIsContainedInNormalFamilyConjugation = true;
                            break;
                        }
                    }
                    if (queryIsContainedInNormalFamilyConjugation) break;
                    currentFamilyConjugations = Globals.VerbKanjiConjDatabase.get(familyIndex);
                    for (int column = Globals.COLUMN_VERB_ISTEM; column < NumberOfSheetCols; column++) {
                        currentFamilyConj = currentFamilyConjugations[column];
                        if (currentFamilyConj.contains(mPreparedConcatenated)) {
                            queryIsContainedInNormalFamilyConjugation = true;
                            break;
                        }
                    }
                    break;
            }
        }
        if (queryIsContainedInASuruConjugation || queryIsContainedInAKuruConjugation || queryIsContainedInADesuConjugation) queryIsContainedInNormalFamilyConjugation = false;
        //endregion

        //region Limiting search functionality for short inputs or non-japanese words
        if (mPreparedQueryTextType == Globals.TYPE_LATIN && mPreparedConcatenated.length() < 4
                || (mPreparedQueryTextType == Globals.TYPE_HIRAGANA || mPreparedQueryTextType == Globals.TYPE_KATAKANA) && mPreparedConcatenated.length() < 3) {
            onlyRetrieveShortRomajiVerbs = true;
        }
        onlyRetrieveEnglishWords = mPreparedQueryTextType == Globals.TYPE_LATIN && mPreparedTranslHiragana.contains("*");
        //endregion

        //region Performing column dilution in order to make the search more efficient (the diluted column ArrayList is used in the Search Algorithm)
        int queryLengthForDilution = 0;
        List<String[]> verbConjugationMaxLengths = new ArrayList<>();
        int conjugationMaxLength;
        if (mPreparedQueryTextType == Globals.TYPE_LATIN) {
            verbConjugationMaxLengths = Utilities.readCSVFileFirstRow("LineVerbsLengths - 3000 kanji.csv", contextRef.get());
            queryLengthForDilution = mPreparedContatenatedLength;
        }
        else if (mPreparedQueryTextType == Globals.TYPE_HIRAGANA || mPreparedQueryTextType == Globals.TYPE_KATAKANA) {
            verbConjugationMaxLengths = Utilities.readCSVFileFirstRow("LineVerbsLengths - 3000 kanji.csv", contextRef.get());
            queryLengthForDilution = mPreparedTranslRomajiContatenatedLength;
        }
        else if (mPreparedQueryTextType == Globals.TYPE_KANJI) {
            verbConjugationMaxLengths = Utilities.readCSVFileFirstRow("LineVerbsKanjiLengths - 3000 kanji.csv", contextRef.get());
            queryLengthForDilution = mPreparedContatenatedLength;
        }

        for (int col = Globals.COLUMN_VERB_ISTEM; col < NumberOfSheetCols; col++) {
            if (!verbConjugationMaxLengths.get(0)[col].equals(""))
                conjugationMaxLength = Integer.parseInt(verbConjugationMaxLengths.get(0)[col]);
            else conjugationMaxLength = 0;

            if (conjugationMaxLength >= queryLengthForDilution) dilutedConjugationColIndexes.add(col);
        }
        //endregion

        Log.i(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - getMatchingVerbIdsAndCols - Initialized");

        //region Getting the matching words from the Words database and filtering for verbs.
        //For words of length>=4, The matches are determined by the word's keywords list.
        List<Word> mMatchingWords;
        if (mWordsFromDictFragment == null) {
            List<Long> mMatchingWordIds = (List<Long>) UtilitiesDb.getMatchingWordIdsAndDoBasicFiltering(mInputQuery, language, false, contextRef.get())[0];
            mMatchingWords = mRoomCentralDatabase.getWordListByWordIds(mMatchingWordIds);
        } else {
            mMatchingWords = mWordsFromDictFragment;
        }
        String type;
        List<long[]> matchingVerbIdsAndColsFromBasicCharacteristics = new ArrayList<>();
        int counter = 0;
        for (Word word : mMatchingWords) {
            type = word.getMeaningsEN().get(0).getType();

            //Preventing the input query being a suru verb conjugation from overloading the results
            if (queryIsContainedInASuruConjugation && word.getRomaji().contains(" suru")) {
                if (counter > MAX_NUM_RESULTS_FOR_SURU_CONJ_SEARCH) break;
                counter++;
            }

            //Adding the word id to the candidates
            if (type.length() > 0 && type.substring(0,1).equals("V") && !type.contains("VC")) {
                matchingVerbIdsAndColsFromBasicCharacteristics.add(new long[]{word.getWordId(), 0});
            }
        }
        //endregion

        //region Adding the suru verb if the query is contained in the suru conjugations, and limiting total results
        if (queryIsContainedInASuruConjugation) {
            Word suruVerb = mRoomCentralDatabase.getWordsByExactRomajiAndKanjiMatch("suru", "為る").get(0);
            boolean alreadyInList = false;
            for (long[] idAndCol : matchingVerbIdsAndColsFromBasicCharacteristics) {
                if (idAndCol[0] == suruVerb.getWordId()) {
                    alreadyInList = true;
                    break;
                }
            }
            if (!alreadyInList) matchingVerbIdsAndColsFromBasicCharacteristics.add(new long[]{suruVerb.getWordId(), 0});
        }
        //endregion

        Log.i(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - getMatchingVerbIdsAndCols - got matching words");

        //region Getting the matching verbs according to the expanded conjugations and updating the conjugation roots if an altSpelling is used
        List<long[]> matchingVerbIdsAndColsFromExpandedConjugations = new ArrayList<>();
        List<long[]> copyOfMatchingVerbIdsAndColsFromBasicCharacteristics = new ArrayList<>(matchingVerbIdsAndColsFromBasicCharacteristics);
        boolean verbAlreadyFound;
        String trimmedAltSpelling;
        for (Verb verb : mCompleteVerbsList) {

            //region Skipping verbs that were already found
            verbAlreadyFound = false;
            for (long[] idAndCol : copyOfMatchingVerbIdsAndColsFromBasicCharacteristics) {
                if (idAndCol[0] == verb.getVerbId()) {

                    //Update the active fields for the current verb according to the altSpelling
                    boolean foundAltSpelling = false;
                    for (String altSpelling : verb.getAltSpellings().split(",")) {
                        trimmedAltSpelling = altSpelling.trim();
                        if (trimmedAltSpelling.equals(mPreparedTranslRomajiContatenated) || trimmedAltSpelling.equals(mPreparedTranslHiraganaContatenated)) {
                            String[] characteristics = getVerbCharacteristicsFromAltSpelling(trimmedAltSpelling, verb);
                            if (characteristics.length == 0) continue;
                            verb.setActiveLatinRoot(characteristics[INDEX_LATIN_ROOT]);
                            verb.setActiveKanjiRoot(characteristics[INDEX_KANJI_ROOT]);
                            verb.setActiveAltSpelling(trimmedAltSpelling);
                            foundAltSpelling = true;
                            break;
                        }
                    }
                    if (!foundAltSpelling) {
                        verb.setActiveLatinRoot(verb.getLatinRoot());
                        verb.setActiveKanjiRoot(verb.getKanjiRoot());
                        verb.setActiveAltSpelling(verb.getRomaji());
                    }
                    mRoomCentralDatabase.updateVerb(verb);

                    //Remove the verb from the candidates list since it is already in the final list
                    copyOfMatchingVerbIdsAndColsFromBasicCharacteristics.remove(idAndCol);
                    verbAlreadyFound = true;
                    break;
                }
            }
            if (verbAlreadyFound) continue;
            //endregion

            //region Loop starting parameters initialization
            foundMatch = false;
            allowExpandedConjugationsComparison = !onlyRetrieveEnglishWords;
            //endregion

            //region Building the list of relevant base characteristics that the algorithm will check
            //This includes the romaji/kanji/romajiroot/kanjiroot/kana1stchar, also also the altSpelling equivalents
            altSpellingsAsString = verb.getAltSpellings();
            family = verb.getFamily();
            exceptionIndex = (verb.getExceptionIndex().equals(""))? 0 : Integer.parseInt(verb.getExceptionIndex());

            verbSearchCandidates = new ArrayList<>();
            String[] characteristics = new String[6];
            characteristics[INDEX_ROMAJI] = verb.getRomaji();
            characteristics[INDEX_KANJI] = verb.getKanji();
            characteristics[INDEX_HIRAGANA_FIRST_CHAR] = verb.getHiraganaFirstChar();
            characteristics[INDEX_LATIN_ROOT] = verb.getLatinRoot();
            characteristics[INDEX_KANJI_ROOT] = verb.getKanjiRoot();
            characteristics[INDEX_ACTIVE_ALTSPELLING] = verb.getRomaji();
            verbSearchCandidates.add(characteristics);

            for (String altSpelling : altSpellingsAsString.split(",")) {

                //Initializations
                trimmedAltSpelling = altSpelling.trim();
                if (trimmedAltSpelling.equals("")) continue;

                characteristics = getVerbCharacteristicsFromAltSpelling(trimmedAltSpelling, verb);
                if (characteristics.length == 0) continue;

                verbSearchCandidates.add(characteristics);
            }
            //endregion

            //region Checking if one of the relevant base words gets a match, and registering it in the match list
            for (String[] verbSearchCandidate : verbSearchCandidates) {

                //region Getting the verb characteristics
                romaji = verbSearchCandidate[INDEX_ROMAJI];
                hiraganaFirstChar = verbSearchCandidate[INDEX_HIRAGANA_FIRST_CHAR].charAt(0);
                latinRoot = verbSearchCandidate[INDEX_LATIN_ROOT].replace(" ","");
                kanjiRoot = verbSearchCandidate[INDEX_KANJI_ROOT];
                //endregion

                //region Only allowing searches on verbs that satisfy the following conditions (including identical 1st char, kuru/suru/da, query length)
                if (    !(     ( mPreparedQueryTextType == Globals.TYPE_LATIN && romaji.charAt(0) == mPreparedConcatenated.charAt(0) )
                            || ( (mPreparedQueryTextType == Globals.TYPE_HIRAGANA || mPreparedQueryTextType == Globals.TYPE_KATAKANA)
                                && (hiraganaFirstChar == mPreparedTranslHiragana.charAt(0)) )
                            || (mPreparedQueryTextType == Globals.TYPE_KANJI && kanjiRoot.contains(mPreparedConcatenated.substring(0,1)))
                            || romaji.contains("kuru")
                            || romaji.equals("suru")
                            || romaji.equals("da") )
                        || (mPreparedQueryTextType == Globals.TYPE_LATIN && mPreparedConcatenated.length() < 4 && !romaji.contains(mPreparedConcatenated))
                        || ((mPreparedQueryTextType == Globals.TYPE_HIRAGANA || mPreparedQueryTextType == Globals.TYPE_KATAKANA)
                            && mPreparedConcatenated.length() < 3 && !romaji.contains(mPreparedTranslRomajiContatenated))
                        || (mPreparedQueryTextType == Globals.TYPE_KANJI && mPreparedConcatenated.length() < 3 && kanjiRoot.length()>0 && !mPreparedConcatenated.contains(kanjiRoot))
                        || (onlyRetrieveShortRomajiVerbs && romaji.length() > 4)     ) {
                    continue;
                }
                //endregion

                //region If the verb is equal to a family conjugation, only roots with length 1 (ie. iru/aru/eru/oru/uru verbs only) or a verb with the exact romaji value are considered. This prevents too many results. This does not conflict with da or kuru.
                if (        queryIsContainedInNormalFamilyConjugation && latinRoot.length() > 1
                        ||  queryIsContainedInASuruConjugation && !(kanjiRoot.equals("為"))
                        ||  queryIsContainedInAKuruConjugation && !romaji.contains("kuru")
                        ||  queryIsContainedInADesuConjugation && !romaji.equals("da")
                        || queryIsContainedInIruVerbConjugation && !romaji.equals("iru")) {

                    //If the input is suru then prevent verbs with suru in the conjugations from giving a hit, but allow other verbs with romaji suru to give a hit
                    if (romaji.contains(" suru")) {
                        allowExpandedConjugationsComparison = false;
                    }
                    //Otherwise, if the verb does not meet the above conditions, skip this verb
                    else {
                        continue;
                    }
                }
                //endregion

                //region Main Comparator Algorithm
                if (allowExpandedConjugationsComparison) {
                    if (!mFamilyConjugationIndexes.containsKey(family)) continue;
                    boolean hasConjExceptions = exceptionIndex != mFamilyConjugationIndexes.get(family);
                    String currentConj;

                    //region Latin conjugations comparison
                    if (mPreparedQueryTextType == Globals.TYPE_LATIN) {

                        //There's no point in checking again if the input query is part of the family conjugation,
                        // so we only check up to the substring that could contain all but the first char of the input query
                        // No check is needed for len(conjugation) < maxCharIndexWhereMatchIsExpected, since we're using only columns diluted by total verb length > mInputQueryContatenatedLength
                        int maxCharIndexWhereMatchIsExpected = mPreparedContatenatedLength - 1;
                        currentConjugations = Globals.VerbLatinConjDatabaseNoSpaces.get(exceptionIndex);
                        if (hasConjExceptions) {
                            currentFamilyConjugations = Globals.VerbLatinConjDatabaseNoSpaces.get(mFamilyConjugationIndexes.get(family));
                            for (int col : dilutedConjugationColIndexes) {
                                currentConj = currentFamilyConjugations[col];
                                if (currentConj.equals("")) {
                                    conjugationValue = latinRoot + ((currentConj.length() > maxCharIndexWhereMatchIsExpected)? currentConj.substring(0, maxCharIndexWhereMatchIsExpected) : currentConj);
                                } else conjugationValue = currentConjugations[col];

                                if (conjugationValue.contains(mPreparedConcatenated)) {
                                    foundMatch = true;
                                    matchColumn = col;
                                    break;
                                }
                            }
                        }
                        else {
                            for (int col : dilutedConjugationColIndexes) {
                                currentConj = currentConjugations[col];
                                conjugationValue = latinRoot + ((currentConj.length() > maxCharIndexWhereMatchIsExpected)? currentConj.substring(0, maxCharIndexWhereMatchIsExpected) : currentConj);

                                if (conjugationValue.contains(mPreparedConcatenated)) {
                                    foundMatch = true;
                                    matchColumn = col;
                                    break;
                                }
                            }
                        }
                    }
                    //endregion

                    //region Kana conjugations comparison
                    else if (mPreparedQueryTextType == Globals.TYPE_HIRAGANA || mPreparedQueryTextType == Globals.TYPE_KATAKANA) {

                        //There's no point in checking again if the input query is part of the family conjugation,
                        // so we only check up to the substring that could contain all but the first char of the input query
                        // No check is needed for len(conjugation) < maxCharIndexWhereMatchIsExpected, since we're using only columns diluted by total verb length > mInputQueryContatenatedLength
                        int maxCharIndexWhereMatchIsExpected = mPreparedTranslRomajiContatenatedLength - 1;
                        currentConjugations = Globals.VerbLatinConjDatabaseNoSpaces.get(exceptionIndex);
                        if (hasConjExceptions) {
                            currentFamilyConjugations = Globals.VerbLatinConjDatabaseNoSpaces.get(mFamilyConjugationIndexes.get(family));
                            for (int col : dilutedConjugationColIndexes) {
                                currentConj = currentFamilyConjugations[col];
                                if (currentConj.equals("")) {
                                    conjugationValue = latinRoot + ((currentConj.length() > maxCharIndexWhereMatchIsExpected)? currentConj.substring(0, maxCharIndexWhereMatchIsExpected) : currentConj);
                                } else conjugationValue = currentConjugations[col];

                                if (conjugationValue.contains(mPreparedTranslRomajiContatenated)) {
                                    foundMatch = true;
                                    matchColumn = col;
                                    break;
                                }
                            }
                        }
                        else {
                            for (int col : dilutedConjugationColIndexes) {
                                currentConj = currentConjugations[col];
                                conjugationValue = latinRoot + ((currentConj.length() > maxCharIndexWhereMatchIsExpected)? currentConj.substring(0, maxCharIndexWhereMatchIsExpected) : currentConj);

                                if (conjugationValue.contains(mPreparedTranslRomajiContatenated)) {
                                    foundMatch = true;
                                    matchColumn = col;
                                    break;
                                }
                            }
                        }
                    }
                    //endregion

                    //region Kanji conjugations comparison
                    else if (mPreparedQueryTextType == Globals.TYPE_KANJI) {

                        //There's no point in checking again if the input query is part of the family conjugation,
                        // so we only check up to the substring that could contain all but the first char of the input query
                        // No check is needed for len(conjugation) < maxCharIndexWhereMatchIsExpected, since we're using only columns diluted by total verb length > mInputQueryContatenatedLength
                        int maxCharIndexWhereMatchIsExpected = mPreparedQueryLength - 1;
                        currentConjugations = Globals.VerbKanjiConjDatabase.get(exceptionIndex);
                        if (hasConjExceptions) {
                            currentFamilyConjugations = Globals.VerbKanjiConjDatabase.get(mFamilyConjugationIndexes.get(family));
                            for (int col : dilutedConjugationColIndexes) {
                                currentConj = currentFamilyConjugations[col];
                                if (currentConj.equals("")) {
                                    conjugationValue = kanjiRoot + ((currentConj.length() > maxCharIndexWhereMatchIsExpected)? currentConj.substring(0, maxCharIndexWhereMatchIsExpected) : currentConj);
                                } else conjugationValue = currentConjugations[col];

                                if (conjugationValue.contains(mPreparedQuery)) {
                                    foundMatch = true;
                                    matchColumn = col;
                                    break;
                                }
                            }
                        }
                        else {
                            for (int col : dilutedConjugationColIndexes) {
                                currentConj = currentConjugations[col];
                                conjugationValue = kanjiRoot + ((currentConj.length() > maxCharIndexWhereMatchIsExpected)? currentConj.substring(0, maxCharIndexWhereMatchIsExpected) : currentConj);

                                if (conjugationValue.contains(mPreparedQuery)) {
                                    foundMatch = true;
                                    matchColumn = col;
                                    break;
                                }
                            }
                        }
                    }
                    //endregion
                }
                //endregion

                if (foundMatch) {
                    //If a match was found for an altSpelling, update the relevant fields
                    verb.setActiveLatinRoot(latinRoot);
                    verb.setActiveKanjiRoot(kanjiRoot);
                    verb.setActiveAltSpelling(verbSearchCandidate[INDEX_ACTIVE_ALTSPELLING]);
                    mRoomCentralDatabase.updateVerb(verb);

                    //Update the list of match ids
                    matchingVerbIdsAndColsFromExpandedConjugations.add(new long[]{verb.getVerbId(), matchColumn});

                    break;
                }
            }
            //endregion
        }
        //endregion

        Log.i(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - getMatchingVerbIdsAndCols - got matching verbs");

        List<long[]> matchingVerbIdsAndCols = new ArrayList<>();
        matchingVerbIdsAndCols.addAll(matchingVerbIdsAndColsFromBasicCharacteristics);
        matchingVerbIdsAndCols.addAll(matchingVerbIdsAndColsFromExpandedConjugations);

        Log.i(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - getMatchingVerbIdsAndCols - Finished");
        return matchingVerbIdsAndCols;
    }
    @NotNull private List<long[]> sortMatchingVerbIdAndColList(List<long[]> matchingVerbIdsAndCols, List<Word> matchingWords) {

        List<long[]> matchingVerbIdLengthColList = new ArrayList<>();

        boolean queryIsVerbWithTo = mInputQuery.getIsVerbWithTo();

        //region Replacing the Kana input word by its romaji equivalent
        String inputQuery = mPreparedQuery.toLowerCase();
        int textType = InputQuery.getTextType(inputQuery);
        if (textType == Globals.TYPE_HIRAGANA || textType == Globals.TYPE_KATAKANA) {
            List<String> translationList = InputQuery.getWaapuroHiraganaKatakana(inputQuery.replace(" ", ""));
            inputQuery = translationList.get(0);
        }
        //endregion

        for (int i = 0; i < matchingVerbIdsAndCols.size(); i++) {

            Word currentWord = null;
            for (Word word : matchingWords) {
                if (word.getWordId() == matchingVerbIdsAndCols.get(i)[0]) {
                    currentWord = word;
                    break;
                }
            }
            if (currentWord == null) continue;

            String language = LocaleHelper.getLanguage(contextRef.get());
            int ranking = UtilitiesDb.getRankingFromWordAttributes(currentWord, inputQuery, queryIsVerbWithTo, language);

            long[] currentMatchingVerbIdLengthCol = new long[3];
            currentMatchingVerbIdLengthCol[0] = matchingVerbIdsAndCols.get(i)[0];
            currentMatchingVerbIdLengthCol[1] = ranking;
            currentMatchingVerbIdLengthCol[2] = matchingVerbIdsAndCols.get(i)[1];

            matchingVerbIdLengthColList.add(currentMatchingVerbIdLengthCol);
        }

        //Sort the results according to total length
        if (matchingVerbIdLengthColList.size() != 0) {
            matchingVerbIdLengthColList = UtilitiesDb.bubbleSortForThreeIntegerList(matchingVerbIdLengthColList);
        }

        List<long[]> matchingVerbIdColListSorted = new ArrayList<>();
        for (long[] element : matchingVerbIdLengthColList) {
            matchingVerbIdColListSorted.add(new long[]{element[0],element[2]});
        }

        return matchingVerbIdColListSorted;

    }
    @NotNull @SuppressWarnings("ConstantConditions")  private List<Verb> getVerbsWithConjugations(@NotNull List<long[]> matchingVerbIdAndColList, List<Word> matchingWords) {

        if (matchingVerbIdAndColList.size() == 0) return new ArrayList<>();

        //region Initializations
        List<Verb> verbs = new ArrayList<>();
        Word currentWord;
        Verb currentVerb;
        long matchingVerbId;
        int conjLength;
        int NumberOfSheetCols = Globals.VerbLatinConjDatabase.get(0).length;
        String[] currentConjugationsRowLatin;
        String[] currentConjugationsRowKanji;
        List<Verb.ConjugationCategory> conjugationCategories;
        Verb.ConjugationCategory conjugationCategory;
        List<Verb.ConjugationCategory.Conjugation> conjugations;
        Verb.ConjugationCategory.Conjugation conjugation;
        List<String> conjugationSetLatin;
        List<String> conjugationSetKanji;
        List<ConjugationTitle.Subtitle> subtitles;
        int currentFamilyConjugationsIndex;
        String[] currentConjugationExceptionsRowLatin;
        String[] currentConjugationExceptionsRowKanji;

        List<Long> ids = new ArrayList<>();
        for (long[] idsAndCols : matchingVerbIdAndColList) { ids.add(idsAndCols[0]); }
        List<Verb> matchingVerbsBeforeOrderingByWordId = mRoomCentralDatabase.getVerbListByVerbIds(ids);
        List<Verb> matchingVerb = new ArrayList<>();
        for (Word word : matchingWords) {
            for (Verb verb : matchingVerbsBeforeOrderingByWordId) {
                if (verb.getVerbId() == word.getWordId()) {
                    matchingVerb.add(verb);
                    break;
                }
            }
        }
        //endregion

        //region Updating the verbs with their conjugations
        for (int t = 0; t < matchingVerbIdAndColList.size(); t++) {
            matchingVerbId = matchingVerbIdAndColList.get(t)[0];
            currentVerb = matchingVerb.get(t);
            currentWord = matchingWords.get(t);
            if (currentWord == null || currentVerb == null || !mFamilyConjugationIndexes.containsKey(currentVerb.getFamily())) continue;
            currentFamilyConjugationsIndex = mFamilyConjugationIndexes.get(currentVerb.getFamily());
            currentConjugationsRowLatin = Arrays.copyOf(Globals.VerbLatinConjDatabase.get(currentFamilyConjugationsIndex), NumberOfSheetCols);
            currentConjugationsRowKanji = Arrays.copyOf(Globals.VerbKanjiConjDatabase.get(currentFamilyConjugationsIndex), NumberOfSheetCols);

            //region Setting the verb's basic characteristics for display
            List<Word.Meaning> meanings;
            String language = "";
            switch (LocaleHelper.getLanguage(contextRef.get())) {
                case Globals.LANG_STR_EN:
                    language = contextRef.get().getResources().getString(R.string.language_label_english).toLowerCase();
                    meanings = currentWord.getMeaningsEN();
                    break;
                case Globals.LANG_STR_FR:
                    language = contextRef.get().getResources().getString(R.string.language_label_french).toLowerCase();
                    meanings = currentWord.getMeaningsFR();
                    break;
                case Globals.LANG_STR_ES:
                    language = contextRef.get().getResources().getString(R.string.language_label_spanish).toLowerCase();
                    meanings = currentWord.getMeaningsES();
                    break;
                default: meanings = currentWord.getMeaningsEN();
            }
            String extract = "";
            if (meanings == null || meanings.size() == 0) {
                meanings = currentWord.getMeaningsEN();
                extract += "["
                        + contextRef.get().getString(R.string.meanings_in)
                        + " "
                        + language.toLowerCase()
                        + " "
                        + contextRef.get().getString(R.string.unavailable)
                        + "] ";
            }
            extract += Utilities.removeDuplicatesFromCommaList(Utilities.getMeaningsExtract(meanings, Globals.BALANCE_POINT_REGULAR_DISPLAY));
            currentVerb.setMeaning(extract);

            switch (currentVerb.getTrans()) {
                case "T": currentVerb.setTrans(contextRef.get().getString(R.string.trans_)); break;
                case "I": currentVerb.setTrans(contextRef.get().getString(R.string.intrans_)); break;
                case "T/I": currentVerb.setTrans(contextRef.get().getString(R.string.trans_intrans_)); break;
            }

            if (Globals.VERB_FAMILIES_FULL_NAME_MAP.containsKey(currentVerb.getFamily())) {
                int value = Globals.VERB_FAMILIES_FULL_NAME_MAP.get(currentVerb.getFamily());
                currentVerb.setFamily(contextRef.get().getString(value));
            }
            //endregion

            //region Getting the conjugations row
            currentConjugationExceptionsRowLatin = new String[NumberOfSheetCols];
            currentConjugationExceptionsRowKanji = new String[NumberOfSheetCols];
            int indexOfExceptionConjugations = Integer.parseInt(currentVerb.getExceptionIndex());

            if (indexOfExceptionConjugations != currentFamilyConjugationsIndex) {
                currentConjugationExceptionsRowLatin = Arrays.copyOf(Globals.VerbLatinConjDatabaseNoSpaces.get(indexOfExceptionConjugations), NumberOfSheetCols);
                currentConjugationExceptionsRowKanji = Arrays.copyOf(Globals.VerbKanjiConjDatabase.get(indexOfExceptionConjugations), NumberOfSheetCols);
            }
            else {
                Arrays.fill(currentConjugationExceptionsRowLatin, "");
                Arrays.fill(currentConjugationExceptionsRowKanji, "");
            }

            for (int col = Globals.COLUMN_VERB_ISTEM; col < NumberOfSheetCols; col++) {

                if (!currentConjugationExceptionsRowLatin[col].equals("")) currentConjugationsRowLatin[col] = currentConjugationExceptionsRowLatin[col];
                else {
                    conjLength = currentConjugationsRowLatin[col].length();
                    if (conjLength > 3 && currentConjugationsRowLatin[col].substring(0, 3).equals("(o)")) {
                        currentConjugationsRowLatin[col] = "(o)" + currentVerb.getActiveLatinRoot() + currentConjugationsRowLatin[col].substring(3, conjLength);
                    } else {
                        currentConjugationsRowLatin[col] = currentVerb.getActiveLatinRoot() + currentConjugationsRowLatin[col];
                    }
                }

                if (!currentConjugationExceptionsRowKanji[col].equals("")) currentConjugationsRowKanji[col] = currentConjugationExceptionsRowKanji[col];
                else {
                    conjLength = currentConjugationsRowKanji[col].length();
                    if (conjLength > 3 && currentConjugationsRowKanji[col].substring(0, 3).equals("(お)")) {
                        currentConjugationsRowKanji[col] = "(お)" + currentVerb.getActiveKanjiRoot() + currentConjugationsRowKanji[col].substring(3, conjLength);
                    } else {
                        currentConjugationsRowKanji[col] = currentVerb.getActiveKanjiRoot() + currentConjugationsRowKanji[col];
                    }
                }
            }
            //endregion

            //region Getting the verb conjugations and putting each conjugation of the conjugations row into its appropriate category
            conjugationCategories = new ArrayList<>();
            String verbClause = "[" + contextRef.get().getString(R.string.verb) + "]";
            for (int categoryIndex = 1; categoryIndex < Globals.ConjugationTitles.size(); categoryIndex++) {

                //region Getting the set of Latin and Kanji conjugations according to the current category's subtitle column indexes
                subtitles = Globals.ConjugationTitles.get(categoryIndex).getSubtitles();
                int subtitleColIndex;
                conjugationSetLatin = new ArrayList<>();
                conjugationSetKanji = new ArrayList<>();
                for (int i=0; i<subtitles.size(); i++) {
                    subtitleColIndex = subtitles.get(i).getSubtitleIndex();
                    conjugationSetLatin.add(currentConjugationsRowLatin[subtitleColIndex].replace("[verb]",verbClause));
                    conjugationSetKanji.add(currentConjugationsRowKanji[subtitleColIndex].replace("[verb]",verbClause));
                }
                //endregion

                //region Intransitive verbs don't have a passive tense, so the relevant entries are removed
//                    if (!currentVerb.getTrans().equals("T") && categoryIndex == passiveTenseCategoryIndex) {
//                        for (int conjugationIndex = 0; conjugationIndex < conjugationSetLatin.size(); conjugationIndex++) {
//                            conjugationSetLatin.set(conjugationIndex, "*");
//                            conjugationSetKanji.set(conjugationIndex, "*");
//                        }
//                    }
                //endregion

                //region Cleaning the entries that contain exceptions
                for (int conjugationIndex = 0; conjugationIndex < conjugationSetLatin.size(); conjugationIndex++) {
                    if (conjugationSetLatin.get(conjugationIndex).contains("*"))
                        conjugationSetLatin.set(conjugationIndex, "*");
                    if (conjugationSetKanji.get(conjugationIndex).contains("*"))
                        conjugationSetKanji.set(conjugationIndex, "*");
                }
                //endregion

                //region Adding the conjugations to the conjugationCategory
                conjugationCategory = new Verb.ConjugationCategory();
                conjugations = new ArrayList<>();
                for (int conjugationIndex = 0; conjugationIndex < conjugationSetLatin.size(); conjugationIndex++) {
                    conjugation = new Verb.ConjugationCategory.Conjugation();
                    conjugation.setConjugationLatin(conjugationSetLatin.get(conjugationIndex));
                    conjugation.setConjugationKanji(conjugationSetKanji.get(conjugationIndex));
                    conjugations.add(conjugation);
                }
                conjugationCategory.setConjugations(conjugations);
                //endregion

                conjugationCategories.add(conjugationCategory);
            }
            //endregion

            currentVerb.setConjugationCategories(conjugationCategories);

            verbs.add(currentVerb);

            //Clearing the active fields since they're not needed anymore
            mRoomCentralDatabase.updateVerbByVerbIdWithParams(
                    matchingVerbId,
                    "",
                    "",
                    ""
            );
        }
        //endregion

        return verbs;
    }

    @Override protected void onPostExecute(Object[] objectArray) {
        super.onPostExecute(objectArray);
        listener.onVerbSearchAsyncTaskResultFound(objectArray);
    }

    public interface VerbSearchAsyncResponseHandler {
        void onVerbSearchAsyncTaskResultFound(Object[] text);
    }
}
