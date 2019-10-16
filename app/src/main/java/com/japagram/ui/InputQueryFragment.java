package com.japagram.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.japagram.R;
import com.japagram.data.Word;
import com.japagram.resources.GlobalConstants;
import com.japagram.resources.Utilities;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.content.Context.DOWNLOAD_SERVICE;

public class InputQueryFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<String>,
        TextToSpeech.OnInitListener {

    //region Parameters
    @BindView(R.id.query) AutoCompleteTextView mInputQueryAutoCompleteTextView;
    @BindView(R.id.button_dict) Button mDictButton;
    @BindView(R.id.button_conj) Button mConjButton;
    @BindView(R.id.button_convert) Button mConvertButton;
    @BindView(R.id.button_search_by_radical) Button mSearchByRadicalButton;
    @BindView(R.id.button_decompose) Button mDecomposeButton;
    @BindView(R.id.button_show_history) Button mShowHistoryButton;
    @BindView(R.id.button_clear_query) Button mClearQueryButton;
    private static final boolean ALLOW_OCR_FOR_LATIN_LANGUAGES = false;
    private static final int MAX_OCR_DIALOG_RECYCLERVIEW_HEIGHT_DP = 150;
    private static final int RESULT_OK = -1;
    private static final int SPEECH_RECOGNIZER_REQUEST_CODE = 101;
    private static final int ADJUST_IMAGE_ACTIVITY_REQUEST_CODE = 201;
    private static final int GET_ROMAJI_FROM_KANJI_USING_JISHO_LOADER = 42;
    private static final int TESSERACT_OCR_LOADER = 69;
    private static final String SPEECH_RECOGNIZER_EXTRA = "inputQueryAutoCompleteTextView";
    private static final String TAG_TESSERACT = "tesseract_ocr";
    private static final String TAG_PERMISSIONS = "Permission error";
    private static final String DOWNLOAD_FILE_PREFS = "download_file_prefs";
    private static final String JPN_FILE_DOWNLOADING_FLAG = "jpn_file_downloading";
    private static final String ENG_FILE_DOWNLOADING_FLAG = "eng_file_downloading";
    private static final String FRA_FILE_DOWNLOADING_FLAG = "fra_file_downloading";
    private static final String SPA_FILE_DOWNLOADING_FLAG = "spa_file_downloading";
    private String mInputQuery;
    private Bitmap mImageToBeDecoded;
    private TessBaseAPI mTess;
    private String mInternalStorageTesseractFolderPath = "";
    private boolean mInitializedOcrApiJpn;
    private boolean mInitializedOcrApiEng;
    private boolean mInitializedOcrApiFra;
    private boolean mInitializedOcrApiSpa;
    private String mOCRLanguage;
    private Uri mPhotoURI;
    private String mOcrResultString;
    private boolean firstTimeInitializedJpn;
    private boolean firstTimeInitializedEng;
    private boolean firstTimeInitializedFra;
    private boolean firstTimeInitializedSpa;
    private TextToSpeech tts;
    private long enqueue;
    private DownloadManager downloadmanager;
    private boolean hasStoragePermissions;
    private String mPhoneAppFolderTesseractDataFilepath;
    private String mDownloadsFolder;
    private int timesPressed;
    private boolean jpnOcrDataIsAvailable;
    private boolean engOcrDataIsAvailable;
    private boolean fraOcrDataIsAvailable;
    private boolean spaOcrDataIsAvailable;
    private String mLanguageBeingDownloadedLabel;
    private String mLanguageBeingDownloaded;
    private AsyncTask mTesseractOCRAsyncTask;
    private String mChosenSpeechToTextLanguage;
    private String mDownloadType;
    private boolean jpnOcrFileISDownloading;
    private boolean mJpnOcrFileIsDownloading;
    private boolean mEngOcrFileIsDownloading;
    private boolean mFraOcrFileIsDownloading;
    private boolean mSpaOcrFileIsDownloading;
    private CropImage.ActivityResult mCropImageResult;
    private BroadcastReceiver mBroadcastReceiver;
    private boolean mRequestedSpeechToText;
    private ProgressDialog mProgressDialog;
    private Unbinder mBinding;
    private boolean mAlreadyGotOcrResult;
    private boolean mAlreadyGotRomajiFromKanji;
    private Typeface mDroidSansJapaneseTypeface;
    private int mMaxOCRDialogResultHeightPixels;
    //endregion


    //Fragment Lifecycle methods
    @Override public void onAttach(Context context) {
        super.onAttach(context);
        mInputQueryOperationsHandler = (InputQueryOperationsHandler) context;
    }
    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getExtras();
        initializeParameters();
        setupOcr();
    }
    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View rootView = inflater.inflate(R.layout.fragment_inputquery, container, false);

        setRetainInstance(true);
        initializeViews(rootView);

        if (savedInstanceState!=null) {
            mAlreadyGotOcrResult = savedInstanceState.getBoolean(getString(R.string.saved_ocr_result_state), false);
            mAlreadyGotRomajiFromKanji = savedInstanceState.getBoolean(getString(R.string.saved_romaji_from_kanji_state), false);
        }

        mMaxOCRDialogResultHeightPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MAX_OCR_DIALOG_RECYCLERVIEW_HEIGHT_DP, getResources().getDisplayMetrics());

        return rootView;
    }
    @Override public void onResume() {
        super.onResume();

        mInputQueryAutoCompleteTextView.setText(mInputQuery);
        if (getActivity()!=null) Utilities.hideSoftKeyboard(getActivity());

        getLanguageParametersFromSettingsAndReinitializeOcrIfNecessary();
    }

    @Override public void onPause() {
        super.onPause();
        mInputQuery = mInputQueryAutoCompleteTextView.getText().toString();

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
        mBinding.unbind();
    }
    @Override public void onDestroy() {
        super.onDestroy();
        if (mTess != null) mTess.end();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (getActivity() != null && mBroadcastReceiver != null) getActivity().unregisterReceiver(mBroadcastReceiver);

    }
    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPEECH_RECOGNIZER_REQUEST_CODE) {

            if (data == null) return;
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results == null || results.size()==0) return;

            mInputQuery = results.get(0);
            mInputQueryAutoCompleteTextView.setText(mInputQuery);

            //Attempting to access jisho.org to get the romaji value of the requested word, if it's a Japanese word
            if (getActivity() != null && mChosenSpeechToTextLanguage.equals(getResources().getString(R.string.languageLocaleJapanese))) {
                Bundle queryBundle = new Bundle();
                queryBundle.putString(SPEECH_RECOGNIZER_EXTRA, results.get(0));

                mAlreadyGotRomajiFromKanji = false;
                LoaderManager loaderManager = getActivity().getSupportLoaderManager();
                Loader<String> webSearchLoader = loaderManager.getLoader(GET_ROMAJI_FROM_KANJI_USING_JISHO_LOADER);
                if (webSearchLoader == null)
                    loaderManager.initLoader(GET_ROMAJI_FROM_KANJI_USING_JISHO_LOADER, queryBundle, this);
                else loaderManager.restartLoader(GET_ROMAJI_FROM_KANJI_USING_JISHO_LOADER, queryBundle, this);
            }
        }
        else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            Utilities.unmuteSpeaker(getActivity());
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mCropImageResult = result;
                sendImageToImageAdjuster(mCropImageResult);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                try {
                    throw error;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else if (requestCode == ADJUST_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Uri adjustedImageUri = Uri.parse(extras.getString("returnImageUri"));
                    mImageToBeDecoded = Utilities.getBitmapFromUri(getActivity(), adjustedImageUri);
                    getOcrTextWithTesseractAndDisplayDialog(mImageToBeDecoded);
                }
            }
        }
    }
    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            hasStoragePermissions = true;

            Log.e(TAG_PERMISSIONS,"Returned from permission request.");
            makeOcrDataAvailableInAppFolder(mLanguageBeingDownloaded);
            if (!mInitializedOcrApiJpn) initializeOcrEngineForChosenLanguage();
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
        mOcrResultString = "";
        firstTimeInitializedJpn = true;
        firstTimeInitializedEng = true;
        firstTimeInitializedFra = true;
        firstTimeInitializedSpa = true;
        mInitializedOcrApiJpn = false;
        mInitializedOcrApiEng = false;
        mInitializedOcrApiFra = false;
        mInitializedOcrApiSpa = false;
        timesPressed = 0;
        jpnOcrDataIsAvailable = false;
        engOcrDataIsAvailable = false;
        fraOcrDataIsAvailable = false;
        spaOcrDataIsAvailable = false;
        mRequestedSpeechToText = false;
        mCropImageResult = null;
        mAlreadyGotOcrResult = false;
        mAlreadyGotRomajiFromKanji = false;

        if (getContext() == null) return;
        //Setting the Typeface
        AssetManager am = getContext().getApplicationContext().getAssets();
        mDroidSansJapaneseTypeface = Utilities.getPreferenceUseJapaneseFont(getActivity()) ?
                Typeface.createFromAsset(am, String.format(Locale.JAPAN, "fonts/%s", "DroidSansJapanese.ttf")) : Typeface.DEFAULT;
    }
    @SuppressLint("ClickableViewAccessibility") private void initializeViews(View rootView) {

        if (getContext()==null || getActivity() == null) return;

        mBinding = ButterKnife.bind(this, rootView);

        mInputQueryAutoCompleteTextView.setText(mInputQuery);
        mInputQueryAutoCompleteTextView.setTypeface(mDroidSansJapaneseTypeface);
        mInputQueryAutoCompleteTextView.setTextColor(Utilities.getResColorValue(getContext(), R.attr.appTextPrimaryColor));

        mInputQueryAutoCompleteTextView.setOnEditorActionListener((exampleView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                mInputQuery = mInputQueryAutoCompleteTextView.getText().toString();

                mInputQueryAutoCompleteTextView.dismissDropDown();

                registerThatUserIsRequestingDictSearch(true);

                drawBorderAroundThisButton(mDictButton);
                mInputQueryOperationsHandler.onDictRequested(mInputQuery);
            }
            return true;
        });

        mSearchByRadicalButton.setEnabled(true);
        mDecomposeButton.setEnabled(true);

        //https://stackoverflow.com/questions/36661068/edittext-drwableleft-dont-work-with-vectors
        mClearQueryButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                VectorDrawableCompat.create(getActivity().getResources(), R.drawable.ic_clear_black_24dp, mClearQueryButton.getContext().getTheme()),
                null, null, null);
        mShowHistoryButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                VectorDrawableCompat.create(getActivity().getResources(), R.drawable.ic_history_black_24dp, mClearQueryButton.getContext().getTheme()),
                null, null, null);

    }
    private void sendImageToImageAdjuster(CropImage.ActivityResult result) {
        mPhotoURI = result.getUri();
        mImageToBeDecoded = Utilities.getBitmapFromUri(getActivity(), mPhotoURI);

        //Send the image Uri to the AdjustImageActivity
        Intent intent = new Intent(getActivity(), AdjustImageActivity.class);
        intent.putExtra("imageUri", mPhotoURI.toString());
        startActivityForResult(intent, ADJUST_IMAGE_ACTIVITY_REQUEST_CODE);
    }
    private void performImageCaptureAndCrop() {

        // start source picker (camera, gallery, etc..) to get image for cropping and then use the image in cropping activity
        //CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(getActivity());

        Utilities.muteSpeaker(getActivity());
        if (getContext() != null) CropImage.activity().start(getContext(), this); //For FragmentActivity use

    }
    private Bitmap adjustImageAfterOCR(Bitmap imageToBeDecoded) {
        //imageToBeDecoded = Utilities.adjustImageAngleAndScale(imageToBeDecoded, 0, 0.5);
        return imageToBeDecoded;
    }
    private void drawBorderAroundThisButton(Button button) {

        if (mDictButton==null) return;

        mDictButton.setBackgroundResource(0);
        mConjButton.setBackgroundResource(0);
        mConvertButton.setBackgroundResource(0);
        mSearchByRadicalButton.setBackgroundResource(0);
        mDecomposeButton.setBackgroundResource(0);

        if (button.getId() == mDictButton.getId()) mDictButton.setBackgroundResource(R.drawable.background_full_no_borders);
        else if (button.getId() == mConjButton.getId()) mConjButton.setBackgroundResource(R.drawable.background_full_no_borders);
        else if (button.getId() == mConvertButton.getId()) mConvertButton.setBackgroundResource(R.drawable.background_full_no_borders);
        else if (button.getId() == mSearchByRadicalButton.getId()) mSearchByRadicalButton.setBackgroundResource(R.drawable.background_full_no_borders);
        else if (button.getId() == mDecomposeButton.getId()) mDecomposeButton.setBackgroundResource(R.drawable.background_full_no_borders);
    }
    private List<String> getQueryHistoryWordsOnly(List<String> queryHistory) {
        List<String> result = new ArrayList<>();
        for (String item : queryHistory) {
            result.add(item.split(GlobalConstants.QUERY_HISTORY_MEANINGS_DELIMITER)[0].trim());
        }
        return result;
    }

    //View click listeners
    @OnClick(R.id.button_dict) public void onDictButtonClick() {
        if (getActivity()==null || getContext()==null) return;

        String inputWordString = mInputQueryAutoCompleteTextView.getText().toString();
        mInputQuery = inputWordString;

        List<String> queryHistory = MainActivity.updateQueryHistoryWithQueryRomajiMeaning(getContext(), mInputQuery, "", "");
        mInputQueryAutoCompleteTextView.setAdapter(new QueryInputSpinnerAdapter(getContext(), R.layout.spinner_item_queryhistory, getQueryHistoryWordsOnly(queryHistory), queryHistory));
        registerThatUserIsRequestingDictSearch(true); //TODO: remove this?
        drawBorderAroundThisButton(mDictButton);

        mInputQueryOperationsHandler.onDictRequested(inputWordString);
    }
    @OnClick(R.id.button_conj) public void onSearchVerbButtonClick() {
        if (getActivity()==null || getContext()==null) return;
        String inputVerbString = mInputQueryAutoCompleteTextView.getText().toString();
        mInputQuery = inputVerbString;

        List<String> queryHistory = MainActivity.updateQueryHistoryWithQueryRomajiMeaning(getContext(), mInputQuery, "", "");
        mInputQueryAutoCompleteTextView.setAdapter(new QueryInputSpinnerAdapter(getContext(), R.layout.spinner_item_queryhistory, getQueryHistoryWordsOnly(queryHistory), queryHistory));

        registerThatUserIsRequestingDictSearch(false); //TODO: remove this?

        drawBorderAroundThisButton(mConjButton);
        mInputQueryOperationsHandler.onConjRequested(inputVerbString);

    }
    @OnClick(R.id.button_convert) public void onConvertButtonClick() {
        if (getActivity()==null || getContext()==null) return;
        mInputQuery = mInputQueryAutoCompleteTextView.getText().toString();

        List<String> queryHistory = MainActivity.updateQueryHistoryWithQueryRomajiMeaning(getContext(), mInputQuery, "", "");
        mInputQueryAutoCompleteTextView.setAdapter(new QueryInputSpinnerAdapter(getContext(), R.layout.spinner_item_queryhistory, getQueryHistoryWordsOnly(queryHistory), queryHistory));

        drawBorderAroundThisButton(mConvertButton);
        mInputQueryOperationsHandler.onConvertRequested(mInputQuery);
    }
    @OnClick(R.id.button_search_by_radical) public void onSearchByRadicalButtonClick() {
        if (getActivity()==null || getContext()==null) return;
        // Break up a Kanji to Radicals

        mInputQuery = mInputQueryAutoCompleteTextView.getText().toString();

        List<String> queryHistory = MainActivity.updateQueryHistoryWithQueryRomajiMeaning(getContext(), mInputQuery, "", "");
        mInputQueryAutoCompleteTextView.setAdapter(new QueryInputSpinnerAdapter(getContext(), R.layout.spinner_item_queryhistory, getQueryHistoryWordsOnly(queryHistory), queryHistory));

        drawBorderAroundThisButton(mSearchByRadicalButton);
        mInputQueryOperationsHandler.onSearchByRadicalRequested(Utilities.removeSpecialCharacters(mInputQuery));
    }
    @OnClick(R.id.button_decompose) public void onDecomposeButtonClick() {
        if (getActivity()==null || getContext()==null) return;
        mInputQuery = mInputQueryAutoCompleteTextView.getText().toString();

        List<String> queryHistory = MainActivity.updateQueryHistoryWithQueryRomajiMeaning(getContext(), mInputQuery, "", "");
        mInputQueryAutoCompleteTextView.setAdapter(new QueryInputSpinnerAdapter(getContext(), R.layout.spinner_item_queryhistory, getQueryHistoryWordsOnly(queryHistory), queryHistory));

        drawBorderAroundThisButton(mDecomposeButton);
        mInputQueryOperationsHandler.onDecomposeRequested(Utilities.removeSpecialCharacters(mInputQuery));
    }
    @OnClick(R.id.button_clear_query) public void onClearQueryButtonClick() {
        if (getActivity()==null || getContext()==null) return;
        mInputQueryAutoCompleteTextView.setText("");
        mInputQuery = "";
    }
    @OnClick(R.id.button_show_history) public void onShowHistoryButtonClick() {
        if (getActivity()==null || getContext()==null) return;
        Utilities.hideSoftKeyboard(getActivity());

        List<String> queryHistory = MainActivity.getQueryHistoryFromPreferences(getContext());
        boolean queryHistoryIsEmpty = true;
        for (String element : queryHistory) {
            if (!element.equals("")) { queryHistoryIsEmpty = false; break; }
        }
        if (!queryHistoryIsEmpty) {
            mInputQueryAutoCompleteTextView.setAdapter(new QueryInputSpinnerAdapter(getContext(), R.layout.spinner_item_queryhistory, getQueryHistoryWordsOnly(queryHistory), queryHistory));
            mInputQueryAutoCompleteTextView.showDropDown();
        }

    }
    @OnClick(R.id.button_speech_to_text) public void onSpeechToTextButtonClick() {
        if (getActivity()==null || getContext()==null) return;

        int maxResultsToReturn = 1;
        try {
            //Getting the user setting from the preferences
            if (getActivity()==null) return;
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String language = sharedPreferences.getString(getString(R.string.pref_preferred_STT_language_key), getString(R.string.pref_language_value_japanese));
            mChosenSpeechToTextLanguage = getLanguageLocale(language!=null? language : getString(R.string.pref_language_value_japanese));

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
    @OnClick(R.id.button_ocr) public void onOcrButtonClick() {
        if (getActivity()==null || getContext()==null) return;

        getOcrDataDownloadingStatus();
        if ((mOCRLanguage.equals("eng") && mEngOcrFileIsDownloading)
                || (mOCRLanguage.equals("jpn") && mJpnOcrFileIsDownloading)
                || (mOCRLanguage.equals("fra") && mFraOcrFileIsDownloading)
                || (mOCRLanguage.equals("spa") && mSpaOcrFileIsDownloading) ) {
            Toast toast = Toast.makeText(getContext(),getResources().getString(R.string.OCR_downloading), Toast.LENGTH_SHORT);
            toast.show();
        }
        else {
            if (mInitializedOcrApiJpn && mOCRLanguage.equals("jpn")) {
                if (firstTimeInitializedJpn) {
                    Toast toast = Toast.makeText(getActivity(), getResources().getString(R.string.OCRinstructions), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();
                    firstTimeInitializedJpn = false;
                }
                timesPressed = 0;
                performImageCaptureAndCrop();
            } else if (mInitializedOcrApiEng && mOCRLanguage.equals("eng")) {
                if (firstTimeInitializedEng) {
                    Toast toast = Toast.makeText(getActivity(), getResources().getString(R.string.OCRinstructions), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();
                    firstTimeInitializedEng = false;
                }
                timesPressed = 0;
                performImageCaptureAndCrop();
            } else if (mInitializedOcrApiFra && mOCRLanguage.equals("fra")) {
                if (firstTimeInitializedFra) {
                    Toast toast = Toast.makeText(getActivity(), getResources().getString(R.string.OCRinstructions), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();
                    firstTimeInitializedFra = false;
                }
                timesPressed = 0;
                performImageCaptureAndCrop();
            } else if (mInitializedOcrApiSpa && mOCRLanguage.equals("spa")) {
                if (firstTimeInitializedSpa) {
                    Toast toast = Toast.makeText(getActivity(), getResources().getString(R.string.OCRinstructions), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();
                    firstTimeInitializedSpa = false;
                }
                timesPressed = 0;
                performImageCaptureAndCrop();
            } else {
                if (timesPressed <= 3) {
                    mLanguageBeingDownloaded = "jpn";
                    ifOcrDataIsNotAvailableThenMakeItAvailable(mLanguageBeingDownloaded);
                    if (ALLOW_OCR_FOR_LATIN_LANGUAGES) {
                        mLanguageBeingDownloaded = "eng";
                        ifOcrDataIsNotAvailableThenMakeItAvailable(mLanguageBeingDownloaded);
                        mLanguageBeingDownloaded = "fra";
                        ifOcrDataIsNotAvailableThenMakeItAvailable(mLanguageBeingDownloaded);
                        mLanguageBeingDownloaded = "spa";
                        ifOcrDataIsNotAvailableThenMakeItAvailable(mLanguageBeingDownloaded);
                    }

                    initializeOcrEngineForChosenLanguage();

                    mInitializedOcrApiJpn = true;
                    timesPressed++; //Prevents multiple clicks on the button from freezing the app
                }
                Toast toast = Toast.makeText(getContext(), getResources().getString(R.string.OCR_reinitializing), Toast.LENGTH_SHORT);
                toast.show();
            }
        }

    }
    @OnClick(R.id.button_text_to_speech) public void onTextToSpeechButtonClick() {
        if (getActivity()==null || getContext()==null) return;

        String queryString = mInputQueryAutoCompleteTextView.getText().toString();
        mInputQuery = queryString;
        speakOut(queryString);

    }


    //Tesseract OCR methods
    private void setupOcr() {

        mDownloadType = "WifiOnly";

        getOcrDataDownloadingStatus();
        getLanguageParametersFromSettingsAndReinitializeOcrIfNecessary();
        setupPaths();
        setupBroadcastReceiverForDownloadedOCRData();
        registerThatUserIsRequestingDictSearch(false);
        tts = new TextToSpeech(getContext(), this);
        mLanguageBeingDownloaded = "jpn";
        ifOcrDataIsNotAvailableThenMakeItAvailable(mLanguageBeingDownloaded);
        mLanguageBeingDownloaded = "eng";
        ifOcrDataIsNotAvailableThenMakeItAvailable(mLanguageBeingDownloaded);
        mLanguageBeingDownloaded = "fra";
        ifOcrDataIsNotAvailableThenMakeItAvailable(mLanguageBeingDownloaded);
        mLanguageBeingDownloaded = "spa";
        ifOcrDataIsNotAvailableThenMakeItAvailable(mLanguageBeingDownloaded);
        initializeOcrEngineForChosenLanguage();
    }
    private void initializeTesseractAPI(String language) {
        //mImageToBeDecoded = BitmapFactory.decodeResource(getResources(), R.drawable.test_image);

        mInitializedOcrApiJpn = false;
        if (getActivity()!=null) {
            //Download language files from https://github.com/tesseract-ocr/tesseract/wiki/Data-Files
            try {
                mTess = new TessBaseAPI();
                mTess.init(mInternalStorageTesseractFolderPath, language);
                if (language.equals("jpn") ) mInitializedOcrApiJpn = true;
                else if (language.equals("eng") ) mInitializedOcrApiEng = true;
                else if (language.equals("fra") ) mInitializedOcrApiFra = true;
                else if (language.equals("spa") ) mInitializedOcrApiSpa = true;
                Log.e(TAG_TESSERACT, "Initialized Tesseract.");
            } catch (Exception e) {
                Log.e(TAG_TESSERACT, "Failed to initialize Tesseract.");
                Toast.makeText(getContext(),getResources().getString(R.string.OCR_failed), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
    private void getOcrTextWithTesseractAndDisplayDialog(Bitmap imageToBeDecoded){
        if (getActivity()==null) return;

        mTess.setImage(imageToBeDecoded);

        mAlreadyGotOcrResult = false;
        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
        Loader<String> tesseractOCRLoader = loaderManager.getLoader(TESSERACT_OCR_LOADER);
        if (tesseractOCRLoader == null)
            loaderManager.initLoader(TESSERACT_OCR_LOADER, null, this);
        else loaderManager.restartLoader(TESSERACT_OCR_LOADER, null, this);

        //mTesseractOCRAsyncTask = new TesseractOCRAsyncTask(getActivity()).execute();
    }
    private void downloadTesseractDataFileToDownloadsFolder(String source, String filename) {

        if (getActivity()==null) return;

        setOcrDataIsDownloadingStatus(filename, true);
        Log.e(TAG_TESSERACT, "Attempting file download");

        String url = source + filename;

        //https://stackoverflow.com/questions/38563474/how-to-store-downloaded-image-in-internal-storage-using-download-manager-in-andr
        downloadmanager = (DownloadManager) getActivity().getSystemService(DOWNLOAD_SERVICE);
        Uri downloadUri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(downloadUri);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
        if (mDownloadType.equals("WifiOnly")) {
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
            Toast.makeText(getActivity(), getString(R.string.downloading_ocr_data_first_part) + mLanguageBeingDownloadedLabel
                    + getString(R.string.downloading_ocr_data_second_part), Toast.LENGTH_SHORT).show();
        }
        else {
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            Toast.makeText(getActivity(), getString(R.string.attempting_download_first_part) + mLanguageBeingDownloadedLabel
                    + getString(R.string.attempting_download_second_part), Toast.LENGTH_SHORT).show();
        }
        request.setAllowedOverRoaming(false);
        request.setTitle("Downloading " + filename);
        request.setVisibleInDownloadsUi(true);
        enqueue = downloadmanager.enqueue(request);


        //Finished download activates the broadcast receiver, that in turn initializes the Tesseract API

    }
    private void getOcrDataDownloadingStatus() {

        if (getContext() != null) {
            SharedPreferences sharedPreferences = getContext().getSharedPreferences(DOWNLOAD_FILE_PREFS, Context.MODE_PRIVATE);
            mJpnOcrFileIsDownloading = sharedPreferences.getBoolean(JPN_FILE_DOWNLOADING_FLAG, false);
            mEngOcrFileIsDownloading = sharedPreferences.getBoolean(ENG_FILE_DOWNLOADING_FLAG, false);
            mFraOcrFileIsDownloading = sharedPreferences.getBoolean(FRA_FILE_DOWNLOADING_FLAG, false);
            mSpaOcrFileIsDownloading = sharedPreferences.getBoolean(SPA_FILE_DOWNLOADING_FLAG, false);
        }
    }
    private String getOCRLanguageFromSettings() {
        if (getActivity() != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String language = sharedPreferences.getString(getString(R.string.pref_preferred_OCR_language_key), getString(R.string.pref_language_value_japanese));
            if (language==null) language = getResources().getString(R.string.pref_language_value_japanese);

            if (language.equals(getResources().getString(R.string.pref_language_value_japanese))) {
                return "jpn";
            } else if (language.equals(getResources().getString(R.string.pref_language_value_english))) {
                return "eng";
            } else if (language.equals(getResources().getString(R.string.pref_language_value_french))) {
                return "fra";
            } else if (language.equals(getResources().getString(R.string.pref_language_value_spanish))) {
                return "spa";
            } else return "jpn";
        }
        else return "jpn";
    }
    private void setupPaths() {
        if (getActivity() != null) {
            //mInternalStorageTesseractFolderPath = Environment.getExternalStoragePublicDirectory(Environment.).getAbsolutePath() + "/";
            mInternalStorageTesseractFolderPath = getActivity().getFilesDir() + "/tesseract/";
            mPhoneAppFolderTesseractDataFilepath = mInternalStorageTesseractFolderPath + "tessdata/";
            mDownloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/";
        }
    }
    private void getLanguageParametersFromSettingsAndReinitializeOcrIfNecessary() {
        String newLanguage = getOCRLanguageFromSettings();
        if (mOCRLanguage != null && !mOCRLanguage.equals(newLanguage)) initializeTesseractAPI(newLanguage);
        mOCRLanguage = newLanguage;
        String mOCRLanguageLabel = getLanguageLabel(mOCRLanguage);
    }
    private String getLanguageLabel(String language) {
        switch (language) {
            case "jpn":
                return getResources().getString(R.string.language_label_japanese);
            case "eng":
                return getResources().getString(R.string.language_label_english);
            case "fra":
                return getResources().getString(R.string.language_label_french);
            case "spa":
                return getResources().getString(R.string.language_label_spanish);
            default:
                return getResources().getString(R.string.language_label_japanese);
        }
    }
    private void setupBroadcastReceiverForDownloadedOCRData() {
        mBroadcastReceiver = new BroadcastReceiver() {
            //https://stackoverflow.com/questions/38563474/how-to-store-downloaded-image-in-internal-storage-using-download-manager-in-andr
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action) && downloadmanager!=null) {
                    long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(enqueue);
                    Cursor c = downloadmanager.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {

                            jpnOcrFileISDownloading = false;
                            setOcrDataIsDownloadingStatus(mLanguageBeingDownloaded+".traineddata", false);

                            //String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                            //Uri a = Uri.parse(uriString);
                            //File d = new File(a.getPath());
                            // copy file from external to internal will easily available on net use google.
                            //String sdCard = Environment.getExternalStorageDirectory().toString();
                            File sourceLocation = new File(mDownloadsFolder + "/" + mLanguageBeingDownloaded + ".traineddata");
                            File targetLocation = new File(mPhoneAppFolderTesseractDataFilepath);
                            moveTesseractDataFile(sourceLocation, targetLocation);

                            initializeOcrEngineForChosenLanguage();
                        }
                    }
                }
            }
        };
        if (getActivity() != null) getActivity().registerReceiver(mBroadcastReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
    private void initializeOcrEngineForChosenLanguage() {
        mOCRLanguage = getOCRLanguageFromSettings();
        if (mOCRLanguage.equals("jpn") && jpnOcrDataIsAvailable) initializeTesseractAPI(mOCRLanguage);
        else if (mOCRLanguage.equals("eng") && engOcrDataIsAvailable) initializeTesseractAPI(mOCRLanguage);
        else if (mOCRLanguage.equals("fra") && fraOcrDataIsAvailable) initializeTesseractAPI(mOCRLanguage);
        else if (mOCRLanguage.equals("spa") && spaOcrDataIsAvailable) initializeTesseractAPI(mOCRLanguage);
    }
    private void ifOcrDataIsNotAvailableThenMakeItAvailable(String language) {

        if (!ALLOW_OCR_FOR_LATIN_LANGUAGES && !language.equals("jpn")) return;

        String filename = language + ".traineddata";
        Boolean fileExistsInAppFolder = Utilities.checkIfFileExistsInSpecificFolder(new File(mPhoneAppFolderTesseractDataFilepath), filename);
        mInitializedOcrApiJpn = false;
        if (!fileExistsInAppFolder) {
            hasStoragePermissions = Utilities.checkStoragePermission(getActivity());
            makeOcrDataAvailableInAppFolder(language);
        }
        else {
            switch (language) {
                case "jpn":
                    jpnOcrDataIsAvailable = true;
                    break;
                case "eng":
                    engOcrDataIsAvailable = true;
                    break;
                case "fra":
                    fraOcrDataIsAvailable = true;
                    break;
                case "spa":
                    spaOcrDataIsAvailable = true;
                    break;
            }
        }
    }
    private void makeOcrDataAvailableInAppFolder(String language) {

        if (!ALLOW_OCR_FOR_LATIN_LANGUAGES && !language.equals("jpn")) return;

        mLanguageBeingDownloadedLabel = getLanguageLabel(language);
        String filename = language + ".traineddata";
        Boolean fileExistsInPhoneDownloadsFolder = Utilities.checkIfFileExistsInSpecificFolder(new File(mDownloadsFolder), filename);
        if (hasStoragePermissions) {
            if (fileExistsInPhoneDownloadsFolder) {
                Log.e(TAG_TESSERACT, filename + " file successfully found in Downloads folder.");
                File sourceLocation = new File(mDownloadsFolder + filename);
                File targetLocation = new File(mPhoneAppFolderTesseractDataFilepath);
                moveTesseractDataFile(sourceLocation, targetLocation);
            } else {
                jpnOcrDataIsAvailable = false;
                if (!jpnOcrFileISDownloading) askForPreferredDownloadTimeAndDownload(language, filename);
            }
        }
    }
    private void copyTesseractDataFileFromAssets(String language) {
        try {
            String filepath = mInternalStorageTesseractFolderPath + "/tessdata/" + language + ".traineddata";

            if (getActivity()==null) return;
            AssetManager assetManager = getActivity().getAssets();

            InputStream instream = assetManager.open("tessdata/" +language+ ".traineddata");
            OutputStream outstream = new FileOutputStream(filepath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }

            outstream.flush();
            outstream.close();
            instream.close();

            File file = new File(filepath);
            if (!file.exists()) {
                throw new FileNotFoundException();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
    private void moveTesseractDataFile(File file, File dir) {
        File newFile = new File(dir, file.getName());
        FileChannel outputChannel = null;
        FileChannel inputChannel = null;
        //TODO: make sure the file completely finished downloading before allowing the program to continue
        try {
            outputChannel = new FileOutputStream(newFile).getChannel();
            inputChannel = new FileInputStream(file).getChannel();
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
            jpnOcrDataIsAvailable = true;
            Toast.makeText(getContext(),getResources().getString(R.string.OCR_copy_data), Toast.LENGTH_SHORT).show();
            Log.v(TAG_TESSERACT, "Successfully moved data file to app folder.");
            //file.delete();
        } catch (IOException e) {
            jpnOcrDataIsAvailable = false;
            Log.v(TAG_TESSERACT, "Copy file failed.");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputChannel != null) inputChannel.close();
                if (outputChannel != null) outputChannel.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

    }
    private void askForPreferredDownloadTimeAndDownload(String language, final String filename) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //builder.setTitle(R.string.OCRDialogTitle);
        builder.setPositiveButton(R.string.DownloadDialogWifiOnly, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mDownloadType = "WifiOnly";

                Boolean hasMemory = checkIfStorageSpaceEnoughForTesseractDataOrShowApology();
                Log.e(TAG_TESSERACT, "File not found in Downloads folder.");
                //if (!fileExistsInInternalStorage) copyFileFromAssets(mOCRLanguage);
                if (hasMemory) downloadTesseractDataFileToDownloadsFolder("https://github.com/tesseract-ocr/tessdata/raw/master/", filename);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.DownloadDialogWifiAndMobile, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mDownloadType = "WifiAndMobile";

                Boolean hasMemory = checkIfStorageSpaceEnoughForTesseractDataOrShowApology();
                Log.e(TAG_TESSERACT, "File not found in Downloads folder.");
                //if (!fileExistsInInternalStorage) copyFileFromAssets(mOCRLanguage);
                if (hasMemory) downloadTesseractDataFileToDownloadsFolder("https://github.com/tesseract-ocr/tessdata/raw/master/", filename);
                dialog.dismiss();
            }
        });
        if (language.equals("jpn")) builder.setMessage(R.string.DownloadDialogMessageJPN);
        else if (language.equals("eng")) builder.setMessage(R.string.DownloadDialogMessageENG);
        else if (language.equals("fra")) builder.setMessage(R.string.DownloadDialogMessageFRA);
        else if (language.equals("spa")) builder.setMessage(R.string.DownloadDialogMessageSPA);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private Boolean checkIfStorageSpaceEnoughForTesseractDataOrShowApology() {
        //https://inducesmile.com/android/how-to-get-android-ram-internal-and-external-memory-information/
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        long availableMemory = availableBlocks * blockSize;
        if (availableMemory<70000000) {
            String toastMessage = getString(R.string.sorry_only_have_first_part) + Utilities.formatSize(availableMemory)
                    + getString(R.string.sorry_only_have_second_part);
            Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_SHORT).show();
            return false;
        }
        else return true;
    }
    private void setOcrDataIsDownloadingStatus(Boolean status, String language) {
        if (getContext() != null) {
            SharedPreferences sharedPref = getContext().getSharedPreferences(DOWNLOAD_FILE_PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            switch (language) {
                case "jpn":
                    editor.putBoolean(JPN_FILE_DOWNLOADING_FLAG, status);
                    break;
                case "eng":
                    editor.putBoolean(ENG_FILE_DOWNLOADING_FLAG, status);
                    break;
                case "fra":
                    editor.putBoolean(FRA_FILE_DOWNLOADING_FLAG, status);
                    break;
                case "spa":
                    editor.putBoolean(SPA_FILE_DOWNLOADING_FLAG, status);
                    break;
            }
            editor.apply();
        }
    }
    private void setOcrDataIsDownloadingStatus(String filename, Boolean status) {

        if (getContext() != null) {
            SharedPreferences sharedPref = getContext().getSharedPreferences(DOWNLOAD_FILE_PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            switch (filename.substring(0,3)) {
                case "jpn":
                    editor.putBoolean(JPN_FILE_DOWNLOADING_FLAG, status);
                    break;
                case "eng":
                    editor.putBoolean(ENG_FILE_DOWNLOADING_FLAG, status);
                    break;
                case "fra":
                    editor.putBoolean(FRA_FILE_DOWNLOADING_FLAG, status);
                    break;
                case "spa":
                    editor.putBoolean(SPA_FILE_DOWNLOADING_FLAG, status);
                    break;
            }
            editor.apply();
        }
    }
    private static class TesseractOCRAsyncTaskLoader extends AsyncTaskLoader<String> {

        final TessBaseAPI mTess;
        private boolean mAllowLoaderStart;

        TesseractOCRAsyncTaskLoader(Context context,
                                    TessBaseAPI mTess) {
            super(context);
            this.mTess = mTess;
        }

        @Override
        protected void onStartLoading() {
            if (mAllowLoaderStart) forceLoad();
        }

        @Override
        public String loadInBackground() {

            String result = mTess.getUTF8Text();
            //String result = mTess.getHOCRText(0);
            return result;

        }

        void setLoaderState(boolean state) {
            mAllowLoaderStart = state;
        }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP) private void createOcrListDialog(String ocrResult) {

        if (getActivity()==null || getContext() == null) return;

        mImageToBeDecoded = adjustImageAfterOCR(mImageToBeDecoded);

        //Setting the elements in the dialog
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View dialogView = inflater.inflate(R.layout.dialog_ocr_results, null);

        ImageView ocrPictureHolder = dialogView.findViewById(R.id.ocrPicture);
        ocrPictureHolder.setImageBitmap(mImageToBeDecoded);
        //CropImageView cropImageView = dialogView.findViewById(R.id.cropImageView);
        //cropImageView.setImageUriAsync(mPhotoURI);
        //mImageToBeDecoded = cropImageView.getCroppedImage();


        //Adjusting the scrollview height
        final ScrollView ocrResultsScrollView = dialogView.findViewById(R.id.ocrResultsTextViewContainer);
        final TextView ocrResultsTextView = dialogView.findViewById(R.id.ocrResultsTextView);
        final TextView ocrResultsTextViewDialogInstructions = dialogView.findViewById(R.id.ocrResultsTextViewDialogInstructions);
        final List<String> ocrResultsList = Arrays.asList(ocrResult.split("\\r\\n|\\n|\\r"));
        List<String> textDisplayedInDialog = new ArrayList<>(ocrResultsList);
        for (int i = 0; i < textDisplayedInDialog.size(); i++) {
            textDisplayedInDialog.set(i, "~ " + textDisplayedInDialog.get(i) + " ~");
        }
        ocrResultsTextView.setText(TextUtils.join("\n", textDisplayedInDialog));
        ocrResultsTextView.setTextColor(Utilities.getResColorValue(getContext(), R.attr.colorAccent));
        ocrResultsTextViewDialogInstructions.setTextColor(Utilities.getResColorValue(getContext(), R.attr.colorAccentDark));
        ocrResultsScrollView.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams params = ocrResultsScrollView.getLayoutParams();
                int totalTextHeight = ocrResultsTextView.getHeight();
                if (totalTextHeight <= mMaxOCRDialogResultHeightPixels) {
                    ocrResultsScrollView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                }
                else {
                    params.height = mMaxOCRDialogResultHeightPixels;
                    ocrResultsScrollView.setLayoutParams(params);
                }
            }
        });

        //Building the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.OCRDialogTitle);
        builder.setPositiveButton(R.string.copy_to_input, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //Overridden later on
            }
        });
        builder.setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        builder.setNeutralButton(R.string.readjust,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (mCropImageResult != null) sendImageToImageAdjuster(mCropImageResult);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setView(dialogView);
        }
        else { builder.setMessage(R.string.LowVersionDialogMessage); }
        AlertDialog dialog = builder.create();
        //scaleImage(ocrPictureHolder, 1);
        dialog.show();


        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getContext()!=null) {
                    String text = ocrResultsTextView.getText().toString();
                    int startIndex = ocrResultsTextView.getSelectionStart();
                    int endIndex = ocrResultsTextView.getSelectionEnd();

                    if (startIndex != endIndex){
                        text = text.substring(startIndex, endIndex);
                    }
                    else {
                        text = text.split("\n")[0];
                    }

                    if (text.length()>2 && text.substring(0,2).equals("~ ")) text = text.substring(2);
                    if (text.length()>2 && text.substring(text.length()-2).equals(" ~")) text = text.substring(0,text.length()-2);

                    mInputQuery = text;
                    mInputQueryAutoCompleteTextView.setText(text);
                }
                //dialog.dismiss();
            }
        });

    }


    //TextToSpeech methods
    @Override public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            setTTSLanguage();
        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }
    private void setTTSLanguage() {
        int result;
        //Getting the user setting from the preferences
        if (getActivity() != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String language = sharedPreferences.getString(getString(R.string.pref_preferred_TTS_language_key), getString(R.string.pref_language_value_japanese));
            String mChosenTextToSpeechLanguage = getLanguageLocale(language!=null? language : getString(R.string.pref_language_value_japanese));

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
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }


    //SpeechToText methods
    private String getLanguageLocale(String language) {
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
    private static class GetRomajiFromKanjiUsingJishoAsyncTaskLoader extends AsyncTaskLoader<String> {

        final String queryText;
        private final boolean requestedSpeechToText;
        private boolean internetIsAvailable;
        private boolean mAllowLoaderStart;

        GetRomajiFromKanjiUsingJishoAsyncTaskLoader(Context context,
                                                    String queryText,
                                                    boolean requestedSpeechToText) {
            super(context);
            this.queryText = queryText;
            this.requestedSpeechToText = requestedSpeechToText;
        }

        @Override
        protected void onStartLoading() {
            if (mAllowLoaderStart) forceLoad();
        }

        @Override
        public String loadInBackground() {

            internetIsAvailable = Utilities.internetIsAvailableCheck(getContext());

            //This method retrieves the first romaji transliteration of the kanji searched word.

            List<Word> matchingWordsFromJisho;
            if (internetIsAvailable) {
                matchingWordsFromJisho = Utilities.getWordsFromJishoOnWeb(queryText, getContext());

                if (matchingWordsFromJisho.size() != 0 && requestedSpeechToText) {
                    Word results = matchingWordsFromJisho.get(0);
                    return results.getRomaji();
                } else return null;

            }
            else return null;
        }

        void setLoaderState(boolean state) {
            mAllowLoaderStart = state;
        }
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
                LayoutInflater inflater = LayoutInflater.from(getActivity().getBaseContext());
                View layout = inflater.inflate(R.layout.spinner_item_queryhistory, parent, false);
                TextView queryHistoryElement = layout.findViewById(R.id.query_value);
                queryHistoryElement.setText(mQueryHistory.get(position).replace(GlobalConstants.QUERY_HISTORY_MEANINGS_DELIMITER, GlobalConstants.QUERY_HISTORY_MEANINGS_DISPLAYED_DELIMITER));
                queryHistoryElement.setMaxLines(1);
                queryHistoryElement.setEllipsize(TextUtils.TruncateAt.END);
                queryHistoryElement.setTypeface(mDroidSansJapaneseTypeface);
                queryHistoryElement.setGravity(View.TEXT_ALIGNMENT_CENTER|View.TEXT_ALIGNMENT_TEXT_START);
                queryHistoryElement.setTextColor(Utilities.getResColorValue(getContext(), R.attr.appTextPrimaryColor));
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


    //Loader methods
    @NonNull @Override public Loader<String> onCreateLoader(int id, final Bundle args) {

        if (id== GET_ROMAJI_FROM_KANJI_USING_JISHO_LOADER) {
            GetRomajiFromKanjiUsingJishoAsyncTaskLoader webResultsAsyncTaskLoader = new GetRomajiFromKanjiUsingJishoAsyncTaskLoader(getContext(), mInputQuery, mRequestedSpeechToText);
            webResultsAsyncTaskLoader.setLoaderState(true);
            return webResultsAsyncTaskLoader;
        }
        else if (id==TESSERACT_OCR_LOADER && getContext()!=null) {

            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setTitle(getContext().getResources().getString(R.string.OCR_waitWhileProcessing));
            mProgressDialog.setMessage(getContext().getResources().getString(R.string.OCR_waitWhileProcessingExplanation));
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getContext().getResources().getString(R.string.cancel), (dialog, which) -> {
                if (mTess!=null) mTess.stop();
                initializeTesseractAPI(mOCRLanguage);
                if (mTesseractOCRAsyncTask!=null) mTesseractOCRAsyncTask.cancel(true);
                dialog.dismiss();
            });
            mProgressDialog.show();

            TesseractOCRAsyncTaskLoader tesseractOCRAsyncTaskLoader = new TesseractOCRAsyncTaskLoader(getContext(), mTess);
            tesseractOCRAsyncTaskLoader.setLoaderState(true);
            return tesseractOCRAsyncTaskLoader;
        }
        else {
            return new GetRomajiFromKanjiUsingJishoAsyncTaskLoader(getContext(), "", false);
        }
    }
    @Override public void onLoadFinished(@NonNull Loader<String> loader, String data) {

        if (loader.getId() == GET_ROMAJI_FROM_KANJI_USING_JISHO_LOADER & !mAlreadyGotRomajiFromKanji) {

            mAlreadyGotRomajiFromKanji = true;
            if (data != null) {
                mInputQueryAutoCompleteTextView.setText(data);
            }
            if (!mInputQueryAutoCompleteTextView.getText().toString().equals("")) {
                mDictButton.performClick();
            }
            getLoaderManager().destroyLoader(GET_ROMAJI_FROM_KANJI_USING_JISHO_LOADER);
        }
        else if (loader.getId() == TESSERACT_OCR_LOADER & !mAlreadyGotOcrResult) {
            mAlreadyGotOcrResult = true;
            if (mProgressDialog != null && mProgressDialog.isShowing()) mProgressDialog.dismiss();
            mOcrResultString = data;
            createOcrListDialog(mOcrResultString);
            getLoaderManager().destroyLoader(TESSERACT_OCR_LOADER);
        }
    }
    @Override public void onLoaderReset(@NonNull Loader<String> loader) {

    }


    //Communication with other classes:

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
        mInputQueryAutoCompleteTextView.setText(query);
    }
    public void setAppendedQuery(String addedText) {
        mInputQuery = mInputQueryAutoCompleteTextView.getText().toString();
        String newQuery = mInputQuery + addedText;
        mInputQueryAutoCompleteTextView.setText(newQuery);
    }
    public void setConjButtonSelected() {
        drawBorderAroundThisButton(mConjButton);
    }
    public void setSTTLanguage(String chosenSpeechToTextLanguage) {
        mChosenSpeechToTextLanguage = chosenSpeechToTextLanguage;
    }
    public void clearHistory() {
        MainActivity.saveQueryHistoryToPreferences(getContext(), new ArrayList<>());
    }
}