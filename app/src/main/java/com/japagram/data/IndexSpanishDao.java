package com.japagram.data;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface IndexSpanishDao {
    //For example: https://github.com/googlesamples/android-architecture-components/blob/master/PersistenceContentProviderSample/app/src/main/java/com/example/android/contentprovidersample/data/CheeseDao.java

    //Return number of IndexSpanish in a table
    @Query("SELECT COUNT(*) FROM " + IndexSpanish.TABLE_NAME)
    int count();

    //Insert IndexSpanish into table
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(IndexSpanish index);

    //Insert multiple LatinIndexes into table
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertAll(List<IndexSpanish> indices);

    //Get all IndexSpanish in the table
    @Query("SELECT * FROM " + IndexSpanish.TABLE_NAME)
    List<IndexSpanish> getAllLatinIndexes();

    //Get a IndexSpanish by Exact query match
    @Query("SELECT * FROM " + IndexSpanish.TABLE_NAME + " WHERE " + IndexSpanish.COLUMN_VALUE + " LIKE :query")
    IndexSpanish getIndexByExactQuery(String query);

    //Get a IndexSpanish list by similar latin index query match - see: https://stackoverflow.com/questions/44234644/android-rooms-search-in-string
    @Query("SELECT * FROM " + IndexSpanish.TABLE_NAME + " WHERE " + IndexSpanish.COLUMN_VALUE + " LIKE :query  || '%' ")
    List<IndexSpanish> getIndexByStartingQuery(String query);

    //Get a IndexSpanish by query list
    @Query("SELECT * FROM " + IndexSpanish.TABLE_NAME + " WHERE " + IndexSpanish.COLUMN_VALUE + " IN (:queries)")
    List<IndexSpanish> getIndexByExactQueries(List<String> queries);

    //Delete a IndexSpanish by Latin
    @Query("DELETE FROM " + IndexSpanish.TABLE_NAME + " WHERE " + IndexSpanish.COLUMN_VALUE + " = :latin")
    int deleteIndexByLatin(String latin);

    @Delete
    void deleteIndexes(IndexSpanish... indices);

    //Update a IndexSpanish by Id
    @Update
    int update(IndexSpanish IndexSpanish);


}
