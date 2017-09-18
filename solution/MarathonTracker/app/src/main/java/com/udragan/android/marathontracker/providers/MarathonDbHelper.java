package com.udragan.android.marathontracker.providers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Extension of {@link android.database.sqlite.SQLiteOpenHelper} for Marathon database.
 */
public class MarathonDbHelper extends SQLiteOpenHelper {

    // members **********************************************************************************************************

    private static final String TAG = MarathonDbHelper.class.getSimpleName();
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
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);

        if (!db.isReadOnly()) {
            db.setForeignKeyConstraintsEnabled(true);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d(TAG, "onCreate.");
        final String SQL_CREATE_TRACKS_TABLE = "CREATE TABLE " + MarathonContract.TrackEntry.TABLE_NAME + " (" +
                MarathonContract.TrackEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MarathonContract.TrackEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                MarathonContract.TrackEntry.COLUMN_IS_COMPLETE + " INTEGER, " +
                MarathonContract.TrackEntry.COLUMN_DURATION + " TIMESTAMP)";
        final String SQL_CREATE_CHECKPOINTS_TABLE = "CREATE TABLE " + MarathonContract.CheckpointEntry.TABLE_NAME + " (" +
                MarathonContract.CheckpointEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MarathonContract.CheckpointEntry.COLUMN_NAME + " TEXT NOT NULL," +
                MarathonContract.CheckpointEntry.COLUMN_INDEX + " INTEGER, " +
                MarathonContract.CheckpointEntry.COLUMN_LATITUDE + " REAL, " +
                MarathonContract.CheckpointEntry.COLUMN_LONGITUDE + " REAL, " +
                MarathonContract.CheckpointEntry.COLUMN_IS_CHECKED + " INTEGER, " +
                MarathonContract.CheckpointEntry.COLUMN_TIME + " TIMESTAMP, " +
                MarathonContract.CheckpointEntry.COLUMN_FC_TRACK_ID + " INTEGER," +
                "FOREIGN KEY(" + MarathonContract.CheckpointEntry.COLUMN_FC_TRACK_ID +
                ") REFERENCES " + MarathonContract.TrackEntry.TABLE_NAME + "(" + MarathonContract.TrackEntry._ID + ")" +
                " ON DELETE CASCADE)";

        Log.v(TAG, String.format("Executing sql: %s",
                SQL_CREATE_TRACKS_TABLE));
        sqLiteDatabase.execSQL(SQL_CREATE_TRACKS_TABLE);
        Log.v(TAG, String.format("Executing sql: %s",
                SQL_CREATE_CHECKPOINTS_TABLE));
        sqLiteDatabase.execSQL(SQL_CREATE_CHECKPOINTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase,
                          int i,
                          int i1) {
        Log.d(TAG, "onUpgrade.");
        final String SQL_DROP_CHECKPOINTS_TABLE = "DROP TABLE IF EXISTS " +
                MarathonContract.CheckpointEntry.TABLE_NAME;
        final String SQL_DROP_TRACKS_TABLE = "DROP TABLE IF EXISTS " +
                MarathonContract.TrackEntry.TABLE_NAME;

        Log.v(TAG, String.format("Executing sql: %s",
                SQL_DROP_CHECKPOINTS_TABLE));
        sqLiteDatabase.execSQL(SQL_DROP_CHECKPOINTS_TABLE);
        Log.v(TAG, String.format("Executing sql: %s",
                SQL_DROP_TRACKS_TABLE));
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MarathonContract.TrackEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
