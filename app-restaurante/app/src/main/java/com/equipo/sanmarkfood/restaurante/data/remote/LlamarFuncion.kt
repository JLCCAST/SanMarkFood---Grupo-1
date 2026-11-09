package com.equipo.sanmarkfood.restaurante.data.remote

import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.ErrorPedidos
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

internal suspend fun <T> llamarFuncion(json: Json, llamada: suspend () -> T): T =
    llamarFuncion(json, ErrorPedidos.SinConexion, ::errorDePedido, llamada)

internal suspend fun <T> llamarFuncion(
    json: Json,
    sinConexion: Exception,
    errorDe: (codigo: String?) -> Exception,
    llamada: suspend () -> T,
): T =
    try {
        llamada()
    } catch (e: HttpException) {
        throw errorDe(codigoDeError(json, e))
    } catch (e: IOException) {
        throw sinConexion
    }

private fun errorDePedido(codigo: String?): Exception = when (codigo) {
    "estado_invalido" -> ErrorPedidos.YaCambio
    "codigo_incorrecto" -> ErrorPedidos.CodigoIncorrecto
    else -> ErrorPedidos.Desconocido
}

private fun codigoDeError(json: Json, e: HttpException): String? {
    val cuerpo = e.response()?.errorBody()?.string() ?: return null
    return runCatching { json.decodeFromString<ErrorFuncionDto>(cuerpo).error }.getOrNull()
}
