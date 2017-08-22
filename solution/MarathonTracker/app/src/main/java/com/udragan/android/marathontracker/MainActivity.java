package com.udragan.android.marathontracker;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
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
import android.widget.Toast;

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
import com.udragan.android.marathontracker.models.CheckpointModel;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // members **********************************************************************************************************

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSION_FINE_LOCATION = 100;
    private static final int REQUEST_CHECK_SETTINGS = 101;
    private static final String REQUESTING_LOCATION_UPDATES_KEY = "requestingLocationUpdates";

    private TextView mLatitudeView;
    private TextView mLongitudeView;
    private TextView mSpeedView;
    private TextView mBearingView;
    private RecyclerView mCheckpointsRecyclerView;
    private CheckpointAdapter mCheckpointAdapter;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private boolean mIsRequestingLocationUpdates;
    private Location mLastKnownLocation;

    private OnSuccessListener<LocationSettingsResponse> mLocationSettingsSuccessListener;
    private OnFailureListener mLocationSettingsFailureListener;
    private OnSuccessListener<Location> mLocationSuccessListener;

    private LocationCallback mLocationCallback;

    // AppCompatActivity ************************************************************************************************

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

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        setupLocationProviderClient();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mIsRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mIsRequestingLocationUpdates) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mIsRequestingLocationUpdates);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            mIsRequestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSION_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupLocationProviderClient();
                } else {
                    // permission denied! Disable the
                    // functionality that depends on this permission.
                    String toastMessage = getResources().getString(R.string.toast_location_permission_denied);
                    Toast.makeText(MainActivity.this, toastMessage, Toast.LENGTH_SHORT).show();
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
            case REQUEST_CHECK_SETTINGS:
                if (resultCode == RESULT_OK) {
                    setupLocationProviderClient();
                }
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
                                    REQUEST_CHECK_SETTINGS);
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
                    String toastMessage = getResources().getString(R.string.toast_location_stale);
                    Toast.makeText(MainActivity.this, toastMessage, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Last location triggered but received location is null!");
                }
            }
        };
    }

    private void defineCallbacks() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d(TAG, String.format("Update location callback for %d locations.",
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
                    REQUEST_PERMISSION_FINE_LOCATION);

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

    public void switchRequestLocationUpdates(View view) {
        Switch requestLocationUpdates = (Switch) view;
        mIsRequestingLocationUpdates = requestLocationUpdates.isChecked();

        if (mIsRequestingLocationUpdates) {
            startLocationUpdates();
        } else {
            stopLocationUpdates();
        }
    }
}
