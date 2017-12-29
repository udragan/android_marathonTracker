package com.udragan.android.marathontracker.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.udragan.android.marathontracker.fragments.PreferencesFragment;

/**
 * Activity for displaying and manipulating application preferences.
 */
public class PreferencesActivity extends Activity {

    // overrides ********************************************************************************************************

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PreferencesFragment())
                .commit();
    }
}
