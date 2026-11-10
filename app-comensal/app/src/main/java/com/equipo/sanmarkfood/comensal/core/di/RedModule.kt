package com.equipo.sanmarkfood.comensal.core.di

import com.equipo.sanmarkfood.comensal.data.remote.AutenticacionInterceptor
import com.equipo.sanmarkfood.comensal.data.remote.FuncionesApi
import com.equipo.sanmarkfood.comensal.data.remote.GeocodingApi
import com.google.firebase.FirebaseApp
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RedModule {
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(autenticacion: AutenticacionInterceptor): OkHttpClient =
        OkHttpClient.Builder().addInterceptor(autenticacion).build()

    @Provides
    @Singleton
    fun provideFuncionesApi(cliente: OkHttpClient, json: Json): FuncionesApi = Retrofit.Builder()
        .baseUrl("https://us-central1-${FirebaseApp.getInstance().options.projectId}.cloudfunctions.net/")
        .client(cliente)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(FuncionesApi::class.java)

    @Provides
    @Singleton
    fun provideGeocodingApi(json: Json): GeocodingApi = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/")
        .client(OkHttpClient())
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(GeocodingApi::class.java)
}