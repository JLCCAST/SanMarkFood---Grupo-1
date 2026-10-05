package com.equipo.sanmarkfood.restaurante.domain.usecase.menu

import com.equipo.sanmarkfood.restaurante.domain.model.menu.SeccionCarta
import com.equipo.sanmarkfood.restaurante.domain.repository.MenuRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class ObservarCartaUseCase @Inject constructor(
    private val menuRepository: MenuRepository
) {
    operator fun invoke(): Flow<List<SeccionCarta>> =
        combine(menuRepository.observarCategorias(), menuRepository.observarPlatos()) { categorias, platos ->
            categorias
                .map { categoria ->
                    SeccionCarta(
                        categoria = categoria,
                        platos = platos
                            .filter { it.datos.categoriaId == categoria.id }
                            .sortedBy { it.datos.nombre.lowercase() },
                    )
                }
                .filter { it.platos.isNotEmpty() }
        }
}
