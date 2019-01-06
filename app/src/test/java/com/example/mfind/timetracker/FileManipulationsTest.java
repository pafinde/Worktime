package com.example.mfind.timetracker;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class FileManipulationsTest {

    private TimeProto.TimeData.Builder wifiData;

    @Test public void testEmptyValues() {
        wifiData = TimeProto.TimeData.newBuilder();
        FileManipulationsPersistentData fm = new FileManipulationsPersistentData();
        fm.updateAverageValues(wifiData.build());
        assertEquals(0, fm.get7DayAverage());
        assertEquals(0, fm.get30DayAverage());
        assertEquals(0, fm.get90DayAverage());
    }
    @Test public void testOnlyDay0HasTicker() {
        wifiData = TimeProto.TimeData.newBuilder();
        FileManipulationsPersistentData fm = new FileManipulationsPersistentData();
        clearAndFillWithEmptyDays();
        addDay(0, 100);
        fm.updateAverageValues(wifiData.build());
        assertEquals(0, fm.get7DayAverage());
        assertEquals(0, fm.get30DayAverage());
        assertEquals(0, fm.get90DayAverage());
    }
    @Test public void testDay1HasTicker() {
        wifiData = TimeProto.TimeData.newBuilder();
        FileManipulationsPersistentData fm = new FileManipulationsPersistentData();
        clearAndFillWithEmptyDays();
        addDay(1, 100);
        fm.updateAverageValues(wifiData.build());
        assertEquals(100, fm.get7DayAverage());
        assertEquals(100, fm.get30DayAverage());
        assertEquals(100, fm.get90DayAverage());
    }
    @Test public void testDay7Bounday() {
        wifiData = TimeProto.TimeData.newBuilder();
        FileManipulationsPersistentData fm = new FileManipulationsPersistentData();
        clearAndFillWithEmptyDays();
        addDay(7, 100);
        addDay(8, 200);
        fm.updateAverageValues(wifiData.build());
        assertEquals(100, fm.get7DayAverage());
    }
    @Test public void testDay30Boundary() {
        wifiData = TimeProto.TimeData.newBuilder();
        FileManipulationsPersistentData fm = new FileManipulationsPersistentData();
        clearAndFillWithEmptyDays();
        addDay(30, 100);
        addDay(31, 200);
        fm.updateAverageValues(wifiData.build());
        assertEquals(100, fm.get30DayAverage());
    }
    @Test public void testDay90Boundary() {
        wifiData = TimeProto.TimeData.newBuilder();
        FileManipulationsPersistentData fm = new FileManipulationsPersistentData();
        clearAndFillWithEmptyDays();
        addDay(90, 100);
        addDay(91, 200);
        fm.updateAverageValues(wifiData.build());
        assertEquals(100, fm.get90DayAverage());
    }
    @Test public void testDay8HasTicker() {
        wifiData = TimeProto.TimeData.newBuilder();
        FileManipulationsPersistentData fm = new FileManipulationsPersistentData();
        clearAndFillWithEmptyDays();
        addDay(8, 100);
        fm.updateAverageValues(wifiData.build());
        assertEquals(0, fm.get7DayAverage());
        assertEquals(100, fm.get30DayAverage());
        assertEquals(100, fm.get90DayAverage());
    }
    @Test public void testDay31HasTicker() {
        wifiData = TimeProto.TimeData.newBuilder();
        FileManipulationsPersistentData fm = new FileManipulationsPersistentData();
        clearAndFillWithEmptyDays();
        addDay(31, 100);
        fm.updateAverageValues(wifiData.build());
        assertEquals(0, fm.get7DayAverage());
        assertEquals(0, fm.get30DayAverage());
        assertEquals(100, fm.get90DayAverage());
    }
    @Test public void testDay91HasTicker() {
        wifiData = TimeProto.TimeData.newBuilder();
        FileManipulationsPersistentData fm = new FileManipulationsPersistentData();
        clearAndFillWithEmptyDays();
        addDay(91, 100);
        fm.updateAverageValues(wifiData.build());
        assertEquals(0, fm.get7DayAverage());
        assertEquals(0, fm.get30DayAverage());
        assertEquals(0, fm.get90DayAverage());
    }
    @Test public void test7DayAverageGeneralGroupTesting() {
        wifiData = TimeProto.TimeData.newBuilder();
        FileManipulationsPersistentData fm = new FileManipulationsPersistentData();
        clearAndFillWithEmptyDays();

        addDay(0, 100);
        fm.updateAverageValues(wifiData.build());
        assertEquals(0, fm.get7DayAverage());

        addDay(1, 100);
        fm.updateAverageValues(wifiData.build());
        assertEquals(100, fm.get7DayAverage());

        addDay(7, 500);
        fm.updateAverageValues(wifiData.build());
        assertEquals(300, fm.get7DayAverage());

        addDay(8, 900);
        fm.updateAverageValues(wifiData.build());
        assertEquals(300, fm.get7DayAverage());
    }

    @Test public void test30DayAverageGeneralGroupTesting() {
        wifiData = TimeProto.TimeData.newBuilder();

        FileManipulationsPersistentData fm = new FileManipulationsPersistentData();
        fm.updateAverageValues(wifiData.build());
        assertEquals(0, fm.get30DayAverage());

        clearAndFillWithEmptyDays();
        addDay(0, 100);
        fm.updateAverageValues(wifiData.build());
        assertEquals(0, fm.get30DayAverage());
        addDay(1, 100);
        fm.updateAverageValues(wifiData.build());
        assertEquals(100, fm.get30DayAverage());

        addDay(7, 500);
        fm.updateAverageValues(wifiData.build());
        assertEquals(300, fm.get30DayAverage());

        addDay(8, 900);
        fm.updateAverageValues(wifiData.build());
        assertEquals(500, fm.get30DayAverage());

        addDay(30, 900);
        fm.updateAverageValues(wifiData.build());
        assertEquals(600, fm.get30DayAverage());

        addDay(31, 3600);
        fm.updateAverageValues(wifiData.build());
        assertEquals(600, fm.get30DayAverage());
    }

    @Test public void test90DayAverageGeneralGroupTesting() {
        wifiData = TimeProto.TimeData.newBuilder();

        FileManipulationsPersistentData fm = new FileManipulationsPersistentData();
        fm.updateAverageValues(wifiData.build());
        assertEquals(0, fm.get90DayAverage());

        clearAndFillWithEmptyDays();

        addDay(0, 100);
        fm.updateAverageValues(wifiData.build());
        assertEquals(0, fm.get90DayAverage());

        addDay(1, 100);
        fm.updateAverageValues(wifiData.build());
        assertEquals(100, fm.get90DayAverage());

        addDay(7, 500);
        fm.updateAverageValues(wifiData.build());
        assertEquals(300, fm.get90DayAverage());

        addDay(8, 900);
        fm.updateAverageValues(wifiData.build());
        assertEquals(500, fm.get90DayAverage());

        addDay(30, 900);
        fm.updateAverageValues(wifiData.build());
        assertEquals(600, fm.get90DayAverage());

        addDay(31, 3600);
        fm.updateAverageValues(wifiData.build());
        assertEquals(1200, fm.get90DayAverage());

        addDay(90, 600);
        fm.updateAverageValues(wifiData.build());
        assertEquals(1100, fm.get90DayAverage());

        addDay(91, 100000);
        fm.updateAverageValues(wifiData.build());
        assertEquals(1100, fm.get90DayAverage());
    }

    @Test public void testGeneralDayAverageGeneralGroupTesting() {
        wifiData = TimeProto.TimeData.newBuilder();

        FileManipulationsPersistentData fm = new FileManipulationsPersistentData();
        fm.updateAverageValues(wifiData.build());
        assertEquals(fm.get7DayAverage(), fm.get30DayAverage());
        assertEquals(fm.get7DayAverage(), fm.get90DayAverage());

        clearAndFillWithEmptyDays();
        addDay(0, 100);
        fm.updateAverageValues(wifiData.build());
        assertEquals(fm.get7DayAverage(), fm.get30DayAverage());
        assertEquals(fm.get7DayAverage(), fm.get90DayAverage());

        addDay(1, 100);
        fm.updateAverageValues(wifiData.build());
        assertEquals(fm.get7DayAverage(), fm.get30DayAverage());
        assertEquals(fm.get7DayAverage(), fm.get90DayAverage());

        addDay(7, 500);
        fm.updateAverageValues(wifiData.build());
        assertEquals(fm.get7DayAverage(), fm.get30DayAverage());
        assertEquals(fm.get7DayAverage(), fm.get90DayAverage());

        addDay(8, 900);
        fm.updateAverageValues(wifiData.build());
        assertNotEquals(fm.get7DayAverage(), fm.get30DayAverage());
        assertEquals(fm.get30DayAverage(), fm.get90DayAverage());

        addDay(30, 900);
        fm.updateAverageValues(wifiData.build());
        assertNotEquals(fm.get7DayAverage(), fm.get30DayAverage());
        assertEquals(fm.get30DayAverage(), fm.get90DayAverage());

        addDay(31, 3600);
        fm.updateAverageValues(wifiData.build());
        assertNotEquals(fm.get7DayAverage(), fm.get30DayAverage());
        assertNotEquals(fm.get30DayAverage(), fm.get90DayAverage());

        addDay(90, 600);
        fm.updateAverageValues(wifiData.build());
        assertNotEquals(fm.get7DayAverage(), fm.get30DayAverage());
        assertNotEquals(fm.get30DayAverage(), fm.get90DayAverage());

        addDay(91, 100000);
        fm.updateAverageValues(wifiData.build());
        assertNotEquals(fm.get7DayAverage(), fm.get30DayAverage());
        assertNotEquals(fm.get30DayAverage(), fm.get90DayAverage());

    }

    private void clearAndFillWithEmptyDays(){
        TimeProto.Day.Builder day = TimeProto.Day.newBuilder();
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
        TimeProto.Day.Builder day = TimeProto.Day.newBuilder();
        LocalDate ldForTest = LocalDate.now().minusDays(daysBefore);

        day.setYear(ldForTest.getYear());
        day.setMonth(ldForTest.getMonthValue());
        day.setDay(ldForTest.getDayOfMonth());
        day.setTickerSeconds(ticker);

        wifiData.setDay(daysBefore, day);
    }

    private void addDayAtIndex(int index, int daysBefore, int ticker){
        TimeProto.Day.Builder day = TimeProto.Day.newBuilder();
        LocalDate ldForTest = LocalDate.now().minusDays(daysBefore);

        day.setYear(ldForTest.getYear());
        day.setMonth(ldForTest.getMonthValue());
        day.setDay(ldForTest.getDayOfMonth());
        day.setTickerSeconds(ticker);

        wifiData.setDay(daysBefore, day);
    }
}