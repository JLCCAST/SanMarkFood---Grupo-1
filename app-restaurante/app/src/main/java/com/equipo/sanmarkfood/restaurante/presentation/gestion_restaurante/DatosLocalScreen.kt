package com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.CampoLocal
import com.equipo.sanmarkfood.restaurante.domain.model.CategoriaRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.TipoFoto
import com.equipo.sanmarkfood.restaurante.domain.model.Ubicacion
import com.equipo.sanmarkfood.restaurante.presentation.auth.BotonPrincipal
import com.equipo.sanmarkfood.restaurante.presentation.auth.CabeceraPaso
import com.equipo.sanmarkfood.restaurante.presentation.auth.CampoFormulario
import com.equipo.sanmarkfood.restaurante.ui.theme.ApprestauranteTheme

@Composable
fun DatosLocalScreen(
    onGuardado: () -> Unit,
    onSalir: () -> Unit,
    onSesionCerrada: () -> Unit,
    viewModel: DatosLocalViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.guardado) {
        if (uiState.guardado) {
            viewModel.onGuardadoAtendido()
            onGuardado()
        }
    }
    LaunchedEffect(uiState.salir) {
        if (uiState.salir) onSalir()
    }
    LaunchedEffect(uiState.sesionCerrada) {
        if (uiState.sesionCerrada) onSesionCerrada()
    }
    BackHandler(enabled = !uiState.eligiendoUbicacion, onBack = viewModel::onVolver)

    if (uiState.confirmandoDescarte) {
        DialogoDescartarCambios(
            onSeguirEditando = viewModel::onSeguirEditando,
            onDescartar = viewModel::onDescartarCambios,
        )
    }

    // Fuera del formulario, para no perder el scroll al abrir y cerrar el selector de ubicación.
    val scroll = rememberScrollState()
    if (uiState.eligiendoUbicacion) {
        SelectorUbicacion(
            ubicacionInicial = uiState.ubicacion,
            onConfirmar = viewModel::onUbicacionElegida,
            onCancelar = viewModel::onCancelarUbicacion,
        )
    } else {
        DatosLocalContenido(
            uiState = uiState,
            scroll = scroll,
            onVolver = viewModel::onVolver,
            onReintentarCarga = viewModel::onReintentarCarga,
            onFotoElegida = viewModel::onFotoElegida,
            onCambiarNombre = viewModel::onCambiarNombre,
            onElegirCategoria = viewModel::onElegirCategoria,
            onCambiarDireccion = viewModel::onCambiarDireccion,
            onMoverPunto = viewModel::onMoverPunto,
            onCambiarTelefono = viewModel::onCambiarTelefono,
            onContinuar = viewModel::onContinuar,
        )
    }
}

@Composable
private fun DatosLocalContenido(
    uiState: DatosLocalUiState,
    scroll: ScrollState,
    onVolver: () -> Unit,
    onReintentarCarga: () -> Unit,
    onFotoElegida: (TipoFoto, String) -> Unit,
    onCambiarNombre: (String) -> Unit,
    onElegirCategoria: (CategoriaRestaurante) -> Unit,
    onCambiarDireccion: (String) -> Unit,
    onMoverPunto: () -> Unit,
    onCambiarTelefono: (String) -> Unit,
    onContinuar: () -> Unit,
) {
    val alta = uiState.modo == ModoFormulario.ALTA
    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        if (alta) {
            CabeceraPaso(texto = stringResource(R.string.datos_local_paso), pasoActual = 2, onVolver = onVolver)
        } else {
            CabeceraEdicion(titulo = stringResource(R.string.perfil_titulo), onVolver = onVolver)
        }

        val errorCarga = uiState.errorCarga
        when {
            uiState.cargando -> Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            errorCarga != null -> Column(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MensajeErrorRestaurante(error = errorCarga)
                BotonPrincipal(texto = stringResource(R.string.arranque_reintentar), onClick = onReintentarCarga)
            }

            else -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scroll)
                        .padding(PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp)),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    FormularioLocal(
                        uiState = uiState,
                        onFotoElegida = onFotoElegida,
                        onCambiarNombre = onCambiarNombre,
                        onElegirCategoria = onElegirCategoria,
                        onCambiarDireccion = onCambiarDireccion,
                        onMoverPunto = onMoverPunto,
                        onCambiarTelefono = onCambiarTelefono,
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Column(
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    uiState.error?.let { MensajeErrorRestaurante(error = it) }
                    BotonPrincipal(
                        texto = stringResource(if (alta) R.string.datos_local_continuar else R.string.editar_guardar),
                        onClick = onContinuar,
                        // Mientras sube una foto todavía no hay URL que guardar.
                        habilitado = !uiState.subiendoFoto,
                        cargando = uiState.guardando,
                    )
                }
            }
        }
    }
}

