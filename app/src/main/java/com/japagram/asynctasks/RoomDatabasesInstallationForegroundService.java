package com.japagram.asynctasks;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;

import com.japagram.R;
import com.japagram.data.RoomExtendedDatabase;
import com.japagram.data.RoomNamesDatabase;
import com.japagram.resources.LocaleHelper;
import com.japagram.utilitiesAndroid.AndroidUtilitiesPrefs;
import com.japagram.ui.MainActivity;
import com.japagram.utilitiesCrossPlatform.Globals;

import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class RoomDatabasesInstallationForegroundService extends Service {
    public static final int NOTIF_CHAN_1 = 1;
    public static final int NOTIF_CHAN_2 = 2;
    private boolean mExtendedDbBeingLoaded;
    private boolean mNamesDbBeingLoaded;
    private Notification notification;
    private NotificationCompat.Builder notificationBuilder;
    private CountDownTimer countDownTimerDisplay;
    private CountDownTimer countDownTimerThread;
    private Thread dbLoadThreadExtended;
    private Thread dbLoadThreadNames;
    private boolean mFirstTickDisplay;
    private boolean mFirstTickThread;
    private NotificationManager manager;

    public RoomDatabasesInstallationForegroundService() {
    }

    public static final String NOTIFICATION_CHANNEL_ID = "ExtendedDbForegroundServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();

        Locale locale = Locale.getDefault();// get the locale to use...
        Configuration conf = getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= 17) {
            conf.setLocale(locale);
        } else {
            conf.locale = locale;
        }

    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final boolean showNames = intent.getBooleanExtra(getString(R.string.show_names), false);
        boolean installExtendedDb = intent.getBooleanExtra(getString(R.string.install_extended_db), false);
        boolean installNamesDb = intent.getBooleanExtra(getString(R.string.install_names_db), true);
        boolean firstTimeRunningApp = AndroidUtilitiesPrefs.getAppPreferenceFirstTimeRunningApp(this);

        Intent appIntent = new Intent(this, MainActivity.class);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "ExtendedDbForegroundServiceChannel";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getBaseContext().getString(R.string.installing_extra_dbs_please_wait))
                    .setContentText(getBaseContext().getString(R.string.starting_installation))
                    .setProgress(100, 0, false)
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(false);
            notification = notificationBuilder.build();
        }
        else {
            notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getBaseContext().getString(R.string.installing_extra_dbs_please_wait))
                    .setContentText(getBaseContext().getString(R.string.starting_installation))
                    .setProgress(100, 0, false);
            notification = notificationBuilder.build();
        }
        if (manager!=null) manager.notify(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O? NOTIF_CHAN_2 : NOTIF_CHAN_1, notification);

        mExtendedDbBeingLoaded = installExtendedDb;
        mNamesDbBeingLoaded = installNamesDb && showNames;
        Runnable dbLoadRunnableExtended = () -> {
            startForeground(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O? NOTIF_CHAN_2 : NOTIF_CHAN_1, notification);
            RoomExtendedDatabase.getInstance(this); //Required for Room
            mExtendedDbBeingLoaded = false;
        };
        Runnable dbLoadRunnableNames = () -> {
            startForeground(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O? NOTIF_CHAN_2 : NOTIF_CHAN_1, notification);
            RoomNamesDatabase.getInstance(this); //Required for Room
            mNamesDbBeingLoaded = false;
        };

        mFirstTickThread = true;
        countDownTimerThread = new CountDownTimer(30000, 5000) {

            @Override
            public void onTick(long l) {

                if (mFirstTickThread) {
                    if (installNamesDb && showNames && !firstTimeRunningApp) {
                        dbLoadThreadNames = new Thread(dbLoadRunnableNames);
                        dbLoadThreadNames.start();
                    }
                    mFirstTickThread = false;
                }
            }

            @Override
            public void onFinish() {
                if (installExtendedDb) {
                    dbLoadThreadExtended = new Thread(dbLoadRunnableExtended);
                    dbLoadThreadExtended.start();
                }
                if (installNamesDb && showNames && firstTimeRunningApp) {
                    dbLoadThreadNames = new Thread(dbLoadRunnableNames);
                    dbLoadThreadNames.start();
                }
                countDownTimerThread.cancel();
            }
        };
        countDownTimerThread.start();

        final int totalTime = 3600000;
        mFirstTickDisplay = true;
        countDownTimerDisplay = new CountDownTimer(totalTime, 5000) {

            @Override
            public void onTick(long millisUntilFinished) {

                if (mFirstTickDisplay) {
                    mFirstTickDisplay = false;
                    return;
                }

                String content;
                int currentProgress = 100;
                if (mExtendedDbBeingLoaded) {
                    currentProgress = (int) AndroidUtilitiesPrefs.getProgressValueForDbInstallation(getBaseContext(), Globals.EXTENDED_DB);
                    content = getBaseContext().getString(R.string.installing_EDICT) + " (" + currentProgress + "%)";
                } else if (mNamesDbBeingLoaded) {
                    currentProgress = (int) AndroidUtilitiesPrefs.getProgressValueForDbInstallation(getBaseContext(), Globals.NAMES_DB);
                    content = getBaseContext().getString(R.string.installing_names) + " (" + currentProgress + "%)";
                } else {
                    content = getBaseContext().getString(R.string.finished);
                }
                notificationBuilder.setOngoing(true)
                        .setContentTitle(getBaseContext().getString(R.string.installing_extra_dbs_please_wait))
                        .setContentText(content)
                        .setProgress(100, currentProgress, false);
                notification = notificationBuilder.build();
                if (manager!=null) manager.notify(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O? NOTIF_CHAN_2 : NOTIF_CHAN_1, notification);

                if (!mExtendedDbBeingLoaded) {
                    if (dbLoadThreadExtended != null) dbLoadThreadExtended.interrupt();
                }
                if (showNames && !mNamesDbBeingLoaded) {
                    if (dbLoadThreadNames != null) dbLoadThreadNames.interrupt();
                }

                if (!mExtendedDbBeingLoaded && (!showNames || !mNamesDbBeingLoaded)){
                    countDownTimerDisplay.onFinish();
                }
            }

            @Override
            public void onFinish() {
                countDownTimerDisplay.cancel();
                stopSelf();
            }
        };
        countDownTimerDisplay.start();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
