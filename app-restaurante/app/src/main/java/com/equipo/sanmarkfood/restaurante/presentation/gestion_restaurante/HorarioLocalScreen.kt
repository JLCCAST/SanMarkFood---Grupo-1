package com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.DiaSemana
import com.equipo.sanmarkfood.restaurante.domain.model.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.Hora
import com.equipo.sanmarkfood.restaurante.domain.model.HorarioDia
import com.equipo.sanmarkfood.restaurante.presentation.auth.BotonPrincipal
import com.equipo.sanmarkfood.restaurante.presentation.auth.CabeceraPaso
import com.equipo.sanmarkfood.restaurante.ui.theme.ApprestauranteTheme

@Composable
fun HorarioLocalScreen(
    onVolver: () -> Unit,
    onEnviado: () -> Unit,
    viewModel: HorarioLocalViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.enviado) {
        if (uiState.enviado) {
            viewModel.onEnvioAtendido()
            onEnviado()
        }
    }

    HorarioLocalContenido(
        uiState = uiState,
        onVolver = onVolver,
        onAlternarDia = viewModel::onAlternarDia,
        onEditarHoras = viewModel::onEditarHoras,
        onCopiarLunesATodos = viewModel::onCopiarLunesATodos,
        onEnviarARevision = viewModel::onEnviarARevision,
    )

    uiState.diaEditando?.let { dia ->
        val horarioDia = uiState.dias.getValue(dia)
        DialogoHoras(
            dia = dia,
            abreInicial = horarioDia.abre,
            cierraInicial = horarioDia.cierra,
            onListo = viewModel::onHorasElegidas,
            onCancelar = viewModel::onCancelarEdicion,
        )
    }
}

