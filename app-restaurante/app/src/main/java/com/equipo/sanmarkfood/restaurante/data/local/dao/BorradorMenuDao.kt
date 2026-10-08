package com.equipo.sanmarkfood.restaurante.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.equipo.sanmarkfood.restaurante.data.local.entity.BorradorMenuEntity
import com.equipo.sanmarkfood.restaurante.data.local.entity.OpcionBorradorEntity
import com.equipo.sanmarkfood.restaurante.domain.model.menu.OrigenMenu
import kotlinx.coroutines.flow.Flow

@Dao
interface BorradorMenuDao {
    @Query("SELECT * FROM borrador_menu WHERE fecha = :fecha")
    suspend fun leerBorrador(fecha: String): BorradorMenuEntity?

    @Query("SELECT origen FROM borrador_menu WHERE fecha = :fecha")
    fun observarOrigen(fecha: String): Flow<OrigenMenu?>

    @Query("SELECT * FROM opcion_borrador WHERE fecha = :fecha ORDER BY id")
    suspend fun leerOpciones(fecha: String): List<OpcionBorradorEntity>

    @Insert
    suspend fun insertarBorrador(borrador: BorradorMenuEntity)

    @Insert
    suspend fun insertarOpciones(opciones: List<OpcionBorradorEntity>)

    @Query("DELETE FROM borrador_menu")
    suspend fun borrarTodo()

    @Transaction
    suspend fun reemplazar(borrador: BorradorMenuEntity, opciones: List<OpcionBorradorEntity>) {
        borrarTodo()
        insertarBorrador(borrador)
        insertarOpciones(opciones)
    }
}
