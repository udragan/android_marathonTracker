package com.udragan.android.marathontracker.services;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.PermissionChecker;
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

/**
 * A background service for managing the geofencing client.
 */
public class TrackerService extends Service
        implements IService {

    // members **********************************************************************************************************

    private static final String TAG = TrackerService.class.getSimpleName();
    private static final int REQUEST_CODE_GEOFENCE_INTENT_SERVICE = REQUEST_CODE_BASE + 2;

    private GeofencingClient mGeofencingClient;
    private PendingIntent mGeofenceIntentServicePendingIntent;
    private Notification mStickyNotification;

    private OnCompleteListener<Void> mAddGeofencesListener;
    private OnCompleteListener<Void> mRemoveGeofencesListener;

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
    public void onCreate() {
        super.onCreate();

        mGeofencingClient = LocationServices.getGeofencingClient(TrackerService.this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStartCommand.");

        // TODO: implement consistent permission checks!
        if (checkPermissions()) {
            Log.d(TAG, "Adding geofences...");
            //noinspection MissingPermission
            mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencingPendingIntent())
                    .addOnCompleteListener(mAddGeofencesListener);
            startForeground(startId, getStickyNotification());
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mGeofencingClient.removeGeofences(getGeofencingPendingIntent())
                .addOnCompleteListener(mRemoveGeofencesListener);
        mGeofencingClient = null;

        Log.d(TAG, "onDestroy.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // private methods **************************************************************************************************

    private void defineListeners() {
        mAddGeofencesListener = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toaster.showShort(TrackerService.this, R.string.toast_add_geofence_successful);
                    Log.d(TAG, "Add geofences successful.");
                } else {
                    Toaster.showLong(TrackerService.this, R.string.toast_add_geofence_failed);
                    String errorMessage = GeofenceErrorHelper.getErrorString(TrackerService.this, task.getException());
                    Log.w(TAG, errorMessage);
                }
            }
        };

        mRemoveGeofencesListener = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toaster.showShort(TrackerService.this, R.string.toast_remove_geofence_successful);
                    Log.d(TAG, "Remove geofences successful.");
                } else {
                    Toaster.showLong(TrackerService.this, R.string.toast_remove_geofence_failed);
                    String errorMessage = GeofenceErrorHelper.getErrorString(TrackerService.this, task.getException());
                    Log.w(TAG, errorMessage);
                }
            }
        };
    }

    private boolean checkPermissions() {
        Log.d(TAG, "Checking permissions...");
        return checkPermission(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private boolean checkPermission(String permission) {
        int permitted = PermissionChecker.checkSelfPermission(TrackerService.this, permission);
        Log.v(TAG, String.format("Checking permission '%s': %d",
                permission, permitted));

        return permitted == PackageManager.PERMISSION_GRANTED;
    }

    @NonNull
    private GeofencingRequest getGeofencingRequest() {
        // TODO: hardcoded geofence for testing, 37.4226, -122.084 ( Googleplex )
        // will be provided from mainActivity upon starting the service/turning on the tracker switch.
        // transitionTypes and initialTrigger will be refined.
        Geofence geofence = new Geofence.Builder()
                .setRequestId("testGeofenceId")
                .setCircularRegion(37.4226, -122.084, 100)
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

    private Notification getStickyNotification() {
        if (mStickyNotification != null) {
            return mStickyNotification;
        }

        Intent mainActivityIntent = new Intent(TrackerService.this, MainActivity.class);
        PendingIntent mainActivityPendingIntent = PendingIntent.getActivity(TrackerService.this,
                MainActivity.REQUEST_CODE_MAIN_ACTIVITY_NOTIFICATION,
                mainActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(TrackerService.this)
                .setContentTitle("Marathon Tracker")
                .setContentText("service running")
                .setContentIntent(mainActivityPendingIntent)
                .setSmallIcon(R.drawable.ic_notification_small);
        mStickyNotification = notificationBuilder.build();

        return mStickyNotification;
    }
}
