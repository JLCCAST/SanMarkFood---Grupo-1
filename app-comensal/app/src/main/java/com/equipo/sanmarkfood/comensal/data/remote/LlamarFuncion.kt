package com.equipo.sanmarkfood.comensal.data.remote

import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ErrorCrearPedido
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

internal suspend fun <T> llamarFuncion(json: Json, llamada: suspend () -> T): T =
    try {
        llamada()
    } catch (e: HttpException) {
        throw errorDeCrearPedido(codigoDeError(json, e))
    } catch (e: IOException) {
        throw ErrorCrearPedido.SinConexion
    }

private fun errorDeCrearPedido(codigo: String?): ErrorCrearPedido = when (codigo) {
    "local_no_disponible" -> ErrorCrearPedido.LocalNoDisponible
    "local_cerrado" -> ErrorCrearPedido.LocalCerrado
    "plato_agotado" -> ErrorCrearPedido.PlatoAgotado
    "opcion_agotada" -> ErrorCrearPedido.OpcionAgotada
    "menu_vencido" -> ErrorCrearPedido.MenuVencido
    "datos_invalidos" -> ErrorCrearPedido.DatosInvalidos
    "no_autenticado", "no_autorizado", "correo_sin_verificar" -> ErrorCrearPedido.SesionInvalida
    else -> ErrorCrearPedido.Desconocido
}

private fun codigoDeError(json: Json, e: HttpException): String? {
    val cuerpo = e.response()?.errorBody()?.string() ?: return null
    return runCatching { json.decodeFromString<ErrorFuncionDto>(cuerpo).error }.getOrNull()
}