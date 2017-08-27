package com.udragan.android.marathontracker.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.GeofencingEvent;
import com.udragan.android.marathontracker.helpers.GeofenceErrorHelper;
import com.udragan.android.marathontracker.infrastructure.interfaces.IService;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class GeofenceIntentService extends IntentService
        implements IService {

    // members **********************************************************************************************************

    private static final String TAG = GeofenceIntentService.class.getSimpleName();
    private static final String SERVICE_NAME = "GeofenceIntentService";

    private Context mContext;

    // constructors *****************************************************************************************************

    /**
     * Initializes a new instance of {@link com.udragan.android.marathontracker.services.GeofenceIntentService} class.
     */
    public GeofenceIntentService() {
        super(SERVICE_NAME);
        mContext = GeofenceIntentService.this;
    }

    // overrides ********************************************************************************************************

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "OnHandleIntent triggered.");

        if (intent != null) {
            handleGeofenceTransition(intent);
        }

        Log.d(TAG, "OnHandleIntent finishing.");
    }

    // private methods **************************************************************************************************

    private void handleGeofenceTransition(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            String errorMsg = GeofenceErrorHelper.getErrorString(mContext, geofencingEvent.getErrorCode());
            Log.e(TAG, errorMsg);

            return;
        }

        String errorMsg = GeofenceErrorHelper.getErrorString(mContext, geofencingEvent.getErrorCode());

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        //TODO: update db
        // and send/update sticky notification


    }
}
