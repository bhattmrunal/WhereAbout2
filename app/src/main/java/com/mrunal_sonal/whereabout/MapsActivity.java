package com.mrunal_sonal.whereabout;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends ActionBarActivity implements LocationListener {

    public String myLatestLocation="0,0";
    ProgressDialog progress;
    ArrayList<Marker> trackingFriendsMarkers;
    ArrayList<Integer> trackingFriends;
    ArrayList<String> friendsNameList;
    ArrayList<CurrentlyTracking> friendsTracking;
    ArrayList<Friends> allFriends;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LocationManager locationManager;
    private String myPhoneNumber = "0000000000";
    private String myName;
    private Toolbar toolbar;
    private Marker myMarker;
    private LatLng myLastLocation;
    private String userPhoneNumber;
    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
            if (loc == null) {
                String[] locltlg = myLatestLocation.split(",");

                Toast.makeText(getApplicationContext(), locltlg[0] + "," + locltlg[1], Toast.LENGTH_SHORT).show();
                loc = new LatLng(Double.parseDouble(locltlg[0]), Double.parseDouble(locltlg[1]));
                myMarker.setPosition(loc);
            } else
                myMarker.setPosition(loc);
            //mMap.addMarker(new MarkerOptions().position(loc));
            if (mMap != null) {
                //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
                String latt = String.valueOf(location.getLatitude());
                String longi = String.valueOf(location.getLongitude());
                myLatestLocation = latt + "," + longi;
                if (isNetworkAvailable())
                    new updateMyLocationTask().execute(new ApiConnector());
                else
                    Toast.makeText(getApplicationContext(), "Cannot connect to internet. \n WhereAbout app needs internet connection to function.", Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.showOverflowMenu();
        setSupportActionBar(toolbar);
        setUpMapIfNeeded();
        friendsNameList = new ArrayList<>();
        trackingFriendsMarkers = new ArrayList<>();
        trackingFriends = new ArrayList<>();
        allFriends = new ArrayList<Friends>();
        friendsTracking = new ArrayList<CurrentlyTracking>();
        String[] ltlg = myLatestLocation.split(",");
        if (!isNetworkAvailable()) {
            buildAlertMessageNoInternet();
        } else {
            checkMyGPSNow();
            try {
                myLastLocation = new LatLng(Double.parseDouble(ltlg[0]), Double.parseDouble(ltlg[1]));
                myName = "You";
                myMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title(myName));
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            //--------------------- FAB setup --------------

            final FloatingActionsMenu fam = (FloatingActionsMenu) findViewById(R.id.multiple_actions);

            final FloatingActionButton actionB = (FloatingActionButton) findViewById(R.id.action_b);
            actionB.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                    fam.collapse();
                    showListOfFriends();
                }
            });

            ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
            drawable.getPaint().setColor(getResources().getColor(R.color.white));

            final FloatingActionButton actionA = (FloatingActionButton) findViewById(R.id.action_a);
            actionA.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent act1 = new Intent(getApplicationContext(), MyActivity.class);
                    act1.putExtra("myPhone", myPhoneNumber);
                    startActivity(act1);
                    fam.collapse();
                }
            });

            final FloatingActionButton actionC = (FloatingActionButton) findViewById(R.id.action_c);
            actionC.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                    System.exit(0);
                    //finish();

                }
            });
            final FloatingActionButton actionD = (FloatingActionButton) findViewById(R.id.action_d);
            actionD.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                    System.exit(0);
                    //finish();

                }
            });
            // --------------------- End FAB setup --------------

            TelephonyManager telephoneManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            userPhoneNumber = myPhoneNumber = telephoneManager.getLine1Number();
            myPhoneNumber = ((myPhoneNumber.length() >= 10) ? myPhoneNumber.substring(myPhoneNumber.length() - 10) : "0000000000");

            // Get the location manager
            progress = new ProgressDialog(this);
            progress.setTitle("Loading Data");
            progress.setMessage("Please wait ...");
            progress.show();
        new GetMyUserDataTask().execute(new ApiConnector());
        new GetMyFriendsDataTask().execute(new ApiConnector());
            try {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                Criteria criteria = new Criteria();
                String provider = locationManager.getBestProvider(criteria, false);
                Log.i("Provider: ", provider);
                mMap.setMyLocationEnabled(true);
                Location location = locationManager.getLastKnownLocation(provider);
                if (location != null) {
                    BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.blue_pin);
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    myLatestLocation = String.valueOf(lat) + "," + String.valueOf(lng);
                    // myMarker= mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(myName));
                    myMarker.setPosition(new LatLng(lat, lng));
                    myMarker.setIcon(icon);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 15));
                    myMarker.showInfoWindow();
                } else {
                    String[] lltllg = myLatestLocation.split(",");
                    myLastLocation = new LatLng(Double.parseDouble(lltllg[0]), Double.parseDouble(lltllg[0]));
                }
                if(isNetworkAvailable()) {
                    new GetMyUserDataTask().execute(new ApiConnector());
                    new GetMyFriendsDataTask().execute(new ApiConnector());
                }
                else
                  Toast.makeText(getApplicationContext(),"Cannot connect to internet. \n WhereAbout app needs internet connection to function.",Toast.LENGTH_LONG).show();

            } catch (Exception e) {
                Log.e("Exception: ", e.getMessage());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mInflater= getMenuInflater();

        mInflater.inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId())
        {
            case R.id.action_settings:
                Intent settingsIntnt= new Intent(getApplicationContext(),SettingsActivity.class);
                startActivity(settingsIntnt);
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    public void onLocationChanged(Location location) {
        LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
        myMarker.setPosition(loc);
        if(isNetworkAvailable())
          new updateMyLocationTask().execute(new ApiConnector());
        else
        {
            Toast.makeText(getApplicationContext(),"Cannot connect to internet. \n WhereAbout app needs internet connection to function.",Toast.LENGTH_LONG).show();
        }
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 15));
    }


