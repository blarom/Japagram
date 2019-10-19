package com.japagram.data;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.japagram.resources.GlobalConstants;
import com.japagram.resources.Utilities;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Word.class,
                        Verb.class,
                        IndexRomaji.class,
                        IndexEnglish.class,
                        IndexFrench.class,
                        IndexSpanish.class,
                        IndexKanji.class},
                    version = GlobalConstants.CENTRAL_DB_VERSION,
                    exportSchema = false)
public abstract class RoomCentralDatabase extends RoomDatabase {
    //Adapted from: https://github.com/googlesamples/android-architecture-components/blob/master/PersistenceContentProviderSample/app/src/main/java/com/example/android/contentprovidersample/data/SampleDatabase.java

    //return The DAO for the tables
    @SuppressWarnings("WeakerAccess")
    public abstract WordDao word();
    public abstract VerbDao verb();
    public abstract IndexKanjiDao indexKanji();
    public abstract IndexRomajiDao indexRomaji();
    public abstract IndexEnglishDao indexEnglish();
    public abstract IndexFrenchDao indexFrench();
    public abstract IndexSpanishDao indexSpanish();


    //Gets the singleton instance of SampleDatabase
    private static RoomCentralDatabase sInstance;
    public static synchronized RoomCentralDatabase getInstance(Context context) {
        if (sInstance == null) {
            try {
                //Use this clause if you want to upgrade the database without destroying the previous database. Here, FROM_1_TO_2 is never satisfied since database version > 2.
                sInstance = Room
                        .databaseBuilder(context.getApplicationContext(), RoomCentralDatabase.class, "japagram_central_room_database")
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
            } catch (Exception e) {
                //If migrations weren't set up from version X to verion X+1, do a destructive migration (rebuilds the db from sratch using the assets)
                e.printStackTrace();
                sInstance = Room
                        .databaseBuilder(context.getApplicationContext(), RoomCentralDatabase.class, "japagram_central_room_database")
                        .fallbackToDestructiveMigration()
                        .enableMultiInstanceInvalidation()
                        .build();

                sInstance.populateDatabases(context);
            }
        }
        return sInstance;
    }

