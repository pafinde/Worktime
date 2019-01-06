package com.example.mfind.timetracker;


import org.junit.Test;

import java.text.ParseException;

import static com.example.mfind.timetracker.ExpandableListAdapter.parseUserInput;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ParserTest {

    private void expectParseException(String input){
        try {
            int result = parseUserInput(input);
            assertTrue("exception expected, got " + result, false);
        } catch (ParseException e) {
            // OK
        }
    }

    @Test public void testAddingEmpty() {
        expectParseException("");
        expectParseException(" ");
    }
    @Test public void testAdding0() throws ParseException {
        expectParseException("0");
        assertEquals(0, parseUserInput("0m"));
    }
    @Test public void testAdding0m() throws ParseException {
        int test = parseUserInput("0m");
        assertEquals(0, test);
    }
    @Test public void testAddingMinus2m() {
        expectParseException("-2m");
    }
    @Test public void testAddingMinusPT2m() throws ParseException {
            int test = parseUserInput("-PT2m");
            assertEquals(-2, test);
    }
    @Test public void testAddingMinusP2m() {
        expectParseException("-P2m");
    }
    @Test public void testAddingMinusT2m() {
        expectParseException("-T2m");
    }
    @Test public void testExtraSpace() throws ParseException {
        assertEquals(1, parseUserInput("     1m"));
        assertEquals(1, parseUserInput("1m     "));
        assertEquals(61, parseUserInput("1h  1m"));
    }
    @Test public void testSpaceBetweenDigits() {
        expectParseException("1 1m");
    }
    @Test public void testIllegalChar() {
        expectParseException("1s");
        expectParseException("1x");
        expectParseException("1h 1x");
    }
    @Test public void testExtraChar() {
        expectParseException("1hh");
    }
}
