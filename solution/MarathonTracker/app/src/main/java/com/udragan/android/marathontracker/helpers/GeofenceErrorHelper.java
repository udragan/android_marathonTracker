package com.udragan.android.marathontracker.helpers;

import android.content.Context;
import android.content.res.Resources;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.udragan.android.marathontracker.R;

/**
 * Helper class for mapping Geofence error messages.
 */
public class GeofenceErrorHelper {

    // constructors *****************************************************************************************************

    /**
     * Prevent instantiation.
     */
    private GeofenceErrorHelper() {
    }

    // public methods ***************************************************************************************************

    /**
     * Translate {@link com.google.android.gms.location.GeofenceStatusCodes} to human friendly string.
     *
     * @param context application context
     * @param e       exception to extract Geofence status code from
     * @return Geofence status code translated to human friendly form
     */
    public static String getErrorString(Context context, Exception e) {
        if (e instanceof ApiException) {
            return getErrorString(context, ((ApiException) e).getStatusCode());
        } else {
            return context.getResources().getString(R.string.geofence_error_unknown_error);
        }
    }

    /**
     * Translate {@link com.google.android.gms.location.GeofenceStatusCodes} to human friendly string.
     *
     * @param context   application context
     * @param errorCode Geofence status code to translate
     * @return Geofence status code translated to human friendly form
     */
    public static String getErrorString(Context context, int errorCode) {
        Resources resources = context.getResources();

        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return resources.getString(R.string.geofence_error_not_available);
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return resources.getString(R.string.geofence_error_too_many_geofences);
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return resources.getString(R.string.geofence_error_too_many_pending_intents);
            default:
                return resources.getString(R.string.geofence_error_unknown_error);
        }
    }
}
