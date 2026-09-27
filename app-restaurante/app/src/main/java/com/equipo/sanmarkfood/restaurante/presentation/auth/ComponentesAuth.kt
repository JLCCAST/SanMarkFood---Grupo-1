package com.equipo.sanmarkfood.restaurante.presentation.auth

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.ErrorAuth

@Composable
fun CabeceraPaso(texto: String, pasoActual: Int, onVolver: () -> Unit) {
    Row(
        modifier = Modifier.height(64.dp).padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        IconButton(onClick = onVolver) {
            Icon(
                painter = painterResource(R.drawable.ic_volver),
                contentDescription = stringResource(R.string.volver),
            )
        }
        Text(
            text = texto,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Row(
        modifier = Modifier.padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(3) { indice ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (indice < pasoActual) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    ),
            )
        }
    }
}

@Composable
fun CampoFormulario(
    etiqueta: String,
    valor: String,
    onValorChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    esContrasena: Boolean = false,
    ejemplo: String? = null,
    mensaje: String? = null,
    colorMensaje: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    esError: Boolean = false,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = etiqueta, style = MaterialTheme.typography.labelLarge)
        OutlinedTextField(
            value = valor,
            onValueChange = onValorChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = ejemplo?.let { { Text(it) } },
            isError = esError,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (esContrasena) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (esContrasena) KeyboardType.Password else KeyboardType.Email
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                errorContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
        )
        if (mensaje != null) {
            Text(text = mensaje, style = MaterialTheme.typography.bodySmall, color = colorMensaje)
        }
    }
}

@Composable
fun BotonPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    cargando: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = habilitado && !cargando,
        modifier = modifier.fillMaxWidth().height(52.dp),
    ) {
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = LocalContentColor.current,
                strokeWidth = 2.dp,
            )
        } else {
            Text(text = texto, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun MensajeError(error: ErrorAuth, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(error.mensaje()),
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
    )
}

@StringRes
private fun ErrorAuth.mensaje(): Int = when (this) {
    ErrorAuth.SinConexion -> R.string.error_sin_conexion
    ErrorAuth.CamposVacios -> R.string.error_campos_vacios
    ErrorAuth.CredencialesInvalidas -> R.string.error_credenciales_invalidas
    ErrorAuth.CorreoYaRegistrado -> R.string.error_correo_ya_registrado
    ErrorAuth.CorreoInvalido -> R.string.error_correo_invalido
    ErrorAuth.CorreoSinVerificar -> R.string.error_correo_sin_verificar
    ErrorAuth.DemasiadosIntentos -> R.string.error_demasiados_intentos
    ErrorAuth.Desconocido -> R.string.error_desconocido
}
