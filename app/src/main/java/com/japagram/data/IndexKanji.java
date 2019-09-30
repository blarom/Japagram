package com.japagram.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;


@Entity(tableName = IndexKanji.TABLE_NAME)
public class IndexKanji {

    static final String TABLE_NAME = "kanji_index_table";
    static final String COLUMN_KANA = "kana";
    private static final String WORD_IDS = "word_ids";
    static final String COLUMN_KANA_IDS = "kana_ids";

    IndexKanji() { }

    @Ignore
    public IndexKanji(@NonNull String kana, String wordIds, @NonNull String kanaIds) {
        this.kana = kana;
        this.wordIds = wordIds;
        this.kanaIds = kanaIds;
    }

    @PrimaryKey()
    @ColumnInfo(name = COLUMN_KANA)
    @NonNull private String kana;
    public void setKana(String kana) {
        this.kana = kana;
    }
    public String getKana() {
        return kana;
    }

    @ColumnInfo(name = WORD_IDS)
    private String wordIds;
    public void setWordIds(String wordIds) {
        this.wordIds = wordIds;
    }
    public String getWordIds() {
        return wordIds;
    }

    @ColumnInfo(index = true, name = COLUMN_KANA_IDS)
    @NonNull private String kanaIds = ".";
    public void setKanaIds(@NonNull String kanaIds) {
        this.kanaIds = kanaIds;
    }
    @NonNull public String getKanaIds() {
        return kanaIds;
    }

}
