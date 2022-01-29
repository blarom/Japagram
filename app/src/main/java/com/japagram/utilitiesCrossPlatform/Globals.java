package com.japagram.utilitiesCrossPlatform;


import androidx.annotation.IntegerRes;

import com.japagram.BuildConfig;
import com.japagram.data.ConjugationTitle;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Globals {

    public static final int CENTRAL_DB_VERSION = 159;
    public static final int EXTENDED_DB_VERSION = 53;
    public static final int KANJI_DB_VERSION = 29;
    public static final int NAMES_DB_VERSION = 13;

    public static final String DEBUG_TAG = "JAPAGRAM_DEBUG";
    public static final int SMALL_WORD_LENGTH = 3;
    public static final String FIREBASE_EMAIL = BuildConfig.firebaseEmail;
    public static final String FIREBASE_PASS = BuildConfig.firebasePass;
    public static final String NAMES_DB = "namesDb";
    public static final String EXTENDED_DB = "extendedDb";
    public static List<String[]> GLOBAL_SIMILARS_DATABASE;
    public static List<String[]> GLOBAL_VERB_LATIN_CONJ_DATABASE;
    public static List<String[]> GLOBAL_VERB_KANJI_CONJ_DATABASE;
    public static List<String[]> GLOBAL_VERB_LATIN_CONJ_LENGTHS;
    public static List<String[]> GLOBAL_VERB_KANJI_CONJ_LENGTHS;
    public static List<ConjugationTitle> GLOBAL_CONJUGATION_TITLES;
    public static List<String[]> GLOBAL_VERB_LATIN_CONJ_DATABASE_NO_SPACES;
    public static List<String[]> GLOBAL_RADICALS_ONLY_DATABASE;
    public static String[][] GLOBAL_ROMANIZATIONS;

    public static final int RESOURCE_MAP_GENERAL = 0;
    public static final int RESOURCE_MAP_VERB_FAMILIES = 1;
    public static final int RESOURCE_MAP_VERB_CONJ_TITLES = 2;
    public static final int RESOURCE_MAP_PARTS_OF_SPEECH = 3;

    public static final int DB_CENTRAL = 0;
    public static final int DB_EXTENDED = 1;
    public static final int DB_NAMES = 2;

    public static final int RANKING_EXACT_MEANING_MATCH_BONUS = 2000;
    public static final int RANKING_EXACT_WORD_MATCH_BONUS = 500;
    public static final int RANKING_WORD_MATCH_IN_SENTENCE_BONUS = 300;
    public static final int RANKING_WORD_MATCH_IN_PARENTHESES_BONUS = 50;
    public static final int RANKING_LATE_HIT_IN_SENTENCE_PENALTY = 25;
    public static final int RANKING_LATE_HIT_IN_MEANING_ELEMENTS_PENALTY = 50;
    public static final int RANKING_LATE_MEANING_MATCH_PENALTY = 100;

    //Verb Search globals
    public static final int MAX_NUM_RESULTS_FOR_SURU_CONJ_SEARCH = 100;
    public static final int MAX_NUM_RESULTS_FOR_VERB_SEARCH = 50;
    public static final int MATCHING_ID = 0;
    public static final int MATCHING_CATEGORY_INDEX = 1;
    public static final int MATCHING_CONJUGATION = 2;
    public final static int INDEX_FAMILY = 0;
    public final static int INDEX_ROMAJI = 1;
    public final static int INDEX_KANJI = 2;
    public final static int INDEX_HIRAGANA_FIRST_CHAR = 3;
    public final static int INDEX_LATIN_ROOT = 4;
    public final static int INDEX_KANJI_ROOT = 5;
    public final static int INDEX_ACTIVE_ALTSPELLING = 6;

    public static final int WORD_TYPE_VERB_CONJ = 0;
    public static final int WORD_TYPE_I_ADJ_CONJ = 1;
    public static final int WORD_TYPE_NA_ADJ_CONJ = 2;
    public static final int WORD_TYPE_ADVERB = 3;
    public static final int WORD_TYPE_NOUN = 4;
    public static final int WORD_TYPE_VERB = 5;

    public static final int KANJI_SEARCH_RESULT_DEFAULT = 0;
    public static final int KANJI_SEARCH_RESULT_SEARCH_TOO_BROAD = 1;
    public static final int KANJI_SEARCH_RESULT_NO_RESULTS = 2;
    public static final int KANJI_SEARCH_RESULT_NO_JAP_RESULTS = 3;
    public static final int KANJI_SEARCH_RESULT_NO_JAP_NO_PRINTABLE_RESULTS = 4;

    public static final float EXTENDED_DB_LINES_WORDS = 186965.f;
    public static final float EXTENDED_DB_LINES_KANJI_INDEX = 220968.f;
    public static final float EXTENDED_DB_LINES_ENGLISH_INDEX = 68127.f;
    public static final float EXTENDED_DB_LINES_ROMAJI_INDEX = 177165.f;
    public static final float EXTENDED_DB_LINES_FRENCH_INDEX = 12820.f;
    public static final float EXTENDED_DB_LINES_SPANISH_INDEX = 24972.f;

    public static final float EXTENDED_DB_SIZE_WORDS = 16627.f;
    public static final float EXTENDED_DB_SIZE_KANJI_INDEX = 4767.f;
    public static final float EXTENDED_DB_SIZE_ENGLISH_INDEX = 5457.f;
    public static final float EXTENDED_DB_SIZE_ROMAJI_INDEX = 3681.f;
    public static final float EXTENDED_DB_SIZE_FRENCH_INDEX = 474.f;
    public static final float EXTENDED_DB_SIZE_SPANISH_INDEX = 924.f;
    public static final float EXTENDED_DB_SIZE_TOTAL = EXTENDED_DB_SIZE_WORDS + EXTENDED_DB_SIZE_KANJI_INDEX + EXTENDED_DB_SIZE_ENGLISH_INDEX + EXTENDED_DB_SIZE_ROMAJI_INDEX + EXTENDED_DB_SIZE_FRENCH_INDEX + EXTENDED_DB_SIZE_SPANISH_INDEX;

    public static final float NAMES_DB_LINES_WORDS = 737309.f;
    public static final float NAMES_DB_LINES_KANJI_INDEX = 607507.f;
    public static final float NAMES_DB_LINES_ROMAJI_INDEX = 413295.f;

    public static final float NAMES_DB_SIZE_WORDS = 22597.f;
    public static final float NAMES_DB_SIZE_KANJI_INDEX = 12761.f;
    public static final float NAMES_DB_SIZE_ROMAJI_INDEX = 10545.f;
    public static final float NAMES_DB_SIZE_TOTAL = NAMES_DB_SIZE_WORDS + NAMES_DB_SIZE_WORDS + NAMES_DB_SIZE_KANJI_INDEX + NAMES_DB_SIZE_ROMAJI_INDEX;


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
    public static int ColIndexConverter(@NotNull String colIndexLetter) {
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
    public static final int COLUMN_HIRAGANA_FIRST_CHAR          = ColIndexConverter("q");
    public static final int COLUMN_FREQUENCY                    = ColIndexConverter("r");
    public static final int COLUMN_KANJI_FIRST_CHARS            = ColIndexConverter("s");

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
    public final static int XDB_COL_FREQUENCY                  = 8;

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

    public final static int INDEX_FULL = 0;
    public final static int INDEX_ACROSS_2 = 1;
    public final static int INDEX_ACROSS_3 = 2;
    public final static int INDEX_ACROSS_4 = 3;
    public final static int INDEX_DOWN_2 = 4;
    public final static int INDEX_DOWN_3 = 5;
    public final static int INDEX_DOWN_4 = 6;
    public final static int INDEX_THREE_REPEAT = 7;
    public final static int INDEX_FOUR_REPEAT = 8;
    public final static int INDEX_FOURSQUARE = 9;
    public final static int INDEX_FIVE_REPEAT = 10;
    public final static int INDEX_TOPLEFTOUT = 11;
    public final static int INDEX_TOPOUT = 12;
    public final static int INDEX_TOPRIGHTOUT = 13;
    public final static int INDEX_LEFTOUT = 14;
    public final static int INDEX_FULLOUT = 15;
    public final static int INDEX_BOTTOMLEFTOUT = 16;
    public final static int INDEX_BOTTOMOUT = 17;

    public static final int TEXT_TYPE_LATIN = 0;
    public static final int TEXT_TYPE_HIRAGANA = 1;
    public static final int TEXT_TYPE_KATAKANA = 2;
    public static final int TEXT_TYPE_KANJI = 3;
    public static final int TEXT_TYPE_NUMBER = 4;
    public static final int TEXT_TYPE_INVALID = 5;

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

    static @NotNull HashMap<String, String> createVerbFamiliesMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put(VERB_FAMILY_SU_GODAN, "verb_family_su");
        map.put(VERB_FAMILY_KU_GODAN, "verb_family_ku");
        map.put(VERB_FAMILY_IKU_SPECIAL, "verb_family_iku");
        map.put(VERB_FAMILY_YUKU_SPECIAL, "verb_family_yuku");
        map.put(VERB_FAMILY_GU_GODAN, "verb_family_gu");
        map.put(VERB_FAMILY_BU_GODAN, "verb_family_bu");
        map.put(VERB_FAMILY_MU_GODAN, "verb_family_mu");
        map.put(VERB_FAMILY_NU_GODAN, "verb_family_nu");
        map.put(VERB_FAMILY_RU_GODAN, "verb_family_rug");
        map.put(VERB_FAMILY_ARU_SPECIAL, "verb_family_aru");
        map.put(VERB_FAMILY_TSU_GODAN, "verb_family_tsu");
        map.put(VERB_FAMILY_U_GODAN, "verb_family_u");
        map.put(VERB_FAMILY_U_SPECIAL, "verb_family_us");
        map.put(VERB_FAMILY_RU_ICHIDAN, "verb_family_rui");
        map.put(VERB_FAMILY_DA, "verb_family_da");
        map.put(VERB_FAMILY_KURU, "verb_family_kuru");
        map.put(VERB_FAMILY_SURU, "verb_family_suru");
        return map;
    }
    public final static HashMap<String, String> VERB_FAMILIES_FULL_NAME_MAP = createVerbFamiliesMap();

    static @NotNull HashMap<String, String> createVerbFamiliesEngMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("su godan", "verb_family_su");
        map.put("ku godan", "verb_family_ku");
        map.put("iku special class", "verb_family_iku");
        map.put("yuku special class", "verb_family_yuku");
        map.put("gu godan", "verb_family_gu");
        map.put("bu godan", "verb_family_bu");
        map.put("mu godan", "verb_family_mu");
        map.put("nu godan", "verb_family_nu");
        map.put("ru godan", "verb_family_rug");
        map.put("aru special class", "verb_family_aru");
        map.put("tsu godan", "verb_family_tsu");
        map.put("u godan", "verb_family_u");
        map.put("u special class", "verb_family_us");
        map.put("ru ichidan", "verb_family_rui");
        map.put("desu copula", "verb_family_da");
        map.put("kuru verb", "verb_family_kuru");
        map.put("suru verb", "verb_family_suru");
        return map;
    }
    public final static HashMap<String, String> VERB_FAMILIES_FULL_NAME_ENG_MAP = createVerbFamiliesEngMap();

    public static final int MAX_SQL_VARIABLES_FOR_QUERY = 500;
    public static final int BALANCE_POINT_REGULAR_DISPLAY = 4;
    public static final int BALANCE_POINT_HISTORY_DISPLAY = 2;

    private static @NotNull HashMap<Integer, String> createStructureMap() {
        HashMap<Integer, String> map = new HashMap<>();
        map.put(INDEX_FULL, "full");
        map.put(INDEX_ACROSS_2, "across2");
        map.put(INDEX_ACROSS_3, "across3");
        map.put(INDEX_ACROSS_4, "across4");
        map.put(INDEX_DOWN_2, "down2");
        map.put(INDEX_DOWN_3, "down3");
        map.put(INDEX_DOWN_4, "down4");
        map.put(INDEX_THREE_REPEAT, "repeat3special");
        map.put(INDEX_FOUR_REPEAT, "repeat4special");
        map.put(INDEX_FOURSQUARE, "foursquare");
        map.put(INDEX_FIVE_REPEAT, "repeat5special");
        map.put(INDEX_TOPLEFTOUT, "topleftout");
        map.put(INDEX_TOPOUT, "topout");
        map.put(INDEX_TOPRIGHTOUT, "toprightout");
        map.put(INDEX_LEFTOUT, "leftout");
        map.put(INDEX_FULLOUT, "fullout");
        map.put(INDEX_BOTTOMLEFTOUT, "bottomleftout");
        map.put(INDEX_BOTTOMOUT, "bottomout");
        return map;
    }
    public final static HashMap<Integer, String> COMPONENT_STRUCTURES_MAP = createStructureMap();
    @Contract(pure = true)
    public static @NotNull String getKanjiStructureEquivalent(@NotNull String componentDecompositionStructure) {
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

    public static final String QUERY_HISTORY_ELEMENTS_DELIMITER = ";";
    public static final String QUERY_HISTORY_MEANINGS_DELIMITER = "@";
    public static final String QUERY_HISTORY_MEANINGS_DISPLAYED_DELIMITER = "~";

    public static final int LANG_EN = 0;
    public static final int LANG_FR = 1;
    public static final int LANG_ES = 2;

    public static final String LANG_STR_EN = "en";
    public static final String LANG_STR_FR = "fr";
    public static final String LANG_STR_ES = "es";

    private static @NotNull HashMap<String, String> createLanguageMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("english", "en");
        map.put("french", "fr");
        map.put("spanish", "es");
        return map;
    }
    public final static HashMap<String, String> LANGUAGE_CODE_MAP = createLanguageMap();

    private static @NotNull HashMap<String, String> createVerbConjugationsMap() {
        HashMap<String, String> map = new HashMap<>();

        map.put("", "EmptyResult");
        map.put("Basics", "verb_TitleBasics");
        map.put("Basics1", "verb_Basics1");
        map.put("Basics2", "verb_Basics2");
        map.put("Basics3", "verb_Basics3");
        map.put("Basics4", "verb_Basics4");
        map.put("Basics5", "verb_Basics5");
        map.put("Basics6", "verb_Basics6");
        map.put("Basics7", "verb_Basics7");
        map.put("Basics8", "verb_Basics8");
        map.put("Basics9", "verb_Basics9");
        map.put("Basics10", "verb_Basics10");
        map.put("Basics11", "verb_Basics11");
        map.put("Basics12", "verb_Basics12");
        map.put("Basics13", "verb_Basics13");
        map.put("Basics14", "verb_Basics14");
        map.put("Basics15", "verb_Basics15");
        map.put("Basics16", "verb_Basics16");

        map.put("TitleSimpleForm", "verb_TitleSimpleForm");
        map.put("TitleProgressive", "verb_TitleProgressive");
        map.put("TitlePoliteness", "verb_TitlePoliteness");
        map.put("TitleRequest", "verb_TitleRequest");
        map.put("TitleImperative", "verb_TitleImperative");
        map.put("TitleDesire", "verb_TitleDesire");
        map.put("TitleProvisional", "verb_TitleProvisional");
        map.put("TitleVolitional", "verb_TitleVolitional");
        map.put("TitleObligation", "verb_TitleObligation");
        map.put("TitlePresumptive", "verb_TitlePresumptive");
        map.put("TitleAlternative", "verb_TitleAlternative");
        map.put("TitleCausativeA", "verb_TitleCausativeA");
        map.put("TitleCausativePv", "verb_TitleCausativePv");
        map.put("TitlePassive", "verb_TitlePassive");
        map.put("TitlePotential", "verb_TitlePotential");
        map.put("TitleContinuative", "verb_TitleContinuative");
        map.put("TitleCompulsion", "verb_TitleCompulsion");
        map.put("TitleGerund", "verb_TitleGerund");

        map.put("PPr", "verb_PPr");
        map.put("PPs", "verb_PPs");
        map.put("PPrN", "verb_PPrN");
        map.put("PPsN", "verb_PPsN");
        map.put("PlPr", "verb_PlPr");
        map.put("PlPs", "verb_PlPs");
        map.put("PlPrN", "verb_PlPrN");
        map.put("PlPsN", "verb_PlPsN");
        map.put("PPrA", "verb_PPrA");
        map.put("ClPr", "verb_ClPr");
        map.put("ClPrA", "verb_ClPrA");
        map.put("ClPrN", "verb_ClPrN");
        map.put("LPl", "verb_LPl");
        map.put("Hn1", "verb_Hn1");
        map.put("Hn2", "verb_Hn2");
        map.put("Hm1", "verb_Hm1");
        map.put("Hm2", "verb_Hm2");
        map.put("Pl1", "verb_Pl1");
        map.put("Pl2", "verb_Pl2");
        map.put("PlN", "verb_PlN");
        map.put("Hn", "verb_Hn");
        map.put("HnN", "verb_HnN");
        map.put("PPrV", "verb_PPrV");
        map.put("PPrL", "verb_PPrL");
        map.put("Pr3rdPO", "verb_Pr3rdPO");
        map.put("Pba", "verb_Pba");
        map.put("ClPba", "verb_ClPba");
        map.put("ClNba", "verb_ClNba");
        map.put("PNba", "verb_PNba");
        map.put("Plba", "verb_Plba");
        map.put("PlNba", "verb_PlNba");
        map.put("Ptara", "verb_Ptara");
        map.put("PNtara", "verb_PNtara");
        map.put("Pltara", "verb_Pltara");
        map.put("PlNtara", "verb_PlNtara");
        map.put("IIWTS", "verb_IIWTS");
        map.put("P", "verb_P");
        map.put("Ps", "verb_Ps");
        map.put("PN", "verb_PN");
        map.put("PPg", "verb_PPg");
        map.put("PlPg", "verb_PlPg");
        map.put("Pl", "verb_Pl");
        map.put("PPr1", "verb_PPr1");
        map.put("PPr2", "verb_PPr2");
        map.put("PPrN1", "verb_PPrN1");
        map.put("PPrN2", "verb_PPrN2");
        map.put("PPrN3", "verb_PPrN3");
        map.put("PlPr1", "verb_PlPr1");
        map.put("PlPr2", "verb_PlPr2");
        map.put("PlPrN1", "verb_PlPrN1");
        map.put("PlPrN2", "verb_PlPrN2");
        map.put("PlPrN3", "verb_PlPrN3");
        map.put("A", "verb_A");
        map.put("N", "verb_N");
        map.put("APg", "verb_APg");
        map.put("NPg", "verb_NPg");
        map.put("PvPg", "verb_PvPg");
        map.put("PvCond", "verb_PvCond");
        map.put("PsPg", "verb_PsPg");
        map.put("teform", "verb_teform");

        map.put("TitleteformCmp", "verb_TitleteformCmp");
        map.put("teformCmp1", "verb_teformCmp1");
        map.put("teformCmp2", "verb_teformCmp2");
        map.put("teformCmp3", "verb_teformCmp3");
        map.put("teformCmp4", "verb_teformCmp4");
        map.put("teformCmp5", "verb_teformCmp5");
        map.put("teformCmp6", "verb_teformCmp6");
        map.put("teformCmp7", "verb_teformCmp7");
        map.put("teformCmp8", "verb_teformCmp8");
        map.put("teformCmp9", "verb_teformCmp9");
        map.put("teformCmp10", "verb_teformCmp10");
        map.put("teformCmp11", "verb_teformCmp11");
        map.put("teformCmp12", "verb_teformCmp12");
        map.put("teformCmp13", "verb_teformCmp13");
        map.put("teformCmp14", "verb_teformCmp14");

        map.put("TitleteformRequestForPermission", "verb_TitleteformRequestForPermission");
        map.put("teformReq1", "verb_teformReq1");
        map.put("teformReq2", "verb_teformReq2");
        map.put("teformReq3", "verb_teformReq3");
        map.put("teformReq4", "verb_teformReq4");

        map.put("TitleteformConj", "verb_TitleteformConj");
        map.put("teformConj1", "verb_teformConj1");
        map.put("teformConj2", "verb_teformConj2");
        map.put("teformConj3", "verb_teformConj3");
        map.put("teformConj4", "verb_teformConj4");

        map.put("TitleSFConj", "verb_TitleSFConj");
        map.put("SFConj1", "verb_SFConj1");
        map.put("SFConj2", "verb_SFConj2");
        map.put("SFConj3", "verb_SFConj3");
        map.put("SFConj4", "verb_SFConj4");

        map.put("TitlemasuCmp", "verb_TitlemasuCmp");
        map.put("masuCmp1", "verb_masuCmp1");
        map.put("masuCmp2", "verb_masuCmp2");
        map.put("masuCmp3", "verb_masuCmp3");
        map.put("masuCmp4", "verb_masuCmp4");
        map.put("masuCmp5", "verb_masuCmp5");
        map.put("masuCmp6", "verb_masuCmp6");
        map.put("masuCmp7", "verb_masuCmp7");
        map.put("masuCmp8", "verb_masuCmp8");
        map.put("masuCmp9", "verb_masuCmp9");
        map.put("masuCmp10", "verb_masuCmp10");
        map.put("masuCmp11", "verb_masuCmp11");
        map.put("masuCmp12", "verb_masuCmp12");
        map.put("masuCmp13", "verb_masuCmp13");

        map.put("TitleStemMisc", "verb_TitleStemMisc");
        map.put("StemMisc1", "verb_StemMisc1");
        map.put("StemMisc2", "verb_StemMisc2");
        map.put("StemMisc3", "verb_StemMisc3");
        map.put("StemMisc4", "verb_StemMisc4");
        map.put("StemMisc5", "verb_StemMisc5");
        map.put("StemMisc6", "verb_StemMisc6");
        map.put("StemMisc7", "verb_StemMisc7");
        map.put("StemMisc8", "verb_StemMisc8");
        map.put("StemMisc9", "verb_StemMisc9");

        map.put("TitleArch", "verb_TitleArch");
        map.put("Arch1", "verb_Arch1");
        map.put("Arch2", "verb_Arch2");
        map.put("Arch2a", "verb_Arch2a");
        map.put("Arch3", "verb_Arch3");
        map.put("Arch4", "verb_Arch4");

        return map;
    }
    public final static HashMap<String, String> VERB_CONJUGATION_TITLES = createVerbConjugationsMap();

    public final static List<String> NAMES_LIST = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "Ne");

    private static @NotNull HashMap<String, String> createPartsOfSpeechMap() {
        HashMap<String, String> map = new HashMap<>();

        map.put("A", "legend_A");
        map.put("Abr", "legend_Abr");
        map.put("Abs", "legend_Abs");
        map.put("Ac", "legend_Ac");
        map.put("Af", "legend_Af");
        map.put("Ai", "legend_Ai");
        map.put("Aj", "legend_Aj");
        map.put("An", "legend_An");
        map.put("Ana", "legend_Ana");
        map.put("Ano", "legend_Ano");
        map.put("Apn", "legend_Apn");
        map.put("Ati", "legend_Ati");
        map.put("Ato", "legend_Ato");
        map.put("Atr", "legend_Atr");
        map.put("ar", "legend_ar");
        map.put("Ax", "legend_Ax");
        map.put("B", "legend_B");
        map.put("C", "legend_C");
        map.put("CE", "legend_CE");
        map.put("CO", "legend_CO");
        map.put("Col", "legend_Col");
        map.put("coq", "legend_coq");
        map.put("Cu", "legend_Cu");
        map.put("Dg", "legend_Dg");
        map.put("DM", "legend_DM");
        map.put("Dr", "legend_Dr");
        map.put("DW", "legend_DW");
        map.put("Fa", "legend_Fa");
        map.put("Fl", "legend_Fl");
        map.put("Fy", "legend_Fy");
        map.put("GO", "legend_GO");
        map.put("iAC", "legend_iAC");
        map.put("idp", "legend_idp");
        map.put("IES", "legend_IES");
        map.put("JEP", "legend_JEP");
        map.put("LF", "legend_LF");
        map.put("LFt", "legend_LFt");
        map.put("LHm", "legend_LHm");
        map.put("LHn", "legend_LHn");
        map.put("LMt", "legend_LMt");
        map.put("loc", "legend_loc");
        map.put("M", "legend_M");
        map.put("MAC", "legend_MAC");
        map.put("Md", "legend_Md");
        map.put("Mo", "legend_Mo");
        map.put("MSE", "legend_MSE");
        map.put("N", "legend_N");
        map.put("naAC", "legend_naAC");
        map.put("NAdv", "legend_NAdv");
        map.put("Ne", "legend_Ne");
        map.put("NE", "legend_NE");
        map.put("Nn", "legend_Nn");
        map.put("num", "legend_num");
        map.put("Obs", "legend_Obs");
        map.put("OI", "legend_OI");
        map.put("org", "legend_org");
        map.put("P", "legend_P");
        map.put("PC", "legend_PC");
        map.put("Pe", "legend_Pe");
        map.put("Pl", "legend_Pl");
        map.put("PP", "legend_PP");
        map.put("Px", "legend_Px");
        map.put("SI", "legend_SI");
        map.put("Sl", "legend_Sl");
        map.put("Sp", "legend_Sp");
        map.put("Sx", "legend_Sx");
        map.put("T", "legend_T");
        map.put("UNC", "legend_UNC");
        map.put("V", "legend_V");
        map.put("VaruI", "legend_VaruI");
        map.put("VaruT", "legend_VaruT");
        map.put("VbuI", "legend_VbuI");
        map.put("VbuT", "legend_VbuT");
        map.put("VC", "legend_VC");
        map.put("VdaI", "legend_VdaI");
        map.put("VdaT", "legend_VdaT");
        map.put("VguI", "legend_VguI");
        map.put("VguT", "legend_VguT");
        map.put("VikuI", "legend_VikuI");
        map.put("VikuT", "legend_VikuT");
        map.put("VyukuI", "legend_VyukuI");
        map.put("VyukuT", "legend_VyukuT");
        map.put("VkuI", "legend_VkuI");
        map.put("VkuruI", "legend_VkuruI");
        map.put("VkuruT", "legend_VkuruT");
        map.put("VkuT", "legend_VkuT");
        map.put("VmuI", "legend_VmuI");
        map.put("VmuT", "legend_VmuT");
        map.put("VnuI", "legend_VnuI");
        map.put("VnuT", "legend_VnuT");
        map.put("VrugI", "legend_VrugI");
        map.put("VrugT", "legend_VrugT");
        map.put("VruiI", "legend_VruiI");
        map.put("VruiT", "legend_VruiT");
        map.put("VsuI", "legend_VsuI");
        map.put("VsuruI", "legend_VsuruI");
        map.put("VsuruT", "legend_VsuruT");
        map.put("VsuT", "legend_VsuT");
        map.put("VtsuI", "legend_VtsuI");
        map.put("VtsuT", "legend_VtsuT");
        map.put("VuI", "legend_VuI");
        map.put("vul", "legend_vul");
        map.put("VusI", "legend_VusI");
        map.put("VusT", "legend_VusT");
        map.put("VuT", "legend_VuT");
        map.put("Vx", "legend_Vx");
        map.put("ZAc", "legend_ZAc");
        map.put("ZAn", "legend_ZAn");
        map.put("ZAs", "legend_ZAs");
        map.put("ZB", "legend_ZB");
        map.put("ZBb", "legend_ZBb");
        map.put("ZBi", "legend_ZBi");
        map.put("ZBs", "legend_ZBs");
        map.put("ZBt", "legend_ZBt");
        map.put("ZC", "legend_ZC");
        map.put("ZCL", "legend_ZCL");
        map.put("ZEc", "legend_ZEc");
        map.put("ZEg", "legend_ZEg");
        map.put("ZF", "legend_ZF");
        map.put("ZFn", "legend_ZFn");
        map.put("ZG", "legend_ZG");
        map.put("ZGg", "legend_ZGg");
        map.put("ZH", "legend_ZH");
        map.put("ZI", "legend_ZI");
        map.put("ZL", "legend_ZL");
        map.put("ZLw", "legend_ZLw");
        map.put("ZM", "legend_ZM");
        map.put("ZMc", "legend_ZMc");
        map.put("ZMg", "legend_ZMg");
        map.put("ZMj", "legend_ZMj");
        map.put("ZMl", "legend_ZMl");
        map.put("ZMt", "legend_ZMt");
        map.put("ZP", "legend_ZP");
        map.put("ZPh", "legend_ZPh");
        map.put("ZSg", "legend_ZSg");
        map.put("ZSm", "legend_ZSm");
        map.put("ZSp", "legend_ZSp");
        map.put("ZSt", "legend_ZSt");
        map.put("ZZ", "legend_ZZ");

        map.put("0", "legend_NmSu");
        map.put("1", "legend_NmPl");
        map.put("2", "legend_NmU");
        map.put("3", "legend_NmC");
        map.put("4", "legend_NmPr");
        map.put("5", "legend_NmW");
        map.put("6", "legend_NmM");
        map.put("7", "legend_NmF");
        map.put("8", "legend_NmPe");
        map.put("9", "legend_NmG");
        map.put("10", "legend_NmSt");
        map.put("11", "legend_NmO");
        map.put("12", "legend_NmI");
        return map;
    }
    public final static HashMap<String, String> PARTS_OF_SPEECH = createPartsOfSpeechMap();

    public static final String KJ_EX_MATCH                         = "kanji exact match";
    public static final String KJ_ALTS_EX_MATCH                    = "kanji alt spelling exact match";
    public static final String R_EX_MATCH                          = "romaji exact match";
    public static final String R_ALTS_EX_MATCH                     = "romaji alt spelling exact match";
    public static final String FIRST_MEANING_EX_PHRASE_MATCH       = "first meaning exact phrase match";
    public static final String FIRST_MEANING_TO_EX_PHRASE_MATCH    = "first meaning with -to- exact 2-word phrase match";
    public static final String SECOND_MEANING_EX_PHRASE_MATCH      = "second+ meaning exact phrase match";
    public static final String SECOND_MEANING_TO_EX_PHRASE_MATCH   = "second+ meaning with -to- exact 2-word phrase match";
    public static final String KW_EX_MATCH                         = "keyword exact match";
    public static final String KJ_PART_START_MATCH                 = "kanji partial match starts with word";
    public static final String KJ_ALTS_PART_START_MATCH            = "kanji alt spelling partial match starts with word";
    public static final String R_PART_START_MATCH                  = "romaji partial match starts with word";
    public static final String R_ALTS_PART_START_MATCH             = "romaji alt spelling partial match starts with word";
    public static final String FIRST_MEANING_EX_WORD_MATCH         = "first meaning exact word match in multi-word sentence";
    public static final String FIRST_MEANING_TO_EX_WORD_MATCH      = "first meaning with -to- exact word match in 3+ word sentence";
    public static final String SECOND_MEANING_EX_WORD_MATCH        = "second+ meaning exact word match in multi-word sentence";
    public static final String SECOND_MEANING_TO_EX_WORD_MATCH     = "second+ meaning with -to- exact word match in 3+ word sentence";
    public static final String KJ_PART_MATCH                       = "kanji partial match";
    public static final String KANJI_ALTS_PART_MATCH               = "kanji alt spelling partial match";
    public static final String R_PART_MATCH                        = "romaji partial match";
    public static final String R_ALTS_PART_MATCH                   = "romaji alt spelling partial match";
    public static final String KW_PART_MATCH                       = "keyword partial match";
    public static final String FIRST_MEANING_PART_PHRASE_MATCH     = "first meaning partial phrase match in single-word sentence";
    public static final String FIRST_MEANING_TO_PART_PHRASE_MATCH  = "first meaning with -to- partial phrase match in 2-word sentence";
    public static final String SECOND_MEANING_PART_PHRASE_MATCH    = "second+ meaning partial phrase match in single-word sentence";
    public static final String SECOND_MEANING_TO_PART_PHRASE_MATCH = "second+ meaning with -to- partial phrase match in 2-word sentence";
    public static final String FIRST_MEANING_PART_WORD_MATCH       = "first meaning partial word match in multi-word sentence";
    public static final String FIRST_MEANING_TO_PART_WORD_MATCH    = "first meaning with -to- partial word match in 3+ word sentence";
    public static final String SECOND_MEANING_PART_WORD_MATCH      = "second+ meaning partial word match in multi-word sentence";
    public static final String SECOND_MEANING_TO_PART_WORD_MATCH   = "second+ meaning with -to- partial word match in 3+ word sentence";

    public static final int STARTING_RANK_VALUE                    = 10000;
    public static final int LONGER_WORD_PENALTY                    = 1;
    public static final int WORD_IN_PARENTHESIS_PENALTY            = 5;
    public static final int NAME_PENALTY                           = 10;
    public static final int MISSING_LANG_PENALTY                   = 10;
    public static final int COMMON_WORD_BONUS                      = 2;
    public static final int EXACT_CONJ_MATCH_BONUS                 = 2;
    public static final int PART_CONJ_MATCH_BONUS                  = 1;
    public static final int DICT_FREQ_BONUS                        = 2;

    private static @NotNull HashMap<String, Integer> createRankingsMap() {
        HashMap<String, Integer> map = new HashMap<>();
        int current_value = STARTING_RANK_VALUE;
        current_value -= 100 ; map.put(SECOND_MEANING_TO_PART_WORD_MATCH  , current_value);
        current_value -= 100 ; map.put(SECOND_MEANING_PART_WORD_MATCH     , current_value);
        current_value -= 100 ; map.put(FIRST_MEANING_TO_PART_WORD_MATCH   , current_value);
        current_value -= 100 ; map.put(FIRST_MEANING_PART_WORD_MATCH      , current_value);
        current_value -= 100 ; map.put(SECOND_MEANING_TO_PART_PHRASE_MATCH, current_value);
        current_value -= 100 ; map.put(SECOND_MEANING_PART_PHRASE_MATCH   , current_value);
        current_value -= 100 ; map.put(FIRST_MEANING_TO_PART_PHRASE_MATCH , current_value);
        current_value -= 100 ; map.put(FIRST_MEANING_PART_PHRASE_MATCH    , current_value);
        current_value -= 100 ; map.put(KW_PART_MATCH                      , current_value);
        current_value -= 100 ; map.put(R_ALTS_PART_MATCH                  , current_value);
        current_value -= 100 ; map.put(R_PART_MATCH                       , current_value);
        current_value -= 100 ; map.put(KANJI_ALTS_PART_MATCH              , current_value);
        current_value -= 100 ; map.put(KJ_PART_MATCH                      , current_value);
        current_value -= 100 ; map.put(SECOND_MEANING_TO_EX_WORD_MATCH    , current_value);
        current_value -= 100 ; map.put(SECOND_MEANING_EX_WORD_MATCH       , current_value);
        current_value -= 100 ; map.put(FIRST_MEANING_TO_EX_WORD_MATCH     , current_value);
        current_value -= 100 ; map.put(FIRST_MEANING_EX_WORD_MATCH        , current_value);
        current_value -= 100 ; map.put(R_ALTS_PART_START_MATCH            , current_value);
        current_value -= 100 ; map.put(R_PART_START_MATCH                 , current_value);
        current_value -= 100 ; map.put(KJ_ALTS_PART_START_MATCH           , current_value);
        current_value -= 100 ; map.put(KJ_PART_START_MATCH                , current_value);
        current_value -= 100 ; map.put(KW_EX_MATCH                        , current_value);
        current_value -= 100 ; map.put(SECOND_MEANING_TO_EX_PHRASE_MATCH  , current_value);
        current_value -= 100 ; map.put(SECOND_MEANING_EX_PHRASE_MATCH     , current_value);
        current_value -= 100 ; map.put(FIRST_MEANING_TO_EX_PHRASE_MATCH   , current_value);
        current_value -= 100 ; map.put(FIRST_MEANING_EX_PHRASE_MATCH      , current_value);
        current_value -= 100 ; map.put(R_ALTS_EX_MATCH                    , current_value);
        current_value -= 100 ; map.put(R_EX_MATCH                         , current_value);
        current_value -= 100 ; map.put(KJ_ALTS_EX_MATCH                   , current_value);
        current_value -= 100 ; map.put(KJ_EX_MATCH                        , current_value);


        return map;
    }
    public static final HashMap<String, Integer> RANKINGS = createRankingsMap();
    private static @NotNull List<String> getSortedRankConditions() {
        List<String> sortedKeys = new ArrayList<>();
        sortedKeys.add(KJ_EX_MATCH                        );
        sortedKeys.add(KJ_ALTS_EX_MATCH                   );
        sortedKeys.add(R_EX_MATCH                         );
        sortedKeys.add(R_ALTS_EX_MATCH                    );
        sortedKeys.add(FIRST_MEANING_EX_PHRASE_MATCH      );
        sortedKeys.add(FIRST_MEANING_TO_EX_PHRASE_MATCH   );
        sortedKeys.add(SECOND_MEANING_EX_PHRASE_MATCH     );
        sortedKeys.add(SECOND_MEANING_TO_EX_PHRASE_MATCH  );
        sortedKeys.add(KW_EX_MATCH                        );
        sortedKeys.add(KJ_PART_START_MATCH                );
        sortedKeys.add(KJ_ALTS_PART_START_MATCH           );
        sortedKeys.add(R_PART_START_MATCH                 );
        sortedKeys.add(R_ALTS_PART_START_MATCH            );
        sortedKeys.add(FIRST_MEANING_EX_WORD_MATCH        );
        sortedKeys.add(FIRST_MEANING_TO_EX_WORD_MATCH     );
        sortedKeys.add(SECOND_MEANING_EX_WORD_MATCH       );
        sortedKeys.add(SECOND_MEANING_TO_EX_WORD_MATCH    );
        sortedKeys.add(KJ_PART_MATCH                      );
        sortedKeys.add(KANJI_ALTS_PART_MATCH              );
        sortedKeys.add(R_PART_MATCH                       );
        sortedKeys.add(R_ALTS_PART_MATCH                  );
        sortedKeys.add(KW_PART_MATCH                      );
        sortedKeys.add(FIRST_MEANING_PART_PHRASE_MATCH    );
        sortedKeys.add(FIRST_MEANING_TO_PART_PHRASE_MATCH );
        sortedKeys.add(SECOND_MEANING_PART_PHRASE_MATCH   );
        sortedKeys.add(SECOND_MEANING_TO_PART_PHRASE_MATCH);
        sortedKeys.add(FIRST_MEANING_PART_WORD_MATCH      );
        sortedKeys.add(FIRST_MEANING_TO_PART_WORD_MATCH   );
        sortedKeys.add(SECOND_MEANING_PART_WORD_MATCH     );
        sortedKeys.add(SECOND_MEANING_TO_PART_WORD_MATCH  );

//        Set<Map.Entry<String, Integer>> entries = RANKINGS.entrySet();
//
//        Comparator<Map.Entry<String, Integer>> valueComparator = (e1, e2) -> {
//            int e1Value = e1.getValue();
//            int e2Value = e2.getValue();
//            if (e1Value == e2Value) return 0;
//            else if (e1Value > e2Value) return 1;
//            else return 0;
//        };
//
//        List<Map.Entry<String, Integer>> listOfEntries = new ArrayList<>(entries);
//        Collections.sort(listOfEntries, valueComparator);
//        LinkedHashMap<String, Integer> sortedByValue = new LinkedHashMap<>(listOfEntries.size());
//        for(Map.Entry<String, Integer> entry : listOfEntries) { sortedByValue.put(entry.getKey(), entry.getValue()); }
//        Set<Map.Entry<String, Integer>> entrySetSortedByValue = sortedByValue.entrySet();
//        List<String> sortedKeys = new ArrayList<>();
//        for(Map.Entry<String, Integer> mapping : entrySetSortedByValue){
//            sortedKeys.add(mapping.getKey());
//        }
        return sortedKeys;
    }
    public static final int LATIN_WORDS = 0;
    public static final int KANJI_WORDS = 1;
    public static final List<String> SORTED_RANK_CONDITIONS = getSortedRankConditions();
    public static final String LATIN_CHAR_ALPHABET = "etaoinsrhdlcumwfgpybvkjxqzéóàüíáäêèãúôçâöñßùûîõìœëïòðåæþýøžš'";
    public static final String LATIN_CHAR_ALPHABET_CAP = "ETAOINSRHDLCUMWFGPYBVKJXQZÉÓÀÜÍÁÄÊÈÃÚÔÇÂÖÑSSÙÛÎÕÌŒËÏÒÐÅÆÞÝØŽŠ";
    public static final String HIRAGANA_CHAR_ALPHABET = "あいうえおかきくけこがぎぐげごさしすせそざじずぜぞたてとだでどちつづなぬねのんにはひふへほばびぶべぼぱぴぷぺぽまみむめもやゆよらりるれろわをゔっゐゑぢぁゃゅぅょぉぇぃ";
    public static final String KATAKANA_CHAR_ALPHABET = "アイウエオカキクケコガギグゲゴサシスセソザジズゼゾタテトダデドチツヅナニヌネノンハヒフヘホバビブベボパピプポペマミムメモヤユヨラリルレロワヲヴーッヰヱァャュゥォョェィ";
    public static final String KANA_CHAR_ALPHABET = HIRAGANA_CHAR_ALPHABET + KATAKANA_CHAR_ALPHABET;
    public static final String NUMBER_ALPHABET = "1234567890'^";
    public static final String SYMBOLS_ALPHABET = ". ,()/1234567890'^[];…!?-+*&:%$«»¿\"？";
}