    private void populateDatabases(Context context) {

        if (word().count() == 0) {
            Utilities.setAppPreferenceWordVerbDatabasesFinishedLoadingFlag(context, false);
            beginTransaction();
            try {
                if (Looper.myLooper() == null) Looper.prepare();
                loadCentralDatabaseIntoRoomDb(context);
                Log.i("Diagnosis Time", "Loaded Central Words & Verbs Database.");
                setTransactionSuccessful();
            } finally {
                endTransaction();
            }
        }
        if (this.indexEnglish().count() == 0) {
            beginTransaction();
            try {
                if (Looper.myLooper() == null) Looper.prepare();
                Utilities.readCSVFileAndAddToDb("LineGrammarSortedIndexRomaji - 3000 kanji.csv", context, "indexRomaji", indexRomaji());
                Utilities.readCSVFileAndAddToDb("LineGrammarSortedIndexLatinEN - 3000 kanji.csv", context, "indexEnglish", indexEnglish());
                Utilities.readCSVFileAndAddToDb("LineGrammarSortedIndexLatinFR - 3000 kanji.csv", context, "indexFrench", indexFrench());
                Utilities.readCSVFileAndAddToDb("LineGrammarSortedIndexLatinES - 3000 kanji.csv", context, "indexSpanish", indexSpanish());
                Utilities.readCSVFileAndAddToDb("LineGrammarSortedIndexKanji - 3000 kanji.csv", context, "indexKanji", indexKanji());
                Log.i("Diagnosis Time", "Loaded Central Indexes Database.");
                setTransactionSuccessful();
            } finally {
                endTransaction();
            }
            Utilities.setAppPreferenceDbVersionCentral(context, GlobalConstants.CENTRAL_DB_VERSION);
        }
        Utilities.setAppPreferenceWordVerbDatabasesFinishedLoadingFlag(context, true);

    }
    private void loadCentralDatabaseIntoRoomDb(Context context) {

        // Import the excel sheets (csv format)
        List<String[]> centralDatabase 		    = new ArrayList<>();
        List<String[]> typesDatabase            = Utilities.readCSVFile("LineTypes - 3000 kanji.csv", context);
        List<String[]> grammarDatabase          = Utilities.readCSVFile("LineGrammar - 3000 kanji.csv", context);
        List<String[]> verbsDatabase     	    = Utilities.readCSVFile("LineVerbsForGrammar - 3000 kanji.csv", context);
        List<String[]> meaningsENDatabase       = Utilities.readCSVFile("LineMeanings - 3000 kanji.csv", context);
        List<String[]> meaningsFRDatabase       = Utilities.readCSVFile("LineMeaningsFR - 3000 kanji.csv", context);
        List<String[]> meaningsESDatabase       = Utilities.readCSVFile("LineMeaningsES - 3000 kanji.csv", context);
        List<String[]> multExplENDatabase       = Utilities.readCSVFile("LineMultExplEN - 3000 kanji.csv", context);
        List<String[]> multExplFRDatabase       = Utilities.readCSVFile("LineMultExplFR - 3000 kanji.csv", context);
        List<String[]> multExplESDatabase       = Utilities.readCSVFile("LineMultExplES - 3000 kanji.csv", context);
        List<String[]> examplesDatabase         = Utilities.readCSVFile("LineExamples - 3000 kanji.csv", context);

        //Removing the titles row in each sheet
        typesDatabase.remove(0);
        grammarDatabase.remove(0);
        verbsDatabase.remove(0);

        //Adding the sheets to the central database
        centralDatabase.addAll(typesDatabase);
        centralDatabase.addAll(grammarDatabase);
        centralDatabase.addAll(verbsDatabase);

        //Checking that there were no accidental line breaks when building the database
        Utilities.checkDatabaseStructure(verbsDatabase, "Verbs Database", Utilities.NUM_COLUMNS_IN_WORDS_CSV_SHEETS);
        Utilities.checkDatabaseStructure(centralDatabase, "Central Database", Utilities.NUM_COLUMNS_IN_WORDS_CSV_SHEETS);
        Utilities.checkDatabaseStructure(meaningsENDatabase, "Meanings Database", Utilities.NUM_COLUMNS_IN_WORDS_CSV_SHEETS);
        Utilities.checkDatabaseStructure(meaningsFRDatabase, "MeaningsFR Database", Utilities.NUM_COLUMNS_IN_WORDS_CSV_SHEETS);
        Utilities.checkDatabaseStructure(meaningsESDatabase, "MeaningsES Database", Utilities.NUM_COLUMNS_IN_WORDS_CSV_SHEETS);
        Utilities.checkDatabaseStructure(multExplENDatabase, "Explanations Database", Utilities.NUM_COLUMNS_IN_WORDS_CSV_SHEETS);
        Utilities.checkDatabaseStructure(multExplFRDatabase, "Explanations Database", Utilities.NUM_COLUMNS_IN_WORDS_CSV_SHEETS);
        Utilities.checkDatabaseStructure(multExplESDatabase, "Explanations Database", Utilities.NUM_COLUMNS_IN_WORDS_CSV_SHEETS);
        Utilities.checkDatabaseStructure(examplesDatabase, "Examples Database", Utilities.NUM_COLUMNS_IN_WORDS_CSV_SHEETS);

        List<Word> wordList = new ArrayList<>();
        for (int i=1; i<centralDatabase.size(); i++) {
            if (centralDatabase.get(i)[0].equals("")) break;
            Word word = Utilities.createWordFromCsvDatabases(centralDatabase,
                    meaningsENDatabase, meaningsFRDatabase, meaningsESDatabase,
                    multExplENDatabase, multExplFRDatabase, multExplESDatabase,
                    examplesDatabase, i);
            wordList.add(word);
            if (wordList.size() % 2000 == 0) {
                word().insertAll(wordList);
                wordList = new ArrayList<>();
            }
        }
        word().insertAll(wordList);
        Log.i("Diagnosis Time","Loaded Words Database.");

        List<Verb> verbList = new ArrayList<>();
        for (int i=1; i<verbsDatabase.size(); i++) {
            if (verbsDatabase.get(i)[0].equals("")) break;
            Verb verb = Utilities.createVerbFromCsvDatabase(verbsDatabase, meaningsENDatabase, i);
            verbList.add(verb);
            if (verbList.size() % 2000 == 0) {
                verb().insertAll(verbList);
                verbList = new ArrayList<>();
            }
        }
        verb().insertAll(verbList);
        Log.i("Diagnosis Time","Loaded Verbs Database.");
    }

    //Switches the internal implementation with an empty in-memory database
    @VisibleForTesting
    public static void switchToInMemory(Context context) {
        sInstance = Room
                .inMemoryDatabaseBuilder(context.getApplicationContext(), RoomCentralDatabase.class)
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
        List<IndexRomaji> preparedSqlElements = new ArrayList<>();
        for (String word : words) {
            preparedSqlElements.addAll(this.indexRomaji().getIndexByStartingQuery(word));
        }
        return preparedSqlElements;
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
        return indexKanji().getKanjiIndexByExactUTF8Query(query);
    }
    public List<IndexKanji> getKanjiIndexesListForStartingWord(String query) {
        //return indexKanji().getKanjiIndexByStartingUTF8Query(query);
        return indexKanji().getKanjiIndexByStartingQuery(query);
    }

    public List<Verb> getVerbListByVerbIds(List<Long> verbIds) {
        return verb().getVerbListByVerbIds(verbIds);
    }
    public Verb getVerbByVerbId(long verbId) {
        return verb().getVerbByVerbId(verbId);
    }
    public void updateVerbByVerbIdWithParams(long verbId, String activeLatinRoot, String activeKanjiRoot, String activeAltSpelling) {
        verb().updateVerbByVerbIdWithParameters(verbId, activeLatinRoot, activeKanjiRoot, activeAltSpelling);
    }
    public void updateVerb(Verb verb) {
        verb().update(verb);
    }
    public List<Verb> getVerbsByExactRomajiMatch(String query) {
        return verb().getVerbByExactRomajiMatch(query);
    }
    public List<Verb> getVerbsByKanjiQuery(String query) {
        return verb().getVerbByExactKanjiQueryMatch(query);
    }
    public List<Verb> getAllVerbs() {
        return verb().getAllVerbs();
    }

}
