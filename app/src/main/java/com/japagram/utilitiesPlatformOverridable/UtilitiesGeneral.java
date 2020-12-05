package com.japagram.utilitiesPlatformOverridable;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

public class UtilitiesGeneral {
    public static void printLog(String tag, String text) {
        Log.i(tag, text);
    }
    public static String joinList(String separator, List<String> list) {
        return TextUtils.join(separator, list);
    }
    public static boolean isEmptyString(String text) {
        return TextUtils.isEmpty(text);
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }
}
