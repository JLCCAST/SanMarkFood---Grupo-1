package com.equipo.sanmarkfood.restaurante.presentation.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.ui.theme.ApprestauranteTheme

/**
 * O6 «Tu negocio». Por ahora solo tiene la sección «Tu local» (HU02): los indicadores y gráficos
 * llegan con HU13, y la barra inferior con R5 (SCRUM-65).
 */
@Composable
fun NegocioScreen(
    onEditarPerfil: () -> Unit,
    onEditarHorario: () -> Unit,
    onSesionCerrada: () -> Unit,
    viewModel: NegocioViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.sesionCerrada) {
        if (uiState.sesionCerrada) onSesionCerrada()
    }

    NegocioContenido(
        onEditarPerfil = onEditarPerfil,
        onEditarHorario = onEditarHorario,
        onCerrarSesion = viewModel::onCerrarSesion,
    )
}

@Composable
private fun NegocioContenido(
    onEditarPerfil: () -> Unit,
    onEditarHorario: () -> Unit,
    onCerrarSesion: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = stringResource(R.string.negocio_titulo), style = MaterialTheme.typography.displaySmall)

        Column {
            Text(
                text = stringResource(R.string.negocio_tu_local),
                modifier = Modifier.padding(bottom = 4.dp),
                style = MaterialTheme.typography.titleSmall,
            )
            FilaEnlace(
                titulo = stringResource(R.string.negocio_perfil),
                ayuda = stringResource(R.string.negocio_perfil_ayuda),
                onClick = onEditarPerfil,
            )
            FilaEnlace(
                titulo = stringResource(R.string.negocio_horario),
                ayuda = stringResource(R.string.negocio_horario_ayuda),
                onClick = onEditarHorario,
            )
            FilaEnlace(
                titulo = stringResource(R.string.negocio_cerrar_sesion),
                ayuda = stringResource(R.string.negocio_cerrar_sesion_ayuda),
                onClick = onCerrarSesion,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun FilaEnlace(
    titulo: String,
    ayuda: String,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f).padding(vertical = 8.dp)) {
            Text(text = titulo, style = MaterialTheme.typography.labelLarge, color = color)
            Text(
                text = ayuda,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            painter = painterResource(R.drawable.ic_siguiente),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = color,
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun NegocioPreview() {
    ApprestauranteTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            NegocioContenido(onEditarPerfil = {}, onEditarHorario = {}, onCerrarSesion = {})
        }
    }
}
