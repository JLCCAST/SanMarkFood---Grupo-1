package com.equipo.sanmarkfood.restaurante.data.repository

import com.equipo.sanmarkfood.restaurante.data.firebase.AuthDataSource
import com.equipo.sanmarkfood.restaurante.data.firebase.MenuDataSource
import com.equipo.sanmarkfood.restaurante.domain.model.menu.Categoria
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.equipo.sanmarkfood.restaurante.domain.repository.MenuRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MenuRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val menuDataSource: MenuDataSource,
) : MenuRepository {

    override fun observarCategorias(): Flow<List<Categoria>> =
        flow { emitAll(menuDataSource.observarCategorias(uid())) }

    override suspend fun leerCategorias(): List<Categoria> = menuDataSource.leerCategorias(uid())

    override suspend fun crearCategoria(nombre: String, orden: Int): Categoria =
        menuDataSource.crearCategoria(uid(), nombre, orden)

    private fun uid(): String = authDataSource.usuarioActual()?.uid ?: throw ErrorMenu.Desconocido
}
