package com.example.timerexam.task;

import android.os.AsyncTask;
import android.util.Log;

import org.ffmpeg.android.FfmpegController;
import org.ffmpeg.android.ShellUtils;

import java.io.File;

/**
 * Created by admin on 3/24/17.
 */

public class ConvertWmaToMp3Task extends AsyncTask<Void, Void, Boolean> {
    String TAG = ConvertWmaToMp3Task.class.getSimpleName();
    FfmpegController ffmpegController;
    String inputAudioFile;
    String outputAudioFile;
    ConvertWmaToMp3Task.EventListener eventListener;

    public ConvertWmaToMp3Task(ConvertWmaToMp3TaskBuilder convertToFlacTaskBuilder) {
        this.ffmpegController = convertToFlacTaskBuilder.ffmpegController;
        this.inputAudioFile = convertToFlacTaskBuilder.inputAudioFile;
        this.outputAudioFile = convertToFlacTaskBuilder.outputAudioFile;
        this.eventListener = convertToFlacTaskBuilder.listener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            ffmpegController.wma2mp3(
                    inputAudioFile,
                    outputAudioFile,
                    new ShellUtils.ShellCallback() {
                        @Override
                        public void shellOut(String shellLine) {
                            Log.i(TAG, shellLine);
                        }

                        @Override
                        public void processComplete(int exitValue) {
                            if (exitValue != 0) {
                                Log.e(TAG, "wma file created" + outputAudioFile);
                            }
                        }
                    });
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (eventListener != null) {
            eventListener.onPostExecute(success);
        }
    }

    public static class ConvertWmaToMp3TaskBuilder {
        private final FfmpegController ffmpegController;
        private final String inputAudioFile;
        private final String outputAudioFile;
        private ConvertWmaToMp3Task.EventListener listener;


        public ConvertWmaToMp3TaskBuilder(FfmpegController ffmpegController, String inputAudioFile, String outputAudioFile) {
            this.ffmpegController = ffmpegController;
            this.inputAudioFile = inputAudioFile;
            this.outputAudioFile = outputAudioFile;
        }

        public ConvertWmaToMp3TaskBuilder listener(ConvertWmaToMp3Task.EventListener listener) {
            this.listener = listener;
            return this;
        }

        public ConvertWmaToMp3Task build() {
            return new ConvertWmaToMp3Task(this);
        }
    }

    public interface EventListener {
        void onPostExecute(boolean success);
    }
}
