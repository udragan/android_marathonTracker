package com.udragan.android.marathontracker.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.udragan.android.marathontracker.MainActivity;
import com.udragan.android.marathontracker.R;
import com.udragan.android.marathontracker.helpers.GeofenceErrorHelper;
import com.udragan.android.marathontracker.infrastructure.Toaster;
import com.udragan.android.marathontracker.infrastructure.common.Constants;
import com.udragan.android.marathontracker.infrastructure.interfaces.IService;
import com.udragan.android.marathontracker.providers.MarathonContract;

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

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Cursor cursor = getCheckpoints(intent);

            if (cursor != null &&
                    cursor.moveToNext()) {
                int latitudeColumnIndex = cursor.getColumnIndex(MarathonContract.CheckpointEntry.COLUMN_LATITUDE);
                int longitudeColumnIndex = cursor.getColumnIndex(MarathonContract.CheckpointEntry.COLUMN_LONGITUDE);

                double latitude = cursor.getDouble(latitudeColumnIndex);
                double longitude = cursor.getDouble(longitudeColumnIndex);
                double transitionLatitude = geofencingEvent.getTriggeringLocation().getLatitude();
                double transitionLongitude = geofencingEvent.getTriggeringLocation().getLongitude();

                //TODO user config: move radiusMeters to config - it is the same as the radius when registering geofences.
                int radiusMeters = 100;
                float[] distance = new float[1];

                Location.distanceBetween(latitude, longitude, transitionLatitude, transitionLongitude, distance);

                if (distance[0] < radiusMeters) {
                    updateCheckpoint(cursor);
                    updateNotification();
                } else {
                    // geofence is triggered but it is NOT the next unvisited checkpoint in track!
                    Toaster.showLong(GeofenceIntentService.this, R.string.toast_checkpoint_not_in_order);
                }

                cursor.close();
            }
        }
    }

    private Cursor getCheckpoints(Intent intent) {
        int trackId = intent.getIntExtra(Constants.EXTRA_TRACK_ID, MarathonContract.INVALID_TRACK_ID);

        String selection = MarathonContract.CheckpointEntry.COLUMN_FC_TRACK_ID + "=? AND " +
                MarathonContract.CheckpointEntry.COLUMN_IS_CHECKED + "=?";
        String[] selectionArgs = new String[]{
                String.valueOf(trackId),
                String.valueOf(0)};

        Log.v(TAG, String.format("Selection: %s",
                selection));
        Log.v(TAG, String.format("Selection args: %s",
                (Object[]) selectionArgs));

        return getContentResolver().query(MarathonContract.CheckpointEntry.CONTENT_URI,
                null,
                selection,
                selectionArgs,
                MarathonContract.CheckpointEntry.COLUMN_INDEX);
    }

    private void updateCheckpoint(Cursor cursor) {
        int idColumnIndex = cursor.getColumnIndex(MarathonContract.CheckpointEntry._ID);
        int id = cursor.getInt(idColumnIndex);

        ContentValues contentValues = new ContentValues(1);
        contentValues.put(MarathonContract.CheckpointEntry.COLUMN_IS_CHECKED, true);
        contentValues.put(MarathonContract.CheckpointEntry.COLUMN_TIME, System.currentTimeMillis());

        getContentResolver().update(MarathonContract.CheckpointEntry.CONTENT_URI,
                contentValues,
                "_id=?",
                new String[]{String.valueOf(id)});
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
