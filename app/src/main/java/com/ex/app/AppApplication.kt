package com.ex.app

import android.app.Application
import timber.log.Timber

class AppApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(TimberDebugTree())
        }
    }

    class TimberDebugTree : Timber.DebugTree() {
        override fun createStackElementTag(element: StackTraceElement): String? {
            return "mTestLog:${element.fileName}:${element.lineNumber}#${element.methodName}"
        }
    }
}