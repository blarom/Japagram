package com.japagram.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.japagram.asynctasks.RoomDatabasesInstallationForegroundService;
import com.japagram.R;
import com.japagram.data.RoomCentralDatabase;
import com.japagram.data.RoomKanjiDatabase;
import com.japagram.resources.Globals;
import com.japagram.resources.Utilities;
import com.japagram.resources.UtilitiesDb;
import com.japagram.resources.UtilitiesPrefs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class SplashScreenActivity extends BaseActivity {

    private final int SPLASH_DISPLAY_LENGTH = 1000; //Miliseconds
    private Unbinder mBinding;
    @BindView(R.id.splashscreen_loading_indicator) ProgressBar mProgressBarLoadingIndicator;
    @BindView(R.id.splashscreen_time_to_load_textview) TextView mTimeToLoadTextView;
    @BindView(R.id.splashscreen_current_loading_database) TextView mLoadingDatabaseTextView;
    @BindView(R.id.splashscreen_loading_database_textview) TextView mLoadingDbTextView;
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

    @Override protected void onCreate(Bundle savedInstanceState) {

        Log.i(Globals.DEBUG_TAG, "Started Splashscreen.");

        UtilitiesPrefs.changeThemeColor(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splashscreen);

        mBinding =  ButterKnife.bind(this);

        mLoadingDbTextView.setTextColor(UtilitiesPrefs.getResColorValue(this, R.attr.colorPrimaryLight));

        mCentralDbBeingLoaded = true;
        mKanjiDbBeingLoaded = true;

        startDbInstallationForegroundService();

        //Loading databases in parallel or series depending on available heap memory (more or less than 1000MB respectively)
        dbLoadRunnableCentral = () -> {
            mCentralDbBeingLoaded = true;
            Globals.SimilarsDatabase = Utilities.readCSVFile("LineSimilars - 3000 kanji.csv", getBaseContext());
            Globals.VerbLatinConjDatabase = Utilities.readCSVFile("LineLatinConj - 3000 kanji.csv", getBaseContext());
            Globals.VerbLatinConjDatabaseNoSpaces = UtilitiesDb.removeSpacesFromConjDb(Globals.VerbLatinConjDatabase);
            Globals.VerbKanjiConjDatabase = Utilities.readCSVFile("LineKanjiConj - 3000 kanji.csv", getBaseContext());
            Globals.RadicalsOnlyDatabase = Utilities.readCSVFile("LineRadicalsOnly - 3000 kanji.csv", getBaseContext());
            Globals.Romanizations = Utilities.readCSVFile("LineRomanizations.csv", getBaseContext());
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
        if (UtilitiesPrefs.getAppPreferenceFirstTimeRunningApp(SplashScreenActivity.this)) {
            Toast.makeText(SplashScreenActivity.this, R.string.first_time_installing, Toast.LENGTH_LONG).show();
        }

        mTicks = 0;
        countDownTimer = new CountDownTimer(3600000, 500) {

            @Override
            public void onTick(long l) {

                //Delaying the start of db loading if the app uses too much memory
                boolean finishedLoadingCentralDatabase = UtilitiesPrefs.getAppPreferenceWordVerbDatabasesFinishedLoadingFlag(SplashScreenActivity.this);
                boolean finishedLoadingKanjiDatabase = UtilitiesPrefs.getAppPreferenceKanjiDatabaseFinishedLoadingFlag(SplashScreenActivity.this);

                if (mTicks >= 2) {
                    if (mCentralDbBeingLoaded) {
                        if (!mLastUIUpdateWasCentral && !finishedLoadingCentralDatabase){
                            if (mLoadingDatabaseTextView != null) mLoadingDatabaseTextView.setText(getString(R.string.loading_central_database));
                            mLastUIUpdateWasCentral = true;
                            mLastUIUpdateWasKanji = false;
                        }
                    }
                    else if (mKanjiDbBeingLoaded) {
                        if (!mLastUIUpdateWasKanji && !finishedLoadingKanjiDatabase) {
                            if (mLoadingDatabaseTextView != null) mLoadingDatabaseTextView.setText(getString(R.string.loading_kanji_database));
                            mLastUIUpdateWasCentral = false;
                            mLastUIUpdateWasKanji = true;
                        }
                    }
                    if (finishedLoadingCentralDatabase) {
                        if (dbLoadThreadCentral != null) dbLoadThreadCentral.interrupt();
                    }
                    if (finishedLoadingKanjiDatabase) {
                        if (dbLoadThreadKanji != null) dbLoadThreadKanji.interrupt();
                    }
                    if (finishedLoadingCentralDatabase && finishedLoadingKanjiDatabase) {
                        hideLoadingIndicator();
                        countDownTimer.onFinish();
                        countDownTimer.cancel();
                    }
                }

                if (mTicks == 3) {
                    showLoadingIndicator();
                }

                mTicks++;
            }

            @Override
            public void onFinish() {
                UtilitiesPrefs.setAppPreferenceFirstTimeRunningApp(SplashScreenActivity.this, false);
                hideLoadingIndicator();
                if (Looper.myLooper()==null) Looper.prepare();
                //Toast.makeText(SplashScreenActivity.this, R.string.finished_loading_databases, Toast.LENGTH_SHORT).show();
                startMainActivity();
            }
        };
        countDownTimer.start();

    }


    @Override protected void onDestroy() {
        super.onDestroy();
        mBinding.unbind();
        countDownTimer.cancel();
    }

    private void startMainActivity() {
        Intent mainIntent = new Intent(SplashScreenActivity.this, MainActivity.class);
        SplashScreenActivity.this.overridePendingTransition(0, 0);
        SplashScreenActivity.this.startActivity(mainIntent);
        SplashScreenActivity.this.finish();
    }
    private void showLoadingIndicator() {
        if (mTimeToLoadTextView!=null) mTimeToLoadTextView.setText(R.string.database_being_installed);
        if (mLoadingDatabaseTextView!=null) mLoadingDatabaseTextView.setVisibility(View.VISIBLE);
        if (mProgressBarLoadingIndicator!=null) mProgressBarLoadingIndicator.setVisibility(View.VISIBLE);
    }
    private void hideLoadingIndicator() {
        if (mTimeToLoadTextView!=null) mTimeToLoadTextView.setText(R.string.splashscreen_should_take_only_a_few_seconds);
        if (mLoadingDatabaseTextView!=null) mLoadingDatabaseTextView.setVisibility(View.GONE);
        if (mProgressBarLoadingIndicator!=null) mProgressBarLoadingIndicator.setVisibility(View.INVISIBLE);
    }
    private void startDbInstallationForegroundService() {
        Intent serviceIntent = new Intent(this, RoomDatabasesInstallationForegroundService.class);
        boolean showNames = UtilitiesPrefs.getPreferenceShowNames(this);
        int currentExtendedDbVersion = UtilitiesPrefs.getAppPreferenceDbVersionExtended(this);
        int currentNamesDbVersion = UtilitiesPrefs.getAppPreferenceDbVersionNames(this);
        boolean installExtendedDb = currentExtendedDbVersion != Globals.EXTENDED_DB_VERSION;
        boolean installNamesDb = currentNamesDbVersion != Globals.NAMES_DB_VERSION;
        serviceIntent.putExtra(getString(R.string.show_names), showNames);
        serviceIntent.putExtra(getString(R.string.install_extended_db), installExtendedDb);
        serviceIntent.putExtra(getString(R.string.install_names_db), installNamesDb);
        if (installExtendedDb || installNamesDb && showNames) startService(serviceIntent);

    }
    private void stopDbInstallationForegroundService() {
        Intent serviceIntent = new Intent(this, RoomDatabasesInstallationForegroundService.class);
        stopService(serviceIntent);
    }
}
