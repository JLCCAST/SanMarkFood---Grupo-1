package com.equipo.sanmarkfood.restaurante.core.di

import com.equipo.sanmarkfood.restaurante.data.repository.AuthRepositoryImpl
import com.equipo.sanmarkfood.restaurante.data.repository.MenuRepositoryImpl
import com.equipo.sanmarkfood.restaurante.data.repository.RestauranteRepositoryImpl
import com.equipo.sanmarkfood.restaurante.domain.repository.AuthRepository
import com.equipo.sanmarkfood.restaurante.domain.repository.MenuRepository
import com.equipo.sanmarkfood.restaurante.domain.repository.RestauranteRepository
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
    abstract fun bindRestauranteRepository(impl: RestauranteRepositoryImpl): RestauranteRepository

    @Binds
    @Singleton
    abstract fun bindMenuRepository(impl: MenuRepositoryImpl): MenuRepository
}
