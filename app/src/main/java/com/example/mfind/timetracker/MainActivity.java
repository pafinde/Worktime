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
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    boolean mBoundedReceiver, mBoundedFileManipulator;
    NetworkStateCheck mServerReceiver;
    FileManipulationsPersistentData mServerFileManipulator;

    private float x1, x2;
    private long startClickTime;
    static final int MIN_DISTANCE = 150;
    static final int MAX_SWIPE_TIME = 200;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = this;

        mBoundedReceiver = false;
        mBoundedFileManipulator = false;

        getServiceStartedIfNeeded();
    }

    @Override
    protected void onStart(){
        super.onStart();
        Intent mIntentSR = new Intent(this, NetworkStateCheck.class);
        bindService(mIntentSR, mConnectionToReceiver, BIND_AUTO_CREATE);
        Intent mIntentFM = new Intent(this, FileManipulationsPersistentData.class);
        bindService(mIntentFM, mConnectionToFileManipulator, BIND_AUTO_CREATE);

        scheduleJob();
    }

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

    ServiceConnection mConnectionToReceiver = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NetworkStateCheck.LocalBinder mLocalBinder = (NetworkStateCheck.LocalBinder)service;
            mServerReceiver = mLocalBinder.getServerInstance();
            if(mBoundedFileManipulator){
                mServerReceiver.saveYourData();
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

    ServiceConnection mConnectionToFileManipulator = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            FileManipulationsPersistentData.LocalBinder mLocalBinder = (FileManipulationsPersistentData.LocalBinder)service;
            mServerFileManipulator = mLocalBinder.getServerInstance();
            mServerFileManipulator.setContext(getApplicationContext());
            if(mBoundedReceiver){
                mServerReceiver.saveYourData();
                mServerFileManipulator.invalidateInitialization();
                refreshValues();
            }
            mBoundedFileManipulator = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBoundedFileManipulator = false;
            mServerFileManipulator = null;
            System.out.println("### I'm OFF FILEMA");
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                startClickTime = Calendar.getInstance().getTimeInMillis();
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                x2 = event.getX();
                float deltaX = x1 - x2;
                if (Math.abs(deltaX) > MIN_DISTANCE && clickDuration < MAX_SWIPE_TIME)
                {
                    System.out.println("### Swiped right2left.");

                    Intent intent = new Intent(this, EntriesEditor.class);
                    startActivity(intent);
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private void scheduleJob(){
        ComponentName compName = new ComponentName(this, PeriodicalSave.class);
        JobInfo.Builder info = new JobInfo.Builder(1, compName)
            .setPeriodic(15 * 60 * 1000, 60 * 1000) // job is set to fire regularly every 15 minutes, with up to 5 minutes of fluctuation
            .setPersisted(true);
        if (Build.VERSION.SDK_INT >= 28)
            info.setEstimatedNetworkBytes(0, 0);
        info.build();

        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(info.build());
        if(resultCode == JobScheduler.RESULT_SUCCESS)
            System.out.println("### Job scheduled!");
        else
            System.out.println("### Job scheduler failure!");
    }

    private void getServiceStartedIfNeeded(){
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
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
        final Intent service = new Intent(context, NetworkStateCheck.class);
        context.startForegroundService(service); // won't start a new service if one is already running

        final Intent serviceF = new Intent(context, FileManipulationsPersistentData.class);
        context.startService(serviceF);

        //fmpd = new FileManipulationsPersistentData();
        //fmpd.setContext(context); // this is required for files to be created for our application
    }

    public void refresh(View view){
        System.out.println("### Clicked refresh.");
        mServerReceiver.saveYourData();
        mServerFileManipulator.invalidateInitialization();
        refreshValues();
    }

    private String changeSecondsToFormat(long seconds){
        return seconds/(60*60) + "h " + (seconds%(60*60))/60 + "min " + seconds%60 + "s";
    }

    /** Called when the user taps the Send button */
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
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String wifiSSID = input.getText().toString();
                mServerReceiver.setWifiSSIDRegexp(wifiSSID);
                Toast.makeText(context, "Changed SSID to: " + wifiSSID, Toast.LENGTH_SHORT).show();
            }
        });
        // Set up the buttons: negative - no changer
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
}
