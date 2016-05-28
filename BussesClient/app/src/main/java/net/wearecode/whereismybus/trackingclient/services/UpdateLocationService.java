package net.wearecode.whereismybus.trackingclient.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import net.wearecode.whereismybus.trackingclient.BuildConfig;
import net.wearecode.whereismybus.trackingclient.R;
import net.wearecode.whereismybus.trackingclient.activities.SettingsActivity;

import java.util.HashMap;
import java.util.Map;


public class UpdateLocationService extends Service  implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener {
    private NotificationManager mNM;

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = 5606;
    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;
    protected static final String TAG = UpdateLocationService.class.getSimpleName();
    FirebaseDatabase database;
    DatabaseReference myRef;
    private static  Location lastLocation;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    FirebaseAuth.AuthStateListener mAuthListener;

    public static final float MAX_DISTANCE_DIFF_IN_METERS = 30f;

    @Override
    public void onCreate() {
        super.onCreate();
        String selectedRoute =  PreferenceManager.getDefaultSharedPreferences(this).getString("SELECTED_ROUTE",null);
        Map<String,Object> item2 = new HashMap<>();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);



        //Update the status of this route, because it's being use
        item2.put("active",true);
        myRef.child(BuildConfig.FIREBASE_BUSSES_NODE).child(selectedRoute).updateChildren(item2);

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();



        // Kick off the process of building a GoogleApiClient and requesting the LocationServices
        // API.
        buildGoogleApiClient();

        //Lister for authenticating events
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                     // User is signed out
                    UpdateLocationService.this.stopLocationUpdates();
                    UpdateLocationService.this.stopSelf();
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(UpdateLocationService.this);
                    sharedPref.edit().putBoolean("UPDATE_LOCATION_RUNNING",false);
                    Log.i(TAG, "user logged in");
                }
            }
        };
        Log.i(TAG, "onCreate");

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Connected to GoogleApiClient");
        startLocationUpdates();
    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String selectedRoute =  sharedPref.getString("SELECTED_ROUTE",null);


        if(lastLocation!=null) {

            //Checking if the location has changed, if not don't do anything
            if(lastLocation.distanceTo(location) < MAX_DISTANCE_DIFF_IN_METERS)
                return;
        }

        Map<String,Object> item = new HashMap<>();
        item.put("latitude",location.getLatitude());
        item.put("longitude",location.getLongitude());
        item.put("created",ServerValue.TIMESTAMP);

        //Updating the new location
        myRef.child(BuildConfig.FIREBASE_BUSSES_NODE).child(selectedRoute).updateChildren(item);
        lastLocation = location;
        Log.i(TAG, "onLocationChanged latitude:"+ location.getLatitude() + ", longitude:"+location.getLongitude());
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection onConnectionSuspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    public int onStartCommand (Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
      return START_STICKY;
    }


    protected void startLocationUpdates() {
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
        mGoogleApiClient.connect();

    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(UpdateLocationService.this);

        String selectedRoute =  sharedPref.getString("SELECTED_ROUTE",null);
        Map<String,Object> item2 = new HashMap<>();
        item2.put("active",false);

        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);

        myRef.child(BuildConfig.FIREBASE_BUSSES_NODE).child(selectedRoute).updateChildren(item2);
        FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        stopLocationUpdates();
        sharedPref.edit().putBoolean("UPDATE_LOCATION_RUNNING",false).apply();
        Log.i(TAG, "onDestroy");

    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.not_summary_on_position_sender);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, SettingsActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.app_name))  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .setOngoing(false)
                .build();

        notification.flags |= Notification.FLAG_NO_CLEAR;

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }

}
