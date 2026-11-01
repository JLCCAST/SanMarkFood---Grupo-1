package com.equipo.sanmarkfood.restaurante.data.firebase

import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.EstadoRestaurante
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MetricasDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun restaurantes() = firestore.collection("restaurantes")

    suspend fun contarRestaurantes(estado: EstadoRestaurante): Int =
        contar(restaurantes().whereEqualTo("estado", estado.valor()))

    suspend fun contarReenviados(): Int = contar(
        restaurantes()
            .whereEqualTo("estado", EstadoRestaurante.PENDIENTE.valor())
            .whereEqualTo("reenviado", true)
    )

    suspend fun contarResenasReportadas(): Int = contar(firestore.collectionGroup("reportes"))

    private suspend fun contar(consulta: Query): Int = llamarFirebaseAdmin {
        consulta.count().get(AggregateSource.SERVER).await().count.toInt()
    }
}
