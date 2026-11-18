package com.equipo.sanmarkfood.comensal.domain.usecase.pedidos

import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Carrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.EstadoPago
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.EstadoPedido
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ItemCarrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ItemPedido
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MenuDelCarrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MetodoPago
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Modalidad
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MotivoRechazo
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Pedido
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.PlatoDelCarrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.RechazoPedido
import com.equipo.sanmarkfood.comensal.domain.repository.CarritoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecuperarPedidoEnCarritoUseCaseTest {

    private class CarritoFalso(var carrito: Carrito? = null) : CarritoRepository {
        override fun observarCarrito(): Flow<Carrito?> = flowOf(carrito)
        override suspend fun leerCarrito(): Carrito? = carrito
        override suspend fun agregar(restauranteId: String, restauranteNombre: String, item: ItemCarrito) {
            carrito = Carrito(restauranteId, restauranteNombre, carrito?.items.orEmpty() + item)
        }
        override suspend fun cambiarCantidad(clave: String, cantidad: Int) = Unit
        override suspend fun quitar(clave: String) = Unit
        override suspend fun vaciar() {
            carrito = null
        }
    }

    private val menu = ItemPedido(
        cantidad = 1,
        nombre = "Menú del día",
        precio = 1400,
        entrada = "Causa limeña",
        segundo = "Ají de gallina",
    )
    private val lomo = ItemPedido(cantidad = 2, nombre = "Lomo saltado", precio = 2400, platoId = "lomo")

    private fun rechazado(platoAgotado: String?) = Pedido(
        id = "pedido-1",
        numero = 132,
        restauranteId = "local-1",
        restauranteNombre = "Doña Carmen",
        modalidad = Modalidad.RECOJO,
        direccion = null,
        metodoPago = MetodoPago.BILLETERA,
        pago = EstadoPago.REVERTIDO,
        items = listOf(menu, lomo),
        total = 6200,
        estado = EstadoPedido.RECHAZADO,
        codigoRecojo = "0132",
        rechazo = RechazoPedido(
            motivo = if (platoAgotado != null) MotivoRechazo.PLATO_AGOTADO else MotivoRechazo.COCINA_SATURADA,
            detalle = null,
            platoAgotado = platoAgotado,
            automatico = false,
        ),
        creadoEn = null,
        aceptadoEn = null,
        listoAprox = null,
        entregadoEn = null,
    )

    @Test
    fun devuelveAlCarritoTodoMenosElPlatoAgotado() = runBlocking {
        val carrito = CarritoFalso()

        val recuperado = RecuperarPedidoEnCarritoUseCase(carrito)(rechazado(platoAgotado = "Lomo saltado"))

        assertTrue(recuperado)
        val items = carrito.carrito?.items.orEmpty()
        assertEquals(1, items.size)
        assertTrue(items.single() is MenuDelCarrito)
        assertEquals("local-1", carrito.carrito?.restauranteId)
    }

    @Test
    fun unaOpcionAgotadaDelMenuDejaFueraElMenu() = runBlocking {
        val carrito = CarritoFalso()

        RecuperarPedidoEnCarritoUseCase(carrito)(rechazado(platoAgotado = "Ají de gallina"))

        assertEquals(
            listOf(PlatoDelCarrito(platoId = "lomo", nombre = "Lomo saltado", precio = 2400, cantidad = 2)),
            carrito.carrito?.items,
        )
    }

    @Test
    fun noPisaUnPedidoQueYaEstasArmando() = runBlocking {
        val otro = PlatoDelCarrito(platoId = "chaufa", nombre = "Chaufa", precio = 1800, cantidad = 1)
        val carrito = CarritoFalso(Carrito("local-2", "El Chifa", listOf(otro)))

        val recuperado = RecuperarPedidoEnCarritoUseCase(carrito)(rechazado(platoAgotado = null))

        assertFalse(recuperado)
        assertEquals(listOf(otro), carrito.carrito?.items)
    }
}
