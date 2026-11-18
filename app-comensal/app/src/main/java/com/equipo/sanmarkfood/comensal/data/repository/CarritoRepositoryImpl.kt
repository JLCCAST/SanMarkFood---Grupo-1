package com.equipo.sanmarkfood.comensal.data.repository

import com.equipo.sanmarkfood.comensal.data.local.dao.CarritoDao
import com.equipo.sanmarkfood.comensal.data.local.entity.ItemCarritoEntity
import com.equipo.sanmarkfood.comensal.data.local.entity.TipoItemCarrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Carrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ItemCarrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MenuDelCarrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.PlatoDelCarrito
import com.equipo.sanmarkfood.comensal.domain.repository.CarritoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CarritoRepositoryImpl @Inject constructor(
    private val carritoDao: CarritoDao
) : CarritoRepository {

    override fun observarCarrito(): Flow<Carrito?> =
        carritoDao.observarItems().map { it.aCarrito() }

    override suspend fun leerCarrito(): Carrito? = carritoDao.leerItems().aCarrito()

    override suspend fun agregar(restauranteId: String, restauranteNombre: String, item: ItemCarrito) {
        carritoDao.agregar(item.aEntity(restauranteId, restauranteNombre, System.currentTimeMillis()))
    }

    override suspend fun cambiarCantidad(clave: String, cantidad: Int) {
        if (cantidad <= 0) carritoDao.quitar(clave) else carritoDao.actualizarCantidad(clave, cantidad)
    }

    override suspend fun quitar(clave: String) = carritoDao.quitar(clave)

    override suspend fun vaciar() = carritoDao.vaciar()
}

private fun List<ItemCarritoEntity>.aCarrito(): Carrito? {
    val primero = firstOrNull() ?: return null
    return Carrito(
        restauranteId = primero.restauranteId,
        restauranteNombre = primero.restauranteNombre,
        items = mapNotNull { it.aItem() }
    )
}

private fun ItemCarritoEntity.aItem(): ItemCarrito? {
    return when (tipo) {
        TipoItemCarrito.PLATO -> PlatoDelCarrito(
            platoId = platoId ?: return null,
            nombre = nombre.orEmpty(),
            precio = precio,
            cantidad = cantidad,
            fotoUrl = fotoUrl
        )
        TipoItemCarrito.MENU -> MenuDelCarrito(
            fecha = fecha ?: return null,
            entrada = entrada ?: return null,
            segundo = segundo ?: return null,
            precio = precio,
            cantidad = cantidad
        )
    }
}

private fun ItemCarrito.aEntity(restauranteId: String, restauranteNombre: String, agregadoEn: Long) =
    when (this) {
        is PlatoDelCarrito -> ItemCarritoEntity(
            clave = clave,
            restauranteId = restauranteId,
            restauranteNombre = restauranteNombre,
            tipo = TipoItemCarrito.PLATO,
            platoId = platoId,
            nombre = nombre,
            fotoUrl = fotoUrl,
            fecha = null,
            entrada = null,
            segundo = null,
            precio = precio,
            cantidad = cantidad,
            agregadoEn = agregadoEn
        )
        is MenuDelCarrito -> ItemCarritoEntity(
            clave = clave,
            restauranteId = restauranteId,
            restauranteNombre = restauranteNombre,
            tipo = TipoItemCarrito.MENU,
            platoId = null,
            nombre = null,
            fotoUrl = null,
            fecha = fecha,
            entrada = entrada,
            segundo = segundo,
            precio = precio,
            cantidad = cantidad,
            agregadoEn = agregadoEn
        )
    }
