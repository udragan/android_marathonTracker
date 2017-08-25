package com.udragan.android.marathontracker.infrastructure;

import android.content.Context;
import android.widget.Toast;

/**
 * Wrapper for displaying Toast messages.
 */
public class Toaster {

    // constructors *****************************************************************************************************

    /**
     * Prevent instantiation.
     */
    private Toaster() {
    }

    // public methods ***************************************************************************************************

    /**
     * Display short {@link android.widget.Toast} message.
     *
     * @param context    context
     * @param resourceId identifier of a string resource to be displayed
     */
    public static void showShort(Context context, int resourceId) {
        Toast.makeText(context, resourceId, Toast.LENGTH_SHORT).show();
    }

    /**
     * Display short {@link android.widget.Toast} message.
     *
     * @param context context
     * @param message message to be displayed
     */
    public static void showShort(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Display long {@link android.widget.Toast} message.
     *
     * @param context    context
     * @param resourceId identifier of a string resource to be displayed
     */
    public static void showLong(Context context, int resourceId) {
        Toast.makeText(context, resourceId, Toast.LENGTH_LONG).show();
    }

    /**
     * Display long {@link android.widget.Toast} message.
     *
     * @param context context
     * @param message message to be displayed
     */
    public static void showLong(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
