package com.japagram.data;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface IndexKanjiDao {
    //For example: https://github.com/googlesamples/android-architecture-components/blob/master/PersistenceContentProviderSample/app/src/main/java/com/example/android/contentprovidersample/data/CheeseDao.java

    //Return number of KanjiIndexes in a table
    @Query("SELECT COUNT(*) FROM " + IndexKanji.TABLE_NAME)
    int count();

    //Insert IndexKanji into table
    @Insert
    long insert(IndexKanji IndexKanji);

    //Insert multiple KanjiIndexes into table
    @Insert
    long[] insertAll(List<IndexKanji> indexKanjis);

    //Get all KanjiIndexes in the table
    @Query("SELECT * FROM " + IndexKanji.TABLE_NAME)
    List<IndexKanji> getAllKanjiIndexes();

    //Get a IndexKanji by Kanji
    @Query("SELECT * FROM " + IndexKanji.TABLE_NAME + " WHERE " + IndexKanji.COLUMN_KANA + " = :kana")
    IndexKanji getKanjiIndexByKanji(String kana);

    //Get a IndexKanji list by similar query match - see: https://stackoverflow.com/questions/44234644/android-rooms-search-in-string
    @Query("SELECT * FROM " + IndexKanji.TABLE_NAME + " WHERE " + IndexKanji.COLUMN_KANA_IDS + " LIKE :query  || '%' ")
    List<IndexKanji> getKanjiIndexByStartingUTF8Query(String query);

    //Get a IndexKanji list by similar query match - see: https://stackoverflow.com/questions/44234644/android-rooms-search-in-string
    @Query("SELECT * FROM " + IndexKanji.TABLE_NAME + " WHERE " + IndexKanji.COLUMN_KANA + " LIKE :query  || '%' ")
    List<IndexKanji> getKanjiIndexByStartingQuery(String query);

    //Get a IndexKanji by Exact query match
    @Query("SELECT * FROM " + IndexKanji.TABLE_NAME + " WHERE " + IndexKanji.COLUMN_KANA_IDS + " LIKE :query")
    IndexKanji getKanjiIndexByExactUTF8Query(String query);

    //Get a IndexKanji by Exact query match
    @Query("SELECT * FROM " + IndexKanji.TABLE_NAME + " WHERE " + IndexKanji.COLUMN_KANA + " LIKE :query")
    IndexKanji getKanjiIndexByExactQuery(String query);

    //Delete a IndexKanji by Kanji
    @Query("DELETE FROM " + IndexKanji.TABLE_NAME + " WHERE " + IndexKanji.COLUMN_KANA + " = :kana")
    int deleteKanjiIndexByKanji(String kana);

    @Delete
    void deleteKanjiIndexes(IndexKanji... indexKanjis);

    //Update a IndexKanji by Id
    @Update
    int update(IndexKanji IndexKanji);


}
