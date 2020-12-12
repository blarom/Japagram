package com.japagram.utilitiesPlatformOverridable;

import android.content.Context;

import com.japagram.data.IndexEnglish;
import com.japagram.data.IndexFrench;
import com.japagram.data.IndexKanji;
import com.japagram.data.IndexRomaji;
import com.japagram.data.IndexSpanish;
import com.japagram.data.RoomCentralDatabase;
import com.japagram.data.RoomExtendedDatabase;
import com.japagram.data.RoomNamesDatabase;
import com.japagram.data.Verb;
import com.japagram.data.Word;
import com.japagram.utilitiesCrossPlatform.Globals;

import java.util.ArrayList;
import java.util.List;

public final class OverridableUtilitiesDb {
    public static List<Word> getWordListByWordIds(List<Long> wordIds, Context context, int db) {
        switch (db) {
            case Globals.DB_CENTRAL:
                RoomCentralDatabase roomCentralDatabaseLocal = RoomCentralDatabase.getInstance(context);
                return roomCentralDatabaseLocal.getWordListByWordIds(wordIds);
            case Globals.DB_EXTENDED:
                RoomExtendedDatabase roomExtendedDatabaseLocal = RoomExtendedDatabase.getInstance(context);
                return roomExtendedDatabaseLocal.getWordListByWordIds(wordIds);
            case Globals.DB_NAMES:
                RoomNamesDatabase roomNamesDatabaseLocal = RoomNamesDatabase.getInstance(context);
                return roomNamesDatabaseLocal.getWordListByWordIds(wordIds);
        }
        return new ArrayList<>();
    }
    public static List<Word> getWordsByExactRomajiAndKanjiMatch(String romaji, String kanji, Context context) {
        RoomCentralDatabase roomCentralDatabaseLocal = RoomCentralDatabase.getInstance(context);
        return roomCentralDatabaseLocal.getWordsByExactRomajiAndKanjiMatch(romaji, kanji);
    }

    public static List<Verb> getVerbListByVerbIds(List<Long> ids, Context context) {
        RoomCentralDatabase roomCentralDatabaseLocal = RoomCentralDatabase.getInstance(context);
        return roomCentralDatabaseLocal.getVerbListByVerbIds(ids);
    }
    public static List<Verb> getAllVerbs(Context context) {
        RoomCentralDatabase roomCentralDatabaseLocal = RoomCentralDatabase.getInstance(context);
        return roomCentralDatabaseLocal.getAllVerbs();
    }

    public static IndexRomaji getRomajiIndexForExactWord(String query, Context context) {
        RoomNamesDatabase roomNamesDatabaseLocal = RoomNamesDatabase.getInstance(context);
        return roomNamesDatabaseLocal.getRomajiIndexForExactWord(query);
    }
    public static List<IndexRomaji> getRomajiIndexesListForStartingWord(String query, Context context) {
        RoomNamesDatabase roomNamesDatabaseLocal = RoomNamesDatabase.getInstance(context);
        return roomNamesDatabaseLocal.getRomajiIndexesListForStartingWord(query);
    }
    public static List<IndexRomaji> getRomajiIndexForExactWordsList(List<String> searchQueries, Context context, int db) {
        switch (db) {
            case Globals.DB_CENTRAL:
                RoomCentralDatabase roomCentralDatabaseLocal = RoomCentralDatabase.getInstance(context);
                return roomCentralDatabaseLocal.getRomajiIndexForExactWordsList(searchQueries);
            case Globals.DB_EXTENDED:
                RoomExtendedDatabase roomExtendedDatabaseLocal = RoomExtendedDatabase.getInstance(context);
                return roomExtendedDatabaseLocal.getRomajiIndexForExactWordsList(searchQueries);
        }
        return new ArrayList<>();
    }
    public static List<IndexRomaji> getRomajiIndexesListForStartingWordsList(List<String> searchQueries, Context context, int db) {
        switch (db) {
            case Globals.DB_CENTRAL:
                RoomCentralDatabase roomCentralDatabaseLocal = RoomCentralDatabase.getInstance(context);
                return roomCentralDatabaseLocal.getRomajiIndexesListForStartingWordsList(searchQueries);
            case Globals.DB_EXTENDED:
                RoomExtendedDatabase roomExtendedDatabaseLocal = RoomExtendedDatabase.getInstance(context);
                return roomExtendedDatabaseLocal.getRomajiIndexesListForStartingWordsList(searchQueries);
        }
        return new ArrayList<>();
    }

    public static IndexKanji getKanjiIndexForExactWord(String query, Context context) {
        RoomNamesDatabase roomNamesDatabaseLocal = RoomNamesDatabase.getInstance(context);
        return roomNamesDatabaseLocal.getKanjiIndexForExactWord(query);
    }
    public static List<IndexKanji> getKanjiIndexesListForStartingWord(String query, Context context) {
        RoomNamesDatabase roomNamesDatabaseLocal = RoomNamesDatabase.getInstance(context);
        return roomNamesDatabaseLocal.getKanjiIndexesListForStartingWord(query);
    }
    public static List<IndexKanji> getKanjiIndexForExactWordsList(List<String> searchQueries, Context context, int db) {
        switch (db) {
            case Globals.DB_CENTRAL:
                RoomCentralDatabase roomCentralDatabaseLocal = RoomCentralDatabase.getInstance(context);
                return roomCentralDatabaseLocal.getKanjiIndexForExactWordsList(searchQueries);
            case Globals.DB_EXTENDED:
                RoomExtendedDatabase roomExtendedDatabaseLocal = RoomExtendedDatabase.getInstance(context);
                return roomExtendedDatabaseLocal.getKanjiIndexForExactWordsList(searchQueries);
        }
        return new ArrayList<>();
    }
    public static List<IndexKanji> getKanjiIndexesListForStartingWordsList(List<String> searchQueries, Context context, int db) {
        switch (db) {
            case Globals.DB_CENTRAL:
                RoomCentralDatabase roomCentralDatabaseLocal = RoomCentralDatabase.getInstance(context);
                return roomCentralDatabaseLocal.getKanjiIndexesListForStartingWordsList(searchQueries);
            case Globals.DB_EXTENDED:
                RoomExtendedDatabase roomExtendedDatabaseLocal = RoomExtendedDatabase.getInstance(context);
                return roomExtendedDatabaseLocal.getKanjiIndexesListForStartingWordsList(searchQueries);
        }
        return new ArrayList<>();
    }

