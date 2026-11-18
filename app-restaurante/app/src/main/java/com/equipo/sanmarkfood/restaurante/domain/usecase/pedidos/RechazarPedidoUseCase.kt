package com.equipo.sanmarkfood.restaurante.domain.usecase.pedidos

import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.OpcionMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.TipoOpcion
import com.equipo.sanmarkfood.restaurante.domain.model.menu.fechaDeHoy
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.Agotado
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.ErrorPedidos
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.EstadoPedido
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.MotivoRechazo
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.Pedido
import com.equipo.sanmarkfood.restaurante.domain.repository.MenuRepository
import com.equipo.sanmarkfood.restaurante.domain.repository.PedidosRepository
import javax.inject.Inject

class RechazarPedidoUseCase @Inject constructor(
    private val pedidosRepository: PedidosRepository,
    private val menuRepository: MenuRepository,
) {
    suspend operator fun invoke(pedido: Pedido, motivo: MotivoRechazo, detalle: String, agotados: Set<Agotado>) {
        if (pedido.estado != EstadoPedido.RECIBIDO) throw ErrorPedidos.YaCambio
        val texto = detalle.trim().ifEmpty { null }
        if (motivo == MotivoRechazo.OTRO && (texto == null || texto.length < 3)) throw ErrorPedidos.Desconocido
        if (motivo == MotivoRechazo.PLATO_AGOTADO && agotados.isEmpty()) throw ErrorPedidos.Desconocido

        val platoAgotado = if (motivo == MotivoRechazo.PLATO_AGOTADO) agotados.joinToString(", ") { it.nombre } else null
        pedidosRepository.rechazar(pedido.id, motivo, texto.takeIf { motivo == MotivoRechazo.OTRO }, platoAgotado)
        if (motivo == MotivoRechazo.PLATO_AGOTADO) marcarAgotados(agotados)
    }

    private suspend fun marcarAgotados(agotados: Set<Agotado>) {
        try {
            agotados.filterIsInstance<Agotado.Plato>().forEach { menuRepository.cambiarAgotadoPlato(it.platoId, fechaDeHoy()) }
            val opciones = agotados.filterIsInstance<Agotado.Opcion>()
            if (opciones.isEmpty()) return
            val menu = menuRepository.leerMenu(fechaDeHoy()) ?: return
            menuRepository.guardarOpcionesMenu(
                menu.fecha,
                marcar(menu.datos.entradas, TipoOpcion.ENTRADA, opciones),
                marcar(menu.datos.segundos, TipoOpcion.SEGUNDO, opciones),
            )
        } catch (e: ErrorMenu) {
        }
    }

    private fun marcar(opciones: List<OpcionMenu>, tipo: TipoOpcion, agotadas: List<Agotado.Opcion>): List<OpcionMenu> =
        opciones.map { opcion -> if (Agotado.Opcion(tipo, opcion.nombre) in agotadas) opcion.copy(agotado = true) else opcion }
}
