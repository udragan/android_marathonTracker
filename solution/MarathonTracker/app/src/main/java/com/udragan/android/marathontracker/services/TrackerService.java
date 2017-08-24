package com.udragan.android.marathontracker.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;

/**
 * A background service for managing the geofencing client.
 */
public class TrackerService extends Service {

    // members **********************************************************************************************************

    private static final String TAG = TrackerService.class.getSimpleName();

    private GeofencingClient mGeofencingClient;

    // constructors *****************************************************************************************************

    /**
     * Initializes a new instance of {@link com.udragan.android.marathontracker.services.TrackerService} class.
     */
    public TrackerService() {
        Log.d(TAG, "TrackerService constructor.");
    }

    // overrides ********************************************************************************************************

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStartCommand");

        mGeofencingClient = LocationServices.getGeofencingClient(this);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mGeofencingClient = null;

        Log.d(TAG, "onDestroy.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
