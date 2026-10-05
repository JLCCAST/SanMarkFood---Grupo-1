package com.equipo.sanmarkfood.restaurante.domain.usecase.admin

import com.equipo.sanmarkfood.restaurante.domain.model.admin.SolicitudLocal
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.CategoriaRestaurante
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

class ObservarSolicitudesPendientesUseCaseTest {

    private class RepositorioFalso(private val pendientes: List<SolicitudLocal>) : SolicitudesRepository {
        override fun observarPendientes(): Flow<List<SolicitudLocal>> = flowOf(pendientes)
    }

    private fun solicitud(
        uid: String,
        enviadoEn: Long,
        rechazoAnterior: Rechazo? = null,
    ) = SolicitudLocal(
        uid = uid,
        nombre = "Local $uid",
        categoria = CategoriaRestaurante.CRIOLLA,
        logoUrl = null,
        enviadoEn = enviadoEn,
        reenviado = rechazoAnterior != null,
        rechazoAnterior = rechazoAnterior,
        cantidadPlatos = 0,
    )

    private fun observar(pendientes: List<SolicitudLocal>): List<SolicitudLocal> = runBlocking {
        ObservarSolicitudesPendientesUseCase(RepositorioFalso(pendientes))().first()
    }

    @Test
    fun ordenaDeLaMasRecienteALaMasAntigua() {
        val ordenadas = observar(
            listOf(
                solicitud("hace-un-dia", enviadoEn = 1_000L),
                solicitud("hace-20-min", enviadoEn = 3_000L),
                solicitud("hace-2-h", enviadoEn = 2_000L),
            )
        )
        assertEquals(listOf("hace-20-min", "hace-2-h", "hace-un-dia"), ordenadas.map { it.uid })
    }

    @Test
    fun conservaElReenvioYElRechazoAnterior() {
        val rechazo = Rechazo(MotivoRechazo.DIRECCION_NO_VERIFICABLE, detalle = null)
        val reenviada = observar(listOf(solicitud("reenviada", enviadoEn = 1_000L, rechazoAnterior = rechazo))).single()
        assertTrue(reenviada.reenviado)
        assertEquals(rechazo, reenviada.rechazoAnterior)
    }

    @Test
    fun sinSolicitudesEmiteUnaListaVacia() {
        assertEquals(emptyList<SolicitudLocal>(), observar(emptyList()))
    }
}
