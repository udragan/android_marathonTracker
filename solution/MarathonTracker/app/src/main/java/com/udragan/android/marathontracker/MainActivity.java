package com.udragan.android.marathontracker;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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
import com.udragan.android.marathontracker.services.TrackerService;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements IActivity {

    // members **********************************************************************************************************

    public static final int REQUEST_CODE_MAIN_ACTIVITY_NOTIFICATION = REQUEST_CODE_BASE + 1;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_PERMISSION_FINE_LOCATION = REQUEST_CODE_BASE + 10;
    private static final int REQUEST_CODE_CHECK_SETTINGS = REQUEST_CODE_BASE + 11;
    private static final String KEY_REQUESTING_LOCATION_UPDATES = "requestingLocationUpdates";

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

    // overrides ********************************************************************************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLatitudeView = (TextView) findViewById(R.id.latitude_value_text_main_activity);
        mLongitudeView = (TextView) findViewById(R.id.longitude_value_text_main_activity);
        mSpeedView = (TextView) findViewById(R.id.speed_value_text_main_activity);
        mBearingView = (TextView) findViewById(R.id.bearing_value_text_main_activity);
        Toolbar appBar = (Toolbar) findViewById(R.id.toolbar_main_activity);
        setSupportActionBar(appBar);

        // test data //
        ArrayList<CheckpointModel> testDataCheckpoints = new ArrayList<>(4);
        testDataCheckpoints.add(new CheckpointModel(16, 45));
        testDataCheckpoints.add(new CheckpointModel(19, 45));
        testDataCheckpoints.add(new CheckpointModel(16, 45));
        testDataCheckpoints.add(new CheckpointModel(19, 45));
        ///////////////

        mCheckpointAdapter = new CheckpointAdapter(MainActivity.this, testDataCheckpoints);
        mCheckpointsRecyclerView = (RecyclerView) findViewById(R.id.checkpoints_recycler_view_main_activity);
        mCheckpointsRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        mCheckpointsRecyclerView.setAdapter(mCheckpointAdapter);

        defineListeners();
        defineCallbacks();

        //TODO: check for permissions appropriately
        Switch geofencingSwitch = (Switch) findViewById(R.id.request_location_updates_switch);
        geofencingSwitch.setChecked(getIsTrackingPreference());

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        setupLocationProviderClient();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //TODO: check for permissions prior to starting updates
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //TODO: check for permissions prior to stoping updates
        stopLocationUpdates();
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
                    setupLocationProviderClient();
                } else {
                    // TODO: permission denied! Disable the
                    // functionality that depends on this permission.
                    Toaster.showShort(MainActivity.this, R.string.toast_location_permission_denied);
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
            case REQUEST_CODE_CHECK_SETTINGS:
                if (resultCode == RESULT_OK) {
                    setupLocationProviderClient();
                }
        }
    }

    // public methods ***************************************************************************************************

    public void switchRequestLocationUpdates(View view) {
        Switch isGeofencingSwitch = (Switch) view;
        saveIsTrackingPreference(isGeofencingSwitch.isChecked());
        Intent trackerServiceIntent = new Intent(this, TrackerService.class);

        if (isGeofencingSwitch.isChecked()) {
            startService(trackerServiceIntent);
        } else {
            stopService(trackerServiceIntent);
        }
    }

    // private methods **************************************************************************************************

    private void defineListeners() {
        mLocationSettingsSuccessListener = new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
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
                            // and checkLocationSettingsSufficient the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(MainActivity.this,
                                    REQUEST_CODE_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        };

        mLocationSuccessListener = new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    Log.d(TAG, String.format("Last location triggered, lat: %f, lon: %f",
                            location.getLatitude(), location.getLongitude()));
                    updateCurrentLocation(location);
                } else {
                    Toaster.showShort(MainActivity.this, R.string.toast_location_stale);
                    Log.d(TAG, "Last location triggered but received location is null!");
                }
            }
        };
    }

    private void defineCallbacks() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d(TAG, String.format("Update location callback triggered.",
                        locationResult.getLocations().size()));

                for (Location location : locationResult.getLocations()) {
                    updateCurrentLocation(location);
                }
            }
        };
    }

    private void setupLocationProviderClient() {
        int permissionAccessFineLocation = ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionAccessFineLocation != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_PERMISSION_FINE_LOCATION);

            return;
        }

        checkLocationSettingsSufficient();
    }

    private void checkLocationSettingsSufficient() {
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

        return mLocationRequest;
    }

    private void startLocationUpdates() {
        //noinspection MissingPermission
        mFusedLocationProviderClient.requestLocationUpdates(getLocationRequest(),
                mLocationCallback,
                null);
    }

    private void stopLocationUpdates() {
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    private void updateCurrentLocation(Location location) {
        defineSpeedAndBearing(location);
        mLastKnownLocation = location;

        mLatitudeView.setText(String.format(Locale.getDefault(), "%.5f", location.getLatitude()));
        mLongitudeView.setText(String.format(Locale.getDefault(), "%.5f", location.getLongitude()));
        mSpeedView.setText(String.valueOf(location.getSpeed()));
        mBearingView.setText(getBearingDisplayText(location.getBearing(), location.getSpeed()));
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
    private String getBearingDisplayText(float bearing, double distance) {
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
        return preferences.getBoolean(Constants.PREFERENCE_KEY_TRACKING, false);
    }

    private void saveIsTrackingPreference(boolean isGeofencing) {
        SharedPreferences preferences = getSharedPreferences(Constants.GLOBAL_PREFERENCES_KEY, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Constants.PREFERENCE_KEY_TRACKING, isGeofencing);
        editor.apply();
    }
}
