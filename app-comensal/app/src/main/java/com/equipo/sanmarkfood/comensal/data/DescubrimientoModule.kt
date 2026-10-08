package com.equipo.sanmarkfood.comensal.data

import com.equipo.sanmarkfood.comensal.data.repository.RestauranteRepositoryImpl
import com.equipo.sanmarkfood.comensal.data.repository.UbicacionRepositoryImpl
import com.equipo.sanmarkfood.comensal.domain.repository.RestauranteRepository
import com.equipo.sanmarkfood.comensal.domain.repository.UbicacionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DescubrimientoModule {

    @Binds
    @Singleton
    abstract fun bindRestauranteRepository(impl: RestauranteRepositoryImpl): RestauranteRepository

    @Binds
    @Singleton
    abstract fun bindUbicacionRepository(impl: UbicacionRepositoryImpl): UbicacionRepository
}