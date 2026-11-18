package com.equipo.sanmarkfood.restaurante.domain.usecase.admin

import com.equipo.sanmarkfood.restaurante.domain.model.admin.DatosCargados
import com.equipo.sanmarkfood.restaurante.domain.repository.DatosInicialesRepository
import javax.inject.Inject

class CargarDatosInicialesUseCase @Inject constructor(
    private val repository: DatosInicialesRepository,
) {
    suspend operator fun invoke(): DatosCargados = repository.cargar()
}
