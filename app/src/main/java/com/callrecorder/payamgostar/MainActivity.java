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

import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.callrecorder.payamgostar.models.CallDataAdapter;
import com.callrecorder.payamgostar.models.CallEntity;
import com.callrecorder.payamgostar.security.AccountHelper;
import com.callrecorder.payamgostar.security.SessionHelper;

import java.util.List;
import java.util.Locale;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.listeners.SwipeToRefreshListener;
import de.codecrafters.tableview.listeners.TableDataClickListener;
import de.codecrafters.tableview.model.TableColumnWeightModel;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

public class MainActivity extends AppCompatActivity implements TableDataClickListener<CallEntity>, SwipeToRefreshListener {

    private static final String APP_LOCALE = "fa";
    private static final int NO_MEMORY_CARD = 2;
    private Context context;
    private TableView callsTableView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCenter.start(getApplication(), "5da69fc8-97fb-4da1-8ed3-5e17159a6fc7",
                Analytics.class, Crashes.class);


        setContentView(R.layout.activity_main);

        context = this.getBaseContext();
        LanguageHelper.setAppLocale(APP_LOCALE, this);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        final Account syncAccount = AccountHelper.CreateSyncAccount(context);

        ContentResolver mResolver = getContentResolver();

        ContentResolver.setSyncAutomatically(syncAccount, Constants.AUTHORITY, true);
        ContentResolver.addPeriodicSync(syncAccount, Constants.AUTHORITY, Bundle.EMPTY, Constants.SYNC_INTERVAL);

        MultiprocessPreferences.MultiprocessSharedPreferences settings = MultiprocessPreferences.getDefaultSharedPreferences(context);
        boolean silentMode = settings.getBoolean("silentMode", true);
        if (silentMode) {
            setSharedPreferences(false);
        }

