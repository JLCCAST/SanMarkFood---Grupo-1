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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

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
                    text = "COMENSALES",
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
                        append("San Mark ")
                        withStyle(SpanStyle(color = colors.secondary)) { append("Food") }
                    },
                    color = colors.inverseOnSurface,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Menús del día, reservas y pedidos para recoger, a pasos de San Marcos.",
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
                text = "Inicia sesión",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )

            AuthTextField(
                label = "Correo electrónico",
                value = email,
                onValueChange = { email = it },
                placeholder = "tu@correo.com",
                keyboardType = KeyboardType.Email,
                isError = emailMissing
            )
            PasswordField(
                label = "Contraseña",
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
                Text("¿Olvidaste tu contraseña?", fontWeight = FontWeight.Bold)
            }

            state.error?.let { ErrorMessage(it) }

            PrimaryButton(
                text = "Iniciar sesión",
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
                Text("Explorar sin cuenta", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿Primera vez aquí?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurfaceVariant
                )
                TextButton(
                    onClick = {
                        viewModel.limpiarError()
                        onIrARegistro()
                    }
                ) {
                    Text("Crea tu cuenta", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showReset) {
        AlertDialog(
            onDismissRequest = closeReset,
            title = { Text("Recuperar contraseña") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (recuperarState.enlaceEnviado) {
                        Text(
                            "Si el correo está registrado, te enviamos un enlace para " +
                                    "crear una nueva contraseña. Revisa también la carpeta de spam.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            "Escribe tu correo y te enviaremos un enlace para crear una nueva contraseña.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant
                        )
                        AuthTextField(
                            label = "Correo electrónico",
                            value = resetEmail,
                            onValueChange = { resetEmail = it },
                            placeholder = "tu@correo.com",
                            keyboardType = KeyboardType.Email,
                            isError = recuperarState.error != null
                        )
                        recuperarState.error?.let { ErrorMessage(it) }
                    }
                }
            },
            confirmButton = {
                if (recuperarState.enlaceEnviado) {
                    TextButton(onClick = closeReset) { Text("Entendido") }
                } else {
                    TextButton(
                        onClick = { recuperarViewModel.sendPasswordReset(resetEmail) },
                        enabled = !recuperarState.isLoading
                    ) {
                        Text(if (recuperarState.isLoading) "Enviando..." else "Enviar enlace")
                    }
                }
            },
            dismissButton = {
                if (!recuperarState.enlaceEnviado) {
                    TextButton(onClick = closeReset) { Text("Cancelar") }
                }
            }
        )
    }
}