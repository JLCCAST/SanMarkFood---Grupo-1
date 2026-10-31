package com.equipo.sanmarkfood.restaurante.data.remote

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.ExecutionException
import javax.inject.Inject

class AutenticacionInterceptor @Inject constructor(
    private val auth: FirebaseAuth,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val usuario = auth.currentUser ?: return chain.proceed(chain.request())
        val token = try {
            Tasks.await(usuario.getIdToken(false)).token
        } catch (e: ExecutionException) {
            throw IOException(e)
        }
        val solicitud = chain.request().newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(solicitud)
    }
}
