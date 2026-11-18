package com.equipo.sanmarkfood.comensal.data.repository

import com.equipo.sanmarkfood.comensal.data.firebase.ReservasDataSource
import com.equipo.sanmarkfood.comensal.data.firebase.aErrorReservas
import com.equipo.sanmarkfood.comensal.data.remote.CambiarReservaDto
import com.equipo.sanmarkfood.comensal.data.remote.CrearReservaDto
import com.equipo.sanmarkfood.comensal.data.remote.FuncionesApi
import com.equipo.sanmarkfood.comensal.data.remote.esErrorDeSesion
import com.equipo.sanmarkfood.comensal.data.remote.llamarFuncion
import com.equipo.sanmarkfood.comensal.domain.model.reservas.ErrorReservas
import com.equipo.sanmarkfood.comensal.domain.model.reservas.Reserva
import com.equipo.sanmarkfood.comensal.domain.model.reservas.SolicitudReserva
import com.equipo.sanmarkfood.comensal.domain.repository.ReservasRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ReservasRepositoryImpl @Inject constructor(
    private val dataSource: ReservasDataSource,
    private val api: FuncionesApi,
    private val json: Json,
) : ReservasRepository {

    override fun observarReservas(comensalUid: String): Flow<List<Reserva>> =
        dataSource.observarReservasDelComensal(comensalUid, RESERVAS_RECIENTES).catch { throw it.aErrorReservas() }

    override fun observarReserva(reservaId: String): Flow<Reserva?> =
        dataSource.observarReserva(reservaId).catch { throw it.aErrorReservas() }

    override fun observarOcupacion(restauranteId: String, fecha: String): Flow<Map<String, Int>> =
        dataSource.observarOcupacion(restauranteId, fecha).catch { throw it.aErrorReservas() }

    override suspend fun crearReserva(solicitud: SolicitudReserva): String =
        llamarFuncion(json, ErrorReservas.SinConexion, ::errorDeReservas) {
            api.crearReserva(
                CrearReservaDto(
                    accion = "crear",
                    restauranteUid = solicitud.restauranteId,
                    fecha = solicitud.fecha,
                    hora = solicitud.hora,
                    personas = solicitud.personas,
                )
            ).reservaId
        }

    override suspend fun modificarReserva(reservaId: String, fecha: String, hora: String) {
        llamarFuncion(json, ErrorReservas.SinConexion, ::errorDeReservas) {
            api.cambiarReserva(CambiarReservaDto(accion = "modificar", reservaId = reservaId, fecha = fecha, hora = hora))
        }
    }

    override suspend fun cancelarReserva(reservaId: String) {
        llamarFuncion(json, ErrorReservas.SinConexion, ::errorDeReservas) {
            api.cambiarReserva(CambiarReservaDto(accion = "cancelar", reservaId = reservaId))
        }
    }
}

private const val RESERVAS_RECIENTES = 30L

private fun errorDeReservas(codigo: String?): Exception = when {
    esErrorDeSesion(codigo) -> ErrorReservas.SesionInvalida
    codigo == "fuera_de_plazo" -> ErrorReservas.FueraDePlazo
    codigo == "fuera_de_horario" -> ErrorReservas.FueraDeHorario
    codigo == "franja_llena" -> ErrorReservas.FranjaLlena
    codigo == "tope_reservas" -> ErrorReservas.TopeReservas
    codigo == "sin_reservas" -> ErrorReservas.SinReservas
    codigo == "estado_invalido" -> ErrorReservas.EstadoInvalido
    else -> ErrorReservas.Desconocido
}
