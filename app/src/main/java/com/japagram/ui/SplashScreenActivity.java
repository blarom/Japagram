package com.japagram.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.japagram.R;
import com.japagram.data.RoomCentralDatabase;
import com.japagram.data.RoomExtendedDatabase;
import com.japagram.data.RoomKanjiDatabase;
import com.japagram.data.RoomNamesDatabase;
import com.japagram.databinding.ActivitySplashscreenBinding;
import com.japagram.resources.LocaleHelper;
import com.japagram.utilitiesAndroid.AndroidUtilitiesIO;
import com.japagram.utilitiesAndroid.AndroidUtilitiesPrefs;
import com.japagram.utilitiesCrossPlatform.Globals;
import com.japagram.utilitiesCrossPlatform.UtilitiesDb;
import com.japagram.utilitiesPlatformOverridable.OvUtilsGeneral;

public class SplashScreenActivity extends BaseActivity {

    private ActivitySplashscreenBinding binding;
    private CountDownTimer countDownTimer;
    private Thread dbLoadDatabasesThread;
    boolean mLastUIUpdateWasCentral;
    boolean mLastUIUpdateWasExtended;
    boolean mLastUIUpdateWasKanji;
    boolean mLastUIUpdateWasNames;
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

        mLanguage = LocaleHelper.getLanguage(getBaseContext());

        //Loading databases in series
        Runnable dbLoadRunnableCentral = () -> {
            loadSmallDatabases(getBaseContext(), mLanguage);
            Log.i(Globals.DEBUG_TAG, "Splashscreen - Loaded Small databases");
            RoomKanjiDatabase.getInstance(SplashScreenActivity.this); //Required for Room
            Log.i(Globals.DEBUG_TAG, "Splashscreen - Instantiated RoomKanjiDatabase");
            RoomCentralDatabase.getInstance(SplashScreenActivity.this); //Required for Room
            Log.i(Globals.DEBUG_TAG, "Splashscreen - Instantiated RoomCentralDatabase");
            RoomExtendedDatabase.getInstance(SplashScreenActivity.this); //Required for Room
            Log.i(Globals.DEBUG_TAG, "Splashscreen - Instantiated RoomExtendedDatabase");
            RoomNamesDatabase.getInstance(SplashScreenActivity.this); //Required for Room
            Log.i(Globals.DEBUG_TAG, "Splashscreen - Instantiated RoomNamesDatabase");
        };
        dbLoadDatabasesThread = new Thread(dbLoadRunnableCentral);
        dbLoadDatabasesThread.start();

        //showLoadingIndicator();
        if (AndroidUtilitiesPrefs.getAppPreferenceFirstTimeRunningApp(SplashScreenActivity.this)) {
            Toast.makeText(SplashScreenActivity.this, R.string.first_time_installing, Toast.LENGTH_LONG).show();
        }

        int centralDbVersion = AndroidUtilitiesPrefs.getAppPreferenceDbVersionCentral(this);
        AndroidUtilitiesPrefs.setAppPreferenceCentralDatabasesFinishedLoadingFlag(this, centralDbVersion == Globals.CENTRAL_DB_VERSION);
        int kanjiDbVersion = AndroidUtilitiesPrefs.getAppPreferenceDbVersionKanji(this);
        AndroidUtilitiesPrefs.setAppPreferenceKanjiDatabaseFinishedLoadingFlag(this, kanjiDbVersion == Globals.KANJI_DB_VERSION);

