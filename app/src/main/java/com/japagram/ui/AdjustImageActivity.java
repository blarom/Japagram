package com.japagram.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.japagram.R;
import com.japagram.resources.ConvolutionMatrix;
import com.japagram.utilitiesAndroid.UtilitiesAndroidIO;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import androidx.preference.PreferenceManager;

public class AdjustImageActivity extends BaseActivity {
    //http://android-er.blogspot.co.il/2013/09/adjust-saturation-of-bitmap-with.html

    private ImageView imageResult;
    private SeekBar contrastBar;
    private TextView contrastText;
    private SeekBar saturationBar;
    private TextView saturationText;
    private SeekBar brightnessBar;
    private TextView brightnessText;
    private Bitmap bitmapMaster;

    //Activity lifecylce methods
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjust_image);

        imageResult = findViewById(R.id.OCRimage);

        //Retrieving the image from the application
        Bundle extras = getIntent().getExtras();
        Uri imageUri = Uri.parse(extras.getString("imageUri"));
        imageResult.setImageURI(imageUri);

        //Getting the master bitmap to perform adjustments on
        bitmapMaster = getBitmapFromImageView(imageResult);

        //Setting the adjustment parameters
        contrastText = findViewById(R.id.contrastDescription);
        contrastBar = findViewById(R.id.contrastBar);
        brightnessText = findViewById(R.id.brightnessDescription);
        brightnessBar = findViewById(R.id.brightnessBar);
        saturationText = findViewById(R.id.saturationDescription);
        saturationBar = findViewById(R.id.saturationBar);

        contrastBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                loadBitmapAfterAdjustment();
            }
        });
        brightnessBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                loadBitmapAfterAdjustment();
            }
        });
        saturationBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                loadBitmapAfterAdjustment();
            }
        });

        setupBitmapImageWithPreferenceValues();
        loadBitmapAfterAdjustment();

    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_adjust_image, menu);
        return true;
    }
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();

        switch (itemThatWasClickedId) {
            case R.id.action_done:
                returnWithResult();
                return true;
            case R.id.action_settings:
                Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
                startSettingsActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(startSettingsActivity);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void returnWithResult() {
        bitmapMaster = getBitmapFromImageView(imageResult);
        Uri adjustedImageURI = createImageUri(bitmapMaster);

        Intent returnIntent = new Intent();
        returnIntent.putExtra("returnImageUri", adjustedImageURI.toString());
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    //Image modification methods
    private Bitmap getBitmapFromImageView(ImageView imageView) {
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        return drawable.getBitmap();
    }
    private Uri createImageUri(Bitmap inImage) {

        Uri uri = null;
        try {
            Bitmap newBitmap = Bitmap.createScaledBitmap(inImage, inImage.getWidth(), inImage.getHeight(), false);
            File file = new File(getFilesDir(), "Image" + new Random().nextInt() + ".jpeg");
            FileOutputStream out = openFileOutput(file.getName(), 0);
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            String realPath = file.getAbsolutePath();
            File f = new File(realPath);
            uri = Uri.fromFile(f);

        } catch (Exception e) {
            Log.e("Your Error Message", e.getMessage());
        }
        return uri;

    }
    private void setupBitmapImageWithPreferenceValues() {

        //Getting the user's chosen default values from the settings
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        float contrastValue = UtilitiesAndroidIO.loadOCRImageContrastFromSharedPreferences(sharedPreferences, getApplicationContext());
        float saturationValue = UtilitiesAndroidIO.loadOCRImageSaturationFromSharedPreferences(sharedPreferences, getApplicationContext());
        int brightnessValue = UtilitiesAndroidIO.loadOCRImageBrightnessFromSharedPreferences(sharedPreferences, getApplicationContext());

        contrastBar.setProgress(UtilitiesAndroidIO.convertContrastValueToProgress(contrastValue, getApplicationContext()));
        saturationBar.setProgress(UtilitiesAndroidIO.convertSaturationValueToProgress(saturationValue, getApplicationContext()));
        brightnessBar.setProgress(UtilitiesAndroidIO.convertBrightnessValueToProgress(brightnessValue, getApplicationContext()));
    }
    private void loadBitmapAfterAdjustment() {
        if (bitmapMaster != null) {
            //Contrast, 1=min. 10=max
            //Brightness, -255=min. 255=max
            //Saturation, 0=gray-scale. 1=identity

            // Get values from progressBars. The ranges are kept wide in order to provide enough increments
            int contrastBarValue = contrastBar.getProgress();
            int brightnessBarValue = brightnessBar.getProgress();
            int saturationBarValue = saturationBar.getProgress();

            // Preparing the values to be set in the image adjustment methods
            float contrastValue = UtilitiesAndroidIO.convertContrastProgressToValue(contrastBarValue, getApplicationContext());
            int brightnessValue = UtilitiesAndroidIO.convertBrightnessProgressToValue(brightnessBarValue, getApplicationContext());
            float saturationValue = UtilitiesAndroidIO.convertSaturationProgressToValue(saturationBarValue, getApplicationContext());

            float contrastDisplay = contrastValue;
            float brightnessDisplay = (float) brightnessValue/256;
            float saturationDisplay = saturationValue;

            String text = getString(R.string.contrast_) + String.valueOf(contrastDisplay);
            contrastText.setText(text);
            text = getString(R.string.brightness_) + String.valueOf(brightnessDisplay);
            brightnessText.setText(text);
            text = getString(R.string.saturation_) + String.valueOf(saturationDisplay);
            saturationText.setText(text);

            Bitmap result = adjustImageContrastAndBrightness(bitmapMaster, contrastValue, brightnessValue);
            result = adjustBitmapSaturation(result, saturationValue);

            imageResult.setImageBitmap(result);

        }
    }
    private Bitmap adjustBitmapSaturation(Bitmap src, float settingSat) {

        int w = src.getWidth();
        int h = src.getHeight();

        Bitmap bitmapResult = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvasResult = new Canvas(bitmapResult);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(settingSat);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(filter);
        canvasResult.drawBitmap(src, 0, 0, paint);

        return bitmapResult;
    }
    private Bitmap adjustImageContrastAndBrightness(Bitmap bmp, float contrast, float brightness) {
        //https://stackoverflow.com/questions/12891520/how-to-programmatically-change-contrast-of-a-bitmap-in-android
        /*
         * @param bmp input bitmap
         * @param contrast 0..10 1 is default
         * @param brightness -255..255 0 is default
         * @return new bitmap
         */
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });

        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, 0, 0, paint);

        return ret;
    }
    private Bitmap adjustImageAngleAndScale(Bitmap source, float angle, double scaleFactor) {

        int newWidth = (int) Math.floor(source.getWidth()*scaleFactor);
        int newHeight = (int) Math.floor(source.getHeight()*scaleFactor);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(source, newWidth, newHeight,true);

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

        return rotatedBitmap;
    }
    public Bitmap adjustImageSharpness(Bitmap src, double weight) {
        //https://xjaphx.wordpress.com/2011/06/22/image-processing-sharpening-image/
        double[][] SharpConfig = new double[][] {
                { 0 , -2    , 0  },
                { -2, weight, -2 },
                { 0 , -2    , 0  }
        };
        ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
        convMatrix.applyConfig(SharpConfig);
        convMatrix.Factor = weight - 8;
        return ConvolutionMatrix.computeConvolution3x3(src, convMatrix);
    }
}
