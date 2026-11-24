package com.equipo.sanmarkfood.restaurante.domain.usecase.menu

import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.PlatoLeido
import com.equipo.sanmarkfood.restaurante.domain.repository.LecturaCartaRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class LeerCartaUseCaseTest {

    private class RepositorioFalso(private val platos: List<PlatoLeido>) : LecturaCartaRepository {
        override suspend fun leerCarta(foto: String): List<PlatoLeido> = platos
        override fun ultimaLectura(): List<PlatoLeido> = platos
    }

    @Test
    fun devuelveCuantosPlatosLeyo() {
        val leer = LeerCartaUseCase(RepositorioFalso(listOf(PlatoLeido("Causa", "12.00", "Entradas"))))
        assertEquals(1, runBlocking { leer("foto") })
    }

    @Test(expected = ErrorMenu.CartaSinPlatos::class)
    fun avisaSiLaFotoNoTienePlatos() {
        runBlocking { LeerCartaUseCase(RepositorioFalso(emptyList()))("foto") }
    }
}
