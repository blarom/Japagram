package com.japagram.resources;

import android.content.Context;
import android.util.Log;

import com.japagram.data.InputQuery;
import com.japagram.data.RoomCentralDatabase;
import com.japagram.data.RoomExtendedDatabase;
import com.japagram.data.RoomNamesDatabase;
import com.japagram.data.Word;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class UtilitiesDictSearchAsyncTask {
    public static List<Word> getMatchingWords(InputQuery mQuery, String language, @NotNull WeakReference<Context> contextRef, boolean mShowNames) {
        List<Word> localMatchingWordsList;
        boolean finishedLoadingExtendedDb = UtilitiesPrefs.getAppPreferenceExtendedDatabasesFinishedLoadingFlag(contextRef.get());
        boolean finishedLoadingNamesDb = UtilitiesPrefs.getAppPreferenceNamesDatabasesFinishedLoadingFlag(contextRef.get());

        Object[] matchingWordIds = UtilitiesDb.getMatchingWordIdsAndDoBasicFiltering(mQuery, language, mShowNames, contextRef.get());
        List<Long> matchingWordIdsCentral = (List<Long>) matchingWordIds[0];
        List<Long> matchingWordIdsExtended = (List<Long>) matchingWordIds[1];
        List<Long> matchingWordIdsNames = (List<Long>) matchingWordIds[2];
        Log.i(Globals.DEBUG_TAG, "LocalSearchAsyncTask - Got matching word ids");

        localMatchingWordsList = RoomCentralDatabase.getInstance(contextRef.get()).getWordListByWordIds(matchingWordIdsCentral);
        Log.i(Globals.DEBUG_TAG, "LocalSearchAsyncTask - Got matching words");

        RoomExtendedDatabase roomExtendedDatabase = finishedLoadingExtendedDb? RoomExtendedDatabase.getInstance(contextRef.get()) : null;
        if (roomExtendedDatabase != null) localMatchingWordsList.addAll(roomExtendedDatabase.getWordListByWordIds(matchingWordIdsExtended));
        Log.i(Globals.DEBUG_TAG, "LocalSearchAsyncTask - Added matching extended words");

        RoomNamesDatabase roomNamesDatabase = (finishedLoadingNamesDb && mShowNames)? RoomNamesDatabase.getInstance(contextRef.get()) : null;
        if (roomNamesDatabase != null) {
            List<Word> originalNames = roomNamesDatabase.getWordListByWordIds(matchingWordIdsNames);
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
