package id.nearyou.app.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.nearyou.app.ui.auth.AuthViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = koinInject()
) {
    val scope = rememberCoroutineScope()
    val authState by authViewModel.uiState.collectAsState()
    var isLoggingOut by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to NearYou ID!",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "You are successfully authenticated",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Button(
            onClick = {
                scope.launch {
                    isLoggingOut = true
                    authViewModel.logout()
                    isLoggingOut = false
                }
            },
            enabled = !isLoggingOut,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isLoggingOut) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Logout")
            }
        }
    }
}

