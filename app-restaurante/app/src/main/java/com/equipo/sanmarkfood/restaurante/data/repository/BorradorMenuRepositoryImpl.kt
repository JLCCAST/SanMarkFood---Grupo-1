package com.equipo.sanmarkfood.restaurante.data.repository

import com.equipo.sanmarkfood.restaurante.data.local.dao.BorradorMenuDao
import com.equipo.sanmarkfood.restaurante.data.local.entity.BorradorMenuEntity
import com.equipo.sanmarkfood.restaurante.data.local.entity.OpcionBorradorEntity
import com.equipo.sanmarkfood.restaurante.domain.model.menu.BorradorMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.OrigenMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.TipoOpcion
import com.equipo.sanmarkfood.restaurante.domain.repository.BorradorMenuRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BorradorMenuRepositoryImpl @Inject constructor(
    private val borradorMenuDao: BorradorMenuDao
) : BorradorMenuRepository {

    override suspend fun leerBorrador(fecha: String): BorradorMenu? {
        val borrador = borradorMenuDao.leerBorrador(fecha) ?: return null
        val opciones = borradorMenuDao.leerOpciones(fecha)
        return BorradorMenu(
            origen = borrador.origen,
            precio = borrador.precio,
            entradas = opciones.filter { it.tipo == TipoOpcion.ENTRADA }.map { it.nombre },
            segundos = opciones.filter { it.tipo == TipoOpcion.SEGUNDO }.map { it.nombre },
            refresco = borrador.refresco,
            postre = borrador.postre,
            horaFin = borrador.horaFin,
            precioPorRevisar = borrador.precioPorRevisar,
            porRevisar = opciones.filter { it.porRevisar }.map { it.nombre }.toSet(),
        )
    }

    override fun observarOrigen(fecha: String): Flow<OrigenMenu?> = borradorMenuDao.observarOrigen(fecha)

    override suspend fun guardarBorrador(fecha: String, borrador: BorradorMenu) {
        val fila = BorradorMenuEntity(
            fecha = fecha,
            origen = borrador.origen,
            precio = borrador.precio,
            refresco = borrador.refresco,
            postre = borrador.postre,
            horaFin = borrador.horaFin,
            precioPorRevisar = borrador.precioPorRevisar,
        )
        val opciones = borrador.entradas.map { opcion(fecha, TipoOpcion.ENTRADA, it, borrador.porRevisar) } +
            borrador.segundos.map { opcion(fecha, TipoOpcion.SEGUNDO, it, borrador.porRevisar) }
        borradorMenuDao.reemplazar(fila, opciones)
    }

    override suspend fun borrarBorrador() = borradorMenuDao.borrarTodo()

    private fun opcion(fecha: String, tipo: TipoOpcion, nombre: String, porRevisar: Set<String>) =
        OpcionBorradorEntity(fecha = fecha, tipo = tipo, nombre = nombre, porRevisar = nombre in porRevisar)
}
