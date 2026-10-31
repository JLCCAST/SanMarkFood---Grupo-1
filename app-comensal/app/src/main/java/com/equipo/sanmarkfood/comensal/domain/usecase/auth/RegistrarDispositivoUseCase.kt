package com.equipo.sanmarkfood.comensal.domain.usecase.auth

import com.equipo.sanmarkfood.comensal.domain.repository.DispositivosRepository
import javax.inject.Inject

class RegistrarDispositivoUseCase @Inject constructor(
    private val dispositivosRepository: DispositivosRepository
) {
    operator fun invoke() = dispositivosRepository.registrar()
}
