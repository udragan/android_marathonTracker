package com.udragan.android.marathontracker.infrastructure.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Helper class for retrieving application preferences.
 */
public class PreferenceHelper {

    // members **********************************************************************************************************

    private static final String TAG = PreferenceHelper.class.getSimpleName();

    // public methods ***************************************************************************************************

    /**
     * Gets long preference from application preferences.
     *
     * @param context preference context.
     * @param key     requested preference key.
     * @return long value of the preference saved under provided key.
     */
    public static long getLongPreference(Context context, String key) {
        long longValue = -1;
        String value = getPreference(context, key);

        try {
            longValue = Long.parseLong(value.trim());
        } catch (NumberFormatException nfe) {
            Log.w(TAG, String.format("Value: %s can not be parsed to type Long!", value));
        }

        return longValue;
    }

    /**
     * Gets string preference from application preferences.
     *
     * @param context preference context.
     * @param key     requested preference key.
     * @return string value of the preference saved under provided key.
     *
     */
    public static String getPreference(Context context, String key) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        return preferences.getString(key, "");
    }
}
