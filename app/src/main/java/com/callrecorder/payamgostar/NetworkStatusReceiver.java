package com.callrecorder.payamgostar;

import android.accounts.Account;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.callrecorder.payamgostar.security.AccountHelper;

/**
 * Created by j.amini on 11/11/2017.
 */

public class NetworkStatusReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        try {
            if(isNetworkAvailable(context, intent)) {
                Logger.i(Constants.TAG, "Running sync manually.");
                final Account syncAccount = AccountHelper.CreateSyncAccount(context);
                ContentResolver.requestSync(syncAccount, Constants.AUTHORITY, Bundle.EMPTY);
            }
            else {
                Logger.i(Constants.TAG, "Connection is not available.");
            }
        } catch (Exception ex) {
            Logger.e(Constants.TAG, "Exception thrown while trying to run sync on network availability change.");
        }
    }

    private boolean isNetworkAvailable(Context context, Intent intent) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }
}