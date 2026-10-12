package com.equipo.sanmarkfood.restaurante.domain.usecase.auth

import com.equipo.sanmarkfood.restaurante.domain.model.auth.EstadoSesion
import com.equipo.sanmarkfood.restaurante.domain.model.auth.Rol
import com.equipo.sanmarkfood.restaurante.domain.repository.AuthRepository
import com.equipo.sanmarkfood.restaurante.domain.repository.DispositivosRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class DispositivoUseCasesTest {

    private val eventos = mutableListOf<String>()

    private val dispositivos = object : DispositivosRepository {
        override fun registrar() {
            eventos += "registrar"
        }
        override fun renovar(token: String) {
            eventos += "renovar:$token"
        }
        override fun olvidar() {
            eventos += "olvidar"
        }
    }

    private val auth = object : AuthRepository {
        override suspend fun registrar(correo: String, contrasena: String) = Unit
        override suspend fun enviarVerificacion() = Unit
        override suspend fun correoVerificado() = true
        override suspend fun iniciarSesion(correo: String, contrasena: String) = Unit
        override suspend fun estadoSesion(): EstadoSesion = EstadoSesion.SinSesion
        override fun cerrarSesion() {
            eventos += "cerrarSesion"
        }
        override suspend fun enviarRecuperacion(correo: String) = Unit
        override suspend fun iniciarSesionConGoogle() = false
    }

    private val registrarDispositivo = RegistrarDispositivoUseCase(dispositivos)

    @Test
    fun registraElCelularDeUnRestauranteConOSinLocal() {
        registrarDispositivo(EstadoSesion.Activa(Rol.RESTAURANTE))
        registrarDispositivo(EstadoSesion.SinLocal)
        assertEquals(listOf("registrar", "registrar"), eventos)
    }

    @Test
    fun noRegistraAlAdministradorNiSesionesIncompletas() {
        listOf(
            EstadoSesion.Activa(Rol.ADMINISTRADOR),
            EstadoSesion.SinSesion,
            EstadoSesion.SinVerificar("local@correo.com"),
        ).forEach { registrarDispositivo(it) }
        assertEquals(emptyList<String>(), eventos)
    }

    @Test
    fun guardaElTokenNuevoYDescartaUnoVacio() {
        val renovarDispositivo = RenovarDispositivoUseCase(dispositivos)
        renovarDispositivo("token-nuevo")
        renovarDispositivo("  ")
        assertEquals(listOf("renovar:token-nuevo"), eventos)
    }

    @Test
    fun alCerrarSesionOlvidaElCelularAntesDeSalir() {
        CerrarSesionUseCase(auth, dispositivos)()
        assertEquals(listOf("olvidar", "cerrarSesion"), eventos)
    }
}
