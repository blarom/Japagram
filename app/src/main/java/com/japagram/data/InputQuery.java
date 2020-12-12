package com.japagram.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.japagram.utilitiesCrossPlatform.UtilitiesQuery;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class InputQuery implements Parcelable {
    private int searchType = 0;
    private int originalType = 0;
    private String withoutTo = "";
    private boolean isVerbWithTo = false;
    private boolean hasIngEnding = false;
    private boolean isTooShort = false;
    private String katakanaSingleElement = "";
    private String hiraganaSingleElement = "";
    private String romajiSingleElement = "";
    private String original = "";
    private String originalNoIng = "";
    private List<String> kanjiChars = new ArrayList<>();
    private List<String> searchQueriesRomaji = new ArrayList<>();
    private List<String> searchQueriesNonJapanese = new ArrayList<>();
    private List<String> searchQueriesKanji = new ArrayList<>();
    private String originalCleaned = "";
    private String originalIngless = "";
    private List<String> hiraganaConversions = new ArrayList<>();
    private List<String> katakanaConversions = new ArrayList<>();
    private List<String> waapuroConversions = new ArrayList<>();
    private List<String> conversionsMH = new ArrayList<>();
    private List<String> conversionsNS = new ArrayList<>();
    private List<String> conversionsKS = new ArrayList<>();
    private List<String> hiraganaUniqueConversions = new ArrayList<>();
    private List<String> katakanaUniqueConversions = new ArrayList<>();
    private List<String> waapuroUniqueConversions = new ArrayList<>();
    private List<String> uniqueConversionsMH = new ArrayList<>();
    private List<String> uniqueConversionsNS = new ArrayList<>();
    private List<String> uniqueConversionsKS = new ArrayList<>();

    public InputQuery(String input) {
        if (input == null || input.equals("")) return;
        Object[] preparedElements = UtilitiesQuery.prepareInputQueryFields(input);

        original = (String) preparedElements[0];
        originalCleaned = (String) preparedElements[1];
        originalNoIng = (String) preparedElements[2];
        romajiSingleElement = (String) preparedElements[3];
        hiraganaSingleElement = (String) preparedElements[4];
        katakanaSingleElement = (String) preparedElements[5];
        originalIngless = (String) preparedElements[6];

        originalType = (int) preparedElements[7];
        searchType = (int) preparedElements[8];

        hasIngEnding = (boolean) preparedElements[9];
        isVerbWithTo = (boolean) preparedElements[10];
        isTooShort = (boolean) preparedElements[11];

        searchQueriesNonJapanese = (List<String>) preparedElements[11];
        searchQueriesRomaji = (List<String>) preparedElements[12];
        searchQueriesKanji = (List<String>) preparedElements[13];
        kanjiChars = (List<String>) preparedElements[14];
        hiraganaConversions = (List<String>) preparedElements[15];
        katakanaConversions = (List<String>) preparedElements[16];
        waapuroConversions = (List<String>) preparedElements[17];
        conversionsMH = (List<String>) preparedElements[18];
        conversionsNS = (List<String>) preparedElements[19];
        conversionsKS = (List<String>) preparedElements[20];
        hiraganaUniqueConversions = (List<String>) preparedElements[21];
        katakanaUniqueConversions = (List<String>) preparedElements[22];
        waapuroUniqueConversions = (List<String>) preparedElements[123];
        uniqueConversionsMH = (List<String>) preparedElements[24];
        uniqueConversionsNS = (List<String>) preparedElements[25];
        uniqueConversionsKS = (List<String>) preparedElements[26];
    }

    protected InputQuery(@NotNull Parcel in) {
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
        originalIngless = in.readString();
        hiraganaConversions = in.createStringArrayList();
        katakanaConversions = in.createStringArrayList();
        waapuroConversions = in.createStringArrayList();
        conversionsMH = in.createStringArrayList();
        conversionsNS = in.createStringArrayList();
        conversionsKS = in.createStringArrayList();
        hiraganaUniqueConversions = in.createStringArrayList();
        katakanaUniqueConversions = in.createStringArrayList();
        waapuroUniqueConversions = in.createStringArrayList();
        uniqueConversionsMH = in.createStringArrayList();
        uniqueConversionsNS = in.createStringArrayList();
        uniqueConversionsKS = in.createStringArrayList();
    }

    public static final Creator<InputQuery> CREATOR = new Creator<InputQuery>() {
        @Contract("_ -> new")
        @Override
        public @NotNull InputQuery createFromParcel(Parcel in) {
            return new InputQuery(in);
        }

        @Contract(value = "_ -> new", pure = true)
        @Override
        public InputQuery @NotNull [] newArray(int size) {
            return new InputQuery[size];
        }
    };

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

    public String getOriginalIngless() {
        return originalIngless;
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

    public List<String> getConversionsMH() {
        return this.conversionsMH;
    }

    public List<String> getConversionsNS() {
        return this.conversionsNS;
    }

    public List<String> getConversionsKS() {
        return this.conversionsKS;
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

    public List<String> getUniqueConversionsMH() {
        return this.uniqueConversionsMH;
    }

    public List<String> getUniqueConversionsNS() {
        return this.uniqueConversionsNS;
    }

    public List<String> getUniqueConversionsKS() {
        return this.uniqueConversionsKS;
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
    public void writeToParcel(@NotNull Parcel parcel, int i) {
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
        parcel.writeString(originalIngless);
        parcel.writeStringList(kanjiChars);
        parcel.writeStringList(searchQueriesRomaji);
        parcel.writeStringList(searchQueriesNonJapanese);
        parcel.writeStringList(searchQueriesKanji);
        parcel.writeStringList(hiraganaConversions);
        parcel.writeStringList(hiraganaConversions);
        parcel.writeStringList(katakanaConversions);
        parcel.writeStringList(waapuroConversions);
        parcel.writeStringList(conversionsMH);
        parcel.writeStringList(conversionsNS);
        parcel.writeStringList(conversionsKS);
        parcel.writeStringList(hiraganaUniqueConversions);
        parcel.writeStringList(katakanaUniqueConversions);
        parcel.writeStringList(waapuroUniqueConversions);
        parcel.writeStringList(uniqueConversionsMH);
        parcel.writeStringList(uniqueConversionsNS);
        parcel.writeStringList(uniqueConversionsKS);
    }
}
