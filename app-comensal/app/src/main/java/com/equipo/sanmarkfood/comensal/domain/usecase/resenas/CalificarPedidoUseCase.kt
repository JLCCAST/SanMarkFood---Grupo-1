package com.equipo.sanmarkfood.comensal.domain.usecase.resenas

import com.equipo.sanmarkfood.comensal.domain.model.resenas.Calificacion
import com.equipo.sanmarkfood.comensal.domain.model.resenas.ErrorCalificar
import com.equipo.sanmarkfood.comensal.domain.repository.ResenasRepository
import javax.inject.Inject

class CalificarPedidoUseCase @Inject constructor(
    private val resenasRepository: ResenasRepository,
) {
    suspend operator fun invoke(
        pedidoId: String,
        estrellas: Int,
        comentario: String,
        platos: Map<String, Boolean>,
    ) {
        if (estrellas !in Calificacion.MIN_ESTRELLAS..Calificacion.MAX_ESTRELLAS) throw ErrorCalificar.DatosInvalidos
        val texto = comentario.trim()
        if (texto.length > Calificacion.MAX_COMENTARIO) throw ErrorCalificar.DatosInvalidos
        resenasRepository.calificar(
            Calificacion(
                pedidoId = pedidoId,
                estrellas = estrellas,
                comentario = texto.ifEmpty { null },
                platos = platos,
            )
        )
    }
}
