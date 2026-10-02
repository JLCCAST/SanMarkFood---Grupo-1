package com.equipo.sanmarkfood.comensal.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.equipo.sanmarkfood.comensal.ui.perfil.PerfilScreen

private enum class Pestana(val titulo: String, val icono: ImageVector) {
    EXPLORAR("Explorar", Icons.Filled.Place),
    ACTIVIDAD("Actividad", Icons.Filled.DateRange),
    PERFIL("Perfil", Icons.Filled.Person)
}

@Composable
fun HomeScaffold(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    var seleccionada by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            Column {
                // Línea fina que separa el contenido de la barra, como en el Figma.
                HorizontalDivider(color = colors.outlineVariant)
                NavigationBar(containerColor = colors.surface) {
                    Pestana.entries.forEachIndexed { index, pestana ->
                        NavigationBarItem(
                            selected = seleccionada == index,
                            onClick = { seleccionada = index },
                            icon = { Icon(pestana.icono, contentDescription = pestana.titulo) },
                            label = { Text(pestana.titulo) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = colors.primary,
                                selectedTextColor = colors.primary,
                                indicatorColor = colors.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        when (Pestana.entries[seleccionada]) {
            Pestana.EXPLORAR -> PestanaProvisional(
                titulo = "Explorar",
                texto = "Pronto verás aquí el mapa, los menús del día y las reseñas de los restaurantes.",
                modifier = Modifier.padding(innerPadding)
            )

            Pestana.ACTIVIDAD -> PestanaProvisional(
                titulo = "Actividad",
                texto = "Pronto verás aquí tus pedidos y reservas.",
                modifier = Modifier.padding(innerPadding)
            )

            Pestana.PERFIL -> PerfilScreen(
                onCerrarSesion = onLogout,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

// Temporal: se reemplazará cuando existan las pantallas de otras historias.
@Composable
private fun PestanaProvisional(
    titulo: String,
    texto: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}