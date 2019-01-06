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

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import javax.security.auth.login.LoginException;

import static java.lang.Character.isDigit;

/**
 * This is used to fill and manage entries as well as allows user to edit them
 * by adding an edit and a comment to specified day
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter{

    private static final String TAG = "ExpandableListAdapter";
    private int entryindex;

    protected Context context;
    private List<String> listDataHeader;
    private HashMap<String,List<String>> listHashMap;

    ExpandableListAdapter(Context context, List<String> listDataHeader, HashMap<String, List<String>> listHashMap) {
        this.context = context;
        this.listDataHeader = listDataHeader;
        this.listHashMap = listHashMap;
    }

    @Override
    public int getGroupCount() {
        return listDataHeader.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return Objects.requireNonNull(listHashMap.get(listDataHeader.get(i))).size();
    }

    @Override
    public Object getGroup(int i) {
        return listDataHeader.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return Objects.requireNonNull(listHashMap.get(listDataHeader.get(i))).get(i1); // i = Group Item , i1 = ChildItem
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        String headerTitle = (String)getGroup(i);
        if(view == null)
        {
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (inflater != null) {
                view = inflater.inflate(R.layout.list_group,null);
            }
        }
        TextView lblListHeader = null;
        if (view != null) {
            lblListHeader = view.findViewById(R.id.lblListHeader);
        }
        if (lblListHeader != null) {
            lblListHeader.setTypeface(null, Typeface.BOLD);
        }
        if (lblListHeader != null) {
            lblListHeader.setText(headerTitle);
        }
        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        final String childText = (String)getChild(i,i1);
        if(view == null)
        {
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (inflater != null) {
                view = inflater.inflate(R.layout.list_item,null);
            }
        }

        TextView txtListChild = null;
        if (view != null) {
            txtListChild = view.findViewById(R.id.lblListItem);
        }

        if (txtListChild != null) {
            txtListChild.setText(childText);

            txtListChild.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    entryindex = findIndexOfElemByView(v);

                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setTitle("Enter your edit (HOURS, MINUTES and SECONDS, in that order)");

                    LinearLayout layout = new LinearLayout(context);
                    layout.setOrientation(LinearLayout.VERTICAL);

                    final EditText inputTime = new EditText(context);
                    inputTime.setHint("[-PT][-]%dh [-]%dm");
                    layout.addView(inputTime); // Notice this is an add method

                    final EditText inputComment = new EditText(context);
                    inputComment.setHint("Description");
                    layout.addView(inputComment); // Another add method

                    dialog.setView(layout);

                    // Set up the buttons: positive - changer
                    dialog.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String userInput = inputTime.getText().toString();
                            String userComment = inputComment.getText().toString();
                            Log.i(TAG, "### onClick: Edited entry with " + userInput + " and comment: " + userComment);
                            enterAnEdit(entryindex, userComment, userInput);
                        }
                    });
                    // Set up the buttons: negative - no changer
                    dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    dialog.show();
                }
            });
        }

        return view;
    }

    /**
     * Finds element id using (sadly, I had no idea how to do this better) data actually saved in
     * a child text
     * @param v - view of clicked child
     * @return - returns index of the day that this View is representing
     */
    private int findIndexOfElemByView(View v){
        TextView text = v.findViewById(R.id.lblListItem);
        String withoutNumbers = ((String)text.getText()).replaceAll( "[/\\-hmins]", " " );

        Scanner s = new Scanner(withoutNumbers);
        FileManipulationsPersistentData fm = new FileManipulationsPersistentData();
        fm.setContext(context);
        int mDay = s.nextInt();
        int mMonth = s.nextInt();
        int mYear = s.nextInt();
        return fm.findIndexByDate(mYear, mMonth, mDay);
    }

    /**
     * performs check on user input to check if user input qualifies to be an edit
     * (comment is not(!) checked)
     *
     * If everything is correct, the edit is added to specified day (by index)
     * @param index - index of day to add edit to
     * @param comment - comment to add
     * @param edit - USER INPUT - time to add
     */
    protected int enterAnEdit(int index, String comment, String edit){
        edit = edit.toUpperCase().replaceAll("[^PT0123456789\\-HM]", " ");
        if(edit.matches(".*\\d\\s+\\d.*")) {
            Log.i(TAG, "### enterAnEdit: User input error! whitespace between digits: " + edit);
            errorHandler();
            return -1;
        }
        edit = edit.replaceAll("\\s", "");

        if(edit.length() >= 1 && edit.charAt(0) != '-' && !isDigit(edit.charAt(0))){
            Log.i(TAG, "### enterAnEdit: User input error! First char is neither digit nor '-': " + edit);
            errorHandler();
            return -1;
        }
        if(edit.length() >= 3 && edit.charAt(0) == '-' && (edit.charAt(1) != 'P' || edit.charAt(2) != 'T')){
            Log.i(TAG, "### enterAnEdit: User input error! '-' without 'PT': " + edit);
            errorHandler();
            return -1;
        }

        String toParse = "";
        if(edit.charAt(0) != '-' && edit.charAt(0) != 'P')
            toParse += "PT";
        toParse += edit;
        Log.d(TAG, "### enterAnEdit: Current String: " + toParse);

        int minutes = 0;
        try {
            minutes = (int) Duration.parse(toParse).toMinutes();
        }catch(DateTimeParseException e){
            Log.e(TAG, "### ### ### enterAnEdit: parse exception!");
            errorHandler();
            return -1;
        }
        if(minutes == 0){
            Toast.makeText(context, "Too little to add!", Toast.LENGTH_LONG).show();
            return 0;
        }

        FileManipulationsPersistentData fm = new FileManipulationsPersistentData();
        fm.setContext(context);

        if(fm.addEditEntry(index, comment, minutes)) {
            Log.i(TAG, "### enterAnEdit: EDIT: adding " + minutes + " minutes!");
            return minutes;
        }else {
            Toast.makeText(context, "Adding this edit would make this day negative! Not adding!", Toast.LENGTH_LONG).show();
            return -1;
        }
    }

    /**
     * Informs user that their input was incorrect
     */
    private void errorHandler(){
        Toast.makeText(context, "Sorry, but your input hasn't met the criteria!", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
