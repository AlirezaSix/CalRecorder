package com.callrecorder.payamgostar;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.callrecorder.payamgostar.models.CallEntity;

import java.util.regex.Pattern;

public class SettingsActivity extends Activity implements View.OnClickListener {

    Button btnSaveSettings = null;
    Button btnCleanAll = null;
    EditText txtServerAddress = null;
    EditText txtLineNumber = null;
    EditText txtTelephonyKey = null;
    EditText txtExtension = null;
    EditText txtUsername = null;
    EditText txtPassword = null;
    CheckBox chkEnableRecording = null;
    CheckBox chkDeleteMediaAfterSync = null;

    RadioGroup grpRecordingMediaSource = null;

    RadioButton rbRecordingSourceAuto = null;
    RadioButton rbRecordingSourceDefault = null;
    RadioButton rbRecordingSourceVoiceCall = null;
    RadioButton rbRecordingSourceUpLink = null;
    RadioButton rbRecordingSourceDownLink = null;
    RadioButton rbRecordingSourceMic = null;
    RadioButton rbRecordingSourceCommunication = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Context context = getApplicationContext();
        MultiprocessPreferences.MultiprocessSharedPreferences settings = MultiprocessPreferences.getDefaultSharedPreferences(context);

        btnSaveSettings = (Button) findViewById(R.id.btnSaveSettings);
        btnSaveSettings.setOnClickListener(this);

        btnCleanAll = (Button) findViewById(R.id.btnCleanAll);
        btnCleanAll.setOnClickListener(this);

        txtServerAddress = (EditText) findViewById(R.id.txtServerAddress);
        txtServerAddress.setText(settings.getString(Constants.SERVER_ADDRESS, ""));

        txtLineNumber = (EditText) findViewById(R.id.txtPhoneNumber);
        txtLineNumber.setText(settings.getString(Constants.LINE_NUMBER, ""));

        txtTelephonyKey = (EditText) findViewById(R.id.txtTelephonyKey);
        txtTelephonyKey.setText(settings.getString(Constants.TELEPHONY_KEY, ""));

        txtExtension = (EditText) findViewById(R.id.txtExtension);
        txtExtension.setText(settings.getString(Constants.TELEPHONY_EXTENSION, ""));


        txtUsername = (EditText) findViewById(R.id.txtUsername);
        txtUsername.setText(settings.getString(Constants.USERNAME, ""));

        txtPassword = (EditText) findViewById(R.id.txtPassword);
        txtPassword.setText(settings.getString(Constants.PASSWORD, ""));

        chkEnableRecording = (CheckBox) findViewById(R.id.chkEnableRecording);
        chkEnableRecording.setChecked(settings.getBoolean(Constants.ENABLE_CALL_RECORDING, true));

        chkDeleteMediaAfterSync = (CheckBox) findViewById(R.id.chkDeleteMediaAfterSync);
        chkDeleteMediaAfterSync.setChecked(settings.getBoolean(Constants.DELETE_MEDIA_FILES_AFTER_SYNC, true));

        grpRecordingMediaSource = (RadioGroup) findViewById(R.id.grpRecordingSource);
        rbRecordingSourceAuto = (RadioButton) findViewById(R.id.rbRecordingSourceAuto);
        rbRecordingSourceDefault = (RadioButton) findViewById(R.id.rbRecordingSourceDefault);
        rbRecordingSourceVoiceCall = (RadioButton) findViewById(R.id.rbRecordingSourceVoiceCall);
        rbRecordingSourceUpLink = (RadioButton) findViewById(R.id.rbRecordingSourceUpLink);
        rbRecordingSourceDownLink = (RadioButton) findViewById(R.id.rbRecordingSourceDownLink);
        rbRecordingSourceMic = (RadioButton) findViewById(R.id.rbRecordingSourceMic);
        rbRecordingSourceCommunication = (RadioButton) findViewById(R.id.rbRecordingSourceCommunication);

