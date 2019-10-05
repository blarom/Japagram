package com.japagram.asynctasks;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;

import com.japagram.R;
import com.japagram.data.RoomExtendedDatabase;
import com.japagram.data.RoomNamesDatabase;
import com.japagram.ui.MainActivity;
import com.japagram.ui.SplashScreenActivity;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class RoomDatabasesInstallationForegroundService extends Service {
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

    public RoomDatabasesInstallationForegroundService() {
    }

    public static final String NOTIFICATION_CHANNEL_ID = "ExtendedDbForegroundServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final boolean showNames = intent.getBooleanExtra(getString(R.string.show_names), false);
        boolean delayExtendedDbInstallation = intent.getBooleanExtra(getString(R.string.delay_extended_db_installation), true);
        boolean delayNamesDbInstallation = intent.getBooleanExtra(getString(R.string.delay_names_db_installation), true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "ExtendedDbForegroundServiceChannel";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.common_google_signin_btn_icon_light)
                    .setContentTitle(getBaseContext().getString(R.string.installing_extra_dbs_please_wait))
                    .setContentText(getBaseContext().getString(R.string.starting_installation))
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
        }
        else {
            notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            notification = notificationBuilder.setOngoing(true)
                    .setContentTitle(getBaseContext().getString(R.string.installing_extra_dbs_please_wait))
                    .setContentText(getBaseContext().getString(R.string.starting_installation))
                    .setSmallIcon(R.drawable.common_google_signin_btn_icon_light)
                    .build();
        }

        mExtendedDbBeingLoaded = true;
        mNamesDbBeingLoaded = true;
        Runnable dbLoadRunnableExtended = () -> {
            startForeground(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O? 2 :1, notification);
            RoomExtendedDatabase.getInstance(this); //Required for Room
            mExtendedDbBeingLoaded = false;
        };
        Runnable dbLoadRunnableNames = () -> {
            startForeground(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O? 2 :1, notification);
            RoomNamesDatabase.getInstance(this); //Required for Room
            mNamesDbBeingLoaded = false;
        };

        mFirstTickThread = true;
        countDownTimerThread = new CountDownTimer(30000, 5000) {

            @Override
            public void onTick(long l) {

                if (mFirstTickThread) {
                    if (!delayExtendedDbInstallation) {
                        dbLoadThreadExtended = new Thread(dbLoadRunnableExtended);
                        dbLoadThreadExtended.start();
                    }
                    if (!delayNamesDbInstallation && showNames) {
                        dbLoadThreadNames = new Thread(dbLoadRunnableNames);
                        dbLoadThreadNames.start();
                    }
                    mFirstTickThread = false;
                }
            }

            @Override
            public void onFinish() {
                if (delayExtendedDbInstallation) {
                    dbLoadThreadExtended = new Thread(dbLoadRunnableExtended);
                    dbLoadThreadExtended.start();
                }
                if (delayNamesDbInstallation && showNames) {
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

                if (mExtendedDbBeingLoaded) {
                    notificationBuilder.setContentText(getBaseContext().getString(R.string.installing_EDICT));
                } else if (mNamesDbBeingLoaded) {
                    notificationBuilder.setContentText(getBaseContext().getString(R.string.installing_names));
                } else {
                    notificationBuilder.setContentText(getBaseContext().getString(R.string.finished));
                }
                notification = notificationBuilder.build();

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
