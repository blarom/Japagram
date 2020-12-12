package com.japagram.data;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;


@Entity(tableName = IndexEnglish.TABLE_NAME)
public class IndexEnglish extends GenericIndex {

    static final String TABLE_NAME = "english_index_table";
    static final String COLUMN_VALUE = "english_value";
    private static final String WORD_IDS = "english_word_ids";

    IndexEnglish() {}

    @Ignore
    public IndexEnglish(@NonNull String value, String wordIds) {
        this.value = value;
        this.wordIds = wordIds;
    }

    @PrimaryKey()
    @ColumnInfo(index = true, name = COLUMN_VALUE)
    @NonNull private String value = ".";

    public IndexEnglish(@NonNull String value, String wordIds, String kanaIds, String kana) {
        super(value, wordIds, kanaIds, kana);
    }

    public void setValue(@NonNull String value) {
        this.value = value;
    }
    @NonNull public String getValue() {
        return value;
    }

    @ColumnInfo(name = WORD_IDS)
    private String wordIds;
    public void setWordIds(String wordIds) {
        this.wordIds = wordIds;
    }
    public String getWordIds() {
        return wordIds;
    }


}
