package com.callrecorder.payamgostar.models;

import com.orm.SugarRecord;

import java.util.Date;

/**
 * Created by Jalal on 11/5/2017.
 */

public class CallEntity extends SugarRecord<CallEntity> {

    public int id;
    public long serverId;
    public String phoneNumber;
    public Date startDate;
    public Date endDate;
    public String mediaFileName;
    public int callType;
    public boolean isSynced;
    public boolean ready;

    public CallEntity() {
    }
}