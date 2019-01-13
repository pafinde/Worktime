/*
Copyright 2018 Mateusz Findeisen

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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * This class is used by BroadcastReceiver Service
 * to get some values or update files in application info proto.
 *
 * It's the only way of communicating with the actual class that does the operations on
 * application info proto
 */
public class FileManipulationsApplicationInfo extends Service {
    private static final String TAG = "FileManipulationsApplic";
    private String APPLICATION_DATA_FILENAME = "ZPWT_ApplicationData.bin";

    boolean initialized = false;
    SettingsProto.AppSettings.Builder appInfo = null;

    private Context context = null;

    IBinder mBinder = new LocalBinder();

    /**
     * when someone is binding to this class, we return out binder - our ~interface~ to talk with us
     *
     * @param intent - specified intent of the class that wants to bind to us, we could read this
     *               intent and decide based on that if we actually want to give caller a binder
     *               or not
     * @return - returns a binder
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Returns instance of out class when created
     */
    class LocalBinder extends Binder {
        FileManipulationsApplicationInfo getServerInstance() {
            return FileManipulationsApplicationInfo.this;
        }
    }

    /**
     * Used by outside sources to give us possibility to write files for our application
     *
     * @param context - context of our application needed to access application files
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * @return - returns wifi ssid regex as saved in application memory
     */
    public String getSSID(){
        if(!initialized)
            appInfo = readDataFromMemory();
        return appInfo.getSsid();
    }

    /**
     * @return - returns max break time as saved in application memory
     */
    public int getMaxBreakTime(){
        if(!initialized)
            appInfo = readDataFromMemory();
        return appInfo.getMaxBreakTime();
    }

    public int getLastSaveTime(){
        if(!initialized)
            appInfo = readDataFromMemory();
        return appInfo.getLastSaveTime();
    }

    public void setLastSaveTime(int elapsedRealtimeInSeconds){
        if(!initialized)
            appInfo = readDataFromMemory();
        appInfo.setLastSaveTime(elapsedRealtimeInSeconds);
        try {
            writeDataToMemory(appInfo.build());
        } catch (IOException e) {
            Log.e(TAG, "### setLastSaveTime: Output stream error.");
            e.printStackTrace();
        }
    }

    /**
     * Used to set and save wifi ssid regex to application memory
     * @param ssid - string containing wifi ssid regex
     */
    public void setSSID(String ssid){
        if(!initialized)
            appInfo = readDataFromMemory();
        appInfo.setSsid(ssid);
        try {
            writeDataToMemory(appInfo.build());
        } catch (IOException e) {
            Log.e(TAG, "### setSSID: Output stream error.");
            e.printStackTrace();
        }
    }

    /**
     * Used only when first launching the application
     *
     * Creates empty proto with default application values in it
     */
    private void prefillWithData(){
        SettingsProto.AppSettings.Builder appInfo = SettingsProto.AppSettings.newBuilder();
        appInfo.setSsid("AndroidWifi");
        appInfo.setMaxBreakTime(10 * 60);
        try {
            writeDataToMemory(appInfo.build());
        } catch (IOException e) {
            Log.e(TAG, "### prefillWithData: Output stream error.");
            e.printStackTrace();
        }
    }

    /**
     * Reads AppSetting data from application memory
     *
     * @return - returns ready builder containing AppSettings
     */
    private SettingsProto.AppSettings.Builder readDataFromMemory(){
        SettingsProto.AppSettings.Builder appInfo = SettingsProto.AppSettings.newBuilder();
        // Read the existing data file.
        try {
            appInfo.mergeFrom(context.openFileInput(APPLICATION_DATA_FILENAME));
        } catch (FileNotFoundException e) {
            Log.d(TAG, "### readDataFromStorage: " + APPLICATION_DATA_FILENAME + ": File not found.  Creating a new file and prefilling it with data.");
            prefillWithData();
            try {
                Log.i(TAG, "### readDataFromStorage: merging");
                appInfo.mergeFrom(context.openFileInput(APPLICATION_DATA_FILENAME));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            Log.e(TAG, "### readDataFromStorage: Input stream error.");
            e.printStackTrace();
        }
        return appInfo;
    }

    /**
     * Writes data to memory
     *
     * @param data - data to write (overwrite!)
     * @throws IOException - when file couldn't be saved
     */
    private void writeDataToMemory(SettingsProto.AppSettings data) throws IOException{
        data.writeTo(context.openFileOutput(APPLICATION_DATA_FILENAME, MODE_PRIVATE));
    }
}
