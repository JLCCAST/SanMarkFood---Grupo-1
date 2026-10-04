package com.equipo.sanmarkfood.restaurante.data.repository

import com.equipo.sanmarkfood.restaurante.data.firebase.AuthDataSource
import com.equipo.sanmarkfood.restaurante.data.firebase.FotosDataSource
import com.equipo.sanmarkfood.restaurante.data.firebase.MenuDataSource
import com.equipo.sanmarkfood.restaurante.data.local.LectorImagenes
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.ErrorRestaurante
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
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MenuRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val menuDataSource: MenuDataSource,
    private val fotosDataSource: FotosDataSource,
    private val lectorImagenes: LectorImagenes,
) : MenuRepository {

    override fun observarCategorias(): Flow<List<Categoria>> =
        flow { emitAll(menuDataSource.observarCategorias(uid())) }

    override suspend fun leerCategorias(): List<Categoria> = menuDataSource.leerCategorias(uid())

    override suspend fun crearCategoria(nombre: String, orden: Int): Categoria =
        menuDataSource.crearCategoria(uid(), nombre, orden)

    override fun observarPlatos(): Flow<List<Plato>> = flow { emitAll(menuDataSource.observarPlatos(uid())) }

    override suspend fun obtenerPlato(id: String): Plato? = menuDataSource.leerPlato(uid(), id)

    override suspend fun crearPlato(datos: DatosPlato, fotoLocal: String?) {
        val uid = uid()
        val id = menuDataSource.nuevoIdPlato(uid)
        val fotoUrl = fotoLocal?.let { subirFotoPlato(uid, id, it) }
        menuDataSource.crearPlato(uid, id, datos, fotoUrl)
        actualizarRangoCarta(uid)
    }

    override suspend fun actualizarPlato(plato: Plato, datos: DatosPlato, fotoLocal: String?) {
        val uid = uid()
        val fotoNueva = fotoLocal?.let { subirFotoPlato(uid, plato.id, it) }
        menuDataSource.actualizarPlato(uid, plato.id, datos, fotoNueva)
        if (fotoNueva != null) plato.fotoUrl?.let { borrarFoto(it) }
        actualizarRangoCarta(uid)
    }

    override suspend fun eliminarPlato(plato: Plato) {
        val uid = uid()
        if (menuDataSource.existePlatoEnServidor(uid, plato.id)) menuDataSource.eliminarPlato(uid, plato.id)
        plato.fotoUrl?.let { borrarFoto(it) }
        actualizarRangoCarta(uid)
    }

    override suspend fun cambiarAgotadoPlato(platoId: String, agotadoEl: String?) {
        val uid = uid()
        if (!menuDataSource.existePlatoEnServidor(uid, platoId)) throw ErrorMenu.Desconocido
        menuDataSource.cambiarAgotadoPlato(uid, platoId, agotadoEl)
    }

    override fun observarMenu(fecha: String): Flow<MenuDelDia?> =
        flow { emitAll(menuDataSource.observarMenu(uid(), fecha)) }

    override suspend fun publicarMenu(fecha: String, datos: DatosMenu, origen: OrigenMenu) {
        if (!menuDataSource.publicarMenu(uid(), fecha, datos, origen)) throw ErrorMenu.MenuYaPublicado
    }

    override suspend fun guardarOpcionesMenu(fecha: String, entradas: List<OpcionMenu>, segundos: List<OpcionMenu>) {
        val uid = uid()
        if (!menuDataSource.existeMenuEnServidor(uid, fecha)) throw ErrorMenu.Desconocido
        menuDataSource.guardarOpcionesMenu(uid, fecha, entradas, segundos)
    }

    private suspend fun subirFotoPlato(uid: String, platoId: String, imagenLocal: String): String {
        val jpeg = try {
            lectorImagenes.leerComoJpeg(imagenLocal, LADO_MAXIMO_FOTO)
        } catch (e: ErrorRestaurante) {
            throw ErrorMenu.ImagenIlegible
        }
        return fotosDataSource.subirFotoPlato(uid, "$platoId-${System.currentTimeMillis()}.jpg", jpeg)
    }

    private suspend fun borrarFoto(url: String) {
        try {
            fotosDataSource.borrarFotoPlato(url)
        } catch (e: ErrorMenu) {
        } catch (e: IllegalArgumentException) {
        }
    }

    private suspend fun actualizarRangoCarta(uid: String) {
        try {
            val precios = menuDataSource.leerPrecios(uid)
            if (precios.isEmpty()) {
                menuDataSource.quitarRangoCarta(uid)
            } else {
                menuDataSource.guardarRangoCarta(uid, precios.min(), precios.max())
            }
        } catch (e: ErrorMenu) {
        }
    }

    private fun uid(): String = authDataSource.usuarioActual()?.uid ?: throw ErrorMenu.Desconocido

    private companion object {
        const val LADO_MAXIMO_FOTO = 1024
    }
}
