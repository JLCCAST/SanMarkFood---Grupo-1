package com.equipo.sanmarkfood.comensal.presentation.pedidos

import android.app.KeyguardManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

enum class ResultadoBiometria { CONFIRMADO, CANCELADO, BLOQUEADO, NO_DISPONIBLE }

enum class Verificacion { BIOMETRIA, BLOQUEO_DE_PANTALLA }

data class TextosBiometria(
    val titulo: String,
    val subtitulo: String,
    val cancelar: String,
)

fun confirmarConBiometria(
    actividad: FragmentActivity,
    textos: TextosBiometria,
    verificacion: Verificacion,
    alTerminar: (ResultadoBiometria) -> Unit,
) {
    val conBloqueo = verificacion == Verificacion.BLOQUEO_DE_PANTALLA
    if (conBloqueo && !tieneBloqueoDePantalla(actividad)) {
        alTerminar(ResultadoBiometria.NO_DISPONIBLE)
        return
    }
    if (!conBloqueo &&
        BiometricManager.from(actividad).canAuthenticate(BIOMETRIC_WEAK) != BiometricManager.BIOMETRIC_SUCCESS
    ) {
        alTerminar(ResultadoBiometria.NO_DISPONIBLE)
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
                alTerminar(
                    when (errorCode) {
                        BiometricPrompt.ERROR_LOCKOUT,
                        BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> ResultadoBiometria.BLOQUEADO
                        BiometricPrompt.ERROR_NO_BIOMETRICS,
                        BiometricPrompt.ERROR_HW_NOT_PRESENT,
                        BiometricPrompt.ERROR_HW_UNAVAILABLE,
                        BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> ResultadoBiometria.NO_DISPONIBLE
                        else -> ResultadoBiometria.CANCELADO
                    }
                )
            }
        }
    )
    val datos = BiometricPrompt.PromptInfo.Builder()
        .setTitle(textos.titulo)
        .setSubtitle(textos.subtitulo)
        .setConfirmationRequired(false)
    if (conBloqueo) {
        datos.setAllowedAuthenticators(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
    } else {
        datos.setAllowedAuthenticators(BIOMETRIC_WEAK).setNegativeButtonText(textos.cancelar)
    }
    prompt.authenticate(datos.build())
}

fun tieneBloqueoDePantalla(context: Context): Boolean =
    context.getSystemService(KeyguardManager::class.java)?.isDeviceSecure == true

fun abrirAjustesDeBiometria(context: Context) {
    val ajustes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Intent(Settings.ACTION_BIOMETRIC_ENROLL)
            .putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, BIOMETRIC_WEAK)
    } else {
        Intent(Settings.ACTION_SECURITY_SETTINGS)
    }
    try {
        context.startActivity(ajustes)
    } catch (e: ActivityNotFoundException) {
        context.startActivity(Intent(Settings.ACTION_SETTINGS))
    }
}
