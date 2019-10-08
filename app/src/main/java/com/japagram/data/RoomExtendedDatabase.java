package com.japagram.data;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.japagram.resources.GlobalConstants;
import com.japagram.resources.Utilities;

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
                        IndexFrench.class,
                        IndexSpanish.class,
                        IndexKanji.class},
                    version = GlobalConstants.EXTENDED_DB_VERSION,
                    exportSchema = false)
public abstract class RoomExtendedDatabase extends RoomDatabase {
    //Adapted from: https://github.com/googlesamples/android-architecture-components/blob/master/PersistenceContentProviderSample/app/src/main/java/com/example/android/contentprovidersample/data/SampleDatabase.java

    //return The DAO for the tables
    @SuppressWarnings("WeakerAccess")
    public abstract WordDao word();
    public abstract IndexKanjiDao indexKanji();
    public abstract IndexRomajiDao indexRomaji();
    public abstract IndexEnglishDao indexEnglish();
    public abstract IndexFrenchDao indexFrench();
    public abstract IndexSpanishDao indexSpanish();


    //Gets the singleton instance of SampleDatabase
    private static RoomExtendedDatabase sInstance;
    public static synchronized RoomExtendedDatabase getInstance(Context context) {
        if (sInstance == null) {
            try {
                //Use this clause if you want to upgrade the database without destroying the previous database. Here, FROM_1_TO_2 is never satisfied since database version > 2.
                sInstance = Room
                        .databaseBuilder(context.getApplicationContext(), RoomExtendedDatabase.class, "japagram_extended_room_database")
                        .addMigrations(FROM_1_TO_2)
                        .enableMultiInstanceInvalidation()
                        .build();

                sInstance.populateDatabases(context);
            } catch (Exception e) {
                //If migrations weren't set up from version X to verion X+1, do a destructive migration (rebuilds the db from sratch using the assets)
                e.printStackTrace();
                sInstance = Room
                        .databaseBuilder(context.getApplicationContext(), RoomExtendedDatabase.class, "japagram_extended_room_database")
                        .fallbackToDestructiveMigration()
                        .enableMultiInstanceInvalidation()
                        .build();

                sInstance.populateDatabases(context);
            }
        }
        return sInstance;
    }

    private void populateDatabases(Context context) {

        Utilities.setProgressValueExtendedDb(context, 0);
        if (word().count() == 0 || indexEnglish().count() == 0) {
            word().nukeTable();
            Utilities.setAppPreferenceDbVersionExtended(context, GlobalConstants.EXTENDED_DB_VERSION);
            Utilities.setAppPreferenceExtendedDatabaseFinishedLoadingFlag(context, false);
            beginTransaction();
            try {
                if (Looper.myLooper() == null) Looper.prepare();
                Utilities.readCSVFileAndAddToDb("LineExtendedDb - Words.csv", context, "extendedDbWords", word());
                Log.i("Diagnosis Time","Loaded Extended Words Database.");
                setTransactionSuccessful();
            } finally {
                endTransaction();
            }
        }
        if (indexEnglish().count() == 0) {
            beginTransaction();
            try {
                if (Looper.myLooper() == null) Looper.prepare();
                Utilities.readCSVFileAndAddToDb("LineExtendedDb - RomajiIndex.csv", context, "indexRomaji", indexRomaji());
                Utilities.readCSVFileAndAddToDb("LineExtendedDb - EnglishIndex.csv", context, "indexEnglish", indexEnglish());
                Utilities.readCSVFileAndAddToDb("LineExtendedDb - FrenchIndex.csv", context, "indexFrench", indexFrench());
                Utilities.readCSVFileAndAddToDb("LineExtendedDb - SpanishIndex.csv", context, "indexSpanish", indexSpanish());
                Utilities.readCSVFileAndAddToDb("LineExtendedDb - KanjiIndex.csv", context, "indexKanji", indexKanji());
                Log.i("Diagnosis Time","Loaded Extended Indexes Database.");
                setTransactionSuccessful();
            } finally {
                endTransaction();
            }
        }
        Utilities.setAppPreferenceExtendedDatabaseFinishedLoadingFlag(context, true);
        Utilities.setProgressValueExtendedDb(context, 100);

    }

    //Switches the internal implementation with an empty in-memory database
    @VisibleForTesting
    public static void switchToInMemory(Context context) {
        sInstance = Room
                .inMemoryDatabaseBuilder(context.getApplicationContext(), RoomExtendedDatabase.class)
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

    public List<IndexRomaji> getRomajiIndexesListForStartingWordsList(List<String> words) {
        for (int i=0; i< words.size(); i++) {
            words.set(i, words.get(i) + "%");
        }
        return this.indexRomaji().getIndexByExactQueries(words);
    }
    public List<IndexRomaji> getRomajiIndexForExactWordsList(List<String> words) {
        return this.indexRomaji().getIndexByExactQueries(words);
    }
    public List<IndexRomaji> getRomajiIndexesListForStartingWord(String query) {
        return this.indexRomaji().getIndexByStartingQuery(query);
    }
    public IndexRomaji getRomajiIndexForExactWord(String query) {
        return this.indexRomaji().getIndexByExactQuery(query);
    }
    public List<IndexEnglish> getEnglishIndexesListForStartingWord(String query) {
        return this.indexEnglish().getIndexByStartingQuery(query);
    }
    public IndexEnglish getEnglishIndexForExactWord(String query) {
        return this.indexEnglish().getIndexByExactQuery(query);
    }
    public List<IndexFrench> getFrenchIndexesListForStartingWord(String query) {
        return this.indexFrench().getIndexByStartingQuery(query);
    }
    public IndexFrench getFrenchIndexForExactWord(String query) {
        return this.indexFrench().getIndexByExactQuery(query);
    }
    public List<IndexSpanish> getSpanishIndexesListForStartingWord(String query) {
        return this.indexSpanish().getIndexByStartingQuery(query);
    }
    public IndexSpanish getSpanishIndexForExactWord(String query) {
        return this.indexSpanish().getIndexByExactQuery(query);
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
