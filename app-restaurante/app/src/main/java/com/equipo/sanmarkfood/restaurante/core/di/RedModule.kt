package com.equipo.sanmarkfood.restaurante.core.di

import com.equipo.sanmarkfood.restaurante.data.remote.AutenticacionInterceptor
import com.equipo.sanmarkfood.restaurante.data.remote.DatosInicialesApi
import com.equipo.sanmarkfood.restaurante.data.remote.FuncionesApi
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
import java.util.concurrent.TimeUnit
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
    fun provideDatosInicialesApi(cliente: OkHttpClient, json: Json): DatosInicialesApi = Retrofit.Builder()
        .baseUrl("https://us-central1-${FirebaseApp.getInstance().options.projectId}.cloudfunctions.net/")
        .client(cliente.newBuilder().readTimeout(ESPERA_DATOS_INICIALES, TimeUnit.SECONDS).build())
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(DatosInicialesApi::class.java)

    private const val ESPERA_DATOS_INICIALES = 120L
}
