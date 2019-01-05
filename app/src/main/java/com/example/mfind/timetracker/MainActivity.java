/*
Copyright 2018-2019 Mateusz Findeisen

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package com.example.mfind.timetracker;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * MainActivity is the main screen we can see when launching app for the first time.
 * It creates periodical job and allows user to read information from app.
 *
 * MainActivity also allows user to change wifi SSID regex and to view and edit entries from last 3 months
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    boolean mBoundedReceiver, mBoundedFileManipulator;
    NetworkStateCheck mServerReceiver;
    FileManipulationsPersistentData mServerFileManipulator;

    private float motionDownX;
    private long startClickTime;
    static final int MIN_DISTANCE = 150;
    static final int MAX_SWIPE_TIME = 200;

    private Context context;

    /**
     * Invoked when MainActivity is created when opening the app.
     * We also start services here.
     * (Broadcast Receiver, to detect wifi state change and FileManipulator to fetch data from
     * our application proto files)
     *
     * BroadcastReceiver Service is never closed, and
     * FileManipulator Service is closed with MainActivity
     *
     * @param savedInstanceState used by super.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = this;

        mBoundedReceiver = false;
        mBoundedFileManipulator = false;

        getServiceStartedIfNeeded();
    }

    /**
     * Invoked right before actually displaying out application to user.
     *
     * We make sure that when onStart finishes, we are already in process of binding to
     * 2 services we are using, and that we actually scheduled a job
     */
    @Override
    protected void onStart(){
        super.onStart();
        Intent mIntentSR = new Intent(this, NetworkStateCheck.class);
        bindService(mIntentSR, mConnectionToReceiver, BIND_AUTO_CREATE);
        Intent mIntentFM = new Intent(this, FileManipulationsPersistentData.class);
        bindService(mIntentFM, mConnectionToFileManipulator, BIND_AUTO_CREATE);

        scheduleJob();
    }

    /**
     * Called when we close out application View, we just unbind services there
     */
    @Override
    protected void onStop() {
        super.onStop();
        if(mBoundedReceiver) {
            unbindService(mConnectionToReceiver);
            mBoundedReceiver = false;
        }
        if(mBoundedFileManipulator){
            unbindService(mConnectionToFileManipulator);
            mBoundedFileManipulator = false;
        }
    }

    /**
     * Used to refresh all fields in activity_main with currently calculated values.
     *
     * The fields we are updating are:
     * todayTicker - how long have we been connected to wifi for today
     * average7Data - average connection time for last 7 (not zero) days
     * average30Data - average connection time for last 30 (not zero) days
     * average90Data - average connection time for last 90 (not zero) days
     */
    private void refreshValues(){
        TextView t;
        t = findViewById(R.id.todayTicker);
        t.setText(changeSecondsToFormat(mServerFileManipulator.getTodayTicker()));
        t = findViewById(R.id.average7Data);
        t.setText(changeSecondsToFormat(mServerFileManipulator.get7DayAverage()));
        t = findViewById(R.id.average30Data);
        t.setText(changeSecondsToFormat(mServerFileManipulator.get30DayAverage()));
        t = findViewById(R.id.average90Data);
        t.setText(changeSecondsToFormat(mServerFileManipulator.get90DayAverage()));
    }

    /**
     * Interface used for connection between this class (MainActivity) and BroadcastReceiver Service
     */
    ServiceConnection mConnectionToReceiver = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NetworkStateCheck.LocalBinder mLocalBinder = (NetworkStateCheck.LocalBinder)service;
            mServerReceiver = mLocalBinder.getServerInstance();
            if(mBoundedFileManipulator){
                //mServerReceiver.saveYourData();
                mServerFileManipulator.invalidateInitialization();
                refreshValues();
            }
            mBoundedReceiver = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBoundedReceiver = false;
            mServerReceiver = null;
        }
    };

    /**
     * Interface used for connection between this class (MainActivity) and FileManipulator Service
     */
    ServiceConnection mConnectionToFileManipulator = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            FileManipulationsPersistentData.LocalBinder mLocalBinder = (FileManipulationsPersistentData.LocalBinder)service;
            mServerFileManipulator = mLocalBinder.getServerInstance();
            mServerFileManipulator.setContext(getApplicationContext());
            if(mBoundedReceiver){
                //mServerReceiver.saveYourData();
                mServerFileManipulator.invalidateInitialization();
                refreshValues();
            }
            mBoundedFileManipulator = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBoundedFileManipulator = false;
            mServerFileManipulator = null;
            Log.w(TAG, "onServiceDisconnected: disconnected from FILEMA");
        }
    };

    /**
     * Detects when user taps or untaps the screen to calculate if there was a swipe performed
     *
     * @param event - touch down or touch up event
     * @return - returns value from super (?)
     */
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                startClickTime = Calendar.getInstance().getTimeInMillis();
                motionDownX = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                float deltaX = motionDownX - event.getX();
                if (deltaX > MIN_DISTANCE && clickDuration < MAX_SWIPE_TIME)
                {
                    Intent intent = new Intent(this, EntriesEditor.class);
                    startActivity(intent);
                }
                if (deltaX < -MIN_DISTANCE && clickDuration < MAX_SWIPE_TIME)
                {
                    Intent intent = new Intent(this, DebugScreen.class);
                    startActivity(intent);
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * Scheduler a job that is executed approx. every 15 minutes with 1m flex time.
     * Also this job is set to be Persisted, that means it stays between reboots.
     */
    private void scheduleJob(){
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        List<JobInfo> pendingJobs = jobScheduler.getAllPendingJobs();
        if(pendingJobs.isEmpty()) {

            ComponentName compName = new ComponentName(this, PeriodicalSave.class);
            JobInfo.Builder info = new JobInfo.Builder(1, compName)
                    .setPeriodic(15 * 60 * 1000, 60 * 1000) // job is set to fire regularly every 15 minutes, with up to 5 minutes of fluctuation
                    .setPersisted(true);
            //if (Build.VERSION.SDK_INT >= 28)
            //    info.setImportantWhileForeground(true);
            info.build();

            int resultCode = jobScheduler.schedule(info.build());
            if (resultCode == JobScheduler.RESULT_SUCCESS)
                Log.i(TAG, "### scheduleJob: Job starts now, doing this every approx. 15 minutes");
            else
                Log.e(TAG, "### ### ### scheduleJob: failure!");
        }
    }

    /**
     * Requests location permission if needed, and then starts 2 services:
     * BroadcastReceiver Service & FileManipulator Service
     */
    private void getServiceStartedIfNeeded(){
        requestLocationPermission();

        final Intent service = new Intent(context, NetworkStateCheck.class);
        context.startForegroundService(service); // won't start a new service if one is already running

        final Intent serviceF = new Intent(context, FileManipulationsPersistentData.class);
        context.startService(serviceF);

        //fmpd = new FileManipulationsPersistentData();
        //fmpd.setContext(context); // this is required for files to be created for our application
    }

    /**
     * Asks user for permission to access coarse location. If user previously disallowed, we show
     * an explanation and ask again
     *
     * Access to coarse location is needed to read SSID of connected network.
     * This is true as of android 8.1.0 (API 27) and later
     */
    private void requestLocationPermission(){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Toast.makeText(context, "App uses localization to access SSID of connected network.", Toast.LENGTH_LONG).show();

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    /**
     * Provides an easy way to change seconds to H/M/S format
     *
     * @param seconds - number of seconds to change to H/M/S format
     * @return - raturn a String that looks like this: %dh %dm %ds
     */
    static String changeSecondsToFormat(long seconds){
        return seconds/(60*60) + "h " + (seconds%(60*60))/60 + "m " + seconds%60 + "s";
    }

    /**
     * Function run when user clicks "refresh" button. It forces BroadcastReceiver Server
     * to save its data and then forces FileManipulator Service to invalidate last calculated
     * values, after which it refreshes the screen so user can see current, correct values
     *
     * @param view - it's the clicked button
     */
    public void refresh(View view){
        Log.i(TAG, "### refresh: clicked");
        mServerReceiver.saveYourData();
        mServerFileManipulator.invalidateInitialization();
        refreshValues();
    }

    /**
     * Called when the user taps the Send button
     *
     * It displays a alert box in which we ask for new SSID that user might want want to observe.
     *
     * @param view - it's the clicked button
     */
    public void changeSSID(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected;
        input.setHint("SSID regex");
        input.setText(mServerReceiver.getCurrentSSID());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setTitle("Enter regex of wifi name (SSID) to track");

        // Set up the buttons: positive - changer
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            /**
             * User submits a new wifi SSID regex
             * @param dialog - Dialog box that this button is part of
             * @param which - ???
             */
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String wifiSSID = input.getText().toString();
                mServerReceiver.setWifiSSIDRegexp(wifiSSID);
                Toast.makeText(context, "Changed SSID to: " + wifiSSID, Toast.LENGTH_SHORT).show();
            }
        });
        // Set up the buttons: negative - no changer
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            /**
             * User cancels a change
             * @param dialog - Dialog box that this button is part of
             * @param which - ???
             */
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
}
