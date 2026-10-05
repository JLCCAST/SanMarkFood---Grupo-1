package com.equipo.sanmarkfood.restaurante.presentation.admin.aprobacion

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.admin.CampoCorregido
import com.equipo.sanmarkfood.restaurante.domain.model.admin.DetalleSolicitud
import com.equipo.sanmarkfood.restaurante.domain.model.admin.ErrorAdmin
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.CategoriaRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.DatosLocal
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.DiaSemana
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.EstadoRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Horario
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.HorarioDia
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.MotivoRechazo
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Rechazo
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Restaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.Ubicacion
import com.equipo.sanmarkfood.restaurante.presentation.admin.EtiquetaSolicitud
import com.equipo.sanmarkfood.restaurante.presentation.admin.MensajeErrorAdmin
import com.equipo.sanmarkfood.restaurante.presentation.auth.BotonPrincipal
import com.equipo.sanmarkfood.restaurante.presentation.auth.CampoFormulario
import com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante.LogoLocal
import com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante.VistaPreviaMapa
import com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante.etiqueta
import com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante.nombreCorto
import com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante.texto
import com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante.titulo
import com.equipo.sanmarkfood.restaurante.ui.theme.ApprestauranteTheme
import com.equipo.sanmarkfood.restaurante.ui.theme.extendedColors
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private val localePeru = Locale.forLanguageTag("es-PE")

@Composable
fun RevisarSolicitudScreen(
    onVolver: () -> Unit,
    viewModel: RevisarSolicitudViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    uiState.hojaRechazo?.let { hoja ->
        HojaRechazo(
            hoja = hoja,
            onElegirMotivo = viewModel::onElegirMotivo,
            onCambiarDetalle = viewModel::onCambiarDetalle,
            onRechazar = viewModel::onRechazar,
            onCerrar = viewModel::onCerrarRechazo,
        )
    }

    RevisarSolicitudContenido(
        uiState = uiState,
        onVolver = onVolver,
        onReintentar = viewModel::onReintentar,
        onAprobar = viewModel::onAprobar,
        onAbrirRechazo = viewModel::onAbrirRechazo,
    )
}

@Composable
private fun RevisarSolicitudContenido(
    uiState: RevisarSolicitudUiState,
    onVolver: () -> Unit,
    onReintentar: () -> Unit,
    onAprobar: () -> Unit,
    onAbrirRechazo: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        BarraRevision(onVolver = onVolver)

        val solicitud = uiState.solicitud
        val errorCarga = uiState.errorCarga
        when {
            uiState.cargando -> Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            errorCarga != null || solicitud == null -> Column(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MensajeErrorAdmin(error = errorCarga ?: ErrorAdmin.Desconocido)
                BotonPrincipal(texto = stringResource(R.string.arranque_reintentar), onClick = onReintentar)
            }

            else -> {
                val pendiente = solicitud.restaurante.estado == EstadoRestaurante.PENDIENTE
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Portada(datos = solicitud.restaurante.datos)
                    Encabezado(solicitud = solicitud)
                    AvisoDecision(restaurante = solicitud.restaurante)
                    val rechazoAnterior = solicitud.rechazoAnterior
                    if (pendiente && rechazoAnterior != null) {
                        AvisoRechazoAnterior(
                            rechazo = rechazoAnterior,
                            rechazadoEl = solicitud.revisadoEn,
                            camposCorregidos = solicitud.camposCorregidos,
                        )
                    }
                    DatosSolicitud(solicitud = solicitud)
                    VistaPreviaMapa(
                        ubicacion = solicitud.restaurante.datos.ubicacion,
                        onMoverPunto = null,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                if (pendiente) {
                    BotonesDecision(
                        aprobando = uiState.aprobando,
                        error = uiState.errorDecision,
                        onAprobar = onAprobar,
                        onRechazar = onAbrirRechazo,
                    )
                }
            }
        }
    }
}

