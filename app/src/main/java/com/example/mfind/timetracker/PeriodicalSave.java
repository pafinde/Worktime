package com.example.mfind.timetracker;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import static java.lang.Thread.sleep;

/** TODO change this
 * This class is periodically invoked by job scheduler.
 *
 * it handles periodical save by invoking saveYourData() in BroadcastReceiver Service
 */
public class PeriodicalSave extends JobService {
    private static final String TAG = "PeriodicalSave";

    boolean mBoundedReceiver = false;
    NetworkStateCheck mServerReceiver;

    /**
     * When Job scheduler runs our job, it actually starts this method
     *
     * @param params - parameters of the job, needed to finish it later
     * @return - returns true when job needs to continue running,
     * we are finishing it with jobFinished()
     */
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "### nStartJob: job started");
        doBackgroundWork(params);
        return true;
    }

    /**
     * Called when something cancells our job.
     * Job can be cancelled by app or system
     *
     * @param params - parameters of job
     * @return - Returns true if we want system to reschedule our job
     */
    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "### onStopJob: job cancelled");
        return true;
    }

    /**
     * Background work here
     *
     * we bind to BroadcastReceiver Service there to invoke saveYourData()
     * @param params - parameters of the job
     */
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

                mServerReceiver.saveYourData();

                if(mBoundedReceiver) {
                    unbindService(mConnectionToReceiver);
                    mBoundedReceiver = false;
                }

                jobFinished(params, false); // TODO was 'true'
            }
        }).start();
    }

    /**
     * Interface used for connection between this class (MainActivity) and BroadcastReceiver Service
     */
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