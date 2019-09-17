package com.japagram.data;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.japagram.resources.Utilities;

import java.util.ArrayList;
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
                    version = 4,
                    exportSchema = false)
public abstract class JapaneseToolboxExtendedRoomDatabase extends RoomDatabase {
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
    private static JapaneseToolboxExtendedRoomDatabase sInstance;
    public static synchronized JapaneseToolboxExtendedRoomDatabase getInstance(Context context) {
        if (sInstance == null) {
            try {
                //Use this clause if you want to upgrade the database without destroying the previous database. Here, FROM_1_TO_2 is never satisfied since database version > 2.
                sInstance = Room
                        .databaseBuilder(context.getApplicationContext(), JapaneseToolboxExtendedRoomDatabase.class, "japanese_toolbox_extended_room_database")
                        .addMigrations(FROM_1_TO_2)
                        .build();

                sInstance.populateDatabases(context);
            } catch (Exception e) {
                //If migrations weren't set up from version X to verion X+1, do a destructive migration (rebuilds the db from sratch using the assets)
                e.printStackTrace();
                sInstance = Room
                        .databaseBuilder(context.getApplicationContext(), JapaneseToolboxExtendedRoomDatabase.class, "japanese_toolbox_extended_room_database")
                        .fallbackToDestructiveMigration()
                        .build();

                sInstance.populateDatabases(context);
            }
        }
        return sInstance;
    }

    private void populateDatabases(Context context) {

        if (word().count() == 0) {
            Utilities.setAppPreferenceExtendedDatabasesFinishedLoadingFlag(context, false);
            beginTransaction();
            try {
                if (Looper.myLooper() == null) Looper.prepare();
                loadExtendedDatabaseIntoRoomDb(context);
                Log.i("Diagnosis Time", "Loaded Room Extended Database.");
                setTransactionSuccessful();
            } finally {
                endTransaction();
            }
        }
        if (this.indexEnglish().count() == 0) {
            beginTransaction();
            try {
                if (Looper.myLooper() == null) Looper.prepare();
                loadExtendedDatabaseIndexesIntoRoomDb(context);
                Log.i("Diagnosis Time", "Loaded Room Extended Indexes Database.");
                setTransactionSuccessful();
            } finally {
                endTransaction();
            }
        }
        Utilities.setAppPreferenceExtendedDatabasesFinishedLoadingFlag(context, true);

    }
    private void loadExtendedDatabaseIntoRoomDb(Context context) {

        // Import the excel sheets (csv format)
        List<String[]> extendedDatabase 		= Utilities.readCSVFile("LineExtendedDb - Words.csv", context);

        //Removing the titles row
        extendedDatabase.remove(0);

        List<Word> wordList = new ArrayList<>();
        for (int i=1; i<extendedDatabase.size(); i++) {
            if (extendedDatabase.get(i)[0].equals("")) break;
            Word word = Utilities.createWordFromExtendedDatabase(extendedDatabase.get(i));
            wordList.add(word);
        }
        word().insertAll(wordList);
        Log.i("Diagnosis Time","Loaded Extended Words Database.");
    }
    private void loadExtendedDatabaseIndexesIntoRoomDb(Context context){

        List<String[]> grammarDatabaseIndexRomaji = Utilities.readCSVFile("LineExtendedDb - RomajiIndex.csv", context);
        List<String[]> grammarDatabaseIndexEN = Utilities.readCSVFile("LineExtendedDb - EnglishIndex.csv", context);
        List<String[]> grammarDatabaseIndexFR = Utilities.readCSVFile("LineExtendedDb - FrenchIndex.csv", context);
        List<String[]> grammarDatabaseIndexES = Utilities.readCSVFile("LineExtendedDb - SpanishIndex.csv", context);
        List<String[]> grammarDatabaseIndexKanji = Utilities.readCSVFile("LineExtendedDb - KanjiIndex.csv", context);

        List<IndexRomaji> indexRomajiList = new ArrayList<>();
        for (int i=0; i<grammarDatabaseIndexRomaji.size(); i++) {
            if (TextUtils.isEmpty(grammarDatabaseIndexRomaji.get(i)[0])) break;
            IndexRomaji index = new IndexRomaji(grammarDatabaseIndexRomaji.get(i)[0], grammarDatabaseIndexRomaji.get(i)[1]);
            indexRomajiList.add(index);
        }
        this.indexRomaji().insertAll(indexRomajiList);

        List<IndexEnglish> indexEnglishList = new ArrayList<>();
        for (int i=1; i<grammarDatabaseIndexEN.size(); i++) {
            if (TextUtils.isEmpty(grammarDatabaseIndexEN.get(i)[0])) break;
            IndexEnglish index = new IndexEnglish(grammarDatabaseIndexEN.get(i)[0], grammarDatabaseIndexEN.get(i)[1]);
            indexEnglishList.add(index);
        }
        this.indexEnglish().insertAll(indexEnglishList);

        List<IndexFrench> indexFrenchList = new ArrayList<>();
        for (int i=1; i<grammarDatabaseIndexFR.size(); i++) {
            if (TextUtils.isEmpty(grammarDatabaseIndexFR.get(i)[0])) break;
            IndexFrench index = new IndexFrench(grammarDatabaseIndexFR.get(i)[0], grammarDatabaseIndexFR.get(i)[1]);
            indexFrenchList.add(index);
        }
        this.indexFrench().insertAll(indexFrenchList);

        List<IndexSpanish> indexSpanishList = new ArrayList<>();
        for (int i=1; i<grammarDatabaseIndexES.size(); i++) {
            if (TextUtils.isEmpty(grammarDatabaseIndexES.get(i)[0])) break;
            IndexSpanish index = new IndexSpanish(grammarDatabaseIndexES.get(i)[0], grammarDatabaseIndexES.get(i)[1]);
            indexSpanishList.add(index);
        }
        this.indexSpanish().insertAll(indexSpanishList);

        List<IndexKanji> indexKanjiList = new ArrayList<>();
        for (int i=0; i<grammarDatabaseIndexKanji.size(); i++) {
            if (TextUtils.isEmpty(grammarDatabaseIndexKanji.get(i)[0])) break;
            IndexKanji indexKanji = new IndexKanji(grammarDatabaseIndexKanji.get(i)[0], grammarDatabaseIndexKanji.get(i)[1], "");
            indexKanjiList.add(indexKanji);
        }
        indexKanji().insertAll(indexKanjiList);

        Log.i("Diagnosis Time","Loaded Extended Indexes.");
    }

    //Switches the internal implementation with an empty in-memory database
    @VisibleForTesting
    public static void switchToInMemory(Context context) {
        sInstance = Room
                .inMemoryDatabaseBuilder(context.getApplicationContext(), JapaneseToolboxExtendedRoomDatabase.class)
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
