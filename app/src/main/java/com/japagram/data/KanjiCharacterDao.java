package com.japagram.data;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface KanjiCharacterDao {
    //For example: https://github.com/googlesamples/android-architecture-components/blob/master/PersistenceContentProviderSample/app/src/main/java/com/example/android/contentprovidersample/data/CheeseDao.java

    //Return number of KanjiCharacters in a table
    @Query("SELECT COUNT(*) FROM " + KanjiCharacter.TABLE_NAME)
    int count();

    //Insert KanjiCharacter into table
    @Insert
    long insert(KanjiCharacter KanjiCharacter);

    //Insert multiple KanjiCharacters into table
    @Insert
    long[] insertAll(List<KanjiCharacter> KanjiCharacters);

    //Get all KanjiCharacters in the table
    @Query("SELECT * FROM " + KanjiCharacter.TABLE_NAME)
    List<KanjiCharacter> getAllKanjiCharacters();

    //Get a KanjiCharacter by Id
    @Query("SELECT * FROM " + KanjiCharacter.TABLE_NAME + " WHERE " + KanjiCharacter.COLUMN_ID + " = :id")
    KanjiCharacter getKanjiCharacterById(long id);

    //Get all Kanji Hex Ids
    @Query("SELECT " + KanjiCharacter.COLUMN_KANJI_HEX_ID + " FROM " + KanjiCharacter.TABLE_NAME)
    List<String> getAllKanjiHexIds();

    //Get all Kanjis
    @Query("SELECT " + KanjiCharacter.COLUMN_KANJI + " FROM " + KanjiCharacter.TABLE_NAME)
    List<String> getAllKanjis();

    //Get all Kanjis used in Japanese
    @Query("SELECT " + KanjiCharacter.COLUMN_KANJI + " FROM " + KanjiCharacter.TABLE_NAME + " WHERE " + KanjiCharacter.COLUMN_KANJI_USED_IN_JAP + " = 1")
    List<String> getAllKanjisUsedInJapanese();

    //Get a KanjiCharacter by Id
    @Query("SELECT * FROM " + KanjiCharacter.TABLE_NAME + " WHERE " + KanjiCharacter.COLUMN_KANJI_HEX_ID + " = :hexId")
    KanjiCharacter getKanjiCharacterByHexId(String hexId);

    //Get a KanjiCharacter list by Ids
    @Query("SELECT * FROM " + KanjiCharacter.TABLE_NAME + " WHERE " + KanjiCharacter.COLUMN_KANJI_HEX_ID + " IN (:hexIdList)")
    List<KanjiCharacter> getKanjiCharactersByHexIdList(List<String> hexIdList);

    //Get a KanjiCharacter list by Ids
    @Query("SELECT * FROM " + KanjiCharacter.TABLE_NAME + " WHERE " + KanjiCharacter.COLUMN_KANJI_USED_IN_JAP + " = 1")
    List<KanjiCharacter> getKanjiCharactersUsedInJapanese();

    //Get a KanjiCharacter list by Descriptor
    @Query("SELECT * FROM " + KanjiCharacter.TABLE_NAME + " WHERE "
            + KanjiCharacter.COLUMN_KANJI_RADICAL_PLUS_STROKES + " =  :query OR "
            + KanjiCharacter.COLUMN_KANJI_RADICAL_PLUS_STROKES + " LIKE  '%' || :query || '+%'")
    List<KanjiCharacter> getKanjiCharactersByDescriptor(String query);

    //Get a KanjiCharacter list by MeaningEN
    @Query("SELECT * FROM " + KanjiCharacter.TABLE_NAME + " WHERE " + KanjiCharacter.COLUMN_KANJI_MEANINGS_EN + " LIKE  '%' || :query || '%'")
    List<KanjiCharacter> getKanjiCharactersByMeaningEN(String query);

    //Get a KanjiCharacter list by MeaningFR
    @Query("SELECT * FROM " + KanjiCharacter.TABLE_NAME + " WHERE " + KanjiCharacter.COLUMN_KANJI_MEANINGS_FR + " LIKE  '%' || :query || '%'")
    List<KanjiCharacter> getKanjiCharactersByMeaningFR(String query);

    //Get a KanjiCharacter list by MeaningES
    @Query("SELECT * FROM " + KanjiCharacter.TABLE_NAME + " WHERE " + KanjiCharacter.COLUMN_KANJI_MEANINGS_ES + " LIKE  '%' || :query || '%'")
    List<KanjiCharacter> getKanjiCharactersByMeaningES(String query);

    //Get a KanjiCharacter list by Reading
    @Query("SELECT * FROM " + KanjiCharacter.TABLE_NAME + " WHERE "
            + KanjiCharacter.COLUMN_KANJI_ON_READINGS + " LIKE '%' || :query || '%' OR "
            + KanjiCharacter.COLUMN_KANJI_KUN_READINGS + " LIKE '%' || :query || '%' OR "
            + KanjiCharacter.COLUMN_KANJI_NAME_READINGS + " LIKE '%' || :query || '%'")
    List<KanjiCharacter> getKanjiCharactersByKanaDescriptor(String query);

    //Delete a KanjiCharacter by Id
    @Query("DELETE FROM " + KanjiCharacter.TABLE_NAME + " WHERE " + KanjiCharacter.COLUMN_ID + " = :id")
    int deleteKanjiCharacterById(long id);

    @Delete
    void deleteKanjiCharacters(KanjiCharacter... KanjiCharacters);

    //Update a KanjiCharacter by Id
    @Update
    int update(KanjiCharacter KanjiCharacter);

    //Delete all KanjiCharacters
    @Query("DELETE FROM " + KanjiCharacter.TABLE_NAME)
    public void nukeTable();

}
