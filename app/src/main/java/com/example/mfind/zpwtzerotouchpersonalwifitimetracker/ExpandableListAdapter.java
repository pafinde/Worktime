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

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class ExpandableListAdapter extends BaseExpandableListAdapter{
    private static final String TAG = "ExpandableListAdapter";

    private int entryindex;

    private Context context;
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
        }

        if (txtListChild != null) {
            txtListChild.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    entryindex = findIndexOfElemByView(v);

                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setTitle("Enter your edit (HOURS, MINUTES and SECONDS, in that order)");

                    LinearLayout layout = new LinearLayout(context);
                    layout.setOrientation(LinearLayout.VERTICAL);

                    final EditText inputTime = new EditText(context);
                    inputTime.setHint("([-]%dh %dmin %ds) or ([-]%d %d %d)");
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
                            System.out.println("### Edited entry with " + userInput + " and comment: " + userComment);
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

    private int findIndexOfElemByView(View v){
        TextView text = v.findViewById(R.id.lblListItem);
        String withoutNumbers = ((String)text.getText()).replaceAll( "[/\\-hmins]", " " );

        Scanner s = new Scanner(withoutNumbers);
        FileManipulationsPersistentData fm = new FileManipulationsPersistentData();
        fm.setContext(context); // i dont know if this will work
        int mDay = s.nextInt();
        int mMonth = s.nextInt();
        int mYear = s.nextInt();
        return fm.findIndexByDate(mYear, mMonth, mDay);
    }
    private void enterAnEdit(int index, String comment, String edit){
        int multiplier = edit.charAt(0)=='-' ? -1 : 1;
        int seconds = 0;
        edit = edit.replaceAll( "[-hmins]", " " );
        Scanner s = new Scanner(edit);
        seconds += s.nextInt()*60*60;
        seconds += s.nextInt()*60;
        seconds += s.nextInt();
        seconds *= multiplier;

        FileManipulationsPersistentData fm = new FileManipulationsPersistentData();
        fm.setContext(context); // i dont know if this will work

        fm.addEditEntry(index, comment, seconds);
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
