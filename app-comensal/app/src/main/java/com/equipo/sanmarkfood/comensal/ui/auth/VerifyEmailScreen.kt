package com.equipo.sanmarkfood.comensal.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmailVerificationScreen(
    state: AuthUiState,
    onCheckVerified: () -> Unit,
    onResend: () -> Unit,
    onChangeEmail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val canResend = state.resendCooldown == 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(colors.secondaryContainer, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "\u2709", fontSize = 34.sp)
        }

        Text(
            text = "Revisa tu correo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = buildAnnotatedString {
                append("Te enviamos un enlace de verificación a ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(state.verificationEmail.ifBlank { "tu correo" })
                }
                append(". Tu cuenta se activará cuando lo abras.")
            },
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "¿No aparece? Revisa la carpeta de spam o de promociones.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant
        )

        Spacer(modifier = Modifier.weight(1f))

        state.error?.let { ErrorMessage(it) }

        PrimaryButton(
            text = "Ya verifiqué mi correo",
            onClick = onCheckVerified,
            isLoading = state.isLoading
        )
        OutlinedButton(
            onClick = onResend,
            enabled = canResend,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = CircleShape
        ) {
            Text(
                text = if (canResend) "Reenviar correo" else "Reenviar correo en ${state.resendCooldown} s",
                fontWeight = FontWeight.SemiBold
            )
        }
        TextButton(
            onClick = onChangeEmail,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Cambiar correo", fontWeight = FontWeight.SemiBold)
        }
    }
}