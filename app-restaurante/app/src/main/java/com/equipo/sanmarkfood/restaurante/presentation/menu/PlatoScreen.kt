package com.equipo.sanmarkfood.restaurante.presentation.menu

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.menu.CampoPlato
import com.equipo.sanmarkfood.restaurante.domain.model.menu.Categoria
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.equipo.sanmarkfood.restaurante.presentation.auth.BotonPrincipal
import com.equipo.sanmarkfood.restaurante.presentation.auth.CampoFormulario
import com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante.CabeceraEdicion
import com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante.bordePunteado
import com.equipo.sanmarkfood.restaurante.ui.theme.ApprestauranteTheme

@Composable
fun PlatoScreen(
    onVolver: () -> Unit,
    onTerminado: () -> Unit,
    viewModel: PlatoViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.terminado) {
        if (uiState.terminado) onTerminado()
    }

    if (uiState.confirmandoEliminacion) {
        DialogoEliminarPlato(
            nombre = uiState.plato?.datos?.nombre.orEmpty(),
            onEliminar = viewModel::onConfirmarEliminacion,
            onVolver = viewModel::onCancelarEliminacion,
        )
    }

    uiState.nuevaCategoria?.let { dialogo ->
        DialogoNuevaCategoria(
            estado = dialogo,
            onCambiarNombre = viewModel::onCambiarNombreCategoria,
            onCrear = viewModel::onCrearCategoria,
            onCerrar = viewModel::onCerrarNuevaCategoria,
        )
    }

    PlatoContenido(
        uiState = uiState,
        onVolver = onVolver,
        onEliminar = viewModel::onEliminar,
        onReintentarCarga = viewModel::onReintentarCarga,
        onFotoElegida = viewModel::onFotoElegida,
        onCambiarNombre = viewModel::onCambiarNombre,
        onCambiarDescripcion = viewModel::onCambiarDescripcion,
        onCambiarPrecio = viewModel::onCambiarPrecio,
        onElegirCategoria = viewModel::onElegirCategoria,
        onNuevaCategoria = viewModel::onNuevaCategoria,
        onReintentarCategorias = viewModel::onReintentarCategorias,
        onGuardar = viewModel::onGuardar,
    )
}

