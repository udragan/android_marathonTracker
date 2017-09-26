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
     * Name of the global preferences file.
     */
    public static final String GLOBAL_PREFERENCES_KEY = PACKAGE_NAME + ".PREFERENCES";

    /**
     * Is tracker service started preference key.
     */
    public static final String PREFERENCE_KEY_TRACKING = "IS_TRACKING";

    /**
     * Id of the last active track preference key.
     */
    public static final String PREFERENCE_KEY_LAST_ACTIVE_TRACK_ID = "LAST_ACTIVE_TRACK_ID";
}
