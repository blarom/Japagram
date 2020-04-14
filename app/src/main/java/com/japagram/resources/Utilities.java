package com.japagram.resources;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.japagram.R;
import com.japagram.data.IndexEnglish;
import com.japagram.data.IndexEnglishDao;
import com.japagram.data.IndexFrench;
import com.japagram.data.IndexFrenchDao;
import com.japagram.data.IndexKanji;
import com.japagram.data.IndexKanjiDao;
import com.japagram.data.IndexRomaji;
import com.japagram.data.IndexRomajiDao;
import com.japagram.data.IndexSpanish;
import com.japagram.data.IndexSpanishDao;
import com.japagram.data.KanjiCharacter;
import com.japagram.data.KanjiCharacterDao;
import com.japagram.data.KanjiComponent;
import com.japagram.data.KanjiComponentDao;
import com.japagram.data.Word;
import com.japagram.data.WordDao;
import com.japagram.ui.ConvertFragment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public final class Utilities {

    //Constants
    public static final int NUM_COLUMNS_IN_WORDS_CSV_SHEETS = 16;
    private static int runningIndex = 0;
    private static String websiteCodeString = "";

    private Utilities() {
    }

    //Activity operation utilities
    public static void trimCache(Context context) {
        // http://stackoverflow.com/questions/10977288/clear-application-cache-on-exit-in-android
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir != null && dir.delete();
    }

    public static long getAvailableMemory() {
        final Runtime runtime = Runtime.getRuntime();
        final long usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
        final long maxHeapSizeInMB = runtime.maxMemory() / 1048576L;
        final long availHeapSizeInMB = maxHeapSizeInMB - usedMemInMB;
        Log.i("Diagnosis Time", "Available heap size: " + availHeapSizeInMB);
        return availHeapSizeInMB;
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getBaseContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && activity.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    public static void muteSpeaker(Activity activity) {
        if (activity != null) {
            AudioManager mgr = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            if (mgr != null) mgr.setStreamMute(AudioManager.STREAM_SYSTEM, true);
        }
    }

    public static void unmuteSpeaker(Activity activity) {
        if (activity != null) {
            AudioManager mgr = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            if (mgr != null) mgr.setStreamMute(AudioManager.STREAM_SYSTEM, false);
        }
    }

    @NonNull
    public static Boolean checkIfFileExistsInSpecificFolder(File dir, String filename) {

        if (!dir.exists() && dir.mkdirs()) {
            return false;
        }
        if (dir.exists()) {
            String datafilepath = dir + "/" + filename;
            File datafile = new File(datafilepath);

            return datafile.exists();
        }
        return true;
    }

    public static boolean checkStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (activity != null) {
                if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Log.e(Globals.DEBUG_TAG, "You have permission");
                    return true;
                } else {
                    Log.e(Globals.DEBUG_TAG, "You have asked for permission");
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    return false;
                }
            } else return false;
        } else { //you dont need to worry about these stuff below api level 23
            Log.e(Globals.DEBUG_TAG, "You already have the permission");
            return true;
        }
    }

    @NonNull
    public static String formatSize(long size) {
        //https://inducesmile.com/android/how-to-get-android-ram-internal-and-external-memory-information/
        String suffix = null;

        if (size >= 1024) {
            suffix = " KB";
            size /= 1024;
            if (size >= 1024) {
                suffix = " MB";
                size /= 1024;
            }
        }
        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }
        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }

    public static int convertPxToDpi(int pixels, Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(pixels / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static void restartApplication(Activity activity) {

        Intent intent = activity.getPackageManager().getLaunchIntentForPackage(activity.getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            activity.finish();
        }
    }

    @NonNull
    public static Resources getLocalizedResources(Context context, Locale desiredLocale) {
        Configuration conf = context.getResources().getConfiguration();
        conf = new Configuration(conf);
        conf.setLocale(desiredLocale);
        Context localizedContext = context.createConfigurationContext(conf);
        return localizedContext.getResources();
    }

    //Image utilities
    public static Bitmap getBitmapFromUri(Activity activity, Uri resultUri) {
        Bitmap imageToBeDecoded = null;
        try {
            //BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            //bmOptions.inJustDecodeBounds = false;
            //image = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
            if (activity != null) imageToBeDecoded = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), resultUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageToBeDecoded;
    }

    public static Bitmap adjustImageAngleAndScale(Bitmap source, float angle, double scaleFactor) {

        int newWidth = (int) Math.floor(source.getWidth() * scaleFactor);
        int newHeight = (int) Math.floor(source.getHeight() * scaleFactor);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(source, newWidth, newHeight, true);

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true); //rotated Bitmap

    }

    @TargetApi(23)
    public static boolean isPrintable(String c) {
        Paint paint = new Paint();
        //paint.setTypeface(MainActivity.CJK_typeface);
        boolean hasGlyph;
        hasGlyph = paint.hasGlyph(c);
        return hasGlyph;
//            Character.UnicodeBlock block = Character.UnicodeBlock.of( c );
//            return (!Character.isISOControl(c)) &&
//                    block != null &&
//                    block != Character.UnicodeBlock.SPECIALS;
    }

    public static void showAndFadeOutAndHideImage(final ImageView img, int fadeOutDurationMillis) {
        if (img == null) return;

        img.setVisibility(View.VISIBLE);

        //from: https://stackoverflow.com/questions/20782260/making-a-smooth-fade-out-for-imageview-in-android
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(fadeOutDurationMillis);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                img.setVisibility(View.GONE);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }
        });

        img.startAnimation(fadeOut);
    }

    //String manipulation utilities
    public static String convertToUTF8Index(String input_string) {

        byte[] byteArray;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            byteArray = input_string.getBytes(StandardCharsets.UTF_8);
            StringBuilder prepared_word = new StringBuilder("1.");
            for (byte b : byteArray) {
                prepared_word.append(Integer.toHexString(b & 0xFF));
            }
            return prepared_word.toString();
        }
        return "";
    }

    public static String convertFromUTF8Index(String inputHex) {

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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            cs = StandardCharsets.UTF_8;
            cb = cs.decode(buff);
            return cb.toString();
        }
        return "";
    }

    static String removeNonSpaceSpecialCharacters(String sentence) {
        String current_char;
        StringBuilder concatenated_sentence = new StringBuilder();
        for (int index = 0; index < sentence.length(); index++) {
            current_char = Character.toString(sentence.charAt(index));
            if (!(current_char.equals(".")
                    || current_char.equals("-")
                    || current_char.equals("(")
                    || current_char.equals(")")
                    || current_char.equals(":")
                    || current_char.equals("/"))) {
                concatenated_sentence.append(current_char);
            }
        }
        return concatenated_sentence.toString();
    }

    public static String removeSpecialCharacters(String sentence) {
        String current_char;
        StringBuilder concatenated_sentence = new StringBuilder();
        for (int index = 0; index < sentence.length(); index++) {
            current_char = Character.toString(sentence.charAt(index));
            if (!(current_char.equals(" ")
                    || current_char.equals(".")
                    || current_char.equals("-")
                    || current_char.equals("(")
                    || current_char.equals(")")
                    || current_char.equals(":")
                    || current_char.equals("/"))) {
                concatenated_sentence.append(current_char);
            }
        }
        return concatenated_sentence.toString();
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }

    public static String removeDuplicatesFromCommaList(String input_list) {

        boolean is_repeated;
        List<String> parsed_cumulative_meaning_value = Arrays.asList(splitAtCommasOutsideParentheses(input_list));
        List<String> final_cumulative_meaning_value_array = new ArrayList<>();
        String current_value;
        for (int j = 0; j < parsed_cumulative_meaning_value.size(); j++) {
            is_repeated = false;
            current_value = parsed_cumulative_meaning_value.get(j).trim();
            for (String s : final_cumulative_meaning_value_array) {
                if (s.equals(current_value)) {
                    is_repeated = true;
                    break;
                }
            }
            if (!is_repeated) final_cumulative_meaning_value_array.add(current_value);
        }
        return TextUtils.join(", ", final_cumulative_meaning_value_array);
    }

    public static List<String> getIntersectionOfLists(List<String> A, List<String> B) {
        //https://stackoverflow.com/questions/2400838/efficient-intersection-of-component_substructures[2]-liststring-in-java
        List<String> rtnList = new LinkedList<>();
        for (String dto : A) {
            if (B.contains(dto)) {
                rtnList.add(dto);
            }
        }
        return rtnList;
    }

    public static List<String> removeDuplicatesFromList(List<String> list) {

        //https://stackoverflow.com/questions/14040331/remove-duplicate-strings-in-a-list-in-java

        Set<String> set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        Iterator<String> i = list.iterator();
        while (i.hasNext()) {
            String s = i.next();
            if (set.contains(s)) {
                i.remove();
            } else {
                set.add(s);
            }
        }

        return new ArrayList<>(set);
    }

    public static String getVerbRoot(String verb, String family, int type) {
        String root;
        if (verb == null || verb.length() == 0 || family == null || family.length() == 0) {
            return "";
        }

        if (type == Globals.TYPE_LATIN) {
            switch (family) {
                case Globals.VERB_FAMILY_BU_GODAN:
                case Globals.VERB_FAMILY_GU_GODAN:
                case Globals.VERB_FAMILY_KU_GODAN:
                case Globals.VERB_FAMILY_IKU_SPECIAL:
                case Globals.VERB_FAMILY_YUKU_SPECIAL:
                case Globals.VERB_FAMILY_MU_GODAN:
                case Globals.VERB_FAMILY_NU_GODAN:
                case Globals.VERB_FAMILY_RU_GODAN:
                case Globals.VERB_FAMILY_ARU_SPECIAL:
                case Globals.VERB_FAMILY_SU_GODAN:
                case Globals.VERB_FAMILY_RU_ICHIDAN:
                    root = verb.substring(0, verb.length() - 2);
                    break;
                case Globals.VERB_FAMILY_TSU_GODAN:
                    root = verb.substring(0, verb.length() - 3);
                    break;
                case Globals.VERB_FAMILY_U_GODAN:
                case Globals.VERB_FAMILY_U_SPECIAL:
                    root = verb.substring(0, verb.length() - 1);
                    break;
                case Globals.VERB_FAMILY_SURU:
                case Globals.VERB_FAMILY_KURU:
                    root = verb.substring(0, verb.length() - 4);
                    break;
                default:
                    root = verb;
                    break;
            }
        } else {
            switch (family) {
                case Globals.VERB_FAMILY_SURU:
                case Globals.VERB_FAMILY_KURU:
                    root = verb.substring(0, verb.length() - 2);
                    break;
                case Globals.VERB_FAMILY_BU_GODAN:
                case Globals.VERB_FAMILY_GU_GODAN:
                case Globals.VERB_FAMILY_KU_GODAN:
                case Globals.VERB_FAMILY_IKU_SPECIAL:
                case Globals.VERB_FAMILY_YUKU_SPECIAL:
                case Globals.VERB_FAMILY_MU_GODAN:
                case Globals.VERB_FAMILY_NU_GODAN:
                case Globals.VERB_FAMILY_RU_GODAN:
                case Globals.VERB_FAMILY_ARU_SPECIAL:
                case Globals.VERB_FAMILY_SU_GODAN:
                case Globals.VERB_FAMILY_RU_ICHIDAN:
                case Globals.VERB_FAMILY_TSU_GODAN:
                case Globals.VERB_FAMILY_U_GODAN:
                case Globals.VERB_FAMILY_U_SPECIAL:
                    root = verb.substring(0, verb.length() - 1);
                    break;
                default:
                    root = verb;
                    break;
            }
        }

        return root;
    }

    public static String capitalizeFirstLetter(String original) {
        if (original == null || original.length() == 0) {
            return original;
        }
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }

    public static String getMeaningsExtract(List<Word.Meaning> meanings, int balancePoint) {
        if (meanings == null) return "";
        List<String> totalMeaningElements = new ArrayList<>();

        if (meanings.size() == 1 || balancePoint < 2) return meanings.get(0).getMeaning();
        else if (meanings.size() >= 2 && meanings.size() <= balancePoint) {
            for (Word.Meaning meaning : meanings) {
                totalMeaningElements = addMeaningElementsToListUpToMaxNumber(
                        totalMeaningElements, meaning.getMeaning(), balancePoint + 1 - meanings.size());
            }
            return TextUtils.join(", ", totalMeaningElements);
        } else if (meanings.size() > balancePoint || balancePoint > 6) {
            for (Word.Meaning meaning : meanings) {
                totalMeaningElements = addMeaningElementsToListUpToMaxNumber(
                        totalMeaningElements, meaning.getMeaning(), 1);
            }
            return TextUtils.join(", ", totalMeaningElements);
        } else return "";
    }

    private static List<String> addMeaningElementsToListUpToMaxNumber(List<String> totalList, String meaning, int maxNumber) {
        String[] meaningelements = splitAtCommasOutsideParentheses(meaning);
        if (meaningelements.length <= maxNumber) totalList.addAll(Arrays.asList(meaningelements));
        else totalList.addAll(Arrays.asList(meaningelements).subList(0, maxNumber));
        return totalList;
    }

    static String[] splitAtCommasOutsideParentheses(String text) {
        // https://stackoverflow.com/questions/9030036/regex-to-match-only-commas-not-in-parentheses
        return text.split(",(?![^(]*\\))(?![^\"']*[\"'](?:[^\"']*[\"'][^\"']*[\"'])*[^\"']*$)");
    }

    //OCR utilities
    public static int loadOCRImageContrastFromSharedPreferences(SharedPreferences sharedPreferences, Context context) {
        float contrastValue = Float.parseFloat(context.getResources().getString(R.string.pref_OCR_image_contrast_default_value));
        try {
            contrastValue = Float.parseFloat(sharedPreferences.getString(context.getResources().getString(R.string.pref_OCR_image_contrast_key),
                    context.getResources().getString(R.string.pref_OCR_image_contrast_default_value)));
        } catch (Exception e) {
            contrastValue = Float.parseFloat(context.getResources().getString(R.string.pref_OCR_image_contrast_default_value));
        } finally {
            contrastValue = truncateFloatToRange(contrastValue,
                    Float.parseFloat(context.getResources().getString(R.string.pref_OCR_image_contrast_min_value)),
                    Float.parseFloat(context.getResources().getString(R.string.pref_OCR_image_contrast_max_value)));
        }
        return (int) contrastValue;
    }

    public static int loadOCRImageSaturationFromSharedPreferences(SharedPreferences sharedPreferences, Context context) {
        float saturationValue = Float.parseFloat(context.getResources().getString(R.string.pref_OCR_image_saturation_default_value));
        try {
            saturationValue = Float.parseFloat(sharedPreferences.getString(context.getResources().getString(R.string.pref_OCR_image_saturation_key),
                    context.getResources().getString(R.string.pref_OCR_image_saturation_default_value)));
        } catch (Exception e) {
            saturationValue = Float.parseFloat(context.getResources().getString(R.string.pref_OCR_image_saturation_default_value));
        } finally {
            saturationValue = truncateFloatToRange(saturationValue,
                    Float.parseFloat(context.getResources().getString(R.string.pref_OCR_image_saturation_min_value)),
                    Float.parseFloat(context.getResources().getString(R.string.pref_OCR_image_saturation_max_value)));
        }
        return (int) saturationValue;
    }

    public static int loadOCRImageBrightnessFromSharedPreferences(SharedPreferences sharedPreferences, Context context) {
        float brightnessValue = Float.parseFloat(context.getResources().getString(R.string.pref_OCR_image_brightness_default_value));
        try {
            brightnessValue = Float.parseFloat(sharedPreferences.getString(context.getResources().getString(R.string.pref_OCR_image_brightness_key),
                    context.getResources().getString(R.string.pref_OCR_image_brightness_default_value)));
        } catch (Exception e) {
            brightnessValue = Float.parseFloat(context.getResources().getString(R.string.pref_OCR_image_brightness_default_value));
        } finally {
            brightnessValue = truncateFloatToRange(brightnessValue,
                    Float.parseFloat(context.getResources().getString(R.string.pref_OCR_image_brightness_min_value)),
                    Float.parseFloat(context.getResources().getString(R.string.pref_OCR_image_brightness_max_value)));
        }
        return (int) brightnessValue;
    }

    private static float truncateFloatToRange(float value, float min, float max) {
        if (value < min) value = min;
        else if (value > max) value = max;
        return value;
    }

    static int truncateIntToRange(int value, int min, int max) {
        if (value < min) value = min;
        else if (value > max) value = max;
        return value;
    }

    public static float convertContrastProgressToValue(float contrastBarValue, Context context) {
        return contrastBarValue
                / ((float) Integer.parseInt(context.getString(R.string.pref_OCR_image_contrast_range)))
                * ((float) Integer.parseInt(context.getString(R.string.pref_OCR_image_contrast_max_value)));
    }

    public static float convertSaturationProgressToValue(float saturationBarValue, Context context) {
        return saturationBarValue
                / ((float) Integer.parseInt(context.getString(R.string.pref_OCR_image_saturation_range)))
                * ((float) Integer.parseInt(context.getString(R.string.pref_OCR_image_saturation_multipliers)));
    }

    public static float convertSaturationProgressToValueOLD(float saturationBarValue, Context context) {
        return saturationBarValue
                / ((float) Integer.parseInt(context.getString(R.string.pref_OCR_image_saturation_range)))
                * ((float) Integer.parseInt(context.getString(R.string.pref_OCR_image_saturation_multipliers)));
    }

    public static int convertBrightnessProgressToValue(int brightnessBarValue, Context context) {
        return brightnessBarValue - 256;
    }

    public static int convertContrastValueToProgress(float contrastValue, Context context) {
        float contrastBarValue = contrastValue
                * ((float) Integer.parseInt(context.getString(R.string.pref_OCR_image_contrast_range)))
                / ((float) Integer.parseInt(context.getString(R.string.pref_OCR_image_contrast_max_value)));
        return (int) contrastBarValue;
    }

    public static int convertSaturationValueToProgress(float saturationValue, Context context) {
        float saturationBarValue = saturationValue
                * ((float) Integer.parseInt(context.getString(R.string.pref_OCR_image_saturation_range)))
                / ((float) Integer.parseInt(context.getString(R.string.pref_OCR_image_saturation_multipliers)));
        return (int) saturationBarValue;
    }

    public static int convertBrightnessValueToProgress(int brightnessValue, Context context) {
        return brightnessValue + 256;
    }

    //Internet Connectivity utilities
    public static boolean internetIsAvailableCheck(Context context) {
        //adapted from https://stackoverflow.com/questions/43315393/android-internet-connection-timeout
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr == null) return false;

        NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();

        if (activeNetworkInfo != null) { // connected to the internet
            //Toast.makeText(context, activeNetworkInfo.getTypeName(), Toast.LENGTH_SHORT).show();

            if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                return isWifiInternetAvailable();
            } else return activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        }
        return false;
    }

    private static boolean isWifiInternetAvailable() {
        //adapted from https://stackoverflow.com/questions/43315393/android-internet-connection-timeout
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name
            return !ipAddr.toString().equals("");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Word> getWordsFromJishoOnWeb(String word, final Context context) {

        if (TextUtils.isEmpty(word)) {
            return new ArrayList<>();
        }

        //region Preparing the word to be included in the url
        StringBuilder prepared_word;
        if (ConvertFragment.getTextType(word) == Globals.TYPE_KANJI) {
            String converted_word = convertToUTF8Index(word);
            converted_word = converted_word.substring(2);
            prepared_word = new StringBuilder();
            for (int i = 0; i < converted_word.length() - 1; i = i + 2) {
                prepared_word.append("%").append(converted_word, i, i + 2);
            }
        } else {
            prepared_word = new StringBuilder(word);
        }
        //endregion

        //Getting the Jisho.org website code
        String website_code = getWebsiteXml(context.getString(R.string.jisho_website_url) + prepared_word);

        //Returning nothing if there was a problem getting results
        if ((website_code != null && website_code.equals(""))
                || website_code == null
                || website_code.length() == 0
                || website_code.contains("Sorry, couldn't find anything matching")
                || website_code.contains("Sorry, couldn't find any words matching")
                || (website_code.contains("Searched for") && website_code.contains("No matches for"))) {
            return new ArrayList<>();
        }

        //Parsing the website code and mapping it to a List<Word>
        List<Object> parsedData = parseJishoWebsiteToTree(website_code);
        List<Word> wordsList = adaptJishoTreeToWordsList(parsedData);

        return wordsList;
    }

    public static List<Word> removeEdictExceptionsFromJisho(List<Word> words) {

        List<Word> nonExceptionWords = new ArrayList<>();
        boolean isException;
        for (Word word : words) {
            isException = false;
            for (String[] romajiKanji : Globals.EDICT_EXCEPTIONS) {
                if (word.getKanji().equals(romajiKanji[1]) && (romajiKanji[0].equals("*") || word.getRomaji().equals(romajiKanji[0]))) {
                    isException = true;
                }
            }
            if (!isException) nonExceptionWords.add(word);
        }
        return nonExceptionWords;
    }

    public static List<Word> cleanUpProblematicWordsFromJisho(List<Word> words) {

        List<Word> cleanWords = new ArrayList<>();
        //Clean up problematic words (e.g. that don't include a meaning)
        for (Word word : words) {
            if (word.getMeaningsEN().size() > 0) cleanWords.add(word);
        }
        return cleanWords;
    }

    private static String getWebsiteXml(String websiteUrl) {

        StringBuilder responseString = new StringBuilder();
        String inputLine;
        HttpURLConnection connection = null;

        try {
            //https://stackoverflow.com/questions/35568584/android-studio-deprecated-on-httpparams-httpconnectionparams-connmanagerparams
            //String current_url = "https://www.google.co.il/search?dcr=0&source=hp&q=" + prepared_word;
            URL dataUrl = new URL(websiteUrl);
            connection = (HttpURLConnection) dataUrl.openConnection();
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            connection.setInstanceFollowRedirects(true);
            // optional default is GET
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                responseString = new StringBuilder();
                while ((inputLine = in.readLine()) != null)
                    responseString.append(inputLine).append('\n');
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("Diagnosis Time", "Failed to access online resources.");
            return null;
        } finally {
            try {
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace(); //If you want further info on failure...
            }
        }
        return responseString.toString();
    }

    private static List<Object> parseJishoWebsiteToTree(String website_code) {

        runningIndex = 0;
        int initial_offset = 15; //Skips <!DOCTYPE html>
        websiteCodeString = website_code.substring(initial_offset);
        List<Object> parsedWebsiteTree = new ArrayList<>();
        try {
            parsedWebsiteTree = getChildren();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return parsedWebsiteTree;
    }

    private static List<Object> getChildren() {

        if (runningIndex < 0) return new ArrayList<>();

        List<Object> currentParent = new ArrayList<>();

        if (runningIndex > websiteCodeString.length() - 1) {
            currentParent.add("");
            return currentParent;
        }
        String remainingWebsiteCodeString = websiteCodeString.substring(runningIndex);

        if (!remainingWebsiteCodeString.contains("<")) {
            currentParent.add(remainingWebsiteCodeString);
            return currentParent;
        }

        while (0 <= runningIndex && runningIndex < websiteCodeString.length()) {

            //Getting the next header characteristics
            int nextHeaderStart = websiteCodeString.indexOf("<", runningIndex);
            if (nextHeaderStart == -1) return currentParent;
            int nextHeaderEnd = websiteCodeString.indexOf(">", nextHeaderStart);
            String currentHeader = websiteCodeString.substring(nextHeaderStart + 1, nextHeaderEnd);

            //Log.i("Diagnosis Time", "Current child: " + runningIndex + ", " + currentHeader);

            //If there is String text before the next header, add it to the list and continue to the header
            if (nextHeaderStart != runningIndex) {
                String currentText = websiteCodeString.substring(runningIndex, nextHeaderStart);
                StringBuilder validText = new StringBuilder();
                for (int i = 0; i < currentText.length(); i++) {
                    if (i < currentText.length() - 1 && currentText.substring(i, i + 1).equals("\n")) {
                        i++;
                        continue;
                    }
                    validText.append(currentText.charAt(i));
                }
                String validTextString = validText.toString().trim();
//                boolean isOnlyWhiteSpace = true;
//                for (int i=0; i<validTextString.length(); i++) {
//                    if (!Character.isWhitespace(validTextString.charAt(i))) {isOnlyWhiteSpace = false; break;}
//                }
                if (!TextUtils.isEmpty(validTextString)) currentParent.add(validTextString);
                runningIndex = nextHeaderStart;
            }

            //If the header is of type "<XXX/>" then there is no subtree. In this case add the header to the tree and move to next subtree.
            if (websiteCodeString.substring(nextHeaderEnd - 1, nextHeaderEnd + 1).equals("/>")) {
                currentParent.add(currentHeader);
                runningIndex = nextHeaderEnd + 1;
            }

            //If the header is of type "<XXX>" then:
            // - if the header is <br> there is no substree and the header should be treated as text
            else if (currentHeader.equals("br")) {
                currentParent.add("<br>");
                runningIndex = nextHeaderEnd + 1;
            }
            // - if the header is a tail, move up the stack
            else if (currentHeader.substring(0, 1).equals("/")) {
                int endOfTail = websiteCodeString.indexOf(">", nextHeaderStart);
                runningIndex = endOfTail + 1;
                return currentParent;
            }
            // - if the header is <!-- XXX> then this is a comment and should be ignored
            else if (currentHeader.contains("!--")) {
                int endOfComment = websiteCodeString.indexOf("-->", runningIndex);
                runningIndex = endOfComment + 3;
            }
            //If the subtree is valid and is not the <head> subtree, add it to the tree
            else if (currentHeader.equals("head")) {
                currentParent.add(currentHeader);
                currentParent.add("");
                runningIndex = websiteCodeString.indexOf("</head>") + 7;
            }
            // - if the header is not <br> then there is a subtree and the methods recurses
            else {
                currentParent.add(currentHeader);
                runningIndex = nextHeaderEnd + 1;
                List<Object> subtree = getChildren();
                currentParent.add(subtree);
            }

        }

        return currentParent;
    }

    private static List<Word> adaptJishoTreeToWordsList(List<Object> parsedData) {

        List<Word> wordsList = new ArrayList<>();

        //Getting to the relevant tree section
        if (parsedData.size() < 1) return new ArrayList<>();
        List<Object> htmlData = (List<Object>) parsedData.get(1);
        if (htmlData == null || htmlData.size() < 3) return new ArrayList<>();
        List<Object> bodyData = (List<Object>) htmlData.get(3);
        List<Object> pageContainerData = (List<Object>) getElementAtHeader(bodyData, "page_container");
        if (pageContainerData == null) return new ArrayList<>();
        List<Object> large12ColumnsData = (List<Object>) getElementAtHeader(pageContainerData, "large-12 columns");
        if (large12ColumnsData == null) return new ArrayList<>();
        List<Object> mainResultsData = (List<Object>) getElementAtHeader(large12ColumnsData, "main_results");
        if (mainResultsData == null) return new ArrayList<>();
        List<Object> rowData = (List<Object>) getElementAtHeader(mainResultsData, "row");
        if (rowData == null) return new ArrayList<>();
        List<Object> primaryData = (List<Object>) getElementAtHeader(rowData, "primary");
        if (primaryData == null) return new ArrayList<>();

        List<Object> exactBlockData = (List<Object>) getElementAtHeader(primaryData, "exact_block");
        List<Object> conceptsBlockData;
        if (exactBlockData == null) {

            conceptsBlockData = (List<Object>) getElementAtHeader(primaryData, "concepts");
            if (conceptsBlockData == null) return wordsList;
            if (conceptsBlockData.size() > 2) wordsList.addAll(addWordsFromBigBlock(conceptsBlockData, 1));

            return wordsList;
        } else if (exactBlockData.size() > 2) {
            wordsList.addAll(addWordsFromBigBlock(exactBlockData, 3));

            conceptsBlockData = (List<Object>) getElementAtHeader(primaryData, "concepts");
            if (conceptsBlockData == null) return wordsList;
            if (conceptsBlockData.size() > 2) wordsList.addAll(addWordsFromBigBlock(conceptsBlockData, 1));
        }

        return wordsList;
    }

    private static List<Word> addWordsFromBigBlock(List<Object> bigBlockData, int startingSubBlock) {

        if (startingSubBlock >= bigBlockData.size()) return new ArrayList<>();

        List<Word> wordsList = new ArrayList<>();
        StringBuilder kanji;
        StringBuilder romaji;
        List<String> meaningTagsFromTree;
        List<String> meaningsFromTree;
        for (int i = startingSubBlock; i < bigBlockData.size(); i = i + 2) {

            Word currentWord = new Word();

            if (!(bigBlockData.get(i) instanceof List)) break;
            List<Object> conceptLightClearFixData = (List<Object>) bigBlockData.get(i);
            if (!(conceptLightClearFixData.get(1) instanceof List)) continue;
            List<Object> conceptLightWrapperData = (List<Object>) conceptLightClearFixData.get(1);
            List<Object> conceptLightReadingsData = (List<Object>) conceptLightWrapperData.get(1);
            List<Object> conceptLightRepresentationData = (List<Object>) conceptLightReadingsData.get(1);

            //region Extracting the kanji
            kanji = new StringBuilder();
            List<Object> TextData = (List<Object>) getElementAtHeader(conceptLightRepresentationData, "text");
            if (TextData != null && TextData.size() > 1) {
                kanji = new StringBuilder();
                for (int j = 0; j < TextData.size(); j++) {
                    String currentText;
                    currentText = "";
                    if (TextData.get(j) instanceof List) {
                        List<Object> list = (List<Object>) TextData.get(j);
                        if (list.size() > 0) currentText = (String) list.get(0);
                    } else {
                        currentText = (String) TextData.get(j);
                        if (currentText.equals("span")) currentText = "";
                    }
                    kanji.append(currentText);
                }
            } else if (TextData != null && TextData.size() > 0) kanji = new StringBuilder((String) TextData.get(0));
            currentWord.setKanji(kanji.toString());
            //endregion

            //region Extracting the romaji
            romaji = new StringBuilder();
            List<Object> furiganaData = (List<Object>) conceptLightRepresentationData.get(1);
            for (int j = 1; j < furiganaData.size(); j = j + 2) {
                List<Object> kanji1UpData = (List<Object>) furiganaData.get(j);
                if (kanji1UpData.size() > 0) romaji.append((String) kanji1UpData.get(0));
            }

            int textType = ConvertFragment.getTextType(kanji.toString());
            if (romaji.length() != 0 && (textType == Globals.TYPE_HIRAGANA || textType == Globals.TYPE_KATAKANA)) {
                //When the word is originally katakana only, the website does not display hiragana. This is corrected here.
                romaji = new StringBuilder(ConvertFragment.getWaapuroHiraganaKatakana(kanji.toString()).get(0));
            }

            List<Object> conceptLightStatusData = (List<Object>) getElementAtHeader(conceptLightWrapperData, "concept_light-status");
            if (conceptLightStatusData == null) conceptLightStatusData = (List<Object>) getElementAtHeader(conceptLightClearFixData, "concept_light-status");
            if (conceptLightStatusData != null) {
                List<Object> ulClassData = (List<Object>) getElementAtHeader(conceptLightStatusData, "ul class");
                if (ulClassData != null) {
                    for (int j = 1; j < ulClassData.size(); j = j + 2) {
                        List<Object> li = (List<Object>) ulClassData.get(j);
                        List<Object> aRef = (List<Object>) li.get(1);
                        String sentenceSearchFor = (String) aRef.get(0);
                        String currentValue = "";
                        if (sentenceSearchFor.length() > 20 && sentenceSearchFor.contains("Sentence search for")) {
                            currentValue = sentenceSearchFor.substring(20);
                        }

                        textType = ConvertFragment.getTextType(currentValue);
                        if (currentValue.length() != 0 &&
                                (textType == Globals.TYPE_HIRAGANA || textType == Globals.TYPE_KATAKANA)) {
                            //When the word is originally katakana only, the website does not display hiragana. This is corrected here.
                            romaji = new StringBuilder(ConvertFragment.getWaapuroHiraganaKatakana(currentValue).get(0));
                            break;
                        }
                    }
                }
            }
            currentWord.setRomaji(ConvertFragment.getWaapuroHiraganaKatakana(romaji.toString()).get(0));
            //endregion

            currentWord.setUniqueIdentifier(currentWord.getRomaji() + "-" + kanji);

            //region Extracting the Common Word status
            if (conceptLightStatusData != null) {
                List<Object> conceptLightCommonSuccess = (List<Object>) getElementAtHeader(conceptLightStatusData, "common success label");
                if (conceptLightCommonSuccess != null && conceptLightCommonSuccess.size() > 0) {
                    String value = (String) conceptLightCommonSuccess.get(0);
                    if (!TextUtils.isEmpty(value) && value.equalsIgnoreCase("Common word")) {
                        currentWord.setIsCommon(true);
                    } else currentWord.setIsCommon(false);
                } else currentWord.setIsCommon(false);
            }
            //endregion

            //region Extracting the meanings (types, meanings, altSpellings)

            List<Object> conceptLightMeaningsData = (List<Object>) getElementAtHeader(conceptLightClearFixData, "concept_light-meanings medium-9 columns");
            if (conceptLightMeaningsData == null) continue;
            List<Object> meaningsWrapperData = (List<Object>) conceptLightMeaningsData.get(1);

            String currentHeader = "";
            String meaningTag = "";
            String meaning;
            meaningTagsFromTree = new ArrayList<>();
            meaningsFromTree = new ArrayList<>();
            for (int j = 0; j < meaningsWrapperData.size(); j++) {

                if (j % 2 == 0) {
                    currentHeader = (String) meaningsWrapperData.get(j);
                    continue;
                }

                if (currentHeader.contains("meaning-tags")) {
                    List<Object> meaningsTagsData = (List<Object>) meaningsWrapperData.get(j);
                    meaningTag = "";
                    if (meaningsTagsData.size() > 0) meaningTag = (String) meaningsTagsData.get(0);
                }
                if (meaningTag.contains("Wikipedia") || meaningTag.contains("Notes")) continue;
                if (currentHeader.contains("meaning-wrapper")) {
                    if (meaningTag.contains("Other forms")) {
                        List<Object> meaningWrapperData = (List<Object>) meaningsWrapperData.get(j);
                        List<Object> meaningDefinitionData = (List<Object>) meaningWrapperData.get(1);
                        List<Object> meaningMeaningData = (List<Object>) getElementAtHeader(meaningDefinitionData, "meaning-meaning");
                        if (meaningMeaningData == null || meaningMeaningData.size() == 0) break;

                        //Getting the altSpellings container bock to extract from
                        StringBuilder altSpellingsContainer = new StringBuilder();
                        for (Object element : meaningMeaningData) {
                            if (element instanceof List) {
                                List<String> elementList = (List<String>) element;
                                if (elementList.size() > 0) {
                                    altSpellingsContainer.append(elementList.get(0));
                                }
                            }
                        }

                        //Extracting the altSpellings using regex
                        List<String> altSpellings = new ArrayList<>();
                        Matcher m = Pattern.compile("\\b(\\w+)\\s【(\\w+)】").matcher(altSpellingsContainer.toString());
                        while (m.find()) {
                            if (!m.group(1).equals(currentWord.getKanji())) altSpellings.add(m.group(1).trim());
                            String convertedMatch = ConvertFragment.getWaapuroHiraganaKatakana(m.group(2)).get(Globals.TYPE_LATIN);
                            if (!convertedMatch.equals(currentWord.getRomaji())) altSpellings.add(convertedMatch.trim());
                        }
                        altSpellings = removeDuplicatesFromList(altSpellings);
                        currentWord.setAltSpellings(TextUtils.join(", ", altSpellings));
                        break;
                    } else {
                        List<Object> meaningWrapperData = (List<Object>) meaningsWrapperData.get(j);
                        List<Object> meaningDefinitionData = (List<Object>) meaningWrapperData.get(1);
                        List<Object> meaningMeaningData = (List<Object>) getElementAtHeader(meaningDefinitionData, "meaning-meaning");
                        meaningTagsFromTree.add(meaningTag);
                        meaning = "";
                        if (meaningMeaningData != null && meaningMeaningData.size() > 0) meaning = (String) meaningMeaningData.get(0);
                        meaningsFromTree.add(reformatMeanings(meaning));
                    }
                }
            }

            List<Word.Meaning> wordMeaningsList = new ArrayList<>();
            for (int j = 0; j < meaningsFromTree.size(); j++) {

                Word.Meaning wordMeaning = new Word.Meaning();

                //Getting the Meaning value
                String matchingWordMeaning = meaningsFromTree.get(j);
                wordMeaning.setMeaning(matchingWordMeaning);

                //Getting the Type value
                String matchingWordType = meaningTagsFromTree.get(j);

                if (matchingWordType.contains("verb") && !matchingWordType.contains("Suru") && !matchingWordType.contains("Kuru")) {
                    if (matchingWordType.contains("su ending")) {
                        if (matchingWordType.contains("intransitive")) matchingWordType = "VsuI";
                        if (matchingWordType.contains("Transitive")) matchingWordType = "VsuT";
                        else matchingWordType = "VsuI";
                    } else if (matchingWordType.contains("ku ending")) {
                        if (matchingWordType.contains("intransitive")) matchingWordType = "VkuI";
                        if (matchingWordType.contains("Transitive")) matchingWordType = "VkuT";
                        else matchingWordType = "VkuI";
                    } else if (matchingWordType.contains("gu ending")) {
                        if (matchingWordType.contains("intransitive")) matchingWordType = "VguI";
                        if (matchingWordType.contains("Transitive")) matchingWordType = "VguT";
                        else matchingWordType = "VguI";
                    } else if (matchingWordType.contains("mu ending")) {
                        if (matchingWordType.contains("intransitive")) matchingWordType = "VmuI";
                        if (matchingWordType.contains("Transitive")) matchingWordType = "VmuT";
                        else matchingWordType = "VmuI";
                    } else if (matchingWordType.contains("bu ending")) {
                        if (matchingWordType.contains("intransitive")) matchingWordType = "VbuI";
                        if (matchingWordType.contains("Transitive")) matchingWordType = "VbuT";
                        else matchingWordType = "VbuI";
                    } else if (matchingWordType.contains("nu ending")) {
                        if (matchingWordType.contains("intransitive")) matchingWordType = "VnuI";
                        if (matchingWordType.contains("Transitive")) matchingWordType = "VnuT";
                        else matchingWordType = "VnuI";
                    } else if (matchingWordType.contains("ru ending")) {
                        if (matchingWordType.contains("intransitive")) matchingWordType = "VrugI";
                        if (matchingWordType.contains("Transitive")) matchingWordType = "VrugT";
                        else matchingWordType = "VrugI";
                    } else if (matchingWordType.contains("tsu ending")) {
                        if (matchingWordType.contains("intransitive")) matchingWordType = "VtsuI";
                        if (matchingWordType.contains("Transitive")) matchingWordType = "VtsuT";
                        else matchingWordType = "VtsuI";
                    } else if (matchingWordType.contains("u ending")) {
                        if (matchingWordType.contains("intransitive")) matchingWordType = "VuI";
                        if (matchingWordType.contains("Transitive")) matchingWordType = "VuT";
                        else matchingWordType = "VuI";
                    } else if (matchingWordType.contains("Ichidan")) {
                        if (matchingWordType.contains("intransitive")) matchingWordType = "VruiI";
                        if (matchingWordType.contains("Transitive")) matchingWordType = "VruiT";
                        else matchingWordType = "VruiI";
                    }
                } else {
                    String[] typeElements = matchingWordType.split(", ");
                    List<String> typesAsLegend = new ArrayList<>();
                    for (String typeElement : typeElements) {

                        if (typeElement.contains("Expression")) {
                            typesAsLegend.add("CE");
                        } else if (typeElement.equals("Adverb")) {
                            typesAsLegend.add("A");
                        } else if (typeElement.equals("Noun")) {
                            typesAsLegend.add("N");
                        } else if (typeElement.equals("Place")) {
                            typesAsLegend.add("Pl");
                        } else if (typeElement.equals("Temporal noun")) {
                            typesAsLegend.add("T");
                        } else if (typeElement.equals("Proper noun")) {
                            typesAsLegend.add("Ne");
                        } else if (typeElement.equals("Numeric")) {
                            typesAsLegend.add("num");
                        } else if (typeElement.equals("Counter")) {
                            typesAsLegend.add("C");
                        } else if (typeElement.contains("Suffix, Counter")) {
                            typesAsLegend.add("C");
                        } else if (typeElement.contains("Suffix") || matchingWordType.contains("suffix")) {
                            typesAsLegend.add("Sx");
                        } else if (typeElement.contains("Prefix") || matchingWordType.contains("prefix")) {
                            typesAsLegend.add("Px");
                        } else if (typeElement.contains("I-adjective") || matchingWordType.contains("i-adjective")) {
                            typesAsLegend.add("Ai");
                        } else if (typeElement.contains("Na-adjective") || matchingWordType.contains("na-adjective")) {
                            typesAsLegend.add("Ana");
                        } else if (typeElement.contains("No-adjective") || matchingWordType.contains("na-adjective")) {
                            typesAsLegend.add("Ano");
                        } else if (typeElement.contains("adjective") || matchingWordType.contains("Adjective")) {
                            typesAsLegend.add("Aj");
                        } else if (typeElement.contains("Pre-noun adjectival") || matchingWordType.contains("Pronoun")) {
                            typesAsLegend.add("P");
                        } else if (typeElement.contains("Auxiliary verb")) {
                            typesAsLegend.add("Vx");
                        } else if (typeElement.contains("Auxiliary adjective")) {
                            typesAsLegend.add("Ax");
                        } else if (typeElement.contains("Particle") || matchingWordType.contains("Preposition")) {
                            typesAsLegend.add("PP");
                        } else if (typeElement.contains("Conjunction")) {
                            typesAsLegend.add("CO");
                        } else if (typeElement.contains("Suru verb")) {
                            if (matchingWordType.contains("intransitive")) typesAsLegend.add("VsuruI");
                            if (matchingWordType.contains("Transitive")) typesAsLegend.add("VsuruT");
                            else typesAsLegend.add("Vsuru"); //TODO: this line prevents "Suru verb, intrans." from appearing in dict results, may want to improve this
                        } else if (typeElement.contains("Kuru verb")) {
                            if (matchingWordType.contains("intransitive")) typesAsLegend.add("VkuruI");
                            if (matchingWordType.contains("Transitive")) typesAsLegend.add("VkuruT");
                            else typesAsLegend.add("Vkuru");
                        }
                    }
                    matchingWordType = TextUtils.join(Globals.DB_ELEMENTS_DELIMITER, typesAsLegend);
                }
                wordMeaning.setType(matchingWordType);

                //Getting the Opposite value
                String matchingWordOpposite = ""; //TODO: See if this can be extracted from the site
                wordMeaning.setAntonym(matchingWordOpposite);

                //Getting the Synonym value
                String matchingWordSynonym = ""; //TODO: See if this can be extracted from the site
                wordMeaning.setSynonym(matchingWordSynonym);

                //Getting the set of Explanations
                List<Word.Meaning.Explanation> explanationsList = new ArrayList<>();
                Word.Meaning.Explanation explanation = new Word.Meaning.Explanation();

                //Getting the Explanation value
                String matchingWordExplanation = "";
                explanation.setExplanation(matchingWordExplanation);

                //Getting the Rules value
                String matchingWordRules = "";
                explanation.setRules(matchingWordRules);

                //Getting the examples
                List<Word.Meaning.Explanation.Example> examplesList = new ArrayList<>();
                explanation.setExamples(examplesList);

                explanationsList.add(explanation);

                wordMeaning.setExplanations(explanationsList);
                wordMeaningsList.add(wordMeaning);
            }

            currentWord.setMeaningsEN(wordMeaningsList);
            //endregion

            wordsList.add(currentWord);
        }

        return wordsList;
    }

    private static Object getElementAtHeader(List<Object> list, String header) {
        for (int i = 0; i < list.size() - 1; i++) {
            if (i % 2 == 0 && ((String) list.get(i)).contains(header)) return list.get(i + 1);
        }
        return null;
    }

    private static String reformatMeanings(String meaningsOriginal) {

        String meanings_commas = meaningsOriginal.replace(Globals.DB_ELEMENTS_DELIMITER, ",");
        meanings_commas = Utilities.fromHtml(meanings_commas).toString();
        meanings_commas = meanings_commas.replaceAll("',", "'");
        meanings_commas = meanings_commas.replaceAll("\",", "\"");
        meanings_commas = meanings_commas.replaceAll(",0", "'0"); //Fixes number display problems
        return meanings_commas;
    }

    //IO utilities
    public static void readCSVFileAndAddToDb(String filename, Context context, String dBType, Object dao) {

        BufferedReader fileReader = null;
        try {
            String line;
            fileReader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename)));
            List<Word> wordList = new ArrayList<>();
            List<KanjiCharacter> kanjiCharacterList = new ArrayList<>();
            List<KanjiComponent> kanjiComponentList = new ArrayList<>();
            List<IndexRomaji> indexRomajiList = new ArrayList<>();
            List<IndexEnglish> indexEnglishList = new ArrayList<>();
            List<IndexFrench> indexFrenchList = new ArrayList<>();
            List<IndexSpanish> indexSpanishList = new ArrayList<>();
            List<IndexKanji> indexKanjiList = new ArrayList<>();
            fileReader.readLine(); //Discarding the first line of the file (titles)
            int lineNum = 0;
            int MAX_NUM_ELEMENTS_IN_KANJI_COMPONENTS_INSERT_BLOCK = 3;
            int KANJI_COMPONENTS_FULL1_BLOCK_SIZE = 3000;
            int MAX_NUM_ELEMENTS_IN_KANJI_CHARS_INSERT_BLOCK = 5000;
            int MAX_NUM_ELEMENTS_IN_WORD_INSERT_BLOCK = 20000;
            int MAX_NUM_ELEMENTS_IN_INDEX_INSERT_BLOCK = 50000;

            float currentProgress = 0;
            float increment = 0;
            int blocksize;
            boolean isExtendedDb = filename.contains("Extended");
            boolean isNamesDb = filename.contains("Names");
            switch (dBType) {
                case "extendedDbWords":
                    WordDao extendedDbWordDao = (WordDao) dao;
                    currentProgress = 0;
                    blocksize = MAX_NUM_ELEMENTS_IN_WORD_INSERT_BLOCK / 4;
                    increment = (((float) blocksize / Globals.extendedDbLinesWords) * Globals.extendedDbSizeWords * 100.f / Globals.extendedDbSizeTotal);
                    while ((line = fileReader.readLine()) != null) {
                        String[] tokens = line.split("\\|", -1);
                        if (tokens.length > 0) {
                            if (tokens[0].equals("")) break;
                            Word word = UtilitiesDb.createWordFromExtendedDatabase(tokens);
                            wordList.add(word);
                        }
                        lineNum++;
                        if (lineNum % MAX_NUM_ELEMENTS_IN_WORD_INSERT_BLOCK == 0) {
                            extendedDbWordDao.insertAll(wordList);
                            wordList = new ArrayList<>();
                        }
                        if (lineNum % blocksize == 0) {
                            currentProgress += increment;
                            UtilitiesPrefs.setProgressValueExtendedDb(context, currentProgress);
                        }
                    }
                    extendedDbWordDao.insertAll(wordList);
                    break;
                case "namesDbWords":
                    WordDao namesDbWordDao = (WordDao) dao;
                    currentProgress = 0;
                    blocksize = MAX_NUM_ELEMENTS_IN_WORD_INSERT_BLOCK * 2;
                    increment = ((float) blocksize / Globals.namesDbLinesWords * (Globals.namesDbSizeWords * 100.f / Globals.namesDbSizeTotal));
                    while ((line = fileReader.readLine()) != null) {
                        String[] tokens = line.split("\\|", -1);
                        if (tokens.length > 0) {
                            if (tokens[0].equals("")) break;
                            Word word = UtilitiesDb.createWordFromNamesDatabase(tokens);
                            wordList.add(word);
                        }
                        lineNum++;
                        if (lineNum % MAX_NUM_ELEMENTS_IN_WORD_INSERT_BLOCK == 0) {
                            namesDbWordDao.insertAll(wordList);
                            wordList = new ArrayList<>();
                        }
                        if (lineNum % blocksize == 0) {
                            currentProgress += increment;
                            UtilitiesPrefs.setProgressValueNamesDb(context, currentProgress);
                        }
                    }
                    namesDbWordDao.insertAll(wordList);
                    break;
                case "indexRomaji":
                    blocksize = MAX_NUM_ELEMENTS_IN_WORD_INSERT_BLOCK / 4;
                    if (isExtendedDb) {
                        currentProgress = UtilitiesPrefs.getProgressValueExtendedDb(context);
                        increment = (((float) blocksize / Globals.extendedDbLinesRomajiIndex) * Globals.extendedDbSizeRomajiIndex * 100.f / Globals.extendedDbSizeTotal);
                    } else if (isNamesDb) {
                        currentProgress = UtilitiesPrefs.getProgressValueNamesDb(context);
                        increment = (((float) blocksize / Globals.namesDbLinesRomajiIndex) * Globals.namesDbSizeRomajiIndex * 100.f / Globals.namesDbSizeTotal);
                    }
                    IndexRomajiDao indexRomajiDao = (IndexRomajiDao) dao;
                    while ((line = fileReader.readLine()) != null) {
                        String[] tokens = line.split("\\|", -1);
                        if (tokens.length > 0) {
                            if (tokens[0].equals("")) break;
                            IndexRomaji index = new IndexRomaji(tokens[0], tokens[1]);
                            indexRomajiList.add(index);
                        }
                        lineNum++;
                        if (lineNum % MAX_NUM_ELEMENTS_IN_INDEX_INSERT_BLOCK == 0) {
                            indexRomajiDao.insertAll(indexRomajiList);
                            indexRomajiList = new ArrayList<>();
                        }
                        if (lineNum % blocksize == 0) {
                            if (isExtendedDb) {
                                currentProgress += increment;
                                UtilitiesPrefs.setProgressValueExtendedDb(context, currentProgress);
                            } else if (isNamesDb) {
                                currentProgress += increment;
                                UtilitiesPrefs.setProgressValueNamesDb(context, currentProgress);
                            }
                        }
                    }
                    indexRomajiDao.insertAll(indexRomajiList);
                    break;
                case "indexEnglish":
                    blocksize = MAX_NUM_ELEMENTS_IN_WORD_INSERT_BLOCK / 10;
                    if (isExtendedDb) {
                        currentProgress = UtilitiesPrefs.getProgressValueExtendedDb(context);
                        increment = (((float) blocksize / Globals.extendedDbLinesEnglishIndex) * Globals.extendedDbSizeEnglishIndex * 100.f / Globals.extendedDbSizeTotal);
                    }
                    IndexEnglishDao indexEnglishDao = (IndexEnglishDao) dao;
                    while ((line = fileReader.readLine()) != null) {
                        String[] tokens = line.split("\\|", -1);
                        if (tokens.length > 0) {
                            if (tokens[0].equals("")) break;
                            IndexEnglish index = new IndexEnglish(tokens[0], tokens[1]);
                            indexEnglishList.add(index);
                        }
                        lineNum++;
                        if (lineNum % MAX_NUM_ELEMENTS_IN_INDEX_INSERT_BLOCK == 0) {
                            indexEnglishDao.insertAll(indexEnglishList);
                            indexEnglishList = new ArrayList<>();
                        }
                        if (lineNum % blocksize == 0) {
                            if (isExtendedDb) {
                                currentProgress += increment;
                                UtilitiesPrefs.setProgressValueExtendedDb(context, currentProgress);
                            }
                        }
                    }
                    indexEnglishDao.insertAll(indexEnglishList);
                    break;
                case "indexFrench":
                    if (isExtendedDb) {
                        currentProgress = UtilitiesPrefs.getProgressValueExtendedDb(context);
                        increment = Globals.extendedDbSizeFrenchIndex * 100.f / Globals.extendedDbSizeTotal;
                    }
                    IndexFrenchDao indexFrenchDao = (IndexFrenchDao) dao;
                    while ((line = fileReader.readLine()) != null) {
                        String[] tokens = line.split("\\|", -1);
                        if (tokens.length > 0) {
                            if (tokens[0].equals("")) break;
                            IndexFrench index = new IndexFrench(tokens[0], tokens[1]);
                            indexFrenchList.add(index);
                        }
                        lineNum++;
                        if (lineNum % MAX_NUM_ELEMENTS_IN_INDEX_INSERT_BLOCK == 0) {
                            indexFrenchDao.insertAll(indexFrenchList);
                            indexFrenchList = new ArrayList<>();
                        }
                    }
                    indexFrenchDao.insertAll(indexFrenchList);
                    if (isExtendedDb) {
                        currentProgress += increment;
                        UtilitiesPrefs.setProgressValueExtendedDb(context, currentProgress);
                    }
                    break;
                case "indexSpanish":
                    if (isExtendedDb) {
                        currentProgress = UtilitiesPrefs.getProgressValueExtendedDb(context);
                        increment = Globals.extendedDbSizeSpanishIndex * 100.f / Globals.extendedDbSizeTotal;
                    }
                    IndexSpanishDao indexSpanishDao = (IndexSpanishDao) dao;
                    while ((line = fileReader.readLine()) != null) {
                        String[] tokens = line.split("\\|", -1);
                        if (tokens.length > 0) {
                            if (tokens[0].equals("")) break;
                            IndexSpanish index = new IndexSpanish(tokens[0], tokens[1]);
                            indexSpanishList.add(index);
                        }
                        lineNum++;
                        if (lineNum % MAX_NUM_ELEMENTS_IN_INDEX_INSERT_BLOCK == 0) {
                            indexSpanishDao.insertAll(indexSpanishList);
                            indexSpanishList = new ArrayList<>();
                        }
                    }
                    indexSpanishDao.insertAll(indexSpanishList);
                    if (isExtendedDb) {
                        currentProgress += increment;
                        UtilitiesPrefs.setProgressValueExtendedDb(context, currentProgress);
                    }
                    break;
                case "indexKanji":
                    blocksize = MAX_NUM_ELEMENTS_IN_WORD_INSERT_BLOCK;
                    if (isExtendedDb) {
                        currentProgress = UtilitiesPrefs.getProgressValueExtendedDb(context);
                        increment = (((float) blocksize / Globals.extendedDbLinesKanjiIndex) * Globals.extendedDbSizeKanjiIndex * 100.f / Globals.extendedDbSizeTotal);
                    } else if (isNamesDb) {
                        currentProgress = UtilitiesPrefs.getProgressValueNamesDb(context);
                        increment = (((float) blocksize / Globals.namesDbLinesKanjiIndex) * Globals.namesDbSizeKanjiIndex * 100.f / Globals.namesDbSizeTotal);
                    }
                    IndexKanjiDao indexKanjiDao = (IndexKanjiDao) dao;
                    while ((line = fileReader.readLine()) != null) {
                        String[] tokens = line.split("\\|", -1);
                        if (tokens.length > 0) {
                            if (tokens[0].equals("")) break;
                            IndexKanji indexKanji = new IndexKanji(tokens[0], tokens[1], tokens[2]);
                            indexKanjiList.add(indexKanji);
                        }
                        lineNum++;
                        if (lineNum % MAX_NUM_ELEMENTS_IN_INDEX_INSERT_BLOCK == 0) {
                            indexKanjiDao.insertAll(indexKanjiList);
                            indexKanjiList = new ArrayList<>();
                        }
                        if (lineNum % blocksize == 0) {
                            if (isExtendedDb) {
                                currentProgress += increment;
                                UtilitiesPrefs.setProgressValueExtendedDb(context, currentProgress);
                            } else if (isNamesDb) {
                                currentProgress += increment;
                                UtilitiesPrefs.setProgressValueNamesDb(context, currentProgress);
                            }
                        }
                    }
                    indexKanjiDao.insertAll(indexKanjiList);
                    break;
                case "kanjiCharactersDb":
                    KanjiCharacterDao kanjiCharacterDaoDao = (KanjiCharacterDao) dao;
                    while ((line = fileReader.readLine()) != null) {
                        String[] tokens = line.split("\\|", -1);
                        if (tokens.length > 0) {
                            if (TextUtils.isEmpty(tokens[0])) break;
                            KanjiCharacter kanjiCharacter = new KanjiCharacter(tokens[0], tokens[1], tokens[2]);
                            kanjiCharacter.setKanji(Utilities.convertFromUTF8Index(kanjiCharacter.getHexIdentifier()));
                            kanjiCharacterList.add(kanjiCharacter);
                        }
                        lineNum++;
                        if (lineNum % MAX_NUM_ELEMENTS_IN_KANJI_CHARS_INSERT_BLOCK == 0) {
                            kanjiCharacterDaoDao.insertAll(kanjiCharacterList);
                            kanjiCharacterList = new ArrayList<>();
                        }
                    }
                    kanjiCharacterDaoDao.insertAll(kanjiCharacterList);
                    break;
                case "kanjiComponentsDb":
                    KanjiComponentDao kanjiComponentDao = (KanjiComponentDao) dao;
                    //KanjiComponents are defined as structure -> component -> associatedComponents
                    //NOTE: "full" block split into "full1" and "full2" to prevent memory crashes when requesting all kanji components with structure "full"
                    //The CSV file starts with the "full" block, so "full1" is defined first
                    List<KanjiComponent> kanjiComponents = new ArrayList<>();
                    List<KanjiComponent.AssociatedComponent> associatedComponents = new ArrayList<>();
                    KanjiComponent kanjiComponent = new KanjiComponent("full1");
                    String firstElement;
                    String secondElement;
                    boolean isBlockTitle;
                    while ((line = fileReader.readLine()) != null) {
                        String[] tokens = line.split("\\|", -1);
                        if (tokens.length > 1) {
                            if (TextUtils.isEmpty(tokens[0])) break;
                            firstElement = tokens[0];
                            secondElement = tokens[1];
                            isBlockTitle = secondElement.equals("");
                            if (isBlockTitle || lineNum == KANJI_COMPONENTS_FULL1_BLOCK_SIZE) {
                                kanjiComponent.setAssociatedComponents(associatedComponents);
                                associatedComponents = new ArrayList<>();
                                if (lineNum != 0) {
                                    kanjiComponents.add(kanjiComponent);
                                    if (kanjiComponents.size() % MAX_NUM_ELEMENTS_IN_KANJI_COMPONENTS_INSERT_BLOCK == 0) {
                                        kanjiComponentDao.insertAll(kanjiComponents);
                                        kanjiComponents = new ArrayList<>();
                                    }
                                }

                                //We define the kanjiComponent's structure here
                                //Since the "full" block has no bockTitle line, it is defined before the loop starts and again when we switch to full2
                                //Thereafter, the structure is defined by firstElement when isBlockTitle==true
                                kanjiComponent = lineNum == KANJI_COMPONENTS_FULL1_BLOCK_SIZE ?
                                        new KanjiComponent("full2") : new KanjiComponent(firstElement);
                            }
                            if (!isBlockTitle) {
                                KanjiComponent.AssociatedComponent associatedComponent = new KanjiComponent.AssociatedComponent();
                                associatedComponent.setComponent(firstElement);
                                associatedComponent.setAssociatedComponents(secondElement);
                                associatedComponents.add(associatedComponent);
                            }

                        }
                        lineNum++;
                    }
                    kanjiComponent.setAssociatedComponents(associatedComponents);
                    kanjiComponents.add(kanjiComponent);
                    kanjiComponentDao.insertAll(kanjiComponents);
                    break;
            }
        } catch (Exception e) {
            System.out.println("Error in CsvFileReader !!!");
            e.printStackTrace();
        } finally {
            try {
                if (fileReader != null) {
                    fileReader.close();
                }
            } catch (IOException e) {
                System.out.println("Error while closing fileReader !!!");
                e.printStackTrace();
            }
        }

    }

    public static List<String[]> readCSVFile(String filename, Context context) {

        List<String[]> mySheet = new ArrayList<>();

        BufferedReader fileReader = null;

        int line_number = 0;
        try {
            String line;
            fileReader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename)));

            while ((line = fileReader.readLine()) != null) {
                String[] tokens = line.split("\\|", -1);
                if (tokens.length > 0) {
                    mySheet.add(tokens);
                    line_number++;
                }
            }
        } catch (Exception e) {
            System.out.println("Error in CsvFileReader !!!");
            e.printStackTrace();
        } finally {
            try {
                if (fileReader != null) {
                    fileReader.close();
                }
            } catch (IOException e) {
                System.out.println("Error while closing fileReader !!!");
                e.printStackTrace();
            }
        }

        return mySheet;
    }

    public static List<String[]> readCSVFileFirstRow(String filename, Context context) {

        List<String[]> mySheetFirstRow = new ArrayList<>();

        //OpenCSV implementation
        //				  String firstrow[] = null;
        //                String next[] = null;
        //                CSVReader reader = null;
        //
        //                try {
        //                    reader = new CSVReader(new InputStreamReader(GlobalTranslatorActivity.getAssets().open(filename)));
        //                } catch (IOException e) {
        //                    e.printStackTrace();
        //                }
        //
        //                if (reader != null) {
        //                    try {
        //                        firstrow = reader.readNext();
        //                    } catch (IOException e) {
        //                        e.printStackTrace();
        //                    }
        //                    if (firstrow != null) {
        //                        mySheetFirstRow.add(firstrow);
        //                    }
        //                }
        //
        //                try {
        //                    reader.close();
        //                } catch (IOException e) {
        //                    e.printStackTrace();
        //                }

        // "|" Parser implementation

        BufferedReader fileReader = null;

        try {
            String line;
            fileReader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename)));

            line = fileReader.readLine();
            String[] tokens = line.split("\\|", -1);
            if (tokens.length > 0) {
                mySheetFirstRow.add(tokens);
            }
        } catch (Exception e) {
            System.out.println("Error in CsvFileReader !!!");
            e.printStackTrace();
        } finally {
            try {
                if (fileReader != null) {
                    fileReader.close();
                }
            } catch (IOException e) {
                System.out.println("Error while closing fileReader !!!");
                e.printStackTrace();
            }
        }

        return mySheetFirstRow;
    }

    public void makeDelay(int milliseconds) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            }
        }, milliseconds);
    }

    public String createQueryOnJMDict(String word) {
        //inspired by: https://stackoverflow.com/questions/38220828/an-htmlunit-alternative-for-android
        //inspired by: https://stackoverflow.com/questions/15805771/submit-form-using-httpurlconnection
        //inspired by: https://stackoverflow.com/questions/9767952/how-to-add-parameters-to-httpurlconnection-using-post-using-namevaluepair

        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL("https://www.edrdg.org/cgi-bin/wwwjdic/wwwjdic?HF");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));

                String request = URLEncoder.encode("NAME", "UTF-8") + "=" + URLEncoder.encode("dsrchkey", "UTF-8") +
                        "&" + URLEncoder.encode("VALUE", "UTF-8") + "=" + URLEncoder.encode(word, "UTF-8") +
                        "&" + URLEncoder.encode("NAME", "UTF-8") + "=" + URLEncoder.encode("dicsel", "UTF-8") +
                        "&" + URLEncoder.encode("SELECTED VALUE", "UTF-8") + "=" + URLEncoder.encode("H", "UTF-8");
                writer.write(request);

                writer.flush();
                writer.close();
                os.close();
            }
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            } else {
                response = new StringBuilder();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response.toString();
    }
}
