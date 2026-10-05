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
import org.junit.Assert.fail
import org.junit.Test

class GuardarDatosLocalUseCaseTest {

    private class RepositorioFalso : RestauranteRepository {
        var guardados: DatosLocal? = null
        override suspend fun obtener(): Restaurante? = null
        override fun observar(): Flow<Restaurante?> = emptyFlow()
        override suspend fun guardarDatos(datos: DatosLocal) {
            guardados = datos
        }
        override suspend fun enviarARevision(horario: Horario) = Unit
        override suspend fun subirFoto(tipo: TipoFoto, imagenLocal: String, alAvanzar: (Float) -> Unit) = ""
        override suspend fun cambiarPausa(pausado: Boolean) = Unit
        override suspend fun reenviarARevision(datos: DatosLocal) = Unit
        override suspend fun guardarHorario(horario: Horario) = Unit
    }

    private class VerificacionFalsa : VerificacionTelefonoRepository {
        var verificado: String? = TELEFONO_VERIFICADO
        override fun telefonoVerificado(): String? = verificado
        override fun enviarCodigo(telefono: String, reenviar: Boolean): Flow<EventoVerificacion> = emptyFlow()
        override suspend fun verificarCodigo(codigo: String) = Unit
    }

    private val repositorio = RepositorioFalso()
    private val verificacion = VerificacionFalsa()
    private val guardar = GuardarDatosLocalUseCase(repositorio, verificacion)

    private fun guardarConTelefono(telefono: String, portadaUrl: String? = PORTADA) = runBlocking {
        guardar(
            nombre = "  La Sazón de Doña Carmen ",
            categoria = CategoriaRestaurante.CRIOLLA,
            direccion = "Av. Venezuela 3450",
            ubicacion = Ubicacion.CiudadUniversitaria,
            telefono = telefono,
            portadaUrl = portadaUrl,
            logoUrl = null,
        )
    }

    private fun camposInvalidosCon(telefono: String): Set<CampoLocal> = try {
        guardarConTelefono(telefono)
        fail("Se esperaba DatosInvalidos para «$telefono»")
        emptySet()
    } catch (e: ErrorRestaurante.DatosInvalidos) {
        e.campos
    }

    @Test
    fun guardaLosDatosLimpiosYElTelefonoConElPrefijoDePeru() {
        guardarConTelefono("987 654 321")
        val datos = repositorio.guardados!!
        assertEquals("La Sazón de Doña Carmen", datos.nombre)
        assertEquals(TELEFONO_VERIFICADO, datos.telefono)
    }

    @Test
    fun aceptaCelularesDeNueveDigitos() {
        listOf(
            "987654321" to "+51987654321",
            "987-654-321" to "+51987654321",
            "912 345 678" to "+51912345678",
        ).forEach { (escrito, guardado) ->
            verificacion.verificado = guardado
            guardarConTelefono(escrito)
            assertEquals(guardado, repositorio.guardados!!.telefono)
        }
    }

    @Test
    fun rechazaLoQueNoEsUnCelularDeNueveDigitos() {
        listOf(
            "",
            "+51987654321",
            "51987654321",
            "4567890",
            "014567890",
            "887654321",
            "98765432",
            "9876543210",
            "98765432a",
            "(987) 654 321",
        ).forEach { telefono ->
            assertEquals(setOf(CampoLocal.TELEFONO), camposInvalidosCon(telefono))
        }
    }

    @Test
    fun muestraElTelefonoGuardadoSinElPrefijo() {
        guardarConTelefono("987 654 321")
        assertEquals("987654321", repositorio.guardados!!.telefonoNacional)
    }

    @Test
    fun noGuardaSiElTelefonoNoEstaVerificado() {
        verificacion.verificado = null
        val error = try {
            guardarConTelefono("987 654 321")
            null
        } catch (e: ErrorRestaurante.TelefonoSinVerificar) {
            e
        }
        assertEquals(ErrorRestaurante.TelefonoSinVerificar, error)
        assertNull(repositorio.guardados)
    }

    @Test
    fun noGuardaSiLaCuentaVerificoOtroTelefono() {
        verificacion.verificado = "+51911111111"
        val error = try {
            guardarConTelefono("987 654 321")
            null
        } catch (e: ErrorRestaurante.TelefonoSinVerificar) {
            e
        }
        assertEquals(ErrorRestaurante.TelefonoSinVerificar, error)
        assertNull(repositorio.guardados)
    }

    @Test
    fun marcaLosCamposAntesDePedirLaVerificacion() {
        verificacion.verificado = null
        assertEquals(setOf(CampoLocal.TELEFONO), camposInvalidosCon("98765"))
    }

    @Test
    fun marcaTodosLosCamposVaciosALaVez() {
        val error = try {
            runBlocking { guardar("  ", null, "", Ubicacion.CiudadUniversitaria, "", portadaUrl = null, logoUrl = null) }
            null
        } catch (e: ErrorRestaurante.DatosInvalidos) {
            e
        }
        assertEquals(CampoLocal.entries.toSet(), error?.campos)
        assertNull(repositorio.guardados)
    }

    @Test
    fun exigeLaPortadaPeroNoElLogo() {
        val error = try {
            guardarConTelefono("987654321", portadaUrl = null)
            null
        } catch (e: ErrorRestaurante.DatosInvalidos) {
            e
        }
        assertEquals(setOf(CampoLocal.PORTADA), error?.campos)

        guardarConTelefono("987654321")
        assertEquals(PORTADA, repositorio.guardados!!.portadaUrl)
        assertNull(repositorio.guardados!!.logoUrl)
    }

    private companion object {
        const val PORTADA = "https://firebasestorage.googleapis.com/v0/b/prueba/o/restaurantes%2Fuid%2Fportada-1.jpg"
        const val TELEFONO_VERIFICADO = "+51987654321"
    }
}
