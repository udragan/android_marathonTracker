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
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Extension of {@link android.content.ContentProvider} for Marathon provider.
 */
public class MarathonContentProvider extends ContentProvider {

    // members **********************************************************************************************************

    private static final String TAG = MarathonContentProvider.class.getSimpleName();

    private static final int TRACKS = 100;
    private static final int TRACK_BY_ID = 101;
    private static final int CHECKPOINTS = 200;
    private static final int CHECKPOINT_BY_ID = 201;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(MarathonContract.AUTHORITY, MarathonContract.PATH_TRACKS, TRACKS);
        sUriMatcher.addURI(MarathonContract.AUTHORITY, MarathonContract.PATH_TRACKS + "/#", TRACK_BY_ID);
        sUriMatcher.addURI(MarathonContract.AUTHORITY, MarathonContract.PATH_CHECKPOINTS, CHECKPOINTS);
        sUriMatcher.addURI(MarathonContract.AUTHORITY, MarathonContract.PATH_CHECKPOINTS + "/#", CHECKPOINT_BY_ID);
    }

    private MarathonDbHelper mMarathonDbHelper;

    // overrides ********************************************************************************************************

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mMarathonDbHelper = new MarathonDbHelper(context);
        Log.d(TAG, "onCreate.");

        return true;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri,
                      @Nullable ContentValues contentValues) {
        final SQLiteDatabase db = mMarathonDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri returnUri;
        long id;

        Log.d(TAG, String.format("Insert for uri: %s\nmatch: %d",
                uri, match));
        Log.v(TAG, String.format("Values: %s",
                contentValues));

        switch (match) {
            case TRACKS:
                id = db.insert(MarathonContract.TrackEntry.TABLE_NAME, null, contentValues);

                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(MarathonContract.TrackEntry.CONTENT_URI, id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }

                break;

            case CHECKPOINTS:
                id = db.insert(MarathonContract.CheckpointEntry.TABLE_NAME, null, contentValues);

                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(MarathonContract.CheckpointEntry.CONTENT_URI, id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }

                break;

            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        //noinspection ConstantConditions
        getContext().getContentResolver().notifyChange(uri, null);
        Log.d(TAG, String.format("Inserted %s",
                returnUri));

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
        String id;
        Cursor cursor;

        Log.d(TAG, String.format("Query for uri: %s\nmatch: %d",
                uri, match));
        Log.v(TAG, String.format("projection: %s\nselection: %s\nselectionArgs: %s\nsortOrder: %s",
                Arrays.toString(projection), selection, Arrays.toString(selectionArgs), sortOrder));

        switch (match) {
            case TRACKS:
                cursor = db.query(MarathonContract.TrackEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case TRACK_BY_ID:
                id = uri.getPathSegments().get(1);
                cursor = db.query(MarathonContract.TrackEntry.TABLE_NAME,
                        projection,
                        "_id=?",
                        new String[]{id},
                        null,
                        null,
                        sortOrder);
                break;

            case CHECKPOINTS:
                cursor = db.query(MarathonContract.CheckpointEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case CHECKPOINT_BY_ID:
                id = uri.getPathSegments().get(1);
                cursor = db.query(MarathonContract.CheckpointEntry.TABLE_NAME,
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
        Log.d(TAG, String.format("Fetched: %d",
                cursor.getCount()));

        return cursor;
    }

    @Override
    public int update(@NonNull Uri uri,
                      @Nullable ContentValues values,
                      @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mMarathonDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        String id;
        int noOfUpdated;

        Log.d(TAG, String.format("Update for uri: %s\nmatch: %d",
                uri, match));
        Log.v(TAG, String.format("content: %s\nselection: %s\nselectionArgs: %s",
                values, selection, Arrays.toString(selectionArgs)));

        switch (match) {
            case TRACKS:
                noOfUpdated = db.update(MarathonContract.TrackEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;

            case TRACK_BY_ID:
                if (selection == null) {
                    selection = MarathonContract.TrackEntry._ID + "=?";
                } else {
                    selection += " AND " + MarathonContract.TrackEntry._ID + "=?";
                }

                id = uri.getPathSegments().get(1);

                if (selectionArgs == null) {
                    selectionArgs = new String[]{id};
                } else {
                    ArrayList<String> selectionArgsList = new ArrayList<>();
                    selectionArgsList.addAll(Arrays.asList(selectionArgs));
                    selectionArgsList.add(id);
                    selectionArgs = selectionArgsList.toArray(new String[selectionArgsList.size()]);
                }

                noOfUpdated = db.update(MarathonContract.TrackEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;

            case CHECKPOINTS:
                noOfUpdated = db.update(MarathonContract.CheckpointEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;

            case CHECKPOINT_BY_ID:
                if (selection == null) {
                    selection = MarathonContract.CheckpointEntry._ID + "=?";
                } else {
                    selection += " AND " + MarathonContract.CheckpointEntry._ID + "=?";
                }

                id = uri.getPathSegments().get(1);

                if (selectionArgs == null) {
                    selectionArgs = new String[]{id};
                } else {
                    ArrayList<String> selectionArgsList = new ArrayList<>();
                    selectionArgsList.addAll(Arrays.asList(selectionArgs));
                    selectionArgsList.add(id);
                    selectionArgs = selectionArgsList.toArray(new String[selectionArgsList.size()]);
                }

                noOfUpdated = db.update(MarathonContract.CheckpointEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }

        if (noOfUpdated != 0) {
            //noinspection ConstantConditions
            getContext().getContentResolver().notifyChange(uri, null);
        }

        Log.d(TAG, String.format("Updated: %d",
                noOfUpdated));

        return noOfUpdated;
    }

    @Override
    public int delete(@NonNull Uri uri,
                      @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mMarathonDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        String id;
        int noOfDeleted;

        Log.d(TAG, String.format("Delete for uri: %s\nmatch: %d",
                uri, match));
        Log.v(TAG, String.format("selection: %s\nselectionArgs: %s",
                selection, Arrays.toString(selectionArgs)));

        switch (match) {
            case TRACKS:
                noOfDeleted = db.delete(MarathonContract.TrackEntry.TABLE_NAME,
                        null,
                        null);
                break;

            case TRACK_BY_ID:
                id = uri.getPathSegments().get(1);
                noOfDeleted = db.delete(MarathonContract.TrackEntry.TABLE_NAME,
                        "_id=?",
                        new String[]{id});
                break;

            case CHECKPOINTS:
                noOfDeleted = db.delete(MarathonContract.CheckpointEntry.TABLE_NAME,
                        null,
                        null);
                break;

            case CHECKPOINT_BY_ID:
                id = uri.getPathSegments().get(1);
                noOfDeleted = db.delete(MarathonContract.CheckpointEntry.TABLE_NAME,
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

        Log.d(TAG, String.format("Deleted: %d",
                noOfDeleted));

        return noOfDeleted;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not supported!");
    }
}