    public static List<IndexEnglish> getEnglishIndexForExactWordsList(List<String> searchQueries, Context context, int db) {
        switch (db) {
            case Globals.DB_CENTRAL:
                RoomCentralDatabase roomCentralDatabaseLocal = RoomCentralDatabase.getInstance(context);
                return roomCentralDatabaseLocal.getEnglishIndexForExactWordsList(searchQueries);
            case Globals.DB_EXTENDED:
                RoomExtendedDatabase roomExtendedDatabaseLocal = RoomExtendedDatabase.getInstance(context);
                return roomExtendedDatabaseLocal.getEnglishIndexForExactWordsList(searchQueries);
        }
        return new ArrayList<>();
    }
    public static List<IndexEnglish> getEnglishIndexesListForStartingWordsList(List<String> searchQueries, Context context, int db) {
        switch (db) {
            case Globals.DB_CENTRAL:
                RoomCentralDatabase roomCentralDatabaseLocal = RoomCentralDatabase.getInstance(context);
                return roomCentralDatabaseLocal.getEnglishIndexesListForStartingWordsList(searchQueries);
            case Globals.DB_EXTENDED:
                RoomExtendedDatabase roomExtendedDatabaseLocal = RoomExtendedDatabase.getInstance(context);
                return roomExtendedDatabaseLocal.getEnglishIndexesListForStartingWordsList(searchQueries);
        }
        return new ArrayList<>();
    }

    public static List<IndexFrench> getFrenchIndexForExactWordsList(List<String> searchQueries, Context context, int db) {
        switch (db) {
            case Globals.DB_CENTRAL:
                RoomCentralDatabase roomCentralDatabaseLocal = RoomCentralDatabase.getInstance(context);
                return roomCentralDatabaseLocal.getFrenchIndexForExactWordsList(searchQueries);
            case Globals.DB_EXTENDED:
                RoomExtendedDatabase roomExtendedDatabaseLocal = RoomExtendedDatabase.getInstance(context);
                return roomExtendedDatabaseLocal.getFrenchIndexForExactWordsList(searchQueries);
        }
        return new ArrayList<>();
    }
    public static List<IndexFrench> getFrenchIndexesListForStartingWordsList(List<String> searchQueries, Context context, int db) {
        switch (db) {
            case Globals.DB_CENTRAL:
                RoomCentralDatabase roomCentralDatabaseLocal = RoomCentralDatabase.getInstance(context);
                return roomCentralDatabaseLocal.getFrenchIndexesListForStartingWordsList(searchQueries);
            case Globals.DB_EXTENDED:
                RoomExtendedDatabase roomExtendedDatabaseLocal = RoomExtendedDatabase.getInstance(context);
                return roomExtendedDatabaseLocal.getFrenchIndexesListForStartingWordsList(searchQueries);
        }
        return new ArrayList<>();
    }

    public static List<IndexSpanish> getSpanishIndexForExactWordsList(List<String> searchQueries, Context context, int db) {
        switch (db) {
            case Globals.DB_CENTRAL:
                RoomCentralDatabase roomCentralDatabaseLocal = RoomCentralDatabase.getInstance(context);
                return roomCentralDatabaseLocal.getSpanishIndexForExactWordsList(searchQueries);
            case Globals.DB_EXTENDED:
                RoomExtendedDatabase roomExtendedDatabaseLocal = RoomExtendedDatabase.getInstance(context);
                return roomExtendedDatabaseLocal.getSpanishIndexForExactWordsList(searchQueries);
        }
        return new ArrayList<>();
    }
    public static List<IndexSpanish> getSpanishIndexesListForStartingWordsList(List<String> searchQueries, Context context, int db) {
        switch (db) {
            case Globals.DB_CENTRAL:
                RoomCentralDatabase roomCentralDatabaseLocal = RoomCentralDatabase.getInstance(context);
                return roomCentralDatabaseLocal.getSpanishIndexesListForStartingWordsList(searchQueries);
            case Globals.DB_EXTENDED:
                RoomExtendedDatabase roomExtendedDatabaseLocal = RoomExtendedDatabase.getInstance(context);
                return roomExtendedDatabaseLocal.getSpanishIndexesListForStartingWordsList(searchQueries);
        }
        return new ArrayList<>();
    }

    public static void updateVerb(Verb verb, Context context) {
        RoomCentralDatabase roomCentralDatabaseLocal = RoomCentralDatabase.getInstance(context);
        roomCentralDatabaseLocal.updateVerb(verb);
    }
    public static void updateVerbByVerbIdWithParams(long matchingVerbId, String activeLatinRoot, String activeKanjiRoot, String activeAltSpelling, Context context) {
        RoomCentralDatabase roomCentralDatabaseLocal = RoomCentralDatabase.getInstance(context);
        roomCentralDatabaseLocal.updateVerbByVerbIdWithParams(
                matchingVerbId,
                activeLatinRoot,
                activeKanjiRoot,
                activeAltSpelling
        );
    }
}
