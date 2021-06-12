package com.japagram.utilitiesCrossPlatform;

import android.content.Context;

import com.japagram.data.ConjugationTitle;
import com.japagram.data.InputQuery;
import com.japagram.data.Verb;
import com.japagram.data.Word;
import com.japagram.utilitiesAndroid.AndroidUtilitiesIO;
import com.japagram.utilitiesPlatformOverridable.OvUtilsDb;
import com.japagram.utilitiesPlatformOverridable.OvUtilsGeneral;
import com.japagram.utilitiesPlatformOverridable.OvUtilsResources;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public final class UtilitiesVerbSearch {

    public static @NotNull InputQuery setInputQueryParameters(@NotNull String inputQueryOriginal) {
        String terminations_ichidan_romaji = "(ta|teiru|teita|te|masu|mashita|nai|nakatta|masen *deshita|masen)";
        String terminations_ichidan_romaji_subset = "(mashita|nakatta|masen *deshita)";
        String terminations_ugodan_romaji = "(tta|tteiru|tteita|tte|imasu|imashita|wanai|wanakatta|imasen *deshita|imasen)";
        String terminations_arugodan_romaji = "(tta|tteiru|tteita|tte|imasu|imashita|ranai|ranakatta|imasen *deshita|imasen)";
        String terminations_kugodan_romaji = "(ita|iteiru|iteita|ite|kimasu|kimashita|kanai|kanakatta|kimasen *deshita|kimasen)";
        String terminations_rugodan_romaji = "(tta|tteiru|tteita|tte|rimasu|rimashita|ranai|ranakatta|rimasen *deshita|rimasen)";
        String terminations_iku_romaji = "(itta|itteiru|itteita|itte|ikimasu|ikimashita|ikanai|ikanakatta|ikimasen *deshita|ikimasen|ikeru|ikeba|ittara)";
        String terminations_sugodan_romaji = "(shita|shitteiru|shitteita|shitte|shimasu|shimashita|sanai|sanakatta|shimasen *deshita|shimasen)";
//        String terminations_ichidan_kana = "(た|ている|ていた|て|ます|ました|ない|なかった|ませんでした|ません)";
//        String terminations_ichidan_kana_subset = "(ました|なかった|ませんでした)";
//        String terminations_ugodan_kana = "(った|っている|っていた|って|います|いました|わない|わなかった|いませんでした|いません)";
//        String terminations_arugodan_kana = "(った|っている|っていた|って|います|いました|らない|らなかった|いませんでした|いません)";
//        String terminations_kugodan_kana = "(いた|いている|いていた|いて|きます|きました|かない|かなかった|きませんでした|きません)";
//        String terminations_rugodan_kana = "(った|っている|っていた|って|ります|りました|らない|らなかった|りませんでした|りません)";
//        String terminations_iku_kana = "(いった|いっている|いっていた|いって|いきます|いきました|いかない|いかなかった|いきませんでした|いきません|いける|いけば|いったら)";
//        String terminations_iku_kanji = "(行った|行っている|行っていた|行って|行きます|行きました|行かない|行かなかった|行きませんでした|行きません|行ける|いけば|行ったら)";
//        String terminations_sugodan_kana = "(した|している|していた|して|します|しました|さない|さなかった|しませんでした|しません)";

        String preparedInputQueryString = inputQueryOriginal;

        int textType = UtilitiesQuery.getTextType(preparedInputQueryString);
        if (textType == Globals.TEXT_TYPE_HIRAGANA || textType == Globals.TEXT_TYPE_KATAKANA) {
            preparedInputQueryString = UtilitiesQuery.getWaapuroHiraganaKatakana(preparedInputQueryString).get(0);
        }

        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"(te|de) *age", terminations_ichidan_romaji, "(| *ka)$"}), "$1 ageru");
        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"(te|de) *kure", terminations_ichidan_romaji, "(| *ka)$"}), "$1 kureru");
        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"(te|de) *mora" , terminations_ugodan_romaji, "(| *ka)$"}), "$1 morau");
        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"(te|de) *itada" , terminations_kugodan_romaji, "(| *ka)$"}), "$1 itadaku");
        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"(te|de) *kudasa" , terminations_arugodan_romaji, "(| *ka)$"}), "$1 kudasaru");
        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"(te|de) *" , terminations_iku_romaji, "(| *ka)$"}), "$1 iku");
        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"(te|de) *o" , terminations_kugodan_romaji, "(| *ka)$"}), "$1 oku");
        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"(te|de) *o" , terminations_rugodan_romaji, "(| *ka)$"}), "$1 oru");
        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"([td])o" , terminations_kugodan_romaji, "(| *ka)$"}), "$1oku");
        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"(te|de) *shima" , terminations_ugodan_romaji, "(| *ka)$"}), "$1 shimau");
        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"(ccha|cha|ja)" , terminations_ugodan_romaji, "(ikenai|ikemasen|)(| *ka)$"}), "$1u");
        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"(te|de) *mi" , terminations_ichidan_romaji, "(| *ka)$"}), "$1 miru");
        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"(te|de) *shi" , terminations_ichidan_romaji, "(| *ka)$"}), "$1 suru");
        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"(te|de) *aru" , terminations_ichidan_romaji, "(| *ka)$"}), "$1 aru");
        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"(te|de) *kuru" , terminations_ichidan_romaji, "(| *ka)$"}), "$1 kuru");
        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"(te|de) *sugi" , terminations_ichidan_romaji, "(| *ka)$"}), "$1 sugiru");
        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"(are)", terminations_ichidan_romaji_subset}), "$1masu");
        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"du"}), "zu");

