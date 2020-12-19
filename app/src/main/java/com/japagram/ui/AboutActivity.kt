package com.japagram.ui

import android.os.Bundle

import com.japagram.R
import com.japagram.utilitiesAndroid.AndroidUtilitiesPrefs

class AboutActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidUtilitiesPrefs.changeThemeColor(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
    }
}