@Composable
private fun FormularioLocal(
    uiState: DatosLocalUiState,
    onFotoElegida: (TipoFoto, String) -> Unit,
    onCambiarNombre: (String) -> Unit,
    onElegirCategoria: (CategoriaRestaurante) -> Unit,
    onCambiarDireccion: (String) -> Unit,
    onMoverPunto: () -> Unit,
    onCambiarTelefono: (String) -> Unit,
) {
    val invalidos = uiState.camposInvalidos

    // Como en el prototipo, el título y la explicación solo van en el alta; en O7 basta la cabecera.
    if (uiState.modo == ModoFormulario.ALTA) {
        Text(
            text = stringResource(R.string.datos_local_titulo),
            style = MaterialTheme.typography.displaySmall,
        )
        Text(
            text = stringResource(R.string.datos_local_subtitulo),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    FotosLocal(
        portada = uiState.portada,
        logo = uiState.logo,
        portadaFaltante = CampoLocal.PORTADA in invalidos,
        habilitado = !uiState.guardando,
        onFotoElegida = onFotoElegida,
    )

    CampoFormulario(
        etiqueta = stringResource(R.string.datos_local_nombre),
        valor = uiState.nombre,
        onValorChange = onCambiarNombre,
        tipoTeclado = KeyboardType.Text,
        capitalizacion = KeyboardCapitalization.Words,
        esError = CampoLocal.NOMBRE in invalidos,
        mensaje = if (CampoLocal.NOMBRE in invalidos) stringResource(R.string.datos_local_error_nombre) else null,
        colorMensaje = MaterialTheme.colorScheme.error,
    )

    SelectorCategoria(
        elegida = uiState.categoria,
        onElegir = onElegirCategoria,
        esError = CampoLocal.CATEGORIA in invalidos,
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        CampoFormulario(
            etiqueta = stringResource(R.string.datos_local_direccion),
            valor = uiState.direccion,
            onValorChange = onCambiarDireccion,
            tipoTeclado = KeyboardType.Text,
            capitalizacion = KeyboardCapitalization.Sentences,
            ejemplo = stringResource(R.string.datos_local_direccion_ejemplo),
            esError = CampoLocal.DIRECCION in invalidos,
            mensaje = if (CampoLocal.DIRECCION in invalidos) stringResource(R.string.datos_local_error_direccion) else null,
            colorMensaje = MaterialTheme.colorScheme.error,
        )
        VistaPreviaMapa(ubicacion = uiState.ubicacion, onMoverPunto = onMoverPunto)
        Text(
            text = stringResource(R.string.datos_local_mapa_ayuda),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    val telefonoInvalido = CampoLocal.TELEFONO in invalidos
    CampoFormulario(
        etiqueta = stringResource(R.string.datos_local_telefono),
        valor = uiState.telefono,
        onValorChange = onCambiarTelefono,
        tipoTeclado = KeyboardType.Phone,
        ejemplo = stringResource(R.string.datos_local_telefono_ejemplo),
        esError = telefonoInvalido,
        mensaje = stringResource(
            if (telefonoInvalido) R.string.datos_local_error_telefono else R.string.datos_local_telefono_ayuda
        ),
        colorMensaje = if (telefonoInvalido) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun SelectorCategoria(
    elegida: CategoriaRestaurante?,
    onElegir: (CategoriaRestaurante) -> Unit,
    esError: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(R.string.datos_local_categoria), style = MaterialTheme.typography.labelLarge)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CategoriaRestaurante.entries.forEach { categoria ->
                val seleccionada = categoria == elegida
                FilterChip(
                    selected = seleccionada,
                    onClick = { onElegir(categoria) },
                    label = { Text(text = stringResource(categoria.etiqueta())) },
                    modifier = Modifier.height(36.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        selectedContainerColor = MaterialTheme.colorScheme.inverseSurface,
                        selectedLabelColor = MaterialTheme.colorScheme.inverseOnSurface,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = seleccionada,
                        borderColor = if (esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                        selectedBorderColor = MaterialTheme.colorScheme.inverseSurface,
                    ),
                )
            }
        }
        if (esError) {
            Text(
                text = stringResource(R.string.datos_local_error_categoria),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun DatosLocalPreview() {
    ApprestauranteTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            DatosLocalContenido(
                uiState = DatosLocalUiState(
                    cargando = false,
                    nombre = "La Sazón de Doña Carmen",
                    categoria = CategoriaRestaurante.CRIOLLA,
                    direccion = "Av. Venezuela 3450, frente a la puerta 3",
                    ubicacion = Ubicacion.CiudadUniversitaria,
                    telefono = "98765",
                    camposInvalidos = setOf(CampoLocal.TELEFONO),
                    error = ErrorRestaurante.DatosInvalidos(setOf(CampoLocal.TELEFONO)),
                ),
                scroll = rememberScrollState(),
                onVolver = {},
                onReintentarCarga = {},
                onFotoElegida = { _, _ -> },
                onCambiarNombre = {},
                onElegirCategoria = {},
                onCambiarDireccion = {},
                onMoverPunto = {},
                onCambiarTelefono = {},
                onContinuar = {},
            )
        }
    }
}
