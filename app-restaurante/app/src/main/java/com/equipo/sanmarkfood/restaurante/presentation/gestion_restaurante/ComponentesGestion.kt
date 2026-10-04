package com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.CategoriaRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.DiaSemana
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Hora
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.MotivoRechazo
import java.util.Locale

/** El logo del local, o la inicial de su nombre si no subió uno (el logo es opcional desde SCRUM-63). */
@Composable
fun LogoLocal(logoUrl: String?, nombre: String, tamano: Dp, radio: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(tamano)
            .clip(RoundedCornerShape(radio))
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        if (logoUrl != null) {
            AsyncImage(
                model = logoUrl,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                text = nombre.trim().take(1).uppercase(Locale.forLanguageTag("es-PE")),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

/** Cabecera de las pantallas que editan algo ya guardado (O7, O8, selector de ubicación). */
@Composable
fun CabeceraEdicion(titulo: String, onVolver: () -> Unit, accion: @Composable () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        IconButton(onClick = onVolver) {
            Icon(
                painter = painterResource(R.drawable.ic_volver),
                contentDescription = stringResource(R.string.volver),
            )
        }
        Text(text = titulo, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
        accion()
    }
}

/** Se muestra al salir de O7 u O8 con cambios sin guardar. */
@Composable
fun DialogoDescartarCambios(onSeguirEditando: () -> Unit, onDescartar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onSeguirEditando,
        title = { Text(text = stringResource(R.string.descartar_titulo)) },
        text = { Text(text = stringResource(R.string.descartar_texto)) },
        confirmButton = {
            TextButton(onClick = onDescartar) {
                Text(text = stringResource(R.string.descartar_confirmar), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onSeguirEditando) {
                Text(text = stringResource(R.string.descartar_seguir))
            }
        },
    )
}

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
fun MotivoRechazo.titulo(): Int = when (this) {
    MotivoRechazo.DATOS_INCOMPLETOS -> R.string.motivo_datos_incompletos
    MotivoRechazo.DIRECCION_NO_VERIFICABLE -> R.string.motivo_direccion_no_verificable
    MotivoRechazo.LOCAL_DUPLICADO -> R.string.motivo_local_duplicado
    MotivoRechazo.OTRO -> R.string.motivo_otro
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

@StringRes
fun DiaSemana.nombreCorto(): Int = when (this) {
    DiaSemana.LUNES -> R.string.dia_corto_lunes
    DiaSemana.MARTES -> R.string.dia_corto_martes
    DiaSemana.MIERCOLES -> R.string.dia_corto_miercoles
    DiaSemana.JUEVES -> R.string.dia_corto_jueves
    DiaSemana.VIERNES -> R.string.dia_corto_viernes
    DiaSemana.SABADO -> R.string.dia_corto_sabado
    DiaSemana.DOMINGO -> R.string.dia_corto_domingo
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
