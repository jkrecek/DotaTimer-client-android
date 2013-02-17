package com.frca.dotatimer;

import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import com.frca.dotatimer.helper.InstanceHolder;
import com.frca.dotatimer.helper.Preferences;
import com.frca.dotatimer.helper.Preferences.Key;
import com.frca.dotatimer.helper.Range;

public class SettingsActivity extends PreferenceActivity {

    private static final boolean ALWAYS_SIMPLE_PREFS = false;

    public Preference.OnPreferenceChangeListener sPreferenceListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpChangeListener();
    }

    @Override
    protected void onResume() {
        super.onResume();

        InstanceHolder.setActivityOnTop(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        InstanceHolder.setActivityOnBackground(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }

    @SuppressWarnings("deprecation")
    private void setupSimplePreferencesScreen() {
        if (!isSimplePreferences(this)) {
            return;
        }

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);
        // getFragmentManager().beginTransaction().add(new GeneralPreferenceFragment(), "general").commit();

        /*// Add 'connection' preferences, and a corresponding header.
        PreferenceCategory fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_connection);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_connection);

        // Add 'sync' preferences, and a corresponding header.
        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_data_sync);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_data_sync);*/

        // Bind preference
        bindPreferences(null, this, null);
    }

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB || !isXLargeTablet(context);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    private void setUpChangeListener() {
        sPreferenceListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                String stringValue = value.toString();
                // String stringKey = preference.getKey();

                if (preference instanceof ListPreference) {
                    ListPreference listPreference = (ListPreference) preference;
                    int index = listPreference.findIndexOfValue(stringValue);

                    preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

                } else
                    preference.setSummary(stringValue);

                return true;
            }
        };
    }

    @SuppressWarnings("deprecation")
    private static void bindPreferences(Range range, SettingsActivity activity, PreferenceFragment fragment) {

        int min = range != null ? range.min() : 0;
        int max = range != null ? range.max() : 99;

        for (int i = min; i <= max;) {
            Key pk = Key.fromInt(i);
            if (pk == null) {
                if (i % 10 != 0) {
                    i = (i / 10 + 1) * 10;
                } else
                    break;
            } else {
                if (fragment != null)
                    activity.bindPreferenceSummaryToValue(fragment.findPreference(pk.getName()));
                else
                    activity.bindPreferenceSummaryToValue(activity.findPreference(pk.getName()));
                ++i;
            }

        }
    }

    public void bindPreferenceSummaryToValue(Preference preference) {
        if (preference == null)
            return;

        preference.setOnPreferenceChangeListener(sPreferenceListener);

        sPreferenceListener.onPreferenceChange(preference, Preferences.getPreferences(preference.getContext()).getString(preference.getKey()));
    }

    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            bindPreferences(new Range(0, 10), (SettingsActivity) getActivity(), this);
        }
    }

    /*
    public static class ConnectionPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_connection);
            bindPreferences(new Range(10, 20), (SettingsActivity) getActivity(), this);
        }
    }

    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);
            bindPreferences(new Range(20, 30), (SettingsActivity) getActivity(), this);
        }
    }*/
}