//        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"([てで])あげ" , terminations_ichidan_kana, "(|か)$"}), "$1あげる");
//        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"([てで])くれ" , terminations_ichidan_kana, "(|か)$"}), "$1くれる");
//        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"([てで])もら" , terminations_ugodan_kana, "(|か)$"}), "$1もらう");
//        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"(te|de) *いただ" , terminations_kugodan_kana, "(|か)$"}), "$1いただく");
//        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"(te|de) *くださ" , terminations_arugodan_kana, "(|か)$"}), "$1くださる");
//        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"([てで])" , terminations_iku_kana, "(|か)$"}), "$1いく");
//        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"([てで])" , terminations_iku_kanji, "(|か)$"}), "$1いく");
//        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"([てで])お" , terminations_kugodan_kana, "(|か)$"}), "$1おく");
//        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"([てで])お" , terminations_rugodan_kana, "(|か)$"}), "$1おる");
//        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"([とど])" , terminations_kugodan_kana, "(|か)$"}), "$1おく");
//        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"([てで])しま" , terminations_ugodan_kana, "(|か)$"}), "$1しまう");
//        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"(っちゃ|ちゃ|じゃ)" , terminations_ugodan_kana, "(いけない|いけません|)(|か)$"}), "$1う");
//        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"([てで])み" , terminations_ichidan_kana, "(|か)$"}), "$1みる");
//        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"([てで])し" , terminations_ichidan_kana, "(|か)$"}), "$1する");
//        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"([てで])ある" , terminations_ichidan_kana, "(|か)$"}), "$1ある");
//        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"([てで])くる" , terminations_ichidan_kana, "(|か)$"}), "$1くる");
//        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"([てで])すぎ" , terminations_ichidan_kana, "(|か)$"}), "$1すぎる");
//        preparedInputQueryString = preparedInputQueryString.replaceAll(OvUtilsGeneral.concat(new String[]{"(あれ)", terminations_ichidan_kana_subset}), "$1ます");

        InputQuery preparedInputQuery = new InputQuery(preparedInputQueryString);

        return preparedInputQuery;
    }

    public static @NotNull HashMap<String, Integer> getFamilyConjugationIndexes() {
        HashMap<String, Integer> mFamilyConjugationIndexes = new HashMap<>();
        for (int rowIndex = 3; rowIndex < Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE.size(); rowIndex++) {

            if (Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE.get(rowIndex)[0].equals("") || !Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE.get(rowIndex)[1].equals("")) continue;

            if ("su godan".equals(Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_SU_GODAN)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_SU_GODAN, rowIndex);
            } else if ("ku godan".equals(Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_KU_GODAN)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_KU_GODAN, rowIndex);
            } else if ("iku special class".equals(Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_IKU_SPECIAL)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_IKU_SPECIAL, rowIndex);
            } else if ("yuku special class".equals(Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_YUKU_SPECIAL)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_YUKU_SPECIAL, rowIndex);
            } else if ("gu godan".equals(Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_GU_GODAN)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_GU_GODAN, rowIndex);
            } else if ("bu godan".equals(Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_BU_GODAN)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_BU_GODAN, rowIndex);
            } else if ("mu godan".equals(Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_MU_GODAN)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_MU_GODAN, rowIndex);
            } else if ("nu godan".equals(Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_NU_GODAN)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_NU_GODAN, rowIndex);
            } else if ("ru godan".equals(Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_RU_GODAN)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_RU_GODAN, rowIndex);
            } else if ("aru special class".equals(Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_ARU_SPECIAL)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_ARU_SPECIAL, rowIndex);
            } else if ("tsu godan".equals(Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_TSU_GODAN)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_TSU_GODAN, rowIndex);
            } else if ("u godan".equals(Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_U_GODAN)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_U_GODAN, rowIndex);
            } else if ("u special class".equals(Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_U_SPECIAL)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_U_SPECIAL, rowIndex);
            } else if ("ru ichidan".equals(Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_RU_ICHIDAN)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_RU_ICHIDAN, rowIndex);
            } else if ("desu copula".equals(Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_DA)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_DA, rowIndex);
            } else if ("kuru verb".equals(Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_KURU)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_KURU, rowIndex);
            } else if ("suru verb".equals(Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE.get(rowIndex)[0]) && !mFamilyConjugationIndexes.containsKey(Globals.VERB_FAMILY_SURU)) {
                mFamilyConjugationIndexes.put(Globals.VERB_FAMILY_SURU, rowIndex);
            }
        }
        return mFamilyConjugationIndexes;
    }

    @NotNull
    public static String[] getVerbCharacteristicsFromAltSpelling(String altSpelling, Verb verb, int mPreparedQueryTextType) {

        String[] characteristics = new String[7];

        int altSpellingType = UtilitiesQuery.getTextType(altSpelling);

        if (altSpellingType != mPreparedQueryTextType) return new String[]{};

        characteristics[Globals.INDEX_FAMILY] = verb.getFamily();
        characteristics[Globals.INDEX_ROMAJI] = (altSpellingType == Globals.TEXT_TYPE_LATIN)? altSpelling : verb.getRomaji();
        characteristics[Globals.INDEX_KANJI] = (altSpellingType == Globals.TEXT_TYPE_KANJI)? altSpelling : verb.getKanji();
        if (altSpellingType == Globals.TEXT_TYPE_HIRAGANA) {
            characteristics[Globals.INDEX_HIRAGANA_FIRST_CHAR] = altSpelling.substring(0,1);
        } else {
            String startOfWord = characteristics[Globals.INDEX_ROMAJI].length() > 5? characteristics[Globals.INDEX_ROMAJI].substring(0,5) : characteristics[Globals.INDEX_ROMAJI];
            characteristics[Globals.INDEX_HIRAGANA_FIRST_CHAR] = UtilitiesQuery.getWaapuroHiraganaKatakana(startOfWord).get(Globals.TEXT_TYPE_HIRAGANA).substring(0,1);
        }
        characteristics[Globals.INDEX_LATIN_ROOT] = UtilitiesDb.getVerbRoot(characteristics[Globals.INDEX_ROMAJI], verb.getFamily(), Globals.TEXT_TYPE_LATIN);
        characteristics[Globals.INDEX_KANJI_ROOT] = UtilitiesDb.getVerbRoot(characteristics[Globals.INDEX_KANJI], verb.getFamily(), Globals.TEXT_TYPE_KANJI);
        characteristics[Globals.INDEX_ACTIVE_ALTSPELLING] = altSpelling;

        return characteristics;
    }

    @NotNull
    @SuppressWarnings({"unchecked"})
    public static List<long[]> getMatchingVerbIdsAndCols(String language,
                                                         @NotNull InputQuery preparedQuery,
                                                         List<Verb> mCompleteVerbsList,
                                                         List<Word> mWordsFromDictFragment,
                                                         HashMap<String, Integer> mFamilyConjugationIndexes,
                                                         Context context) {

        if (preparedQuery.isEmpty()) return new ArrayList<>();

        String mPreparedQuery;
        int mPreparedQueryLength;
        String mPreparedCleaned;
        int mPreparedCleanedLength;
        String mPreparedTranslRomaji;
        int mPreparedTranslRomajiLength;
        String mPreparedTranslHiragana;
        mPreparedQuery = preparedQuery.getOriginal();
        mPreparedQueryLength = preparedQuery.getOriginal().length();
        mPreparedCleaned = preparedQuery.getOriginalCleaned().replaceAll("\\s", "");
        mPreparedCleanedLength = mPreparedCleaned.length();
        mPreparedTranslRomaji = preparedQuery.getRomajiSingleElement();
        mPreparedTranslRomajiLength = mPreparedTranslRomaji.length();
        mPreparedTranslHiragana = preparedQuery.getHiraganaSingleElement();

        int mPreparedQueryTextType = preparedQuery.getOriginalType();

        if (mPreparedQueryTextType == Globals.TEXT_TYPE_INVALID || mCompleteVerbsList==null) return new ArrayList<>();

        OvUtilsGeneral.printLog(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - getMatchingVerbIdsAndCols - Starting");

        //region Initializations
        int numberOfSheetCols = Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE.get(0).length;
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
        String conjugationValue;
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
                    currentFamilyConjugations = Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE.get(familyIndex);
                    for (int column = 1; column < numberOfSheetCols; column++) {
                        currentFamilyConj = currentFamilyConjugations[column];
                        if (currentFamilyConj.equals(mPreparedCleaned) || currentFamilyConj.equals(mPreparedTranslHiragana)) {
                            queryIsContainedInADesuConjugation = true;
                            break;
                        }
                    }
                    if (queryIsContainedInADesuConjugation) break;
                    currentFamilyConjugations = Globals.GLOBAL_VERB_KANJI_CONJ_DATABASE.get(familyIndex);
                    for (int column = 1; column < numberOfSheetCols; column++) {
                        currentFamilyConj = currentFamilyConjugations[column];
                        if (currentFamilyConj.equals(mPreparedCleaned)) {
                            queryIsContainedInADesuConjugation = true;
                            break;
                        }
                    }
                    break;
                case Globals.VERB_FAMILY_KURU:
                    currentFamilyConjugations = Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE.get(familyIndex);
                    for (int column = 1; column < numberOfSheetCols; column++) {
                        currentFamilyConj = currentFamilyConjugations[column];
                        if (currentFamilyConj.equals(mPreparedCleaned) || currentFamilyConj.equals(mPreparedTranslHiragana)) {
                            queryIsContainedInAKuruConjugation = true;
                            break;
                        }
                    }
                    if (queryIsContainedInAKuruConjugation) break;
                    currentFamilyConjugations = Globals.GLOBAL_VERB_KANJI_CONJ_DATABASE.get(familyIndex);
                    for (int column = 1; column < numberOfSheetCols; column++) {
                        currentFamilyConj = currentFamilyConjugations[column];
                        if (currentFamilyConj.equals(mPreparedCleaned)) {
                            queryIsContainedInAKuruConjugation = true;
                            break;
                        }
                    }
                    break;
                case Globals.VERB_FAMILY_SURU:
                    currentFamilyConjugations = Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE.get(familyIndex);
                    for (int column = 1; column < numberOfSheetCols; column++) {
                        currentFamilyConj = currentFamilyConjugations[column];
                        if (currentFamilyConj.equals(mPreparedCleaned) || currentFamilyConj.equals(mPreparedTranslHiragana)) {
                            queryIsContainedInASuruConjugation = true;
                            break;
                        }
                    }
                    if (queryIsContainedInASuruConjugation) break;
                    currentFamilyConjugations = Globals.GLOBAL_VERB_KANJI_CONJ_DATABASE.get(familyIndex);
                    for (int column = 1; column < numberOfSheetCols; column++) {
                        currentFamilyConj = currentFamilyConjugations[column];
                        if (currentFamilyConj.equals(mPreparedCleaned)) {
                            queryIsContainedInASuruConjugation = true;
                            break;
                        }
                    }
                    break;
                case Globals.VERB_FAMILY_RU_ICHIDAN:
                    currentFamilyConjugations = Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE.get(familyIndex);
                    String currentConjugation;
                    for (int column = Globals.COLUMN_VERB_ISTEM; column < numberOfSheetCols; column++) {
                        currentConjugation = OvUtilsGeneral.concat(new String[]{ "i", currentFamilyConjugations[column]});
                        if (currentConjugation.contains(mPreparedCleaned) || currentConjugation.contains(mPreparedTranslHiragana)) {
                            queryIsContainedInIruVerbConjugation = true;
                            break;
                        }
                    }
                    break;
                default:
                    currentFamilyConjugations = Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE.get(familyIndex);
                    for (int column = Globals.COLUMN_VERB_ISTEM; column < numberOfSheetCols; column++) {
                        currentFamilyConj = currentFamilyConjugations[column];
                        if (currentFamilyConj.contains(mPreparedCleaned) || currentFamilyConj.contains(mPreparedTranslHiragana)) {
                            queryIsContainedInNormalFamilyConjugation = true;
                            break;
                        }
                    }
                    if (queryIsContainedInNormalFamilyConjugation) break;
                    currentFamilyConjugations = Globals.GLOBAL_VERB_KANJI_CONJ_DATABASE.get(familyIndex);
                    for (int column = Globals.COLUMN_VERB_ISTEM; column < numberOfSheetCols; column++) {
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
        if (mPreparedQueryTextType == Globals.TEXT_TYPE_LATIN && mPreparedCleaned.length() < 4
                || (mPreparedQueryTextType == Globals.TEXT_TYPE_HIRAGANA || mPreparedQueryTextType == Globals.TEXT_TYPE_KATAKANA) && mPreparedCleaned.length() < 3) {
            onlyRetrieveShortRomajiVerbs = true;
        }
        onlyRetrieveNonJapWords = mPreparedQueryTextType == Globals.TEXT_TYPE_LATIN && mPreparedTranslHiragana.contains("*");
        //endregion

        //region Performing column dilution in order to make the search more efficient (the diluted column ArrayList is used in the Search Algorithm)
        int queryLengthForDilution = 0;
        List<String[]> verbConjugationMaxLengths = new ArrayList<>();
        int conjugationMaxLength;
        if (mPreparedQueryTextType == Globals.TEXT_TYPE_LATIN) {
            verbConjugationMaxLengths = Globals.GLOBAL_VERB_LATIN_CONJ_LENGTHS;
            queryLengthForDilution = mPreparedCleanedLength;
        }
        else if (mPreparedQueryTextType == Globals.TEXT_TYPE_HIRAGANA || mPreparedQueryTextType == Globals.TEXT_TYPE_KATAKANA) {
            verbConjugationMaxLengths = Globals.GLOBAL_VERB_LATIN_CONJ_LENGTHS;
            queryLengthForDilution = mPreparedTranslRomajiLength;
        }
        else if (mPreparedQueryTextType == Globals.TEXT_TYPE_KANJI) {
            verbConjugationMaxLengths = Globals.GLOBAL_VERB_KANJI_CONJ_LENGTHS;
            queryLengthForDilution = mPreparedCleanedLength;
        }

        HashMap<String, List<Integer>> dilutedConjugationColIndexesByFamily = new HashMap<>();
        for (int row=1; row<verbConjugationMaxLengths.size(); row++) {
            String key = verbConjugationMaxLengths.get(row)[0];
            if (key.equals("")) continue;
            String family_name = Globals.VERB_FAMILIES_FULL_NAME_ENG_MAP.get(key);
            List<Integer> dilutedConjugationColIndexesTemp = new ArrayList<>();
            for (int col = Globals.COLUMN_VERB_ISTEM; col < numberOfSheetCols; col++) {
                if (!verbConjugationMaxLengths.get(row)[col].equals(""))
                    conjugationMaxLength = Integer.parseInt(verbConjugationMaxLengths.get(row)[col]);
                else conjugationMaxLength = 0;
                if (conjugationMaxLength >= queryLengthForDilution) dilutedConjugationColIndexesTemp.add(col);
            }
            dilutedConjugationColIndexesByFamily.put(family_name, new ArrayList<>(dilutedConjugationColIndexesTemp));
        }
        //endregion

        OvUtilsGeneral.printLog(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - getMatchingVerbIdsAndCols - Initialized");

        //region Getting the matching words from the Words database and filtering for verbs
        //For words of length>=4, The matches are determined by the word's keywords list.
        List<Word> mMatchingWords;
        if (mWordsFromDictFragment == null) {
            List<Long> mMatchingWordIds = (List<Long>) UtilitiesDb.getMatchingWordIdsAndDoBasicFiltering(
                    false,
                    false,
                    false,
                    preparedQuery,
                    language,
                    false,
                    context)[0];
            mMatchingWords = OvUtilsDb.getWordListByWordIds(mMatchingWordIds, context, Globals.DB_CENTRAL, language);
        } else {
            mMatchingWords = mWordsFromDictFragment;
        }
        String type;
        List<long[]> matchingVerbIdsAndColsFromBasicCharacteristics = new ArrayList<>();
        int counter = 0;
        boolean typeIsVerb;
        for (Word word : mMatchingWords) {
            type = word.getMeaningsEN().get(0).getType();
            typeIsVerb = type.length() > 0 && type.startsWith("V") && !type.contains("VC");
            if (!typeIsVerb) continue;

            //Preventing the input query being a suru verb conjugation from overloading the results
            if (queryIsContainedInASuruConjugation && word.getRomaji().contains(" suru")) {
                if (counter > Globals.MAX_NUM_RESULTS_FOR_SURU_CONJ_SEARCH) continue;
                counter++;
            }

            //If the input querty includes "to ", removing all words that don't have "to " in their english meanings - this prevents overloading search results with irrelevant words
            if (preparedQuery.isVerbWithTo()) {
                for (Word.Meaning meaning : word.getMeaningsEN()) {
                    if (meaning.getMeaning().contains(preparedQuery.getOriginalCleaned())) {
                        matchingVerbIdsAndColsFromBasicCharacteristics.add(new long[]{word.getId(), 0});
                        break;
                    }
                }
            } else {
                matchingVerbIdsAndColsFromBasicCharacteristics.add(new long[]{word.getId(), 0});
            }
        }
        //endregion

        //region Adding the suru verb if the query is contained in the suru conjugations, and limiting total results
        if (queryIsContainedInASuruConjugation) {
            Word suruVerb = OvUtilsDb.getWordsByExactRomajiAndKanjiMatch("suru", "為る", context).get(0);
            boolean alreadyInList = false;
            for (long[] idAndCol : matchingVerbIdsAndColsFromBasicCharacteristics) {
                if (idAndCol[0] == suruVerb.getId()) {
                    alreadyInList = true;
                    break;
                }
            }
            if (!alreadyInList) matchingVerbIdsAndColsFromBasicCharacteristics.add(new long[]{suruVerb.getId(), 0});
        }
        //endregion

        OvUtilsGeneral.printLog(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - getMatchingVerbIdsAndCols - got matching words");

        //region Getting the matching verbs according to the expanded conjugations and updating the conjugation roots if an altSpelling is used
        List<long[]> matchingVerbIdsAndColsFromExpandedConjugations = new ArrayList<>();
        HashMap<Long, Long> matchingVerbIdsAndColsFromBasicCharacteristicsDict = new HashMap<>();
        for (long[] item : matchingVerbIdsAndColsFromBasicCharacteristics) {
            matchingVerbIdsAndColsFromBasicCharacteristicsDict.put(item[0], item[1]);
        }
        boolean verbAlreadyFound;
        boolean hasConjExceptions;
        String lastFamily = "";
        String firstKanji = preparedQuery.getKanjiChars().size() > 0? preparedQuery.getKanjiChars().get(0) : "";
        String[] characteristics;
        char preparedTranslHiraganaChar0 = mPreparedTranslHiragana.charAt(0);
        String preparedCleanedChar0String = mPreparedCleaned.substring(0,1);
        char preparedCleanedChar0 = preparedCleanedChar0String.charAt(0);
        boolean preparedIsLatin = mPreparedQueryTextType == Globals.TEXT_TYPE_LATIN;
        boolean preparedIsKanji = mPreparedQueryTextType == Globals.TEXT_TYPE_KANJI;
        boolean inputQueryFirstKanaIsVowel = preparedTranslHiraganaChar0 == 'あ'
                || preparedTranslHiraganaChar0 == 'え'
                || preparedTranslHiraganaChar0 == 'い'
                || preparedTranslHiraganaChar0 == 'お'
                || preparedTranslHiraganaChar0 == 'う';
        currentFamilyConjugations = Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE_NO_SPACES.get(mFamilyConjugationIndexes.get("su"));

        //There's no point in checking again if the input query is part of the family conjugation,
        // so we only check up to the substring that could contain all but the first char of the input query
        // No check is needed for len(conjugation) < maxCharIndexWhereMatchIsExpected, since we're using only columns diluted by total verb length > mInputQueryContatenatedLength
        int maxCharIndexWhereMatchIsExpected = mPreparedQueryTextType != Globals.TEXT_TYPE_LATIN && mPreparedQueryTextType != Globals.TEXT_TYPE_KANJI ? mPreparedTranslRomajiLength - 1 : mPreparedCleanedLength - 1;

        String familyForDilution;
        for (Verb verb : mCompleteVerbsList) {

            //region Skipping verbs that were already found
            verbAlreadyFound = false;
            for (long id : matchingVerbIdsAndColsFromBasicCharacteristicsDict.keySet()) {
                if (id == verb.getId()) {

                    //Update the active fields for the current verb according to the altSpelling
                    boolean foundAltSpelling = false;
                    for (String altSpelling : verb.getAltSpellings().split(Globals.DB_ELEMENTS_DELIMITER)) {
                        if (altSpelling.equals(mPreparedTranslRomaji) || altSpelling.equals(mPreparedTranslHiragana)) {
                            characteristics = getVerbCharacteristicsFromAltSpelling(altSpelling, verb, mPreparedQueryTextType);
                            if (characteristics.length == 0) continue;
                            verb.setActiveLatinRoot(characteristics[Globals.INDEX_LATIN_ROOT]);
                            verb.setActiveKanjiRoot(characteristics[Globals.INDEX_KANJI_ROOT]);
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
                    OvUtilsDb.updateVerb(verb, context);

                    //Remove the verb from the candidates list since it is already in the final list
                    matchingVerbIdsAndColsFromBasicCharacteristicsDict.remove(id);
                    verbAlreadyFound = true;
                    break;
                }
            }
            if (verbAlreadyFound) continue;
            //endregion

            //region Skipping verbs that don't include the first kanji seen in the input query
            if (preparedIsKanji && !(verb.getKanji().contains(firstKanji) || verb.getAltSpellings().contains(firstKanji))) {
                continue;
            }
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
                        Globals.GLOBAL_VERB_KANJI_CONJ_DATABASE.get(mFamilyConjugationIndexes.get(family)) :
                        Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE_NO_SPACES.get(mFamilyConjugationIndexes.get(family));
                lastFamily = family;
            }
            exceptionIndex = (verb.getExceptionIndex().equals(""))? 0 : Integer.parseInt(verb.getExceptionIndex());

            verbSearchCandidates = new ArrayList<>();
            characteristics = new String[7];
            characteristics[Globals.INDEX_FAMILY] = verb.getFamily();
            characteristics[Globals.INDEX_ROMAJI] = verb.getRomaji();
            characteristics[Globals.INDEX_KANJI] = verb.getKanji();
            characteristics[Globals.INDEX_HIRAGANA_FIRST_CHAR] = verb.getHiraganaFirstChar();
            characteristics[Globals.INDEX_LATIN_ROOT] = verb.getLatinRoot();
            characteristics[Globals.INDEX_KANJI_ROOT] = verb.getKanjiRoot();
            characteristics[Globals.INDEX_ACTIVE_ALTSPELLING] = verb.getRomaji();
            verbSearchCandidates.add(characteristics);

            for (String altSpelling : altSpellingsAsString.split(Globals.DB_ELEMENTS_DELIMITER)) {
                if (altSpelling.equals("")) continue;
                characteristics = getVerbCharacteristicsFromAltSpelling(altSpelling, verb, mPreparedQueryTextType);
                if (characteristics.length != 0) verbSearchCandidates.add(characteristics);
            }
            //endregion

            //region Checking if one of the relevant base words gets a match, and registering it in the match list
            for (String[] verbSearchCandidate : verbSearchCandidates) {

                //region Getting the verb characteristics
                //familyForDilution = OvUtilsResources.getString(Globals.VERB_FAMILIES_FULL_NAME_MAP.get(verbSearchCandidate[Globals.INDEX_FAMILY]), context, Globals.RESOURCE_MAP_VERB_FAMILIES, null);
                familyForDilution = Globals.VERB_FAMILIES_FULL_NAME_MAP.get(verbSearchCandidate[Globals.INDEX_FAMILY]);
                romaji = verbSearchCandidate[Globals.INDEX_ROMAJI];
                hiraganaFirstChar = verbSearchCandidate[Globals.INDEX_HIRAGANA_FIRST_CHAR].charAt(0);
                latinRoot = verbSearchCandidate[Globals.INDEX_LATIN_ROOT].replace(" ","");
                kanjiRoot = verbSearchCandidate[Globals.INDEX_KANJI_ROOT];
                //endregion

                //region Only allowing searches on verbs that satisfy the following conditions (including identical 1st char, kuru/suru/da, query length)
//                if (    !(     ( preparedIsLatin && romaji.charAt(0) == preparedCleanedChar0 )
//                            || ( preparedIsKana && (hiraganaFirstChar == preparedTranslHiraganaChar0) )
//                            || ( preparedIsKanji && kanjiRoot.contains(preparedCleanedChar0String))
//                            || romaji.contains("kuru")
//                            || romaji.equals("suru")
//                            || romaji.equals("da") )
//                        || ( preparedIsLatin && mPreparedCleanedLength < 4 && inputQueryFirstKanaIsVowel && !romaji.contains(mPreparedCleaned))
//                        || ( preparedIsKana && mPreparedCleanedLength < 3 && inputQueryFirstKanaIsVowel && !romaji.contains(mPreparedTranslRomaji))
//                        || ( preparedIsKanji && mPreparedCleanedLength < 3 && kanjiRoot.length()>0 && !mPreparedCleaned.contains(kanjiRoot))
//                        ||      ) {
//                    continue;
//                }
                if ((onlyRetrieveShortRomajiVerbs && romaji.length() > 4)
                ) {
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
                            currentConjugations = Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE_NO_SPACES.get(exceptionIndex);
                            for (int col : dilutedConjugationColIndexesByFamily.get(familyForDilution)) {
                                currentFamilyConj = currentFamilyConjugations[col];
                                currentConj = currentConjugations[col];
                                if (currentConj.equals("")) {
                                    conjugationValue = OvUtilsGeneral.concat(new String[]{
                                            latinRoot,
                                            (currentFamilyConj.length() > maxCharIndexWhereMatchIsExpected)? currentFamilyConj.substring(0, maxCharIndexWhereMatchIsExpected) : currentFamilyConj
                                    });
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
                                conjugationValue = OvUtilsGeneral.concat(new String[]{
                                        latinRoot,
                                        (currentFamilyConj.length() > maxCharIndexWhereMatchIsExpected)? currentFamilyConj.substring(0, maxCharIndexWhereMatchIsExpected) : currentFamilyConj
                                });
                                if (conjugationValue.contains(mPreparedCleaned)) {
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
                            currentConjugations = Globals.GLOBAL_VERB_KANJI_CONJ_DATABASE.get(exceptionIndex);
                            for (int col : dilutedConjugationColIndexesByFamily.get(familyForDilution)) {
                                currentFamilyConj = currentFamilyConjugations[col];
                                currentConj = currentConjugations[col];
                                if (currentConj.equals("")) {
                                    conjugationValue = OvUtilsGeneral.concat(new String[]{
                                            kanjiRoot,
                                            (currentFamilyConj.length() > maxCharIndexWhereMatchIsExpected)? currentFamilyConj.substring(0, maxCharIndexWhereMatchIsExpected) : currentFamilyConj
                                    });
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
                                conjugationValue = OvUtilsGeneral.concat(new String[]{
                                        kanjiRoot,
                                        (currentFamilyConj.length() > maxCharIndexWhereMatchIsExpected)? currentFamilyConj.substring(0, maxCharIndexWhereMatchIsExpected) : currentFamilyConj
                                });

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
                    verb.setActiveAltSpelling(verbSearchCandidate[Globals.INDEX_ACTIVE_ALTSPELLING]);
                    OvUtilsDb.updateVerb(verb, context);

                    //Update the list of match ids
                    matchingVerbIdsAndColsFromExpandedConjugations.add(new long[]{verb.getId(), matchColumn});

                    break;
                }
            }
            //endregion
        }
        //endregion

        OvUtilsGeneral.printLog(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - getMatchingVerbIdsAndCols - got matching verbs");

        List<long[]> matchingVerbIdsAndCols = new ArrayList<>();
        matchingVerbIdsAndCols.addAll(matchingVerbIdsAndColsFromBasicCharacteristics);
        matchingVerbIdsAndCols.addAll(matchingVerbIdsAndColsFromExpandedConjugations);

        OvUtilsGeneral.printLog(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - getMatchingVerbIdsAndCols - Finished");
        return matchingVerbIdsAndCols;
    }

    @NotNull
    public static List<Verb> getVerbsWithConjugations(@NotNull List<long[]> matchingVerbIdAndColList,
                                                      List<Word> matchingWords,
                                                      HashMap<String, Integer> mFamilyConjugationIndexes,
                                                      List<ConjugationTitle> mConjugationTitles,
                                                      Context context, String language) {

        if (matchingVerbIdAndColList.size() == 0) return new ArrayList<>();

        //region Initializations
        List<Verb> verbs = new ArrayList<>();
        Word currentWord;
        Verb currentVerb;
        long matchingVerbId;
        int conjLength;
        int numberOfSheetCols = Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE.get(0).length;
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
        List<Verb> matchingVerbsBeforeOrderingByWordId = OvUtilsDb.getVerbListByVerbIds(ids, context);
        for (Verb verb : matchingVerbsBeforeOrderingByWordId) {
            if (verb.getActiveLatinRoot().equals("0")) verb.setActiveLatinRoot("");
        }
        List<Verb> matchingVerbs = new ArrayList<>();
        boolean found;
        for (Word word : matchingWords) {
            found = false;
            for (Verb verb : matchingVerbsBeforeOrderingByWordId) {
                if (verb.getId() == word.getId()) {
                    matchingVerbs.add(verb);
                    found = true;
                    break;
                }
            }
            if (!found) {
                word.setId(word.getId());
                OvUtilsGeneral.printLog(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - ERROR! Missing verb for word with id" + word.getId());
            }
        }
        //endregion

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
            currentConjugationsRowLatin = Arrays.copyOf(Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE.get(currentFamilyConjugationsIndex), numberOfSheetCols);
            currentConjugationsRowKanji = Arrays.copyOf(Globals.GLOBAL_VERB_KANJI_CONJ_DATABASE.get(currentFamilyConjugationsIndex), numberOfSheetCols);

            //region Setting the verb's basic characteristics for display
            List<Word.Meaning> meanings;
            String languageText = OvUtilsResources.getLanguageText(language, context);
            switch (language) {
                case Globals.LANG_STR_EN:
                    meanings = currentWord.getMeaningsEN();
                    break;
                case Globals.LANG_STR_FR:
                    meanings = currentWord.getMeaningsFR();
                    break;
                case Globals.LANG_STR_ES:
                    meanings = currentWord.getMeaningsES();
                    break;
                default: meanings = currentWord.getMeaningsEN();
            }
            String extract = "";
            if (meanings == null || meanings.size() == 0) {
                meanings = currentWord.getMeaningsEN();
                extract = OvUtilsGeneral.concat(new String[]{
                        "[", OvUtilsResources.getString("meanings_in", context, Globals.RESOURCE_MAP_GENERAL, language),
                        " ", languageText.toLowerCase(), " ",
                        OvUtilsResources.getString("unavailable", context, Globals.RESOURCE_MAP_GENERAL, language), "] "});
            }
            extract = OvUtilsGeneral.concat(new String[]{
                    UtilitiesGeneral.removeDuplicatesFromCommaList(UtilitiesDb.getMeaningsExtract(meanings, Globals.BALANCE_POINT_REGULAR_DISPLAY)),
                    extract});
            currentVerb.setMeaning(extract);

            switch (currentVerb.getTrans()) {
                case "T": currentVerb.setTrans(OvUtilsResources.getString("trans_", context, Globals.RESOURCE_MAP_GENERAL, language)); break;
                case "I": currentVerb.setTrans(OvUtilsResources.getString("intrans_", context, Globals.RESOURCE_MAP_GENERAL, language)); break;
                case "T/I": currentVerb.setTrans(OvUtilsResources.getString("trans_intrans_", context, Globals.RESOURCE_MAP_GENERAL, language)); break;
            }

            if (Globals.VERB_FAMILIES_FULL_NAME_MAP.containsKey(currentVerb.getFamily())) {
                String value = Globals.VERB_FAMILIES_FULL_NAME_MAP.get(currentVerb.getFamily());
                currentVerb.setFamily(OvUtilsResources.getString(value, context, Globals.RESOURCE_MAP_VERB_FAMILIES, language));
            }
            //endregion

            //region Getting the conjugations row
            currentConjugationExceptionsRowLatin = new String[numberOfSheetCols];
            currentConjugationExceptionsRowKanji = new String[numberOfSheetCols];
            int indexOfExceptionConjugations = Integer.parseInt(currentVerb.getExceptionIndex());

            if (indexOfExceptionConjugations != currentFamilyConjugationsIndex) {
                currentConjugationExceptionsRowLatin = Arrays.copyOf(Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE_NO_SPACES.get(indexOfExceptionConjugations), numberOfSheetCols);
                currentConjugationExceptionsRowKanji = Arrays.copyOf(Globals.GLOBAL_VERB_KANJI_CONJ_DATABASE.get(indexOfExceptionConjugations), numberOfSheetCols);
            }
            else {
                Arrays.fill(currentConjugationExceptionsRowLatin, "");
                Arrays.fill(currentConjugationExceptionsRowKanji, "");
            }

            for (int col = Globals.COLUMN_VERB_ISTEM; col < numberOfSheetCols; col++) {

                if (!currentConjugationExceptionsRowLatin[col].equals("")) currentConjugationsRowLatin[col] = currentConjugationExceptionsRowLatin[col];
                else {
                    conjLength = currentConjugationsRowLatin[col].length();
                    if (conjLength > 3 && currentConjugationsRowLatin[col].startsWith("(o)")) {
                        currentConjugationsRowLatin[col] = OvUtilsGeneral.concat(new String[]{"(o)", currentVerb.getActiveLatinRoot(), currentConjugationsRowLatin[col].substring(3, conjLength)});
                    } else {
                        currentConjugationsRowLatin[col] = OvUtilsGeneral.concat(new String[]{currentVerb.getActiveLatinRoot(), currentConjugationsRowLatin[col]});
                    }
                }

                if (!currentConjugationExceptionsRowKanji[col].equals("")) currentConjugationsRowKanji[col] = currentConjugationExceptionsRowKanji[col];
                else {
                    conjLength = currentConjugationsRowKanji[col].length();
                    if (conjLength > 3 && currentConjugationsRowKanji[col].startsWith("(お)")) {
                        currentConjugationsRowKanji[col] = OvUtilsGeneral.concat(new String[]{"(お)", currentVerb.getActiveKanjiRoot(), currentConjugationsRowKanji[col].substring(3, conjLength)});
                    } else {
                        currentConjugationsRowKanji[col] = OvUtilsGeneral.concat(new String[]{currentVerb.getActiveKanjiRoot(), currentConjugationsRowKanji[col]});
                    }
                }
            }
            //endregion

            //region Getting the verb conjugations and putting each conjugation of the conjugations row into its appropriate category
            conjugationCategories = new ArrayList<>();
            String verbClause = OvUtilsGeneral.concat(new String[]{"[", OvUtilsResources.getString("verb", context, Globals.RESOURCE_MAP_GENERAL, language), "]"});
            for (int categoryIndex = 1; categoryIndex < mConjugationTitles.size(); categoryIndex++) {

                //region Getting the set of Latin and Kanji conjugations according to the current category's subtitle column indexes
                subtitles = mConjugationTitles.get(categoryIndex).getSubtitles();
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
            OvUtilsDb.updateVerbByVerbIdWithParams(matchingVerbId, "", "", "", context);
        }
        //endregion

        return verbs;
    }

    @NotNull
    public static List<long[]> sortMatchingVerbIdAndColList(@NotNull InputQuery mInputQuery, List<long[]> matchingVerbIdsAndCols, List<Word> matchingWords, String language) {

        List<long[]> matchingVerbIdLengthColList = new ArrayList<>();

        boolean queryIsVerbWithTo = mInputQuery.isVerbWithTo();

        String inputQuery = mInputQuery.getOriginalCleaned();
        if (mInputQuery.getOriginalType() == Globals.TEXT_TYPE_HIRAGANA || mInputQuery.getOriginalType() == Globals.TEXT_TYPE_KATAKANA) inputQuery = mInputQuery.getSearchQueriesRomaji().get(0);

        for (int i = 0; i < matchingVerbIdsAndCols.size(); i++) {

            Word currentWord = null;
            for (Word word : matchingWords) {
                if (word.getId() == matchingVerbIdsAndCols.get(i)[0]) {
                    currentWord = word;
                    break;
                }
            }
            if (currentWord == null) continue;

            int ranking = UtilitiesDb.getRankingFromWordAttributes(currentWord, inputQuery, queryIsVerbWithTo, language);

            long[] currentMatchingVerbIdLengthCol = new long[3];
            currentMatchingVerbIdLengthCol[0] = matchingVerbIdsAndCols.get(i)[0];
            currentMatchingVerbIdLengthCol[1] = ranking;
            currentMatchingVerbIdLengthCol[2] = matchingVerbIdsAndCols.get(i)[1];

            matchingVerbIdLengthColList.add(currentMatchingVerbIdLengthCol);
        }

        //Sort the results according to total length
        if (matchingVerbIdLengthColList.size() != 0) {
            matchingVerbIdLengthColList = UtilitiesGeneral.bubbleSortForThreeIntegerList(matchingVerbIdLengthColList);
        }

        List<long[]> matchingVerbIdColListSorted = new ArrayList<>();
        for (long[] element : matchingVerbIdLengthColList) {
            matchingVerbIdColListSorted.add(new long[]{element[0],element[2]});
        }

        return matchingVerbIdColListSorted;

    }

    @NotNull
    public static Object @NotNull [] getConjugationParameters(@NotNull Verb verb, @NotNull InputQuery preparedQuery) {

        String inputQuery = preparedQuery.getOriginal();
        int mPreparedQueryTextType = preparedQuery.getOriginalType();
        String inputQueryLatin = preparedQuery.getRomajiSingleElement();
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
                    if (mPreparedQueryTextType == Globals.TEXT_TYPE_LATIN
                            && preparedConjugationLatin.equals(inputQuery.replace(" ", ""))) {
                        matchingConjugation = conjugation.getConjugationLatin();
                        foundMatch = true;
                    }
                    else if ((mPreparedQueryTextType == Globals.TEXT_TYPE_HIRAGANA || mPreparedQueryTextType == Globals.TEXT_TYPE_KATAKANA)
                            && preparedConjugationLatin.equals(inputQueryLatin.replace(" ", ""))) {
                        matchingConjugation = conjugation.getConjugationLatin();
                        foundMatch = true;
                    }
                    else if (mPreparedQueryTextType == Globals.TEXT_TYPE_KANJI && preparedConjugationKanji.equals(inputQuery)) {
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
                        if (mPreparedQueryTextType == Globals.TEXT_TYPE_LATIN
                                && (preparedConjugationLatin.contains(inputQuery) || conjugation.getConjugationLatin().contains(inputQuery))) {
                            matchingConjugation = conjugation.getConjugationLatin();
                            foundMatch = true;
                        }
                        else if ((mPreparedQueryTextType == Globals.TEXT_TYPE_HIRAGANA || mPreparedQueryTextType == Globals.TEXT_TYPE_KATAKANA)
                                && (preparedConjugationLatin.contains(inputQueryLatin) || conjugation.getConjugationLatin().contains(inputQueryLatin))) {
                            matchingConjugation = conjugation.getConjugationLatin();
                            foundMatch = true;
                        }
                        else if (mPreparedQueryTextType == Globals.TEXT_TYPE_KANJI
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
        parameters[Globals.MATCHING_ID] = verb.getId();
        parameters[Globals.MATCHING_CATEGORY_INDEX] = matchingConjugationCategoryIndex;
        parameters[Globals.MATCHING_CONJUGATION] = matchingConjugation;

        return parameters;
    }

    @NotNull
    public static List<Word> updateWordsWithConjMatchStatus(List<Word> matchingWords, List<Verb> matchingVerbs, @NotNull InputQuery preparedQuery) {

        String mPreparedQuery = preparedQuery.getOriginal();
        String mPreparedCleaned = preparedQuery.getOriginalCleaned().replaceAll("\\s", "");
        boolean foundExactMatch;
        boolean foundContainedMatch;
        if (matchingWords == null || matchingVerbs == null) return new ArrayList<>();
        for (Word word : matchingWords) {
            for (Verb verb : matchingVerbs) {
                if (verb.getId() == word.getId()) {
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

    @Contract("_, _, _, _, _, _ -> new")
    public static Object @NotNull [] getSortedVerbsWordsAndConjParams(
            @NotNull Context context,
            String inputQuery,
            List<Word> mWordsFromDictFragment,
            List<Verb> mCompleteVerbsList,
            List<ConjugationTitle> mConjugationTitles,
            String language) {

        List<Verb> matchingVerbs;
        List<Word> matchingWords;
        List<Object[]> matchingConjugationParameters = new ArrayList<>();
        List<Verb> matchingVerbsSorted = new ArrayList<>();
        List<Word> matchingWordsSorted = new ArrayList<>();
        List<long[]> mMatchingVerbIdAndColList;

        OvUtilsGeneral.printLog(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - Starting");
        OvUtilsGeneral.printLog(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - Loaded Room Verbs Instance");

        InputQuery preparedQuery = setInputQueryParameters(inputQuery);

        HashMap<String, Integer> mFamilyConjugationIndexes = getFamilyConjugationIndexes();
        OvUtilsGeneral.printLog(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - Initialized parameters");

        mMatchingVerbIdAndColList = getMatchingVerbIdsAndCols(
                language,
                preparedQuery,
                mCompleteVerbsList,
                mWordsFromDictFragment,
                mFamilyConjugationIndexes,
                context);
        OvUtilsGeneral.printLog(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - Got matchingVerbIdsAndCols");

        List<Long> ids = new ArrayList<>();
        for (long[] idsAndCols : mMatchingVerbIdAndColList) { ids.add(idsAndCols[0]); }
        matchingWords = OvUtilsDb.getWordListByWordIds(ids, context, Globals.DB_CENTRAL, language);
        OvUtilsGeneral.printLog(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - Got matchingWords");

        matchingVerbs = getVerbsWithConjugations(
                mMatchingVerbIdAndColList,
                matchingWords,
                mFamilyConjugationIndexes,
                mConjugationTitles,
                context,
                language);

        matchingWords = updateWordsWithConjMatchStatus(matchingWords, matchingVerbs, preparedQuery);
        OvUtilsGeneral.printLog(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - Updated verbs with conjugations");

        List<long[]> matchingVerbIdColListSortedByLength = sortMatchingVerbIdAndColList(preparedQuery, mMatchingVerbIdAndColList, matchingWords, language);
        for (int i = 0; i < matchingVerbIdColListSortedByLength.size(); i++) {
            for (int j = 0; j < matchingWords.size(); j++) {
                Word word = matchingWords.get(j);
                Verb verb = null;
                for (Verb currentVerb : matchingVerbs) {
                    if (currentVerb.getId() == word.getId()){
                        verb = currentVerb;
                        break;
                    }
                }
                if (verb == null) {
                    OvUtilsGeneral.printLog(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - ERROR! Missing verb for word with id" + word.getId());
                    continue;
                }

                if (word.getId() == matchingVerbIdColListSortedByLength.get(i)[0]) {
                    matchingWordsSorted.add(word);
                    matchingVerbsSorted.add(verb);
                    break;
                }
            }
        }
        OvUtilsGeneral.printLog(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - Sorted verbs list");

        for (Verb verb : matchingVerbsSorted) {
            Object[] parameters = getConjugationParameters(verb, preparedQuery);
            matchingConjugationParameters.add(parameters);
        }

        OvUtilsGeneral.printLog(Globals.DEBUG_TAG, "VerbsSearchAsyncTask - Returning objects");
        return new Object[]{matchingVerbsSorted, matchingWordsSorted, matchingConjugationParameters};
    }
    public static List<Verb> getAllVerbsForInputQuery(Context context, @NotNull InputQuery inputQuery) {
        List<Verb> mCompleteVerbsList;
        if (inputQuery.getOriginalType() != Globals.TEXT_TYPE_KANJI) {
            String hiraganaFirstChar = inputQuery.getHiraganaSingleElement().substring(0,1);
            mCompleteVerbsList = OvUtilsDb.getAllVerbsWithHiraganaFirstChar(context, hiraganaFirstChar);
        } else {
            String[] chars = OvUtilsGeneral.splitToChars(inputQuery.getOriginal());
            List<String> firstKanjiChars = new ArrayList<>();
            for (String inputQueryChar : chars) {
                if (UtilitiesQuery.getTextType(inputQueryChar) == Globals.TEXT_TYPE_KANJI) {
                    firstKanjiChars.add(inputQueryChar);
                } else {
                    break;
                }
            }
            String firstKanjiCharsString = OvUtilsGeneral.joinList("", firstKanjiChars);
            mCompleteVerbsList = OvUtilsDb.getAllVerbsWithKanjiFirstChars(context, firstKanjiCharsString);
        }
        return mCompleteVerbsList;
    }

}
