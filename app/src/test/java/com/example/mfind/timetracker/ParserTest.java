package com.example.mfind.timetracker;


import org.junit.Test;

import java.text.ParseException;

import static com.example.mfind.timetracker.ExpandableListAdapter.parseUserInput;
import static org.junit.Assert.assertEquals;

public class ParserTest {

    @Test public void checkAddingNull() {
        try {
            parseUserInput(null);
        } catch (ParseException | NullPointerException e) {
            return;
        }
        assertEquals(0, 1);
    }
    @Test public void checkAddingEmpty() {
        try {
            parseUserInput("");
        } catch (ParseException e) {
            return;
        }
        assertEquals(0, 1);
    }
    @Test public void checkAdding0() {
        try {
            parseUserInput("0");
        } catch (ParseException e) {
            return;
        }
        assertEquals(0, 1);
    }
    @Test public void checkAdding0m() {
        int test = 0;
        try {
            test = parseUserInput("0m");
        } catch (ParseException e) {
            assertEquals(0, 1);
        }
        assertEquals(0, test);
    }
    @Test public void checkAddingMinus2m() {
        try {
            parseUserInput("-2m");
        } catch (ParseException e) {
            return;
        }
        assertEquals(0, 1);
    }
    @Test public void checkAddingMinusPT2m() {
        int test = 0;
        try {
            test = parseUserInput("-PT2m");
        } catch (ParseException e) {
            assertEquals(0, 1);
        }
        assertEquals(-2, test);
    }
    @Test public void checkAddingMinusP2m() {
        try {
            parseUserInput("-P2m");
        } catch (ParseException e) {
            return;
        }
        assertEquals(0, 1);
    }
    @Test public void checkAddingMinusT2m() {
        try {
            parseUserInput("-T2m");
        } catch (ParseException e) {
            return;
        }
        assertEquals(0, 1);
    }
    @Test public void checkAddingSpaceBeforeValues() {
        int test = 0;
        try {
            test = parseUserInput("     1m");
        } catch (ParseException e) {
            assertEquals(0, 1);
        }
        assertEquals(1, test);
    }
    @Test public void checkAddingSpaceInBetween2Digits() {
        try { parseUserInput("1 1");
        } catch (ParseException e) {
            return;
        }
        assertEquals(0, 1);
    }

}
