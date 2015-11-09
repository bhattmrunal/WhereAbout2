package com.mrunal_sonal.whereabout;

/**
 * Created by mrunal on 11/1/15.
 */

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Sonal on 7/19/2015.
 */
public class GPSTracker extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {


    IBinder mBinder = new LocalBinder();
    SharedPreferences loadingData;
    private GoogleApiClient mLocationClient;
    private LocationRequest mLocationRequest;
    // Flag that indicates if a request is underway.
    private boolean mInProgress;
    private Boolean servicesAvailable = false;

    @Override
    public void onCreate() {
        super.onCreate();

        loadingData = getSharedPreferences("loadingData", Context.MODE_PRIVATE);

        mInProgress = false;
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();

        //Using PRIORITY_BALANCED_POWER_ACCURACY instead of High Accuracy. Because High Accuracy uses more battery
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(15000);
        servicesAvailable = servicesConnected();

            /*
             * Create a new location client, using the enclosing class to handle
             * callbacks.
             */

        mLocationClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

//        Intent intent = new Intent(this, MapsActivity.class);
//        PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);
//        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        alarm.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(),
//                1000 * 15, pintent);

    }

    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        return ConnectionResult.SUCCESS == resultCode;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (!servicesAvailable || mLocationClient.isConnected() || mInProgress)
            return START_STICKY;

        setUpLocationClientIfNeeded();
        if (!mLocationClient.isConnected() || !mLocationClient.isConnecting() && !mInProgress) {
            mInProgress = true;
            mLocationClient.connect();
        }

        return START_STICKY;
    }

    /*
     * Create a new location client, using the enclosing class to handle
     * callbacks.
     */
    private void setUpLocationClientIfNeeded() {
        if (mLocationClient == null)
            mLocationClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        // Turn off the request flag
        mInProgress = false;
        if (servicesAvailable && mLocationClient != null) {
            mLocationClient.disconnect();
            // Destroy the current location client
            mLocationClient = null;
        }
        super.onDestroy();
    }

    @Override
    public void onConnected(Bundle bundle) {

        // Request location updates using static settings
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mLocationClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mInProgress = false;

        /*
         * Google Play services can resolve some errors it detects. If the error
         * has a resolution, try sending an Intent to start a Google Play
         * services activity that can resolve error.
         */
        if (connectionResult.hasResolution()) {

            // If no resolution is available, display an error dialog
        } else {

        }

    }

    @Override
    public void onLocationChanged(Location location) {

        // Report to the UI that the location was updated
        String msg = Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Log.d("debug....", msg);
        Log.d("debug", "" + new SimpleDateFormat("MM/dd/yyyy hh:mm:ss").format(new Date(location.getTime())));

        SharedPreferences.Editor edit = loadingData.edit();
        edit.putString("latitude", location.getLatitude() + "");
        edit.putString("longitude", location.getLongitude() + "");
        edit.apply();

    }

    public class LocalBinder extends Binder {
        public GPSTracker getServerInstance() {
            return GPSTracker.this;
        }
    }

}