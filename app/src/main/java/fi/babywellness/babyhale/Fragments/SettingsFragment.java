package fi.babywellness.babyhale.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fi.babywellness.babyhale.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static String PREFERENCES_KEY = "MY_PREFERENCES";
    private static String RESULT_DISPLAY_KEY = "resultDisplay";

    private SharedPreferences preferences;
    private ListPreference resultDisplayPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        preferences = getActivity().getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);

        initPreferenceObjects();
        initDefaultValues();
        updateSummaries();

        resultDisplayPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                writeString(RESULT_DISPLAY_KEY, (String) o);
                updateSummaries();
                return false;
            }
        });

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void initPreferenceObjects() {
        resultDisplayPreference = (ListPreference) getPreferenceManager().findPreference("result_display_preference");
    }

    private void initDefaultValues() {
        if (!preferences.contains(RESULT_DISPLAY_KEY))
            writeString(RESULT_DISPLAY_KEY, getResources().getStringArray(R.array.display_result_values)[0]);
    }

    private void writeString(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private void updateSummaries() {
        String[] values = getResources().getStringArray(R.array.display_result_values);
        for (int i = 0; i < values.length; i++) {
            if (preferences.getString(RESULT_DISPLAY_KEY, "").equals(values[i])) {
                resultDisplayPreference.setSummary(getResources().getStringArray(R.array.display_result_styles)[i]);
            }
        }
    }

    public static boolean areResultsSimplified(Context context) {
        String savedValue = context.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE).getString(RESULT_DISPLAY_KEY, "null");
        return savedValue.equals(context.getResources().getStringArray(R.array.display_result_values)[1]);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) { }
}
