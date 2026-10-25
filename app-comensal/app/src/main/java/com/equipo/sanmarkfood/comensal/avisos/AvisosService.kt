package com.equipo.sanmarkfood.comensal.avisos

import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.domain.usecase.auth.RenovarDispositivoUseCase
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
        val clave = mensaje.data["pedidoId"] ?: mensaje.data["reservaId"] ?: mensaje.data["tipo"]
        mostrarAviso(
            context = this,
            canal = aviso.channelId ?: getString(R.string.canal_pedidos_id),
            id = clave.hashCode(),
            titulo = aviso.title,
            texto = aviso.body
        )
    }
}
