package com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante

enum class DiaSemana {
    LUNES,
    MARTES,
    MIERCOLES,
    JUEVES,
    VIERNES,
    SABADO,
    DOMINGO,
}

/** Hora del día en formato de 24 horas. No se usa java.time porque la app llega hasta Android 7 (API 24). */
data class Hora(val hora: Int, val minuto: Int) : Comparable<Hora> {
    init {
        require(hora in 0..23 && minuto in 0..59) { "Hora fuera de rango: $hora:$minuto" }
    }

    override fun compareTo(other: Hora): Int = (hora * 60 + minuto).compareTo(other.hora * 60 + other.minuto)
}

/** Un día cerrado conserva sus horas, para que al volver a abrirlo no haya que elegirlas otra vez. */
data class HorarioDia(val abierto: Boolean, val abre: Hora, val cierra: Hora)

data class Horario(val dias: Map<DiaSemana, HorarioDia>) {
    init {
        require(dias.keys == DiaSemana.entries.toSet()) { "El horario debe tener los siete días" }
    }

    /**
     * Los días abiertos agrupados en tramos seguidos, de lunes a domingo: lun–vie y dom abiertos
     * da [(LUNES, VIERNES), (DOMINGO, DOMINGO)]. Sirve para resumirlo, como «Lun a vie, dom».
     */
    fun tramosAbiertos(): List<Pair<DiaSemana, DiaSemana>> {
        val tramos = mutableListOf<Pair<DiaSemana, DiaSemana>>()
        var inicio: DiaSemana? = null
        for ((indice, dia) in DiaSemana.entries.withIndex()) {
            val abierto = dias.getValue(dia).abierto
            if (abierto && inicio == null) inicio = dia
            val desde = inicio ?: continue
            if (!abierto) {
                tramos += desde to DiaSemana.entries[indice - 1]
                inicio = null
            } else if (indice == DiaSemana.entries.lastIndex) {
                tramos += desde to dia
            }
        }
        return tramos
    }

    companion object {
        /** El horario con el que empieza R4, el mismo del prototipo. */
        val PorDefecto: Horario = run {
            val semana = HorarioDia(abierto = true, abre = Hora(11, 30), cierra = Hora(16, 0))
            Horario(
                DiaSemana.entries.associateWith { dia ->
                    when (dia) {
                        DiaSemana.SABADO -> HorarioDia(abierto = true, abre = Hora(12, 0), cierra = Hora(15, 0))
                        DiaSemana.DOMINGO -> semana.copy(abierto = false)
                        else -> semana
                    }
                }
            )
        }
    }
}
