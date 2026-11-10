package com.equipo.sanmarkfood.comensal.domain.usecase.pedidos

import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Carrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ItemCarrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MenuDelCarrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.PlatoDelCarrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ResultadoAgregarAlCarrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.claveDePlato
import com.equipo.sanmarkfood.comensal.domain.repository.CarritoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AgregarAlCarritoUseCaseTest {

    private class CarritoFalso : CarritoRepository {
        val carrito = MutableStateFlow<Carrito?>(null)

        override fun observarCarrito(): Flow<Carrito?> = carrito

        override suspend fun leerCarrito(): Carrito? = carrito.value

        override suspend fun agregar(restauranteId: String, restauranteNombre: String, item: ItemCarrito) {
            val actual = carrito.value?.items.orEmpty()
            val existente = actual.firstOrNull { it.clave == item.clave }
            val items = if (existente == null) {
                actual + item
            } else {
                actual.map { if (it.clave == item.clave) it.conCantidad(it.cantidad + item.cantidad) else it }
            }
            carrito.value = Carrito(restauranteId, restauranteNombre, items)
        }

        override suspend fun cambiarCantidad(clave: String, cantidad: Int) = Unit

        override suspend fun quitar(clave: String) = Unit

        override suspend fun vaciar() {
            carrito.value = null
        }

        private fun ItemCarrito.conCantidad(cantidad: Int): ItemCarrito = when (this) {
            is PlatoDelCarrito -> copy(cantidad = cantidad)
            is MenuDelCarrito -> copy(cantidad = cantidad)
        }
    }

    private val repositorio = CarritoFalso()
    private val agregar = AgregarAlCarritoUseCase(repositorio)

    private fun plato(id: String, cantidad: Int = 1) =
        PlatoDelCarrito(platoId = id, nombre = "Plato $id", precio = 2400, cantidad = cantidad)

    private fun menu(entrada: String, segundo: String) =
        MenuDelCarrito(fecha = "2026-11-09", entrada = entrada, segundo = segundo, precio = 1400, cantidad = 1)

    @Test
    fun agregaAlCarritoVacio() = runBlocking {
        val resultado = agregar("local-1", "Doña Carmen", plato("lomo"))

        assertEquals(ResultadoAgregarAlCarrito.Agregado, resultado)
        assertEquals(1, repositorio.carrito.value?.cantidadDe(claveDePlato("lomo")))
    }

    @Test
    fun conPlatosDeOtroLocalPreguntaAntesDeVaciar() = runBlocking {
        agregar("local-1", "Doña Carmen", plato("lomo"))

        val resultado = agregar("local-2", "El Chifa", plato("chaufa"))

        assertEquals(ResultadoAgregarAlCarrito.OtroLocal("Doña Carmen"), resultado)
        assertEquals("local-1", repositorio.carrito.value?.restauranteId)
        assertEquals(0, repositorio.carrito.value?.cantidadDe(claveDePlato("chaufa")))
    }

    @Test
    fun alAceptarVaciarQuedaSoloElPlatoDelOtroLocal() = runBlocking {
        agregar("local-1", "Doña Carmen", plato("lomo"))

        val resultado = agregar("local-2", "El Chifa", plato("chaufa"), vaciarSiOtroLocal = true)

        assertEquals(ResultadoAgregarAlCarrito.Agregado, resultado)
        assertEquals("local-2", repositorio.carrito.value?.restauranteId)
        assertEquals(listOf(claveDePlato("chaufa")), repositorio.carrito.value?.items?.map { it.clave })
    }

    @Test
    fun sumaLaCantidadSiElPlatoYaEstaba() = runBlocking {
        agregar("local-1", "Doña Carmen", plato("lomo"))
        agregar("local-1", "Doña Carmen", plato("lomo", cantidad = 2))

        assertEquals(3, repositorio.carrito.value?.cantidadDe(claveDePlato("lomo")))
        assertEquals(1, repositorio.carrito.value?.items?.size)
    }

    @Test
    fun noPasaDeVeinteUnidadesPorPlato() = runBlocking {
        agregar("local-1", "Doña Carmen", plato("lomo", cantidad = Carrito.MAX_CANTIDAD_POR_ITEM))

        val resultado = agregar("local-1", "Doña Carmen", plato("lomo"))

        assertEquals(ResultadoAgregarAlCarrito.LimiteAlcanzado, resultado)
        assertEquals(Carrito.MAX_CANTIDAD_POR_ITEM, repositorio.carrito.value?.cantidadDe(claveDePlato("lomo")))
    }

    @Test
    fun noPasaDeVeintePlatosDistintos() = runBlocking {
        repeat(Carrito.MAX_ITEMS_DISTINTOS) { agregar("local-1", "Doña Carmen", plato("plato-$it")) }

        val resultado = agregar("local-1", "Doña Carmen", plato("uno-mas"))

        assertEquals(ResultadoAgregarAlCarrito.LimiteAlcanzado, resultado)
        assertEquals(Carrito.MAX_ITEMS_DISTINTOS, repositorio.carrito.value?.items?.size)
    }

    @Test
    fun cadaCombinacionDelMenuEsUnItemDistinto() = runBlocking {
        agregar("local-1", "Doña Carmen", menu("Causa limeña", "Ají de gallina"))
        agregar("local-1", "Doña Carmen", menu("Causa limeña", "Ají de gallina"))
        agregar("local-1", "Doña Carmen", menu("Sopa de casa", "Ají de gallina"))

        val carrito = repositorio.carrito.value
        assertEquals(2, carrito?.items?.size)
        assertEquals(3, carrito?.cantidadDeMenus)
        assertEquals(3 * 1400, carrito?.total)
    }
}
