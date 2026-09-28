package com.equipo.sanmarkfood.restaurante.domain.repository

import com.equipo.sanmarkfood.restaurante.domain.model.DatosLocal
import com.equipo.sanmarkfood.restaurante.domain.model.Horario
import com.equipo.sanmarkfood.restaurante.domain.model.Restaurante
import com.equipo.sanmarkfood.restaurante.domain.model.TipoFoto
import kotlinx.coroutines.flow.Flow

interface RestauranteRepository {
    /** El local de la cuenta, o null si todavía no lo registra. */
    suspend fun obtener(): Restaurante?

    /** El local de la cuenta en tiempo real: emite otra vez cada vez que cambia (por ejemplo, al aprobarlo). */
    fun observar(): Flow<Restaurante?>

    /**
     * Si el local todavía no existe, lo crea en borrador; si ya existe, solo cambia estos datos.
     * Después borra de Storage las fotos subidas que ya no se usan.
     */
    suspend fun guardarDatos(datos: DatosLocal)

    /**
     * Achica la foto elegida en el celular ([imagenLocal] es su dirección) y la sube a Storage.
     * Devuelve la URL de descarga, que se guarda con los datos del local.
     */
    suspend fun subirFoto(tipo: TipoFoto, imagenLocal: String, alAvanzar: (fraccion: Float) -> Unit): String

    /** Guarda el horario y pasa el local de borrador a pendiente: desde ahí lo ve el administrador. */
    suspend fun enviarARevision(horario: Horario)

    /** Cambia el horario de un local que ya terminó el alta, sin tocar su estado. */
    suspend fun guardarHorario(horario: Horario)
}
