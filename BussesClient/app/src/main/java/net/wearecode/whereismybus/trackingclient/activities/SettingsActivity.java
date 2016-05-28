package net.wearecode.whereismybus.trackingclient.activities;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import net.wearecode.whereismybus.trackingclient.BuildConfig;
import net.wearecode.whereismybus.trackingclient.R;
import net.wearecode.whereismybus.trackingclient.services.UpdateLocationService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatPreferenceActivity {



    static String lastRoute;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        PrefsFragment mPrefsFragment = new PrefsFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, mPrefsFragment).commit();

    }



    public static class PrefsFragment extends PreferenceFragment {

        private static final String TAG = PrefsFragment.class.getSimpleName() ;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
           FirebaseDatabase  database = FirebaseDatabase.getInstance();
           final ListPreference mSelectedRoute = (ListPreference) findPreference("SELECTED_ROUTE");
           final DatabaseReference myRef = database.getReference();
           final SwitchPreference mPreLocationRunning = (SwitchPreference) findPreference("UPDATE_LOCATION_RUNNING");




            getPreferenceManager().setSharedPreferencesMode(Context.MODE_MULTI_PROCESS);
            Preference button = findPreference("prefLogout");
            lastRoute =  PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("SELECTED_ROUTE",null);
            //Getting the routes from Firebase database
            myRef.addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            ArrayList<String> buses = new ArrayList<>();
                            HashMap<String, Object> map =  ((HashMap<String,HashMap<String, Object>>) dataSnapshot.getValue()).get(BuildConfig.FIREBASE_BUSSES_NODE);
                            for (Map.Entry<String, Object> entry : map.entrySet())
                            {
                                if(!((Map<String ,Boolean>)entry.getValue()).get("active") || (lastRoute !=null && lastRoute.equals(entry.getKey())  )) {
                                    buses.add(entry.getKey());
                                }

                            }

                            CharSequence[] entries = buses.toArray(new String[buses.size()]);
                            CharSequence[] entryValues = buses.toArray(new String[buses.size()]);

                            //Updating UI
                            mSelectedRoute.setEntries(entries);
                            mSelectedRoute.setEntryValues(entryValues);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        }
                    });


            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                   //Log out  user
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    mAuth.signOut();
                    getActivity().finish();
                    Log.i(TAG, "Log out user");

                    return true;
                }
            });



            // Adding listener to Firebase database for each
            // time that the user change its actual route
            mSelectedRoute.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    Map<String,Object> item = new HashMap<>();
                    if(mPreLocationRunning.isChecked()) {
                        item.put("active", true);
                        myRef.child(BuildConfig.FIREBASE_BUSSES_NODE).child(newValue.toString()).updateChildren(item);
                    }
                    if(lastRoute !=null) {
                        Map<String,Object> item2 = new HashMap<>();
                        item2.put("active",false);
                        myRef.child(BuildConfig.FIREBASE_BUSSES_NODE).child(lastRoute).updateChildren(item2);
                    }
                    lastRoute = newValue.toString();
                    return true;
                }
            });

            //Listener for each time that the user switch on or off the
            //Location tracker
            mPreLocationRunning.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ListPreference mSelectedRoute = (ListPreference) findPreference("SELECTED_ROUTE");

                    if (mSelectedRoute.getValue() != null) {
                        if (mPreLocationRunning.isChecked()) {
                            //Starting the  service for location tracker in a new thread
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    getActivity().startService(new Intent(getActivity(), UpdateLocationService.class));
                                }
                            }).start();

                        } else {
                            //Shutting down the service for location tracker
                            getActivity().stopService(new Intent(getActivity(), UpdateLocationService.class));
                        }
                    } else {

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage(R.string.message_select_route)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        startActivity(new Intent(getActivity(), SettingsActivity.class));
                                    }
                                }).create().show();
                    }

                    return true;
                }
            });


        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //ActionBar's back button
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


}