        switch (settings.getInt(Constants.RECORDING_MEDIA_SOURCE, Constants.RECORDING_MEDIA_SOURCE_AutoDetect)) {
            case Constants.RECORDING_MEDIA_SOURCE_AutoDetect:
                rbRecordingSourceAuto.setChecked(true);
                break;
            case MediaRecorder.AudioSource.DEFAULT:
                rbRecordingSourceDefault.setChecked(true);
                break;
            case MediaRecorder.AudioSource.VOICE_CALL:
                rbRecordingSourceVoiceCall.setChecked(true);
                break;
            case MediaRecorder.AudioSource.VOICE_UPLINK:
                rbRecordingSourceUpLink.setChecked(true);
                break;
            case MediaRecorder.AudioSource.VOICE_DOWNLINK:
                rbRecordingSourceDownLink.setChecked(true);
                break;
            case MediaRecorder.AudioSource.MIC:
                rbRecordingSourceMic.setChecked(true);
                break;
            case MediaRecorder.AudioSource.VOICE_COMMUNICATION:
                rbRecordingSourceCommunication.setChecked(true);
                break;
        }
    }

    static public boolean isValidUrl(String serverUrl) {
        return Patterns.WEB_URL.matcher(serverUrl).matches();
//        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo netInfo = cm.getActiveNetworkInfo();
//        if (netInfo != null && netInfo.isConnected()) {
//            try {
//                URL url = new URL(serverUrl);   // Change to "http://google.com" for www  test.
//                return true;
//            } catch (MalformedURLException e1) {
//                Logger.e(Constants.TAG, "Connection to " + serverUrl + " failed with exception.");
//                Logger.printStackTrace(e1);
//                return false;
//            }
//        }
//        return false;
    }

    private void cleanupAll() {
        try {
            CallEntity.deleteAll(CallEntity.class);
            FileHelper.deleteAllRecords(SettingsActivity.this);
            Logger.clearLogs();

            Toast.makeText(getApplicationContext(),
                    R.string.msg_all_records_cleaned, Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            Logger.e(Constants.TAG, getString(R.string.err_error_clean_all_records));
            Logger.printStackTrace(ex);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCleanAll:
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_menu_delete)
                        .setTitle(getString(R.string.msg_clean_title))
                        .setMessage(getString(R.string.msg_clean_message))
                        .setPositiveButton(getString(R.string.msg_clean_btn_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                cleanupAll();
                            }

                        })
                        .setNegativeButton(getString(R.string.msg_clean_btn_cancel), null)
                        .show();
                break;
            case R.id.btnSaveSettings:
                Context context = getApplicationContext();

                MultiprocessPreferences.MultiprocessSharedPreferences settings = MultiprocessPreferences.getDefaultSharedPreferences(context);
                MultiprocessPreferences.Editor settingEditor = settings.edit();

                String serverAddress = txtServerAddress.getText().toString();
                if (isValidUrl(serverAddress)) {
                    settingEditor.putString(Constants.SERVER_ADDRESS, serverAddress);
                } else {
                    Toast.makeText(context, R.string.server_address_is_not_valid, Toast.LENGTH_SHORT).show();
                    return;
                }

                String lineNumber = txtLineNumber.getText().toString();
                if (Pattern.matches("\\d{3,11}", lineNumber)) {
                    settingEditor.putString(Constants.LINE_NUMBER, lineNumber);
                } else {
                    Toast.makeText(context, R.string.enter_valid_phone_number, Toast.LENGTH_SHORT).show();
                    return;
                }

                String extNumber = txtExtension.getText().toString();
                if (Pattern.matches("\\d{1,11}", extNumber)) {
                    settingEditor.putString(Constants.TELEPHONY_EXTENSION, extNumber);
                } else {
                    Toast.makeText(context, R.string.enter_valid_extension_number, Toast.LENGTH_SHORT).show();
                    return;
                }

                String username = txtUsername.getText().toString();
                if (Pattern.matches("^[a-z0-9_-]{3,15}$", username)) {
                    settingEditor.putString(Constants.USERNAME, username);
                } else {
                    Toast.makeText(context, R.string.err_username_not_valid, Toast.LENGTH_SHORT).show();
                    return;
                }


                String password = txtPassword.getText().toString();
                if (password.length() < 3) {
                    Toast.makeText(context, R.string.error_password_length, Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    settingEditor.putString(Constants.PASSWORD, password);
                }

                String telephonyKey = txtTelephonyKey.getText().toString();
                settingEditor.putString(Constants.TELEPHONY_KEY, telephonyKey);

                settingEditor.putBoolean(Constants.ENABLE_CALL_RECORDING, chkEnableRecording.isChecked());
                settingEditor.putBoolean(Constants.DELETE_MEDIA_FILES_AFTER_SYNC, chkDeleteMediaAfterSync.isChecked());

                settingEditor.putInt(Constants.RECORDING_MEDIA_SOURCE, getSelectedRecordingMediaSource());
                settingEditor.commit();

                Intent mainIntent = new Intent(context, MainActivity.class);
                startActivity(mainIntent);

                break;
        }
    }

    private int getSelectedRecordingMediaSource() {
        RadioButton selectedRadio = (RadioButton) findViewById(grpRecordingMediaSource.getCheckedRadioButtonId());
        int mediaSource;

        switch (selectedRadio.getId()) {
            case R.id.rbRecordingSourceAuto:
                String manufacturer = Build.MANUFACTURER;
                if (manufacturer.toLowerCase().contains("samsung") ||
                        manufacturer.toLowerCase().contains("htc")) {
                    mediaSource = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
                } else {
                    mediaSource = MediaRecorder.AudioSource.VOICE_CALL;
                }
                break;

            case R.id.rbRecordingSourceVoiceCall:
                mediaSource = MediaRecorder.AudioSource.VOICE_CALL;
                break;

            case R.id.rbRecordingSourceUpLink:
                mediaSource = MediaRecorder.AudioSource.VOICE_UPLINK;
                break;

            case R.id.rbRecordingSourceDownLink:
                mediaSource = MediaRecorder.AudioSource.VOICE_DOWNLINK;
                break;

            case R.id.rbRecordingSourceMic:
                mediaSource = MediaRecorder.AudioSource.MIC;
                break;

            case R.id.rbRecordingSourceCommunication:
                mediaSource = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
                break;

            default:
            case R.id.rbRecordingSourceDefault:
                mediaSource = MediaRecorder.AudioSource.DEFAULT;
                break;
        }

        return mediaSource;
    }
}
