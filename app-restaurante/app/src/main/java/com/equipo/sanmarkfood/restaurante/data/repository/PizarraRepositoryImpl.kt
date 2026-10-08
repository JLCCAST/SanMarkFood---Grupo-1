package com.equipo.sanmarkfood.restaurante.data.repository

import com.equipo.sanmarkfood.restaurante.ai.digitalizacion_carta.LectorPizarra
import com.equipo.sanmarkfood.restaurante.data.local.LectorImagenes
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.menu.BorradorMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.DatosMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.OrigenMenu
import com.equipo.sanmarkfood.restaurante.domain.repository.PizarraRepository
import com.google.firebase.ai.type.FirebaseAIException
import com.google.firebase.ai.type.RequestTimeoutException
import kotlinx.serialization.SerializationException
import java.io.IOException
import java.util.Locale
import javax.inject.Inject

class PizarraRepositoryImpl @Inject constructor(
    private val lectorImagenes: LectorImagenes,
    private val lectorPizarra: LectorPizarra,
) : PizarraRepository {

    override suspend fun leerMenu(foto: String): BorradorMenu {
        val jpeg = try {
            lectorImagenes.leerComoJpeg(foto, LADO_MAXIMO_FOTO)
        } catch (e: ErrorRestaurante) {
            throw ErrorMenu.ImagenIlegible
        }
        val leido = try {
            lectorPizarra.leer(jpeg)
        } catch (e: FirebaseAIException) {
            throw if (e is RequestTimeoutException || e.cause is IOException) ErrorMenu.SinConexion else ErrorMenu.Desconocido
        } catch (e: SerializationException) {
            throw ErrorMenu.Desconocido
        }
        return BorradorMenu(
            origen = OrigenMenu.IA,
            precio = leido.precio?.let { "%.2f".format(Locale.ROOT, it) }.orEmpty(),
            entradas = leido.entradas.map { it.trim() }.filter { it.isNotEmpty() },
            segundos = leido.segundos.map { it.trim() }.filter { it.isNotEmpty() },
            refresco = leido.refresco?.trim().orEmpty(),
            postre = leido.postre?.trim().orEmpty(),
            horaFin = DatosMenu.HORA_FIN_POR_DEFECTO,
        )
    }

    private companion object {
        const val LADO_MAXIMO_FOTO = 1600
    }
}
