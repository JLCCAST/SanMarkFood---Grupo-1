package com.equipo.sanmarkfood.restaurante.domain.usecase.admin

import com.equipo.sanmarkfood.restaurante.domain.model.admin.DetalleSolicitud
import com.equipo.sanmarkfood.restaurante.domain.model.admin.ErrorAdmin
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.MotivoRechazo
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Rechazo
import com.equipo.sanmarkfood.restaurante.domain.repository.RevisionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RevisionSolicitudUseCasesTest {

    private class RepositorioFalso : RevisionRepository {
        var aprobado: String? = null
        var rechazado: Pair<String, Rechazo>? = null
        override fun observar(uid: String): Flow<DetalleSolicitud?> = emptyFlow()
        override suspend fun aprobar(uid: String) {
            aprobado = uid
        }
        override suspend fun rechazar(uid: String, rechazo: Rechazo) {
            rechazado = uid to rechazo
        }
    }

    private val repositorio = RepositorioFalso()
    private val aprobar = AprobarLocalUseCase(repositorio)
    private val rechazar = RechazarLocalUseCase(repositorio)

    private fun errorAlRechazar(motivos: Set<MotivoRechazo>, detalle: String): ErrorAdmin? = try {
        runBlocking { rechazar(UID, motivos, detalle) }
        null
    } catch (e: ErrorAdmin) {
        e
    }

    @Test
    fun apruebaElLocalElegido() {
        runBlocking { aprobar(UID) }
        assertEquals(UID, repositorio.aprobado)
    }

    @Test
    fun noRechazaSinMotivos() {
        assertEquals(ErrorAdmin.MotivoFaltante, errorAlRechazar(emptySet(), "La dirección no existe"))
        assertNull(repositorio.rechazado)
    }

    @Test
    fun rechazaConVariosMotivosALaVez() {
        val motivos = setOf(MotivoRechazo.DATOS_INCOMPLETOS, MotivoRechazo.DIRECCION_NO_VERIFICABLE)
        runBlocking { rechazar(UID, motivos, "") }
        assertEquals(UID to Rechazo(motivos, null), repositorio.rechazado)
    }

    @Test
    fun conOtroElDetalleEsObligatorioAunqueHayaOtrosMotivos() {
        listOf(setOf(MotivoRechazo.OTRO), setOf(MotivoRechazo.LOCAL_DUPLICADO, MotivoRechazo.OTRO)).forEach { motivos ->
            listOf("", "   ", "ab").forEach { detalle ->
                assertEquals(ErrorAdmin.DetalleObligatorio, errorAlRechazar(motivos, detalle))
            }
        }
        assertNull(repositorio.rechazado)
    }

    @Test
    fun conOtroEnviaElDetalleSinEspaciosDeMas() {
        val motivos = setOf(MotivoRechazo.DATOS_INCOMPLETOS, MotivoRechazo.OTRO)
        runBlocking { rechazar(UID, motivos, "  La portada no es del local  ") }
        assertEquals(UID to Rechazo(motivos, "La portada no es del local"), repositorio.rechazado)
    }

    @Test
    fun sinOtroElDetalleEsOpcional() {
        runBlocking { rechazar(UID, setOf(MotivoRechazo.DIRECCION_NO_VERIFICABLE), "   ") }
        assertEquals(UID to Rechazo(setOf(MotivoRechazo.DIRECCION_NO_VERIFICABLE), null), repositorio.rechazado)
    }

    @Test
    fun elDetalleSeRecortaAlMaximo() {
        val detalle = "a".repeat(RechazarLocalUseCase.MAX_DETALLE + 50)
        runBlocking { rechazar(UID, setOf(MotivoRechazo.DATOS_INCOMPLETOS), detalle) }
        assertEquals(RechazarLocalUseCase.MAX_DETALLE, repositorio.rechazado!!.second.detalle!!.length)
    }

    private companion object {
        const val UID = "local-123"
    }
}
