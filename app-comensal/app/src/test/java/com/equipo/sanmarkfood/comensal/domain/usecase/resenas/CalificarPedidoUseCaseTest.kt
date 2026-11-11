package com.equipo.sanmarkfood.comensal.domain.usecase.resenas

import com.equipo.sanmarkfood.comensal.domain.model.resenas.Calificacion
import com.equipo.sanmarkfood.comensal.domain.model.resenas.ErrorCalificar
import com.equipo.sanmarkfood.comensal.domain.repository.ResenasRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test

class CalificarPedidoUseCaseTest {

    private class ResenasFalso : ResenasRepository {
        val enviadas = mutableListOf<Calificacion>()

        override suspend fun calificar(calificacion: Calificacion) {
            enviadas += calificacion
        }
    }

    private val repositorio = ResenasFalso()
    private val calificar = CalificarPedidoUseCase(repositorio)

    private suspend fun esperarError(bloque: suspend () -> Unit): ErrorCalificar {
        try {
            bloque()
        } catch (e: ErrorCalificar) {
            return e
        }
        fail("Se esperaba un error")
        throw IllegalStateException()
    }

    @Test
    fun enviaLasEstrellasElComentarioYLosVotos() = runBlocking {
        calificar("pedido-1", 4, "  Llegó caliente y rápido  ", mapOf("lomo" to true))

        assertEquals(
            listOf(Calificacion("pedido-1", 4, "Llegó caliente y rápido", mapOf("lomo" to true))),
            repositorio.enviadas,
        )
    }

    @Test
    fun unComentarioVacioNoSeEnvia() = runBlocking {
        calificar("pedido-1", 5, "   ", emptyMap())

        assertNull(repositorio.enviadas.single().comentario)
    }

    @Test
    fun exigeDeUnaACincoEstrellas() = runBlocking {
        assertEquals(ErrorCalificar.DatosInvalidos, esperarError { calificar("pedido-1", 0, "", emptyMap()) })
        assertEquals(ErrorCalificar.DatosInvalidos, esperarError { calificar("pedido-1", 6, "", emptyMap()) })
        assertEquals(emptyList<Calificacion>(), repositorio.enviadas)
    }

    @Test
    fun rechazaUnComentarioDeMasDeQuinientosCaracteres() = runBlocking {
        val largo = "a".repeat(Calificacion.MAX_COMENTARIO + 1)

        assertEquals(ErrorCalificar.DatosInvalidos, esperarError { calificar("pedido-1", 3, largo, emptyMap()) })
    }
}
