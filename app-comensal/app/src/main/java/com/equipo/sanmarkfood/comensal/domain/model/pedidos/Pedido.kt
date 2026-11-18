package com.equipo.sanmarkfood.comensal.domain.model.pedidos

enum class EstadoPedido { RECIBIDO, PREPARANDO, LISTO, EN_CAMINO, ENTREGADO, RECHAZADO, CANCELADO }

enum class EstadoPago { PAGADO, POR_COBRAR, REVERTIDO }

enum class MotivoRechazo { PLATO_AGOTADO, COCINA_SATURADA, LOCAL_POR_CERRAR, OTRO, SIN_RESPUESTA }

data class RechazoPedido(
    val motivo: MotivoRechazo,
    val detalle: String?,
    val platoAgotado: String?,
    val automatico: Boolean,
)

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
    val restauranteId: String,
    val restauranteNombre: String,
    val modalidad: Modalidad,
    val direccion: String?,
    val metodoPago: MetodoPago,
    val pago: EstadoPago,
    val items: List<ItemPedido>,
    val total: Int,
    val estado: EstadoPedido,
    val codigoRecojo: String?,
    val rechazo: RechazoPedido?,
    val creadoEn: Long?,
    val aceptadoEn: Long?,
    val listoAprox: Long?,
    val entregadoEn: Long?,
    val actualizadoEn: Long? = null,
) {
    val enCurso: Boolean
        get() = estado != EstadoPedido.ENTREGADO &&
                estado != EstadoPedido.RECHAZADO &&
                estado != EstadoPedido.CANCELADO

    fun seAgoto(item: ItemPedido): Boolean {
        val agotado = rechazo?.takeIf { it.motivo == MotivoRechazo.PLATO_AGOTADO }?.platoAgotado ?: return false
        return item.nombre == agotado || item.entrada == agotado || item.segundo == agotado
    }
}
