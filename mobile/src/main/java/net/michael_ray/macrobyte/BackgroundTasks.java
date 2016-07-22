package net.michael_ray.macrobyte;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.format.Formatter;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class BackgroundTasks extends Service implements GoogleApiClient.ConnectionCallbacks, LocationListener, GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient locationClient;
    private LocationRequest locationRequest;
    private Location myLocation;
    static final int START_LOCATION_LISTENER = 0;
    static final int STOP_LOCATION_LISTENER = 1;
    private SharedPreferences sharedPref;

    /**
     * Run when the service has been created but before the service is actually run
     */
    @Override
    public void onCreate() {
        super.onCreate();

        //Start the location tracker
        locationClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationClient!=null && locationRequest!=null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(locationClient, (com.google.android.gms.location.LocationListener) this);
            if (locationClient.isConnected()) {
                locationClient.disconnect();
            }
        }
    }
    @Override
    public void onConnected(Bundle bundle) {
        Log.i("FireNode", "Info - connected to location services");
        //Location request and handling
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(2000);
        ((MacroByte)getApplication()).myLocation = myLocation;
        myLocation = LocationServices.FusedLocationApi.getLastLocation(locationClient);
        if (myLocation==null) {
            myLocation = new Location("");
            myLocation.setLatitude(42.2912);
            myLocation.setLongitude(-83.7161);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(locationClient, locationRequest, this);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("MacroByte", "Error - connection to location services failed: " + connectionResult.toString());
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("MacroByte", "Debug - connection to location services suspended");
    }

    /**
     * Handler of incoming messages from clients (i.e. starting & stopping companion viewer)
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_LOCATION_LISTENER:
                    locationClient.connect();
                    break;
                case STOP_LOCATION_LISTENER:
                    locationClient.disconnect();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger messenger = new Messenger(new IncomingHandler());

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("MacroByte", "Info - Background Tasks service started");
        locationClient.connect();
        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    @Override
    public void onLocationChanged(Location location) {
        ((MacroByte)getApplication()).myLocation = location;
        this.myLocation = location;
    }
}