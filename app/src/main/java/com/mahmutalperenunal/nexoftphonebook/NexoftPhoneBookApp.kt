package com.mahmutalperenunal.nexoftphonebook

import android.app.Application

// Application class used for global app-level initialization
class NexoftPhoneBookApp : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}