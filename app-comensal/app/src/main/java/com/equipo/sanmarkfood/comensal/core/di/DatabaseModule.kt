package com.equipo.sanmarkfood.comensal.core.di

import android.content.Context
import androidx.room.Room
import com.equipo.sanmarkfood.comensal.data.local.SanMarkFoodDatabase
import com.equipo.sanmarkfood.comensal.data.local.dao.CarritoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SanMarkFoodDatabase =
        Room.databaseBuilder(context, SanMarkFoodDatabase::class.java, "sanmarkfood.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideCarritoDao(database: SanMarkFoodDatabase): CarritoDao = database.carritoDao()
}