package com.japagram.data;

import android.provider.BaseColumns;

import java.util.Comparator;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = KanjiCharacter.TABLE_NAME, indices = {@Index("hexIdentifier")})
public class KanjiCharacter  {

    public static final String TABLE_NAME = "kanji_characters_table";
    public static final String COLUMN_ID = BaseColumns._ID;
    static final String COLUMN_KANJI = "kanji";
    static final String COLUMN_KANJI_HEX_ID = "hexIdentifier";
    private static final String COLUMN_KANJI_STRUCTURE = "structure";
    private static final String COLUMN_KANJI_COMPONENTS = "components";
    static final String COLUMN_KANJI_ON_READINGS = "onreadings";
    static final String COLUMN_KANJI_KUN_READINGS = "kunreadings";
    static final String COLUMN_KANJI_NAME_READINGS = "nameReadings";
    static final String COLUMN_KANJI_MEANINGS_EN = "meaningsEN";
    static final String COLUMN_KANJI_MEANINGS_FR = "meaningsFR";
    static final String COLUMN_KANJI_MEANINGS_ES = "meaningsES";
    static final String COLUMN_KANJI_RADICAL_PLUS_STROKES = "radPlusStrokes";
    static final String COLUMN_KANJI_USED_IN_JAP = "usedinjap";

    public KanjiCharacter() {
    }

    @Ignore
    public KanjiCharacter(String hexId) {
        this.hexIdentifier = hexId;
    }

    @Ignore
    public KanjiCharacter(String hexId, String structure, String components) {
        this.hexIdentifier = hexId;
        this.structure = structure;
        this.components = components;
    }

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(index = true, name = COLUMN_ID)
    public long id;
    public long getKanjiCharacterId() {
        return id;
    }
    public void setKanjiCharacterId(long id) {
        this.id = id;
    }

    @ColumnInfo(name = COLUMN_KANJI)
    private String kanji;
    public String getKanji() {
        return kanji;
    }
    public void setKanji(String kanji) {
        this.kanji = kanji;
    }

    @ColumnInfo(name = COLUMN_KANJI_HEX_ID)
    private String hexIdentifier;
    public String getHexIdentifier() {
        return hexIdentifier;
    }
    public void setHexIdentifier(String hexIdentifier) {
        this.hexIdentifier = hexIdentifier;
    }

    @ColumnInfo(name = COLUMN_KANJI_STRUCTURE)
    private String structure;
    public void setStructure(String structure) {
        this.structure = structure;
    }
    public String getStructure() {
        return structure;
    }

    @ColumnInfo(name = COLUMN_KANJI_COMPONENTS)
    private String components;
    public void setComponents(String components) {
        this.components = components;
    }
    public String getComponents() {
        return components;
    }

    @ColumnInfo(name = COLUMN_KANJI_ON_READINGS)
    private String onReadings;
    public void setOnReadings(String onReadings) {
        this.onReadings = onReadings;
    }
    public String getOnReadings() {
        return onReadings;
    }

    @ColumnInfo(name = COLUMN_KANJI_KUN_READINGS)
    private String kunReadings;
    public void setKunReadings(String kunReadings) {
        this.kunReadings = kunReadings;
    }
    public String getKunReadings() {
        return kunReadings;
    }

    @ColumnInfo(name = COLUMN_KANJI_NAME_READINGS)
    private String nameReadings;
    public void setNameReadings(String nameReadings) {
        this.nameReadings = nameReadings;
    }
    public String getNameReadings() {
        return nameReadings;
    }

    @ColumnInfo(name = COLUMN_KANJI_MEANINGS_EN)
    private String meaningsEN;
    public void setMeaningsEN(String meaningsEN) {
        this.meaningsEN = meaningsEN;
    }
    public String getMeaningsEN() {
        return meaningsEN;
    }

    @ColumnInfo(name = COLUMN_KANJI_MEANINGS_FR)
    private String meaningsFR;
    public void setMeaningsFR(String meaningsFR) {
        this.meaningsFR = meaningsFR;
    }
    public String getMeaningsFR() {
        return meaningsFR;
    }

    @ColumnInfo(name = COLUMN_KANJI_MEANINGS_ES)
    private String meaningsES;
    public void setMeaningsES(String meaningsES) {
        this.meaningsES = meaningsES;
    }
    public String getMeaningsES() {
        return meaningsES;
    }

    @ColumnInfo(name = COLUMN_KANJI_RADICAL_PLUS_STROKES)
    private String radPlusStrokes;
    public void setRadPlusStrokes(String radPlusStrokes) {
        this.radPlusStrokes = radPlusStrokes;
    }
    public String getRadPlusStrokes() {
        return radPlusStrokes;
    }

    public static Comparator<KanjiCharacter> hexIdentiferComparatorAscending = new Comparator<KanjiCharacter>() {
        public int compare(KanjiCharacter u1, KanjiCharacter u2)
        {
            return u1.getHexIdentifier().compareTo(u2.getHexIdentifier());
        }
    };

    @ColumnInfo(name = COLUMN_KANJI_USED_IN_JAP)
    private int usedInJapanese;
    public void setUsedInJapanese(int usedInJapanese) {
        this.usedInJapanese = usedInJapanese;
    }
    public int getUsedInJapanese() {
        return usedInJapanese;
    }
}
