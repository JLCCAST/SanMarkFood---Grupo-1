package com.equipo.sanmarkfood.restaurante.data.repository

import com.equipo.sanmarkfood.restaurante.data.remote.AccionDatosInicialesDto
import com.equipo.sanmarkfood.restaurante.data.remote.DatosInicialesApi
import com.equipo.sanmarkfood.restaurante.data.remote.DatosInicialesDto
import com.equipo.sanmarkfood.restaurante.data.remote.llamarFuncion
import com.equipo.sanmarkfood.restaurante.domain.model.admin.CuentaDePrueba
import com.equipo.sanmarkfood.restaurante.domain.model.admin.DatosBorrados
import com.equipo.sanmarkfood.restaurante.domain.model.admin.DatosCargados
import com.equipo.sanmarkfood.restaurante.domain.model.admin.ErrorDatosIniciales
import com.equipo.sanmarkfood.restaurante.domain.model.admin.RolDePrueba
import com.equipo.sanmarkfood.restaurante.domain.repository.DatosInicialesRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject

class DatosInicialesRepositoryImpl @Inject constructor(
    private val api: DatosInicialesApi,
    private val json: Json,
) : DatosInicialesRepository {

    override suspend fun cargar(): DatosCargados = llamar("cargar").aDatosCargados()

    override suspend fun borrar(): DatosBorrados {
        val respuesta = llamar("borrar")
        return DatosBorrados(
            cuentas = respuesta.cuentasBorradas,
            pedidos = respuesta.pedidos,
            reservas = respuesta.reservas,
        )
    }

    private suspend fun llamar(accion: String): DatosInicialesDto =
        llamarFuncion(json, ErrorDatosIniciales.SinConexion, ::errorDeDatosIniciales) {
            api.datosIniciales(AccionDatosInicialesDto(accion))
        }
}

private fun DatosInicialesDto.aDatosCargados() = DatosCargados(
    locales = locales,
    comensales = comensales,
    platos = platos,
    pedidos = pedidos,
    reservas = reservas,
    resenas = resenas,
    contrasena = contrasena,
    cuentas = cuentas.orEmpty().map { cuenta ->
        CuentaDePrueba(
            rol = if (cuenta.rol == "restaurante") RolDePrueba.RESTAURANTE else RolDePrueba.COMENSAL,
            nombre = cuenta.nombre,
            correo = cuenta.correo,
        )
    },
)

private fun errorDeDatosIniciales(codigo: String?): Exception = when (codigo) {
    "no_autorizado", "no_autenticado", "correo_sin_verificar" -> ErrorDatosIniciales.NoAutorizado
    else -> ErrorDatosIniciales.Desconocido
}
