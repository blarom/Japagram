package com.japagram.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.japagram.utilitiesCrossPlatform.Globals;
import com.japagram.utilitiesCrossPlatform.UtilitiesQuery;
import com.japagram.utilitiesCrossPlatform.UtilitiesDb;
import com.japagram.utilitiesCrossPlatform.UtilitiesGeneral;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InputQuery implements Parcelable {
    private int searchType;
    private int originalType;
    private String withoutTo;
    private boolean isVerbWithTo;
    private boolean hasIngEnding;
    private boolean isTooShort;
    private String katakanaSingleElement;
    private String hiraganaSingleElement;
    private String romajiSingleElement;
    private final String original;
    private String originalNoIng;
    private List<String> kanjiChars = new ArrayList<>();
    private final List<String> searchQueriesRomaji = new ArrayList<>();
    private final List<String> searchQueriesNonJapanese = new ArrayList<>();
    private final List<String> searchQueriesKanji = new ArrayList<>();
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
        if (isEmpty()) return;
        originalType = getTextType(this.original);
        kanjiChars = UtilitiesQuery.extractKanjiChars(this.original);
        originalCleaned = UtilitiesGeneral.removeNonSpaceSpecialCharacters(original);
        Object[] results = UtilitiesQuery.getInglessVerb(this.originalCleaned.replace("'",""), getOriginalType());
        ingless = (String) results[0];
        hasIngEnding = (boolean) results[1];
        originalNoIng = hasIngEnding? "to " + ingless : original;
        String originalCleanedNoSpaces = originalCleaned.replace("\\s","");

        if (originalCleaned.length() > 3 && originalCleaned.startsWith("to ")) {
            isVerbWithTo = true;
            withoutTo = originalCleaned.substring(3);
        }

        Object[] conversions = UtilitiesQuery.getTransliterationsAsLists(originalCleaned.replaceAll("\\s",""));
        hiraganaConversions = (List<String>)conversions[Globals.ROM_COL_HIRAGANA];
        katakanaConversions = (List<String>)conversions[Globals.ROM_COL_KATAKANA];
        waapuroConversions = (List<String>)conversions[Globals.ROM_COL_WAAPURO];
        MHConversions = (List<String>)conversions[Globals.ROM_COL_MOD_HEPBURN];
        NSConversions = (List<String>)conversions[Globals.ROM_COL_NIHON_SHIKI];
        KSConversions = (List<String>)conversions[Globals.ROM_COL_KUNREI_SHIKI];

        hiraganaUniqueConversions = UtilitiesGeneral.removeDuplicatesFromStringList(hiraganaConversions);
        katakanaUniqueConversions = UtilitiesGeneral.removeDuplicatesFromStringList(katakanaConversions);
        waapuroUniqueConversions = UtilitiesGeneral.removeDuplicatesFromStringList(waapuroConversions);
        MHUniqueConversions = UtilitiesGeneral.removeDuplicatesFromStringList(MHConversions);
        NSUniqueConversions = UtilitiesGeneral.removeDuplicatesFromStringList(NSConversions);
        KSUniqueConversions = UtilitiesGeneral.removeDuplicatesFromStringList(KSConversions);

        this.romajiSingleElement = waapuroConversions.get(0);
        this.hiraganaSingleElement = hiraganaConversions.get(0);
        this.katakanaSingleElement = katakanaConversions.get(0);

        if (originalType == Globals.TYPE_LATIN) {
            searchType = Globals.TYPE_LATIN;
            boolean isEnglishWord = false;
            this.searchQueriesNonJapanese.add(originalCleaned);
            if (originalCleaned.length() > 3 && originalCleaned.endsWith("ing")) {
                //this.searchQueriesNonJapanese.add(ingless);
                isEnglishWord = true;
            }
            if (originalCleaned.length() > 3 && originalCleaned.startsWith("to ")) {
                searchQueriesNonJapanese.add(withoutTo);
                isEnglishWord = true;
            }
            if (!isEnglishWord) {
                searchQueriesRomaji.add(originalCleaned);
                for (String conversion : waapuroUniqueConversions) {
                    if (!conversion.contains("*") && !conversion.equals(originalCleaned)) searchQueriesRomaji.add(conversion);
                }
            }
            isTooShort = originalCleanedNoSpaces.length() < Globals.SMALL_WORD_LENGTH;
        }
        else if (originalType == Globals.TYPE_HIRAGANA || originalType == Globals.TYPE_KATAKANA) {
            searchType = Globals.TYPE_LATIN;
            searchQueriesRomaji.addAll(waapuroUniqueConversions);
            isTooShort = searchQueriesRomaji.get(0).length() < Globals.SMALL_WORD_LENGTH;
        }
        else if (originalType == Globals.TYPE_NUMBER) {
            searchType = Globals.TYPE_LATIN;
            searchQueriesNonJapanese.add(originalCleaned);
            isTooShort = originalCleanedNoSpaces.length() < Globals.SMALL_WORD_LENGTH - 1;
        }
        else if (originalType == Globals.TYPE_KANJI) {
            searchType = Globals.TYPE_KANJI;
            searchQueriesKanji.add(UtilitiesDb.replaceInvalidKanjisWithValidOnes(originalCleaned));
            isTooShort = false;
        }

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
        originalType = in.readInt();
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

    public static int getTextType(@NotNull String input_value) {

        if (input_value.contains("*") || input_value.contains("＊") || input_value.equals("") || input_value.equals("-") ) { return Globals.TYPE_INVALID;}

        input_value = UtilitiesGeneral.removeSpecialCharacters(input_value);
        String character;
        int text_type = Globals.TYPE_INVALID;

        String hiraganaAlphabet = "あいうえおかきくけこがぎぐげごさしすせそざじずぜぞたてとだでどちつづなぬねのんにはひふへほばびぶべぼぱぴぷぺぽまみむめもやゆよらりるれろわをゔっゐゑぢぁゃゅぅょぉぇぃ";
        String katakanaAlphabet = "アイウエオカキクケコガギグゲゴサシスセソザジズゼゾタテトダデドチツヅナニヌネノンハヒフヘホバビブベボパピプポペマミムメモヤユヨラリルレロワヲヴーッヰヱァャュゥォョェィ";
        String latinAlphabet = "aāáÀÂÄÆbcÇdeēÈÉÊËfghiíÎÏjklmnñoōóÔŒpqrstuūúÙÛÜvwxyz'".toLowerCase();
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

    public List<String> getKanjiChars() {
        return kanjiChars;
    }

    public boolean isEmpty() {
        return TextUtils.isEmpty(original);
    }

    public String getOriginal() {
        return original;
    }

    public String getOriginalCleaned() {
        return originalCleaned;
    }

    public List<String> getSearchQueriesRomaji() {
        return searchQueriesRomaji;
    }

    public String getIngless() {
        return ingless;
    }

    public int getOriginalType() {
        return originalType;
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

    public boolean isVerbWithTo() {
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

    public int getSearchType() {
        return searchType;
    }

    public List<String> getSearchQueriesNonJapanese() {
        return searchQueriesNonJapanese;
    }

    public List<String> getSearchQueriesKanji() {
        return searchQueriesKanji;
    }

    public boolean isTooShort() {
        return isTooShort;
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
        parcel.writeInt(originalType);
        parcel.writeString(originalCleaned);
        parcel.writeString(ingless);
        parcel.writeStringList(kanjiChars);
        parcel.writeStringList(searchQueriesRomaji);
        parcel.writeStringList(searchQueriesNonJapanese);
        parcel.writeStringList(searchQueriesKanji);
        parcel.writeStringList(hiraganaConversions);
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
