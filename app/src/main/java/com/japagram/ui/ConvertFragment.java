package com.japagram.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.japagram.R;
import com.japagram.resources.GlobalConstants;
import com.japagram.resources.Utilities;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ConvertFragment extends Fragment {


    private String mInputQuery;

    // Fragment Lifecycle Functions
    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getExtras();
    }
    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Retain this fragment (used to save user inputs on activity creation/destruction)
        setRetainInstance(true);

        // Define that this fragment is related to fragment_conjugator.xml
        return inflater.inflate(R.layout.fragment_convert, container, false);
    }
    @Override public void onStart() {
    super.onStart();

    getConversion(mInputQuery);
}


    // Fragment Modules
    private void getExtras() {
        if (getArguments()!=null) {
            mInputQuery = getArguments().getString(getString(R.string.user_query_word));
        }
    }
    private void getConversion(final String inputQuery) {

        // Gets the output of the InputQueryFragment and makes it available to the current fragment

        if (getActivity() == null) return;

        TextView Conversion = getActivity().findViewById(R.id.transliteration);
        TextView ConversionLatin = getActivity().findViewById(R.id.conversion_waapuro);
        TextView ConversionHiragana = getActivity().findViewById(R.id.conversion_hiragana);
        TextView ConversionKatakana = getActivity().findViewById(R.id.conversion_katakana);
        TextView ResultHiragana = getActivity().findViewById(R.id.Result_hiragana);
        TextView ResultKatakana = getActivity().findViewById(R.id.Result_katakana);
        TextView transliterationWaapuro = getActivity().findViewById(R.id.Result_waapuro);
        TextView transliterationModHepburn = getActivity().findViewById(R.id.Result_mod_hepburn);
        TextView transliterationNihonShiki = getActivity().findViewById(R.id.Result_nihon_shiki);
        TextView transliterationKunreiShiki = getActivity().findViewById(R.id.Result_kunrei_shiki);

        if (TextUtils.isEmpty(inputQuery)) {
            Conversion.setText(getResources().getString(R.string.EnterWord));
            ConversionLatin.setText("");
            ConversionHiragana.setText("");
            ConversionKatakana.setText("");
            ResultHiragana.setText("");
            ResultKatakana.setText("");
            transliterationWaapuro.setText("");
            transliterationModHepburn.setText("");
            transliterationNihonShiki.setText("");
            transliterationKunreiShiki.setText("");
        }
        else {
            Conversion.setText(getResources().getString(R.string.transliteration));
            ConversionLatin.setText(getResources().getString(R.string.conversion_waapuro));
            ConversionHiragana.setText(getResources().getString(R.string.ConversionHiragana));
            ConversionKatakana.setText(getResources().getString(R.string.ConversionKatakana));

            Object[] conversions = getTransliterationsAsLists(inputQuery);

            ResultHiragana.setText(TextUtils.join(",\n", Utilities.removeDuplicatesFromList((List<String>)conversions[GlobalConstants.ROM_COL_HIRAGANA])));
            ResultKatakana.setText(TextUtils.join(",\n", Utilities.removeDuplicatesFromList((List<String>)conversions[GlobalConstants.ROM_COL_KATAKANA])));
            transliterationWaapuro.setText(TextUtils.join(",\n", Utilities.removeDuplicatesFromList((List<String>)conversions[GlobalConstants.ROM_COL_WAAPURO])));
            transliterationModHepburn.setText(TextUtils.join(",\n", Utilities.removeDuplicatesFromList((List<String>)conversions[GlobalConstants.ROM_COL_MOD_HEPBURN])));
            transliterationNihonShiki.setText(TextUtils.join(",\n", Utilities.removeDuplicatesFromList((List<String>)conversions[GlobalConstants.ROM_COL_NIHON_SHIKI])));
            transliterationKunreiShiki.setText(TextUtils.join(",\n", Utilities.removeDuplicatesFromList((List<String>)conversions[GlobalConstants.ROM_COL_KUNREI_SHIKI])));
        }
    }
    private static Object[] getTransliterationsAsLists(String inputQuery) {

        List<String> waapuroRomanizations = getWaapuroRomanizationsFromLatinText(inputQuery);
        List<String> hiraganaConversions = new ArrayList<>();
        List<String> katakanaConversions = new ArrayList<>();
        List<String> waapuroConversions = new ArrayList<>();
        List<String> MHConversions = new ArrayList<>();
        List<String> NSConversions = new ArrayList<>();
        List<String> KSConversions = new ArrayList<>();
        for (String conversion : waapuroRomanizations) {
            String[] HK = getOfficialKana(conversion);
            String hiragana = HK[GlobalConstants.ROM_COL_HIRAGANA];
            String katakana = HK[GlobalConstants.ROM_COL_KATAKANA];
            String[] romanizations = getOfficialRomanizations(hiragana);
            hiraganaConversions.add(hiragana);
            katakanaConversions.add(katakana);
            waapuroConversions.add(romanizations[GlobalConstants.ROM_WAAPURO]);
            MHConversions.add(romanizations[GlobalConstants.ROM_MOD_HEPBURN]);
            NSConversions.add(romanizations[GlobalConstants.ROM_NIHON_SHIKI]);
            KSConversions.add(romanizations[GlobalConstants.ROM_KUNREI_SHIKI]);
        }

        return new Object[]{
                hiraganaConversions,
                katakanaConversions,
                waapuroConversions,
                MHConversions,
                NSConversions,
                KSConversions
        };
    }
    public static List<String> getWaapuroHiraganaKatakana(String input_value) {

        List<String> translation = new ArrayList<>();

        if (TextUtils.isEmpty(input_value)) {
            translation.add("");
            translation.add("");
            translation.add("");
        } else {
            Object[] conversions = getTransliterationsAsLists(input_value);
            translation.add(((List<String>) conversions[GlobalConstants.ROM_COL_WAAPURO]).get(0));
            translation.add(((List<String>) conversions[GlobalConstants.ROM_COL_HIRAGANA]).get(0));
            translation.add(((List<String>) conversions[GlobalConstants.ROM_COL_KATAKANA]).get(0));
        }


        /*
        StringBuilder translation_latin = new StringBuilder();
        StringBuilder translation_hiragana = new StringBuilder();
        StringBuilder translation_katakana = new StringBuilder();
        translation.add(translation_latin.toString());
        translation.add(translation_hiragana.toString());
        translation.add(translation_katakana.toString());

        String character;
        if (!input_value.equals("")) { character = Character.toString(input_value.charAt(0)); }
        else { return translation; }

        translation_latin = new StringBuilder();
        translation_hiragana = new StringBuilder();
        translation_katakana = new StringBuilder();

        String added_string_latin;
        String added_string_hiragana;
        String added_string_katakana;

        String character_next;
        String character_next2;
        String character_last;
        String added_string;
        String added_string_last = "";
        List<String> scriptdetectorOutput;
        List<String> charFinderOutput;

        int final_index = 0;
        final_index = input_value.length() - 1;

        for (int i=0; i <= final_index; i++) {
            character_next = "";
            character_next2 = "";
            character_last = "";

            character = Character.toString(input_value.charAt(i));
            if (i <= final_index-1) { character_next  = Character.toString(input_value.charAt(i+1));}
            if (i <= final_index-2) { character_next2 = Character.toString(input_value.charAt(i+2));}
            if (i>0) { character_last = Character.toString(input_value.charAt(i-1));}

            // Detecting what the current character represents
                scriptdetectorOutput = getPhonemeBasedOnLetter(i, character, character_next, character_next2, character_last);

                i = Integer.parseInt(scriptdetectorOutput.get(0)); added_string = scriptdetectorOutput.get(1);

            // Getting the current string addition
                charFinderOutput = getCharBasedOnPhoneme(i, added_string, character, character_next, added_string_last);
                added_string_last = added_string;

                i = Integer.parseInt(charFinderOutput.get(0));
                added_string_latin = charFinderOutput.get(1);
                added_string_hiragana = charFinderOutput.get(2);
                added_string_katakana = charFinderOutput.get(3);

                // Add the string to the translation
                translation_latin.append(added_string_latin);
                translation_hiragana.append(added_string_hiragana);
                translation_katakana.append(added_string_katakana);

        }

        translation.set(GlobalConstants.TYPE_LATIN, Utilities.removeSpecialCharacters(translation_latin.toString()));
        translation.set(GlobalConstants.TYPE_HIRAGANA, Utilities.removeSpecialCharacters(translation_hiragana.toString()));
        translation.set(GlobalConstants.TYPE_KATAKANA, Utilities.removeSpecialCharacters(translation_katakana.toString()));
        */

        return translation;
    }
    public static int getTextType(String input_value) {

        if (input_value.contains("*") || input_value.contains("＊") || input_value.equals("") || input_value.equals("-") ) { return GlobalConstants.TYPE_INVALID;}

        input_value = Utilities.removeSpecialCharacters(input_value);
        String character;
        int text_type = GlobalConstants.TYPE_INVALID;

        String hiraganaAlphabet = "あいうえおかきくけこがぎぐげごさしすせそざじずぜぞたてとだでどちつづなぬねのんにはひふへほばびぶべぼぱぴぷぺぽまみむめもやゆよらりるれろわをゔっゐゑぢぁゃゅぅょぉぇぃ";
        String katakanaAlphabet = "アイウエオカキクケコガギグゲゴサシスセソザジズゼゾタテトダデドチツヅナニヌネノンハヒフヘホバビブベボパピプポペマミムメモヤユヨラリルレロワヲヴーッヰヱァャュゥォョェィ";
        String latinAlphabet = "aāáÀÂÄÆbcÇdeēÈÉÊËfghiíÎÏjklmnñoōóÔŒpqrstuūúÙÛÜvwxyz".toLowerCase();
        String latinAlphabetCap = latinAlphabet.toUpperCase();
        String numberAlphabet = "0123456789";

        if (!input_value.equals("")) {
            for (int i=0; i<input_value.length();i++) {

                if (text_type == GlobalConstants.TYPE_KANJI) { break;}

                character = Character.toString(input_value.charAt(i));

                if (hiraganaAlphabet.contains(character)) {
                    text_type = GlobalConstants.TYPE_HIRAGANA;
                } else if (katakanaAlphabet.contains(character)) {
                    text_type = GlobalConstants.TYPE_KATAKANA;
                } else if (latinAlphabet.contains(character) || latinAlphabetCap.contains(character)) {
                    text_type = GlobalConstants.TYPE_LATIN;
                } else if (numberAlphabet.contains(character)) {
                    text_type = GlobalConstants.TYPE_NUMBER;
                } else {
                    text_type = GlobalConstants.TYPE_KANJI;
                }
            }
        } else {
            return text_type;
        }

        return text_type;
    }
    private static List<List<String>> addPhonemesToInterpretations(List<List<String>> possibleInterpretations, @NotNull String[] phonemes) {
        List<String> newCharacterList;
        if (phonemes.length == 2) {
            if (possibleInterpretations.size() == 0) {
                newCharacterList = new ArrayList<>();
                newCharacterList.add(phonemes[0]);
                possibleInterpretations.add(newCharacterList);
                newCharacterList = new ArrayList<>();
                newCharacterList.add(phonemes[1]);
                possibleInterpretations.add(newCharacterList);
            } else {
                int initialSize = possibleInterpretations.size();
                for (int i = 0; i < initialSize; i++) {
                    newCharacterList = new ArrayList<>(possibleInterpretations.get(i));
                    newCharacterList.add(phonemes[0]);
                    possibleInterpretations.get(i).add(phonemes[1]);
                    possibleInterpretations.add(newCharacterList);
                }
            }
        }
        else if (phonemes.length == 1) {
            if (possibleInterpretations.size() == 0) {
                newCharacterList = new ArrayList<>();
                newCharacterList.add(phonemes[0]);
                possibleInterpretations.add(newCharacterList);
            } else {
                for (int i = 0; i < possibleInterpretations.size(); i++) {
                    possibleInterpretations.get(i).add(phonemes[0]);
                }
            }
        }
        else return new ArrayList<>();

        return possibleInterpretations;
    }
    public static List<String> getWaapuroRomanizationsFromLatinText(String text) {

        text = text.toLowerCase();
        List<String> finalStrings = new ArrayList<>();

        //Reverting directly from Nihon/Kunrei-Shiki special forms to Waapuro
        text = text.replace("sy","sh")
                .replace("ty","ch");

        //Phonemes that can have multiple Waapuro equivalents are prepared here
        //Note that wi we wo (ゐ ゑ を - i e o in MH/NH/KH romanizations) are not handled here since they could lead to too many false positives
        text = text.replace("j","A")
                .replace("zy","B")
                .replace("ts","C")
                .replace("zu","D")
                .replace("wê","E")
                .replace("dû","F")
                .replace("si","G")
                .replace("ti","H")
                .replaceAll("([^sc])hu","\1I")
                .replaceAll("([^sc])hû","\1J")
                .replace("tû","K")
                .replace("tu","L")
                .replace("zû","M")
                .replace("n\'","N")
                .replace("du","O");

        //Replacing relevant phonemes with the Waapuro equivalent
        List<List<String>> possibleInterpretations = new ArrayList<>();
        String[] newPhonemes;
        for (String character : text.split("(?!^)")) {
            switch (character) {
                case "ō":
                case "ô":
                    newPhonemes = new String[]{"ou", "oo"};
                    break;
                case "A":
                case "B":
                    newPhonemes = new String[]{"j", "dy"};
                    break;
                case "C":
                    newPhonemes = new String[]{"ts"};
                    break;
                case "D":
                case "O":
                    newPhonemes = new String[]{"zu", "du"};
                    break;
                case "E":
                case "ē":
                case "ê":
                    newPhonemes = new String[]{"ee"};
                    break;
                case "F":
                    newPhonemes = new String[]{"zuu"};
                    break;
                case "G":
                    newPhonemes = new String[]{"shi"};
                    break;
                case "H":
                    newPhonemes = new String[]{"chi"};
                    break;
                case "I":
                    newPhonemes = new String[]{"fu"};
                    break;
                case "J":
                    newPhonemes = new String[]{"fuu"};
                    break;
                case "K":
                    newPhonemes = new String[]{"tsuu"};
                    break;
                case "L":
                    newPhonemes = new String[]{"tsu"};
                    break;
                case "M":
                    newPhonemes = new String[]{"duu", "zuu"};
                    break;
                case "N":
                    newPhonemes = new String[]{"n\'"};
                    break;
                case "ā":
                case "â":
                    newPhonemes = new String[]{"aa"};
                    break;
                case "ū":
                case "û":
                    newPhonemes = new String[]{"uu"};
                    break;
                default:
                    newPhonemes = new String[]{character};
                    break;
            }
            possibleInterpretations = addPhonemesToInterpretations(possibleInterpretations, newPhonemes);
        }
        for (int i=0; i<possibleInterpretations.size(); i++) {
            finalStrings.add(TextUtils.join("", possibleInterpretations.get(i)));
        }
        return finalStrings;
    }
    private static String[] getOfficialRomanizations(String kana) {

        if (MainActivity.Romanizations == null) {
            return new String[]{"", "", "", ""};
        }
        //Transliterations performed according to https://en.wikipedia.org/wiki/Romanization_of_Japanese
        /*
        Rules:
        The combination o + u is written ou if they are in two adjacent syllables or it is the end part of terminal form of a verb
        The combination u + u is written uu if they are in two adjacent syllables or it is the end part of terminal form of a verb

         */
        String romanizedKanaWaapuro = kana;
        String romanizedKanaModHepburn = kana;
        String romanizedKanaNihonShiki = kana;
        String romanizedKanaKunreiShiki = kana;
        String[] currentRow;
        String currentKana;
        for (int i=1; i<MainActivity.Romanizations.size(); i++) {
            currentRow = MainActivity.Romanizations.get(i);
            if (currentRow.length < 6) break;

            currentKana = currentRow[GlobalConstants.ROM_COL_HIRAGANA];
            if (!currentKana.equals("")) {
                romanizedKanaWaapuro = romanizedKanaWaapuro.replace(currentKana, currentRow[GlobalConstants.ROM_COL_WAAPURO]);
                romanizedKanaModHepburn = romanizedKanaModHepburn.replace(currentKana, currentRow[GlobalConstants.ROM_COL_MOD_HEPBURN]);
                romanizedKanaNihonShiki = romanizedKanaNihonShiki.replace(currentKana, currentRow[GlobalConstants.ROM_COL_NIHON_SHIKI]);
                romanizedKanaKunreiShiki = romanizedKanaKunreiShiki.replace(currentKana, currentRow[GlobalConstants.ROM_COL_KUNREI_SHIKI]);
            }

            currentKana = currentRow[GlobalConstants.ROM_COL_KATAKANA];
            if (!currentKana.equals("")) {
                romanizedKanaWaapuro = romanizedKanaWaapuro.replace(currentKana, currentRow[GlobalConstants.ROM_COL_WAAPURO]);
                romanizedKanaModHepburn = romanizedKanaModHepburn.replace(currentKana, currentRow[GlobalConstants.ROM_COL_MOD_HEPBURN]);
                romanizedKanaNihonShiki = romanizedKanaNihonShiki.replace(currentKana, currentRow[GlobalConstants.ROM_COL_NIHON_SHIKI]);
                romanizedKanaKunreiShiki = romanizedKanaKunreiShiki.replace(currentKana, currentRow[GlobalConstants.ROM_COL_KUNREI_SHIKI]);
            }
        }

        return new String[]{romanizedKanaWaapuro, romanizedKanaModHepburn, romanizedKanaNihonShiki, romanizedKanaKunreiShiki};
    }
    private static String[] getOfficialKana(String romaji) {

        if (MainActivity.Romanizations == null) {
            return new String[]{"", "", "", ""};
        }
        //Transliterations performed according to https://en.wikipedia.org/wiki/Romanization_of_Japanese
        /*
        Rules:
        The combination o + u is written ou if they are in two adjacent syllables or it is the end part of terminal form of a verb
        The combination u + u is written uu if they are in two adjacent syllables or it is the end part of terminal form of a verb

         */
        String transliteratedToHiragana = romaji;
        String transliteratedToKatakana = romaji;
        String[] currentRow;
        String currentRomaji;
        int[] romajiTypes = new int[]{GlobalConstants.ROM_COL_WAAPURO, GlobalConstants.ROM_COL_MOD_HEPBURN, GlobalConstants.ROM_COL_NIHON_SHIKI, GlobalConstants.ROM_COL_KUNREI_SHIKI};
        for (int romajiType : romajiTypes) {
            for (int i = 1; i < MainActivity.Romanizations.size(); i++) {
                currentRow = MainActivity.Romanizations.get(i);
                if (currentRow.length < 6) break;

                currentRomaji = currentRow[romajiType];
                transliteratedToHiragana = transliteratedToHiragana.replace(currentRomaji, currentRow[GlobalConstants.ROM_COL_HIRAGANA]);
                transliteratedToKatakana = transliteratedToKatakana.replace(currentRomaji, currentRow[GlobalConstants.ROM_COL_KATAKANA]);
            }
        }

        return new String[]{transliteratedToHiragana, transliteratedToKatakana};
    }
}