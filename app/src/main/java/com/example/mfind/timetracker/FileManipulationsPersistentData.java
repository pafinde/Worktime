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
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * This class is used by MainActivity and BroadcastReceiver Service
 * to get some values or update files in application persistent data proto.
 *
 * It's the only way of communicating with the actual class that does the operations on
 * persistent data proto
 */
public class FileManipulationsPersistentData extends Service {

    private static final String TAG = "FileManipulationsPersis";
    
    private String CONNECTION_DATA_FILENAME = "ZPWT_WifiConnectionDataList.bin";
    protected int todayTicker;
    protected int average7;
    protected int average30;
    protected int average90;
    protected boolean valuesInitialized = false;

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
        FileManipulationsPersistentData getServerInstance() {
            return FileManipulationsPersistentData.this;
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
     * invalidates already calculated values, so the next time someone asks for values we
     * actively need to refresh them. This allows to later return true values while
     * at the same time minimizing number of times that the calculation have to be done
     */
    public void invalidateInitialization(){
        valuesInitialized = false;
    }

    /**
     * @return - returns number of seconds we spend connected to watched wifi today
     */
    long getTodayTicker(){
        updateValuesForReadingIfNecessary();
        return todayTicker;
    }

    /**
     * (Updates values if needed)
     * @return - return average number of seconds spend connected from last 7 days
     */
    long get7DayAverage(){
        updateValuesForReadingIfNecessary();
        return average7;
    }

    /**
     * (Updates values if needed)
     * @return - return average number of seconds spend connected from last 30 days
     */
    long get30DayAverage(){
        updateValuesForReadingIfNecessary();
        return average30;
    }

    /**
     * (Updates values if needed)
     * @return - return average number of seconds spend connected from last 90 days
     */
    long get90DayAverage(){
        updateValuesForReadingIfNecessary();
        return average90;
    }

    /**
     * Used to add seconds to todays day, creating (prepending) it if needed
     * (if day doesn't already exist)
     *
     * @param secs - number of seconds to prepend
     * @return - returns today ticker
     */
    int prependTicker(int secs){
        if(secs != 0)
            Log.i(TAG, "### prependTicker: adding seconds: " + secs);

        /// we are prefilling protos with empty days for protos continuity
        TimeProto.TimeData.Builder wifiData = prependWithEmptyDays(readDataFromStorage());

        /// now our day(0) is for sure the current one
        TimeProto.Day day = wifiData.getDay(0);
        TimeProto.Day.Builder dayBuilder = copyDay(day);

        int todayTicker;

        todayTicker = day.getTickerSeconds();
        todayTicker += secs;
        dayBuilder.setTickerSeconds(todayTicker);
        wifiData.setDay(0, dayBuilder);  // SET day - means replace

        try {
            writeDataToStorage(wifiData.build());
        } catch (IOException e) {
            Log.d(TAG, "### ### ### prependTicker: output stream error!");
            e.printStackTrace();
        }
        return todayTicker;
    }

    /**
     * Finds index of a day by a days: year value, month value and day value
     *
     * @param year - year value of the day you are looking for
     * @param month - month value of the day you are looking for
     * @param day - day value of the day you are looking for
     * @return - returns index that should be used fast - in case of doing this close to midnight
     * value might become inaccurate quite fast
     */
    public int findIndexByDate(int year, int month, int day){
        TimeProto.TimeData wifiData = prependWithEmptyDays(readDataFromStorage()).build();
        int i = 0;
        for(; i < wifiData.getDayCount(); i++){
            if(year == wifiData.getDay(i).getYear())
                if(month == wifiData.getDay(i).getMonth())
                    if(day == wifiData.getDay(i).getDay())
                        break;
        }
        return i >= wifiData.getDayCount() ? -1 : i;
    }

    /**
     * Adds an edit to an day entry specified with index, consisting of:
     * comment and number of seconds (can be negative)
     *
     * @param index - index of the day to add edit to
     * @param comment - comment
     * @param minutes - number of seconds (can be negative)
     */
    public Boolean addEditEntry(int index, String comment, int minutes){
        TimeProto.TimeData.Builder wifiData = prependWithEmptyDays(readDataFromStorage());
        TimeProto.Day.Builder day = copyDay(wifiData.getDay(index));
        TimeProto.Day.Edit.Builder edit = TimeProto.Day.Edit.newBuilder();

        int temp = day.getTickerSeconds() + editSeconds(day.build()) + minutes * 60;
        if(temp < 0){
            return false;
        }

        edit.setMinuteOfDay(LocalTime.now().getHour() * 60 + LocalTime.now().getMinute());
        edit.setDeltaMinutes(minutes);
        edit.setComment(comment);
        day.addEdits(edit);

        wifiData.setDay(index, day);

        try {
            writeDataToStorage(wifiData.build());
        } catch (IOException e) {
            Log.d(TAG, "### ### ### addEditEntry: output stream error!");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private TimeProto.Day.Builder copyDay(TimeProto.Day message){
        TimeProto.Day.Builder builder = TimeProto.Day.newBuilder();
        builder.mergeFrom(message);
        return builder;
    }

    /**
     * Returns list of entries in application memory
     * @return - returns days with prepended empty days
     */
    public TimeProto.TimeData getEntries(){
        return prependWithEmptyDays(readDataFromStorage()).build();
    }

    /**
     * Prepends wifiData with empty days - is makes calculating averages easier
     *
     * @param wifiData - data to prepend some days to
     * @return - returns wifiData with prepended days
     */
    protected TimeProto.TimeData.Builder prependWithEmptyDays(TimeProto.TimeData.Builder wifiData){
        TimeProto.Day.Builder dayBuilder = TimeProto.Day.newBuilder();

        for(int i = 0;; i++){
            LocalDate dayDate = LocalDate.now().minusDays(i);
            TimeProto.Day dayData = wifiData.getDay(i);

            LocalDate tempDate = LocalDate.of(dayData.getYear(), dayData.getMonth(), dayData.getDay());

            if(dayDate.equals(tempDate))
                break;
            if(dayDate.isBefore(tempDate)) { // this means that we changed timezone backwards (our current day is lower than it once was before)
                // TODO NOT SAFE! We shoudl probably just leave this day as it is
                int temp = dayData.getTickerSeconds() + editSeconds(dayData);
                Toast.makeText(context, "Sorry, special case, removing newest day, deleted: " + temp + "s", Toast.LENGTH_LONG).show();
                wifiData.removeDay(0);
                continue;
            }
            dayBuilder.setYear(dayDate.getYear());
            dayBuilder.setMonth(dayDate.getMonthValue());
            dayBuilder.setDay(dayDate.getDayOfMonth());
            dayBuilder.setTickerSeconds(0);
            wifiData.addDay(i, dayBuilder);
        }

        return deleteExcessDays(wifiData);
    }

    /**
     * Removes excess days - days above 91 are NOT required to calculate averages
     *
     * @param wifiData - Builder consisting of all days, with possibly some excess days
     * @return - returns Builder without excess days
     */
    private TimeProto.TimeData.Builder deleteExcessDays(TimeProto.TimeData.Builder wifiData){
        while(wifiData.getDayCount() > 91)
            wifiData.removeDay(91);
        return wifiData;
    }

    /**
     * Runs updateAverageValues if necessary
     */
    protected void updateValuesForReadingIfNecessary(){
        if (valuesInitialized)
            return;

        TimeProto.TimeData wifiData = prependWithEmptyDays(readDataFromStorage()).build();
        updateAverageValues(wifiData);
    }

    /**
     * Calculates all the averages and todayTicker
     */
    void updateAverageValues(TimeProto.TimeData wifiData){
        average7 = average30 = average90 = 0;
        int notZeroDaysCount7 = 0, notZeroDaysCount30 = 0, notZeroDaysCount90 = 0;
        LocalDate today = LocalDate.now();
        LocalDate localDate7DaysAgo  = today.minusDays(7);
        LocalDate localDate30DaysAgo = today.minusDays(30);
        LocalDate localDate90DaysAgo = today.minusDays(90);

        for(TimeProto.Day day : wifiData.getDayList()){
            int temp = day.getTickerSeconds() + editSeconds(day);
            if(temp < 60) {
                // days shorter tham 1m do not count towards average
                continue;
            }
            LocalDate date = LocalDate.of(day.getYear(), day.getMonth(), day.getDay());
            if(date.equals(today)) {
                continue;
            }
            if(!localDate7DaysAgo.isAfter(date)){
                average7 += temp;
                notZeroDaysCount7++;
            }
            if(!localDate30DaysAgo.isAfter(date)){
                average30 += temp;
                notZeroDaysCount30++;
            }
            if(!localDate90DaysAgo.isAfter(date)){
                average90 += temp;
                notZeroDaysCount90++;
            }
        }

        average7  = (notZeroDaysCount7  != 0 ? (average7  / notZeroDaysCount7 ) : 0);
        average30 = (notZeroDaysCount30 != 0 ? (average30 / notZeroDaysCount30) : 0);
        average90 = (notZeroDaysCount90 != 0 ? (average90 / notZeroDaysCount90) : 0);
        todayTicker = wifiData.getDayCount() != 0 ? wifiData.getDay(0).getTickerSeconds() + editSeconds(wifiData.getDay(0)) : 0;

        valuesInitialized = true;
    }

    /**
     * Calculates number of seconds from all of the edits from a given Day
     *
     * @param day - Day to calculate sum of ticker edits on
     * @return - returns number of seconds
     */
    public static int editSeconds(final TimeProto.Day day){
        int sum = 0;
        for(TimeProto.Day.Edit e : day.getEditsList())
            sum += e.getDeltaMinutes();
        return sum * 60;
    }

    /**
     * Used only when first launching the application
     *
     * Creates empty proto with 91 empty days in it
     */
    private void prefillWithData(){
        TimeProto.TimeData.Builder TimeProtoList = TimeProto.TimeData.newBuilder();
        for(int i = 0; i <= 90; i++){
            TimeProto.Day.Builder day = TimeProto.Day.newBuilder();
            try {
                LocalDate localDateMinusDays = LocalDate.now().minusDays(i+1);
                day.setYear(localDateMinusDays.getYear());
                day.setMonth(localDateMinusDays.getMonthValue());
                day.setDay(localDateMinusDays.getDayOfMonth());
                day.setTickerSeconds(0);
            }catch(DateTimeException e){
                Log.e(TAG, "### prefillWithData: TIME WARP ERROR!");
                e.printStackTrace();
            }
            TimeProtoList.addDay(day);
        }
        try {
            writeDataToStorage(TimeProtoList.build());
        } catch (IOException e) {
            Log.d(TAG, "### ### ### prefillWithData: output stream error!");
            e.printStackTrace();
        }
    }

    /**
     * Reads TimeProto data from application storage
     *
     * @return - returns ready builder containing Days
     */
    protected TimeProto.TimeData.Builder readDataFromStorage(){
        TimeProto.TimeData.Builder TimeProtoList = TimeProto.TimeData.newBuilder();
        // Read the existing address book.
        try {
            TimeProtoList.mergeFrom(context.openFileInput(CONNECTION_DATA_FILENAME));
        } catch (FileNotFoundException e) {
            Log.d(TAG, "### readDataFromStorage: " + CONNECTION_DATA_FILENAME + ": File not found.  Creating a new file and prefilling it with data.");
            prefillWithData();
            try {
                Log.i(TAG, "### readDataFromStorage: merging");
                TimeProtoList.mergeFrom(context.openFileInput(CONNECTION_DATA_FILENAME));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            Log.e(TAG, "### ### ### readDataFromStorage: input stream error!");
            e.printStackTrace();
        }
        return TimeProtoList;
    }

    /**
     * Writes data to storage.
     *
     * @param data - data to write (overwrite!)
     * @throws IOException - when file couldn't be saved
     */
    private void writeDataToStorage(TimeProto.TimeData data) throws IOException{
        data.writeTo(context.openFileOutput(CONNECTION_DATA_FILENAME, MODE_PRIVATE));
    }
}
