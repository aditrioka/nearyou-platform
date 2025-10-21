package id.nearyou.app.di

import id.nearyou.app.ui.auth.AuthViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * App-level DI module for ViewModels and UI dependencies
 */
val appModule = module {
    // AuthViewModel depends on AuthRepository (provided by shared module)
    viewModel { AuthViewModel(get()) }
}

