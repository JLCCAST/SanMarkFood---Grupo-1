package com.equipo.sanmarkfood.restaurante

import android.app.Application
import com.equipo.sanmarkfood.restaurante.data.firebase.ActividadActual
import com.equipo.sanmarkfood.restaurante.avisos.crearCanalesDeAvisos
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class RestauranteApp : Application() {

    @Inject
    lateinit var actividadActual: ActividadActual

    override fun onCreate() {
        super.onCreate()
        Firebase.appCheck.installAppCheckProviderFactory(proveedorAppCheck())
        registerActivityLifecycleCallbacks(actividadActual)
        crearCanalesDeAvisos(this)
    }
}