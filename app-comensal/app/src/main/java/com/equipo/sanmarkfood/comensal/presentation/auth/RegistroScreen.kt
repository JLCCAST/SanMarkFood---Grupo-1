package com.equipo.sanmarkfood.comensal.presentation.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.equipo.sanmarkfood.comensal.R

@Composable
fun RegistroScreen(
    onIrAInicioSesion: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegistroViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var acceptedTerms by rememberSaveable { mutableStateOf(false) }

    val hasMinLength = password.length >= 8
    val hasDigit = password.any { it.isDigit() }
    val canSubmit = name.isNotBlank() &&
            email.contains("@") &&
            hasMinLength &&
            hasDigit &&
            acceptedTerms &&
            !state.isLoading

    fun submit() {
        if (canSubmit) viewModel.register(name, email, password, acceptedTerms)
    }

    val volverAInicioSesion: () -> Unit = {
        viewModel.limpiarError()
        onIrAInicioSesion()
    }

    val descripcionVolver = stringResource(R.string.registro_volver_descripcion)

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        IconButton(
            onClick = volverAInicioSesion,
            modifier = Modifier
                .padding(start = 4.dp, top = 8.dp)
                .semantics { contentDescription = descripcionVolver }
        ) {
            Text(text = "\u2190", fontSize = 24.sp)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.registro_titulo),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = stringResource(R.string.registro_subtitulo),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AuthTextField(
                label = stringResource(R.string.registro_etiqueta_nombre),
                value = name,
                onValueChange = { name = it }
            )
            AuthTextField(
                label = stringResource(R.string.auth_etiqueta_correo),
                value = email,
                onValueChange = { email = it },
                placeholder = stringResource(R.string.auth_placeholder_correo),
                keyboardType = KeyboardType.Email
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                PasswordField(
                    label = stringResource(R.string.auth_etiqueta_contrasena),
                    value = password,
                    onValueChange = { password = it },
                    imeAction = ImeAction.Done,
                    onImeAction = ::submit
                )
                PasswordRule(
                    text = stringResource(R.string.registro_regla_longitud),
                    fulfilled = hasMinLength
                )
                PasswordRule(
                    text = stringResource(R.string.registro_regla_numero),
                    fulfilled = hasDigit
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { acceptedTerms = !acceptedTerms }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Checkbox(checked = acceptedTerms, onCheckedChange = null)
                Text(
                    text = stringResource(R.string.registro_acepto_terminos),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            state.error?.let { ErrorMessage(stringResource(it.mensaje())) }

            PrimaryButton(
                text = stringResource(R.string.registro_boton_crear),
                onClick = ::submit,
                enabled = canSubmit,
                isLoading = state.isLoading
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.registro_ya_tienes_cuenta),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = volverAInicioSesion) {
                    Text(
                        stringResource(R.string.registro_ir_a_login),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun PasswordRule(text: String, fulfilled: Boolean) {
    val colors = MaterialTheme.colorScheme
    val tint = if (fulfilled) colors.tertiary else colors.onSurfaceVariant
    val descripcionCumplida = stringResource(R.string.registro_regla_cumplida)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (fulfilled) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .semantics { contentDescription = descripcionCumplida },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "\u2713",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = tint
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .padding(2.dp)
                    .border(BorderStroke(1.5.dp, tint), CircleShape)
            )
        }
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = tint)
    }
}