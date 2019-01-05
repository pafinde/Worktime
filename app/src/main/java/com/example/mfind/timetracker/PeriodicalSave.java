package com.example.mfind.timetracker;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import static java.lang.Thread.sleep;

public class PeriodicalSave extends JobService {
    private static final String TAG = "PeriodicalSave";

    boolean mBoundedReceiver = false;
    NetworkStateCheck mServerReceiver;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "### nStartJob: job started");
        doBackgroundWork(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "### onStopJob: job cancelled");
        return true;
    }

    private void doBackgroundWork(final JobParameters params) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent mIntentSR = new Intent(getApplicationContext(), NetworkStateCheck.class);
                startForegroundService(mIntentSR);
                bindService(mIntentSR, mConnectionToReceiver, BIND_AUTO_CREATE);

                while(!mBoundedReceiver){
                    try {
                        Log.i(TAG, "### run: Sleep required! Sleeping...");
                        sleep(2 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.i(TAG, "### run: And still binding....");
                }

                int i = mServerReceiver.saveYourData();

                if(mBoundedReceiver) {
                    unbindService(mConnectionToReceiver);
                    mBoundedReceiver = false;
                }

                jobFinished(params, true);
            }
        }).start();
    }

    ServiceConnection mConnectionToReceiver = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if(!mBoundedReceiver) {
                NetworkStateCheck.LocalBinder mLocalBinder = (NetworkStateCheck.LocalBinder) service;
                mServerReceiver = mLocalBinder.getServerInstance();
                mBoundedReceiver = true;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if(mBoundedReceiver) {
                mBoundedReceiver = false;
                mServerReceiver = null;
            }
        }
    };

}