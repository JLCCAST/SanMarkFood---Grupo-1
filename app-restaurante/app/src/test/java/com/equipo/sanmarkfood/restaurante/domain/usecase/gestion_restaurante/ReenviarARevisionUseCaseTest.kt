package com.equipo.sanmarkfood.restaurante.domain.usecase.gestion_restaurante

import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.CampoLocal
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.CategoriaRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.DatosLocal
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.EventoVerificacion
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Horario
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Restaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.TipoFoto
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Ubicacion
import com.equipo.sanmarkfood.restaurante.domain.repository.RestauranteRepository
import com.equipo.sanmarkfood.restaurante.domain.repository.VerificacionTelefonoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReenviarARevisionUseCaseTest {

    private class RepositorioFalso : RestauranteRepository {
        var reenviados: DatosLocal? = null
        var guardados: DatosLocal? = null
        override suspend fun obtener(): Restaurante? = null
        override fun observar(): Flow<Restaurante?> = emptyFlow()
        override suspend fun guardarDatos(datos: DatosLocal) {
            guardados = datos
        }
        override suspend fun enviarARevision(horario: Horario) = Unit
        override suspend fun subirFoto(tipo: TipoFoto, imagenLocal: String, alAvanzar: (Float) -> Unit) = ""
        override suspend fun cambiarPausa(pausado: Boolean) = Unit
        override suspend fun reenviarARevision(datos: DatosLocal) {
            reenviados = datos
        }
        override suspend fun guardarHorario(horario: Horario) = Unit
    }

    private class VerificacionFalsa : VerificacionTelefonoRepository {
        var verificado: String? = "+51987654321"
        override fun telefonoVerificado(): String? = verificado
        override fun enviarCodigo(telefono: String, reenviar: Boolean): Flow<EventoVerificacion> = emptyFlow()
        override suspend fun verificarCodigo(codigo: String) = Unit
    }

    private val repositorio = RepositorioFalso()
    private val verificacion = VerificacionFalsa()
    private val reenviar = ReenviarARevisionUseCase(repositorio, verificacion)

    private fun reenviarCon(direccion: String) = runBlocking {
        reenviar(
            nombre = "La Sazón de Doña Carmen",
            categoria = CategoriaRestaurante.CRIOLLA,
            direccion = direccion,
            ubicacion = Ubicacion.CiudadUniversitaria,
            telefono = "987 654 321",
            portadaUrl = "https://firebasestorage.googleapis.com/v0/b/prueba/o/restaurantes%2Fuid%2Fportada-1.jpg",
            logoUrl = null,
        )
    }

    @Test
    fun reenviaLosDatosValidadosSinUsarElGuardadoNormal() {
        reenviarCon("  Av. Venezuela 3450, puerta 3 ")
        assertEquals("Av. Venezuela 3450, puerta 3", repositorio.reenviados!!.direccion)
        assertEquals("+51987654321", repositorio.reenviados!!.telefono)
        assertNull(repositorio.guardados)
    }

    @Test
    fun noReenviaSinElTelefonoVerificado() {
        verificacion.verificado = null
        val error = try {
            reenviarCon("Av. Venezuela 3450")
            null
        } catch (e: ErrorRestaurante.TelefonoSinVerificar) {
            e
        }
        assertEquals(ErrorRestaurante.TelefonoSinVerificar, error)
        assertNull(repositorio.reenviados)
    }

    @Test
    fun conDatosInvalidosNoReenvia() {
        val error = try {
            reenviarCon("   ")
            null
        } catch (e: ErrorRestaurante.DatosInvalidos) {
            e
        }
        assertEquals(setOf(CampoLocal.DIRECCION), error?.campos)
        assertNull(repositorio.reenviados)
    }
}