@Composable
private fun BarraRevision(onVolver: () -> Unit) {
    val colores = MaterialTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(colores.pizarra)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        IconButton(onClick = onVolver) {
            Icon(
                painter = painterResource(R.drawable.ic_volver),
                contentDescription = stringResource(R.string.volver),
                tint = colores.sobreSuperficieFija,
            )
        }
        Text(
            text = stringResource(R.string.revisar_titulo),
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.titleMedium,
            color = colores.sobreSuperficieFija,
        )
    }
}

@Composable
private fun Portada(datos: DatosLocal) {
    val formaLogo = RoundedCornerShape(16.dp)
    Box(modifier = Modifier.fillMaxWidth().height(164.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        ) {
            datos.portadaUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        LogoLocal(
            logoUrl = datos.logoUrl,
            nombre = datos.nombre,
            tamano = 64.dp,
            radio = 16.dp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp)
                .border(3.dp, MaterialTheme.colorScheme.background, formaLogo),
        )
    }
}

@Composable
private fun Encabezado(solicitud: DetalleSolicitud) {
    val datos = solicitud.restaurante.datos
    val categoria = stringResource(datos.categoria.etiqueta())
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        EtiquetaSolicitud(reenviado = solicitud.reenviado)
        Text(
            text = datos.nombre,
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = solicitud.correo?.let { stringResource(R.string.revisar_categoria_cuenta, categoria, it) } ?: categoria,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AvisoDecision(restaurante: Restaurante) {
    when (restaurante.estado) {
        EstadoRestaurante.APROBADO -> Aviso(
            fondo = MaterialTheme.colorScheme.tertiaryContainer,
            contenido = MaterialTheme.colorScheme.onTertiaryContainer,
            titulo = stringResource(R.string.revisar_aprobado),
            detalle = stringResource(R.string.revisar_aprobado_detalle),
        )
        EstadoRestaurante.RECHAZADO -> Aviso(
            fondo = MaterialTheme.colorScheme.errorContainer,
            contenido = MaterialTheme.colorScheme.onErrorContainer,
            titulo = stringResource(
                R.string.revisar_rechazado,
                restaurante.rechazo?.let { textoMotivo(it).replaceFirstChar { c -> c.lowercase(localePeru) } }.orEmpty(),
            ),
            detalle = stringResource(R.string.revisar_rechazado_detalle),
        )
        else -> Unit
    }
}

@Composable
private fun Aviso(fondo: Color, contenido: Color, titulo: String, detalle: String) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(titulo) }
            append(" ")
            append(detalle)
        },
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(fondo)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        style = MaterialTheme.typography.bodyMedium,
        color = contenido,
    )
}

