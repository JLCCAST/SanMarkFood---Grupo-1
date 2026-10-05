package com.equipo.sanmarkfood.restaurante.domain.usecase.auth

import com.equipo.sanmarkfood.restaurante.domain.repository.DispositivosRepository
import javax.inject.Inject

class RenovarDispositivoUseCase @Inject constructor(
    private val dispositivosRepository: DispositivosRepository
) {
    operator fun invoke(token: String) {
        if (token.isNotBlank()) dispositivosRepository.renovar(token)
    }
}
