package id.nearyou.app

import androidx.compose.ui.window.ComposeUIViewController
import di.platformModule
import di.sharedModule
import id.nearyou.app.di.appModule
import org.koin.core.context.startKoin

fun MainViewController() = ComposeUIViewController {
    // Initialize Koin for iOS
    startKoin {
        modules(
            platformModule,  // Platform-specific (iOS TokenStorage)
            sharedModule,    // Shared (AuthRepository)
            appModule        // App-level (ViewModels)
        )
    }

    App()
}