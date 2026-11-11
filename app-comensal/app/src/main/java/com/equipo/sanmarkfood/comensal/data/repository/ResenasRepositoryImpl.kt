package com.equipo.sanmarkfood.comensal.data.repository

import com.equipo.sanmarkfood.comensal.data.remote.CalificacionDto
import com.equipo.sanmarkfood.comensal.data.remote.FuncionesApi
import com.equipo.sanmarkfood.comensal.data.remote.llamarFuncion
import com.equipo.sanmarkfood.comensal.domain.model.resenas.Calificacion
import com.equipo.sanmarkfood.comensal.domain.model.resenas.ErrorCalificar
import com.equipo.sanmarkfood.comensal.domain.repository.ResenasRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ResenasRepositoryImpl @Inject constructor(
    private val api: FuncionesApi,
    private val json: Json,
) : ResenasRepository {

    override suspend fun calificar(calificacion: Calificacion) {
        llamarFuncion(json, ErrorCalificar.SinConexion, ::errorDeCalificar) {
            api.calificarPedido(
                CalificacionDto(
                    pedidoId = calificacion.pedidoId,
                    estrellas = calificacion.estrellas,
                    comentario = calificacion.comentario,
                    platos = calificacion.platos.takeIf { it.isNotEmpty() },
                )
            )
        }
    }
}

private fun errorDeCalificar(codigo: String?): Exception = when (codigo) {
    "ya_calificado" -> ErrorCalificar.YaCalificado
    "estado_invalido" -> ErrorCalificar.NoEntregado
    "datos_invalidos" -> ErrorCalificar.DatosInvalidos
    else -> ErrorCalificar.Desconocido
}
