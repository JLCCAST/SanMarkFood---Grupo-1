package com.equipo.sanmarkfood.comensal.core.di

import com.equipo.sanmarkfood.comensal.data.repository.AuthRepositoryImpl
import com.equipo.sanmarkfood.comensal.data.repository.ComensalRepositoryImpl
import com.equipo.sanmarkfood.comensal.data.repository.FavoritosRepositoryImpl
import com.equipo.sanmarkfood.comensal.data.repository.RestauranteRepositoryImpl
import com.equipo.sanmarkfood.comensal.data.repository.UbicacionRepositoryImpl
import com.equipo.sanmarkfood.comensal.domain.repository.AuthRepository
import com.equipo.sanmarkfood.comensal.domain.repository.ComensalRepository
import com.equipo.sanmarkfood.comensal.domain.repository.FavoritosRepository
import com.equipo.sanmarkfood.comensal.domain.repository.RestauranteRepository
import com.equipo.sanmarkfood.comensal.domain.repository.UbicacionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindComensalRepository(impl: ComensalRepositoryImpl): ComensalRepository

    @Binds
    @Singleton
    abstract fun bindRestauranteRepository(impl: RestauranteRepositoryImpl): RestauranteRepository

    @Binds
    @Singleton
    abstract fun bindUbicacionRepository(impl: UbicacionRepositoryImpl): UbicacionRepository

    @Binds
    @Singleton
    abstract fun bindFavoritosRepository(impl: FavoritosRepositoryImpl): FavoritosRepository
}
