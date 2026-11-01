package com.equipo.sanmarkfood.comensal.presentation.descubrimiento

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.CategoriaRestaurante
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.ErrorDescubrimiento
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.HorarioDia
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.Restaurante
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.horaCierraHoy
import com.equipo.sanmarkfood.comensal.domain.model.descubrimiento.menuVigente
import com.equipo.sanmarkfood.comensal.ui.theme.extendedColors
import java.util.Locale

// Claves del horario guardado en Firestore y el nombre que se muestra de cada día.
private val DIAS_DE_LA_SEMANA = listOf(
    "lun" to R.string.detalle_dia_lunes,
    "mar" to R.string.detalle_dia_martes,
    "mie" to R.string.detalle_dia_miercoles,
    "jue" to R.string.detalle_dia_jueves,
    "vie" to R.string.detalle_dia_viernes,
    "sab" to R.string.detalle_dia_sabado,
    "dom" to R.string.detalle_dia_domingo
)

@Composable
fun DetalleRestauranteScreen(
    restauranteId: String,
    onVolver: () -> Unit,
    onVerEnMapa: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DescubrimientoViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    // Se busca en la lista completa, no en la filtrada: un filtro activo no debe esconder el local.
    val restaurante = state.todosLosRestaurantes.firstOrNull { it.id == restauranteId }

    if (restaurante == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    DetalleRestauranteContent(
        restaurante = restaurante,
        esFavorito = restauranteId in state.favoritos,
        errorFavorito = state.errorFavorito,
        onCambiarFavorito = { viewModel.cambiarFavorito(restauranteId) },
        onErrorFavoritoMostrado = { viewModel.limpiarErrorFavorito() },
        onVolver = onVolver,
        onVerEnMapa = { onVerEnMapa(restauranteId) },
        modifier = modifier
    )
}

@Composable
private fun DetalleRestauranteContent(
    restaurante: Restaurante,
    esFavorito: Boolean,
    errorFavorito: ErrorDescubrimiento?,
    onCambiarFavorito: () -> Unit,
    onErrorFavoritoMostrado: () -> Unit,
    onVolver: () -> Unit,
    onVerEnMapa: () -> Unit,
    modifier: Modifier = Modifier
) {
    val guinda = MaterialTheme.colorScheme.primary
    val dorado = MaterialTheme.colorScheme.secondary
    val verdeEstado = MaterialTheme.colorScheme.tertiary

    // Solo cuenta el menú que es de hoy y está publicado.
    val menuHoy = restaurante.menuVigente()

    // Si el local no tiene logo, se usa su portada (igual que la lista).
    val urlLogo = restaurante.logoUrl.ifBlank { restaurante.portadaUrl }

    // Aviso cuando falla marcar o quitar un favorito. El error se limpia al terminar el aviso
    // o al salir de la pantalla, para que no reaparezca al volver a abrir el detalle.
    val snackbarHostState = remember { SnackbarHostState() }
    val mensajeErrorFavorito = errorFavorito?.let { stringResource(it.mensajeFavorito()) }
    LaunchedEffect(errorFavorito) {
        if (mensajeErrorFavorito != null) {
            try {
                snackbarHostState.showSnackbar(mensajeErrorFavorito)
            } finally {
                onErrorFavoritoMostrado()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onVolver) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.detalle_volver)
                    )
                }
                IconButton(onClick = onCambiarFavorito) {
                    Icon(
                        imageVector = if (esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(
                            if (esFavorito) R.string.detalle_favorito_quitar
                            else R.string.detalle_favorito_agregar
                        ),
                        tint = guinda
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(28.dp),
                color = guinda,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = dorado,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "0",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.detalle_pedido_titulo),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = stringResource(R.string.detalle_pedido_subtitulo),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.detalle_precio_soles, "0.00"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header del local
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (urlLogo.isNotBlank()) {
                        AsyncImage(
                            model = urlLogo,
                            contentDescription = restaurante.nombre,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = restaurante.nombre.firstOrNull()?.uppercase().orEmpty(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = restaurante.nombre,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )

                    val categoria = stringResource(restaurante.categoria.etiquetaCategoria())
                    val distancia = restaurante.distanciaMetros?.let {
                        " · " + stringResource(R.string.descubrimiento_distancia_m, it)
                    }.orEmpty()
                    Text(
                        text = categoria + distancia,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val horaCierra = restaurante.horaCierraHoy()
                    Text(
                        text = if (horaCierra != null) {
                            stringResource(R.string.detalle_abierto_hasta, horaCierra)
                        } else {
                            stringResource(R.string.descubrimiento_cerrado)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (horaCierra != null) verdeEstado else MaterialTheme.colorScheme.error
                    )
                }
            }

            // Acciones rápidas (sin función: son de otras historias).
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = guinda)
                ) {
                    Text(stringResource(R.string.detalle_boton_reservar))
                }
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.detalle_boton_preguntar))
                }
            }

            // Calificación: solo si el local ya tiene reseñas.
            val promedio = restaurante.calificacionPromedio
            if (promedio != null && restaurante.totalResenas > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f", promedio),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = restaurante.resumenResenas
                                ?.let { stringResource(R.string.detalle_resumen_ia, it.trimEnd('.')) }
                                ?: stringResource(R.string.detalle_calificacion_general),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = pluralStringResource(
                            R.plurals.detalle_resenas,
                            restaurante.totalResenas,
                            restaurante.totalResenas
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = guinda,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.descubrimiento_sin_resenas),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Tarjeta Menú de hoy (dinámico, de Firestore)
            if (menuHoy != null) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.extendedColors.superficieFija
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.detalle_menu_titulo),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.extendedColors.sobreSuperficieFija
                                )
                                Text(
                                    text = stringResource(R.string.detalle_menu_sirve_hasta, menuHoy.horaFin),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.extendedColors.sobreSuperficieFijaTenue
                                )
                            }
                            Text(
                                text = stringResource(
                                    R.string.detalle_precio_soles,
                                    String.format(Locale.US, "%.2f", menuHoy.precio / 100.0)
                                ),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = dorado
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.detalle_menu_publicado),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = guinda
                                )
                                Text(stringResource(R.string.detalle_menu_consulta))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.detalle_menu_disponible_hoy),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Button(
                                        onClick = { },
                                        colors = ButtonDefaults.buttonColors(containerColor = guinda)
                                    ) {
                                        Text(stringResource(R.string.detalle_boton_armar_menu))
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.detalle_sin_menu),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Horario y ubicación reales de Firestore
            Text(
                text = stringResource(R.string.detalle_horario_ubicacion),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = guinda
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = restaurante.direccion.ifBlank {
                            stringResource(R.string.detalle_direccion_vacia)
                        },
                        fontWeight = FontWeight.SemiBold
                    )

                    if (restaurante.horario.isNotEmpty()) {
                        DIAS_DE_LA_SEMANA.forEach { (clave, etiqueta) ->
                            FilaHorario(stringResource(etiqueta), restaurante.horario[clave])
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.detalle_horario_no_disponible),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.detalle_ver_en_mapa),
                        color = guinda,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable(onClick = onVerEnMapa)
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun FilaHorario(dia: String, horarioDia: HorarioDia?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(dia)
        if (horarioDia != null && horarioDia.abierto) {
            Text(stringResource(R.string.detalle_horario_rango, horarioDia.abre, horarioDia.cierra))
        } else {
            Text(
                text = stringResource(R.string.detalle_horario_cerrado),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@StringRes
private fun ErrorDescubrimiento.mensajeFavorito(): Int = when (this) {
    ErrorDescubrimiento.SinConexion -> R.string.error_descubrimiento_sin_conexion
    ErrorDescubrimiento.Desconocido -> R.string.error_favorito_desconocido
}

@StringRes
private fun CategoriaRestaurante.etiquetaCategoria(): Int = when (this) {
    CategoriaRestaurante.CRIOLLA -> R.string.descubrimiento_categoria_criolla
    CategoriaRestaurante.CHIFA -> R.string.descubrimiento_categoria_chifa
    CategoriaRestaurante.POLLERIA -> R.string.descubrimiento_categoria_polleria
    CategoriaRestaurante.MARINA -> R.string.descubrimiento_categoria_marina
    CategoriaRestaurante.VEGETARIANA -> R.string.descubrimiento_categoria_vegetariana
    CategoriaRestaurante.OTRA -> R.string.descubrimiento_categoria_otra
}