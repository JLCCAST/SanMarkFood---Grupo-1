package com.equipo.sanmarkfood.comensal.presentation.perfil

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.domain.model.perfil.Direccion
import com.equipo.sanmarkfood.comensal.presentation.auth.AuthTextField
import com.equipo.sanmarkfood.comensal.presentation.auth.ErrorMessage
import com.equipo.sanmarkfood.comensal.presentation.auth.PrimaryButton
import kotlinx.coroutines.delay

@Composable
fun PerfilScreen(
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ComensalViewModel = hiltViewModel()
) {
    val colors = MaterialTheme.colorScheme
    val state by viewModel.uiState.collectAsState()

    // Datos de solo lectura que vienen de Firebase Auth (a través del ViewModel).
    val correo = remember { viewModel.correo }
    val correoVerificado = remember { viewModel.correoVerificado }
    val miembroDesde = remember { viewModel.miembroDesde }
    val nombreRegistro = remember { viewModel.nombreRegistro }

    // Si Firestore aún no tiene nombre, se usa el que se escribió al registrarse.
    val nombreMostrado = state.comensal.nombre.ifBlank { nombreRegistro }

    // Selector de imágenes de Android: no necesita permisos.
    val selectorFoto = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.subirFoto(uri)
    }

    LaunchedEffect(Unit) {
        viewModel.cargarPerfil()
    }

    // El mensaje "Cambios guardados" se oculta solo a los 2,5 segundos.
    LaunchedEffect(state.guardadoExitoso) {
        if (state.guardadoExitoso) {
            delay(2500)
            viewModel.clearGuardadoExitoso()
        }
    }

    var nombre by rememberSaveable(state.comensal.nombre) {
        mutableStateOf(state.comensal.nombre.ifBlank { nombreRegistro })
    }
    var telefono by rememberSaveable(state.comensal.telefono) { mutableStateOf(state.comensal.telefono) }

    val telefonoIncompleto = telefono.isNotEmpty() && telefono.length != 9
    val hayCambios = nombre.trim() != state.comensal.nombre || telefono != state.comensal.telefono
    val puedeGuardar = hayCambios && nombre.isNotBlank() && !telefonoIncompleto

    val nombrePorDefecto = stringResource(R.string.perfil_nombre_por_defecto)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = stringResource(R.string.perfil_titulo),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )

        // Avatar, nombre y fecha de alta
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AvatarPerfil(
                iniciales = iniciales(nombreMostrado, correo),
                fotoUrl = state.comensal.fotoUrl,
                subiendo = state.subiendoFoto,
                onCambiarFoto = {
                    selectorFoto.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = nombreMostrado.ifBlank { nombrePorDefecto },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (miembroDesde.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.perfil_miembro_desde, miembroDesde),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        }

        // Aviso no bloqueante: solo invita a completar los datos.
        if (state.perfilIncompleto && !state.isLoading && state.error == null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colors.secondaryContainer
            ) {
                Text(
                    text = stringResource(R.string.perfil_incompleto_aviso),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSecondaryContainer
                )
            }
        }

        Text(
            text = stringResource(R.string.perfil_seccion_datos),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        AuthTextField(
            label = stringResource(R.string.perfil_etiqueta_nombre),
            value = nombre,
            onValueChange = { nombre = it },
            placeholder = stringResource(R.string.perfil_placeholder_nombre)
        )
        AuthTextField(
            label = stringResource(R.string.perfil_etiqueta_telefono),
            value = telefono,
            onValueChange = { telefono = it.filter(Char::isDigit).take(9) },
            placeholder = stringResource(R.string.perfil_placeholder_telefono),
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Done,
            isError = telefonoIncompleto
        )
        if (telefonoIncompleto) {
            ErrorMessage(stringResource(R.string.perfil_error_telefono))
        }
        CorreoSoloLectura(correo = correo, verificado = correoVerificado)

        state.error?.let { ErrorMessage(stringResource(it)) }

        if (state.guardadoExitoso) {
            Text(
                text = stringResource(R.string.perfil_cambios_guardados),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.tertiary
            )
        }

        PrimaryButton(
            text = stringResource(R.string.perfil_boton_guardar),
            onClick = { viewModel.actualizarDatos(nombre.trim(), telefono) },
            enabled = puedeGuardar,
            isLoading = state.isLoading
        )

        // Las direcciones se guardan al instante, sin pulsar "Guardar cambios".
        SeccionDirecciones(
            direcciones = state.comensal.direcciones,
            onAgregar = viewModel::agregarDireccion,
            onEliminar = viewModel::eliminarDireccion
        )

        SeccionNotificaciones(
            notificarReservas = state.comensal.notificarReservas,
            notificarResenas = state.comensal.notificarResenas,
            onReservasChange = {
                viewModel.actualizarPreferenciasNotificacion(it, state.comensal.notificarResenas)
            },
            onResenasChange = {
                viewModel.actualizarPreferenciasNotificacion(state.comensal.notificarReservas, it)
            }
        )

        OutlinedButton(
            onClick = onCerrarSesion,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = CircleShape
        ) {
            Text(stringResource(R.string.perfil_boton_cerrar_sesion), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SeccionDirecciones(
    direcciones: List<Direccion>,
    onAgregar: (Direccion) -> Unit,
    onEliminar: (Direccion) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var mostrarFormulario by rememberSaveable { mutableStateOf(false) }
    var etiqueta by rememberSaveable { mutableStateOf("") }
    var direccionTexto by rememberSaveable { mutableStateOf("") }

    // Cuando la lista cambia (se guardó o se eliminó una dirección) se cierra el formulario.
    LaunchedEffect(direcciones.size) {
        mostrarFormulario = false
        etiqueta = ""
        direccionTexto = ""
    }

    val puedeAgregar = etiqueta.isNotBlank() && direccionTexto.isNotBlank()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.perfil_direcciones_titulo),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (direcciones.isEmpty() && !mostrarFormulario) {
            Text(
                text = stringResource(R.string.perfil_direcciones_vacio),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        }

        direcciones.forEach { direccion ->
            DireccionItem(direccion = direccion, onEliminar = { onEliminar(direccion) })
        }

        if (mostrarFormulario) {
            AuthTextField(
                label = stringResource(R.string.perfil_direccion_etiqueta),
                value = etiqueta,
                onValueChange = { etiqueta = it.take(30) },
                placeholder = stringResource(R.string.perfil_direccion_placeholder_etiqueta)
            )
            AuthTextField(
                label = stringResource(R.string.perfil_direccion_etiqueta_direccion),
                value = direccionTexto,
                onValueChange = { direccionTexto = it.take(150) },
                placeholder = stringResource(R.string.perfil_direccion_placeholder_direccion),
                imeAction = ImeAction.Done
            )
            PrimaryButton(
                text = stringResource(R.string.perfil_direccion_guardar),
                onClick = {
                    onAgregar(
                        Direccion(
                            etiqueta = etiqueta.trim(),
                            direccionTexto = direccionTexto.trim()
                        )
                    )
                },
                enabled = puedeAgregar,
                isLoading = false
            )
            TextButton(
                onClick = {
                    mostrarFormulario = false
                    etiqueta = ""
                    direccionTexto = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.common_cancelar))
            }
        } else {
            OutlinedButton(
                onClick = { mostrarFormulario = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = CircleShape
            ) {
                Text(stringResource(R.string.perfil_direccion_agregar), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun SeccionNotificaciones(
    notificarReservas: Boolean,
    notificarResenas: Boolean,
    onReservasChange: (Boolean) -> Unit,
    onResenasChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.perfil_notificaciones_titulo),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        NotificacionItem(
            titulo = stringResource(R.string.perfil_notif_reservas_titulo),
            descripcion = stringResource(R.string.perfil_notif_reservas_desc),
            checked = notificarReservas,
            onCheckedChange = onReservasChange
        )
        NotificacionItem(
            titulo = stringResource(R.string.perfil_notif_resenas_titulo),
            descripcion = stringResource(R.string.perfil_notif_resenas_desc),
            checked = notificarResenas,
            onCheckedChange = onResenasChange
        )
        NotificacionItem(
            titulo = stringResource(R.string.perfil_notif_pedido_titulo),
            descripcion = stringResource(R.string.perfil_notif_pedido_desc),
            checked = true,
            onCheckedChange = {},
            habilitado = false
        )
    }
}

@Composable
private fun NotificacionItem(
    titulo: String,
    descripcion: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    habilitado: Boolean = true
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = habilitado
            )
        }
    }
}

@Composable
private fun DireccionItem(
    direccion: Direccion,
    onEliminar: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, end = 4.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = direccion.etiqueta,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = direccion.direccionTexto,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }
            TextButton(onClick = onEliminar) {
                Text(stringResource(R.string.perfil_direccion_eliminar), color = colors.error)
            }
        }
    }
}

