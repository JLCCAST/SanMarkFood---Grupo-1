package com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.LARGO_CODIGO_SMS
import com.equipo.sanmarkfood.restaurante.presentation.auth.BotonPrincipal
import com.equipo.sanmarkfood.restaurante.presentation.auth.CampoFormulario
import com.equipo.sanmarkfood.restaurante.ui.theme.ApprestauranteTheme

@Composable
fun VerificacionTelefono(
    telefono: String,
    codigo: String,
    error: ErrorRestaurante?,
    verificando: Boolean,
    enviando: Boolean,
    segundosParaReenviar: Int,
    onCambiarCodigo: (String) -> Unit,
    onVerificar: () -> Unit,
    onReenviar: () -> Unit,
    onCancelar: () -> Unit,
) {
    BackHandler(onBack = onCancelar)

    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        CabeceraEdicion(titulo = stringResource(R.string.otp_titulo), onVolver = onCancelar)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            val enviamos = stringResource(R.string.otp_enviamos, telefono)
            Text(
                text = buildAnnotatedString {
                    append(enviamos)
                    val inicio = enviamos.indexOf(telefono)
                    if (inicio >= 0) {
                        addStyle(SpanStyle(fontWeight = FontWeight.Bold), inicio, inicio + telefono.length)
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
            )
            CampoFormulario(
                etiqueta = stringResource(R.string.otp_codigo),
                valor = codigo,
                onValorChange = onCambiarCodigo,
                tipoTeclado = KeyboardType.Number,
                ejemplo = stringResource(R.string.otp_codigo_ejemplo),
                esError = error != null,
            )
            error?.let { MensajeErrorRestaurante(error = it) }
            Text(
                text = stringResource(R.string.otp_no_llego),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            BotonPrincipal(
                texto = stringResource(R.string.otp_verificar),
                onClick = onVerificar,
                habilitado = codigo.length == LARGO_CODIGO_SMS && !enviando,
                cargando = verificando,
            )
            TextButton(
                onClick = onReenviar,
                enabled = segundosParaReenviar == 0 && !enviando && !verificando,
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) {
                if (enviando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        text = if (segundosParaReenviar > 0) {
                            stringResource(R.string.otp_reenviar_en, segundosParaReenviar)
                        } else {
                            stringResource(R.string.otp_reenviar)
                        },
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun VerificacionTelefonoPreview() {
    ApprestauranteTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            VerificacionTelefono(
                telefono = "+51 987 654 321",
                codigo = "4821",
                error = ErrorRestaurante.CodigoIncorrecto,
                verificando = false,
                enviando = false,
                segundosParaReenviar = 42,
                onCambiarCodigo = {},
                onVerificar = {},
                onReenviar = {},
                onCancelar = {},
            )
        }
    }
}
