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

public class FileManipulationsApplicationInfo extends Service {
    private static final String TAG = "FileManipulationsApplic";
    private String APPLICATION_DATA_FILENAME = "ZPWT_ApplicationData.bin";

    boolean initialized = false;
    SettingsProto.AppSettings.Builder appInfo = null;

    private Context context = null;

    IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class LocalBinder extends Binder {
        FileManipulationsApplicationInfo getServerInstance() {
            return FileManipulationsApplicationInfo.this;
        }
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getSSID(){
        if(!initialized)
            appInfo = readDataFromMemory();
        return appInfo.getSsid();
    }

    public int getMaxBreakTime(){
        if(!initialized)
            appInfo = readDataFromMemory();
        return appInfo.getMaxBreakTime();
    }

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

    /// used only when first launching the application
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

    private SettingsProto.AppSettings.Builder readDataFromMemory(){
        SettingsProto.AppSettings.Builder appInfo = SettingsProto.AppSettings.newBuilder();
        // Read the existing data file.
        try {
            appInfo.mergeFrom(context.openFileInput(APPLICATION_DATA_FILENAME));
        } catch (FileNotFoundException e) {
            Log.d(TAG, "### readDataFromMemory: " + APPLICATION_DATA_FILENAME + ": File not found.  Creating a new file and prefilling it with data.");
            prefillWithData();
            try {
                Log.i(TAG, "### readDataFromMemory: merging");
                appInfo.mergeFrom(context.openFileInput(APPLICATION_DATA_FILENAME));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            Log.e(TAG, "### readDataFromMemory: Input stream error.");
            e.printStackTrace();
        }
        return appInfo;
    }

    private void writeDataToMemory(SettingsProto.AppSettings data) throws IOException{
        data.writeTo(context.openFileOutput(APPLICATION_DATA_FILENAME, MODE_PRIVATE));
    }
}
