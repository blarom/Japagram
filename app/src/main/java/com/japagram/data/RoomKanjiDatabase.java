package com.japagram.data;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.japagram.utilitiesAndroid.AndroidUtilitiesIO;
import com.japagram.utilitiesCrossPlatform.Globals;
import com.japagram.utilitiesAndroid.AndroidUtilitiesPrefs;
import com.japagram.utilitiesPlatformOverridable.OvUtilsGeneral;

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
                        .databaseBuilder(context.getApplicationContext(), RoomKanjiDatabase.class, "japagram_kanji_room_database")
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

            List<String[]> KanjiDict_Database = AndroidUtilitiesIO.readCSVFile("LineKanjiDictionary - 3000 kanji.csv", context);

            HashMap<String, String[]> kanjiProperties = new HashMap<>();
            for (int i = 0; i < KanjiDict_Database.size(); i++) {
                String key = KanjiDict_Database.get(i)[0];
                if (TextUtils.isEmpty(key)) break;
                String[] readings = KanjiDict_Database.get(i)[1].split("#", -1); //-1 to prevent discarding last empty string
                kanjiProperties.put(key, new String[]{
                        readings.length > 2 ? readings[0].trim() : "",
                        readings.length > 2 ? readings[1].trim() : "",
                        readings.length > 2 ? readings[2].trim() : "",
                        KanjiDict_Database.get(i)[2],
                        KanjiDict_Database.get(i)[3],
                        KanjiDict_Database.get(i)[4]
                });
            }

            List<String[]> RadicalsDatabase = AndroidUtilitiesIO.readCSVFile("LineRadicals - 3000 kanji.csv", context);
            HashMap<String, String> radicalProperties = new HashMap<>();
            for (int i = 0; i < RadicalsDatabase.size(); i++) {
                String key = RadicalsDatabase.get(i)[0];
                if (TextUtils.isEmpty(key)) break;
                radicalProperties.put(key, RadicalsDatabase.get(i)[1]);
            }

            List<List<String>> lineBlocks = AndroidUtilitiesIO.readCSVFileAsLineBlocks("LineCJK_Decomposition - 3000 kanji.csv", 5000, context, true);

            for (List<String> lineBlock : lineBlocks) {
                runInTransaction(() -> {
                    String[] tokens;
                    String[] properties;
                    String key;
                    List<KanjiCharacter> kanjiCharacterList = new ArrayList<>();
                    for (String line : lineBlock) {
                        tokens = line.split("\\|", -1);
                        if (tokens.length > 0) {
                            key = tokens[0];
                            if (TextUtils.isEmpty(key)) break;
                            KanjiCharacter kanjiCharacter = new KanjiCharacter(key, tokens[1], tokens[2]);
                            kanjiCharacter.setKanji(OvUtilsGeneral.convertFromUTF8Index(key));
                            if (kanjiProperties.containsKey(key)) {
                                properties = kanjiProperties.get(key);
                                if (properties != null && properties.length == 6) {
                                    kanjiCharacter.setOnReadings(properties[0]);
                                    kanjiCharacter.setKunReadings(properties[1]);
                                    kanjiCharacter.setNameReadings(properties[2]);
                                    kanjiCharacter.setMeaningsEN(properties[3]);
                                    kanjiCharacter.setMeaningsFR(properties[4]);
                                    kanjiCharacter.setMeaningsES(properties[5]);
                                    kanjiCharacter.setUsedInJapanese(1);
                                }
                            }
                            if (radicalProperties.containsKey(key)) {
                                kanjiCharacter.setRadPlusStrokes(radicalProperties.get(key));
                            }
                            kanjiCharacterList.add(kanjiCharacter);
                        }
                    }
                    if (Looper.myLooper() == null) Looper.prepare();
                    kanjiCharacter().insertAll(kanjiCharacterList);
                });
            }
            String key = "";
            if (kanjiCharacter().count() == 0) {
                key = key.replace(" ", "");
            }
            mFinishedLoadingKanjiCharacterDb = true;
            registerDbWasLoaded(context);
        } else {
            mFinishedLoadingKanjiCharacterDb = true;
        }

        if (kanjiComponent().count() == 0) {

            List<List<String>> lineBlocks = AndroidUtilitiesIO.readCSVFileAsLineBlocks("LineComponents - 3000 kanji.csv", 5000, context, false);

            for (List<String> lineBlock : lineBlocks) {
                runInTransaction(() -> {
                    if (Looper.myLooper() == null) Looper.prepare();
                    //AndroidUtilitiesIO.readCSVFileAndAddToDb("LineComponents - 3000 kanji.csv", context, "kanjiComponentsDb", kanjiComponent());

                    //KanjiComponents are defined as structure -> component -> associatedComponents
                    //NOTE: "full" block split into "full1" and "full2" to prevent memory crashes when requesting all kanji components with structure "full"
                    //The CSV file starts with the "full" block, so "full1" is defined first
                    List<KanjiComponent> kanjiComponents = new ArrayList<>();
                    List<KanjiComponent.AssociatedComponent> associatedComponents = new ArrayList<>();
                    KanjiComponent kanjiComponent = new KanjiComponent("full1");
                    String firstElement;
                    String secondElement;
                    int lineNum = 0;
                    boolean isBlockTitle;
                    String[] tokens;
                    int KANJI_COMPONENTS_FULL1_BLOCK_SIZE = 3000;
                    int MAX_NUM_ELEMENTS_IN_KANJI_COMPONENTS_INSERT_BLOCK = 3;
                    for (String line : lineBlock) {
                        tokens = line.split("\\|", -1);
                        if (tokens.length > 1) {
                            if (TextUtils.isEmpty(tokens[0])) break;
                            firstElement = tokens[0];
                            secondElement = tokens[1];
                            isBlockTitle = secondElement.equals("");
                            if (isBlockTitle || lineNum == KANJI_COMPONENTS_FULL1_BLOCK_SIZE) {
                                kanjiComponent.setAssociatedComponents(associatedComponents);
                                associatedComponents = new ArrayList<>();
                                if (lineNum != 0) {
                                    kanjiComponents.add(kanjiComponent);
                                    if (kanjiComponents.size() % MAX_NUM_ELEMENTS_IN_KANJI_COMPONENTS_INSERT_BLOCK == 0) {
                                        kanjiComponent().insertAll(kanjiComponents);
                                        kanjiComponents = new ArrayList<>();
                                    }
                                }

                                //We define the kanjiComponent's structure here
                                //Since the "full" block has no bockTitle line, it is defined before the loop starts and again when we switch to full2
                                //Thereafter, the structure is defined by firstElement when isBlockTitle==true
                                kanjiComponent = lineNum == KANJI_COMPONENTS_FULL1_BLOCK_SIZE ?
                                        new KanjiComponent("full2") : new KanjiComponent(firstElement);
                            }
                            if (!isBlockTitle) {
                                KanjiComponent.AssociatedComponent associatedComponent = new KanjiComponent.AssociatedComponent();
                                associatedComponent.setComponent(firstElement);
                                associatedComponent.setAssociatedComponents(secondElement);
                                associatedComponents.add(associatedComponent);
                            }
                        }
                        lineNum++;
                    }
                    kanjiComponent.setAssociatedComponents(associatedComponents);
                    kanjiComponents.add(kanjiComponent);
                    kanjiComponent().insertAll(kanjiComponents);
                    Log.i(Globals.DEBUG_TAG, "Loaded Room Kanji Components Database.");
                });
            }
            mFinishedLoadingKanjiComponentDb = true;
            registerDbWasLoaded(context);
        } else {
            mFinishedLoadingKanjiComponentDb = true;
        }
        registerDbWasLoaded(context);
    }
    private void registerDbWasLoaded(Context context) {
        if (mFinishedLoadingKanjiCharacterDb && mFinishedLoadingKanjiComponentDb) {
            AndroidUtilitiesPrefs.setAppPreferenceDbVersionKanji(context, Globals.KANJI_DB_VERSION);
            AndroidUtilitiesPrefs.setAppPreferenceKanjiDatabaseFinishedLoadingFlag(context, true);
        }

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
