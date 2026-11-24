package com.equipo.sanmarkfood.restaurante.domain.usecase.menu

import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.equipo.sanmarkfood.restaurante.domain.repository.LecturaCartaRepository
import javax.inject.Inject

class LeerCartaUseCase @Inject constructor(
    private val lecturaCartaRepository: LecturaCartaRepository,
) {
    suspend operator fun invoke(foto: String): Int {
        val platos = lecturaCartaRepository.leerCarta(foto)
        if (platos.isEmpty()) throw ErrorMenu.CartaSinPlatos
        return platos.size
    }
}
