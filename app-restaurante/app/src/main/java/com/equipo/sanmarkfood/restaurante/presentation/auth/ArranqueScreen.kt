package com.equipo.sanmarkfood.restaurante.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.auth.EstadoSesion

@Composable
fun ArranqueScreen(
    onSesionLeida: (EstadoSesion) -> Unit,
    viewModel: ArranqueViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.sesion) {
        uiState.sesion?.let(onSesionLeida)
    }

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val error = uiState.error
        if (error == null) {
            CircularProgressIndicator()
        } else {
            MensajeError(error = error)
            BotonPrincipal(texto = stringResource(R.string.arranque_reintentar), onClick = viewModel::onReintentar)
        }
    }
}
