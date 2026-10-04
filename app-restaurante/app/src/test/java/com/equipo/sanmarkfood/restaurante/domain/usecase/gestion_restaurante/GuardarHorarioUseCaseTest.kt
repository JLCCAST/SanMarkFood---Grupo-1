package com.equipo.sanmarkfood.restaurante.domain.usecase.gestion_restaurante

import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.DatosLocal
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Horario
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Restaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.TipoFoto
import com.equipo.sanmarkfood.restaurante.domain.repository.RestauranteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GuardarHorarioUseCaseTest {

    private class RepositorioFalso : RestauranteRepository {
        var guardado: Horario? = null
        var enviadoARevision = false
        override suspend fun obtener(): Restaurante? = null
        override fun observar(): Flow<Restaurante?> = emptyFlow()
        override suspend fun guardarDatos(datos: DatosLocal) = Unit
        override suspend fun enviarARevision(horario: Horario) {
            enviadoARevision = true
        }
        override suspend fun subirFoto(tipo: TipoFoto, imagenLocal: String, alAvanzar: (Float) -> Unit) = ""
        override suspend fun cambiarPausa(pausado: Boolean) = Unit
        override suspend fun reenviarARevision(datos: DatosLocal) = Unit
        override suspend fun guardarHorario(horario: Horario) {
            guardado = horario
        }
    }

    private val repositorio = RepositorioFalso()
    private val guardar = GuardarHorarioUseCase(repositorio)

    @Test
    fun guardaElHorarioSinEnviarloARevision() {
        runBlocking { guardar(Horario.PorDefecto) }
        assertEquals(Horario.PorDefecto, repositorio.guardado)
        assertEquals(false, repositorio.enviadoARevision)
    }

    @Test
    fun aplicaLasMismasReglasQueElAlta() {
        val cerrado = Horario(Horario.PorDefecto.dias.mapValues { (_, dia) -> dia.copy(abierto = false) })
        val error = try {
            runBlocking { guardar(cerrado) }
            null
        } catch (e: ErrorRestaurante) {
            e
        }
        assertEquals(ErrorRestaurante.NingunDiaAbierto, error)
        assertNull(repositorio.guardado)
    }
}
