package com.equipo.sanmarkfood.restaurante.domain.repository

import com.equipo.sanmarkfood.restaurante.domain.model.admin.DatosBorrados
import com.equipo.sanmarkfood.restaurante.domain.model.admin.DatosCargados

interface DatosInicialesRepository {
    suspend fun cargar(): DatosCargados
    suspend fun borrar(): DatosBorrados
}
