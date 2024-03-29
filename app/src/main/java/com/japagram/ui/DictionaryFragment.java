package com.japagram.ui;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.japagram.R;
import com.japagram.adapters.DictionaryRecyclerViewAdapter;
import com.japagram.asynctasks.JishoOnlineSearchAsyncTask;
import com.japagram.asynctasks.DictSearchAsyncTask;
import com.japagram.asynctasks.VerbSearchAsyncTask;
import com.japagram.data.InputQuery;
import com.japagram.data.Verb;
import com.japagram.data.Word;
import com.japagram.databinding.FragmentDictionaryBodyBinding;
import com.japagram.utilitiesAndroid.AndroidUtilitiesIO;
import com.japagram.utilitiesAndroid.AndroidUtilitiesWeb;
import com.japagram.utilitiesCrossPlatform.Globals;
import com.japagram.resources.LocaleHelper;
import com.japagram.utilitiesCrossPlatform.UtilitiesDb;
import com.japagram.utilitiesAndroid.AndroidUtilitiesPrefs;
import com.japagram.utilitiesPlatformOverridable.OvUtilsGeneral;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DictionaryFragment extends Fragment implements
        DictionaryRecyclerViewAdapter.DictionaryItemClickHandler,
        DictSearchAsyncTask.LocalDictSearchAsyncResponseHandler,
        VerbSearchAsyncTask.VerbSearchAsyncResponseHandler {


    public static final int LOCAL = 0;
    public static final int CONJ = 1;
    public static final int ALL = 2;
    //region Parameters
    private FragmentDictionaryBodyBinding binding;
    private static final int WORD_RESULTS_MAX_RESPONSE_DELAY = 2000;
    private static final int MAX_NUMBER_RESULTS_SHOWN = 50;
    private static final int MAX_NUM_WORDS_TO_SHARE = 30;
    private InputQuery mInputQuery;
    private List<Word> mLocalMatchingWordsList;
    private List<Word> mMergedMatchingWordsList;
    private DictionaryRecyclerViewAdapter mDictionaryRecyclerViewAdapter;
    private List<Word> mJishoMatchingWordsList;
    private List<Word> mDifferentJishoWords;
    private List<Word> mMatchingWordsFromVerbs;
    private boolean mSuccessfullyDisplayedResultsBeforeTimeout;
    private boolean mOverrideDisplayConditions;
    private JishoOnlineSearchAsyncTask mJishoOnlineSearchAsyncTask;
    private DictSearchAsyncTask mLocalDictSearchAsyncTask;
    private VerbSearchAsyncTask mVerbSearchAsyncTask;
    private boolean mShowNames;
    private String mLanguage;
    private boolean mReceivedLocalResults = false;
    private boolean mReceivedConjResults = false;
    //endregion


    //Fragment Lifecycle methods
    @Override public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        dictionaryFragmentOperationsHandler = (DictionaryFragmentOperationsHandler) context;
   }
    @Override public void onCreate(Bundle savedInstanceState) { //instead of onActivityCreated
        super.onCreate(savedInstanceState);

        getExtras();
        initializeParameters();

    }
    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //setRetainInstance(true);
        final View rootView = inflater.inflate(R.layout.fragment_dictionary, container, false);
        return rootView;
    }
    @Override public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentDictionaryBodyBinding.bind(view);
        initializeViews();
        getQuerySearchResults();
    }
    @Override public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        //outState.putParcelableArrayList(getString(R.string.saved_local_results), new ArrayList<>(mLocalMatchingWordsList)); //causes cash because parcel too big, can limit with sublist
        //outState.putParcelableArrayList(getString(R.string.saved_merged_results), new ArrayList<>(mMergedMatchingWordsList));
        outState.putParcelable(getString(R.string.saved_input_query), mInputQuery);

        cancelAsyncOperations();

    }
    @Override public void onDetach() {
        super.onDetach();
        cancelAsyncOperations();
        cancelAsyncOperations();
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override public void onDestroy() {
        super.onDestroy();
    }


	//Functionality methods
    private void getExtras() {
        if (getArguments()!=null) {
            mInputQuery = new InputQuery(getArguments().getString(getString(R.string.user_query_word)));
            mShowNames = getArguments().getBoolean(getString(R.string.show_names));
        }
    }
    private void initializeParameters() {

        mLocalMatchingWordsList = new ArrayList<>();
        mMergedMatchingWordsList = new ArrayList<>();

        mLanguage = LocaleHelper.getLanguage(getContext());
    }
    private void initializeViews() {

        if (getContext() == null) return;

        AssetManager am = getContext().getApplicationContext().getAssets();
        Typeface typeface = AndroidUtilitiesPrefs.getPreferenceUseJapaneseFont(getActivity()) ?
                Typeface.createFromAsset(am, String.format(Locale.JAPAN, "fonts/%s", "DroidSansJapanese.ttf")) : Typeface.DEFAULT;

        binding.dictionaryResults.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        binding.dictionaryResults.setNestedScrollingEnabled(true);
        mDictionaryRecyclerViewAdapter = new DictionaryRecyclerViewAdapter(getContext(), this, null, mInputQuery, LocaleHelper.getLanguage(getContext()), typeface);
        binding.dictionaryResults.setAdapter(mDictionaryRecyclerViewAdapter);
    }
    private void getQuerySearchResults() {

        if (getContext()==null || getActivity()==null) return;

        mSuccessfullyDisplayedResultsBeforeTimeout = false;
        mLocalMatchingWordsList = new ArrayList<>();
        mJishoMatchingWordsList = new ArrayList<>();
        mMergedMatchingWordsList = new ArrayList<>();
        mDifferentJishoWords = new ArrayList<>();
        mMergedMatchingWordsList = new ArrayList<>();
        mMatchingWordsFromVerbs = new ArrayList<>();
        if (mInputQuery != null && !mInputQuery.isEmpty()) {

            showLoadingIndicator();

            mDictionaryRecyclerViewAdapter.setShowSources(AndroidUtilitiesPrefs.getPreferenceShowSources(getActivity()));

            startSearchingForWordsInRoomDb();
            if (AndroidUtilitiesPrefs.getPreferenceShowConjResults(getActivity())) {
                startReverseConjSearchForMatchingVerbs();
            }

            //Preventing computation/connectivity delays from freezing the UI thread
            mOverrideDisplayConditions = false;
            new Handler().postDelayed(() -> {
                mOverrideDisplayConditions = true;
                Log.i(Globals.DEBUG_TAG, "Displaying merged words at WORD_RESULTS_MAX_RESPONSE_DELAY");
                if (!mSuccessfullyDisplayedResultsBeforeTimeout) displayMergedWordsToUser(ALL);
            }, WORD_RESULTS_MAX_RESPONSE_DELAY);
        }
        else showEmptySearchResults();
    }
    private void startSearchingForWordsInRoomDb() {
        if (getActivity()!=null) {
            Log.i(Globals.DEBUG_TAG, "Starting search for Room words");
            mLocalDictSearchAsyncTask = new DictSearchAsyncTask(getContext(), mInputQuery, this, mShowNames);
            mLocalDictSearchAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
    private void startReverseConjSearchForMatchingVerbs() {
        if (getActivity()!=null) {
            Log.i(Globals.DEBUG_TAG, "DictionaryFragment - Starting search for verbs");
            mVerbSearchAsyncTask = new VerbSearchAsyncTask(getContext(), mInputQuery, new ArrayList<>(), this);
            mVerbSearchAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
    private void showEmptySearchResults() {
        binding.dictionaryHint.setText(OvUtilsGeneral.fromHtml(getResources().getString(R.string.please_enter_valid_word)));
        binding.dictionaryHint.setVisibility(View.VISIBLE);
        binding.dictionaryResults.setVisibility(View.GONE);
    }
    private void updateDisplayedList() {
        if (getContext()==null || getActivity()==null) return;
        mMergedMatchingWordsList = UtilitiesDb.sortWordsAccordingToRanking(mMergedMatchingWordsList, mInputQuery, mLanguage);
        List<Word> finalDisplayedWords = (mMergedMatchingWordsList.size()>MAX_NUMBER_RESULTS_SHOWN) ?
                mMergedMatchingWordsList.subList(0,MAX_NUMBER_RESULTS_SHOWN) : mMergedMatchingWordsList;
        mDictionaryRecyclerViewAdapter.setContents(finalDisplayedWords);
        binding.dictionaryHint.setVisibility(View.GONE);
        binding.dictionaryResults.setVisibility(View.VISIBLE);
        Log.i(Globals.DEBUG_TAG, "DictionaryFragment - Display successful");
        mSuccessfullyDisplayedResultsBeforeTimeout = true;
        AndroidUtilitiesIO.hideSoftKeyboard(getActivity());
        hideLoadingIndicator();
    }
    private void displayMergedWordsToUser(int sourceType) {
        if (getContext()==null || getActivity()==null) return;

        boolean showConjResults = AndroidUtilitiesPrefs.getPreferenceShowConjResults(getActivity());
        boolean waitForAllResults = AndroidUtilitiesPrefs.getPreferenceWaitForAllResults(getActivity());
        boolean haveMoreWordsForDisplayedList;
        int oldSize = mMergedMatchingWordsList.size();

        if (sourceType == LOCAL) {
            if (mLocalMatchingWordsList.size() > 0) {
                mMergedMatchingWordsList = UtilitiesDb.getMergedWordsList(mMergedMatchingWordsList, mLocalMatchingWordsList);
            }
            mReceivedLocalResults = true;
        } else if (sourceType == CONJ) {
            if (mMatchingWordsFromVerbs.size() > 0 && showConjResults) {
                mMergedMatchingWordsList = UtilitiesDb.getMergedWordsList(mMergedMatchingWordsList, mMatchingWordsFromVerbs);
            }
            mReceivedConjResults = true;
        }
        haveMoreWordsForDisplayedList = mMergedMatchingWordsList.size() > oldSize;

        if ( mMergedMatchingWordsList.size() > 0 ) {
            if (!waitForAllResults && haveMoreWordsForDisplayedList || waitForAllResults && mReceivedLocalResults && mReceivedConjResults) {
                updateDisplayedList();
                int maxIndex = Math.min(mMergedMatchingWordsList.size(), MAX_NUM_WORDS_TO_SHARE);
                mMergedMatchingWordsList = UtilitiesDb.sortWordsAccordingToRanking(mMergedMatchingWordsList, mInputQuery, mLanguage);
                dictionaryFragmentOperationsHandler.onFinalMatchingWordsFound(mMergedMatchingWordsList.subList(0,maxIndex));
                mSuccessfullyDisplayedResultsBeforeTimeout = true;
                hideLoadingIndicator();
            }
        } else {
            if (mReceivedLocalResults && mReceivedConjResults) {
                binding.dictionaryHint.setText(OvUtilsGeneral.fromHtml(getResources().getString(R.string.no_results_found)));
                binding.dictionaryHint.setVisibility(View.VISIBLE);
                binding.dictionaryResults.setVisibility(View.GONE);
                mSuccessfullyDisplayedResultsBeforeTimeout = true;
                hideLoadingIndicator();
            }
        }

    }

    private void cancelAsyncOperations() {
        if (mLocalDictSearchAsyncTask != null) mLocalDictSearchAsyncTask.cancel(true);
        if (mJishoOnlineSearchAsyncTask != null) mJishoOnlineSearchAsyncTask.cancel(true);
        if (mVerbSearchAsyncTask != null) mVerbSearchAsyncTask.cancel(true);
    }
    private void showLoadingIndicator() {
        binding.dictionaryResultsLoadingIndicator.setVisibility(View.VISIBLE);
        binding.dictionaryHint.setVisibility(View.GONE);
    }
    private void hideLoadingIndicator() {
        binding.dictionaryResultsLoadingIndicator.setVisibility(View.INVISIBLE);
    }


    //Communication with other classes

    //Communication with DictionaryRecyclerViewAdapter
    @Override public void onDecomposeKanjiLinkClicked(String text) {
        dictionaryFragmentOperationsHandler.onQueryTextUpdateFromDictRequested(text);
        dictionaryFragmentOperationsHandler.onDecomposeKanjiRequested(text);
    }
    @Override public void onVerbLinkClicked(String text) {
        dictionaryFragmentOperationsHandler.onQueryTextUpdateFromDictRequested(text);
        dictionaryFragmentOperationsHandler.onVerbConjugationFromDictRequested(text);
    }

    //Communication with parent activity
    private DictionaryFragmentOperationsHandler dictionaryFragmentOperationsHandler;
    interface DictionaryFragmentOperationsHandler {
        void onQueryTextUpdateFromDictRequested(String selectedWordString);
        void onDecomposeKanjiRequested(String selectedKanji);
        void onVerbConjugationFromDictRequested(String selectedVerbString);
        void onLocalMatchingWordsFound(List<Word> matchingWords);
        void onFinalMatchingWordsFound(List<Word> matchingWords);
    }
    public void setQuery(String query) {
        mInputQuery = new InputQuery(query);
        getQuerySearchResults();
    }
    void setShowNames(boolean status) {
        mShowNames = status;
    }

    //Communication with AsyncTasks
    @Override public void onLocalDictSearchAsyncTaskResultFound(List<Word> words) {

        if (getContext()==null) return;
        Log.i(Globals.DEBUG_TAG, "DictionaryFragment - Finished Words Search AsyncTask");

        mLocalMatchingWordsList = words;
        mLocalMatchingWordsList = UtilitiesDb.sortWordsAccordingToRanking(mLocalMatchingWordsList, mInputQuery, mLanguage);

        int maxIndex = Math.min(mLocalMatchingWordsList.size(), MAX_NUM_WORDS_TO_SHARE);
        dictionaryFragmentOperationsHandler.onLocalMatchingWordsFound(mLocalMatchingWordsList.subList(0,maxIndex));

        Log.i(Globals.DEBUG_TAG, "DictionaryFragment - Displaying Room words");
        displayMergedWordsToUser(LOCAL);
    }
    @Override @SuppressWarnings("unchecked") public void onVerbSearchAsyncTaskResultFound(Object[] dataElements) {

        if (getContext()==null) return;
        Log.i(Globals.DEBUG_TAG, "DictionaryFragment - Finished Verbs Search AsyncTask");

        List<Verb> mMatchingVerbs = (List<Verb>) dataElements[0];
        mMatchingWordsFromVerbs = (List<Word>) dataElements[1];
        List<Object[]> mMatchingConjugationParametersList = (List<Object[]>) dataElements[2];

        //Adapting the words list to include information used for proper display in the results list
        for (int i = 0; i < mMatchingWordsFromVerbs.size(); i++) {
            Word word = mMatchingWordsFromVerbs.get(i);
            word.setIsLocal(true);
            for (Object[] matchingConjugationParameters : mMatchingConjugationParametersList) {
                if ((long) matchingConjugationParameters[Globals.MATCHING_ID] == word.getId()) {
                    String matchingConjugation = (String) matchingConjugationParameters[Globals.MATCHING_CONJUGATION];
                    word.setMatchingConj(matchingConjugation);
                    break;
                }
            }
        }

        Log.i(Globals.DEBUG_TAG, "DictionaryFragment - Displaying Verb merged words");
        displayMergedWordsToUser(CONJ);
    }
}