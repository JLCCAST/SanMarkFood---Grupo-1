package com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.CategoriaRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.DatosLocal
import com.equipo.sanmarkfood.restaurante.domain.model.DiaSemana
import com.equipo.sanmarkfood.restaurante.domain.model.EstadoRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.Horario
import com.equipo.sanmarkfood.restaurante.domain.model.MotivoRechazo
import com.equipo.sanmarkfood.restaurante.domain.model.Rechazo
import com.equipo.sanmarkfood.restaurante.domain.model.Restaurante
import com.equipo.sanmarkfood.restaurante.domain.model.Ubicacion
import com.equipo.sanmarkfood.restaurante.ui.theme.ApprestauranteTheme
import java.util.Locale

/**
 * Lo que ve el local en «Pedidos» según su estado (SCRUM-65): R5 mientras está pendiente, R6 si lo
 * rechazaron y, hasta que HU09 traiga la bandeja de pedidos, un aviso cuando ya está aprobado.
 */
@Composable
fun EstadoLocal(restaurante: Restaurante, onCorregir: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CabeceraLocal(restaurante.datos)
        when (restaurante.estado) {
            EstadoRestaurante.PENDIENTE -> TarjetaEnRevision()
            EstadoRestaurante.RECHAZADO -> TarjetaRechazado(restaurante.rechazo, onCorregir)
            EstadoRestaurante.APROBADO -> TarjetaAprobado()
            // Un borrador no llega aquí: el arranque lo manda a terminar el alta.
            EstadoRestaurante.BORRADOR -> Unit
        }
        if (restaurante.estado != EstadoRestaurante.APROBADO) ListaPreparacion(restaurante, onCorregir)
    }
}

@Composable
private fun CabeceraLocal(datos: DatosLocal) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        LogoLocal(logoUrl = datos.logoUrl, nombre = datos.nombre, tamano = 48.dp, radio = 14.dp)
        Column {
            Text(text = datos.nombre, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = stringResource(
                    R.string.estado_categoria_direccion,
                    stringResource(datos.categoria.etiqueta()),
                    datos.direccion,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TarjetaEnRevision() {
    TarjetaEstado(
        etiqueta = stringResource(R.string.estado_en_revision),
        titulo = stringResource(R.string.estado_en_revision_titulo),
        texto = stringResource(R.string.estado_en_revision_texto),
        fondo = MaterialTheme.colorScheme.secondaryContainer,
        contenido = MaterialTheme.colorScheme.onSecondaryContainer,
        marca = { PuntoParpadeante() },
    )
}

@Composable
private fun TarjetaRechazado(rechazo: Rechazo?, onCorregir: () -> Unit) {
    val indicacion = stringResource(R.string.estado_rechazado_texto)
    TarjetaEstado(
        etiqueta = stringResource(R.string.estado_rechazado),
        titulo = stringResource((rechazo?.motivo ?: MotivoRechazo.OTRO).titulo()),
        texto = rechazo?.detalle?.let { stringResource(R.string.estado_rechazado_con_detalle, it, indicacion) } ?: indicacion,
        fondo = MaterialTheme.colorScheme.errorContainer,
        contenido = MaterialTheme.colorScheme.onErrorContainer,
        accion = {
            Button(onClick = onCorregir, modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(48.dp)) {
                Text(text = stringResource(R.string.corregir_boton), style = MaterialTheme.typography.labelLarge)
            }
        },
    )
}

@Composable
private fun TarjetaAprobado() {
    TarjetaEstado(
        etiqueta = stringResource(R.string.estado_aprobado),
        titulo = stringResource(R.string.estado_aprobado_titulo),
        texto = stringResource(R.string.estado_aprobado_texto),
        fondo = MaterialTheme.colorScheme.tertiaryContainer,
        contenido = MaterialTheme.colorScheme.onTertiaryContainer,
    )
}

// Se anuncia sola al cambiar: si el administrador aprueba o rechaza mientras el local mira la pantalla.
@Composable
private fun TarjetaEstado(
    etiqueta: String,
    titulo: String,
    texto: String,
    fondo: Color,
    contenido: Color,
    marca: (@Composable () -> Unit)? = null,
    accion: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(fondo)
            .padding(16.dp)
            .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CompositionLocalProvider(LocalContentColor provides contenido) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                marca?.invoke()
                Text(text = etiqueta, style = MaterialTheme.typography.labelMedium)
            }
            Text(text = titulo, style = MaterialTheme.typography.headlineSmall)
            Text(text = texto, style = MaterialTheme.typography.bodyMedium)
        }
        accion?.invoke()
    }
}

// El prototipo usa un ámbar oscuro (#A86F00) que no está en el tema; se usa el color del texto de la tarjeta.
@Composable
private fun PuntoParpadeante() {
    val transicion = rememberInfiniteTransition(label = "enRevision")
    val alfa by transicion.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 800), RepeatMode.Reverse),
        label = "alfa",
    )
    Box(
        modifier = Modifier
            .size(8.dp)
            .graphicsLayer { alpha = alfa }
            .background(LocalContentColor.current, CircleShape),
    )
}

