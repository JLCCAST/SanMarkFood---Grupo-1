package com.equipo.sanmarkfood.restaurante.data.repository

import com.equipo.sanmarkfood.restaurante.data.firebase.AuthDataSource
import com.equipo.sanmarkfood.restaurante.data.firebase.DispositivosDataSource
import com.equipo.sanmarkfood.restaurante.domain.repository.DispositivosRepository
import javax.inject.Inject

class DispositivosRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val dispositivosDataSource: DispositivosDataSource,
) : DispositivosRepository {

    override fun registrar() {
        val uid = authDataSource.usuarioActual()?.uid ?: return
        dispositivosDataSource.registrar(uid)
    }

    override fun renovar(token: String) {
        val uid = authDataSource.usuarioActual()?.uid ?: return
        dispositivosDataSource.guardar(uid, token)
    }

    override fun olvidar() = dispositivosDataSource.olvidar()
}
