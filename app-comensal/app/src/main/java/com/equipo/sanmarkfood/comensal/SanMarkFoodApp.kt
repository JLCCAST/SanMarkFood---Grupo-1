package com.equipo.sanmarkfood.comensal

import android.app.Application
import com.equipo.sanmarkfood.comensal.avisos.crearCanalesDeAvisos
import com.equipo.sanmarkfood.comensal.data.firebase.ActividadActual
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SanMarkFoodApp : Application() {

    @Inject
    lateinit var actividadActual: ActividadActual

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(actividadActual)
        crearCanalesDeAvisos(this)
    }
}
