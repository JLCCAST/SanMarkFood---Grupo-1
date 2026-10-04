package com.equipo.sanmarkfood.restaurante.data.repository

import com.equipo.sanmarkfood.restaurante.data.firebase.AuthDataSource
import com.equipo.sanmarkfood.restaurante.data.firebase.FotosDataSource
import com.equipo.sanmarkfood.restaurante.data.firebase.RestaurantesDataSource
import com.equipo.sanmarkfood.restaurante.data.local.LectorImagenes
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.DatosLocal
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.EstadoRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Horario
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Restaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.TipoFoto
import com.equipo.sanmarkfood.restaurante.domain.repository.RestauranteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RestauranteRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val restaurantesDataSource: RestaurantesDataSource,
    private val fotosDataSource: FotosDataSource,
    private val lectorImagenes: LectorImagenes,
) : RestauranteRepository {

    override suspend fun obtener(): Restaurante? = restaurantesDataSource.leer(uid())

    // Dentro de flow {} para que, si no hay sesión, el error llegue a quien recolecta y no a quien llama.
    override fun observar(): Flow<Restaurante?> = flow { emitAll(restaurantesDataSource.observar(uid())) }

    override suspend fun guardarDatos(datos: DatosLocal) {
        val uid = uid()
        if (restaurantesDataSource.existeEnServidor(uid)) {
            restaurantesDataSource.actualizarDatos(uid, datos)
        } else {
            restaurantesDataSource.crear(uid, datos, EstadoRestaurante.BORRADOR)
        }
        borrarFotosSinUsar(uid, datos)
    }

    override suspend fun enviarARevision(horario: Horario) {
        val uid = uid()
        // Igual que al guardar los datos: la lectura al servidor avisa si no hay conexión antes de escribir.
        if (!restaurantesDataSource.existeEnServidor(uid)) throw ErrorRestaurante.Desconocido
        restaurantesDataSource.enviarARevision(uid, horario)
    }

    override suspend fun reenviarARevision(datos: DatosLocal) {
        val uid = uid()
        if (!restaurantesDataSource.reenviarARevision(uid, datos)) throw ErrorRestaurante.Desconocido
        borrarFotosSinUsar(uid, datos)
    }

    override suspend fun guardarHorario(horario: Horario) {
        val uid = uid()
        if (!restaurantesDataSource.existeEnServidor(uid)) throw ErrorRestaurante.Desconocido
        restaurantesDataSource.actualizarHorario(uid, horario)
    }

    // La pausa sí tiene que llegar al servidor: si el local cree que pausó y no fue así, le siguen
    // llegando pedidos. Por eso, sin conexión falla en vez de quedar en espera.
    override suspend fun cambiarPausa(pausado: Boolean) {
        val uid = uid()
        if (!restaurantesDataSource.existeEnServidor(uid)) throw ErrorRestaurante.Desconocido
        restaurantesDataSource.cambiarPausa(uid, pausado)
    }

    override suspend fun subirFoto(tipo: TipoFoto, imagenLocal: String, alAvanzar: (fraccion: Float) -> Unit): String {
        val uid = uid()
        val (prefijo, ladoMaximo) = when (tipo) {
            TipoFoto.PORTADA -> "portada" to LADO_MAXIMO_PORTADA
            TipoFoto.LOGO -> "logo" to LADO_MAXIMO_LOGO
        }
        val jpeg = lectorImagenes.leerComoJpeg(imagenLocal, ladoMaximo)
        return fotosDataSource.subir(uid, "$prefijo-${System.currentTimeMillis()}.jpg", jpeg, alAvanzar)
    }

    // Quedan fotos sueltas al cambiar una ya guardada o al elegir otra antes de continuar. Si la limpieza
    // falla no pasa nada: los datos ya se guardaron y se vuelve a intentar la próxima vez que se guarden.
    private suspend fun borrarFotosSinUsar(uid: String, datos: DatosLocal) {
        try {
            val enUso = listOfNotNull(datos.portadaUrl, datos.logoUrl).map(fotosDataSource::nombreDeArchivo).toSet()
            if (enUso.isNotEmpty()) fotosDataSource.borrarLasDemas(uid, enUso)
        } catch (e: ErrorRestaurante) {
            // Se reintenta al próximo guardado.
        } catch (e: IllegalArgumentException) {
            // Una URL que no es de este bucket: no se borra nada.
        }
    }

    // Cada cuenta tiene un solo local, guardado en restaurantes/{uid}.
    private fun uid(): String = authDataSource.usuarioActual()?.uid ?: throw ErrorRestaurante.Desconocido

    private companion object {
        const val LADO_MAXIMO_PORTADA = 1600
        const val LADO_MAXIMO_LOGO = 512
    }
}
