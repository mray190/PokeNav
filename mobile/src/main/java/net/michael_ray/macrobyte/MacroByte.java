package net.michael_ray.macrobyte;

import android.app.Activity;
import android.content.Intent;
import android.support.multidex.MultiDexApplication;

import com.google.android.gms.maps.GoogleMap;

/**
 * Created by mray on 7/21/16.
 */
public class MacroByte extends MultiDexApplication {

    private Intent background_service;
    public GoogleMap map;
    private Activity currActivity;

    public MacroByte() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        background_service = new Intent(getBaseContext(), BackgroundTasks.class);
    }

    public void startLocationUpdates() {
        startService(background_service);
    }

    public void stopLocationUpdates() {
        stopService(background_service);
    }

    public void setCurrActivity(Activity currActivity) {
        this.currActivity = currActivity;
    }
}
