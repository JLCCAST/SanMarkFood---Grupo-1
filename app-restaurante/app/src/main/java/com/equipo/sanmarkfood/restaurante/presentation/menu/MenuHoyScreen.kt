package com.equipo.sanmarkfood.restaurante.presentation.menu

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.menu.DatosMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.EstadoMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.MenuDelDia
import com.equipo.sanmarkfood.restaurante.domain.model.menu.OpcionMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.OrigenMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.TipoOpcion
import com.equipo.sanmarkfood.restaurante.presentation.auth.BotonPrincipal
import com.equipo.sanmarkfood.restaurante.ui.theme.ApprestauranteTheme
import com.equipo.sanmarkfood.restaurante.ui.theme.extendedColors
import java.util.Date

@Composable
fun MenuHoyScreen(
    onElegirSeccion: (SeccionMenu) -> Unit,
    onArmarMenu: (ModoArmarMenu) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MenuHoyViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MenuHoyContenido(
        uiState = uiState,
        onElegirSeccion = onElegirSeccion,
        onArmarMenu = onArmarMenu,
        onCambiarDisponible = viewModel::onCambiarDisponible,
        onReintentar = viewModel::onReintentar,
        modifier = modifier,
    )
}

@Composable
private fun MenuHoyContenido(
    uiState: MenuHoyUiState,
    onElegirSeccion: (SeccionMenu) -> Unit,
    onArmarMenu: (ModoArmarMenu) -> Unit,
    onCambiarDisponible: (TipoOpcion, Int, Boolean) -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val menu = uiState.menu
    val error = uiState.error
    Column(modifier = modifier.fillMaxSize()) {
        CabeceraMenu(
            seccion = SeccionMenu.HOY,
            detalle = textoFecha(uiState.fecha),
            onElegirSeccion = onElegirSeccion,
        )

        val contenido = Modifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)
        when {
            error != null -> Column(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MensajeErrorMenu(error = error)
                BotonPrincipal(texto = stringResource(R.string.arranque_reintentar), onClick = onReintentar)
            }

            uiState.cargando -> Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            menu == null -> MenuSinPublicar(menuDeAyer = uiState.menuDeAyer, onArmarMenu = onArmarMenu, modifier = contenido)

            else -> Column(modifier = contenido, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                uiState.errorOpcion?.let { MensajeErrorMenu(error = it) }
                TarjetaMenu(
                    menu = menu,
                    cambiando = uiState.cambiandoOpcion,
                    onCambiarDisponible = onCambiarDisponible,
                )
            }
        }
    }
}

