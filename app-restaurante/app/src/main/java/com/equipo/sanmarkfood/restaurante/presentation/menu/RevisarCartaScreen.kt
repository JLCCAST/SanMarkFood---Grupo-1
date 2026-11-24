package com.equipo.sanmarkfood.restaurante.presentation.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.presentation.auth.BotonPrincipal
import com.equipo.sanmarkfood.restaurante.ui.theme.ApprestauranteTheme
import java.util.Locale

@Composable
fun RevisarCartaScreen(
    onTomarOtraFoto: () -> Unit,
    onAgregada: () -> Unit,
    viewModel: RevisarCartaViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.agregada) {
        if (uiState.agregada) onAgregada()
    }

    RevisarCartaContenido(
        uiState = uiState,
        onTomarOtraFoto = onTomarOtraFoto,
        onCambiarNombre = viewModel::onCambiarNombre,
        onCambiarPrecio = viewModel::onCambiarPrecio,
        onMarcar = viewModel::onMarcar,
        onAgregar = viewModel::onAgregar,
    )
}

@Composable
private fun RevisarCartaContenido(
    uiState: RevisarCartaUiState,
    onTomarOtraFoto: () -> Unit,
    onCambiarNombre: (Int, String) -> Unit,
    onCambiarPrecio: (Int, String) -> Unit,
    onMarcar: (Int, Boolean) -> Unit,
    onAgregar: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            IconButton(onClick = onTomarOtraFoto, enabled = !uiState.agregando) {
                Icon(painter = painterResource(R.drawable.ic_volver), contentDescription = stringResource(R.string.revision_otra_foto))
            }
            Text(text = stringResource(R.string.revision_titulo), style = MaterialTheme.typography.titleLarge)
        }

        if (uiState.filas.isEmpty()) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.revision_sin_lectura),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
                BotonPrincipal(texto = stringResource(R.string.revision_otra_foto), onClick = onTomarOtraFoto)
            }
            return@Column
        }

        val secciones = uiState.secciones
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "aviso") { AvisoLectura(platos = uiState.filas.size, categorias = secciones.size) }
            secciones.forEach { (categoria, filas) ->
                item(key = "categoria-$categoria") { TituloCategoria(categoria) }
                items(filas, key = { it.id }) { fila ->
                    FilaPlatoLeido(
                        fila = fila,
                        habilitada = !uiState.agregando,
                        onCambiarNombre = { onCambiarNombre(fila.id, it) },
                        onCambiarPrecio = { onCambiarPrecio(fila.id, it) },
                        onMarcar = { onMarcar(fila.id, it) },
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            uiState.error?.let { MensajeErrorMenu(error = it) }
            BotonPrincipal(
                texto = pluralStringResource(R.plurals.revision_agregar, uiState.marcadas, uiState.marcadas),
                onClick = onAgregar,
                habilitado = uiState.marcadas > 0,
                cargando = uiState.agregando,
            )
            Text(
                text = stringResource(R.string.revision_nota),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AvisoLectura(platos: Int, categorias: Int) {
    val textoPlatos = pluralStringResource(R.plurals.carta_platos, platos, platos)
    val textoCategorias = pluralStringResource(R.plurals.carta_categorias, categorias, categorias)
    Text(
        text = buildAnnotatedString {
            append(stringResource(R.string.revision_aviso_inicio))
            append(" ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(textoPlatos) }
            append(" ")
            append(stringResource(R.string.revision_aviso_resto, textoCategorias))
        },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
    )
}

@Composable
private fun TituloCategoria(nombre: String) {
    Row(
        modifier = Modifier.padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = nombre.uppercase(Locale.forLanguageTag("es-PE")),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun FilaPlatoLeido(
    fila: FilaLeida,
    habilitada: Boolean,
    onCambiarNombre: (String) -> Unit,
    onCambiarPrecio: (String) -> Unit,
    onMarcar: (Boolean) -> Unit,
) {
    val colores = MaterialTheme.colorScheme
    val dudosa = fila.nombreDudoso || fila.precioDudoso
    val forma = RoundedCornerShape(12.dp)
    val descripcion = stringResource(R.string.revision_marcar, fila.nombre)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(
                when {
                    dudosa -> colores.secondaryContainer
                    fila.marcada -> colores.surfaceContainerLowest
                    else -> colores.surfaceContainer
                }
            )
            .border(
                1.5.dp,
                when {
                    fila.invalida -> colores.error
                    dudosa -> colores.secondary
                    else -> colores.outlineVariant
                },
                forma,
            )
            .padding(end = 10.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Checkbox(
            checked = fila.marcada,
            onCheckedChange = onMarcar,
            enabled = habilitada,
            modifier = Modifier.semantics { contentDescription = descripcion },
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CampoLeido(
                    valor = fila.nombre,
                    onValorChange = onCambiarNombre,
                    descripcion = stringResource(R.string.revision_nombre),
                    dudoso = fila.nombreDudoso,
                    habilitado = habilitada,
                    teclado = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.weight(1f),
                )
                CampoLeido(
                    valor = fila.precio,
                    onValorChange = onCambiarPrecio,
                    descripcion = stringResource(R.string.revision_precio),
                    dudoso = fila.precioDudoso,
                    habilitado = habilitada,
                    teclado = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    prefijo = stringResource(R.string.plato_moneda),
                    modifier = Modifier.width(112.dp),
                )
            }
            val aviso = when {
                fila.invalida -> R.string.revision_invalido
                fila.precioDudoso -> R.string.revision_precio_dudoso
                fila.nombreDudoso -> R.string.revision_nombre_dudoso
                else -> null
            }
            aviso?.let {
                Text(
                    text = stringResource(it),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (fila.invalida) colores.error else colores.onSecondaryContainer,
                )
            }
        }
    }
}

@Composable
private fun CampoLeido(
    valor: String,
    onValorChange: (String) -> Unit,
    descripcion: String,
    dudoso: Boolean,
    habilitado: Boolean,
    teclado: KeyboardOptions,
    modifier: Modifier = Modifier,
    prefijo: String? = null,
) {
    val colores = MaterialTheme.colorScheme
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        modifier = modifier.semantics { contentDescription = descripcion },
        enabled = habilitado,
        textStyle = MaterialTheme.typography.titleSmall,
        prefix = prefijo?.let { { Text(text = it, style = MaterialTheme.typography.titleSmall) } },
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        keyboardOptions = teclado,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colores.surfaceContainerLowest,
            unfocusedContainerColor = colores.surfaceContainerLowest,
            unfocusedBorderColor = if (dudoso) colores.secondary else colores.outlineVariant,
        ),
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun RevisarCartaPreview() {
    ApprestauranteTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            RevisarCartaContenido(
                uiState = RevisarCartaUiState(
                    filas = listOf(
                        FilaLeida(0, "Platos", "Lomo saltado", "24.00", nombreDudoso = false, precioDudoso = false),
                        FilaLeida(1, "Platos", "Tallarines verdes c/ bistec", "", nombreDudoso = false, precioDudoso = true),
                        FilaLeida(2, "Bebidas", "Chicha morada 1 L", "8.00", nombreDudoso = false, precioDudoso = false),
                        FilaLeida(3, "Postres", "Arroz con leche", "5.00", nombreDudoso = true, precioDudoso = false, marcada = false),
                    ),
                ),
                onTomarOtraFoto = {},
                onCambiarNombre = { _, _ -> },
                onCambiarPrecio = { _, _ -> },
                onMarcar = { _, _ -> },
                onAgregar = {},
            )
        }
    }
}