@Composable
private fun HorarioLocalContenido(
    uiState: HorarioLocalUiState,
    onVolver: () -> Unit,
    onAlternarDia: (DiaSemana) -> Unit,
    onEditarHoras: (DiaSemana) -> Unit,
    onCopiarLunesATodos: () -> Unit,
    onEnviarARevision: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        CabeceraPaso(texto = stringResource(R.string.horario_paso), pasoActual = 3, onVolver = onVolver)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp)),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = stringResource(R.string.horario_titulo),
                style = MaterialTheme.typography.displaySmall,
            )

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = stringResource(R.string.horario_atencion), style = MaterialTheme.typography.titleSmall)
                    TextButton(onClick = onCopiarLunesATodos) {
                        Text(text = stringResource(R.string.horario_copiar_lunes), style = MaterialTheme.typography.labelLarge)
                    }
                }
                DiaSemana.entries.forEach { dia ->
                    FilaDia(
                        dia = dia,
                        horarioDia = uiState.dias.getValue(dia),
                        invalido = dia in uiState.diasInvalidos,
                        onAlternar = { onAlternarDia(dia) },
                        onEditar = { onEditarHoras(dia) },
                    )
                }
                Text(
                    text = stringResource(R.string.horario_ayuda),
                    modifier = Modifier.padding(top = 6.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            uiState.error?.let { MensajeErrorRestaurante(error = it) }
            BotonPrincipal(
                texto = stringResource(R.string.horario_enviar),
                onClick = onEnviarARevision,
                cargando = uiState.enviando,
            )
            Text(
                text = stringResource(R.string.horario_mientras_revisamos),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun FilaDia(
    dia: DiaSemana,
    horarioDia: HorarioDia,
    invalido: Boolean,
    onAlternar: () -> Unit,
    onEditar: () -> Unit,
) {
    val nombre = stringResource(dia.nombre())
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(text = nombre, modifier = Modifier.width(84.dp), style = MaterialTheme.typography.labelLarge)
        Text(
            text = if (horarioDia.abierto) {
                stringResource(R.string.horario_rango, horarioDia.abre.texto(), horarioDia.cierra.texto())
            } else {
                stringResource(R.string.horario_cerrado)
            },
            modifier = Modifier
                .weight(1f)
                .clickable(
                    enabled = horarioDia.abierto,
                    onClickLabel = stringResource(R.string.horario_cambiar),
                    role = Role.Button,
                    onClick = onEditar,
                )
                .padding(vertical = 14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = when {
                invalido -> MaterialTheme.colorScheme.error
                horarioDia.abierto -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        Switch(
            checked = horarioDia.abierto,
            onCheckedChange = { onAlternar() },
            modifier = Modifier.semantics { contentDescription = nombre },
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

/** Reloj de 24 horas en dos pasos: primero la hora de apertura, luego la de cierre. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoHoras(
    dia: DiaSemana,
    abreInicial: Hora,
    cierraInicial: Hora,
    onListo: (abre: Hora, cierra: Hora) -> Unit,
    onCancelar: () -> Unit,
) {
    var eligiendoCierre by rememberSaveable { mutableStateOf(false) }
    val abre = rememberTimePickerState(abreInicial.hora, abreInicial.minuto, is24Hour = true)
    val cierra = rememberTimePickerState(cierraInicial.hora, cierraInicial.minuto, is24Hour = true)
    val horaAbre = Hora(abre.hour, abre.minute)
    val horaCierra = Hora(cierra.hour, cierra.minute)
    val cierreValido = horaCierra > horaAbre
    val nombreDia = stringResource(dia.nombre())

    TimePickerDialog(
        onDismissRequest = onCancelar,
        title = {
            Text(
                stringResource(
                    if (eligiendoCierre) R.string.horario_dialogo_cierra else R.string.horario_dialogo_abre,
                    nombreDia,
                )
            )
        },
        confirmButton = {
            if (eligiendoCierre) {
                TextButton(onClick = { onListo(horaAbre, horaCierra) }, enabled = cierreValido) {
                    Text(stringResource(R.string.horario_listo))
                }
            } else {
                TextButton(onClick = { eligiendoCierre = true }) {
                    Text(stringResource(R.string.horario_siguiente))
                }
            }
        },
        dismissButton = {
            if (eligiendoCierre) {
                TextButton(onClick = { eligiendoCierre = false }) { Text(stringResource(R.string.horario_atras)) }
            } else {
                TextButton(onClick = onCancelar) { Text(stringResource(R.string.horario_cancelar)) }
            }
        },
    ) {
        // El aviso ocupa su lugar desde el primer paso, invisible mientras la hora sea válida:
        // así el diálogo no cambia de tamaño y los botones no se mueven cuando aparece.
        val avisoVisible = eligiendoCierre && !cierreValido
        RelojConAviso(
            reloj = {
                key(eligiendoCierre) {
                    TimePicker(state = if (eligiendoCierre) cierra else abre)
                }
            },
            aviso = {
                Text(
                    text = stringResource(R.string.horario_cierre_antes),
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .alpha(if (avisoVisible) 1f else 0f)
                        .then(
                            if (avisoVisible) Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                            else Modifier.clearAndSetSemantics {}
                        ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            },
        )
    }
}

// Mide el aviso con el ancho del reloj. Medido por su cuenta, un texto largo ensancharía el diálogo.
@Composable
private fun RelojConAviso(reloj: @Composable () -> Unit, aviso: @Composable () -> Unit) {
    Layout(contents = listOf(reloj, aviso)) { (medibleReloj, medibleAviso), constraints ->
        val colocableReloj = medibleReloj.first().measure(constraints)
        val colocableAviso = medibleAviso.first().measure(Constraints.fixedWidth(colocableReloj.width))
        layout(colocableReloj.width, colocableReloj.height + colocableAviso.height) {
            colocableReloj.place(0, 0)
            colocableAviso.place(0, colocableReloj.height)
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HorarioLocalPreview() {
    ApprestauranteTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            HorarioLocalContenido(
                uiState = HorarioLocalUiState(error = ErrorRestaurante.SinConexion),
                onVolver = {},
                onAlternarDia = {},
                onEditarHoras = {},
                onCopiarLunesATodos = {},
                onEnviarARevision = {},
            )
        }
    }
}
