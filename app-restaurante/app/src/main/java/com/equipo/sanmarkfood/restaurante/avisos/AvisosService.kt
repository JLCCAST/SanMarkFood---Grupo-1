package com.equipo.sanmarkfood.restaurante.avisos

import com.equipo.sanmarkfood.restaurante.domain.usecase.auth.RenovarDispositivoUseCase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AvisosService : FirebaseMessagingService() {

    @Inject
    lateinit var renovarDispositivo: RenovarDispositivoUseCase

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onNewToken(token: String) {
        renovarDispositivo(token)
    }

    override fun onMessageReceived(mensaje: RemoteMessage) {
        val aviso = mensaje.notification ?: return
        mostrarAvisoEstadoLocal(this, aviso.title, aviso.body)
    }
}
