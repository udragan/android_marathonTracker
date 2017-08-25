package com.udragan.android.marathontracker.services;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.udragan.android.marathontracker.R;
import com.udragan.android.marathontracker.helpers.GeofenceErrorHelper;
import com.udragan.android.marathontracker.infrastructure.Toaster;

/**
 * A background service for managing the geofencing client.
 */
public class TrackerService extends Service {

    // members **********************************************************************************************************

    private static final String TAG = TrackerService.class.getSimpleName();
    private static final int REQUEST_CODE_GEOFENCE_INTENT_SERVICE = 2001;

    private Context mContext;
    private GeofencingClient mGeofencingClient;

    private OnCompleteListener<Void> mAddRemoveGeofencesListener;

    // constructors *****************************************************************************************************

    /**
     * Initializes a new instance of {@link com.udragan.android.marathontracker.services.TrackerService} class.
     */
    public TrackerService() {
        Log.d(TAG, "constructor.");
        mContext = TrackerService.this;

        defineListeners();
    }

    // overrides ********************************************************************************************************

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStartCommand.");

        mGeofencingClient = LocationServices.getGeofencingClient(TrackerService.this);

        // TODO: implement consistent permission checks!
        int permission = ActivityCompat.checkSelfPermission(TrackerService.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission == PackageManager.PERMISSION_GRANTED) {
            mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencingPendingIntent())
                    .addOnCompleteListener(mAddRemoveGeofencesListener);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mGeofencingClient.removeGeofences(getGeofencingPendingIntent());
        mGeofencingClient = null;

        Log.d(TAG, "onDestroy.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // private methods **************************************************************************************************

    private void defineListeners() {
        mAddRemoveGeofencesListener = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toaster.showShort(mContext, R.string.toast_add_geofence_successful);
                    Log.d(TAG, "Add geofence successful.");
                } else {
                    Toaster.showLong(mContext, R.string.toast_add_geofence_failed);
                    String errorMessage = GeofenceErrorHelper.getErrorString(mContext, task.getException());
                    Log.w(TAG, errorMessage);
                }
            }
        };
    }

    @NonNull
    private GeofencingRequest getGeofencingRequest() {
        // TODO: hardcoded geofence for testing, 53.2182, 6.57861
        // will be provided from mainActivity upon starting the service/turning on the tracker switch.
        // transitionTypes and initialTrigger will be refined.
        Geofence geofence = new Geofence.Builder()
                .setRequestId("testGeofenceId")
                .setCircularRegion(53.2182, 6.57861, 50)
                .setExpirationDuration(60 * 60 * 1000)
                .setTransitionTypes(GeofencingRequest.INITIAL_TRIGGER_ENTER |
                        GeofencingRequest.INITIAL_TRIGGER_EXIT)
                .build();
        GeofencingRequest.Builder requestBuilder = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER |
                        GeofencingRequest.INITIAL_TRIGGER_EXIT |
                        GeofencingRequest.INITIAL_TRIGGER_DWELL)
                .addGeofence(geofence);

        return requestBuilder.build();
    }

    private PendingIntent getGeofencingPendingIntent() {
        Intent intent = new Intent(TrackerService.this, GeofenceIntentService.class);

        return PendingIntent.getService(TrackerService.this,
                REQUEST_CODE_GEOFENCE_INTENT_SERVICE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
