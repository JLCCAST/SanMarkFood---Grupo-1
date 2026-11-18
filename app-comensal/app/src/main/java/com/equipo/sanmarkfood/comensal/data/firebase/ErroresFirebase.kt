package com.equipo.sanmarkfood.comensal.data.firebase

import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ErrorPedidos
import com.equipo.sanmarkfood.comensal.domain.model.reservas.ErrorReservas
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException

internal fun Throwable.aErrorPedidos(): ErrorPedidos = when {
    this is ErrorPedidos -> this
    this is FirebaseNetworkException -> ErrorPedidos.SinConexion
    this is FirebaseFirestoreException && code == FirebaseFirestoreException.Code.UNAVAILABLE -> ErrorPedidos.SinConexion
    else -> ErrorPedidos.Desconocido
}

internal fun Throwable.aErrorReservas(): ErrorReservas = when {
    this is ErrorReservas -> this
    this is FirebaseNetworkException -> ErrorReservas.SinConexion
    this is FirebaseFirestoreException && code == FirebaseFirestoreException.Code.UNAVAILABLE -> ErrorReservas.SinConexion
    else -> ErrorReservas.Desconocido
}
