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

import java.util.ArrayList;
import java.util.Arrays;
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

    if (!TextUtils.isEmpty(mInputQuery)) getConversion(mInputQuery);
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

        TextView Conversion = getActivity().findViewById(R.id.conversion);
        TextView ConversionLatin = getActivity().findViewById(R.id.conversion_latin);
        TextView ConversionHiragana = getActivity().findViewById(R.id.conversion_hiragana);
        TextView ConversionKatakana = getActivity().findViewById(R.id.conversion_katakana);
        TextView ResultLatin = getActivity().findViewById(R.id.Result_latin);
        TextView ResultHiragana = getActivity().findViewById(R.id.Result_hiragana);
        TextView ResultKatakana = getActivity().findViewById(R.id.Result_katakana);

        if (getLatinHiraganaKatakana(inputQuery).get(0).equals("no_input")) {
            Conversion.setText(getResources().getString(R.string.EnterWord));
            ConversionLatin.setText("");
            ConversionHiragana.setText("");
            ConversionKatakana.setText("");
            ResultLatin.setText("");
            ResultHiragana.setText("");
            ResultKatakana.setText("");
        }
        else {
            Conversion.setText(getResources().getString(R.string.Conversion));
            ConversionLatin.setText(getResources().getString(R.string.ConversionLatin));
            ConversionHiragana.setText(getResources().getString(R.string.ConversionHiragana));
            ConversionKatakana.setText(getResources().getString(R.string.ConversionKatakana));
            ResultLatin.setText(getLatinHiraganaKatakana(inputQuery).get(0));
            ResultHiragana.setText(getLatinHiraganaKatakana(inputQuery).get(1));
            ResultKatakana.setText(getLatinHiraganaKatakana(inputQuery).get(2));

        }
    }
    public static List<String> getLatinHiraganaKatakana(String input_value) {

        List<String> translation = new ArrayList<>();
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
    private static List<String> getPhonemeBasedOnLetter(int i, String character, String character_next, String character_next2, String character_last) {

        character = character.toLowerCase();
        character_next = character_next.toLowerCase();
        character_next2 = character_next2.toLowerCase();
        character_last = character_last.toLowerCase();

        String added_string;
        switch (character) {
            case "あ":
                if (character.equals(character_last)) { added_string = "a_double_vowel";
                } else { switch (character_next) {
                    case "ぁ": added_string = "aa"; i++; break;
                    default: added_string = "a"; break;
                } break; } break;
            case "い":
                if (character.equals(character_last)) { added_string = "i_double_vowel";
                } else { switch (character_next) {
                    case "ぃ": added_string = "ii"; i++; break;
                    case "ぇ": added_string = "ye"; i++; break;
                    default: added_string = "i"; break;
                } break; } break;
            case "え":
                if (character.equals(character_last)) { added_string = "e_double_vowel";
                } else { switch (character_next) {
                    case "ぇ": added_string = "ee"; i++; break;
                    default: added_string = "e"; break;
                } break; } break;
            case "お":
                if (character.equals(character_last)) { added_string = "o_double_vowel";
                } else { switch (character_next) {
                    case "ぉ": added_string = "oo"; i++; break;
                    default: added_string = "o"; break;
                } break; } break;
            case "か": added_string = "ka"; break;
            case "き":
                switch (character_next) {
                    case "ゃ": added_string = "kya"; i++; break;
                    case "ゅ": added_string = "kyu"; i++; break;
                    case "ょ": added_string = "kyo"; i++; break;
                    default: added_string = "ki"; break;
                } break;
            case "く":
                switch (character_next) {
                    case "ゃ": added_string = "kwa"; i++; break;
                    case "ゅ": added_string = "*"; i++; break;
                    case "ょ": added_string = "*"; i++; break;
                    default: added_string = "ku"; break;
                } break;
            case "け": added_string = "ke"; break;
            case "こ": added_string = "ko"; break;
            case "が": added_string = "ga"; break;
            case "ぎ":
                switch (character_next) {
                    case "ゃ": added_string = "gya"; i++; break;
                    case "ゅ": added_string = "gyu"; i++; break;
                    case "ょ": added_string = "gyo"; i++; break;
                    default: added_string = "gi"; break;
                } break;
            case "ぐ":
                switch (character_next) {
                    case "ゃ": added_string = "gwa"; i++; break;
                    case "ゅ": added_string = "*"; i++; break;
                    case "ょ": added_string = "*"; i++; break;
                    default: added_string = "gu"; break;
                } break;
            case "げ": added_string = "ge"; break;
            case "ご": added_string = "go"; break;
            case "さ": added_string = "sa"; break;
            case "す": added_string = "su"; break;
            case "せ": added_string = "se"; break;
            case "そ": added_string = "so"; break;
            case "ざ": added_string = "za"; break;
            case "ず": added_string = "zu"; break;
            case "ぜ": added_string = "ze"; break;
            case "ぞ": added_string = "zo"; break;
            case "し":
                switch (character_next) {
                    case "ゃ": added_string = "sha"; i++; break;
                    case "ゅ": added_string = "shu"; i++; break;
                    case "ぇ": added_string = "she"; i++; break;
                    case "ょ": added_string = "sho"; i++; break;
                    default: added_string = "shi"; break;
                } break;
            case "じ":
                switch (character_next) {
                    case "ゃ": added_string = "ja"; i++; break;
                    case "ゅ": added_string = "ju"; i++; break;
                    case "ぇ": added_string = "je"; i++; break;
                    case "ょ": added_string = "jo"; i++; break;
                    default: added_string = "ji"; break;
                } break;
            case "た": added_string = "ta"; break;
            case "て": added_string = "te"; break;
            case "と": added_string = "to"; break;
            case "だ": added_string = "da"; break;
            case "で": added_string = "de"; break;
            case "ど": added_string = "do"; break;
            case "ち":
                switch (character_next) {
                    case "ゃ": added_string = "cha"; i++; break;
                    case "ゅ": added_string = "chu"; i++; break;
                    case "ぇ": added_string = "che"; i++; break;
                    case "ょ": added_string = "cho"; i++; break;
                    default: added_string = "chi"; break;
                } break;
            case "ぢ":
                switch (character_next) {
                    case "ゃ": added_string = "dya"; i++; break;
                    case "ゅ": added_string = "dyu"; i++; break;
                    case "ぇ": added_string = "*"; i++; break;
                    case "ょ": added_string = "dyo"; i++; break;
                    default: added_string = "di"; break;
                } break;
            case "つ":
                switch (character_next) {
                    case "ぁ": added_string = "tsa"; i++; break;
                    case "ぃ": added_string = "tsi"; i++; break;
                    case "ぇ": added_string = "tse"; i++; break;
                    case "ぉ": added_string = "tso"; i++; break;
                    default: added_string = "tsu"; break;
                } break;
            case "づ":
                switch (character_next) {
                    case "ぁ": added_string = "*"; i++; break;
                    case "ゅ": added_string = "*"; i++; break;
                    case "ぃ": added_string = "*"; i++; break;
                    case "ぇ": added_string = "*"; i++; break;
                    case "ぉ": added_string = "*"; i++; break;
                    default: added_string = "du"; break;
                } break;
            case "な": added_string = "na"; break;
            case "ぬ": added_string = "nu"; break;
            case "ね": added_string = "ne"; break;
            case "の": added_string = "no"; break;
            case "ん":
                switch (character_next) {
                    case "あ": added_string = "n'"; break;
                    case "え": added_string = "n'"; break;
                    case "い": added_string = "n'"; break;
                    case "お": added_string = "n'"; break;
                    case "う": added_string = "n'"; break;
                    case "や": added_string = "n'"; break;
                    case "よ": added_string = "n'"; break;
                    case "ゆ": added_string = "n'"; break;
                    default: added_string = "n"; break;
                } break;
            case "に":
                switch (character_next) {
                    case "ゃ": added_string = "nya"; i++; break;
                    case "ゅ": added_string = "nyu"; i++; break;
                    case "ぇ": added_string = "nye"; i++; break;
                    case "ょ": added_string = "nyo"; i++; break;
                    default: added_string = "ni"; break;
                } break;
            case "は": added_string = "ha"; break;
            case "ひ":
                switch (character_next) {
                    case "ゃ": added_string = "hya"; i++; break;
                    case "ゅ": added_string = "hyu"; i++; break;
                    case "ぇ": added_string = "hye"; i++; break;
                    case "ょ": added_string = "hyo"; i++; break;
                    default: added_string = "hi"; break;
                } break;
            case "へ": added_string = "he"; break;
            case "ほ": added_string = "ho"; break;
            case "ば": added_string = "ba"; break;
            case "び":
                switch (character_next) { case "ゃ": added_string = "bya"; i++; break;
                    case "ゅ": added_string = "byu"; i++; break;
                    case "ぇ": added_string = "bye"; i++; break;
                    case "ょ": added_string = "byo"; i++; break;
                    default: added_string = "bi"; break;
                } break;
            case "べ": added_string = "be"; break;
            case "ぼ": added_string = "bo"; break;
            case "ぶ": added_string = "bu"; break;
            case "ぱ": added_string = "pa"; break;
            case "ぴ":
                switch (character_next) {
                    case "ゃ": added_string = "pya"; i++; break;
                    case "ゅ": added_string = "pyu"; i++; break;
                    case "ぇ": added_string = "pye"; i++; break;
                    case "ょ": added_string = "pyo"; i++; break;
                    default: added_string = "pi"; break;
                } break;
            case "ぺ": added_string = "pe"; break;
            case "ぽ": added_string = "po"; break;
            case "ぷ": added_string = "pu"; break;
            case "ふ":
                switch (character_next) {
                    case "ぁ": added_string = "fa"; i++; break;
                    case "ぃ": added_string = "fi"; i++; break;
                    case "ぇ": added_string = "fe"; i++; break;
                    case "ぉ": added_string = "fo"; i++; break;
                    case "ゃ": added_string = "fya"; i++; break;
                    case "ゅ": added_string = "fyu"; i++; break;
                    case "ょ": added_string = "fyo"; i++; break;
                    default: added_string = "fu"; break;
                } break;
            case "ま": added_string = "ma"; break;
            case "み":
                switch (character_next) {
                    case "ゃ": added_string = "mya"; i++; break;
                    case "ゅ": added_string = "myu"; i++; break;
                    case "ぇ": added_string = "mye"; i++; break;
                    case "ょ": added_string = "myo"; i++; break;
                    default: added_string = "mi"; break;
                } break;
            case "む": added_string = "mu"; break;
            case "め": added_string = "me"; break;
            case "も": added_string = "mo"; break;
            case "や": added_string = "ya"; break;
            case "ゆ": added_string = "yu"; break;
            case "よ": added_string = "yo"; break;
            case "ら": added_string = "ra"; break;
            case "り":
                switch (character_next) {
                    case "ゃ": added_string = "rya"; i++; break;
                    case "ゅ": added_string = "ryu"; i++; break;
                    case "ぇ": added_string = "rye"; i++; break;
                    case "ょ": added_string = "ryo"; i++; break;
                    default: added_string = "ri"; break;
                } break;
            case "る": added_string = "ru"; break;
            case "れ": added_string = "re"; break;
            case "ろ": added_string = "ro"; break;
            case "わ": added_string = "wa"; break;
            case "う":
                if (character.equals(character_last)) { added_string = "u_double_vowel";
                } else {
                    switch (character_next) {
                        case "ぃ": added_string = "wi"; i++; break;
                        case "ぇ": added_string = "we"; i++; break;
                        default: added_string = "u"; break;
                    }
                } break;
            case "を": added_string = "wo"; break;
            case "ゔ":
                switch (character_next) {
                    case "ぁ": added_string = "va"; i++; break;
                    case "ぃ": added_string = "vi"; i++; break;
                    case "ぇ": added_string = "ve"; i++; break;
                    case "ぉ": added_string = "vo"; i++; break;
                    default: added_string = "vu"; break;
                } break;
            case "ゐ": added_string = "xwi"; break;
            case "ゑ": added_string = "xwe"; break;
            case "っ": added_string = "small_tsu"; break;

            case "ア":
                if (character.equals(character_last)) { added_string = "a_double_vowel";
                } else { added_string = "a";
                } break;
            case "イ":
                if (character.equals(character_last)) { added_string = "i_double_vowel";
                } else if (character_next.equals("ェ")) { added_string = "ye"; i++;
                } else { added_string = "i";
                } break;
            case "エ":
                if (character.equals(character_last)) { added_string = "e_double_vowel";
                } else { added_string = "e";
                } break;
            case "オ":
                if (character.equals(character_last)) { added_string = "o_double_vowel";
                } else { added_string = "o";
                } break;
            case "カ": added_string = "ka"; break;
            case "キ":
                switch (character_next) {
                    case "ャ": added_string = "kya"; i++; break;
                    case "ュ": added_string = "kyu"; i++; break;
                    case "ェ": added_string = "kye"; i++; break;
                    case "ョ": added_string = "kyo"; i++; break;
                    case "ァ": added_string = "*"; i++; break;
                    case "ィ": added_string = "kyi"; i++; break;
                    case "ォ": added_string = "*"; i++; break;
                    default: added_string = "ki"; break;
                } break;
            case "ク":
                switch (character_next) {
                    case "ャ": added_string = "kwa"; i++; break;
                    case "ュ": added_string = "*"; i++; break;
                    case "ョ": added_string = "*"; i++; break;
                    default: added_string = "ku"; break;
                } break;
            case "ケ": added_string = "ke"; break;
            case "コ": added_string = "ko"; break;
            case "ガ": added_string = "ga"; break;
            case "ギ":
                switch (character_next) {
                    case "ャ": added_string = "gya"; i++; break;
                    case "ュ": added_string = "gyu"; i++; break;
                    case "ェ": added_string = "gye"; i++; break;
                    case "ョ": added_string = "gyo"; i++; break;
                    case "ァ": added_string = "*"; i++; break;
                    case "ィ": added_string = "gyi"; i++; break;
                    case "ォ": added_string = "*"; i++; break;
                    default: added_string = "gi"; break;
                } break;
            case "グ":
                switch (character_next) {
                    case "ャ": added_string = "gwa"; i++; break;
                    case "ュ": added_string = "*"; i++; break;
                    case "ョ": added_string = "*"; i++; break;
                    default: added_string = "gu"; break;
                } break;
            case "ゲ": added_string = "ge"; break;
            case "ゴ": added_string = "go"; break;
            case "サ": added_string = "sa"; break;
            case "ス":
                switch (character_next) {
                    case "ゥ": added_string = "su"; i++; break;
                    default: added_string = "su"; break;
                } break;
            case "セ": added_string = "se"; break;
            case "ソ": added_string = "so"; break;
            case "ザ": added_string = "za"; break;
            case "ズ": added_string = "zu"; break;
            case "ゼ": added_string = "ze"; break;
            case "ゾ": added_string = "zo"; break;
            case "シ":
                switch (character_next) {
                    case "ャ": added_string = "sha"; i++; break;
                    case "ュ": added_string = "shu"; i++; break;
                    case "ェ": added_string = "she"; i++; break;
                    case "ョ": added_string = "sho"; i++; break;
                    case "ァ": added_string = "*"; i++; break;
                    case "ィ": added_string = "*"; i++; break;
                    case "ォ": added_string = "*"; i++; break;
                    default: added_string = "shi"; break;
                } break;
            case "ジ":
                switch (character_next) {
                    case "ャ": added_string = "ja"; i++; break;
                    case "ュ": added_string = "ju"; i++; break;
                    case "ェ": added_string = "je"; i++; break;
                    case "ョ": added_string = "jo"; i++; break;
                    case "ァ": added_string = "*"; i++; break;
                    case "ィ": added_string = "*"; i++; break;
                    case "ォ": added_string = "*"; i++; break;
                    default: added_string = "ji"; break;
                } break;
            case "タ": added_string = "ta"; break;
            case "テ":
                switch (character_next) {
                    case "ィ": added_string = "ti"; i++; break;
                    case "ュ": added_string = "tu"; i++; break;
                    default: added_string = "te"; break;
                } break;
            case "ト":
                switch (character_next) {
                    case "ャ": added_string = "ta"; i++; break;
                    case "ゥ": added_string = "tu"; i++; break;
                    case "ェ": added_string = "te"; i++; break;
                    case "ィ": added_string = "ti"; i++; break;
                    case "ョ": added_string = "to"; i++; break;
                    default: added_string = "to"; break;
                } break;
            case "ダ": added_string = "da"; break;
            case "デ":
                switch (character_next) {
                    case "ャ": added_string = "*"; i++; break;
                    case "ュ": added_string = "du"; i++; break;
                    case "ェ": added_string = "*"; i++; break;
                    case "ョ": added_string = "*"; i++; break;
                    case "ァ": added_string = "*"; i++; break;
                    case "ィ": added_string = "di"; i++; break;
                    case "ォ": added_string = "*"; i++; break;
                    default: added_string = "de"; break;
                } break;
            case "ド": added_string = "do"; break;
            case "チ":
                switch (character_next) {
                    case "ャ": added_string = "cha"; i++; break;
                    case "ュ": added_string = "chu"; i++; break;
                    case "ェ": added_string = "che"; i++; break;
                    case "ョ": added_string = "cho"; i++; break;
                    case "ァ": added_string = "*"; i++; break;
                    case "ィ": added_string = "*"; i++; break;
                    case "ォ": added_string = "*"; i++; break;
                    default: added_string = "chi"; break;
                } break;
            case "ヂ":
                switch (character_next) {
                    case "ャ": added_string = "dja"; i++; break;
                    case "ュ": added_string = "dju"; i++; break;
                    case "ェ": added_string = "dje"; i++; break;
                    case "ョ": added_string = "djo"; i++; break;
                    case "ァ": added_string = "*"; i++; break;
                    case "ィ": added_string = "*"; i++; break;
                    case "ォ": added_string = "*"; i++; break;
                    default: added_string = "dji"; break;
                } break;
            case "ツ":
                switch (character_next) {
                    case "ャ": added_string = "*"; i++; break;
                    case "ュ": added_string = "*"; i++; break;
                    case "ェ": added_string = "tse"; i++; break;
                    case "ョ": added_string = "*"; i++; break;
                    case "ァ": added_string = "tsa"; i++; break;
                    case "ィ": added_string = "tsi"; i++; break;
                    case "ォ": added_string = "tso"; i++; break;
                    default: added_string = "tsu"; break;
                } break;
            case "ヅ":
                switch (character_next) {
                    case "ャ": added_string = "*"; i++; break;
                    case "ュ": added_string = "*"; i++; break;
                    case "ェ": added_string = "dze"; i++; break;
                    case "ョ": added_string = "*"; i++; break;
                    case "ァ": added_string = "dza"; i++; break;
                    case "ィ": added_string = "dzi"; i++; break;
                    case "ォ": added_string = "dzo"; i++; break;
                    default: added_string = "dzu"; break;
                } break;
            case "ナ": added_string = "na"; break;
            case "ヌ": added_string = "nu"; break;
            case "ネ": added_string = "ne"; break;
            case "ノ": added_string = "no"; break;
            case "ン":
                switch (character_next) {
                    case "ア": added_string = "n'"; break;
                    case "エ": added_string = "n'"; break;
                    case "イ": added_string = "n'"; break;
                    case "オ": added_string = "n'"; break;
                    case "ウ": added_string = "n'"; break;
                    case "ヤ": added_string = "n'"; break;
                    case "ヨ": added_string = "n'"; break;
                    case "ュ": added_string = "n'"; break;
                    default: added_string = "n"; break;
                } break;
            case "ニ":
                switch (character_next) {
                    case "ャ": added_string = "nya"; i++; break;
                    case "ュ": added_string = "nyu"; i++; break;
                    case "ェ": added_string = "nye"; i++; break;
                    case "ョ": added_string = "nyo"; i++; break;
                    case "ァ": added_string = "*"; i++; break;
                    case "ィ": added_string = "nyi"; i++; break;
                    case "ォ": added_string = "*"; i++; break;
                    default: added_string = "ni"; break;
                } break;
            case "ハ": added_string = "ha"; break;
            case "ヒ":
                switch (character_next) {
                    case "ャ": added_string = "hya"; i++; break;
                    case "ュ": added_string = "hyu"; i++; break;
                    case "ェ": added_string = "hye"; i++; break;
                    case "ョ": added_string = "hyo"; i++; break;
                    case "ァ": added_string = "*"; i++; break;
                    case "ィ": added_string = "hyi"; i++; break;
                    case "ォ": added_string = "*"; i++; break;
                    default: added_string = "hi"; break;
                } break;
            case "ヘ": added_string = "he"; break;
            case "ホ": added_string = "ho"; break;
            case "バ": added_string = "ba"; break;
            case "ビ":
                switch (character_next) {
                    case "ャ": added_string = "bya"; i++; break;
                    case "ュ": added_string = "byu"; i++; break;
                    case "ェ": added_string = "bye"; i++; break;
                    case "ョ": added_string = "byo"; i++; break;
                    case "ァ": added_string = "*"; i++; break;
                    case "ィ": added_string = "byi"; i++; break;
                    case "ォ": added_string = "*"; i++; break;
                    default: added_string = "bi"; break;
                } break;
            case "ベ": added_string = "be"; break;
            case "ボ": added_string = "bo"; break;
            case "ブ": added_string = "bu"; break;
            case "パ": added_string = "pa"; break;
            case "ピ":
                switch (character_next) {
                    case "ャ": added_string = "pya"; i++; break;
                    case "ュ": added_string = "pyu"; i++; break;
                    case "ェ": added_string = "pye"; i++; break;
                    case "ョ": added_string = "pyo"; i++; break;
                    case "ァ": added_string = "*"; i++; break;
                    case "ィ": added_string = "pyi"; i++; break;
                    case "ォ": added_string = "*"; i++; break;
                    default: added_string = "pi"; break;
                } break;
            case "ペ": added_string = "pe"; break;
            case "ポ": added_string = "po"; break;
            case "プ": added_string = "pu"; break;
            case "フ":
                switch (character_next) {
                    case "ャ": added_string = "fya"; i++; break;
                    case "ュ": added_string = "fyu"; i++; break;
                    case "ェ": added_string = "fe"; i++; break;
                    case "ョ": added_string = "fyo"; i++; break;
                    case "ァ": added_string = "fa"; i++; break;
                    case "ィ": added_string = "fi"; i++; break;
                    case "ォ": added_string = "fo"; i++; break;
                    default: added_string = "fu"; break;
                } break;
            case "マ": added_string = "ma"; break;
            case "ミ":
                switch (character_next) {
                    case "ャ": added_string = "mya"; i++; break;
                    case "ュ": added_string = "myu"; i++; break;
                    case "ェ": added_string = "mye"; i++; break;
                    case "ョ": added_string = "myo"; i++; break;
                    case "ァ": added_string = "*"; i++; break;
                    case "ィ": added_string = "*"; i++; break;
                    case "ォ": added_string = "*"; i++; break;
                    default: added_string = "mi"; break;
                } break;
            case "ム": added_string = "mu"; break;
            case "メ": added_string = "me"; break;
            case "モ": added_string = "mo"; break;
            case "ヤ": added_string = "ya"; break;
            case "ユ": added_string = "yu"; break;
            case "ヨ": added_string = "yo"; break;
            case "ラ": added_string = "ra"; break;
            case "リ":
                switch (character_next) {
                    case "ャ": added_string = "rya"; i++; break;
                    case "ュ": added_string = "ryu"; i++; break;
                    case "ェ": added_string = "rye"; i++; break;
                    case "ョ": added_string = "ryo"; i++; break;
                    case "ァ": added_string = "*"; i++; break;
                    case "ィ": added_string = "*"; i++; break;
                    case "ォ": added_string = "*"; i++; break;
                    default: added_string = "ri"; break;
                } break;
            case "ル": added_string = "ru"; break;
            case "レ": added_string = "re"; break;
            case "ロ": added_string = "ro"; break;
            case "ワ": added_string = "wa"; break;
            case "ウ":
                if (character.equals(character_last)) { added_string = "u_double_vowel";
                } else {
                    switch (character_next) {
                        case "ャ": added_string = "*"; i++; break;
                        case "ュ": added_string = "*"; i++; break;
                        case "ェ": added_string = "we"; i++; break;
                        case "ョ": added_string = "*"; i++; break;
                        case "ァ": added_string = "*"; i++; break;
                        case "ィ": added_string = "wi"; i++; break;
                        case "ォ": added_string = "vo"; i++; break;
                        default: added_string = "u"; break;
                    }
                } break;
            case "ヲ": added_string = "wo"; break;
            case "ヴ":
                switch (character_next) {
                    case "ァ": added_string = "va"; i++; break;
                    case "ィ": added_string = "vi"; i++; break;
                    case "ェ": added_string = "ve"; i++; break;
                    case "ォ": added_string = "vo"; i++; break;
                    default: added_string = "vu"; break;
                } break;
            case "ヷ": added_string = "va"; break;
            case "ヸ": added_string = "vi"; break;
            case "ヹ": added_string = "ve"; break;
            case "ヺ": added_string = "vo"; break;
            case "ヰ": added_string = "xwi"; break;
            case "ヱ": added_string = "xwe"; break;
            case "ッ": added_string = "small_tsu"; break;

            case "ー": added_string = "katakana_repeat_bar"; break;

            case "a":
                if (character.equals(character_last)) {
                    added_string = "a_double_vowel";
                } else {
                    added_string = "a";
                } break;
            case "b":
                switch (character_next) {
                    case "a": added_string = "ba"; i++; break;
                    case "e": added_string = "be"; i++; break;
                    case "i": added_string = "bi"; i++; break;
                    case "o": added_string = "bo"; i++; break;
                    case "u": added_string = "bu"; i++; break;
                    case "y":
                        switch (character_next2) {
                            case "a": added_string = "bya"; i++; i++; break;
                            case "e": added_string = "bye"; i++; i++; break;
                            case "i": added_string = "byi"; i++; i++; break;
                            case "o": added_string = "byo"; i++; i++; break;
                            case "u": added_string = "byu"; i++; i++; break;
                            default: added_string = "*"; break;
                        } break;
                    case "b": added_string = "small_tsu"; break;
                    default: added_string = "*"; break;
                } break;
            case "c":
                switch (character_next) {
                    case "h":
                        switch (character_next2) {
                            case "a": added_string = "cha"; i++; i++; break;
                            case "e": added_string = "che"; i++; i++; break;
                            case "i": added_string = "chi"; i++; i++; break;
                            case "o": added_string = "cho"; i++; i++; break;
                            case "u": added_string = "chu"; i++; i++; break;
                            default: added_string = "*"; break;
                        } break;
                    case "c": added_string = "small_tsu"; break;
                    default: added_string = "*"; break;
                } break;
            case "d":
                switch (character_next) {
                    case "a": added_string = "da"; i++; break;
                    case "e": added_string = "de"; i++; break;
                    case "i": added_string = "di"; i++; break;
                    case "o": added_string = "do"; i++; break;
                    case "u": added_string = "du"; i++; break;
                    case "d": added_string = "small_tsu"; break;
                    default: added_string = "*"; break;
                } break;
            case "e":
                if (character.equals(character_last)) {
                    added_string = "e_double_vowel";
                } else {
                    added_string = "e";
                } break;
            case "f":
                switch (character_next) {
                    case "a": added_string = "fa"; i++; break;
                    case "e": added_string = "fe"; i++; break;
                    case "i": added_string = "fi"; i++; break;
                    case "o": added_string = "fo"; i++; break;
                    case "u": added_string = "fu"; i++; break;
                    case "y":
                        switch (character_next2) {
                            case "a": added_string = "fya"; i++; i++; break;
                            case "e": added_string = "fye"; i++; i++; break;
                            case "i": added_string = "fyi"; i++; i++; break;
                            case "o": added_string = "fyo"; i++; i++; break;
                            case "u": added_string = "fyu"; i++; i++; break;
                            default: added_string = "*"; break;
                        } break;
                    case "f": added_string = "small_tsu"; break;
                    default: added_string = "*"; break;
                } break;
            case "g":
                switch (character_next) {
                    case "a": added_string = "ga"; i++; break;
                    case "e": added_string = "ge"; i++; break;
                    case "i": added_string = "gi"; i++; break;
                    case "o": added_string = "go"; i++; break;
                    case "u": added_string = "gu"; i++; break;
                    case "y":
                        switch (character_next2) {
                            case "a": added_string = "gya"; i++; i++; break;
                            case "e": added_string = "gye"; i++; i++; break;
                            case "i": added_string = "gyi"; i++; i++; break;
                            case "o": added_string = "gyo"; i++; i++; break;
                            case "u": added_string = "gyu"; i++; i++; break;
                            default: added_string = "*"; break;
                        } break;
                    case "g": added_string = "small_tsu"; break;
                    default: added_string = "*"; break;
                } break;
            case "h":
                switch (character_next) {
                    case "a": added_string = "ha"; i++; break;
                    case "e": added_string = "he"; i++; break;
                    case "i": added_string = "hi"; i++; break;
                    case "o": added_string = "ho"; i++; break;
                    case "u": added_string = "hu"; i++; break;
                    case "y":
                        switch (character_next2) {
                            case "a": added_string = "hya"; i++; i++; break;
                            case "e": added_string = "hye"; i++; i++; break;
                            case "i": added_string = "hyi"; i++; i++; break;
                            case "o": added_string = "hyo"; i++; i++; break;
                            case "u": added_string = "hyu"; i++; i++; break;
                            default: added_string = "*"; break;
                        } break;
                    default: added_string = "*"; break;
                } break;
            case "i":
                if (character.equals(character_last)) {
                    added_string = "i_double_vowel";
                } else {
                    added_string = "i";
                } break;
            case "j":
                switch (character_next) {
                    case "a": added_string = "ja"; i++; break;
                    case "e": added_string = "je"; i++; break;
                    case "i": added_string = "ji"; i++; break;
                    case "o": added_string = "jo"; i++; break;
                    case "u": added_string = "ju"; i++; break;
                    case "y":
                        switch (character_next2) {
                            case "a": added_string = "jya"; i++; i++; break;
                            case "e": added_string = "jye"; i++; i++; break;
                            case "i": added_string = "jyi"; i++; i++; break;
                            case "o": added_string = "jyo"; i++; i++; break;
                            case "u": added_string = "jyu"; i++; i++; break;
                            default: added_string = "*"; break;
                        } break;
                    case "j": added_string = "small_tsu"; break;
                    default: added_string = "*"; break;
                } break;
            case "k":
                switch (character_next) {
                    case "a": added_string = "ka"; i++; break;
                    case "e": added_string = "ke"; i++; break;
                    case "i": added_string = "ki"; i++; break;
                    case "o": added_string = "ko"; i++; break;
                    case "u": added_string = "ku"; i++; break;
                    case "y":
                        switch (character_next2) {
                            case "a": added_string = "kya"; i++; i++; break;
                            case "e": added_string = "kye"; i++; i++; break;
                            case "i": added_string = "kyi"; i++; i++; break;
                            case "o": added_string = "kyo"; i++; i++; break;
                            case "u": added_string = "kyu"; i++; i++; break;
                            default: added_string = "*"; break;
                        } break;
                    case "k": added_string = "small_tsu"; break;
                    default: added_string = "*"; break;
                } break;
            case "l": added_string = "*"; break;
            case "m":
                switch (character_next) {
                    case "a": added_string = "ma"; i++; break;
                    case "e": added_string = "me"; i++; break;
                    case "i": added_string = "mi"; i++; break;
                    case "o": added_string = "mo"; i++; break;
                    case "u": added_string = "mu"; i++; break;
                    case "y":
                        switch (character_next2) {
                            case "a": added_string = "mya"; i++; i++; break;
                            case "e": added_string = "mye"; i++; i++; break;
                            case "i": added_string = "myi"; i++; i++; break;
                            case "o": added_string = "myo"; i++; i++; break;
                            case "u": added_string = "myu"; i++; i++; break;
                            default: added_string = "*"; break;
                        } break;
                    case "m": added_string = "small_tsu"; break;
                    default: added_string = "*"; break;
                } break;
            case "n":
                switch (character_next) {
                    case "'": added_string = "n'"; i++; break;
                    case "a": added_string = "na"; i++; break;
                    case "e": added_string = "ne"; i++; break;
                    case "i": added_string = "ni"; i++; break;
                    case "o": added_string = "no"; i++; break;
                    case "u": added_string = "nu"; i++; break;
                    case "y":
                        switch (character_next2) {
                            case "a": added_string = "nya"; i++; i++; break;
                            case "e": added_string = "nye"; i++; i++; break;
                            case "i": added_string = "nyi"; i++; i++; break;
                            case "o": added_string = "nyo"; i++; i++; break;
                            case "u": added_string = "nyu"; i++; i++; break;
                            default: added_string = "*"; i++; break;
                        } break;
                    default: added_string = "n"; break;
                } break;
            case "o":
                if (character.equals(character_last)) {
                    added_string = "o_double_vowel";
                } else {
                    added_string = "o";
                } break;
            case "p":
                switch (character_next) {
                    case "a": added_string = "pa"; i++; break;
                    case "e": added_string = "pe"; i++; break;
                    case "i": added_string = "pi"; i++; break;
                    case "o": added_string = "po"; i++; break;
                    case "u": added_string = "pu"; i++; break;
                    case "y":
                        switch (character_next2) {
                            case "a": added_string = "pya"; i++; i++; break;
                            case "e": added_string = "pye"; i++; i++; break;
                            case "i": added_string = "pyi"; i++; i++; break;
                            case "o": added_string = "pyo"; i++; i++; break;
                            case "u": added_string = "pyu"; i++; i++; break;
                            default: added_string = "*"; break;
                        } break;
                    case "p": added_string = "small_tsu"; break;
                    default: added_string = "*"; break;
                } break;
            case "q": added_string = "*"; break;
            case "r":
                switch (character_next) {
                    case "a": added_string = "ra"; i++; break;
                    case "e": added_string = "re"; i++; break;
                    case "i": added_string = "ri"; i++; break;
                    case "o": added_string = "ro"; i++; break;
                    case "u": added_string = "ru"; i++; break;
                    case "y":
                        switch (character_next2) {
                            case "a": added_string = "rya"; i++; i++; break;
                            case "e": added_string = "rye"; i++; i++; break;
                            case "i": added_string = "ryi"; i++; i++; break;
                            case "o": added_string = "ryo"; i++; i++; break;
                            case "u": added_string = "ryu"; i++; i++; break;
                            default: added_string = "*"; break;
                        } break;
                    case "r": added_string = "small_tsu"; break;
                    default: added_string = "*"; break;
                } break;
            case "s":
                switch (character_next) {
                    case "a": added_string = "sa"; i++; break;
                    case "e": added_string = "se"; i++; break;
                    case "o": added_string = "so"; i++; break;
                    case "u": added_string = "su"; i++; break;
                    case "h":
                        switch (character_next2) {
                            case "i": added_string = "shi"; i++; i++; break;
                            case "a": added_string = "sha"; i++; i++; break;
                            case "o": added_string = "sho"; i++; i++; break;
                            case "u": added_string = "shu"; i++; i++; break;
                            case "e": added_string = "she"; i++; i++; break;
                            default: added_string = "*"; break;
                        } break;
                    case "s": added_string = "small_tsu"; break;
                    default: added_string = "*"; break;
                } break;
            case "t":
                switch (character_next) {
                    case "a": added_string = "ta"; i++; break;
                    case "e": added_string = "te"; i++; break;
                    case "i": added_string = "ti"; i++; break;
                    case "o": added_string = "to"; i++; break;
                    case "u": added_string = "tu"; i++; break;
                    case "s":
                        switch (character_next2) {
                            case "a": added_string = "tsa"; i++; i++; break;
                            case "i": added_string = "tsi"; i++; i++; break;
                            case "u": added_string = "tsu"; i++; i++; break;
                            case "e": added_string = "tse"; i++; i++; break;
                            case "o": added_string = "tso"; i++; i++; break;
                            default: added_string = "*"; break;
                        } break;
                    case "t": added_string = "small_tsu"; break;
                    default: added_string = "*"; break;
                } break;
            case "u":
                if (character.equals(character_last)) {
                    added_string = "u_double_vowel";
                } else {
                    added_string = "u";
                } break;
            case "v":
                switch (character_next) {
                    case "a": added_string = "va"; i++; break;
                    case "e": added_string = "ve"; i++; break;
                    case "i": added_string = "vi"; i++; break;
                    case "o": added_string = "vo"; i++; break;
                    case "u": added_string = "vu"; i++; break;
                    case "v": added_string = "small_tsu"; break;
                    default: added_string = "*"; break;
                } break;
            case "w":
                switch (character_next) {
                    case "a": added_string = "wa"; i++; break;
                    case "e": added_string = "we"; i++; break;
                    case "i": added_string = "wi"; i++; break;
                    case "o": added_string = "wo"; i++; break;
                    case "u": added_string = "wu"; i++; break;
                    case "w": added_string = "small_tsu"; break;
                    default: added_string = "*"; break;
                } break;
            case "x": added_string = "*"; break;
            case "y":
                switch (character_next) {
                    case "a": added_string = "ya"; i++; break;
                    case "e": added_string = "ye"; i++; break;
                    case "o": added_string = "yo"; i++; break;
                    case "u": added_string = "yu"; i++; break;
                    default: added_string = "*"; break;
                } break;
            case "z":
                switch (character_next) {
                    case "a": added_string = "za"; i++; break;
                    case "e": added_string = "ze"; i++; break;
                    case "o": added_string = "zo"; i++; break;
                    case "u": added_string = "zu"; i++; break;
                    case "z": added_string = "small_tsu"; break;
                    default: added_string = "*"; break;
                } break;

            default: added_string = "original"; break;
        }

        List<String> output = new ArrayList<>();
        output.add(Integer.toString(i));
        output.add(added_string);
        return output;
    }
    private static List<String> getCharBasedOnPhoneme(int i, String added_string, String character, String character_next, String added_string_last) {

        String added_string_latin = "";
        String added_string_hiragana = "";
        String added_string_katakana = "";

        switch (added_string) {
            case "a": added_string_latin = "a"; added_string_hiragana = "あ"; added_string_katakana = "ア"; break;
            case "ba": added_string_latin = "ba"; added_string_hiragana = "ば"; added_string_katakana = "バ"; break;
            case "bi": added_string_latin = "bi"; added_string_hiragana = "び"; added_string_katakana = "ビ"; break;
            case "bu": added_string_latin = "bu"; added_string_hiragana = "ぶ"; added_string_katakana = "ブ"; break;
            case "be": added_string_latin = "be"; added_string_hiragana = "べ"; added_string_katakana = "ベ"; break;
            case "bo": added_string_latin = "bo"; added_string_hiragana = "ぼ"; added_string_katakana = "ボ"; break;
            case "bya": added_string_latin = "bya"; added_string_hiragana = "びゃ"; added_string_katakana = "ビャ"; break;
            case "byu": added_string_latin = "byu"; added_string_hiragana = "びゅ"; added_string_katakana = "ビュ"; break;
            case "byi": added_string_latin = "byi"; added_string_hiragana = "びぃ"; added_string_katakana = "ビィ"; break;
            case "bye": added_string_latin = "bye"; added_string_hiragana = "びぇ"; added_string_katakana = "ビェ"; break;
            case "byo": added_string_latin = "byo"; added_string_hiragana = "びょ"; added_string_katakana = "ビョ"; break;
            case "cha": added_string_latin = "cha"; added_string_hiragana = "ちゃ"; added_string_katakana = "チャ"; break;
            case "chi": added_string_latin = "chi"; added_string_hiragana = "ち"; added_string_katakana = "チ"; break;
            case "chu": added_string_latin = "chu"; added_string_hiragana = "ちゅ"; added_string_katakana = "チュ"; break;
            case "che": added_string_latin = "che"; added_string_hiragana = "ちぇ"; added_string_katakana = "チェ"; break;
            case "cho": added_string_latin = "cho"; added_string_hiragana = "ちょ"; added_string_katakana = "チョ"; break;
            case "da": added_string_latin = "da"; added_string_hiragana = "だ"; added_string_katakana = "ダ"; break;
            case "di": added_string_latin = "di"; added_string_hiragana = "ぢ"; added_string_katakana = "ヂ"; break;
            case "du": added_string_latin = "du"; added_string_hiragana = "づ"; added_string_katakana = "ヅ"; break;
            case "de": added_string_latin = "de"; added_string_hiragana = "で"; added_string_katakana = "デ"; break;
            case "do": added_string_latin = "do"; added_string_hiragana = "ど"; added_string_katakana = "ド"; break;
            case "dja": added_string_latin = "dja"; added_string_hiragana = "＊"; added_string_katakana = "ヂャ"; break;
            case "dji": added_string_latin = "dji"; added_string_hiragana = "ぢ"; added_string_katakana = "ヂ"; break;
            case "dju": added_string_latin = "dju"; added_string_hiragana = "＊"; added_string_katakana = "ヂュ"; break;
            case "dje": added_string_latin = "dje"; added_string_hiragana = "＊"; added_string_katakana = "ヂェ"; break;
            case "djo": added_string_latin = "djo"; added_string_hiragana = "＊"; added_string_katakana = "ヂョ"; break;
            case "dya": added_string_latin = "dya"; added_string_hiragana = "ぢゃ"; added_string_katakana = "ヂァ"; break;
            case "dyi": added_string_latin = "dyi"; added_string_hiragana = "＊"; added_string_katakana = "ヂィ"; break;
            case "dyu": added_string_latin = "dyu"; added_string_hiragana = "ぢゅ"; added_string_katakana = "ヂュ"; break;
            case "dye": added_string_latin = "dye"; added_string_hiragana = "＊"; added_string_katakana = "ヂェ"; break;
            case "dyo": added_string_latin = "dyo"; added_string_hiragana = "ぢょ"; added_string_katakana = "ヂォ"; break;
            case "dza": added_string_latin = "dza"; added_string_hiragana = "＊"; added_string_katakana = "ヅァ"; break;
            case "dzi": added_string_latin = "dzi"; added_string_hiragana = "＊"; added_string_katakana = "ヅィ"; break;
            case "dzu": added_string_latin = "dzu"; added_string_hiragana = "＊"; added_string_katakana = "ヅ"; break;
            case "dze": added_string_latin = "dze"; added_string_hiragana = "＊"; added_string_katakana = "ヅェ"; break;
            case "dzo": added_string_latin = "dzo"; added_string_hiragana = "＊"; added_string_katakana = "ヅォ"; break;
            case "e": added_string_latin = "e"; added_string_hiragana = "え"; added_string_katakana = "エ"; break;
            case "fa": added_string_latin = "fa"; added_string_hiragana = "ふぁ"; added_string_katakana = "ファ"; break;
            case "fi": added_string_latin = "fi"; added_string_hiragana = "ふぃ"; added_string_katakana = "フィ"; break;
            case "fu": added_string_latin = "fu"; added_string_hiragana = "ふ"; added_string_katakana = "フ"; break;
            case "fe": added_string_latin = "fe"; added_string_hiragana = "ふぇ"; added_string_katakana = "フェ"; break;
            case "fo": added_string_latin = "fo"; added_string_hiragana = "ふぉ"; added_string_katakana = "フォ"; break;
            case "fya": added_string_latin = "fya"; added_string_hiragana = "ふゃ"; added_string_katakana = "フャ"; break;
            case "fye": added_string_latin = "fye"; added_string_hiragana = "ふぇ"; added_string_katakana = "フェ"; break;
            case "fyi": added_string_latin = "fyi"; added_string_hiragana = "ふぃ"; added_string_katakana = "フィ"; break;
            case "fyu": added_string_latin = "fyu"; added_string_hiragana = "ふゅ"; added_string_katakana = "フュ"; break;
            case "fyo": added_string_latin = "fyo"; added_string_hiragana = "ふょ"; added_string_katakana = "フョ"; break;
            case "ga": added_string_latin = "ga"; added_string_hiragana = "が"; added_string_katakana = "ガ"; break;
            case "gi": added_string_latin = "gi"; added_string_hiragana = "ぎ"; added_string_katakana = "ギ"; break;
            case "gu": added_string_latin = "gu"; added_string_hiragana = "ぐ"; added_string_katakana = "グ"; break;
            case "ge": added_string_latin = "ge"; added_string_hiragana = "げ"; added_string_katakana = "ゲ"; break;
            case "go": added_string_latin = "go"; added_string_hiragana = "ご"; added_string_katakana = "ゴ"; break;
            case "gwa": added_string_latin = "gwa"; added_string_hiragana = "ぐゎ"; added_string_katakana = "グァ"; break;
            case "gya": added_string_latin = "gya"; added_string_hiragana = "ぎゃ"; added_string_katakana = "ギャ"; break;
            case "gye": added_string_latin = "gye"; added_string_hiragana = "ぎぇ"; added_string_katakana = "ギェ"; break;
            case "gyi": added_string_latin = "gyi"; added_string_hiragana = "ぎぃ"; added_string_katakana = "ギィ"; break;
            case "gyu": added_string_latin = "gyu"; added_string_hiragana = "ぎゅ"; added_string_katakana = "ギュ"; break;
            case "gyo": added_string_latin = "gyo"; added_string_hiragana = "ぎょ"; added_string_katakana = "ギョ"; break;
            case "ha": added_string_latin = "ha"; added_string_hiragana = "は"; added_string_katakana = "ハ"; break;
            case "hi": added_string_latin = "hi"; added_string_hiragana = "ひ"; added_string_katakana = "ヒ"; break;
            case "hu": added_string_latin = "hu"; added_string_hiragana = "ふ"; added_string_katakana = "フ"; break;
            case "he": added_string_latin = "he"; added_string_hiragana = "へ"; added_string_katakana = "ヘ"; break;
            case "ho": added_string_latin = "ho"; added_string_hiragana = "ほ"; added_string_katakana = "ホ"; break;
            case "hya": added_string_latin = "hya"; added_string_hiragana = "ひゃ"; added_string_katakana = "ヒャ"; break;
            case "hyi": added_string_latin = "hyi"; added_string_hiragana = "ひぃ"; added_string_katakana = "ヒィ"; break;
            case "hyu": added_string_latin = "hyu"; added_string_hiragana = "ひゅ"; added_string_katakana = "ヒュ"; break;
            case "hye": added_string_latin = "hye"; added_string_hiragana = "ひぇ"; added_string_katakana = "ヒェ"; break;
            case "hyo": added_string_latin = "hyo"; added_string_hiragana = "ひょ"; added_string_katakana = "ヒョ"; break;
            case "i": added_string_latin = "i"; added_string_hiragana = "い"; added_string_katakana = "イ"; break;
            case "ja": added_string_latin = "ja"; added_string_hiragana = "じゃ"; added_string_katakana = "ジャ"; break;
            case "ji": added_string_latin = "ji"; added_string_hiragana = "じ"; added_string_katakana = "ジ"; break;
            case "ju": added_string_latin = "ju"; added_string_hiragana = "じゅ"; added_string_katakana = "ジュ"; break;
            case "je": added_string_latin = "je"; added_string_hiragana = "じぇ"; added_string_katakana = "ジェ"; break;
            case "jo": added_string_latin = "jo"; added_string_hiragana = "じょ"; added_string_katakana = "ジョ"; break;
            case "jya": added_string_latin = "jya"; added_string_hiragana = "じゃ"; added_string_katakana = "ジャ"; break;
            case "jye": added_string_latin = "jye"; added_string_hiragana = "じぇ"; added_string_katakana = "ジェ"; break;
            case "jyi": added_string_latin = "jyi"; added_string_hiragana = "じぃ"; added_string_katakana = "ジィ"; break;
            case "jyu": added_string_latin = "jyu"; added_string_hiragana = "じゅ"; added_string_katakana = "ジュ"; break;
            case "jyo": added_string_latin = "jyo"; added_string_hiragana = "じょ"; added_string_katakana = "ジョ"; break;
            case "ka": added_string_latin = "ka"; added_string_hiragana = "か"; added_string_katakana = "カ"; break;
            case "ki": added_string_latin = "ki"; added_string_hiragana = "き"; added_string_katakana = "キ"; break;
            case "ku": added_string_latin = "ku"; added_string_hiragana = "く"; added_string_katakana = "ク"; break;
            case "ke": added_string_latin = "ke"; added_string_hiragana = "け"; added_string_katakana = "ケ"; break;
            case "ko": added_string_latin = "ko"; added_string_hiragana = "こ"; added_string_katakana = "コ"; break;
            case "kwa": added_string_latin = "kwa"; added_string_hiragana = "くゎ"; added_string_katakana = "クァ"; break;
            case "kya": added_string_latin = "kya"; added_string_hiragana = "きゃ"; added_string_katakana = "キャ"; break;
            case "kye": added_string_latin = "kye"; added_string_hiragana = "きぇ"; added_string_katakana = "キェ"; break;
            case "kyi": added_string_latin = "kyi"; added_string_hiragana = "きぃ"; added_string_katakana = "キィ"; break;
            case "kyu": added_string_latin = "kyu"; added_string_hiragana = "きゅ"; added_string_katakana = "キュ"; break;
            case "kyo": added_string_latin = "kyo"; added_string_hiragana = "きょ"; added_string_katakana = "キョ"; break;
            case "ma": added_string_latin = "ma"; added_string_hiragana = "ま"; added_string_katakana = "マ"; break;
            case "mi": added_string_latin = "mi"; added_string_hiragana = "み"; added_string_katakana = "ミ"; break;
            case "mu": added_string_latin = "mu"; added_string_hiragana = "む"; added_string_katakana = "ム"; break;
            case "me": added_string_latin = "me"; added_string_hiragana = "め"; added_string_katakana = "メ"; break;
            case "mo": added_string_latin = "mo"; added_string_hiragana = "も"; added_string_katakana = "モ"; break;
            case "mya": added_string_latin = "mya"; added_string_hiragana = "みゃ"; added_string_katakana = "ミャ"; break;
            case "myu": added_string_latin = "myu"; added_string_hiragana = "みゅ"; added_string_katakana = "ミュ"; break;
            case "myi": added_string_latin = "myi"; added_string_hiragana = "みぃ"; added_string_katakana = "ミィ"; break;
            case "mye": added_string_latin = "mye"; added_string_hiragana = "みぇ"; added_string_katakana = "ミェ"; break;
            case "myo": added_string_latin = "myo"; added_string_hiragana = "みょ"; added_string_katakana = "ミョ"; break;
            case "n": added_string_latin = "n"; added_string_hiragana = "ん"; added_string_katakana = "ン"; break;
            case "n'": added_string_latin = "n'"; added_string_hiragana = "ん"; added_string_katakana = "ン"; break;
            case "na": added_string_latin = "na"; added_string_hiragana = "な"; added_string_katakana = "ナ"; break;
            case "ni": added_string_latin = "ni"; added_string_hiragana = "に"; added_string_katakana = "ニ"; break;
            case "nu": added_string_latin = "nu"; added_string_hiragana = "ぬ"; added_string_katakana = "ヌ"; break;
            case "ne": added_string_latin = "ne"; added_string_hiragana = "ね"; added_string_katakana = "ネ"; break;
            case "no": added_string_latin = "no"; added_string_hiragana = "の"; added_string_katakana = "ノ"; break;
            case "nya": added_string_latin = "nya"; added_string_hiragana = "にゃ"; added_string_katakana = "ニャ"; break;
            case "nyu": added_string_latin = "nyu"; added_string_hiragana = "にゅ"; added_string_katakana = "ニュ"; break;
            case "nye": added_string_latin = "nye"; added_string_hiragana = "にぇ"; added_string_katakana = "ニェ"; break;
            case "nyi": added_string_latin = "nyi"; added_string_hiragana = "にぃ"; added_string_katakana = "ニィ"; break;
            case "nyo": added_string_latin = "nyo"; added_string_hiragana = "にょ"; added_string_katakana = "ニョ"; break;
            case "o": added_string_latin = "o"; added_string_hiragana = "お"; added_string_katakana = "オ"; break;
            case "pa": added_string_latin = "pa"; added_string_hiragana = "ぱ"; added_string_katakana = "パ"; break;
            case "pi": added_string_latin = "pi"; added_string_hiragana = "ぴ"; added_string_katakana = "ビ"; break;
            case "pu": added_string_latin = "pu"; added_string_hiragana = "ぷ"; added_string_katakana = "プ"; break;
            case "pe": added_string_latin = "pe"; added_string_hiragana = "ぺ"; added_string_katakana = "ペ"; break;
            case "po": added_string_latin = "po"; added_string_hiragana = "ぽ"; added_string_katakana = "ポ"; break;
            case "pya": added_string_latin = "pya"; added_string_hiragana = "ぴゃ"; added_string_katakana = "ピャ"; break;
            case "pyu": added_string_latin = "pyu"; added_string_hiragana = "ぴゅ"; added_string_katakana = "ピュ"; break;
            case "pyi": added_string_latin = "pyi"; added_string_hiragana = "ぴぃ"; added_string_katakana = "ピィ"; break;
            case "pye": added_string_latin = "pye"; added_string_hiragana = "ぴぇ"; added_string_katakana = "ピェ"; break;
            case "pyo": added_string_latin = "pyo"; added_string_hiragana = "ぴょ"; added_string_katakana = "ピョ"; break;
            case "ra": added_string_latin = "ra"; added_string_hiragana = "ら"; added_string_katakana = "ラ"; break;
            case "ri": added_string_latin = "ri"; added_string_hiragana = "り"; added_string_katakana = "リ"; break;
            case "ru": added_string_latin = "ru"; added_string_hiragana = "る"; added_string_katakana = "ル"; break;
            case "re": added_string_latin = "re"; added_string_hiragana = "れ"; added_string_katakana = "レ"; break;
            case "ro": added_string_latin = "ro"; added_string_hiragana = "ろ"; added_string_katakana = "ロ"; break;
            case "rya": added_string_latin = "rya"; added_string_hiragana = "りゃ"; added_string_katakana = "リャ"; break;
            case "ryu": added_string_latin = "ryu"; added_string_hiragana = "りゅ"; added_string_katakana = "リュ"; break;
            case "ryi": added_string_latin = "ryi"; added_string_hiragana = "りぃ"; added_string_katakana = "リィ"; break;
            case "rye": added_string_latin = "rye"; added_string_hiragana = "りぇ"; added_string_katakana = "リェ"; break;
            case "ryo": added_string_latin = "ryo"; added_string_hiragana = "りょ"; added_string_katakana = "リョ"; break;
            case "sa": added_string_latin = "sa"; added_string_hiragana = "さ"; added_string_katakana = "サ"; break;
            case "si": added_string_latin = "si"; added_string_hiragana = "＊"; added_string_katakana = "＊"; break;
            case "su": added_string_latin = "su"; added_string_hiragana = "す"; added_string_katakana = "ス"; break;
            case "se": added_string_latin = "se"; added_string_hiragana = "せ"; added_string_katakana = "セ"; break;
            case "so": added_string_latin = "so"; added_string_hiragana = "そ"; added_string_katakana = "ソ"; break;
            case "sha": added_string_latin = "sha"; added_string_hiragana = "しゃ"; added_string_katakana = "シャ"; break;
            case "shi": added_string_latin = "shi"; added_string_hiragana = "し"; added_string_katakana = "シ"; break;
            case "shu": added_string_latin = "shu"; added_string_hiragana = "しゅ"; added_string_katakana = "シュ"; break;
            case "she": added_string_latin = "she"; added_string_hiragana = "しぇ"; added_string_katakana = "シェ"; break;
            case "sho": added_string_latin = "sho"; added_string_hiragana = "しょ"; added_string_katakana = "ショ"; break;
            case "sya": added_string_latin = "sya"; added_string_hiragana = "＊"; added_string_katakana = "＊"; break;
            case "syu": added_string_latin = "syu"; added_string_hiragana = "＊"; added_string_katakana = "＊"; break;
            case "syo": added_string_latin = "syo"; added_string_hiragana = "＊"; added_string_katakana = "＊"; break;
            case "ta": added_string_latin = "ta"; added_string_hiragana = "た"; added_string_katakana = "タ"; break;
            case "ti": added_string_latin = "ti"; added_string_hiragana = "＊"; added_string_katakana = "ティ"; break;
            case "tu": added_string_latin = "tu"; added_string_hiragana = "＊"; added_string_katakana = "テュ"; break;
            case "te": added_string_latin = "te"; added_string_hiragana = "て"; added_string_katakana = "テ"; break;
            case "to": added_string_latin = "to"; added_string_hiragana = "と"; added_string_katakana = "ト"; break;
            case "tsu": added_string_latin = "tsu"; added_string_hiragana = "つ"; added_string_katakana = "ツ"; break;
            case "u": added_string_latin = "u"; added_string_hiragana = "う"; added_string_katakana = "ウ"; break;
            case "va": added_string_latin = "va"; added_string_hiragana = "ヴぁ"; added_string_katakana = "ヴァ"; break;
            case "vi": added_string_latin = "vi"; added_string_hiragana = "ヴぃ"; added_string_katakana = "ヴィ"; break;
            case "vu": added_string_latin = "vu"; added_string_hiragana = "ヴ"; added_string_katakana = "ヴ"; break;
            case "ve": added_string_latin = "ve"; added_string_hiragana = "ヴぇ"; added_string_katakana = "ヴェ"; break;
            case "vo": added_string_latin = "vo"; added_string_hiragana = "ヴぉ"; added_string_katakana = "ヴォ"; break;
            case "wa": added_string_latin = "wa"; added_string_hiragana = "わ"; added_string_katakana = "ワ"; break;
            case "wi": added_string_latin = "wi"; added_string_hiragana = "うぃ"; added_string_katakana = "ウィ"; break;
            case "wu": added_string_latin = "wu"; added_string_hiragana = "う"; added_string_katakana = "ウ"; break;
            case "we": added_string_latin = "we"; added_string_hiragana = "うぇ"; added_string_katakana = "ウェ"; break;
            case "wo": added_string_latin = "wo"; added_string_hiragana = "を"; added_string_katakana = "ヲ"; break;
            case "ya": added_string_latin = "ya"; added_string_hiragana = "や"; added_string_katakana = "ヤ"; break;
            case "yu": added_string_latin = "yu"; added_string_hiragana = "ゆ"; added_string_katakana = "ユ"; break;
            case "ye": added_string_latin = "ye"; added_string_hiragana = "いぇ"; added_string_katakana = "イェ"; break;
            case "yo": added_string_latin = "yo"; added_string_hiragana = "よ"; added_string_katakana = "ヨ"; break;
            case "za": added_string_latin = "za"; added_string_hiragana = "ざ"; added_string_katakana = "ザ"; break;
            case "zu": added_string_latin = "zu"; added_string_hiragana = "ず"; added_string_katakana = "ズ"; break;
            case "ze": added_string_latin = "ze"; added_string_hiragana = "ぜ"; added_string_katakana = "ゼ"; break;
            case "zo": added_string_latin = "zo"; added_string_hiragana = "ぞ"; added_string_katakana = "ゾ"; break;
            case "xwi": added_string_latin = "wi"; added_string_hiragana = "ゐ"; added_string_katakana = "ヰ"; break;
            case "xwe": added_string_latin = "we"; added_string_hiragana = "ゑ"; added_string_katakana = "ヱ"; break;
            case "*": added_string_latin = "*"; added_string_hiragana = "＊"; added_string_katakana = "＊"; break;
            case "a_double_vowel": added_string_latin = "a"; added_string_hiragana = "あ"; added_string_katakana = "ー"; break;
            case "e_double_vowel": added_string_latin = "e"; added_string_hiragana = "え"; added_string_katakana = "ー"; break;
            case "i_double_vowel": added_string_latin = "i"; added_string_hiragana = "い"; added_string_katakana = "ー"; break;
            case "o_double_vowel": added_string_latin = "o"; added_string_hiragana = "お"; added_string_katakana = "ー"; break;
            case "u_double_vowel": added_string_latin = "u"; added_string_hiragana = "う"; added_string_katakana = "ー"; break;
            case "katakana_repeat_bar":

                if (!added_string_last.equals("")) {
                    if (added_string_last.substring(added_string_last.length() - 1).equals("a")) { added_string_latin = "a"; added_string_hiragana = "あ"; added_string_katakana = "ー";
                    } else if (added_string_last.substring(added_string_last.length() - 1).equals("i")) { added_string_latin = "i"; added_string_hiragana = "い"; added_string_katakana = "ー";
                    } else if (added_string_last.substring(added_string_last.length() - 1).equals("u")) { added_string_latin = "u"; added_string_hiragana = "う"; added_string_katakana = "ー";
                    } else if (added_string_last.substring(added_string_last.length() - 1).equals("e")) { added_string_latin = "e"; added_string_hiragana = "え"; added_string_katakana = "ー";
                    } else if (added_string_last.substring(added_string_last.length() - 1).equals("o")) { added_string_latin = "o"; added_string_hiragana = "お"; added_string_katakana = "ー";
                    } else if (added_string_last.equals("*")) { added_string_latin = ""; added_string_hiragana = ""; added_string_katakana = "";
                    }
                } break;
            case "small_tsu": added_string_latin = ""; added_string_hiragana = "っ"; added_string_katakana = "ッ"; break;
        }

        char first_char;
        if (!added_string_latin.equals("")) {
        }
        if (added_string_last.equals("small_tsu")) {
            if (added_string_latin.equals("")) { added_string_latin = "*"; } //If the character after small_tsu is invlid (e.g. a kanji), this line prevents the program from crashing
            else {
                first_char = added_string_latin.charAt(0);
                if (first_char == 'a' || first_char == 'e' || first_char == 'i' || first_char == 'o' || first_char == 'u' || first_char == 'y') {
                    added_string_latin = "*" + added_string_latin;
                } else {
                    added_string_latin = first_char + added_string_latin;
                }
            }
        }

        // Delimiters
        else if (character.equals(",")) {
            added_string_latin = ", ";
            added_string_katakana = ", ";
            added_string_hiragana = ", ";
            if (character_next.equals(" ")) { i++; }
        }

        else if (added_string.equals("original"))   { added_string_latin = character; added_string_hiragana = character; added_string_katakana = character; }

        List<String> output = new ArrayList<>();

        output.add(Integer.toString(i));
        output.add(added_string_latin);
        output.add(added_string_hiragana);
        output.add(added_string_katakana);

        return output;
    }
    public static List<String> getKunreiShikiRomanizations(String text) {
        List<List<String>> possibleInterpretations = new ArrayList<>();
        List<String> newCharacterList;
        for (String character : text.split("(?!^)")) {
            if (character.equalsIgnoreCase("ō") || character.equalsIgnoreCase("ô")) {
                if (possibleInterpretations.size() == 0) {
                    newCharacterList = new ArrayList<>();
                    newCharacterList.add("ou");
                    possibleInterpretations.add(newCharacterList);
                    newCharacterList = new ArrayList<>();
                    newCharacterList.add("oo");
                    possibleInterpretations.add(newCharacterList);
                } else {
                    int initialSize = possibleInterpretations.size();
                    for (int i = 0; i < initialSize; i++) {
                        newCharacterList = new ArrayList<>(possibleInterpretations.get(i));
                        newCharacterList.add("oo");
                        possibleInterpretations.get(i).add("ou");
                        possibleInterpretations.add(newCharacterList);
                    }
                }
            }
            else if (character.equalsIgnoreCase("ā") || character.equalsIgnoreCase("â")) {
                if (possibleInterpretations.size() == 0) {
                    newCharacterList = new ArrayList<>();
                    newCharacterList.add("aa");
                    possibleInterpretations.add(newCharacterList);
                } else {
                    for (int i = 0; i < possibleInterpretations.size(); i++) {
                        possibleInterpretations.get(i).add("aa");
                    }
                }
            }
            else if (character.equalsIgnoreCase("ē") || character.equalsIgnoreCase("ê")) {
                if (possibleInterpretations.size() == 0) {
                    newCharacterList = new ArrayList<>();
                    newCharacterList.add("ee");
                    possibleInterpretations.add(newCharacterList);
                } else {
                    for (int i = 0; i < possibleInterpretations.size(); i++) {
                        possibleInterpretations.get(i).add("ee");
                    }
                }
            }
            else if (character.equalsIgnoreCase("ū") || character.equalsIgnoreCase("û")) {
                if (possibleInterpretations.size() == 0) {
                    newCharacterList = new ArrayList<>();
                    newCharacterList.add("uu");
                    possibleInterpretations.add(newCharacterList);
                } else {
                    for (int i = 0; i < possibleInterpretations.size(); i++) {
                        possibleInterpretations.get(i).add("uu");
                    }
                }
            }
            else {
                if (possibleInterpretations.size() == 0) {
                    newCharacterList = new ArrayList<>();
                    newCharacterList.add(character);
                    possibleInterpretations.add(newCharacterList);
                } else {
                    for (int i = 0; i < possibleInterpretations.size(); i++) {
                        possibleInterpretations.get(i).add(character);
                    }
                }
            }
        }
        List<String> finalStrings = new ArrayList<>();
        for (int i=0; i<possibleInterpretations.size(); i++) {
            finalStrings.add(TextUtils.join("", possibleInterpretations.get(i)));
        }
        return finalStrings;
    }
}