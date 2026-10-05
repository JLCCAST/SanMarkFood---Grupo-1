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

    private fun errorAlRechazar(motivo: MotivoRechazo?, detalle: String): ErrorAdmin? = try {
        runBlocking { rechazar(UID, motivo, detalle) }
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
    fun noRechazaSinMotivo() {
        assertEquals(ErrorAdmin.MotivoFaltante, errorAlRechazar(null, "La dirección no existe"))
        assertNull(repositorio.rechazado)
    }

    @Test
    fun conOtroElDetalleEsObligatorio() {
        listOf("", "   ", "ab").forEach { detalle ->
            assertEquals(ErrorAdmin.DetalleObligatorio, errorAlRechazar(MotivoRechazo.OTRO, detalle))
        }
        assertNull(repositorio.rechazado)
    }

    @Test
    fun conOtroEnviaElDetalleSinEspaciosDeMas() {
        runBlocking { rechazar(UID, MotivoRechazo.OTRO, "  La portada no es del local  ") }
        assertEquals(UID to Rechazo(MotivoRechazo.OTRO, "La portada no es del local"), repositorio.rechazado)
    }

    @Test
    fun conLosDemasMotivosElDetalleEsOpcional() {
        runBlocking { rechazar(UID, MotivoRechazo.DIRECCION_NO_VERIFICABLE, "   ") }
        assertEquals(UID to Rechazo(MotivoRechazo.DIRECCION_NO_VERIFICABLE, null), repositorio.rechazado)
    }

    @Test
    fun elDetalleSeRecortaAlMaximo() {
        runBlocking { rechazar(UID, MotivoRechazo.DATOS_INCOMPLETOS, "a".repeat(RechazarLocalUseCase.MAX_DETALLE + 50)) }
        assertEquals(RechazarLocalUseCase.MAX_DETALLE, repositorio.rechazado!!.second.detalle!!.length)
    }

    private companion object {
        const val UID = "local-123"
    }
}
