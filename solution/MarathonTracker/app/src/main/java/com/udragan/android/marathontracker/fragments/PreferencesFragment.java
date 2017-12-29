package com.udragan.android.marathontracker.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.udragan.android.marathontracker.R;

/**
 * Preferences fragment.
 */
public class PreferencesFragment extends PreferenceFragment {

    // overrides ********************************************************************************************************

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
