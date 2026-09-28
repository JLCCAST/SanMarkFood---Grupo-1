package com.equipo.sanmarkfood.restaurante.domain.usecase

import com.equipo.sanmarkfood.restaurante.domain.model.CampoLocal
import com.equipo.sanmarkfood.restaurante.domain.model.CategoriaRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.DatosLocal
import com.equipo.sanmarkfood.restaurante.domain.model.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.Horario
import com.equipo.sanmarkfood.restaurante.domain.model.Restaurante
import com.equipo.sanmarkfood.restaurante.domain.model.Ubicacion
import com.equipo.sanmarkfood.restaurante.domain.repository.RestauranteRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test

class GuardarDatosLocalUseCaseTest {

    private class RepositorioFalso : RestauranteRepository {
        var guardados: DatosLocal? = null
        override suspend fun obtener(): Restaurante? = null
        override suspend fun guardarDatos(datos: DatosLocal) {
            guardados = datos
        }
        override suspend fun enviarARevision(horario: Horario) = Unit
    }

    private val repositorio = RepositorioFalso()
    private val guardar = GuardarDatosLocalUseCase(repositorio)

    private fun guardarConTelefono(telefono: String) = runBlocking {
        guardar(
            nombre = "  La Sazón de Doña Carmen ",
            categoria = CategoriaRestaurante.CRIOLLA,
            direccion = "Av. Venezuela 3450",
            ubicacion = Ubicacion.CiudadUniversitaria,
            telefono = telefono,
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
            runBlocking { guardar("  ", null, "", Ubicacion.CiudadUniversitaria, "") }
            null
        } catch (e: ErrorRestaurante.DatosInvalidos) {
            e
        }
        assertEquals(CampoLocal.entries.toSet(), error?.campos)
        assertNull(repositorio.guardados)
    }
}
