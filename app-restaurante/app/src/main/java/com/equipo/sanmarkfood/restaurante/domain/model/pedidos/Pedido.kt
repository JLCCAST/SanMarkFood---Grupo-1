package com.equipo.sanmarkfood.restaurante.domain.model.pedidos

import java.util.Date

val TIEMPOS_ESTIMADOS = listOf(10, 15, 20, 30)

enum class EstadoPedido { RECIBIDO, PREPARANDO, LISTO, EN_CAMINO, ENTREGADO, RECHAZADO, CANCELADO }

enum class Modalidad { RECOJO, DELIVERY }

enum class MetodoPago { EFECTIVO, BILLETERA, TARJETA }

enum class EstadoPago { PAGADO, POR_COBRAR, REVERTIDO }

data class ItemPedido(
    val cantidad: Int,
    val nombre: String,
    val precio: Int,
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

data class PedidosDeHoy(
    val pedidos: List<Pedido>,
    val sinConexion: Boolean,
)
