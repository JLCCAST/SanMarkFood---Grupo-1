package com.equipo.sanmarkfood.comensal.data.remote

import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ErrorCrearPedido
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

internal suspend fun <T> llamarFuncion(json: Json, llamada: suspend () -> T): T =
    llamarFuncion(json, ErrorCrearPedido.SinConexion, ::errorDeCrearPedido, llamada)

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

internal fun esErrorDeSesion(codigo: String?): Boolean =
    codigo == "no_autenticado" || codigo == "no_autorizado" || codigo == "correo_sin_verificar"

private fun errorDeCrearPedido(codigo: String?): Exception = when {
    esErrorDeSesion(codigo) -> ErrorCrearPedido.SesionInvalida
    codigo == "local_no_disponible" -> ErrorCrearPedido.LocalNoDisponible
    codigo == "local_cerrado" -> ErrorCrearPedido.LocalCerrado
    codigo == "plato_agotado" -> ErrorCrearPedido.PlatoAgotado
    codigo == "opcion_agotada" -> ErrorCrearPedido.OpcionAgotada
    codigo == "menu_vencido" -> ErrorCrearPedido.MenuVencido
    codigo == "datos_invalidos" -> ErrorCrearPedido.DatosInvalidos
    else -> ErrorCrearPedido.Desconocido
}

private fun codigoDeError(json: Json, e: HttpException): String? {
    val cuerpo = e.response()?.errorBody()?.string() ?: return null
    return runCatching { json.decodeFromString<ErrorFuncionDto>(cuerpo).error }.getOrNull()
}
