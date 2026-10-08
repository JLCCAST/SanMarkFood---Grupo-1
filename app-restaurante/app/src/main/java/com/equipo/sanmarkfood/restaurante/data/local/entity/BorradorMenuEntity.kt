package com.equipo.sanmarkfood.restaurante.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.equipo.sanmarkfood.restaurante.domain.model.menu.OrigenMenu

@Entity(tableName = "borrador_menu")
data class BorradorMenuEntity(
    @PrimaryKey val fecha: String,
    val origen: OrigenMenu,
    val precio: String,
    val refresco: String,
    val postre: String,
    val horaFin: String,
)