@Composable
private fun PlatoContenido(
    uiState: PlatoUiState,
    onVolver: () -> Unit,
    onEliminar: () -> Unit,
    onReintentarCarga: () -> Unit,
    onFotoElegida: (String) -> Unit,
    onCambiarNombre: (String) -> Unit,
    onCambiarDescripcion: (String) -> Unit,
    onCambiarPrecio: (String) -> Unit,
    onElegirCategoria: (String) -> Unit,
    onNuevaCategoria: () -> Unit,
    onReintentarCategorias: () -> Unit,
    onGuardar: () -> Unit,
) {
    val errorCarga = uiState.errorCarga
    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        CabeceraEdicion(
            titulo = stringResource(if (uiState.editando) R.string.plato_editar_titulo else R.string.plato_nuevo_titulo),
            onVolver = onVolver,
        ) {
            if (uiState.plato != null) {
                IconButton(onClick = onEliminar, enabled = !uiState.ocupado) {
                    Icon(
                        painter = painterResource(R.drawable.ic_eliminar),
                        contentDescription = stringResource(R.string.plato_eliminar),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        when {
            uiState.cargando -> Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            errorCarga != null -> Column(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MensajeErrorMenu(error = errorCarga)
                BotonPrincipal(texto = stringResource(R.string.arranque_reintentar), onClick = onReintentarCarga)
            }

            else -> FormularioPlato(
                uiState = uiState,
                onFotoElegida = onFotoElegida,
                onCambiarNombre = onCambiarNombre,
                onCambiarDescripcion = onCambiarDescripcion,
                onCambiarPrecio = onCambiarPrecio,
                onElegirCategoria = onElegirCategoria,
                onNuevaCategoria = onNuevaCategoria,
                onReintentarCategorias = onReintentarCategorias,
                onGuardar = onGuardar,
            )
        }
    }
}

@Composable
private fun ColumnScope.FormularioPlato(
    uiState: PlatoUiState,
    onFotoElegida: (String) -> Unit,
    onCambiarNombre: (String) -> Unit,
    onCambiarDescripcion: (String) -> Unit,
    onCambiarPrecio: (String) -> Unit,
    onElegirCategoria: (String) -> Unit,
    onNuevaCategoria: () -> Unit,
    onReintentarCategorias: () -> Unit,
    onGuardar: () -> Unit,
) {
    val invalidos = uiState.camposInvalidos
    Column(
        modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        FotoPlato(imagen = uiState.foto, habilitado = !uiState.ocupado, onFotoElegida = onFotoElegida)

        CampoFormulario(
            etiqueta = stringResource(R.string.plato_nombre),
            valor = uiState.nombre,
            onValorChange = onCambiarNombre,
            tipoTeclado = KeyboardType.Text,
            capitalizacion = KeyboardCapitalization.Sentences,
            ejemplo = stringResource(R.string.plato_nombre_ejemplo),
            esError = CampoPlato.NOMBRE in invalidos,
            mensaje = if (CampoPlato.NOMBRE in invalidos) stringResource(R.string.plato_error_nombre) else null,
            colorMensaje = MaterialTheme.colorScheme.error,
        )

        CampoFormulario(
            etiqueta = stringResource(R.string.plato_descripcion),
            valor = uiState.descripcion,
            onValorChange = onCambiarDescripcion,
            tipoTeclado = KeyboardType.Text,
            capitalizacion = KeyboardCapitalization.Sentences,
            ejemplo = stringResource(R.string.plato_descripcion_ejemplo),
            minLineas = 3,
            esError = CampoPlato.DESCRIPCION in invalidos,
            mensaje = if (CampoPlato.DESCRIPCION in invalidos) stringResource(R.string.plato_error_descripcion) else null,
            colorMensaje = MaterialTheme.colorScheme.error,
        )

        CampoPrecio(
            etiqueta = stringResource(R.string.plato_precio),
            valor = uiState.precio,
            onValorChange = onCambiarPrecio,
            esError = CampoPlato.PRECIO in invalidos,
            mensajeError = stringResource(R.string.plato_error_precio),
        )

        SelectorCategoriaPlato(
            categorias = uiState.categorias,
            error = uiState.errorCategorias,
            elegida = uiState.categoriaId,
            esError = CampoPlato.CATEGORIA in invalidos,
            habilitado = !uiState.ocupado,
            onElegir = onElegirCategoria,
            onNueva = onNuevaCategoria,
            onReintentar = onReintentarCategorias,
        )
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        uiState.error?.let { MensajeErrorMenu(error = it) }
        BotonPrincipal(
            texto = stringResource(R.string.plato_guardar),
            onClick = onGuardar,
            habilitado = !uiState.eliminando,
            cargando = uiState.guardando,
        )
    }
}

@Composable
private fun DialogoEliminarPlato(nombre: String, onEliminar: () -> Unit, onVolver: () -> Unit) {
    AlertDialog(
        onDismissRequest = onVolver,
        title = { Text(text = stringResource(R.string.eliminar_plato_titulo, nombre)) },
        text = { Text(text = stringResource(R.string.eliminar_plato_texto)) },
        confirmButton = {
            TextButton(onClick = onEliminar) {
                Text(text = stringResource(R.string.eliminar_plato_confirmar), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onVolver) {
                Text(text = stringResource(R.string.eliminar_plato_volver))
            }
        },
    )
}

@Composable
private fun FotoPlato(imagen: String?, habilitado: Boolean, onFotoElegida: (String) -> Unit) {
    val elegir = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { onFotoElegida(it.toString()) }
    }
    val radio = 16.dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(radio))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .then(if (imagen == null) Modifier.bordePunteado(MaterialTheme.colorScheme.outline, radio) else Modifier),
        contentAlignment = Alignment.BottomEnd,
    ) {
        imagen?.let {
            AsyncImage(
                model = it,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        }
        Surface(
            onClick = { elegir.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            enabled = habilitado,
            modifier = Modifier.padding(10.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shadowElevation = 1.dp,
        ) {
            Row(
                modifier = Modifier.height(40.dp).padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(painter = painterResource(R.drawable.ic_camara), contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    text = stringResource(if (imagen == null) R.string.plato_agregar_foto else R.string.plato_cambiar_foto),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun SelectorCategoriaPlato(
    categorias: List<Categoria>?,
    error: ErrorMenu?,
    elegida: String?,
    esError: Boolean,
    habilitado: Boolean,
    onElegir: (String) -> Unit,
    onNueva: () -> Unit,
    onReintentar: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(R.string.plato_categoria), style = MaterialTheme.typography.labelLarge)
        when {
            error != null -> Row(verticalAlignment = Alignment.CenterVertically) {
                MensajeErrorMenu(error = error, modifier = Modifier.weight(1f))
                TextButton(onClick = onReintentar) { Text(text = stringResource(R.string.plato_reintentar)) }
            }

            categorias == null -> CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)

            else -> FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                categorias.forEach { categoria ->
                    val seleccionada = categoria.id == elegida
                    FilterChip(
                        selected = seleccionada,
                        onClick = { onElegir(categoria.id) },
                        label = { Text(text = categoria.nombre) },
                        enabled = habilitado,
                        modifier = Modifier.height(36.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            selectedContainerColor = MaterialTheme.colorScheme.inverseSurface,
                            selectedLabelColor = MaterialTheme.colorScheme.inverseOnSurface,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = habilitado,
                            selected = seleccionada,
                            borderColor = if (esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                            selectedBorderColor = MaterialTheme.colorScheme.inverseSurface,
                        ),
                    )
                }
                ChipNuevaCategoria(habilitado = habilitado, onClick = onNueva)
            }
        }
        if (esError) {
            Text(
                text = stringResource(R.string.plato_error_categoria),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun ChipNuevaCategoria(habilitado: Boolean, onClick: () -> Unit) {
    val radio = 8.dp
    Box(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(radio))
            .bordePunteado(MaterialTheme.colorScheme.outline, radio)
            .clickable(enabled = habilitado, role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.plato_nueva_categoria),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun DialogoNuevaCategoria(
    estado: NuevaCategoriaUiState,
    onCambiarNombre: (String) -> Unit,
    onCrear: () -> Unit,
    onCerrar: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(text = stringResource(R.string.categoria_nueva_titulo)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CampoFormulario(
                    etiqueta = stringResource(R.string.categoria_nombre),
                    valor = estado.nombre,
                    onValorChange = onCambiarNombre,
                    tipoTeclado = KeyboardType.Text,
                    capitalizacion = KeyboardCapitalization.Sentences,
                    ejemplo = stringResource(R.string.categoria_nombre_ejemplo),
                    esError = estado.error != null,
                )
                estado.error?.let { MensajeErrorMenu(error = it) }
            }
        },
        confirmButton = {
            TextButton(onClick = onCrear, enabled = !estado.creando) {
                Text(text = stringResource(R.string.categoria_crear))
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar, enabled = !estado.creando) {
                Text(text = stringResource(R.string.categoria_cancelar))
            }
        },
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PlatoPreview() {
    ApprestauranteTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PlatoContenido(
                uiState = PlatoUiState(
                    nombre = "Lomo saltado",
                    descripcion = "Res al wok con cebolla, tomate, papas fritas y arroz",
                    precio = "24.00",
                    categoriaId = "p",
                    categorias = listOf(Categoria("p", "Platos", 0), Categoria("b", "Bebidas", 1)),
                ),
                onVolver = {},
                onEliminar = {},
                onReintentarCarga = {},
                onFotoElegida = {},
                onCambiarNombre = {},
                onCambiarDescripcion = {},
                onCambiarPrecio = {},
                onElegirCategoria = {},
                onNuevaCategoria = {},
                onReintentarCategorias = {},
                onGuardar = {},
            )
        }
    }
}
