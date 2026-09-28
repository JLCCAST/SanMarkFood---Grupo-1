package com.equipo.sanmarkfood.restaurante.domain.usecase

import com.equipo.sanmarkfood.restaurante.domain.model.DatosLocal
import com.equipo.sanmarkfood.restaurante.domain.model.DiaSemana
import com.equipo.sanmarkfood.restaurante.domain.model.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.Hora
import com.equipo.sanmarkfood.restaurante.domain.model.Horario
import com.equipo.sanmarkfood.restaurante.domain.model.Restaurante
import com.equipo.sanmarkfood.restaurante.domain.model.TipoFoto
import com.equipo.sanmarkfood.restaurante.domain.repository.RestauranteRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EnviarARevisionUseCaseTest {

    private class RepositorioFalso : RestauranteRepository {
        var enviado: Horario? = null
        override suspend fun obtener(): Restaurante? = null
        override suspend fun guardarDatos(datos: DatosLocal) = Unit
        override suspend fun enviarARevision(horario: Horario) {
            enviado = horario
        }
        override suspend fun subirFoto(tipo: TipoFoto, imagenLocal: String, alAvanzar: (Float) -> Unit) = ""
    }

    private val repositorio = RepositorioFalso()
    private val enviar = EnviarARevisionUseCase(repositorio)

    private fun errorAlEnviar(horario: Horario): ErrorRestaurante? = try {
        runBlocking { enviar(horario) }
        null
    } catch (e: ErrorRestaurante) {
        e
    }

    @Test
    fun enviaElHorarioPorDefecto() {
        assertNull(errorAlEnviar(Horario.PorDefecto))
        assertEquals(Horario.PorDefecto, repositorio.enviado)
    }

    @Test
    fun noEnviaSiTodosLosDiasEstanCerrados() {
        val cerrado = Horario(Horario.PorDefecto.dias.mapValues { (_, dia) -> dia.copy(abierto = false) })
        assertEquals(ErrorRestaurante.NingunDiaAbierto, errorAlEnviar(cerrado))
        assertNull(repositorio.enviado)
    }

    @Test
    fun marcaLosDiasAbiertosQueCierranAntesDeAbrir() {
        val dias = Horario.PorDefecto.dias.toMutableMap()
        dias[DiaSemana.MARTES] = dias.getValue(DiaSemana.MARTES).copy(abre = Hora(16, 0), cierra = Hora(11, 30))
        // Un día cerrado con horas al revés no cuenta: no se usa hasta que lo abran.
        dias[DiaSemana.DOMINGO] = dias.getValue(DiaSemana.DOMINGO).copy(abre = Hora(20, 0), cierra = Hora(8, 0))

        assertEquals(ErrorRestaurante.HorasInvalidas(setOf(DiaSemana.MARTES)), errorAlEnviar(Horario(dias)))
        assertNull(repositorio.enviado)
    }
}
