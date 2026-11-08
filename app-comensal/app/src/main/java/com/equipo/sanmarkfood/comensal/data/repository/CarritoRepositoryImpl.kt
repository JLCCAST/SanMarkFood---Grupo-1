package com.equipo.sanmarkfood.comensal.data.repository

import com.equipo.sanmarkfood.comensal.data.local.dao.CarritoDao
import com.equipo.sanmarkfood.comensal.data.local.entity.ItemCarritoEntity
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Carrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ItemCarrito
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
        carritoDao.agregar(
            ItemCarritoEntity(
                platoId = item.platoId,
                restauranteId = restauranteId,
                restauranteNombre = restauranteNombre,
                nombre = item.nombre,
                precio = item.precio,
                cantidad = item.cantidad,
                fotoUrl = item.fotoUrl,
                agregadoEn = System.currentTimeMillis()
            )
        )
    }

    override suspend fun cambiarCantidad(platoId: String, cantidad: Int) {
        if (cantidad <= 0) carritoDao.quitar(platoId) else carritoDao.actualizarCantidad(platoId, cantidad)
    }

    override suspend fun quitar(platoId: String) = carritoDao.quitar(platoId)

    override suspend fun vaciar() = carritoDao.vaciar()

    private fun List<ItemCarritoEntity>.aCarrito(): Carrito? {
        val primero = firstOrNull() ?: return null
        return Carrito(
            restauranteId = primero.restauranteId,
            restauranteNombre = primero.restauranteNombre,
            items = map { ItemCarrito(it.platoId, it.nombre, it.precio, it.cantidad, it.fotoUrl) }
        )
    }
}