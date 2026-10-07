package com.equipo.sanmarkfood.comensal.domain.model.auth

/**
 * Errores de autenticación del comensal. Kotlin puro: no depende de Android ni de Firebase.
 * La traducción de las excepciones de Firebase se hace en AuthRepositoryImpl y el texto
 * que se muestra se elige en presentation (ErrorAuth.mensaje()).
 */
sealed class ErrorAuth : Exception() {
    // Los mismos casos que usa app-restaurante.
    data object SinConexion : ErrorAuth()
    data object CamposVacios : ErrorAuth()
    data object CredencialesInvalidas : ErrorAuth()
    data object CorreoYaRegistrado : ErrorAuth()
    data object CorreoInvalido : ErrorAuth()
    data object CorreoSinVerificar : ErrorAuth()
    data object DemasiadosIntentos : ErrorAuth()
    data object Desconocido : ErrorAuth()

    // Propios de comensal.
    /** La cuenta existe pero tiene otro rol (D5): reemplaza a CuentaDeOtroRolException. */
    data object CuentaDeOtroRol : ErrorAuth()
    data object ContrasenaDebil : ErrorAuth()
    data object ContrasenaCorta : ErrorAuth()
    data object ContrasenaSinNumero : ErrorAuth()
    data object NombreVacio : ErrorAuth()
    data object TerminosNoAceptados : ErrorAuth()
    data object CorreoVacio : ErrorAuth()
    data object CorreoNoRegistrado : ErrorAuth()
}