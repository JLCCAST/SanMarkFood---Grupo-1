package com.equipo.sanmarkfood.comensal.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.equipo.sanmarkfood.comensal.data.local.dao.CarritoDao
import com.equipo.sanmarkfood.comensal.data.local.entity.ItemCarritoEntity

@Database(
    entities = [ItemCarritoEntity::class],
    version = 2,
    exportSchema = false
)
abstract class SanMarkFoodDatabase : RoomDatabase() {
    abstract fun carritoDao(): CarritoDao
}
