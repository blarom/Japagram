package com.japagram.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
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
    private String mPreparedTranslRomaji;
    private String mPreparedTranslHiragana;
    private int mPreparedTranslRomajiLength;
    private int mPreparedQueryLength;
    private String mPreparedCleaned;
    private int mPreparedCleanedLength;
    private RoomCentralDatabase mRoomCentralDatabase;
    private final HashMap<String, Integer> mFamilyConjugationIndexes = new HashMap<>();
    private final static int INDEX_FAMILY = 0;
    private final static int INDEX_ROMAJI = 1;
    private final static int INDEX_KANJI = 2;
    private final static int INDEX_HIRAGANA_FIRST_CHAR = 3;
    private final static int INDEX_LATIN_ROOT = 4;
    private final static int INDEX_KANJI_ROOT = 5;
    private final static int INDEX_ACTIVE_ALTSPELLING = 6;
    //endregion

    public VerbSearchAsyncTask(Context context, InputQuery inputQuery, List<Word> mWordsFromDictFragment, VerbSearchAsyncResponseHandler listener) {
        contextRef = new WeakReference<>(context);
        //String preparedQuery = inputQuery.hasIngEnding()? "to " + inputQuery.getIngless() : inputQuery.getOriginalCleaned();
        this.mInputQuery = inputQuery; //new InputQuery(inputQuery);
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
                    Verb verb = null;
                    for (Verb currentVerb : matchingVerbs) {
                       if (currentVerb.getVerbId() == word.getWordId()){
                           verb = currentVerb;
                           break;
                       }
                    }
                    if (verb == null) {
                        Log.i(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - ERROR! Missing verb for word with id" + word.getWordId());
                        continue;
                    }

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
                                    || conjugation.getConjugationLatin().equals(mPreparedCleaned) || conjugation.getConjugationKanji().equals(mPreparedCleaned)) {
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
                                || conjugation.getConjugationLatin().contains(mPreparedCleaned) || conjugation.getConjugationKanji().contains(mPreparedCleaned)) {
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
        String preparedConjugationLatin;
        String preparedConjugationKanji;
        boolean foundMatch = false;
        if (!inputQuery.equals(verb.getLatinRoot()) && !inputQuery.equals(verb.getKanjiRoot())) {

            //First pass - checking for conjugations that equal the input query
            for (int i=0; i<conjugationCategories.size(); i++) {
                conjugations = conjugationCategories.get(i).getConjugations();
                for (Verb.ConjugationCategory.Conjugation conjugation : conjugations) {
                    preparedConjugationLatin = conjugation.getConjugationLatin().replace(" ", "").split("\\(")[0];
                    preparedConjugationKanji = conjugation.getConjugationKanji().replace(" ", "").split("\\(")[0];
                    if (mPreparedQueryTextType == Globals.TYPE_LATIN
                            && preparedConjugationLatin.equals(inputQuery.replace(" ", ""))) {
                        matchingConjugation = conjugation.getConjugationLatin();
                        foundMatch = true;
                    }
                    else if ((mPreparedQueryTextType == Globals.TYPE_HIRAGANA || mPreparedQueryTextType == Globals.TYPE_KATAKANA)
                            && preparedConjugationLatin.equals(inputQueryLatin.replace(" ", ""))) {
                        matchingConjugation = conjugation.getConjugationLatin();
                        foundMatch = true;
                    }
                    else if (mPreparedQueryTextType == Globals.TYPE_KANJI && preparedConjugationKanji.equals(inputQuery)) {
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
                        preparedConjugationLatin = conjugation.getConjugationLatin().replace(" ", "").split("\\(")[0];
                        preparedConjugationKanji = conjugation.getConjugationKanji().replace(" ", "").split("\\(")[0];
                        if (mPreparedQueryTextType == Globals.TYPE_LATIN
                                && (preparedConjugationLatin.contains(inputQuery) || conjugation.getConjugationLatin().contains(inputQuery))) {
                            matchingConjugation = conjugation.getConjugationLatin();
                            foundMatch = true;
                        }
                        else if ((mPreparedQueryTextType == Globals.TYPE_HIRAGANA || mPreparedQueryTextType == Globals.TYPE_KATAKANA)
                                && (preparedConjugationLatin.contains(inputQueryLatin) || conjugation.getConjugationLatin().contains(inputQueryLatin))) {
                            matchingConjugation = conjugation.getConjugationLatin();
                            foundMatch = true;
                        }
                        else if (mPreparedQueryTextType == Globals.TYPE_KANJI
                                && (preparedConjugationKanji.contains(inputQuery) || conjugation.getConjugationKanji().contains(inputQuery))) {
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
        String terminations_ichidan_romaji = "(ta|teiru|teita|te|masu|mashita|nai|nakatta|masen *deshita|masen)";
        String terminations_ugodan_romaji = "(tta|tteiru|tteita|tte|imasu|imashita|wanai|wanakatta|imasen *deshita|imasen)";
        String terminations_arugodan_romaji = "(tta|tteiru|tteita|tte|imasu|imashita|ranai|ranakatta|imasen *deshita|imasen)";
        String terminations_kugodan_romaji = "(ita|iteiru|iteita|ite|kimasu|kimashita|kanai|kanakatta|kimasen *deshita|kimasen)";
        String terminations_rugodan_romaji = "(tta|tteiru|tteita|tte|rimasu|rimashita|ranai|ranakatta|rimasen *deshita|rimasen)";
        String terminations_iku_romaji = "(itta|itteiru|itteita|itte|ikimasu|ikimashita|ikanai|ikanakatta|ikimasen *deshita|ikimasen)";
        String terminations_sugodan_romaji = "(shita|shitteiru|shitteita|shitte|shimasu|shimashita|sanai|sanakatta|shimasen *deshita|shimasen)";
        String terminations_ichidan_kana = "(た|ている|ていた|て|ます|ました|ない|なかった|あせんでした|ません)";
        String terminations_ugodan_kana = "(った|っている|っていた|って|います|いました|わない|わなかった|いませんでした|いません)";
        String terminations_arugodan_kana = "(った|っている|っていた|って|います|いました|らない|らなかった|いませんでした|いません)";
        String terminations_kugodan_kana = "(いた|いている|いていた|いて|きます|きました|かない|かなかった|きませんでした|きません)";
        String terminations_rugodan_kana = "(った|っている|っていた|って|ります|りました|らない|らなかった|りませんでした|りません)";
        String terminations_iku_kana = "(いった|いっている|いっていた|いって|いきます|いきました|いかない|いかなかった|いきませんでした|いきません)";
        String terminations_sugodan_kana = "(した|している|していた|して|します|しました|さない|さなかった|しませんでした|しません)";
        mInputQuery = new InputQuery(mInputQuery.getOriginal()
                .replaceAll("(te|de) *age" + terminations_ichidan_romaji + "(| *ka)$", "$1 ageru")
                .replaceAll("(te|de) *kure" + terminations_ichidan_romaji + "(| *ka)$", "$1 kureru")
                .replaceAll("(te|de) *mora" + terminations_ugodan_romaji + "(| *ka)$", "$1 morau")
                .replaceAll("(te|de) *itada" + terminations_kugodan_romaji + "(| *ka)$", "$1 itadaku")
                .replaceAll("(te|de) *kudasa" + terminations_arugodan_romaji + "(| *ka)$", "$1 kudasaru")
                .replaceAll("(te|de) *" + terminations_iku_romaji + "(| *ka)$", "$1 iku")
                .replaceAll("(te|de) *o" + terminations_kugodan_romaji + "(| *ka)$", "$1 oku")
                .replaceAll("(te|de) *o" + terminations_rugodan_romaji + "(| *ka)$", "$1 oru")
                .replaceAll("([td])o" + terminations_kugodan_romaji + "(| *ka)$", "$1oku")
                .replaceAll("(te|de) *shima" + terminations_ugodan_romaji + "(| *ka)$", "$1 shimau")
                .replaceAll("(ccha|cha|ja)" + terminations_ugodan_romaji + "(ikenai|ikemasen|)(| *ka)$", "$1u")
                .replaceAll("(te|de) *mi" + terminations_ichidan_romaji + "(| *ka)$", "$1 miru")
                .replaceAll("(te|de) *shi" + terminations_ichidan_romaji + "(| *ka)$", "$1 suru")
                .replaceAll("(te|de) *aru" + terminations_ichidan_romaji + "(| *ka)$", "$1 aru")
                .replaceAll("(te|de) *kuru" + terminations_ichidan_romaji + "(| *ka)$", "$1 kuru")
                .replaceAll("(te|de) *sugi" + terminations_ichidan_romaji + "(| *ka)$", "$1 sugiru")

                .replaceAll("([てで])あげ" + terminations_ichidan_kana + "(|か)$", "$1あげる")
                .replaceAll("([てで])くれ" + terminations_ichidan_kana + "(|か)$", "$1くれる")
                .replaceAll("([てで])もら" + terminations_ugodan_kana + "(|か)$", "$1もらう")
                .replaceAll("(te|de) *いただ" + terminations_kugodan_kana + "(|か)$", "$1いただく")
                .replaceAll("(te|de) *くださ" + terminations_arugodan_kana + "(|か)$", "$1くださる")
                .replaceAll("([てで])" + terminations_iku_kana + "(|か)$", "$1いく")
                .replaceAll("([てで])お" + terminations_kugodan_kana + "(|か)$", "$1おく")
                .replaceAll("([てで])お" + terminations_rugodan_kana + "(|か)$", "$1おる")
                .replaceAll("([とど])" + terminations_kugodan_kana + "(|か)$", "$1おく")
                .replaceAll("([てで])しま" + terminations_ugodan_kana + "(|か)$", "$1しまう")
                .replaceAll("(っちゃ|ちゃ|じゃ)" + terminations_ugodan_kana + "(いけない|いけません|)(|か)$", "$1う")
                .replaceAll("([てで])み" + terminations_ichidan_kana + "(|か)$", "$1みる")
                .replaceAll("([てで])し" + terminations_ichidan_kana + "(|か)$", "$1する")
                .replaceAll("([てで])ある" + terminations_ichidan_kana + "(|か)$", "$1ある")
                .replaceAll("([てで])くる" + terminations_ichidan_kana + "(|か)$", "$1くる")
                .replaceAll("([てで])すぎ" + terminations_ichidan_kana + "(|か)$", "$1すぎる")
        );

        mPreparedQuery = mInputQuery.getOriginal();
        mPreparedQueryLength = mInputQuery.getOriginal().length();
        mPreparedQueryTextType = mInputQuery.getOriginalType();
        mPreparedCleaned = mInputQuery.getOriginalCleaned().replaceAll("\\s", "");
        mPreparedCleanedLength = mPreparedCleaned.length();
        mPreparedTranslRomaji = mInputQuery.getRomajiSingleElement();
        mPreparedTranslRomajiLength = mPreparedTranslRomaji.length();
        mPreparedTranslHiragana = mInputQuery.getHiraganaSingleElement();
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
    @NotNull private String[] getVerbCharacteristicsFromAltSpelling(String altSpelling, Verb verb) {

        String[] characteristics = new String[7];

        int altSpellingType = InputQuery.getTextType(altSpelling);

        if (altSpellingType != mPreparedQueryTextType) return new String[]{};

        characteristics[INDEX_FAMILY] = verb.getFamily();
        characteristics[INDEX_ROMAJI] = (altSpellingType == Globals.TYPE_LATIN)? altSpelling : verb.getRomaji();
        characteristics[INDEX_KANJI] = (altSpellingType == Globals.TYPE_KANJI)? altSpelling : verb.getKanji();
        if (altSpellingType == Globals.TYPE_HIRAGANA) {
            characteristics[INDEX_HIRAGANA_FIRST_CHAR] = altSpelling.substring(0,1);
        } else {
            String startOfWord = characteristics[INDEX_ROMAJI].length() > 5? characteristics[INDEX_ROMAJI].substring(0,5) : characteristics[INDEX_ROMAJI];
            characteristics[INDEX_HIRAGANA_FIRST_CHAR] = InputQuery.getWaapuroHiraganaKatakana(startOfWord).get(Globals.TYPE_HIRAGANA).substring(0,1);
        }
        characteristics[INDEX_LATIN_ROOT] = Utilities.getVerbRoot(characteristics[INDEX_ROMAJI], verb.getFamily(), Globals.TYPE_LATIN);
        characteristics[INDEX_KANJI_ROOT] = Utilities.getVerbRoot(characteristics[INDEX_KANJI], verb.getFamily(), Globals.TYPE_KANJI);
        characteristics[INDEX_ACTIVE_ALTSPELLING] = altSpelling;

        return characteristics;
    }
    @NotNull
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    private List<long[]> getMatchingVerbIdsAndCols(String language) {

        if (mPreparedQueryTextType == Globals.TYPE_INVALID || mCompleteVerbsList==null) return new ArrayList<>();
        Log.i(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - getMatchingVerbIdsAndCols - Starting");

        //region Initializations
        int NumberOfSheetCols = Globals.VerbLatinConjDatabase.get(0).length;
        boolean queryIsContainedInNormalFamilyConjugation;
        boolean queryIsContainedInAKuruConjugation;
        boolean queryIsContainedInASuruConjugation;
        boolean queryIsContainedInADesuConjugation;
        boolean queryIsContainedInIruVerbConjugation;
        int exceptionIndex;
        int familyIndex;
        String currentFamilyConj;
        String currentConj;
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
        boolean onlyRetrieveNonJapWords;
        //endregion

        //region Taking care of the case where the input is a basic conjugation that will cause the app to return too many verbs
        queryIsContainedInNormalFamilyConjugation = false;
        queryIsContainedInASuruConjugation = false;
        queryIsContainedInAKuruConjugation = false;
        queryIsContainedInADesuConjugation = false;
        queryIsContainedInIruVerbConjugation = false;
        for (String key : mFamilyConjugationIndexes.keySet()) {
            familyIndex = mFamilyConjugationIndexes.get(key);
            switch (key) {
                case Globals.VERB_FAMILY_DA:
                    currentFamilyConjugations = Globals.VerbLatinConjDatabase.get(familyIndex);
                    for (int column = 1; column < NumberOfSheetCols; column++) {
                        currentFamilyConj = currentFamilyConjugations[column];
                        if (currentFamilyConj.equals(mPreparedCleaned) || currentFamilyConj.equals(mPreparedTranslHiragana)) {
                            queryIsContainedInADesuConjugation = true;
                            break;
                        }
                    }
                    if (queryIsContainedInADesuConjugation) break;
                    currentFamilyConjugations = Globals.VerbKanjiConjDatabase.get(familyIndex);
                    for (int column = 1; column < NumberOfSheetCols; column++) {
                        currentFamilyConj = currentFamilyConjugations[column];
                        if (currentFamilyConj.equals(mPreparedCleaned)) {
                            queryIsContainedInADesuConjugation = true;
                            break;
                        }
                    }
                    break;
                case Globals.VERB_FAMILY_KURU:
                    currentFamilyConjugations = Globals.VerbLatinConjDatabase.get(familyIndex);
                    for (int column = 1; column < NumberOfSheetCols; column++) {
                        currentFamilyConj = currentFamilyConjugations[column];
                        if (currentFamilyConj.equals(mPreparedCleaned) || currentFamilyConj.equals(mPreparedTranslHiragana)) {
                            queryIsContainedInAKuruConjugation = true;
                            break;
                        }
                    }
                    if (queryIsContainedInAKuruConjugation) break;
                    currentFamilyConjugations = Globals.VerbKanjiConjDatabase.get(familyIndex);
                    for (int column = 1; column < NumberOfSheetCols; column++) {
                        currentFamilyConj = currentFamilyConjugations[column];
                        if (currentFamilyConj.equals(mPreparedCleaned)) {
                            queryIsContainedInAKuruConjugation = true;
                            break;
                        }
                    }
                    break;
                case Globals.VERB_FAMILY_SURU:
                    currentFamilyConjugations = Globals.VerbLatinConjDatabase.get(familyIndex);
                    for (int column = 1; column < NumberOfSheetCols; column++) {
                        currentFamilyConj = currentFamilyConjugations[column];
                        if (currentFamilyConj.equals(mPreparedCleaned) || currentFamilyConj.equals(mPreparedTranslHiragana)) {
                            queryIsContainedInASuruConjugation = true;
                            break;
                        }
                    }
                    if (queryIsContainedInASuruConjugation) break;
                    currentFamilyConjugations = Globals.VerbKanjiConjDatabase.get(familyIndex);
                    for (int column = 1; column < NumberOfSheetCols; column++) {
                        currentFamilyConj = currentFamilyConjugations[column];
                        if (currentFamilyConj.equals(mPreparedCleaned)) {
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
                        if (currentConjugation.contains(mPreparedCleaned) || currentConjugation.contains(mPreparedTranslHiragana)) {
                            queryIsContainedInIruVerbConjugation = true;
                            break;
                        }
                    }
                    break;
                default:
                    currentFamilyConjugations = Globals.VerbLatinConjDatabase.get(familyIndex);
                    for (int column = Globals.COLUMN_VERB_ISTEM; column < NumberOfSheetCols; column++) {
                        currentFamilyConj = currentFamilyConjugations[column];
                        if (currentFamilyConj.contains(mPreparedCleaned) || currentFamilyConj.contains(mPreparedTranslHiragana)) {
                            queryIsContainedInNormalFamilyConjugation = true;
                            break;
                        }
                    }
                    if (queryIsContainedInNormalFamilyConjugation) break;
                    currentFamilyConjugations = Globals.VerbKanjiConjDatabase.get(familyIndex);
                    for (int column = Globals.COLUMN_VERB_ISTEM; column < NumberOfSheetCols; column++) {
                        currentFamilyConj = currentFamilyConjugations[column];
                        if (currentFamilyConj.contains(mPreparedCleaned)) {
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
        if (mPreparedQueryTextType == Globals.TYPE_LATIN && mPreparedCleaned.length() < 4
                || (mPreparedQueryTextType == Globals.TYPE_HIRAGANA || mPreparedQueryTextType == Globals.TYPE_KATAKANA) && mPreparedCleaned.length() < 3) {
            onlyRetrieveShortRomajiVerbs = true;
        }
        onlyRetrieveNonJapWords = mPreparedQueryTextType == Globals.TYPE_LATIN && mPreparedTranslHiragana.contains("*");
        //endregion

        //region Performing column dilution in order to make the search more efficient (the diluted column ArrayList is used in the Search Algorithm)
        int queryLengthForDilution = 0;
        List<String[]> verbConjugationMaxLengths = new ArrayList<>();
        int conjugationMaxLength;
        if (mPreparedQueryTextType == Globals.TYPE_LATIN) {
            verbConjugationMaxLengths = Utilities.readCSVFile("LineVerbsLengths - 3000 kanji.csv", contextRef.get());
            queryLengthForDilution = mPreparedCleanedLength;
        }
        else if (mPreparedQueryTextType == Globals.TYPE_HIRAGANA || mPreparedQueryTextType == Globals.TYPE_KATAKANA) {
            verbConjugationMaxLengths = Utilities.readCSVFile("LineVerbsLengths - 3000 kanji.csv", contextRef.get());
            queryLengthForDilution = mPreparedTranslRomajiLength;
        }
        else if (mPreparedQueryTextType == Globals.TYPE_KANJI) {
            verbConjugationMaxLengths = Utilities.readCSVFile("LineVerbsKanjiLengths - 3000 kanji.csv", contextRef.get());
            queryLengthForDilution = mPreparedCleanedLength;
        }

        HashMap<String, List<Integer>> dilutedConjugationColIndexesByFamily = new HashMap<>();
        for (int row=1; row<verbConjugationMaxLengths.size(); row++) {
            String family_name = verbConjugationMaxLengths.get(row)[0];
            if (family_name.equals("")) continue;
            List<Integer> dilutedConjugationColIndexesTemp = new ArrayList<>();
            for (int col = Globals.COLUMN_VERB_ISTEM; col < NumberOfSheetCols; col++) {
                if (!verbConjugationMaxLengths.get(row)[col].equals(""))
                    conjugationMaxLength = Integer.parseInt(verbConjugationMaxLengths.get(row)[col]);
                else conjugationMaxLength = 0;
                if (conjugationMaxLength >= queryLengthForDilution) dilutedConjugationColIndexesTemp.add(col);
            }
            dilutedConjugationColIndexesByFamily.put(family_name, new ArrayList<>(dilutedConjugationColIndexesTemp));
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
        boolean typeIsVerb;
        for (Word word : mMatchingWords) {
            type = word.getMeaningsEN().get(0).getType();
            typeIsVerb = type.length() > 0 && type.substring(0,1).equals("V") && !type.contains("VC");
            if (!typeIsVerb) continue;

            //Preventing the input query being a suru verb conjugation from overloading the results
            if (queryIsContainedInASuruConjugation && word.getRomaji().contains(" suru")) {
                if (counter > MAX_NUM_RESULTS_FOR_SURU_CONJ_SEARCH) continue;
                counter++;
            }

            //If the input querty includes "to ", removing all words that don't have "to " in their english meanings - this prevents overloading search results with irrelevant words
            if (mInputQuery.isVerbWithTo()) {
                for (Word.Meaning meaning : word.getMeaningsEN()) {
                    if (meaning.getMeaning().contains(mInputQuery.getOriginalCleaned())) {
                        matchingVerbIdsAndColsFromBasicCharacteristics.add(new long[]{word.getWordId(), 0});
                        break;
                    }
                }
            } else {
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
        boolean hasConjExceptions;
        String lastFamily = "";
        String[] characteristics;
        char preparedTranslHiraganaChar0 = mPreparedTranslHiragana.charAt(0);
        String preparedCleanedChar0String = mPreparedCleaned.substring(0,1);
        char preparedCleanedChar0 = preparedCleanedChar0String.charAt(0);
        boolean preparedIsLatin = mPreparedQueryTextType == Globals.TYPE_LATIN;
        boolean preparedIsKana = mPreparedQueryTextType == Globals.TYPE_HIRAGANA || mPreparedQueryTextType == Globals.TYPE_KATAKANA;
        boolean preparedIsKanji = mPreparedQueryTextType == Globals.TYPE_KANJI;
        boolean inputQueryFirstKanaIsVowel = preparedTranslHiraganaChar0 == 'あ'
                || preparedTranslHiraganaChar0 == 'え'
                || preparedTranslHiraganaChar0 == 'い'
                || preparedTranslHiraganaChar0 == 'お'
                || preparedTranslHiraganaChar0 == 'う';
        currentFamilyConjugations = Globals.VerbLatinConjDatabaseNoSpaces.get(mFamilyConjugationIndexes.get("su"));

        //There's no point in checking again if the input query is part of the family conjugation,
        // so we only check up to the substring that could contain all but the first char of the input query
        // No check is needed for len(conjugation) < maxCharIndexWhereMatchIsExpected, since we're using only columns diluted by total verb length > mInputQueryContatenatedLength
        int maxCharIndexWhereMatchIsExpected = mPreparedQueryTextType != Globals.TYPE_LATIN && mPreparedQueryTextType != Globals.TYPE_KANJI ? mPreparedTranslRomajiLength - 1 : mPreparedCleanedLength - 1;

        String familyForDilution;
        for (Verb verb : mCompleteVerbsList) {

            //region Skipping verbs that were already found
            verbAlreadyFound = false;
            for (long[] idAndCol : copyOfMatchingVerbIdsAndColsFromBasicCharacteristics) {
                if (idAndCol[0] == verb.getVerbId()) {

                    //Update the active fields for the current verb according to the altSpelling
                    boolean foundAltSpelling = false;
                    for (String altSpelling : verb.getAltSpellings().split(Globals.DB_ELEMENTS_DELIMITER)) {
                        if (altSpelling.equals(mPreparedTranslRomaji) || altSpelling.equals(mPreparedTranslHiragana)) {
                            characteristics = getVerbCharacteristicsFromAltSpelling(altSpelling, verb);
                            if (characteristics.length == 0) continue;
                            verb.setActiveLatinRoot(characteristics[INDEX_LATIN_ROOT]);
                            verb.setActiveKanjiRoot(characteristics[INDEX_KANJI_ROOT]);
                            verb.setActiveAltSpelling(altSpelling);
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
            allowExpandedConjugationsComparison = !onlyRetrieveNonJapWords;
            //endregion

            //region Building the list of relevant base characteristics that the algorithm will check
            //This includes the romaji/kanji/romajiroot/kanjiroot/kana1stchar, also also the altSpelling equivalents
            altSpellingsAsString = verb.getAltSpellings();
            family = verb.getFamily();
            if (!mFamilyConjugationIndexes.containsKey(family)) continue;
            if (!family.equals(lastFamily)) {
                //Updating the family conjugations only when a new family is seen
                currentFamilyConjugations = preparedIsKanji?
                        Globals.VerbKanjiConjDatabase.get(mFamilyConjugationIndexes.get(family)) : Globals.VerbLatinConjDatabaseNoSpaces.get(mFamilyConjugationIndexes.get(family));
                lastFamily = family;
            }
            exceptionIndex = (verb.getExceptionIndex().equals(""))? 0 : Integer.parseInt(verb.getExceptionIndex());

            verbSearchCandidates = new ArrayList<>();
            characteristics = new String[7];
            characteristics[INDEX_FAMILY] = verb.getFamily();
            characteristics[INDEX_ROMAJI] = verb.getRomaji();
            characteristics[INDEX_KANJI] = verb.getKanji();
            characteristics[INDEX_HIRAGANA_FIRST_CHAR] = verb.getHiraganaFirstChar();
            characteristics[INDEX_LATIN_ROOT] = verb.getLatinRoot();
            characteristics[INDEX_KANJI_ROOT] = verb.getKanjiRoot();
            characteristics[INDEX_ACTIVE_ALTSPELLING] = verb.getRomaji();
            verbSearchCandidates.add(characteristics);

            for (String altSpelling : altSpellingsAsString.split(Globals.DB_ELEMENTS_DELIMITER)) {
                if (altSpelling.equals("")) continue;
                characteristics = getVerbCharacteristicsFromAltSpelling(altSpelling, verb);
                if (characteristics.length != 0) verbSearchCandidates.add(characteristics);
            }
            //endregion

            //region Checking if one of the relevant base words gets a match, and registering it in the match list
            for (String[] verbSearchCandidate : verbSearchCandidates) {

                //region Getting the verb characteristics
                familyForDilution = contextRef.get().getString(Globals.VERB_FAMILIES_FULL_NAME_MAP.get(verbSearchCandidate[INDEX_FAMILY]));
                romaji = verbSearchCandidate[INDEX_ROMAJI];
                hiraganaFirstChar = verbSearchCandidate[INDEX_HIRAGANA_FIRST_CHAR].charAt(0);
                latinRoot = verbSearchCandidate[INDEX_LATIN_ROOT].replace(" ","");
                kanjiRoot = verbSearchCandidate[INDEX_KANJI_ROOT];
                //endregion

                //region Only allowing searches on verbs that satisfy the following conditions (including identical 1st char, kuru/suru/da, query length)
                if (    !(     ( preparedIsLatin && romaji.charAt(0) == preparedCleanedChar0 )
                            || ( preparedIsKana && (hiraganaFirstChar == preparedTranslHiraganaChar0) )
                            || ( preparedIsKanji && kanjiRoot.contains(preparedCleanedChar0String))
                            || romaji.contains("kuru")
                            || romaji.equals("suru")
                            || romaji.equals("da") )
                        || ( preparedIsLatin && mPreparedCleanedLength < 4 && inputQueryFirstKanaIsVowel && !romaji.contains(mPreparedCleaned))
                        || ( preparedIsKana && mPreparedCleanedLength < 3 && inputQueryFirstKanaIsVowel && !romaji.contains(mPreparedTranslRomaji))
                        || ( preparedIsKanji && mPreparedCleanedLength < 3 && kanjiRoot.length()>0 && !mPreparedCleaned.contains(kanjiRoot))
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

                    hasConjExceptions = exceptionIndex != mFamilyConjugationIndexes.get(family);

                    //region Latin conjugations comparison
                    if (preparedIsLatin) {

                        if (hasConjExceptions) {
                            currentConjugations = Globals.VerbLatinConjDatabaseNoSpaces.get(exceptionIndex);
                            for (int col : dilutedConjugationColIndexesByFamily.get(familyForDilution)) {
                                currentFamilyConj = currentFamilyConjugations[col];
                                currentConj = currentConjugations[col];
                                if (currentConj.equals("")) {
                                    conjugationValue = latinRoot + ((currentFamilyConj.length() > maxCharIndexWhereMatchIsExpected)?
                                            currentFamilyConj.substring(0, maxCharIndexWhereMatchIsExpected) : currentFamilyConj);
                                } else conjugationValue = currentConj;

                                if (conjugationValue.contains(mPreparedCleaned)) {
                                    foundMatch = true;
                                    matchColumn = col;
                                    break;
                                }
                            }
                        }
                        else {
                            for (int col : dilutedConjugationColIndexesByFamily.get(familyForDilution)) {
                                currentFamilyConj = currentFamilyConjugations[col];
                                conjugationValue = latinRoot + ((currentFamilyConj.length() > maxCharIndexWhereMatchIsExpected)?
                                        currentFamilyConj.substring(0, maxCharIndexWhereMatchIsExpected) : currentFamilyConj);

                                if (conjugationValue.contains(mPreparedCleaned)) {
                                    foundMatch = true;
                                    matchColumn = col;
                                    break;
                                }
                            }
                        }
                    }
                    //endregion

                    //region Kana conjugations comparison
                    else if (preparedIsKana) {

                        //There's no point in checking again if the input query is part of the family conjugation,
                        // so we only check up to the substring that could contain all but the first char of the input query
                        // No check is needed for len(conjugation) < maxCharIndexWhereMatchIsExpected, since we're using only columns diluted by total verb length > mInputQueryContatenatedLength
                        if (hasConjExceptions) {
                            currentConjugations = Globals.VerbLatinConjDatabaseNoSpaces.get(exceptionIndex);
                            for (int col : dilutedConjugationColIndexesByFamily.get(familyForDilution)) {
                                currentFamilyConj = currentFamilyConjugations[col];
                                currentConj = currentConjugations[col];
                                if (currentConj.equals("")) {
                                    conjugationValue = latinRoot + ((currentFamilyConj.length() > maxCharIndexWhereMatchIsExpected)?
                                            currentFamilyConj.substring(0, maxCharIndexWhereMatchIsExpected) : currentFamilyConj);
                                } else conjugationValue = currentConj;

                                if (conjugationValue.contains(mPreparedTranslRomaji)) {
                                    foundMatch = true;
                                    matchColumn = col;
                                    break;
                                }
                            }
                        }
                        else {
                            for (int col : dilutedConjugationColIndexesByFamily.get(familyForDilution)) {
                                currentFamilyConj = currentFamilyConjugations[col];
                                conjugationValue = latinRoot + ((currentFamilyConj.length() > maxCharIndexWhereMatchIsExpected)?
                                        currentFamilyConj.substring(0, maxCharIndexWhereMatchIsExpected) : currentFamilyConj);

                                if (conjugationValue.contains(mPreparedTranslRomaji)) {
                                    foundMatch = true;
                                    matchColumn = col;
                                    break;
                                }
                            }
                        }
                    }
                    //endregion

                    //region Kanji conjugations comparison
                    else if (preparedIsKanji) {

                        //There's no point in checking again if the input query is part of the family conjugation,
                        // so we only check up to the substring that could contain all but the first char of the input query
                        // No check is needed for len(conjugation) < maxCharIndexWhereMatchIsExpected, since we're using only columns diluted by total verb length > mInputQueryContatenatedLength
                        maxCharIndexWhereMatchIsExpected = mPreparedQueryLength - 1;
                        if (hasConjExceptions) {
                            currentConjugations = Globals.VerbKanjiConjDatabase.get(exceptionIndex);
                            for (int col : dilutedConjugationColIndexesByFamily.get(familyForDilution)) {
                                currentFamilyConj = currentFamilyConjugations[col];
                                currentConj = currentConjugations[col];
                                if (currentConj.equals("")) {
                                    conjugationValue = kanjiRoot + ((currentFamilyConj.length() > maxCharIndexWhereMatchIsExpected)?
                                            currentFamilyConj.substring(0, maxCharIndexWhereMatchIsExpected) : currentFamilyConj);
                                } else conjugationValue = currentConj;

                                if (conjugationValue.contains(mPreparedQuery)) {
                                    foundMatch = true;
                                    matchColumn = col;
                                    break;
                                }
                            }
                        }
                        else {
                            for (int col : dilutedConjugationColIndexesByFamily.get(familyForDilution)) {
                                currentFamilyConj = currentFamilyConjugations[col];
                                conjugationValue = kanjiRoot + ((currentFamilyConj.length() > maxCharIndexWhereMatchIsExpected)?
                                        currentFamilyConj.substring(0, maxCharIndexWhereMatchIsExpected) : currentFamilyConj);

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

        boolean queryIsVerbWithTo = mInputQuery.isVerbWithTo();

        String inputQuery = mInputQuery.getOriginalCleaned();
        if (mInputQuery.getOriginalType() == Globals.TYPE_HIRAGANA || mInputQuery.getOriginalType() == Globals.TYPE_KATAKANA) inputQuery = mInputQuery.getSearchQueriesRomaji().get(0);

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
        List<Verb> matchingVerbs = new ArrayList<>();
        boolean found;
        for (Word word : matchingWords) {
            found = false;
            for (Verb verb : matchingVerbsBeforeOrderingByWordId) {
                if (verb.getVerbId() == word.getWordId()) {
                    matchingVerbs.add(verb);
                    found = true;
                    break;
                }
            }
            if (!found) {
                word.setWordId(word.getWordId());
                Log.i(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - ERROR! Missing verb for word with id" + word.getWordId());
            }
        }
        //endregion

        ////////////////////////////////////TODO : fix shagamu verb not found
        /// ids: 18455

        //region Updating the verbs with their conjugations
        for (int t = 0; t < matchingVerbIdAndColList.size(); t++) {
            matchingVerbId = matchingVerbIdAndColList.get(t)[0];
            try {
                currentVerb = matchingVerbs.get(t);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            currentWord = matchingWords.get(t);
            if (currentWord == null || currentVerb == null || !mFamilyConjugationIndexes.containsKey(currentVerb.getFamily())) {
                continue;
            }
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