        mTicks = 0;
        countDownTimer = new CountDownTimer(3600000, 200) {

            @Override
            public void onTick(long l) {

                if (mTicks == -1) {
                    //mTicks == -1 is used to add a small delay after dbs updated, to prevent db update vs. counter end race
                    hideLoadingIndicator();
                    countDownTimer.onFinish();
                }
                if (mTicks == 0 || (mTicks > 3 && mTicks % 3 == 0)) { //Reducing the amount of computations
                    mTicks++;
                    return;
                }

                //Delaying the start of db loading if the app uses too much memory
                boolean finishedLoadingCentralDatabase = AndroidUtilitiesPrefs.getAppPreferenceCentralDatabasesFinishedLoadingFlag(SplashScreenActivity.this);
                boolean finishedLoadingExtendedDatabase = AndroidUtilitiesPrefs.getAppPreferenceExtendedDatabasesFinishedLoadingFlag(SplashScreenActivity.this);
                boolean finishedLoadingNamesDatabase = AndroidUtilitiesPrefs.getAppPreferenceNamesDatabasesFinishedLoadingFlag(SplashScreenActivity.this);
                boolean finishedLoadingKanjiDatabase = AndroidUtilitiesPrefs.getAppPreferenceKanjiDatabaseFinishedLoadingFlag(SplashScreenActivity.this);

                Log.i(Globals.DEBUG_TAG, "Splashscreen - Counter Tick - tick=" + mTicks +
                        " finishedLoadingCentralDatabase=" + finishedLoadingCentralDatabase +
                        " finishedLoadingKanjiDatabase=" + finishedLoadingKanjiDatabase +
                        " finishedLoadingExtendedDatabase=" + finishedLoadingExtendedDatabase +
                        " finishedLoadingNamesDatabase=" + finishedLoadingNamesDatabase);

                if (mTicks == 3) {
                    showLoadingIndicator();
                }
                if (!finishedLoadingKanjiDatabase) {
                    if (!mLastUIUpdateWasKanji) {
                        binding.splashscreenCurrentLoadingDatabase.setText(getString(R.string.loading_kanji_database));
                        mLastUIUpdateWasKanji = true;
                        mLastUIUpdateWasCentral = false;
                        mLastUIUpdateWasExtended = false;
                        mLastUIUpdateWasNames = false;
                    }
                }
                else if (!finishedLoadingCentralDatabase) {
                    if (!mLastUIUpdateWasCentral){
                        binding.splashscreenCurrentLoadingDatabase.setText(getString(R.string.loading_central_database));
                        mLastUIUpdateWasKanji = false;
                        mLastUIUpdateWasCentral = true;
                        mLastUIUpdateWasExtended = false;
                        mLastUIUpdateWasNames = false;
                    }
                }
                else if (!finishedLoadingExtendedDatabase) {
                    if (!mLastUIUpdateWasExtended){
                        binding.splashscreenCurrentLoadingDatabase.setText(getString(R.string.loading_extended_database));
                        mLastUIUpdateWasKanji = false;
                        mLastUIUpdateWasCentral = false;
                        mLastUIUpdateWasExtended = true;
                        mLastUIUpdateWasNames = false;
                    }
                }
                else if (!finishedLoadingNamesDatabase) {
                    if (!mLastUIUpdateWasNames){
                        binding.splashscreenCurrentLoadingDatabase.setText(getString(R.string.loading_names_database));
                        mLastUIUpdateWasCentral = false;
                        mLastUIUpdateWasKanji = false;
                        mLastUIUpdateWasExtended = false;
                        mLastUIUpdateWasNames = true;
                    }
                }

                mTicks++;
                if (finishedLoadingCentralDatabase
                        && finishedLoadingKanjiDatabase
                        && finishedLoadingExtendedDatabase
                        && finishedLoadingNamesDatabase) {
                    mTicks = -1;
                }
            }

            @Override
            public void onFinish() {
                AndroidUtilitiesPrefs.setAppPreferenceFirstTimeRunningApp(SplashScreenActivity.this, false);
                hideLoadingIndicator();
                if (Looper.myLooper()==null) Looper.prepare();
                //Toast.makeText(SplashScreenActivity.this, R.string.finished_loading_databases, Toast.LENGTH_SHORT).show();
                if (dbLoadDatabasesThread != null) dbLoadDatabasesThread.interrupt();
                startMainActivity();
                countDownTimer.cancel();
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

    public static void loadSmallDatabases(Context context, String language) {
        Globals.GLOBAL_SIMILARS_DATABASE = AndroidUtilitiesIO.readCSVFile("LineSimilars - 3000 kanji.csv", context);
        Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE = AndroidUtilitiesIO.readCSVFile("LineLatinConj - 3000 kanji.csv", context);
        Globals.GLOBAL_VERB_KANJI_CONJ_DATABASE = AndroidUtilitiesIO.readCSVFile("LineKanjiConj - 3000 kanji.csv", context);
        Globals.GLOBAL_VERB_LATIN_CONJ_LENGTHS = AndroidUtilitiesIO.readCSVFile("LineVerbsLengths - 3000 kanji.csv", context);
        Globals.GLOBAL_VERB_KANJI_CONJ_LENGTHS = AndroidUtilitiesIO.readCSVFile("LineVerbsKanjiLengths - 3000 kanji.csv", context);
        Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE_NO_SPACES = UtilitiesDb.removeSpacesFromConjDb(Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE);
        Globals.GLOBAL_RADICALS_ONLY_DATABASE = AndroidUtilitiesIO.readCSVFile("LineRadicalsOnly - 3000 kanji.csv", context);
        Globals.GLOBAL_ROMANIZATIONS = OvUtilsGeneral.getTranspose(AndroidUtilitiesIO.readCSVFile("LineRomanizations.csv", context));
        Globals.GLOBAL_CONJUGATION_TITLES = com.japagram.utilitiesCrossPlatform.UtilitiesDb.getConjugationTitles(Globals.GLOBAL_VERB_LATIN_CONJ_DATABASE, context, language);
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
}
