package com.gafure.hyperlauncher

import android.app.Application
import com.gafure.hyperlauncher.data.AppRepository

/** Process-wide owner of the app list, so package broadcasts outlive the activity. */
class HyperLauncherApp : Application() {

    lateinit var repository: AppRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = AppRepository(this).also { it.start() }
    }
}
