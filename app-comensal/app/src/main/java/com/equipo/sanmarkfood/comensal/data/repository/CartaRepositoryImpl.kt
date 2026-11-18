package com.equipo.sanmarkfood.comensal.data.repository

import com.equipo.sanmarkfood.comensal.data.firebase.CartaDataSource
import com.equipo.sanmarkfood.comensal.data.firebase.aErrorPedidos
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.CategoriaCarta
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MenuDeHoy
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Plato
import com.equipo.sanmarkfood.comensal.domain.repository.CartaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import javax.inject.Inject

class CartaRepositoryImpl @Inject constructor(
    private val dataSource: CartaDataSource,
) : CartaRepository {

    override fun observarCategorias(restauranteId: String): Flow<List<CategoriaCarta>> =
        dataSource.observarCategorias(restauranteId).catch { throw it.aErrorPedidos() }

    override fun observarPlatos(restauranteId: String): Flow<List<Plato>> =
        dataSource.observarPlatos(restauranteId).catch { throw it.aErrorPedidos() }

    override fun observarMenu(restauranteId: String, fecha: String): Flow<MenuDeHoy?> =
        dataSource.observarMenu(restauranteId, fecha).catch { throw it.aErrorPedidos() }
}