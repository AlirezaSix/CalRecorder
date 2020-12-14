package com.callrecorder.payamgostar.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.callrecorder.payamgostar.Constants;
import com.callrecorder.payamgostar.FileHelper;
import com.callrecorder.payamgostar.Logger;
import com.callrecorder.payamgostar.MultiprocessPreferences;
import com.callrecorder.payamgostar.WcfServiceHelper;
import com.callrecorder.payamgostar.models.CallEntity;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by Jalal on 11/10/2017.
 */

public class CallSyncAdapter extends AbstractThreadedSyncAdapter {
    private final ContentResolver mContentResolver;

    public CallSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        mContentResolver = context.getContentResolver();
    }

    public CallSyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        /*
         * If your app uses a content resolver, get an instance of it
         * from the incoming Context
         */
        mContentResolver = context.getContentResolver();
    }

    public void onPerformSync(Account account, Bundle bundle, String authority,
                              ContentProviderClient contentProviderClient, SyncResult syncResult) {

        MultiprocessPreferences.MultiprocessSharedPreferences settings = MultiprocessPreferences.getDefaultSharedPreferences(getContext());

        String serverAddress = settings.getString(Constants.SERVER_ADDRESS, "");
        String lineNumber = settings.getString(Constants.LINE_NUMBER, "");
        String extension = settings.getString(Constants.TELEPHONY_EXTENSION, "");

        if (serverAddress == null || serverAddress.trim() == "" ||
                lineNumber == null || lineNumber.trim() == "" ||
                extension == null || extension == "") {
            Logger.e(Constants.TAG, "Settings is not valid. Please configure settings.");
        } else {
            try {
                Logger.i(Constants.TAG, "Sync queue started.");
                internalSync();
                Logger.i(Constants.TAG, "Sync queue completed.");
            } catch (UnsupportedEncodingException e) {
                Logger.e(Constants.TAG, "Failed to decode media file.");
                Logger.printStackTrace(e);
            } catch (Exception ex) {
                Logger.e(Constants.TAG, "Error while trying to sync data...");
                Logger.printStackTrace(ex);
            }
        }
    }

    private void internalSync() throws UnsupportedEncodingException {
        WcfServiceHelper helper = new WcfServiceHelper(getContext());
        MultiprocessPreferences.MultiprocessSharedPreferences settings = MultiprocessPreferences.getDefaultSharedPreferences(getContext());

        List<CallEntity> pendingCalls = CallEntity.find(CallEntity.class, "is_synced = ? AND [ready] = 1", "0");
        if(pendingCalls.size() == 0){
            Logger.i(Constants.TAG, "No call to sync.");
        }
        for (CallEntity call : pendingCalls) {
            Logger.i(Constants.TAG, "Sending call " + call.phoneNumber + " with id " + call.id);

            call.serverId = helper.syncCall(call.mediaFileName, call.startDate, call.endDate,
                    call.callType, call.phoneNumber);
            if (call.serverId >= 0) {
                call.isSynced = true;
                call.save();

                Logger.i(Constants.TAG, "Sending call id " + call.id + " completed.");

                if (settings.getBoolean(Constants.DELETE_MEDIA_FILES_AFTER_SYNC, true)) {
                    FileHelper.deleteFile(call.mediaFileName);
                }
            } else {
                Logger.i(Constants.TAG, "Failed to sync call id " + call.id);
            }
        }
    }
}
