package com.japagram.data;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface WordDao {
    //For example: https://github.com/googlesamples/android-architecture-components/blob/master/PersistenceContentProviderSample/app/src/main/java/com/example/android/contentprovidersample/data/CheeseDao.java

    //Return number of Words in a table
    @Query("SELECT COUNT(*) FROM " + Word.TABLE_NAME)
    int count();

    //Insert Word into table
    @Insert
    long insert(Word Word);

    //Insert multiple Words into table
    @Insert
    long[] insertAll(List<Word> Words);

    //Get all Words in the table
    @Query("SELECT * FROM " + Word.TABLE_NAME)
    List<Word> getAllWords();

    //Get a Word by Id
    @Query("SELECT * FROM " + Word.TABLE_NAME + " WHERE " + Word.COLUMN_ID + " = :id")
    Word getWordByWordId(long id);

    //Get a Word by exact romaji and kanji match
    @Query("SELECT * FROM " + Word.TABLE_NAME + " WHERE " + Word.COLUMN_WORD_ROMAJI + " = :romaji AND " + Word.COLUMN_WORD_KANJI + " = :kanji")
    List<Word> getWordsByExactRomajiAndKanjiMatch(String romaji, String kanji);

    //Get a Word containing romaji
    @Query("SELECT * FROM " + Word.TABLE_NAME + " WHERE " + Word.COLUMN_WORD_ROMAJI + " LIKE '%' || :romaji || '%' ")
    List<Word> getWordsContainingRomajiMatch(String romaji);

    //Get a Word list by Ids
    @Query("SELECT * FROM " + Word.TABLE_NAME + " WHERE " + Word.COLUMN_ID + " IN (:ids)")
    List<Word> getWordListByWordIds(List<Long> ids);

    //Delete a Word by Id
    @Query("DELETE FROM " + Word.TABLE_NAME + " WHERE " + Word.COLUMN_ID + " = :id")
    int deleteWordById(long id);

    @Delete
    void deleteWords(Word... Words);

    //Update a Word by Id
    @Update
    int update(Word Word);

    //Delete all words
    @Query("DELETE FROM " + Word.TABLE_NAME)
    public void nukeTable();

}
