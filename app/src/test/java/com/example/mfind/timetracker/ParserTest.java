package com.example.mfind.timetracker;


import org.junit.Test;

import java.text.ParseException;

import static com.example.mfind.timetracker.ExpandableListAdapter.parseUserInput;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ParserTest {

    @Test public void checkAddingNull() {
        try {
            parseUserInput(null);
            assertTrue("exception expected", false);
        } catch (ParseException | NullPointerException e) {
            // OK
        }
    }
    @Test public void checkAddingEmpty() {
        try {
            parseUserInput("");
            assertTrue("exception expected", false);
        } catch (ParseException e) {
            // OK
        }
    }
    @Test public void checkAdding0() {
        try {
            parseUserInput("0");
            assertTrue("exception expected", false);
        } catch (ParseException e) {
            // OK
        }
    }
    @Test public void checkAdding0m() throws ParseException {
        int test = parseUserInput("0m");
        assertEquals(0, test);
    }
    @Test public void checkAddingMinus2m() {
        try {
            parseUserInput("-2m");
            assertTrue("exception expected", false);
        } catch (ParseException e) {
            // OK
        }
    }
    @Test public void checkAddingMinusPT2m() throws ParseException {
            int test = parseUserInput("-PT2m");
            assertEquals(-2, test);
    }
    @Test public void checkAddingMinusP2m() {
        try {
            parseUserInput("-P2m");
            assertTrue("exception expected", false);
        } catch (ParseException e) {
            // OK
        }
    }
    @Test public void checkAddingMinusT2m() {
        try {
            parseUserInput("-T2m");
            assertTrue("exception expected", false);
        } catch (ParseException e) {
            // OK
        }
    }
    @Test public void checkAddingSpaceBeforeValues() throws ParseException {
        int test = parseUserInput("     1m");
        assertEquals(1, test);
    }
    @Test public void checkAddingSpaceInBetween2Digits() {
        try {
            parseUserInput("1 1");
            assertTrue("exception expected", false);
        } catch (ParseException e) {
            return;
        }
    }
    @Test public void checkIllegalChar() {
        try {
            parseUserInput("1s");
            assertTrue("exception expected", false);
        } catch (ParseException e) {
            return;
        }
    }

}