//-----------------   Check methods-----------------------------------------------
// ------------========================================================------------
    public void checkMyGPSNow() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }
        private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, WhereAbout cannot get your location. " +
                "Enable your GPS?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        System.exit(0);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }
    private void buildAlertMessageNoInternet() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("No internet connection found. WhereAbout needs internet connection to work ")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        System.exit(0);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();

    }


   //==============================================================================================================
    //=============================================================================================================

    @Override
    protected void onResume() {
        super.onResume();
        if(isNetworkAvailable())
         new GetMyFriendsDataTask().execute(new ApiConnector());
        else
            Toast.makeText(getApplicationContext(),"Cannot connect to internet. \n WhereAbout app needs internet connection to function.",Toast.LENGTH_LONG).show();
        setUpMapIfNeeded();

        try {
            String friendPhone= getIntent().getStringExtra("PhoneNoFromFriendsList");
            if(progress.isShowing())
                progress.dismiss();

        }
        catch (Exception e)
        {
            Log.e("Exception: ",e.getMessage());
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
                mMap.setOnMyLocationChangeListener(myLocationChangeListener);
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        // mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    private int findFriend(int ID) {
        for (int i = 0; i < trackingFriends.size(); i++) {
            if (trackingFriends.get(i) == ID) {
                return trackingFriends.get(i);
            }
        }
        return -1;
    }
//-------------------------------------------------------------------------------------------------

    private void showListOfFriends() {
        try {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);
            LayoutInflater inflater = getLayoutInflater();
            View convertView = inflater.inflate(R.layout.friends_list, null);
            alertDialog.setView(convertView);
            alertDialog.setTitle("Friends!!");
            ListView FriendsListView = (ListView) convertView.findViewById(R.id.friendsListView);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, friendsNameList);
            FriendsListView.setAdapter(adapter);
            final AlertDialog alert = alertDialog.create();

            FriendsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                BitmapDescriptor iconn;

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position,
                                        long id) {

                    CurrentlyTracking ct;
                    boolean trackingFound = false;
                    for (int i = 0; i < trackingFriends.size() && trackingFound == false; i++) /*trackingFriends.size()*/ {
                        if (trackingFriends.get(i) == position)
                            trackingFound = true;
                    }
                    if (!trackingFound) {

                        trackingFriends.add(Integer.valueOf(position));
                        String strLoc = allFriends.get(position).getLocation();

                        String[] Tdat = strLoc.split(",");
                        iconn = BitmapDescriptorFactory.fromResource(R.drawable.pink_pin);
                        LatLng currentLoc = new LatLng(Double.parseDouble(Tdat[0]), Double.parseDouble(Tdat[1]));
                        trackingFriendsMarkers.add(mMap.addMarker(new MarkerOptions().position(currentLoc).title(allFriends.get(position).getName())));
                        trackingFriendsMarkers.get(trackingFriendsMarkers.size() - 1).setIcon(iconn);
                        trackingFriendsMarkers.get(trackingFriendsMarkers.size() - 1).showInfoWindow();
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 15));

                        TrackFriendsTask(60000, 10000, position);

                        alert.dismiss();
                    } else {
                        String strLoc = allFriends.get(position).getLocation();

                        String[] Tdat = strLoc.split(",");
                        LatLng currentLoc = new LatLng(Double.parseDouble(Tdat[0]), Double.parseDouble(Tdat[1]));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 15));
                        alert.dismiss();

                    }
                }
            });
            alert.show();
        } catch (Exception e) {
            // Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT);
        }
    }

    public void TrackFriendsTask(long totalSharingTime, long tick, int cursor) {
        final int cur = cursor;

        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        if (isNetworkAvailable())
                            new getUserLocationTask(cur).execute(new ApiConnector());
                        else
                            Toast.makeText(getApplicationContext(), "Cannot connect to internet. \n WhereAbout app needs internet connection to function.", Toast.LENGTH_LONG).show();
                        //Toast.makeText(getApplicationContext(),"In Timer",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
        timer.schedule(task, 0, 15000);
    }

    // This is Async task, which will update the loaction separately of the main thread
    private class updateMyLocationTask extends AsyncTask<ApiConnector, Long, JSONArray>
    {
        @Override
        protected JSONArray doInBackground(ApiConnector... params) {

            // it is executed on Background thread

            return params[0].updateMyLocation(myPhoneNumber, myLatestLocation);
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {

        }
    }

    // This is Async task to find if the user is registered
    private class GetMyUserDataTask extends AsyncTask<ApiConnector, Long, JSONArray>
    {
        @Override
        protected JSONArray doInBackground(ApiConnector... params) {

            // it is executed on Background thread

            return params[0].GetMyUserData(myPhoneNumber);
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {

            try {
                if (jsonArray != null) {
                    JSONObject json = jsonArray.getJSONObject(0);

                    //Toast.makeText(getApplicationContext(),json.getString("Name"), Toast.LENGTH_LONG).show();
                    String name = json.getString("Name");

                    SharedPreferences pref = getSharedPreferences("register", Context.MODE_PRIVATE);
                    //SharedPreferences.Editor editr=pref.edit();

                    if (pref.getString("myname", null) == null) {
                        if (!name.equals("-1#+1")) {
                            myName = json.getString("Name");
                            myLatestLocation = json.getString("Lastlocation");
                            String[] ltlg = myLatestLocation.split(",");
                            LatLng latlang = new LatLng(Double.parseDouble(ltlg[0]), Double.parseDouble(ltlg[1]));
                            myMarker.setPosition(latlang);
                            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.blue_pin);
                            myMarker.setIcon(icon);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlang, 15));
                            Log.i("Test: ", myName);
                        } else {

                            if (isNetworkAvailable()) {
                                Intent myIntent = new Intent(getApplicationContext(), Register.class);
                                myIntent.putExtra("myPhone", myPhoneNumber);
                                startActivityForResult(myIntent, 0);
                            } else {
                                buildAlertMessageNoInternet();
                            }
                        }
                    }
                }
            } catch (Exception e)
            {
                Log.e("Exception:", e.getMessage());
                //Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();

            }
        }
    }

    private class GetMyFriendsDataTask extends AsyncTask<ApiConnector, Long, JSONArray>
    {
        @Override
        protected JSONArray doInBackground(ApiConnector... params) {

            // it is executed on Background thread

            return params[0].GetMyFriends(myPhoneNumber);
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {

            if (jsonArray != null) {
                JSONObject json;

                try {
                    allFriends.clear();
                    friendsNameList.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        json = jsonArray.getJSONObject(i);

                        String name = json.getString("Name");
                        String receivedPhonenumber = json.getString("Phonenumber");
                        receivedPhonenumber = receivedPhonenumber.substring(
                                receivedPhonenumber.length() - 10, receivedPhonenumber.length());
                        if (!name.equals("-1#+1")) {
                            if (!receivedPhonenumber.equals(myPhoneNumber)) {
                                Friends f = new Friends();
                                f.setId(json.getLong("ID"));
                                f.setPhonenumber(receivedPhonenumber);
                                f.setName(name);
                                f.setLocation(json.getString("Lastlocation"));
                                allFriends.add(f);
                                friendsNameList.add(name + "\n" + json.getString("Phonenumber"));
                            }

                        } else {

                            Toast.makeText(getApplicationContext(), "You have no friends in your list yet. Please add friends to your list using Add Friends button", Toast.LENGTH_LONG);
                        }
                    }
                    progress.dismiss();
                } catch (Exception e) {
                    //Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private class getUserLocationTask extends AsyncTask<ApiConnector, Long, JSONArray> {
        int cursor;

        public getUserLocationTask(int position) {
            this.cursor = position;
        }

        @Override
        protected JSONArray doInBackground(ApiConnector... params) {

            // it is executed on Background thread

            return params[0].getUserLocation(allFriends.get(cursor).getPhonenumber());
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {

            if (jsonArray != null) {
                JSONObject json;

                try {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        json = jsonArray.getJSONObject(i);

                        String name = json.getString("Name");
                        if (!name.equals("-1#+1")) {
                            if (cursor != -1) {
                                String[] Tdat = json.getString("Lastlocation").split(",");
                                LatLng currentLoc = new LatLng(Double.parseDouble(Tdat[0]), Double.parseDouble(Tdat[1]));
                                allFriends.get(cursor).setLocation(json.getString("Lastlocation"));
                                int correctMarkerPos = findFriend(cursor);
                                trackingFriendsMarkers.get(correctMarkerPos).setPosition(currentLoc);
                                trackingFriendsMarkers.get(correctMarkerPos).setTitle(name);
                                trackingFriendsMarkers.get(correctMarkerPos).showInfoWindow();
                                //Toast.makeText(getApplicationContext(), "Marker updated for: " + json.getString("Name"), Toast.LENGTH_LONG);
                            }

                        } else {

                            Toast.makeText(getApplicationContext(), "You have no friends in your list yet. Please add friends to your list using Add Friends button", Toast.LENGTH_LONG);
                        }
                    }
                    progress.dismiss();
                } catch (Exception e) {
                    // Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        }


    }


}