package com.equipo.sanmarkfood.restaurante.domain.usecase.menu

import com.equipo.sanmarkfood.restaurante.domain.model.menu.Categoria
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.equipo.sanmarkfood.restaurante.domain.repository.MenuRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CrearCategoriaUseCaseTest {

    private class RepositorioFalso : MenuRepository {
        var existentes: List<Categoria> = emptyList()
        var errorAlLeer: ErrorMenu? = null
        var creada: Categoria? = null
        override fun observarCategorias(): Flow<List<Categoria>> = emptyFlow()
        override suspend fun leerCategorias(): List<Categoria> {
            errorAlLeer?.let { throw it }
            return existentes
        }
        override suspend fun crearCategoria(nombre: String, orden: Int): Categoria =
            Categoria(id = "nueva", nombre = nombre, orden = orden).also { creada = it }
    }

    private val repositorio = RepositorioFalso()
    private val crear = CrearCategoriaUseCase(repositorio)

    private fun errorAlCrear(nombre: String): ErrorMenu? =
        try {
            runBlocking { crear(nombre) }
            null
        } catch (e: ErrorMenu) {
            e
        }

    @Test
    fun laPrimeraCategoriaTieneOrdenCero() {
        val categoria = runBlocking { crear("Platos") }
        assertEquals(0, categoria.orden)
    }

    @Test
    fun laNuevaVaDespuesDeLaUltima() {
        repositorio.existentes = listOf(Categoria("a", "Platos", 0), Categoria("b", "Bebidas", 3))
        val categoria = runBlocking { crear("Postres") }
        assertEquals(4, categoria.orden)
    }

    @Test
    fun guardaElNombreSinEspaciosAlRededor() {
        runBlocking { crear("  Postres  ") }
        assertEquals("Postres", repositorio.creada?.nombre)
    }

    @Test
    fun rechazaUnNombreVacioOEnBlanco() {
        assertEquals(ErrorMenu.NombreCategoriaInvalido, errorAlCrear(""))
        assertEquals(ErrorMenu.NombreCategoriaInvalido, errorAlCrear("   "))
        assertNull(repositorio.creada)
    }

    @Test
    fun aceptaHastaCuarentaCaracteresYRechazaMas() {
        runBlocking { crear("a".repeat(Categoria.MAX_NOMBRE)) }
        assertEquals(Categoria.MAX_NOMBRE, repositorio.creada?.nombre?.length)

        repositorio.creada = null
        assertEquals(ErrorMenu.NombreCategoriaInvalido, errorAlCrear("a".repeat(Categoria.MAX_NOMBRE + 1)))
        assertNull(repositorio.creada)
    }

    @Test
    fun rechazaUnNombreRepetidoSinImportarMayusculas() {
        repositorio.existentes = listOf(Categoria("a", "Platos", 0))
        assertEquals(ErrorMenu.CategoriaRepetida, errorAlCrear("platos "))
        assertNull(repositorio.creada)
    }

    @Test
    fun sinConexionNoCreaNada() {
        repositorio.errorAlLeer = ErrorMenu.SinConexion
        assertEquals(ErrorMenu.SinConexion, errorAlCrear("Platos"))
        assertNull(repositorio.creada)
    }
}
