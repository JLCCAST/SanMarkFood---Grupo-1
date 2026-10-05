package com.equipo.sanmarkfood.restaurante.data.firebase

import com.equipo.sanmarkfood.restaurante.domain.model.auth.ErrorAuth
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.CampoLocal
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageException

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
    is FirebaseFirestoreException ->
        if (code == FirebaseFirestoreException.Code.UNAVAILABLE) ErrorAuth.SinConexion else ErrorAuth.Desconocido
    is FirebaseAuthException -> when (errorCode) {
        "ERROR_INVALID_EMAIL" -> ErrorAuth.CorreoInvalido
        "ERROR_INVALID_CREDENTIAL", "ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND" -> ErrorAuth.CredencialesInvalidas
        else -> ErrorAuth.Desconocido
    }
    else -> ErrorAuth.Desconocido
}

/** Como [llamarFirebase], para los datos del local (HU02). */
internal suspend fun <T> llamarFirebaseRestaurante(llamada: suspend () -> T): T =
    try {
        llamada()
    } catch (e: FirebaseException) {
        throw e.aErrorRestaurante()
    }

internal fun FirebaseException.aErrorRestaurante(): ErrorRestaurante = when {
    this is FirebaseNetworkException -> ErrorRestaurante.SinConexion
    this is FirebaseFirestoreException && code == FirebaseFirestoreException.Code.UNAVAILABLE -> ErrorRestaurante.SinConexion
    // Storage no falla al instante sin conexión: reintenta hasta el límite de FirebaseModule.
    this is StorageException && errorCode == StorageException.ERROR_RETRY_LIMIT_EXCEEDED -> ErrorRestaurante.SinConexion
    else -> ErrorRestaurante.Desconocido
}

internal suspend fun <T> llamarFirebaseVerificacion(llamada: suspend () -> T): T =
    try {
        llamada()
    } catch (e: FirebaseException) {
        throw e.aErrorVerificacion()
    }

internal fun FirebaseException.aErrorVerificacion(): ErrorRestaurante = when {
    this is FirebaseTooManyRequestsException -> ErrorRestaurante.DemasiadosIntentos
    this is FirebaseAuthUserCollisionException -> ErrorRestaurante.TelefonoEnUso
    this is FirebaseAuthInvalidCredentialsException -> when (errorCode) {
        "ERROR_INVALID_PHONE_NUMBER" -> ErrorRestaurante.DatosInvalidos(setOf(CampoLocal.TELEFONO))
        "ERROR_INVALID_VERIFICATION_CODE" -> ErrorRestaurante.CodigoIncorrecto
        "ERROR_SESSION_EXPIRED", "ERROR_INVALID_VERIFICATION_ID" -> ErrorRestaurante.CodigoVencido
        else -> ErrorRestaurante.Desconocido
    }
    else -> aErrorRestaurante()
}

internal suspend fun <T> llamarFirebaseMenu(llamada: suspend () -> T): T =
    try {
        llamada()
    } catch (e: FirebaseException) {
        throw e.aErrorMenu()
    }

internal fun FirebaseException.aErrorMenu(): ErrorMenu = when {
    this is FirebaseNetworkException -> ErrorMenu.SinConexion
    this is FirebaseFirestoreException && code == FirebaseFirestoreException.Code.UNAVAILABLE -> ErrorMenu.SinConexion
    this is StorageException && errorCode == StorageException.ERROR_RETRY_LIMIT_EXCEEDED -> ErrorMenu.SinConexion
    else -> ErrorMenu.Desconocido
}
