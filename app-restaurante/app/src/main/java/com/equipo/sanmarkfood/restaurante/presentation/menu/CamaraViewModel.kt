package com.equipo.sanmarkfood.restaurante.presentation.menu

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.equipo.sanmarkfood.restaurante.core.navigation.Camara
import com.equipo.sanmarkfood.restaurante.domain.model.menu.ErrorMenu
import com.equipo.sanmarkfood.restaurante.domain.usecase.menu.LeerCartaUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.menu.LeerPizarraUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CamaraUiState(
    val modo: ModoCamara = ModoCamara.PIZARRA,
    val tomando: Boolean = false,
    val leyendo: Boolean = false,
    val listo: Boolean = false,
    val platosLeidos: Int = 0,
    val error: ErrorMenu? = null,
)

@HiltViewModel
class CamaraViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val leerPizarra: LeerPizarraUseCase,
    private val leerCarta: LeerCartaUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CamaraUiState(modo = savedStateHandle.toRoute<Camara>().modo))
    val uiState: StateFlow<CamaraUiState> = _uiState.asStateFlow()

    fun onCambiarModo(modo: ModoCamara) = _uiState.update {
        if (it.tomando || it.leyendo) it else it.copy(modo = modo, listo = false, error = null)
    }

    fun onTomarFoto() = _uiState.update { it.copy(tomando = true, error = null) }

    fun onErrorFoto() = _uiState.update { it.copy(tomando = false, error = ErrorMenu.Desconocido) }

    fun onFotoLista(foto: String) {
        val modo = _uiState.value.modo
        _uiState.update { it.copy(tomando = false, leyendo = true, error = null) }
        viewModelScope.launch {
            try {
                val platos = when (modo) {
                    ModoCamara.CARTA -> leerCarta(foto)
                    ModoCamara.PIZARRA -> {
                        leerPizarra(foto)
                        0
                    }
                }
                _uiState.update { it.copy(leyendo = false, listo = true, platosLeidos = platos) }
            } catch (e: ErrorMenu) {
                _uiState.update { it.copy(leyendo = false, error = e) }
            }
        }
    }
}
