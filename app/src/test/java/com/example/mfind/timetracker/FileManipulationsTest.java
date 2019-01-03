package com.example.mfind.timetracker;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

class FileManipulationsForTesting extends FileManipulationsPersistentData {

    @Override
    protected void updateValuesForReading(){
    }

    protected void updateValuesForReading(WIFIConnectionTime.PersistentData wifiData){
        average7 = average30 = average90 = 0;
        int notZeroDaysCount7 = 0, notZeroDaysCount30 = 0, notZeroDaysCount90 = 0;
        /// this is for detecting days that actually are after a gap in
        /// PersistentData (should never happen, but checking anyways)
        LocalDate localDate7DaysAgo  = LocalDate.now().minusDays(7);
        LocalDate localDate30DaysAgo = LocalDate.now().minusDays(30);
        LocalDate localDate90DaysAgo = LocalDate.now().minusDays(90);

        for(int i = 1; i < wifiData.getDayCount(); i++){
            WIFIConnectionTime.Day day = wifiData.getDay(i);
            int temp = day.getTickerSeconds() + InSeconds(day);
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
        todayTicker = (wifiData.getDayCount() == 0 ? 0 : wifiData.getDay(0).getTickerSeconds());
    }
}

public class FileManipulationsTest {

    WIFIConnectionTime.PersistentData.Builder wifiData;

    @Test public void get7DayAverageWhenNoDayHasTicker() {
        wifiData = WIFIConnectionTime.PersistentData.newBuilder();
        FileManipulationsForTesting fm = new FileManipulationsForTesting();
        fm.updateValuesForReading(wifiData.build());
        assertEquals(0, fm.get7DayAverage());
    }
    @Test public void get7DayAverageWhenOnlyDay0HasTicker() {
        wifiData = WIFIConnectionTime.PersistentData.newBuilder();
        FileManipulationsForTesting fm = new FileManipulationsForTesting();
        clearAndFillWithEmptyDays();
        addDay(0, 100);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(0, fm.get7DayAverage());
    }
    @Test public void get7DayAverageWhenOnlyDay1HasTicker() {
        wifiData = WIFIConnectionTime.PersistentData.newBuilder();
        FileManipulationsForTesting fm = new FileManipulationsForTesting();
        clearAndFillWithEmptyDays();
        addDay(1, 100);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(100, fm.get7DayAverage());
    }
    @Test public void get7DayAverageWhenOnlyDay7HasTicker() {
        wifiData = WIFIConnectionTime.PersistentData.newBuilder();
        FileManipulationsForTesting fm = new FileManipulationsForTesting();
        clearAndFillWithEmptyDays();
        addDay(7, 100);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(100, fm.get7DayAverage());
    }
    @Test public void get7DayAverageWhenOnlyDay8HasTicker() {
        wifiData = WIFIConnectionTime.PersistentData.newBuilder();
        FileManipulationsForTesting fm = new FileManipulationsForTesting();
        clearAndFillWithEmptyDays();
        addDay(8, 100);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(0, fm.get7DayAverage());
    }
    @Test public void get7DayAverageGeneralGroupTesting() {
        wifiData = WIFIConnectionTime.PersistentData.newBuilder();
        FileManipulationsForTesting fm = new FileManipulationsForTesting();
        clearAndFillWithEmptyDays();

        addDay(0, 100);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(0, fm.get7DayAverage());
        addDay(1, 1);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(1, fm.get7DayAverage());

        addDay(7, 5);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(3, fm.get7DayAverage());

        addDay(8, 9);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(3, fm.get7DayAverage());
    }

