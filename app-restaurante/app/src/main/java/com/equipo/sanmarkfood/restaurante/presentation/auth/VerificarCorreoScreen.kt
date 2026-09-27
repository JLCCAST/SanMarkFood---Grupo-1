package com.equipo.sanmarkfood.restaurante.presentation.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.ui.theme.ApprestauranteTheme

@Composable
fun VerificarCorreoScreen(
    onVolver: () -> Unit,
    onVerificado: () -> Unit,
    viewModel: VerificarCorreoViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.verificado) {
        if (uiState.verificado) onVerificado()
    }
    BackHandler(onBack = onVolver)

    VerificarCorreoContenido(
        uiState = uiState,
        onVolver = onVolver,
        onYaLoConfirme = viewModel::onYaLoConfirme,
        onReenviarEnlace = viewModel::onReenviarEnlace,
    )
}

@Composable
private fun VerificarCorreoContenido(
    uiState: VerificarCorreoUiState,
    onVolver: () -> Unit,
    onYaLoConfirme: () -> Unit,
    onReenviarEnlace: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        CabeceraPaso(texto = stringResource(R.string.registro_paso), pasoActual = 1, onVolver = onVolver)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_correo),
                    contentDescription = null,
                    modifier = Modifier.size(34.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = stringResource(R.string.verificar_titulo),
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.displaySmall,
            )
            val enviamos = stringResource(R.string.verificar_enviamos, uiState.correo)
            Text(
                text = buildAnnotatedString {
                    append(enviamos)
                    val inicio = enviamos.indexOf(uiState.correo)
                    addStyle(SpanStyle(fontWeight = FontWeight.Bold), inicio, inicio + uiState.correo.length)
                },
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(R.string.verificar_no_llego),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (uiState.reenviado) {
                Text(
                    text = stringResource(R.string.verificar_reenviado),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            uiState.error?.let { MensajeError(error = it) }
        }

        Column(
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            BotonPrincipal(
                texto = stringResource(R.string.verificar_ya_confirme),
                onClick = onYaLoConfirme,
                habilitado = !uiState.reenviando,
                cargando = uiState.comprobando,
            )
            TextButton(
                onClick = onReenviarEnlace,
                enabled = !uiState.ocupado,
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) {
                if (uiState.reenviando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(text = stringResource(R.string.verificar_reenviar), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun VerificarCorreoPreview() {
    ApprestauranteTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            VerificarCorreoContenido(
                uiState = VerificarCorreoUiState(correo = "carmen@correo.com", reenviado = true),
                onVolver = {},
                onYaLoConfirme = {},
                onReenviarEnlace = {},
            )
        }
    }
}
