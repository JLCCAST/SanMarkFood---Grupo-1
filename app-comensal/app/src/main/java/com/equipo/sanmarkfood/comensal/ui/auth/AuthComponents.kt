package com.equipo.sanmarkfood.comensal.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AuthTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: @Composable (() -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = placeholder?.let { { Text(it) } },
            singleLine = true,
            isError = isError,
            shape = RoundedCornerShape(12.dp),
            visualTransformation = visualTransformation,
            trailingIcon = trailing,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = KeyboardActions(onDone = { onImeAction() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.surfaceContainerLowest,
                unfocusedContainerColor = colors.surfaceContainerLowest,
                errorContainerColor = colors.surfaceContainerLowest
            )
        )
    }
}

@Composable
fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {},
    isError: Boolean = false
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    AuthTextField(
        label = label,
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        keyboardType = KeyboardType.Password,
        imeAction = imeAction,
        onImeAction = onImeAction,
        isError = isError,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailing = {
            TextButton(onClick = { visible = !visible }) {
                Text(if (visible) "Ocultar" else "Mostrar")
            }
        }
    )
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = CircleShape
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.5.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ErrorMessage(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error
    )
}