@Composable
private fun ListaPreparacion(restaurante: Restaurante, onCorregir: () -> Unit) {
    val rechazado = restaurante.estado == EstadoRestaurante.RECHAZADO
    val horario = restaurante.horario
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.preparacion_titulo),
            modifier = Modifier.padding(bottom = 4.dp),
            style = MaterialTheme.typography.titleSmall,
        )
        ItemPreparacion(
            titulo = stringResource(R.string.preparacion_cuenta),
            ayuda = stringResource(R.string.preparacion_cuenta_ayuda),
            listo = true,
        )
        ItemPreparacion(
            titulo = stringResource(R.string.preparacion_datos),
            ayuda = stringResource(
                when {
                    !rechazado -> R.string.preparacion_datos_ayuda
                    restaurante.rechazo?.motivo == MotivoRechazo.DIRECCION_NO_VERIFICABLE -> R.string.preparacion_datos_direccion
                    else -> R.string.preparacion_datos_revisar
                }
            ),
            listo = !rechazado,
            accion = if (rechazado) stringResource(R.string.corregir_accion) else null,
            onAccion = onCorregir,
        )
        ItemPreparacion(
            titulo = stringResource(R.string.preparacion_horario),
            ayuda = horario?.let { resumenDias(it) } ?: stringResource(R.string.preparacion_horario_falta),
            listo = horario != null,
        )
        // HU03 agrega el botón «Cargar» y el estado real de la carta.
        ItemPreparacion(
            titulo = stringResource(R.string.preparacion_carta),
            ayuda = stringResource(R.string.preparacion_carta_ayuda),
            listo = false,
        )
        ItemPreparacion(
            titulo = stringResource(R.string.preparacion_menu),
            ayuda = stringResource(R.string.preparacion_menu_ayuda),
            listo = false,
        )
    }
}

@Composable
private fun ItemPreparacion(
    titulo: String,
    ayuda: String,
    listo: Boolean,
    accion: String? = null,
    onAccion: () -> Unit = {},
) {
    val estado = stringResource(if (listo) R.string.preparacion_listo else R.string.preparacion_pendiente)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .semantics(mergeDescendants = true) { stateDescription = estado },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .then(
                    if (listo) Modifier.background(MaterialTheme.colorScheme.tertiary)
                    else Modifier.border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (listo) {
                Icon(
                    painter = painterResource(R.drawable.ic_listo),
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.onTertiary,
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = titulo, style = MaterialTheme.typography.labelLarge)
            Text(
                text = ayuda,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (accion != null) {
            Button(
                onClick = onAccion,
                modifier = Modifier.height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                ),
                contentPadding = PaddingValues(horizontal = 14.dp),
            ) {
                Text(text = accion, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

/** «Lun a sáb», «Lun a vie, dom», «Todos los días». */
@Composable
private fun resumenDias(horario: Horario): String {
    val tramos = horario.tramosAbiertos()
    if (tramos == listOf(DiaSemana.LUNES to DiaSemana.DOMINGO)) return stringResource(R.string.dias_todos)
    val partes = tramos.map { (desde, hasta) ->
        val primero = stringResource(desde.nombreCorto())
        val ultimo = stringResource(hasta.nombreCorto())
        when (hasta.ordinal - desde.ordinal) {
            0 -> primero
            1 -> stringResource(R.string.dias_par, primero, ultimo)
            else -> stringResource(R.string.dias_tramo, primero, ultimo)
        }
    }
    return partes.joinToString(", ").replaceFirstChar { it.titlecase(Locale.forLanguageTag("es-PE")) }
}

private val restauranteDeEjemplo = Restaurante(
    datos = DatosLocal(
        nombre = "La Sazón de Doña Carmen",
        categoria = CategoriaRestaurante.CRIOLLA,
        direccion = "Av. Venezuela 3450",
        ubicacion = Ubicacion.CiudadUniversitaria,
        telefono = "987654321",
        portadaUrl = null,
        logoUrl = null,
    ),
    estado = EstadoRestaurante.PENDIENTE,
    horario = Horario.PorDefecto,
    rechazo = null,
    pausado = false,
)

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun EnRevisionPreview() {
    ApprestauranteTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            EstadoLocal(restauranteDeEjemplo, onCorregir = {})
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun RechazadoPreview() {
    ApprestauranteTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            EstadoLocal(
                restauranteDeEjemplo.copy(
                    estado = EstadoRestaurante.RECHAZADO,
                    rechazo = Rechazo(
                        MotivoRechazo.DIRECCION_NO_VERIFICABLE,
                        "La dirección no coincide con el punto marcado en el mapa.",
                    ),
                ),
                onCorregir = {},
            )
        }
    }
}
