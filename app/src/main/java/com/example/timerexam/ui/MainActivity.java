package com.example.timerexam.ui;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.timerexam.R;
import com.example.timerexam.data.TimerParameters;
import com.example.timerexam.service.ReminderService;
import com.example.timerexam.task.ConvertWmaToMp3Task;
import com.example.timerexam.util.MarshmallowPermissionUtil;
import com.example.timerexam.util.UriUtil;

import org.ffmpeg.android.FfmpegController;

import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getSimpleName();
    private int PICK_AUDIO_REQUEST_CODE = 1001;

    //region UI
    private RadioButton mRadioRepeatNo;
    private RadioButton mRadioRepeatDaily;
    private RadioButton mRadioRepeatWeekly;
    private RadioButton mRadioRepeatMonthly;
    private TextView mTxtStartDate;
    private TextView mTxtStartTime;
    private TextView mTxtEndTime;
    private EditText mInputPlayInterval;
    private EditText mInputPauseInterval;
    private TextView mTxtTonePath;
    private CheckBox mCheckVibrate;
    private Button mBtnSave;
    private Button mBtnStop;

    private void bindUI() {
        //Init controls
        mRadioRepeatNo = (RadioButton) findViewById(R.id.radioRepeatNo);
        mRadioRepeatDaily = (RadioButton) findViewById(R.id.radioRepeatDaily);
        mRadioRepeatWeekly = (RadioButton) findViewById(R.id.radioRepeatWeekly);
        mRadioRepeatMonthly = (RadioButton) findViewById(R.id.radioRepeatMonthly);

        mTxtStartDate = (TextView) findViewById(R.id.txtStartDate);
        mTxtStartTime = (TextView) findViewById(R.id.txtStartTime);
        mTxtEndTime = (TextView) findViewById(R.id.txtEndTime);

        mInputPlayInterval = (EditText) findViewById(R.id.inputPlayInterval);
        mInputPauseInterval = (EditText) findViewById(R.id.inputPauseInterval);

        mTxtTonePath = (TextView) findViewById(R.id.txtTonePath);
        mCheckVibrate = (CheckBox) findViewById(R.id.checkVibrate);

        mBtnSave = (Button) findViewById(R.id.btnSave);
        mBtnStop = (Button) findViewById(R.id.btnStop);

        //Init listeners
        mRadioRepeatNo.setOnCheckedChangeListener(repeatModeCheckListener);
        mRadioRepeatDaily.setOnCheckedChangeListener(repeatModeCheckListener);
        mRadioRepeatWeekly.setOnCheckedChangeListener(repeatModeCheckListener);
        mRadioRepeatMonthly.setOnCheckedChangeListener(repeatModeCheckListener);

        mTxtStartDate.setOnClickListener(dateClickListener);
        mTxtStartTime.setOnClickListener(timeClickListener);
        mTxtEndTime.setOnClickListener(timeClickListener);

        mInputPlayInterval.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String strValue = s.toString();
                int value = strValue.isEmpty() ? 0 : Integer.parseInt(strValue);
                value = value > 120 ? 120 : value;

                String strNewValue = String.valueOf(value);
                if (!strValue.equals(strNewValue)) {
                    mInputPlayInterval.removeTextChangedListener(this);
                    mInputPlayInterval.setText(strNewValue);
                    mInputPlayInterval.setSelection(strNewValue.length());
                    mInputPlayInterval.addTextChangedListener(this);
                }
            }
        });

        mInputPauseInterval.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String strValue = s.toString();
                int value = strValue.isEmpty() ? 0 : Integer.parseInt(strValue);
                value = value > 120 ? 120 : value;

                String strNewValue = String.valueOf(value);
                if (!strValue.equals(strNewValue)) {
                    mInputPauseInterval.removeTextChangedListener(this);
                    mInputPauseInterval.setText(strNewValue);
                    mInputPauseInterval.setSelection(strNewValue.length());
                    mInputPauseInterval.addTextChangedListener(this);
                }
            }
        });

        mTxtTonePath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MarshmallowPermissionUtil.checkPermissionForStorage(MainActivity.this)) {
                    Intent intent = new Intent();
                    intent.setType("audio/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, PICK_AUDIO_REQUEST_CODE);
                }
            }
        });

        mCheckVibrate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mTimerParameter.vibrate = isChecked;
            }
        });

        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimerParameter.playInterval = Integer.parseInt(mInputPlayInterval.getText().toString());
                mTimerParameter.pauseInterval = Integer.parseInt(mInputPauseInterval.getText().toString());
                startNewAlert();
            }
        });

        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimerParameter.started = false;
                mTimerParameter.saveToPreference(MainActivity.this);
                mBtnStop.setEnabled(false);
                removeAlarm();
            }
        });

        //Fill content
        representRepeatMode(mTimerParameter.repeatMode);
        mTxtStartDate.setText(mTimerParameter.startDate);
        mTxtStartTime.setText(mTimerParameter.startTime);
        mTxtEndTime.setText(mTimerParameter.endTime);
        mInputPlayInterval.setText(String.valueOf(mTimerParameter.playInterval));
        mInputPauseInterval.setText(String.valueOf(mTimerParameter.pauseInterval));
        mTxtTonePath.setText(mTimerParameter.getToneTitle());
        mCheckVibrate.setChecked(mTimerParameter.vibrate);

        mCheckVibrate.setEnabled(false); //TODO: implement later
        mBtnStop.setEnabled(mTimerParameter.started);
        //Prevent default auto-focus for edit text views.
        findViewById(R.id.rootView).requestFocus();
    }

    private void representRepeatMode(TimerParameters.RepeatMode repeatMode) {
        switch (repeatMode) {
            case NONE:
                mRadioRepeatNo.setChecked(true);
                break;
            case DAILY:
                mRadioRepeatDaily.setChecked(true);
                break;
            case WEEKLY:
                mRadioRepeatWeekly.setChecked(true);
                break;
            case MONTHLY:
                mRadioRepeatMonthly.setChecked(true);
                break;
        }

        mTxtStartDate.setEnabled(repeatMode != TimerParameters.RepeatMode.DAILY);
        mTimerParameter.repeatMode = repeatMode;
    }

    CompoundButton.OnCheckedChangeListener repeatModeCheckListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                if (buttonView == mRadioRepeatNo)
                    representRepeatMode(TimerParameters.RepeatMode.NONE);
                else if (buttonView == mRadioRepeatDaily)
                    representRepeatMode(TimerParameters.RepeatMode.DAILY);
                else if (buttonView == mRadioRepeatWeekly)
                    representRepeatMode(TimerParameters.RepeatMode.WEEKLY);
                else if (buttonView == mRadioRepeatMonthly)
                    representRepeatMode(TimerParameters.RepeatMode.MONTHLY);
            }
        }
    };

    View.OnClickListener dateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v != mTxtStartDate) return;
            String[] ymd = mTimerParameter.startDate.split("-");
            int year = Integer.parseInt(ymd[0]);
            int month = Integer.parseInt(ymd[1]);
            int day = Integer.parseInt(ymd[2]);

            DatePickerDialog dlg = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    mTimerParameter.startDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    mTxtStartDate.setText(mTimerParameter.startDate);
                }
            }, year, month - 1, day);
            dlg.show();
        }
    };

    View.OnClickListener timeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            if (v != mTxtStartTime && v != mTxtEndTime) return;

            String[] hms = ((TextView) v).getText().toString().split(":");
            int hour = Integer.parseInt(hms[0]);
            int minute = Integer.parseInt(hms[1]);

            TimePickerDialog dlg = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    if (v == mTxtStartTime) {
                        mTimerParameter.startTime = String.format("%02d:%02d", hourOfDay, minute);
                        mTxtStartTime.setText(mTimerParameter.startTime);
                    } else {
                        mTimerParameter.endTime = String.format("%02d:%02d", hourOfDay, minute);
                        mTxtEndTime.setText(mTimerParameter.endTime);
                    }
                }
            }, hour, minute, true);
            dlg.show();
        }
    };
    //endregion

    private TimerParameters mTimerParameter = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTimerParameter = TimerParameters.loadFromPreference(this);
        bindUI();

        if (!MarshmallowPermissionUtil.checkPermissionForStorage(this)) {
            Log.e(TAG, "requesting permission");
            MarshmallowPermissionUtil.requestPermissionForStorage(this);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (resultCode == RESULT_OK && requestCode == PICK_AUDIO_REQUEST_CODE) {
            Uri uri = intent.getData();
            if (uri != null) {
                mTimerParameter.tonePath = UriUtil.getPath(this, uri);
                Log.e(TAG, uri.toString());
                mTxtTonePath.setText(mTimerParameter.getToneTitle());
            } else {
                mTimerParameter.tonePath = null;
            }
        }
    }

    private void showParameterError(TimerParameters.IntegrityResult testResult) {
        if (testResult == TimerParameters.IntegrityResult.FAIL_WITH_TIME_MISMATCH) {
            Toast.makeText(this, "End-time should be greater than start-time", Toast.LENGTH_LONG).show();
            Log.e(TAG, "end time = " + mTimerParameter.endTime + " start time = " + mTimerParameter.startTime);
        } else if (testResult == TimerParameters.IntegrityResult.FAIL_WITH_NO_SOUND) {
            Toast.makeText(this, "No sound nor vibrate", Toast.LENGTH_LONG).show();
        } else if (testResult == TimerParameters.IntegrityResult.FAIL_WITH_PASSED) {
            Toast.makeText(this, "Already passed, the alarm will never be triggered", Toast.LENGTH_LONG).show();
        } else if (testResult == TimerParameters.IntegrityResult.FAIL_WITH_ZERO_PLAY) {
            Toast.makeText(this, "Play time cannot be zero", Toast.LENGTH_LONG).show();
        }
    }

    private void startNewAlert() {
        TimerParameters.IntegrityResult testResult = mTimerParameter.test();
        if (testResult == TimerParameters.IntegrityResult.OK) {
            if (mTimerParameter.tonePath != null && mTimerParameter.tonePath.endsWith("wma")) {
                Log.e(TAG, "need to convert wma file to mp3 file");
                if (initializeFfmpegController()) {
                    final String wmaPath = mTimerParameter.tonePath;
                    final String mp3Path = wmaPath + ".mp3"; //TODO: Put temp file to application data directory with random-generated name.
                    showProgressDialog();
                    new ConvertWmaToMp3Task.ConvertWmaToMp3TaskBuilder(mFfmpegController, wmaPath, mp3Path)
                            .listener(new ConvertWmaToMp3Task.EventListener() {
                                @Override
                                public void onPostExecute(boolean success) {
                                    dismissProgressDialog();
                                    registerAlarm();
                                }
                            })
                            .build()
                            .execute();
                } else {
                    Toast.makeText(this, "Unable to convert wma file to mp3 file", Toast.LENGTH_LONG).show();
                }
            } else {
                registerAlarm();
            }
        } else {
            showParameterError(testResult);
        }
    }

    private void registerAlarm() {
        mTimerParameter.saveToPreference(MainActivity.this);

        //First, remove alarm already registered
        removeAlarm();

        Log.e(TAG, "registering alarm");
        Intent intent = new Intent(getApplicationContext(), ReminderService.class);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent, 0);

        if (mTimerParameter.repeatMode == TimerParameters.RepeatMode.NONE) {
            long nextTime = mTimerParameter.getNextTriggerTimeInMs() - new Date().getTime();

            if (nextTime != -1) {
                Log.e(TAG, "start one-time alarm after " + nextTime + "ms");
                alarmManager.set(AlarmManager.RTC_WAKEUP, nextTime, pendingIntent);

                mTimerParameter.started = true;
                mTimerParameter.saveToPreference(this);
                mBtnStop.setEnabled(true);
            } else {
                Log.e(TAG, "bad next time");
            }
        } else {
            long nextTime = mTimerParameter.getNextTriggerTimeInMs() - new Date().getTime();
            long interval = mTimerParameter.getIntervalInMs();

            if (nextTime != -1 && interval != -1) {
                Log.e(TAG, "start repeated alarm after " + nextTime + "ms" + " with interval " + interval + "ms");
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, nextTime, interval, pendingIntent);

                mTimerParameter.started = true;
                mTimerParameter.saveToPreference(this);
                mBtnStop.setEnabled(true);
            } else {
                Log.e(TAG, "bad next time or interval");
            }
        }
    }

    private void removeAlarm() {
        Log.e(TAG, "remove alarm");
        Intent intent = new Intent(getApplicationContext(), ReminderService.class);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        //Stop service if running
        stopService(intent);
    }


    //region FFMPEG
    FfmpegController mFfmpegController;

    public boolean initializeFfmpegController() {
        if (mFfmpegController == null) {
            try {
                mFfmpegController = new FfmpegController(this);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    ProgressDialog progressDialog;

    private void showProgressDialog() {
        dismissProgressDialog();

        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(this, null, "Processing audio...");
        }

        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    //endregion
}
