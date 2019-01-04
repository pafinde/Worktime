package com.example.mfind.timetracker;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

public class DebugScreen extends AppCompatActivity {

    boolean mBoundedReceiver;
    NetworkStateCheck mServerReceiver;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_screen);
        this.context = this;
        mBoundedReceiver = false;
    }

    @Override
    protected void onStart(){
        super.onStart();
        Intent mIntentSR = new Intent(this, NetworkStateCheck.class);
        bindService(mIntentSR, mConnectionToReceiver, BIND_AUTO_CREATE);
    }

    ServiceConnection mConnectionToReceiver = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NetworkStateCheck.LocalBinder mLocalBinder = (NetworkStateCheck.LocalBinder)service;
            mServerReceiver = mLocalBinder.getServerInstance();
            mBoundedReceiver = true;

            TextView t;
            t = findViewById(R.id.serviceStartTime);
            t.setText(mServerReceiver.getStartTime());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBoundedReceiver = false;
            mServerReceiver = null;
        }
    };
}
