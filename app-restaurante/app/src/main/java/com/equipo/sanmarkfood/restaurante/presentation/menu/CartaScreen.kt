package com.equipo.sanmarkfood.restaurante.presentation.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.menu.Categoria
import com.equipo.sanmarkfood.restaurante.domain.model.menu.DatosPlato
import com.equipo.sanmarkfood.restaurante.domain.model.menu.Plato
import com.equipo.sanmarkfood.restaurante.domain.model.menu.SeccionCarta
import com.equipo.sanmarkfood.restaurante.presentation.auth.BotonPrincipal
import com.equipo.sanmarkfood.restaurante.ui.theme.ApprestauranteTheme
import java.util.Locale

@Composable
fun CartaScreen(
    onElegirSeccion: (SeccionMenu) -> Unit,
    onAgregarPlato: () -> Unit,
    onAbrirPlato: (platoId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CartaViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CartaContenido(
        uiState = uiState,
        onElegirSeccion = onElegirSeccion,
        onAgregarPlato = onAgregarPlato,
        onAbrirPlato = onAbrirPlato,
        onReintentar = viewModel::onReintentar,
        modifier = modifier,
    )
}

@Composable
private fun CartaContenido(
    uiState: CartaUiState,
    onElegirSeccion: (SeccionMenu) -> Unit,
    onAgregarPlato: () -> Unit,
    onAbrirPlato: (platoId: String) -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val secciones = uiState.secciones
    val error = uiState.error
    Column(modifier = modifier.fillMaxSize()) {
        CabeceraMenu(
            seccion = SeccionMenu.CARTA,
            detalle = secciones?.let { resumenCarta(it) },
            onElegirSeccion = onElegirSeccion,
        )
        Button(
            onClick = onAgregarPlato,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp).fillMaxWidth().height(44.dp),
        ) {
            Icon(painter = painterResource(R.drawable.ic_agregar), contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = stringResource(R.string.carta_agregar_plato), style = MaterialTheme.typography.labelLarge)
        }

        when {
            error != null -> Column(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MensajeErrorMenu(error = error)
                BotonPrincipal(texto = stringResource(R.string.arranque_reintentar), onClick = onReintentar)
            }

            secciones == null -> Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            secciones.isEmpty() -> CartaVacia(modifier = Modifier.weight(1f).fillMaxWidth())

            else -> LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
            ) {
                secciones.forEach { seccion ->
                    item(key = "categoria-${seccion.categoria.id}") { TituloSeccion(seccion) }
                    items(seccion.platos, key = { it.id }) { plato -> FilaPlato(plato, onAbrir = { onAbrirPlato(plato.id) }) }
                }
            }
        }
    }
}

@Composable
private fun resumenCarta(secciones: List<SeccionCarta>): String {
    val platos = secciones.sumOf { it.platos.size }
    if (platos == 0) return stringResource(R.string.carta_sin_platos)
    return stringResource(
        R.string.carta_resumen,
        pluralStringResource(R.plurals.carta_platos, platos, platos),
        pluralStringResource(R.plurals.carta_categorias, secciones.size, secciones.size),
    )
}

@Composable
private fun CartaVacia(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.carta_vacia_titulo),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.carta_vacia_texto),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun TituloSeccion(seccion: SeccionCarta) {
    Row(
        modifier = Modifier.padding(top = 16.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = seccion.categoria.nombre.uppercase(Locale.forLanguageTag("es-PE")),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = pluralStringResource(R.plurals.carta_platos, seccion.platos.size, seccion.platos.size),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun FilaPlato(plato: Plato, onAbrir: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clickable(role = Role.Button, onClick = onAbrir)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        ) {
            plato.fotoUrl?.let {
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = plato.datos.nombre, style = MaterialTheme.typography.labelLarge)
            Text(
                text = precioTexto(plato.datos.precio),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

private fun platoDeEjemplo(id: String, nombre: String, precio: Int, categoriaId: String) =
    Plato(id = id, datos = DatosPlato(nombre, null, precio, categoriaId), fotoUrl = null)

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CartaPreview() {
    ApprestauranteTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            CartaContenido(
                uiState = CartaUiState(
                    secciones = listOf(
                        SeccionCarta(
                            Categoria("p", "Platos", 0),
                            listOf(
                                platoDeEjemplo("1", "Chicharrón de pescado", 2600, "p"),
                                platoDeEjemplo("2", "Lomo saltado", 2400, "p"),
                                platoDeEjemplo("3", "Tallarines verdes", 2200, "p"),
                            ),
                        ),
                        SeccionCarta(
                            Categoria("b", "Bebidas", 1),
                            listOf(
                                platoDeEjemplo("4", "Chicha morada 1 L", 800, "b"),
                                platoDeEjemplo("5", "Limonada frozen", 700, "b"),
                            ),
                        ),
                    ),
                ),
                onElegirSeccion = {},
                onAgregarPlato = {},
                onAbrirPlato = {},
                onReintentar = {},
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CartaVaciaPreview() {
    ApprestauranteTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            CartaContenido(
                uiState = CartaUiState(secciones = emptyList()),
                onElegirSeccion = {},
                onAgregarPlato = {},
                onAbrirPlato = {},
                onReintentar = {},
            )
        }
    }
}
