package id.nearyou.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import id.nearyou.app.ui.theme.Dimensions
import id.nearyou.app.ui.theme.Spacing
import id.nearyou.app.ui.theme.Strings

/**
 * Auth Header Component - Refactored
 * 
 * Uses centralized strings and dimensions
 */
@Composable
fun AuthHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo/Icon placeholder - using Material Icon for now
        Icon(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = "${Strings.APP_NAME} Logo",
            modifier = Modifier.size(Dimensions.AUTH_LOGO_SIZE),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(Spacing.md))
        
        Text(
            text = Strings.APP_NAME,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
