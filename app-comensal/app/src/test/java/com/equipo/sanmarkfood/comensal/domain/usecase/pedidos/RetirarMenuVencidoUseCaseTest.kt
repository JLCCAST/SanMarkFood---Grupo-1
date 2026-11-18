package com.equipo.sanmarkfood.comensal.domain.usecase.pedidos

import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.EstadoMenuDelDia
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Carrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.CategoriaCarta
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ItemCarrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MenuDeHoy
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MenuDelCarrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MenuRetirado
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.OpcionMenu
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Plato
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.PlatoDelCarrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.claveDePlato
import com.equipo.sanmarkfood.comensal.domain.repository.CarritoRepository
import com.equipo.sanmarkfood.comensal.domain.repository.CartaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class RetirarMenuVencidoUseCaseTest {

    private class CarritoFalso(var carrito: Carrito?) : CarritoRepository {
        override fun observarCarrito(): Flow<Carrito?> = flowOf(carrito)
        override suspend fun leerCarrito(): Carrito? = carrito
        override suspend fun agregar(restauranteId: String, restauranteNombre: String, item: ItemCarrito) = Unit
        override suspend fun cambiarCantidad(clave: String, cantidad: Int) = Unit
        override suspend fun quitar(clave: String) {
            carrito = carrito?.let { actual -> actual.copy(items = actual.items.filterNot { it.clave == clave }) }
        }
        override suspend fun vaciar() {
            carrito = null
        }
    }

    private class CartaFalsa(private val menu: MenuDeHoy?) : CartaRepository {
        override fun observarCategorias(restauranteId: String): Flow<List<CategoriaCarta>> = emptyFlow()
        override fun observarPlatos(restauranteId: String): Flow<List<Plato>> = emptyFlow()
        override fun observarMenu(restauranteId: String, fecha: String): Flow<MenuDeHoy?> = flowOf(menu)
    }

    private val lomo = PlatoDelCarrito(platoId = "lomo", nombre = "Lomo saltado", precio = 2400, cantidad = 1)

    private fun menuDelCarrito(entrada: String) =
        MenuDelCarrito(fecha = "2026-11-10", entrada = entrada, segundo = "Ají de gallina", precio = 1400, cantidad = 1)

    private fun menuQueTerminoA(horaFin: String) = MenuDeHoy(
        fecha = "2026-11-10",
        precio = 1400,
        entradas = listOf(OpcionMenu("Causa limeña", agotado = false)),
        segundos = listOf(OpcionMenu("Ají de gallina", agotado = false)),
        refresco = null,
        postre = null,
        horaFin = horaFin,
        estado = EstadoMenuDelDia.PUBLICADO,
    )

    private fun retirar(carrito: CarritoFalso, menu: MenuDeHoy?) =
        RetirarMenuVencidoUseCase(carrito, ObservarDisponibilidadMenuUseCase(ObservarMenuDeHoyUseCase(CartaFalsa(menu))))

    @Test
    fun quitaSoloLosMenusYConservaLosPlatos() = runBlocking {
        val carrito = CarritoFalso(
            Carrito(
                restauranteId = "local-1",
                restauranteNombre = "Doña Carmen",
                items = listOf(lomo, menuDelCarrito("Causa limeña"), menuDelCarrito("Sopa de casa")),
            )
        )

        retirar(carrito, menuQueTerminoA("00:00"))()

        assertEquals(listOf(claveDePlato("lomo")), carrito.carrito?.items?.map { it.clave })
    }

    @Test
    fun avisaHastaQueHoraSeSirvioElMenu() = runBlocking {
        val carrito = CarritoFalso(Carrito("local-1", "Doña Carmen", listOf(menuDelCarrito("Causa limeña"))))

        val retirado = retirar(carrito, menuQueTerminoA("00:00"))()

        assertEquals(MenuRetirado(restauranteNombre = "Doña Carmen", horaFin = "00:00"), retirado)
    }

    @Test
    fun sinMenuPublicadoNoIndicaHora() = runBlocking {
        val carrito = CarritoFalso(Carrito("local-1", "Doña Carmen", listOf(menuDelCarrito("Causa limeña"))))

        val retirado = retirar(carrito, menu = null)()

        assertEquals(MenuRetirado(restauranteNombre = "Doña Carmen", horaFin = null), retirado)
    }
}
