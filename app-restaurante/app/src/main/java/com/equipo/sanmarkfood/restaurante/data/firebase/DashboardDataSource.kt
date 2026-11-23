package com.equipo.sanmarkfood.restaurante.data.firebase

import com.equipo.sanmarkfood.restaurante.domain.model.dashboard.CifrasVentas
import com.google.firebase.firestore.AggregateField
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class DashboardDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    suspend fun ventasEntre(uid: String, desde: Date, hasta: Date): CifrasVentas = llamarFirebaseRestaurante {
        val total = AggregateField.sum("total")
        val foto = delLocal("pedidos", uid, "entregado", desde, hasta)
            .aggregate(AggregateField.count(), total)
            .get(AggregateSource.SERVER)
            .await()
        CifrasVentas(pedidos = foto.count.toInt(), centimos = foto.getLong(total) ?: 0L)
    }

    suspend fun reservasConfirmadasEntre(uid: String, desde: Date, hasta: Date): Int = llamarFirebaseRestaurante {
        delLocal("reservas", uid, "confirmada", desde, hasta)
            .count()
            .get(AggregateSource.SERVER)
            .await()
            .count
            .toInt()
    }

    suspend fun promedioDeResenasHasta(uid: String, hasta: Date): Double? = llamarFirebaseRestaurante {
        val estrellas = AggregateField.average("estrellas")
        firestore.collection("restaurantes").document(uid).collection("resenas")
            .whereLessThan("creadoEn", hasta)
            .aggregate(estrellas)
            .get(AggregateSource.SERVER)
            .await()
            .getDouble(estrellas)
    }

    private fun delLocal(coleccion: String, uid: String, estado: String, desde: Date, hasta: Date): Query =
        firestore.collection(coleccion)
            .whereEqualTo("restauranteUid", uid)
            .whereEqualTo("estado", estado)
            .whereGreaterThanOrEqualTo("creadoEn", desde)
            .whereLessThan("creadoEn", hasta)
}
