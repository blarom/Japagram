package com.japagram.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.japagram.R;
import com.japagram.asynctasks.RoomDatabasesInstallationForegroundService;
import com.japagram.data.RoomCentralDatabase;
import com.japagram.data.RoomKanjiDatabase;
import com.japagram.databinding.ActivitySplashscreenBinding;
import com.japagram.resources.LocaleHelper;
import com.japagram.utilitiesAndroid.AndroidUtilitiesIO;
import com.japagram.utilitiesAndroid.AndroidUtilitiesPrefs;
import com.japagram.utilitiesCrossPlatform.Globals;
import com.japagram.utilitiesCrossPlatform.UtilitiesDb;
import com.japagram.utilitiesPlatformOverridable.OvUtilsGeneral;

public class SplashScreenActivity extends BaseActivity {

    private final int SPLASH_DISPLAY_LENGTH = 1000; //Miliseconds
    private ActivitySplashscreenBinding binding;
    private boolean mCentralDbBeingLoaded;
    private boolean mKanjiDbBeingLoaded;
    private CountDownTimer countDownTimer;
    private Runnable dbLoadRunnableCentral;
    private Runnable dbLoadRunnableKanji;
    private Thread dbLoadThreadCentral;
    private Thread dbLoadThreadKanji;
    boolean mLastUIUpdateWasCentral;
    boolean mLastUIUpdateWasKanji;
    private int mTicks;
    private String mLanguage;

    @Override protected void onCreate(Bundle savedInstanceState) {

        Log.i(Globals.DEBUG_TAG, "Started Splashscreen.");

        AndroidUtilitiesPrefs.changeThemeColor(this);
        super.onCreate(savedInstanceState);

        binding = ActivitySplashscreenBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        //setContentView(R.layout.activity_splashscreen);

        binding.splashscreenBackground.setBackgroundResource(AndroidUtilitiesPrefs.getAppPreferenceColorTheme(this).contains("day")? R.drawable.background1_day : R.drawable.background1_night);
        binding.splashscreenLoadingDatabase.setTextColor(AndroidUtilitiesPrefs.getResColorValue(this, R.attr.colorPrimaryLight));

        mCentralDbBeingLoaded = true;
        mKanjiDbBeingLoaded = true;
        mLanguage = LocaleHelper.getLanguage(getBaseContext());

        startDbInstallationForegroundService();

        //Loading databases in parallel or series depending on available heap memory (more or less than 1000MB respectively)
        dbLoadRunnableCentral = () -> {
            mCentralDbBeingLoaded = true;
            Globals.GLOBAL_SIMILARS_DATABASE = AndroidUtilitiesIO.readCSVFile("LineSimilars - 3000 kanji.csv", getBaseContext());
            Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE = AndroidUtilitiesIO.readCSVFile("LineLatinConj - 3000 kanji.csv", getBaseContext());
            Globals.GLOBAL_VERB_KANJI_CONJ_DATABASE = AndroidUtilitiesIO.readCSVFile("LineKanjiConj - 3000 kanji.csv", getBaseContext());
            Globals.GLOBAL_VERB_LATIN_CONJ_LENGTHS = AndroidUtilitiesIO.readCSVFile("LineVerbsLengths - 3000 kanji.csv", getBaseContext());
            Globals.GLOBAL_VERB_KANJI_CONJ_LENGTHS = AndroidUtilitiesIO.readCSVFile("LineVerbsKanjiLengths - 3000 kanji.csv", getBaseContext());
            Globals.GLOBAL_CONJUGATION_TITLES = com.japagram.utilitiesCrossPlatform.UtilitiesDb.getConjugationTitles(Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE, this, mLanguage);
            Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE_NO_SPACES = UtilitiesDb.removeSpacesFromConjDb(Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE);
            Globals.GLOBAL_RADICALS_ONLY_DATABASE = AndroidUtilitiesIO.readCSVFile("LineRadicalsOnly - 3000 kanji.csv", getBaseContext());
            Globals.GLOBAL_ROMANIZATIONS = OvUtilsGeneral.getTranspose(AndroidUtilitiesIO.readCSVFile("LineRomanizations.csv", getBaseContext()));
            Log.i(Globals.DEBUG_TAG, "Splashscreen - Loaded Small databases");
            RoomCentralDatabase.getInstance(SplashScreenActivity.this); //Required for Room
            Log.i(Globals.DEBUG_TAG, "Splashscreen - Instantiated RoomCentralDatabase");
            mCentralDbBeingLoaded = false;
        };
        dbLoadRunnableKanji = () -> {
            mKanjiDbBeingLoaded = true;
            RoomKanjiDatabase.getInstance(SplashScreenActivity.this); //Required for Room
            Log.i(Globals.DEBUG_TAG, "Splashscreen - Instantiated RoomKanjiDatabase");
            mKanjiDbBeingLoaded = false;
        };
        dbLoadThreadCentral = new Thread(dbLoadRunnableCentral);
        dbLoadThreadCentral.start();
        dbLoadThreadKanji = new Thread(dbLoadRunnableKanji);
        dbLoadThreadKanji.start();

        //showLoadingIndicator();
        if (AndroidUtilitiesPrefs.getAppPreferenceFirstTimeRunningApp(SplashScreenActivity.this)) {
            Toast.makeText(SplashScreenActivity.this, R.string.first_time_installing, Toast.LENGTH_LONG).show();
        }

        if (AndroidUtilitiesPrefs.getAppPreferenceDbVersionCentral(this) != Globals.CENTRAL_DB_VERSION) {
            AndroidUtilitiesPrefs.setAppPreferenceCentralDatabasesFinishedLoadingFlag(this, false);
        }
        if (AndroidUtilitiesPrefs.getAppPreferenceDbVersionKanji(this) != Globals.KANJI_DB_VERSION) {
            AndroidUtilitiesPrefs.setAppPreferenceKanjiDatabaseFinishedLoadingFlag(this, false);
        }

        mTicks = 0;
        countDownTimer = new CountDownTimer(3600000, 200) {

            @Override
            public void onTick(long l) {

                if (mTicks == 0 || (mTicks > 3 && mTicks % 3 == 0)) { //Reducing the amount of computations
                    mTicks++;
                    return;
                }

                //Delaying the start of db loading if the app uses too much memory
                boolean finishedLoadingCentralDatabase = AndroidUtilitiesPrefs.getAppPreferenceCentralDatabasesFinishedLoadingFlag(SplashScreenActivity.this);
                boolean finishedLoadingKanjiDatabase = AndroidUtilitiesPrefs.getAppPreferenceKanjiDatabaseFinishedLoadingFlag(SplashScreenActivity.this);

                Log.i(Globals.DEBUG_TAG, "Splashscreen - Counter Tick - tick=" + mTicks +
                        " mCentralDbBeingLoaded=" + mCentralDbBeingLoaded + " finishedLoadingCentralDatabase=" + finishedLoadingCentralDatabase +
                        " mKanjiDbBeingLoaded=" + mKanjiDbBeingLoaded + " finishedLoadingKanjiDatabase=" + finishedLoadingKanjiDatabase);

                if (mTicks == 3) showLoadingIndicator();
                if (mCentralDbBeingLoaded) {
                    if (!mLastUIUpdateWasCentral && !finishedLoadingCentralDatabase){
                        binding.splashscreenCurrentLoadingDatabase.setText(getString(R.string.loading_central_database));
                        mLastUIUpdateWasCentral = true;
                        mLastUIUpdateWasKanji = false;
                    }
                }
                else if (mKanjiDbBeingLoaded) {
                    if (!mLastUIUpdateWasKanji && !finishedLoadingKanjiDatabase) {
                        binding.splashscreenCurrentLoadingDatabase.setText(getString(R.string.loading_kanji_database));
                        mLastUIUpdateWasCentral = false;
                        mLastUIUpdateWasKanji = true;
                    }
                }
                if (finishedLoadingCentralDatabase && dbLoadThreadCentral != null) dbLoadThreadCentral.interrupt();
                if (finishedLoadingKanjiDatabase && dbLoadThreadKanji != null) dbLoadThreadKanji.interrupt();
                if (finishedLoadingCentralDatabase && finishedLoadingKanjiDatabase) {
                    hideLoadingIndicator();
                    countDownTimer.onFinish();
                    countDownTimer.cancel();
                }

                mTicks++;
            }

            @Override
            public void onFinish() {
                AndroidUtilitiesPrefs.setAppPreferenceFirstTimeRunningApp(SplashScreenActivity.this, false);
                hideLoadingIndicator();
                if (Looper.myLooper()==null) Looper.prepare();
                //Toast.makeText(SplashScreenActivity.this, R.string.finished_loading_databases, Toast.LENGTH_SHORT).show();
                startMainActivity();
            }
        };
        Log.i(Globals.DEBUG_TAG, "SplashScreen - onCreate - starting counter");
        countDownTimer.start();
    }
    @Override protected void onDestroy() {
        super.onDestroy();
        binding = null;
        countDownTimer.cancel();
    }

