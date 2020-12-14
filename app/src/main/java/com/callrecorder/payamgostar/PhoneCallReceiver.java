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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.callrecorder.payamgostar.models.CallEntity;

import java.util.Date;

public class PhoneCallReceiver extends BroadcastReceiver {
    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    private static boolean isIncoming;
    private static String savedNumber;  //because the passed incoming is only valid in ringing


    @Override
    public void onReceive(Context context, Intent intent) {
        MultiprocessPreferences.MultiprocessSharedPreferences settings = MultiprocessPreferences.getDefaultSharedPreferences(context);
        if(!settings.getBoolean(Constants.ENABLE_CALL_RECORDING, true)){
            Logger.i(Constants.TAG, "recording is disabled.");
        }
        else {
            if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                return;
            }

            //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
            if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
                savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
            } else {
                String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
                String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                int state = 0;
                if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    state = TelephonyManager.CALL_STATE_IDLE;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    state = TelephonyManager.CALL_STATE_OFFHOOK;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    state = TelephonyManager.CALL_STATE_RINGING;
                }


                onCallStateChanged(context, state, number);
            }
        }
    }

    //Deals with actual events

    //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
    //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
    public void onCallStateChanged(Context context, int state, String number) {
        if (lastState == state) {
            //No change, debounce extras
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                callStartTime = new Date();
                savedNumber = number;
                onIncomingCallStarted(context, number, callStartTime);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false;
                    callStartTime = new Date();
                    onOutgoingCallStarted(context, savedNumber, callStartTime);
                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    //Ring but no pickup-  a miss
                    onMissedCall(context, savedNumber, callStartTime);
                } else if (isIncoming) {
                    onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                } else {
                    onOutgoingCallEnded(context, savedNumber, callStartTime, new Date());
                }
                break;
        }
        lastState = state;
    }


    //Derived classes should override these to respond to specific events of interest
    protected void onIncomingCallStarted(Context ctx, String number, Date start) {

        Logger.i(Constants.TAG, "An incoming call started from " + number + " at " + start);

        Intent recordingIntent = new Intent(ctx, RecordService.class);
        recordingIntent.putExtra("commandType", Constants.STATE_CALL_START);
        recordingIntent.putExtra("callType", Constants.INCOMING_CALL);
        recordingIntent.putExtra("phoneNumber", number);

        ctx.startService(recordingIntent);
    }

    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        Logger.i(Constants.TAG, "An outgoing call started to " + number + " at " + start);

        Intent recordingIntent = new Intent(ctx, RecordService.class);
        recordingIntent.putExtra("commandType", Constants.STATE_CALL_START);
        recordingIntent.putExtra("callType", Constants.OUTGOING_CALL);
        recordingIntent.putExtra("phoneNumber", number);
        recordingIntent.putExtra("startDate", start);
        ctx.startService(recordingIntent);
    }

    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        Logger.i(Constants.TAG, "The incoming call ended from " + number + " at " + end);

        Intent recordingIntent = new Intent(ctx, RecordService.class);
        recordingIntent.putExtra("commandType", Constants.STATE_CALL_END);
        recordingIntent.putExtra("callType", Constants.INCOMING_CALL);
        recordingIntent.putExtra("phoneNumber", number);
        recordingIntent.putExtra("startDate", start);

        ctx.startService(recordingIntent);
    }

    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        Logger.i(Constants.TAG, "The outgoing call ended to " + number + " at " + end);

        Intent recordingIntent = new Intent(ctx, RecordService.class);
        recordingIntent.putExtra("commandType", Constants.STATE_CALL_END);
        recordingIntent.putExtra("callType", Constants.OUTGOING_CALL);
        recordingIntent.putExtra("phoneNumber", number);
        recordingIntent.putExtra("startDate", start);
        recordingIntent.putExtra("endDate", end);

        ctx.startService(recordingIntent);
    }

    protected void onMissedCall(Context ctx, String number, Date start) {
        Logger.i(Constants.TAG, "A miss call received from " + number + " at " + start);
        Toast.makeText(ctx, "Miss call received from " + number, Toast.LENGTH_LONG).show();

        Intent recordingIntent = new Intent(ctx, RecordService.class);
        recordingIntent.putExtra("commandType", Constants.STATE_CALL_END);
        recordingIntent.putExtra("callType", Constants.MISS_CALL);
        recordingIntent.putExtra("phoneNumber", number);
        recordingIntent.putExtra("endDate", start);
        ctx.startService(recordingIntent);

        try {

            CallEntity.deleteAll(CallEntity.class, "ready = ?", "0");

            CallEntity callEntity = new CallEntity();
            callEntity.startDate = start;
            callEntity.endDate = start;
            callEntity.callType = Constants.MISS_CALL;
            callEntity.isSynced = false;
            callEntity.ready = true;
            callEntity.mediaFileName = "";
            callEntity.phoneNumber = number;
            callEntity.save();
        } catch (Exception ex) {
            Logger.e(Constants.TAG, "Failed to register miss call in database. See exception details.");
            ex.printStackTrace();
        }
    }
}