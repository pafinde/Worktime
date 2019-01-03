/* Copyright 2018 Mateusz Findeisen */

package com.example.mfind.zpwtzerotouchpersonalwifitimetracker;

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
import android.widget.Toast;

import java.util.Objects;

import static java.lang.Thread.sleep;

public class NetworkStateCheck extends Service {

    private static final String TAG = "NetworkStateCheck";

    private String wifiSSIDRegexp = "";
    private int maxBreakTime = 0;
    private String currentNetworkSSID = "";
    private Boolean currentWifiIsCorrect = false;

    private static final String CHANNEL_ID = "ZPWT";
    private long connectionStartTime;

    private Context context;

    /**
    boolean mBoundedFileManipulator;
    FileManipulationsPersistentData mServerFileManipulator;
     //*/
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

    public void setWifiSSIDRegexp(String ssid){
        wifiSSIDRegexp = ssid;
        currentWifiIsCorrect = wifiSSIDRegexp.equals(currentNetworkSSID);
        saveYourData();
        doBindInfo();
        fmai.setSSID(ssid);
        doUnbindInfo();
    }

    public int saveYourData(){
        System.out.println("### Attempt to save!");
        if(currentWifiIsCorrect){
            int temp;
            temp = (int)(SystemClock.elapsedRealtime() - connectionStartTime)/1000;
            connectionStartTime = SystemClock.elapsedRealtime();
            return saveData(temp);
        }
        return saveData(0);
    }

    private int saveData(int seconds){
        // gets todays ticker, increases it and returns current ticker
        System.out.println("### Saving data! " + seconds + "s");
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
            System.out.println("### Foreground service started!");
        }catch(SecurityException e){
            System.out.println("### ### ### That... did not happen before...");
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
        System.out.println("### ### ### because we run in foreground, we should not ever be destroyed, unless in critical memory condition or when shutting down phone?");
        //unregisterReceiver(mWifiStateChangeReceiver);
        //mWifiStateChangeReceiver = null;
    }
//**


    @Override
    public void onLowMemory(){
        System.out.println("### System is really low on memory!!! Syncing!!!");

        saveYourData();

        super.onLowMemory();
    }

    private void startOrStopCounting(Boolean type){
        if(type){
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
                System.out.println(currentNetworkSSID);
            } while(currentNetworkSSID.equals("<unknown ssid>")); // to detect '<repeat if read was unsuccessful ssid>'
            System.out.println("### Currently connected to " + currentNetworkSSID);

            connectionStartTime = SystemClock.elapsedRealtime();

            currentWifiIsCorrect = currentNetworkSSID.matches(wifiSSIDRegexp);
            if(currentWifiIsCorrect) {
                detectShortBreak();
            }
        }else {
            if (currentWifiIsCorrect) { /// if not first run, when app started with WiFi turned off
                saveYourData();
                currentWifiIsCorrect = false;
                connectionStartTime = SystemClock.elapsedRealtime();
                System.out.println("### Wifi disconnected : stopped ticker!");
            }
        }
    }

    private void detectShortBreak(){
        if(SystemClock.elapsedRealtime() - connectionStartTime <= maxBreakTime * 1000)
            saveYourData();
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
                    System.out.println("### ### ### HOW COULD THAT HAPPEN??? That's the only action I'm looking for!");
                }

            }
        };
        IntentFilter filter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        this.registerReceiver(mWifiStateChangeReceiver, filter);
    }

    private void displayNetworkInfo(){

    }
}
