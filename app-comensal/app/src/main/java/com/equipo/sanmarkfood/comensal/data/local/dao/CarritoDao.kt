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

    @Query("SELECT cantidad FROM item_carrito WHERE clave = :clave")
    suspend fun leerCantidad(clave: String): Int?

    @Insert
    suspend fun insertar(item: ItemCarritoEntity)

    @Query("UPDATE item_carrito SET cantidad = :cantidad WHERE clave = :clave")
    suspend fun actualizarCantidad(clave: String, cantidad: Int)

    @Query("DELETE FROM item_carrito WHERE clave = :clave")
    suspend fun quitar(clave: String)

    @Query("DELETE FROM item_carrito")
    suspend fun vaciar()

    @Transaction
    suspend fun agregar(item: ItemCarritoEntity) {
        val actual = leerCantidad(item.clave)
        if (actual == null) insertar(item) else actualizarCantidad(item.clave, actual + item.cantidad)
    }
}
