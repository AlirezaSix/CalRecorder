/*
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

public class Constants {

    // The authority for the sync adapter's content provider
    public static final String AUTHORITY = "com.callrecorder.payamgostar.datasync.provider";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "com.payamgostar";
    // The account name
    public static final String ACCOUNT = "syncaccount";


    public static final String TAG = "Call recorder: ";

	public static final String FILE_DIRECTORY = "recordedCalls";
	//public static final String LISTEN_ENABLED = "ListenEnabled";
	public static final String FILE_NAME_PATTERN = "^d[\\d]{14}p[_\\d]*\\.mp3$";

	public static final int MEDIA_MOUNTED = 0;
	public static final int MEDIA_MOUNTED_READ_ONLY = 1;
	public static final int NO_MEDIA = 2;

	public static final int STATE_CALL_START = 2;
	public static final int STATE_CALL_END = 3;

    public static final int OUTGOING_CALL = 1;
	public static final int INCOMING_CALL = 0;
	public static final int MISS_CALL = 2;

	public static final String LINE_NUMBER = "LineNumber";
	public static final String SERVER_ADDRESS = "ServerAddress";
	public static final String TELEPHONY_EXTENSION = "TelephonyExtension";
    public static final String TELEPHONY_KEY = "TelephonySystemKey";
	//public static final String APP_SETTINGS = "AppSettings";
    public static final String USERNAME = "AppUsername";
	public static final String PASSWORD = "AppPassword";
	public static final String IS_AUTHENTICATED = "IsAuthenticated";
	public static final String ENABLE_CALL_RECORDING = "EnableCallRecording";
	public static final String RECORDING_MEDIA_SOURCE = "RecordingMediaSource";
	public static final int RECORDING_MEDIA_SOURCE_AutoDetect = -11;
	public static final String DELETE_MEDIA_FILES_AFTER_SYNC = "DeleteMediaFilesAfterSync";
	public static long SYNC_INTERVAL = 60 /* 30 /* in seconds */;
}
