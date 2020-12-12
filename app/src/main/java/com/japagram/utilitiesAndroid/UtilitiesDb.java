package com.japagram.utilitiesAndroid;

import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;
import com.japagram.utilitiesCrossPlatform.UtilitiesQuery;
import com.japagram.utilitiesCrossPlatform.Globals;
import com.japagram.data.Verb;
import com.japagram.data.Word;
import com.japagram.utilitiesPlatformOverridable.OverridableUtilitiesGeneral;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class UtilitiesDb {
    private static FirebaseDatabase mDatabase;


    //Database creation utilities
    public static FirebaseDatabase getDatabase() {
        //inspired by: https://github.com/firebase/quickstart-android/issues/15
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase;
    }

    @NotNull
    public static String cleanIdentifierForFirebase(String string) {
        if (OverridableUtilitiesGeneral.isEmptyString(string)) return "";
        string = string.replaceAll("\\.", "*");
        string = string.replaceAll("#", "*");
        string = string.replaceAll("\\$", "*");
        string = string.replaceAll("\\[", "*");
        string = string.replaceAll("]", "*");
        //string = string.replaceAll("\\{","*");
        //string = string.replaceAll("}","*");
        return string;
    }

    public static void checkDatabaseStructure(@NotNull List<String[]> databaseFromCsv, String databaseName, int numColumns) {
        for (String[] line : databaseFromCsv) {
            if (line.length < numColumns) {
                Log.v("JapaneseToolbox", "Serious error in row [" + line[0] + "] in " + databaseName + ": CSV file row has less columns than expected! Check for accidental line breaks.");
                break;
            }
        }
    }

    @NotNull
    public static Word createWordFromCsvDatabases(@NotNull List<String[]> centralDatabase,
                                                  List<String[]> meaningsENDatabase,
                                                  List<String[]> meaningsFRDatabase,
                                                  List<String[]> meaningsESDatabase,
                                                  List<String[]> multExplENDatabase,
                                                  List<String[]> multExplFRDatabase,
                                                  List<String[]> multExplESDatabase,
                                                  List<String[]> examplesDatabase,
                                                  @NotNull HashMap<String, Integer> frequenciesHash,
                                                  int centralDbRowIndex) {

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

        //Setting the frequency
        if (matchingWordKanji.equals("為る")) {
            word.setFrequency(frequenciesHash.get("する"));
        } else {
            String key = matchingWordKanji.replace("～", "").replaceAll("する$", "");
            if (frequenciesHash.containsKey(key)) word.setFrequency(frequenciesHash.get(key));
            else {
                String katakana = UtilitiesQuery.getWaapuroHiraganaKatakana(matchingWordRomaji).get(Globals.TYPE_KATAKANA);
                if (frequenciesHash.containsKey(katakana)) word.setFrequency(frequenciesHash.get(katakana));
            }
        }

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
        if (word.getMeaningsEN().size() == 0) {
            OverridableUtilitiesGeneral.printLog(Globals.DEBUG_TAG, "************ ERROR ************ Word with id "
                    + matchingWordId + " (" + word.getRomaji() + " - " + word.getKanji()
                    + ") has invalid english meanings");
            return new Word();
        }

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

    @NotNull
    public static Word createWordFromExtendedDatabase(@NotNull String[] extendedDatabaseRow) {

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

    @NotNull
    public static Word createWordFromNamesDatabase(@NotNull String[] namesDatabaseRow) {

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

    @NotNull
    private static List<Word.Meaning> getMeanings(List<String[]> centralDatabase, List<String[]> meaningsDatabase, int meaningsColumn,
                                                  List<String[]> multExplDatabase, List<String[]> examplesDatabase, int centralDbRowIndex, @NotNull String language) {

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

                    int lenWithoutOpeningParenthesis = meaningElements.get(k).replace("(", "").length();
                    int lenWithoutClosingParenthesis = meaningElements.get(k).replace(")", "").length();
                    if (lenWithoutOpeningParenthesis < lenWithoutClosingParenthesis) valueIsInParentheses = true;
                    else if (lenWithoutOpeningParenthesis > lenWithoutClosingParenthesis) valueIsInParentheses = false;
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

    @NotNull
    public static Verb createVerbFromCsvDatabase(@NotNull List<String[]> verbDatabase, List<String[]> meaningsDatabase, int verbDbRowIndex) {

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
        verb.setHiraganaFirstChar(UtilitiesQuery.getWaapuroHiraganaKatakana(verb.getRomaji()).get(Globals.TYPE_HIRAGANA).substring(0, 1));

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
                if (type.startsWith("V") && (type.substring(lastCharIndex).equals("T") || type.substring(lastCharIndex).equals("I"))) {
                    trans.add(String.valueOf(type.charAt(lastCharIndex)));
                    if (i == 0) verb.setFamily(type.substring(1, lastCharIndex)); //only keeping the verb itself
                    foundVerbType = true;
                    break;
                }
            }

            if (!foundVerbType) {
                OverridableUtilitiesGeneral.printLog(Globals.DEBUG_TAG, "Warning! No VxxxT/I type found for verb " + verb.getRomaji() + " (Meaning index:" + current_MM_index + ")");
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

    @NotNull public static List<String[]> removeSpacesFromConjDb(@NotNull List<String[]> db) {
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
