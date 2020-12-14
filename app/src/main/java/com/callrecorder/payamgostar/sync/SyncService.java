package com.callrecorder.payamgostar.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Jalal on 11/10/2017.
 */

public class SyncService extends Service {

    private static CallSyncAdapter sSyncAdapter = null;
    private static final Object sSyncAdapterLock = new Object();

    @Override
    public void onCreate() {
        super.onCreate();

        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new CallSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
