package com.japagram.asynctasks;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;

import com.japagram.data.RoomKanjiDatabase;
import com.japagram.data.KanjiCharacter;
import com.japagram.resources.LocaleHelper;
import com.japagram.resources.Utilities;
import com.japagram.resources.UtilitiesKanjiDecompositionAsyncTask;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

public class KanjiCharacterDecompositionAsyncTask extends AsyncTask<Void, Void, Object> {

    private final String inputQuery;
    private final List<String[]> mRadicalsOnlyDatabase;
    private final int radicalIteration;
    private final int kanjiListIndex;
    private final WeakReference<Context> contextRef;
    public KanjiCharacterDecompositionAsyncResponseHandler listener;
    //endregion

    public KanjiCharacterDecompositionAsyncTask(Context context,
                                                String inputQuery,
                                                int radicalIteration,
                                                List<String[]> mRadicalsOnlyDatabase,
                                                int kanjiListIndex,
                                                KanjiCharacterDecompositionAsyncResponseHandler listener) {
        contextRef = new WeakReference<>(context);
        this.inputQuery = inputQuery;
        this.radicalIteration = radicalIteration;
        this.mRadicalsOnlyDatabase = mRadicalsOnlyDatabase;
        this.kanjiListIndex = kanjiListIndex;
        this.listener = listener;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }
    @Override
    protected Object doInBackground(Void... voids) {

        //region Parameters
        RoomKanjiDatabase mRoomKanjiDatabase = RoomKanjiDatabase.getInstance(contextRef.get());
        Resources mLocalizedResources = Utilities.getLocalizedResources(contextRef.get(), Locale.getDefault());

        // Search for the input in the database and retrieve the result's characteristics

        String language = LocaleHelper.getLanguage(contextRef.get());
        String concatenated_input = Utilities.removeSpecialCharacters(inputQuery);
        String inputHexIdentifier = Utilities.convertToUTF8Index(concatenated_input).toUpperCase();
        KanjiCharacter mCurrentKanjiCharacter = mRoomKanjiDatabase.getKanjiCharacterByHexId(inputHexIdentifier);
        List<String> currentKanjiDetailedCharacteristics = UtilitiesKanjiDecompositionAsyncTask.getKanjiDetailedCharacteristics(mCurrentKanjiCharacter, language, mLocalizedResources);
        List<String> currentKanjiMainRadicalInfo = UtilitiesKanjiDecompositionAsyncTask.getKanjiRadicalCharacteristics(mCurrentKanjiCharacter, mRadicalsOnlyDatabase, mLocalizedResources);

        List<List<String>> decomposedKanji = UtilitiesKanjiDecompositionAsyncTask.Decomposition(inputQuery, mRoomKanjiDatabase);
        Object[] radicalInfo = UtilitiesKanjiDecompositionAsyncTask.getRadicalInfo(inputQuery, mRadicalsOnlyDatabase, mRoomKanjiDatabase, language, mLocalizedResources);

        return new Object[] {
                decomposedKanji,
                currentKanjiDetailedCharacteristics,
                currentKanjiMainRadicalInfo,
                inputQuery,
                radicalIteration,
                radicalInfo[0],
                radicalInfo[1],
                radicalInfo[2],
                kanjiListIndex
        };
    }

    @Override
    protected void onPostExecute(Object words) {
        super.onPostExecute(words);
        listener.onKanjiCharacterDecompositionAsyncTaskResultFound(words);
    }

    public interface KanjiCharacterDecompositionAsyncResponseHandler {
        void onKanjiCharacterDecompositionAsyncTaskResultFound(Object text);
    }

}
