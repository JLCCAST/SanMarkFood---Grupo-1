package com.equipo.sanmarkfood.comensal.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.equipo.sanmarkfood.comensal.R

@Composable
fun InicioSesionScreen(
    onIrARegistro: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InicioSesionViewModel = hiltViewModel(),
    recuperarViewModel: RecuperarContrasenaViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val recuperarState by recuperarViewModel.uiState.collectAsState()

    val colors = MaterialTheme.colorScheme
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    // Diálogo de recuperar contraseña
    var showReset by rememberSaveable { mutableStateOf(false) }
    var resetEmail by rememberSaveable { mutableStateOf("") }
    val closeReset: () -> Unit = {
        showReset = false
        recuperarViewModel.limpiar()
    }

    val emailMissing = state.error != null && email.isBlank()
    val passwordMissing = state.error != null && password.isEmpty()

    // Partes del nombre de la marca (el espacio entre ambas se agrega en el código).
    val marcaSanMark = stringResource(R.string.login_marca_san_mark)
    val marcaFood = stringResource(R.string.login_marca_food)

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Cabecera de marca
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = colors.inverseSurface,
                    shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                )
                .statusBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 28.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Etiqueta que diferencia esta app de la de restaurantes
                Text(
                    text = stringResource(R.string.login_etiqueta_comensal),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.secondary)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSecondary
                )
                Text(
                    text = buildAnnotatedString {
                        append("$marcaSanMark ")
                        withStyle(SpanStyle(color = colors.secondary)) { append(marcaFood) }
                    },
                    color = colors.inverseOnSurface,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = stringResource(R.string.login_eslogan),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.inverseOnSurface.copy(alpha = 0.85f)
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.login_titulo),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )

            AuthTextField(
                label = stringResource(R.string.auth_etiqueta_correo),
                value = email,
                onValueChange = { email = it },
                placeholder = stringResource(R.string.auth_placeholder_correo),
                keyboardType = KeyboardType.Email,
                isError = emailMissing
            )
            PasswordField(
                label = stringResource(R.string.auth_etiqueta_contrasena),
                value = password,
                onValueChange = { password = it },
                imeAction = ImeAction.Done,
                onImeAction = { viewModel.login(email, password) },
                isError = passwordMissing
            )

            // Enlace para recuperar la contraseña (SCRUM-82)
            TextButton(
                onClick = {
                    recuperarViewModel.limpiar()
                    resetEmail = email
                    showReset = true
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    stringResource(R.string.login_olvidaste_contrasena),
                    fontWeight = FontWeight.Bold
                )
            }

            state.error?.let { ErrorMessage(it) }

            PrimaryButton(
                text = stringResource(R.string.login_boton_iniciar),
                onClick = { viewModel.login(email, password) },
                isLoading = state.isLoading
            )

            // Exploración sin cuenta (SCRUM-154)
            OutlinedButton(
                onClick = viewModel::continuarComoInvitado,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    stringResource(R.string.login_boton_explorar),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.login_primera_vez),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurfaceVariant
                )
                TextButton(
                    onClick = {
                        viewModel.limpiarError()
                        onIrARegistro()
                    }
                ) {
                    Text(
                        stringResource(R.string.login_crear_cuenta),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showReset) {
        AlertDialog(
            onDismissRequest = closeReset,
            title = { Text(stringResource(R.string.recuperar_titulo)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (recuperarState.enlaceEnviado) {
                        Text(
                            stringResource(R.string.recuperar_enlace_enviado),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            stringResource(R.string.recuperar_instruccion),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant
                        )
                        AuthTextField(
                            label = stringResource(R.string.auth_etiqueta_correo),
                            value = resetEmail,
                            onValueChange = { resetEmail = it },
                            placeholder = stringResource(R.string.auth_placeholder_correo),
                            keyboardType = KeyboardType.Email,
                            isError = recuperarState.error != null
                        )
                        recuperarState.error?.let { ErrorMessage(it) }
                    }
                }
            },
            confirmButton = {
                if (recuperarState.enlaceEnviado) {
                    TextButton(onClick = closeReset) {
                        Text(stringResource(R.string.recuperar_entendido))
                    }
                } else {
                    TextButton(
                        onClick = { recuperarViewModel.sendPasswordReset(resetEmail) },
                        enabled = !recuperarState.isLoading
                    ) {
                        Text(
                            stringResource(
                                if (recuperarState.isLoading) R.string.recuperar_enviando
                                else R.string.recuperar_enviar
                            )
                        )
                    }
                }
            },
            dismissButton = {
                if (!recuperarState.enlaceEnviado) {
                    TextButton(onClick = closeReset) {
                        Text(stringResource(R.string.common_cancelar))
                    }
                }
            }
        )
    }
}