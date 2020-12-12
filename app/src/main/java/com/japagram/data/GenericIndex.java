package com.japagram.data;

import androidx.annotation.NonNull;

public class GenericIndex {

    private String wordIds = "";
    private String value = "";
    private String kana = "";
    private String kanaIds = "";

    public GenericIndex() {

    }
    public GenericIndex(@NonNull String value, String wordIds, String kanaIds, String kana) {
        this.value = value;
        this.wordIds = wordIds;
        this.kanaIds = kanaIds;
        this.kana = kana;
    }
    public String getWordIds() {
        return wordIds;
    }

    public String getKana() {
        return kana;
    }

    public void setKana(String kana) {
        this.kana = kana;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getKanaIds() {
        return kanaIds;
    }

    public void setKanaIds(String kanaIds) {
        this.kanaIds = kanaIds;
    }
}