    @Test public void get30DayAverageWhenNoDayHasTicker() {
        wifiData = WIFIConnectionTime.PersistentData.newBuilder();
        FileManipulationsForTesting fm = new FileManipulationsForTesting();
        fm.updateValuesForReading(wifiData.build());
        assertEquals(0, fm.get30DayAverage());
    }
    @Test public void get30DayAverageWhenOnlyDay0HasTicker() {
        wifiData = WIFIConnectionTime.PersistentData.newBuilder();
        FileManipulationsForTesting fm = new FileManipulationsForTesting();
        clearAndFillWithEmptyDays();
        addDay(0, 100);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(0, fm.get30DayAverage());
    }
    @Test public void get30DayAverageWhenOnlyDay1HasTicker() {
        wifiData = WIFIConnectionTime.PersistentData.newBuilder();
        FileManipulationsForTesting fm = new FileManipulationsForTesting();
        clearAndFillWithEmptyDays();
        addDay(1, 100);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(100, fm.get30DayAverage());
    }
    @Test public void get30DayAverageWhenOnlyDay30HasTicker() {
        wifiData = WIFIConnectionTime.PersistentData.newBuilder();
        FileManipulationsForTesting fm = new FileManipulationsForTesting();
        clearAndFillWithEmptyDays();
        addDay(30, 100);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(100, fm.get30DayAverage());
    }
    @Test public void get30DayAverageWhenOnlyDay31HasTicker() {
        wifiData = WIFIConnectionTime.PersistentData.newBuilder();
        FileManipulationsForTesting fm = new FileManipulationsForTesting();
        clearAndFillWithEmptyDays();
        addDay(31, 100);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(0, fm.get30DayAverage());
    }
    @Test public void get30DayAverageGeneralGroupTesting() {
        wifiData = WIFIConnectionTime.PersistentData.newBuilder();

        FileManipulationsForTesting fm = new FileManipulationsForTesting();
        fm.updateValuesForReading(wifiData.build());
        assertEquals(0, fm.get30DayAverage());

        clearAndFillWithEmptyDays();
        addDay(0, 100);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(0, fm.get30DayAverage());
        addDay(1, 1);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(1, fm.get30DayAverage());

        addDay(7, 5);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(3, fm.get30DayAverage());

        addDay(8, 9);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(5, fm.get30DayAverage());

        addDay(30, 9);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(6, fm.get30DayAverage());

        addDay(31, 36);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(6, fm.get30DayAverage());
    }

    @Test public void get90DayAverageWhenNoDayHasTicker() {
        wifiData = WIFIConnectionTime.PersistentData.newBuilder();
        FileManipulationsForTesting fm = new FileManipulationsForTesting();
        fm.updateValuesForReading(wifiData.build());
        assertEquals(0, fm.get90DayAverage());
    }
    @Test public void get90DayAverageWhenOnlyDay0HasTicker() {
        wifiData = WIFIConnectionTime.PersistentData.newBuilder();
        FileManipulationsForTesting fm = new FileManipulationsForTesting();
        clearAndFillWithEmptyDays();
        addDay(0, 100);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(0, fm.get90DayAverage());
    }
    @Test public void get90DayAverageWhenOnlyDay1HasTicker() {
        wifiData = WIFIConnectionTime.PersistentData.newBuilder();
        FileManipulationsForTesting fm = new FileManipulationsForTesting();
        clearAndFillWithEmptyDays();
        addDay(1, 100);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(100, fm.get90DayAverage());
    }
    @Test public void get90DayAverageWhenOnlyDay90HasTicker() {
        wifiData = WIFIConnectionTime.PersistentData.newBuilder();
        FileManipulationsForTesting fm = new FileManipulationsForTesting();
        clearAndFillWithEmptyDays();
        addDay(90, 100);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(100, fm.get90DayAverage());
    }
    @Test public void get90DayAverageWhenOnlyDay91HasTicker() {
        wifiData = WIFIConnectionTime.PersistentData.newBuilder();
        FileManipulationsForTesting fm = new FileManipulationsForTesting();
        clearAndFillWithEmptyDays();
        addDay(91, 100);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(0, fm.get90DayAverage());
    }
    @Test public void get90DayAverageGeneralGroupTesting() {
        wifiData = WIFIConnectionTime.PersistentData.newBuilder();

        FileManipulationsForTesting fm = new FileManipulationsForTesting();
        fm.updateValuesForReading(wifiData.build());
        assertEquals(0, fm.get90DayAverage());

        clearAndFillWithEmptyDays();

        addDay(0, 100);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(0, fm.get90DayAverage());

        addDay(1, 1);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(1, fm.get90DayAverage());

        addDay(7, 5);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(3, fm.get90DayAverage());

        addDay(8, 9);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(5, fm.get90DayAverage());

        addDay(30, 9);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(6, fm.get90DayAverage());

        addDay(31, 36);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(12, fm.get90DayAverage());

        addDay(90, 6);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(11, fm.get90DayAverage());

        addDay(91, 1000);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(11, fm.get90DayAverage());
    }

