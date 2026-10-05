package com.equipo.sanmarkfood.restaurante.data.firebase

import android.util.Log
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.EventoVerificacion
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VerificacionTelefonoDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val actividadActual: ActividadActual,
) {
    private var idVerificacion: String? = null
    private var tokenReenvio: PhoneAuthProvider.ForceResendingToken? = null

    fun telefonoVerificado(): String? = auth.currentUser?.phoneNumber?.takeIf { it.isNotBlank() }

    fun enviarCodigo(telefono: String, reenviar: Boolean): Flow<EventoVerificacion> = callbackFlow {
        val actividad = actividadActual.obtener() ?: throw ErrorRestaurante.Desconocido
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                idVerificacion = id
                tokenReenvio = token
                trySend(EventoVerificacion.CodigoEnviado)
            }

            override fun onVerificationCompleted(credencial: PhoneAuthCredential) {
                val codigo = credencial.smsCode
                if (codigo != null) {
                    trySend(EventoVerificacion.CodigoRecibido(codigo))
                    return
                }
                launch {
                    try {
                        aplicar(credencial)
                        send(EventoVerificacion.Verificado)
                    } catch (e: ErrorRestaurante) {
                        close(e)
                    }
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e("BINGO", "Error detectado: ", e)
                close(e.aErrorVerificacion())
            }
        }

        val opciones = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(telefono)
            .setTimeout(SEGUNDOS_ESPERA_SMS, TimeUnit.SECONDS)
            .setActivity(actividad)
            .setCallbacks(callbacks)
        val token = tokenReenvio
        if (reenviar && token != null) opciones.setForceResendingToken(token)
        PhoneAuthProvider.verifyPhoneNumber(opciones.build())
        awaitClose()
    }

    suspend fun verificarCodigo(codigo: String) {
        val id = idVerificacion ?: throw ErrorRestaurante.CodigoVencido
        aplicar(PhoneAuthProvider.getCredential(id, codigo))
    }

    private suspend fun aplicar(credencial: PhoneAuthCredential) {
        val usuario = auth.currentUser ?: throw ErrorRestaurante.Desconocido
        llamarFirebaseVerificacion {
            usuario.updatePhoneNumber(credencial).await()
            usuario.getIdToken(true).await()
        }
    }

    private companion object {
        const val SEGUNDOS_ESPERA_SMS = 60L
    }
}