@Composable
private fun MenuSinPublicar(
    menuDeAyer: MenuDelDia?,
    onArmarMenu: (ModoArmarMenu) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = stringResource(R.string.menu_hoy_sin_publicar_titulo), style = MaterialTheme.typography.headlineSmall)
            Text(
                text = stringResource(R.string.menu_hoy_sin_publicar_texto),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (menuDeAyer != null) {
            OpcionInicio(
                icono = R.drawable.ic_copiar,
                titulo = stringResource(R.string.menu_hoy_copiar),
                ayuda = resumenMenu(menuDeAyer.datos),
                onClick = { onArmarMenu(ModoArmarMenu.COPIAR_AYER) },
            )
        }
        OpcionInicio(
            icono = R.drawable.ic_agregar,
            titulo = stringResource(R.string.menu_hoy_de_cero),
            ayuda = stringResource(R.string.menu_hoy_de_cero_ayuda),
            onClick = { onArmarMenu(ModoArmarMenu.CERO) },
        )
        Text(
            text = stringResource(R.string.menu_hoy_vale_hoy),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun resumenMenu(datos: DatosMenu): String {
    val nombres = (datos.entradas + datos.segundos).map { it.nombre }
    val opciones = nombres.take(3).joinToString(", ") + if (nombres.size > 3) "…" else ""
    return stringResource(R.string.menu_hoy_copiar_ayuda, opciones, precioTexto(datos.precio))
}

@Composable
private fun OpcionInicio(@DrawableRes icono: Int, titulo: String, ayuda: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painter = painterResource(icono), contentDescription = null)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = titulo, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = ayuda,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(painter = painterResource(R.drawable.ic_siguiente), contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun TarjetaMenu(
    menu: MenuDelDia,
    cambiando: Boolean,
    onCambiarDisponible: (TipoOpcion, Int, Boolean) -> Unit,
) {
    val forma = RoundedCornerShape(18.dp)
    val datos = menu.datos
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.5.dp, MaterialTheme.colorScheme.onSurface, forma),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.extendedColors.superficieFija)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.menu_hoy),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.extendedColors.sobreSuperficieFija,
                )
                Text(
                    text = menu.publicadoEn?.let { stringResource(R.string.menu_hoy_publicado_a, textoHora(it), datos.horaFin) }
                        ?: stringResource(R.string.menu_hoy_se_sirve_hasta, datos.horaFin),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.extendedColors.sobreSuperficieFijaTenue,
                )
            }
            Text(
                text = precioTexto(datos.precio),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        GrupoOpciones(
            titulo = stringResource(R.string.menu_entradas),
            opciones = datos.entradas,
            cambiando = cambiando,
            onCambiarDisponible = { indice, disponible -> onCambiarDisponible(TipoOpcion.ENTRADA, indice, disponible) },
        )
        GrupoOpciones(
            titulo = stringResource(R.string.menu_segundos),
            opciones = datos.segundos,
            cambiando = cambiando,
            onCambiarDisponible = { indice, disponible -> onCambiarDisponible(TipoOpcion.SEGUNDO, indice, disponible) },
        )
        textoIncluye(datos)?.let {
            Text(
                text = it,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 14.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GrupoOpciones(
    titulo: String,
    opciones: List<OpcionMenu>,
    cambiando: Boolean,
    onCambiarDisponible: (indice: Int, disponible: Boolean) -> Unit,
) {
    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 4.dp)) {
        Text(
            text = titulo,
            modifier = Modifier.padding(bottom = 2.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        opciones.forEachIndexed { indice, opcion ->
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = opcion.nombre,
                        style = MaterialTheme.typography.titleSmall.copy(
                            textDecoration = if (opcion.agotado) TextDecoration.LineThrough else TextDecoration.None,
                        ),
                        color = if (opcion.agotado) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(if (opcion.agotado) R.string.agotado else R.string.disponible),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                InterruptorDisponible(
                    disponible = !opcion.agotado,
                    onCambiar = { disponible -> onCambiarDisponible(indice, disponible) },
                    descripcion = stringResource(R.string.disponible_descripcion, opcion.nombre),
                    habilitado = !cambiando,
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)
        }
    }
}

@Composable
private fun textoIncluye(datos: DatosMenu): String? {
    val incluidos = listOfNotNull(datos.refresco, datos.postre)
    return when (incluidos.size) {
        0 -> null
        1 -> stringResource(R.string.menu_incluye, incluidos[0])
        else -> stringResource(R.string.menu_incluye_dos, incluidos[0], incluidos[1])
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun MenuSinPublicarPreview() {
    ApprestauranteTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            MenuHoyContenido(
                uiState = MenuHoyUiState(fecha = "2026-10-05", cargando = false),
                onElegirSeccion = {},
                onArmarMenu = {},
                onCambiarDisponible = { _, _, _ -> },
                onReintentar = {},
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun MenuPublicadoPreview() {
    val opciones = { nombres: List<String> -> nombres.map { OpcionMenu(it, agotado = false) } }
    ApprestauranteTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            MenuHoyContenido(
                uiState = MenuHoyUiState(
                    fecha = "2026-10-05",
                    cargando = false,
                    menu = MenuDelDia(
                        fecha = "2026-10-05",
                        datos = DatosMenu(
                            precio = 1400,
                            entradas = opciones(listOf("Papa a la huancaína", "Causa limeña", "Sopa de casa")),
                            segundos = opciones(listOf("Ají de gallina", "Seco con frejoles", "Arroz con pollo")),
                            refresco = "Chicha morada",
                            postre = "Mazamorra morada",
                            horaFin = "15:00",
                        ),
                        estado = EstadoMenu.PUBLICADO,
                        origen = OrigenMenu.CERO,
                        publicadoEn = Date(),
                    ),
                ),
                onElegirSeccion = {},
                onArmarMenu = {},
                onCambiarDisponible = { _, _, _ -> },
                onReintentar = {},
            )
        }
    }
}
