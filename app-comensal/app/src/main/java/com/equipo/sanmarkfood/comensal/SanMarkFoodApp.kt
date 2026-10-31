package com.equipo.sanmarkfood.comensal

import android.app.Application
import com.equipo.sanmarkfood.comensal.avisos.crearCanalesDeAvisos
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SanMarkFoodApp : Application() {
    override fun onCreate() {
        super.onCreate()
        crearCanalesDeAvisos(this)
    }
}
