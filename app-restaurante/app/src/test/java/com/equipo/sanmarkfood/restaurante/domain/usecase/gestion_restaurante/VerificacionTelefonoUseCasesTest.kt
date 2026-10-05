package com.equipo.sanmarkfood.restaurante.domain.usecase.gestion_restaurante

import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.CampoLocal
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.EventoVerificacion
import com.equipo.sanmarkfood.restaurante.domain.repository.VerificacionTelefonoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VerificacionTelefonoUseCasesTest {

    private class VerificacionFalsa : VerificacionTelefonoRepository {
        var enviadoA: String? = null
        var codigoVerificado: String? = null
        override fun telefonoVerificado(): String? = null
        override fun enviarCodigo(telefono: String, reenviar: Boolean): Flow<EventoVerificacion> {
            enviadoA = telefono
            return flowOf(EventoVerificacion.CodigoEnviado)
        }
        override suspend fun verificarCodigo(codigo: String) {
            codigoVerificado = codigo
        }
    }

    private val verificacion = VerificacionFalsa()
    private val enviarCodigo = EnviarCodigoSmsUseCase(verificacion)
    private val verificarCodigo = VerificarCodigoSmsUseCase(verificacion)

    @Test
    fun enviaElSmsAlTelefonoNormalizado() {
        val eventos = runBlocking { enviarCodigo("+51 (987) 654-321", reenviar = false).toList() }
        assertEquals("+51987654321", verificacion.enviadoA)
        assertEquals(listOf(EventoVerificacion.CodigoEnviado), eventos)
    }

    @Test
    fun noEnviaSmsAUnTelefonoInvalido() {
        val error = try {
            enviarCodigo("987 654 321", reenviar = false)
            null
        } catch (e: ErrorRestaurante.DatosInvalidos) {
            e
        }
        assertEquals(setOf(CampoLocal.TELEFONO), error?.campos)
        assertNull(verificacion.enviadoA)
    }

    @Test
    fun verificaSoloCodigosDeSeisDigitos() {
        listOf("", "12345", "1234567", "12a456", " 123456").forEach { codigo ->
            val error = try {
                runBlocking { verificarCodigo(codigo) }
                null
            } catch (e: ErrorRestaurante.CodigoIncorrecto) {
                e
            }
            assertEquals(ErrorRestaurante.CodigoIncorrecto, error)
        }
        assertNull(verificacion.codigoVerificado)

        runBlocking { verificarCodigo("482193") }
        assertEquals("482193", verificacion.codigoVerificado)
    }
}
