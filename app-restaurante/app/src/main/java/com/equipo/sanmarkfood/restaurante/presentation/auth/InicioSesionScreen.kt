package com.equipo.sanmarkfood.restaurante.presentation.auth

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.ErrorAuth
import com.equipo.sanmarkfood.restaurante.domain.model.EstadoSesion
import com.equipo.sanmarkfood.restaurante.ui.theme.ApprestauranteTheme

@Composable
fun InicioSesionScreen(
    onSesionIniciada: (EstadoSesion) -> Unit,
    onRegistrarse: () -> Unit,
    viewModel: InicioSesionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.sesion) {
        uiState.sesion?.let(onSesionIniciada)
    }

    BarraDeEstadoSobreCabecera()
    InicioSesionContenido(
        uiState = uiState,
        onCambiarCorreo = viewModel::onCambiarCorreo,
        onCambiarContrasena = viewModel::onCambiarContrasena,
        onIniciarSesion = viewModel::onIniciarSesion,
        onRegistrarse = onRegistrarse,
    )
}

@Composable
private fun InicioSesionContenido(
    uiState: InicioSesionUiState,
    onCambiarCorreo: (String) -> Unit,
    onCambiarContrasena: (String) -> Unit,
    onIniciarSesion: () -> Unit,
    onRegistrarse: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().navigationBarsPadding().imePadding()) {
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            CabeceraMarca()
            Column(
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(R.string.login_titulo),
                    style = MaterialTheme.typography.titleLarge,
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
                )
                BotonPrincipal(
                    texto = stringResource(R.string.login_iniciar_sesion),
                    onClick = onIniciarSesion,
                    cargando = uiState.cargando,
                )
                uiState.error?.let { MensajeError(error = it) }
                Text(
                    text = stringResource(R.string.login_sesion_guardada),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.login_sin_local),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onRegistrarse) {
                Text(text = stringResource(R.string.login_registralo), style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun CabeceraMarca() {
    val marca = stringResource(R.string.login_marca)
    val acento = stringResource(R.string.login_marca_acento)
    val colorAcento = MaterialTheme.colorScheme.secondary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(272.dp)
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(MaterialTheme.colorScheme.onSurface)
            .padding(start = 24.dp, end = 24.dp, bottom = 26.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Bottom),
    ) {
        Text(
            text = stringResource(R.string.login_etiqueta),
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.secondary)
                .padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Text(
            text = buildAnnotatedString {
                append("$marca ")
                withStyle(SpanStyle(color = colorAcento)) { append(acento) }
            },
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.surface,
        )
        Text(
            text = stringResource(R.string.login_lema),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

@Composable
private fun BarraDeEstadoSobreCabecera() {
    val vista = LocalView.current
    val temaOscuro = isSystemInDarkTheme()

    DisposableEffect(temaOscuro) {
        val barras = WindowCompat.getInsetsController((vista.context as Activity).window, vista)
        barras.isAppearanceLightStatusBars = temaOscuro
        onDispose { barras.isAppearanceLightStatusBars = !temaOscuro }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun InicioSesionPreview() {
    ApprestauranteTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            InicioSesionContenido(
                uiState = InicioSesionUiState(correo = "carmen@correo.com", error = ErrorAuth.CredencialesInvalidas),
                onCambiarCorreo = {},
                onCambiarContrasena = {},
                onIniciarSesion = {},
                onRegistrarse = {},
            )
        }
    }
}
