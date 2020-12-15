package com.japagram.utilitiesPlatformOverridable;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class OverridableUtilitiesGeneral {
    public static void printLog(String tag, String text) {
        Log.i(tag, text);
    }
    public static String joinList(String separator, List<String> list) {
        return TextUtils.join(separator, list);
    }
    public static boolean arraysIntersect(List<String> list1, List<String> list2) {
        return false;
    }
    public static boolean isEmptyString(String text) {
        return TextUtils.isEmpty(text);
    }
    public static String[] splitToChars(String text) {
        return text.split("(?!^)");
    }
    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }

    //String manipulation utilities
    @NotNull
    public static String convertToUTF8Index(String input_string) {
        byte[] byteArray;
        byteArray = input_string.getBytes(StandardCharsets.UTF_8);
        StringBuilder prepared_word = new StringBuilder("1.");
        for (byte b : byteArray) {
            prepared_word.append(Integer.toHexString(b & 0xFF));
        }
        return prepared_word.toString();
    }

    @NotNull
    public static String convertFromUTF8Index(@NotNull String inputHex) {

        //inspired by: https://stackoverflow.com/questions/15749475/java-string-hex-to-string-ascii-with-accentuation
        if (inputHex.length() < 4) return "";
        inputHex = inputHex.toLowerCase().substring(2, inputHex.length());

        ByteBuffer buff = ByteBuffer.allocate(inputHex.length() / 2);
        for (int i = 0; i < inputHex.length(); i += 2) {
            buff.put((byte) Integer.parseInt(inputHex.substring(i, i + 2), 16));
        }
        buff.rewind();
        Charset cs;
        CharBuffer cb;
        cs = StandardCharsets.UTF_8;
        cb = cs.decode(buff);
        return cb.toString();
    }

    @NotNull
    public static String getHexId(@NotNull String word) {
        byte[] bytes;
        StringBuilder sb = new StringBuilder();
        bytes = word.getBytes(StandardCharsets.UTF_8);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return "1." + sb.toString().toUpperCase();
    }

    public static String getStringFromUTF8(@NotNull String word) {

        String hex = word.substring(2);
        ByteBuffer buff = ByteBuffer.allocate(hex.length()/2);
        for (int i = 0; i < hex.length(); i+=2) {
            buff.put((byte)Integer.parseInt(hex.substring(i, i+2), 16));
        }
        buff.rewind();
        Charset cs = StandardCharsets.UTF_8;
        CharBuffer cb = cs.decode(buff);
        String stringValueOfHex = cb.toString();

        return stringValueOfHex;
    }

}
