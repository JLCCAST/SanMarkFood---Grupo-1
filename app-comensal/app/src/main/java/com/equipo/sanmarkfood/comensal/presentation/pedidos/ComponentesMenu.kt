package com.equipo.sanmarkfood.comensal.presentation.pedidos

import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.MenuDeHoy
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.OpcionMenu
import com.equipo.sanmarkfood.comensal.ui.theme.extendedColors

@Composable
fun TarjetaMenuDeHoy(
    precio: Int,
    horaFin: String,
    menu: MenuDeHoy?,
    menusEnPedido: Int,
    onArmar: () -> Unit,
) {
    val fijos = MaterialTheme.extendedColors
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = fijos.superficieFija),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.detalle_menu_titulo),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = fijos.sobreSuperficieFija
                )
                Text(
                    text = stringResource(R.string.detalle_menu_sirve_hasta, horaFin),
                    style = MaterialTheme.typography.bodySmall,
                    color = fijos.sobreSuperficieFijaTenue
                )
            }
            Text(
                text = stringResource(R.string.detalle_precio_soles, formatoSoles(precio)),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (menu == null) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                } else {
                    ResumenOpciones(R.string.menu_elige_entrada, menu.entradas)
                    ResumenOpciones(R.string.menu_elige_segundo, menu.segundos)
                    val incluidos = listOfNotNull(menu.refresco, menu.postre)
                    if (incluidos.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            incluidos.forEach { EtiquetaIncluye(it) }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (menusEnPedido > 0) {
                            pluralStringResource(R.plurals.menu_en_pedido, menusEnPedido, menusEnPedido)
                        } else {
                            stringResource(R.string.menu_incluidos_por_defecto)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Button(onClick = onArmar, enabled = menu != null) {
                        Text(stringResource(R.string.detalle_boton_armar_menu))
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumenOpciones(@StringRes titulo: Int, opciones: List<OpcionMenu>) {
    val agotada = stringResource(R.string.menu_opcion_agotada)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(titulo),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = opciones.joinToString(" · ") { if (it.agotado) "${it.nombre} $agotada" else it.nombre },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun EtiquetaIncluye(texto: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer
    ) {
        Text(
            text = stringResource(R.string.menu_incluye, texto),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaArmarMenu(
    menu: MenuDeHoy,
    armado: ArmadoMenu,
    puedeAgregar: Boolean,
    onElegirEntrada: (String) -> Unit,
    onElegirSegundo: (String) -> Unit,
    onAgregar: () -> Unit,
    onCerrar: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.menu_arma_titulo),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onCerrar) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.menu_cerrar)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GrupoOpciones(
                    titulo = stringResource(R.string.menu_elige_entrada),
                    opciones = menu.entradas,
                    elegida = armado.entrada,
                    onElegir = onElegirEntrada
                )
                GrupoOpciones(
                    titulo = stringResource(R.string.menu_elige_segundo),
                    opciones = menu.segundos,
                    elegida = armado.segundo,
                    onElegir = onElegirSegundo
                )
                val incluidos = listOfNotNull(menu.refresco, menu.postre)
                if (incluidos.isNotEmpty()) {
                    Text(
                        text = if (incluidos.size == 1) {
                            stringResource(R.string.menu_incluido_uno, incluidos[0])
                        } else {
                            stringResource(R.string.menu_incluido_dos, incluidos[0], incluidos[1])
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
            Button(
                onClick = onAgregar,
                enabled = puedeAgregar,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .height(52.dp)
            ) {
                Text(
                    text = stringResource(R.string.menu_agregar, formatoSoles(menu.precio)),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun GrupoOpciones(
    titulo: String,
    opciones: List<OpcionMenu>,
    elegida: String?,
    onElegir: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        opciones.forEach { opcion ->
            FilaOpcion(
                opcion = opcion,
                seleccionada = opcion.nombre == elegida && !opcion.agotado,
                onElegir = { onElegir(opcion.nombre) }
            )
        }
    }
}

@Composable
private fun FilaOpcion(opcion: OpcionMenu, seleccionada: Boolean, onElegir: () -> Unit) {
    val forma = RoundedCornerShape(12.dp)
    val colores = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(forma)
            .border(
                width = 1.5.dp,
                color = when {
                    opcion.agotado -> colores.surfaceContainerHigh
                    seleccionada -> colores.primary
                    else -> colores.outlineVariant
                },
                shape = forma
            )
            .selectable(
                selected = seleccionada,
                enabled = !opcion.agotado,
                role = Role.RadioButton,
                onClick = onElegir
            )
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = seleccionada, onClick = null, enabled = !opcion.agotado)
        Text(
            text = opcion.nombre,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (opcion.agotado) colores.onSurfaceVariant else colores.onSurface,
            textDecoration = if (opcion.agotado) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier.weight(1f)
        )
        if (opcion.agotado) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = colores.surfaceContainerHigh,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.menu_se_agoto),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = colores.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}
