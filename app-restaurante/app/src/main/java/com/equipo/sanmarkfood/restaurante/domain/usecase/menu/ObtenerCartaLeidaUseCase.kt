package com.equipo.sanmarkfood.restaurante.domain.usecase.menu

import com.equipo.sanmarkfood.restaurante.domain.model.menu.PlatoLeido
import com.equipo.sanmarkfood.restaurante.domain.repository.LecturaCartaRepository
import javax.inject.Inject

class ObtenerCartaLeidaUseCase @Inject constructor(
    private val lecturaCartaRepository: LecturaCartaRepository,
) {
    operator fun invoke(): List<PlatoLeido> = lecturaCartaRepository.ultimaLectura()
}
