package com.equipo.sanmarkfood.comensal.domain.repository

import com.equipo.sanmarkfood.comensal.domain.model.resenas.Calificacion

interface ResenasRepository {
    suspend fun calificar(calificacion: Calificacion)
}
