package id.nearyou.app

import android.app.Application
import di.platformModule
import di.sharedModule
import id.nearyou.app.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import util.AppConfig

/**
 * Application class for initializing Koin DI
 */
class NearYouApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize logging configuration (IMPORTANT for HTTP logging!)
        // Use applicationInfo.flags to detect debug mode
        val isDebug = (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        AppConfig.initialize(isDevelopment = isDebug)

        // Initialize Koin
        startKoin {
            // Reference Android context
            androidContext(this@NearYouApplication)
            // Load modules
            modules(
                platformModule,  // Platform-specific (Android TokenStorage)
                sharedModule,    // Shared (AuthRepository)
                appModule        // App-level (ViewModels)
            )
        }
    }
}

