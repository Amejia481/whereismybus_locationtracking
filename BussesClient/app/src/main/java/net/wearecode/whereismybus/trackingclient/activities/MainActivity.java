package net.wearecode.whereismybus.trackingclient.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import net.wearecode.whereismybus.trackingclient.BuildConfig;
import net.wearecode.whereismybus.trackingclient.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    int RC_SIGN_IN = 10;
    private static String lastUpdateLocationDate;
    @BindView(R.id.status_text)    TextView mStatusText;
    @BindView(R.id.status_image)  ImageView mStatusImage;
    FirebaseDatabase database;
    DatabaseReference myRef;
    String selectedRoute;
    private ChildEventListener mChildEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();

        mChildEventListener =  new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //Updating the last time that we sent a location
                if(dataSnapshot.getKey().equals("created")){
                    String lastTimeLocationUpdated = DateUtils.getRelativeDateTimeString(MainActivity.this,
                            dataSnapshot.getValue(Long.class),
                            DateUtils.SECOND_IN_MILLIS,
                            DateUtils.WEEK_IN_MILLIS,0
                    ).toString();

                    //Updating UI
                    lastUpdateLocationDate = lastTimeLocationUpdated;
                    mStatusText.setText(getString(R.string.running_status).replace("#",lastTimeLocationUpdated));

                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        selectedRoute =  sharedPref.getString("SELECTED_ROUTE",null);

        //Checking if the user is logged in
        if (mAuth.getCurrentUser() != null) {

            //Checking if the update location service is running
            boolean isServiceRunning =  sharedPref.getBoolean("UPDATE_LOCATION_RUNNING",false);


            if(isServiceRunning){

                mStatusImage.setImageDrawable( ContextCompat.getDrawable(this, R.drawable.ok));
                if(lastUpdateLocationDate == null){
                    mStatusText.setText(getString(R.string.running));
                }
            }else{
                mStatusImage.setImageDrawable( ContextCompat.getDrawable(this, R.drawable.bad));
                mStatusText.setText(getString(R.string.message_location_off));
            }

                //Checking if the user set the default route
                if(selectedRoute == null) {
                     mStatusText.setText(R.string.message_select_route);

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.message_select_route)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                                }
                            } ).create().show();

                }


        } else {
            //Launching Log in UI
            startActivityForResult(
                    AuthUI.getInstance(FirebaseApp.getInstance())
                            .createSignInIntentBuilder()
                            .setProviders( AuthUI.EMAIL_PROVIDER
                            )
                            .build(),RC_SIGN_IN);
        }



        if(mAuth.getCurrentUser() != null && selectedRoute !=null) {
            //Adding listener to get update data from the server
            myRef.child(BuildConfig.FIREBASE_BUSSES_NODE).child(selectedRoute).addChildEventListener(mChildEventListener );
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        //Removing listener from Firebase updates
        myRef.removeEventListener(mChildEventListener);
    }
}
