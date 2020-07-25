package com.japagram.ui;

import android.app.ActionBar;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;

import com.japagram.R;
import com.japagram.resources.UtilitiesPrefs;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        UtilitiesPrefs.changeThemeColor(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        View view = findViewById(R.id.activity_settings);

        view.setBackgroundColor(UtilitiesPrefs.getResColorValue(this, R.attr.colorMonochromeBlend));
    }
}
