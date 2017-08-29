package com.udragan.android.marathontracker.infrastructure.common;

/**
 * A holder for application wide constants.
 */
public class Constants {

    // members **********************************************************************************************************

    private static final String PACKAGE_NAME = "com.udragan.android.marathontracker";

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
     * Is tracker service started reference key.
     */
    public static final String GEOFENCING_PREFERENCE_KEY = "IS_GEOFENCING";
}
