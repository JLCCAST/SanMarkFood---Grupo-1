package com.equipo.sanmarkfood.comensal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TipoItemCarrito { PLATO, MENU }

@Entity(tableName = "item_carrito")
data class ItemCarritoEntity(
    @PrimaryKey val clave: String,
    val restauranteId: String,
    val restauranteNombre: String,
    val tipo: TipoItemCarrito,
    val platoId: String?,
    val nombre: String?,
    val fotoUrl: String?,
    val fecha: String?,
    val entrada: String?,
    val segundo: String?,
    val precio: Int,
    val cantidad: Int,
    val agregadoEn: Long
)
