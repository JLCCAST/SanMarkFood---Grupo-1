package com.equipo.sanmarkfood.restaurante.presentation.admin.datos_iniciales

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.admin.DatosBorrados
import com.equipo.sanmarkfood.restaurante.domain.model.admin.DatosCargados
import com.equipo.sanmarkfood.restaurante.domain.model.admin.ErrorDatosIniciales
import com.equipo.sanmarkfood.restaurante.domain.model.admin.RolDePrueba
import com.equipo.sanmarkfood.restaurante.presentation.admin.CabeceraAdministracion
import com.equipo.sanmarkfood.restaurante.presentation.auth.BotonPrincipal

@Composable
fun DatosInicialesScreen(
    onSalir: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DatosInicialesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    uiState.confirmando?.let { accion ->
        DialogoConfirmarDatos(accion = accion, onConfirmar = viewModel::onConfirmar, onCancelar = viewModel::onCancelar)
    }

    Column(modifier = modifier) {
        CabeceraAdministracion(
            titulo = stringResource(R.string.datos_titulo),
            subtitulo = stringResource(R.string.datos_subtitulo),
            onSalir = onSalir,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            QueSeCarga()

            BotonPrincipal(
                texto = stringResource(R.string.datos_cargar),
                onClick = { viewModel.onPedir(AccionDatos.CARGAR) },
                habilitado = uiState.trabajando == null,
                cargando = uiState.trabajando == AccionDatos.CARGAR,
            )
            BotonBorrar(
                trabajando = uiState.trabajando,
                onClick = { viewModel.onPedir(AccionDatos.BORRAR) },
            )

            if (uiState.trabajando != null) {
                Text(
                    text = stringResource(R.string.datos_trabajando),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            uiState.error?.let { error ->
                Text(
                    text = stringResource(error.mensaje()),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            uiState.cargados?.let { ResumenCargados(it) }
            uiState.borrados?.let { ResumenBorrados(it) }
        }
    }
}

@Composable
private fun QueSeCarga() {
    val forma = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, forma)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(R.string.datos_que_se_carga),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        listOf(
            R.string.datos_incluye_locales,
            R.string.datos_incluye_carta,
            R.string.datos_incluye_comensales,
            R.string.datos_incluye_actividad,
        ).forEach { texto ->
            Text(
                text = stringResource(R.string.datos_vineta, stringResource(texto)),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Text(
            text = stringResource(R.string.datos_aviso),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun BotonBorrar(trabajando: AccionDatos?, onClick: () -> Unit) {
    val rojo = MaterialTheme.colorScheme.error
    OutlinedButton(
        onClick = onClick,
        enabled = trabajando == null,
        border = BorderStroke(1.5.dp, rojo),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = rojo),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
    ) {
        if (trabajando == AccionDatos.BORRAR) {
            CircularProgressIndicator(color = rojo, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
        } else {
            Text(text = stringResource(R.string.datos_borrar), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun ResumenCargados(datos: DatosCargados) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .semantics { liveRegion = LiveRegionMode.Polite }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(
                R.string.datos_cargados,
                datos.locales,
                datos.platos,
                datos.comensales,
                datos.pedidos,
                datos.reservas,
                datos.resenas,
            ),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
        )
        Text(
            text = stringResource(R.string.datos_cuentas, datos.contrasena),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(top = 4.dp),
        )
        datos.cuentas.forEach { cuenta ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(
                        if (cuenta.rol == RolDePrueba.RESTAURANTE) R.string.datos_rol_local else R.string.datos_rol_comensal
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Column {
                    Text(
                        text = cuenta.nombre,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                    Text(
                        text = cuenta.correo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun ResumenBorrados(datos: DatosBorrados) {
    Text(
        text = stringResource(R.string.datos_borrados, datos.cuentas, datos.pedidos, datos.reservas),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .semantics { liveRegion = LiveRegionMode.Polite }
            .padding(16.dp),
        style = MaterialTheme.typography.bodyLarge,
    )
}

@Composable
private fun DialogoConfirmarDatos(accion: AccionDatos, onConfirmar: () -> Unit, onCancelar: () -> Unit) {
    val borrar = accion == AccionDatos.BORRAR
    AlertDialog(
        onDismissRequest = onCancelar,
        title = {
            Text(stringResource(if (borrar) R.string.datos_confirmar_borrar_titulo else R.string.datos_confirmar_cargar_titulo))
        },
        text = {
            Text(stringResource(if (borrar) R.string.datos_confirmar_borrar_texto else R.string.datos_confirmar_cargar_texto))
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmar,
                colors = if (borrar) {
                    ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                } else {
                    ButtonDefaults.textButtonColors()
                },
            ) {
                Text(stringResource(if (borrar) R.string.datos_borrar else R.string.datos_cargar))
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text(stringResource(R.string.datos_cancelar)) }
        },
    )
}

@StringRes
private fun ErrorDatosIniciales.mensaje(): Int = when (this) {
    ErrorDatosIniciales.SinConexion -> R.string.datos_error_sin_conexion
    ErrorDatosIniciales.NoAutorizado -> R.string.datos_error_no_autorizado
    ErrorDatosIniciales.Desconocido -> R.string.datos_error
}