@Composable
private fun AvisoRechazoAnterior(rechazo: Rechazo, rechazadoEl: Long?, camposCorregidos: Set<CampoCorregido>) {
    val encabezado = rechazadoEl?.let { stringResource(R.string.revisar_rechazo_anterior_fecha, fechaCorta(it)) }
        ?: stringResource(R.string.revisar_rechazo_anterior)
    val motivo = textoMotivo(rechazo).trimEnd('.')
    val cambio = stringResource(R.string.revisar_cambio)
    val corregidos = listaDeCampos(camposCorregidos)
    val sinCambios = stringResource(R.string.revisar_sin_cambios)
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = encabezado.uppercase(localePeru),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        Text(
            text = buildAnnotatedString {
                append("$motivo. ")
                if (corregidos == null) {
                    append(sinCambios)
                } else {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(cambio) }
                    append(" $corregidos.")
                }
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun DatosSolicitud(solicitud: DetalleSolicitud) {
    val datos = solicitud.restaurante.datos
    val platos = solicitud.cantidadPlatos
    val categorias = solicitud.cantidadCategorias
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        FilaDato(etiqueta = stringResource(R.string.revisar_direccion), valor = datos.direccion)
        FilaDato(etiqueta = stringResource(R.string.revisar_telefono), valor = datos.telefono)
        FilaDato(etiqueta = stringResource(R.string.revisar_horario), valor = resumenHorario(solicitud.restaurante.horario))
        FilaDato(
            etiqueta = stringResource(R.string.revisar_carta),
            valor = if (platos == 0) {
                stringResource(R.string.revisar_sin_carta)
            } else {
                stringResource(
                    R.string.revisar_carta_resumen,
                    pluralStringResource(R.plurals.carta_platos, platos, platos),
                    pluralStringResource(R.plurals.carta_categorias, categorias, categorias),
                )
            },
        )
    }
}

@Composable
private fun FilaDato(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = etiqueta,
            modifier = Modifier.width(110.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = valor, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun BotonesDecision(
    aprobando: Boolean,
    error: ErrorAdmin?,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit,
) {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        error?.let { MensajeErrorAdmin(error = it) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onRechazar,
                enabled = !aprobando,
                modifier = Modifier.weight(1f).height(52.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Text(text = stringResource(R.string.revisar_rechazar), style = MaterialTheme.typography.labelLarge)
            }
            Button(
                onClick = onAprobar,
                enabled = !aprobando,
                modifier = Modifier.weight(1f).height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                ),
            ) {
                if (aprobando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = LocalContentColor.current,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(text = stringResource(R.string.revisar_aprobar), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaRechazo(
    hoja: HojaRechazoUiState,
    onElegirMotivo: (MotivoRechazo) -> Unit,
    onCambiarDetalle: (String) -> Unit,
    onRechazar: () -> Unit,
    onCerrar: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outline) },
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(text = stringResource(R.string.revisar_rechazo_titulo), style = MaterialTheme.typography.headlineSmall)
            Text(
                text = stringResource(R.string.revisar_rechazo_explicacion),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.revisar_rechazo_motivo),
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.labelLarge,
            )
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                MotivoRechazo.entries.forEach { motivo ->
                    OpcionMotivo(
                        motivo = motivo,
                        seleccionado = hoja.motivo == motivo,
                        habilitado = !hoja.enviando,
                        onElegir = { onElegirMotivo(motivo) },
                    )
                }
            }
            CampoFormulario(
                etiqueta = stringResource(
                    if (hoja.motivo == MotivoRechazo.OTRO) R.string.revisar_rechazo_detalle_obligatorio
                    else R.string.revisar_rechazo_detalle_opcional
                ),
                valor = hoja.detalle,
                onValorChange = onCambiarDetalle,
                tipoTeclado = KeyboardType.Text,
                capitalizacion = KeyboardCapitalization.Sentences,
                ejemplo = stringResource(R.string.revisar_rechazo_detalle_ejemplo),
            )
            hoja.error?.let { MensajeErrorAdmin(error = it) }
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(onClick = onCerrar, modifier = Modifier.height(48.dp)) {
                    Text(text = stringResource(R.string.revisar_rechazo_volver), style = MaterialTheme.typography.labelLarge)
                }
                Button(
                    onClick = onRechazar,
                    enabled = hoja.completa && !hoja.enviando,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    if (hoja.enviando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = LocalContentColor.current,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(text = stringResource(R.string.revisar_rechazo_confirmar), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun OpcionMotivo(motivo: MotivoRechazo, seleccionado: Boolean, habilitado: Boolean, onElegir: () -> Unit) {
    val forma = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(forma)
            .border(
                1.5.dp,
                if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                forma,
            )
            .selectable(selected = seleccionado, enabled = habilitado, role = Role.RadioButton, onClick = onElegir)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(selected = seleccionado, onClick = null, enabled = habilitado)
        Text(text = stringResource(motivo.etiquetaParaAdministrador()), style = MaterialTheme.typography.labelLarge)
    }
}

@StringRes
private fun MotivoRechazo.etiquetaParaAdministrador(): Int =
    if (this == MotivoRechazo.OTRO) R.string.revisar_rechazo_otro else titulo()

@Composable
private fun textoMotivo(rechazo: Rechazo): String {
    val detalle = rechazo.detalle
    if (rechazo.motivo == MotivoRechazo.OTRO && detalle != null) return detalle
    return stringResource(rechazo.motivo.titulo())
}

@Composable
private fun listaDeCampos(campos: Set<CampoCorregido>): String? {
    val nombres = CampoCorregido.entries.filter { it in campos }.map { stringResource(it.nombre()) }
    return when (nombres.size) {
        0 -> null
        1 -> nombres.single()
        else -> stringResource(R.string.revisar_lista_y, nombres.dropLast(1).joinToString(", "), nombres.last())
    }
}

@StringRes
private fun CampoCorregido.nombre(): Int = when (this) {
    CampoCorregido.NOMBRE -> R.string.revisar_campo_nombre
    CampoCorregido.CATEGORIA -> R.string.revisar_campo_categoria
    CampoCorregido.DIRECCION -> R.string.revisar_campo_direccion
    CampoCorregido.UBICACION -> R.string.revisar_campo_ubicacion
    CampoCorregido.TELEFONO -> R.string.revisar_campo_telefono
    CampoCorregido.PORTADA -> R.string.revisar_campo_portada
    CampoCorregido.LOGO -> R.string.revisar_campo_logo
}

@Composable
private fun resumenHorario(horario: Horario?): String {
    if (horario == null) return stringResource(R.string.revisar_sin_horario)
    val tramos = mutableListOf<Triple<DiaSemana, DiaSemana, HorarioDia>>()
    DiaSemana.entries.forEach { dia ->
        val horas = horario.dias.getValue(dia)
        val ultimo = tramos.lastOrNull()
        when {
            !horas.abierto -> Unit
            ultimo != null && ultimo.second.ordinal == dia.ordinal - 1 &&
                ultimo.third.abre == horas.abre && ultimo.third.cierra == horas.cierra ->
                tramos[tramos.lastIndex] = Triple(ultimo.first, dia, ultimo.third)
            else -> tramos += Triple(dia, dia, horas)
        }
    }
    if (tramos.isEmpty()) return stringResource(R.string.revisar_sin_dias)
    val partes = tramos.map { (desde, hasta, horas) ->
        val primero = stringResource(desde.nombreCorto())
        val ultimo = stringResource(hasta.nombreCorto())
        val dias = when (hasta.ordinal - desde.ordinal) {
            0 -> primero
            1 -> stringResource(R.string.dias_par, primero, ultimo)
            else -> stringResource(R.string.dias_tramo, primero, ultimo)
        }
        stringResource(R.string.revisar_horario_tramo, dias, horas.abre.texto(), horas.cierra.texto())
    }
    return partes.joinToString(" · ").replaceFirstChar { it.titlecase(localePeru) }
}

@Composable
private fun fechaCorta(milisegundos: Long): String {
    val meses = stringArrayResource(R.array.meses_cortos)
    val fecha = Calendar.getInstance(TimeZone.getTimeZone("America/Lima")).apply { timeInMillis = milisegundos }
    return "${fecha.get(Calendar.DAY_OF_MONTH)} ${meses[fecha.get(Calendar.MONTH)]}"
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun RevisarSolicitudPreview() {
    ApprestauranteTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            RevisarSolicitudContenido(
                uiState = RevisarSolicitudUiState(
                    cargando = false,
                    solicitud = DetalleSolicitud(
                        uid = "1",
                        restaurante = Restaurante(
                            datos = DatosLocal(
                                nombre = "La Sazón de Doña Carmen",
                                categoria = CategoriaRestaurante.CRIOLLA,
                                direccion = "Av. Venezuela 3450, frente a la puerta 3",
                                ubicacion = Ubicacion.CiudadUniversitaria,
                                telefono = "987654321",
                                portadaUrl = null,
                                logoUrl = null,
                            ),
                            estado = EstadoRestaurante.PENDIENTE,
                            horario = Horario.PorDefecto,
                            rechazo = null,
                            pausado = false,
                        ),
                        correo = "carmen@correo.com",
                        reenviado = true,
                        rechazoAnterior = Rechazo(MotivoRechazo.DIRECCION_NO_VERIFICABLE, detalle = null),
                        camposCorregidos = setOf(CampoCorregido.DIRECCION, CampoCorregido.UBICACION),
                        revisadoEn = System.currentTimeMillis(),
                        cantidadPlatos = 5,
                        cantidadCategorias = 2,
                    ),
                ),
                onVolver = {},
                onReintentar = {},
                onAprobar = {},
                onAbrirRechazo = {},
            )
        }
    }
}
