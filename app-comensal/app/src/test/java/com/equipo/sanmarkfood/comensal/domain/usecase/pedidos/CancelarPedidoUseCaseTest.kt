package com.equipo.sanmarkfood.comensal.domain.usecase.pedidos

import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Carrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.DireccionEntrega
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ErrorCancelarPedido
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.EstadoPago
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.EstadoPedido
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MetodoPago
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Modalidad
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Pedido
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.PedidoCreado
import com.equipo.sanmarkfood.comensal.domain.repository.PedidosRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class CancelarPedidoUseCaseTest {

    private class PedidosFalso : PedidosRepository {
        val cancelados = mutableListOf<String>()

        override suspend fun crearPedido(
            carrito: Carrito,
            modalidad: Modalidad,
            metodoPago: MetodoPago,
            direccion: DireccionEntrega?,
        ): PedidoCreado = error("No se usa")

        override fun observarPedido(pedidoId: String): Flow<Pedido?> = emptyFlow()

        override suspend fun cancelarPedido(pedidoId: String) {
            cancelados += pedidoId
        }
    }

    private val repositorio = PedidosFalso()
    private val cancelar = CancelarPedidoUseCase(repositorio)

    private fun pedido(estado: EstadoPedido) = Pedido(
        id = "pedido-1",
        numero = 132,
        restauranteId = "local-1",
        restauranteNombre = "Doña Carmen",
        modalidad = Modalidad.RECOJO,
        direccion = null,
        metodoPago = MetodoPago.BILLETERA,
        pago = EstadoPago.PAGADO,
        items = emptyList(),
        total = 2400,
        estado = estado,
        codigoRecojo = "0132",
        rechazo = null,
        creadoEn = null,
        aceptadoEn = null,
        listoAprox = null,
        entregadoEn = null,
    )

    @Test
    fun cancelaUnPedidoQueElLocalNoAcepto() = runBlocking {
        cancelar(pedido(EstadoPedido.RECIBIDO))

        assertEquals(listOf("pedido-1"), repositorio.cancelados)
    }

    @Test
    fun noCancelaUnPedidoQueYaSeEstaPreparando() = runBlocking {
        try {
            cancelar(pedido(EstadoPedido.PREPARANDO))
            fail("Se esperaba un error")
        } catch (e: ErrorCancelarPedido) {
            assertEquals(ErrorCancelarPedido.YaAceptado, e)
        }
        assertEquals(emptyList<String>(), repositorio.cancelados)
    }
}
