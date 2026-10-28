package com.equipo.sanmarkfood.restaurante.data.firebase

import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.ErrorPedidos
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.ItemPedido
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.Pedido
import com.google.firebase.firestore.DocumentSnapshot

internal fun DocumentSnapshot.aPedido(): Pedido = Pedido(
    id = id,
    numero = getLong("numero")?.toInt() ?: throw ErrorPedidos.Desconocido,
    comensalNombre = getString("comensalNombre").orEmpty(),
    modalidad = opcion("modalidad"),
    direccion = getString("direccion.texto"),
    metodoPago = opcion("metodoPago"),
    pago = opcion("pago"),
    items = (get("items") as? List<*>).orEmpty().map { it.aItemPedido() },
    total = getLong("total")?.toInt() ?: throw ErrorPedidos.Desconocido,
    estado = opcion("estado"),
    creadoEn = getTimestamp("creadoEn")?.toDate() ?: throw ErrorPedidos.Desconocido,
    listoAprox = getTimestamp("listoAprox")?.toDate(),
)

private fun Any?.aItemPedido(): ItemPedido {
    val campos = this as? Map<*, *> ?: throw ErrorPedidos.Desconocido
    return ItemPedido(
        cantidad = (campos["cantidad"] as? Number)?.toInt() ?: throw ErrorPedidos.Desconocido,
        nombre = campos["nombre"] as? String ?: throw ErrorPedidos.Desconocido,
        precio = (campos["precio"] as? Number)?.toInt() ?: throw ErrorPedidos.Desconocido,
        platoId = campos["platoId"] as? String,
        entrada = campos["entrada"] as? String,
        segundo = campos["segundo"] as? String,
    )
}

private inline fun <reified T : Enum<T>> DocumentSnapshot.opcion(campo: String): T {
    val valor = getString(campo)
    return enumValues<T>().firstOrNull { it.name.lowercase() == valor } ?: throw ErrorPedidos.Desconocido
}
