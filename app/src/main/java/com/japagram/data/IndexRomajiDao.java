package com.japagram.data;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface IndexRomajiDao {
    //For example: https://github.com/googlesamples/android-architecture-components/blob/master/PersistenceContentProviderSample/app/src/main/java/com/example/android/contentprovidersample/data/CheeseDao.java

    //Return number of IndexRomaji in a table
    @Query("SELECT COUNT(*) FROM " + IndexRomaji.TABLE_NAME)
    int count();

    //Insert IndexRomaji into table
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(IndexRomaji index);

    //Insert multiple LatinIndexes into table
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertAll(List<IndexRomaji> indices);

    //Get all IndexRomaji in the table
    @Query("SELECT * FROM " + IndexRomaji.TABLE_NAME)
    List<IndexRomaji> getAllLatinIndexes();

    //Get a IndexRomaji by Exact query match
    @Query("SELECT * FROM " + IndexRomaji.TABLE_NAME + " WHERE " + IndexRomaji.COLUMN_VALUE + " LIKE :query")
    IndexRomaji getIndexByExactQuery(String query);

    //Get a IndexRomaji list by similar latin index query match - see: https://stackoverflow.com/questions/44234644/android-rooms-search-in-string
    @Query("SELECT * FROM " + IndexRomaji.TABLE_NAME + " WHERE " + IndexRomaji.COLUMN_VALUE + " LIKE :query  || '%' ")
    List<IndexRomaji> getIndexByStartingQuery(String query);

    //Get a IndexRomaji by query list
    @Query("SELECT * FROM " + IndexRomaji.TABLE_NAME + " WHERE " + IndexRomaji.COLUMN_VALUE + " IN (:queries)")
    List<IndexRomaji> getIndexByExactQueries(List<String> queries);

    //Delete a IndexRomaji by Latin
    @Query("DELETE FROM " + IndexRomaji.TABLE_NAME + " WHERE " + IndexRomaji.COLUMN_VALUE + " = :romaji")
    int deletIndexByRomaji(String romaji);

    @Delete
    void deleteIndexes(IndexRomaji... indices);

    //Update a IndexRomaji by Id
    @Update
    int update(IndexRomaji IndexRomaji);


}
