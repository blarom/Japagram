package com.japagram.ui

import android.os.Bundle

import com.japagram.R
import com.japagram.resources.UtilitiesPrefs

class AboutActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        UtilitiesPrefs.changeThemeColor(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
    }
}
