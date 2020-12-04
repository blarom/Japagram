package com.japagram.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.japagram.data.InputQuery;
import com.japagram.data.RoomCentralDatabase;
import com.japagram.data.RoomExtendedDatabase;
import com.japagram.data.RoomNamesDatabase;
import com.japagram.data.Word;
import com.japagram.resources.Globals;
import com.japagram.resources.LocaleHelper;
import com.japagram.resources.UtilitiesDictSearchAsyncTask;
import com.japagram.resources.UtilitiesPrefs;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class DictSearchAsyncTask extends AsyncTask<Void, Void, List<Word>> {

    private final boolean mShowNames;
    private final WeakReference<Context> contextRef;
    private final InputQuery mQuery;
    public LocalDictSearchAsyncResponseHandler listener;

    public DictSearchAsyncTask(Context context, InputQuery query, LocalDictSearchAsyncResponseHandler listener, boolean mShowNames) {
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

        if (mQuery.isEmpty()) {
            return new ArrayList<>();
        }

        Log.i(Globals.DEBUG_TAG, "LocalSearchAsyncTask - Starting");
        String language = LocaleHelper.getLanguage(contextRef.get());

        boolean finishedLoadingExtendedDb = UtilitiesPrefs.getAppPreferenceExtendedDatabasesFinishedLoadingFlag(contextRef.get());
        boolean finishedLoadingNamesDb = UtilitiesPrefs.getAppPreferenceNamesDatabasesFinishedLoadingFlag(contextRef.get());

        RoomCentralDatabase mRoomCentralDatabase = RoomCentralDatabase.getInstance(contextRef.get());
        RoomExtendedDatabase roomExtendedDatabase = finishedLoadingExtendedDb? RoomExtendedDatabase.getInstance(contextRef.get()) : null;
        RoomNamesDatabase roomNamesDatabase = (finishedLoadingNamesDb && mShowNames)? RoomNamesDatabase.getInstance(contextRef.get()) : null;

        return UtilitiesDictSearchAsyncTask.getMatchingWords(mRoomCentralDatabase, roomExtendedDatabase, roomNamesDatabase, mQuery, language, contextRef.get(), mShowNames);
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
