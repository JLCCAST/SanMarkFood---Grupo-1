package com.equipo.sanmarkfood.restaurante.presentation.menu

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.ui.theme.ApprestauranteTheme
import com.equipo.sanmarkfood.restaurante.ui.theme.extendedColors
import java.io.File

@Composable
fun CamaraScreen(
    onCerrar: () -> Unit,
    onRevisar: (ModoCamara) -> Unit,
    viewModel: CamaraViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val camara = remember {
        LifecycleCameraController(context).apply { setEnabledUseCases(CameraController.IMAGE_CAPTURE) }
    }

    var permitida by remember { mutableStateOf(tienePermisoDeCamara(context)) }
    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permitida = it }
    LaunchedEffect(Unit) {
        if (!permitida) pedirPermiso.launch(Manifest.permission.CAMERA)
    }

    val elegirDeGaleria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onFotoLista(uri.toString())
    }

    ApprestauranteTheme(darkTheme = true) {
        CamaraContenido(
            uiState = uiState,
            camara = camara.takeIf { permitida },
            onCerrar = onCerrar,
            onCambiarModo = viewModel::onCambiarModo,
            onRevisar = { onRevisar(uiState.modo) },
            onPermitir = { pedirPermiso.launch(Manifest.permission.CAMERA) },
            onTomarFoto = {
                viewModel.onTomarFoto()
                tomarFoto(
                    context = context,
                    camara = camara,
                    archivo = if (uiState.modo == ModoCamara.CARTA) "carta.jpg" else "pizarra.jpg",
                    onLista = viewModel::onFotoLista,
                    onFallo = viewModel::onErrorFoto,
                )
            },
            onElegirDeGaleria = {
                elegirDeGaleria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
        )
    }
}

private fun tienePermisoDeCamara(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

private fun tomarFoto(
    context: Context,
    camara: LifecycleCameraController,
    archivo: String,
    onLista: (String) -> Unit,
    onFallo: () -> Unit,
) {
    val destino = File(context.cacheDir, archivo)
    camara.takePicture(
        ImageCapture.OutputFileOptions.Builder(destino).build(),
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(resultado: ImageCapture.OutputFileResults) = onLista(Uri.fromFile(destino).toString())

            override fun onError(excepcion: ImageCaptureException) = onFallo()
        },
    )
}

@Composable
private fun CamaraContenido(
    uiState: CamaraUiState,
    camara: LifecycleCameraController?,
    onCerrar: () -> Unit,
    onCambiarModo: (ModoCamara) -> Unit,
    onRevisar: () -> Unit,
    onPermitir: () -> Unit,
    onTomarFoto: () -> Unit,
    onElegirDeGaleria: () -> Unit,
) {
    val ocupada = uiState.tomando || uiState.leyendo || uiState.listo
    val carta = uiState.modo == ModoCamara.CARTA
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.extendedColors.superficieFija)
            .safeDrawingPadding(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).padding(start = 4.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onCerrar) {
                Icon(
                    painter = painterResource(R.drawable.ic_cerrar),
                    contentDescription = stringResource(R.string.camara_cerrar),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            SelectorModo(
                actual = uiState.modo,
                habilitado = !uiState.tomando && !uiState.leyendo,
                onElegir = onCambiarModo,
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer),
            contentAlignment = Alignment.Center,
        ) {
            if (camara != null) VistaPreviaCamara(camara = camara) else AvisoPermiso(onPermitir = onPermitir)
            GuiasEncuadre(modifier = Modifier.fillMaxSize())
            if (uiState.leyendo || uiState.listo) {
                Lectura(carta = carta, listo = uiState.listo, platosLeidos = uiState.platosLeidos, onRevisar = onRevisar)
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            val error = uiState.error
            if (error != null) {
                MensajeErrorMenu(error = error)
            } else {
                Text(
                    text = stringResource(if (carta) R.string.camara_consejo_carta else R.string.camara_consejo_pizarra),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedIconButton(
                    onClick = onElegirDeGaleria,
                    enabled = !ocupada,
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_galeria),
                        contentDescription = stringResource(R.string.camara_galeria),
                    )
                }
                BotonDisparador(habilitado = camara != null && !ocupada, onClick = onTomarFoto)
                Spacer(modifier = Modifier.size(52.dp))
            }
        }
    }
}

