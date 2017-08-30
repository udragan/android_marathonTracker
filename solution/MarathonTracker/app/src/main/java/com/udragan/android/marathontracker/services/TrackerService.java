package com.udragan.android.marathontracker.services;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.udragan.android.marathontracker.MainActivity;
import com.udragan.android.marathontracker.R;
import com.udragan.android.marathontracker.helpers.GeofenceErrorHelper;
import com.udragan.android.marathontracker.infrastructure.Toaster;
import com.udragan.android.marathontracker.infrastructure.interfaces.IService;

//import static com.udragan.android.marathontracker.MainActivity.REQUEST_CODE_MAIN_ACTIVITY_NOTIFICATION;

/**
 * A background service for managing the geofencing client.
 */
public class TrackerService extends Service
        implements IService {

    // members **********************************************************************************************************

    private static final String TAG = TrackerService.class.getSimpleName();
    private static final int REQUEST_CODE_GEOFENCE_INTENT_SERVICE = REQUEST_CODE_BASE + 2;

    private NotificationManager mNotificationManager;
    private GeofencingClient mGeofencingClient;
    private PendingIntent mGeofenceIntentServicePendingIntent;

    private OnCompleteListener<Void> mAddRemoveGeofencesListener;

    // constructors *****************************************************************************************************

    /**
     * Initializes a new instance of {@link com.udragan.android.marathontracker.services.TrackerService} class.
     */
    public TrackerService() {
        Log.d(TAG, "constructor.");
        defineListeners();
    }

    // overrides ********************************************************************************************************

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStartCommand.");

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mGeofencingClient = LocationServices.getGeofencingClient(TrackerService.this);

        // TODO: implement consistent permission checks!
        int permission = ActivityCompat.checkSelfPermission(TrackerService.this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission == PackageManager.PERMISSION_GRANTED) {
            mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencingPendingIntent())
                    .addOnCompleteListener(mAddRemoveGeofencesListener);
            sendStickyNotification();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mGeofencingClient.removeGeofences(getGeofencingPendingIntent())
                .addOnCompleteListener(mAddRemoveGeofencesListener);
        mGeofencingClient = null;
        //TODO: cancel only if it is started
        cancelStickyNotification();
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
                    Toaster.showShort(TrackerService.this, R.string.toast_add_geofence_successful);
                    Log.d(TAG, "Add geofence successful.");
                } else {
                    Toaster.showLong(TrackerService.this, R.string.toast_add_geofence_failed);
                    String errorMessage = GeofenceErrorHelper.getErrorString(TrackerService.this, task.getException());
                    Log.w(TAG, errorMessage);
                }
            }
        };
    }

    @NonNull
    private GeofencingRequest getGeofencingRequest() {
        // TODO: hardcoded geofence for testing, 53.219193, 6.567972
        // will be provided from mainActivity upon starting the service/turning on the tracker switch.
        // transitionTypes and initialTrigger will be refined.
        Geofence geofence = new Geofence.Builder()
                .setRequestId("testGeofenceId")
                .setCircularRegion(53.219193, 6.567972, 100)
                .setExpirationDuration(2 * 60 * 1000)
                .setTransitionTypes(GeofencingRequest.INITIAL_TRIGGER_ENTER |
                        GeofencingRequest.INITIAL_TRIGGER_EXIT)
                .build();
        GeofencingRequest.Builder requestBuilder = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence);

        return requestBuilder.build();
    }

    private PendingIntent getGeofencingPendingIntent() {
        if (mGeofenceIntentServicePendingIntent != null) {
            return mGeofenceIntentServicePendingIntent;
        }

        Intent intent = new Intent(TrackerService.this, GeofenceIntentService.class);

        mGeofenceIntentServicePendingIntent = PendingIntent.getService(TrackerService.this,
                REQUEST_CODE_GEOFENCE_INTENT_SERVICE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        return mGeofenceIntentServicePendingIntent;
    }

    private void sendStickyNotification() {
        Intent mainActivityIntent = new Intent(TrackerService.this, MainActivity.class);
        PendingIntent mainActivityPendingIntent = PendingIntent.getActivity(TrackerService.this,
                MainActivity.REQUEST_CODE_MAIN_ACTIVITY_NOTIFICATION,
                mainActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(TrackerService.this)
                .setContentTitle("Marathon Tracker")
                .setContentText("service running")
                .setContentIntent(mainActivityPendingIntent)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setOngoing(true)
                .setAutoCancel(false);

        mNotificationManager.notify(MainActivity.REQUEST_CODE_MAIN_ACTIVITY_NOTIFICATION,
                notificationBuilder.build());
    }

    private void cancelStickyNotification() {
        mNotificationManager.cancel(MainActivity.REQUEST_CODE_MAIN_ACTIVITY_NOTIFICATION);
    }
}
