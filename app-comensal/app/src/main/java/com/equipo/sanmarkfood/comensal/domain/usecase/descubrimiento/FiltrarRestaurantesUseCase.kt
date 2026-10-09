package com.equipo.sanmarkfood.comensal.domain.usecase.descubrimiento

import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.FiltroDescubrimiento
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.Restaurante
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.estaAbiertoAhora
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.menuVigente
import javax.inject.Inject

/** Filtra sobre la lista ya traída (D7): nada de volver a consultar Firestore. */
class FiltrarRestaurantesUseCase @Inject constructor() {

    operator fun invoke(lista: List<Restaurante>, filtro: FiltroDescubrimiento): List<Restaurante> =
        lista.filter {
            coincideCategoria(it, filtro) &&
                    coincidePrecio(it, filtro) &&
                    coincideCalificacion(it, filtro) &&
                    coincideMenuHoy(it, filtro) &&
                    coincideAbiertoAhora(it, filtro)
        }

    private fun coincideCategoria(restaurante: Restaurante, filtro: FiltroDescubrimiento): Boolean =
        filtro.categoria == null || restaurante.categoria == filtro.categoria

    private fun coincidePrecio(restaurante: Restaurante, filtro: FiltroDescubrimiento): Boolean {
        if (filtro.precioMinimo == null && filtro.precioMaximo == null) return true

        val precioMenuVigente = restaurante.menuVigente()?.precio

        val coincideMenu = precioMenuVigente != null && enRango(precioMenuVigente, filtro)
        val coincideCarta = restaurante.rangoCarta?.let { seCruzaConRango(it.min, it.max, filtro) } ?: false

        return coincideMenu || coincideCarta
    }

    private fun enRango(precio: Int, filtro: FiltroDescubrimiento): Boolean =
        (filtro.precioMinimo == null || precio >= filtro.precioMinimo) &&
                (filtro.precioMaximo == null || precio <= filtro.precioMaximo)

    private fun seCruzaConRango(min: Int, max: Int, filtro: FiltroDescubrimiento): Boolean {
        val desde = filtro.precioMinimo ?: Int.MIN_VALUE
        val hasta = filtro.precioMaximo ?: Int.MAX_VALUE
        return min <= hasta && max >= desde
    }

    private fun coincideCalificacion(restaurante: Restaurante, filtro: FiltroDescubrimiento): Boolean =
        filtro.calificacionMinima == null ||
                (restaurante.calificacionPromedio != null && restaurante.calificacionPromedio >= filtro.calificacionMinima)

    /** «Con menú hoy»: el local tiene un menú de hoy publicado. */
    private fun coincideMenuHoy(restaurante: Restaurante, filtro: FiltroDescubrimiento): Boolean =
        !filtro.soloConMenuHoy || restaurante.menuVigente() != null

    /** «Abierto ahora»: según el `horario` del local, en hora de Lima. */
    private fun coincideAbiertoAhora(restaurante: Restaurante, filtro: FiltroDescubrimiento): Boolean =
        !filtro.soloAbiertoAhora || restaurante.estaAbiertoAhora()
}