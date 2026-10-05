package com.equipo.sanmarkfood.restaurante.domain.usecase.gestion_restaurante

import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.CampoLocal
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.CategoriaRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.DatosLocal
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.EstadoRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Horario
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Restaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.TipoFoto
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Ubicacion
import com.equipo.sanmarkfood.restaurante.domain.repository.RestauranteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test

class GuardarDatosLocalUseCaseTest {

    private class RepositorioFalso : RestauranteRepository {
        var actual: Restaurante? = null
        var guardados: DatosLocal? = null
        var pedidosARevision: DatosLocal? = null
        override suspend fun obtener(): Restaurante? = actual
        override fun observar(): Flow<Restaurante?> = emptyFlow()
        override suspend fun guardarDatos(datos: DatosLocal) {
            guardados = datos
        }
        override suspend fun guardarDatosYPedirRevision(datos: DatosLocal) {
            pedidosARevision = datos
        }
        override suspend fun enviarARevision(horario: Horario) = Unit
        override suspend fun subirFoto(tipo: TipoFoto, imagenLocal: String, alAvanzar: (Float) -> Unit) = ""
        override suspend fun cambiarPausa(pausado: Boolean) = Unit
        override suspend fun reenviarARevision(datos: DatosLocal) = Unit
        override suspend fun guardarHorario(horario: Horario) = Unit
    }

    private val repositorio = RepositorioFalso()
    private val guardar = GuardarDatosLocalUseCase(repositorio)

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
    fun guardaLosDatosLimpiosYElTelefonoSoloConDigitos() {
        guardarConTelefono("+51 987 654 321")
        val datos = repositorio.guardados!!
        assertEquals("La Sazón de Doña Carmen", datos.nombre)
        assertEquals("987654321", datos.telefono)
    }

    @Test
    fun aceptaCelularFijoDeLimaYFijoConCodigo() {
        listOf("987-654-321" to "987654321", "456 7890" to "4567890", "(01) 456-7890" to "014567890", "044 123456" to "044123456")
            .forEach { (escrito, guardado) ->
                guardarConTelefono(escrito)
                assertEquals(guardado, repositorio.guardados!!.telefono)
            }
    }

    @Test
    fun rechazaTelefonosInvalidos() {
        listOf("", "98765", "887654321", "9876543210", "98765432a", "12345678").forEach { telefono ->
            assertEquals(setOf(CampoLocal.TELEFONO), camposInvalidosCon(telefono))
        }
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

    private fun localEn(estado: EstadoRestaurante) =
        Restaurante(datos = DATOS_GUARDADOS, estado = estado, horario = null, rechazo = null, pausado = false)

    private fun guardarEdicion(cambio: (DatosLocal) -> DatosLocal) = runBlocking {
        val editados = cambio(DATOS_GUARDADOS)
        guardar(
            editados.nombre,
            editados.categoria,
            editados.direccion,
            editados.ubicacion,
            editados.telefono,
            editados.portadaUrl,
            editados.logoUrl,
        )
    }

    @Test
    fun unLocalAprobadoQueCambiaCamposMenoresSigueAprobado() {
        repositorio.actual = localEn(EstadoRestaurante.APROBADO)
        guardarEdicion { it.copy(nombre = "La Sazón de Carmen", categoria = CategoriaRestaurante.MARINA, telefono = "912345678") }
        assertEquals("La Sazón de Carmen", repositorio.guardados!!.nombre)
        assertNull(repositorio.pedidosARevision)
    }

    @Test
    fun unLocalAprobadoQueCambiaCamposSensiblesVuelveARevision() {
        listOf<(DatosLocal) -> DatosLocal>(
            { it.copy(direccion = "Av. Universitaria 1801") },
            { it.copy(ubicacion = Ubicacion(-12.06, -77.08)) },
            { it.copy(portadaUrl = OTRA_PORTADA) },
            { it.copy(logoUrl = LOGO) },
            { it.copy(nombre = "La Sazón de Carmen", direccion = "Av. Universitaria 1801") },
        ).forEach { cambio ->
            repositorio.actual = localEn(EstadoRestaurante.APROBADO)
            repositorio.pedidosARevision = null
            guardarEdicion(cambio)
            assertEquals(cambio(DATOS_GUARDADOS), repositorio.pedidosARevision)
        }
        assertNull(repositorio.guardados)
    }

    @Test
    fun avisaAntesDeGuardarSoloSiUnLocalAprobadoCambiaCamposSensibles() {
        val requiereNuevaRevision = RequiereNuevaRevisionUseCase(repositorio)
        fun consultar(cambio: (DatosLocal) -> DatosLocal): Boolean = runBlocking {
            val editados = cambio(DATOS_GUARDADOS)
            requiereNuevaRevision(
                editados.nombre,
                editados.categoria,
                editados.direccion,
                editados.ubicacion,
                editados.telefono,
                editados.portadaUrl,
                editados.logoUrl,
            )
        }

        repositorio.actual = localEn(EstadoRestaurante.APROBADO)
        assertEquals(true, consultar { it.copy(portadaUrl = OTRA_PORTADA) })
        assertEquals(false, consultar { it.copy(nombre = "La Sazón de Carmen") })

        repositorio.actual = localEn(EstadoRestaurante.PENDIENTE)
        assertEquals(false, consultar { it.copy(portadaUrl = OTRA_PORTADA) })

        repositorio.actual = null
        assertEquals(false, consultar { it.copy(portadaUrl = OTRA_PORTADA) })
        assertNull(repositorio.guardados)
        assertNull(repositorio.pedidosARevision)
    }

    @Test
    fun soloUnLocalAprobadoVuelveARevision() {
        listOf(null, EstadoRestaurante.BORRADOR, EstadoRestaurante.PENDIENTE, EstadoRestaurante.RECHAZADO).forEach { estado ->
            repositorio.actual = estado?.let { localEn(it) }
            guardarEdicion { it.copy(direccion = "Av. Universitaria 1801") }
            assertEquals("Av. Universitaria 1801", repositorio.guardados!!.direccion)
        }
        assertNull(repositorio.pedidosARevision)
    }

    private companion object {
        const val PORTADA = "https://firebasestorage.googleapis.com/v0/b/prueba/o/restaurantes%2Fuid%2Fportada-1.jpg"
        const val OTRA_PORTADA = "https://firebasestorage.googleapis.com/v0/b/prueba/o/restaurantes%2Fuid%2Fportada-2.jpg"
        const val LOGO = "https://firebasestorage.googleapis.com/v0/b/prueba/o/restaurantes%2Fuid%2Flogo-1.jpg"
        val DATOS_GUARDADOS = DatosLocal(
            nombre = "La Sazón de Doña Carmen",
            categoria = CategoriaRestaurante.CRIOLLA,
            direccion = "Av. Venezuela 3450",
            ubicacion = Ubicacion.CiudadUniversitaria,
            telefono = "987654321",
            portadaUrl = PORTADA,
            logoUrl = null,
        )
    }
}
