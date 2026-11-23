package com.equipo.sanmarkfood.restaurante.domain.usecase.dashboard

import com.equipo.sanmarkfood.restaurante.domain.model.dashboard.CifrasVentas
import com.equipo.sanmarkfood.restaurante.domain.model.dashboard.PeriodoNegocio
import com.equipo.sanmarkfood.restaurante.domain.model.dashboard.ResumenNegocio
import com.equipo.sanmarkfood.restaurante.domain.model.dashboard.TramoDePedidos
import com.equipo.sanmarkfood.restaurante.domain.model.menu.inicioDeSemana
import com.equipo.sanmarkfood.restaurante.domain.repository.DashboardRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.Date
import javax.inject.Inject

class ObtenerResumenNegocioUseCase @Inject constructor(
    private val dashboardRepository: DashboardRepository,
) {
    suspend operator fun invoke(periodo: PeriodoNegocio, ahora: Date = Date()): ResumenNegocio = coroutineScope {
        val inicios = iniciosDeTramos(periodo, ahora)
        val duracionTramo = if (periodo == PeriodoNegocio.SEMANA) UN_DIA else UNA_SEMANA
        val desplazamiento = if (periodo == PeriodoNegocio.SEMANA) UNA_SEMANA else SEMANAS_DEL_MES * UNA_SEMANA
        val desde = inicios.first()
        val desdeAntes = Date(desde.time - desplazamiento)
        val hastaAntes = Date(ahora.time - desplazamiento)

        val cifrasPorTramo = inicios.map { inicio ->
            async {
                if (inicio.before(ahora)) {
                    dashboardRepository.ventasEntre(inicio, minOf(Date(inicio.time + duracionTramo), ahora))
                } else {
                    CifrasVentas.Cero
                }
            }
        }
        val anterior = async { dashboardRepository.ventasEntre(desdeAntes, hastaAntes) }
        val reservas = async { dashboardRepository.reservasConfirmadasEntre(desde, ahora) }
        val reservasAnterior = async { dashboardRepository.reservasConfirmadasEntre(desdeAntes, hastaAntes) }
        val platosMasPedidos = async { dashboardRepository.platosMasPedidos(desde, ahora) }
        val cifras = cifrasPorTramo.awaitAll()

        ResumenNegocio(
            periodo = periodo,
            actual = cifras.fold(CifrasVentas.Cero, CifrasVentas::plus),
            anterior = anterior.await(),
            reservas = reservas.await(),
            reservasAnterior = reservasAnterior.await(),
            tramos = inicios.zip(cifras) { inicio, cifrasDelTramo -> TramoDePedidos(inicio, cifrasDelTramo.pedidos) },
            tramoActual = inicios.indexOfLast { !it.after(ahora) },
            platosMasPedidos = platosMasPedidos.await(),
        )
    }

    private fun iniciosDeTramos(periodo: PeriodoNegocio, ahora: Date): List<Date> = when (periodo) {
        PeriodoNegocio.SEMANA -> {
            val lunes = inicioDeSemana(ahora)
            (0 until DIAS_DE_LA_SEMANA).map { Date(lunes.time + it * UN_DIA) }
        }
        PeriodoNegocio.MES -> (SEMANAS_DEL_MES - 1 downTo 0).map { inicioDeSemana(ahora, it) }
    }

    private companion object {
        const val UN_DIA = 24L * 60 * 60 * 1000
        const val UNA_SEMANA = 7 * UN_DIA
        const val DIAS_DE_LA_SEMANA = 7
        const val SEMANAS_DEL_MES = 4
    }
}
