/*
 *  Copyright 2012 Kobi Krasnoff
 * 
 * This file is part of Call recorder For Android.

    Call recorder For Android is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Call recorder For Android is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Call recorder For Android.  If not, see <http://www.gnu.org/licenses/>
 */
package com.callrecorder.payamgostar;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.callrecorder.payamgostar.models.CallEntity;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.io.IOException;
import java.util.Date;

public class RecordService extends Service {

    private MediaRecorder recorder = null;
    private String phoneNumber = null;

    private String fileName;
    private boolean recording = false;
    private boolean onForeground = false;
    private int callType = 0;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        phoneNumber = intent.getStringExtra("phoneNumber");
        int commandType = intent.getIntExtra("commandType", 0);
        callType = intent.getIntExtra("callType", 0);

        if (commandType == Constants.STATE_CALL_START) {
            recording = true;
            startService();
            startRecording(intent);
        } else if (commandType == Constants.STATE_CALL_END) {
            stopAndReleaseRecorder();
            recording = false;
            stopService();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * in case it is impossible to record
     */
    private void terminateAndEraseFile() {
        Logger.d(Constants.TAG, "RecordService terminateAndEraseFile");
        stopAndReleaseRecorder();
        recording = false;
        deleteFile();
    }

    private void stopService() {
        Logger.d(Constants.TAG, "RecordService stopService");
        stopForeground(true);
        onForeground = false;
        this.stopSelf();
    }

    private void deleteFile() {
        Logger.d(Constants.TAG, "RecordService deleteFile");
        FileHelper.deleteFile(fileName);
        fileName = null;
    }

    private void stopAndReleaseRecorder() {
        if (recorder == null) {
            recording = false;
            return;
        }

        Logger.d(Constants.TAG, "RecordService stopAndReleaseRecorder");
        boolean recorderStopped = false;
        boolean exception = false;

        try {
            recorder.stop();
            recorderStopped = true;
        } catch (IllegalStateException e) {
            Logger.e(Constants.TAG, "IllegalStateException");
            Logger.printStackTrace(e);
            exception = true;
        } catch (RuntimeException e) {
            Logger.e(Constants.TAG, "RuntimeException");
            Logger.printStackTrace(e);
            exception = true;
        } catch (Exception e) {
            Logger.e(Constants.TAG, "Exception");
            Logger.printStackTrace(e);
            exception = true;
        }
        try {
            recorder.reset();
        } catch (Exception e) {
            Logger.e(Constants.TAG, "Exception");
            Logger.printStackTrace(e);
            exception = true;
        }
        try {
            recorder.release();
        } catch (Exception e) {
            Logger.e(Constants.TAG, "Exception");
            Logger.printStackTrace(e);
            exception = true;
        }

        recorder = null;
        if (exception) {
            deleteFile();
        } else if (callType != Constants.MISS_CALL) {
            try {
                CallEntity callEntity = Select.from(CallEntity.class)
                        .where(Condition.prop("media_file_name").eq(fileName))
                        .first();

                if (callEntity != null) {
                    callEntity.endDate = new Date();
                    callEntity.ready = true;
                    callEntity.save();

                    Logger.i(Constants.TAG, "CallEntity.endDate saved successfully.");
                } else {
                    Logger.e(Constants.TAG, "Can not find CallEntity with given mediaFilename.");
                }
            } catch (Exception ex) {
                Logger.e(Constants.TAG, "Exception thrown while trying to fined call with file name " + fileName);
                Logger.printStackTrace(ex);
            }
        }
        if (recorderStopped) {
            Toast toast = Toast.makeText(this,
                    this.getString(R.string.receiver_end_call),
                    Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public void onDestroy() {
        Logger.d(Constants.TAG, "RecordService onDestroy");
        stopAndReleaseRecorder();
        stopService();
        super.onDestroy();
    }

    private static String getAudioSourceName(int audioSource){
        switch (audioSource){
            case MediaRecorder.AudioSource.VOICE_CALL:
                return "voice call";

            case MediaRecorder.AudioSource.VOICE_DOWNLINK:
                return "voice downlink";

            case MediaRecorder.AudioSource.VOICE_UPLINK:
                return "voice uplink";

            case MediaRecorder.AudioSource.VOICE_COMMUNICATION:
                return "voice communication";

            case MediaRecorder.AudioSource.MIC:
                return "microphone";

            default:
            case MediaRecorder.AudioSource.DEFAULT:
                return "default";
        }
    }

    private void startRecording(Intent intent) {
        Logger.d(Constants.TAG, "RecordService startRecording");
        boolean exception = false;
        recorder = new MediaRecorder();

        try {

            Context context = getBaseContext();
            MultiprocessPreferences.MultiprocessSharedPreferences settings = MultiprocessPreferences.getDefaultSharedPreferences(context);
            int mediaRecordingSource = settings.getInt(Constants.RECORDING_MEDIA_SOURCE, MediaRecorder.AudioSource.VOICE_CALL);
            recorder.setAudioSource(mediaRecordingSource);
            Logger.i(Constants.TAG, "MediaRecorder.AudioSource set to " + getAudioSourceName(mediaRecordingSource));

            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            fileName = FileHelper.getFilename(phoneNumber);
            recorder.setOutputFile(fileName);

            OnErrorListener errorListener = new OnErrorListener() {
                public void onError(MediaRecorder arg0, int arg1, int arg2) {
                    Logger.e(Constants.TAG, "OnErrorListener " + arg1 + "," + arg2);
                    terminateAndEraseFile();
                }
            };
            recorder.setOnErrorListener(errorListener);

            OnInfoListener infoListener = new OnInfoListener() {
                public void onInfo(MediaRecorder arg0, int arg1, int arg2) {
                    Logger.e(Constants.TAG, "OnInfoListener " + arg1 + "," + arg2);
                    terminateAndEraseFile();
                }
            };
            recorder.setOnInfoListener(infoListener);

            recorder.prepare();
            // Sometimes prepare takes some time to complete
            Thread.sleep(2000);
            recorder.start();
            recording = true;
            Logger.d(Constants.TAG, "RecordService recorderStarted");
        } catch (IllegalStateException e) {
            Logger.e(Constants.TAG, "IllegalStateException");
            e.printStackTrace();
            exception = true;
        } catch (IOException e) {
            Logger.e(Constants.TAG, "IOException");
            e.printStackTrace();
            exception = true;
        } catch (Exception e) {
            Logger.e(Constants.TAG, "Exception");
            e.printStackTrace();
            exception = true;
        }

        if (exception) {
            terminateAndEraseFile();
        }

        if (recording) {
            Toast toast = Toast.makeText(this,
                    this.getString(R.string.receiver_start_call),
                    Toast.LENGTH_SHORT);
            toast.show();
            try {

                Logger.i(Constants.TAG, "Inserting CallEntity...");

                CallEntity callEntity = new CallEntity();
                Date now = new Date();
                callEntity.startDate = now;
                callEntity.endDate = now;
                callEntity.callType = callType;
                callEntity.isSynced = false;
                callEntity.ready = false;
                callEntity.mediaFileName = fileName;
                callEntity.phoneNumber = phoneNumber;
                callEntity.save();

            } catch (Exception ex) {
                Logger.e(Constants.TAG, "Failed to save CallEntity.");
                ex.printStackTrace();
            }
        } else {
            Toast toast = Toast.makeText(this,
                    this.getString(R.string.record_impossible),
                    Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void startService() {
        if (!onForeground) {
            Logger.d(Constants.TAG, "RecordService startService");
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    getBaseContext(), 0, intent, 0);

            Notification notification = new NotificationCompat.Builder(
                    getBaseContext())
                    .setContentTitle(
                            this.getString(R.string.notification_title))
                    .setTicker(this.getString(R.string.notification_ticker))
                    .setContentText(this.getString(R.string.notification_text))
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(pendingIntent).setOngoing(true)
                    .getNotification();

            notification.flags = Notification.FLAG_NO_CLEAR;

            startForeground(1337, notification);
            onForeground = true;
        }
    }
}
