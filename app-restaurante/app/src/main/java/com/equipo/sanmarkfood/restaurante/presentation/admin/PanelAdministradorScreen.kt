package com.equipo.sanmarkfood.restaurante.presentation.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.ui.theme.ApprestauranteTheme

@Composable
fun PanelAdministradorScreen(
    onSesionCerrada: () -> Unit,
    viewModel: PanelAdministradorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.sesionCerrada) {
        if (uiState.sesionCerrada) onSesionCerrada()
    }

    PanelAdministradorContenido(onCerrarSesion = viewModel::onCerrarSesion)
}

@Composable
private fun PanelAdministradorContenido(onCerrarSesion: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = stringResource(R.string.admin_titulo),
            style = MaterialTheme.typography.displaySmall,
        )
        OutlinedButton(onClick = onCerrarSesion) {
            Text(text = stringResource(R.string.admin_cerrar_sesion), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PanelAdministradorPreview() {
    ApprestauranteTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PanelAdministradorContenido(onCerrarSesion = {})
        }
    }
}
