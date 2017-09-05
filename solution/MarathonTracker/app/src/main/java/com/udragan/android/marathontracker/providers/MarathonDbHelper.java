package com.udragan.android.marathontracker.providers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.udragan.android.marathontracker.providers.MarathonContract.TrackEntry;

/**
 * Extension of {@link android.database.sqlite.SQLiteOpenHelper} for Marathon database.
 */
public class MarathonDbHelper extends SQLiteOpenHelper {

    // members **********************************************************************************************************

    private static final String DATABASE_NAME = "MarathonTracker.db";
    private static final int DATABASE_VERSION = 1;

    // constructors *****************************************************************************************************

    /**
     * Initializes a new instance of {@link com.udragan.android.marathontracker.providers.MarathonDbHelper} class.
     *
     * @param context context
     */
    public MarathonDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // overrides ********************************************************************************************************

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_TRACK_TABLE = "CREATE TABLE " + TrackEntry.TABLE_NAME + " (" +
                TrackEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                TrackEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                TrackEntry.COLUMN_IS_COMPLETE + " INTEGER, " +
                TrackEntry.COLUMN_DURATION + " TIMESTAMP)";

        sqLiteDatabase.execSQL(SQL_CREATE_TRACK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase,
                          int i,
                          int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TrackEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
