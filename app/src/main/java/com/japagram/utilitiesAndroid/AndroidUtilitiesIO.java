package com.japagram.utilitiesAndroid;

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
import com.japagram.data.KanjiComponent;
import com.japagram.data.KanjiComponentDao;
import com.japagram.data.Word;
import com.japagram.data.WordDao;
import com.japagram.utilitiesCrossPlatform.Globals;
import com.japagram.utilitiesPlatformOverridable.OvUtilsGeneral;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public class AndroidUtilitiesIO {
    //Constants
    public static final int NUM_COLUMNS_IN_WORDS_CSV_SHEETS = 16;
    public static final int NUM_COLUMNS_IN_MEANINGS_CSV_SHEETS = 8;
    public static final int NUM_COLUMNS_IN_EXPL_CSV_SHEETS = 4;
    public static final int NUM_COLUMNS_IN_EXAMPLES_CSV_SHEETS = 6;

    public static void hideSoftKeyboard(@NotNull Activity activity) {
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
    public static Boolean checkIfFileExistsInSpecificFolder(@NotNull File dir, String filename) {

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

    public static int convertPxToDpi(int pixels, @NotNull Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(pixels / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static void restartApplication(@NotNull Activity activity) {

        Intent intent = activity.getPackageManager().getLaunchIntentForPackage(activity.getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            activity.finish();
        }
    }

    @NonNull
    public static Resources getLocalizedResources(@NotNull Context context, Locale desiredLocale) {
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

    public static Bitmap adjustImageAngleAndScale(@NotNull Bitmap source, float angle, double scaleFactor) {

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

    //OCR utilities
    public static int loadOCRImageContrastFromSharedPreferences(SharedPreferences sharedPreferences, @NotNull Context context) {
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

    public static int loadOCRImageSaturationFromSharedPreferences(SharedPreferences sharedPreferences, @NotNull Context context) {
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

    public static int loadOCRImageBrightnessFromSharedPreferences(SharedPreferences sharedPreferences, @NotNull Context context) {
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

    public static float convertContrastProgressToValue(float contrastBarValue, @NotNull Context context) {
        return contrastBarValue
                / ((float) Integer.parseInt(context.getString(R.string.pref_OCR_image_contrast_range)))
                * ((float) Integer.parseInt(context.getString(R.string.pref_OCR_image_contrast_max_value)));
    }

    public static float convertSaturationProgressToValue(float saturationBarValue, @NotNull Context context) {
        return saturationBarValue
                / ((float) Integer.parseInt(context.getString(R.string.pref_OCR_image_saturation_range)))
                * ((float) Integer.parseInt(context.getString(R.string.pref_OCR_image_saturation_multipliers)));
    }

    public static float convertSaturationProgressToValueOLD(float saturationBarValue, @NotNull Context context) {
        return saturationBarValue
                / ((float) Integer.parseInt(context.getString(R.string.pref_OCR_image_saturation_range)))
                * ((float) Integer.parseInt(context.getString(R.string.pref_OCR_image_saturation_multipliers)));
    }

    @Contract(pure = true)
    public static int convertBrightnessProgressToValue(int brightnessBarValue, Context context) {
        return brightnessBarValue - 256;
    }

    public static int convertContrastValueToProgress(float contrastValue, @NotNull Context context) {
        float contrastBarValue = contrastValue
                * ((float) Integer.parseInt(context.getString(R.string.pref_OCR_image_contrast_range)))
                / ((float) Integer.parseInt(context.getString(R.string.pref_OCR_image_contrast_max_value)));
        return (int) contrastBarValue;
    }

    public static int convertSaturationValueToProgress(float saturationValue, @NotNull Context context) {
        float saturationBarValue = saturationValue
                * ((float) Integer.parseInt(context.getString(R.string.pref_OCR_image_saturation_range)))
                / ((float) Integer.parseInt(context.getString(R.string.pref_OCR_image_saturation_multipliers)));
        return (int) saturationBarValue;
    }

    @Contract(pure = true)
    public static int convertBrightnessValueToProgress(int brightnessValue, Context context) {
        return brightnessValue + 256;
    }

    //Internet Connectivity utilities
    public static boolean internetIsAvailableCheck(@NotNull Context context) {
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

    //IO utilities
    @NotNull
    public static List<String> readSingleColumnFile(String filename, Context context) {

        List<String> mySheet = new ArrayList<>();

        BufferedReader fileReader = null;

        try {
            String line;
            fileReader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename)));

            while ((line = fileReader.readLine()) != null) {
                mySheet.add(line);
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

    @NotNull
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

    @NotNull
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

    @Contract("null -> false")
    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children == null) return false;
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
        OvUtilsGeneral.printLog("Diagnosis Time", "Available heap size: " + availHeapSizeInMB);
        return availHeapSizeInMB;
    }

    @Contract(pure = true)
    public static float truncateFloatToRange(float value, float min, float max) {
        if (value < min) value = min;
        else if (value > max) value = max;
        return value;
    }

    @Contract(pure = true)
    public static int truncateIntToRange(int value, int min, int max) {
        if (value < min) value = min;
        else if (value > max) value = max;
        return value;
    }

    public static void makeDelay(int milliseconds) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
            }
        }, milliseconds);
    }

    public static @NotNull List<List<String>> readCSVFileAsLineBlocks(String filename, int blockSize, @NotNull Context context) {

        List<List<String>> lineBlocks = new ArrayList<>();
        List<String> linesTemp = new ArrayList<>();
        try {
            String lineTemp;
            BufferedReader fileReader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename)));
            fileReader.readLine(); //Discarding the first line of the file (titles)
            int lineNum = 0;
            while ((lineTemp = fileReader.readLine()) != null) {
                linesTemp.add(lineTemp);
                if (lineNum % blockSize == 0) {
                    lineBlocks.add(linesTemp);
                    linesTemp = new ArrayList<>();
                }
                lineNum++;
            }
            lineBlocks.add(linesTemp);
            return lineBlocks;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void insertWordBlock(@NotNull List<String> lineBlock, Context context, Object dao, float increment, int blockSizeForDisplay, String database) {
        String[] tokens;
        float currentProgress = AndroidUtilitiesPrefs.getProgressValueForDbInstallation(context, database);
        List<Word> elements = new ArrayList<>();
        boolean extendedDb = database.equals(Globals.EXTENDED_DB);
        int lineNum = 0;
        for (String line : lineBlock) {
            tokens = line.split("\\|", -1);
            if (tokens.length > 0) {
                if (tokens[0].equals("")) break;
                Word element = extendedDb? AndroidUtilitiesDb.createWordFromExtendedDatabase(tokens) : AndroidUtilitiesDb.createWordFromNamesDatabase(tokens);
                elements.add(element);
            }
            lineNum++;
            if (lineNum % blockSizeForDisplay == 0) {
                currentProgress += increment;
                AndroidUtilitiesPrefs.setProgressValueForDbInstallation(context, currentProgress, database);
            }
        }
        ((WordDao) dao).insertAll(elements);

    }

    public static void insertIndexBlock(@NotNull List<String> lineBlock, Context context, Object dao, float increment, int blockSizeForDisplay, @NotNull String indexType) {
        String[] tokens;
        float currentProgress = AndroidUtilitiesPrefs.getProgressValueForDbInstallation(context, Globals.EXTENDED_DB);
        List<String[]> elementsBlock = new ArrayList<>();
        int lineNum = 0;
        boolean hasToken2 = indexType.equals("indexKanji");
        for (String line : lineBlock) {
            tokens = line.split("\\|", -1);
            if (tokens.length > 0) {
                if (tokens[0].equals("")) break;
                elementsBlock.add(new String[]{tokens[0], tokens[1], hasToken2? tokens[2] : ""});
            }
            lineNum++;
            if (lineNum % blockSizeForDisplay == 0) {
                currentProgress += increment;
                AndroidUtilitiesPrefs.setProgressValueForDbInstallation(context, currentProgress, Globals.EXTENDED_DB);
            }
        }
        switch (indexType) {
            case "indexRomaji":
                List<IndexRomaji> elementsRomaji = new ArrayList<>();
                for (String[] item : elementsBlock) {
                    elementsRomaji.add(new IndexRomaji(item[0], item[1]));
                }
                ((IndexRomajiDao) dao).insertAll(elementsRomaji);
                break;
            case "indexEnglish":
                List<IndexEnglish> elementsEnglish = new ArrayList<>();
                for (String[] item : elementsBlock) {
                    elementsEnglish.add(new IndexEnglish(item[0], item[1]));
                }
                ((IndexEnglishDao) dao).insertAll(elementsEnglish);
                break;
            case "indexFrench":
                List<IndexFrench> elementsFrench = new ArrayList<>();
                for (String[] item : elementsBlock) {
                    elementsFrench.add(new IndexFrench(item[0], item[1]));
                }
                ((IndexFrenchDao) dao).insertAll(elementsFrench);
                break;
            case "indexSpanish":
                List<IndexSpanish> elementsSpanish = new ArrayList<>();
                for (String[] item : elementsBlock) {
                    elementsSpanish.add(new IndexSpanish(item[0], item[1]));
                }
                ((IndexSpanishDao) dao).insertAll(elementsSpanish);
                break;
            case "indexKanji":
                List<IndexKanji> elementsKanji = new ArrayList<>();
                for (String[] item : elementsBlock) {
                    elementsKanji.add(new IndexKanji(item[0], item[1], item[2]));
                }
                ((IndexKanjiDao) dao).insertAll(elementsKanji);
                break;
        }
    }
}
