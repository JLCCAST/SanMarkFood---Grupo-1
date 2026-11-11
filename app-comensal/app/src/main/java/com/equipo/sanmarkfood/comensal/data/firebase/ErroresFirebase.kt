package com.equipo.sanmarkfood.comensal.data.firebase

import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ErrorPedidos
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException

internal fun Throwable.aErrorPedidos(): ErrorPedidos = when {
    this is ErrorPedidos -> this
    this is FirebaseNetworkException -> ErrorPedidos.SinConexion
    this is FirebaseFirestoreException && code == FirebaseFirestoreException.Code.UNAVAILABLE -> ErrorPedidos.SinConexion
    else -> ErrorPedidos.Desconocido
}