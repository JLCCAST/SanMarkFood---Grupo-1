package com.equipo.sanmarkfood.restaurante.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.equipo.sanmarkfood.restaurante.domain.model.menu.TipoOpcion

@Entity(
    tableName = "opcion_borrador",
    foreignKeys = [
        ForeignKey(
            entity = BorradorMenuEntity::class,
            parentColumns = ["fecha"],
            childColumns = ["fecha"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("fecha")],
)
data class OpcionBorradorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fecha: String,
    val tipo: TipoOpcion,
    val nombre: String,
    val porRevisar: Boolean,
)
