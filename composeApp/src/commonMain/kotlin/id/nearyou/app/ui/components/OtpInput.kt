package id.nearyou.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.nearyou.app.ui.theme.Spacing

/**
 * OTP Input Component - Refactored for better UX
 * 
 * Shows 6 individual boxes but uses a single TextField for proper input handling
 * This approach:
 * - Maintains proper focus management
 * - Supports accessibility
 * - Handles keyboard input correctly
 * - Works on all platforms (iOS, Android, Desktop)
 */
@Composable
fun OtpInput(
    value: String,
    onValueChange: (String) -> Unit,
    length: Int = 6,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val focusRequester = remember { FocusRequester() }
    
    // Request focus when component becomes enabled
    LaunchedEffect(enabled) {
        if (enabled) {
            focusRequester.requestFocus()
        }
    }
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Visual OTP boxes
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(length) { index ->
                OtpDigitBox(
                    digit = value.getOrNull(index)?.toString() ?: "",
                    isFocused = value.length == index && enabled,
                    enabled = enabled
                )
            }
        }
        
        Spacer(modifier = Modifier.height(Spacing.xs))
        
        // Actual text field (visible for accessibility and input)
        // Using TextFieldValue to control cursor position
        var textFieldValue by remember(value) {
            mutableStateOf(
                TextFieldValue(
                    text = value,
                    selection = TextRange(value.length)
                )
            )
        }
        
        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                // Only allow digits and max length
                val filtered = newValue.text.filter { it.isDigit() }.take(length)
                if (filtered != value) {
                    onValueChange(filtered)
                    textFieldValue = newValue.copy(
                        text = filtered,
                        selection = TextRange(filtered.length)
                    )
                }
            },
            modifier = Modifier
                .width(1.dp)  // Nearly invisible but still focusable
                .height(1.dp)
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword
            ),
            enabled = enabled,
            singleLine = true
        )
    }
}

@Composable
private fun OtpDigitBox(
    digit: String,
    isFocused: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = when {
                    !enabled -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    isFocused -> MaterialTheme.colorScheme.primary
                    digit.isNotEmpty() -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outline
                },
                shape = MaterialTheme.shapes.small
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digit,
            style = MaterialTheme.typography.headlineMedium,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            },
            textAlign = TextAlign.Center
        )
    }
}
