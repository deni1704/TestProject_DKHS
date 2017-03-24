package com.example.timerexam.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.example.timerexam.data.TimerParameters;
import com.example.timerexam.ui.MainActivity;

/**
 * Created by admin on 3/24/17.
 */

public class ReminderService extends Service {
    private String TAG = ReminderService.class.getSimpleName();
    private static int NOTIFY_UID = 1200;

    private MediaPlayer mMediaPlayer;
    private TimerParameters mTimerParameter;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "destroy");

        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "create");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mTimerParameter = TimerParameters.loadFromPreference(getApplicationContext());


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Log.e(TAG, "ReminderService triggered!!!");
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(this, MainActivity.class), 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setTicker("TimerExam")
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle("Alarm started")
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(NOTIFY_UID, notification);

        //stop itself after period
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "reached to max time period, stopping service");
                ReminderService.this.stopSelf();
            }
        }, mTimerParameter.getPeriodInMs());

        //play audio looping
        if (mTimerParameter.isValidTonePath()) {
            initMediaPlayer();
        }
        return START_NOT_STICKY;
    }

    private void initMediaPlayer() {
        try {
            Log.e(TAG, "play started....");
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(mTimerParameter.tonePath);
            mMediaPlayer.prepare();
            mMediaPlayer.setLooping(true);
            mMediaPlayer.start();

            if (mTimerParameter.playInterval > 0 && mTimerParameter.pauseInterval > 0) { //don't need Play/Pause when pause interval equals 0
                new PlayStopCounter(mTimerParameter.playInterval * 60 * 1000, 1000).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class PlayStopCounter extends CountDownTimer {
        public PlayStopCounter(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            if (mMediaPlayer != null) {
                Log.e(TAG, "reached to end of play time");
                mMediaPlayer.release();
                mMediaPlayer = null;

                new PauseStopCounter(mTimerParameter.pauseInterval * 60 * 1000, 1000).start();
            }
        }
    }

    private class PauseStopCounter extends CountDownTimer {
        public PauseStopCounter(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            Log.e(TAG, "reached to end of pause time");
            initMediaPlayer();
        }
    }
}
