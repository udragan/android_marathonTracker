package com.udragan.android.marathontracker.services;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.udragan.android.marathontracker.MainActivity;
import com.udragan.android.marathontracker.R;
import com.udragan.android.marathontracker.helpers.GeofenceErrorHelper;
import com.udragan.android.marathontracker.infrastructure.common.Constants;
import com.udragan.android.marathontracker.infrastructure.interfaces.IService;
import com.udragan.android.marathontracker.providers.MarathonContract;

import java.util.ArrayList;
import java.util.List;

/**
 * A background service for managing the geofencing client.
 */
public class TrackerService extends Service
        implements IService {

    // members **********************************************************************************************************

    private static final String TAG = TrackerService.class.getSimpleName();
    private static final int REQUEST_CODE_GEOFENCE_INTENT_SERVICE = REQUEST_CODE_BASE + 2;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private GeofencingClient mGeofencingClient;
    private PendingIntent mGeofenceIntentServicePendingIntent;
    private Notification mStickyNotification;

    private OnCompleteListener<Void> mAddGeofencesListener;
    private OnCompleteListener<Void> mRemoveGeofencesListener;
    private LocationCallback mLocationCallback;

    // constructors *****************************************************************************************************

    /**
     * Initializes a new instance of {@link com.udragan.android.marathontracker.services.TrackerService} class.
     */
    public TrackerService() {
        Log.d(TAG, "constructor.");
        defineListeners();
        defineCallbacks();
    }

    // overrides ********************************************************************************************************

    @Override
    public void onCreate() {
        super.onCreate();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(TrackerService.this);
        mGeofencingClient = LocationServices.getGeofencingClient(TrackerService.this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStartCommand.");

        // TODO: implement consistent permission checks!
        if (checkPermissions()) {
            int trackId = intent.getIntExtra(Constants.EXTRA_TRACK_ID, MarathonContract.INVALID_TRACK_ID);

            // TODO: move to worker thread since we will contact the database in getGeofencingRequest()
            Log.d(TAG, "Start location updates.");
            //noinspection MissingPermission
            mFusedLocationProviderClient.requestLocationUpdates(getLocationRequest(),
                    mLocationCallback,
                    null);
            Log.d(TAG, "Adding geofences...");
            //noinspection MissingPermission
            mGeofencingClient.addGeofences(getGeofencingRequest(trackId), getGeofencingPendingIntent(trackId))
                    .addOnCompleteListener(mAddGeofencesListener);
            startForeground(startId, getStickyNotification());
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mGeofencingClient.removeGeofences(getGeofencingPendingIntent(MarathonContract.INVALID_TRACK_ID))
                .addOnCompleteListener(mRemoveGeofencesListener);
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);

        mGeofencingClient = null;
        mFusedLocationProviderClient = null;

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
                    Log.d(TAG, "Add geofences successful.");
                } else {
                    String errorMessage = GeofenceErrorHelper.getErrorString(TrackerService.this, task.getException());
                    Log.w(TAG, errorMessage);
                }
            }
        };

        mRemoveGeofencesListener = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Remove geofences successful.");
                } else {
                    String errorMessage = GeofenceErrorHelper.getErrorString(TrackerService.this, task.getException());
                    Log.w(TAG, errorMessage);
                }
            }
        };
    }

    private void defineCallbacks() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d(TAG, "Current location callback triggered.");
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
    private LocationRequest getLocationRequest() {
        if (mLocationRequest == null) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(10000);        //TODO: move to preferences
            mLocationRequest.setFastestInterval(5000);  //TODO: move to preferences
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }

        Log.v(TAG, String.format("Location request: \ninterval: %d\nfastestInterval: %d\npriority: %d",
                mLocationRequest.getInterval(), mLocationRequest.getFastestInterval(), mLocationRequest.getPriority()));

        return mLocationRequest;
    }

    @NonNull
    private GeofencingRequest getGeofencingRequest(int trackId) {
        List<Geofence> geofences;

        try (Cursor cursor = getCheckpoints(trackId)) {
            geofences = getGeofences(cursor);
        }

        GeofencingRequest.Builder requestBuilder = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofences);

        return requestBuilder.build();
    }

    private Cursor getCheckpoints(int trackId) {
        String selection = MarathonContract.CheckpointEntry.COLUMN_FC_TRACK_ID + "=?";
        String[] selectionArgs = new String[]{
                String.valueOf(trackId)};

        return getContentResolver().query(MarathonContract.CheckpointEntry.CONTENT_URI,
                null,
                selection,
                selectionArgs,
                MarathonContract.CheckpointEntry.COLUMN_INDEX);
    }

    private List<Geofence> getGeofences(Cursor cursor) {
        //hardcoded geofence for testing, 37.4226, -122.084 ( Googleplex )
        List<Geofence> result = new ArrayList<>();

        while (cursor.moveToNext()) {
            int nameColumnIndex = cursor.getColumnIndex(MarathonContract.CheckpointEntry.COLUMN_NAME);
            int latitudeColumnIndex = cursor.getColumnIndex(MarathonContract.CheckpointEntry.COLUMN_LATITUDE);
            int longitudeColumnIndex = cursor.getColumnIndex(MarathonContract.CheckpointEntry.COLUMN_LONGITUDE);

            String name = cursor.getString(nameColumnIndex);
            double latitude = cursor.getDouble(latitudeColumnIndex);
            double longitude = cursor.getDouble(longitudeColumnIndex);

            Geofence geofence = new Geofence.Builder()
                    .setRequestId(name)
                    .setCircularRegion(latitude, longitude, 100)
                    .setExpirationDuration(2 * 60 * 1000) //TODO: this should be part of the track data
                    .setTransitionTypes(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .build();

            result.add(geofence);
        }

        return result;
    }

    private PendingIntent getGeofencingPendingIntent(int trackId) {
        if (mGeofenceIntentServicePendingIntent != null) {
            return mGeofenceIntentServicePendingIntent;
        }

        Intent intent = new Intent(TrackerService.this, GeofenceIntentService.class);
        intent.putExtra(Constants.EXTRA_TRACK_ID, trackId);

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
