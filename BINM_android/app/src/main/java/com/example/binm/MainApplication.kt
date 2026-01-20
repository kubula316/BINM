package com.example.binm

import android.app.Application
import com.example.binm.manager.SessionManager
import com.example.binm.manager.ThemeManager

class MainApplication : Application() {

    companion object {
        lateinit var sessionManager: SessionManager
        lateinit var themeManager: ThemeManager
    }

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(this)
        themeManager = ThemeManager(this)
    }
}
