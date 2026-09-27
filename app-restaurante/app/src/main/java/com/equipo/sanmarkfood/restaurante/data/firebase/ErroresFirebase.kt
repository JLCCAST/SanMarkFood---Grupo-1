package com.equipo.sanmarkfood.restaurante.data.firebase

import com.equipo.sanmarkfood.restaurante.domain.model.ErrorAuth
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException

/** Ejecuta una llamada a Firebase y traduce sus excepciones. */
internal suspend fun <T> llamarFirebase(llamada: suspend () -> T): T =
    try {
        llamada()
    } catch (e: FirebaseException) {
        throw e.aErrorAuth()
    }

private fun FirebaseException.aErrorAuth(): ErrorAuth = when (this) {
    is FirebaseNetworkException -> ErrorAuth.SinConexion
    is FirebaseTooManyRequestsException -> ErrorAuth.DemasiadosIntentos
    is FirebaseAuthUserCollisionException -> ErrorAuth.CorreoYaRegistrado
    is FirebaseAuthException -> when (errorCode) {
        "ERROR_INVALID_EMAIL" -> ErrorAuth.CorreoInvalido
        "ERROR_INVALID_CREDENTIAL", "ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND" -> ErrorAuth.CredencialesInvalidas
        else -> ErrorAuth.Desconocido
    }
    else -> ErrorAuth.Desconocido
}
