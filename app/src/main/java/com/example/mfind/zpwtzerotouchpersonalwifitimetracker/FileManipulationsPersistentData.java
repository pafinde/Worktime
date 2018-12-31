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

package com.example.mfind.zpwtzerotouchpersonalwifitimetracker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;

public class FileManipulationsPersistentData extends Service {
    private String CONNECTION_DATA_FILENAME = "ZPWT_WifiConnectionDataList.bin";
    protected int todayTicker;
    protected int average7;
    protected int average30;
    protected int average90;
    protected boolean valuesInitialized = false;

    private Context context = null;

    IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class LocalBinder extends Binder {
        FileManipulationsPersistentData getServerInstance() {
            return FileManipulationsPersistentData.this;
        }
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void invalidateInitialization(){
        valuesInitialized = false;
    }

    /**
    void actTest(double r){
        System.out.println("### " + r);
    }
    void recTest(){
        System.out.println("### ----->> Receiver Entered!");
        try {
            sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("### ----->> Receiver Leaved!");
    }
     //*/

    long getTodayTicker(){
        updateValuesForReading();
        return todayTicker;
    }

    long get7DayAverage(){
        updateValuesForReading();
        return average7;
    }

    long get30DayAverage(){
        updateValuesForReading();
        return average30;
    }

    long get90DayAverage(){
        updateValuesForReading();
        return average90;
    }

    int prependTicker(int secs){
        System.out.println("### Adding seconds: " + secs);

        /// we are prefilling protos with empty days for protos continuity
        WIFIConnectionTime.PersistentData.Builder wifiData = prependWithEmptyDays(readDataFromMemory());

        /// now our day(0) is for sure the current one
        WIFIConnectionTime.Day.Builder dayBuilder = WIFIConnectionTime.Day.newBuilder();
        WIFIConnectionTime.Day day = wifiData.getDay(0);

        int todayTicker;

        dayBuilder = copyDay(dayBuilder, day);

        todayTicker = day.getTickerSeconds();
        todayTicker += secs;
        dayBuilder.setTickerSeconds(todayTicker);
        wifiData.setDay(0, dayBuilder);  // SET day - means replace

        try {
            writeDataToMemory(wifiData.build());
        } catch (IOException e) {
            System.out.println("### Output stream error.");
            e.printStackTrace();
        }
        return todayTicker;
    }

    public int findIndexByDate(int year, int month, int day){
        WIFIConnectionTime.PersistentData wifiData = prependWithEmptyDays(readDataFromMemory()).build();
        int i = 0;
        for(; i < wifiData.getDayCount(); i++){
            if(year == wifiData.getDay(i).getYear())
                if(month == wifiData.getDay(i).getMonth())
                    if(day == wifiData.getDay(i).getDay())
                        break;
        }
        return i >= wifiData.getDayCount() ? -1 : i;
    }

    public void addEditEntry(int index, String comment, int seconds){
        WIFIConnectionTime.PersistentData.Builder wifiData = prependWithEmptyDays(readDataFromMemory());
        WIFIConnectionTime.Day.Builder day = WIFIConnectionTime.Day.newBuilder();
        WIFIConnectionTime.Day.Edit.Builder edit = WIFIConnectionTime.Day.Edit.newBuilder();

        day = copyDay(day, wifiData.getDay(index));

        edit.setMinuteOfDay(LocalTime.now().getHour() * 60 + LocalTime.now().getMinute());
        edit.setDeltaMinutes(seconds);
        edit.setComment(comment);
        day.addEdits(edit);

        wifiData.setDay(index, day);

        try {
            writeDataToMemory(wifiData.build());
        } catch (IOException e) {
            System.out.println("### Output stream error.");
            e.printStackTrace();
        }
    }

    private WIFIConnectionTime.Day.Builder copyDay(WIFIConnectionTime.Day.Builder that, WIFIConnectionTime.Day other){
        that.setYear(other.getYear());
        that.setMonth(other.getMonth());
        that.setDay(other.getDay());
        that.setTickerSeconds(other.getTickerSeconds());

        WIFIConnectionTime.Day.Edit.Builder edit = WIFIConnectionTime.Day.Edit.newBuilder();
        for(int i = 0; i < other.getEditsCount(); i++){
            edit.setMinuteOfDay(other.getEdits(i).getMinuteOfDay());
            edit.setDeltaMinutes(other.getEdits(i).getDeltaMinutes());
            edit.setComment(other.getEdits(i).getComment());
            that.addEdits(edit);
        }
        return that;
    }

    public WIFIConnectionTime.PersistentData getEntries(){
        return prependWithEmptyDays(readDataFromMemory()).build();
    }

    /// prepends wifiData with empty days - easier to compute averages!
    protected WIFIConnectionTime.PersistentData.Builder prependWithEmptyDays(WIFIConnectionTime.PersistentData.Builder wifiData){
        WIFIConnectionTime.Day.Builder dayBuilder = WIFIConnectionTime.Day.newBuilder();

        for(int i = 0;; i++){
            LocalDate dayDate = LocalDate.now().minusDays(i);
            WIFIConnectionTime.Day dayData = wifiData.getDay(i);

            if(dayDate.equals(LocalDate.of(dayData.getYear(), dayData.getMonth(), dayData.getDay())))
                break;
            dayBuilder.setYear(dayDate.getYear());
            dayBuilder.setMonth(dayDate.getMonthValue());
            dayBuilder.setDay(dayDate.getDayOfMonth());
            dayBuilder.setTickerSeconds(0);
            wifiData.addDay(i, dayBuilder);
        }
        return deleteExcessDays(wifiData);
    }

    // removes excess days - days above 91 required to compute all averages
    private WIFIConnectionTime.PersistentData.Builder deleteExcessDays(WIFIConnectionTime.PersistentData.Builder wifiData){
        while(wifiData.getDayCount() > 91)
            wifiData.removeDay(91);
        return wifiData;
    }

    protected void updateValuesForReading(){
        if (valuesInitialized)
            return;

        WIFIConnectionTime.PersistentData wifiData = prependWithEmptyDays(readDataFromMemory()).build();

        average7 = average30 = average90 = 0;
        int notZeroDaysCount7 = 0, notZeroDaysCount30 = 0, notZeroDaysCount90 = 0;
        /// this is for detecting days that actually are after a gap in
        /// PersistentData (should never happen, but checking anyways)
        LocalDate localDate7DaysAgo  = LocalDate.now().minusDays(7);
        LocalDate localDate30DaysAgo = LocalDate.now().minusDays(30);
        LocalDate localDate90DaysAgo = LocalDate.now().minusDays(90);

        for(int i = 1; i < wifiData.getDayCount(); i++){
            WIFIConnectionTime.Day day = wifiData.getDay(i);
            int temp = day.getTickerSeconds() + getSumOfDeltaMinutesFromEdits(day);
            if(temp != 0) {
                LocalDate dateOfCurrentlyCheckingElement = LocalDate.of(day.getYear(), day.getMonth(), day.getDay());
                if(i <= 7 && !localDate7DaysAgo.isAfter(dateOfCurrentlyCheckingElement)){
                    average7 += temp;
                    notZeroDaysCount7++;
                }
                if(i <= 30 && !localDate30DaysAgo.isAfter(dateOfCurrentlyCheckingElement)){
                    average30 += temp;
                    notZeroDaysCount30++;
                }
                if(i <= 90 && !localDate90DaysAgo.isAfter(dateOfCurrentlyCheckingElement)){
                    average90 += temp;
                    notZeroDaysCount90++;
                }
            }
        }

        average7  = (notZeroDaysCount7  != 0 ? (average7  / notZeroDaysCount7 ) : 0);
        average30 = (notZeroDaysCount30 != 0 ? (average30 / notZeroDaysCount30) : 0);
        average90 = (notZeroDaysCount90 != 0 ? (average90 / notZeroDaysCount90) : 0);
        todayTicker = wifiData.getDay(0).getTickerSeconds();

        valuesInitialized = true;
    }

    public static int getSumOfDeltaMinutesFromEdits(final WIFIConnectionTime.Day day){
        int sum = 0;
        for(int j = 0; j < day.getEditsCount(); j++)
            sum += day.getEdits(j).getDeltaMinutes();
        return sum;
    }

    /// used only when first launching the application
    private void prefillWithData(){
        WIFIConnectionTime.PersistentData.Builder wifiConnectionTimeList = WIFIConnectionTime.PersistentData.newBuilder();
        for(int i = 0; i <= 90; i++){
            WIFIConnectionTime.Day.Builder day = WIFIConnectionTime.Day.newBuilder();
            try {
                LocalDate localDateMinusDays = LocalDate.now().minusDays(i+1);
                day.setYear(localDateMinusDays.getYear());
                day.setMonth(localDateMinusDays.getMonthValue());
                day.setDay(localDateMinusDays.getDayOfMonth());
                day.setTickerSeconds(0);
            }catch(DateTimeException e){
                System.out.println("### TIME WARP ERROR");
                e.printStackTrace();
            }
            wifiConnectionTimeList.addDay(day);
        }
        try {
            writeDataToMemory(wifiConnectionTimeList.build());
        } catch (IOException e) {
            System.out.println("### Output stream error.");
            e.printStackTrace();
        }
    }


    private void deleteProtoFile(){
        System.out.println("### ### ### I'm deleteing protofile!");
        File file = context.getFilesDir();
        File temp[] = file.listFiles();
        for(int i = 0; i < temp.length; ++i)
            System.out.println("### File number " + i + " to " + temp[i]);
        //noinspection ResultOfMethodCallIgnored
        if(temp.length >= 1)
            temp[0].delete();
        if(temp.length >= 2)
            temp[1].delete();
    }

    protected WIFIConnectionTime.PersistentData.Builder readDataFromMemory(){
        //deleteProtoFile();

        WIFIConnectionTime.PersistentData.Builder wifiConnectionTimeList = WIFIConnectionTime.PersistentData.newBuilder();
        // Read the existing address book.
        try {
            wifiConnectionTimeList.mergeFrom(context.openFileInput(CONNECTION_DATA_FILENAME));
        } catch (FileNotFoundException e) {
            System.out.println("### " + CONNECTION_DATA_FILENAME + ": File not found.  Creating a new file and prefilling it with data.");
            prefillWithData();
            try {
                System.out.println("### You ARE merging, aren't you?");
                wifiConnectionTimeList.mergeFrom(context.openFileInput(CONNECTION_DATA_FILENAME));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            System.out.println("### Input stream error.");
            e.printStackTrace();
        }
        return wifiConnectionTimeList;
    }

    private void writeDataToMemory(WIFIConnectionTime.PersistentData data) throws IOException{
        data.writeTo(context.openFileOutput(CONNECTION_DATA_FILENAME, MODE_PRIVATE));
    }
}
