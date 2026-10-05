package com.equipo.sanmarkfood.restaurante.presentation.auth

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.equipo.sanmarkfood.restaurante.domain.model.auth.ErrorAuth
import com.equipo.sanmarkfood.restaurante.domain.model.auth.EstadoSesion
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
        onAbrirRecuperacion = viewModel::onAbrirRecuperacion,
        onRegistrarse = onRegistrarse,
    )
    uiState.recuperacion?.let { recuperacion ->
        PanelRecuperacion(
            recuperacion = recuperacion,
            correo = uiState.correo,
            onCambiarCorreo = viewModel::onCambiarCorreo,
            onEnviarEnlace = viewModel::onEnviarEnlace,
            onCerrar = viewModel::onCerrarRecuperacion,
        )
    }
}

@Composable
private fun InicioSesionContenido(
    uiState: InicioSesionUiState,
    onCambiarCorreo: (String) -> Unit,
    onCambiarContrasena: (String) -> Unit,
    onIniciarSesion: () -> Unit,
    onAbrirRecuperacion: () -> Unit,
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
                Column {
                    CampoFormulario(
                        etiqueta = stringResource(R.string.campo_contrasena),
                        valor = uiState.contrasena,
                        onValorChange = onCambiarContrasena,
                        esContrasena = true,
                    )
                    TextButton(
                        onClick = onAbrirRecuperacion,
                        modifier = Modifier.align(Alignment.End),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text(text = stringResource(R.string.login_olvidaste), style = MaterialTheme.typography.labelLarge)
                    }
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PanelRecuperacion(
    recuperacion: RecuperacionUiState,
    correo: String,
    onCambiarCorreo: (String) -> Unit,
    onEnviarEnlace: () -> Unit,
    onCerrar: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outline) },
    ) {
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.recuperar_titulo),
                style = MaterialTheme.typography.headlineSmall,
            )
            if (recuperacion.enviado) {
                Text(
                    text = stringResource(R.string.recuperar_listo),
                    style = MaterialTheme.typography.bodyLarge,
                )
                BotonPrincipal(texto = stringResource(R.string.recuperar_entendido), onClick = onCerrar)
            } else {
                Text(
                    text = stringResource(R.string.recuperar_explicacion),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                CampoFormulario(
                    etiqueta = stringResource(R.string.recuperar_campo_correo),
                    valor = correo,
                    onValorChange = onCambiarCorreo,
                    ejemplo = stringResource(R.string.campo_correo_ejemplo),
                )
                recuperacion.error?.let { MensajeError(error = it) }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onCerrar, modifier = Modifier.height(52.dp)) {
                        Text(text = stringResource(R.string.recuperar_cancelar), style = MaterialTheme.typography.labelLarge)
                    }
                    BotonPrincipal(
                        texto = stringResource(R.string.recuperar_enviar),
                        onClick = onEnviarEnlace,
                        modifier = Modifier.weight(1f),
                        habilitado = correo.isNotBlank(),
                        cargando = recuperacion.enviando,
                    )
                }
            }
        }
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
                onAbrirRecuperacion = {},
                onRegistrarse = {},
            )
        }
    }
}
