package com.equipo.sanmarkfood.comensal.presentation.resenas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.equipo.sanmarkfood.comensal.R
import com.equipo.sanmarkfood.comensal.domain.model.pedidos.ItemPedido
import com.equipo.sanmarkfood.comensal.domain.model.resenas.Calificacion
import com.equipo.sanmarkfood.comensal.domain.model.resenas.ErrorCalificar

data class CalificacionUiState(
    val estrellas: Int = 0,
    val comentario: String = "",
    val votos: Map<String, Boolean> = emptyMap(),
    val enviando: Boolean = false,
    val enviada: Boolean = false,
    val yaCalificada: Boolean = false,
    val error: ErrorCalificar? = null,
)

data class PlatoCalificable(
    val platoId: String,
    val nombre: String,
)

fun platosCalificables(items: List<ItemPedido>): List<PlatoCalificable> =
    items.mapNotNull { item -> item.platoId?.let { PlatoCalificable(it, item.nombre) } }
        .distinctBy { it.platoId }

@Composable
fun TarjetaCalificacion(
    restauranteNombre: String,
    platos: List<PlatoCalificable>,
    estado: CalificacionUiState,
    onElegirEstrellas: (Int) -> Unit,
    onEscribirComentario: (String) -> Unit,
    onVotar: (platoId: String, meGusto: Boolean) -> Unit,
    onEnviar: () -> Unit,
) {
    if (estado.enviada) {
        Text(
            text = stringResource(
                if (estado.yaCalificada) R.string.calificacion_ya_enviada else R.string.calificacion_gracias
            ),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer)
                .padding(16.dp)
        )
        return
    }

    val tinta = MaterialTheme.colorScheme.onSecondaryContainer
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(R.string.calificacion_titulo),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = tinta
        )
        Text(
            text = stringResource(R.string.calificacion_subtitulo, restauranteNombre),
            style = MaterialTheme.typography.labelLarge,
            color = tinta
        )
        Estrellas(elegidas = estado.estrellas, tinta = tinta, onElegir = onElegirEstrellas)

        if (platos.isNotEmpty()) {
            Text(
                text = stringResource(R.string.calificacion_platos_titulo),
                style = MaterialTheme.typography.labelLarge,
                color = tinta,
                modifier = Modifier.padding(top = 4.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            ) {
                platos.forEachIndexed { i, plato ->
                    FilaVotoPlato(
                        plato = plato,
                        voto = estado.votos[plato.platoId],
                        onVotar = { meGusto -> onVotar(plato.platoId, meGusto) }
                    )
                    if (i < platos.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)
                }
            }
        }

        Text(
            text = stringResource(R.string.calificacion_comentario_titulo),
            style = MaterialTheme.typography.labelLarge,
            color = tinta,
            modifier = Modifier.padding(top = 4.dp)
        )
        OutlinedTextField(
            value = estado.comentario,
            onValueChange = { onEscribirComentario(it.take(Calificacion.MAX_COMENTARIO)) },
            placeholder = { Text(stringResource(R.string.calificacion_comentario_pista)) },
            minLines = 2,
            enabled = !estado.enviando,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = onEnviar,
            enabled = estado.estrellas > 0 && !estado.enviando,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            if (estado.enviando) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(stringResource(R.string.calificacion_enviar), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun Estrellas(elegidas: Int, tinta: Color, onElegir: (Int) -> Unit) {
    Row(modifier = Modifier.selectableGroup()) {
        (Calificacion.MIN_ESTRELLAS..Calificacion.MAX_ESTRELLAS).forEach { n ->
            val descripcion = pluralStringResource(R.plurals.calificacion_estrellas, n, n)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .selectable(selected = elegidas == n, role = Role.RadioButton, onClick = { onElegir(n) })
                    .semantics { contentDescription = descripcion },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (n <= elegidas) tinta else tinta.copy(alpha = 0.3f),
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    }
}

@Composable
private fun FilaVotoPlato(plato: PlatoCalificable, voto: Boolean?, onVotar: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = plato.nombre,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.calificacion_de_la_carta),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        BotonVoto(
            activo = voto == true,
            descripcion = stringResource(R.string.calificacion_me_gusto, plato.nombre),
            fondo = MaterialTheme.colorScheme.tertiaryContainer,
            tinta = MaterialTheme.colorScheme.onTertiaryContainer,
            girado = false,
            onClick = { onVotar(true) }
        )
        BotonVoto(
            activo = voto == false,
            descripcion = stringResource(R.string.calificacion_no_me_gusto, plato.nombre),
            fondo = MaterialTheme.colorScheme.errorContainer,
            tinta = MaterialTheme.colorScheme.onErrorContainer,
            girado = true,
            onClick = { onVotar(false) }
        )
    }
}

@Composable
private fun BotonVoto(
    activo: Boolean,
    descripcion: String,
    fondo: Color,
    tinta: Color,
    girado: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (activo) fondo else Color.Transparent)
            .toggleable(value = activo, role = Role.Switch, onValueChange = { onClick() })
            .semantics { contentDescription = descripcion },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.ThumbUp,
            contentDescription = null,
            tint = if (activo) tinta else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(20.dp)
                .rotate(if (girado) 180f else 0f)
        )
    }
}
