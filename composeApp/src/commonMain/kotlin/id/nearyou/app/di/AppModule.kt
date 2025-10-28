package id.nearyou.app.di

import id.nearyou.app.ui.auth.AuthViewModel
import id.nearyou.app.ui.profile.ProfileViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * App-level DI module for ViewModels and UI dependencies
 */
val appModule = module {
    // ViewModels
    viewModel { AuthViewModel(get()) }
    viewModel { ProfileViewModel(get()) }
}

