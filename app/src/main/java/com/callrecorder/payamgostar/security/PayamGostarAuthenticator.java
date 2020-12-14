package com.callrecorder.payamgostar.security;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;

/**
 * Created by Jalal on 11/10/2017.
 * Since PayamGostar's Call API doesn't require any authentication, there is no implementation for this class.
 */

public class PayamGostarAuthenticator extends AbstractAccountAuthenticator {

    public PayamGostarAuthenticator(Context context) {
        super(context);
    }

    public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
        return null;
    }

    public Bundle addAccount(AccountAuthenticatorResponse accountAuthenticatorResponse, String s, String s1, String[] strings, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    public String getAuthTokenLabel(String s) {
        return null;
    }

    public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String[] strings) throws NetworkErrorException {
        return null;
    }
}
