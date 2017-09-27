package com.udragan.android.marathontracker.helpers;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

/**
 * Service oriented helper class.
 */
public final class ServiceHelper {

    // members **********************************************************************************************************

    private static final String TAG = ServiceHelper.class.getSimpleName();

    // constructors *****************************************************************************************************

    /**
     * Prevent instantiation.
     */
    private ServiceHelper() {
    }

    // public methods ***************************************************************************************************

    /**
     * Determine if specified service is running in provided context.
     *
     * @param serviceClass service to check
     * @param context      context
     * @return true if service is running, false otherwise
     */
    public static boolean isServiceRunning(Class<?> serviceClass,
                                           Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        boolean isRunning = false;

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                isRunning = true;
                break;
            }
        }

        Log.d(TAG, String.format("Service: %s is %s.",
                serviceClass, isRunning ? "running" : "not running"));
        return isRunning;
    }
}
