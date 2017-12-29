package com.udragan.android.marathontracker.infrastructure.preferences;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

/**
 * Custom preference control that also shows the value of the text preference.
 */
public class EditTextPreferenceWithValue extends EditTextPreference {

    // constructors *****************************************************************************************************

    /**
     * Initializes a new instance of {@link com.udragan.android.marathontracker.infrastructure.preferences.EditTextPreferenceWithValue} class.
     *
     * @param context the context.
     * @param attrs   attribute set.
     */
    public EditTextPreferenceWithValue(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // overrides ********************************************************************************************************

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            setSummary(getSummary());
        }
    }

    @Override
    public CharSequence getSummary() {
        return this.getText();
    }
}
