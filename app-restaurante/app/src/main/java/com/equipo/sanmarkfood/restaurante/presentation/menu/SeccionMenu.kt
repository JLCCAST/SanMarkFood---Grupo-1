package com.equipo.sanmarkfood.restaurante.presentation.menu

import androidx.annotation.StringRes
import com.equipo.sanmarkfood.restaurante.R

enum class SeccionMenu(@param:StringRes val etiqueta: Int) {
    HOY(R.string.menu_hoy),
    CARTA(R.string.menu_carta),
}
