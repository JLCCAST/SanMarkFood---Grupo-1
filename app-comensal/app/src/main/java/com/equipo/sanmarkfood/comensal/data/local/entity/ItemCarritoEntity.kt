package com.equipo.sanmarkfood.comensal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "item_carrito")
data class ItemCarritoEntity(
    @PrimaryKey val platoId: String,
    val restauranteId: String,
    val restauranteNombre: String,
    val nombre: String,
    val precio: Int,
    val cantidad: Int,
    val fotoUrl: String?,
    val agregadoEn: Long
)