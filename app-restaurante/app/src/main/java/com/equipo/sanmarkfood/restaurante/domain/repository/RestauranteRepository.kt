package com.equipo.sanmarkfood.restaurante.domain.repository

import com.equipo.sanmarkfood.restaurante.domain.model.DatosLocal
import com.equipo.sanmarkfood.restaurante.domain.model.Restaurante

interface RestauranteRepository {
    /** El local de la cuenta, o null si todavía no lo registra. */
    suspend fun obtener(): Restaurante?

    /** Si el local todavía no existe, lo crea en borrador; si ya existe, solo cambia estos datos. */
    suspend fun guardarDatos(datos: DatosLocal)
}
