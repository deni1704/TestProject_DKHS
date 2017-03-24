package com.example.timerexam.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.timerexam.data.TimerParameters;

import java.util.Date;

/**
 * Created by admin on 3/24/17.
 */

public class BootReceiver extends BroadcastReceiver {
    String TAG = BootReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, intent.getAction());

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            TimerParameters timerParameter = TimerParameters.loadFromPreference(context);
            if (timerParameter.started) {
                registerAlarm(context, timerParameter);
            }
        }
    }

    private void registerAlarm(Context context, TimerParameters timerParameter) {
        //First, remove alarm already registered
        removeAlarm(context);

        Log.e(TAG, "registering alarm");
        Intent intent = new Intent(context, ReminderService.class);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);

        if (timerParameter.repeatMode == TimerParameters.RepeatMode.NONE) {
            long nextTime = timerParameter.getNextTriggerTimeInMs() - new Date().getTime();

            if (nextTime != -1) {
                Log.e(TAG, "start one-time alarm after " + nextTime + "ms");
                alarmManager.set(AlarmManager.RTC_WAKEUP, nextTime, pendingIntent);

                timerParameter.started = true;
                timerParameter.saveToPreference(context);
            } else {
                Log.e(TAG, "bad next time");
            }
        } else {
            long nextTime = timerParameter.getNextTriggerTimeInMs() - new Date().getTime();
            long interval = timerParameter.getIntervalInMs();

            if (nextTime != -1 && interval != -1) {
                Log.e(TAG, "start repeated alarm after " + nextTime + "ms" + " with interval " + interval + "ms");
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, nextTime, interval, pendingIntent);

                timerParameter.started = true;
                timerParameter.saveToPreference(context);
            } else {
                Log.e(TAG, "bad next time or interval");
            }
        }
    }

    private void removeAlarm(Context context) {
        Log.e(TAG, "remove alarm");
        Intent intent = new Intent(context, ReminderService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        //Stop service if running
        context.stopService(intent);
    }
}
