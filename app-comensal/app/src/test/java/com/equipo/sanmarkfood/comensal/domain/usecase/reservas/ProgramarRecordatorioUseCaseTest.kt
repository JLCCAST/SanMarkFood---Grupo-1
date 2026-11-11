package com.equipo.sanmarkfood.comensal.domain.usecase.reservas

import com.equipo.sanmarkfood.comensal.domain.model.reservas.AnticipacionRecordatorio
import com.equipo.sanmarkfood.comensal.domain.model.reservas.Recordatorio
import com.equipo.sanmarkfood.comensal.domain.model.reservas.momentoDeReserva
import com.equipo.sanmarkfood.comensal.domain.repository.RecordatoriosRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgramarRecordatorioUseCaseTest {

    private class RecordatoriosFalsos : RecordatoriosRepository {
        val programados = mutableListOf<Pair<Recordatorio, Long>>()
        val cancelados = mutableListOf<String>()

        override fun programar(recordatorio: Recordatorio, demoraMilis: Long) {
            programados += recordatorio to demoraMilis
        }

        override fun cancelar(reservaId: String) {
            cancelados += reservaId
        }

        override fun observarRecordatorios(): Flow<Map<String, AnticipacionRecordatorio>> = emptyFlow()
    }

    private val ahora = momentoDeReserva("2026-09-17", "10:00")!!

    @Test
    fun `programa el aviso la anticipacion elegida antes de la hora`() {
        val repositorio = RecordatoriosFalsos()

        val programado = ProgramarRecordatorioUseCase(repositorio)(
            "r1", "Verde Campus", "2026-09-17", "13:00", AnticipacionRecordatorio.UNA_HORA, ahora,
        )

        assertTrue(programado)
        assertEquals(2 * 3_600_000L, repositorio.programados.single().second)
    }

    @Test
    fun `sin recordatorio cancela el que hubiera`() {
        val repositorio = RecordatoriosFalsos()

        val programado = ProgramarRecordatorioUseCase(repositorio)("r1", "Verde Campus", "2026-09-17", "13:00", null, ahora)

        assertFalse(programado)
        assertEquals(listOf("r1"), repositorio.cancelados)
        assertTrue(repositorio.programados.isEmpty())
    }

    @Test
    fun `no programa un aviso que ya habria pasado`() {
        val repositorio = RecordatoriosFalsos()

        val programado = ProgramarRecordatorioUseCase(repositorio)(
            "r1", "Verde Campus", "2026-09-17", "11:30", AnticipacionRecordatorio.DOS_HORAS, ahora,
        )

        assertFalse(programado)
        assertTrue(repositorio.programados.isEmpty())
    }
}
