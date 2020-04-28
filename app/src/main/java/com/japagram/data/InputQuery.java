package com.japagram.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.japagram.resources.Globals;
import com.japagram.resources.Utilities;
import com.japagram.resources.UtilitiesDb;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InputQuery implements Parcelable {
    private String withoutTo;
    private boolean isVerbWithTo;
    private boolean hasIngEnding;
    private String katakanaSingleElement;
    private String hiraganaSingleElement;
    private String romajiSingleElement;
    private String original;
    private String originalNoIng;
    private String searchQuery;
    private int type;
    private String noSpaces = "";
    private String originalCleaned = "";
    private String ingless = "";
    private List<String> hiraganaConversions = new ArrayList<>();
    private List<String> katakanaConversions = new ArrayList<>();
    private List<String> waapuroConversions = new ArrayList<>();
    private List<String> MHConversions = new ArrayList<>();
    private List<String> NSConversions = new ArrayList<>();
    private List<String> KSConversions = new ArrayList<>();
    private List<String> hiraganaUniqueConversions = new ArrayList<>();
    private List<String> katakanaUniqueConversions = new ArrayList<>();
    private List<String> waapuroUniqueConversions = new ArrayList<>();
    private List<String> MHUniqueConversions = new ArrayList<>();
    private List<String> NSUniqueConversions = new ArrayList<>();
    private List<String> KSUniqueConversions = new ArrayList<>();

    public InputQuery(String input) {
        this.original = input.toLowerCase(Locale.ENGLISH); //Converting the word to lowercase (the search algorithm is not efficient if needing to search both lower and upper case)
        this.original = UtilitiesDb.replaceInvalidKanjisWithValidOnes(input, Globals.SimilarsDatabase);
        if (isEmpty()) return;
        this.type = getTextType(this.original);
        this.noSpaces = original.replace(" ","");
        this.originalCleaned = Utilities.removeNonSpaceSpecialCharacters(original);
        this.ingless = getInglessVerb(this.originalCleaned.replace("'",""));
        this.originalNoIng = hasIngEnding()? "to " + this.ingless : this.original;

        if (this.originalCleaned.length() > 3 && this.originalCleaned.substring(0, 3).equals("to ")) {
            isVerbWithTo = true;
            withoutTo = this.originalCleaned.substring(3);
        }

        Object[] conversions = InputQuery.getTransliterationsAsLists(this.originalCleaned.replaceAll("\\s",""));
        hiraganaConversions = (List<String>)conversions[Globals.ROM_COL_HIRAGANA];
        katakanaConversions = (List<String>)conversions[Globals.ROM_COL_KATAKANA];
        waapuroConversions = (List<String>)conversions[Globals.ROM_COL_WAAPURO];
        MHConversions = (List<String>)conversions[Globals.ROM_COL_MOD_HEPBURN];
        NSConversions = (List<String>)conversions[Globals.ROM_COL_NIHON_SHIKI];
        KSConversions = (List<String>)conversions[Globals.ROM_COL_KUNREI_SHIKI];

        hiraganaUniqueConversions = Utilities.removeDuplicatesFromList(hiraganaConversions);
        katakanaUniqueConversions = Utilities.removeDuplicatesFromList(katakanaConversions);
        waapuroUniqueConversions = Utilities.removeDuplicatesFromList(waapuroConversions);
        MHUniqueConversions = Utilities.removeDuplicatesFromList(MHConversions);
        NSUniqueConversions = Utilities.removeDuplicatesFromList(NSConversions);
        KSUniqueConversions = Utilities.removeDuplicatesFromList(KSConversions);

        this.romajiSingleElement = waapuroConversions.get(0);
        this.hiraganaSingleElement = hiraganaConversions.get(0);
        this.katakanaSingleElement = katakanaConversions.get(0);

        this.searchQuery = (getType() == Globals.TYPE_HIRAGANA || getType() == Globals.TYPE_KATAKANA)? this.searchQuery = getRomajiSingleElement() : getOriginalCleaned();
    }


    protected InputQuery(Parcel in) {
        withoutTo = in.readString();
        isVerbWithTo = in.readByte() != 0;
        hasIngEnding = in.readByte() != 0;
        katakanaSingleElement = in.readString();
        hiraganaSingleElement = in.readString();
        romajiSingleElement = in.readString();
        original = in.readString();
        originalNoIng = in.readString();
        searchQuery = in.readString();
        type = in.readInt();
        noSpaces = in.readString();
        originalCleaned = in.readString();
        ingless = in.readString();
        hiraganaConversions = in.createStringArrayList();
        katakanaConversions = in.createStringArrayList();
        waapuroConversions = in.createStringArrayList();
        MHConversions = in.createStringArrayList();
        NSConversions = in.createStringArrayList();
        KSConversions = in.createStringArrayList();
        hiraganaUniqueConversions = in.createStringArrayList();
        katakanaUniqueConversions = in.createStringArrayList();
        waapuroUniqueConversions = in.createStringArrayList();
        MHUniqueConversions = in.createStringArrayList();
        NSUniqueConversions = in.createStringArrayList();
        KSUniqueConversions = in.createStringArrayList();
    }

    public static final Creator<InputQuery> CREATOR = new Creator<InputQuery>() {
        @Override
        public InputQuery createFromParcel(Parcel in) {
            return new InputQuery(in);
        }

        @Override
        public InputQuery[] newArray(int size) {
            return new InputQuery[size];
        }
    };

    @NotNull
    @Contract("_ -> new")
    private static String[] getOfficialKana(String romaji) {

        if (Globals.Romanizations == null) {
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
        int[] romajiTypes = new int[]{Globals.ROM_COL_WAAPURO, Globals.ROM_COL_MOD_HEPBURN, Globals.ROM_COL_NIHON_SHIKI, Globals.ROM_COL_KUNREI_SHIKI};
        for (int romajiType : romajiTypes) {
            for (int i = 1; i < Globals.Romanizations.size(); i++) {
                currentRow = Globals.Romanizations.get(i);
                if (currentRow.length < 6) break;

                currentRomaji = currentRow[romajiType];
                transliteratedToHiragana = transliteratedToHiragana.replace(currentRomaji, currentRow[Globals.ROM_COL_HIRAGANA]);
                transliteratedToKatakana = transliteratedToKatakana.replace(currentRomaji, currentRow[Globals.ROM_COL_KATAKANA]);
            }
        }

        return new String[]{transliteratedToHiragana, transliteratedToKatakana};
    }

    @NotNull
    @Contract("_ -> new")
    private static String[] getOfficialRomanizations(String kana) {

        if (Globals.Romanizations == null) {
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
        for (int i = 1; i< Globals.Romanizations.size(); i++) {
            currentRow = Globals.Romanizations.get(i);
            if (currentRow.length < 6) break;

            currentKana = currentRow[Globals.ROM_COL_HIRAGANA];
            if (!currentKana.equals("")) {
                romanizedKanaWaapuro = romanizedKanaWaapuro.replace(currentKana, currentRow[Globals.ROM_COL_WAAPURO]);
                romanizedKanaModHepburn = romanizedKanaModHepburn.replace(currentKana, currentRow[Globals.ROM_COL_MOD_HEPBURN]);
                romanizedKanaNihonShiki = romanizedKanaNihonShiki.replace(currentKana, currentRow[Globals.ROM_COL_NIHON_SHIKI]);
                romanizedKanaKunreiShiki = romanizedKanaKunreiShiki.replace(currentKana, currentRow[Globals.ROM_COL_KUNREI_SHIKI]);
            }

            currentKana = currentRow[Globals.ROM_COL_KATAKANA];
            if (!currentKana.equals("")) {
                romanizedKanaWaapuro = romanizedKanaWaapuro.replace(currentKana, currentRow[Globals.ROM_COL_WAAPURO]);
                romanizedKanaModHepburn = romanizedKanaModHepburn.replace(currentKana, currentRow[Globals.ROM_COL_MOD_HEPBURN]);
                romanizedKanaNihonShiki = romanizedKanaNihonShiki.replace(currentKana, currentRow[Globals.ROM_COL_NIHON_SHIKI]);
                romanizedKanaKunreiShiki = romanizedKanaKunreiShiki.replace(currentKana, currentRow[Globals.ROM_COL_KUNREI_SHIKI]);
            }
        }

        return new String[]{romanizedKanaWaapuro, romanizedKanaModHepburn, romanizedKanaNihonShiki, romanizedKanaKunreiShiki};
    }

    @NotNull
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

    @NotNull
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

    public static int getTextType(@NotNull String input_value) {

        if (input_value.contains("*") || input_value.contains("＊") || input_value.equals("") || input_value.equals("-") ) { return Globals.TYPE_INVALID;}

        input_value = Utilities.removeSpecialCharacters(input_value);
        String character;
        int text_type = Globals.TYPE_INVALID;

        String hiraganaAlphabet = "あいうえおかきくけこがぎぐげごさしすせそざじずぜぞたてとだでどちつづなぬねのんにはひふへほばびぶべぼぱぴぷぺぽまみむめもやゆよらりるれろわをゔっゐゑぢぁゃゅぅょぉぇぃ";
        String katakanaAlphabet = "アイウエオカキクケコガギグゲゴサシスセソザジズゼゾタテトダデドチツヅナニヌネノンハヒフヘホバビブベボパピプポペマミムメモヤユヨラリルレロワヲヴーッヰヱァャュゥォョェィ";
        String latinAlphabet = "aāáÀÂÄÆbcÇdeēÈÉÊËfghiíÎÏjklmnñoōóÔŒpqrstuūúÙÛÜvwxyz".toLowerCase();
        String latinAlphabetCap = latinAlphabet.toUpperCase();
        String numberAlphabet = "0123456789";

        if (!input_value.equals("")) {
            for (int i=0; i<input_value.length();i++) {

                if (text_type == Globals.TYPE_KANJI) { break;}

                character = Character.toString(input_value.charAt(i));

                if (hiraganaAlphabet.contains(character)) {
                    text_type = Globals.TYPE_HIRAGANA;
                } else if (katakanaAlphabet.contains(character)) {
                    text_type = Globals.TYPE_KATAKANA;
                } else if (latinAlphabet.contains(character) || latinAlphabetCap.contains(character)) {
                    text_type = Globals.TYPE_LATIN;
                } else if (numberAlphabet.contains(character)) {
                    text_type = Globals.TYPE_NUMBER;
                } else {
                    text_type = Globals.TYPE_KANJI;
                }
            }
        } else {
            return text_type;
        }

        return text_type;
    }

    @NotNull
    @Contract("_ -> new")
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
            String hiragana = HK[Globals.ROM_COL_HIRAGANA];
            String katakana = HK[Globals.ROM_COL_KATAKANA];
            String[] romanizations = getOfficialRomanizations(hiragana);
            hiraganaConversions.add(hiragana);
            katakanaConversions.add(katakana);
            waapuroConversions.add(romanizations[Globals.ROM_WAAPURO]);
            MHConversions.add(romanizations[Globals.ROM_MOD_HEPBURN]);
            NSConversions.add(romanizations[Globals.ROM_NIHON_SHIKI]);
            KSConversions.add(romanizations[Globals.ROM_KUNREI_SHIKI]);
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

    @NotNull
    public static List<String> getWaapuroHiraganaKatakana(String text) {

        List<String> conversionsFirstElement = new ArrayList<>();

        if (TextUtils.isEmpty(text)) {
            conversionsFirstElement.add("");
            conversionsFirstElement.add("");
            conversionsFirstElement.add("");
        } else {
            Object[] conversions = getTransliterationsAsLists(text);
            conversionsFirstElement.add(((List<String>) conversions[Globals.ROM_COL_WAAPURO]).get(0));
            conversionsFirstElement.add(((List<String>) conversions[Globals.ROM_COL_HIRAGANA]).get(0));
            conversionsFirstElement.add(((List<String>) conversions[Globals.ROM_COL_KATAKANA]).get(0));
        }

        return conversionsFirstElement;
    }

    public boolean isEmpty() {
        return TextUtils.isEmpty(original);
    }

    private String getInglessVerb(@NotNull String input) {

        String returnVerb = input;
        if (input.length() > 2 && input.substring(input.length() - 3).equals("ing")) {

            if (input.length() > 5 && input.substring(input.length() - 6).equals("inging")) {
                if ( (input.substring(0, 3).equals("to ") && isOfTypeIngIng(input.substring(3))) || (!input.substring(0, 3).equals("to ") && isOfTypeIngIng(input)) ) {
                    // If the verb ends with "inging" then remove the the second "ing"
                    returnVerb = input.substring(0, input.length() - 3);
                }
            }

            this.hasIngEnding = getType() == Globals.TYPE_LATIN;
        }

        return returnVerb;

    }

    private Boolean isOfTypeIngIng(@NotNull String verb) {
        boolean answer = false;
        if (verb.equals("accinging") || verb.equals("astringing") || verb.equals("befringing") || verb.equals("besinging") ||
                verb.equals("binging") || verb.equals("boinging") || verb.equals("bowstringing") || verb.equals("bringing") ||
                verb.equals("clinging") || verb.equals("constringing") || verb.equals("cringing") || verb.equals("dinging") ||
                verb.equals("enringing") || verb.equals("flinging") || verb.equals("folksinging") || verb.equals("fringing") ||
                verb.equals("gunslinging") || verb.equals("hamstringing") || verb.equals("handwringing") || verb.equals("hinging") ||
                verb.equals("impinging") || verb.equals("inbringing") || verb.equals("infringing") || verb.equals("kinging") ||
                verb.equals("minging") || verb.equals("mudslinging") || verb.equals("outringing") || verb.equals("outsinging") ||
                verb.equals("outspringing") || verb.equals("outswinging") || verb.equals("outwinging") || verb.equals("overswinging") ||
                verb.equals("overwinging") || verb.equals("perstringing") || verb.equals("pinging") || verb.equals("refringing") ||
                verb.equals("rehinging") || verb.equals("respringing") || verb.equals("restringing") || verb.equals("ringing") ||
                verb.equals("singing") || verb.equals("slinging") || verb.equals("springing") || verb.equals("stinging") ||
                verb.equals("stringing") || verb.equals("swinging") || verb.equals("syringing") || verb.equals("twinging") ||
                verb.equals("unhinging") || verb.equals("unkinging") || verb.equals("unslinging") || verb.equals("unstringing") ||
                verb.equals("upbringing") || verb.equals("upflinging") || verb.equals("upspringing") || verb.equals("upswinging") ||
                verb.equals("whinging") || verb.equals("winging") || verb.equals("wringing") || verb.equals("zinging")) {
            answer = true;
        }
        return answer;
    }

    public String getOriginal() {
        return original;
    }

    public String getOriginalCleaned() {
        return originalCleaned;
    }

    public String getNoSpaces() {
        return noSpaces;
    }

    public String getIngless() {
        return ingless;
    }

    public int getType() {
        return type;
    }

    public String getRomajiSingleElement() {
        return romajiSingleElement;
    }

    public String getHiraganaSingleElement() {
        return hiraganaSingleElement;
    }

    public String getKatakanaSingleElement() {
        return katakanaSingleElement;
    }

    public boolean getIsVerbWithTo() {
        return isVerbWithTo;
    }

    public String getWithoutTo() {
        return withoutTo;
    }

    public boolean hasIngEnding() {
        return hasIngEnding;
    }

    public List<String> getWaapuroConversions() {
        return this.waapuroConversions;
    }

    public List<String> getHiraganaConversions() {
        return this.hiraganaConversions;
    }

    public List<String> getKatakanaConversions() {
        return this.katakanaConversions;
    }

    public List<String> getMHConversions() {
        return this.MHConversions;
    }

    public List<String> getNSConversions() {
        return this.NSConversions;
    }

    public List<String> getKSConversions() {
        return this.KSConversions;
    }

    public List<String> getWaapuroUniqueConversions() {
        return this.waapuroUniqueConversions;
    }

    public List<String> getHiraganaUniqueConversions() {
        return this.hiraganaUniqueConversions;
    }

    public List<String> getKatakanaUniqueConversions() {
        return this.katakanaUniqueConversions;
    }

    public List<String> getMHUniqueConversions() {
        return this.MHUniqueConversions;
    }

    public List<String> getNSUniqueConversions() {
        return this.NSUniqueConversions;
    }

    public List<String> getKSUniqueConversions() {
        return this.KSUniqueConversions;
    }



    public String getOriginalNoIng() {
        return originalNoIng;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(withoutTo);
        parcel.writeByte((byte) (isVerbWithTo ? 1 : 0));
        parcel.writeByte((byte) (hasIngEnding ? 1 : 0));
        parcel.writeString(katakanaSingleElement);
        parcel.writeString(hiraganaSingleElement);
        parcel.writeString(romajiSingleElement);
        parcel.writeString(original);
        parcel.writeString(originalNoIng);
        parcel.writeString(searchQuery);
        parcel.writeInt(type);
        parcel.writeString(noSpaces);
        parcel.writeString(originalCleaned);
        parcel.writeString(ingless);
        parcel.writeStringList(hiraganaConversions);
        parcel.writeStringList(katakanaConversions);
        parcel.writeStringList(waapuroConversions);
        parcel.writeStringList(MHConversions);
        parcel.writeStringList(NSConversions);
        parcel.writeStringList(KSConversions);
        parcel.writeStringList(hiraganaUniqueConversions);
        parcel.writeStringList(katakanaUniqueConversions);
        parcel.writeStringList(waapuroUniqueConversions);
        parcel.writeStringList(MHUniqueConversions);
        parcel.writeStringList(NSUniqueConversions);
        parcel.writeStringList(KSUniqueConversions);
    }
}