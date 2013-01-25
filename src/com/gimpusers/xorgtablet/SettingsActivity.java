package com.gimpusers.xorgtablet;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
    public static final String
		KEY_PREF_HOST = "host_preference",
		KEY_PREF_TOUCH = "touch_input_preference",
		KEY_PREF_TOUCH_ABSOLUTE = "absolute_motion_preference",
		KEY_PREF_PRESSURE_THRESHOLD = "pressure_threshold_preference";

    @SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.tablet_preferences);
    }
}
