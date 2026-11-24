package com.equipo.sanmarkfood.restaurante.data.repository

import com.equipo.sanmarkfood.restaurante.ai.digitalizacion_carta.LectorCarta
import com.equipo.sanmarkfood.restaurante.data.local.LectorImagenes
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.PlatoLeido
import com.equipo.sanmarkfood.restaurante.domain.repository.LecturaCartaRepository
import com.google.firebase.ai.type.FirebaseAIException
import com.google.firebase.ai.type.RequestTimeoutException
import kotlinx.serialization.SerializationException
import java.io.IOException
import java.util.Locale
import javax.inject.Inject

class LecturaCartaRepositoryImpl @Inject constructor(
    private val lectorImagenes: LectorImagenes,
    private val lectorCarta: LectorCarta,
) : LecturaCartaRepository {

    private var ultima: List<PlatoLeido> = emptyList()

    override suspend fun leerCarta(foto: String): List<PlatoLeido> {
        val jpeg = try {
            lectorImagenes.leerComoJpeg(foto, LADO_MAXIMO_FOTO)
        } catch (e: ErrorRestaurante) {
            throw ErrorMenu.ImagenIlegible
        }
        val leida = try {
            lectorCarta.leer(jpeg)
        } catch (e: FirebaseAIException) {
            throw if (e is RequestTimeoutException || e.cause is IOException) ErrorMenu.SinConexion else ErrorMenu.Desconocido
        } catch (e: SerializationException) {
            throw ErrorMenu.Desconocido
        }
        ultima = leida.platos
            .filter { it.nombre.isNotBlank() }
            .map {
                PlatoLeido(
                    nombre = it.nombre.trim(),
                    precio = it.precio?.let { precio -> "%.2f".format(Locale.ROOT, precio) }.orEmpty(),
                    categoria = it.categoria.trim().ifEmpty { PlatoLeido.CATEGORIA_POR_DEFECTO },
                    nombreDudoso = it.nombreDudoso,
                    precioDudoso = it.precio == null || it.precioDudoso,
                )
            }
        return ultima
    }

    override fun ultimaLectura(): List<PlatoLeido> = ultima

    private companion object {
        const val LADO_MAXIMO_FOTO = 2048
    }
}
