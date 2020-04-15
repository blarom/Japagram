package com.japagram.resources;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.util.TypedValue;

import com.japagram.R;

import androidx.annotation.ColorInt;
import androidx.preference.PreferenceManager;

public final class UtilitiesPrefs {
    private UtilitiesPrefs() {
    }

    //Preference utilities
    public static boolean getPreferenceShowNames(Activity activity) {
        boolean state = false;
        if (activity != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
            state = sharedPreferences.getBoolean(activity.getString(R.string.pref_complete_local_with_names_search_key),
                    activity.getResources().getBoolean(R.bool.pref_complete_local_with_names_search_default));
        }
        return state;
    }

    public static boolean getPreferenceShowOnlineResults(Activity activity) {
        boolean state = false;
        if (activity != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
            state = sharedPreferences.getBoolean(activity.getString(R.string.pref_complete_local_with_online_search_key),
                    activity.getResources().getBoolean(R.bool.pref_complete_local_with_online_search_default));
        }
        return state;
    }

    public static boolean getPreferenceWaitForOnlineResults(Activity activity) {
        boolean state = false;
        if (activity != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
            state = sharedPreferences.getBoolean(activity.getString(R.string.pref_wait_for_online_results_key),
                    activity.getResources().getBoolean(R.bool.pref_wait_for_online_results_default));
        }
        return state;
    }

    public static boolean getPreferenceShowConjResults(Activity activity) {
        boolean state = false;
        if (activity != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
            state = sharedPreferences.getBoolean(activity.getString(R.string.pref_complete_with_conj_search_key),
                    activity.getResources().getBoolean(R.bool.pref_complete_with_conj_search_default));
        }
        return state;
    }

    public static boolean getPreferenceWaitForConjResults(Activity activity) {
        boolean state = false;
        if (activity != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
            state = sharedPreferences.getBoolean(activity.getString(R.string.pref_wait_for_conj_results_key),
                    activity.getResources().getBoolean(R.bool.pref_wait_for_conj_results_default));
        }
        return state;
    }

    public static boolean getPreferenceShowSources(Activity activity) {
        boolean state = false;
        if (activity != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
            state = sharedPreferences.getBoolean(activity.getString(R.string.pref_show_sources_key),
                    activity.getResources().getBoolean(R.bool.pref_show_sources_default));
        }
        return state;
    }

    public static Boolean getPreferenceShowInfoBoxesOnSearch(Activity activity) {
        boolean showInfoBoxesOnSearch = false;
        if (activity != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
            showInfoBoxesOnSearch = sharedPreferences.getBoolean(activity.getString(R.string.pref_show_info_boxes_on_search_key),
                    activity.getResources().getBoolean(R.bool.pref_show_info_boxes_on_search_default));
        }
        return showInfoBoxesOnSearch;
    }

    public static Boolean getPreferenceUseJapaneseFont(Activity activity) {
        boolean state = false;
        if (activity != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
            state = sharedPreferences.getBoolean(activity.getString(R.string.pref_use_japanese_font_key),
                    activity.getResources().getBoolean(R.bool.pref_use_japanese_font_default));
        }
        return state;
    }

    public static Boolean getPreferenceShowDecompKanjiStructureInfo(Activity activity) {
        boolean showDecompStructureInfo = false;
        if (activity != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
            showDecompStructureInfo = sharedPreferences.getBoolean(activity.getString(R.string.pref_show_decomp_structure_info_key),
                    activity.getResources().getBoolean(R.bool.pref_show_decomp_structure_info_default));
        }
        return showDecompStructureInfo;
    }

    public static int getPreferenceQueryHistorySize(Context context) {
        if (context == null) return 0;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int queryHistorySize = Integer.parseInt(context.getResources().getString(R.string.pref_query_history_size_default_value));
        try {
            String queryHist = sharedPreferences.getString(context.getResources().getString(R.string.pref_query_history_size_key), context.getResources().getString(R.string.pref_query_history_size_default_value));
            queryHistorySize = Integer.parseInt(queryHist);
        } catch (Exception e) {
            queryHistorySize = Integer.parseInt(context.getResources().getString(R.string.pref_query_history_size_default_value));
        } finally {
            queryHistorySize = Utilities.truncateIntToRange(queryHistorySize,
                    Integer.parseInt(context.getResources().getString(R.string.pref_query_history_size_min_value)),
                    Integer.parseInt(context.getResources().getString(R.string.pref_query_history_size_max_value)));
        }
        return queryHistorySize;
    }

