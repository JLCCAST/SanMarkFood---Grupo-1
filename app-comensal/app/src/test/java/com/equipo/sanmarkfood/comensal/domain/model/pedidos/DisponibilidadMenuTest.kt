package com.equipo.sanmarkfood.comensal.domain.model.pedidos

import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.EstadoMenuDelDia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DisponibilidadMenuTest {

    private fun menu(
        entradas: List<OpcionMenu> = listOf(OpcionMenu("Causa limeña", agotado = false)),
        segundos: List<OpcionMenu> = listOf(OpcionMenu("Ají de gallina", agotado = false)),
        estado: EstadoMenuDelDia = EstadoMenuDelDia.PUBLICADO,
    ) = MenuDeHoy(
        fecha = "2026-11-09",
        precio = 1400,
        entradas = entradas,
        segundos = segundos,
        refresco = "Chicha morada",
        postre = "Mazamorra morada",
        horaFin = "15:00",
        estado = estado,
    )

    @Test
    fun sinMenuDeHoyQuedaPorPublicar() {
        assertEquals(DisponibilidadMenu.SinPublicar, disponibilidadDelMenu(null, "12:00"))
    }

    @Test
    fun antesDeLaHoraDeFinSePuedePedir() {
        val menu = menu()
        assertEquals(DisponibilidadMenu.Disponible(menu), disponibilidadDelMenu(menu, "14:59"))
    }

    @Test
    fun desdeLaHoraDeFinElMenuTermina() {
        assertEquals(DisponibilidadMenu.Terminado("15:00"), disponibilidadDelMenu(menu(), "15:00"))
        assertEquals(DisponibilidadMenu.Terminado("15:00"), disponibilidadDelMenu(menu(), "18:30"))
    }

    @Test
    fun elMenuTerminadoPorElLocalSeMuestraAgotado() {
        val menu = menu(estado = EstadoMenuDelDia.TERMINADO)
        assertEquals(DisponibilidadMenu.Agotado, disponibilidadDelMenu(menu, "12:00"))
    }

    @Test
    fun sinEntradasOSegundosDisponiblesElMenuSeAgota() {
        val sinEntradas = menu(entradas = listOf(OpcionMenu("Causa limeña", agotado = true)))
        val sinSegundos = menu(segundos = listOf(OpcionMenu("Ají de gallina", agotado = true)))
        assertEquals(DisponibilidadMenu.Agotado, disponibilidadDelMenu(sinEntradas, "12:00"))
        assertEquals(DisponibilidadMenu.Agotado, disponibilidadDelMenu(sinSegundos, "12:00"))
    }

    @Test
    fun conAlgunaOpcionAgotadaElMenuSigueDisponible() {
        val menu = menu(
            segundos = listOf(
                OpcionMenu("Arroz con pollo", agotado = true),
                OpcionMenu("Ají de gallina", agotado = false),
            )
        )
        assertEquals(DisponibilidadMenu.Disponible(menu), disponibilidadDelMenu(menu, "12:00"))
    }

    @Test
    fun armarCopiaElPrecioYLaFechaDelMenu() {
        assertEquals(
            MenuDelCarrito(
                fecha = "2026-11-09",
                entrada = "Causa limeña",
                segundo = "Ají de gallina",
                precio = 1400,
                cantidad = 1,
            ),
            menu().armar("Causa limeña", "Ají de gallina"),
        )
    }

    @Test
    fun armarNoAceptaOpcionesAgotadasNiInexistentes() {
        val menu = menu(segundos = listOf(OpcionMenu("Arroz con pollo", agotado = true)))
        assertNull(menu.armar("Causa limeña", "Arroz con pollo"))
        assertNull(menu.armar("Sopa de casa", "Arroz con pollo"))
    }
}
