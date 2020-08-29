package com.japagram.resources;


import com.japagram.BuildConfig;
import com.japagram.R;
import com.japagram.data.ConjugationTitle;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public final class Globals {

    public static final String DEBUG_TAG = "JAPAGRAM_DEBUG";
    public static final int SMALL_WORD_LENGTH = 3;
    public static final String firebaseEmail = BuildConfig.firebaseEmail;
    public static final String firebasePass = BuildConfig.firebasePass;
    public static List<String[]> SimilarsDatabase;
    public static List<String[]> VerbLatinConjDatabase;
    public static List<ConjugationTitle> ConjugationTitles;
    public static List<String[]> VerbKanjiConjDatabase;
    public static List<String[]> VerbLatinConjDatabaseNoSpaces;
    public static List<String[]> RadicalsOnlyDatabase;
    public static List<String[]> Romanizations;

    public static final int CENTRAL_DB_VERSION = 135;
    public static final int KANJI_DB_VERSION = 26;
    public static final int EXTENDED_DB_VERSION = 32;
    public static final int NAMES_DB_VERSION = 13;

    public static final int KANJI_SEARCH_RESULT_DEFAULT = 0;
    public static final int KANJI_SEARCH_RESULT_SEARCH_TOO_BROAD = 1;
    public static final int KANJI_SEARCH_RESULT_NO_RESULTS = 2;
    public static final int KANJI_SEARCH_RESULT_NO_JAP_RESULTS = 3;
    public static final int KANJI_SEARCH_RESULT_NO_JAP_NO_PRINTABLE_RESULTS = 4;

    public static final float extendedDbLinesWords = 162314.f;
    public static final float extendedDbSizeWords = 12917.f;
    public static final float extendedDbLinesKanjiIndex = 190374.f;
    public static final float extendedDbSizeKanjiIndex = 4063.f;
    public static final float extendedDbLinesEnglishIndex = 64639.f;
    public static final float extendedDbSizeEnglishIndex = 4436.f;
    public static final float extendedDbLinesRomajiIndex = 157921.f;
    public static final float extendedDbSizeRomajiIndex = 3169.f;
    public static final float extendedDbSizeFrenchIndex = 143.f;
    public static final float extendedDbSizeSpanishIndex = 624.f;
    public static final float extendedDbSizeTotal = extendedDbSizeWords + extendedDbSizeKanjiIndex + extendedDbSizeEnglishIndex + extendedDbSizeRomajiIndex + extendedDbSizeFrenchIndex + extendedDbSizeSpanishIndex;

    public static final float namesDbLinesWords = 737309.f;
    public static final float namesDbSizeWords = 22597.f;
    public static final float namesDbLinesKanjiIndex = 607507.f;
    public static final float namesDbSizeKanjiIndex = 12761.f;
    public static final float namesDbLinesRomajiIndex = 413295.f;
    public static final float namesDbSizeRomajiIndex = 10545.f;
    public static final float namesDbSizeTotal = namesDbSizeWords + namesDbSizeWords + namesDbSizeKanjiIndex + namesDbSizeRomajiIndex;


    public static final String DB_ELEMENTS_DELIMITER = ";";
    public static final String KANJI_ASSOCIATED_COMPONENTS_DELIMITER = "";
    public static final String[][] EDICT_EXCEPTIONS = new String[][]{
        {"ha", "は"},
        {"wa","は"},
        {"he","へ"},
        {"e","へ"},
        {"deha","では"},
        {"dewa","では"},
        {"niha","には"},
        {"niwa","には"},
        {"kana","かな"},
        {"node","ので"},
        {"nanode","なので"},
        {"to","と"},
        {"ya","や"},
        {"mo","も"},
        {"no","の"},
        {"noga","のが"},
        {"nowo","のを"},
        {"n","ん"},
        {"wo","を"},
        {"wa","わ"},
        {"yo","よ"},
        {"na","な"},
        {"ka","か"},
        {"ga","が"},
        {"ni","に"},
        {"*","ケ"},
        {"*","ヶ"},
        {"noha","のは"},
        {"nowa","のは"},
        {"demo","でも"},
        {"tte","って"},
        {"datte","だって"},
        {"temo","ても"},
        {"ba","ば"},
        {"nakereba","なければ"},
        {"nakereba","無ければ"},
        {"nakya","なきゃ"},
        {"nakya","無きゃ"},
        {"shi","し"},
        {"kara","から"},
        {"dakara","だから"},
        {"tara","たら"},
        {"datara","だたら"},
        {"nakattara","なかったら"},
        {"soshitara","そしたら"},
        {"node","ので"},
        {"nde","んで"},
        {"te","て"},
        {"noni","のに"},
        {"nagara","ながら"},
        {"nagara","乍ら"},
        {"nara","なら"},
        {"dano","だの"},
        {"oyobi", "及び"},
        {"goto", "如"},
        {"nozoi", "除い"},
        {"made", "まで"},
        {"kara", "から"},
        {"toka", "とか"},
        {"yueni", "故に"},
        {"soko de", "其処で"},
        {"sore de", "それで"},
        {"ni yotte", "に因って"},
        {"you de", "ようで"},
        {"no made", "間で"},
        {"bakarini", "許りに"},
        {"kakawarazu", "拘らず"},
        {"soredemo", "それでも"},
        {"soreyori", "それより"},
        {"tadashi", "但し"},
        {"kedo", "けど"},
        {"keredomo", "けれども"},
        {"tokorode", "所で"},
        {"shikashi", "然し"},
        {"soreni", "其れに"},
        {"tari", "たり"},
        {"igai", "以外"},
        {"ato", "あと"},
        {"tameni", "為に"},
        {"tame", "為"},
        {"hazu", "筈"},
        {"nitsuite", "に就いて"},
        {"naru", "なる"},
        {"onaji", "同じ"},
        {"youni", "ように"},
        {"souna", "そうな"},
        {"yori", "より"},
        {"ato de", "後で"},
        {"maeni", "前に"},
        {"sorekara", "それから"},
        {"soshite", "然して"}
    };

    // Defining the column title (and index) of each column in the excel files

    public static final int COLUMN_WORD_ID                      = ColIndexConverter("a");
    public static final int COLUMN_ROMAJI                       = ColIndexConverter("b");
    public static final int COLUMN_KANJI                        = ColIndexConverter("c");
    public static final int COLUMN_ALT_SPELLINGS                = ColIndexConverter("d");
    public static final int COLUMN_COMMON                       = ColIndexConverter("e");
    public static final int COLUMN_EXTRA_KEYWORDS_JAP           = ColIndexConverter("f");
    public static final int COLUMN_PREPOSITION                  = ColIndexConverter("g");
    public static final int COLUMN_KANJI_ROOT                   = ColIndexConverter("h");
    public static final int COLUMN_LATIN_ROOT                   = ColIndexConverter("i");
    public static final int COLUMN_EXCEPTION_INDEX              = ColIndexConverter("j");
    public static final int COLUMN_MEANING_EN_INDEXES           = ColIndexConverter("k");
    public static final int COLUMN_EXTRA_KEYWORDS_EN            = ColIndexConverter("l");
    public static final int COLUMN_MEANING_FR_INDEXES           = ColIndexConverter("m");
    public static final int COLUMN_EXTRA_KEYWORDS_FR            = ColIndexConverter("n");
    public static final int COLUMN_MEANING_ES_INDEXES           = ColIndexConverter("o");
    public static final int COLUMN_EXTRA_KEYWORDS_ES            = ColIndexConverter("p");

    public static final int COLUMN_MEANINGS_MEANING             = ColIndexConverter("b");
    public static final int COLUMN_MEANINGS_TYPE                = ColIndexConverter("c");
    public static final int COLUMN_MEANINGS_EXPLANATION         = ColIndexConverter("d");
    public static final int COLUMN_MEANINGS_RULES               = ColIndexConverter("e");
    public static final int COLUMN_MEANINGS_EXAMPLES            = ColIndexConverter("f");
    public static final int COLUMN_MEANINGS_ANTONYM             = ColIndexConverter("g");
    public static final int COLUMN_MEANINGS_SYNONYM             = ColIndexConverter("h");

    public static final int COLUMN_MULT_EXPLANATIONS_ITEM       = ColIndexConverter("b");
    public static final int COLUMN_MULT_EXPLANATIONS_RULE       = ColIndexConverter("c");
    public static final int COLUMN_MULT_EXPLANATIONS_EXAMPLES   = ColIndexConverter("d");

    public final static int COLUMN_EXAMPLES_ROMAJI              = ColIndexConverter("b");
    public final static int COLUMN_EXAMPLES_KANJI               = ColIndexConverter("c");
    public final static int COLUMN_EXAMPLES_ENGLISH             = ColIndexConverter("d");
    public final static int COLUMN_EXAMPLES_FRENCH              = ColIndexConverter("e");
    public final static int COLUMN_EXAMPLES_SPANISH             = ColIndexConverter("f");

    public final static int COLUMN_VERB_ISTEM                   = ColIndexConverter("j");
    public final static int COLUMN_VERB_MASUSTEM                = ColIndexConverter("p");

    public final static int XDB_COL_INDEX                      = 0;
    public final static int XDB_COL_ROMAJI                     = 1;
    public final static int XDB_COL_KANJI                      = 2;
    public final static int XDB_COL_POS                        = 3;
    public final static int XDB_COL_ALTS                       = 4;
    public final static int XDB_COL_MEANINGS_EN                = 5;
    public final static int XDB_COL_MEANINGS_FR                = 6;
    public final static int XDB_COL_MEANINGS_ES                = 7;

    public final static int NDB_COL_INDEX                      = 0;
    public final static int NDB_COL_ROMAJI                     = 1;
    public final static int NDB_COL_KANJI                      = 2;
    public final static int NDB_COL_POS                        = 3;
    public final static int NDB_COL_MEANINGS_EN                = 4;

    public final static int ROM_COL_HIRAGANA                   = 0;
    public final static int ROM_COL_KATAKANA                   = 1;
    public final static int ROM_COL_WAAPURO                    = 2;
    public final static int ROM_COL_MOD_HEPBURN                = 3;
    public final static int ROM_COL_NIHON_SHIKI                = 4;
    public final static int ROM_COL_KUNREI_SHIKI               = 5;

    public final static int ROM_WAAPURO                        = 0;
    public final static int ROM_MOD_HEPBURN                    = 1;
    public final static int ROM_NIHON_SHIKI                    = 2;
    public final static int ROM_KUNREI_SHIKI                   = 3;

    public final static int Index_full = 0;
    public final static int Index_across2 = 1;
    public final static int Index_across3 = 2;
    public final static int Index_across4 = 3;
    public final static int Index_down2 = 4;
    public final static int Index_down3 = 5;
    public final static int Index_down4 = 6;
    public final static int Index_three_repeat = 7;
    public final static int Index_four_repeat = 8;
    public final static int Index_foursquare = 9;
    public final static int Index_five_repeat = 10;
    public final static int Index_topleftout = 11;
    public final static int Index_topout = 12;
    public final static int Index_toprightout = 13;
    public final static int Index_leftout = 14;
    public final static int Index_fullout = 15;
    public final static int Index_bottomleftout = 16;
    public final static int Index_bottomout = 17;

    public static final int TYPE_LATIN = 0;
    public static final int TYPE_HIRAGANA = 1;
    public static final int TYPE_KATAKANA = 2;
    public static final int TYPE_KANJI = 3;
    public static final int TYPE_NUMBER = 4;
    public static final int TYPE_INVALID = 5;

    public static final int RADICAL_KANA = 0;
    public static final int RADICAL_UTF8 = 1;
    public static final int RADICAL_NUM = 2;
    public static final int RADICAL_NUM_STROKES = 3;
    public static final int RADICAL_NAME_EN = 4;
    public static final int RADICAL_NAME_FR = 5;
    public static final int RADICAL_NAME_ES = 6;

    public final static int KANJI_ON_READING = 0;
    public final static int KANJI_KUN_READING = 1;
    public final static int KANJI_NAME_READING = 2;
    public final static int KANJI_MEANING = 3;
    public final static int DECOMP_KANJI_LIST_INDEX = 0;
    public final static int DECOMP_RADICAL_ITERATION = 1;
    public final static int DECOMP_PARENT_ALREADY_DISPLAYED = 2;

    public static final String VERB_FAMILY_BU_GODAN = "bu";
    public static final String VERB_FAMILY_DA = "da";
    public static final String VERB_FAMILY_GU_GODAN = "gu";
    public static final String VERB_FAMILY_KU_GODAN = "ku";
    public static final String VERB_FAMILY_IKU_SPECIAL = "iku";
    public static final String VERB_FAMILY_YUKU_SPECIAL = "yuku";
    public static final String VERB_FAMILY_KURU = "kuru";
    public static final String VERB_FAMILY_MU_GODAN = "mu";
    public static final String VERB_FAMILY_NU_GODAN = "nu";
    public static final String VERB_FAMILY_RU_GODAN = "rug";
    public static final String VERB_FAMILY_RU_ICHIDAN = "rui";
    public static final String VERB_FAMILY_ARU_SPECIAL = "aru";
    public static final String VERB_FAMILY_SU_GODAN = "su";
    public static final String VERB_FAMILY_SURU = "suru";
    public static final String VERB_FAMILY_TSU_GODAN = "tsu";
    public static final String VERB_FAMILY_U_GODAN = "u";
    public static final String VERB_FAMILY_U_SPECIAL = "us";

    public final static HashMap<String, Integer> VERB_FAMILIES_FULL_NAME_MAP = createVerbFamiliesMap();
    private static HashMap<String, Integer> createVerbFamiliesMap() {
        HashMap<String, Integer> map = new HashMap<>();
        map.put(VERB_FAMILY_SU_GODAN, R.string.verb_family_su);
        map.put(VERB_FAMILY_KU_GODAN, R.string.verb_family_ku);
        map.put(VERB_FAMILY_IKU_SPECIAL, R.string.verb_family_iku);
        map.put(VERB_FAMILY_YUKU_SPECIAL, R.string.verb_family_yuku);
        map.put(VERB_FAMILY_GU_GODAN, R.string.verb_family_gu);
        map.put(VERB_FAMILY_BU_GODAN, R.string.verb_family_bu);
        map.put(VERB_FAMILY_MU_GODAN, R.string.verb_family_mu);
        map.put(VERB_FAMILY_NU_GODAN, R.string.verb_family_nu);
        map.put(VERB_FAMILY_RU_GODAN, R.string.verb_family_rug);
        map.put(VERB_FAMILY_ARU_SPECIAL, R.string.verb_family_aru);
        map.put(VERB_FAMILY_TSU_GODAN, R.string.verb_family_tsu);
        map.put(VERB_FAMILY_U_GODAN, R.string.verb_family_u);
        map.put(VERB_FAMILY_U_SPECIAL, R.string.verb_family_us);
        map.put(VERB_FAMILY_RU_ICHIDAN, R.string.verb_family_rui);
        map.put(VERB_FAMILY_DA, R.string.verb_family_da);
        map.put(VERB_FAMILY_KURU, R.string.verb_family_kuru);
        map.put(VERB_FAMILY_SURU, R.string.verb_family_suru);
        return map;
    }

    static final int MAX_SQL_VARIABLES_FOR_QUERY = 500;
    public static final int BALANCE_POINT_REGULAR_DISPLAY = 4;
    public static final int BALANCE_POINT_HISTORY_DISPLAY = 2;

    public final static HashMap<Integer, String> COMPONENT_STRUCTURES_MAP = createStructureMap();
    private static HashMap<Integer, String> createStructureMap() {
        HashMap<Integer, String> map = new HashMap<>();
        map.put(Index_full, "full");
        map.put(Index_across2, "across2");
        map.put(Index_across3, "across3");
        map.put(Index_across4, "across4");
        map.put(Index_down2, "down2");
        map.put(Index_down3, "down3");
        map.put(Index_down4, "down4");
        map.put(Index_three_repeat, "repeat3special");
        map.put(Index_four_repeat, "repeat4special");
        map.put(Index_foursquare, "foursquare");
        map.put(Index_five_repeat, "repeat5special");
        map.put(Index_topleftout, "topleftout");
        map.put(Index_topout, "topout");
        map.put(Index_toprightout, "toprightout");
        map.put(Index_leftout, "leftout");
        map.put(Index_fullout, "fullout");
        map.put(Index_bottomleftout, "bottomleftout");
        map.put(Index_bottomout, "bottomout");
        return map;
    }
    public static String getKanjiStructureEquivalent(String componentDecompositionStructure) {
        switch (componentDecompositionStructure) {
            case "c":
            case "refh":
            case "refr":
            case "refv":
            case "rot":
            case "w":
            case "wa":
            case "wb":
            case "wbl":
            case "wtr":
            case "wtl":
            case "wbr":
                return "full";
            case "a2":
            case "a2m":
            case "a2t":
            case "rrefl":
            case "rrefr":
            case "rrotr":
                return "across2";
            case "d2":
            case "d2m":
            case "d2t":
            case "rrefd":
            case "rrotd":
            case "rrotu":
                return "down2";
            case "a3":
                return "across3";
            case "a4":
                return "across4";
            case "d3":
                return "down3";
            case "d4":
                return "down4";
            case "r3gw":
            case "r3tr":
                return "repeat3special";
            case "4sq":
                return "foursquare";
            case "r4sq":
                return "repeat4special";
            case "r5":
                return "repeat5special";
            case "s":
                return "fullout";
            case "sb":
                return "bottomout";
            case "sbl":
            case "sbr":
                return "bottomleftout";
            case "sl":
            case "sr":
                return "leftout";
            case "st":
            case "r3st":
                return "topout";
            case "stl":
            case "r3stl":
                return "topleftout";
            case "str":
            case "r3str":
                return "toprightout";
        }
        return "";
    }

	private static int ColIndexConverter(String colIndexLetter) {
        int colIndexNumber = 0;
        int value = 0;
        if (colIndexLetter.length() == 1) {
            value = (int)colIndexLetter.charAt(0) - (int)'a';
        }
        else if (colIndexLetter.length() == 2) {
            value = ((int)colIndexLetter.charAt(0) - (int)'a' + 1)*26 + (int)colIndexLetter.charAt(1) - (int)'a' + 1;
        }
        else if (colIndexLetter.length() == 3) {
            value = (((int)colIndexLetter.charAt(0) - (int)'a' + 1)*26 + (int)colIndexLetter.charAt(1) - (int)'a' + 1)*26 + (int)colIndexLetter.charAt(2) - (int)'a' + 1;
        }
        colIndexNumber = value;
        return colIndexNumber;
	}

    public static final String QUERY_HISTORY_ELEMENTS_DELIMITER = ";";
    public static final String QUERY_HISTORY_MEANINGS_DELIMITER = "@";
    public static final String QUERY_HISTORY_MEANINGS_DISPLAYED_DELIMITER = "~";

    public static final int LANG_EN = 0;
    public static final int LANG_FR = 1;
    public static final int LANG_ES = 2;

    public static final String LANG_STR_EN = "en";
    public static final String LANG_STR_FR = "fr";
    public static final String LANG_STR_ES = "es";

    public final static HashMap<String, String> LANGUAGE_CODE_MAP = createLanguageMap();
    private static HashMap<String, String> createLanguageMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("english", "en");
        map.put("french", "fr");
        map.put("spanish", "es");
        return map;
    }

    public final static HashMap<String, Integer> VERB_CONJUGATION_TITLES = createVerbConjugationsMap();
    private static HashMap<String, Integer> createVerbConjugationsMap() {
        HashMap<String, Integer> map = new HashMap<>();

        map.put("", R.string.EmptyResult);
        map.put("Basics", R.string.verb_TitleBasics);
        map.put("Basics1", R.string.verb_Basics1);
        map.put("Basics2", R.string.verb_Basics2);
        map.put("Basics3", R.string.verb_Basics3);
        map.put("Basics4", R.string.verb_Basics4);
        map.put("Basics5", R.string.verb_Basics5);
        map.put("Basics6", R.string.verb_Basics6);
        map.put("Basics7", R.string.verb_Basics7);
        map.put("Basics8", R.string.verb_Basics8);
        map.put("Basics9", R.string.verb_Basics9);
        map.put("Basics10", R.string.verb_Basics10);
        map.put("Basics11", R.string.verb_Basics11);
        map.put("Basics12", R.string.verb_Basics12);
        map.put("Basics13", R.string.verb_Basics13);
        map.put("Basics14", R.string.verb_Basics14);
        map.put("Basics15", R.string.verb_Basics15);
        map.put("Basics16", R.string.verb_Basics16);

        map.put("TitleSimpleForm", R.string.verb_TitleSimpleForm);
        map.put("TitleProgressive", R.string.verb_TitleProgressive);
        map.put("TitlePoliteness", R.string.verb_TitlePoliteness);
        map.put("TitleRequest", R.string.verb_TitleRequest);
        map.put("TitleImperative", R.string.verb_TitleImperative);
        map.put("TitleDesire", R.string.verb_TitleDesire);
        map.put("TitleProvisional", R.string.verb_TitleProvisional);
        map.put("TitleVolitional", R.string.verb_TitleVolitional);
        map.put("TitleObligation", R.string.verb_TitleObligation);
        map.put("TitlePresumptive", R.string.verb_TitlePresumptive);
        map.put("TitleAlternative", R.string.verb_TitleAlternative);
        map.put("TitleCausativeA", R.string.verb_TitleCausativeA);
        map.put("TitleCausativePv", R.string.verb_TitleCausativePv);
        map.put("TitlePassive", R.string.verb_TitlePassive);
        map.put("TitlePotential", R.string.verb_TitlePotential);
        map.put("TitleContinuative", R.string.verb_TitleContinuative);
        map.put("TitleCompulsion", R.string.verb_TitleCompulsion);
        map.put("TitleGerund", R.string.verb_TitleGerund);

        map.put("PPr", R.string.verb_PPr);
        map.put("PPs", R.string.verb_PPs);
        map.put("PPrN", R.string.verb_PPrN);
        map.put("PPsN", R.string.verb_PPsN);
        map.put("PlPr", R.string.verb_PlPr);
        map.put("PlPs", R.string.verb_PlPs);
        map.put("PlPrN", R.string.verb_PlPrN);
        map.put("PlPsN", R.string.verb_PlPsN);
        map.put("PPrA", R.string.verb_PPrA);
        map.put("ClPr", R.string.verb_ClPr);
        map.put("ClPrA", R.string.verb_ClPrA);
        map.put("ClPrN", R.string.verb_ClPrN);
        map.put("LPl", R.string.verb_LPl);
        map.put("Hn1", R.string.verb_Hn1);
        map.put("Hn2", R.string.verb_Hn2);
        map.put("Hm1", R.string.verb_Hm1);
        map.put("Hm2", R.string.verb_Hm2);
        map.put("Pl1", R.string.verb_Pl1);
        map.put("Pl2", R.string.verb_Pl2);
        map.put("PlN", R.string.verb_PlN);
        map.put("Hn", R.string.verb_Hn);
        map.put("HnN", R.string.verb_HnN);
        map.put("PPrV", R.string.verb_PPrV);
        map.put("PPrL", R.string.verb_PPrL);
        map.put("Pr3rdPO", R.string.verb_Pr3rdPO);
        map.put("Pba", R.string.verb_Pba);
        map.put("ClPba", R.string.verb_ClPba);
        map.put("ClNba", R.string.verb_ClNba);
        map.put("PNba", R.string.verb_PNba);
        map.put("Plba", R.string.verb_Plba);
        map.put("PlNba", R.string.verb_PlNba);
        map.put("Ptara", R.string.verb_Ptara);
        map.put("PNtara", R.string.verb_PNtara);
        map.put("Pltara", R.string.verb_Pltara);
        map.put("PlNtara", R.string.verb_PlNtara);
        map.put("IIWTS", R.string.verb_IIWTS);
        map.put("P", R.string.verb_P);
        map.put("Ps", R.string.verb_Ps);
        map.put("PN", R.string.verb_PN);
        map.put("PPg", R.string.verb_PPg);
        map.put("PlPg", R.string.verb_PlPg);
        map.put("Pl", R.string.verb_Pl);
        map.put("PPr1", R.string.verb_PlPr1);
        map.put("PPr2", R.string.verb_PlPr2);
        map.put("PPrN1", R.string.verb_PlPrN1);
        map.put("PPrN2", R.string.verb_PlPrN2);
        map.put("PPrN3", R.string.verb_PlPrN3);
        map.put("PlPr1", R.string.verb_PlPr1);
        map.put("PlPr2", R.string.verb_PlPr2);
        map.put("PlPrN1", R.string.verb_PlPrN1);
        map.put("PlPrN2", R.string.verb_PlPrN2);
        map.put("PlPrN3", R.string.verb_PlPrN3);
        map.put("A", R.string.verb_A);
        map.put("N", R.string.verb_N);
        map.put("APg", R.string.verb_APg);
        map.put("NPg", R.string.verb_NPg);
        map.put("PvPg", R.string.verb_PvPg);
        map.put("PvCond", R.string.verb_PvCond);
        map.put("PsPg", R.string.verb_PsPg);
        map.put("teform", R.string.verb_teform);

        map.put("TitleteformCmp", R.string.verb_TitleteformCmp);
        map.put("teformCmp1", R.string.verb_teformCmp1);
        map.put("teformCmp2", R.string.verb_teformCmp2);
        map.put("teformCmp3", R.string.verb_teformCmp3);
        map.put("teformCmp4", R.string.verb_teformCmp4);
        map.put("teformCmp5", R.string.verb_teformCmp5);
        map.put("teformCmp6", R.string.verb_teformCmp6);
        map.put("teformCmp7", R.string.verb_teformCmp7);
        map.put("teformCmp8", R.string.verb_teformCmp8);
        map.put("teformCmp9", R.string.verb_teformCmp9);
        map.put("teformCmp10", R.string.verb_teformCmp10);
        map.put("teformCmp11", R.string.verb_teformCmp11);
        map.put("teformCmp12", R.string.verb_teformCmp12);
        map.put("teformCmp13", R.string.verb_teformCmp13);
        map.put("teformCmp14", R.string.verb_teformCmp14);

        map.put("TitleteformRequestForPermission", R.string.verb_TitleteformRequestForPermission);
        map.put("teformReq1", R.string.verb_teformReq1);
        map.put("teformReq2", R.string.verb_teformReq2);
        map.put("teformReq3", R.string.verb_teformReq3);
        map.put("teformReq4", R.string.verb_teformReq4);

        map.put("TitleteformConj", R.string.verb_TitleteformConj);
        map.put("teformConj1", R.string.verb_teformConj1);
        map.put("teformConj2", R.string.verb_teformConj2);
        map.put("teformConj3", R.string.verb_teformConj3);
        map.put("teformConj4", R.string.verb_teformConj4);

        map.put("TitleSFConj", R.string.verb_TitleSFConj);
        map.put("SFConj1", R.string.verb_SFConj1);
        map.put("SFConj2", R.string.verb_SFConj2);
        map.put("SFConj3", R.string.verb_SFConj3);
        map.put("SFConj4", R.string.verb_SFConj4);

        map.put("TitlemasuCmp", R.string.verb_TitlemasuCmp);
        map.put("masuCmp1", R.string.verb_masuCmp1);
        map.put("masuCmp2", R.string.verb_masuCmp2);
        map.put("masuCmp3", R.string.verb_masuCmp3);
        map.put("masuCmp4", R.string.verb_masuCmp4);
        map.put("masuCmp5", R.string.verb_masuCmp5);
        map.put("masuCmp6", R.string.verb_masuCmp6);
        map.put("masuCmp7", R.string.verb_masuCmp7);
        map.put("masuCmp8", R.string.verb_masuCmp8);
        map.put("masuCmp9", R.string.verb_masuCmp9);
        map.put("masuCmp10", R.string.verb_masuCmp10);
        map.put("masuCmp11", R.string.verb_masuCmp11);
        map.put("masuCmp12", R.string.verb_masuCmp12);
        map.put("masuCmp13", R.string.verb_masuCmp13);

        map.put("TitleStemMisc", R.string.verb_TitleStemMisc);
        map.put("StemMisc1", R.string.verb_StemMisc1);
        map.put("StemMisc2", R.string.verb_StemMisc2);
        map.put("StemMisc3", R.string.verb_StemMisc3);
        map.put("StemMisc4", R.string.verb_StemMisc4);
        map.put("StemMisc5", R.string.verb_StemMisc5);
        map.put("StemMisc6", R.string.verb_StemMisc6);
        map.put("StemMisc7", R.string.verb_StemMisc7);
        map.put("StemMisc8", R.string.verb_StemMisc8);
        map.put("StemMisc9", R.string.verb_StemMisc9);

        map.put("TitleArch", R.string.verb_TitleArch);
        map.put("Arch1", R.string.verb_Arch1);
        map.put("Arch2", R.string.verb_Arch2);
        map.put("Arch3", R.string.verb_Arch3);
        map.put("Arch4", R.string.verb_Arch4);

        return map;
    }

    public final static List<String> NAMES_LIST = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "Ne");

    public final static HashMap<String, Integer> TYPES = createTypesMap();
    private static HashMap<String, Integer> createTypesMap() {
        HashMap<String, Integer> map = new HashMap<>();

        map.put("A", R.string.legend_A);
        map.put("Abr", R.string.legend_Abr);
        map.put("Abs", R.string.legend_Abs);
        map.put("Ac", R.string.legend_Ac);
        map.put("Af", R.string.legend_Af);
        map.put("Ai", R.string.legend_Ai);
        map.put("Aj", R.string.legend_Aj);
        map.put("An", R.string.legend_An);
        map.put("Ana", R.string.legend_Ana);
        map.put("Ano", R.string.legend_Ano);
        map.put("Apn", R.string.legend_Apn);
        map.put("Ati", R.string.legend_Ati);
        map.put("Ato", R.string.legend_Ato);
        map.put("Atr", R.string.legend_Atr);
        map.put("ar", R.string.legend_ar);
        map.put("Ax", R.string.legend_Ax);
        map.put("B", R.string.legend_B);
        map.put("C", R.string.legend_C);
        map.put("CE", R.string.legend_CE);
        map.put("CO", R.string.legend_CO);
        map.put("Col", R.string.legend_Col);
        map.put("coq", R.string.legend_coq);
        map.put("Cu", R.string.legend_Cu);
        map.put("Dg", R.string.legend_Dg);
        map.put("DM", R.string.legend_DM);
        map.put("Dr", R.string.legend_Dr);
        map.put("DW", R.string.legend_DW);
        map.put("Fa", R.string.legend_Fa);
        map.put("Fl", R.string.legend_Fl);
        map.put("Fy", R.string.legend_Fy);
        map.put("GO", R.string.legend_GO);
        map.put("iAC", R.string.legend_iAC);
        map.put("idp", R.string.legend_idp);
        map.put("IES", R.string.legend_IES);
        map.put("JEP", R.string.legend_JEP);
        map.put("LF", R.string.legend_LF);
        map.put("LFt", R.string.legend_LFt);
        map.put("LHm", R.string.legend_LHm);
        map.put("LHn", R.string.legend_LHn);
        map.put("LMt", R.string.legend_LMt);
        map.put("loc", R.string.legend_loc);
        map.put("M", R.string.legend_M);
        map.put("MAC", R.string.legend_MAC);
        map.put("Md", R.string.legend_Md);
        map.put("Mo", R.string.legend_Mo);
        map.put("MSE", R.string.legend_MSE);
        map.put("N", R.string.legend_N);
        map.put("naAC", R.string.legend_naAC);
        map.put("NAdv", R.string.legend_NAdv);
        map.put("Ne", R.string.legend_Ne);
        map.put("NE", R.string.legend_NE);
        map.put("Nn", R.string.legend_Nn);
        map.put("num", R.string.legend_num);
        map.put("Obs", R.string.legend_Obs);
        map.put("OI", R.string.legend_OI);
        map.put("org", R.string.legend_org);
        map.put("P", R.string.legend_P);
        map.put("PC", R.string.legend_PC);
        map.put("Pe", R.string.legend_Pe);
        map.put("Pl", R.string.legend_Pl);
        map.put("PP", R.string.legend_PP);
        map.put("Px", R.string.legend_Px);
        map.put("SI", R.string.legend_SI);
        map.put("Sl", R.string.legend_Sl);
        map.put("Sp", R.string.legend_Sp);
        map.put("Sx", R.string.legend_Sx);
        map.put("T", R.string.legend_T);
        map.put("UNC", R.string.legend_UNC);
        map.put("V", R.string.legend_V);
        map.put("VaruI", R.string.legend_VaruI);
        map.put("VaruT", R.string.legend_VaruT);
        map.put("VbuI", R.string.legend_VbuI);
        map.put("VbuT", R.string.legend_VbuT);
        map.put("VC", R.string.legend_VC);
        map.put("VdaI", R.string.legend_VdaI);
        map.put("VdaT", R.string.legend_VdaT);
        map.put("VguI", R.string.legend_VguI);
        map.put("VguT", R.string.legend_VguT);
        map.put("VikuI", R.string.legend_VikuI);
        map.put("VikuT", R.string.legend_VikuT);
        map.put("VyukuI", R.string.legend_VyukuI);
        map.put("VyukuT", R.string.legend_VyukuT);
        map.put("VkuI", R.string.legend_VkuI);
        map.put("VkuruI", R.string.legend_VkuruI);
        map.put("VkuruT", R.string.legend_VkuruT);
        map.put("VkuT", R.string.legend_VkuT);
        map.put("VmuI", R.string.legend_VmuI);
        map.put("VmuT", R.string.legend_VmuT);
        map.put("VnuI", R.string.legend_VnuI);
        map.put("VnuT", R.string.legend_VnuT);
        map.put("VrugI", R.string.legend_VrugI);
        map.put("VrugT", R.string.legend_VrugT);
        map.put("VruiI", R.string.legend_VruiI);
        map.put("VruiT", R.string.legend_VruiT);
        map.put("VsuI", R.string.legend_VsuI);
        map.put("VsuruI", R.string.legend_VsuruI);
        map.put("VsuruT", R.string.legend_VsuruT);
        map.put("VsuT", R.string.legend_VsuT);
        map.put("VtsuI", R.string.legend_VtsuI);
        map.put("VtsuT", R.string.legend_VtsuT);
        map.put("VuI", R.string.legend_VuI);
        map.put("vul", R.string.legend_vul);
        map.put("VusI", R.string.legend_VusI);
        map.put("VusT", R.string.legend_VusT);
        map.put("VuT", R.string.legend_VuT);
        map.put("Vx", R.string.legend_Vx);
        map.put("ZAc", R.string.legend_ZAc);
        map.put("ZAn", R.string.legend_ZAn);
        map.put("ZAs", R.string.legend_ZAs);
        map.put("ZB", R.string.legend_ZB);
        map.put("ZBb", R.string.legend_ZBb);
        map.put("ZBi", R.string.legend_ZBi);
        map.put("ZBs", R.string.legend_ZBs);
        map.put("ZBt", R.string.legend_ZBt);
        map.put("ZC", R.string.legend_ZC);
        map.put("ZCL", R.string.legend_ZCL);
        map.put("ZEc", R.string.legend_ZEc);
        map.put("ZEg", R.string.legend_ZEg);
        map.put("ZF", R.string.legend_ZF);
        map.put("ZFn", R.string.legend_ZFn);
        map.put("ZG", R.string.legend_ZG);
        map.put("ZGg", R.string.legend_ZGg);
        map.put("ZH", R.string.legend_ZH);
        map.put("ZI", R.string.legend_ZI);
        map.put("ZL", R.string.legend_ZL);
        map.put("ZLw", R.string.legend_ZLw);
        map.put("ZM", R.string.legend_ZM);
        map.put("ZMc", R.string.legend_ZMc);
        map.put("ZMg", R.string.legend_ZMg);
        map.put("ZMj", R.string.legend_ZMj);
        map.put("ZMl", R.string.legend_ZMl);
        map.put("ZMt", R.string.legend_ZMt);
        map.put("ZP", R.string.legend_ZP);
        map.put("ZPh", R.string.legend_ZPh);
        map.put("ZSg", R.string.legend_ZSg);
        map.put("ZSm", R.string.legend_ZSm);
        map.put("ZSp", R.string.legend_ZSp);
        map.put("ZSt", R.string.legend_ZSt);
        map.put("ZZ", R.string.legend_ZZ);
        
        map.put("0", R.string.legend_NmSu);
        map.put("1", R.string.legend_NmPl);
        map.put("2", R.string.legend_NmU);
        map.put("3", R.string.legend_NmC);
        map.put("4", R.string.legend_NmPr);
        map.put("5", R.string.legend_NmW);
        map.put("6", R.string.legend_NmM);
        map.put("7", R.string.legend_NmF);
        map.put("8", R.string.legend_NmPe);
        map.put("9", R.string.legend_NmG);
        map.put("10", R.string.legend_NmSt);
        map.put("11", R.string.legend_NmO);
        map.put("12", R.string.legend_NmI);
        return map;
    }
}
