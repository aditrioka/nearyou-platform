package id.nearyou.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.nearyou.app.ui.theme.Spacing

@Composable
fun OtpInput(
    value: String,
    onValueChange: (String) -> Unit,
    length: Int = 6,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs, Alignment.CenterHorizontally)
    ) {
        repeat(length) { index ->
            OtpDigitBox(
                digit = value.getOrNull(index)?.toString() ?: "",
                isFocused = value.length == index,
                enabled = enabled
            )
        }
    }
    
    // Hidden text field to capture input
    BasicTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.length <= length && newValue.all { it.isDigit() }) {
                onValueChange(newValue)
            }
        },
        modifier = Modifier.size(0.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        enabled = enabled
    )
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
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

