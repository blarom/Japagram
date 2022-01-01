package com.japagram.data;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.japagram.utilitiesAndroid.AndroidUtilitiesIO;
import com.japagram.utilitiesCrossPlatform.Globals;
import com.japagram.utilitiesAndroid.AndroidUtilitiesPrefs;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
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
                if (AndroidUtilitiesPrefs.getAppPreferenceDbVersionNames(context) != Globals.NAMES_DB_VERSION) {
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

        AndroidUtilitiesPrefs.setProgressValueForDbInstallation(context, 0, Globals.NAMES_DB);
        if (word().count() == 0 || indexRomaji().count() == 0) {
            word().nukeTable();
            AndroidUtilitiesPrefs.setAppPreferenceNamesDatabasesFinishedLoadingFlag(context, false);
            runInTransaction(() -> {
                if (Looper.myLooper() == null) Looper.prepare();
                loadIntoDatabase("LineNamesDb - Words.csv", "namesDbWords", 40000, context, word());
                Log.i(Globals.DEBUG_TAG,"Loaded Names Words Database.");
            });
        }
        if (this.indexRomaji().count() == 0) {
            runInTransaction(() -> {
                if (Looper.myLooper() == null) Looper.prepare();
                loadIntoDatabase("LineNamesDb - RomajiIndex.csv", "indexRomaji", 50000, context, indexRomaji());
                loadIntoDatabase("LineNamesDb - KanjiIndex.csv", "indexKanji", 50000, context, indexKanji());
                Log.i(Globals.DEBUG_TAG,"Loaded Names Indexes Database.");
                AndroidUtilitiesPrefs.setAppPreferenceDbVersionNames(context, Globals.NAMES_DB_VERSION);
            });
        }
        AndroidUtilitiesPrefs.setAppPreferenceNamesDatabasesFinishedLoadingFlag(context, true);
        AndroidUtilitiesPrefs.setProgressValueForDbInstallation(context, 100, Globals.NAMES_DB);

    }
    public void loadIntoDatabase(String filename, @NotNull String database, int blockSize, Context context, Object dao) {

        List<List<String>> lineBlocks = AndroidUtilitiesIO.readCSVFileAsLineBlocks(filename, blockSize, context, true);
        HashMap<String, float[]> parameters = new HashMap<>();
        parameters.put("namesDbWords", new float[]{(float) blockSize / 2,    Globals.NAMES_DB_LINES_WORDS,         Globals.NAMES_DB_SIZE_WORDS});
        parameters.put("indexRomaji",     new float[]{(float) blockSize / 4, Globals.NAMES_DB_LINES_ROMAJI_INDEX,  Globals.NAMES_DB_SIZE_ROMAJI_INDEX});
        parameters.put("indexKanji",      new float[]{(float) blockSize,     Globals.NAMES_DB_LINES_KANJI_INDEX,   Globals.NAMES_DB_SIZE_KANJI_INDEX});

        float increment = ((parameters.get(database)[0] / parameters.get(database)[1]) * parameters.get(database)[2] * 100.f / Globals.EXTENDED_DB_SIZE_TOTAL);

        if (database.equals("namesDbWords")) {
            for (List<String> lineBlock : lineBlocks) {
                runInTransaction(() -> AndroidUtilitiesIO.insertWordBlock(lineBlock, context, dao, increment, (int) parameters.get(database)[0], Globals.NAMES_DB));
            }
        } else {
            for (List<String> lineBlock : lineBlocks) {
                runInTransaction(() -> AndroidUtilitiesIO.insertIndexBlock(lineBlock, context, dao, increment, (int) parameters.get(database)[0], database));
            }

        }
    }

    //Switches the internal implementation with an empty in-memory database
    @VisibleForTesting
    public static void switchToInMemory(@NotNull Context context) {
        sInstance = Room
                .inMemoryDatabaseBuilder(context.getApplicationContext(), RoomNamesDatabase.class)
                .build();
    }

    private static final Migration FROM_1_TO_2 = new Migration(1, 2) {
        @Override
        public void migrate(final @NotNull SupportSQLiteDatabase database) {
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
