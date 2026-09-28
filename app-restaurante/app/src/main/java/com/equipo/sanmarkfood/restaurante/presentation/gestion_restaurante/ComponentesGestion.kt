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
import com.equipo.sanmarkfood.restaurante.domain.model.ErrorRestaurante

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
private fun ErrorRestaurante.mensaje(): Int = when (this) {
    ErrorRestaurante.SinConexion -> R.string.error_sin_conexion
    is ErrorRestaurante.DatosInvalidos -> R.string.error_datos_invalidos
    ErrorRestaurante.Desconocido -> R.string.error_desconocido
}
