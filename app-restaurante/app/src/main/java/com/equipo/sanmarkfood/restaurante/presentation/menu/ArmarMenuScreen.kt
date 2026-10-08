package com.equipo.sanmarkfood.restaurante.presentation.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.menu.CampoMenu
import com.equipo.sanmarkfood.restaurante.domain.model.menu.OrigenMenu
import com.equipo.sanmarkfood.restaurante.presentation.auth.BotonPrincipal
import com.equipo.sanmarkfood.restaurante.presentation.auth.CampoFormulario
import com.equipo.sanmarkfood.restaurante.ui.theme.ApprestauranteTheme

@Composable
fun ArmarMenuScreen(
    onCerrar: () -> Unit,
    onPublicado: () -> Unit,
    onTomarOtraFoto: () -> Unit,
    viewModel: ArmarMenuViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.publicado) {
        if (uiState.publicado) onPublicado()
    }

    ArmarMenuContenido(
        uiState = uiState,
        onCerrar = onCerrar,
        onTomarOtraFoto = onTomarOtraFoto,
        onReintentarCarga = viewModel::onReintentarCarga,
        onCambiarPrecio = viewModel::onCambiarPrecio,
        onCambiarNuevaEntrada = viewModel::onCambiarNuevaEntrada,
        onAgregarEntrada = viewModel::onAgregarEntrada,
        onQuitarEntrada = viewModel::onQuitarEntrada,
        onCambiarNuevoSegundo = viewModel::onCambiarNuevoSegundo,
        onAgregarSegundo = viewModel::onAgregarSegundo,
        onQuitarSegundo = viewModel::onQuitarSegundo,
        onCambiarRefresco = viewModel::onCambiarRefresco,
        onCambiarPostre = viewModel::onCambiarPostre,
        onElegirHoraFin = viewModel::onElegirHoraFin,
        onPublicar = viewModel::onPublicar,
    )
}