    public static void setAppPreferenceKanjiDatabaseFinishedLoadingFlag(Context context, boolean flag) {
        if (context != null) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(context.getString(R.string.database_finished_loading_flag), flag);
            editor.apply();
        }
    }

    public static boolean getAppPreferenceKanjiDatabaseFinishedLoadingFlag(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
        return sharedPref.getBoolean(context.getString(R.string.database_finished_loading_flag), false);
    }

    public static void setAppPreferenceWordVerbDatabasesFinishedLoadingFlag(Context context, boolean flag) {
        if (context != null) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(context.getString(R.string.word_and_verb_database_finished_loading_flag), flag);
            editor.apply();
        }
    }

    public static boolean getAppPreferenceWordVerbDatabasesFinishedLoadingFlag(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
        return sharedPref.getBoolean(context.getString(R.string.word_and_verb_database_finished_loading_flag), false);
    }

    public static void setAppPreferenceExtendedDatabaseFinishedLoadingFlag(Context context, boolean flag) {
        if (context != null) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(context.getString(R.string.extended_database_finished_loading_flag), flag);
            editor.apply();
        }
    }

    public static boolean getAppPreferenceExtendedDatabasesFinishedLoadingFlag(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
        return sharedPref.getBoolean(context.getString(R.string.extended_database_finished_loading_flag), false);
    }

    public static void setAppPreferenceNamesDatabasesFinishedLoadingFlag(Context context, boolean flag) {
        if (context != null) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(context.getString(R.string.names_database_finished_loading_flag), flag);
            editor.apply();
        }
    }

    public static boolean getAppPreferenceNamesDatabasesFinishedLoadingFlag(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
        return sharedPref.getBoolean(context.getString(R.string.names_database_finished_loading_flag), false);
    }

    public static void setAppPreferenceFirstTimeRunningApp(Context context, boolean flag) {
        if (context != null) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(context.getString(R.string.first_time_installing_flag), flag);
            editor.apply();
        }
    }

    public static boolean getAppPreferenceFirstTimeRunningApp(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
        return sharedPref.getBoolean(context.getString(R.string.first_time_installing_flag), true);
    }

    public static void setAppPreferenceCompleteWithNames(Context context, boolean flag) {
        if (context != null) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(context.getString(R.string.pref_search_names_no_matter_what), flag);
            editor.apply();
        }
    }

    public static boolean getAppPreferenceCompleteWithNames(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
        return sharedPref.getBoolean(context.getString(R.string.pref_search_names_no_matter_what), true);
    }

    public static void setAppPreferenceDbVersionCentral(Context context, int value) {
        if (context != null) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(context.getString(R.string.pref_db_version_central), value);
            editor.apply();
        }
    }

    public static int getAppPreferenceDbVersionCentral(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
        return sharedPref.getInt(context.getString(R.string.pref_db_version_central), 1);
    }

    public static void setAppPreferenceDbVersionKanji(Context context, int value) {
        if (context != null) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(context.getString(R.string.pref_db_version_kanji), value);
            editor.apply();
        }
    }

    public static int getAppPreferenceDbVersionKanji(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
        return sharedPref.getInt(context.getString(R.string.pref_db_version_kanji), 1);
    }

    public static void setAppPreferenceDbVersionExtended(Context context, int value) {
        if (context != null) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(context.getString(R.string.pref_db_version_extended), value);
            editor.apply();
        }
    }

    public static int getAppPreferenceDbVersionExtended(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
        return sharedPref.getInt(context.getString(R.string.pref_db_version_extended), 1);
    }

    public static void setAppPreferenceDbVersionNames(Context context, int value) {
        if (context != null) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(context.getString(R.string.pref_db_version_names), value);
            editor.apply();
        }
    }

    public static int getAppPreferenceDbVersionNames(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
        return sharedPref.getInt(context.getString(R.string.pref_db_version_names), 1);
    }

    public static void setProgressValueExtendedDb(Context context, float value) {
        if (context != null) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putFloat(context.getString(R.string.progress_value_extended_db), value);
            editor.apply();
        }
    }

    public static float getProgressValueExtendedDb(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
        return sharedPref.getFloat(context.getString(R.string.progress_value_extended_db), 0.f);
    }

    public static void setProgressValueNamesDb(Context context, float value) {
        if (context != null) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putFloat(context.getString(R.string.progress_value_names_db), value);
            editor.apply();
        }
    }

    public static float getProgressValueNamesDb(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
        return sharedPref.getFloat(context.getString(R.string.progress_value_names_db), 0.f);
    }

    public static void setAppPreferenceColorTheme(Context context, String theme) {
        if (context != null) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(context.getString(R.string.pref_app_theme_color), theme);
            editor.apply();
        }
    }

    static String getAppPreferenceColorTheme(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
        return sharedPref.getString(context.getString(R.string.pref_app_theme_color), context.getString(R.string.pref_theme_color_value_lightredblack));
    }

    public static boolean changeThemeColor(Activity activity) {

        String themeColor = getAppPreferenceColorTheme(activity);
        boolean changeThemeColor = false;
        try {
            int currentTheme = activity.getPackageManager().getActivityInfo(activity.getComponentName(), 0).getThemeResource();
            if (themeColor.equals(activity.getString(R.string.pref_theme_color_value_lightbluegreen)) && currentTheme != R.style.AppTheme_LightBlueGreen) {
                activity.setTheme(R.style.AppTheme_LightBlueGreen);
                changeThemeColor = true;
            } else if (themeColor.equals(activity.getString(R.string.pref_theme_color_value_lightredblack)) && currentTheme != R.style.AppTheme_LightRedBlack) {
                activity.setTheme(R.style.AppTheme_LightRedBlack);
                changeThemeColor = true;
            }
            return changeThemeColor;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return changeThemeColor;
    }

    public static int getResColorValue(Context context, int res_value) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(res_value, typedValue, true);
        @ColorInt int color = typedValue.data;
        return color;
    }
}
