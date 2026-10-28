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
    val codigo: String? = null,
    val sinCodigo: Boolean? = null,
    val motivo: String? = null,
    val detalle: String? = null,
    val platoAgotado: String? = null,
)

@Serializable
data class EstadoPedidoDto(val estado: String)

@Serializable
data class ErrorFuncionDto(val error: String)
