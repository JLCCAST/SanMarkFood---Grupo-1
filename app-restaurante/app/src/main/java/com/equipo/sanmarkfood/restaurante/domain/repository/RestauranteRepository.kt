package com.equipo.sanmarkfood.restaurante.domain.repository

import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.DatosLocal
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Horario
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Restaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.TipoFoto
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

    suspend fun guardarDatosYPedirRevision(datos: DatosLocal)

    /**
     * Achica la foto elegida en el celular ([imagenLocal] es su dirección) y la sube a Storage.
     * Devuelve la URL de descarga, que se guarda con los datos del local.
     */
    suspend fun subirFoto(tipo: TipoFoto, imagenLocal: String, alAvanzar: (fraccion: Float) -> Unit): String

    /** Guarda el horario y pasa el local de borrador a pendiente: desde ahí lo ve el administrador. */
    suspend fun enviarARevision(horario: Horario)

    /**
     * Guarda los datos corregidos de un local rechazado y lo pasa a pendiente. Para el administrador deja
     * el rechazo como «anterior», marca el reenvío y anota qué campos cambiaron.
     */
    suspend fun reenviarARevision(datos: DatosLocal)

    /** Cambia el horario de un local que ya terminó el alta, sin tocar su estado. */
    suspend fun guardarHorario(horario: Horario)

    /** Pausa o reanuda la recepción de pedidos nuevos de un local aprobado. */
    suspend fun cambiarPausa(pausado: Boolean)
}
