package com.japagram.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.Window

import com.japagram.R
import com.japagram.resources.LocaleHelper

import java.util.Locale

import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_base)

        val locale = Locale.getDefault()// get the locale to use...
        val conf = resources.configuration
        if (Build.VERSION.SDK_INT >= 17) {
            conf.setLocale(locale)
        } else {
            conf.locale = locale
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(base))
    }
}
