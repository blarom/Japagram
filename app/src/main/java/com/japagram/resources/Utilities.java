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
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.google.firebase.database.FirebaseDatabase;
import com.japagram.BuildConfig;
import com.japagram.R;
import com.japagram.data.ConjugationTitle;
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
import com.japagram.data.RoomCentralDatabase;
import com.japagram.data.RoomExtendedDatabase;
import com.japagram.data.RoomNamesDatabase;
import com.japagram.data.Verb;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

public final class Utilities {

    private Utilities() {}

    //Constants
    public static final String firebaseEmail = BuildConfig.firebaseEmail;
    public static final String firebasePass = BuildConfig.firebasePass;
    private static final int WORD_SEARCH_CHAR_COUNT_THRESHOLD = 3;
    public static final int NUM_COLUMNS_IN_WORDS_CSV_SHEETS = 16;
    private static final String DEBUG_TAG = "JT Debug";
    private static FirebaseDatabase mDatabase;


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
    public void makeDelay(int milliseconds){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {}
        }, milliseconds);
    }
    public static long getAvailableMemory() {
        final Runtime runtime = Runtime.getRuntime();
        final long usedMemInMB=(runtime.totalMemory() - runtime.freeMemory()) / 1048576L;
        final long maxHeapSizeInMB=runtime.maxMemory() / 1048576L;
        final long availHeapSizeInMB = maxHeapSizeInMB - usedMemInMB;
        Log.i("Diagnosis Time","Available heap size: " + availHeapSizeInMB);
        return availHeapSizeInMB;
    }
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =(InputMethodManager) activity.getBaseContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
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
    @NonNull public static Boolean checkIfFileExistsInSpecificFolder(File dir, String filename) {

        if (!dir.exists()&& dir.mkdirs()){
            return false;
        }
        if(dir.exists()) {
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
                    Log.e(DEBUG_TAG, "You have permission");
                    return true;
                } else {
                    Log.e(DEBUG_TAG, "You have asked for permission");
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    return false;
                }
            }
            else return false;
        }
        else { //you dont need to worry about these stuff below api level 23
            Log.e(DEBUG_TAG,"You already have the permission");
            return true;
        }
    }
    @NonNull public static String formatSize(long size) {
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

        Intent intent = activity.getPackageManager().getLaunchIntentForPackage( activity.getPackageName() );
        if (intent!=null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            activity.finish();
        }
    }
    @NonNull public static Resources getLocalizedResources(Context context, Locale desiredLocale) {
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

        int newWidth = (int) Math.floor(source.getWidth()*scaleFactor);
        int newHeight = (int) Math.floor(source.getHeight()*scaleFactor);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(source, newWidth, newHeight,true);

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true); //rotated Bitmap

    }
    @TargetApi(23) public static boolean isPrintable(String c ) {
        Paint paint=new Paint();
        //paint.setTypeface(MainActivity.CJK_typeface);
        boolean hasGlyph;
        hasGlyph=paint.hasGlyph(c);
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
            public void onAnimationEnd(Animation animation)
            {
                img.setVisibility(View.GONE);
            }
            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {}
        });

        img.startAnimation(fadeOut);
    }


    //String manipulation utilities
    public static String convertToUTF8Index(String input_string) {

        byte[] byteArray;
        byteArray = input_string.getBytes(Charset.forName("UTF-8"));
        StringBuilder prepared_word = new StringBuilder("1.");
        for (byte b : byteArray) {
            prepared_word.append(Integer.toHexString(b & 0xFF));
        }
        return prepared_word.toString();
    }
    public static String convertFromUTF8Index(String inputHex) {

        //inspired by: https://stackoverflow.com/questions/15749475/java-string-hex-to-string-ascii-with-accentuation
        if(inputHex.length()<4) return "";
        inputHex = inputHex.toLowerCase().substring(2,inputHex.length());

        ByteBuffer buff = ByteBuffer.allocate(inputHex.length()/2);
        for (int i = 0; i < inputHex.length(); i+=2) {
            buff.put((byte)Integer.parseInt(inputHex.substring(i, i+2), 16));
        }
        buff.rewind();
        Charset cs = Charset.forName("UTF-8");
        CharBuffer cb = cs.decode(buff);

        return cb.toString();
    }
    private static String removeNonSpaceSpecialCharacters(String sentence) {
        String current_char;
        StringBuilder concatenated_sentence = new StringBuilder();
        for (int index=0; index<sentence.length(); index++) {
            current_char = Character.toString(sentence.charAt(index));
            if (!(current_char.equals(".")
                    || current_char.equals("-")
                    || current_char.equals("(")
                    || current_char.equals(")")
                    || current_char.equals(":")
                    || current_char.equals("/") ) ) {
                concatenated_sentence.append(current_char);
            }
        }
        return concatenated_sentence.toString();
    }
    public static String removeSpecialCharacters(String sentence) {
        String current_char;
        StringBuilder concatenated_sentence = new StringBuilder();
        for (int index=0; index<sentence.length(); index++) {
            current_char = Character.toString(sentence.charAt(index));
            if (!( current_char.equals(" ")
                    || current_char.equals(".")
                    || current_char.equals("-")
                    || current_char.equals("(")
                    || current_char.equals(")")
                    || current_char.equals(":")
                    || current_char.equals("/") ) ) {
                concatenated_sentence.append(current_char);
            }
        }
        return concatenated_sentence.toString();
    }
    @SuppressWarnings("deprecation") public static Spanned fromHtml(String source) {
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
        for (int j = 0; j <parsed_cumulative_meaning_value.size(); j++) {
            is_repeated = false;
            current_value = parsed_cumulative_meaning_value.get(j).trim();
            for (String s : final_cumulative_meaning_value_array) {
                if (s.equals(current_value)) { is_repeated = true; break; }
            }
            if (!is_repeated)  final_cumulative_meaning_value_array.add(current_value);
        }
        return TextUtils.join(", ", final_cumulative_meaning_value_array);
    }
    public static List<String> getIntersectionOfLists(List<String> A, List<String> B) {
        //https://stackoverflow.com/questions/2400838/efficient-intersection-of-component_substructures[2]-liststring-in-java
        List<String> rtnList = new LinkedList<>();
        for(String dto : A) {
            if(B.contains(dto)) {
                rtnList.add(dto);
            }
        }
        return rtnList;
    }
    public static List<String> removeDuplicatesFromList(List<String> list) {

            /*
            int end_index = list_of_intersecting_results_temp.size();
            String current_value;
            for (int i=0; i<end_index; i++) {
                current_value = list_of_intersecting_results_temp.get(i);
                for (int j=end_index; j>i; j--) {
                    if (current_value.equals(list_of_intersecting_results_temp.get(j))) {
                        list_of_intersecting_results_temp.remove(j);
                    }
                }
            }
            list_of_intersecting_results = list_of_intersecting_results_temp;
            */

        //https://stackoverflow.com/questions/14040331/remove-duplicate-strings-in-a-list-in-java

        Set<String> set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        Iterator<String> i = list.iterator();
        while (i.hasNext()) {
            String s = i.next();
            if (set.contains(s)) {
                i.remove();
            }
            else {
                set.add(s);
            }
        }

        return new ArrayList<>(set);
    }
    public static String getVerbRoot(String verb, String family, int type) {
        String root;
        if (verb == null || verb.length()==0 || family == null || family.length()==0) {
            return "";
        }

        if (type == GlobalConstants.TYPE_LATIN) {
            switch (family) {
                case GlobalConstants.VERB_FAMILY_BU_GODAN:
                case GlobalConstants.VERB_FAMILY_GU_GODAN:
                case GlobalConstants.VERB_FAMILY_KU_GODAN:
                case GlobalConstants.VERB_FAMILY_IKU_SPECIAL:
                case GlobalConstants.VERB_FAMILY_YUKU_SPECIAL:
                case GlobalConstants.VERB_FAMILY_MU_GODAN:
                case GlobalConstants.VERB_FAMILY_NU_GODAN:
                case GlobalConstants.VERB_FAMILY_RU_GODAN:
                case GlobalConstants.VERB_FAMILY_ARU_SPECIAL:
                case GlobalConstants.VERB_FAMILY_SU_GODAN:
                case GlobalConstants.VERB_FAMILY_RU_ICHIDAN:
                    root = verb.substring(0, verb.length() - 2);
                    break;
                case GlobalConstants.VERB_FAMILY_TSU_GODAN:
                    root = verb.substring(0, verb.length() - 3);
                    break;
                case GlobalConstants.VERB_FAMILY_U_GODAN:
                case GlobalConstants.VERB_FAMILY_U_SPECIAL:
                    root = verb.substring(0, verb.length() - 1);
                    break;
                case GlobalConstants.VERB_FAMILY_SURU:
                case GlobalConstants.VERB_FAMILY_KURU:
                    root = verb.substring(0, verb.length() - 4);
                    break;
                default:
                    root = verb;
                    break;
            }
        }
        else {
            switch (family) {
                case GlobalConstants.VERB_FAMILY_SURU:
                case GlobalConstants.VERB_FAMILY_KURU:
                    root = verb.substring(0, verb.length() - 2);
                    break;
                case GlobalConstants.VERB_FAMILY_BU_GODAN:
                case GlobalConstants.VERB_FAMILY_GU_GODAN:
                case GlobalConstants.VERB_FAMILY_KU_GODAN:
                case GlobalConstants.VERB_FAMILY_IKU_SPECIAL:
                case GlobalConstants.VERB_FAMILY_YUKU_SPECIAL:
                case GlobalConstants.VERB_FAMILY_MU_GODAN:
                case GlobalConstants.VERB_FAMILY_NU_GODAN:
                case GlobalConstants.VERB_FAMILY_RU_GODAN:
                case GlobalConstants.VERB_FAMILY_ARU_SPECIAL:
                case GlobalConstants.VERB_FAMILY_SU_GODAN:
                case GlobalConstants.VERB_FAMILY_RU_ICHIDAN:
                case GlobalConstants.VERB_FAMILY_TSU_GODAN:
                case GlobalConstants.VERB_FAMILY_U_GODAN:
                case GlobalConstants.VERB_FAMILY_U_SPECIAL:
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
                        totalMeaningElements, meaning.getMeaning(),balancePoint + 1 - meanings.size());
            }
            return TextUtils.join(", ", totalMeaningElements);
        }
        else if (meanings.size() > balancePoint || balancePoint > 6) {
            for (Word.Meaning meaning : meanings) {
                totalMeaningElements = addMeaningElementsToListUpToMaxNumber(
                        totalMeaningElements, meaning.getMeaning(), 1);
            }
            return TextUtils.join(", ", totalMeaningElements);
        }
        else return "";
    }
    private static List<String> addMeaningElementsToListUpToMaxNumber(List<String> totalList, String meaning, int maxNumber) {
        String[] meaningelements = splitAtCommasOutsideParentheses(meaning);
        if (meaningelements.length <= maxNumber) totalList.addAll(Arrays.asList(meaningelements));
        else totalList.addAll(Arrays.asList(meaningelements).subList(0, maxNumber));
        return totalList;
    }
    private static String[] splitAtCommasOutsideParentheses(String text) {
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
    private static int truncateIntToRange(int value, int min, int max) {
        if (value < min) value = min;
        else if (value > max) value = max;
        return value;
    }
    public static float convertContrastProgressToValue(float contrastBarValue, Context context) {
        return contrastBarValue
                /((float) Integer.parseInt(context.getString(R.string.pref_OCR_image_contrast_range)))
                *((float) Integer.parseInt(context.getString(R.string.pref_OCR_image_contrast_max_value)));
    }
    public static float convertSaturationProgressToValue(float saturationBarValue, Context context) {
        return saturationBarValue
                /((float) Integer.parseInt(context.getString(R.string.pref_OCR_image_saturation_range)))
                *((float) Integer.parseInt(context.getString(R.string.pref_OCR_image_saturation_multipliers)));
    }
    public static float convertSaturationProgressToValueOLD(float saturationBarValue, Context context) {
        return saturationBarValue
                /((float) Integer.parseInt(context.getString(R.string.pref_OCR_image_saturation_range)))
                *((float) Integer.parseInt(context.getString(R.string.pref_OCR_image_saturation_multipliers)));
    }
    public static int convertBrightnessProgressToValue(int brightnessBarValue, Context context) {
        return brightnessBarValue-256;
    }
    public static int convertContrastValueToProgress(float contrastValue, Context context) {
        float contrastBarValue = contrastValue
                *((float) Integer.parseInt(context.getString(R.string.pref_OCR_image_contrast_range)))
                /((float) Integer.parseInt(context.getString(R.string.pref_OCR_image_contrast_max_value)));
        return (int) contrastBarValue;
    }
    public static int convertSaturationValueToProgress(float saturationValue, Context context) {
        float saturationBarValue = saturationValue
                *((float) Integer.parseInt(context.getString(R.string.pref_OCR_image_saturation_range)))
                /((float) Integer.parseInt(context.getString(R.string.pref_OCR_image_saturation_multipliers)));
        return (int) saturationBarValue;
    }
    public static int convertBrightnessValueToProgress(int brightnessValue, Context context) {
        return brightnessValue+256;
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

        if (TextUtils.isEmpty(word)) { return new ArrayList<>(); }

        //region Preparing the word to be included in the url
        StringBuilder prepared_word;
        if (ConvertFragment.getTextType(word) == GlobalConstants.TYPE_KANJI) {
            String converted_word = convertToUTF8Index(word);
            converted_word = converted_word.substring(2);
            prepared_word = new StringBuilder();
            for (int i = 0; i < converted_word.length() - 1; i = i + 2) {
                prepared_word.append("%").append(converted_word, i, i + 2);
            }
        }
        else {
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
            for (String[] romajiKanji : GlobalConstants.EDICT_EXCEPTIONS) {
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
            if (word.getMeaningsEN().size()>0) cleanWords.add(word);
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
                if (connection != null) {connection.disconnect(); }
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
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return parsedWebsiteTree;
    }
    private static int runningIndex = 0;
    private static String websiteCodeString = "";
    private static List<Object> getChildren() {

        if (runningIndex < 0) return new ArrayList<>();

        List<Object> currentParent = new ArrayList<>();

        if (runningIndex > websiteCodeString.length()-1) {
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
            if (nextHeaderStart==-1) return currentParent;
            int nextHeaderEnd = websiteCodeString.indexOf(">", nextHeaderStart);
            String currentHeader = websiteCodeString.substring(nextHeaderStart + 1, nextHeaderEnd);

            //Log.i("Diagnosis Time", "Current child: " + runningIndex + ", " + currentHeader);

            //If there is String text before the next header, add it to the list and continue to the header
            if (nextHeaderStart != runningIndex) {
                String currentText = websiteCodeString.substring(runningIndex, nextHeaderStart);
                StringBuilder validText = new StringBuilder();
                for (int i=0; i<currentText.length(); i++) {
                    if (i<currentText.length()-1 && currentText.substring(i,i+1).equals("\n")) { i++; continue;}
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
            else if (currentHeader.substring(0,1).equals("/")) {
                int endOfTail = websiteCodeString.indexOf(">", nextHeaderStart);
                runningIndex = endOfTail+1;
                return currentParent;
            }
            // - if the header is <!-- XXX> then this is a comment and should be ignored
            else if (currentHeader.contains("!--")) {
                int endOfComment = websiteCodeString.indexOf("-->", runningIndex);
                runningIndex = endOfComment+3;
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
                runningIndex = nextHeaderEnd+1;
                List<Object> subtree = getChildren();
                currentParent.add(subtree);
            }

        }

        return currentParent;
    }
    private static List<Word> adaptJishoTreeToWordsList(List<Object> parsedData) {

        List<Word> wordsList = new ArrayList<>();

        //Getting to the relevant tree section
        if (parsedData.size()<1) return new ArrayList<>();
        List<Object> htmlData = (List<Object>) parsedData.get(1);
        if (htmlData==null || htmlData.size()<3) return new ArrayList<>();
        List<Object> bodyData = (List<Object>) htmlData.get(3);
        List<Object> pageContainerData = (List<Object>) getElementAtHeader(bodyData,"page_container");
        if (pageContainerData==null) return new ArrayList<>();
        List<Object> large12ColumnsData = (List<Object>) getElementAtHeader(pageContainerData,"large-12 columns");
        if (large12ColumnsData==null) return new ArrayList<>();
        List<Object> mainResultsData = (List<Object>) getElementAtHeader(large12ColumnsData,"main_results");
        if (mainResultsData==null) return new ArrayList<>();
        List<Object> rowData = (List<Object>) getElementAtHeader(mainResultsData,"row");
        if (rowData==null) return new ArrayList<>();
        List<Object> primaryData = (List<Object>) getElementAtHeader(rowData,"primary");
        if (primaryData==null) return new ArrayList<>();

        List<Object> exactBlockData = (List<Object>) getElementAtHeader(primaryData,"exact_block");
        List<Object> conceptsBlockData;
        if (exactBlockData==null) {

            conceptsBlockData = (List<Object>) getElementAtHeader(primaryData,"concepts");
            if (conceptsBlockData==null) return wordsList;
            if (conceptsBlockData.size()>2) wordsList.addAll(addWordsFromBigBlock(conceptsBlockData, 1));

            return wordsList;
        }
        else if (exactBlockData.size()>2) {
            wordsList.addAll(addWordsFromBigBlock(exactBlockData, 3));

            conceptsBlockData = (List<Object>) getElementAtHeader(primaryData,"concepts");
            if (conceptsBlockData==null) return wordsList;
            if (conceptsBlockData.size()>2) wordsList.addAll(addWordsFromBigBlock(conceptsBlockData, 1));
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
        for (int i = startingSubBlock; i < bigBlockData.size(); i=i+2) {

            Word currentWord = new Word();

            if (!(bigBlockData.get(i) instanceof List)) break;
            List<Object> conceptLightClearFixData = (List<Object>) bigBlockData.get(i);
            if (!(conceptLightClearFixData.get(1) instanceof List)) continue;
            List<Object> conceptLightWrapperData = (List<Object>) conceptLightClearFixData.get(1);
            List<Object> conceptLightReadingsData = (List<Object>) conceptLightWrapperData.get(1);
            List<Object> conceptLightRepresentationData = (List<Object>) conceptLightReadingsData.get(1);

            //region Extracting the kanji
            kanji = new StringBuilder();
            List<Object> TextData = (List<Object>) getElementAtHeader(conceptLightRepresentationData,"text");
            if (TextData!=null && TextData.size()>1) {
                kanji = new StringBuilder();
                for (int j=0; j<TextData.size(); j++) {
                    String currentText;
                    currentText = "";
                    if (TextData.get(j) instanceof List) {
                        List<Object> list = (List<Object>) TextData.get(j);
                        if (list.size()>0) currentText = (String) list.get(0);
                    }
                    else {
                        currentText = (String) TextData.get(j);
                        if (currentText.equals("span")) currentText = "";
                    }
                    kanji.append(currentText);
                }
            }
            else if (TextData!=null && TextData.size()>0) kanji = new StringBuilder((String) TextData.get(0));
            currentWord.setKanji(kanji.toString());
            //endregion

            //region Extracting the romaji
            romaji = new StringBuilder();
            List<Object> furiganaData = (List<Object>) conceptLightRepresentationData.get(1);
            for (int j=1; j<furiganaData.size(); j=j+2) {
                List<Object> kanji1UpData = (List<Object>) furiganaData.get(j);
                if (kanji1UpData.size()>0) romaji.append((String) kanji1UpData.get(0));
            }

            int textType = ConvertFragment.getTextType(kanji.toString());
            if (romaji.length()!=0 && (textType == GlobalConstants.TYPE_HIRAGANA || textType == GlobalConstants.TYPE_KATAKANA)) {
                //When the word is originally katakana only, the website does not display hiragana. This is corrected here.
                romaji = new StringBuilder(ConvertFragment.getLatinHiraganaKatakana(kanji.toString()).get(0));
            }

            List<Object> conceptLightStatusData = (List<Object>) getElementAtHeader(conceptLightWrapperData,"concept_light-status");
            if (conceptLightStatusData==null) conceptLightStatusData = (List<Object>) getElementAtHeader(conceptLightClearFixData,"concept_light-status");
            if (conceptLightStatusData!=null) {
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
                                (textType == GlobalConstants.TYPE_HIRAGANA || textType == GlobalConstants.TYPE_KATAKANA)) {
                            //When the word is originally katakana only, the website does not display hiragana. This is corrected here.
                            romaji = new StringBuilder(ConvertFragment.getLatinHiraganaKatakana(currentValue).get(0));
                            break;
                        }
                    }
                }
            }
            currentWord.setRomaji(ConvertFragment.getLatinHiraganaKatakana(romaji.toString()).get(0));
            //endregion

            currentWord.setUniqueIdentifier(currentWord.getRomaji()+"-"+kanji);

            //region Extracting the Common Word status
            if (conceptLightStatusData!=null) {
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

            List<Object> conceptLightMeaningsData = (List<Object>) getElementAtHeader(conceptLightClearFixData,"concept_light-meanings medium-9 columns");
            if (conceptLightMeaningsData==null) continue;
            List<Object> meaningsWrapperData = (List<Object>) conceptLightMeaningsData.get(1);

            String currentHeader = "";
            String meaningTag = "";
            String meaning;
            meaningTagsFromTree = new ArrayList<>();
            meaningsFromTree = new ArrayList<>();
            for (int j=0; j<meaningsWrapperData.size(); j++) {

                if (j%2==0) { currentHeader = (String) meaningsWrapperData.get(j); continue;}

                if (currentHeader.contains("meaning-tags")) {
                    List<Object> meaningsTagsData = (List<Object>) meaningsWrapperData.get(j);
                    meaningTag = "";
                    if (meaningsTagsData.size()>0) meaningTag = (String) meaningsTagsData.get(0);
                }
                if (meaningTag.contains("Wikipedia") || meaningTag.contains("Notes")) continue;
                if (currentHeader.contains("meaning-wrapper")) {
                    if (meaningTag.contains("Other forms")) {
                        List<Object> meaningWrapperData = (List<Object>) meaningsWrapperData.get(j);
                        List<Object> meaningDefinitionData = (List<Object>) meaningWrapperData.get(1);
                        List<Object> meaningMeaningData = (List<Object>) getElementAtHeader(meaningDefinitionData,"meaning-meaning");
                        if (meaningMeaningData ==null || meaningMeaningData.size()==0) break;

                        //Getting the altSpellings container bock to extract from
                        StringBuilder altSpellingsContainer = new StringBuilder();
                        for (Object element : meaningMeaningData) {
                            if (element instanceof List) {
                                List<String> elementList = (List<String>) element;
                                if (elementList.size()>0) {
                                    altSpellingsContainer.append(elementList.get(0));
                                }
                            }
                        }

                        //Extracting the altSpellings using regex
                        List<String> altSpellings = new ArrayList<>();
                        Matcher m = Pattern.compile("\\b(\\w+)\\s【(\\w+)】").matcher(altSpellingsContainer.toString());
                        while (m.find()) {
                            if (!m.group(1).equals(currentWord.getKanji())) altSpellings.add(m.group(1).trim());
                            String convertedMatch = ConvertFragment.getLatinHiraganaKatakana(m.group(2)).get(GlobalConstants.TYPE_LATIN);
                            if (!convertedMatch.equals(currentWord.getRomaji())) altSpellings.add(convertedMatch.trim());
                        }
                        altSpellings = removeDuplicatesFromList(altSpellings);
                        currentWord.setAltSpellings(TextUtils.join(", ", altSpellings));
                        break;
                    }
                    else {
                        List<Object> meaningWrapperData = (List<Object>) meaningsWrapperData.get(j);
                        List<Object> meaningDefinitionData = (List<Object>) meaningWrapperData.get(1);
                        List<Object> meaningMeaningData = (List<Object>) getElementAtHeader(meaningDefinitionData,"meaning-meaning");
                        meaningTagsFromTree.add(meaningTag);
                        meaning = "";
                        if (meaningMeaningData !=null && meaningMeaningData.size()>0) meaning = (String) meaningMeaningData.get(0);
                        meaningsFromTree.add(reformatMeanings(meaning));
                    }
                }
            }

            List<Word.Meaning> wordMeaningsList = new ArrayList<>();
            for (int j=0; j<meaningsFromTree.size(); j++) {

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
                }
                else {
                    String[] typeElements = matchingWordType.split(", ");
                    List<String> typesAsLegend = new ArrayList<>();
                    for (String typeElement : typeElements) {

                        if (typeElement.contains("Expression")) {
                            typesAsLegend.add("CE");
                        }
                        else if (typeElement.equals("Adverb")) {
                            typesAsLegend.add("A");
                        }
                        else if (typeElement.equals("Noun")) {
                            typesAsLegend.add("N");
                        }
                        else if (typeElement.equals("Place")) {
                            typesAsLegend.add("Pl");
                        }
                        else if (typeElement.equals("Temporal noun")) {
                            typesAsLegend.add("T");
                        }
                        else if (typeElement.equals("Proper noun")) {
                            typesAsLegend.add("Ne");
                        }
                        else if (typeElement.equals("Numeric")) {
                            typesAsLegend.add("num");
                        }
                        else if (typeElement.equals("Counter")) {
                            typesAsLegend.add("C");
                        }
                        else if (typeElement.contains("Suffix, Counter")) {
                            typesAsLegend.add("C");
                        }
                        else if (typeElement.contains("Suffix") || matchingWordType.contains("suffix")) {
                            typesAsLegend.add("Sx");
                        }
                        else if (typeElement.contains("Prefix") || matchingWordType.contains("prefix")) {
                            typesAsLegend.add("Px");
                        }
                        else if (typeElement.contains("I-adjective") || matchingWordType.contains("i-adjective")) {
                            typesAsLegend.add("Ai");
                        }
                        else if (typeElement.contains("Na-adjective") || matchingWordType.contains("na-adjective")) {
                            typesAsLegend.add("Ana");
                        }
                        else if (typeElement.contains("No-adjective") || matchingWordType.contains("na-adjective")) {
                            typesAsLegend.add("Ano");
                        }
                        else if (typeElement.contains("adjective") || matchingWordType.contains("Adjective")) {
                            typesAsLegend.add("Aj");
                        }
                        else if (typeElement.contains("Pre-noun adjectival") || matchingWordType.contains("Pronoun")) {
                            typesAsLegend.add("P");
                        }
                        else if (typeElement.contains("Auxiliary verb")) {
                            typesAsLegend.add("Vx");
                        }
                        else if (typeElement.contains("Auxiliary adjective")) {
                            typesAsLegend.add("Ax");
                        }
                        else if (typeElement.contains("Particle") || matchingWordType.contains("Preposition")) {
                            typesAsLegend.add("PP");
                        }
                        else if (typeElement.contains("Conjunction")) {
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
                    matchingWordType = TextUtils.join(GlobalConstants.DB_ELEMENTS_DELIMITER, typesAsLegend);
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
        for (int i=0; i<list.size()-1; i++) {
            if (i%2==0 && ((String)list.get(i)).contains(header)) return list.get(i+1);
        }
        return null;
    }
    private static String reformatMeanings(String meaningsOriginal) {

        String meanings_commas = meaningsOriginal.replace(GlobalConstants.DB_ELEMENTS_DELIMITER,",");
        meanings_commas = Utilities.fromHtml(meanings_commas).toString();
        meanings_commas = meanings_commas.replaceAll("',", "'");
        meanings_commas = meanings_commas.replaceAll("\",", "\"");
        meanings_commas = meanings_commas.replaceAll(",0", "'0"); //Fixes number display problems
        return meanings_commas;
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
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, Charset.forName("UTF-8")));

            String request = URLEncoder.encode("NAME", "UTF-8") + "=" + URLEncoder.encode("dsrchkey", "UTF-8") +
                    "&" + URLEncoder.encode("VALUE", "UTF-8") + "=" + URLEncoder.encode(word, "UTF-8") +
                    "&" + URLEncoder.encode("NAME", "UTF-8") + "=" + URLEncoder.encode("dicsel", "UTF-8") +
                    "&" + URLEncoder.encode("SELECTED VALUE", "UTF-8") + "=" + URLEncoder.encode("H", "UTF-8");
            writer.write(request);

            writer.flush();
            writer.close();
            os.close();
            int responseCode=conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response.append(line);
                }
            }
            else {
                response = new StringBuilder();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response.toString();
    }


    //IO utilities
    public static void readCSVFileAndAddToDb(String filename, Context context, String dBType, Object dao) {

        BufferedReader fileReader = null;
        try {
            String line;
            fileReader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename)));
            List<Word> wordList = new ArrayList<>();
            List<IndexRomaji> indexRomajiList = new ArrayList<>();
            List<IndexEnglish> indexEnglishList = new ArrayList<>();
            List<IndexFrench> indexFrenchList = new ArrayList<>();
            List<IndexSpanish> indexSpanishList = new ArrayList<>();
            List<IndexKanji> indexKanjiList = new ArrayList<>();
            fileReader.readLine(); //Discarding the first line of the file (titles)
            int lineNum = 0;
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
                    blocksize = MAX_NUM_ELEMENTS_IN_WORD_INSERT_BLOCK/4;
                    increment = (((float) blocksize/GlobalConstants.extendedDbLinesWords) * GlobalConstants.extendedDbSizeWords*100.f/GlobalConstants.extendedDbSizeTotal);
                    while ((line = fileReader.readLine()) != null) {
                        String[] tokens = line.split("\\|",-1);
                        if (tokens.length > 0) {
                            if (tokens[0].equals("")) break;
                            Word word = Utilities.createWordFromExtendedDatabase(tokens);
                            wordList.add(word);
                        }
                        lineNum++;
                        if (lineNum % MAX_NUM_ELEMENTS_IN_WORD_INSERT_BLOCK == 0) {
                            extendedDbWordDao.insertAll(wordList);
                            wordList = new ArrayList<>();
                        }
                        if (lineNum % blocksize == 0) {
                            currentProgress += increment;
                            Utilities.setProgressValueExtendedDb(context, currentProgress);
                        }
                    }
                    extendedDbWordDao.insertAll(wordList);
                    break;
                case "namesDbWords":
                    WordDao namesDbWordDao = (WordDao) dao;
                    currentProgress = 0;
                    blocksize = MAX_NUM_ELEMENTS_IN_WORD_INSERT_BLOCK*2;
                    increment = ((float) blocksize/GlobalConstants.namesDbLinesWords * (GlobalConstants.namesDbSizeWords*100.f/GlobalConstants.namesDbSizeTotal));
                    while ((line = fileReader.readLine()) != null) {
                        String[] tokens = line.split("\\|",-1);
                        if (tokens.length > 0) {
                            if (tokens[0].equals("")) break;
                            Word word = Utilities.createWordFromNamesDatabase(tokens);
                            wordList.add(word);
                        }
                        lineNum++;
                        if (lineNum % MAX_NUM_ELEMENTS_IN_WORD_INSERT_BLOCK == 0) {
                            namesDbWordDao.insertAll(wordList);
                            wordList = new ArrayList<>();
                        }
                        if (lineNum % blocksize == 0) {
                            currentProgress += increment;
                            Utilities.setProgressValueNamesDb(context, currentProgress);
                        }
                    }
                    namesDbWordDao.insertAll(wordList);
                    break;
                case "indexRomaji":
                    blocksize = MAX_NUM_ELEMENTS_IN_WORD_INSERT_BLOCK/4;
                    if (isExtendedDb) {
                        currentProgress = Utilities.getProgressValueExtendedDb(context);
                        increment = (((float) blocksize/GlobalConstants.extendedDbLinesRomajiIndex) * GlobalConstants.extendedDbSizeRomajiIndex*100.f/GlobalConstants.extendedDbSizeTotal);
                    } else if (isNamesDb) {
                        currentProgress = Utilities.getProgressValueNamesDb(context);
                        increment = (((float) blocksize/GlobalConstants.namesDbLinesRomajiIndex) * GlobalConstants.namesDbSizeRomajiIndex*100.f/GlobalConstants.namesDbSizeTotal);
                    }
                    IndexRomajiDao indexRomajiDao = (IndexRomajiDao) dao;
                    while ((line = fileReader.readLine()) != null) {
                        String[] tokens = line.split("\\|",-1);
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
                                Utilities.setProgressValueExtendedDb(context, currentProgress);
                            } else if (isNamesDb) {
                                currentProgress += increment;
                                Utilities.setProgressValueNamesDb(context, currentProgress);
                            }
                        }
                    }
                    indexRomajiDao.insertAll(indexRomajiList);
                    break;
                case "indexEnglish":
                    blocksize = MAX_NUM_ELEMENTS_IN_WORD_INSERT_BLOCK/10;
                    if (isExtendedDb) {
                        currentProgress = Utilities.getProgressValueExtendedDb(context);
                        increment = (((float) blocksize/GlobalConstants.extendedDbLinesEnglishIndex) * GlobalConstants.extendedDbSizeEnglishIndex*100.f/GlobalConstants.extendedDbSizeTotal);
                    }
                    IndexEnglishDao indexEnglishDao = (IndexEnglishDao) dao;
                    while ((line = fileReader.readLine()) != null) {
                        String[] tokens = line.split("\\|",-1);
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
                                Utilities.setProgressValueExtendedDb(context, currentProgress);
                            }
                        }
                    }
                    indexEnglishDao.insertAll(indexEnglishList);
                    break;
                case "indexFrench":
                    if (isExtendedDb) {
                        currentProgress = Utilities.getProgressValueExtendedDb(context);
                        increment = GlobalConstants.extendedDbSizeFrenchIndex*100.f/GlobalConstants.extendedDbSizeTotal;
                    }
                    IndexFrenchDao indexFrenchDao = (IndexFrenchDao) dao;
                    while ((line = fileReader.readLine()) != null) {
                        String[] tokens = line.split("\\|",-1);
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
                        Utilities.setProgressValueExtendedDb(context, currentProgress);
                    }
                    break;
                case "indexSpanish":
                    if (isExtendedDb) {
                        currentProgress = Utilities.getProgressValueExtendedDb(context);
                        increment = GlobalConstants.extendedDbSizeSpanishIndex*100.f/GlobalConstants.extendedDbSizeTotal;
                    }
                    IndexSpanishDao indexSpanishDao = (IndexSpanishDao) dao;
                    while ((line = fileReader.readLine()) != null) {
                        String[] tokens = line.split("\\|",-1);
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
                        Utilities.setProgressValueExtendedDb(context, currentProgress);
                    }
                    break;
                case "indexKanji":
                    blocksize = MAX_NUM_ELEMENTS_IN_WORD_INSERT_BLOCK;
                    if (isExtendedDb) {
                        currentProgress = Utilities.getProgressValueExtendedDb(context);
                        increment = (((float) blocksize/GlobalConstants.extendedDbLinesKanjiIndex) * GlobalConstants.extendedDbSizeKanjiIndex*100.f/GlobalConstants.extendedDbSizeTotal);
                    } else if (isNamesDb) {
                        currentProgress = Utilities.getProgressValueNamesDb(context);
                        increment = (((float) blocksize/GlobalConstants.namesDbLinesKanjiIndex) * GlobalConstants.namesDbSizeKanjiIndex*100.f/GlobalConstants.namesDbSizeTotal);
                    }
                    IndexKanjiDao indexKanjiDao = (IndexKanjiDao) dao;
                    while ((line = fileReader.readLine()) != null) {
                        String[] tokens = line.split("\\|",-1);
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
                                Utilities.setProgressValueExtendedDb(context, currentProgress);
                            } else if (isNamesDb) {
                                currentProgress += increment;
                                Utilities.setProgressValueNamesDb(context, currentProgress);
                            }
                        }
                    }
                    indexKanjiDao.insertAll(indexKanjiList);
                    break;
            }
        } catch (Exception e) {
            System.out.println("Error in CsvFileReader !!!");
            e.printStackTrace();
        } finally {
            try {
                if (fileReader != null) {fileReader.close();}
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
                String[] tokens = line.split("\\|",-1);
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
                if (fileReader != null) {fileReader.close();}
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
            String[] tokens = line.split("\\|",-1);
            if (tokens.length > 0) {
                mySheetFirstRow.add(tokens);
            }
        } catch (Exception e) {
            System.out.println("Error in CsvFileReader !!!");
            e.printStackTrace();
        } finally {
            try {
                if (fileReader != null) {fileReader.close();}
            } catch (IOException e) {
                System.out.println("Error while closing fileReader !!!");
                e.printStackTrace();
            }
        }

        return mySheetFirstRow;
    }


    //Database creation utilities
    public static FirebaseDatabase getDatabase() {
        //inspired by: https://github.com/firebase/quickstart-android/issues/15
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase;
    }
    public static String cleanIdentifierForFirebase(String string) {
        if (TextUtils.isEmpty(string)) return "";
        string = string.replaceAll("\\.","*");
        string = string.replaceAll("#","*");
        string = string.replaceAll("\\$","*");
        string = string.replaceAll("\\[","*");
        string = string.replaceAll("]","*");
        //string = string.replaceAll("\\{","*");
        //string = string.replaceAll("}","*");
        return string;
    }
    public static void checkDatabaseStructure(List<String[]> databaseFromCsv, String databaseName, int numColumns) {
        for (String[] line : databaseFromCsv) {
            if (line.length < numColumns) {
                Log.v("JapaneseToolbox","Serious error in row [" + line[0] + "] in " + databaseName + ": CSV file row has less columns than expected! Check for accidental line breaks.");
                break;
            }
        }
    }
    public static Word createWordFromCsvDatabases(List<String[]> centralDatabase,
                                                  List<String[]> meaningsENDatabase,
                                                  List<String[]> meaningsFRDatabase,
                                                  List<String[]> meaningsESDatabase,
                                                  List<String[]> multExplENDatabase,
                                                  List<String[]> multExplFRDatabase,
                                                  List<String[]> multExplESDatabase,
                                                  List<String[]> examplesDatabase, int centralDbRowIndex) {

        Word word = new Word();

        //Getting the index value
        int matchingWordId = Integer.parseInt(centralDatabase.get(centralDbRowIndex)[GlobalConstants.COLUMN_WORD_ID]);
        word.setWordId(matchingWordId);

        //Getting the Romaji value
        String matchingWordRomaji = centralDatabase.get(centralDbRowIndex)[GlobalConstants.COLUMN_ROMAJI];
        word.setRomaji(matchingWordRomaji);

        //Getting the Kanji value
        String matchingWordKanji = centralDatabase.get(centralDbRowIndex)[GlobalConstants.COLUMN_KANJI];
        word.setKanji(matchingWordKanji);

        //Setting the unique identifier
        word.setUniqueIdentifier(matchingWordRomaji+"-"+matchingWordKanji);

        //Getting the Jap Keywords
        word.setExtraKeywordsJAP(centralDatabase.get(centralDbRowIndex)[GlobalConstants.COLUMN_EXTRA_KEYWORDS_JAP]);

        //Setting the flags
        word.setIsCommon(centralDatabase.get(centralDbRowIndex)[GlobalConstants.COLUMN_COMMON].equals("1"));
        word.setIsLocal(true);

        //Getting the AltSpellings value
        String matchingWordAltSpellings = centralDatabase.get(centralDbRowIndex)[GlobalConstants.COLUMN_ALT_SPELLINGS];
        word.setAltSpellings(matchingWordAltSpellings);

        //Getting the Meanings
        word.setMeaningsEN(getMeanings(centralDatabase, meaningsENDatabase, GlobalConstants.COLUMN_MEANING_EN_INDEXES,
                multExplENDatabase, examplesDatabase, centralDbRowIndex, "en"));
        if (word.getMeaningsEN().size()==0) return new Word();

        word.setMeaningsFR(getMeanings(centralDatabase, meaningsFRDatabase, GlobalConstants.COLUMN_MEANING_FR_INDEXES,
                multExplFRDatabase, examplesDatabase, centralDbRowIndex, "fr"));
        word.setMeaningsES(getMeanings(centralDatabase, meaningsESDatabase, GlobalConstants.COLUMN_MEANING_ES_INDEXES,
                multExplESDatabase, examplesDatabase, centralDbRowIndex, "es"));

        //Setting the keywords value
        word.setExtraKeywordsEN(centralDatabase.get(centralDbRowIndex)[GlobalConstants.COLUMN_EXTRA_KEYWORDS_EN]);
        word.setExtraKeywordsFR(centralDatabase.get(centralDbRowIndex)[GlobalConstants.COLUMN_EXTRA_KEYWORDS_FR]);
        word.setExtraKeywordsES(centralDatabase.get(centralDbRowIndex)[GlobalConstants.COLUMN_EXTRA_KEYWORDS_ES]);

        return word;
    }
    public static Word createWordFromExtendedDatabase(String[] extendedDatabaseRow) {

        Word word = new Word();
        word.setWordId(Long.parseLong(extendedDatabaseRow[GlobalConstants.XDB_COL_INDEX]));
        word.setRomaji(extendedDatabaseRow[GlobalConstants.XDB_COL_ROMAJI]);
        word.setKanji(extendedDatabaseRow[GlobalConstants.XDB_COL_KANJI]);
        word.setAltSpellings(extendedDatabaseRow[GlobalConstants.XDB_COL_ALTS].replace("#", ", "));
        word.setExtraKeywordsJAP("");
        word.setExtraKeywordsEN("");
        word.setExtraKeywordsFR("");
        word.setExtraKeywordsES("");

        String[] POS_list = extendedDatabaseRow[GlobalConstants.XDB_COL_POS].split("#", -1);
        String[] meaningsEN_list = extendedDatabaseRow[GlobalConstants.XDB_COL_MEANINGS_EN].split("#", -1);
        String[] meaningsFR_list = extendedDatabaseRow[GlobalConstants.XDB_COL_MEANINGS_FR].split("#", -1);
        String[] meaningsES_list = extendedDatabaseRow[GlobalConstants.XDB_COL_MEANINGS_ES].split("#", -1);

        List<Word.Meaning> meaningsEN = new ArrayList<>();
        List<Word.Meaning> meaningsFR = new ArrayList<>();
        List<Word.Meaning> meaningsES = new ArrayList<>();

        if (!meaningsEN_list[0].equals("")) {
            for (int i = 0; i < meaningsEN_list.length; i++) {
                Word.Meaning meaning = new Word.Meaning();
                meaning.setMeaning(meaningsEN_list[i].replace("@@@@", "#").replace("$$$$", "\""));
                meaning.setType(POS_list[i]);
                meaningsEN.add(meaning);
            }
        }
        if (!meaningsFR_list[0].equals("")) {
            for (int i = 0; i < meaningsFR_list.length; i++) {
                Word.Meaning meaning = new Word.Meaning();
                meaning.setMeaning(meaningsFR_list[i].replace("@@@@", "#").replace("$$$$", "\""));
                meaning.setType(POS_list[0]);
                meaningsFR.add(meaning);
            }
        }
        if (!meaningsES_list[0].equals("")) {
            for (int i = 0; i < meaningsES_list.length; i++) {
                Word.Meaning meaning = new Word.Meaning();
                meaning.setMeaning(meaningsES_list[i].replace("@@@@", "#").replace("$$$$", "\""));
                meaning.setType(POS_list[0]);
                meaningsES.add(meaning);
            }
        }

        word.setMeaningsEN(meaningsEN);
        word.setMeaningsFR(meaningsFR);
        word.setMeaningsES(meaningsES);

        return word;
    }
    public static Word createWordFromNamesDatabase(String[] namesDatabaseRow) {

        Word word = new Word();
        word.setWordId(Long.parseLong(namesDatabaseRow[GlobalConstants.NDB_COL_INDEX]));
        word.setRomaji(namesDatabaseRow[GlobalConstants.NDB_COL_ROMAJI]);
        word.setKanji(namesDatabaseRow[GlobalConstants.NDB_COL_KANJI]);
        word.setAltSpellings("");
        word.setExtraKeywordsJAP("");
        word.setExtraKeywordsEN("");
        word.setExtraKeywordsFR("");
        word.setExtraKeywordsES("");

        String[] POS_list = namesDatabaseRow[GlobalConstants.NDB_COL_POS].split("#", -1);

        List<Word.Meaning> meaningsEN = new ArrayList<>();

        /*
        String[] meaningsEN_list = namesDatabaseRow[GlobalConstants.NDB_COL_MEANINGS_EN].split("#", -1);
        if (!meaningsEN_list[0].equals("")) {
            for (int i = 0; i < meaningsEN_list.length; i++) {
                Word.Meaning meaning = new Word.Meaning();
                meaning.setMeaning(meaningsEN_list[i].replace("@@@@", "#").replace("$$$$", "\""));
                meaning.setType(POS_list[i]);
                meaningsEN.add(meaning);
            }
        }*/
        Word.Meaning meaning = new Word.Meaning();
        meaning.setMeaning("*");
        meaning.setType(POS_list[0]);
        meaningsEN.add(meaning);

        word.setMeaningsEN(meaningsEN);
        word.setMeaningsFR(new ArrayList<>());
        word.setMeaningsES(new ArrayList<>());

        return word;
    }
    private static List<Word.Meaning> getMeanings(List<String[]> centralDatabase, List<String[]> meaningsDatabase, int meaningsColumn,
                                                 List<String[]> multExplDatabase, List<String[]> examplesDatabase, int centralDbRowIndex, String language) {

        //Initializations
        int example_index;
        List<String> parsed_example_list;
        String matchingWordMeaning;
        String matchingWordType;
        String matchingWordAntonym;
        String matchingWordSynonym;
        String matchingWordExplanation;
        String matchingWordRules;
        String matchingWordExampleList;
        String[] current_meaning_characteristics;
        boolean has_multiple_explanations;
        String ME_index;
        int exampleLatinColumn;
        switch (language) {
            case "en": exampleLatinColumn = GlobalConstants.COLUMN_EXAMPLES_ENGLISH; break;
            case "fr": exampleLatinColumn = GlobalConstants.COLUMN_EXAMPLES_FRENCH; break;
            case "es": exampleLatinColumn = GlobalConstants.COLUMN_EXAMPLES_SPANISH; break;
            default: exampleLatinColumn = GlobalConstants.COLUMN_EXAMPLES_ENGLISH; break;
        }

        //Finding the meanings using the supplied index
        String MM_indexEN = centralDatabase.get(centralDbRowIndex)[meaningsColumn];
        List<String> MM_index_list = Arrays.asList(MM_indexEN.split(GlobalConstants.DB_ELEMENTS_DELIMITER));
        if (MM_index_list.size() == 0 || MM_index_list.get(0).equals("")) { return new ArrayList<>(); }

        List<Word.Meaning> meaningsList = new ArrayList<>();
        int current_MM_index;
        for (int i=0; i< MM_index_list.size(); i++) {

            Word.Meaning meaning = new Word.Meaning();
            current_MM_index = Integer.parseInt(MM_index_list.get(i))-1;
            current_meaning_characteristics = meaningsDatabase.get(current_MM_index);

            //Getting the Meaning value
            matchingWordMeaning = current_meaning_characteristics[GlobalConstants.COLUMN_MEANINGS_MEANING];
            if (matchingWordMeaning.equals("")) continue;

            //Getting the Type value
            matchingWordType = current_meaning_characteristics[GlobalConstants.COLUMN_MEANINGS_TYPE];

            //Adding "to " to the meaning values if the hit is a verb and these are english meanings
            if (language.equals("en") && matchingWordType.contains("V") && !matchingWordType.equals("VC") && !matchingWordType.equals("NV") && !matchingWordType.equals("VdaI")) {

                List<String> meaningElements = Arrays.asList(matchingWordMeaning.split(","));
                StringBuilder meaningFixed = new StringBuilder();
                boolean valueIsInParentheses = false;
                for (int k = 0; k < meaningElements.size(); k++) {
                    if (valueIsInParentheses) meaningFixed.append(meaningElements.get(k).trim());
                    else meaningFixed.append("to ").append(meaningElements.get(k).trim());

                    if (k < meaningElements.size() - 1) meaningFixed.append(", ");

                    if (meaningElements.get(k).contains("(") && !meaningElements.get(k).contains(")")) valueIsInParentheses = true;
                    else if (!meaningElements.get(k).contains("(") && meaningElements.get(k).contains(")")) valueIsInParentheses = false;
                }
                matchingWordMeaning = meaningFixed.toString();
            }

            //Setting the Meaning and Type values in the returned list
            meaning.setMeaning(matchingWordMeaning);
            meaning.setType(matchingWordType);

            //Getting the Opposite value
            matchingWordAntonym = current_meaning_characteristics[GlobalConstants.COLUMN_MEANINGS_ANTONYM];
            meaning.setAntonym(matchingWordAntonym);

            //Getting the Synonym value
            matchingWordSynonym = current_meaning_characteristics[GlobalConstants.COLUMN_MEANINGS_SYNONYM];
            meaning.setSynonym(matchingWordSynonym);

            //Getting the set of Explanations
            has_multiple_explanations = false;
            ME_index = "";
            if (current_meaning_characteristics[GlobalConstants.COLUMN_MEANINGS_EXPLANATION].length() > 3) {
                if (current_meaning_characteristics[GlobalConstants.COLUMN_MEANINGS_EXPLANATION].substring(0,3).equals("ME#")) {
                    has_multiple_explanations = true;
                    ME_index = current_meaning_characteristics[GlobalConstants.COLUMN_MEANINGS_EXPLANATION].substring(3);
                }
            }

            List<Word.Meaning.Explanation> explanationList = new ArrayList<>();
            if (has_multiple_explanations) {
                List<String> ME_index_list = Arrays.asList(ME_index.split(GlobalConstants.DB_ELEMENTS_DELIMITER));
                int current_ME_index;
                for (int j=0; j<ME_index_list.size(); j++) {

                    Word.Meaning.Explanation explanation = new Word.Meaning.Explanation();

                    current_ME_index = Integer.parseInt(ME_index_list.get(j))-1;

                    //Getting the Explanation value
                    matchingWordExplanation = multExplDatabase.get(current_ME_index)[GlobalConstants.COLUMN_MULT_EXPLANATIONS_ITEM];
                    explanation.setExplanation(matchingWordExplanation);

                    //Getting the Rules value
                    matchingWordRules = multExplDatabase.get(current_ME_index)[GlobalConstants.COLUMN_MULT_EXPLANATIONS_RULE];
                    explanation.setRules(matchingWordRules);

                    //Getting the Examples
                    matchingWordExampleList = multExplDatabase.get(current_ME_index)[GlobalConstants.COLUMN_MULT_EXPLANATIONS_EXAMPLES];
                    List<Word.Meaning.Explanation.Example> exampleList = new ArrayList<>();
                    if (!matchingWordExampleList.equals("") && !matchingWordExampleList.contains("Example")) {
                        parsed_example_list = Arrays.asList(matchingWordExampleList.split(GlobalConstants.DB_ELEMENTS_DELIMITER));
                        for (int t = 0; t < parsed_example_list.size(); t++) {
                            Word.Meaning.Explanation.Example example = new Word.Meaning.Explanation.Example();
                            example_index = Integer.parseInt(parsed_example_list.get(t)) - 1;
                            example.setLatinSentence(examplesDatabase.get(example_index)[exampleLatinColumn]);
                            example.setRomajiSentence(examplesDatabase.get(example_index)[GlobalConstants.COLUMN_EXAMPLES_ROMAJI]);
                            example.setKanjiSentence(examplesDatabase.get(example_index)[GlobalConstants.COLUMN_EXAMPLES_KANJI]);
                            exampleList.add(example);
                        }
                    }
                    explanation.setExamples(exampleList);
                    explanationList.add(explanation);
                }
            }
            else {
                Word.Meaning.Explanation explanation = new Word.Meaning.Explanation();

                //Getting the Explanation value
                matchingWordExplanation = meaningsDatabase.get(current_MM_index)[GlobalConstants.COLUMN_MEANINGS_EXPLANATION];
                explanation.setExplanation(matchingWordExplanation);

                //Getting the Rules value
                matchingWordRules = meaningsDatabase.get(current_MM_index)[GlobalConstants.COLUMN_MEANINGS_RULES];
                explanation.setRules(matchingWordRules);

                //Getting the Examples
                matchingWordExampleList = meaningsDatabase.get(current_MM_index)[GlobalConstants.COLUMN_MEANINGS_EXAMPLES];
                List<Word.Meaning.Explanation.Example> exampleList = new ArrayList<>();
                if (!matchingWordExampleList.equals("") && !matchingWordExampleList.contains("Example")) {
                    parsed_example_list = Arrays.asList(matchingWordExampleList.split(GlobalConstants.DB_ELEMENTS_DELIMITER));
                    for (int t = 0; t < parsed_example_list.size(); t++) {
                        Word.Meaning.Explanation.Example example = new Word.Meaning.Explanation.Example();
                        example_index = Integer.parseInt(parsed_example_list.get(t)) - 1;
                        example.setLatinSentence(examplesDatabase.get(example_index)[exampleLatinColumn]);
                        example.setRomajiSentence(examplesDatabase.get(example_index)[GlobalConstants.COLUMN_EXAMPLES_ROMAJI]);
                        example.setKanjiSentence(examplesDatabase.get(example_index)[GlobalConstants.COLUMN_EXAMPLES_KANJI]);
                        exampleList.add(example);
                    }
                }
                explanation.setExamples(exampleList);
                explanationList.add(explanation);
            }
            meaning.setExplanations(explanationList);
            meaningsList.add(meaning);

        }

        return meaningsList;
    }
    public static Verb createVerbFromCsvDatabase(List<String[]> verbDatabase, List<String[]> meaningsDatabase, int verbDbRowIndex) {

        // Value Initializations
        Verb verb = new Verb();
        boolean foundVerbType;
        String[] types;
        int lastCharIndex;
        String[] currentMeaningCharacteristics;

        verb.setVerbId(Integer.parseInt(verbDatabase.get(verbDbRowIndex)[GlobalConstants.COLUMN_WORD_ID]));
        verb.setPreposition(verbDatabase.get(verbDbRowIndex)[GlobalConstants.COLUMN_PREPOSITION]);
        verb.setKanjiRoot(verbDatabase.get(verbDbRowIndex)[GlobalConstants.COLUMN_KANJI_ROOT]);
        verb.setLatinRoot(verbDatabase.get(verbDbRowIndex)[GlobalConstants.COLUMN_LATIN_ROOT]);
        verb.setExceptionIndex(verbDatabase.get(verbDbRowIndex)[GlobalConstants.COLUMN_EXCEPTION_INDEX]);
        verb.setRomaji(verbDatabase.get(verbDbRowIndex)[GlobalConstants.COLUMN_ROMAJI]);
        verb.setKanji(verbDatabase.get(verbDbRowIndex)[GlobalConstants.COLUMN_KANJI]);
        verb.setAltSpellings(verbDatabase.get(verbDbRowIndex)[GlobalConstants.COLUMN_ALT_SPELLINGS]);
        verb.setHiraganaFirstChar(ConvertFragment.getLatinHiraganaKatakana(verb.getRomaji()).get(GlobalConstants.TYPE_HIRAGANA).substring(0,1));

        //Setting the family
        String MM_index = verbDatabase.get(verbDbRowIndex)[GlobalConstants.COLUMN_MEANING_EN_INDEXES];
        List<String> MM_index_list = Arrays.asList(MM_index.split(GlobalConstants.DB_ELEMENTS_DELIMITER));
        if (MM_index_list.size() == 0) { return verb; }

        int current_MM_index;
        List<String> trans = new ArrayList<>();
        for (int i=0; i< MM_index_list.size(); i++) {

            current_MM_index = Integer.parseInt(MM_index_list.get(i)) - 1;
            currentMeaningCharacteristics = meaningsDatabase.get(current_MM_index);

            //Getting the Family value
            types = currentMeaningCharacteristics[2].split(GlobalConstants.DB_ELEMENTS_DELIMITER);

            foundVerbType = false;
            for (String type : types) {
                lastCharIndex = type.length()-1;
                if (type.substring(0,1).equals("V") && (type.substring(lastCharIndex).equals("T") || type.substring(lastCharIndex).equals("I"))) {
                    trans.add(String.valueOf(type.charAt(lastCharIndex)));
                    if (i==0) verb.setFamily(type.substring(1, lastCharIndex)); //only keeping the verb itself
                    foundVerbType = true;
                    break;
                }
            }

            if (!foundVerbType) {
                Log.i(DEBUG_TAG, "Warning! No VxxxT/I type found for verb " + verb.getRomaji() + " (Meaning index:" + Integer.toString(current_MM_index) + ")");
                //No exception catching is made here, in order to make sure that database errors are caught before production
            }
        }

        //Setting the transitive/intransitive flag
        for (int i=0; i<trans.size(); i++) {
            if (i>0 && !trans.get(i-1).equals(trans.get(i)) ) {
                verb.setTrans("T/I");
                break;
            }
            if (i == trans.size()-1) verb.setTrans(trans.get(i));
        }


        return verb;
    }


    //Database operations utilities
    public static List<Word> getMergedWordsList(List<Word> localWords, List<Word> asyncWords, String languageCode) {

        List<Word> finalWordsList = new ArrayList<>();
        List<Word> finalAsyncWords = new ArrayList<>(asyncWords);
        boolean asyncMeaningFoundLocally;

        for (int j = 0; j< localWords.size(); j++) {
            Word currentLocalWord = localWords.get(j);
            Word finalWord = new Word();

            //Copying basic properties
            finalWord.setRomaji(currentLocalWord.getRomaji());
            finalWord.setKanji(currentLocalWord.getKanji());
            finalWord.setExtraKeywordsEN(currentLocalWord.getExtraKeywordsEN());
            finalWord.setExtraKeywordsFR(currentLocalWord.getExtraKeywordsFR());
            finalWord.setExtraKeywordsES(currentLocalWord.getExtraKeywordsES());
            finalWord.setExtraKeywordsJAP(currentLocalWord.getExtraKeywordsJAP());
            finalWord.setIsCommon(currentLocalWord.getIsCommon());
            finalWord.setIsLocal(currentLocalWord.getIsLocal());
            finalWord.setVerbConjMatchStatus(currentLocalWord.getVerbConjMatchStatus());
            finalWord.setMatchingConj(currentLocalWord.getMatchingConj());

            //Adjusting and copying alt spellings
            List<String> finalAltSpellings;
            if (TextUtils.isEmpty(currentLocalWord.getAltSpellings())) finalAltSpellings = new ArrayList<>();
            else {
                finalAltSpellings = new ArrayList<>(Arrays.asList(currentLocalWord.getAltSpellings().split(",")));
                for (int i = 0; i< finalAltSpellings.size(); i++) finalAltSpellings.set(i,finalAltSpellings.get(i).trim());
            }
            finalWord.setAltSpellings(TextUtils.join(", ", finalAltSpellings));

            //Updating and copying meanings/alt spellings from the async word
            List<Word.Meaning> currentLocalMeanings = currentLocalWord.getMeaningsEN();
            List<Word.Meaning> currentFinalMeanings = new ArrayList<>(currentLocalMeanings);

            int currentIndex = finalAsyncWords.size()-1;
            while (currentIndex >= 0 && finalAsyncWords.size() != 0) {

                if (currentIndex > finalAsyncWords.size()-1) break;
                Word currentAsyncWord = finalAsyncWords.get(currentIndex);

                if (    currentAsyncWord.getRomaji().replace(" ", "")
                        .equals(currentLocalWord.getRomaji().replace(" ", ""))
                        &&  currentAsyncWord.getKanji().equals(currentLocalWord.getKanji())   ) {

                    //Setting the altSpellings
                    String finalAsyncWordAltSpellings = finalAsyncWords.get(currentIndex).getAltSpellings();
                    if (!TextUtils.isEmpty(finalAsyncWordAltSpellings)) {
                        for (String altSpelling : finalAsyncWordAltSpellings.split(",")) {
                            if (!finalAltSpellings.contains(altSpelling.trim())) {
                                finalAltSpellings.add(altSpelling.trim());
                            }
                        }
                    }
                    finalWord.setAltSpellings(TextUtils.join(", ", finalAltSpellings));

                    //Setting the meanings
                    List<Word.Meaning> currentAsyncMeanings = currentAsyncWord.getMeaningsEN();
                    for (int m = 0; m< currentAsyncMeanings.size(); m++) {

                        asyncMeaningFoundLocally = false;
                        for (int k = 0; k< currentLocalMeanings.size(); k++) {

                            if (currentLocalMeanings.get(k).getMeaning()
                                    .contains( currentAsyncMeanings.get(m).getMeaning() ) ) {
                                asyncMeaningFoundLocally = true;
                                break;
                            }
                        }
                        if (!asyncMeaningFoundLocally) {
                            currentFinalMeanings.add(currentAsyncMeanings.get(m));
                        }
                    }
                    finalAsyncWords.remove(currentIndex);
                    if (currentIndex == 0) break;
                }
                else {
                    currentIndex -= 1;
                }
            }
            finalWord.setMeaningsEN(currentFinalMeanings);
            finalWord.setMeaningsFR(currentLocalWord.getMeaningsFR());
            finalWord.setMeaningsES(currentLocalWord.getMeaningsES());

            finalWordsList.add(finalWord);
        }

        //Once all async words have been merged with the local words, set isLocal=false to the remaining async words and add them to the list
        finalWordsList.addAll(finalAsyncWords);

        return finalWordsList;
    }
    public static List<Word> getDifferentAsyncWords(List<Word> localWords, List<Word> asyncWords) {

        List<Word> differentAsyncWords = new ArrayList<>();
        List<Word> remainingLocalWords = new ArrayList<>(localWords);
        List<Word.Meaning> localMeanings;
        List<Word.Meaning> asyncMeanings;
        List<Word.Meaning> remainingLocalMeanings;
        boolean foundMatchingLocalWord;
        int localMeaningIndex ;
        int localWordIndex;
        String asyncRomaji;
        String localRomaji;
        String asyncKanji;
        String localKanji;

        Word localWord;

        for (Word asyncWord : asyncWords) {

            foundMatchingLocalWord = false;
            localWordIndex = 0;

            asyncRomaji = asyncWord.getRomaji();
            asyncKanji = asyncWord.getKanji();

            while (localWordIndex < remainingLocalWords.size()) {

                localWord = remainingLocalWords.get(localWordIndex);
                localRomaji = localWord.getRomaji().replace(" ", "");
                localKanji = localWord.getKanji();

                if ( asyncRomaji.equals(localRomaji) && asyncKanji.equals(localKanji) ) {

                    foundMatchingLocalWord = true;

                    localMeanings = localWord.getMeaningsEN();
                    asyncMeanings = asyncWord.getMeaningsEN();


                    //If non-identical meanings remain, it is possible that a Jisho meaning was split by types in the local database, therefore check the following:
                    StringBuilder allLocalMeanings = new StringBuilder();
                    for (Word.Meaning meaning : localMeanings) {
                        allLocalMeanings.append(meaning.getMeaning());
                        allLocalMeanings.append(", ");
                    }

                    List<String> allAsyncMeaningElements = new ArrayList<>();
                    boolean isInParenthesis;
                    StringBuilder currentElement;
                    String currentAsyncMeaning;
                    String currentAsyncMeaningChar;
                    for (Word.Meaning asyncWordMeaning : asyncMeanings) {
                        isInParenthesis = false;
                        currentElement = new StringBuilder();
                        currentAsyncMeaning = asyncWordMeaning.getMeaning();
                        for (int i=0; i<asyncWordMeaning.getMeaning().length(); i++) {
                            currentAsyncMeaningChar = currentAsyncMeaning.substring(i,i+1);
                            if (currentAsyncMeaningChar.equals("(")) isInParenthesis = true;
                            else if (currentAsyncMeaningChar.equals(")")) isInParenthesis = false;

                            if (isInParenthesis || !currentAsyncMeaningChar.equals(",")) currentElement.append(currentAsyncMeaningChar);

                            if (currentAsyncMeaningChar.equals(",") && !isInParenthesis || i == asyncWordMeaning.getMeaning().length()-1) {
                                allAsyncMeaningElements.add(currentElement.toString().trim());
                                currentElement = new StringBuilder();
                            }
                        }
                    }

                    String allLocalMeaningsAsString = allLocalMeanings.toString();
                    boolean meaningNotFoundInLocalWord = false;
                    for (String element : allAsyncMeaningElements) {
                        if (!allLocalMeaningsAsString.contains(element)) {
                            meaningNotFoundInLocalWord = true;
                            break;
                        }
                    }

                    boolean altSpellingNotFoundInLocalWord = false;
                    if (!TextUtils.isEmpty(asyncWord.getAltSpellings())) {
                        if (TextUtils.isEmpty(localWord.getAltSpellings())) altSpellingNotFoundInLocalWord = true;
                        else {
                            for (String asyncAltSpelling : asyncWord.getAltSpellings().split(",")) {
                                if (!localWord.getAltSpellings().contains(asyncAltSpelling.trim())) {
                                    altSpellingNotFoundInLocalWord = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (meaningNotFoundInLocalWord || altSpellingNotFoundInLocalWord) differentAsyncWords.add(asyncWord);

                    remainingLocalWords.remove(localWord);
                    break;
                }
                else {
                    localWordIndex++;
                }
            }

            if (!foundMatchingLocalWord) differentAsyncWords.add(asyncWord);

        }

        for (Word word : differentAsyncWords) {
            if (word.getKanji().equals("為る")) {
                differentAsyncWords.remove(word);
                break;
            }
        }

        return differentAsyncWords;
    }
    public static List<Word> getCommonWords(List<Word> wordsList) {
        List<Word> commonWords = new ArrayList<>();
        for (Word word : wordsList) {
            if (word.getIsCommon()) commonWords.add(word);
        }
        return commonWords;
    }
    public static List<long[]> bubbleSortForThreeIntegerList(List<long[]> MatchList) {

        // Sorting the results according to the shortest keyword as found in the above search

        // Computing the value length
        int list_size = MatchList.size();
        long[][] matches = new long[list_size][3];
        for (int i=0;i<list_size;i++) {
            matches[i][0] = MatchList.get(i)[0];
            matches[i][1] = MatchList.get(i)[1];
            matches[i][2] = MatchList.get(i)[2];
        }

        // Sorting
        long tempVar0;
        long tempVar1;
        long tempVar2;
        for (int i=0;i<list_size;i++) { //Bubble sort
            for (int t=1;t<list_size-i;t++) {
                if (matches[t-1][1] > matches[t][1]) {
                    tempVar0 = matches[t-1][0];
                    tempVar1 = matches[t-1][1];
                    tempVar2 = matches[t-1][2];
                    matches[t-1][0] = matches[t][0];
                    matches[t-1][1] = matches[t][1];
                    matches[t-1][2] = matches[t][2];
                    matches[t][0] = tempVar0;
                    matches[t][1] = tempVar1;
                    matches[t][2] = tempVar2;
                }
            }
        }

        List<long[]> sortedMatchList = new ArrayList<>();
        long[] element;
        for (int i=0;i<list_size;i++) {
            element = new long[3];
            element[0] = matches[i][0];
            element[1] = matches[i][1];
            element[2] = matches[i][2];
            sortedMatchList.add(element);
        }

        return sortedMatchList;
    }

    /**
     * Gets the length of the shortest meaning containing the word and other characteristics, and use it to determine the word's ranking
     * @param currentWord
     * @param mInputQuery
     * @param queryWordWithoutTo
     * @param queryIsVerbWithTo
     * @return
     */
    public static int getRankingFromWordAttributes(Word currentWord, String mInputQuery, String queryWordWithoutTo, boolean queryIsVerbWithTo, String language) {

        String inputQueryLatin = ConvertFragment.getLatinHiraganaKatakana(mInputQuery).get(GlobalConstants.TYPE_LATIN);
        int ranking;
        String romaji_value = currentWord.getRomaji();
        String kanji_value = currentWord.getKanji();
        String altSpellings_value = currentWord.getAltSpellings();
        String kwJap_value = currentWord.getExtraKeywordsJAP() == null? "" : currentWord.getExtraKeywordsJAP();
        String kwLat_value ="";
        String type = currentWord.getMeaningsEN().get(0).getType();
        boolean currentWordIsAVerb = type.length()>0 && type.substring(0,1).equals("V") && !type.equals("VC") && !type.equals("NV");

        int EXACT_WORD_MATCH_BONUS = 300;
        int WORD_MATCH_IN_SENTENCE_BONUS = 100;
        int LATE_HIT_IN_SENTENCE_PENALTY = 25;
        int LATE_MEANING_MATCH_PENALTY = 100;

        // Getting ranking according to meaning string length
        // with penalties depending on the lateness of the word in the meanings
        // and the exactness of the match
        List<Word.Meaning> currentMeanings = currentWord.getMeaningsEN();
        switch (language) {
            case "en":
                currentMeanings = currentWord.getMeaningsEN();
                kwLat_value = currentWord.getExtraKeywordsEN() == null? "" : currentWord.getExtraKeywordsEN();
                break;
            case "fr":
                currentMeanings = currentWord.getMeaningsFR();
                kwLat_value = currentWord.getExtraKeywordsFR() == null? "" : currentWord.getExtraKeywordsFR();
                break;
            case "es":
                currentMeanings = currentWord.getMeaningsES();
                kwLat_value = currentWord.getExtraKeywordsES() == null? "" : currentWord.getExtraKeywordsES();
                break;
        }
        int missingLanguagePenatly = 0;
        if (currentMeanings == null || currentMeanings.size()==0) {
            missingLanguagePenatly = 10000;
            currentMeanings = currentWord.getMeaningsEN();
        }

        String currentMeaning;
        String inputQuery;
        int baseMeaningLength = 1500;
        int lateMeaningPenalty = 0;
        boolean foundMeaningLength;
        int lateHitInMeaningPenalty; //Adding a penalty for late hits in the meaning
        int cumulativeMeaningLength; //Using a cumulative meaning length instead of the total length, since if a word is at the start of a meaning it's more important and the hit is more likely to be relevant

        ranking = baseMeaningLength;
        for (int j = 0; j< currentMeanings.size(); j++) {
            currentMeaning = currentMeanings.get(j).getMeaning().toLowerCase();

            //region If the current word is not a verb
            if (!currentWordIsAVerb) {
                foundMeaningLength = false;

                inputQuery = mInputQuery;
                baseMeaningLength = 1000; //was 1500

                //If meaning has the exact word, get the length as follows
                String[] currentMeaningIndividualElements = Utilities.splitAtCommasOutsideParentheses(currentMeaning);
                lateHitInMeaningPenalty = 0;
                cumulativeMeaningLength = 0;
                for (String currentMeaningElement : currentMeaningIndividualElements) {

                    String trimmedElement = currentMeaningElement.trim().toLowerCase();

                    //If there's an exact match, push the word up in ranking
                    if (trimmedElement.equals(inputQuery)) {
                        ranking = baseMeaningLength + lateMeaningPenalty + lateHitInMeaningPenalty + cumulativeMeaningLength - EXACT_WORD_MATCH_BONUS;
                        foundMeaningLength = true;
                    }
                    if (foundMeaningLength) break;

                    String[] currentMeaningIndividualWords = trimmedElement.split(" ");
                    for (String word : currentMeaningIndividualWords) {
                        cumulativeMeaningLength += word.length() + 2; //Added 2 to account for missing ", " instances in loop
                        if (word.equals(inputQuery)) {
                            ranking = baseMeaningLength + lateMeaningPenalty + lateHitInMeaningPenalty + cumulativeMeaningLength - WORD_MATCH_IN_SENTENCE_BONUS;
                            foundMeaningLength = true;
                            break;
                        }
                        lateHitInMeaningPenalty += LATE_HIT_IN_SENTENCE_PENALTY;
                    }
                    if (foundMeaningLength) break;

                    //If meaning has the exact word but maybe in parentheses, get the length as follows
                    String[] currentMeaningIndividualWordsWithoutParentheses = trimmedElement
                            .replace("(","").replace(")","").split(" ");
                    lateHitInMeaningPenalty = 0;
                    cumulativeMeaningLength = 0;
                    for (String word : currentMeaningIndividualWordsWithoutParentheses) {
                        cumulativeMeaningLength += word.length() + 2; //Added 2 to account for missing ", " instances in loop
                        if (word.equals(inputQuery)) {
                            ranking = baseMeaningLength + lateMeaningPenalty + lateHitInMeaningPenalty + cumulativeMeaningLength;
                            foundMeaningLength = true;
                            break;
                        }
                        lateHitInMeaningPenalty += LATE_HIT_IN_SENTENCE_PENALTY;
                    }
                    if (foundMeaningLength) break;

                    lateHitInMeaningPenalty += 50;
                }
                if (foundMeaningLength) break;

                //If still not found, get the length of the less important results
                if (currentMeaning.contains(inputQuery) && currentMeaning.length() <= ranking) {
                    ranking = ranking + currentMeaning.length();
                }
            }
            //endregion

            //region If the current word is a verb
            else {
                foundMeaningLength = false;

                String[] currentMeaningIndividualElements = Utilities.splitAtCommasOutsideParentheses(currentMeaning);
                if (!queryIsVerbWithTo) {
                    baseMeaningLength = 1000;

                    //Calculate the length first by adding "to " to the input query. If it leads to a hit, that means that this verb is relevant
                    inputQuery = "to " + mInputQuery;

                    lateHitInMeaningPenalty = 0;
                    cumulativeMeaningLength = 0;
                    for (String element : currentMeaningIndividualElements) {

                        String trimmedElement = element.trim();

                        //If there's an exact match, push the word up in ranking
                        if (trimmedElement.equals(inputQuery)) {
                            ranking = baseMeaningLength + lateMeaningPenalty + lateHitInMeaningPenalty + cumulativeMeaningLength - EXACT_WORD_MATCH_BONUS;
                            foundMeaningLength = true;
                        }
                        if (foundMeaningLength) break;

                        cumulativeMeaningLength += element.length() + 2; //Added 2 to account for missing ", " instances in loop
                        if (trimmedElement.contains(inputQuery)) {
                            ranking = baseMeaningLength + lateMeaningPenalty + lateHitInMeaningPenalty + cumulativeMeaningLength - WORD_MATCH_IN_SENTENCE_BONUS;
                            foundMeaningLength = true;
                            break;
                        }
                        lateHitInMeaningPenalty += LATE_HIT_IN_SENTENCE_PENALTY;
                    }
                    if (foundMeaningLength) break;


                    //Otherwise, use the original query to get the length
                    inputQuery = mInputQuery;
                    lateHitInMeaningPenalty = 0;
                    cumulativeMeaningLength = 0;
                    for (String element : currentMeaningIndividualElements) {

                        String trimmedElement = element.trim();

                        //If there's an exact match, push the word up in ranking
                        if (trimmedElement.equals(inputQuery)) {
                            ranking = baseMeaningLength + lateMeaningPenalty + lateHitInMeaningPenalty + cumulativeMeaningLength - EXACT_WORD_MATCH_BONUS;
                            foundMeaningLength = true;
                        }
                        if (foundMeaningLength) break;

                        if (trimmedElement.contains(inputQuery)) {
                            ranking = baseMeaningLength + lateMeaningPenalty + lateHitInMeaningPenalty + cumulativeMeaningLength - WORD_MATCH_IN_SENTENCE_BONUS;
                            foundMeaningLength = true;
                            break;
                        }
                        cumulativeMeaningLength += element.length() + 2; //Added 2 to account for missing ", " instances in loop
                        lateHitInMeaningPenalty += LATE_HIT_IN_SENTENCE_PENALTY;
                    }
                    if (foundMeaningLength) break;
                }
                else {
                    baseMeaningLength = 200;
                    inputQuery = mInputQuery;

                    //Get the length according to the position of the verb in the meanings list
                    lateHitInMeaningPenalty = 0;
                    cumulativeMeaningLength = 0;
                    for (String element : currentMeaningIndividualElements) {

                        String trimmedElement = element.trim();

                        //If there's an exact match, push the word up in ranking
                        if (trimmedElement.equals(inputQuery)) {
                            ranking = baseMeaningLength + lateMeaningPenalty + lateHitInMeaningPenalty + cumulativeMeaningLength - EXACT_WORD_MATCH_BONUS;
                            foundMeaningLength = true;
                        }
                        if (foundMeaningLength) break;

                        cumulativeMeaningLength += element.length() + 2; //Added 2 to account for missing ", " instances in loop
                        if (trimmedElement.contains(inputQuery)) {
                            ranking = baseMeaningLength + lateMeaningPenalty + lateHitInMeaningPenalty + cumulativeMeaningLength - WORD_MATCH_IN_SENTENCE_BONUS;
                            foundMeaningLength = true;
                            break;
                        }
                        lateHitInMeaningPenalty += LATE_HIT_IN_SENTENCE_PENALTY;
                    }
                    if (foundMeaningLength) break;
                }

            }
            //endregion

            lateMeaningPenalty += LATE_MEANING_MATCH_PENALTY;

        }

        //Adding the romaji and kanji lengths to the ranking (ie. shorter is better)
        ranking = 2*(romaji_value.length() + kanji_value.length()) + ranking;

        //If the word starts with the inputQuery, its ranking improves
        String romajiNoSpaces = getRomajiNoSpacesForSpecialPartsOfSpeech(romaji_value);

        if (       (romaji_value.length() >= mInputQuery.length() && romaji_value.substring(0,mInputQuery.length()).equals(mInputQuery))
                || (romaji_value.length() >= mInputQuery.length() && romaji_value.substring(0,mInputQuery.length()).equals(inputQueryLatin))
                || romajiNoSpaces.equals(mInputQuery) || romajiNoSpaces.equals(inputQueryLatin)
                || (kanji_value.length() >= mInputQuery.length() && kanji_value.substring(0,mInputQuery.length()).equals(mInputQuery))
                ) {
            ranking -= 100;
        }

        //If the word is a name, the ranking worsens
        if (GlobalConstants.NAMES_LIST.contains(currentWord.getMeaningsEN().get(0).getType())) ranking += 5000;

        //If the word is common, the ranking improves
        if (currentWord.getIsCommon()) ranking -= 50;

        //If the word is a verb and one of its conjugations is a perfect match, the ranking improves
        if (currentWord.getVerbConjMatchStatus() == Word.CONJ_MATCH_EXACT) ranking -= 100;

        //If the word is a verb and one of its conjugations is a partial match, the ranking improves a bit
        if (currentWord.getVerbConjMatchStatus() == Word.CONJ_MATCH_CONTAINED) ranking -= 30;

        //If one of the elements in altSpellings is a perfect match, the ranking improves
        for (String element : altSpellings_value.split(",")) {
            if (mInputQuery.equals(element.trim()) || inputQueryLatin.equals(element.trim())) {
                ranking -= 70;
                break;
            }
        }

        //If one of the elements in the Japanese Keywords is a perfect match, the ranking improves
        for (String element : kwJap_value.split(",")) {
            if (mInputQuery.equals(element.trim()) || inputQueryLatin.equals(element.trim())) {
                ranking -= 70;
                break;
            }
        }

        //If one of the elements in the Latin Keywords is a perfect match, the ranking improves
        for (String element : kwLat_value.split(",")) {
            if (mInputQuery.equals(element.trim()) || inputQueryLatin.equals(element.trim())) {
                ranking -= 40;
                break;
            }
        }

        //If the romaji or Kanji value is an exact match to the search word, then it must appear at the start of the list
        if (romaji_value.equals(mInputQuery) || kanji_value.equals(mInputQuery)) ranking = 0;

        ranking += missingLanguagePenatly;

        return ranking;
    }
    /**
     * Returns the word ids that match the searchWord.
     * Performing basic filtering according to each word's keywords list if length>=4, or differently for shorter words.
     * @param inputWord
     * @param roomCentralDatabase
     * @param roomExtendedRoomDatabase
     * @param roomNamesRoomDatabase
     * @param language
     * @param completeWithNamesIfNoResultsEvenIfDontShowNames
     * @return
     */
    public static Object[] getMatchingWordIdsAndDoBasicFiltering(String inputWord,
                                                                 RoomCentralDatabase roomCentralDatabase,
                                                                 RoomExtendedDatabase roomExtendedRoomDatabase,
                                                                 RoomNamesDatabase roomNamesRoomDatabase,
                                                                 String language,
                                                                 boolean showNames, boolean completeWithNamesIfNoResultsEvenIfDontShowNames) {

        //region Initializations
        List<Long> matchingWordIdsCentral = new ArrayList<>();
        List<Long> matchingWordIdsExtended = new ArrayList<>();
        List<Long> matchingWordIdsNames = new ArrayList<>();
        String searchWordNoSpaces;
        boolean queryIsVerbWithTo = false;
        String searchWordWithoutTo = "";
        String searchWord;
        //endregion

        //region Converting the searchWord to usable forms, preventing invalid characters from influencing the search results
        inputWord = inputWord.toLowerCase(Locale.ENGLISH);
        inputWord = removeApostrophes(inputWord);
        inputWord = Utilities.removeNonSpaceSpecialCharacters(inputWord);
        searchWord = inputWord;
        searchWordNoSpaces = searchWord.replace(" ", "");

        //Registering if the input query is a "to " verb
        if (searchWord.length()>3 && searchWord.substring(0,3).equals("to ")) {
            queryIsVerbWithTo = true;
            searchWordWithoutTo = searchWord.substring(3);
        }
        //endregion

        //region If there is an "inging" verb instance, reduce it to an "ing" instance (e.g. singing >> sing)
        String verb2;
        String inglessVerb = searchWord;
        if (searchWord.length() > 2 && searchWord.substring(searchWord.length()-3).equals("ing")) {

            if (searchWord.length() > 5 && searchWord.substring(searchWord.length()-6).equals("inging")) {
                if (	(searchWord.substring(0, 3).equals("to ") && isOfTypeIngIng(searchWord.substring(3))) ||
                        (!searchWord.substring(0, 3).equals("to ") && isOfTypeIngIng(searchWord))   ) {
                    // If the verb ends with "inging" then remove the the second "ing"
                    inglessVerb = searchWord.substring(0,searchWord.length()-3);
                }
            }
            else {
//                verb2 = searchWord + "ing";
//                if ((!verb2.substring(0, 2+1).equals("to ") || !checkIfWordIsOfTypeIngIng(verb2.substring(3, searchWord.length() + 3))) &&
//                        (verb2.substring(0, 2+1).equals("to ") || !checkIfWordIsOfTypeIngIng(verb2.substring(0, searchWord.length() + 3)))) {
//                    // If the verb does not belong to the keywords, then remove the ending "ing" so that it can be compared later on to the verbs excel
//                    //If the verb is for e.g. to sing / sing, where verb2 = to singing / singing, then check that verb2 (without the "to ") belongs to the keywords, and if it does then do nothing
//
//                    inglessVerb = searchWord.substring(0,searchWord.length()-3);
//                }
            }
        }
        //endregion

        //region Getting the input type and its converted form (romaji/kanji/invalid)
        List<String> translationList = ConvertFragment.getLatinHiraganaKatakana(searchWordNoSpaces);

        String searchWordTransliteratedLatin = translationList.get(GlobalConstants.TYPE_LATIN);
        String searchWordTransliteratedHiragana = translationList.get(GlobalConstants.TYPE_HIRAGANA);
        String searchWordTransliteratedKatakana = translationList.get(GlobalConstants.TYPE_KATAKANA);
        int inputTextType = ConvertFragment.getTextType(searchWord);

        List<String> possibleInterpretations = new ArrayList<>();
        if (inputTextType == GlobalConstants.TYPE_LATIN) possibleInterpretations = ConvertFragment.getWaapuroRomanizationsFromLatinText(searchWordNoSpaces);
        else {
            possibleInterpretations.add(searchWordTransliteratedLatin);
        }

        if (inputTextType == GlobalConstants.TYPE_INVALID) return new Object[]{new ArrayList<>(), new ArrayList<>()};
        //endregion

        //region Replacing a Kana input by its Romaji form
        if (inputTextType == GlobalConstants.TYPE_HIRAGANA || inputTextType == GlobalConstants.TYPE_KATAKANA) {
            searchWord = searchWordTransliteratedLatin;
            searchWordNoSpaces = searchWordTransliteratedLatin;
        }
        //endregion

        //region Getting the matches
        matchingWordIdsCentral = addNormalMatchesToMatchesList(searchWord, searchWordNoSpaces, inglessVerb, searchWordWithoutTo, possibleInterpretations,
                queryIsVerbWithTo, inputTextType, matchingWordIdsCentral,
                roomCentralDatabase, roomExtendedRoomDatabase,
                language, false);

        matchingWordIdsCentral = addConjugatedAdjectivesToMatchesList(searchWord, inputTextType, matchingWordIdsCentral, roomCentralDatabase);
        matchingWordIdsCentral = addCountersToMatchesList(searchWord, inputTextType, matchingWordIdsCentral, roomCentralDatabase);

        if (roomExtendedRoomDatabase != null)
            matchingWordIdsExtended = addNormalMatchesToMatchesList(searchWord, searchWordNoSpaces, inglessVerb, searchWordWithoutTo, possibleInterpretations,
                queryIsVerbWithTo, inputTextType, new ArrayList<>(),
                    roomCentralDatabase, roomExtendedRoomDatabase,
                language, true);

        if (roomNamesRoomDatabase != null && (showNames || matchingWordIdsCentral.size()==0 && completeWithNamesIfNoResultsEvenIfDontShowNames)) {
            matchingWordIdsNames = addNamesToMatchesList(searchWord, inputTextType, roomNamesRoomDatabase);
        }

        //endregion

        return new Object[]{matchingWordIdsCentral, matchingWordIdsExtended, matchingWordIdsNames};
    }
    private static List<Long> getMatchingWordIdsForOriginalInputQuery(boolean forceExactSearch, int inputTextType, String searchWord, String searchWordNoSpaces, List<String> possibleInterpretations,
                                                                      RoomCentralDatabase roomCentralDatabase,
                                                                      RoomExtendedDatabase japaneseToolboxExtendedRoomDatabase,
                                                                      String language, boolean use_extended_db) {

        List<Long> matchingWordIds = new ArrayList<>();

        //region Search for the matches in the indexed keywords
        List<String> searchResultIndexesArray = new ArrayList<>();
        List<Object> latinIndices;
        List<IndexKanji> kanjiIndices;
        if (inputTextType == GlobalConstants.TYPE_LATIN || inputTextType == GlobalConstants.TYPE_HIRAGANA
                || inputTextType == GlobalConstants.TYPE_KATAKANA || inputTextType == GlobalConstants.TYPE_NUMBER) {

            //If the input is a verb in "to " form, remove the "to " for the search only (results will be filtered later on)
            String inputWord = Utilities.removeNonSpaceSpecialCharacters(searchWord);
            if (searchWord.length()>3) {
                if (searchWord.substring(0, 3).equals("to ")) {
                    inputWord = searchWordNoSpaces.substring(2);
                }
            }
            String inputWordNoSpaces = Utilities.removeSpecialCharacters(inputWord);

            boolean exactSearch = inputWordNoSpaces.length() < 3 || forceExactSearch;
            latinIndices = findQueryInLatinIndices(inputWordNoSpaces, possibleInterpretations, exactSearch, new String[]{"romaji", language},
                    roomCentralDatabase, japaneseToolboxExtendedRoomDatabase, use_extended_db);

            if (latinIndices.size()==0) return matchingWordIds;

            // If the entered word is Latin and only has up to WORD_SEARCH_CHAR_COUNT_THRESHOLD characters, limit the word keywords to be checked later
            if ((   (inputTextType == GlobalConstants.TYPE_LATIN
                        || inputTextType == GlobalConstants.TYPE_HIRAGANA
                        || inputTextType == GlobalConstants.TYPE_KATAKANA)
                        && searchWordNoSpaces.length() < WORD_SEARCH_CHAR_COUNT_THRESHOLD)
                    || (inputTextType == GlobalConstants.TYPE_NUMBER
                        && searchWordNoSpaces.length() < WORD_SEARCH_CHAR_COUNT_THRESHOLD-1)) {

                for (Object indexLatin : latinIndices) {
                    if (indexLatin instanceof IndexRomaji && ((IndexRomaji) indexLatin).getValue().length() < WORD_SEARCH_CHAR_COUNT_THRESHOLD ) {
                        searchResultIndexesArray.add(((IndexRomaji) indexLatin).getWordIds());
                        break;
                    }
                    else if (indexLatin instanceof IndexEnglish && ((IndexEnglish) indexLatin).getValue().length() < WORD_SEARCH_CHAR_COUNT_THRESHOLD ) {
                        searchResultIndexesArray.add(((IndexEnglish) indexLatin).getWordIds());
                        break;
                    }
                    else if (indexLatin instanceof IndexFrench && ((IndexFrench) indexLatin).getValue().length() < WORD_SEARCH_CHAR_COUNT_THRESHOLD ) {
                        searchResultIndexesArray.add(((IndexFrench) indexLatin).getWordIds());
                        break;
                    }
                    else if (indexLatin instanceof IndexSpanish && ((IndexSpanish) indexLatin).getValue().length() < WORD_SEARCH_CHAR_COUNT_THRESHOLD ) {
                        searchResultIndexesArray.add(((IndexSpanish) indexLatin).getWordIds());
                        break;
                    }
                }
            }
            else {
                for (Object indexLatin : latinIndices) {
                    if (indexLatin instanceof IndexRomaji )         searchResultIndexesArray.add(((IndexRomaji) indexLatin).getWordIds());
                    else if (indexLatin instanceof IndexEnglish )   searchResultIndexesArray.add(((IndexEnglish) indexLatin).getWordIds());
                    else if (indexLatin instanceof IndexFrench )    searchResultIndexesArray.add(((IndexFrench) indexLatin).getWordIds());
                    else if (indexLatin instanceof IndexSpanish )   searchResultIndexesArray.add(((IndexSpanish) indexLatin).getWordIds());
                }
            }

        } else if (inputTextType == GlobalConstants.TYPE_KANJI) {
            kanjiIndices = findQueryInKanjiIndex(searchWordNoSpaces, forceExactSearch, roomCentralDatabase, japaneseToolboxExtendedRoomDatabase, use_extended_db);
            if (kanjiIndices.size()==0) return matchingWordIds;
            for (IndexKanji indexKanji : kanjiIndices) {
                searchResultIndexesArray.add(indexKanji.getWordIds());
            }
        } else {
            return new ArrayList<>();
        }
        //endregion

        //region Get the indexes of all of the results that were found
        List<String> indexList;
        for (String searchResultIndexes : searchResultIndexesArray) {
            indexList = Arrays.asList(searchResultIndexes.split(GlobalConstants.DB_ELEMENTS_DELIMITER));
            for (int j = 0; j < indexList.size(); j++) {
                matchingWordIds.add(Long.valueOf(indexList.get(j)));
            }
        }
        //endregion

        return matchingWordIds;
    }
    private static List<Long> getMatchingWordIdsForQueryWithoutExtraIng(boolean forceExactSearch, List<Long> matchingWordIdsFromIndex, String inglessVerb,
                                                                        RoomCentralDatabase roomCentralDatabase,
                                                                        RoomExtendedDatabase japaneseToolboxExtendedRoomDatabase, boolean use_extended_db) {

        List<Long> newMatchingWordIdsFromIndex = new ArrayList<>(matchingWordIdsFromIndex);

        boolean exactSearch = inglessVerb.length() < 3 || forceExactSearch;
        List<Object> latinIndices = findQueryInLatinIndices(inglessVerb, new ArrayList<>(), exactSearch,  new String[]{GlobalConstants.LANG_STR_EN},
                roomCentralDatabase, japaneseToolboxExtendedRoomDatabase, use_extended_db);

        for (Object indexLatin : latinIndices) {
            List<String> indexList = new ArrayList<>();
            if (indexLatin instanceof IndexRomaji )         indexList = Arrays.asList(((IndexRomaji) indexLatin).getWordIds().split(GlobalConstants.DB_ELEMENTS_DELIMITER));
            else if (indexLatin instanceof IndexEnglish )   indexList = Arrays.asList(((IndexEnglish) indexLatin).getWordIds().split(GlobalConstants.DB_ELEMENTS_DELIMITER));
            else if (indexLatin instanceof IndexFrench )    indexList = Arrays.asList(((IndexFrench) indexLatin).getWordIds().split(GlobalConstants.DB_ELEMENTS_DELIMITER));
            else if (indexLatin instanceof IndexSpanish )   indexList = Arrays.asList(((IndexSpanish) indexLatin).getWordIds().split(GlobalConstants.DB_ELEMENTS_DELIMITER));

            for (int j = 0; j < indexList.size(); j++) {
                newMatchingWordIdsFromIndex.add(Long.valueOf(indexList.get(j)));
            }
        }

        return newMatchingWordIdsFromIndex;
    }
    /**
     * Returns matching word ids before filtering, based on the latin an kanji indexes.
     * If the returned list of ids is too big, returns a list of exact matches only.
     * In any case, tries to add results where "ing" is removed from gerunds.
     * @param inputTextType
     * @param searchWord
     * @param searchWordNoSpaces
     * @param inglessVerb
     * @param roomCentralDatabase
     * @return
     */
    private static List<Long> getMatchingWordIds(int inputTextType, String searchWord, String searchWordNoSpaces, String inglessVerb, List<String> possibleInterpretations,
                                                 RoomCentralDatabase roomCentralDatabase,
                                                 RoomExtendedDatabase japaneseToolboxExtendedRoomDatabase,
                                                 String language, boolean use_extended_db) {

        List<Long> matchingWordIds = getMatchingWordIdsForOriginalInputQuery(false, inputTextType,
                searchWord, searchWordNoSpaces, possibleInterpretations, roomCentralDatabase, japaneseToolboxExtendedRoomDatabase, language, use_extended_db);

        //If the number of matching ids is larger than MAX_SQL_VARIABLES_FOR_QUERY, perform an exact search
        if (matchingWordIds.size() > GlobalConstants.MAX_SQL_VARIABLES_FOR_QUERY) {
            matchingWordIds = getMatchingWordIdsForOriginalInputQuery(true, inputTextType,
                    searchWord, searchWordNoSpaces, possibleInterpretations, roomCentralDatabase, japaneseToolboxExtendedRoomDatabase, language, use_extended_db);
        }

        //If the number of matching ids is still larger than MAX_SQL_VARIABLES_FOR_QUERY, limit the list length to MAX_SQL_VARIABLES_FOR_QUERY
        if (matchingWordIds.size() > GlobalConstants.MAX_SQL_VARIABLES_FOR_QUERY) {
            matchingWordIds = matchingWordIds.subList(0,GlobalConstants.MAX_SQL_VARIABLES_FOR_QUERY);
        }

        //Adding search results where the "ing" is removed from an "ing" verb in english
        if (language.equals(GlobalConstants.LANG_STR_EN) && !inglessVerb.equals(searchWord)
                && (inputTextType == GlobalConstants.TYPE_LATIN || inputTextType == GlobalConstants.TYPE_HIRAGANA
                || inputTextType == GlobalConstants.TYPE_KATAKANA || inputTextType == GlobalConstants.TYPE_NUMBER)) {

            List<Long> newMatchingWordIds = getMatchingWordIdsForQueryWithoutExtraIng(false,
                    matchingWordIds, inglessVerb, roomCentralDatabase, japaneseToolboxExtendedRoomDatabase, use_extended_db);

            if (matchingWordIds.size() + newMatchingWordIds.size() > GlobalConstants.MAX_SQL_VARIABLES_FOR_QUERY) {
                newMatchingWordIds = getMatchingWordIdsForQueryWithoutExtraIng(true,
                        matchingWordIds, inglessVerb, roomCentralDatabase, japaneseToolboxExtendedRoomDatabase, use_extended_db);
            }

            if (matchingWordIds.size() + newMatchingWordIds.size() <= GlobalConstants.MAX_SQL_VARIABLES_FOR_QUERY) {
                matchingWordIds.addAll(newMatchingWordIds);
            }
        }

        //Removing duplicates while keeping the list order (https://stackoverflow.com/questions/19511797/remove-duplicates-in-an-array-without-changing-order-of-elements)
        matchingWordIds = new ArrayList<>(new LinkedHashSet<>(matchingWordIds));

        return matchingWordIds;
    }
    /**
     * Gets the matching word ids from the latin/kanji indexes and filters them to return only words matching the searchWord/
     * The filter checks the keywords list for matches for word of length 4 and up, and handles shorter words differently.
     * @param searchWord
     * @param searchWordNoSpaces
     * @param inglessVerb
     * @param searchWordWithoutTo
     * @param queryIsVerbWithTo
     * @param inputTextType
     * @param matchingWordIds
     * @param roomCentralDatabase
     * @return
     */
    private static List<Long> addNormalMatchesToMatchesList(String searchWord, String searchWordNoSpaces, String inglessVerb, String searchWordWithoutTo, List<String> possibleInterpretations,
                                                            boolean queryIsVerbWithTo, int inputTextType, List<Long> matchingWordIds,
                                                            RoomCentralDatabase roomCentralDatabase,
                                                            RoomExtendedDatabase japaneseToolboxExtendedRoomDatabase,
                                                            String language, boolean use_extended_db) {

        //region Initializations
        List<long[]> MatchList = new ArrayList<>();
        String keywords;
        long[] current_match_values;
        boolean foundMatch;
        //endregion

        List<Long> matchingWordIdsFromIndex = getMatchingWordIds(inputTextType, searchWord, searchWordNoSpaces, inglessVerb, possibleInterpretations,
                roomCentralDatabase, japaneseToolboxExtendedRoomDatabase,
                language, use_extended_db);

        //region Limiting the database query if there are too many results (prevents long query times)
        boolean onlyRetrieveShortRomajiWords = false;
        if ((inputTextType == GlobalConstants.TYPE_LATIN || inputTextType == GlobalConstants.TYPE_HIRAGANA
                || inputTextType == GlobalConstants.TYPE_KATAKANA) && searchWord.length() < 3) {
            onlyRetrieveShortRomajiWords = true;
        }
        //endregion

        //region Filtering the matches
        List<Word> matchingWordList = use_extended_db? japaneseToolboxExtendedRoomDatabase.getWordListByWordIds(matchingWordIdsFromIndex)
                : roomCentralDatabase.getWordListByWordIds(matchingWordIdsFromIndex);
        String romaji;
        String altSpellings;
        boolean isExactMeaningWordsMatch;
        boolean isRomajiMatch;
        boolean isAltSpellingsMatch;
        int searchWordLength = searchWord.length();
        int romajiLength;
        List<String> keywordsList;
        for (Word word : matchingWordList) {

            foundMatch = false;
            isRomajiMatch = false;
            isAltSpellingsMatch = false;

            //region Handling short words
            if ((inputTextType == GlobalConstants.TYPE_LATIN || inputTextType == GlobalConstants.TYPE_HIRAGANA
                    || inputTextType == GlobalConstants.TYPE_KATAKANA) && onlyRetrieveShortRomajiWords) {

                //Checking if the word is an exact match to one of the words in the meanings
                isExactMeaningWordsMatch = getMeaningsContainingExactQueryMatch(searchWord, word, language);

                if (!isExactMeaningWordsMatch) {
                    //Checking if the romaji is a match
                    romaji = word.getRomaji();
                    romajiLength = romaji.length();
                    isRomajiMatch = romajiLength <= searchWordLength + 1 && romaji.contains(searchWord);

                }

                if (!isExactMeaningWordsMatch && !isRomajiMatch) {

                    //Checking if one of the elements of altSpellings is a match
                    altSpellings = word.getAltSpellings();
                    isAltSpellingsMatch = false;
                    for (String altSpelling : altSpellings.split(",")) {
                        if (altSpelling.trim().contains(searchWord)) {
                            isAltSpellingsMatch = true;
                            break;
                        }
                    }
                }

                //Setting foundMatch
                if (isExactMeaningWordsMatch || isRomajiMatch || isAltSpellingsMatch) {
                    foundMatch = true;
                }
                else continue;

            }
            //endregion

            //Otherwise, handling longer words
            if (!foundMatch) {

                keywordsList = new ArrayList<>();
                keywordsList.add(word.getRomaji());
                keywordsList.add(word.getKanji());
                keywordsList.add(word.getAltSpellings());
                keywordsList.add(word.getExtraKeywordsJAP());
                switch (language) {
                    case GlobalConstants.LANG_STR_EN: keywordsList.add(word.getExtraKeywordsEN()); break;
                    case GlobalConstants.LANG_STR_FR: keywordsList.add(word.getExtraKeywordsFR()); break;
                    case GlobalConstants.LANG_STR_ES: keywordsList.add(word.getExtraKeywordsES()); break;
                }
                keywordsList.add(getConcatenatedMeanings(word));
                keywords = TextUtils.join(", ", keywordsList).replace(" ","").toLowerCase();

                for (String interpretation : possibleInterpretations) {
                    if (keywords.contains(interpretation.replace(" ",""))) {
                        foundMatch = true;
                        break;
                    }
                }
                foundMatch |= keywords.contains(inglessVerb);
                foundMatch |= queryIsVerbWithTo && keywords.contains(searchWordWithoutTo);
            }

            if (foundMatch) {
                current_match_values = new long[2];
                current_match_values[0] = word.getWordId();
                //current_match_values[1] = (long) match_length;
                MatchList.add(current_match_values);
            }
        }

        for (int i=0;i<MatchList.size();i++) {
            matchingWordIds.add(MatchList.get(i)[0]);
        }
        //endregion

        return matchingWordIds;
    }
    private static List<Long> addNamesToMatchesList(String searchWord, int inputTextType, RoomNamesDatabase japaneseToolboxNamesRoomDatabase) {

        List<Long> matchingWordIdsNames = findQueryInNameIndices(searchWord, false, inputTextType, japaneseToolboxNamesRoomDatabase);

        //If the number of matching ids is larger than MAX_SQL_VARIABLES_FOR_QUERY, perform an exact search
        if (matchingWordIdsNames.size() > GlobalConstants.MAX_SQL_VARIABLES_FOR_QUERY) {
            matchingWordIdsNames = findQueryInNameIndices(searchWord, true, inputTextType, japaneseToolboxNamesRoomDatabase);
        }

        //If the number of matching ids is still larger than MAX_SQL_VARIABLES_FOR_QUERY, limit the list length to MAX_SQL_VARIABLES_FOR_QUERY
        if (matchingWordIdsNames.size() > GlobalConstants.MAX_SQL_VARIABLES_FOR_QUERY) {
            matchingWordIdsNames = matchingWordIdsNames.subList(0,GlobalConstants.MAX_SQL_VARIABLES_FOR_QUERY);
        }

        return matchingWordIdsNames;
    }
    private static boolean getMeaningsContainingExactQueryMatch(String searchWord, Word word, String language) {

        List<Word.Meaning> meanings = new ArrayList<>();
        switch (language) {
            case GlobalConstants.LANG_STR_EN:
                meanings = word.getMeaningsEN();
                break;
            case GlobalConstants.LANG_STR_FR:
                meanings = word.getMeaningsFR();
                break;
            case GlobalConstants.LANG_STR_ES:
                meanings = word.getMeaningsES();
                break;
        }

        StringBuilder builder = new StringBuilder();
        for (Word.Meaning meaning : meanings) {
            builder.append(" ");
            builder.append(meaning.getMeaning().replace(", ", " ").replace("(", " ").replace(")", " "));
        }
        String meaningsString = builder.toString();
        String[] meaningSet = meaningsString.split(" ");
        boolean isExactMeaningWordsMatch = false;
        for (String meaningSetElement : meaningSet) {
            if (meaningSetElement.equals(searchWord)) {
                isExactMeaningWordsMatch = true;
                break;
            }
        }
        return isExactMeaningWordsMatch;
    }
    private static String getConcatenatedMeanings(Word word) {
        List<String> meanings = new ArrayList<>();
        for (Word.Meaning meaning : word.getMeaningsEN()) {
            meanings.add(meaning.getMeaning());
        }
        for (Word.Meaning meaning : word.getMeaningsFR()) {
            meanings.add(meaning.getMeaning());
        }
        for (Word.Meaning meaning : word.getMeaningsES()) {
            meanings.add(meaning.getMeaning());
        }
        return TextUtils.join(", ", meanings);
    }
    private static List<Long> addConjugatedAdjectivesToMatchesList(String searchWord, int inputTextType, List<Long> matchingWordIds,
                                                                   RoomCentralDatabase roomCentralDatabase) {

        //Adding relevant adjectives to the list of matches if the input query is an adjective conjugation
        List<Long> matchingWordIdsFromIndex = getmatchingWordIdsFromIndexForAdjectiveSearch(searchWord, inputTextType,
                matchingWordIds, roomCentralDatabase, false);

        if (matchingWordIdsFromIndex.size() > GlobalConstants.MAX_SQL_VARIABLES_FOR_QUERY) {
            matchingWordIdsFromIndex = getmatchingWordIdsFromIndexForAdjectiveSearch(searchWord, inputTextType,
                    matchingWordIds, roomCentralDatabase, true);
        }

        if (matchingWordIdsFromIndex.size() > GlobalConstants.MAX_SQL_VARIABLES_FOR_QUERY) {
            Log.i(DEBUG_TAG, "WARNING: exceeded MAX_SQL_VARIABLES_FOR_QUERY when searching for "+ searchWord +" in adjectives search, but prevented crash.");
        }
        else {
            List<Word> matchingPotentialAdjectives = roomCentralDatabase.getWordListByWordIds(matchingWordIdsFromIndex);
            boolean isAlreadyInList;
            for (Word word : matchingPotentialAdjectives) {
                isAlreadyInList = false;

                List<String> typesList = new ArrayList<>();
                for (Word.Meaning meaning : word.getMeaningsEN()) {
                    typesList.add(meaning.getType());
                }
                typesList = Arrays.asList(TextUtils.join(GlobalConstants.DB_ELEMENTS_DELIMITER, typesList).split(GlobalConstants.DB_ELEMENTS_DELIMITER));
                if (typesList.contains("Ai") || typesList.contains("Ana")) {
                    for (long id : matchingWordIds) {
                        if (id == word.getWordId()) {
                            isAlreadyInList = true;
                            break;
                        }
                    }
                    if (!isAlreadyInList) matchingWordIds.add(word.getWordId());
                }
            }
        }

        return matchingWordIds;
    }
    private static List<Long> addCountersToMatchesList(String searchWord, int inputTextType, List<Long> matchingWordIds,
                                                       RoomCentralDatabase roomCentralDatabase) {

        if (inputTextType == GlobalConstants.TYPE_KANJI && searchWord.length()==2
                && "何一二三四五六七八九十".contains(searchWord.substring(0,1)) ) {

            List<IndexKanji> kanjiIndicesForCounter = findQueryInKanjiIndex(searchWord.substring(1,2), true, roomCentralDatabase, null, false);

            if (kanjiIndicesForCounter.size()==0) return matchingWordIds;

            List<String> searchResultIndexesArray = new ArrayList<>();
            for (IndexKanji indexKanji : kanjiIndicesForCounter) {
                searchResultIndexesArray.add(indexKanji.getWordIds());
            }

            List<String> indexList;
            List<Long> matchingWordIdsFromIndex = new ArrayList<>();
            for (String searchResultIndexes : searchResultIndexesArray) {
                indexList = Arrays.asList(searchResultIndexes.split(GlobalConstants.DB_ELEMENTS_DELIMITER));
                for (int j = 0; j < indexList.size(); j++) {
                    matchingWordIdsFromIndex.add(Long.valueOf(indexList.get(j)));
                }
            }

            if (matchingWordIdsFromIndex.size() > GlobalConstants.MAX_SQL_VARIABLES_FOR_QUERY) {
                Log.i(DEBUG_TAG, "WARNING: exceeded MAX_SQL_VARIABLES_FOR_QUERY when searching for "+ searchWord +" in counter search, but prevented crash.");
            }
            else {
                List<Word> matchingPotentialCounters = roomCentralDatabase.getWordListByWordIds(matchingWordIdsFromIndex);
                boolean isAlreadyInList;
                for (Word word : matchingPotentialCounters) {
                    isAlreadyInList = false;
                    List<String> typesList = new ArrayList<>();
                    for (Word.Meaning meaning : word.getMeaningsEN()) {
                        typesList.add(meaning.getType());
                    }
                    typesList = Arrays.asList(TextUtils.join(GlobalConstants.DB_ELEMENTS_DELIMITER, typesList).split(GlobalConstants.DB_ELEMENTS_DELIMITER));
                    if (typesList.contains("C")) {
                        for (long id : matchingWordIds) {
                            if (id == word.getWordId()) {
                                isAlreadyInList = true;
                                break;
                            }
                        }
                        if (!isAlreadyInList) matchingWordIds.add(word.getWordId());
                    }
                }
            }
        }

        return matchingWordIds;
    }
    private static List<Long> getmatchingWordIdsFromIndexForAdjectiveSearch(String searchWord, int inputTextType, List<Long> matchingWordIds,
                                                                     RoomCentralDatabase roomCentralDatabase,
                                                                     boolean forceExactSearch) {

        List<Long> matchingWordIdsFromIndex = new ArrayList<>();
        String input_word = Utilities.removeNonSpaceSpecialCharacters(searchWord);
        String adjectiveConjugation;
        String baseAdjective = "";
        boolean isPotentialAdjective = false;
        List<String> searchResultIndexesArray = new ArrayList<>();

        if (inputTextType == GlobalConstants.TYPE_LATIN || inputTextType == GlobalConstants.TYPE_HIRAGANA || inputTextType == GlobalConstants.TYPE_KATAKANA) {

            input_word = ConvertFragment.getLatinHiraganaKatakana(input_word).get(GlobalConstants.TYPE_LATIN);

            if (input_word.length()>9) {
                adjectiveConjugation = input_word.substring(input_word.length()-9);
                baseAdjective = input_word.substring(0, input_word.length()-9) + "i";
                if (adjectiveConjugation.equals("kunakatta")) isPotentialAdjective = true;
            }
            if (!isPotentialAdjective && input_word.length()>6) {
                adjectiveConjugation = input_word.substring(input_word.length()-6);
                baseAdjective = input_word.substring(0, input_word.length()-6) + "i";
                if (adjectiveConjugation.equals("kereba")) isPotentialAdjective = true;
            }
            if (!isPotentialAdjective && input_word.length()>5) {
                adjectiveConjugation = input_word.substring(input_word.length()-5);
                baseAdjective = input_word.substring(0, input_word.length()-5) + "i";
                if (adjectiveConjugation.equals("kunai")
                        || adjectiveConjugation.equals("katta")
                        || adjectiveConjugation.equals("karou")) isPotentialAdjective = true;
            }
            if (!isPotentialAdjective && input_word.length()>4) {
                adjectiveConjugation = input_word.substring(input_word.length()-4);
                baseAdjective = input_word.substring(0, input_word.length()-4) + "i";
                if (adjectiveConjugation.equals("kute")) isPotentialAdjective = true;
            }
            if (!isPotentialAdjective && input_word.length()>2) {
                adjectiveConjugation = input_word.substring(input_word.length()-2);
                if (adjectiveConjugation.equals("mi") || adjectiveConjugation.equals("ku")) {
                    isPotentialAdjective = true;
                    baseAdjective = input_word.substring(0, input_word.length()-2) + "i";
                }
                else if (adjectiveConjugation.equals("ni") || adjectiveConjugation.equals("na") || adjectiveConjugation.equals("de")) {
                    isPotentialAdjective = true;
                    baseAdjective = input_word.substring(0, input_word.length()-2);
                }
            }

            if (!isPotentialAdjective) return matchingWordIds;

            boolean exactSearch = baseAdjective.length() < 3 || forceExactSearch;

            List<String> possibleInterpretations = ConvertFragment.getWaapuroRomanizationsFromLatinText(baseAdjective);
            List<Object> latinIndicesForAdjective = findQueryInLatinIndices(baseAdjective, possibleInterpretations, exactSearch, new String[]{"romaji"}, roomCentralDatabase, null, false);

            if (latinIndicesForAdjective.size()==0) return matchingWordIds;

            for (Object indexLatin : latinIndicesForAdjective) {
                if (indexLatin instanceof IndexRomaji )         searchResultIndexesArray.add(((IndexRomaji) indexLatin).getWordIds());
                else if (indexLatin instanceof IndexEnglish )   searchResultIndexesArray.add(((IndexEnglish) indexLatin).getWordIds());
                else if (indexLatin instanceof IndexFrench )    searchResultIndexesArray.add(((IndexFrench) indexLatin).getWordIds());
                else if (indexLatin instanceof IndexSpanish )   searchResultIndexesArray.add(((IndexSpanish) indexLatin).getWordIds());
            }

        } else if (inputTextType == GlobalConstants.TYPE_KANJI) {

            if (input_word.length()>5) {
                adjectiveConjugation = input_word.substring(input_word.length()-5);
                baseAdjective = input_word.substring(0, input_word.length()-5) + "い";
                if (adjectiveConjugation.equals("くなかった")) isPotentialAdjective = true;
            }
            if (!isPotentialAdjective && input_word.length()>3) {
                adjectiveConjugation = input_word.substring(input_word.length()-3);
                baseAdjective = input_word.substring(0, input_word.length()-3) + "い";
                if (adjectiveConjugation.equals("くない")
                        || adjectiveConjugation.equals("ければ")
                        || adjectiveConjugation.equals("かった")
                        || adjectiveConjugation.equals("かろう")) isPotentialAdjective = true;
            }
            if (!isPotentialAdjective && input_word.length()>2) {
                adjectiveConjugation = input_word.substring(input_word.length()-2);
                baseAdjective = input_word.substring(0, input_word.length()-2) + "い";
                if (adjectiveConjugation.equals("くて")) isPotentialAdjective = true;
            }
            if (!isPotentialAdjective && input_word.length()>1) {
                adjectiveConjugation = input_word.substring(input_word.length()-1);
                if (adjectiveConjugation.equals("み") || adjectiveConjugation.equals("く")) {
                    isPotentialAdjective = true;
                    baseAdjective = input_word.substring(0, input_word.length()-1) + "";
                }
                else if (adjectiveConjugation.equals("に") || adjectiveConjugation.equals("な") || adjectiveConjugation.equals("で")) {
                    isPotentialAdjective = true;
                    baseAdjective = input_word.substring(0, input_word.length()-1);
                }
            }

            if (!isPotentialAdjective) return matchingWordIds;

            List<IndexKanji> kanjiIndicesForAdjective = findQueryInKanjiIndex(baseAdjective, forceExactSearch, roomCentralDatabase, null, false);

            if (kanjiIndicesForAdjective.size()==0) return matchingWordIds;

            for (IndexKanji indexKanji : kanjiIndicesForAdjective) {
                searchResultIndexesArray.add(indexKanji.getWordIds());
            }

        } else {
            return matchingWordIds;
        }

        List<String> indexList;
        for (String searchResultIndexes : searchResultIndexesArray) {
            indexList = Arrays.asList(searchResultIndexes.split(GlobalConstants.DB_ELEMENTS_DELIMITER));
            for (int j = 0; j < indexList.size(); j++) {
                matchingWordIdsFromIndex.add(Long.valueOf(indexList.get(j)));
            }
        }

        return matchingWordIdsFromIndex;
    }
    public static String replaceInvalidKanjisWithValidOnes(String input, List<String[]> mSimilarsDatabase) {
        StringBuilder output = new StringBuilder();
        char currentChar;
        boolean found;
        for (int i=0; i<input.length(); i++) {
            currentChar = input.charAt(i);
            found = false;
            for (int j = 0; j < mSimilarsDatabase.size(); j++) {
                if (mSimilarsDatabase.get(j).length > 0 && mSimilarsDatabase.get(j)[0].charAt(0) == currentChar) {
                    output.append(mSimilarsDatabase.get(j)[1].charAt(0));
                    found = true;
                    break;
                }
            }
            if (!found) output.append(currentChar);
        }
        return output.toString();
    }
    public static Boolean isOfTypeIngIng(String verb) {
        boolean answer = false;
        if (	verb.equals("accinging") || verb.equals("astringing") || verb.equals("befringing") || verb.equals("besinging") ||
                verb.equals("binging") || verb.equals("boinging") || verb.equals("bowstringing") || verb.equals("bringing") ||
                verb.equals("clinging") || verb.equals("constringing") || verb.equals("cringing") || verb.equals("dinging") ||
                verb.equals("enringing") || verb.equals("flinging") || verb.equals("folksinging") || verb.equals("fringing") ||
                verb.equals("gunslinging") || verb.equals("hamstringing") || verb.equals("handwringing") || verb.equals("hinging") ||
                verb.equals("impinging") || verb.equals("inbringing") || verb.equals("infringing") || verb.equals("kinging") ||
                verb.equals("minging") || verb.equals("mudslinging") || verb.equals("outringing") || verb.equals("outsinging") ||
                verb.equals("outspringing") || verb.equals("outswinging") || verb.equals("outwinging") || verb.equals("overswinging") ||
                verb.equals("overwinging") || verb.equals("perstringing") || verb.equals("pinging") || verb.equals("refringing") ||
                verb.equals("rehinging") || verb.equals("respringing") || verb.equals("restringing") || verb.equals("ringing") ||
                verb.equals("singing") || verb.equals("slinging") || verb.equals("springing") || verb.equals("stinging") ||
                verb.equals("stringing") || verb.equals("swinging") || verb.equals("syringing") || verb.equals("twinging") ||
                verb.equals("unhinging") || verb.equals("unkinging") || verb.equals("unslinging") || verb.equals("unstringing") ||
                verb.equals("upbringing") || verb.equals("upflinging") || verb.equals("upspringing") || verb.equals("upswinging") ||
                verb.equals("whinging") || verb.equals("winging") || verb.equals("wringing") || verb.equals("zinging") ) {
            answer = true;
        }
        return answer;
    }
    private static String removeApostrophes(String sentence) {
        String current_char;
        StringBuilder concatenated_sentence = new StringBuilder();
        for (int index=0; index<sentence.length(); index++) {
            current_char = Character.toString(sentence.charAt(index));
            if (!( current_char.equals("'")) ) {
                concatenated_sentence.append(current_char);
            }
        }
        return concatenated_sentence.toString();
    }
    private static List<Long> findQueryInNameIndices(String concatenated_word, boolean exactSearch, int inputTextType,
                                                        RoomNamesDatabase japaneseToolboxNamesRoomDatabase) {

        List<String> matchingIndices = new ArrayList<>();
        if (exactSearch) {
            //Preventing the index search from returning too many results and crashing the app

            if (inputTextType == GlobalConstants.TYPE_KANJI) {
                IndexKanji indexKanji = japaneseToolboxNamesRoomDatabase.getKanjiIndexForExactWord(concatenated_word);
                if (indexKanji != null) {
                    matchingIndices.addAll(Arrays.asList(indexKanji.getWordIds().split(GlobalConstants.DB_ELEMENTS_DELIMITER)));
                }
            } else {
                if (inputTextType == GlobalConstants.TYPE_HIRAGANA || inputTextType == GlobalConstants.TYPE_KATAKANA) {
                    concatenated_word = ConvertFragment.getLatinHiraganaKatakana(concatenated_word).get(GlobalConstants.TYPE_LATIN);
                }
                IndexRomaji indexRomaji = japaneseToolboxNamesRoomDatabase.getRomajiIndexForExactWord(concatenated_word);
                if (indexRomaji != null) {
                    matchingIndices.addAll(Arrays.asList(indexRomaji.getWordIds().split(GlobalConstants.DB_ELEMENTS_DELIMITER)));
                }
            }

        } else {
            if (inputTextType == GlobalConstants.TYPE_KANJI) {
                List<IndexKanji> indexesKanji = japaneseToolboxNamesRoomDatabase.getKanjiIndexesListForStartingWord(concatenated_word);
                if (indexesKanji != null && indexesKanji.size()>0) {
                    for (IndexKanji indexKanji : indexesKanji) {
                        matchingIndices.addAll(Arrays.asList(indexKanji.getWordIds().split(GlobalConstants.DB_ELEMENTS_DELIMITER)));
                    }
                }
            } else {
                if (inputTextType == GlobalConstants.TYPE_HIRAGANA || inputTextType == GlobalConstants.TYPE_KATAKANA) {
                    concatenated_word = ConvertFragment.getLatinHiraganaKatakana(concatenated_word).get(GlobalConstants.TYPE_LATIN);
                }
                List<IndexRomaji> indexesRomaji = japaneseToolboxNamesRoomDatabase.getRomajiIndexesListForStartingWord(concatenated_word);
                if (indexesRomaji != null && indexesRomaji.size()>0) {
                    for (IndexRomaji indexRomaji : indexesRomaji) {
                        matchingIndices.addAll(Arrays.asList(indexRomaji.getWordIds().split(GlobalConstants.DB_ELEMENTS_DELIMITER)));
                    }
                }
            }

        }
        matchingIndices = removeDuplicatesFromList(matchingIndices);
        List<Long> finalIndexList = new ArrayList<>();
        for (String element : matchingIndices) {
            finalIndexList.add(Long.parseLong(element));
        }
        return finalIndexList;
    }
    private static List<Object> findQueryInLatinIndices(String concatenated_word, List<String> possibleInterpretations, boolean exactSearch, String[] searchType,
                                                        RoomCentralDatabase roomCentralDatabase,
                                                        RoomExtendedDatabase japaneseToolboxExtendedRoomDatabase, boolean use_extended_db) {

        //Exact search Prevents the index search from returning too many results and crashing the app

        List<Object> matchingIndices = new ArrayList<>();
        if (exactSearch) {

            if (Arrays.asList(searchType).contains("romaji")) {
                List<IndexRomaji> indexesRomaji = use_extended_db ? japaneseToolboxExtendedRoomDatabase.getRomajiIndexForExactWordsList(possibleInterpretations)
                        : roomCentralDatabase.getRomajiIndexForExactWordsList(possibleInterpretations);
                if (indexesRomaji != null && indexesRomaji.size() > 0) matchingIndices.addAll(indexesRomaji);
            }

            if (Arrays.asList(searchType).contains(GlobalConstants.LANG_STR_EN)) {
                IndexEnglish indexEnglish = use_extended_db? japaneseToolboxExtendedRoomDatabase.getEnglishIndexForExactWord(concatenated_word)
                        : roomCentralDatabase.getEnglishIndexForExactWord(concatenated_word);
                if (indexEnglish!=null) matchingIndices.add(indexEnglish); //Only add the index if the word was found in the index
            }
            else if (Arrays.asList(searchType).contains(GlobalConstants.LANG_STR_FR)) {
                IndexFrench indexFrench = use_extended_db? japaneseToolboxExtendedRoomDatabase.getFrenchIndexForExactWord(concatenated_word)
                        : roomCentralDatabase.getFrenchIndexForExactWord(concatenated_word);
                if (indexFrench != null) matchingIndices.add(indexFrench); //Only add the index if the word was found in the index
                IndexEnglish indexEnglish = use_extended_db? japaneseToolboxExtendedRoomDatabase.getEnglishIndexForExactWord(concatenated_word)
                        : roomCentralDatabase.getEnglishIndexForExactWord(concatenated_word);
                if (indexEnglish!=null) matchingIndices.add(indexEnglish); //Only add the index if the word was found in the index
            }
            else if (Arrays.asList(searchType).contains(GlobalConstants.LANG_STR_ES)) {
                IndexSpanish indexSpanish = use_extended_db? japaneseToolboxExtendedRoomDatabase.getSpanishIndexForExactWord(concatenated_word)
                        : roomCentralDatabase.getSpanishIndexForExactWord(concatenated_word);
                if (indexSpanish != null) matchingIndices.add(indexSpanish); //Only add the index if the word was found in the index
                IndexEnglish indexEnglish = use_extended_db? japaneseToolboxExtendedRoomDatabase.getEnglishIndexForExactWord(concatenated_word)
                        : roomCentralDatabase.getEnglishIndexForExactWord(concatenated_word);
                if (indexEnglish!=null) matchingIndices.add(indexEnglish); //Only add the index if the word was found in the index
            }

        } else {
            if (Arrays.asList(searchType).contains("romaji")) {
                List<IndexRomaji> indexesRomaji = use_extended_db ? japaneseToolboxExtendedRoomDatabase.getRomajiIndexesListForStartingWordsList(possibleInterpretations)
                        : roomCentralDatabase.getRomajiIndexesListForStartingWordsList(possibleInterpretations);
                if (indexesRomaji != null && indexesRomaji.size() > 0) matchingIndices.addAll(indexesRomaji);
            }

            if (Arrays.asList(searchType).contains(GlobalConstants.LANG_STR_EN)) {
                List<IndexEnglish> indexesEnglish = use_extended_db? japaneseToolboxExtendedRoomDatabase.getEnglishIndexesListForStartingWord(concatenated_word)
                        : roomCentralDatabase.getEnglishIndexesListForStartingWord(concatenated_word);
                if (indexesEnglish != null && indexesEnglish.size()>0) matchingIndices.addAll(indexesEnglish);
            }
            else if (Arrays.asList(searchType).contains(GlobalConstants.LANG_STR_FR)) {
                List<IndexFrench> indexesFrench = use_extended_db? japaneseToolboxExtendedRoomDatabase.getFrenchIndexesListForStartingWord(concatenated_word)
                        : roomCentralDatabase.getFrenchIndexesListForStartingWord(concatenated_word);
                if (indexesFrench != null && indexesFrench.size()>0) matchingIndices.addAll(indexesFrench);
                List<IndexEnglish> indexesEnglish = use_extended_db? japaneseToolboxExtendedRoomDatabase.getEnglishIndexesListForStartingWord(concatenated_word)
                        : roomCentralDatabase.getEnglishIndexesListForStartingWord(concatenated_word);
                if (indexesEnglish != null && indexesEnglish.size()>0) matchingIndices.addAll(indexesEnglish);
            }
            else if (Arrays.asList(searchType).contains(GlobalConstants.LANG_STR_ES)) {
                List<IndexSpanish> indexesSpanish = use_extended_db? japaneseToolboxExtendedRoomDatabase.getSpanishIndexesListForStartingWord(concatenated_word)
                        : roomCentralDatabase.getSpanishIndexesListForStartingWord(concatenated_word);
                if (indexesSpanish != null && indexesSpanish.size()>0) matchingIndices.addAll(indexesSpanish);
                List<IndexEnglish> indexesEnglish = use_extended_db? japaneseToolboxExtendedRoomDatabase.getEnglishIndexesListForStartingWord(concatenated_word)
                        : roomCentralDatabase.getEnglishIndexesListForStartingWord(concatenated_word);
                if (indexesEnglish != null && indexesEnglish.size()>0) matchingIndices.addAll(indexesEnglish);
            }
        }
        return matchingIndices;
    }
    private static List<IndexKanji> findQueryInKanjiIndex(String concatenated_word, boolean exactSearch,
                                                          RoomCentralDatabase roomCentralDatabase,
                                                          RoomExtendedDatabase japaneseToolboxExtendedRoomDatabase, boolean use_extended_db) {

        // Prepare the input word to be used in the following algorithm: the word is converted to its hex utf-8 value as a string, in fractional form
        //String prepared_word = convertToUTF8Index(concatenated_word);
        String prepared_word = concatenated_word;

        List<IndexKanji> matchingIndexKanjis;
        if (exactSearch) {
            //Preventing the index search from returning too many results and crashing the app
            matchingIndexKanjis = new ArrayList<>();
            IndexKanji index = use_extended_db? japaneseToolboxExtendedRoomDatabase.getKanjiIndexForExactWord(prepared_word)
                    : roomCentralDatabase.getKanjiIndexForExactWord(prepared_word);
            if (index!=null) matchingIndexKanjis.add(index); //Only add the index if the word was found in the index
            return matchingIndexKanjis;
        } else {
            matchingIndexKanjis = use_extended_db? japaneseToolboxExtendedRoomDatabase.getKanjiIndexesListForStartingWord(prepared_word)
                    : roomCentralDatabase.getKanjiIndexesListForStartingWord(prepared_word);
            return matchingIndexKanjis;
        }
    }
    public static String getRomajiNoSpacesForSpecialPartsOfSpeech(String romaji) {
        return romaji.replace(" ni", "ni")
                .replace(" de", "de")
                .replace(" wo", "wo")
                .replace(" to", "to")
                .replace(" na", "na");
    }
    public static boolean wordsAreEquivalent(Word wordA, Word wordB) {
        return wordA.getRomaji().trim().equals(wordB.getRomaji().trim()) && wordA.getKanji().trim().equals(wordB.getKanji().trim());
    }
    public static boolean wordsAreSimilar(Word wordA, String wordB) {
        return wordA.getRomaji().trim().equals(wordB) || wordA.getKanji().trim().equals(wordB);
    }


    //Preference utilities
    public static boolean getPreferenceShowNames(Activity activity) {
        boolean state = false;
        if (activity!=null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
            state = sharedPreferences.getBoolean(activity.getString(R.string.pref_complete_local_with_names_search_key),
                    activity.getResources().getBoolean(R.bool.pref_complete_local_with_names_search_default));
        }
        return state;
    }
    public static boolean getPreferenceShowOnlineResults(Activity activity) {
        boolean state = false;
        if (activity!=null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
            state = sharedPreferences.getBoolean(activity.getString(R.string.pref_complete_local_with_online_search_key),
                    activity.getResources().getBoolean(R.bool.pref_complete_local_with_online_search_default));
        }
        return state;
    }
    public static boolean getPreferenceWaitForOnlineResults(Activity activity) {
        boolean state = false;
        if (activity!=null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
            state = sharedPreferences.getBoolean(activity.getString(R.string.pref_wait_for_online_results_key),
                    activity.getResources().getBoolean(R.bool.pref_wait_for_online_results_default));
        }
        return state;
    }
    public static boolean getPreferenceShowConjResults(Activity activity) {
        boolean state = false;
        if (activity!=null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
            state = sharedPreferences.getBoolean(activity.getString(R.string.pref_complete_with_conj_search_key),
                    activity.getResources().getBoolean(R.bool.pref_complete_with_conj_search_default));
        }
        return state;
    }
    public static boolean getPreferenceWaitForConjResults(Activity activity) {
        boolean state = false;
        if (activity!=null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
            state = sharedPreferences.getBoolean(activity.getString(R.string.pref_wait_for_conj_results_key),
                    activity.getResources().getBoolean(R.bool.pref_wait_for_conj_results_default));
        }
        return state;
    }
    public static boolean getPreferenceShowSources(Activity activity) {
        boolean state = false;
        if (activity!=null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
            state = sharedPreferences.getBoolean(activity.getString(R.string.pref_show_sources_key),
                    activity.getResources().getBoolean(R.bool.pref_show_sources_default));
        }
        return state;
    }
    public static Boolean getPreferenceShowInfoBoxesOnSearch(Activity activity) {
        boolean showInfoBoxesOnSearch = false;
        if (activity!=null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
            showInfoBoxesOnSearch = sharedPreferences.getBoolean(activity.getString(R.string.pref_show_info_boxes_on_search_key),
                    activity.getResources().getBoolean(R.bool.pref_show_info_boxes_on_search_default));
        }
        return showInfoBoxesOnSearch;
    }
    public static Boolean getPreferenceUseJapaneseFont(Activity activity) {
        boolean state = false;
        if (activity!=null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
            state = sharedPreferences.getBoolean(activity.getString(R.string.pref_use_japanese_font_key),
                    activity.getResources().getBoolean(R.bool.pref_use_japanese_font_default));
        }
        return state;
    }
    public static Boolean getPreferenceShowDecompKanjiStructureInfo(Activity activity) {
        boolean showDecompStructureInfo = false;
        if (activity!=null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
            showDecompStructureInfo = sharedPreferences.getBoolean(activity.getString(R.string.pref_show_decomp_structure_info_key),
                    activity.getResources().getBoolean(R.bool.pref_show_decomp_structure_info_default));
        }
        return showDecompStructureInfo;
    }
    public static int getPreferenceQueryHistorySize(Context context) {
        if (context==null) return 0;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int queryHistorySize = Integer.parseInt(context.getResources().getString(R.string.pref_query_history_size_default_value));
        try {
            String queryHist = sharedPreferences.getString(context.getResources().getString(R.string.pref_query_history_size_key), context.getResources().getString(R.string.pref_query_history_size_default_value));
            queryHistorySize = Integer.parseInt(queryHist);
        } catch (Exception e) {
            queryHistorySize = Integer.parseInt(context.getResources().getString(R.string.pref_query_history_size_default_value));
        } finally {
            queryHistorySize = truncateIntToRange(queryHistorySize,
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
            editor.putBoolean(context.getString(R.string.pref_complete_with_names_key), flag);
            editor.apply();
        }
    }
    public static boolean getAppPreferenceCompleteWithNames(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
        return sharedPref.getBoolean(context.getString(R.string.pref_complete_with_names_key), true);
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
    public static String getAppPreferenceColorTheme(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.app_preferences), Context.MODE_PRIVATE);
        return sharedPref.getString(context.getString(R.string.pref_app_theme_color), context.getString(R.string.pref_theme_color_value_lightredblack));
    }
    public static boolean changeThemeColor(Activity activity) {

        String themeColor = Utilities.getAppPreferenceColorTheme(activity);
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
    public static int getResColorValue(Context context, int res_value){
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(res_value, typedValue, true);
        @ColorInt int color = typedValue.data;
        return color;
    }

    //Conjugator Module utilities
    public static List<ConjugationTitle> getConjugationTitles(List<String[]> verbLatinConjDatabase, Context context) {

        String[] titlesRow = verbLatinConjDatabase.get(0);
        String[] subtitlesRow = verbLatinConjDatabase.get(1);
        String[] endingsRow = verbLatinConjDatabase.get(2);
        int sheetLength = titlesRow.length;
        List<ConjugationTitle> conjugationTitles = new ArrayList<>();
        List<ConjugationTitle.Subtitle> subtitles = new ArrayList<>();
        ConjugationTitle conjugationTitle = new ConjugationTitle();

        for (int col = 0; col < sheetLength; col++) {

            if (col == 0) {
                int titleRef = GlobalConstants.VERB_CONJUGATION_TITLES.get(titlesRow[col]);
                conjugationTitle.setTitle(context.getString(titleRef));
                conjugationTitle.setTitleIndex(col);

                ConjugationTitle.Subtitle subtitle = new ConjugationTitle.Subtitle();
                int subtitleRef = GlobalConstants.VERB_CONJUGATION_TITLES.get(subtitlesRow[col]);
                subtitle.setSubtitle(context.getString(subtitleRef));
                subtitle.setSubtitleIndex(col);
                subtitles.add(subtitle);
            }
            else if (col == sheetLength -1) {
                conjugationTitle.setSubtitles(subtitles);
                conjugationTitles.add(conjugationTitle);
            }
            else {
                if (!titlesRow[col].equals("")) {

                    conjugationTitle.setSubtitles(subtitles);
                    conjugationTitles.add(conjugationTitle);

                    conjugationTitle = new ConjugationTitle();
                    subtitles = new ArrayList<>();

                    int titleRef = GlobalConstants.VERB_CONJUGATION_TITLES.get(titlesRow[col]);
                    conjugationTitle.setTitle(context.getString(titleRef));
                    conjugationTitle.setTitleIndex(col);

                }

                ConjugationTitle.Subtitle subtitle = new ConjugationTitle.Subtitle();
                int subtitleRef = GlobalConstants.VERB_CONJUGATION_TITLES.get(subtitlesRow[col]);
                subtitle.setSubtitle(context.getString(subtitleRef));
                subtitle.setEnding((col <= GlobalConstants.COLUMN_VERB_MASUSTEM)? "" : endingsRow[col]);
                subtitle.setSubtitleIndex(col);
                subtitles.add(subtitle);
            }
        }

        return conjugationTitles;
    }
}
