package com.japagram.resources;

import android.content.Context;

import com.japagram.data.RoomCentralDatabase;
import com.japagram.data.RoomExtendedDatabase;
import com.japagram.data.RoomNamesDatabase;
import com.japagram.data.Verb;
import com.japagram.data.Word;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class UtilitiesDbAccess {
    public static List<Word> getWordListByWordIdsFromCentralDb(List<Long> mMatchingWordIds, Context context) {
        RoomCentralDatabase roomCentralDatabaseLocal = RoomCentralDatabase.getInstance(context);
        return roomCentralDatabaseLocal.getWordListByWordIds(mMatchingWordIds);
    }
    public static List<Word> getWordListByWordIdsFromExtendedDb(List<Long> matchingWordIdsExtended, Context context) {
        RoomExtendedDatabase roomExtendedDatabaseLocal = RoomExtendedDatabase.getInstance(context);
        return roomExtendedDatabaseLocal.getWordListByWordIds(matchingWordIdsExtended);
    }
    public static List<Word> getWordListByWordIdsFromNamesDb(List<Long> matchingWordIdsNames, Context context) {
        RoomNamesDatabase roomNamesDatabaseLocal = RoomNamesDatabase.getInstance(context);
        return roomNamesDatabaseLocal.getWordListByWordIds(matchingWordIdsNames);
    }
    public static List<Word> getWordsByExactRomajiAndKanjiMatch(String romaji, String kanji, Context context) {
        RoomCentralDatabase roomCentralDatabaseLocal = RoomCentralDatabase.getInstance(context);
        return roomCentralDatabaseLocal.getWordsByExactRomajiAndKanjiMatch(romaji, kanji);
    }
    public static void updateVerb(Verb verb, Context context) {
        RoomCentralDatabase roomCentralDatabaseLocal = RoomCentralDatabase.getInstance(context);
        roomCentralDatabaseLocal.updateVerb(verb);
    }
    public static List<Verb> getVerbListByVerbIds(List<Long> ids, Context context) {
        RoomCentralDatabase roomCentralDatabaseLocal = RoomCentralDatabase.getInstance(context);
        return roomCentralDatabaseLocal.getVerbListByVerbIds(ids);
    }
    public static List<Verb> getAllVerbs(Context context) {
        RoomCentralDatabase roomCentralDatabaseLocal = RoomCentralDatabase.getInstance(context);
        return roomCentralDatabaseLocal.getAllVerbs();
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
