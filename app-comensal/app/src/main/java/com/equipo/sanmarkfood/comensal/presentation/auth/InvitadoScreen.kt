package com.equipo.sanmarkfood.comensal.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.equipo.sanmarkfood.comensal.R

// Temporal: se reemplazará por el mapa, los menús y las reseñas cuando existan.
@Composable
fun InvitadoScreen(
    onCrearCuenta: () -> Unit,
    onIniciarSesion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.invitado_titulo),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.invitado_texto),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Button(onClick = onCrearCuenta, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.invitado_crear_cuenta))
        }
        OutlinedButton(onClick = onIniciarSesion, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.invitado_iniciar_sesion))
        }
    }
}
