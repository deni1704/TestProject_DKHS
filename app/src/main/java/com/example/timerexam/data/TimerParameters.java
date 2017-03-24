package com.example.timerexam.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by admin on 3/24/17.
 */

public class TimerParameters {
    String TAG = TimerParameters.class.getSimpleName();
    public enum RepeatMode {
        NONE(0), DAILY(1), WEEKLY(2), MONTHLY(3);

        private final int value;
        RepeatMode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static RepeatMode fromInt(int value) {
            switch (value) {
                case 0: return NONE;
                case 1: return DAILY;
                case 2: return WEEKLY;
                case 3: return MONTHLY;
            }
            return null;
        }
    }

    public enum IntegrityResult {
        OK,
        FAIL_WITH_TIME_MISMATCH,
        FAIL_WITH_NO_SOUND,
        FAIL_WITH_PASSED,
        FAIL_WITH_ZERO_PLAY
    };

    private static SimpleDateFormat sdfDate;
    private static SimpleDateFormat sdfDateTime;
    static {
        sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdfDateTime = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.getDefault());
    }

    public String startDate;
    public String startTime;
    public String endTime;
    public int playInterval;
    public int pauseInterval;
    public RepeatMode repeatMode;
    public String tonePath;
    public boolean vibrate;
    public boolean started;

    public String getToneTitle() {
        if(tonePath == null) {
            return "Not Set";
        } else {
            return tonePath.substring(tonePath.lastIndexOf("/") + 1);
        }
    }

    private static final String PREF_NAME = "parameter";
    private static final String PREF_KEY_REPEAT_MODE = "repeatMode";
    private static final String PREF_KEY_START_DATE = "startDate";
    private static final String PREF_KEY_START_TIME = "startTime";
    private static final String PREF_KEY_END_TIME = "endTime";
    private static final String PREF_KEY_PLAY_INTERVAL = "playInterval";
    private static final String PREF_KEY_PAUSE_INTERVAL = "pauseInterval";
    private static final String PREF_KEY_TONE_PATH = "tonePath";
    private static final String PREF_KEY_VIBRATE = "vibrate";

    private static final String PREF_KEY_STARTED = "started";

    public static TimerParameters loadFromPreference(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        TimerParameters param = new TimerParameters();
        param.repeatMode = RepeatMode.fromInt(pref.getInt(PREF_KEY_REPEAT_MODE, RepeatMode.NONE.getValue()));
        param.startDate = pref.getString(PREF_KEY_START_DATE, sdfDate.format(new Date()));
        param.startTime = pref.getString(PREF_KEY_START_TIME, "12:00");
        param.endTime = pref.getString(PREF_KEY_END_TIME, "19:00");
        param.playInterval = pref.getInt(PREF_KEY_PLAY_INTERVAL, 4);
        param.pauseInterval = pref.getInt(PREF_KEY_PAUSE_INTERVAL, 2);
        param.tonePath = pref.getString(PREF_KEY_TONE_PATH, null);
        param.vibrate = pref.getBoolean(PREF_KEY_VIBRATE, false);
        param.started = pref.getBoolean(PREF_KEY_STARTED, false);
        return param;
    }

    public void saveToPreference(Context context) {
        SharedPreferences.Editor e = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        e.putInt(PREF_KEY_REPEAT_MODE, repeatMode.getValue());
        e.putString(PREF_KEY_START_DATE, startDate);
        e.putString(PREF_KEY_START_TIME, startTime);
        e.putString(PREF_KEY_END_TIME, endTime);
        e.putInt(PREF_KEY_PLAY_INTERVAL, playInterval);
        e.putInt(PREF_KEY_PAUSE_INTERVAL, pauseInterval);
        e.putString(PREF_KEY_TONE_PATH, tonePath);
        e.putBoolean(PREF_KEY_VIBRATE, vibrate);
        e.putBoolean(PREF_KEY_STARTED, started);
        e.apply();
    }

    public long getNextTriggerTimeInMs() {
        if(repeatMode == RepeatMode.DAILY) {
            try {
                Date startDateTime = sdfDateTime.parse(startDate + " " + startTime);
                Log.e(TAG, "start time = " + startDateTime.toString());
                if(startDateTime.before(new Date())) {
                    Log.e(TAG, "not available for today, use tomorrow time");
                    return startDateTime.getTime() + 24 * 60 * 60 * 1000;
                } else {
                    return startDateTime.getTime();
                }
            } catch (ParseException e) {
                e.printStackTrace();
                return -1;
            }

        } else {
            try {
                Date startDateTime = sdfDateTime.parse(startDate + " " + startTime);
                Log.e(TAG, "start time = " + startDateTime.toString());
                return startDateTime.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
                return -1;
            }

        }
    }

    long DAY_IN_MS = 24 * 3600 * 1000;
    long WEEK_IN_MS = 7 * DAY_IN_MS;
    long MONTH_IN_MS = 30 * DAY_IN_MS; //TODO : Requirement is not clear. Treat it's meaning as 30 days
    public long getIntervalInMs() {
        switch(repeatMode) {
            case NONE:
                return 0;
            case DAILY:
                return DAY_IN_MS;
            case WEEKLY:
                return WEEK_IN_MS;
            case MONTHLY:
                return MONTH_IN_MS;
            default:
                return -1;
        }
    }

    public IntegrityResult test() {
        //End-time should be greater than start-time
        if(getPeriodInMs() <= 0) {
            return IntegrityResult.FAIL_WITH_TIME_MISMATCH;
        }
        //No sounds nor vibrate
        if(tonePath == null && !vibrate) {
            return IntegrityResult.FAIL_WITH_NO_SOUND;
        }

        if(repeatMode == RepeatMode.NONE && getNextTriggerTimeInMs() < new Date().getTime()) {
            return IntegrityResult.FAIL_WITH_PASSED;
        }

        if(playInterval == 0) {
            return IntegrityResult.FAIL_WITH_ZERO_PLAY;
        }
        return IntegrityResult.OK;
    }

    public boolean isValidTonePath() {
        return tonePath != null && new File(tonePath).exists();
    }

    public long getPeriodInMs() {
        String[] sMS = startTime.split(":");
        String[] eMS = endTime.split(":");

        int sM = Integer.parseInt(sMS[0]), sS = Integer.parseInt(sMS[1]);
        int eM = Integer.parseInt(eMS[0]), eS = Integer.parseInt(eMS[1]);

        return ((eM * 60 + eS) - (sM * 60 + sS)) * 1000;
    }
}
