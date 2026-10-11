package com.equipo.sanmarkfood.comensal.presentation.auth

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.domain.model.auth.ErrorAuth

@Composable
fun AuthTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: @Composable (() -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = placeholder?.let { { Text(it) } },
            singleLine = true,
            isError = isError,
            shape = RoundedCornerShape(12.dp),
            visualTransformation = visualTransformation,
            trailingIcon = trailing,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = KeyboardActions(onDone = { onImeAction() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.surfaceContainerLowest,
                unfocusedContainerColor = colors.surfaceContainerLowest,
                errorContainerColor = colors.surfaceContainerLowest
            )
        )
    }
}

@Composable
fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {},
    isError: Boolean = false
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    AuthTextField(
        label = label,
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        keyboardType = KeyboardType.Password,
        imeAction = imeAction,
        onImeAction = onImeAction,
        isError = isError,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailing = {
            TextButton(onClick = { visible = !visible }) {
                Text(
                    stringResource(
                        if (visible) R.string.auth_ocultar else R.string.auth_mostrar
                    )
                )
            }
        }
    )
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = CircleShape
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.5.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ErrorMessage(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error
    )
}

/**
 * Elige el texto que se muestra para cada error de autenticación.
 * Reemplaza a MapeadorErroresAuth: el dominio solo conoce ErrorAuth y aquí,
 * en presentation, es donde se decide el mensaje de strings.xml.
 */
@StringRes
fun ErrorAuth.mensaje(): Int = when (this) {
    ErrorAuth.SinConexion -> R.string.error_sin_conexion
    ErrorAuth.CamposVacios -> R.string.error_login_campos_vacios
    ErrorAuth.CredencialesInvalidas -> R.string.error_credenciales_incorrectas
    ErrorAuth.CorreoYaRegistrado -> R.string.error_correo_en_uso
    ErrorAuth.CorreoInvalido -> R.string.error_correo_invalido
    ErrorAuth.DemasiadosIntentos -> R.string.error_demasiados_intentos
    ErrorAuth.Desconocido -> R.string.error_generico
    ErrorAuth.CuentaDeOtroRol -> R.string.error_cuenta_otro_rol
    ErrorAuth.ContrasenaDebil -> R.string.error_contrasena_debil
    ErrorAuth.ContrasenaCorta -> R.string.error_contrasena_longitud
    ErrorAuth.ContrasenaSinNumero -> R.string.error_contrasena_numero
    ErrorAuth.NombreVacio -> R.string.error_registro_nombre_vacio
    ErrorAuth.TerminosNoAceptados -> R.string.error_registro_terminos
    ErrorAuth.CorreoVacio -> R.string.error_recuperar_correo_vacio
    ErrorAuth.CorreoNoRegistrado -> R.string.error_recuperar_correo_no_encontrado
    ErrorAuth.CorreoSinVerificar -> R.string.error_verificacion_pendiente
}