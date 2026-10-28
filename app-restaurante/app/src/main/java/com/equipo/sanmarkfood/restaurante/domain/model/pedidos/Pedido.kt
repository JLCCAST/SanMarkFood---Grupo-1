package com.equipo.sanmarkfood.restaurante.domain.model.pedidos

import com.equipo.sanmarkfood.restaurante.domain.model.menu.TipoOpcion
import java.util.Date

val TIEMPOS_ESTIMADOS = listOf(10, 15, 20, 30)

enum class EstadoPedido { RECIBIDO, PREPARANDO, LISTO, EN_CAMINO, ENTREGADO, RECHAZADO, CANCELADO }

enum class Modalidad { RECOJO, DELIVERY }

enum class MetodoPago { EFECTIVO, BILLETERA, TARJETA }

enum class EstadoPago { PAGADO, POR_COBRAR, REVERTIDO }

enum class MotivoRechazo { PLATO_AGOTADO, COCINA_SATURADA, LOCAL_POR_CERRAR, OTRO }

sealed interface Agotado {
    val nombre: String

    data class Plato(val platoId: String, override val nombre: String) : Agotado

    data class Opcion(val tipo: TipoOpcion, override val nombre: String) : Agotado
}

data class ItemPedido(
    val cantidad: Int,
    val nombre: String,
    val precio: Int,
    val platoId: String? = null,
    val entrada: String? = null,
    val segundo: String? = null,
)

data class Pedido(
    val id: String,
    val numero: Int,
    val comensalNombre: String,
    val modalidad: Modalidad,
    val direccion: String?,
    val metodoPago: MetodoPago,
    val pago: EstadoPago,
    val items: List<ItemPedido>,
    val total: Int,
    val estado: EstadoPedido,
    val creadoEn: Date,
    val listoAprox: Date?,
)

fun Pedido.agotables(): List<Agotado> = items.flatMap { item ->
    when {
        item.platoId != null -> listOf(Agotado.Plato(item.platoId, item.nombre))
        item.entrada != null && item.segundo != null -> listOf(
            Agotado.Opcion(TipoOpcion.ENTRADA, item.entrada),
            Agotado.Opcion(TipoOpcion.SEGUNDO, item.segundo),
        )
        else -> emptyList()
    }
}.distinct()

data class PedidosDeHoy(
    val pedidos: List<Pedido>,
    val sinConexion: Boolean,
)
