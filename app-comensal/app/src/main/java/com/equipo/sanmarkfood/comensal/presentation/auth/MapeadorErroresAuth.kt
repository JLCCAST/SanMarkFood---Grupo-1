package com.equipo.sanmarkfood.comensal.presentation.auth

import androidx.annotation.StringRes
import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.domain.model.auth.CuentaDeOtroRolException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

/**
 * Traduce las excepciones de Firebase Auth a mensajes para el usuario.
 * Compartido por los ViewModels de inicio de sesión, registro,
 * verificación y recuperación de contraseña.
 */
object MapeadorErroresAuth {

    @StringRes
    fun mapearError(e: Throwable): Int = when (e) {
        is CuentaDeOtroRolException -> R.string.error_cuenta_otro_rol
        is FirebaseAuthUserCollisionException -> R.string.error_correo_en_uso
        is FirebaseAuthWeakPasswordException -> R.string.error_contrasena_debil
        is FirebaseAuthInvalidUserException,
        is FirebaseAuthInvalidCredentialsException -> R.string.error_credenciales_incorrectas
        is FirebaseTooManyRequestsException -> R.string.error_demasiados_intentos
        is FirebaseNetworkException -> R.string.error_sin_conexion
        else -> R.string.error_generico
    }

    /** Mensajes propios de la recuperación (no valen los de "contraseña incorrecta"). */
    @StringRes
    fun mapearErrorRecuperacion(e: Throwable): Int = when (e) {
        is FirebaseAuthInvalidUserException -> R.string.error_recuperar_correo_no_encontrado
        is FirebaseAuthInvalidCredentialsException -> R.string.error_correo_invalido
        else -> mapearError(e)
    }
}