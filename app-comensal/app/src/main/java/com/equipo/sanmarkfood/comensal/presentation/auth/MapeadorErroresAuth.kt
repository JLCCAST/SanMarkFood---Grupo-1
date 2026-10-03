package com.equipo.sanmarkfood.comensal.presentation.auth

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

    fun mapearError(e: Throwable): String = when (e) {
        is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con ese correo"
        is FirebaseAuthWeakPasswordException -> "La contraseña es muy débil"
        is FirebaseAuthInvalidUserException,
        is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos"
        is FirebaseTooManyRequestsException -> "Demasiados intentos. Espera un momento e inténtalo de nuevo"
        is FirebaseNetworkException -> "Sin conexión a internet"
        else -> "Ocurrió un error. Inténtalo de nuevo"
    }

    /** Mensajes propios de la recuperación (no valen los de "contraseña incorrecta"). */
    fun mapearErrorRecuperacion(e: Throwable): String = when (e) {
        is FirebaseAuthInvalidUserException -> "No encontramos una cuenta con ese correo"
        is FirebaseAuthInvalidCredentialsException -> "Correo no válido"
        else -> mapearError(e)
    }
}