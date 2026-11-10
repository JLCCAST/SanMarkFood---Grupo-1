package com.equipo.sanmarkfood.comensal.domain.usecase.pedidos

import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.Coordenadas
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Carrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.DireccionEntrega
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ErrorCrearPedido
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ErrorUbicacion
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ItemCarrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MetodoPago
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Modalidad
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Pedido
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.PedidoCreado
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.PlatoDelCarrito
import com.equipo.sanmarkfood.comensal.domain.model.perfil.Direccion
import com.equipo.sanmarkfood.comensal.domain.repository.CarritoRepository
import com.equipo.sanmarkfood.comensal.domain.repository.GeocodificacionRepository
import com.equipo.sanmarkfood.comensal.domain.repository.PedidosRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test

class CrearPedidoUseCaseTest {

    private class CarritoFalso(var carrito: Carrito?) : CarritoRepository {
        override fun observarCarrito(): Flow<Carrito?> = flowOf(carrito)
        override suspend fun leerCarrito(): Carrito? = carrito
        override suspend fun agregar(restauranteId: String, restauranteNombre: String, item: ItemCarrito) = Unit
        override suspend fun cambiarCantidad(clave: String, cantidad: Int) = Unit
        override suspend fun quitar(clave: String) = Unit
        override suspend fun vaciar() {
            carrito = null
        }
    }

    private class PedidosFalso(private val error: Exception? = null) : PedidosRepository {
        var direccionEnviada: DireccionEntrega? = null
        var llamadas = 0

        override suspend fun crearPedido(
            carrito: Carrito,
            modalidad: Modalidad,
            metodoPago: MetodoPago,
            direccion: DireccionEntrega?,
        ): PedidoCreado {
            llamadas++
            direccionEnviada = direccion
            error?.let { throw it }
            return PedidoCreado(pedidoId = "pedido-1", numero = 132)
        }

        override fun observarPedido(pedidoId: String): Flow<Pedido?> = emptyFlow()
    }

    private class GeocodificacionFalsa(private val error: Exception? = null) : GeocodificacionRepository {
        var consultas = 0

        override suspend fun coordenadasDe(direccion: String): Coordenadas {
            consultas++
            error?.let { throw it }
            return Coordenadas(latitud = -12.0563, longitud = -77.0844)
        }
    }

    private val carritoConPlato = Carrito(
        restauranteId = "local-1",
        restauranteNombre = "Doña Carmen",
        items = listOf(PlatoDelCarrito(platoId = "lomo", nombre = "Lomo saltado", precio = 2400, cantidad = 1)),
    )

    private val casa = Direccion(etiqueta = "Casa", direccionTexto = "Av. Universitaria 1801, Lima")

    private fun crear(
        carrito: CarritoFalso = CarritoFalso(carritoConPlato),
        pedidos: PedidosFalso = PedidosFalso(),
        geocodificacion: GeocodificacionFalsa = GeocodificacionFalsa(),
    ) = CrearPedidoUseCase(pedidos, carrito, UbicarDireccionEntregaUseCase(geocodificacion))

    private suspend fun esperarError(bloque: suspend () -> Unit): Exception {
        try {
            bloque()
        } catch (e: Exception) {
            return e
        }
        fail("Se esperaba un error")
        throw IllegalStateException()
    }

    @Test
    fun enRecojoCreaElPedidoYVaciaElCarrito() = runBlocking {
        val carrito = CarritoFalso(carritoConPlato)
        val pedidos = PedidosFalso()

        val creado = crear(carrito, pedidos)(Modalidad.RECOJO, MetodoPago.EFECTIVO)

        assertEquals(PedidoCreado("pedido-1", 132), creado)
        assertNull(pedidos.direccionEnviada)
        assertNull(carrito.carrito)
    }

    @Test
    fun sinPlatosNoLlamaALaFuncion() = runBlocking {
        val pedidos = PedidosFalso()

        val error = esperarError { crear(CarritoFalso(null), pedidos)(Modalidad.RECOJO, MetodoPago.EFECTIVO) }

        assertEquals(ErrorCrearPedido.DatosInvalidos, error)
        assertEquals(0, pedidos.llamadas)
    }

    @Test
    fun elDeliveryNecesitaUnaDireccion() = runBlocking {
        val carrito = CarritoFalso(carritoConPlato)

        val error = esperarError { crear(carrito)(Modalidad.DELIVERY, MetodoPago.BILLETERA, direccion = null) }

        assertEquals(ErrorCrearPedido.SinDireccion, error)
        assertNotNull(carrito.carrito)
    }

    @Test
    fun elDeliveryEnviaLaDireccionConSusCoordenadas() = runBlocking {
        val pedidos = PedidosFalso()
        val geocodificacion = GeocodificacionFalsa()

        crear(pedidos = pedidos, geocodificacion = geocodificacion)(Modalidad.DELIVERY, MetodoPago.TARJETA, casa)

        assertEquals(
            DireccionEntrega(texto = "Casa · Av. Universitaria 1801, Lima", latitud = -12.0563, longitud = -77.0844),
            pedidos.direccionEnviada,
        )
        assertEquals(1, geocodificacion.consultas)
    }

    @Test
    fun siLaDireccionYaTieneCoordenadasNoConsultaAGoogle() = runBlocking {
        val geocodificacion = GeocodificacionFalsa()
        val conCoordenadas = casa.copy(latitud = -12.05, longitud = -77.08)

        crear(geocodificacion = geocodificacion)(Modalidad.DELIVERY, MetodoPago.EFECTIVO, conCoordenadas)

        assertEquals(0, geocodificacion.consultas)
    }

    @Test
    fun siNoSeEncuentraLaDireccionNoSeCreaElPedido() = runBlocking {
        val pedidos = PedidosFalso()
        val geocodificacion = GeocodificacionFalsa(error = ErrorUbicacion.NoEncontrada)

        val error = esperarError {
            crear(pedidos = pedidos, geocodificacion = geocodificacion)(Modalidad.DELIVERY, MetodoPago.EFECTIVO, casa)
        }

        assertEquals(ErrorUbicacion.NoEncontrada, error)
        assertEquals(0, pedidos.llamadas)
    }

    @Test
    fun sinConexionElPedidoSigueArmado() = runBlocking {
        val carrito = CarritoFalso(carritoConPlato)

        val error = esperarError {
            crear(carrito, PedidosFalso(error = ErrorCrearPedido.SinConexion))(Modalidad.RECOJO, MetodoPago.BILLETERA)
        }

        assertEquals(ErrorCrearPedido.SinConexion, error)
        assertEquals(carritoConPlato, carrito.carrito)
    }
}
