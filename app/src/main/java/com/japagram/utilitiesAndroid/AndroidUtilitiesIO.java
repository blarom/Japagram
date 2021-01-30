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
import com.japagram.data.KanjiCharacter;
import com.japagram.data.KanjiCharacterDao;
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
import java.util.HashMap;
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
            String[] tokens;
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
                    increment = (((float) blocksize / Globals.EXTENDED_DB_LINES_WORDS) * Globals.EXTENDED_DB_SIZE_WORDS * 100.f / Globals.EXTENDED_DB_SIZE_TOTAL);
                    while ((line = fileReader.readLine()) != null) {
                        tokens = line.split("\\|", -1);
                        if (tokens.length > 0) {
                            if (tokens[0].equals("")) break;
                            Word word = AndroidUtilitiesDb.createWordFromExtendedDatabase(tokens);
                            wordList.add(word);
                        }
                        lineNum++;
                        if (lineNum % MAX_NUM_ELEMENTS_IN_WORD_INSERT_BLOCK == 0) {
                            extendedDbWordDao.insertAll(wordList);
                            wordList = new ArrayList<>();
                        }
                        if (lineNum % blocksize == 0) {
                            currentProgress += increment;
                            AndroidUtilitiesPrefs.setProgressValueExtendedDb(context, currentProgress);
                        }
                    }
                    extendedDbWordDao.insertAll(wordList);
                    break;
                case "namesDbWords":
                    WordDao namesDbWordDao = (WordDao) dao;
                    currentProgress = 0;
                    blocksize = MAX_NUM_ELEMENTS_IN_WORD_INSERT_BLOCK * 2;
                    increment = ((float) blocksize / Globals.NAMES_DB_LINES_WORDS * (Globals.NAMES_DB_SIZE_WORDS * 100.f / Globals.NAMES_DB_SIZE_TOTAL));
                    while ((line = fileReader.readLine()) != null) {
                        tokens = line.split("\\|", -1);
                        if (tokens.length > 0) {
                            if (tokens[0].equals("")) break;
                            Word word = AndroidUtilitiesDb.createWordFromNamesDatabase(tokens);
                            wordList.add(word);
                        }
                        lineNum++;
                        if (lineNum % MAX_NUM_ELEMENTS_IN_WORD_INSERT_BLOCK == 0) {
                            namesDbWordDao.insertAll(wordList);
                            wordList = new ArrayList<>();
                        }
                        if (lineNum % blocksize == 0) {
                            currentProgress += increment;
                            AndroidUtilitiesPrefs.setProgressValueNamesDb(context, currentProgress);
                        }
                    }
                    namesDbWordDao.insertAll(wordList);
                    break;
                case "indexRomaji":
                    blocksize = MAX_NUM_ELEMENTS_IN_WORD_INSERT_BLOCK / 4;
                    if (isExtendedDb) {
                        currentProgress = AndroidUtilitiesPrefs.getProgressValueExtendedDb(context);
                        increment = (((float) blocksize / Globals.EXTENDED_DB_LINES_ROMAJI_INDEX) * Globals.EXTENDED_DB_SIZE_ROMAJI_INDEX * 100.f / Globals.EXTENDED_DB_SIZE_TOTAL);
                    } else if (isNamesDb) {
                        currentProgress = AndroidUtilitiesPrefs.getProgressValueNamesDb(context);
                        increment = (((float) blocksize / Globals.NAMES_DB_LINES_ROMAJI_INDEX) * Globals.NAMES_DB_SIZE_ROMAJI_INDEX * 100.f / Globals.NAMES_DB_SIZE_TOTAL);
                    }
                    IndexRomajiDao indexRomajiDao = (IndexRomajiDao) dao;
                    while ((line = fileReader.readLine()) != null) {
                        tokens = line.split("\\|", -1);
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
                                AndroidUtilitiesPrefs.setProgressValueExtendedDb(context, currentProgress);
                            } else if (isNamesDb) {
                                currentProgress += increment;
                                AndroidUtilitiesPrefs.setProgressValueNamesDb(context, currentProgress);
                            }
                        }
                    }
                    indexRomajiDao.insertAll(indexRomajiList);
                    break;
                case "indexEnglish":
                    blocksize = MAX_NUM_ELEMENTS_IN_WORD_INSERT_BLOCK / 10;
                    if (isExtendedDb) {
                        currentProgress = AndroidUtilitiesPrefs.getProgressValueExtendedDb(context);
                        increment = (((float) blocksize / Globals.EXTENDED_DB_LINES_ENGLISH_INDEX) * Globals.EXTENDED_DB_SIZE_ENGLISH_INDEX * 100.f / Globals.EXTENDED_DB_SIZE_TOTAL);
                    }
                    IndexEnglishDao indexEnglishDao = (IndexEnglishDao) dao;
                    while ((line = fileReader.readLine()) != null) {
                        tokens = line.split("\\|", -1);
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
                                AndroidUtilitiesPrefs.setProgressValueExtendedDb(context, currentProgress);
                            }
                        }
                    }
                    indexEnglishDao.insertAll(indexEnglishList);
                    break;
                case "indexFrench":
                    if (isExtendedDb) {
                        currentProgress = AndroidUtilitiesPrefs.getProgressValueExtendedDb(context);
                        increment = Globals.EXTENDED_DB_SIZE_FRENCH_INDEX * 100.f / Globals.EXTENDED_DB_SIZE_TOTAL;
                    }
                    IndexFrenchDao indexFrenchDao = (IndexFrenchDao) dao;
                    while ((line = fileReader.readLine()) != null) {
                        tokens = line.split("\\|", -1);
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
                        AndroidUtilitiesPrefs.setProgressValueExtendedDb(context, currentProgress);
                    }
                    break;
                case "indexSpanish":
                    if (isExtendedDb) {
                        currentProgress = AndroidUtilitiesPrefs.getProgressValueExtendedDb(context);
                        increment = Globals.EXTENDED_DB_SIZE_SPANISH_INDEX * 100.f / Globals.EXTENDED_DB_SIZE_TOTAL;
                    }
                    IndexSpanishDao indexSpanishDao = (IndexSpanishDao) dao;
                    while ((line = fileReader.readLine()) != null) {
                        tokens = line.split("\\|", -1);
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
                        AndroidUtilitiesPrefs.setProgressValueExtendedDb(context, currentProgress);
                    }
                    break;
                case "indexKanji":
                    blocksize = MAX_NUM_ELEMENTS_IN_WORD_INSERT_BLOCK;
                    if (isExtendedDb) {
                        currentProgress = AndroidUtilitiesPrefs.getProgressValueExtendedDb(context);
                        increment = (((float) blocksize / Globals.EXTENDED_DB_LINES_KANJI_INDEX) * Globals.EXTENDED_DB_SIZE_KANJI_INDEX * 100.f / Globals.EXTENDED_DB_SIZE_TOTAL);
                    } else if (isNamesDb) {
                        currentProgress = AndroidUtilitiesPrefs.getProgressValueNamesDb(context);
                        increment = (((float) blocksize / Globals.NAMES_DB_LINES_KANJI_INDEX) * Globals.NAMES_DB_SIZE_KANJI_INDEX * 100.f / Globals.NAMES_DB_SIZE_TOTAL);
                    }
                    IndexKanjiDao indexKanjiDao = (IndexKanjiDao) dao;
                    while ((line = fileReader.readLine()) != null) {
                        tokens = line.split("\\|", -1);
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
                                AndroidUtilitiesPrefs.setProgressValueExtendedDb(context, currentProgress);
                            } else if (isNamesDb) {
                                currentProgress += increment;
                                AndroidUtilitiesPrefs.setProgressValueNamesDb(context, currentProgress);
                            }
                        }
                    }
                    indexKanjiDao.insertAll(indexKanjiList);
                    break;
                case "kanjiCharactersDb":
                    List<String[]> KanjiDict_Database = AndroidUtilitiesIO.readCSVFile("LineKanjiDictionary - 3000 kanji.csv", context);

                    HashMap<String, String[]> kanjiProperties = new HashMap<>();
                    String key;
                    for (int i=0; i<KanjiDict_Database.size(); i++) {
                        key = KanjiDict_Database.get(i)[0];
                        if (TextUtils.isEmpty(key)) break;
                        String[] readings = KanjiDict_Database.get(i)[1].split("#",-1); //-1 to prevent discarding last empty string
                        kanjiProperties.put(key, new String[]{
                                readings.length > 2? readings[0].trim() : "",
                                readings.length > 2? readings[1].trim() : "",
                                readings.length > 2? readings[2].trim() : "",
                                KanjiDict_Database.get(i)[2],
                                KanjiDict_Database.get(i)[3],
                                KanjiDict_Database.get(i)[4]
                        });
                    }

                    List<String[]> RadicalsDatabase = AndroidUtilitiesIO.readCSVFile("LineRadicals - 3000 kanji.csv", context);
                    HashMap<String, String> radicalProperties = new HashMap<>();
                    for (int i=0; i<RadicalsDatabase.size(); i++) {
                        key = RadicalsDatabase.get(i)[0];
                        if (TextUtils.isEmpty(key)) break;
                        radicalProperties.put(key, RadicalsDatabase.get(i)[1]);
                    }

                    KanjiCharacterDao kanjiCharacterDao = (KanjiCharacterDao) dao;
                    String[] properties;
                    while ((line = fileReader.readLine()) != null) {
                        tokens = line.split("\\|", -1);
                        if (tokens.length > 0) {
                            key = tokens[0];
                            if (TextUtils.isEmpty(key)) break;
                            KanjiCharacter kanjiCharacter = new KanjiCharacter(key, tokens[1], tokens[2]);
                            kanjiCharacter.setKanji(OvUtilsGeneral.convertFromUTF8Index(key));
                            if (kanjiProperties.containsKey(key)) {
                                properties = kanjiProperties.get(key);
                                if (properties != null && properties.length == 6) {
                                    kanjiCharacter.setOnReadings(properties[0]);
                                    kanjiCharacter.setKunReadings(properties[1]);
                                    kanjiCharacter.setNameReadings(properties[2]);
                                    kanjiCharacter.setMeaningsEN(properties[3]);
                                    kanjiCharacter.setMeaningsFR(properties[4]);
                                    kanjiCharacter.setMeaningsES(properties[5]);
                                    kanjiCharacter.setUsedInJapanese(1);
                                }
                            }
                            if (radicalProperties.containsKey(key)) {
                                kanjiCharacter.setRadPlusStrokes(radicalProperties.get(key));
                            }
                            kanjiCharacterList.add(kanjiCharacter);
                        }
                        lineNum++;
                        if (lineNum % MAX_NUM_ELEMENTS_IN_KANJI_CHARS_INSERT_BLOCK == 0) {
                            kanjiCharacterDao.insertAll(kanjiCharacterList);
                            kanjiCharacterList = new ArrayList<>();
                        }
                    }
                    kanjiCharacterDao.insertAll(kanjiCharacterList);
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
                        tokens = line.split("\\|", -1);
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
}