@Composable
private fun ArmarMenuContenido(
    uiState: ArmarMenuUiState,
    onCerrar: () -> Unit,
    onTomarOtraFoto: () -> Unit,
    onReintentarCarga: () -> Unit,
    onCambiarPrecio: (String) -> Unit,
    onCambiarNuevaEntrada: (String) -> Unit,
    onAgregarEntrada: () -> Unit,
    onQuitarEntrada: (Int) -> Unit,
    onCambiarNuevoSegundo: (String) -> Unit,
    onAgregarSegundo: () -> Unit,
    onQuitarSegundo: (Int) -> Unit,
    onCambiarRefresco: (String) -> Unit,
    onCambiarPostre: (String) -> Unit,
    onElegirHoraFin: (String) -> Unit,
    onPublicar: () -> Unit,
) {
    val invalidos = uiState.camposInvalidos
    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        CabeceraArmarMenu(fecha = uiState.fecha, onCerrar = onCerrar)

        val errorCarga = uiState.errorCarga
        if (uiState.cargando || errorCarga != null) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (errorCarga != null) {
                    MensajeErrorMenu(error = errorCarga)
                    BotonPrincipal(texto = stringResource(R.string.arranque_reintentar), onClick = onReintentarCarga)
                } else {
                    CircularProgressIndicator()
                }
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            if (uiState.origen == OrigenMenu.IA) {
                AvisoPizarraLeida(habilitado = !uiState.publicando, onTomarOtraFoto = onTomarOtraFoto)
            }
            if (uiState.origen == OrigenMenu.AYER) {
                Text(
                    text = stringResource(R.string.armar_copiado),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            CampoPrecio(
                etiqueta = stringResource(R.string.armar_precio),
                valor = uiState.precio,
                onValorChange = onCambiarPrecio,
                esError = CampoMenu.PRECIO in invalidos,
                mensajeError = stringResource(R.string.armar_error_precio),
            )

            SeccionOpciones(
                titulo = stringResource(R.string.armar_entradas),
                textoVacio = stringResource(R.string.armar_sin_entradas),
                textoAgregar = stringResource(R.string.armar_agregar_entrada),
                opciones = uiState.entradas,
                nueva = uiState.nuevaEntrada,
                esError = CampoMenu.ENTRADAS in invalidos,
                onCambiarNueva = onCambiarNuevaEntrada,
                onAgregar = onAgregarEntrada,
                onQuitar = onQuitarEntrada,
            )

            SeccionOpciones(
                titulo = stringResource(R.string.armar_segundos),
                textoVacio = stringResource(R.string.armar_sin_segundos),
                textoAgregar = stringResource(R.string.armar_agregar_segundo),
                opciones = uiState.segundos,
                nueva = uiState.nuevoSegundo,
                esError = CampoMenu.SEGUNDOS in invalidos,
                onCambiarNueva = onCambiarNuevoSegundo,
                onAgregar = onAgregarSegundo,
                onQuitar = onQuitarSegundo,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CampoFormulario(
                    etiqueta = stringResource(R.string.armar_refresco),
                    valor = uiState.refresco,
                    onValorChange = onCambiarRefresco,
                    modifier = Modifier.weight(1f),
                    tipoTeclado = KeyboardType.Text,
                    capitalizacion = KeyboardCapitalization.Sentences,
                    ejemplo = stringResource(R.string.armar_refresco_ejemplo),
                )
                CampoFormulario(
                    etiqueta = stringResource(R.string.armar_postre),
                    valor = uiState.postre,
                    onValorChange = onCambiarPostre,
                    modifier = Modifier.weight(1f),
                    tipoTeclado = KeyboardType.Text,
                    capitalizacion = KeyboardCapitalization.Sentences,
                    ejemplo = stringResource(R.string.armar_postre_ejemplo),
                )
            }

            SelectorHoraFin(elegida = uiState.horaFin, onElegir = onElegirHoraFin)
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            uiState.error?.let { MensajeErrorMenu(error = it) }
            BotonPrincipal(
                texto = stringResource(if (uiState.editando) R.string.editar_guardar else R.string.armar_publicar),
                onClick = onPublicar,
                habilitado = uiState.completo,
                cargando = uiState.publicando,
            )
            Text(
                text = when {
                    !uiState.completo -> stringResource(R.string.armar_falta)
                    uiState.editando -> stringResource(R.string.armar_conservan)
                    else -> stringResource(R.string.armar_vence, uiState.horaFin)
                },
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AvisoPizarraLeida(habilitado: Boolean, onTomarOtraFoto: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(start = 14.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.armar_leido_titulo))
                }
                append(" ")
                append(stringResource(R.string.armar_leido_texto))
            },
            modifier = Modifier.weight(1f).padding(vertical = 6.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        TextButton(
            onClick = onTomarOtraFoto,
            enabled = habilitado,
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
        ) {
            Text(text = stringResource(R.string.armar_tomar_otra), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CabeceraArmarMenu(fecha: String, onCerrar: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        IconButton(onClick = onCerrar) {
            Icon(painter = painterResource(R.drawable.ic_cerrar), contentDescription = stringResource(R.string.armar_cerrar))
        }
        Column {
            Text(text = stringResource(R.string.menu_hoy), style = MaterialTheme.typography.titleMedium)
            Text(
                text = textoFecha(fecha),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SeccionOpciones(
    titulo: String,
    textoVacio: String,
    textoAgregar: String,
    opciones: List<String>,
    nueva: String,
    esError: Boolean,
    onCambiarNueva: (String) -> Unit,
    onAgregar: () -> Unit,
    onQuitar: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = titulo,
            modifier = Modifier.padding(bottom = 2.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        if (opciones.isEmpty()) {
            Text(
                text = textoVacio,
                style = MaterialTheme.typography.bodyMedium,
                color = if (esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        opciones.forEachIndexed { indice, nombre ->
            FilaOpcion(nombre = nombre, onQuitar = { onQuitar(indice) })
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = nueva,
                onValueChange = onCambiarNueva,
                modifier = Modifier.weight(1f),
                placeholder = { Text(text = textoAgregar) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { onAgregar() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
            )
            FilledIconButton(
                onClick = onAgregar,
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                ),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_agregar),
                    contentDescription = textoAgregar,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun FilaOpcion(nombre: String, onQuitar: () -> Unit) {
    val forma = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(forma)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, forma)
            .padding(start = 12.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = nombre, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
        IconButton(onClick = onQuitar) {
            Icon(
                painter = painterResource(R.drawable.ic_cerrar),
                contentDescription = stringResource(R.string.armar_quitar, nombre),
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ArmarMenuPreview() {
    ApprestauranteTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ArmarMenuContenido(
                uiState = ArmarMenuUiState(
                    fecha = "2026-10-05",
                    precio = "14.00",
                    entradas = listOf("Papa a la huancaína", "Causa limeña"),
                    segundos = listOf("Ají de gallina"),
                    refresco = "Chicha morada",
                ),
                onCerrar = {},
                onTomarOtraFoto = {},
                onReintentarCarga = {},
                onCambiarPrecio = {},
                onCambiarNuevaEntrada = {},
                onAgregarEntrada = {},
                onQuitarEntrada = {},
                onCambiarNuevoSegundo = {},
                onAgregarSegundo = {},
                onQuitarSegundo = {},
                onCambiarRefresco = {},
                onCambiarPostre = {},
                onElegirHoraFin = {},
                onPublicar = {},
            )
        }
    }
}
