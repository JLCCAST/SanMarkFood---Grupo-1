package com.equipo.sanmarkfood.comensal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.equipo.sanmarkfood.comensal.ui.auth.AuthViewModel
import com.equipo.sanmarkfood.comensal.ui.auth.LoginScreen
import com.equipo.sanmarkfood.comensal.ui.auth.RegisterScreen
import com.equipo.sanmarkfood.comensal.ui.auth.EmailVerificationScreen
import com.equipo.sanmarkfood.comensal.ui.theme.AppcomensalTheme

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
private fun AuthGate(viewModel: AuthViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    var showRegister by rememberSaveable { mutableStateOf(false) }

    when {
        state.isLoggedIn -> LoggedInPlaceholder(onLogout = viewModel::logout)

        state.needsVerification -> EmailVerificationScreen(
            state = state,
            onCheckVerified = viewModel::checkVerified,
            onResend = viewModel::resendVerification,
            onChangeEmail = viewModel::changeEmail
        )

        showRegister -> RegisterScreen(
            state = state,
            onRegister = viewModel::register,
            onGoToLogin = {
                viewModel.clearError()
                showRegister = false
            }
        )

        else -> LoginScreen(
            state = state,
            onLogin = viewModel::login,
            onGoToRegister = {
                viewModel.clearError()
                showRegister = true
            },
            onSendPasswordReset = viewModel::sendPasswordReset,
            onDismissReset = viewModel::clearReset
        )
    }
}

// Temporal: sirve para probar el flujo hasta que exista la pantalla de inicio.
@Composable
private fun LoggedInPlaceholder(onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sesión iniciada",
            style = MaterialTheme.typography.headlineSmall
        )
        OutlinedButton(onClick = onLogout, modifier = Modifier.padding(top = 16.dp)) {
            Text("Cerrar sesión")
        }
    }
}