@Composable
private fun AvatarPerfil(
    iniciales: String,
    fotoUrl: String?,
    subiendo: Boolean,
    onCambiarFoto: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Box(modifier = Modifier.size(72.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(colors.inverseSurface)
                .clickable(enabled = !subiendo, onClick = onCambiarFoto),
            contentAlignment = Alignment.Center
        ) {
            when {
                subiendo -> CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 3.dp,
                    color = colors.inverseOnSurface
                )

                !fotoUrl.isNullOrBlank() -> AsyncImage(
                    model = fotoUrl,
                    contentDescription = stringResource(R.string.perfil_avatar_foto_descripcion),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                else -> Text(
                    text = iniciales,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.inverseOnSurface
                )
            }
        }
        // Insignia para cambiar la foto.
        Box(
            modifier = Modifier
                .size(26.dp)
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(colors.primary)
                .clickable(enabled = !subiendo, onClick = onCambiarFoto),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = stringResource(R.string.perfil_avatar_cambiar_descripcion),
                tint = colors.onPrimary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun CorreoSoloLectura(correo: String, verificado: Boolean) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(R.string.perfil_etiqueta_correo),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = colors.surfaceContainerHigh
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = correo,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurfaceVariant
                )
                if (verificado) {
                    Text(
                        text = stringResource(R.string.perfil_correo_verificado),
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colors.tertiaryContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onTertiaryContainer
                    )
                }
            }
        }
    }
}

// Iniciales para el avatar: dos letras con nombre y apellido, una con un solo nombre.
private fun iniciales(nombre: String, correo: String): String {
    val partes = nombre.trim().split(" ").filter { it.isNotBlank() }
    return when {
        partes.size >= 2 -> "${partes[0].first()}${partes[1].first()}"
        partes.size == 1 -> partes[0].first().toString()
        else -> correo.firstOrNull()?.toString().orEmpty()
    }.uppercase()
}