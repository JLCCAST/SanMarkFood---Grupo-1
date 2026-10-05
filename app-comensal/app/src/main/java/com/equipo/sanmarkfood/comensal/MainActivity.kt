package com.equipo.sanmarkfood.comensal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.equipo.sanmarkfood.comensal.presentation.auth.EstadoSesion
import com.equipo.sanmarkfood.comensal.presentation.auth.InicioSesionScreen
import com.equipo.sanmarkfood.comensal.presentation.auth.RegistroScreen
import com.equipo.sanmarkfood.comensal.presentation.auth.SesionViewModel
import com.equipo.sanmarkfood.comensal.presentation.auth.VerificacionCorreoScreen
import com.equipo.sanmarkfood.comensal.ui.home.HomeScaffold
import com.equipo.sanmarkfood.comensal.ui.theme.AppcomensalTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppcomensalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AuthGate()
                }
            }
        }
    }
}

@Composable
private fun AuthGate(viewModel: SesionViewModel = hiltViewModel()) {
    val sesion by viewModel.estado.collectAsState()
    var mostrarRegistro by rememberSaveable { mutableStateOf(false) }

    when (val actual = sesion) {
        EstadoSesion.Autenticado -> HomeScaffold(onLogout = viewModel::cerrarSesion)

        is EstadoSesion.PorVerificar -> VerificacionCorreoScreen(correo = actual.correo)

        EstadoSesion.Invitado -> InvitadoPlaceholder(
            onCrearCuenta = {
                viewModel.salirDeInvitado()
                mostrarRegistro = true
            },
            onIniciarSesion = {
                viewModel.salirDeInvitado()
                mostrarRegistro = false
            }
        )

        EstadoSesion.SinSesion -> if (mostrarRegistro) {
            RegistroScreen(onIrAInicioSesion = { mostrarRegistro = false })
        } else {
            InicioSesionScreen(onIrARegistro = { mostrarRegistro = true })
        }
    }
}

// Temporal: se reemplazará por el mapa, los menús y las reseñas cuando existan.
@Composable
private fun InvitadoPlaceholder(
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