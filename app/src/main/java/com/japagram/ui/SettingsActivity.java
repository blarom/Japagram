package com.japagram.ui;

import android.os.Bundle;
import android.view.View;

import com.japagram.R;
import com.japagram.utilitiesAndroid.AndroidUtilitiesPrefs;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        AndroidUtilitiesPrefs.changeThemeColor(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        View view = findViewById(R.id.activity_settings);

        view.setBackgroundColor(AndroidUtilitiesPrefs.getResColorValue(this, R.attr.colorMonochromeBlend));
    }
}
