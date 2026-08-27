package com.example

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics

class MilkDeliveryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Initialize Analytics
        FirebaseAnalytics.getInstance(this).apply {
            logEvent(FirebaseAnalytics.Event.APP_OPEN, null)
        }
    }
}