        if (!new SessionHelper(context).isLoggedIn()) {

            Intent loginIntent = new Intent(context, LoginActivity.class);
            startActivity(loginIntent);
            finish();

        } else {
            callsTableView = (TableView)findViewById(R.id.callTableView);
            callsTableView.setSwipeToRefreshEnabled(true);
            callsTableView.addDataClickListener(this);
            callsTableView.setSwipeToRefreshListener(this);
            String[] TABLE_HEADERS = { "شماره تلفن", "نوع", "تاریخ", "وضعیت" };
            callsTableView.setHeaderAdapter(new SimpleTableHeaderAdapter(this, TABLE_HEADERS));
            TableColumnWeightModel columnModel = new TableColumnWeightModel(4);
            columnModel.setColumnWeight(0, 3);
            columnModel.setColumnWeight(1, 2);
            columnModel.setColumnWeight(2, 2);
            columnModel.setColumnWeight(3, 2);
            callsTableView.setColumnModel(columnModel);

            int headerColor = getResources().getColor(de.codecrafters.tableview.R.color.accent_material_dark);
            callsTableView.setHeaderBackgroundColor(headerColor);
            StatisticsDataTask statisticsTask = new StatisticsDataTask();
            statisticsTask.execute();
        }
    }

    @Override
    protected void onResume() {

        StatisticsDataTask statisticsTask = new StatisticsDataTask();
        statisticsTask.execute();

        if (updateExternalStorageState() == Constants.MEDIA_MOUNTED) {

        } else if (updateExternalStorageState() == Constants.MEDIA_MOUNTED_READ_ONLY) {
            showDialog(NO_MEMORY_CARD);
        } else {

            showDialog(NO_MEMORY_CARD);
        }

        super.onResume();
    }

    /**
     * checks if an external memory card is available
     *
     * @return
     */
    public static int updateExternalStorageState() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return Constants.MEDIA_MOUNTED;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return Constants.MEDIA_MOUNTED_READ_ONLY;
        } else {
            return Constants.NO_MEDIA;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem settings = menu.findItem(R.id.menu_settings);
        settings.setEnabled(true);
        settings.setVisible(true);
        if (!menu.hasVisibleItems()) {
            Logger.d(Constants.TAG, "No menu item is visible!");
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Toast toast;
        final Activity currentActivity = this;
        switch (item.getItemId()) {
            case R.id.menu_about:
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        MainActivity.this);
                builder.setTitle(R.string.about_title)
                        .setMessage(R.string.about_content)
                        .setPositiveButton(R.string.about_close_button,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int id) {
                                        dialog.cancel();
                                    }
                                }).show();
                break;
            case R.id.menu_settings:
                Intent settingsIntent = new Intent(context, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.menu_refresh:
                StatisticsDataTask statisticsTask = new StatisticsDataTask();
                statisticsTask.execute();
                break;
            case R.id.menu_logout:
                new SessionHelper(context).signOut();
                Intent loginIntent = new Intent(context, LoginActivity.class);
                startActivity(loginIntent);
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setSharedPreferences(boolean silentMode) {
        MultiprocessPreferences.MultiprocessSharedPreferences settings = MultiprocessPreferences.getDefaultSharedPreferences(context);
        MultiprocessPreferences.Editor editor = settings.edit();
        editor.putBoolean("silentMode", silentMode);
        editor.commit();
    }

    @Override
    public void onDataClicked(int rowIndex, final CallEntity clickedData) {
        new android.support.v7.app.AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_menu_delete)
                .setTitle("حذف تماس")
                .setMessage("آیا برای حذف تماس " + clickedData.phoneNumber + " مطمئن هستید؟")
                .setPositiveButton(getString(R.string.msg_clean_btn_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CallEntity.deleteAll(CallEntity.class, "media_file_name = ?", clickedData.mediaFileName);
                        FileHelper.deleteFile(clickedData.mediaFileName);
                        StatisticsDataTask statisticsTask = new StatisticsDataTask();
                        statisticsTask.execute();
                    }
                })
                .setNegativeButton(getString(R.string.msg_clean_btn_cancel), null)
                .show();
    }

    @Override
    public void onRefresh(RefreshIndicator refreshIndicator) {
        try {
            final Account syncAccount = AccountHelper.CreateSyncAccount(context);
            ContentResolver.requestSync(syncAccount, Constants.AUTHORITY, Bundle.EMPTY);

            StatisticsDataTask statisticsTask = new StatisticsDataTask();
            statisticsTask.execute();
            refreshIndicator.hide();
        }
        catch (Exception ex){
            Logger.e(Constants.TAG, "Error while trying to refresh data source.");
            Logger.printStackTrace(ex);
        }
    }

    private class StatisticsDataTask extends AsyncTask<Void, Void, CallEntity[]> {

        @Override
        protected CallEntity[] doInBackground(Void... voids) {
            List<CallEntity> calls = null;

            try {
                calls = CallEntity.find(CallEntity.class, "is_synced >= ?", "0");

                publishProgress();

            } catch (Exception ex) {
                Logger.d(Constants.TAG, "Exception throws while reading sync data report.");
                ex.printStackTrace();
            }

            CallEntity[] array = calls.toArray(new CallEntity[calls.size()]);
            return array;
        }

        @Override
        protected void onPostExecute(CallEntity[] data) {
            //lblPending.setText(Integer.toString(data.totalCount - data.pendingCount));
            //lblTotal.setText(Integer.toString(data.totalCount));
            Locale appLocale = new Locale(APP_LOCALE);
            //lblPending.setText(String.format(appLocale, "%d", data.totalCount - data.pendingCount));
            //lblTotal.setText(String.format(appLocale, "%d", data.totalCount));

            TextView lblNoCallRecords = (TextView)findViewById(R.id.lblNoCalls);
            if(data.length == 0){
                lblNoCallRecords.setVisibility(View.VISIBLE);
                callsTableView.setVisibility(View.GONE);
            }else{
                lblNoCallRecords.setVisibility(View.GONE);
                CallDataAdapter adapter = new CallDataAdapter(getApplicationContext(), data);
                callsTableView.setDataAdapter(adapter);
            }
        }
    }
}
