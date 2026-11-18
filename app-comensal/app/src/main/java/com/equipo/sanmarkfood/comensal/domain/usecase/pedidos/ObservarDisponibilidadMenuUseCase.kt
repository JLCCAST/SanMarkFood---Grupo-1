package com.equipo.sanmarkfood.comensal.domain.usecase.pedidos

import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.horaActualLima
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.DisponibilidadMenu
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.disponibilidadDelMenu
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ObservarDisponibilidadMenuUseCase @Inject constructor(
    private val observarMenuDeHoy: ObservarMenuDeHoyUseCase,
) {
    operator fun invoke(restauranteId: String): Flow<DisponibilidadMenu> =
        combine(observarMenuDeHoy(restauranteId), horaPorMinuto()) { menu, hora ->
            disponibilidadDelMenu(menu, hora)
        }.distinctUntilChanged()

    private fun horaPorMinuto(): Flow<String> = flow {
        while (true) {
            emit(horaActualLima())
            delay(UN_MINUTO)
        }
    }.distinctUntilChanged()

    private companion object {
        const val UN_MINUTO = 60_000L
    }
}
