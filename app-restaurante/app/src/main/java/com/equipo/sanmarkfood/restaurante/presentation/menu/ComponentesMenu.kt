package com.equipo.sanmarkfood.restaurante.presentation.menu

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun CabeceraMenu(
    seccion: SeccionMenu,
    detalle: String?,
    onElegirSeccion: (SeccionMenu) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = stringResource(R.string.menu_titulo),
                modifier = Modifier.alignByBaseline(),
                style = MaterialTheme.typography.displaySmall,
            )
            if (detalle != null) {
                Text(
                    text = detalle,
                    modifier = Modifier.alignByBaseline(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        SelectorSeccion(actual = seccion, onElegir = onElegirSeccion)
    }
}

@Composable
private fun SelectorSeccion(actual: SeccionMenu, onElegir: (SeccionMenu) -> Unit) {
    val forma = RoundedCornerShape(22.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(forma)
            .border(1.dp, MaterialTheme.colorScheme.outline, forma)
            .selectableGroup(),
    ) {
        SeccionMenu.entries.forEachIndexed { indice, seccion ->
            if (indice > 0) VerticalDivider(color = MaterialTheme.colorScheme.outline)
            val elegida = seccion == actual
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .then(if (elegida) Modifier.background(MaterialTheme.colorScheme.inverseSurface) else Modifier)
                    .selectable(selected = elegida, role = Role.Tab, onClick = { onElegir(seccion) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(seccion.etiqueta),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (elegida) MaterialTheme.colorScheme.inverseOnSurface else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
fun CampoPrecio(
    etiqueta: String,
    valor: String,
    onValorChange: (String) -> Unit,
    esError: Boolean,
    mensajeError: String,
    porRevisar: Boolean = false,
) {
    val fondo = if (porRevisar) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest
    val texto = if (porRevisar) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = etiqueta, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = valor,
                onValueChange = onValorChange,
                modifier = Modifier.width(150.dp),
                textStyle = MaterialTheme.typography.titleMedium,
                prefix = { Text(text = stringResource(R.string.plato_moneda), style = MaterialTheme.typography.titleMedium) },
                placeholder = { Text(text = stringResource(R.string.plato_precio_ejemplo)) },
                isError = esError,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = fondo,
                    unfocusedContainerColor = fondo,
                    errorContainerColor = fondo,
                    focusedTextColor = texto,
                    unfocusedTextColor = texto,
                    focusedPrefixColor = texto,
                    unfocusedPrefixColor = texto,
                    unfocusedBorderColor = if (porRevisar) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                ),
            )
        }
        if (esError) {
            Text(text = mensajeError, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

private val HORAS_FIN = listOf("14:00", "14:30", "15:00", "15:30", "16:00")

@Composable
fun SelectorHoraFin(elegida: String, onElegir: (String) -> Unit, habilitado: Boolean = true) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(R.string.armar_se_sirve_hasta), style = MaterialTheme.typography.titleSmall)
        Row(modifier = Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            HORAS_FIN.forEach { hora ->
                val seleccionada = hora == elegida
                val forma = RoundedCornerShape(10.dp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(forma)
                        .border(
                            1.5.dp,
                            if (seleccionada) MaterialTheme.colorScheme.inverseSurface else MaterialTheme.colorScheme.outline,
                            forma,
                        )
                        .then(if (seleccionada) Modifier.background(MaterialTheme.colorScheme.inverseSurface) else Modifier)
                        .selectable(
                            selected = seleccionada,
                            enabled = habilitado,
                            role = Role.RadioButton,
                            onClick = { onElegir(hora) },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = hora,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (seleccionada) MaterialTheme.colorScheme.inverseOnSurface else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
fun InterruptorDisponible(
    disponible: Boolean,
    onCambiar: (Boolean) -> Unit,
    descripcion: String,
    habilitado: Boolean = true,
) {
    Switch(
        checked = disponible,
        onCheckedChange = onCambiar,
        modifier = Modifier.semantics { contentDescription = descripcion },
        enabled = habilitado,
        colors = SwitchDefaults.colors(
            checkedTrackColor = MaterialTheme.colorScheme.tertiary,
            checkedBorderColor = MaterialTheme.colorScheme.tertiary,
            checkedThumbColor = MaterialTheme.colorScheme.onTertiary,
            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    )
}

@Composable
fun precioTexto(centimos: Int): String = stringResource(R.string.precio_soles, precioEnSoles(centimos))

fun precioEnSoles(centimos: Int): String = "%d.%02d".format(Locale.ROOT, centimos / 100, centimos % 100)

fun textoFecha(fecha: String): String {
    val dia = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).parse(fecha) ?: return fecha
    val espanol = Locale.forLanguageTag("es-PE")
    return SimpleDateFormat("EEEE d 'de' MMMM", espanol).format(dia).replaceFirstChar { it.titlecase(espanol) }
}

fun textoHora(momento: Date): String =
    SimpleDateFormat("H:mm", Locale.ROOT)
        .apply { timeZone = TimeZone.getTimeZone("America/Lima") }
        .format(momento)

@Composable
fun MensajeErrorMenu(error: ErrorMenu, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(error.mensaje()),
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
    )
}

@StringRes
private fun ErrorMenu.mensaje(): Int = when (this) {
    ErrorMenu.SinConexion -> R.string.error_sin_conexion
    ErrorMenu.NombreCategoriaInvalido -> R.string.error_nombre_categoria
    ErrorMenu.CategoriaRepetida -> R.string.error_categoria_repetida
    is ErrorMenu.DatosPlatoInvalidos -> R.string.error_datos_invalidos
    is ErrorMenu.DatosMenuInvalidos -> R.string.error_datos_menu
    ErrorMenu.MenuYaPublicado -> R.string.error_menu_ya_publicado
    ErrorMenu.ImagenIlegible -> R.string.error_imagen_ilegible
    ErrorMenu.PizarraSinMenu -> R.string.error_pizarra_sin_menu
    ErrorMenu.Desconocido -> R.string.error_desconocido
}
