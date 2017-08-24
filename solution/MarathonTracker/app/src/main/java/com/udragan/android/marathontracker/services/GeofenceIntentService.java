package com.udragan.android.marathontracker.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class GeofenceIntentService extends IntentService {

    // members **********************************************************************************************************

    public static final String ACTION_REGISTER_GEOFENCES = "com.udragan.android.marathontracker.services.action.REGISTER_GEOFENCES";
    public static final String ACTION_UNREGISTER_GEOFENCES = "com.udragan.android.marathontracker.services.action.UNREGISTER_GEOFENCES";

    private static final String TAG = GeofenceIntentService.class.getSimpleName();
    private static final String SERVICE_NAME = "GeofenceIntentService";

    // constructors *****************************************************************************************************

    /**
     * Initializes a new instance of {@link com.udragan.android.marathontracker.services.GeofenceIntentService} class.
     */
    public GeofenceIntentService() {
        super(SERVICE_NAME);
    }

    // overrides ********************************************************************************************************

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "OnHandleIntent triggered");

        if (intent != null) {
            final String action = intent.getAction();
            Log.d(TAG, String.format("\t for action %s", action));

            switch (action) {
                case ACTION_REGISTER_GEOFENCES:
                    registerGeofences();
                    break;
                case ACTION_UNREGISTER_GEOFENCES:
                    unregisterGeofences();
            }
        }

        Log.d(TAG, "OnHandleIntent finishing.");
    }

    // public methods ***************************************************************************************************

    /**
     * Starts GeofenceIntentService to perform action Register Geofences with the given parameters.
     * If the service is already performing a task this action will be queued.
     */
    public static void startActionRegisterGeofences(Context context, String param1, String param2) {
        Intent intent = new Intent(context, GeofenceIntentService.class);
        intent.setAction(ACTION_REGISTER_GEOFENCES);
        //intent.putExtra(EXTRA_PARAM1, param1);
        //intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts GeofenceIntentService to perform action Unregister Geofence with the given parameters.
     * If the service is already performing a task this action will be queued.
     *
     * @param context the context to start the service.
     * @param param1
     * @param param2
     */
    public static void startActionUnregisterGeofences(Context context, String param1, String param2) {
        Intent intent = new Intent(context, GeofenceIntentService.class);
        intent.setAction(ACTION_UNREGISTER_GEOFENCES);

        context.startService(intent);
    }

    // private methods **************************************************************************************************

    private void registerGeofences() {
        Log.d(TAG, "\t\t registerGeofences called.");
        Log.d(TAG, "\t\t registering...");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "\t\t registering done.");
    }

    private void unregisterGeofences() {
        Log.d(TAG, "\t\t unregisterGeofences called.");
        Log.d(TAG, "\t\t unregistering...");

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "\t\t unregistering done.");
    }
}
