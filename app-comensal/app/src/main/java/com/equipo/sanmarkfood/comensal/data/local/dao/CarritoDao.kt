package com.equipo.sanmarkfood.comensal.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.equipo.sanmarkfood.comensal.data.local.entity.ItemCarritoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CarritoDao {
    @Query("SELECT * FROM item_carrito ORDER BY agregadoEn")
    fun observarItems(): Flow<List<ItemCarritoEntity>>

    @Query("SELECT * FROM item_carrito ORDER BY agregadoEn")
    suspend fun leerItems(): List<ItemCarritoEntity>

    @Query("SELECT cantidad FROM item_carrito WHERE platoId = :platoId")
    suspend fun leerCantidad(platoId: String): Int?

    @Insert
    suspend fun insertar(item: ItemCarritoEntity)

    @Query("UPDATE item_carrito SET cantidad = :cantidad WHERE platoId = :platoId")
    suspend fun actualizarCantidad(platoId: String, cantidad: Int)

    @Query("DELETE FROM item_carrito WHERE platoId = :platoId")
    suspend fun quitar(platoId: String)

    @Query("DELETE FROM item_carrito")
    suspend fun vaciar()

    // Si el plato ya está, suma la cantidad; si no, lo inserta. Todo en una transacción.
    @Transaction
    suspend fun agregar(item: ItemCarritoEntity) {
        val actual = leerCantidad(item.platoId)
        if (actual == null) insertar(item) else actualizarCantidad(item.platoId, actual + item.cantidad)
    }
}