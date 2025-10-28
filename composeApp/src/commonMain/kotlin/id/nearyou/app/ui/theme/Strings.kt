package id.nearyou.app.ui.theme

/**
 * Centralized strings for internationalization
 * 
 * TODO: Replace with actual resource strings when adding i18n support
 */
object Strings {
    // Common
    const val APP_NAME = "NearYou ID"
    
    // Auth - Common
    const val EMAIL_OR_PHONE_LABEL = "Email or Phone"
    const val EMAIL_OR_PHONE_PLACEHOLDER = "Enter your email or phone"
    const val VERIFICATION_CODE_MESSAGE = "We'll send you a verification code"
    
    // Login
    const val SIGN_IN = "Sign In"
    const val DONT_HAVE_ACCOUNT = "Don't have an account? "
    const val SIGN_UP = "Sign Up"
    
    // Signup
    const val USERNAME_LABEL = "Username"
    const val USERNAME_PLACEHOLDER = "Choose a username"
    const val CREATE_ACCOUNT = "Create Account"
    const val ALREADY_HAVE_ACCOUNT = "Already have an account? "
    const val SIGN_IN_LINK = "Sign In"
    
    // OTP Verification
    const val VERIFICATION_CODE_SENT = "Verification code sent to"
    const val CODE_EXPIRES_IN = "Code expires in"
    const val DIDNT_RECEIVE_CODE = "Didn't receive the code? "
    const val RESEND = "Resend"
    const val RESEND_IN = "Didn't receive the code? Resend in"
    const val VERIFY = "Verify"
    const val USE_DIFFERENT_CONTACT = "Use Different Email or Phone"
    
    // Main Screen
    const val WELCOME_MESSAGE = "Welcome to NearYou ID!"
    const val AUTHENTICATED_MESSAGE = "You are successfully authenticated"
    const val LOGOUT = "Logout"
    
    // Error Messages
    const val ERROR_BLANK_IDENTIFIER = "Please enter your email or phone"
    const val ERROR_INVALID_EMAIL = "Please enter a valid email address"
    const val ERROR_INVALID_PHONE = "Please enter a valid phone number"
    const val ERROR_BLANK_USERNAME = "Please enter a username"
    const val ERROR_SHORT_USERNAME = "Username must be at least 3 characters"
    const val ERROR_INVALID_OTP = "Please enter a 6-digit code"
    const val ERROR_REGISTRATION_FAILED = "Registration failed"
    const val ERROR_LOGIN_FAILED = "Login failed"
    const val ERROR_VERIFICATION_FAILED = "Verification failed"
    const val ERROR_RESEND_FAILED = "Failed to resend code"
    const val ERROR_UNEXPECTED = "An unexpected error occurred"
    const val ERROR_LOGOUT_FAILED = "Logout failed"
    const val ERROR_TOKEN_REFRESH_FAILED = "Token refresh failed"
    const val ERROR_GOOGLE_LOGIN_FAILED = "Google login failed"
    
    // Success Messages
    const val SUCCESS_CODE_RESENT = "Verification code resent successfully"
    const val SUCCESS_VERIFICATION = "Verification successful!"
}
