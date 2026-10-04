package com.equipo.sanmarkfood.restaurante.domain.usecase.gestion_restaurante

import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.CampoLocal
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.CategoriaRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.DatosLocal
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.ErrorRestaurante
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

    private companion object {
        const val PORTADA = "https://firebasestorage.googleapis.com/v0/b/prueba/o/restaurantes%2Fuid%2Fportada-1.jpg"
    }
}
