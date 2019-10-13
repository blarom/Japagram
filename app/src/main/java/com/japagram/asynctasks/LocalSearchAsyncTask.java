package com.japagram.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.japagram.data.RoomCentralDatabase;
import com.japagram.data.RoomExtendedDatabase;
import com.japagram.data.RoomNamesDatabase;
import com.japagram.data.Word;
import com.japagram.resources.LocaleHelper;
import com.japagram.resources.Utilities;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class LocalSearchAsyncTask extends AsyncTask<Void, Void, List<Word>> {

    private final boolean mShowNames;
    private WeakReference<Context> contextRef;
    private final String mQuery;
    public LocalDictSearchAsyncResponseHandler listener;

    public LocalSearchAsyncTask(Context context, String query, LocalDictSearchAsyncResponseHandler listener, boolean mShowNames) {
        contextRef = new WeakReference<>(context);
        this.mQuery = query;
        this.listener = listener;
        this.mShowNames = mShowNames;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }
    @Override
    protected List<Word> doInBackground(Void... voids) {

        List<Word> localMatchingWordsList = new ArrayList<>();
        if (!TextUtils.isEmpty(mQuery)) {
            boolean completeWithNamesIfNoResultsEvenIfDontShowNames = Utilities.getAppPreferenceCompleteWithNames(contextRef.get());
            RoomCentralDatabase roomCentralDatabase = RoomCentralDatabase.getInstance(contextRef.get());
            RoomExtendedDatabase roomExtendedDatabase = Utilities.getAppPreferenceExtendedDatabasesFinishedLoadingFlag(contextRef.get())?
                    RoomExtendedDatabase.getInstance(contextRef.get()) : null;
            RoomNamesDatabase roomNamesDatabase = (
                    Utilities.getAppPreferenceNamesDatabasesFinishedLoadingFlag(contextRef.get())
                    && (mShowNames || completeWithNamesIfNoResultsEvenIfDontShowNames))? RoomNamesDatabase.getInstance(contextRef.get()) : null;
            String language = LocaleHelper.getLanguage(contextRef.get());
            Object[] matchingWordIds = Utilities.getMatchingWordIdsAndDoBasicFiltering(mQuery,
                    roomCentralDatabase, roomExtendedDatabase, roomNamesDatabase, language, mShowNames, completeWithNamesIfNoResultsEvenIfDontShowNames);
            List<Long> matchingWordIdsCentral = (List<Long>) matchingWordIds[0];
            List<Long> matchingWordIdsExtended = (List<Long>) matchingWordIds[1];
            List<Long> matchingWordIdsNames = (List<Long>) matchingWordIds[2];
            localMatchingWordsList = roomCentralDatabase.getWordListByWordIds(matchingWordIdsCentral);
            if (roomExtendedDatabase != null) localMatchingWordsList.addAll(roomExtendedDatabase.getWordListByWordIds(matchingWordIdsExtended));
            if (roomNamesDatabase != null) {
                List<Word> originalNames = roomNamesDatabase.getWordListByWordIds(matchingWordIdsNames);
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

        }

        return localMatchingWordsList;
    }

    @Override
    protected void onPostExecute(List<Word> words) {
        super.onPostExecute(words);
        listener.onLocalDictSearchAsyncTaskResultFound(words);
    }

    public interface LocalDictSearchAsyncResponseHandler {
        void onLocalDictSearchAsyncTaskResultFound(List<Word> text);
    }
}
