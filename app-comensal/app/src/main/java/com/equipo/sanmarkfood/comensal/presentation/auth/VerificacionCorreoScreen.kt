package com.equipo.sanmarkfood.comensal.presentation.auth

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.equipo.sanmarkfood.comensal.R

@Composable
fun VerificacionCorreoScreen(
    correo: String,
    modifier: Modifier = Modifier,
    viewModel: VerificacionCorreoViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val colors = MaterialTheme.colorScheme
    val canResend = state.resendCooldown == 0

    val enlaceInicio = stringResource(R.string.verificacion_enlace_inicio)
    val enlaceFin = stringResource(R.string.verificacion_enlace_fin)
    val correoMostrado = correo.ifBlank { stringResource(R.string.verificacion_correo_por_defecto) }

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
            text = stringResource(R.string.verificacion_titulo),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = buildAnnotatedString {
                append("$enlaceInicio ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(correoMostrado)
                }
                append(enlaceFin)
            },
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = stringResource(R.string.verificacion_no_aparece),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant
        )

        Spacer(modifier = Modifier.weight(1f))

        state.error?.let { ErrorMessage(stringResource(it)) }

        PrimaryButton(
            text = stringResource(R.string.verificacion_boton_verificado),
            onClick = viewModel::checkVerified,
            isLoading = state.isLoading
        )
        OutlinedButton(
            onClick = viewModel::resendVerification,
            enabled = canResend,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = CircleShape
        ) {
            Text(
                text = if (canResend) {
                    stringResource(R.string.verificacion_reenviar)
                } else {
                    stringResource(R.string.verificacion_reenviar_en, state.resendCooldown)
                },
                fontWeight = FontWeight.SemiBold
            )
        }
        TextButton(
            onClick = viewModel::changeEmail,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                stringResource(R.string.verificacion_cambiar_correo),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}