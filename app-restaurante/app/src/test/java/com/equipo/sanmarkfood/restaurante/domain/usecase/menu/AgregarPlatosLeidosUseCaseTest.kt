package com.equipo.sanmarkfood.restaurante.domain.usecase.menu

import com.equipo.sanmarkfood.restaurante.domain.model.menu.Categoria
import com.equipo.sanmarkfood.restaurante.domain.model.menu.DatosMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.DatosPlato
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.EstadoMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.MenuDelDia
import com.equipo.sanmarkfood.restaurante.domain.model.menu.OpcionMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.OrigenMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.Plato
import com.equipo.sanmarkfood.restaurante.domain.model.menu.PlatoLeido
import com.equipo.sanmarkfood.restaurante.domain.repository.MenuRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class AgregarPlatosLeidosUseCaseTest {

    private class RepositorioFalso(
        val categorias: MutableList<Categoria> = mutableListOf(Categoria("fondo", "Platos de fondo", 0)),
        private val platos: List<Plato> = emptyList(),
    ) : MenuRepository {
        val creados = mutableListOf<DatosPlato>()
        override fun observarCategorias(): Flow<List<Categoria>> = emptyFlow()
        override suspend fun leerCategorias(): List<Categoria> = categorias.toList()
        override suspend fun crearCategoria(nombre: String, orden: Int): Categoria =
            Categoria("cat-$orden", nombre, orden).also { categorias += it }
        override fun observarPlatos(): Flow<List<Plato>> = flowOf(platos)
        override suspend fun obtenerPlato(id: String): Plato? = null
        override suspend fun crearPlato(datos: DatosPlato, fotoLocal: String?) {
            creados += datos
        }
        override suspend fun actualizarPlato(plato: Plato, datos: DatosPlato, fotoLocal: String?) = Unit
        override suspend fun eliminarPlato(plato: Plato) = Unit
        override suspend fun cambiarAgotadoPlato(platoId: String, agotadoEl: String?) = Unit
        override fun observarMenu(fecha: String): Flow<MenuDelDia?> = emptyFlow()
        override suspend fun leerMenu(fecha: String): MenuDelDia? = null
        override suspend fun publicarMenu(fecha: String, datos: DatosMenu, origen: OrigenMenu) = Unit
        override suspend fun actualizarMenu(fecha: String, datos: DatosMenu, estado: EstadoMenu) = Unit
        override suspend fun guardarOpcionesMenu(fecha: String, entradas: List<OpcionMenu>, segundos: List<OpcionMenu>) = Unit
    }

    @Test
    fun usaLasCategoriasQueYaExistenYCreaLasNuevas() {
        val repositorio = RepositorioFalso()
        val agregados = runBlocking {
            AgregarPlatosLeidosUseCase(repositorio)(
                listOf(
                    PlatoLeido("Lomo saltado", "24.00", "platos de fondo"),
                    PlatoLeido("Chicha morada 1 L", "8", "Bebidas"),
                    PlatoLeido("Limonada", "7.50", "Bebidas"),
                )
            )
        }

        assertEquals(3, agregados)
        assertEquals(listOf("Platos de fondo", "Bebidas"), repositorio.categorias.map { it.nombre })
        assertEquals(1, repositorio.categorias.last().orden)
        assertEquals(listOf("fondo", "cat-1", "cat-1"), repositorio.creados.map { it.categoriaId })
        assertEquals(listOf(2400, 800, 750), repositorio.creados.map { it.precio })
    }

    @Test
    fun noRepiteLosPlatosQueYaEstanEnLaCarta() {
        val existente = Plato("p1", DatosPlato("Lomo saltado", null, 2400, "fondo"), null)
        val repositorio = RepositorioFalso(platos = listOf(existente))
        val agregados = runBlocking {
            AgregarPlatosLeidosUseCase(repositorio)(
                listOf(
                    PlatoLeido("lomo saltado ", "25.00", "Platos de fondo"),
                    PlatoLeido("Ají de gallina", "18", "Platos de fondo"),
                    PlatoLeido("Ají de gallina", "18", "Platos de fondo"),
                )
            )
        }

        assertEquals(1, agregados)
        assertEquals(listOf("Ají de gallina"), repositorio.creados.map { it.nombre })
    }

    @Test
    fun usaPlatosSiLaCategoriaVieneVaciaYRecortaLasLargas() {
        val repositorio = RepositorioFalso(categorias = mutableListOf())
        runBlocking {
            AgregarPlatosLeidosUseCase(repositorio)(
                listOf(
                    PlatoLeido("Causa", "12", "  "),
                    PlatoLeido("Tiradito", "20", "C".repeat(60)),
                )
            )
        }

        assertEquals(listOf("Platos", "C".repeat(Categoria.MAX_NOMBRE)), repositorio.categorias.map { it.nombre })
    }

    @Test
    fun rechazaLasFilasSinNombreOConPrecioInvalidoSinAgregarNada() {
        val repositorio = RepositorioFalso()
        try {
            runBlocking {
                AgregarPlatosLeidosUseCase(repositorio)(
                    listOf(
                        PlatoLeido("Lomo saltado", "24", "Platos"),
                        PlatoLeido("Tallarines verdes", "2?.00", "Platos"),
                        PlatoLeido(" ", "10", "Platos"),
                        PlatoLeido("Ceviche", "", "Platos"),
                    )
                )
            }
            fail("Debió rechazar las filas inválidas")
        } catch (e: ErrorMenu.PlatosLeidosInvalidos) {
            assertEquals(setOf(1, 2, 3), e.filas)
        }
        assertTrue(repositorio.creados.isEmpty())
        assertEquals(1, repositorio.categorias.size)
    }
}
