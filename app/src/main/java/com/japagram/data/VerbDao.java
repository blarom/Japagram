package com.japagram.data;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface VerbDao {
    //For example: https://github.com/googlesamples/android-architecture-components/blob/master/PersistenceContentProviderSample/app/src/main/java/com/example/android/contentprovidersample/data/CheeseDao.java

    //Return number of Verbs in a table
    @Query("SELECT COUNT(*) FROM " + Verb.TABLE_NAME)
    int count();

    //Insert Verb into table
    @Insert
    long insert(Verb Verb);

    //Insert multiple Verbs into table
    @Insert
    long[] insertAll(List<Verb> Verbs);

    //Get all Verbs in the table
    @Query("SELECT * FROM " + Verb.TABLE_NAME)
    List<Verb> getAllVerbs();

    //Get a Verb by Id
    @Query("SELECT * FROM " + Verb.TABLE_NAME + " WHERE " + Verb.COLUMN_ID + " = :id")
    Verb getVerbByVerbId(long id);

    //Get a Verb by Id
    @Query("SELECT * FROM " + Verb.TABLE_NAME + " WHERE " + Verb.COLUMN_ID + " IN (:ids)")
    List<Verb> getVerbListByVerbIds(List<Long> ids);

    //Get a List of Verbs by Exact query match in Romaji column
    @Query("SELECT * FROM " + Verb.TABLE_NAME + " WHERE " + Verb.COLUMN_VERB_ROMAJI + " = :query")
    List<Verb> getVerbByExactRomajiMatch(String query);

    //Get a List of Verbs by Exact query match in Romaji column
    @Query("SELECT * FROM " + Verb.TABLE_NAME + " WHERE " + Verb.COLUMN_VERB_KANJI + " = :query")
    List<Verb> getVerbByExactKanjiQueryMatch(String query);

    //Delete a Verb by Id
    @Query("DELETE FROM " + Verb.TABLE_NAME + " WHERE " + Verb.COLUMN_ID + " = :id")
    int deleteVerbById(long id);

    @Delete
    void deleteVerbs(Verb... Verbs);

    //Update specific parameters of the verb (not working correctly for some reason - therefore not used in code)
    @Query("UPDATE " + Verb.TABLE_NAME + " SET "
                     + Verb.COLUMN_VERB_ACTIVE_LATINROOT + " = :activeLatinRoot "
            + "AND " + Verb.COLUMN_VERB_ACTIVE_KANJIROOT + " = :activeKanjiRoot "
            + "AND " + Verb.COLUMN_VERB_ACTIVE_ALTSPELLING + " = :activeAltSpelling "
            + "WHERE " + Verb.COLUMN_ID + " = :id")
    void updateVerbByVerbIdWithParameters(long id, String activeLatinRoot, String activeKanjiRoot, String activeAltSpelling);

    //Update a Verb by Id
    @Update
    int update(Verb Verb);


}
