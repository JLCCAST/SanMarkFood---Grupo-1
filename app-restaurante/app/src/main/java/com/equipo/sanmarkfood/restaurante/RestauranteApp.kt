package com.equipo.sanmarkfood.restaurante

import android.app.Application
import com.equipo.sanmarkfood.restaurante.avisos.crearCanalesDeAvisos
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RestauranteApp : Application() {

    override fun onCreate() {
        super.onCreate()
        crearCanalesDeAvisos(this)
    }
}
