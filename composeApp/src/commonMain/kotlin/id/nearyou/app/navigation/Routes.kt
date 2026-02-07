package id.nearyou.app.navigation

import kotlinx.serialization.Serializable

@Serializable
data object AuthGraph

@Serializable
data object Login

@Serializable
data object Signup

@Serializable
data class OtpVerification(
    val identifier: String,
    val identifierType: String,
    val username: String? = null,
)

@Serializable
data object MainGraph

@Serializable
data object Home

@Serializable
data object Profile

@Serializable
data object EditProfile
