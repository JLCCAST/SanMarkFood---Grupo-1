package com.equipo.sanmarkfood.restaurante.domain.usecase.menu

import com.equipo.sanmarkfood.restaurante.domain.model.menu.Categoria
import com.equipo.sanmarkfood.restaurante.domain.repository.MenuRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservarCategoriasUseCase @Inject constructor(
    private val menuRepository: MenuRepository
) {
    operator fun invoke(): Flow<List<Categoria>> = menuRepository.observarCategorias()
}
