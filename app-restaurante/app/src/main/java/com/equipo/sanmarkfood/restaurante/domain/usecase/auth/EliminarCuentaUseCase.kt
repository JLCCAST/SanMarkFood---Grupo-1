package com.equipo.sanmarkfood.restaurante.domain.usecase.auth

import com.equipo.sanmarkfood.restaurante.domain.repository.AuthRepository
import com.equipo.sanmarkfood.restaurante.domain.repository.BorradorMenuRepository
import com.equipo.sanmarkfood.restaurante.domain.repository.DispositivosRepository
import javax.inject.Inject

class EliminarCuentaUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val borradorMenuRepository: BorradorMenuRepository,
    private val dispositivosRepository: DispositivosRepository,
) {
    suspend operator fun invoke() {
        authRepository.eliminarCuenta()
        borradorMenuRepository.borrarBorrador()
        dispositivosRepository.olvidar()
        authRepository.cerrarSesion()
    }
}
