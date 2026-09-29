package com.equipo.sanmarkfood.restaurante.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.ui.theme.ApprestauranteTheme
@Composable
fun RegistroScreen(
    onVolver: () -> Unit,
    onCuentaCreada: (correo: String) -> Unit,
    viewModel: RegistroViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.cuentaCreada) {
        if (uiState.cuentaCreada) onCuentaCreada(uiState.correo.trim())
    }

    RegistroContenido(
        uiState = uiState,
        onVolver = onVolver,
        onCambiarCorreo = viewModel::onCambiarCorreo,
        onCambiarContrasena = viewModel::onCambiarContrasena,
        onCambiarRepetirContrasena = viewModel::onCambiarRepetirContrasena,
        onCrearCuenta = viewModel::onCrearCuenta,
    )
}

@Composable
private fun RegistroContenido(
    uiState: RegistroUiState,
    onVolver: () -> Unit,
    onCambiarCorreo: (String) -> Unit,
    onCambiarContrasena: (String) -> Unit,
    onCambiarRepetirContrasena: (String) -> Unit,
    onCrearCuenta: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        CabeceraPaso(texto = stringResource(R.string.registro_paso), pasoActual = 1, onVolver = onVolver)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(PaddingValues(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 16.dp)),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(R.string.registro_titulo),
                style = MaterialTheme.typography.displaySmall,
            )
            Text(
                text = stringResource(R.string.registro_subtitulo),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            CampoFormulario(
                etiqueta = stringResource(R.string.campo_correo),
                valor = uiState.correo,
                onValorChange = onCambiarCorreo,
                ejemplo = stringResource(R.string.campo_correo_ejemplo),
            )
            CampoFormulario(
                etiqueta = stringResource(R.string.campo_contrasena),
                valor = uiState.contrasena,
                onValorChange = onCambiarContrasena,
                esContrasena = true,
                mensaje = stringResource(
                    if (uiState.contrasenaValida) R.string.registro_contrasena_valida
                    else R.string.registro_minimo_caracteres
                ),
                colorMensaje = if (uiState.contrasenaValida) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            CampoFormulario(
                etiqueta = stringResource(R.string.registro_repetir_contrasena),
                valor = uiState.repetirContrasena,
                onValorChange = onCambiarRepetirContrasena,
                esContrasena = true,
                esError = uiState.noCoinciden,
                mensaje = if (uiState.noCoinciden) stringResource(R.string.registro_no_coinciden) else null,
                colorMensaje = MaterialTheme.colorScheme.error,
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            uiState.error?.let { MensajeError(error = it) }
            BotonPrincipal(
                texto = stringResource(R.string.registro_crear_cuenta),
                onClick = onCrearCuenta,
                habilitado = uiState.puedeCrear,
                cargando = uiState.cargando,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun RegistroPreview() {
    ApprestauranteTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            RegistroContenido(
                uiState = RegistroUiState(correo = "carmen@correo.com", contrasena = "sazon2026", repetirContrasena = "sazon"),
                onVolver = {},
                onCambiarCorreo = {},
                onCambiarContrasena = {},
                onCambiarRepetirContrasena = {},
                onCrearCuenta = {},
            )
        }
    }
}
