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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EntriesEditor extends AppCompatActivity {

    private List<String> listDataHeader;
    private HashMap<String, List<String>> listHash;

    private Context context = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);

        ExpandableListView listView = findViewById(R.id.lvExp);

        prepareHashmap();

        ExpandableListAdapter listAdapter = new ExpandableListAdapter(context, listDataHeader, listHash);
        listView.setAdapter(listAdapter);
    }

    private String changeSecondsToFormat(long seconds){
        return seconds/(60*60) + "h " + (seconds%(60*60))/60 + "min " + seconds%60 + "s";
    }

    private void prepareHashmap(){ // works only if there are 12 unique months; there is no collision: Year1 Month1 <-> Year2 Month1
        FileManipulationsPersistentData fm = new FileManipulationsPersistentData();
        fm.setContext(getApplication());
        TimeProto.TimeData wifiData = fm.getEntries();

        listDataHeader = new ArrayList<>();
        listHash = new HashMap<>();

        List<String>[] m = new List[5];
        for (int i = 0; i < 5; i++) {
            m[i] = new ArrayList<>();
        }

        int tempMonth = -1;
        int repetition = -1;
        for(int i = 0; i < wifiData.getDayCount(); i++){
            if(tempMonth == -1 || tempMonth != wifiData.getDay(i).getMonth()){
                tempMonth = wifiData.getDay(i).getMonth();
                listDataHeader.add("Month nr.: " + tempMonth);
                repetition++;
                listHash.put(listDataHeader.get(repetition), m[repetition]);
                //System.out.println("### -> " + listHash);
            }
            int sumOfSecs = wifiData.getDay(i).getTickerSeconds() + FileManipulationsPersistentData.inSeconds(wifiData.getDay(i));
            m[repetition].add(wifiData.getDay(i).getDay() + "/" + tempMonth + "/" + wifiData.getDay(i).getYear() + "  -  " + changeSecondsToFormat(sumOfSecs));
        }
    }
}