package com.japagram.data;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface KanjiComponentDao {
    //For example: https://github.com/googlesamples/android-architecture-components/blob/master/PersistenceContentProviderSample/app/src/main/java/com/example/android/contentprovidersample/data/CheeseDao.java

    //Return number of KanjiComponents in a table
    @Query("SELECT COUNT(*) FROM " + KanjiComponent.TABLE_NAME)
    int count();

    //Insert KanjiComponent into table
    @Insert
    long insert(KanjiComponent KanjiComponent);

    //Insert multiple KanjiComponents into table
    @Insert
    long[] insertAll(List<KanjiComponent> KanjiComponents);

    //Get all KanjiComponents in the table
    @Query("SELECT * FROM " + KanjiComponent.TABLE_NAME)
    List<KanjiComponent> getAllKanjiComponents();

    //Get a List of KanjiComponents by Exact structure match
    @Query("SELECT * FROM " + KanjiComponent.TABLE_NAME + " WHERE " + KanjiComponent.COLUMN_COMPONENT_STRUCTURE + " = :query")
    List<KanjiComponent> getKanjiComponentsByStructure(String query);

    //Get a KanjiComponent by Id
    @Query("SELECT * FROM " + KanjiComponent.TABLE_NAME + " WHERE " + KanjiComponent.COLUMN_ID + " = :id")
    KanjiComponent getKanjiComponentById(long id);


    //Delete a KanjiComponent by Id
    @Query("DELETE FROM " + KanjiComponent.TABLE_NAME + " WHERE " + KanjiComponent.COLUMN_ID + " = :id")
    int deleteKanjiComponentById(long id);

    @Delete
    void deleteKanjiComponents(KanjiComponent... KanjiComponents);

    //Update a KanjiComponent by Id
    @Update
    int update(KanjiComponent KanjiComponent);


}
