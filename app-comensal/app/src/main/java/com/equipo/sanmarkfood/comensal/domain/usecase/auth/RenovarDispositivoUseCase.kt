package com.equipo.sanmarkfood.comensal.domain.usecase.auth

import com.equipo.sanmarkfood.comensal.domain.repository.DispositivosRepository
import javax.inject.Inject

class RenovarDispositivoUseCase @Inject constructor(
    private val dispositivosRepository: DispositivosRepository
) {
    operator fun invoke(token: String) {
        if (token.isNotBlank()) dispositivosRepository.renovar(token)
    }
}
