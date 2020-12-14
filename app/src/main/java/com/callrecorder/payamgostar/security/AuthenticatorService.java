package com.callrecorder.payamgostar.security;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Jalal on 11/10/2017.
 */

public class AuthenticatorService extends Service {
    private PayamGostarAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        super.onCreate();
        mAuthenticator = new PayamGostarAuthenticator(this);
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
