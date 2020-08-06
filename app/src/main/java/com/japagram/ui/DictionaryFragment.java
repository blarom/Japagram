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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.japagram.R;
import com.japagram.adapters.DictionaryRecyclerViewAdapter;
import com.japagram.asynctasks.JishoSearchAsyncTask;
import com.japagram.asynctasks.LocalSearchAsyncTask;
import com.japagram.asynctasks.VerbSearchAsyncTask;
import com.japagram.data.ConjugationTitle;
import com.japagram.data.FirebaseDao;
import com.japagram.data.InputQuery;
import com.japagram.data.Verb;
import com.japagram.data.Word;
import com.japagram.resources.Globals;
import com.japagram.resources.LocaleHelper;
import com.japagram.resources.Utilities;
import com.japagram.resources.UtilitiesDb;
import com.japagram.resources.UtilitiesPrefs;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class DictionaryFragment extends Fragment implements
        FirebaseDao.FirebaseOperationsHandler,
        DictionaryRecyclerViewAdapter.DictionaryItemClickHandler,
        JishoSearchAsyncTask.JishoSearchAsyncResponseHandler,
        LocalSearchAsyncTask.LocalDictSearchAsyncResponseHandler, VerbSearchAsyncTask.VerbSearchAsyncResponseHandler {


    public static final String LOCAL = "local";
    public static final String CONJ = "conj";
    public static final String ONLINE = "online";
    public static final String ALL = "all";
    //region Parameters
    @BindView(R.id.dictionary_recyclerview) RecyclerView mDictionaryRecyclerView;
    @BindView(R.id.word_hint) TextView mHintTextView;
    @BindView(R.id.dict_results_loading_indicator) ProgressBar mProgressBarLoadingIndicator;
    private static final int WORD_RESULTS_MAX_RESPONSE_DELAY = 2000;
    private static final int MAX_NUMBER_RESULTS_SHOWN = 50;
    private static final int MAX_NUM_WORDS_TO_SHARE = 30;
    private InputQuery mInputQuery;
    private List<Word> mLocalMatchingWordsList;
    private List<Word> mMergedMatchingWordsList;
    private FirebaseDao mFirebaseDao;
    private Unbinder mBinding;
    private boolean mAlreadyLoadedRoomResults;
    private boolean mAlreadyLoadedJishoResults;
    private boolean mAlreadyLoadedConjResults;
    private List<ConjugationTitle> mConjugationTitles;
    private DictionaryRecyclerViewAdapter mDictionaryRecyclerViewAdapter;
    private List<Word> mJishoMatchingWordsList;
    private List<Word> mDifferentJishoWords;
    private List<Word> mMatchingWordsFromVerbs;
    private boolean mSuccessfullyDisplayedResultsBeforeTimeout;
    private boolean mOverrideDisplayConditions;
    private JishoSearchAsyncTask mJishoSearchAsyncTask;
    private LocalSearchAsyncTask mLocalDictSearchAsyncTask;
    private VerbSearchAsyncTask mVerbSearchAsyncTask;
    private boolean mShowNames;
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

        initializeViews(rootView);

        getQuerySearchResults();

        return rootView;
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
        mFirebaseDao.removeListeners();
        cancelAsyncOperations();
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        mBinding.unbind();
    }
    @Override public void onDestroy() {
        super.onDestroy();
        //if (getActivity()!=null && MainApplication.getRefWatcher(getActivity())!=null) MainApplication.getRefWatcher(getActivity()).watch(this);
    }


	//Functionality methods
    private void getExtras() {
        if (getArguments()!=null) {
            mInputQuery = new InputQuery(getArguments().getString(getString(R.string.user_query_word)));
            mShowNames = getArguments().getBoolean(getString(R.string.show_names));
        }
    }
    private void initializeParameters() {

        mFirebaseDao = new FirebaseDao(this);

        mLocalMatchingWordsList = new ArrayList<>();
        mMergedMatchingWordsList = new ArrayList<>();

        mAlreadyLoadedRoomResults = false;
        mAlreadyLoadedJishoResults = false;

        mConjugationTitles = UtilitiesDb.getConjugationTitles(Globals.VerbLatinConjDatabase, getContext());
    }
    private void initializeViews(View rootView) {
        mBinding = ButterKnife.bind(this, rootView);

        if (getContext() == null) return;

        AssetManager am = getContext().getApplicationContext().getAssets();
        Typeface typeface = UtilitiesPrefs.getPreferenceUseJapaneseFont(getActivity()) ?
                Typeface.createFromAsset(am, String.format(Locale.JAPAN, "fonts/%s", "DroidSansJapanese.ttf")) : Typeface.DEFAULT;

        mDictionaryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        mDictionaryRecyclerView.setNestedScrollingEnabled(true);
        mDictionaryRecyclerViewAdapter = new DictionaryRecyclerViewAdapter(getContext(), this, null, mInputQuery, LocaleHelper.getLanguage(getContext()), typeface);
        mDictionaryRecyclerView.setAdapter(mDictionaryRecyclerViewAdapter);
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

            mDictionaryRecyclerViewAdapter.setShowSources(UtilitiesPrefs.getPreferenceShowSources(getActivity()));

            startSearchingForWordsInRoomDb();
            if (UtilitiesPrefs.getPreferenceShowConjResults(getActivity())) {
                startReverseConjSearchForMatchingVerbs();
            }
            if (UtilitiesPrefs.getPreferenceShowOnlineResults(getActivity())) {
                startSearchingForWordsInJisho();
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
            mLocalDictSearchAsyncTask = new LocalSearchAsyncTask(getContext(), mInputQuery, this, mShowNames);
            mLocalDictSearchAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
    private void startSearchingForWordsInJisho() {

        if (mInputQuery != null && !mInputQuery.isEmpty() && getActivity() != null && getContext() != null) {
            Log.i(Globals.DEBUG_TAG, "DictionaryFragment - Starting search for Jisho words");
            mJishoSearchAsyncTask = new JishoSearchAsyncTask(getContext(), mInputQuery.getOriginal(), this);
            mJishoSearchAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        mHintTextView.setText(Utilities.fromHtml(getResources().getString(R.string.please_enter_valid_word)));
        mHintTextView.setVisibility(View.VISIBLE);
        mDictionaryRecyclerView.setVisibility(View.GONE);
    }
    private void displayMergedWordsToUser(String sourceType) {

        if (getContext()==null || getActivity()==null) return;
        Utilities.hideSoftKeyboard(getActivity());

        boolean showOnlineResults = UtilitiesPrefs.getPreferenceShowOnlineResults(getActivity());
        boolean showConjResults = UtilitiesPrefs.getPreferenceShowConjResults(getActivity());
        boolean waitForOnlineResults = UtilitiesPrefs.getPreferenceWaitForOnlineResults(getActivity());
        boolean waitForConjResults = UtilitiesPrefs.getPreferenceWaitForConjResults(getActivity());
        boolean gotNewResultsFromOnline = false; //Prevents refreshing the words list when there are no new results
        boolean gotNewResultsFromConj = false; //Prevents refreshing the words list when there are no new results
        boolean gotNewResultsOnTimerDelay = false; //Prevents refreshing the words list when there are no new results

        if (!showOnlineResults) mJishoMatchingWordsList = new ArrayList<>();
        if (!showConjResults) mMatchingWordsFromVerbs = new ArrayList<>();

        if (mAlreadyLoadedRoomResults &&
                (       showOnlineResults && showConjResults &&
                                (!waitForOnlineResults && !waitForConjResults)
                                || (!waitForOnlineResults && waitForConjResults && mAlreadyLoadedConjResults)
                                || (waitForOnlineResults && !waitForConjResults && mAlreadyLoadedJishoResults)
                                || (waitForOnlineResults && waitForConjResults && mAlreadyLoadedJishoResults && mAlreadyLoadedConjResults)
                ) || (  showOnlineResults && !showConjResults && (!waitForOnlineResults || mAlreadyLoadedJishoResults)
                ) || (  !showOnlineResults && showConjResults && (!waitForConjResults || mAlreadyLoadedConjResults)
                ) || (  !showOnlineResults && !showConjResults)
            || mOverrideDisplayConditions) {

            //Getting the merged results
            int oldSize = mMergedMatchingWordsList.size();
            if (sourceType.equals(LOCAL) && mLocalMatchingWordsList.size() > 0) {
                mMergedMatchingWordsList = UtilitiesDb.getMergedWordsList(mMergedMatchingWordsList, mLocalMatchingWordsList, "");
            }
            if (sourceType.equals(ONLINE) && mJishoMatchingWordsList.size() > 0) {
                mMergedMatchingWordsList = UtilitiesDb.getMergedWordsList(mMergedMatchingWordsList, mJishoMatchingWordsList, "");
                gotNewResultsFromOnline = true;
            }
            if (sourceType.equals(CONJ) && mMatchingWordsFromVerbs.size() > 0) {
                mMergedMatchingWordsList = UtilitiesDb.getMergedWordsList(mMergedMatchingWordsList, mMatchingWordsFromVerbs, "");
                if (mMergedMatchingWordsList.size() > oldSize) gotNewResultsFromConj = true;
            }
            mMergedMatchingWordsList = sortWordsAccordingToRanking(mMergedMatchingWordsList);

            if (mMergedMatchingWordsList.size() > 0) {
                List<Word> finalDisplayedWords = (mMergedMatchingWordsList.size()>MAX_NUMBER_RESULTS_SHOWN) ?
                        mMergedMatchingWordsList.subList(0,MAX_NUMBER_RESULTS_SHOWN) : mMergedMatchingWordsList;
                if (sourceType.equals(LOCAL)
                        || sourceType.equals(ALL)
                        || sourceType.equals(ONLINE) && gotNewResultsFromOnline
                        || sourceType.equals(CONJ) && gotNewResultsFromConj) {
                    mDictionaryRecyclerViewAdapter.setContents(finalDisplayedWords);
                }
                mHintTextView.setVisibility(View.GONE);
                mDictionaryRecyclerView.setVisibility(View.VISIBLE);
                Log.i(Globals.DEBUG_TAG, "DictionaryFragment - Display successful");
                mSuccessfullyDisplayedResultsBeforeTimeout = true;
                hideLoadingIndicator();
            }
            else {
                if (waitForConjResults) {
                    mHintTextView.setText(Utilities.fromHtml(getResources().getString(R.string.please_enter_valid_word)));
                    Log.i(Globals.DEBUG_TAG, "DictionaryFragment - Display successful for Local + Conj Search");
                    mSuccessfullyDisplayedResultsBeforeTimeout = true;
                    hideLoadingIndicator();
                } else {
                    if (mAlreadyLoadedConjResults) {
                        mHintTextView.setText(Utilities.fromHtml(getResources().getString(R.string.no_results_found)));
                        Log.i(Globals.DEBUG_TAG, "DictionaryFragment - Display successful for Local + Conj Search");
                        mSuccessfullyDisplayedResultsBeforeTimeout = true;
                        hideLoadingIndicator();
                    } else {
                        mHintTextView.setText(Utilities.fromHtml(getResources().getString(R.string.no_match_found_for_now)));
                        Log.i(Globals.DEBUG_TAG, "DictionaryFragment - Display successful for Local without Conj Search");
                    }
                }
                mHintTextView.setVisibility(View.VISIBLE);
                mDictionaryRecyclerView.setVisibility(View.GONE);
            }

            int maxIndex = Math.min(mMergedMatchingWordsList.size(), MAX_NUM_WORDS_TO_SHARE);
            dictionaryFragmentOperationsHandler.onFinalMatchingWordsFound(mMergedMatchingWordsList.subList(0,maxIndex));

            if (UtilitiesPrefs.getPreferenceShowInfoBoxesOnSearch(getActivity())) {
                String text = getString(R.string.found) + " "
                        + mLocalMatchingWordsList.size()
                        + " "
                        + ((mLocalMatchingWordsList.size()==1)? getString(R.string.local_result) : getString(R.string.local_results));

                if (showOnlineResults && mAlreadyLoadedJishoResults) {
                    if (showConjResults) text += ", ";
                    else text += " " + getString(R.string.and) + " ";

                    switch (mDifferentJishoWords.size()) {
                        case 0:
                            text += getString(R.string.no_new_online_results);
                            break;
                        case 1:
                            text += getString(R.string.one_new_or_fuller_online_result);
                            break;
                        default:
                            text += mDifferentJishoWords.size() + " " + getString(R.string.new_or_fuller_online_results);
                            break;
                    }
                }
                if (showConjResults && mAlreadyLoadedConjResults) {
                    if (showOnlineResults && mAlreadyLoadedJishoResults) text += ", "+getString(R.string.and)+" ";
                    else if (showOnlineResults) text += ", ";
                    else text += " "+getString(R.string.and)+" ";

                    text += getString(R.string.including)+" ";

                    switch (mMatchingWordsFromVerbs.size()) {
                        case 0:
                            text += getString(R.string.no_verb);
                            break;
                        case 1:
                            text += getString(R.string.one_verb);
                            break;
                        default:
                            text += mMatchingWordsFromVerbs.size() + " " + getString(R.string.verbs);
                            break;
                    }
                    text += " " + getString(R.string.with_conjugations_matching_the_search_word);
                }
                text += ".";
                Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();
            }

        }

    }
    @NotNull @Contract("null -> new") private List<Word> sortWordsAccordingToRanking(List<Word> wordsList) {

        if (wordsList == null || wordsList.size()==0) return new ArrayList<>();

        List<long[]> matchingWordIndexesAndLengths = new ArrayList<>();
        boolean queryIsVerbWithTo = mInputQuery.isVerbWithTo();

        //region Replacing the Kana input word by its romaji equivalent
        String inputQuery = mInputQuery.getOriginal();
        int inputTextType = mInputQuery.getOriginalType();
        if (inputTextType == Globals.TYPE_HIRAGANA || inputTextType == Globals.TYPE_KATAKANA) {
            inputQuery = mInputQuery.getRomajiSingleElement();
        }
        //endregion

        for (int i = 0; i < wordsList.size(); i++) {

            Word currentWord = wordsList.get(i);
            if (currentWord==null) continue;

            String language = LocaleHelper.getLanguage(getContext());
            int ranking = UtilitiesDb.getRankingFromWordAttributes(currentWord, inputQuery, queryIsVerbWithTo, language);

            long[] currentMatchingWordIndexAndLength = new long[3];
            currentMatchingWordIndexAndLength[0] = i;
            currentMatchingWordIndexAndLength[1] = ranking;

            matchingWordIndexesAndLengths.add(currentMatchingWordIndexAndLength);
        }

        //Sort the results according to total length
        if (matchingWordIndexesAndLengths.size() != 0) {
            matchingWordIndexesAndLengths = UtilitiesDb.bubbleSortForThreeIntegerList(matchingWordIndexesAndLengths);
        }

        //Return the sorted list
        List<Word> sortedWordsList = new ArrayList<>();
        for (int i = 0; i < matchingWordIndexesAndLengths.size(); i++) {
            long sortedIndex = matchingWordIndexesAndLengths.get(i)[0];
            sortedWordsList.add(wordsList.get((int) sortedIndex));
        }

        return sortedWordsList;
    }
    private void updateFirebaseDbWithJishoWords(List<Word> wordsList) {
        mFirebaseDao.updateObjectsOrCreateThemInFirebaseDb(wordsList);
    }
    private void cancelAsyncOperations() {
        if (mLocalDictSearchAsyncTask != null) mLocalDictSearchAsyncTask.cancel(true);
        if (mJishoSearchAsyncTask != null) mJishoSearchAsyncTask.cancel(true);
        if (mVerbSearchAsyncTask != null) mVerbSearchAsyncTask.cancel(true);
    }
    private void showLoadingIndicator() {
        if (mProgressBarLoadingIndicator!=null) mProgressBarLoadingIndicator.setVisibility(View.VISIBLE);
        if (mHintTextView!=null) mHintTextView.setVisibility(View.GONE);
    }
    private void hideLoadingIndicator() {
        if (mProgressBarLoadingIndicator!=null) mProgressBarLoadingIndicator.setVisibility(View.INVISIBLE);
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
        mAlreadyLoadedRoomResults = false;
        mAlreadyLoadedJishoResults = false;
        mAlreadyLoadedConjResults = false;
        getQuerySearchResults();
    }
    void setShowNames(boolean status) {
        mShowNames = status;
    }

    //Communication with Firebase DAO
    @Override public void onWordsListFound(List<Word> wordsList) {

    }

    //Communication with AsyncTasks
    @Override public void onLocalDictSearchAsyncTaskResultFound(List<Word> words) {

        if (getContext()==null) return;
        Log.i(Globals.DEBUG_TAG, "DictionaryFragment - Finished Words Search AsyncTask");

        mAlreadyLoadedRoomResults = true;

        mLocalMatchingWordsList = words;
        mLocalMatchingWordsList = sortWordsAccordingToRanking(mLocalMatchingWordsList);

        int maxIndex = Math.min(mLocalMatchingWordsList.size(), MAX_NUM_WORDS_TO_SHARE);
        dictionaryFragmentOperationsHandler.onLocalMatchingWordsFound(mLocalMatchingWordsList.subList(0,maxIndex));

        Log.i(Globals.DEBUG_TAG, "DictionaryFragment - Displaying Room words");
        displayMergedWordsToUser(LOCAL);
    }
    @Override public void onJishoSearchAsyncTaskResultFound(List<Word> loaderResultWordsList) {

        if (getContext()==null) return;

        mAlreadyLoadedJishoResults = true;
        mJishoMatchingWordsList = Utilities.removeEdictExceptionsFromJisho(mJishoMatchingWordsList);
        mJishoMatchingWordsList = Utilities.cleanUpProblematicWordsFromJisho(loaderResultWordsList);
        for (Word word : mJishoMatchingWordsList) word.setIsLocal(false);

        if (!UtilitiesPrefs.getPreferenceShowOnlineResults(getActivity())) mJishoMatchingWordsList = new ArrayList<>();

        if (mJishoMatchingWordsList.size() != 0) {
            mDifferentJishoWords = UtilitiesDb.getDifferentAsyncWords(mLocalMatchingWordsList, mJishoMatchingWordsList);
            if (mDifferentJishoWords.size()>0) {
                updateFirebaseDbWithJishoWords(UtilitiesDb.getCommonWords(mDifferentJishoWords));
                if (UtilitiesDb.wordsAreSimilar(mDifferentJishoWords.get(0), mInputQuery.getOriginal())) {
                    updateFirebaseDbWithJishoWords(mDifferentJishoWords.subList(0, 1)); //If the word was searched for then it is useful even if it's not defined as common
                }
            }
        }

        Log.i(Globals.DEBUG_TAG, "DictionaryFragment - Displaying Jisho merged words");
        displayMergedWordsToUser(ONLINE);
    }
    @Override @SuppressWarnings("unchecked") public void onVerbSearchAsyncTaskResultFound(Object[] dataElements) {

        if (getContext()==null) return;
        Log.i(Globals.DEBUG_TAG, "DictionaryFragment - Finished Verbs Search AsyncTask");

        mAlreadyLoadedConjResults = true;
        List<Verb> mMatchingVerbs = (List<Verb>) dataElements[0];
        mMatchingWordsFromVerbs = (List<Word>) dataElements[1];
        List<Object[]> mMatchingConjugationParametersList = (List<Object[]>) dataElements[2];

        //Adapting the words list to include information used for proper display in the results list
        for (int i = 0; i < mMatchingWordsFromVerbs.size(); i++) {
            Word word = mMatchingWordsFromVerbs.get(i);
            word.setIsLocal(true);
            for (Object[] matchingConjugationParameters : mMatchingConjugationParametersList) {
                if ((long) matchingConjugationParameters[VerbSearchAsyncTask.MATCHING_ID] == word.getWordId()) {
                    String matchingConjugation = (String) matchingConjugationParameters[VerbSearchAsyncTask.MATCHING_CONJUGATION];
                    word.setMatchingConj(matchingConjugation);
                    break;
                }
            }
        }

        Log.i(Globals.DEBUG_TAG, "DictionaryFragment - Displaying Verb merged words");
        displayMergedWordsToUser(CONJ);
    }
}