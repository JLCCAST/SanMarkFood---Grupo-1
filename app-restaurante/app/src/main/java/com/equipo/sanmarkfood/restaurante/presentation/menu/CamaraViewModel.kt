package com.equipo.sanmarkfood.restaurante.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.equipo.sanmarkfood.restaurante.domain.usecase.menu.LeerPizarraUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CamaraUiState(
    val tomando: Boolean = false,
    val leyendo: Boolean = false,
    val listo: Boolean = false,
    val error: ErrorMenu? = null,
)

@HiltViewModel
class CamaraViewModel @Inject constructor(
    private val leerPizarra: LeerPizarraUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CamaraUiState())
    val uiState: StateFlow<CamaraUiState> = _uiState.asStateFlow()

    fun onTomarFoto() = _uiState.update { it.copy(tomando = true, error = null) }

    fun onErrorFoto() = _uiState.update { it.copy(tomando = false, error = ErrorMenu.Desconocido) }

    fun onFotoLista(foto: String) {
        _uiState.update { it.copy(tomando = false, leyendo = true, error = null) }
        viewModelScope.launch {
            try {
                leerPizarra(foto)
                _uiState.update { it.copy(leyendo = false, listo = true) }
            } catch (e: ErrorMenu) {
                _uiState.update { it.copy(leyendo = false, error = e) }
            }
        }
    }
}
