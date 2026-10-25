package com.equipo.sanmarkfood.restaurante.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

interface FuncionesApi {
    @POST("actualizarPedido")
    suspend fun actualizarPedido(@Body accion: AccionPedidoDto): EstadoPedidoDto
}

@Serializable
data class AccionPedidoDto(
    val pedidoId: String,
    val accion: String,
    val minutos: Int? = null,
)

@Serializable
data class EstadoPedidoDto(val estado: String)

@Serializable
data class ErrorFuncionDto(val error: String)
