package com.equipo.sanmarkfood.comensal.domain.usecase.auth

import com.equipo.sanmarkfood.comensal.domain.model.auth.ErrorAuth
import com.equipo.sanmarkfood.comensal.domain.model.auth.ResultadoInicioSesion
import com.equipo.sanmarkfood.comensal.domain.repository.AuthRepository
import javax.inject.Inject

class IniciarSesionConGoogleUseCase @Inject constructor(
    private val repositorio: AuthRepository
) {
    suspend operator fun invoke(): ResultadoInicioSesion? =
        repositorio.loginConGoogle().fold(
            onSuccess = { sesion ->
                when {
                    sesion == null -> null
                    sesion.correoVerificado -> ResultadoInicioSesion.Autenticado
                    else -> ResultadoInicioSesion.PorVerificar(sesion.correo)
                }
            },
            onFailure = { ResultadoInicioSesion.Fallo(it as? ErrorAuth ?: ErrorAuth.Desconocido) }
        )
}
