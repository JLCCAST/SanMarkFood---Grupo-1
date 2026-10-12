package com.equipo.sanmarkfood.restaurante.domain.usecase.auth

import com.equipo.sanmarkfood.restaurante.domain.model.auth.ErrorAuth
import com.equipo.sanmarkfood.restaurante.domain.model.auth.EstadoSesion
import com.equipo.sanmarkfood.restaurante.domain.model.auth.Rol
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.CategoriaRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.DatosLocal
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.EstadoRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Horario
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Restaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.TipoFoto
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Ubicacion
import com.equipo.sanmarkfood.restaurante.domain.repository.AuthRepository
import com.equipo.sanmarkfood.restaurante.domain.repository.RestauranteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ObtenerEstadoSesionUseCaseTest {

    private class AuthFalso : AuthRepository {
        var sesion: EstadoSesion = EstadoSesion.SinSesion
        var sesionCerrada = false
        override suspend fun registrar(correo: String, contrasena: String) = Unit
        override suspend fun enviarVerificacion() = Unit
        override suspend fun correoVerificado() = true
        override suspend fun iniciarSesion(correo: String, contrasena: String) = Unit
        override suspend fun estadoSesion(): EstadoSesion = sesion
        override fun cerrarSesion() {
            sesionCerrada = true
        }
        override suspend fun enviarRecuperacion(correo: String) = Unit
        override suspend fun iniciarSesionConGoogle() = false
    }

    private class RestauranteFalso : RestauranteRepository {
        var local: Restaurante? = null
        var error: ErrorRestaurante? = null
        var lecturas = 0
        override suspend fun obtener(): Restaurante? {
            lecturas++
            error?.let { throw it }
            return local
        }
        override fun observar(): Flow<Restaurante?> = emptyFlow()
        override suspend fun guardarDatos(datos: DatosLocal) = Unit
        override suspend fun guardarDatosYPedirRevision(datos: DatosLocal) = Unit
        override suspend fun subirFoto(tipo: TipoFoto, imagenLocal: String, alAvanzar: (Float) -> Unit) = ""
        override suspend fun enviarARevision(horario: Horario) = Unit
        override suspend fun reenviarARevision(datos: DatosLocal) = Unit
        override suspend fun guardarHorario(horario: Horario) = Unit
        override suspend fun cambiarPausa(pausado: Boolean) = Unit
    }

    private val auth = AuthFalso()
    private val restaurantes = RestauranteFalso()
    private val obtenerEstadoSesion = ObtenerEstadoSesionUseCase(auth, restaurantes)

    private fun obtener(): EstadoSesion = runBlocking { obtenerEstadoSesion() }

    private fun localEn(estado: EstadoRestaurante) = Restaurante(
        datos = DatosLocal(
            nombre = "La Sazón de Doña Carmen",
            categoria = CategoriaRestaurante.CRIOLLA,
            direccion = "Av. Venezuela 3450",
            ubicacion = Ubicacion.CiudadUniversitaria,
            telefono = "987654321",
            portadaUrl = null,
            logoUrl = null,
        ),
        estado = estado,
        horario = null,
        rechazo = null,
        pausado = false,
    )

    @Test
    fun elAdministradorEntraSinPasarPorElAltaDelLocal() {
        auth.sesion = EstadoSesion.Activa(Rol.ADMINISTRADOR)
        assertEquals(EstadoSesion.Activa(Rol.ADMINISTRADOR), obtener())
        assertEquals(0, restaurantes.lecturas)
        assertFalse(auth.sesionCerrada)
    }

    @Test
    fun elRestauranteSinLocalOEnBorradorVuelveAlAlta() {
        auth.sesion = EstadoSesion.Activa(Rol.RESTAURANTE)
        assertEquals(EstadoSesion.SinLocal, obtener())

        restaurantes.local = localEn(EstadoRestaurante.BORRADOR)
        assertEquals(EstadoSesion.SinLocal, obtener())
    }

    @Test
    fun elRestauranteConElAltaTerminadaEntraAlPanel() {
        auth.sesion = EstadoSesion.Activa(Rol.RESTAURANTE)
        listOf(EstadoRestaurante.PENDIENTE, EstadoRestaurante.APROBADO, EstadoRestaurante.RECHAZADO).forEach { estado ->
            restaurantes.local = localEn(estado)
            assertEquals(EstadoSesion.Activa(Rol.RESTAURANTE), obtener())
        }
    }

    @Test
    fun laCuentaDeComensalSeRechazaYSeCierraSuSesion() {
        auth.sesion = EstadoSesion.Activa(Rol.COMENSAL)
        val error = try {
            obtener()
            null
        } catch (e: ErrorAuth) {
            e
        }
        assertEquals(ErrorAuth.CuentaDeComensal, error)
        assertTrue(auth.sesionCerrada)
    }

    @Test
    fun sinSesionOSinVerificarNoSeLeeElLocal() {
        auth.sesion = EstadoSesion.SinSesion
        assertEquals(EstadoSesion.SinSesion, obtener())

        auth.sesion = EstadoSesion.SinVerificar("admin@sanmarkfood.pe")
        assertEquals(EstadoSesion.SinVerificar("admin@sanmarkfood.pe"), obtener())
        assertEquals(0, restaurantes.lecturas)
    }

    @Test
    fun sinConexionAlLeerElLocalSeAvisaComoErrorDeSesion() {
        auth.sesion = EstadoSesion.Activa(Rol.RESTAURANTE)
        restaurantes.error = ErrorRestaurante.SinConexion
        val error = try {
            obtener()
            null
        } catch (e: ErrorAuth) {
            e
        }
        assertEquals(ErrorAuth.SinConexion, error)
    }
}
