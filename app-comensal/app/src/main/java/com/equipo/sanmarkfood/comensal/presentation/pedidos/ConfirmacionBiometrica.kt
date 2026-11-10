package com.equipo.sanmarkfood.comensal.presentation.pedidos

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

enum class ResultadoBiometria { CONFIRMADO, CANCELADO }

data class TextosBiometria(
    val titulo: String,
    val subtitulo: String,
    val cancelar: String,
)

fun confirmarConBiometria(
    actividad: FragmentActivity,
    textos: TextosBiometria,
    alTerminar: (ResultadoBiometria) -> Unit,
) {
    if (BiometricManager.from(actividad).canAuthenticate(BIOMETRIC_WEAK) != BiometricManager.BIOMETRIC_SUCCESS) {
        alTerminar(ResultadoBiometria.CANCELADO)
        return
    }
    val prompt = BiometricPrompt(
        actividad,
        ContextCompat.getMainExecutor(actividad),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                alTerminar(ResultadoBiometria.CONFIRMADO)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                alTerminar(ResultadoBiometria.CANCELADO)
            }
        }
    )
    prompt.authenticate(
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(textos.titulo)
            .setSubtitle(textos.subtitulo)
            .setNegativeButtonText(textos.cancelar)
            .setAllowedAuthenticators(BIOMETRIC_WEAK)
            .setConfirmationRequired(false)
            .build()
    )
}
