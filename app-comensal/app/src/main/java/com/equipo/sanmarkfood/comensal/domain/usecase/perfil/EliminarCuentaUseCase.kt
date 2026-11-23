package com.equipo.sanmarkfood.comensal.domain.usecase.perfil

import com.equipo.sanmarkfood.comensal.domain.repository.CarritoRepository
import com.equipo.sanmarkfood.comensal.domain.repository.ComensalRepository
import com.equipo.sanmarkfood.comensal.domain.repository.RecordatoriosRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class EliminarCuentaUseCase @Inject constructor(
    private val comensalRepository: ComensalRepository,
    private val carritoRepository: CarritoRepository,
    private val recordatoriosRepository: RecordatoriosRepository
) {
    suspend operator fun invoke(): Result<Unit> =
        comensalRepository.eliminarCuenta().onSuccess {
            carritoRepository.vaciar()
            recordatoriosRepository.observarRecordatorios().first().keys.forEach(recordatoriosRepository::cancelar)
        }
}
