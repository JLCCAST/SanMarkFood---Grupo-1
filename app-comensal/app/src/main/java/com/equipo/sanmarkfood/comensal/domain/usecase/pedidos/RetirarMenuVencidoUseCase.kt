package com.equipo.sanmarkfood.comensal.domain.usecase.pedidos

import com.equipo.sanmarkfood.comensal.domain.model.pedidos.DisponibilidadMenu
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MenuDelCarrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MenuRetirado
import com.equipo.sanmarkfood.comensal.domain.repository.CarritoRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

class RetirarMenuVencidoUseCase @Inject constructor(
    private val carritoRepository: CarritoRepository,
    private val observarDisponibilidadMenu: ObservarDisponibilidadMenuUseCase,
) {
    suspend operator fun invoke(): MenuRetirado {
        val carrito = carritoRepository.leerCarrito() ?: return MenuRetirado(restauranteNombre = "", horaFin = null)
        carrito.items.filterIsInstance<MenuDelCarrito>().forEach { carritoRepository.quitar(it.clave) }
        val disponibilidad = withTimeoutOrNull(ESPERA_MAXIMA) {
            observarDisponibilidadMenu(carrito.restauranteId).catch { }.firstOrNull()
        }
        return MenuRetirado(
            restauranteNombre = carrito.restauranteNombre,
            horaFin = (disponibilidad as? DisponibilidadMenu.Terminado)?.horaFin,
        )
    }

    private companion object {
        const val ESPERA_MAXIMA = 3_000L
    }
}