    private void startMainActivity() {
        Intent mainIntent = new Intent(SplashScreenActivity.this, MainActivity.class);
        SplashScreenActivity.this.startActivity(mainIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
        SplashScreenActivity.this.overridePendingTransition(0, 0);
        SplashScreenActivity.this.finish();
    }
    private void showLoadingIndicator() {
        binding.splashscreenTimeToLoad.setText(R.string.database_being_installed);
        binding.splashscreenCurrentLoadingDatabase.setVisibility(View.VISIBLE);
        binding.splashscreenLoadingIndicator.setVisibility(View.VISIBLE);
    }
    private void hideLoadingIndicator() {
        binding.splashscreenTimeToLoad.setText(R.string.splashscreen_should_take_only_a_few_seconds);
        binding.splashscreenCurrentLoadingDatabase.setVisibility(View.GONE);
        binding.splashscreenLoadingIndicator.setVisibility(View.INVISIBLE);
    }
    private void startDbInstallationForegroundService() {
        Runnable instantiateRunnable = () -> {
            Intent serviceIntent = new Intent(this, RoomDatabasesInstallationForegroundService.class);
            boolean showNames = AndroidUtilitiesPrefs.getPreferenceShowNames(this);
            int currentExtendedDbVersion = AndroidUtilitiesPrefs.getAppPreferenceDbVersionExtended(this);
            int currentNamesDbVersion = AndroidUtilitiesPrefs.getAppPreferenceDbVersionNames(this);
            boolean installExtendedDb = currentExtendedDbVersion != Globals.EXTENDED_DB_VERSION;
            boolean installNamesDb = currentNamesDbVersion != Globals.NAMES_DB_VERSION;
            serviceIntent.putExtra(getString(R.string.show_names), showNames);
            serviceIntent.putExtra(getString(R.string.install_extended_db), installExtendedDb);
            serviceIntent.putExtra(getString(R.string.install_names_db), installNamesDb);
            if (installExtendedDb || installNamesDb && showNames) startService(serviceIntent);
        };
        Thread instantiateThread = new Thread(instantiateRunnable);
        instantiateThread.start();

    }
    private void stopDbInstallationForegroundService() {
        Intent serviceIntent = new Intent(this, RoomDatabasesInstallationForegroundService.class);
        stopService(serviceIntent);
    }
}
