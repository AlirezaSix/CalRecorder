package com.callrecorder.payamgostar.security;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.callrecorder.payamgostar.Constants;
import com.callrecorder.payamgostar.Logger;

/**
 * Created by j.amini on 11/11/2017.
 */

public class AccountHelper {

    public static Account CreateSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(
                Constants.ACCOUNT, Constants.ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        Context.ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            Logger.i(Constants.TAG, "Account for PayamGostar call sync created successfully.");
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
            Logger.i(Constants.TAG, "Account for PayamGostar call sync already exists.");
        }

        return newAccount;
    }
}
