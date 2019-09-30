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

import com.japagram.R;
import com.japagram.data.RoomCentralDatabase;
import com.japagram.data.RoomExtendedDatabase;
import com.japagram.data.RoomKanjiDatabase;
import com.japagram.data.RoomNamesDatabase;
import com.japagram.resources.Utilities;

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
    private boolean mExtendedDbBeingLoaded;
    private boolean mNamesDbBeingLoaded;
    private boolean mKanjiDbBeingLoaded;
    private CountDownTimer countDownTimer;
    private Runnable dbLoadRunnableCentral;
    private Runnable dbLoadRunnableExtended;
    private Runnable dbLoadRunnableNames;
    private Runnable dbLoadRunnableKanji;
    private Thread dbLoadThread;
    boolean mLastUIUpdateWasCentral;
    boolean mLastUIUpdateWasExtended;
    boolean mLastUIUpdateWasNames;
    boolean mLastUIUpdateWasKanji;
    private int mTicks;

    @Override protected void onCreate(Bundle savedInstanceState) {

        Log.i("Diagnosis Time", "Started Splashscreen.");

        Utilities.changeThemeColor(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splashscreen);

        mBinding =  ButterKnife.bind(this);

        mLoadingDbTextView.setTextColor(Utilities.getResColorValue(this, R.attr.colorPrimaryLight));

        mCentralDbBeingLoaded = true;
        mExtendedDbBeingLoaded = true;
        mNamesDbBeingLoaded = true;
        mKanjiDbBeingLoaded = true;

        //Loading databases in parallel or series depending on available heap memory (more or less than 1000MB respectively)
        dbLoadRunnableCentral = () -> {
            mCentralDbBeingLoaded = true;
            RoomCentralDatabase.getInstance(SplashScreenActivity.this); //Required for Room
            mCentralDbBeingLoaded = false;
        };
        dbLoadRunnableExtended = () -> {
            mExtendedDbBeingLoaded = true;
            RoomExtendedDatabase.getInstance(SplashScreenActivity.this); //Required for Room
            mExtendedDbBeingLoaded = false;
        };
        dbLoadRunnableKanji = () -> {
            mKanjiDbBeingLoaded = true;
            RoomKanjiDatabase.getInstance(SplashScreenActivity.this); //Required for Room
            mKanjiDbBeingLoaded = false;
        };
        dbLoadRunnableNames = () -> {
            mNamesDbBeingLoaded = true;
            RoomNamesDatabase.getInstance(SplashScreenActivity.this); //Required for Room
            mNamesDbBeingLoaded = false;
        };
        dbLoadThread = new Thread(dbLoadRunnableCentral);
        dbLoadThread.start();
        dbLoadThread = new Thread(dbLoadRunnableExtended);
        dbLoadThread.start();
        dbLoadThread = new Thread(dbLoadRunnableKanji);
        dbLoadThread.start();
        boolean showNames = Utilities.getPreferenceShowNames(SplashScreenActivity.this);
        if (showNames) {
            dbLoadThread = new Thread(dbLoadRunnableNames);
            dbLoadThread.start();
        }

        showLoadingIndicator();
        if (Utilities.getAppPreferenceFirstTimeInstalling(SplashScreenActivity.this)) {
            Toast.makeText(SplashScreenActivity.this, R.string.first_time_installing, Toast.LENGTH_SHORT).show();
        }

        mTicks = 0;
        countDownTimer = new CountDownTimer(3600000, 500) {

            boolean showNames = Utilities.getPreferenceShowNames(SplashScreenActivity.this);

            @Override
            public void onTick(long l) {

                //Delaying the start of db loading if the app uses too much memory
                boolean finishedLoadingCentralDatabase = Utilities.getAppPreferenceWordVerbDatabasesFinishedLoadingFlag(SplashScreenActivity.this);
                boolean finishedLoadingExtendedDatabase = Utilities.getAppPreferenceExtendedDatabasesFinishedLoadingFlag(SplashScreenActivity.this);
                boolean finishedLoadingKanjiDatabase = Utilities.getAppPreferenceKanjiDatabaseFinishedLoadingFlag(SplashScreenActivity.this);
                boolean finishedLoadingNamesDatabase = !showNames || Utilities.getAppPreferenceNamesDatabasesFinishedLoadingFlag(SplashScreenActivity.this);

                if (mCentralDbBeingLoaded) {
                    if (!mLastUIUpdateWasCentral && !finishedLoadingCentralDatabase){
                        if (mLoadingDatabaseTextView != null) mLoadingDatabaseTextView.setText(getString(R.string.loading_central_database));
                        mLastUIUpdateWasCentral = true;
                        mLastUIUpdateWasExtended = false;
                        mLastUIUpdateWasNames = true;
                        mLastUIUpdateWasKanji = true;
                    }
                }
                else if (mExtendedDbBeingLoaded) {
                    if (!mLastUIUpdateWasExtended && !finishedLoadingExtendedDatabase) {
                        if (mLoadingDatabaseTextView != null) mLoadingDatabaseTextView.setText(getString(R.string.loading_extended_database));
                        mLastUIUpdateWasCentral = false;
                        mLastUIUpdateWasExtended = true;
                        mLastUIUpdateWasNames = false;
                        mLastUIUpdateWasKanji = false;
                    }
                }
                else if (mKanjiDbBeingLoaded) {
                    if (!mLastUIUpdateWasKanji && !finishedLoadingKanjiDatabase) {
                        if (mLoadingDatabaseTextView != null) mLoadingDatabaseTextView.setText(getString(R.string.loading_kanji_database));
                        mLastUIUpdateWasCentral = false;
                        mLastUIUpdateWasExtended = false;
                        mLastUIUpdateWasNames = false;
                        mLastUIUpdateWasKanji = true;
                    }
                }
                else if (mNamesDbBeingLoaded) {
                    if (!mLastUIUpdateWasNames && !finishedLoadingNamesDatabase) {
                        if (mLoadingDatabaseTextView != null) mLoadingDatabaseTextView.setText(getString(R.string.loading_names_database));
                        mLastUIUpdateWasCentral = false;
                        mLastUIUpdateWasExtended = false;
                        mLastUIUpdateWasNames = true;
                        mLastUIUpdateWasKanji = false;
                    }
                }

                if (mTicks >= 2 && finishedLoadingCentralDatabase && finishedLoadingExtendedDatabase && finishedLoadingKanjiDatabase && (!showNames || finishedLoadingNamesDatabase)){
                    hideLoadingIndicator();
                    countDownTimer.onFinish();
                    countDownTimer.cancel();
                }

                mTicks++;
            }

            @Override
            public void onFinish() {
                Utilities.setAppPreferenceFirstTimeInstalling(SplashScreenActivity.this, false);
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
}
