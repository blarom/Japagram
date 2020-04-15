package com.japagram.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.japagram.R;
import com.japagram.asynctasks.RoomDatabasesInstallationForegroundService;
import com.japagram.resources.Globals;
import com.japagram.resources.LocaleHelper;
import com.japagram.resources.Utilities;
import com.japagram.resources.UtilitiesPrefs;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_panel);

        PreferenceScreen prefScreen = getPreferenceScreen();
        SharedPreferences sharedPreferences = prefScreen.getSharedPreferences();

        // Setting the roomInstancesAsyncResponseHandler on the Langage Preference
        for (int i = 0; i < prefScreen.getPreferenceCount(); i++) {
            Preference currentPreference = prefScreen.getPreference(i);
            if (currentPreference instanceof ListPreference) {
                setSummaryForPreference(currentPreference, sharedPreferences);

                if (currentPreference.getKey().equals(getString(R.string.pref_app_language_key))) {
                    setLanguage(currentPreference);
                }
                else if (currentPreference.getKey().equals(getString(R.string.pref_app_theme_color_key))) {
                    setThemeColor(currentPreference);
                }
            }
        }

        // Go through all of the preferences, and set up their characteristics.
        for (int i = 0; i < prefScreen.getPreferenceCount(); i++) {
            Preference currentPreference = prefScreen.getPreference(i);
            if (currentPreference instanceof ListPreference || currentPreference instanceof EditTextPreference) {
                setSummaryForPreference(currentPreference, sharedPreferences);
                setTitleForPreference(currentPreference);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (getActivity() == null) return;
        Preference currentPreference = findPreference(key);
        if (currentPreference != null) {
            if (currentPreference instanceof EditTextPreference) {
                checkIfValueIsInRangeOrWarnUser(currentPreference, sharedPreferences);
                setSummaryForPreference(currentPreference, sharedPreferences);

            }
            else if (currentPreference instanceof ListPreference) {
                setSummaryForPreference(currentPreference, sharedPreferences);
                if (currentPreference.getKey().equals(getString(R.string.pref_app_language_key))) {
                    setLanguage(currentPreference);
                    if (getActivity() != null) Utilities.restartApplication(getActivity());
                }
                else if (currentPreference.getKey().equals(getString(R.string.pref_app_theme_color_key))) {
                    setThemeColor(currentPreference);
                    if (getActivity() != null) Utilities.restartApplication(getActivity());
                }
            }
            else if (currentPreference.getKey().equals(getString(R.string.pref_complete_local_with_names_search_key))) {
                boolean finishedLoadingNamesDatabase = UtilitiesPrefs.getAppPreferenceNamesDatabasesFinishedLoadingFlag(getActivity());
                boolean showNames = sharedPreferences.getBoolean(getString(R.string.pref_complete_local_with_names_search_key), false);
                if (!finishedLoadingNamesDatabase && showNames) showNamesDbDownloadDialog((CheckBoxPreference) currentPreference, sharedPreferences);
            }
        }
    }
    private void setLanguage(Preference currentPreference) {
        String language = ((ListPreference) currentPreference).getValue();
        String languageCode = Globals.LANGUAGE_CODE_MAP.get(language);
        String currentLanguageCode = LocaleHelper.getLanguage(getContext());
        if (!currentLanguageCode.equals(languageCode)) {
            LocaleHelper.setLocale(getContext(), languageCode);
        }
    }
    private void setThemeColor(Preference currentPreference) {
        String themeColor = ((ListPreference) currentPreference).getValue();
        if (getActivity() == null || getContext() == null) return;
        UtilitiesPrefs.setAppPreferenceColorTheme(getContext(), themeColor);
    }
    private void checkIfValueIsInRangeOrWarnUser(Preference preference, SharedPreferences sharedPreferences) {
        String newValue = sharedPreferences.getString(preference.getKey(), "");
        Toast error;
        if (preference.getKey().equals(getString(R.string.pref_OCR_image_contrast_key))) {
            try {
                float contrast = Float.parseFloat(newValue);
                error = Toast.makeText(getContext(), getString(R.string.pref_cannot_set_value_outside_range) + " [ "
                        + getString(R.string.pref_OCR_image_contrast_min_display_value) + " : "
                        + getString(R.string.pref_OCR_image_contrast_max_display_value) + " ].", Toast.LENGTH_SHORT);
                if (contrast > Float.valueOf(getString(R.string.pref_OCR_image_contrast_max_display_value))) {
                    sharedPreferences.edit().putString(preference.getKey(), getString(R.string.pref_OCR_image_contrast_max_display_value)).apply();
                    error.show();
                }
                else if (contrast < Float.valueOf(getString(R.string.pref_OCR_image_contrast_min_display_value))) {
                    sharedPreferences.edit().putString(preference.getKey(), getString(R.string.pref_OCR_image_contrast_min_display_value)).apply();
                    error.show();
                }
            } catch (NumberFormatException nfe) {
                sharedPreferences.edit().putString(preference.getKey(), getString(R.string.pref_OCR_image_contrast_default_value)).apply();
                error = Toast.makeText(getContext(), R.string.pref_invalid_input_default_set, Toast.LENGTH_SHORT);
                error.show();
            }
        }
        else if (preference.getKey().equals(getString(R.string.pref_OCR_image_saturation_key))) {
            try {
                float saturation = Float.parseFloat(newValue);
                error = Toast.makeText(getContext(), getString(R.string.pref_cannot_set_value_outside_range) + " [ "
                        + getString(R.string.pref_OCR_image_saturation_min_display_value) + " : "
                        + getString(R.string.pref_OCR_image_saturation_max_display_value) + " ].", Toast.LENGTH_SHORT);
                if (saturation > Float.valueOf(getString(R.string.pref_OCR_image_saturation_max_display_value))) {
                    sharedPreferences.edit().putString(preference.getKey(), getString(R.string.pref_OCR_image_saturation_max_display_value)).apply();
                    error.show();
                }
                else if (saturation < Float.valueOf(getString(R.string.pref_OCR_image_saturation_min_display_value))) {
                    sharedPreferences.edit().putString(preference.getKey(), getString(R.string.pref_OCR_image_saturation_min_display_value)).apply();
                    error.show();
                }
            } catch (NumberFormatException nfe) {
                sharedPreferences.edit().putString(preference.getKey(), getString(R.string.pref_OCR_image_saturation_default_value)).apply();
                error = Toast.makeText(getContext(), R.string.pref_invalid_input_default_set, Toast.LENGTH_SHORT);
                error.show();
            }
        }
        else if (preference.getKey().equals(getString(R.string.pref_OCR_image_brightness_key))) {

            try {
                float brightness = Float.parseFloat(newValue);
                error = Toast.makeText(getContext(), getString(R.string.pref_cannot_set_value_outside_range) + " [ "
                        + getString(R.string.pref_OCR_image_brightness_min_display_value) + " : "
                        + getString(R.string.pref_OCR_image_brightness_max_display_value) + " ].", Toast.LENGTH_SHORT);
                if (brightness > Float.valueOf(getString(R.string.pref_OCR_image_brightness_max_display_value))) {
                    sharedPreferences.edit().putString(preference.getKey(), getString(R.string.pref_OCR_image_brightness_max_display_value)).apply();
                    error.show();
                }
                else if (brightness < Float.valueOf(getString(R.string.pref_OCR_image_brightness_min_display_value))) {
                    sharedPreferences.edit().putString(preference.getKey(), getString(R.string.pref_OCR_image_brightness_min_display_value)).apply();
                    error.show();
                }
            } catch (NumberFormatException nfe) {
                sharedPreferences.edit().putString(preference.getKey(), getString(R.string.pref_OCR_image_brightness_default_value)).apply();
                error = Toast.makeText(getContext(), R.string.pref_invalid_input_default_set, Toast.LENGTH_SHORT);
                error.show();
            }
        }
        else if (preference.getKey().equals(getString(R.string.pref_query_history_size_key))) {

            try {
                int size = Integer.parseInt(newValue);
                error = Toast.makeText(getContext(), getString(R.string.pref_cannot_set_value_outside_range) + " [ "
                        + getString(R.string.pref_query_history_size_min_display_value) + " : "
                        + getString(R.string.pref_query_history_size_max_display_value) + " ].", Toast.LENGTH_SHORT);
                if (size > Integer.parseInt(getString(R.string.pref_query_history_size_max_display_value))) {
                    sharedPreferences.edit().putString(preference.getKey(), getString(R.string.pref_query_history_size_max_display_value)).apply();
                    error.show();
                }
                else if (size < Integer.parseInt(getString(R.string.pref_query_history_size_min_display_value))) {
                    sharedPreferences.edit().putString(preference.getKey(), getString(R.string.pref_query_history_size_min_display_value)).apply();
                    error.show();
                }
            } catch (Exception nfe) {
                sharedPreferences.edit().putString(preference.getKey(), getString(R.string.pref_query_history_size_default_value)).apply();
                error = Toast.makeText(getContext(), R.string.pref_invalid_input_default_set, Toast.LENGTH_SHORT);
                error.show();
            }
        }
    }
    private void setSummaryForPreference(Preference currentPreference, SharedPreferences sharedPreferences) {

        String currentPreferenceValue = sharedPreferences.getString(currentPreference.getKey(), "");
        if (currentPreference instanceof ListPreference) {
            ListPreference currentListPreference = (ListPreference) currentPreference;
            int prefIndex = currentListPreference.findIndexOfValue(currentPreferenceValue);
            if (prefIndex >= 0)  currentListPreference.setSummary(currentListPreference.getEntries()[prefIndex]);
        }
        else if (currentPreference instanceof EditTextPreference) {
            // For EditTextPreferences, set the summary to the value's simple string representation.
            currentPreference.setSummary(getString(R.string.value_) + sharedPreferences.getString(currentPreference.getKey(), ""));
        }
    }
    private void setTitleForPreference(Preference currentPreference) {

        if (currentPreference instanceof EditTextPreference) {
            if (currentPreference.getKey().equals(getString(R.string.pref_OCR_image_contrast_key))) {
                currentPreference.setTitle(getString(R.string.pref_OCR_image_contrast_title) + " ["
                        + getString(R.string.pref_OCR_image_contrast_min_display_value) + ":"
                        + getString(R.string.pref_OCR_image_contrast_max_display_value) + "]");
            }
            else if (currentPreference.getKey().equals(getString(R.string.pref_OCR_image_saturation_key))) {
                currentPreference.setTitle(getString(R.string.pref_OCR_image_saturation_title) + " ["
                        + getString(R.string.pref_OCR_image_saturation_min_display_value) + ":"
                        + getString(R.string.pref_OCR_image_saturation_max_display_value) + "]");

            }
            else if (currentPreference.getKey().equals(getString(R.string.pref_OCR_image_brightness_key))) {
                currentPreference.setTitle(getString(R.string.pref_OCR_image_brightness_title) + " ["
                        + getString(R.string.pref_OCR_image_brightness_min_display_value) + ":"
                        + getString(R.string.pref_OCR_image_brightness_max_display_value) + "]");

            }
        }
    }

    private void startDbInstallationForegroundService() {
        if (getActivity() == null) return;
        Intent serviceIntent = new Intent(getActivity(), RoomDatabasesInstallationForegroundService.class);
        serviceIntent.putExtra(getString(R.string.show_names), true);
        serviceIntent.putExtra(getString(R.string.install_extended_db), false);
        serviceIntent.putExtra(getString(R.string.install_names_db), true);
        getActivity().startService(serviceIntent);

    }
    private void showNamesDbDownloadDialog(final CheckBoxPreference currentPreference, final SharedPreferences sharedPreferences) {
        if (getActivity() == null) return;
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setMessage(getString(R.string.confirm_download_names_db));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes),
                (dialog, which) -> {
                    startDbInstallationForegroundService();
                    dialog.dismiss();
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel),
                (dialog, which) -> {
                    if (getContext()!=null) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(getContext().getString(R.string.pref_complete_local_with_names_search_key), false);
                        editor.apply();
                        currentPreference.setChecked(false);
                    }
                    dialog.dismiss();
                });
        alertDialog.show();
    }

}