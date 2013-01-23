package com.gimpusers.xorgtablet;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
    public static final String
		KEY_PREF_HOST = "host_preference",
		KEY_PREF_STYLUS_ONLY = "stylus_only_preference",
		KEY_PREF_ABSOLUTE_MOTION = "absolute_motion_preference";

    @SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.tablet_preferences);
    }
}
