package com.japagram.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.japagram.R;
import com.japagram.asynctasks.DictSearchAsyncTask;
import com.japagram.data.InputQuery;
import com.japagram.data.Word;
import com.japagram.databinding.FragmentInputqueryBodyBinding;
import com.japagram.utilitiesAndroid.AndroidUtilitiesIO;
import com.japagram.utilitiesAndroid.AndroidUtilitiesPrefs;
import com.japagram.utilitiesCrossPlatform.Globals;
import com.japagram.utilitiesCrossPlatform.UtilitiesGeneral;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InputQueryFragment extends Fragment implements
        TextToSpeech.OnInitListener, DictSearchAsyncTask.LocalDictSearchAsyncResponseHandler {

    //region Parameters
    private FragmentInputqueryBodyBinding binding;
    private static final int SPEECH_RECOGNIZER_REQUEST_CODE = 101;
    private static final String TAG_PERMISSIONS = "Permission error";
    private String mInputQuery;
    private TextToSpeech tts;
    private String mChosenSpeechToTextLanguage;
    private boolean mAlreadyGotOcrResult;
    private boolean mAlreadyGotRomajiFromKanji;
    private Typeface mDroidSansJapaneseTypeface;
    //endregion

    //Fragment Lifecycle methods
    @Override public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        mInputQueryOperationsHandler = (InputQueryOperationsHandler) context;
    }
    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getExtras();
        initializeParameters();
    }
    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View rootView = inflater.inflate(R.layout.fragment_inputquery, container, false);

        setRetainInstance(true);

        if (savedInstanceState!=null) {
            mAlreadyGotOcrResult = savedInstanceState.getBoolean(getString(R.string.saved_ocr_result_state), false);
            mAlreadyGotRomajiFromKanji = savedInstanceState.getBoolean(getString(R.string.saved_romaji_from_kanji_state), false);
        }

        return rootView;
    }
    @Override public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding = FragmentInputqueryBodyBinding.bind(view);
        initializeViews();
    }
    @Override public void onResume() {
        super.onResume();
        binding.query.setText(mInputQuery);
        if (getActivity()!=null) AndroidUtilitiesIO.hideSoftKeyboard(getActivity());
    }

    @Override public void onPause() {
        super.onPause();
        mInputQuery = binding.query.getText().toString();

    }
    @Override public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        if (getActivity() != null) {
            EditText query = getActivity().findViewById(R.id.query);
            savedInstanceState.putString("inputQueryAutoCompleteTextView", query.getText().toString());
            savedInstanceState.putBoolean(getActivity().getString(R.string.saved_ocr_result_state), mAlreadyGotOcrResult);
            savedInstanceState.putBoolean(getActivity().getString(R.string.saved_romaji_from_kanji_state), mAlreadyGotRomajiFromKanji);

        }
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override public void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

    }
    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPEECH_RECOGNIZER_REQUEST_CODE) {

            if (data == null) return;
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results == null || results.isEmpty()) return;

            mInputQuery = results.get(0);
            binding.query.setText(mInputQuery);

            startRomajiFromKanjiThread();
        }
    }
    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.e(TAG_PERMISSIONS,"Returned from permission request.");
        }
    }


    //Functionality methods
    private void getExtras() {
        if (getArguments()!=null) {
            mChosenSpeechToTextLanguage = getArguments().getString(getString(R.string.pref_preferred_STT_language));
        }
    }
    private void initializeParameters() {

        mInputQuery = "";
        mAlreadyGotRomajiFromKanji = false;

        tts = new TextToSpeech(getContext(), this);
        if (getContext() == null) return;
        Runnable instantiateRunnable = () -> {
            //Setting the Typeface
            AssetManager am = getContext().getApplicationContext().getAssets();
            mDroidSansJapaneseTypeface = AndroidUtilitiesPrefs.getPreferenceUseJapaneseFont(getActivity()) ?
                    Typeface.createFromAsset(am, String.format(Locale.JAPAN, "fonts/%s", "DroidSansJapanese.ttf")) : Typeface.DEFAULT;
        };
        Thread instantiateThread = new Thread(instantiateRunnable);
        instantiateThread.start();
    }
    private void initializeViews() {

        if (getContext()==null || getActivity() == null) return;

        binding.buttonDict.setOnClickListener(view1 -> onDictButtonClick());
        binding.buttonConj.setOnClickListener(view1 -> onSearchVerbButtonClick());
        binding.buttonConvert.setOnClickListener(view1 -> onConvertButtonClick());
        binding.buttonSearchByRadical.setOnClickListener(view1 -> onSearchByRadicalButtonClick());
        binding.buttonDecompose.setOnClickListener(view1 -> onDecomposeButtonClick());
        binding.buttonClearQuery.setOnClickListener(view1 -> onClearQueryButtonClick());
        binding.buttonShowHistory.setOnClickListener(view1 -> onShowHistoryButtonClick());
        binding.buttonSpeechToText.setOnClickListener(view1 -> onSpeechToTextButtonClick());
        binding.buttonTextToSpeech.setOnClickListener(view1 -> onTextToSpeechButtonClick());

        binding.query.setText(mInputQuery);
        binding.query.setTypeface(mDroidSansJapaneseTypeface);
        binding.query.setTextColor(AndroidUtilitiesPrefs.getResColorValue(getContext(), R.attr.colorPrimaryNormal));

        binding.query.setOnEditorActionListener((exampleView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                mInputQuery = binding.query.getText().toString();

                binding.query.dismissDropDown();

                registerThatUserIsRequestingDictSearch(true);

                drawBorderAroundThisButton(binding.buttonDict);
                mInputQueryOperationsHandler.onDictRequested(mInputQuery);
            }
            return true;
        });

        binding.buttonSearchByRadical.setEnabled(true);
        binding.buttonDecompose.setEnabled(true);

        //https://stackoverflow.com/questions/36661068/edittext-drwableleft-dont-work-with-vectors
        Resources resources = getActivity().getResources();
        binding.buttonClearQuery.setCompoundDrawablesRelativeWithIntrinsicBounds(
                VectorDrawableCompat.create(resources, R.drawable.ic_clear_black_24dp, binding.buttonClearQuery.getContext().getTheme()),
                null, null, null);
        binding.buttonShowHistory.setCompoundDrawablesRelativeWithIntrinsicBounds(
                VectorDrawableCompat.create(resources, R.drawable.ic_history_black_24dp, binding.buttonShowHistory.getContext().getTheme()),
                null, null, null);

    }
    private void drawBorderAroundThisButton(Button button) {

        if (getActivity() == null) return;

        binding.buttonDict.setBackgroundResource(0);
        binding.buttonConj.setBackgroundResource(0);
        binding.buttonConvert.setBackgroundResource(0);
        binding.buttonSearchByRadical.setBackgroundResource(0);
        binding.buttonSearchByRadical.setBackgroundResource(0);
        binding.buttonDecompose.setBackgroundResource(0);

        TypedValue typedValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr.inputQuery_buttonBackground, typedValue, true);
        if (button.getId() == binding.buttonDict.getId()) binding.buttonDict.setBackgroundResource(typedValue.resourceId);
        else if (button.getId() == binding.buttonConj.getId()) binding.buttonConj.setBackgroundResource(typedValue.resourceId);
        else if (button.getId() == binding.buttonConvert.getId()) binding.buttonConvert.setBackgroundResource(typedValue.resourceId);
        else if (button.getId() == binding.buttonSearchByRadical.getId()) binding.buttonSearchByRadical.setBackgroundResource(typedValue.resourceId);
        else if (button.getId() == binding.buttonDecompose.getId()) binding.buttonDecompose.setBackgroundResource(typedValue.resourceId);
    }
    @NotNull private List<String> getQueryHistoryWordsOnly(@NotNull List<String> queryHistory) {
        List<String> result = new ArrayList<>();
        for (String item : queryHistory) {
            result.add(item.split(Globals.QUERY_HISTORY_MEANINGS_DELIMITER)[0].trim());
        }
        return result;
    }


    //View click listeners
    public void onDictButtonClick() {
        if (getActivity()==null || getContext()==null) return;

        String inputWordString = binding.query.getText().toString();
        mInputQuery = inputWordString;

        List<String> queryHistory = MainActivity.updateQueryHistoryWithQueryRomajiMeaning(getContext(), mInputQuery, "", "");
        binding.query.setAdapter(new QueryInputSpinnerAdapter(getContext(), R.layout.spinner_item_queryhistory, getQueryHistoryWordsOnly(queryHistory), queryHistory));
        registerThatUserIsRequestingDictSearch(true); //TODO: remove this?
        drawBorderAroundThisButton(binding.buttonDict);

        mInputQueryOperationsHandler.onDictRequested(inputWordString);
    }
    public void onSearchVerbButtonClick() {
        if (getActivity()==null || getContext()==null) return;
        String inputVerbString = binding.query.getText().toString();
        mInputQuery = inputVerbString;

        List<String> queryHistory = MainActivity.updateQueryHistoryWithQueryRomajiMeaning(getContext(), mInputQuery, "", "");
        binding.query.setAdapter(new QueryInputSpinnerAdapter(getContext(), R.layout.spinner_item_queryhistory, getQueryHistoryWordsOnly(queryHistory), queryHistory));

        registerThatUserIsRequestingDictSearch(false); //TODO: remove this?

        drawBorderAroundThisButton(binding.buttonConj);
        mInputQueryOperationsHandler.onConjRequested(inputVerbString);

    }
    public void onConvertButtonClick() {
        if (getActivity()==null || getContext()==null) return;
        mInputQuery = binding.query.getText().toString();

        List<String> queryHistory = MainActivity.updateQueryHistoryWithQueryRomajiMeaning(getContext(), mInputQuery, "", "");
        binding.query.setAdapter(new QueryInputSpinnerAdapter(getContext(), R.layout.spinner_item_queryhistory, getQueryHistoryWordsOnly(queryHistory), queryHistory));

        drawBorderAroundThisButton(binding.buttonConvert);
        mInputQueryOperationsHandler.onConvertRequested(mInputQuery);
    }
    public void onSearchByRadicalButtonClick() {
        if (getActivity()==null || getContext()==null) return;
        // Break up a Kanji to Radicals

        mInputQuery = binding.query.getText().toString();

        List<String> queryHistory = MainActivity.updateQueryHistoryWithQueryRomajiMeaning(getContext(), mInputQuery, "", "");
        binding.query.setAdapter(new QueryInputSpinnerAdapter(getContext(), R.layout.spinner_item_queryhistory, getQueryHistoryWordsOnly(queryHistory), queryHistory));

        drawBorderAroundThisButton(binding.buttonSearchByRadical);
        mInputQueryOperationsHandler.onSearchByRadicalRequested(UtilitiesGeneral.removeSpecialCharacters(mInputQuery));
    }
    public void onDecomposeButtonClick() {
        if (getActivity()==null || getContext()==null) return;
        mInputQuery = binding.query.getText().toString();

        List<String> queryHistory = MainActivity.updateQueryHistoryWithQueryRomajiMeaning(getContext(), mInputQuery, "", "");
        binding.query.setAdapter(new QueryInputSpinnerAdapter(getContext(), R.layout.spinner_item_queryhistory, getQueryHistoryWordsOnly(queryHistory), queryHistory));

        drawBorderAroundThisButton(binding.buttonDecompose);
        mInputQueryOperationsHandler.onDecomposeRequested(UtilitiesGeneral.removeSpecialCharacters(mInputQuery));
    }
    public void onClearQueryButtonClick() {
        if (getActivity()==null || getContext()==null) return;
        binding.query.setText("");
        mInputQuery = "";
    }
    public void onShowHistoryButtonClick() {
        if (getActivity()==null || getContext()==null) return;
        AndroidUtilitiesIO.hideSoftKeyboard(getActivity());

        List<String> queryHistory = MainActivity.getQueryHistoryFromPreferences(getContext());
        boolean queryHistoryIsEmpty = true;
        for (String element : queryHistory) {
            if (!element.isEmpty()) { queryHistoryIsEmpty = false; break; }
        }
        if (!queryHistoryIsEmpty) {
            binding.query.setAdapter(new QueryInputSpinnerAdapter(getContext(), R.layout.spinner_item_queryhistory, getQueryHistoryWordsOnly(queryHistory), queryHistory));
            binding.query.showDropDown();
        }

    }
    public void onSpeechToTextButtonClick() {

        if (getActivity()==null || getContext()==null) {
            return;
        }

        int maxResultsToReturn = 1;
        try {
            //Getting the user setting from the preferences
            if (getActivity()==null) return;
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String language = sharedPreferences.getString(getString(R.string.pref_preferred_STT_language_key), getString(R.string.pref_language_value_japanese));
            mChosenSpeechToTextLanguage = getLanguageLocale(language);

            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, maxResultsToReturn);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, mChosenSpeechToTextLanguage);
            intent.putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, getResources().getString(R.string.languageLocaleEnglishUS));
            if (mChosenSpeechToTextLanguage.equals(getResources().getString(R.string.languageLocaleJapanese))) {
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getResources().getString(R.string.SpeechToTextUserPromptJapanese));
            } else if (mChosenSpeechToTextLanguage.equals(getResources().getString(R.string.languageLocaleEnglishUS))) {
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getResources().getString(R.string.SpeechToTextUserPromptEnglish));
            } else if (mChosenSpeechToTextLanguage.equals(getResources().getString(R.string.languageLocaleFrench))) {
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getResources().getString(R.string.SpeechToTextUserPromptFrench));
            } else if (mChosenSpeechToTextLanguage.equals(getResources().getString(R.string.languageLocaleSpanish))) {
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getResources().getString(R.string.SpeechToTextUserPromptSpanish));
            }
            startActivityForResult(intent, SPEECH_RECOGNIZER_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(),getResources().getString(R.string.STTACtivityNotFound),Toast.LENGTH_SHORT).show();
            String appPackageName = "com.google.android.tts";
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,   Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
            startActivity(browserIntent);
        }

    }
    public void onTextToSpeechButtonClick() {
        if (getActivity()==null || getContext()==null) return;

        String queryString = binding.query.getText().toString();
        mInputQuery = queryString;
        speakOut(queryString);

    }
    private void startRomajiFromKanjiThread() {

        if (getActivity() != null && getContext() != null) {
            Log.i(Globals.DEBUG_TAG, "DictionaryFragment - Starting search for Romaji values of words recognized by speech");
            DictSearchAsyncTask mLocalDictSearchAsyncTask = new DictSearchAsyncTask(getContext(), new InputQuery(mInputQuery), this, false);
            mLocalDictSearchAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

    }

    //TextToSpeech methods
    @Override public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            setTTSLanguage();
        } else {
            Log.e("TTS", "Initialization Failed!");
        }
    }
    private void setTTSLanguage() {
        int result;
        //Getting the user setting from the preferences
        if (getActivity() != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String language = sharedPreferences.getString(getString(R.string.pref_preferred_TTS_language_key), getString(R.string.pref_language_value_japanese));
            String mChosenTextToSpeechLanguage = getLanguageLocale(language);

            //Setting the language
            if (mChosenTextToSpeechLanguage.equals(getResources().getString(R.string.languageLocaleJapanese))) {
                result = tts.setLanguage(Locale.JAPAN);
            } else if (mChosenTextToSpeechLanguage.equals(getResources().getString(R.string.languageLocaleEnglishUS))) {
                result = tts.setLanguage(Locale.US);
            } else if (mChosenTextToSpeechLanguage.equals(getResources().getString(R.string.languageLocaleFrench))) {
                result = tts.setLanguage(Locale.FRANCE);
            } else if (mChosenTextToSpeechLanguage.equals(getResources().getString(R.string.languageLocaleSpanish))) {
                Locale spanish = new Locale("es", "ES");
                result = tts.setLanguage(spanish);
            } else result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported, set to default English.");
                tts.setLanguage(Locale.US);
            }
        }
    }
    private void speakOut(String text) {
        setTTSLanguage();
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }


    //SpeechToText methods
    @NotNull private String getLanguageLocale(@NotNull String language) {
        if (language.equals(getResources().getString(R.string.pref_language_value_japanese))) {
            return getResources().getString(R.string.languageLocaleJapanese);
        }
        else if (language.equals(getResources().getString(R.string.pref_language_value_english))) {
            return getResources().getString(R.string.languageLocaleEnglishUS);
        }
        else if (language.equals(getResources().getString(R.string.pref_language_value_french))) {
            return getResources().getString(R.string.languageLocaleFrench);
        }
        else if (language.equals(getResources().getString(R.string.pref_language_value_french))) {
            return getResources().getString(R.string.languageLocaleFrench);
        }
        else return getResources().getString(R.string.languageLocaleEnglishUS);
    }


    //Query input methods
    private class QueryInputSpinnerAdapter extends ArrayAdapter<String> {
        // Code adapted from http://mrbool.com/how-to-customize-spinner-in-android/28286
        List<String> mQueryHistory;
        QueryInputSpinnerAdapter(Context ctx, int txtViewResourceId, List<String> queries, List<String> queriesRomajiMeaning) {
            super(ctx, txtViewResourceId, queries);
            mQueryHistory = queriesRomajiMeaning;
        }
        @Override
        public View getDropDownView(int position, View cnvtView, @NonNull ViewGroup prnt) {
            return getCustomView(position, cnvtView, prnt);
        }
        @NonNull
        @Override
        public View getView(int pos, View cnvtView, @NonNull ViewGroup prnt) {
            return getCustomView(pos, cnvtView, prnt);
        }
        View getCustomView(int position, View convertView, ViewGroup parent) {

            if (getActivity() != null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                View layout = inflater.inflate(R.layout.spinner_item_queryhistory, parent, false);
                TextView queryHistoryElement = layout.findViewById(R.id.query_value);
                queryHistoryElement.setText(mQueryHistory.get(position).replace(Globals.QUERY_HISTORY_MEANINGS_DELIMITER, Globals.QUERY_HISTORY_MEANINGS_DISPLAYED_DELIMITER));
                queryHistoryElement.setMaxLines(1);
                queryHistoryElement.setEllipsize(TextUtils.TruncateAt.END);
                queryHistoryElement.setTypeface(mDroidSansJapaneseTypeface);
                queryHistoryElement.setGravity(View.TEXT_ALIGNMENT_CENTER|View.TEXT_ALIGNMENT_TEXT_START);
                queryHistoryElement.setTextColor(AndroidUtilitiesPrefs.getResColorValue(getContext(), R.attr.colorPrimaryNormal));
                return layout;
            }
            else return null;
        }

    }
    private void registerThatUserIsRequestingDictSearch(Boolean state) {
        if (getActivity() != null) {
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(getString(R.string.requestingDictSearch), state);
            editor.apply();
        }
    }


    //Communication with other classes:
    @Override public void onLocalDictSearchAsyncTaskResultFound(@NotNull List<Word> words) {
        mInputQuery = words.get(0).getRomaji();
        binding.query.setText(mInputQuery);
    }

    //Communication with parent activity:
    private InputQueryOperationsHandler mInputQueryOperationsHandler;
    interface InputQueryOperationsHandler {
        void onDictRequested(String query);
        void onConjRequested(String query);
        void onConvertRequested(String query);
        void onSearchByRadicalRequested(String query);
        void onDecomposeRequested(String query);
    }
    public void setQuery(String query) {
        binding.query.setText(query);
    }
    public void setAppendedQuery(String addedText) {
        mInputQuery = binding.query.getText().toString();
        String newQuery = mInputQuery + addedText;
        binding.query.setText(newQuery);
    }
    public void setConjButtonSelected() {
        drawBorderAroundThisButton(binding.buttonConj);
    }
    public void setSTTLanguage(String chosenSpeechToTextLanguage) {
        mChosenSpeechToTextLanguage = chosenSpeechToTextLanguage;
    }
    public void clearHistory() {
        MainActivity.saveQueryHistoryToPreferences(getContext(), new ArrayList<>());
    }
}