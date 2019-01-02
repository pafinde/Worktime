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

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EntriesEditor extends AppCompatActivity {

    private static final String TAG = "EntriesEditor";

    private List<String> listDataHeader;
    private HashMap<String, List<String>> listHash;

    private float x1,x2;
    private long startClickTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);

        ExpandableListView listView = findViewById(R.id.lvExp);

        prepareHashmap();

        ExpandableListAdapter listAdapter = new ExpandableListAdapter(this, listDataHeader, listHash);
        listView.setAdapter(listAdapter);
    }
/**
    @ Override
    public boolean onTouchEvent(MotionEvent event)
    {
        switch(event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                startClickTime = Calendar.getInstance().getTimeInMillis();
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                x2 = event.getX();
                float deltaX = x2 - x1;
                if (Math.abs(deltaX) > MIN_DISTANCE && clickDuration < MAX_SWIPE_TIME)
                {
                    System.out.println("### Swiped right2left.");

                    Intent intent = new Intent(this, EntriesEditor.class);
                    startActivity(intent);
                }
                else
                {
                    // consider as something else - a screen tap for example
                }
                break;
        }
        return super.onTouchEvent(event);
    }
//*/

    private String changeSecondsToFormat(long seconds){
        return seconds/(60*60) + "h " + (seconds%(60*60))/60 + "min " + seconds%60 + "s";
    }

    private void prepareHashmap(){ // works only if there are 12 unique months; there is no collision: Year1 Month1 <-> Year2 Month1
        FileManipulationsPersistentData fm = new FileManipulationsPersistentData();
        fm.setContext(getApplication());
        WIFIConnectionTime.PersistentData wifiData = fm.getEntries();

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
            int sumOfSecs = wifiData.getDay(i).getTickerSeconds() + FileManipulationsPersistentData.InSeconds(wifiData.getDay(i));
            m[repetition].add(wifiData.getDay(i).getDay() + "/" + tempMonth + "/" + wifiData.getDay(i).getYear() + "  -  " + changeSecondsToFormat(sumOfSecs));
        }
    }
}
