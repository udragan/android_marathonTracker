package com.udragan.android.marathontracker.providers;

import android.net.Uri;
import android.provider.BaseColumns;

import com.udragan.android.marathontracker.infrastructure.common.Constants;

/**
 * Marathon database contract.
 */
public class MarathonContract {

    // members **********************************************************************************************************

    public static final String AUTHORITY = Constants.PACKAGE_NAME + ".provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_TRACKS = "tracks";

    /**
     * Track table declaration.
     */
    public static final class TrackEntry
            implements BaseColumns {

        // members ******************************************************************************************************

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_TRACKS)
                .build();

        public static final String TABLE_NAME = "tracks";

        public static final String COLUMN_NAME = "_name";
        public static final String COLUMN_IS_COMPLETE = "_isComplete";
        public static final String COLUMN_DURATION = "_duration";
    }
}
