package com.equipo.sanmarkfood.restaurante.domain.usecase.admin

import com.equipo.sanmarkfood.restaurante.domain.model.admin.DatosBorrados
import com.equipo.sanmarkfood.restaurante.domain.repository.DatosInicialesRepository
import javax.inject.Inject

class BorrarDatosInicialesUseCase @Inject constructor(
    private val repository: DatosInicialesRepository,
) {
    suspend operator fun invoke(): DatosBorrados = repository.borrar()
}
