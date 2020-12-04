package com.japagram.resources;

import android.content.Context;
import android.util.Log;

import com.japagram.data.InputQuery;
import com.japagram.data.RoomCentralDatabase;
import com.japagram.data.RoomExtendedDatabase;
import com.japagram.data.RoomNamesDatabase;
import com.japagram.data.Word;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class UtilitiesDictSearchAsyncTask {
    public static List<Word> getMatchingWords(RoomCentralDatabase mRoomCentralDatabase,
                                              RoomExtendedDatabase roomExtendedDatabase,
                                              RoomNamesDatabase roomNamesDatabase,
                                              InputQuery mQuery,
                                              String language,
                                              @NotNull Context context,
                                              boolean mShowNames) {
        List<Word> localMatchingWordsList;

        Object[] matchingWordIds = UtilitiesDb.getMatchingWordIdsAndDoBasicFiltering(mQuery, language, mShowNames, context);
        List<Long> matchingWordIdsCentral = (List<Long>) matchingWordIds[0];
        List<Long> matchingWordIdsExtended = (List<Long>) matchingWordIds[1];
        List<Long> matchingWordIdsNames = (List<Long>) matchingWordIds[2];
        Log.i(Globals.DEBUG_TAG, "LocalSearchAsyncTask - Got matching word ids");

        localMatchingWordsList = UtilitiesDbAccess.getWordListByWordIdsFromCentralDb(mRoomCentralDatabase, matchingWordIdsCentral);
        Log.i(Globals.DEBUG_TAG, "LocalSearchAsyncTask - Got matching words");

        if (roomExtendedDatabase != null) localMatchingWordsList.addAll(UtilitiesDbAccess.getWordListByWordIdsFromExtendedDb(roomExtendedDatabase,matchingWordIdsExtended));
        Log.i(Globals.DEBUG_TAG, "LocalSearchAsyncTask - Added matching extended words");

        if (roomNamesDatabase != null) {
            List<Word> originalNames = UtilitiesDbAccess.getWordListByWordIdsFromNamesDb(roomNamesDatabase, matchingWordIdsNames);
            Log.i(Globals.DEBUG_TAG, "LocalSearchAsyncTask - Added matching names");
            List<Word> condensedNames = new ArrayList<>();
            boolean foundName;
            for (Word name : originalNames) {
                foundName = false;
                for (Word condensedName : condensedNames) {
                    if (name.getRomaji().equals(condensedName.getRomaji())
                            && name.getMeaningsEN().get(0).getType().equals(condensedName.getMeaningsEN().get(0).getType())) {
                        foundName = true;
                        condensedName.setKanji(condensedName.getKanji() + "・" + name.getKanji());
                        break;
                    }
                }
                if (!foundName) {
                    condensedNames.add(name);
                }
            }
            localMatchingWordsList.addAll(condensedNames);
        }
        return localMatchingWordsList;
    }
}
