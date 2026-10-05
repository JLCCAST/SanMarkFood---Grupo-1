package com.equipo.sanmarkfood.restaurante.domain.usecase.auth

import com.equipo.sanmarkfood.restaurante.domain.model.auth.EstadoSesion
import com.equipo.sanmarkfood.restaurante.domain.model.auth.Rol
import com.equipo.sanmarkfood.restaurante.domain.repository.DispositivosRepository
import javax.inject.Inject

class RegistrarDispositivoUseCase @Inject constructor(
    private val dispositivosRepository: DispositivosRepository
) {
    operator fun invoke(sesion: EstadoSesion) {
        val esRestaurante = sesion == EstadoSesion.SinLocal ||
            (sesion is EstadoSesion.Activa && sesion.rol == Rol.RESTAURANTE)
        if (esRestaurante) dispositivosRepository.registrar()
    }
}
