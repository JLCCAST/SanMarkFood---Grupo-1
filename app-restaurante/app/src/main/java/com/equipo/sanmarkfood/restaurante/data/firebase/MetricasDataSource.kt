package com.equipo.sanmarkfood.restaurante.data.firebase

import com.equipo.sanmarkfood.restaurante.domain.model.admin.LocalAprobado
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.EstadoRestaurante
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import java.util.Date
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

    suspend fun contarPedidos(desde: Date, hasta: Date): Int = contar(
        firestore.collection("pedidos")
            .whereGreaterThanOrEqualTo("creadoEn", desde)
            .whereLessThan("creadoEn", hasta)
    )

    suspend fun listarAprobados(): List<LocalAprobado> = llamarFirebaseAdmin {
        restaurantes()
            .whereEqualTo("estado", EstadoRestaurante.APROBADO.valor())
            .get(Source.SERVER)
            .await()
            .documents
            .map { LocalAprobado(uid = it.id, nombre = it.getString("nombre").orEmpty()) }
    }

    suspend fun contarPedidosDeLocal(uid: String, desde: Date, hasta: Date): Int = contar(
        firestore.collection("pedidos")
            .whereEqualTo("restauranteUid", uid)
            .whereGreaterThanOrEqualTo("creadoEn", desde)
            .whereLessThan("creadoEn", hasta)
    )

    private suspend fun contar(consulta: Query): Int = llamarFirebaseAdmin {
        consulta.count().get(AggregateSource.SERVER).await().count.toInt()
    }
}
