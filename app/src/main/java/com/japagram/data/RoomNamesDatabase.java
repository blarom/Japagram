package com.japagram.data;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.japagram.resources.Globals;
import com.japagram.resources.Utilities;
import com.japagram.resources.UtilitiesPrefs;

import java.util.List;

import androidx.annotation.VisibleForTesting;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Word.class,
                        IndexRomaji.class,
                        IndexEnglish.class,
                        IndexKanji.class},
                    version = Globals.NAMES_DB_VERSION,
                    exportSchema = false)
public abstract class RoomNamesDatabase extends RoomDatabase {
    //Adapted from: https://github.com/googlesamples/android-architecture-components/blob/master/PersistenceContentProviderSample/app/src/main/java/com/example/android/contentprovidersample/data/SampleDatabase.java

    //return The DAO for the tables
    @SuppressWarnings("WeakerAccess")
    public abstract WordDao word();
    public abstract IndexKanjiDao indexKanji();
    public abstract IndexRomajiDao indexRomaji();


    //Gets the singleton instance of SampleDatabase
    private static RoomNamesDatabase sInstance;
    public static synchronized RoomNamesDatabase getInstance(Context context) {
        if (sInstance == null) {
            try {
                if (UtilitiesPrefs.getAppPreferenceDbVersionNames(context) != Globals.NAMES_DB_VERSION) {
                    throw new Exception();
                }
                //Use this clause if you want to upgrade the database without destroying the previous database. Here, FROM_1_TO_2 is never satisfied since database version > 2.
                sInstance = Room
                        .databaseBuilder(context.getApplicationContext(), RoomNamesDatabase.class, "japagram_names_room_database")
                        .addMigrations(FROM_1_TO_2)
                        .enableMultiInstanceInvalidation()
                        .build();

                sInstance.populateDatabases(context);
            } catch (Exception e) {
                //If migrations weren't set up from version X to verion X+1, do a destructive migration (rebuilds the db from sratch using the assets)
                e.printStackTrace();
                sInstance = Room
                        .databaseBuilder(context.getApplicationContext(), RoomNamesDatabase.class, "japagram_names_room_database")
                        .fallbackToDestructiveMigration()
                        .enableMultiInstanceInvalidation()
                        .build();

                sInstance.populateDatabases(context);
            }
        }
        return sInstance;
    }

    private void populateDatabases(Context context) {

        UtilitiesPrefs.setProgressValueNamesDb(context, 0);
        if (word().count() == 0 || indexRomaji().count() == 0) {
            word().nukeTable();
            UtilitiesPrefs.setAppPreferenceNamesDatabasesFinishedLoadingFlag(context, false);
            runInTransaction(() -> {
                if (Looper.myLooper() == null) Looper.prepare();
                Utilities.readCSVFileAndAddToDb("LineNamesDb - Words.csv", context, "namesDbWords", word());
                Log.i(Globals.DEBUG_TAG,"Loaded Names Words Database.");
            });
        }
        if (this.indexRomaji().count() == 0) {
            runInTransaction(() -> {
                if (Looper.myLooper() == null) Looper.prepare();
                Utilities.readCSVFileAndAddToDb("LineNamesDb - RomajiIndex.csv", context, "indexRomaji", indexRomaji());
                Utilities.readCSVFileAndAddToDb("LineNamesDb - KanjiIndex.csv", context, "indexKanji", indexKanji());
                Log.i(Globals.DEBUG_TAG,"Loaded Names Indexes Database.");
                UtilitiesPrefs.setAppPreferenceDbVersionNames(context, Globals.NAMES_DB_VERSION);
            });
        }
        UtilitiesPrefs.setAppPreferenceNamesDatabasesFinishedLoadingFlag(context, true);
        UtilitiesPrefs.setProgressValueNamesDb(context, 100);

    }

    //Switches the internal implementation with an empty in-memory database
    @VisibleForTesting
    public static void switchToInMemory(Context context) {
        sInstance = Room
                .inMemoryDatabaseBuilder(context.getApplicationContext(), RoomNamesDatabase.class)
                .build();
    }

    private static final Migration FROM_1_TO_2 = new Migration(1, 2) {
        @Override
        public void migrate(final SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Repo ADD COLUMN createdAt TEXT");
        }
    };
    public static void destroyInstance() {
        sInstance = null;
    }

    public List<Word> getAllWords() {
        return word().getAllWords();
    }
    public void updateWord(Word word) {
        word().update(word);
    }
    public Word getWordByWordId(long wordId) {
        return word().getWordByWordId(wordId);
    }
    public List<Word> getWordsByExactRomajiAndKanjiMatch(String romaji, String kanji) {
        return word().getWordsByExactRomajiAndKanjiMatch(romaji, kanji);
    }
    public List<Word> getWordsContainingRomajiMatch(String romaji) {
        return word().getWordsContainingRomajiMatch(romaji);
    }
    public List<Word> getWordListByWordIds(List<Long> wordIds) {
        return word().getWordListByWordIds(wordIds);
    }
    public void insertWordList(List<Word> wordList) {
        word().insertAll(wordList);
    }
    public int getDatabaseSize() {
        return word().count();
    }

    public List<IndexRomaji> getRomajiIndexesListForStartingWord(String query) {
        return this.indexRomaji().getIndexByStartingQuery(query);
    }
    public IndexRomaji getRomajiIndexForExactWord(String query) {
        return this.indexRomaji().getIndexByExactQuery(query);
    }
    public IndexKanji getKanjiIndexForExactWord(String query) {
        //return indexKanji().getKanjiIndexByExactUTF8Query(query);
        return indexKanji().getKanjiIndexByExactQuery(query);
    }
    public List<IndexKanji> getKanjiIndexesListForStartingWord(String query) {
        //return indexKanji().getKanjiIndexByStartingUTF8Query(query);
        return indexKanji().getKanjiIndexByStartingQuery(query);
    }

}
