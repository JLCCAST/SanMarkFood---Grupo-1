package com.equipo.sanmarkfood.restaurante.data.remote

import com.equipo.sanmarkfood.restaurante.domain.model.pedidos.ErrorPedidos
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

internal suspend fun <T> llamarFuncion(json: Json, llamada: suspend () -> T): T =
    try {
        llamada()
    } catch (e: HttpException) {
        throw when (codigoDeError(json, e)) {
            "estado_invalido" -> ErrorPedidos.YaCambio
            else -> ErrorPedidos.Desconocido
        }
    } catch (e: IOException) {
        throw ErrorPedidos.SinConexion
    }

private fun codigoDeError(json: Json, e: HttpException): String? {
    val cuerpo = e.response()?.errorBody()?.string() ?: return null
    return runCatching { json.decodeFromString<ErrorFuncionDto>(cuerpo).error }.getOrNull()
}
