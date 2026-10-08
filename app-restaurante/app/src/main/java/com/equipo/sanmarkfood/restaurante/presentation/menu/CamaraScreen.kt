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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.ui.theme.ApprestauranteTheme
import com.equipo.sanmarkfood.restaurante.ui.theme.extendedColors
import java.io.File

@Composable
fun CamaraScreen(
    onCerrar: () -> Unit,
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
            onPermitir = { pedirPermiso.launch(Manifest.permission.CAMERA) },
            onTomarFoto = {
                viewModel.onTomarFoto()
                tomarFoto(context, camara, onLista = viewModel::onFotoLista, onFallo = viewModel::onErrorFoto)
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
    onLista: (String) -> Unit,
    onFallo: () -> Unit,
) {
    val archivo = File(context.cacheDir, "pizarra.jpg")
    camara.takePicture(
        ImageCapture.OutputFileOptions.Builder(archivo).build(),
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(resultado: ImageCapture.OutputFileResults) = onLista(Uri.fromFile(archivo).toString())

            override fun onError(excepcion: ImageCaptureException) = onFallo()
        },
    )
}

@Composable
private fun CamaraContenido(
    uiState: CamaraUiState,
    camara: LifecycleCameraController?,
    onCerrar: () -> Unit,
    onPermitir: () -> Unit,
    onTomarFoto: () -> Unit,
    onElegirDeGaleria: () -> Unit,
) {
    val ocupada = uiState.tomando || uiState.foto != null
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.extendedColors.superficieFija)
            .safeDrawingPadding(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onCerrar) {
                Icon(
                    painter = painterResource(R.drawable.ic_cerrar),
                    contentDescription = stringResource(R.string.camara_cerrar),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
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
            when {
                uiState.foto != null -> AsyncImage(
                    model = uiState.foto,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                camara != null -> VistaPreviaCamara(camara = camara)
                else -> AvisoPermiso(onPermitir = onPermitir)
            }
            GuiasEncuadre(modifier = Modifier.fillMaxSize())
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(if (uiState.error) R.string.camara_error else R.string.camara_consejo_pizarra),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
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
            uiState = CamaraUiState(),
            camara = null,
            onCerrar = {},
            onPermitir = {},
            onTomarFoto = {},
            onElegirDeGaleria = {},
        )
    }
}
