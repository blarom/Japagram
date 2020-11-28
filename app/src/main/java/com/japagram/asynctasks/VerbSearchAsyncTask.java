package com.japagram.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.japagram.data.InputQuery;
import com.japagram.data.Verb;
import com.japagram.data.Word;
import com.japagram.resources.LocaleHelper;
import com.japagram.resources.UtilitiesVerbSearchAsyncTask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class VerbSearchAsyncTask extends AsyncTask<Void, Void, Object[]> {

    //region Parameters
    private final WeakReference<Context> contextRef;
    public VerbSearchAsyncResponseHandler listener;
    private final List<Word> mWordsFromDictFragment;
    private final InputQuery mInputQuery;
    //endregion

    public VerbSearchAsyncTask(Context context, InputQuery inputQuery, List<Word> mWordsFromDictFragment, VerbSearchAsyncResponseHandler listener) {
        contextRef = new WeakReference<>(context);
        this.mInputQuery = inputQuery;
        this.mWordsFromDictFragment = mWordsFromDictFragment;
        this.listener = listener;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }
    @Override protected Object[] doInBackground(Void... voids) {

        List<Object[]> matchingConjugationParameters = new ArrayList<>();
        List<Verb> matchingVerbsSorted = new ArrayList<>();
        List<Word> matchingWordsSorted = new ArrayList<>();

        if (mInputQuery.isEmpty()) {
            return new Object[]{matchingVerbsSorted, matchingWordsSorted, matchingConjugationParameters};
        }

        String language = LocaleHelper.getLanguage(contextRef.get());
        return UtilitiesVerbSearchAsyncTask.getSortedVerbsWordsAndConjParams(contextRef, mInputQuery.getOriginal(),  mWordsFromDictFragment, language);
    }

    @Override protected void onPostExecute(Object[] objectArray) {
        super.onPostExecute(objectArray);
        listener.onVerbSearchAsyncTaskResultFound(objectArray);
    }

    public interface VerbSearchAsyncResponseHandler {
        void onVerbSearchAsyncTaskResultFound(Object[] text);
    }
}
