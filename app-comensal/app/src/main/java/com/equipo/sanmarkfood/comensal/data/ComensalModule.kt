package com.equipo.sanmarkfood.comensal.data

import com.equipo.sanmarkfood.comensal.data.repository.ComensalRepositoryImpl
import com.equipo.sanmarkfood.comensal.domain.repository.ComensalRepository
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class ComensalModule {

    @Binds
    @Singleton
    abstract fun bindComensalRepository(impl: ComensalRepositoryImpl): ComensalRepository

    companion object {
        @Provides
        @Singleton
        fun provideStorage(): FirebaseStorage = FirebaseStorage.getInstance()
    }
}