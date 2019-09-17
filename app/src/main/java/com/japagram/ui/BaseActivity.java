package com.japagram.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;

import com.japagram.R;
import com.japagram.resources.LocaleHelper;

import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_base);

        Locale locale = Locale.getDefault();// get the locale to use...
        Configuration conf = getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= 17) {
            conf.setLocale(locale);
        } else {
            conf.locale = locale;
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }
}
