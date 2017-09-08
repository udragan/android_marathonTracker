package com.udragan.android.marathontracker.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.udragan.android.marathontracker.providers.MarathonContract.TrackEntry;

/**
 * Extension of {@link android.content.ContentProvider} for Marathon provider.
 */
public class MarathonContentProvider extends ContentProvider {

    // members **********************************************************************************************************

    private static final int TRACKS = 100;
    private static final int TRACKS_BY_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(MarathonContract.AUTHORITY, MarathonContract.PATH_TRACKS, TRACKS);
        sUriMatcher.addURI(MarathonContract.AUTHORITY, MarathonContract.PATH_TRACKS + "/#", TRACKS_BY_ID);
    }

    private MarathonDbHelper mMarathonDbHelper;

    // overrides ********************************************************************************************************

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mMarathonDbHelper = new MarathonDbHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri,
                      @Nullable ContentValues contentValues) {
        final SQLiteDatabase db = mMarathonDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case TRACKS:
                long id = db.insert(TrackEntry.TABLE_NAME, null, contentValues);

                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(TrackEntry.CONTENT_URI, id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }

                break;

            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        //noinspection ConstantConditions
        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,
                        @Nullable String[] projection,
                        @Nullable String selection,
                        @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        final SQLiteDatabase db = mMarathonDbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor cursor;

        switch (match) {
            case TRACKS:
                cursor = db.query(TrackEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case TRACKS_BY_ID:
                String id = uri.getPathSegments().get(1);
                cursor = db.query(TrackEntry.TABLE_NAME,
                        projection,
                        "_id=?",
                        new String[]{id},
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        //noinspection ConstantConditions
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public int delete(@NonNull Uri uri,
                      @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mMarathonDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int noOfDeleted;
        
        switch (match) {
            case TRACKS:
                noOfDeleted = db.delete(TrackEntry.TABLE_NAME,
                        "",
                        null);
                break;

            case TRACKS_BY_ID:
                String id = uri.getPathSegments().get(1);
                noOfDeleted = db.delete(TrackEntry.TABLE_NAME,
                        "_id=?",
                        new String[]{id});
                break;

            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        if (noOfDeleted != 0) {
            //noinspection ConstantConditions
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return noOfDeleted;
    }

    @Override
    public int update(@NonNull Uri uri,
                      @Nullable ContentValues contentValues,
                      @Nullable String s,
                      @Nullable String[] strings) {
        return 0;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }
}
