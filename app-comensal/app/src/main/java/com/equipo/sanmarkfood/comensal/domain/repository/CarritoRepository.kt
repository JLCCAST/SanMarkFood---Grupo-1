package com.equipo.sanmarkfood.comensal.domain.repository

import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Carrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ItemCarrito
import kotlinx.coroutines.flow.Flow

interface CarritoRepository {

    fun observarCarrito(): Flow<Carrito?>

    suspend fun leerCarrito(): Carrito?

    suspend fun agregar(restauranteId: String, restauranteNombre: String, item: ItemCarrito)

    suspend fun cambiarCantidad(clave: String, cantidad: Int)

    suspend fun quitar(clave: String)

    suspend fun vaciar()
}
