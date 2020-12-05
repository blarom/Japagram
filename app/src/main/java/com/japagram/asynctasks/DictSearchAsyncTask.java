package com.japagram.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.japagram.data.InputQuery;
import com.japagram.data.RoomExtendedDatabase;
import com.japagram.data.RoomNamesDatabase;
import com.japagram.data.Word;
import com.japagram.utilitiesCrossPlatform.Globals;
import com.japagram.resources.LocaleHelper;
import com.japagram.utilitiesCrossPlatform.UtilitiesDictSearch;
import com.japagram.utilitiesAndroid.UtilitiesPrefs;

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

        Context context = contextRef.get();

        Log.i(Globals.DEBUG_TAG, "LocalSearchAsyncTask - Starting");
        String language = LocaleHelper.getLanguage(contextRef.get());

        boolean finishedLoadingExtendedDb = UtilitiesPrefs.getAppPreferenceExtendedDatabasesFinishedLoadingFlag(context);
        boolean finishedLoadingNamesDb = UtilitiesPrefs.getAppPreferenceNamesDatabasesFinishedLoadingFlag(context);

        boolean roomExtendedDbIsAvailable = finishedLoadingExtendedDb && RoomExtendedDatabase.getInstance(context) != null;
        boolean roomNamesDbIsAvailable = (finishedLoadingNamesDb && mShowNames) && RoomNamesDatabase.getInstance(context) != null;
        boolean roomNamesDatabasesFinishedLoading = UtilitiesPrefs.getAppPreferenceNamesDatabasesFinishedLoadingFlag(context);

        return UtilitiesDictSearch.getMatchingWords(
                roomExtendedDbIsAvailable,
                roomNamesDbIsAvailable,
                roomNamesDatabasesFinishedLoading,
                mQuery,
                language,
                context,
                mShowNames);
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
