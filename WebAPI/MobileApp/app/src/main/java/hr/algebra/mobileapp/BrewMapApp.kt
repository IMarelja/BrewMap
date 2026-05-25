package hr.algebra.mobileapp

import android.app.Application
import android.content.Context
import org.osmdroid.config.Configuration

class BrewMapApp : Application() {
    companion object {
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        Configuration.getInstance().userAgentValue = packageName
    }
}
