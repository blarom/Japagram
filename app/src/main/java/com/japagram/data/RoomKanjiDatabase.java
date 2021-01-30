package com.japagram.data;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.japagram.utilitiesAndroid.AndroidUtilitiesIO;
import com.japagram.utilitiesCrossPlatform.Globals;
import com.japagram.utilitiesAndroid.AndroidUtilitiesPrefs;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {KanjiCharacter.class, KanjiComponent.class},
        version = Globals.KANJI_DB_VERSION,
        exportSchema = false)
public abstract class RoomKanjiDatabase extends RoomDatabase {
    //Adapted from: https://github.com/googlesamples/android-architecture-components/blob/master/PersistenceContentProviderSample/app/src/main/java/com/example/android/contentprovidersample/data/SampleDatabase.java

    //return The DAO for the tables
    @SuppressWarnings("WeakerAccess")
    public abstract KanjiCharacterDao kanjiCharacter();
    public abstract KanjiComponentDao kanjiComponent();


    //Gets the singleton instance of SampleDatabase
    private static RoomKanjiDatabase sInstance;
    public static synchronized RoomKanjiDatabase getInstance(Context context) {
        if (sInstance == null) {
            try {
                if (AndroidUtilitiesPrefs.getAppPreferenceDbVersionKanji(context) != Globals.KANJI_DB_VERSION) {
                    throw new Exception();
                }
                //Use this clause if you want to upgrade the database without destroying the previous database. Here, FROM_1_TO_2 is never satisfied since database version > 2.
                sInstance = Room
                        .databaseBuilder(context.getApplicationContext(), RoomKanjiDatabase.class, "japanese_toolbox_kanji_room_database")
                        .addMigrations(FROM_1_TO_2)
                        .enableMultiInstanceInvalidation()
                        .build();

                sInstance.populateDatabases(context);
            } catch (Exception e) {
                //If migrations weren't set up from version X to verion X+1, do a destructive migration (rebuilds the db from sratch using the assets)
                e.printStackTrace();
                sInstance = Room
                        .databaseBuilder(context.getApplicationContext(), RoomKanjiDatabase.class, "japagram_kanji_room_database")
                        .fallbackToDestructiveMigration()
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
            }
        }
        return sInstance;
    }

    boolean mFinishedLoadingKanjiCharacterDb = false;
    boolean mFinishedLoadingKanjiComponentDb = false;
    private void populateDatabases(Context context) {

        AndroidUtilitiesPrefs.setAppPreferenceKanjiDatabaseFinishedLoadingFlag(context, false);
        if (kanjiCharacter().count() == 0) {
            kanjiCharacter().nukeTable();
            runInTransaction(() -> {
                if (Looper.myLooper() == null) Looper.prepare();
                loadKanjiCharactersIntoRoomDb(context);
                Log.i(Globals.DEBUG_TAG, "Loaded Room Kanji Characters Database.");
                mFinishedLoadingKanjiCharacterDb = true;
                AndroidUtilitiesIO.readCSVFileAndAddToDb("LineComponents - 3000 kanji.csv", context, "kanjiComponentsDb", kanjiComponent());
                Log.i(Globals.DEBUG_TAG, "Loaded Room Kanji Components Database.");
                mFinishedLoadingKanjiComponentDb = true;
                registerDbWasLoaded(context);
            });
        } else mFinishedLoadingKanjiCharacterDb = true;

        if (kanjiComponent().count() == 0) {
            runInTransaction(() -> {
                if (Looper.myLooper() == null) Looper.prepare();
                registerDbWasLoaded(context);
            });
        } else mFinishedLoadingKanjiComponentDb = true;
        registerDbWasLoaded(context);
    }
    private void registerDbWasLoaded(Context context) {
        if (mFinishedLoadingKanjiCharacterDb && mFinishedLoadingKanjiComponentDb) {
            AndroidUtilitiesPrefs.setAppPreferenceDbVersionKanji(context, Globals.KANJI_DB_VERSION);
            AndroidUtilitiesPrefs.setAppPreferenceKanjiDatabaseFinishedLoadingFlag(context, true);
        }

    }
    private void loadKanjiCharactersIntoRoomDb(Context context) {

        AndroidUtilitiesIO.readCSVFileAndAddToDb("LineCJK_Decomposition - 3000 kanji.csv", context, "kanjiCharactersDb", kanjiCharacter());

        Log.i("Diagnosis Time","Loaded Kanji Characters Database.");
    }

    //Switches the internal implementation with an empty in-memory database
    @VisibleForTesting
    public static void switchToInMemory(@NotNull Context context) {
        sInstance = Room
                .inMemoryDatabaseBuilder(context.getApplicationContext(), RoomKanjiDatabase.class)
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

    public List<KanjiCharacter> getKanjiCharactersByHexIdList(List<String> queryHexList) {
        return kanjiCharacter().getKanjiCharactersByHexIdList(queryHexList);
    }
    public List<KanjiCharacter> getKanjiCharactersByDescriptor(String query) {
        return kanjiCharacter().getKanjiCharactersByDescriptor(query);
    }
    public List<KanjiCharacter> getKanjiCharactersByMeaningEN(String query) {
        return kanjiCharacter().getKanjiCharactersByMeaningEN(query);
    }
    public List<KanjiCharacter> getKanjiCharactersByMeaningFR(String query) {
        return kanjiCharacter().getKanjiCharactersByMeaningFR(query);
    }
    public List<KanjiCharacter> getKanjiCharactersByMeaningES(String query) {
        return kanjiCharacter().getKanjiCharactersByMeaningES(query);
    }
    public List<KanjiCharacter> getKanjiCharactersByKanaDescriptor(String query) {
        return kanjiCharacter().getKanjiCharactersByKanaDescriptor(query);
    }
    public List<String> getAllKanjis(boolean usedInJapanese) {
        return usedInJapanese? kanjiCharacter().getAllKanjisUsedInJapanese() : kanjiCharacter().getAllKanjis();
    }

    public List<KanjiCharacter> getAllKanjiCharacters() {
        return kanjiCharacter().getAllKanjiCharacters();
    }
    public List<KanjiCharacter> getKanjiCharactersUsedInJapanese() {
        return kanjiCharacter().getKanjiCharactersUsedInJapanese();
    }
    public KanjiCharacter getKanjiCharacterByHexId(String queryHex) {
        return kanjiCharacter().getKanjiCharacterByHexId(queryHex);
    }

    public List<KanjiComponent> getKanjiComponentsByStructureName(String structure) {
        return kanjiComponent().getKanjiComponentsByStructure(structure);
    }
    public KanjiComponent getKanjiComponentsById(long id) {
        return kanjiComponent().getKanjiComponentById(id);
    }
    public List<KanjiComponent> getAllKanjiComponents() {
        return kanjiComponent().getAllKanjiComponents();
    }

}
