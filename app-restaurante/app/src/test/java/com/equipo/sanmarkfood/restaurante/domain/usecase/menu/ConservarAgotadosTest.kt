package com.equipo.sanmarkfood.restaurante.domain.usecase.menu

import com.equipo.sanmarkfood.restaurante.domain.model.menu.OpcionMenu
import org.junit.Assert.assertEquals
import org.junit.Test

class ConservarAgotadosTest {

    @Test
    fun unaOpcionAgotadaSigueAgotadaSiConservaSuNombre() {
        val anteriores = listOf(OpcionMenu("Ají de gallina", agotado = false), OpcionMenu("Arroz con pollo", agotado = true))
        val nuevas = listOf(
            OpcionMenu("Arroz con pollo", agotado = false),
            OpcionMenu("Tacu tacu", agotado = false),
        )
        assertEquals(
            listOf(OpcionMenu("Arroz con pollo", agotado = true), OpcionMenu("Tacu tacu", agotado = false)),
            conservarAgotados(nuevas, anteriores),
        )
    }
}
