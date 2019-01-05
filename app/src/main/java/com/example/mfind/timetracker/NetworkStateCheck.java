/* Copyright 2018 Mateusz Findeisen */

package com.example.mfind.timetracker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.time.LocalDateTime;

import static java.lang.Thread.sleep;

public class NetworkStateCheck extends Service {
    private static final String TAG = "NetworkStateCheck";

    private String wifiSSIDRegexp = "";
    private int maxBreakTime = 0;
    private String currentNetworkSSID = "";
    private Boolean currentWifiIsCorrect = false;

    private static final String CHANNEL_ID = "ZPWT notification - service is working";
    private long connectionCurrentTime;
    private long connectionStartTime;

    private LocalDateTime startTime = null;

    private Context context;

    FileManipulationsPersistentData fmpd;
    FileManipulationsApplicationInfo fmai;

    IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class LocalBinder extends Binder {
        NetworkStateCheck getServerInstance() {
            return NetworkStateCheck.this;
        }
    }

    public String getCurrentSSID(){
        return wifiSSIDRegexp;
    }

    String getStartTime(){
        return "Service start time: "+startTime;
    }
    long getLastUpdateDifference(){
        return (SystemClock.elapsedRealtime() - connectionCurrentTime)/1000;
    }
    long getLastSavedValue(){
        return saveData(0);
    }
    long getHowLongAgoWeConnectedToWifi(){
        return (SystemClock.elapsedRealtime() - connectionCurrentTime)/1000;
    }

    public void setWifiSSIDRegexp(String ssid){
        wifiSSIDRegexp = ssid;
        currentWifiIsCorrect = wifiSSIDRegexp.equals(currentNetworkSSID);
        saveYourData();
        doBindInfo();
        fmai.setSSID(ssid);
        doUnbindInfo();
    }

    public int saveYourData(){
        Log.i(TAG, "### saveYourData: Attempt to save!");
        if(currentWifiIsCorrect){
            int temp;
            temp = (int)(SystemClock.elapsedRealtime() - connectionCurrentTime)/1000;
            connectionCurrentTime = SystemClock.elapsedRealtime();
            Log.i(TAG, "### saveYourData: Saving data! " + temp + "s");
            return saveData(temp);
        }
        return saveData(0);
    }

    private int saveData(int seconds){
        // gets todays ticker, increases it and returns current ticker
        doBindData();
        int temp = fmpd.prependTicker(seconds);
        //fmpd.invalidateInitialization();
        doUnbindData();
        return temp;
    }

    private void doBindData(){
        fmpd = new FileManipulationsPersistentData();
        final Intent serviceF = new Intent(context, FileManipulationsPersistentData.class);
        startService(serviceF);
        fmpd.setContext(context);
    }

    private void doUnbindData(){
        fmpd.stopSelf();
    }

    private void doBindInfo(){
        fmai = new FileManipulationsApplicationInfo();
        final Intent serviceF = new Intent(context, FileManipulationsApplicationInfo.class);
        startService(serviceF);
        fmai.setContext(context);
    }

    private void doUnbindInfo(){
        fmai.stopSelf();
    }

    @Override
    public void onCreate(){
        super.onCreate();
        this.context = getApplicationContext();

        startTime = LocalDateTime.now();

        connectionCurrentTime = 0;
        connectionStartTime = 0;
        currentWifiIsCorrect = false;
        prepareAndStartForeground();
        readAppInfo();
        registerWifiChangeReceiver();
    }

    private void readAppInfo(){
        doBindInfo();
        wifiSSIDRegexp = fmai.getSSID();
        maxBreakTime = fmai.getMaxBreakTime();
        doUnbindInfo();
    }

    private void prepareAndStartForeground(){
        try {
            Intent notificationIntent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                    notificationIntent, 0);
            String channelID = "This_is_my_channel_ha!";
            NotificationCompat.Builder notification = new NotificationCompat.Builder(context, channelID);
            notification.setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Zero-touch Personal Worktime Tracker")
                    .setContentText("Watching over your WiFi connection")
                    .setShowWhen(false)
                    .setContentIntent(pendingIntent);
            createNotificationChannel();
            startForeground(1337, notification.build());
            Log.i(TAG, "### prepareAndStartForeground: Foreground service started!");
        }catch(SecurityException e){
            Log.e(TAG, "### ### ### prepareAndStartForeground: That... did not happen before...");
            e.printStackTrace();
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String description = "This is your ZPWT";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel("This_is_my_channel_ha!", CHANNEL_ID, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    @Override
    public void onDestroy(){
        saveYourData();
        super.onDestroy();
        Log.d(TAG, "### ### ### onDestroy: because we run in foreground, we should not ever be destroyed, unless in critical memory condition or when shutting down phone?");
        //unregisterReceiver(mWifiStateChangeReceiver);
        //mWifiStateChangeReceiver = null;
    }
//**


    @Override
    public void onLowMemory(){
        Log.i(TAG, "### onLowMemory: Syncing!!!");

        saveYourData();

        super.onLowMemory();
    }

    private void startOrStopCounting(Boolean type){
        if(type){
            if(currentWifiIsCorrect){
                Log.i(TAG, "### startOrStopCounting: repeated signal detected");
                return;
            }
            // e.g. To check the Network Name or other info:
            do{
                currentNetworkSSID = ""; // emptying - might be needed
                try {
                    // we actually need to wait few seconds,
                    // if we won't do that, SSID might not yet be propagated
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = null;
                if (wifiManager != null) {
                    wifiInfo = wifiManager.getConnectionInfo();
                }
                if (wifiInfo != null) {
                    currentNetworkSSID = wifiInfo.getSSID();
                }

                currentNetworkSSID = currentNetworkSSID.replaceAll("^\"|\"$", "");
            } while(currentNetworkSSID.equals("<unknown ssid>")); // to detect '<repeat if read was unsuccessful ssid>'
            Log.d(TAG, "### startOrStopCounting: Currently connected to " + currentNetworkSSID);

            currentWifiIsCorrect = currentNetworkSSID.matches(wifiSSIDRegexp);
            if(currentWifiIsCorrect) {
                if(!detectShortBreak())
                    connectionStartTime = SystemClock.elapsedRealtime();
            }
            
            connectionCurrentTime = SystemClock.elapsedRealtime();
        }else {
            if (currentWifiIsCorrect) { /// if not first run, when app started with WiFi turned off
                saveYourData();
                currentWifiIsCorrect = false;
                connectionCurrentTime = SystemClock.elapsedRealtime();
                Log.i(TAG, "### startOrStopCounting: Wifi disconnected : stopped ticker!");
            }
        }
    }

    private Boolean detectShortBreak(){
        if(SystemClock.elapsedRealtime() - connectionCurrentTime <= maxBreakTime * 1000){
            Log.i(TAG, "### detectShortBreak: Saving...");
            saveYourData();
            return true;
        }
        return false;
    }

    private void registerWifiChangeReceiver()
    {
        BroadcastReceiver mWifiStateChangeReceiver =    new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                    // Do your work.
                    NetworkInfo nwInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    startOrStopCounting(nwInfo.isConnected());

                } else {
                    Log.d(TAG, "### ### ### onReceive: HOW COULD THAT HAPPEN??? That's the only action I'm looking for!");
                }

            }
        };
        IntentFilter filter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        this.registerReceiver(mWifiStateChangeReceiver, filter);
    }

    private void displayNetworkInfo(){

    }
}