@Composable
private fun VistaPreviaCamara(camara: LifecycleCameraController) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        camara.bindToLifecycle(lifecycleOwner)
        onDispose { camara.unbind() }
    }
    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                controller = camara
            }
        },
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
private fun AvisoPermiso(onPermitir: () -> Unit) {
    Column(
        modifier = Modifier.padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.camara_permiso),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onPermitir,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
            ),
        ) {
            Text(text = stringResource(R.string.camara_permitir), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun SelectorModo(actual: ModoCamara, habilitado: Boolean, onElegir: (ModoCamara) -> Unit) {
    val descripcion = stringResource(R.string.camara_que_fotografiar)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(3.dp)
            .selectableGroup()
            .semantics { contentDescription = descripcion },
    ) {
        listOf(ModoCamara.CARTA to R.string.camara_modo_carta, ModoCamara.PIZARRA to R.string.camara_modo_pizarra)
            .forEach { (modo, texto) ->
                val elegido = modo == actual
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .then(if (elegido) Modifier.background(MaterialTheme.colorScheme.secondary) else Modifier)
                        .selectable(selected = elegido, enabled = habilitado, role = Role.Tab, onClick = { onElegir(modo) })
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(texto),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (elegido) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
    }
}

@Composable
private fun Lectura(carta: Boolean, listo: Boolean, platosLeidos: Int, onRevisar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.extendedColors.superficieFija.copy(alpha = 0.82f))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = when {
                carta && listo -> pluralStringResource(R.plurals.camara_leida_carta, platosLeidos, platosLeidos)
                carta -> stringResource(R.string.camara_leyendo_carta)
                listo -> stringResource(R.string.camara_leido)
                else -> stringResource(R.string.camara_leyendo)
            },
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PasoLectura(texto = stringResource(R.string.camara_paso_texto), hecho = listo, enCurso = !listo)
            PasoLectura(
                texto = stringResource(if (carta) R.string.camara_paso_ordenar else R.string.camara_paso_separar),
                hecho = listo,
                enCurso = !listo,
            )
            PasoLectura(texto = stringResource(R.string.camara_paso_listo), hecho = listo, enCurso = false)
        }
        if (listo) {
            Button(
                onClick = onRevisar,
                modifier = Modifier.padding(top = 8.dp).height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                ),
            ) {
                Text(text = stringResource(R.string.camara_revisar), style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Composable
private fun PasoLectura(texto: String, hecho: Boolean, enCurso: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        when {
            hecho -> Box(
                modifier = Modifier.size(20.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_listo),
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onTertiary,
                )
            }
            enCurso -> CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.secondary,
                strokeWidth = 2.dp,
            )
            else -> Box(
                modifier = Modifier.size(20.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outlineVariant),
            )
        }
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyLarge,
            color = if (hecho || enCurso) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
        )
    }
}

@Composable
private fun GuiasEncuadre(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.secondary
    Canvas(modifier = modifier) {
        val margen = 20.dp.toPx()
        val largo = 36.dp.toPx()
        val grosor = 4.dp.toPx()
        val esquinas = listOf(
            Offset(margen, margen) to Offset(1f, 1f),
            Offset(size.width - margen, margen) to Offset(-1f, 1f),
            Offset(margen, size.height - margen) to Offset(1f, -1f),
            Offset(size.width - margen, size.height - margen) to Offset(-1f, -1f),
        )
        esquinas.forEach { (esquina, hacia) ->
            drawLine(color, esquina, esquina + Offset(largo * hacia.x, 0f), grosor, StrokeCap.Round)
            drawLine(color, esquina, esquina + Offset(0f, largo * hacia.y), grosor, StrokeCap.Round)
        }
    }
}

@Composable
private fun BotonDisparador(habilitado: Boolean, onClick: () -> Unit) {
    val descripcion = stringResource(R.string.camara_tomar)
    Surface(
        onClick = onClick,
        enabled = habilitado,
        modifier = Modifier
            .size(76.dp)
            .alpha(if (habilitado) 1f else 0.38f)
            .semantics { contentDescription = descripcion },
        shape = CircleShape,
        color = MaterialTheme.extendedColors.superficieFija,
        border = BorderStroke(4.dp, MaterialTheme.colorScheme.onSurface),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CamaraPreview() {
    ApprestauranteTheme(darkTheme = true) {
        CamaraContenido(
            uiState = CamaraUiState(modo = ModoCamara.CARTA, leyendo = true),
            camara = null,
            onCerrar = {},
            onCambiarModo = {},
            onRevisar = {},
            onPermitir = {},
            onTomarFoto = {},
            onElegirDeGaleria = {},
        )
    }
}
