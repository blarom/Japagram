package com.japagram.ui;

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
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.StatFs;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.japagram.R;
import com.japagram.asynctasks.DictSearchAsyncTask;
import com.japagram.asynctasks.TesseractOCRAsyncTask;
import com.japagram.data.InputQuery;
import com.japagram.data.Word;
import com.japagram.databinding.FragmentInputqueryBodyBinding;
import com.japagram.utilitiesAndroid.AndroidUtilitiesIO;
import com.japagram.utilitiesAndroid.AndroidUtilitiesPrefs;
import com.japagram.utilitiesCrossPlatform.Globals;
import com.japagram.utilitiesCrossPlatform.UtilitiesGeneral;
import com.theartofdev.edmodo.cropper.CropImage;

import org.jetbrains.annotations.NotNull;

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
import androidx.preference.PreferenceManager;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import static android.content.Context.DOWNLOAD_SERVICE;

public class InputQueryFragment extends Fragment implements
        TesseractOCRAsyncTask.TesseractOCRAsyncResponseHandler,
        TextToSpeech.OnInitListener, DictSearchAsyncTask.LocalDictSearchAsyncResponseHandler {

    //region Parameters
    private FragmentInputqueryBodyBinding binding;
    private static final boolean ALLOW_OCR_FOR_LATIN_LANGUAGES = false;
    public static final String OCR_LANG_JPN = "jpn";
    public static final String OCR_LANG_ENG = "eng";
    public static final String OCR_LANG_FRA = "fra";
    public static final String OCR_LANG_SPA = "spa";
    private static final int MAX_OCR_DIALOG_RECYCLERVIEW_HEIGHT_DP = 150;
    private static final int RESULT_OK = -1;
    private static final int SPEECH_RECOGNIZER_REQUEST_CODE = 101;
    private static final int ADJUST_IMAGE_ACTIVITY_REQUEST_CODE = 201;
    private static final String TAG_TESSERACT = "tesseract_ocr";
    private static final String TAG_PERMISSIONS = "Permission error";
    private static final String DOWNLOAD_FILE_PREFS = "download_file_prefs";
    private static final String JPN_FILE_DOWNLOADING_FLAG = "jpn_file_downloading";
    private String mInputQuery;
    private Bitmap mImageToBeDecoded;
    private TessBaseAPI mTess;
    private String mInternalStorageTesseractFolderPath = "";
    boolean mInitializedOcrApi = false;
    boolean mFirstTimeInitialized = true;
    boolean mOcrDataIsAvailable = false;
    boolean mOcrFileIsDownloading = false;
    private String mOCRLanguage;
    private String mOcrResultString;
    private TextToSpeech tts;
    private long enqueue;
    private DownloadManager downloadmanager;
    private boolean hasStoragePermissions;
    private String mPhoneAppFolderTesseractDataFilepath;
    private String mDownloadsFolder;
    private int timesPressed;
    private String mLanguageBeingDownloadedLabel;
    private String mLanguageBeingDownloaded;
    private TesseractOCRAsyncTask mTesseractOCRAsyncTask;
    private String mChosenSpeechToTextLanguage;
    private String mDownloadType;
    private boolean jpnOcrFileISDownloading;
    private CropImage.ActivityResult mCropImageResult;
    private BroadcastReceiver mBroadcastReceiver;
    private ProgressDialog mProgressDialog;
    private boolean mAlreadyGotOcrResult;
    private boolean mAlreadyGotRomajiFromKanji;
    private Typeface mDroidSansJapaneseTypeface;
    private int mMaxOCRDialogResultHeightPixels;
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
        setupOcr();
    }
    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View rootView = inflater.inflate(R.layout.fragment_inputquery, container, false);

        setRetainInstance(true);

        if (savedInstanceState!=null) {
            mAlreadyGotOcrResult = savedInstanceState.getBoolean(getString(R.string.saved_ocr_result_state), false);
            mAlreadyGotRomajiFromKanji = savedInstanceState.getBoolean(getString(R.string.saved_romaji_from_kanji_state), false);
        }

        mMaxOCRDialogResultHeightPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MAX_OCR_DIALOG_RECYCLERVIEW_HEIGHT_DP, getResources().getDisplayMetrics());

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
        binding.query.setText(mInputQuery);
        if (getActivity()!=null) AndroidUtilitiesIO.hideSoftKeyboard(getActivity());

        getLanguageParametersFromSettingsAndReinitializeOcrIfNecessary();
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
        if (mTess != null) mTess.end();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (getActivity() != null && mBroadcastReceiver != null) {
            try {
                getActivity().unregisterReceiver(mBroadcastReceiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

    }
    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPEECH_RECOGNIZER_REQUEST_CODE) {

            if (data == null) return;
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results == null || results.size()==0) return;

            mInputQuery = results.get(0);
            binding.query.setText(mInputQuery);

            startRomajiFromKanjiThread();
        }
        else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            AndroidUtilitiesIO.unmuteSpeaker(getActivity());
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
                    mImageToBeDecoded = AndroidUtilitiesIO.getBitmapFromUri(getActivity(), adjustedImageUri);
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
            if (!mInitializedOcrApi) initializeOcrEngineForChosenLanguage();
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
        timesPressed = 0;
        mCropImageResult = null;
        mAlreadyGotOcrResult = false;
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
        binding.buttonOcr.setOnClickListener(view1 -> onOcrButtonClick());
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
    private void sendImageToImageAdjuster(@NotNull CropImage.ActivityResult result) {
        Uri mPhotoURI = result.getUri();
        mImageToBeDecoded = AndroidUtilitiesIO.getBitmapFromUri(getActivity(), mPhotoURI);

        //Send the image Uri to the AdjustImageActivity
        Intent intent = new Intent(getActivity(), AdjustImageActivity.class);
        intent.putExtra("imageUri", mPhotoURI.toString());
        startActivityForResult(intent, ADJUST_IMAGE_ACTIVITY_REQUEST_CODE);
    }
    private void performImageCaptureAndCrop() {

        // start source picker (camera, gallery, etc..) to get image for cropping and then use the image in cropping activity
        //CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(getActivity());

        AndroidUtilitiesIO.muteSpeaker(getActivity());
        if (getContext() != null) CropImage.activity().start(getContext(), this); //For FragmentActivity use

    }
    private Bitmap adjustImageAfterOCR(Bitmap imageToBeDecoded) {
        //imageToBeDecoded = Utilities.adjustImageAngleAndScale(imageToBeDecoded, 0, 0.5);
        return imageToBeDecoded;
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
            if (!element.equals("")) { queryHistoryIsEmpty = false; break; }
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
    public void onOcrButtonClick() {
        if (getActivity()==null || getContext()==null) return;

        getOcrDataDownloadingStatus();
        if (mOCRLanguage.equals(OCR_LANG_JPN) && mOcrFileIsDownloading) {
            Toast toast = Toast.makeText(getContext(),getResources().getString(R.string.OCR_downloading), Toast.LENGTH_SHORT);
            toast.show();
        }
        else {
            if (mInitializedOcrApi && mOCRLanguage.equals(OCR_LANG_JPN)) {
                if (mFirstTimeInitialized) {
                    Toast toast = Toast.makeText(getActivity(), getResources().getString(R.string.OCRinstructions), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
                    toast.show();
                    mFirstTimeInitialized = false;
                }
                timesPressed = 0;
                performImageCaptureAndCrop();
            } else {
                if (timesPressed <= 3) {
                    mLanguageBeingDownloaded = OCR_LANG_JPN;
                    ifOcrDataIsNotAvailableThenMakeItAvailable(mLanguageBeingDownloaded);

                    initializeOcrEngineForChosenLanguage();
                    mInitializedOcrApi = false;
                    timesPressed++; //Prevents multiple clicks on the button from freezing the app
                }
                Toast toast = Toast.makeText(getContext(), getResources().getString(R.string.OCR_reinitializing), Toast.LENGTH_SHORT);
                toast.show();
            }
        }

    }
    public void onTextToSpeechButtonClick() {
        if (getActivity()==null || getContext()==null) return;

        String queryString = binding.query.getText().toString();
        mInputQuery = queryString;
        speakOut(queryString);

    }


    //Tesseract OCR methods
    private void setupOcr() {

        Runnable instantiateRunnable = () -> {
            Log.i(Globals.DEBUG_TAG, "InputQueryFragment - setupOcr - start");
            mDownloadType = "WifiOnly";

            getOcrDataDownloadingStatus();
            getLanguageParametersFromSettingsAndReinitializeOcrIfNecessary();
            setupPaths();
            setupBroadcastReceiverForDownloadedOCRData();
            registerThatUserIsRequestingDictSearch(false);
            mLanguageBeingDownloaded = OCR_LANG_JPN;
//            ifOcrDataIsNotAvailableThenMakeItAvailable(mLanguageBeingDownloaded);
//            mLanguageBeingDownloaded = OCR_LANG_ENG;
//            ifOcrDataIsNotAvailableThenMakeItAvailable(mLanguageBeingDownloaded);
//            mLanguageBeingDownloaded = OCR_LANG_FRA;
//            ifOcrDataIsNotAvailableThenMakeItAvailable(mLanguageBeingDownloaded);
//            mLanguageBeingDownloaded = OCR_LANG_SPA;
            ifOcrDataIsNotAvailableThenMakeItAvailable(mLanguageBeingDownloaded);
            initializeOcrEngineForChosenLanguage();
            Log.i(Globals.DEBUG_TAG, "InputQueryFragment - setupOcr - end");
        };
        Thread instantiateThread = new Thread(instantiateRunnable);
        instantiateThread.start();

    }
    private void initializeTesseractAPI(String language) {
        //mImageToBeDecoded = BitmapFactory.decodeResource(getResources(), R.drawable.test_image);

        mInitializedOcrApi = false;
        if (getActivity()!=null) {
            //Download language files from https://github.com/tesseract-ocr/tesseract/wiki/Data-Files
            try {
                mTess = new TessBaseAPI();
                mTess.init(mInternalStorageTesseractFolderPath, language);
                mInitializedOcrApi = true;
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

        if (mTess != null) {
            mTess.setImage(imageToBeDecoded);
            startTesseractOCRThread();
        }
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
        if (Looper.myLooper() == null) Looper.prepare();
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
            mOcrFileIsDownloading = sharedPreferences.getBoolean(JPN_FILE_DOWNLOADING_FLAG, false);
        }
    }
    @NotNull private String getOCRLanguageFromSettings() {
        if (getActivity() != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String language = sharedPreferences.getString(getString(R.string.pref_preferred_OCR_language_key), getString(R.string.pref_language_value_japanese));

            if (language.equals(getResources().getString(R.string.pref_language_value_japanese))) {
                return OCR_LANG_JPN;
            } else if (language.equals(getResources().getString(R.string.pref_language_value_english))) {
                return OCR_LANG_ENG;
            } else if (language.equals(getResources().getString(R.string.pref_language_value_french))) {
                return OCR_LANG_FRA;
            } else if (language.equals(getResources().getString(R.string.pref_language_value_spanish))) {
                return OCR_LANG_SPA;
            } else return OCR_LANG_JPN;
        }
        else return OCR_LANG_JPN;
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
        Runnable instantiateRunnable = () -> {
            String newLanguage = getOCRLanguageFromSettings();
            if (mOCRLanguage != null && !mOCRLanguage.equals(newLanguage)) initializeTesseractAPI(newLanguage);
            mOCRLanguage = newLanguage;
        };
        Thread instantiateThread = new Thread(instantiateRunnable);
        instantiateThread.start();
    }
    @NotNull private String getLanguageLabel(@NotNull String language) {
        switch (language) {
            case OCR_LANG_JPN:
                return getResources().getString(R.string.language_label_japanese);
            case OCR_LANG_ENG:
                return getResources().getString(R.string.language_label_english);
            case OCR_LANG_FRA:
                return getResources().getString(R.string.language_label_french);
            case OCR_LANG_SPA:
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
        if (mOCRLanguage.equals(OCR_LANG_JPN) && mOcrDataIsAvailable) initializeTesseractAPI(mOCRLanguage);
    }
    private void ifOcrDataIsNotAvailableThenMakeItAvailable(String language) {

        if (!ALLOW_OCR_FOR_LATIN_LANGUAGES && !language.equals(OCR_LANG_JPN)) return;

        String filename = language + ".traineddata";
        try {
            File file = new File(mPhoneAppFolderTesseractDataFilepath);
            boolean fileExistsInAppFolder = AndroidUtilitiesIO.checkIfFileExistsInSpecificFolder(file, filename);
            mInitializedOcrApi = false;
            if (!fileExistsInAppFolder) {
                hasStoragePermissions = AndroidUtilitiesIO.checkStoragePermission(getActivity());
                makeOcrDataAvailableInAppFolder(language);
            } else {
                setOcrDataIsDownloadingStatus(filename, false);
                mOcrDataIsAvailable = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void makeOcrDataAvailableInAppFolder(String language) {

        if (!ALLOW_OCR_FOR_LATIN_LANGUAGES && !language.equals(OCR_LANG_JPN)) return;

        mLanguageBeingDownloadedLabel = getLanguageLabel(language);
        String filename = language + ".traineddata";
        Boolean fileExistsInPhoneDownloadsFolder = AndroidUtilitiesIO.checkIfFileExistsInSpecificFolder(new File(mDownloadsFolder), filename);
        if (hasStoragePermissions) {
            if (fileExistsInPhoneDownloadsFolder) {
                Log.e(TAG_TESSERACT, filename + " file successfully found in Downloads folder.");
                File sourceLocation = new File(mDownloadsFolder + filename);
                File targetLocation = new File(mPhoneAppFolderTesseractDataFilepath);
                moveTesseractDataFile(sourceLocation, targetLocation);
            } else {
                mOcrDataIsAvailable = false;
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
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }
    private void moveTesseractDataFile(@NotNull File file, File dir) {
        File newFile = new File(dir, file.getName());
        FileChannel outputChannel = null;
        FileChannel inputChannel = null;
        //TODO: make sure the file completely finished downloading before allowing the program to continue
        try {
            outputChannel = new FileOutputStream(newFile).getChannel();
            inputChannel = new FileInputStream(file).getChannel();
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
            mOcrDataIsAvailable = true;
            if (Looper.myLooper() == null) Looper.prepare();
            Toast.makeText(getContext(),getResources().getString(R.string.OCR_copy_data), Toast.LENGTH_SHORT).show();
            Log.v(TAG_TESSERACT, "Successfully moved data file to app folder.");
            //file.delete();
        } catch (IOException e) {
            mOcrDataIsAvailable = false;
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

        if (getContext() == null) return;
        mDownloadType = "WifiOnly";
        Boolean hasMemory = checkIfStorageSpaceEnoughForTesseractDataOrShowApology();
        Log.e(TAG_TESSERACT, "File not found in Downloads folder.");
        //if (!fileExistsInInternalStorage) copyFileFromAssets(mOCRLanguage);
        if (hasMemory) downloadTesseractDataFileToDownloadsFolder("https://github.com/tesseract-ocr/tessdata/raw/master/", filename);


//        if (Looper.myLooper() == null) Looper.prepare();
//        AlertDialog alertDialog = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialogStyle).create();
//        //builder.setTitle(R.string.OCRDialogTitle);
//        switch (language) {
//            case OCR_LANG_JPN: alertDialog.setMessage(getString(R.string.DownloadDialogMessageJPN)); break;
//            case OCR_LANG_ENG: alertDialog.setMessage(getString(R.string.DownloadDialogMessageENG)); break;
//            case OCR_LANG_FRA: alertDialog.setMessage(getString(R.string.DownloadDialogMessageFRA)); break;
//            case OCR_LANG_SPA: alertDialog.setMessage(getString(R.string.DownloadDialogMessageSPA)); break;
//        }
//        alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE, getString(R.string.DownloadDialogWifiOnly),
//                (dialog, which) -> {
//                    mDownloadType = "WifiOnly";
//                    Boolean hasMemory = checkIfStorageSpaceEnoughForTesseractDataOrShowApology();
//                    Log.e(TAG_TESSERACT, "File not found in Downloads folder.");
//                    //if (!fileExistsInInternalStorage) copyFileFromAssets(mOCRLanguage);
//                    if (hasMemory) downloadTesseractDataFileToDownloadsFolder("https://github.com/tesseract-ocr/tessdata/raw/master/", filename);
//                    dialog.dismiss();
//                });
//        alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE, getString(R.string.DownloadDialogWifiAndMobile),
//                (dialog, which) -> {
//                    mDownloadType = "WifiAndMobile";
//                    Boolean hasMemory = checkIfStorageSpaceEnoughForTesseractDataOrShowApology();
//                    Log.e(TAG_TESSERACT, "File not found in Downloads folder.");
//                    //if (!fileExistsInInternalStorage) copyFileFromAssets(mOCRLanguage);
//                    if (hasMemory) downloadTesseractDataFileToDownloadsFolder("https://github.com/tesseract-ocr/tessdata/raw/master/", filename);
//                    dialog.dismiss();
//                });
//        alertDialog.show();

//        if (alertDialog.getWindow() == null) return;
//        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(AndroidUtilitiesPrefs.getResColorValue(getContext(), R.attr.colorMonochromeBlend)));
    }
    @NotNull private Boolean checkIfStorageSpaceEnoughForTesseractDataOrShowApology() {
        //https://inducesmile.com/android/how-to-get-android-ram-internal-and-external-memory-information/
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        long availableMemory = availableBlocks * blockSize;
        if (availableMemory<70000000) {
            String toastMessage = getString(R.string.sorry_only_have_first_part) + AndroidUtilitiesIO.formatSize(availableMemory)
                    + getString(R.string.sorry_only_have_second_part);
            Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_SHORT).show();
            return false;
        }
        else return true;
    }
    private void setOcrDataIsDownloadingStatus(String filename, Boolean status) {

        if (getContext() != null) {
            SharedPreferences sharedPref = getContext().getSharedPreferences(DOWNLOAD_FILE_PREFS, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(JPN_FILE_DOWNLOADING_FLAG, status);
            editor.apply();
        }
    }
    private void startRomajiFromKanjiThread() {

        if (getActivity() != null && getContext() != null) {
            Log.i(Globals.DEBUG_TAG, "DictionaryFragment - Starting search for Romaji values of words recognized by speech");
            DictSearchAsyncTask mLocalDictSearchAsyncTask = new DictSearchAsyncTask(getContext(), new InputQuery(mInputQuery), this, false);
            mLocalDictSearchAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

    }
    private void startTesseractOCRThread() {

        if (getActivity() != null && getContext() != null) {
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
            if (mProgressDialog.getWindow() != null) mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(AndroidUtilitiesPrefs.getResColorValue(getContext(), R.attr.colorMonochromeBlend)));

            if (mTess == null) return;
            Log.i(Globals.DEBUG_TAG, "DictionaryFragment - Starting Terresact OCR");
            mTesseractOCRAsyncTask = new TesseractOCRAsyncTask(getContext(), mTess, this);
            mTesseractOCRAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
        ocrResultsTextView.setTextColor(AndroidUtilitiesPrefs.getResColorValue(getContext(), R.attr.colorAccent));
        ocrResultsTextViewDialogInstructions.setTextColor(AndroidUtilitiesPrefs.getResColorValue(getContext(), R.attr.colorAccentDark));
        ocrResultsScrollView.post(() -> {
            ViewGroup.LayoutParams params = ocrResultsScrollView.getLayoutParams();
            int totalTextHeight = ocrResultsTextView.getHeight();
            if (totalTextHeight <= mMaxOCRDialogResultHeightPixels) {
                ocrResultsScrollView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            }
            else {
                params.height = mMaxOCRDialogResultHeightPixels;
                ocrResultsScrollView.setLayoutParams(params);
            }
        });

        //Building the dialog
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialogStyle).create();
        Window window = alertDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.CENTER);
        alertDialog.setTitle(R.string.OCRDialogTitle);
        alertDialog.setView(dialogView);
        alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE, getString(R.string.copy_to_input),
                (dialog, which) -> {
                    //Overridden later on
                });
        alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE, getString(R.string.close),
                (dialog, which) -> dialog.dismiss()
        );
        alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL, getString(R.string.readjust),
                (dialog, which) -> {
                    if (mCropImageResult != null) sendImageToImageAdjuster(mCropImageResult);
                });
        alertDialog.show();
        //scaleImage(ocrPictureHolder, 1);

//        if (dialog.getWindow() == null) return;
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(AndroidUtilitiesPrefs.getResColorValue(getContext(), R.attr.colorMonochromeBlend)));

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
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

                if (text.length()>2 && text.startsWith("~ ")) text = text.substring(2);
                if (text.length()>2 && text.endsWith(" ~")) text = text.substring(0,text.length()-2);

                mInputQuery = text;
                binding.query.setText(text);
            }
            //dialog.dismiss();
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
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
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
    @Override public void onTesseractOCRAsyncTaskResultFound(String result) {
        if (getContext() == null) return;

        if (mProgressDialog != null && mProgressDialog.isShowing()) mProgressDialog.dismiss();
        if (mProgressDialog != null && mProgressDialog.getWindow() != null) {
            mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(AndroidUtilitiesPrefs.getResColorValue(getContext(), R.attr.colorMonochromeBlend)));
        }

        mOcrResultString = result;
        createOcrListDialog(mOcrResultString);
    }
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