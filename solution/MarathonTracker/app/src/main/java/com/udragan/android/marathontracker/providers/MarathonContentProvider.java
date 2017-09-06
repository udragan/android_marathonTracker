package com.udragan.android.marathontracker.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Extension of {@link android.content.ContentProvider} for Marathon provider.
 */
public class MarathonContentProvider extends ContentProvider {

    // members **********************************************************************************************************

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
    public Cursor query(@NonNull Uri uri,
                        @Nullable String[] strings,
                        @Nullable String s,
                        @Nullable String[] strings1,
                        @Nullable String s1) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri,
                      @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri,
                      @Nullable String s,
                      @Nullable String[] strings) {
        return 0;
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
