package com.callrecorder.payamgostar.security;

import android.content.Context;
import com.callrecorder.payamgostar.Constants;
import com.callrecorder.payamgostar.MultiprocessPreferences;

/**
 * Created by j.amini on 11/12/2017.
 */

public class SessionHelper {

    private final MultiprocessPreferences.MultiprocessSharedPreferences settings;

    public SessionHelper(Context context) {
        settings = MultiprocessPreferences.getDefaultSharedPreferences(context);
    }

    public boolean isLoggedIn() {
        return settings.getBoolean(Constants.IS_AUTHENTICATED, false);
    }

    public boolean login(String username, String password) {

        String dbUsername = settings.getString(Constants.USERNAME, "admin");
        String dbPassword = settings.getString(Constants.PASSWORD, "admin");

        if (username.equalsIgnoreCase(dbUsername) && password.equalsIgnoreCase(dbPassword)) {
            settings.edit().putBoolean(Constants.IS_AUTHENTICATED, true).commit();
            return true;
        }

        settings.edit().putBoolean(Constants.IS_AUTHENTICATED, false).commit();
        return false;
    }

    public void register(String username, String password) {
        MultiprocessPreferences.Editor editor = settings.edit();
        editor.putString(Constants.PASSWORD, password);
        editor.putString(Constants.USERNAME, username);
        editor.putBoolean(Constants.IS_AUTHENTICATED, true);
        editor.commit();
    }

    public void signOut() {
        MultiprocessPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.IS_AUTHENTICATED, false);
        editor.commit();
    }
}
