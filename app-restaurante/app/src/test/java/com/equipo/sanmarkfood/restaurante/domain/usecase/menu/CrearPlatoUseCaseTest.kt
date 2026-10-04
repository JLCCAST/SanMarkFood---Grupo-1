package com.equipo.sanmarkfood.restaurante.domain.usecase.menu

import com.equipo.sanmarkfood.restaurante.domain.model.menu.CampoPlato
import com.equipo.sanmarkfood.restaurante.domain.model.menu.Categoria
import com.equipo.sanmarkfood.restaurante.domain.model.menu.DatosMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.DatosPlato
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.MenuDelDia
import com.equipo.sanmarkfood.restaurante.domain.model.menu.OpcionMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.OrigenMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.Plato
import com.equipo.sanmarkfood.restaurante.domain.repository.MenuRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CrearPlatoUseCaseTest {

    private class RepositorioFalso : MenuRepository {
        var creado: DatosPlato? = null
        override fun observarCategorias(): Flow<List<Categoria>> = emptyFlow()
        override suspend fun leerCategorias(): List<Categoria> = listOf(Categoria("platos", "Platos", 0))
        override suspend fun crearCategoria(nombre: String, orden: Int): Categoria = Categoria("x", nombre, orden)
        override fun observarPlatos(): Flow<List<Plato>> = emptyFlow()
        override suspend fun obtenerPlato(id: String): Plato? = null
        override suspend fun crearPlato(datos: DatosPlato, fotoLocal: String?) {
            creado = datos
        }
        override suspend fun actualizarPlato(plato: Plato, datos: DatosPlato, fotoLocal: String?) = Unit
        override suspend fun eliminarPlato(plato: Plato) = Unit
        override fun observarMenu(fecha: String): Flow<MenuDelDia?> = emptyFlow()
        override suspend fun leerMenu(fecha: String): MenuDelDia? = null
        override suspend fun publicarMenu(fecha: String, datos: DatosMenu, origen: OrigenMenu) = Unit
        override suspend fun cambiarAgotadoPlato(platoId: String, agotadoEl: String?) = Unit
        override suspend fun guardarOpcionesMenu(fecha: String, entradas: List<OpcionMenu>, segundos: List<OpcionMenu>) = Unit
    }

    private val repositorio = RepositorioFalso()
    private val crear = CrearPlatoUseCase(repositorio)

    private fun precioGuardado(precio: String): Int? {
        runBlocking { crear("Lomo saltado", "", precio, "platos", null) }
        return repositorio.creado?.precio
    }

    private fun camposInvalidos(nombre: String = "Lomo saltado", precio: String = "24", categoriaId: String? = "platos") =
        try {
            runBlocking { crear(nombre, "", precio, categoriaId, null) }
            emptySet()
        } catch (e: ErrorMenu.DatosPlatoInvalidos) {
            e.campos
        }

    @Test
    fun guardaElPrecioEnCentimos() {
        assertEquals(2400, precioGuardado("24"))
        assertEquals(2450, precioGuardado("24.5"))
        assertEquals(2450, precioGuardado("24,50"))
        assertEquals(2400, precioGuardado(" 24. "))
        assertEquals(50, precioGuardado(".50"))
        assertEquals(100_000, precioGuardado("1000"))
    }

    @Test
    fun rechazaPreciosInvalidos() {
        listOf("", "0", "0.00", "abc", "24.505", "1.2.3", "1000.01", "-5", "S/ 24").forEach { precio ->
            assertEquals(precio, setOf(CampoPlato.PRECIO), camposInvalidos(precio = precio))
        }
        assertNull(repositorio.creado)
    }

    @Test
    fun marcaTodosLosCamposQueFaltan() {
        assertEquals(
            setOf(CampoPlato.NOMBRE, CampoPlato.PRECIO, CampoPlato.CATEGORIA),
            camposInvalidos(nombre = "  ", precio = "", categoriaId = null),
        )
    }

    @Test
    fun rechazaUnaCategoriaQueYaNoExiste() {
        assertEquals(setOf(CampoPlato.CATEGORIA), camposInvalidos(categoriaId = "borrada"))
        assertNull(repositorio.creado)
    }

    @Test
    fun recortaLosTextosYOmiteLaDescripcionVacia() {
        runBlocking { crear("  Lomo saltado ", "   ", "24", "platos", null) }
        assertEquals(DatosPlato("Lomo saltado", null, 2400, "platos"), repositorio.creado)
    }
}
