package com.japagram.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.japagram.data.InputQuery;
import com.japagram.data.RoomCentralDatabase;
import com.japagram.data.RoomExtendedDatabase;
import com.japagram.data.RoomNamesDatabase;
import com.japagram.data.Word;
import com.japagram.resources.Globals;
import com.japagram.resources.LocaleHelper;
import com.japagram.resources.UtilitiesDb;
import com.japagram.resources.UtilitiesPrefs;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class LocalSearchAsyncTask extends AsyncTask<Void, Void, List<Word>> {

    private final boolean mShowNames;
    private WeakReference<Context> contextRef;
    private final InputQuery mQuery;
    public LocalDictSearchAsyncResponseHandler listener;

    public LocalSearchAsyncTask(Context context, InputQuery query, LocalDictSearchAsyncResponseHandler listener, boolean mShowNames) {
        contextRef = new WeakReference<>(context);
        this.mQuery = query;
        this.listener = listener;
        this.mShowNames = mShowNames;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }
    @Override @SuppressWarnings("unchecked")
    protected List<Word> doInBackground(Void... voids) {

        List<Word> localMatchingWordsList = new ArrayList<>();
        if (!mQuery.isEmpty()) {

            Log.i(Globals.DEBUG_TAG, "LocalSearchAsyncTask - Starting");
            String language = LocaleHelper.getLanguage(contextRef.get());

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

            RoomNamesDatabase roomNamesDatabase = (finishedLoadingNamesDb && (mShowNames))? RoomNamesDatabase.getInstance(contextRef.get()) : null;
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
