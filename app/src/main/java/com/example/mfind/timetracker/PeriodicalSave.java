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
    Boolean jobCancelled = false;

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
        jobCancelled = false;
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
        synchronized (PeriodicalSave.class) {
            jobCancelled = true;
        }
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
            NetworkStateCheck mServerReceiver = null;

            @Override
            public void run() {
                Intent mIntentSR = new Intent(getApplicationContext(), NetworkStateCheck.class);
                startForegroundService(mIntentSR);

                // Interface used for connection with BroadcastReceiver Service
                ServiceConnection mConnectionToReceiver = new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        synchronized (PeriodicalSave.class) {
                            if (mServerReceiver == null) {
                                NetworkStateCheck.LocalBinder mLocalBinder = (NetworkStateCheck.LocalBinder) service;
                                mServerReceiver = mLocalBinder.getServerInstance();
                            }
                        }
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                        synchronized (PeriodicalSave.class) {
                            mServerReceiver = null;
                        }
                    }
                };

                synchronized (PeriodicalSave.class) {
                    if(!bindService(mIntentSR, mConnectionToReceiver, BIND_AUTO_CREATE)){
                        // Service does not exist (or we have no permission to access it).
                        return;
                    }
                }

                while(mServerReceiver == null){
                    if(jobCancelled){
                        Log.w(TAG, "### run: job cancelled by system! Aborting...");
                        return;
                    }
                    try {
                        Log.i(TAG, "### run: Sleep required! Sleeping...");
                        sleep(2 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.i(TAG, "### run: And still binding....");
                }

                mServerReceiver.saveYourData();

                synchronized (PeriodicalSave.class) {
                    if(jobCancelled){
                        Log.w(TAG, "### run: job cancelled by system! Aborting...");
                        return;
                    }
                    unbindService(mConnectionToReceiver);
                }

                jobFinished(params, false);
            }
        }).start();
    }
}