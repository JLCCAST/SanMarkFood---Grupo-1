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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import java.util.Locale

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
fun precioTexto(centimos: Int): String = stringResource(R.string.precio_soles, precioEnSoles(centimos))

fun precioEnSoles(centimos: Int): String = "%d.%02d".format(Locale.ROOT, centimos / 100, centimos % 100)

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
    ErrorMenu.ImagenIlegible -> R.string.error_imagen_ilegible
    ErrorMenu.Desconocido -> R.string.error_desconocido
}
