package com.equipo.sanmarkfood.comensal.domain.repository

import com.equipo.sanmarkfood.comensal.domain.model.pedidos.Carrito
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ItemCarrito
import kotlinx.coroutines.flow.Flow

/** El carrito del comensal. Vive en el celular (Room): no se manda a Firestore hasta crear el pedido. */
interface CarritoRepository {

    fun observarCarrito(): Flow<Carrito?>

    suspend fun leerCarrito(): Carrito?

    suspend fun agregar(restauranteId: String, restauranteNombre: String, item: ItemCarrito)

    suspend fun cambiarCantidad(platoId: String, cantidad: Int)

    suspend fun quitar(platoId: String)

    suspend fun vaciar()
}