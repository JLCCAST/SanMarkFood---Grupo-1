package com.equipo.sanmarkfood.comensal.data.firebase

import com.equipo.sanmarkfood.comensal.domain.model.pedidos.EstadoPago
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.EstadoPedido
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ItemPedido
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MetodoPago
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Modalidad
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MotivoRechazo
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Pedido
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.RechazoPedido
import com.google.firebase.firestore.DocumentSnapshot

internal fun DocumentSnapshot.aPedido(): Pedido? {
    val rechazo = get("rechazo") as? Map<*, *>
    return Pedido(
        id = id,
        numero = getLong("numero")?.toInt() ?: return null,
        restauranteId = getString("restauranteUid") ?: return null,
        restauranteNombre = getString("restauranteNombre").orEmpty(),
        modalidad = opcion<Modalidad>("modalidad") ?: return null,
        direccion = getString("direccion.texto"),
        metodoPago = opcion<MetodoPago>("metodoPago") ?: return null,
        pago = opcion<EstadoPago>("pago") ?: return null,
        items = (get("items") as? List<*>).orEmpty().mapNotNull { it.aItemPedido() },
        total = getLong("total")?.toInt() ?: return null,
        estado = opcion<EstadoPedido>("estado") ?: return null,
        codigoRecojo = getString("codigoRecojo"),
        rechazo = rechazo?.aRechazo(),
        creadoEn = getTimestamp("creadoEn")?.toDate()?.time,
        aceptadoEn = getTimestamp("aceptadoEn")?.toDate()?.time,
        listoAprox = getTimestamp("listoAprox")?.toDate()?.time,
        entregadoEn = getTimestamp("entregadoEn")?.toDate()?.time,
    )
}

private fun Any?.aItemPedido(): ItemPedido? {
    val campos = this as? Map<*, *> ?: return null
    return ItemPedido(
        cantidad = (campos["cantidad"] as? Number)?.toInt() ?: return null,
        nombre = campos["nombre"] as? String ?: return null,
        precio = (campos["precio"] as? Number)?.toInt() ?: return null,
        platoId = campos["platoId"] as? String,
        entrada = campos["entrada"] as? String,
        segundo = campos["segundo"] as? String,
    )
}

private fun Map<*, *>.aRechazo(): RechazoPedido? {
    val motivo = this["motivo"] as? String
    return RechazoPedido(
        motivo = MotivoRechazo.entries.firstOrNull { it.name.lowercase() == motivo } ?: return null,
        detalle = (this["detalle"] as? String)?.takeIf { it.isNotBlank() },
        platoAgotado = (this["platoAgotado"] as? String)?.takeIf { it.isNotBlank() },
        automatico = this["automatico"] as? Boolean ?: false,
    )
}

private inline fun <reified T : Enum<T>> DocumentSnapshot.opcion(campo: String): T? {
    val valor = getString(campo)
    return enumValues<T>().firstOrNull { it.name.lowercase() == valor }
}
