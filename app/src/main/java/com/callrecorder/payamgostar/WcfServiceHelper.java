package com.callrecorder.payamgostar;

import android.content.Context;
import android.os.Build;

import com.callrecorder.payamgostar.wcf.BasicHttpBinding_ICall;
import com.callrecorder.payamgostar.wcf.Enums;

import org.kobjects.base64.Base64;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

/**
 * Created by Jalal on 11/5/2017.
 */

public class WcfServiceHelper {

    private final Context mContext;

    public WcfServiceHelper(Context context) {
        mContext = context;
    }

    private byte[] getFileBytes(String path) {
        File file = new File(path);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Logger.e(Constants.TAG, e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Logger.e(Constants.TAG, e.toString());
        }
        return bytes;
    }

    private String toBase64String(byte[] data) throws UnsupportedEncodingException {
        return Base64.encode(data);
    }

    public long syncCall(String filename, Date callStartDate,
                         Date callEndDate, int callTypeCode, String phoneNumber)
            throws UnsupportedEncodingException {
        Context context = mContext.getApplicationContext();
        MultiprocessPreferences.MultiprocessSharedPreferences settings = MultiprocessPreferences.getDefaultSharedPreferences(context);

        String tsKey = settings.getString(Constants.TELEPHONY_KEY, "");
        String lineNumber = settings.getString(Constants.LINE_NUMBER, "");
        String serverAddress = settings.getString(Constants.SERVER_ADDRESS, "");
        String telExtension = settings.getString(Constants.TELEPHONY_EXTENSION, "");

        Enums.PhoneCallType callType = Enums.PhoneCallType.fromCode(callTypeCode);

        byte[] data = getFileBytes(filename);
        String mediaFile = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mediaFile = toBase64String(data);
        }

        BasicHttpBinding_ICall binding = new BasicHttpBinding_ICall(serverAddress);
        long result = -1;
        try {
            result = binding.RegisterFullCall(callType, phoneNumber, lineNumber, telExtension, callStartDate, callEndDate, tsKey, mediaFile);
            if (result > 0) {
                Logger.i(Constants.TAG, "OMG! Phone call saved successfully! Can you believe that?!");
            } else {
                Logger.i(Constants.TAG, "Failed to save phone call.");
            }
        } catch (Exception e) {
            Logger.e(Constants.TAG, "Exception thrown while trying to send data.");
            e.printStackTrace();
        }

        return result;
    }
}
