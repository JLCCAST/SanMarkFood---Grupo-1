package com.equipo.sanmarkfood.restaurante.domain.usecase.admin

import com.equipo.sanmarkfood.restaurante.domain.model.admin.FiltroSolicitudes
import com.equipo.sanmarkfood.restaurante.domain.model.admin.SolicitudLocal
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.CategoriaRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.EstadoRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.MotivoRechazo
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Rechazo
import com.equipo.sanmarkfood.restaurante.domain.repository.SolicitudesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ObservarSolicitudesUseCaseTest {

    private class RepositorioFalso(private val solicitudes: List<SolicitudLocal>) : SolicitudesRepository {
        var estadoPedido: EstadoRestaurante? = null
        override fun observar(estado: EstadoRestaurante): Flow<List<SolicitudLocal>> {
            estadoPedido = estado
            return flowOf(solicitudes)
        }
    }

    private fun solicitud(
        uid: String,
        enviadoEn: Long = 0L,
        revisadoEn: Long? = null,
        rechazoAnterior: Rechazo? = null,
    ) = SolicitudLocal(
        uid = uid,
        nombre = "Local $uid",
        categoria = CategoriaRestaurante.CRIOLLA,
        logoUrl = null,
        estado = EstadoRestaurante.PENDIENTE,
        enviadoEn = enviadoEn,
        revisadoEn = revisadoEn,
        reenviado = rechazoAnterior != null,
        rechazoAnterior = rechazoAnterior,
        cantidadPlatos = 0,
    )

    private fun observar(filtro: FiltroSolicitudes, repositorio: RepositorioFalso): List<SolicitudLocal> = runBlocking {
        ObservarSolicitudesUseCase(repositorio)(filtro).first()
    }

    @Test
    fun cadaFiltroPideSuEstado() {
        mapOf(
            FiltroSolicitudes.PENDIENTES to EstadoRestaurante.PENDIENTE,
            FiltroSolicitudes.APROBADAS to EstadoRestaurante.APROBADO,
            FiltroSolicitudes.RECHAZADAS to EstadoRestaurante.RECHAZADO,
        ).forEach { (filtro, estado) ->
            val repositorio = RepositorioFalso(emptyList())
            observar(filtro, repositorio)
            assertEquals(estado, repositorio.estadoPedido)
        }
    }

    @Test
    fun lasPendientesVanDeLaMasRecienteALaMasAntigua() {
        val ordenadas = observar(
            FiltroSolicitudes.PENDIENTES,
            RepositorioFalso(
                listOf(
                    solicitud("hace-un-dia", enviadoEn = 1_000L),
                    solicitud("hace-20-min", enviadoEn = 3_000L),
                    solicitud("hace-2-h", enviadoEn = 2_000L),
                )
            ),
        )
        assertEquals(listOf("hace-20-min", "hace-2-h", "hace-un-dia"), ordenadas.map { it.uid })
    }

    @Test
    fun lasRevisadasVanDeLaUltimaDecisionALaPrimera() {
        listOf(FiltroSolicitudes.APROBADAS, FiltroSolicitudes.RECHAZADAS).forEach { filtro ->
            val ordenadas = observar(
                filtro,
                RepositorioFalso(
                    listOf(
                        solicitud("sin-fecha", enviadoEn = 9_000L, revisadoEn = null),
                        solicitud("ayer", enviadoEn = 1_000L, revisadoEn = 2_000L),
                        solicitud("hoy", enviadoEn = 500L, revisadoEn = 5_000L),
                    )
                ),
            )
            assertEquals(listOf("hoy", "ayer", "sin-fecha"), ordenadas.map { it.uid })
        }
    }

    @Test
    fun conservaElReenvioYElRechazoAnterior() {
        val rechazo = Rechazo(setOf(MotivoRechazo.DIRECCION_NO_VERIFICABLE), detalle = null)
        val reenviada = observar(
            FiltroSolicitudes.PENDIENTES,
            RepositorioFalso(listOf(solicitud("reenviada", rechazoAnterior = rechazo))),
        ).single()
        assertTrue(reenviada.reenviado)
        assertEquals(rechazo, reenviada.rechazoAnterior)
    }

    @Test
    fun sinSolicitudesEmiteUnaListaVacia() {
        assertEquals(emptyList<SolicitudLocal>(), observar(FiltroSolicitudes.PENDIENTES, RepositorioFalso(emptyList())))
    }
}
