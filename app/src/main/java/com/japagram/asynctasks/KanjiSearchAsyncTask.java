package com.japagram.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.japagram.data.RoomKanjiDatabase;
import com.japagram.utilitiesCrossPlatform.UtilitiesKanjiSearch;

import java.lang.ref.WeakReference;
import java.util.List;

public class KanjiSearchAsyncTask extends AsyncTask<Void, Void, Object[]> {

    //region Parameters
    private final String[] elements_list;
    private final int mSelectedStructure;
    private final List<String[]> mSimilarsDatabase;
    private final boolean showOnlyJapCharacters;
    private final WeakReference<Context> contextRef;
    //endregion
    public KanjiSearchAsyncResponseHandler listener;

    public KanjiSearchAsyncTask(Context context, String[] elements_list, int mSelectedStructure, List<String[]> mSimilarsDatabase,
                                boolean showOnlyJapCharacters, KanjiSearchAsyncResponseHandler listener) {
        contextRef = new WeakReference<>(context);
        this.elements_list = elements_list;
        this.mSelectedStructure = mSelectedStructure;
        this.mSimilarsDatabase = mSimilarsDatabase;
        this.listener = listener;
        this.showOnlyJapCharacters = showOnlyJapCharacters;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }
    @Override
    protected Object[] doInBackground(Void... voids) {

        RoomKanjiDatabase mRoomKanjiDatabase = RoomKanjiDatabase.getInstance(contextRef.get());
        Object[] findKanjisResult = UtilitiesKanjiSearch.findKanjis(elements_list, mSimilarsDatabase, mSelectedStructure, mRoomKanjiDatabase, showOnlyJapCharacters);
        List<String> result = (List<String>) findKanjisResult[0];
        String mSearchInfoMessage = (String) findKanjisResult[1];

        return new Object[] {result, mSearchInfoMessage};
    }

    @Override
    protected void onPostExecute(Object[] data) {
        super.onPostExecute(data);
        listener.onKanjiSearchAsyncTaskResultsFound(data);
    }

    public interface KanjiSearchAsyncResponseHandler {
        void onKanjiSearchAsyncTaskResultsFound(Object[] data);
    }

}
