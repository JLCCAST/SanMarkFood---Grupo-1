package com.equipo.sanmarkfood.restaurante.data.firebase

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActividadActual @Inject constructor() : Application.ActivityLifecycleCallbacks {
    private var actividad: WeakReference<Activity>? = null

    fun obtener(): Activity? = actividad?.get()

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = recordar(activity)

    override fun onActivityStarted(activity: Activity) = recordar(activity)

    override fun onActivityResumed(activity: Activity) = recordar(activity)

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) {
        if (actividad?.get() === activity) actividad = null
    }

    private fun recordar(activity: Activity) {
        actividad = WeakReference(activity)
    }
}
