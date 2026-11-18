package com.equipo.sanmarkfood.comensal.domain.usecase.pedidos

import com.equipo.sanmarkfood.comensal.domain.model.pedidos.SeccionCarta
import com.equipo.sanmarkfood.comensal.domain.repository.CartaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class ObservarCartaUseCase @Inject constructor(
    private val cartaRepository: CartaRepository,
) {
    operator fun invoke(restauranteId: String): Flow<List<SeccionCarta>> =
        combine(
            cartaRepository.observarCategorias(restauranteId),
            cartaRepository.observarPlatos(restauranteId),
        ) { categorias, platos ->
            categorias
                .map { categoria ->
                    SeccionCarta(
                        categoria = categoria,
                        platos = platos
                            .filter { it.categoriaId == categoria.id }
                            .sortedBy { it.nombre.lowercase() },
                    )
                }
                .filter { it.platos.isNotEmpty() }
        }
}