package com.udragan.android.marathontracker;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.udragan.android.marathontracker.adapters.CheckpointAdapter;
import com.udragan.android.marathontracker.infrastructure.Toaster;
import com.udragan.android.marathontracker.infrastructure.common.Constants;
import com.udragan.android.marathontracker.infrastructure.interfaces.IActivity;
import com.udragan.android.marathontracker.models.CheckpointModel;
import com.udragan.android.marathontracker.providers.MarathonContract;
import com.udragan.android.marathontracker.services.TrackerService;
import com.udragan.android.marathontracker.testing.TestTrackAdapter;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements IActivity {

    // members **********************************************************************************************************

    public static final int REQUEST_CODE_MAIN_ACTIVITY_NOTIFICATION = REQUEST_CODE_BASE + 1;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_PERMISSION_FINE_LOCATION = REQUEST_CODE_BASE + 10;
    private static final int REQUEST_CODE_CHECK_LOCATION_SETTINGS = REQUEST_CODE_BASE + 11;

    private Switch mTrackingSwitch;
    private TextView mLatitudeView;
    private TextView mLongitudeView;
    private TextView mSpeedView;
    private TextView mBearingView;
    private RecyclerView mCheckpointsRecyclerView;
    private CheckpointAdapter mCheckpointAdapter;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private Location mLastKnownLocation;

    private OnSuccessListener<LocationSettingsResponse> mLocationSettingsSuccessListener;
    private OnFailureListener mLocationSettingsFailureListener;
    private OnSuccessListener<Location> mLocationSuccessListener;
    private LocationCallback mLocationCallback;

    // testing //////////////////////////////////

    private LoaderManager.LoaderCallbacks<Cursor> mTestLoaderCallback;
    private TestTrackAdapter mTestTrackAdapter;

    ///end testing //////////////////////////////

    // overrides ********************************************************************************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        defineListeners();
        defineCallbacks();

        mTrackingSwitch = (Switch) findViewById(R.id.is_tracking_switch);
        mLatitudeView = (TextView) findViewById(R.id.latitude_value_text_main_activity);
        mLongitudeView = (TextView) findViewById(R.id.longitude_value_text_main_activity);
        mSpeedView = (TextView) findViewById(R.id.speed_value_text_main_activity);
        mBearingView = (TextView) findViewById(R.id.bearing_value_text_main_activity);
        Toolbar appBar = (Toolbar) findViewById(R.id.toolbar_main_activity);
        setSupportActionBar(appBar);

        // testing //////////////////////////////

        mTestTrackAdapter = new TestTrackAdapter(MainActivity.this, null);
        RecyclerView testRecyclerView = (RecyclerView) findViewById(R.id.test_recycler_view_tracks);
        testRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        testRecyclerView.setAdapter(mTestTrackAdapter);

        getSupportLoaderManager().initLoader(1, null, mTestLoaderCallback);

        // end testing //////////////////////////

        mCheckpointAdapter = new CheckpointAdapter(MainActivity.this, null);
        mCheckpointsRecyclerView = (RecyclerView) findViewById(R.id.checkpoints_recycler_view_main_activity);
        mCheckpointsRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        mCheckpointsRecyclerView.setAdapter(mCheckpointAdapter);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupLocationProviderClient();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (checkPermissions()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (checkPermissions()) {
            stopLocationUpdates();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, String.format("%s permission granted.",
                            permissions[0]));
                    requestLocationSettings();
                } else {
                    // TODO: permission denied! Disable the
                    // functionality that depends on this permission.
                    Toaster.showLong(MainActivity.this, R.string.toast_location_permission_denied);
                    Log.w(TAG, String.format("%s permission denied!",
                            Manifest.permission.ACCESS_FINE_LOCATION));
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_CHECK_LOCATION_SETTINGS:
                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "Location services turned on.");
                    setupLocationProviderClient();
                } else {
                    //TODO: disable everything because permissions/settings are not satisfied!
                    Toaster.showLong(MainActivity.this, R.string.toast_location_services_off);
                    Log.w(TAG, "Location services turned off.");
                }
        }
    }

    // public methods ***************************************************************************************************

    public void switchIsTracking(View view) {
        Switch geofencingSwitch = (Switch) view;
        boolean isTracking = geofencingSwitch.isChecked();
        saveIsTrackingPreference(isTracking);
        Intent trackerServiceIntent = new Intent(this, TrackerService.class);

        if (isTracking) {
            startService(trackerServiceIntent);
        } else {
            stopService(trackerServiceIntent);
        }

        Log.d(TAG, String.format("Tracking active: %s", isTracking));
    }

    // private methods **************************************************************************************************

    private void defineListeners() {
        mLocationSettingsSuccessListener = new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.d(TAG, "Location service settings sufficient, getting last known location...");
                mTrackingSwitch.setChecked(getIsTrackingPreference());
                //noinspection MissingPermission
                mFusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(MainActivity.this, mLocationSuccessListener);
            }
        };

        mLocationSettingsFailureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();

                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and requestLocationSettings the result in onActivityResult().
                            Log.i(TAG, "Location service settings insufficient, invoke resolution...");
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(MainActivity.this,
                                    REQUEST_CODE_CHECK_LOCATION_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                            Log.e(TAG, "Location service settings resolution FAILED!", sendEx);
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        //TODO: disable everything because permissions/settings are not satisfied!
                        Toaster.showLong(MainActivity.this, R.string.toast_location_services_settings_change_unavailable);
                        Log.w(TAG, "Location service settings change not possible!");
                        break;
                }
            }
        };

        mLocationSuccessListener = new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    Log.d(TAG, String.format("Last known location received, lat: %f, lon: %f",
                            location.getLatitude(), location.getLongitude()));
                    updateUICurrentLocation(location);
                } else {
                    Toaster.showShort(MainActivity.this, R.string.toast_location_stale);
                    Log.d(TAG, "Last known location received but location is stale (null)!");
                }
            }
        };
    }

    private void defineCallbacks() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d(TAG, "Current location callback triggered.");

                for (Location location : locationResult.getLocations()) {
                    updateUICurrentLocation(location);
                }
            }
        };

        // testing //////////////////////////////

        mTestLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                Uri TRACKS_URI = MarathonContract.BASE_CONTENT_URI
                        .buildUpon()
                        .appendPath(MarathonContract.PATH_TRACKS)
                        .build();
                return new CursorLoader(MainActivity.this,
                        TRACKS_URI,
                        null,
                        null,
                        null,
                        MarathonContract.TrackEntry._ID);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                data.moveToFirst();
                mTestTrackAdapter.swapCursor(data);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
            }
        };

        ///end testing //////////////////////////
    }

    private boolean checkPermissions() {
        Log.d(TAG, "Checking permissions...");
        return checkPermission(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private boolean checkPermission(String permission) {
        int permitted = PermissionChecker.checkSelfPermission(MainActivity.this, permission);
        Log.v(TAG, String.format("Checking permission '%s': %d",
                permission, permitted));

        return permitted == PackageManager.PERMISSION_GRANTED;
    }

    private void setupLocationProviderClient() {
        Log.d(TAG, "Setting up FusedLocationProviderClient...");

        if (!checkPermissions()) {
            String permission = Manifest.permission.ACCESS_FINE_LOCATION;
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{permission},
                    REQUEST_CODE_PERMISSION_FINE_LOCATION);
            Log.i(TAG, String.format("%s denied, requesting...",
                    permission));

            return;
        }

        requestLocationSettings();
    }

    private void requestLocationSettings() {
        Log.d(TAG, "Checking Location service settings...");
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(getLocationRequest());
        SettingsClient settingsClient = LocationServices.getSettingsClient(MainActivity.this);
        settingsClient.checkLocationSettings(builder.build())
                .addOnSuccessListener(MainActivity.this, mLocationSettingsSuccessListener)
                .addOnFailureListener(MainActivity.this, mLocationSettingsFailureListener);
    }

    private LocationRequest getLocationRequest() {
        if (mLocationRequest == null) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(10000);
            mLocationRequest.setFastestInterval(5000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }

        Log.v(TAG, String.format("Location request: \ninterval: %d\nfastestInterval: %d\npriority: %d",
                mLocationRequest.getInterval(), mLocationRequest.getFastestInterval(), mLocationRequest.getPriority()));

        return mLocationRequest;
    }

    private void startLocationUpdates() {
        Log.d(TAG, "Start location updates.");
        //noinspection MissingPermission
        mFusedLocationProviderClient.requestLocationUpdates(getLocationRequest(),
                mLocationCallback,
                null);
    }

    private void stopLocationUpdates() {
        Log.d(TAG, "Stop location updates.");
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    private void updateUICurrentLocation(Location location) {
        defineSpeedAndBearing(location);
        mLastKnownLocation = location;

        String bearingText = getBearingDisplayText(location.getBearing(), location.getSpeed());
        mLatitudeView.setText(String.format(Locale.getDefault(), "%.5f", location.getLatitude()));
        mLongitudeView.setText(String.format(Locale.getDefault(), "%.5f", location.getLongitude()));
        mSpeedView.setText(String.valueOf(location.getSpeed()));
        mBearingView.setText(bearingText);

        Log.v(TAG, String.format("Update UI current location:\nlat: %.5f\nlon: %.5f\nspeed: %.2f\nbearing: %s",
                location.getLatitude(), location.getLongitude(), location.getSpeed(), bearingText));
    }

    private void defineSpeedAndBearing(Location location) {
        if (mLastKnownLocation != null
                && (!location.hasSpeed() || !location.hasBearing())) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            double lastLatitude = mLastKnownLocation.getLatitude();
            double lastLongitude = mLastKnownLocation.getLongitude();

            float[] results = new float[2];
            Location.distanceBetween(latitude, longitude, lastLatitude, lastLongitude, results);

            if (!location.hasSpeed()) {
                double distance = results[0];
                double timeSecs = (location.getElapsedRealtimeNanos() - mLastKnownLocation.getElapsedRealtimeNanos()) / 1e9;

                if (timeSecs != 0) {
                    location.setSpeed(Math.round(distance / timeSecs));
                }
            }

            if (!location.hasBearing()) {
                location.setBearing(results[1]);
            }
        }
    }

    @NonNull
    private String getBearingDisplayText(float bearing,
                                         double distance) {
        if (distance == 0) {
            return getString(R.string.not_applicable);
        }

        int quadrant = (int) Math.floor(bearing / 22.5);

        switch (quadrant) {
            case 1:
            case 2:
                return getString(R.string.north_east_abbr);
            case 3:
            case 4:
                return getString(R.string.north_abbr);
            case 5:
            case 6:
                return getString(R.string.north_west_abbr);
            case 7:
            case 8:
                return getString(R.string.west_abbr);
            case 9:
            case 10:
                return getString(R.string.south_west_abbr);
            case 11:
            case 12:
                return getString(R.string.south_abbr);
            case 13:
            case 14:
                return getString(R.string.south_east_abbr);
            case 15:
            case 0:
                return getString(R.string.east_abbr);
        }

        return getString(R.string.not_applicable);
    }

    private boolean getIsTrackingPreference() {
        SharedPreferences preferences = getSharedPreferences(Constants.GLOBAL_PREFERENCES_KEY, MODE_PRIVATE);
        boolean trackingPreference = preferences.getBoolean(Constants.PREFERENCE_KEY_TRACKING, false);
        Log.v(TAG, String.format("Retrieved tracking preference: %s", trackingPreference));

        return trackingPreference;
    }

    private void saveIsTrackingPreference(boolean isTracking) {
        SharedPreferences preferences = getSharedPreferences(Constants.GLOBAL_PREFERENCES_KEY, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Constants.PREFERENCE_KEY_TRACKING, isTracking);
        editor.apply();
        Log.v(TAG, String.format("Saved tracking preference: %s", isTracking));
    }

    // testing //////////////////////////////////

    public void testAddTrack(View view) {
        ContentValues cv = new ContentValues(1);
        cv.put(MarathonContract.TrackEntry.COLUMN_NAME, "Test track");
        cv.put(MarathonContract.TrackEntry.COLUMN_IS_COMPLETE, false);
        cv.put(MarathonContract.TrackEntry.COLUMN_DURATION, 0);

        Uri insertUri = getContentResolver().insert(MarathonContract.TrackEntry.CONTENT_URI, cv);

        Toaster.showShort(MainActivity.this, String.valueOf(insertUri));

        String key = insertUri != null ? insertUri.getLastPathSegment() : null;
        cv = new ContentValues(1);
        cv.put(MarathonContract.CheckpointEntry.COLUMN_NAME, "Test checkpoint");
        cv.put(MarathonContract.CheckpointEntry.COLUMN_INDEX, 1);
        cv.put(MarathonContract.CheckpointEntry.COLUMN_LATITUDE, 46.3561);
        cv.put(MarathonContract.CheckpointEntry.COLUMN_LONGITUDE, -72.5397);
        cv.put(MarathonContract.CheckpointEntry.COLUMN_IS_CHECKED, 1);
        cv.put(MarathonContract.CheckpointEntry.COLUMN_TIME, 123);
        cv.put(MarathonContract.CheckpointEntry.COLUMN_FC_TRACK_ID, key);

        Uri insertCheckpointUri = getContentResolver().insert(MarathonContract.CheckpointEntry.CONTENT_URI, cv);
        Toaster.showShort(MainActivity.this, String.valueOf(insertCheckpointUri));
    }

    public void testLoadTracks(View view) {

    }

    public void testClearDb(View view) {
        int noOfDeleted = getContentResolver().delete(MarathonContract.TrackEntry.CONTENT_URI, null, null);
        Toaster.showShort(MainActivity.this, String.format(Locale.getDefault(), "Deleted: %d", noOfDeleted));
    }

    // end testing //////////////////////////////
}
