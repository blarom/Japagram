package com.japagram.data;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.japagram.utilitiesAndroid.AndroidUtilitiesIO;
import com.japagram.utilitiesCrossPlatform.Globals;
import com.japagram.utilitiesAndroid.AndroidUtilitiesPrefs;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
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
                    version = Globals.EXTENDED_DB_VERSION,
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
                if (AndroidUtilitiesPrefs.getAppPreferenceDbVersionExtended(context) != Globals.EXTENDED_DB_VERSION) {
                    throw new Exception();
                }
                //Use this clause if you want to upgrade the database without destroying the previous database. Here, FROM_1_TO_2 is never satisfied since database version > 2.
                sInstance = Room
                        .databaseBuilder(context.getApplicationContext(), RoomExtendedDatabase.class, "japagram_extended_room_database")
                        .addMigrations(FROM_1_TO_2)
                        .addCallback(new Callback() {
                            @Override
                            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                super.onCreate(db);
                                //getInstance(context).populateDatabases(context);
                            }
                        })
                        .enableMultiInstanceInvalidation()
                        .build();

                sInstance.populateDatabases(context);
                //sInstance.runInTransaction(() -> sInstance.populateDatabases(context));
            } catch (Exception e) {
                //If migrations weren't set up from version X to verion X+1, do a destructive migration (rebuilds the db from sratch using the assets)
                e.printStackTrace();
                sInstance = Room
                        .databaseBuilder(context.getApplicationContext(), RoomExtendedDatabase.class, "japagram_extended_room_database")
                        .fallbackToDestructiveMigration()
                        .enableMultiInstanceInvalidation()
                        .build();

                sInstance.populateDatabases(context);
                //sInstance.runInTransaction(() -> sInstance.populateDatabases(context));
            }
        }
        return sInstance;
    }

    private void populateDatabases(Context context) {

        AndroidUtilitiesPrefs.setProgressValueForDbInstallation(context, 0, Globals.EXTENDED_DB);
        if (Looper.myLooper() == null) Looper.prepare();

        if (word().count() == 0 || indexEnglish().count() == 0) {
            word().nukeTable();
            AndroidUtilitiesPrefs.setAppPreferenceExtendedDatabaseFinishedLoadingFlag(context, false);

            loadIntoDatabase("LineExtendedDb - Words.csv", "extendedDbWords", 2000, context, word());
            Log.i(Globals.DEBUG_TAG,"Loaded Extended Words Database.");
        }
        if (indexEnglish().count() == 0) {

            loadIntoDatabase("LineExtendedDb - RomajiIndex.csv", "indexRomaji", 50000, context, indexRomaji());
            loadIntoDatabase("LineExtendedDb - EnglishIndex.csv", "indexEnglish", 50000, context, indexEnglish());
            loadIntoDatabase("LineExtendedDb - FrenchIndex.csv", "indexFrench", 50000, context, indexFrench());
            loadIntoDatabase("LineExtendedDb - SpanishIndex.csv", "indexSpanish", 50000, context, indexSpanish());
            loadIntoDatabase("LineExtendedDb - KanjiIndex.csv", "indexKanji", 50000, context, indexKanji());
            Log.i(Globals.DEBUG_TAG,"Loaded Extended Indexes Database.");
            AndroidUtilitiesPrefs.setAppPreferenceDbVersionExtended(context, Globals.EXTENDED_DB_VERSION);
        }
        AndroidUtilitiesPrefs.setAppPreferenceExtendedDatabaseFinishedLoadingFlag(context, true);
        AndroidUtilitiesPrefs.setProgressValueForDbInstallation(context, 100, Globals.EXTENDED_DB);

    }
    public void loadIntoDatabase(String filename, @NotNull String database, int blockSize, Context context, Object dao) {

        List<List<String>> lineBlocks = AndroidUtilitiesIO.readCSVFileAsLineBlocks(filename, blockSize, context);
        HashMap<String, float[]> parameters = new HashMap<>();
        parameters.put("extendedDbWords", new float[]{(float) blockSize / 4, Globals.EXTENDED_DB_LINES_WORDS,         Globals.EXTENDED_DB_SIZE_WORDS});
        parameters.put("indexRomaji",     new float[]{(float) blockSize / 4, Globals.EXTENDED_DB_LINES_ROMAJI_INDEX,  Globals.EXTENDED_DB_SIZE_ROMAJI_INDEX});
        parameters.put("indexEnglish",    new float[]{(float) blockSize / 4, Globals.EXTENDED_DB_LINES_ENGLISH_INDEX, Globals.EXTENDED_DB_SIZE_ENGLISH_INDEX});
        parameters.put("indexFrench",     new float[]{(float) blockSize,     Globals.EXTENDED_DB_LINES_FRENCH_INDEX,  Globals.EXTENDED_DB_SIZE_FRENCH_INDEX});
        parameters.put("indexSpanish",    new float[]{(float) blockSize,     Globals.EXTENDED_DB_LINES_SPANISH_INDEX, Globals.EXTENDED_DB_SIZE_SPANISH_INDEX});
        parameters.put("indexKanji",      new float[]{(float) blockSize,     Globals.EXTENDED_DB_LINES_KANJI_INDEX,   Globals.EXTENDED_DB_SIZE_KANJI_INDEX});

        float increment = ((parameters.get(database)[0] / parameters.get(database)[1]) * parameters.get(database)[2] * 100.f / Globals.EXTENDED_DB_SIZE_TOTAL);

        if (database.equals("extendedDbWords")) {
            for (List<String> lineBlock : lineBlocks) {
                runInTransaction(() -> AndroidUtilitiesIO.insertWordBlock(lineBlock, context, dao, increment, (int) parameters.get(database)[0], Globals.EXTENDED_DB));
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
                .inMemoryDatabaseBuilder(context.getApplicationContext(), RoomExtendedDatabase.class)
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

    public List<IndexRomaji> getRomajiIndexesListForStartingWordsList(@NotNull List<String> words) {
        List<IndexRomaji> indexes = new ArrayList<>();
        for (String word : words) {
            indexes.addAll(indexRomaji().getIndexByStartingQuery(word));
        }
        return indexes;
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
    public List<IndexEnglish> getEnglishIndexesListForStartingWordsList(@NotNull List<String> words) {
        List<IndexEnglish> indexes = new ArrayList<>();
        for (String word : words) {
            indexes.addAll(indexEnglish().getIndexByStartingQuery(word));
        }
        return indexes;
    }
    public List<IndexEnglish> getEnglishIndexForExactWordsList(List<String> words) {
        return this.indexEnglish().getIndexByExactQueries(words);
    }
    public List<IndexFrench> getFrenchIndexesListForStartingWord(String query) {
        return this.indexFrench().getIndexByStartingQuery(query);
    }
    public IndexFrench getFrenchIndexForExactWord(String query) {
        return this.indexFrench().getIndexByExactQuery(query);
    }
    public List<IndexFrench> getFrenchIndexesListForStartingWordsList(@NotNull List<String> words) {
        List<IndexFrench> indexes = new ArrayList<>();
        for (String word : words) {
            indexes.addAll(indexFrench().getIndexByStartingQuery(word));
        }
        return indexes;
    }
    public List<IndexFrench> getFrenchIndexForExactWordsList(List<String> words) {
        return this.indexFrench().getIndexByExactQueries(words);
    }
    public List<IndexSpanish> getSpanishIndexesListForStartingWord(String query) {
        return this.indexSpanish().getIndexByStartingQuery(query);
    }
    public IndexSpanish getSpanishIndexForExactWord(String query) {
        return this.indexSpanish().getIndexByExactQuery(query);
    }
    public List<IndexSpanish> getSpanishIndexesListForStartingWordsList(@NotNull List<String> words) {
        List<IndexSpanish> indexes = new ArrayList<>();
        for (String word : words) {
            indexes.addAll(indexSpanish().getIndexByStartingQuery(word));
        }
        return indexes;
    }
    public List<IndexSpanish> getSpanishIndexForExactWordsList(List<String> words) {
        return this.indexSpanish().getIndexByExactQueries(words);
    }
    public IndexKanji getKanjiIndexForExactWord(String query) {
        //return indexKanji().getKanjiIndexByExactUTF8Query(query);
        return indexKanji().getKanjiIndexByExactQuery(query);
    }
    public List<IndexKanji> getKanjiIndexesListForStartingWord(String query) {
        //return indexKanji().getKanjiIndexByStartingUTF8Query(query);
        return indexKanji().getKanjiIndexByStartingQuery(query);
    }
    public List<IndexKanji> getKanjiIndexesListForStartingWordsList(@NotNull List<String> words) {
        List<IndexKanji> indexes = new ArrayList<>();
        for (String word : words) {
            indexes.addAll(indexKanji().getKanjiIndexByStartingQuery(word));
        }
        return indexes;
    }
    public List<IndexKanji> getKanjiIndexForExactWordsList(List<String> words) {
        return this.indexKanji().getIndexByExactQueries(words);
    }

}
