package id.nearyou.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun SignupScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToOtpVerification: (String, String, String) -> Unit, // identifier, type, username
    modifier: Modifier = Modifier
) {
    var username by remember { mutableStateOf("") }
    var identifier by remember { mutableStateOf("") }
    var identifierType by remember { mutableStateOf("email") } // "email" or "phone"
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Join NearYou ID today",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Username Input
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            placeholder = { Text("johndoe") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = !isLoading
        )

        // Identifier Type Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = identifierType == "email",
                onClick = { identifierType = "email" },
                label = { Text("Email") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = identifierType == "phone",
                onClick = { identifierType = "phone" },
                label = { Text("Phone") },
                modifier = Modifier.weight(1f)
            )
        }

        // Identifier Input
        OutlinedTextField(
            value = identifier,
            onValueChange = { identifier = it },
            label = { Text(if (identifierType == "email") "Email" else "Phone Number") },
            placeholder = { Text(if (identifierType == "email") "you@example.com" else "+1234567890") },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (identifierType == "email") KeyboardType.Email else KeyboardType.Phone
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = !isLoading
        )

        // Error Message
        errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Sign Up Button
        Button(
            onClick = {
                when {
                    username.isBlank() -> {
                        errorMessage = "Please enter a username"
                        return@Button
                    }
                    identifier.isBlank() -> {
                        errorMessage = "Please enter your ${if (identifierType == "email") "email" else "phone number"}"
                        return@Button
                    }
                }
                
                isLoading = true
                errorMessage = null
                
                // Navigate to OTP verification
                // In a real app, this would trigger registration and OTP sending via repository
                onNavigateToOtpVerification(identifier, identifierType, username)
                isLoading = false
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Sign Up")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Divider
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = "OR",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        // Google Sign-In Button
        OutlinedButton(
            onClick = {
                // TODO: Implement Google Sign-In
                errorMessage = "Google Sign-In coming soon"
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading
        ) {
            Text("Continue with Google")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Login Link
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account? ",
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(
                onClick = onNavigateToLogin,
                enabled = !isLoading
            ) {
                Text("Log In")
            }
        }
    }
}

