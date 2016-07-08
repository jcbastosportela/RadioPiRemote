package apps.porty.radiopiremote;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceActivity;
import android.util.Log;

/**
 * Created by porty on 7/8/16.
 */

public class PreferencesActivity extends PreferenceActivity
{
    private static String KEY_PREF_RADIO_PI_IP;
    private static String KEY_PREF_RADIO_PI_PORT;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /* init the preferences keys */
        KEY_PREF_RADIO_PI_IP = getText(R.string.radio_pi_IP).toString();
        KEY_PREF_RADIO_PI_PORT = getText(R.string.radio_pi_PORT).toString();

        getFragmentManager().beginTransaction().replace(android.R.id.content, new RadioPiPreferences()).commit();
    }

    @Override
    protected boolean isValidFragment(String fragmentName)
    {
        return RadioPiPreferences.class.getName().equals(fragmentName);
    }

    public static class RadioPiPreferences extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.settings);

            SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
            sp.registerOnSharedPreferenceChangeListener(this);
        }

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
        {
            /* if the modifications relate to the Connection */
            if (key.equals(PreferencesActivity.KEY_PREF_RADIO_PI_IP) ||
                    key.equals(PreferencesActivity.KEY_PREF_RADIO_PI_PORT))
            {
                Preference connectionPref = findPreference(key);
                // Set summary to be the user-description for the selected value
                connectionPref.setSummary(sharedPreferences.getString(key, ""));
                String IP = sharedPreferences.getString(PreferencesActivity.KEY_PREF_RADIO_PI_IP, "");
                int Port = Integer.parseInt(sharedPreferences.getString(PreferencesActivity.KEY_PREF_RADIO_PI_PORT, ""));
                Log.d("Prefs", "Attemmpting new connection to " + IP + Port);
                MainActivity.conn.reconnect(IP, Port);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }
    }
}