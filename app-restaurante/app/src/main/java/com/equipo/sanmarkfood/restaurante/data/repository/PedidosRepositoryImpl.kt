package com.equipo.sanmarkfood.restaurante.data.repository

import com.equipo.sanmarkfood.restaurante.data.firebase.AuthDataSource
import com.equipo.sanmarkfood.restaurante.data.firebase.PedidosDataSource
import com.equipo.sanmarkfood.restaurante.data.remote.AccionPedidoDto
import com.equipo.sanmarkfood.restaurante.data.remote.FuncionesApi
import com.equipo.sanmarkfood.restaurante.data.remote.llamarFuncion
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.ErrorPedidos
import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.PedidosDeHoy
import com.equipo.sanmarkfood.restaurante.domain.repository.PedidosRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import java.util.Date
import javax.inject.Inject

class PedidosRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val pedidosDataSource: PedidosDataSource,
    private val funcionesApi: FuncionesApi,
    private val json: Json,
) : PedidosRepository {

    override fun observarPedidosDesde(desde: Date): Flow<PedidosDeHoy> = flow {
        val uid = authDataSource.usuarioActual()?.uid ?: throw ErrorPedidos.Desconocido
        emitAll(pedidosDataSource.observarDesde(uid, desde))
    }

    override suspend fun aceptar(pedidoId: String, minutos: Int) {
        llamarFuncion(json) {
            funcionesApi.actualizarPedido(AccionPedidoDto(pedidoId = pedidoId, accion = "aceptar", minutos = minutos))
        }
    }
}
