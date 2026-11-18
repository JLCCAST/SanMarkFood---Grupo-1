package com.equipo.sanmarkfood.comensal.domain.model.reservas

enum class AnticipacionRecordatorio(val minutos: Int) {
    TREINTA_MINUTOS(30),
    UNA_HORA(60),
    DOS_HORAS(120);

    companion object {
        fun desde(minutos: Int): AnticipacionRecordatorio? = entries.firstOrNull { it.minutos == minutos }
    }
}

data class Recordatorio(
    val reservaId: String,
    val restauranteNombre: String,
    val fecha: String,
    val hora: String,
    val anticipacion: AnticipacionRecordatorio,
) {
    val momentoDelAviso: Long?
        get() = momentoDeReserva(fecha, hora)?.minus(anticipacion.minutos * MILIS_POR_MINUTO)
}
