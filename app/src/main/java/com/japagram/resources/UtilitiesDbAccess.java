package com.japagram.resources;

import android.content.Context;

import com.japagram.data.RoomCentralDatabase;
import com.japagram.data.Verb;
import com.japagram.data.Word;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class UtilitiesDbAccess {
    public static List<Word> getWordListByWordIds(@NotNull RoomCentralDatabase mRoomCentralDatabase, List<Long> mMatchingWordIds) {
        return mRoomCentralDatabase.getWordListByWordIds(mMatchingWordIds);
    }
    public static List<Word> getWordsByExactRomajiAndKanjiMatch(@NotNull RoomCentralDatabase mRoomCentralDatabase, String romaji, String kanji) {
        return mRoomCentralDatabase.getWordsByExactRomajiAndKanjiMatch(romaji, kanji);
    }
    public static void updateVerb(@NotNull RoomCentralDatabase mRoomCentralDatabase, Verb verb) {
        mRoomCentralDatabase.updateVerb(verb);
    }
    public static List<Verb> getVerbListByVerbIds(@NotNull RoomCentralDatabase mRoomCentralDatabase, List<Long> ids) {
        return mRoomCentralDatabase.getVerbListByVerbIds(ids);
    }
    public static List<Verb> getAllVerbs(@NotNull RoomCentralDatabase mRoomCentralDatabase) {
        return mRoomCentralDatabase.getAllVerbs();
    }
    public static void updateVerbByVerbIdWithParams(@NotNull RoomCentralDatabase mRoomCentralDatabase, long matchingVerbId, String activeLatinRoot, String activeKanjiRoot, String activeAltSpelling) {
        mRoomCentralDatabase.updateVerbByVerbIdWithParams(
                matchingVerbId,
                activeLatinRoot,
                activeKanjiRoot,
                activeAltSpelling
        );
    }
}
