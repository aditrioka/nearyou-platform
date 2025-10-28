package id.nearyou.app.ui.theme

import androidx.compose.ui.unit.dp

/**
 * UI dimension constants
 * 
 * Centralizes all hardcoded dimensions for consistency
 */
object Dimensions {
    // Button dimensions
    val BUTTON_HEIGHT = 50.dp
    val BUTTON_LOADING_INDICATOR_SIZE = 24.dp
    
    // OTP input dimensions
    val OTP_BOX_SIZE = 48.dp
    val OTP_LENGTH = 6
    
    // Timer indicator
    val TIMER_DOT_SIZE = 8.dp
    
    // Auth header
    val AUTH_LOGO_SIZE = 80.dp
    
    // OTP timer duration (seconds)
    const val OTP_TIMER_DURATION = 60
    
    // Username constraints
    const val MIN_USERNAME_LENGTH = 3
    const val MIN_PHONE_LENGTH = 10
}
