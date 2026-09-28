package com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante

import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.CategoriaRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.DiaSemana
import com.equipo.sanmarkfood.restaurante.domain.model.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.Hora
import java.util.Locale

@Composable
fun MensajeErrorRestaurante(error: ErrorRestaurante, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(error.mensaje()),
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
    )
}

@StringRes
fun CategoriaRestaurante.etiqueta(): Int = when (this) {
    CategoriaRestaurante.CRIOLLA -> R.string.categoria_criolla
    CategoriaRestaurante.CHIFA -> R.string.categoria_chifa
    CategoriaRestaurante.POLLERIA -> R.string.categoria_polleria
    CategoriaRestaurante.MARINA -> R.string.categoria_marina
    CategoriaRestaurante.VEGETARIANA -> R.string.categoria_vegetariana
    CategoriaRestaurante.OTRA -> R.string.categoria_otra
}

@StringRes
fun DiaSemana.nombre(): Int = when (this) {
    DiaSemana.LUNES -> R.string.dia_lunes
    DiaSemana.MARTES -> R.string.dia_martes
    DiaSemana.MIERCOLES -> R.string.dia_miercoles
    DiaSemana.JUEVES -> R.string.dia_jueves
    DiaSemana.VIERNES -> R.string.dia_viernes
    DiaSemana.SABADO -> R.string.dia_sabado
    DiaSemana.DOMINGO -> R.string.dia_domingo
}

fun Hora.texto(): String = "%02d:%02d".format(Locale.ROOT, hora, minuto)

@StringRes
private fun ErrorRestaurante.mensaje(): Int = when (this) {
    ErrorRestaurante.SinConexion -> R.string.error_sin_conexion
    is ErrorRestaurante.DatosInvalidos -> R.string.error_datos_invalidos
    ErrorRestaurante.NingunDiaAbierto -> R.string.error_ningun_dia_abierto
    is ErrorRestaurante.HorasInvalidas -> R.string.error_horas_invalidas
    ErrorRestaurante.ImagenIlegible -> R.string.error_imagen_ilegible
    ErrorRestaurante.Desconocido -> R.string.error_desconocido
}
