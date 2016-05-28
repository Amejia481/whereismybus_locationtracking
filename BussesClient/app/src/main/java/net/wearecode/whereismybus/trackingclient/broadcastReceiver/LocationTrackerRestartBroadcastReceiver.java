package net.wearecode.whereismybus.trackingclient.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;

import net.wearecode.whereismybus.trackingclient.services.UpdateLocationService;


public class LocationTrackerRestartBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        //This is call when the cellphone it's restarted
        //Starts the location service in background

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        if (mAuth.getCurrentUser() != null) {
            sharedPref.edit().putBoolean("UPDATE_LOCATION_RUNNING",true).apply();
            context.startService(new Intent(context,UpdateLocationService.class));
        } else {
            sharedPref.edit().putBoolean("UPDATE_LOCATION_RUNNING",false).apply();

        }
    }


}