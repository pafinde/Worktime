package com.example.mfind.timetracker;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.junit.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;

import static java.lang.Character.isDigit;
import static org.junit.Assert.assertEquals;

class ExpandableListAdapterForTesting extends ExpandableListAdapter{
    private static final String TAG = "ExpandableListAdapterForTesting";
    
    ExpandableListAdapterForTesting(Context context, List<String> listDataHeader, HashMap<String, List<String>> listHashMap) {
        super(context, listDataHeader, listHashMap);
    }

    protected static int enterAnEdit(String edit) {
        edit = edit.toUpperCase().replaceAll("[^PT0123456789\\-HM]", " ");
        for (int i = 1; i < edit.length() - 1; i++) {
            if (edit.charAt(i) == ' ' && isLastCharADigit(edit.substring(0, i)) && isFirstCharADigit(edit.substring(i + 1))) {
                System.out.println("### enterAnEdit: User input error! at: \" + i + \" of: \" + edit");
                return -1;
            }
        }
        edit = edit.replaceAll("[ ]", "");

        if (edit.length() >= 1 && edit.charAt(0) != '-' && !isDigit(edit.charAt(0))) {
            System.out.println("### enterAnEdit: User input error! First char is neither digit nor '-': " + edit);
            return -1;
        }
        if (edit.length() >= 3 && edit.charAt(0) == '-' && (edit.charAt(1) != 'P' || edit.charAt(2) != 'T')) {
            System.out.println("### enterAnEdit: User input error! '-' without 'PT': " + edit);
            return -1;
        }
        if (edit.length() >= 2 && edit.charAt(0) == 'P' && edit.charAt(1) != 'T') {
            System.out.println("### enterAnEdit: User input error! 'P' without 'T': " + edit);
            return -1;
        }

        String toParse = "";
        if (edit.charAt(0) != '-' && edit.charAt(0) != 'P')
            toParse += "PT";
        toParse += edit;
        System.out.println("### enterAnEdit: Current String: " + toParse);

        int minutes;
        try {
            minutes = (int) Duration.parse(toParse).toMinutes();
        } catch (DateTimeParseException e) {
            System.out.println("### ### ### enterAnEdit: parse exception!");
            return -1;
        }

        return minutes;
    }
}

public class EditEntriesTest{

    @Test public void checkAdding0() {
        int test = ExpandableListAdapterForTesting.enterAnEdit("0");
        assertEquals(-1, test);
    }
    @Test public void checkAdding0m() {
        int test = ExpandableListAdapterForTesting.enterAnEdit("0m");
        assertEquals(0, test);
    }
    @Test public void checkAddingMinus2m() {
        int test = ExpandableListAdapterForTesting.enterAnEdit("-2m");
        assertEquals(-1, test);
    }
    @Test public void checkAddingMinusPT2m() {
        int test = ExpandableListAdapterForTesting.enterAnEdit("-PT2m");
        assertEquals(-2, test);
    }
    @Test public void checkAddingMinusP2m() {
        int test = ExpandableListAdapterForTesting.enterAnEdit("-P2m");
        assertEquals(-1, test);
    }
    @Test public void checkAddingMinusT2m() {
        int test = ExpandableListAdapterForTesting.enterAnEdit("-T2m");
        assertEquals(-1, test);
    }

}
