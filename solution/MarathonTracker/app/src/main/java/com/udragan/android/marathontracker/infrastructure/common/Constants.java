package com.udragan.android.marathontracker.infrastructure.common;

/**
 * A holder for application wide constants.
 */
public class Constants {

    // members **********************************************************************************************************

    public static final String PACKAGE_NAME = "com.udragan.android.marathontracker";

    // constructors *****************************************************************************************************

    /**
     * Prevent instantiation.
     */
    private Constants() {
    }

    // application wide constants ***************************************************************************************

    /**
     * Invalid time in milliseconds.
     */
    public static final long INVALID_TIME_MILLIS = -1;

    /**
     * Invalid track id.
     */
    public static final int INVALID_TRACK_ID = -1;
    /**
     * Name of the global preferences file.
     */
    public static final String GLOBAL_PREFERENCES_KEY = PACKAGE_NAME + ".PREFERENCES";

    /**
     * Id of the last active track preference key.
     */
    public static final String PREF_KEY_LAST_ACTIVE_TRACK_ID = "LAST_ACTIVE_TRACK_ID";

    /**
     * Location request interval preference key. (from preferences.xml)
     */
    public static final String PREF_KEY_LOCATION_REQUEST_INTERVAL = "LOCATION_REQUEST_INTERVAL";

    /**
     * Location request fastest interval preference key. (from preferences.xml)
     */
    public static final String PREF_KEY_LOCATION_REQUEST_FASTEST_INTERVAL = "LOCATION_REQUEST_FASTEST_INTERVAL";

    /**
     * Id for TrackId extra inside bundle or intent.
     */
    public static final String EXTRA_TRACK_ID = Constants.PACKAGE_NAME + ".extra.TRACK_ID";
}
