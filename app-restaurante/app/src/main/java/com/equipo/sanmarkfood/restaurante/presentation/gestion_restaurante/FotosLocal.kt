package com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.equipo.sanmarkfood.restaurante.R
import com.equipo.sanmarkfood.restaurante.domain.model.TipoFoto

/**
 * Portada con el logo encima, como en R3. Cada una abre el selector de fotos de Android, que no pide
 * permisos. La foto se sube apenas la eligen; mientras sube se ve con el progreso encima.
 */
@Composable
fun FotosLocal(
    portada: FotoUiState,
    logo: FotoUiState,
    portadaFaltante: Boolean,
    habilitado: Boolean,
    onFotoElegida: (TipoFoto, imagenLocal: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val soloImagenes = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
    val elegirPortada = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { onFotoElegida(TipoFoto.PORTADA, it.toString()) }
    }
    val elegirLogo = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { onFotoElegida(TipoFoto.LOGO, it.toString()) }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // El logo sobresale 22 dp bajo la portada; este margen lo deja casi todo dentro del bloque.
        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp)) {
            Portada(
                foto = portada,
                faltante = portadaFaltante,
                habilitado = habilitado,
                onElegir = { elegirPortada.launch(soloImagenes) },
                modifier = Modifier.fillMaxWidth().height(150.dp),
            )
            Logo(
                foto = logo,
                habilitado = habilitado,
                onElegir = { elegirLogo.launch(soloImagenes) },
                modifier = Modifier.align(Alignment.BottomStart).offset(x = 14.dp, y = 22.dp),
            )
        }

        val errorFoto = portada.error ?: logo.error
        when {
            errorFoto != null -> MensajeErrorRestaurante(error = errorFoto, modifier = Modifier.padding(top = 4.dp))
            portadaFaltante -> Text(
                text = stringResource(R.string.fotos_error_portada),
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun Portada(
    foto: FotoUiState,
    faltante: Boolean,
    habilitado: Boolean,
    onElegir: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val radio = 16.dp
    val colorBorde = if (faltante) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(radio))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .then(if (foto.imagen == null) Modifier.bordePunteado(colorBorde, radio) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        foto.imagen?.let {
            AsyncImage(
                model = it,
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        }
        if (foto.subiendo) {
            ProgresoSubida(progreso = foto.progreso ?: 0f, tamano = 40.dp)
        } else {
            Surface(
                onClick = onElegir,
                enabled = habilitado,
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Row(
                    modifier = Modifier.height(40.dp).padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_camara),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = stringResource(
                            if (foto.imagen == null) R.string.fotos_subir_portada else R.string.fotos_cambiar_portada
                        ),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

// El prototipo pinta el cuadro del logo con un café claro (#E6C29C) que no está en el tema; mientras
// no se agregue, se usa el contenedor secundario (ají claro), que es el más parecido.
@Composable
private fun Logo(
    foto: FotoUiState,
    habilitado: Boolean,
    onElegir: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val descripcion = stringResource(if (foto.imagen == null) R.string.fotos_subir_logo else R.string.fotos_cambiar_logo)
    Surface(
        onClick = onElegir,
        enabled = habilitado && !foto.subiendo,
        modifier = modifier.size(64.dp).semantics { contentDescription = descripcion },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        border = BorderStroke(3.dp, MaterialTheme.colorScheme.background),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (foto.imagen != null) {
                AsyncImage(
                    model = foto.imagen,
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Text(text = stringResource(R.string.fotos_logo), style = MaterialTheme.typography.labelSmall)
            }
            if (foto.subiendo) ProgresoSubida(progreso = foto.progreso ?: 0f, tamano = 28.dp)
        }
    }
}

// Velo oscuro con el avance encima de la foto. En 0 todavía se está achicando la imagen: gira sin porcentaje.
@Composable
private fun BoxScope.ProgresoSubida(progreso: Float, tamano: Dp) {
    val descripcion = stringResource(R.string.fotos_subiendo)
    val colorIndicador = MaterialTheme.colorScheme.surfaceContainerLowest
    Box(
        modifier = Modifier
            .matchParentSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f))
            .semantics { contentDescription = descripcion },
        contentAlignment = Alignment.Center,
    ) {
        if (progreso <= 0f) {
            CircularProgressIndicator(modifier = Modifier.size(tamano), color = colorIndicador)
        } else {
            CircularProgressIndicator(
                progress = { progreso },
                modifier = Modifier.size(tamano),
                color = colorIndicador,
                trackColor = colorIndicador.copy(alpha = 0.3f),
            )
        }
    }
}

private fun Modifier.bordePunteado(color: Color, radio: Dp): Modifier = drawBehind {
    val grosor = 1.5.dp.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(grosor / 2, grosor / 2),
        size = Size(size.width - grosor, size.height - grosor),
        cornerRadius = CornerRadius(radio.toPx()),
        style = Stroke(width = grosor, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))),
    )
}
