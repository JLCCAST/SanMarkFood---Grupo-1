package com.equipo.sanmarkfood.restaurante.avisos

import com.equipo.sanmarkfood.restaurante.R
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
        val datos = mensaje.data
        if (datos["tipo"] == "pedido_nuevo") {
            mostrarAvisoPedidoNuevo(this, datos["titulo"], datos["cuerpo"])
            return
        }
        val aviso = mensaje.notification ?: return
        val canal = aviso.channelId
        if (canal == null || canal == getString(R.string.canal_estado_local_id)) {
            mostrarAvisoEstadoLocal(this, aviso.title, aviso.body)
        } else {
            val clave = datos["pedidoId"] ?: datos["reservaId"] ?: datos["tipo"]
            mostrarAviso(this, canal, clave.hashCode(), aviso.title, aviso.body)
        }
    }
}