    @Test public void getGeneralDayAverageGeneralGroupTesting() {
        wifiData = WIFIConnectionTime.PersistentData.newBuilder();

        FileManipulationsForTesting fm = new FileManipulationsForTesting();
        fm.updateValuesForReading(wifiData.build());
        assertEquals(fm.get7DayAverage(), fm.get30DayAverage());
        assertEquals(fm.get7DayAverage(), fm.get90DayAverage());

        clearAndFillWithEmptyDays();
        addDay(0, 100);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(fm.get7DayAverage(), fm.get30DayAverage());
        assertEquals(fm.get7DayAverage(), fm.get90DayAverage());

        addDay(1, 1);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(fm.get7DayAverage(), fm.get30DayAverage());
        assertEquals(fm.get7DayAverage(), fm.get90DayAverage());

        addDay(7, 5);
        fm.updateValuesForReading(wifiData.build());
        assertEquals(fm.get7DayAverage(), fm.get30DayAverage());
        assertEquals(fm.get7DayAverage(), fm.get90DayAverage());

        addDay(8, 9);
        fm.updateValuesForReading(wifiData.build());
        assertNotEquals(fm.get7DayAverage(), fm.get30DayAverage());
        assertEquals(fm.get30DayAverage(), fm.get90DayAverage());

        addDay(30, 9);
        fm.updateValuesForReading(wifiData.build());
        assertNotEquals(fm.get7DayAverage(), fm.get30DayAverage());
        assertEquals(fm.get30DayAverage(), fm.get90DayAverage());

        addDay(31, 36);
        fm.updateValuesForReading(wifiData.build());
        assertNotEquals(fm.get7DayAverage(), fm.get30DayAverage());
        assertNotEquals(fm.get30DayAverage(), fm.get90DayAverage());

        addDay(90, 6);
        fm.updateValuesForReading(wifiData.build());
        assertNotEquals(fm.get7DayAverage(), fm.get30DayAverage());
        assertNotEquals(fm.get30DayAverage(), fm.get90DayAverage());

        addDay(91, 1000);
        fm.updateValuesForReading(wifiData.build());
        assertNotEquals(fm.get7DayAverage(), fm.get30DayAverage());
        assertNotEquals(fm.get30DayAverage(), fm.get90DayAverage());

    }

    private void clearAndFillWithEmptyDays(){
        WIFIConnectionTime.Day.Builder day = WIFIConnectionTime.Day.newBuilder();
        LocalDate ld = LocalDate.now().minusDays(100);
        day.setYear(ld.getYear());
        day.setMonth(ld.getMonthValue());
        day.setDay(ld.getDayOfMonth());
        day.setTickerSeconds(0);

        wifiData.clear();
        for(int i = 0; i < 92; i++){
            wifiData.addDay(i, day);
        }
    }

    private void addDay(int daysBefore, int ticker){
        WIFIConnectionTime.Day.Builder day = WIFIConnectionTime.Day.newBuilder();
        LocalDate ldForTest = LocalDate.now().minusDays(daysBefore);

        day.setYear(ldForTest.getYear());
        day.setMonth(ldForTest.getMonthValue());
        day.setDay(ldForTest.getDayOfMonth());
        day.setTickerSeconds(ticker);

        wifiData.setDay(daysBefore, day);
    }

    private void addDayAtIndex(int index, int daysBefore, int ticker){
        WIFIConnectionTime.Day.Builder day = WIFIConnectionTime.Day.newBuilder();
        LocalDate ldForTest = LocalDate.now().minusDays(daysBefore);

        day.setYear(ldForTest.getYear());
        day.setMonth(ldForTest.getMonthValue());
        day.setDay(ldForTest.getDayOfMonth());
        day.setTickerSeconds(ticker);

        wifiData.setDay(daysBefore, day);
    }
}