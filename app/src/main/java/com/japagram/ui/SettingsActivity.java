package com.japagram.ui;

import android.os.Bundle;

import com.japagram.R;
import com.japagram.resources.Utilities;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Utilities.changeThemeColor(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }
}
