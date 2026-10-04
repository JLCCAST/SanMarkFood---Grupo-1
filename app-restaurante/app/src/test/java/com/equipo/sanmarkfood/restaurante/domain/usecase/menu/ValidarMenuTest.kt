package com.equipo.sanmarkfood.restaurante.domain.usecase.menu

import com.equipo.sanmarkfood.restaurante.domain.model.menu.CampoMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.DatosMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.OpcionMenu
import org.junit.Assert.assertEquals
import org.junit.Test

class ValidarMenuTest {

    private fun camposInvalidos(precio: String, entradas: List<String>, segundos: List<String>) =
        try {
            validarMenu(precio, entradas, segundos, "", "", DatosMenu.HORA_FIN_POR_DEFECTO)
            emptySet()
        } catch (e: ErrorMenu.DatosMenuInvalidos) {
            e.campos
        }

    @Test
    fun armaElMenuConLasOpcionesDisponibles() {
        val menu = validarMenu("14", listOf(" Causa limeña "), listOf("Ají de gallina"), " Chicha morada ", "", "15:00")
        assertEquals(
            DatosMenu(
                precio = 1400,
                entradas = listOf(OpcionMenu("Causa limeña", agotado = false)),
                segundos = listOf(OpcionMenu("Ají de gallina", agotado = false)),
                refresco = "Chicha morada",
                postre = null,
                horaFin = "15:00",
            ),
            menu,
        )
    }

    @Test
    fun pideElPrecioUnaEntradaYUnSegundo() {
        assertEquals(
            setOf(CampoMenu.PRECIO, CampoMenu.ENTRADAS, CampoMenu.SEGUNDOS),
            camposInvalidos(precio = "", entradas = emptyList(), segundos = listOf("   ")),
        )
    }
}
