package com.equipo.sanmarkfood.restaurante.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.equipo.sanmarkfood.restaurante.data.local.dao.BorradorMenuDao
import com.equipo.sanmarkfood.restaurante.data.local.entity.BorradorMenuEntity
import com.equipo.sanmarkfood.restaurante.data.local.entity.OpcionBorradorEntity

@Database(
    entities = [BorradorMenuEntity::class, OpcionBorradorEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class SanMarkFoodDatabase : RoomDatabase() {
    abstract fun borradorMenuDao(): BorradorMenuDao
}
