package com.equipo.sanmarkfood.restaurante.data.firebase

import com.equipo.sanmarkfood.restaurante.domain.model.menu.Categoria
import com.equipo.sanmarkfood.restaurante.domain.model.menu.DatosMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.DatosPlato
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.EstadoMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.MenuDelDia
import com.equipo.sanmarkfood.restaurante.domain.model.menu.OpcionMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.OrigenMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.Plato
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MenuDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun restaurante(uid: String) = firestore.collection("restaurantes").document(uid)

    private fun categorias(uid: String) = restaurante(uid).collection("categorias")

    private fun platos(uid: String) = restaurante(uid).collection("platos")

    private fun menu(uid: String, fecha: String) = restaurante(uid).collection("menus").document(fecha)

    fun observarCategorias(uid: String): Flow<List<Categoria>> =
        observar(categorias(uid).orderBy("orden")) { it.aCategoria() }

    suspend fun leerCategorias(uid: String): List<Categoria> =
        llamarFirebaseMenu {
            categorias(uid).orderBy("orden").get(Source.SERVER).await().documents.map { it.aCategoria() }
        }

    suspend fun crearCategoria(uid: String, nombre: String, orden: Int): Categoria =
        llamarFirebaseMenu {
            val documento = categorias(uid).document()
            documento.set(mapOf("nombre" to nombre, "orden" to orden)).await()
            Categoria(id = documento.id, nombre = nombre, orden = orden)
        }

    fun observarPlatos(uid: String): Flow<List<Plato>> = observar(platos(uid)) { it.aPlato() }

    suspend fun leerPlato(uid: String, id: String): Plato? =
        llamarFirebaseMenu {
            val documento = platos(uid).document(id).get().await()
            if (documento.exists()) documento.aPlato() else null
        }

    suspend fun existePlatoEnServidor(uid: String, id: String): Boolean =
        llamarFirebaseMenu { platos(uid).document(id).get(Source.SERVER).await().exists() }

    fun nuevoIdPlato(uid: String): String = platos(uid).document().id

    suspend fun crearPlato(uid: String, id: String, datos: DatosPlato, fotoUrl: String?) {
        val campos = buildMap {
            putAll(datos.aCampos())
            fotoUrl?.let { put("fotoUrl", it) }
            put("creadoEn", FieldValue.serverTimestamp())
            put("actualizadoEn", FieldValue.serverTimestamp())
        }
        llamarFirebaseMenu { platos(uid).document(id).set(campos).await() }
    }

    suspend fun actualizarPlato(uid: String, id: String, datos: DatosPlato, fotoNueva: String?) {
        val campos = buildMap {
            putAll(datos.aCampos())
            if (datos.descripcion == null) put("descripcion", FieldValue.delete())
            if (datos.agotadoEl == null) put("agotadoEl", FieldValue.delete())
            fotoNueva?.let { put("fotoUrl", it) }
            put("actualizadoEn", FieldValue.serverTimestamp())
        }
        llamarFirebaseMenu { platos(uid).document(id).update(campos).await() }
    }

    suspend fun cambiarAgotadoPlato(uid: String, id: String, agotadoEl: String?) {
        llamarFirebaseMenu {
            platos(uid).document(id).update(
                mapOf(
                    "agotadoEl" to (agotadoEl ?: FieldValue.delete()),
                    "actualizadoEn" to FieldValue.serverTimestamp(),
                )
            ).await()
        }
    }

    suspend fun eliminarPlato(uid: String, id: String) {
        llamarFirebaseMenu { platos(uid).document(id).delete().await() }
    }

    suspend fun leerPrecios(uid: String): List<Int> =
        llamarFirebaseMenu {
            platos(uid).get(Source.SERVER).await().documents.mapNotNull { it.getLong("precio")?.toInt() }
        }

    suspend fun guardarRangoCarta(uid: String, min: Int, max: Int) {
        llamarFirebaseMenu {
            restaurante(uid).update(
                mapOf(
                    "rangoCarta" to mapOf("min" to min, "max" to max),
                    "actualizadoEn" to FieldValue.serverTimestamp(),
                )
            ).await()
        }
    }

    suspend fun quitarRangoCarta(uid: String) {
        llamarFirebaseMenu {
            restaurante(uid).update(
                mapOf(
                    "rangoCarta" to FieldValue.delete(),
                    "actualizadoEn" to FieldValue.serverTimestamp(),
                )
            ).await()
        }
    }

    fun observarMenu(uid: String, fecha: String): Flow<MenuDelDia?> = callbackFlow {
        val registro = menu(uid, fecha).addSnapshotListener { foto, error ->
            if (error != null) {
                close(error.aErrorMenu())
                return@addSnapshotListener
            }
            try {
                trySend(if (foto != null && foto.exists()) foto.aMenuDelDia() else null)
            } catch (e: ErrorMenu) {
                close(e)
            }
        }
        awaitClose { registro.remove() }
    }

    suspend fun leerMenu(uid: String, fecha: String): MenuDelDia? =
        llamarFirebaseMenu {
            val documento = menu(uid, fecha).get().await()
            if (documento.exists()) documento.aMenuDelDia() else null
        }

    suspend fun publicarMenu(uid: String, fecha: String, datos: DatosMenu, origen: OrigenMenu): Boolean =
        llamarFirebaseMenu {
            firestore.runTransaction { transaccion ->
                if (transaccion.get(menu(uid, fecha)).exists()) return@runTransaction false

                transaccion.set(
                    menu(uid, fecha),
                    datos.aCampos() + mapOf(
                        "estado" to EstadoMenu.PUBLICADO.valor(),
                        "origen" to origen.valor(),
                        "publicadoEn" to FieldValue.serverTimestamp(),
                        "actualizadoEn" to FieldValue.serverTimestamp(),
                    ),
                )
                transaccion.update(restaurante(uid), cambioMenuHoy(fecha, datos, EstadoMenu.PUBLICADO))
                true
            }.await()
        }

    suspend fun actualizarMenu(uid: String, fecha: String, datos: DatosMenu, estado: EstadoMenu) {
        val camposMenu = buildMap {
            putAll(datos.aCampos())
            if (datos.refresco == null) put("refresco", FieldValue.delete())
            if (datos.postre == null) put("postre", FieldValue.delete())
            put("estado", estado.valor())
            put("actualizadoEn", FieldValue.serverTimestamp())
        }
        llamarFirebaseMenu {
            firestore.batch()
                .update(menu(uid, fecha), camposMenu)
                .update(restaurante(uid), cambioMenuHoy(fecha, datos, estado))
                .commit()
                .await()
        }
    }

    private fun cambioMenuHoy(fecha: String, datos: DatosMenu, estado: EstadoMenu): Map<String, Any> = mapOf(
        "menuHoy" to mapOf(
            "fecha" to fecha,
            "precio" to datos.precio,
            "horaFin" to datos.horaFin,
            "estado" to estado.valor(),
        ),
        "actualizadoEn" to FieldValue.serverTimestamp(),
    )

    suspend fun existeMenuEnServidor(uid: String, fecha: String): Boolean =
        llamarFirebaseMenu { menu(uid, fecha).get(Source.SERVER).await().exists() }

    suspend fun guardarOpcionesMenu(uid: String, fecha: String, entradas: List<OpcionMenu>, segundos: List<OpcionMenu>) {
        llamarFirebaseMenu {
            menu(uid, fecha).update(
                mapOf(
                    "entradas" to entradas.map { it.aCampos() },
                    "segundos" to segundos.map { it.aCampos() },
                    "actualizadoEn" to FieldValue.serverTimestamp(),
                )
            ).await()
        }
    }

    private fun <T> observar(consulta: Query, mapear: (DocumentSnapshot) -> T): Flow<List<T>> = callbackFlow {
        val registro = consulta.addSnapshotListener { foto, error ->
            if (error != null) {
                close(error.aErrorMenu())
                return@addSnapshotListener
            }
            try {
                trySend(foto?.documents.orEmpty().map(mapear))
            } catch (e: ErrorMenu) {
                close(e)
            }
        }
        awaitClose { registro.remove() }
    }
}
