package com.equipo.sanmarkfood.restaurante.domain.repository

interface DispositivosRepository {
    fun registrar()

    fun renovar(token: String)

    fun olvidar()
}
