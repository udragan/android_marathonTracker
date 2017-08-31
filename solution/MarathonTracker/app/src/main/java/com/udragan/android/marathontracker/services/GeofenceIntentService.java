package com.udragan.android.marathontracker.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.GeofencingEvent;
import com.udragan.android.marathontracker.MainActivity;
import com.udragan.android.marathontracker.R;
import com.udragan.android.marathontracker.helpers.GeofenceErrorHelper;
import com.udragan.android.marathontracker.infrastructure.interfaces.IService;

import static com.udragan.android.marathontracker.MainActivity.REQUEST_CODE_MAIN_ACTIVITY_NOTIFICATION;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class GeofenceIntentService extends IntentService
        implements IService {

    // members **********************************************************************************************************

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
            String errorMsg = GeofenceErrorHelper.getErrorString(GeofenceIntentService.this,
                    geofencingEvent.getErrorCode());
            Log.e(TAG, errorMsg);

            return;
        }

        //TODO: update db

        updateNotification();
    }

    private void updateNotification() {
        Intent mainActivityIntent = new Intent(GeofenceIntentService.this,
                MainActivity.class);
        PendingIntent mainActivityPendingIntent = PendingIntent.getActivity(GeofenceIntentService.this,
                REQUEST_CODE_MAIN_ACTIVITY_NOTIFICATION,
                mainActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(GeofenceIntentService.this)
                .setContentTitle(getString(R.string.checkpoint_achieved))
                .setContentText(System.currentTimeMillis() + " : geofence triggered.")
                .setContentIntent(mainActivityPendingIntent)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setOngoing(true)
                .setAutoCancel(false);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(REQUEST_CODE_MAIN_ACTIVITY_NOTIFICATION,
                notificationBuilder.build());
    }
}
