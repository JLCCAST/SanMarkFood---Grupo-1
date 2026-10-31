package com.equipo.sanmarkfood.comensal.domain.repository

interface DispositivosRepository {
    fun registrar()

    fun renovar(token: String)

    fun olvidar()
}
