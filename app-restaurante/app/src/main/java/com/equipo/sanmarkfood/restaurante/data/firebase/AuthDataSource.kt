package com.equipo.sanmarkfood.restaurante.data.firebase

import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.auth.ErrorAuth
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val actividadActual: ActividadActual,
) {
    suspend fun registrarYEnviarVerificacion(correo: String, contrasena: String): String {
        val resultado = llamarFirebase { auth.createUserWithEmailAndPassword(correo, contrasena).await() }
        val usuario = resultado.user ?: throw ErrorAuth.Desconocido
        usuario.sendEmailVerification()
        return usuario.uid
    }

    suspend fun enviarVerificacion() {
        llamarFirebase { auth.currentUser?.sendEmailVerification()?.await() }
    }

    suspend fun correoVerificado(): Boolean = llamarFirebase {
        auth.currentUser?.reload()?.await()
        val verificado = auth.currentUser?.isEmailVerified == true
        // Las reglas de Firestore leen email_verified del token, que no se entera hasta pedir uno nuevo.
        if (verificado) auth.currentUser?.getIdToken(true)?.await()
        verificado
    }

    suspend fun iniciarSesion(correo: String, contrasena: String) {
        llamarFirebase { auth.signInWithEmailAndPassword(correo, contrasena).await() }
    }

    suspend fun iniciarSesionConGoogle(): Boolean {
        val actividad = actividadActual.obtener() ?: throw ErrorAuth.Desconocido
        val opcion = GetSignInWithGoogleOption.Builder(actividad.getString(R.string.default_web_client_id)).build()
        val pedido = GetCredentialRequest.Builder().addCredentialOption(opcion).build()
        val respuesta = try {
            CredentialManager.create(actividad).getCredential(actividad, pedido)
        } catch (e: GetCredentialCancellationException) {
            return false
        } catch (e: NoCredentialException) {
            throw ErrorAuth.SinCuentaGoogle
        } catch (e: GetCredentialException) {
            throw ErrorAuth.Desconocido
        }
        val credencial = respuesta.credential
        if (credencial !is CustomCredential || credencial.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            throw ErrorAuth.Desconocido
        }
        val idToken = GoogleIdTokenCredential.createFrom(credencial.data).idToken
        llamarFirebase { auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await() }
        return true
    }

    fun usuarioActual(): FirebaseUser? = auth.currentUser

    fun cerrarSesion() = auth.signOut()

    suspend fun enviarRecuperacion(correo: String) {
        llamarFirebase { auth.sendPasswordResetEmail(correo).await() }
    }
}
