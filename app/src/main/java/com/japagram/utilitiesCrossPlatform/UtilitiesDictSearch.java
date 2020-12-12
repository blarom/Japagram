package com.japagram.utilitiesCrossPlatform;

import android.content.Context;

import com.japagram.data.InputQuery;
import com.japagram.data.Word;
import com.japagram.utilitiesPlatformOverridable.OverridableUtilitiesDb;
import com.japagram.utilitiesPlatformOverridable.OverridableUtilitiesGeneral;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class UtilitiesDictSearch {
    public static List<Word> getMatchingWords(boolean roomExtendedDbIsAvailable,
                                              boolean roomNamesDatabaseIsAvailable,
                                              boolean roomNamesDatabasesFinishedLoading,
                                              InputQuery mQuery,
                                              String language,
                                              @NotNull Context context,
                                              boolean mShowNames) {
        List<Word> localMatchingWordsList;

        Object[] matchingWordIds = UtilitiesDb.getMatchingWordIdsAndDoBasicFiltering(
                roomExtendedDbIsAvailable,
                roomNamesDatabaseIsAvailable,
                roomNamesDatabasesFinishedLoading,
                mQuery,
                language,
                mShowNames,
                context);

        List<Long> matchingWordIdsCentral = (List<Long>) matchingWordIds[0];
        List<Long> matchingWordIdsExtended = (List<Long>) matchingWordIds[1];
        List<Long> matchingWordIdsNames = (List<Long>) matchingWordIds[2];
        OverridableUtilitiesGeneral.printLog(Globals.DEBUG_TAG, "LocalSearchAsyncTask - Got matching word ids");

        localMatchingWordsList = OverridableUtilitiesDb.getWordListByWordIds(matchingWordIdsCentral, context, Globals.DB_CENTRAL);
        OverridableUtilitiesGeneral.printLog(Globals.DEBUG_TAG, "LocalSearchAsyncTask - Got matching words");

        if (roomExtendedDbIsAvailable) localMatchingWordsList.addAll(OverridableUtilitiesDb.getWordListByWordIds(matchingWordIdsExtended, context, Globals.DB_EXTENDED));
        OverridableUtilitiesGeneral.printLog(Globals.DEBUG_TAG, "LocalSearchAsyncTask - Added matching extended words");

        if (roomNamesDatabaseIsAvailable) {
        List<Word> originalNames = OverridableUtilitiesDb.getWordListByWordIds(matchingWordIdsNames, context, Globals.DB_NAMES);
            OverridableUtilitiesGeneral.printLog(Globals.DEBUG_TAG, "LocalSearchAsyncTask - Added matching names");
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
