package com.equipo.sanmarkfood.restaurante.presentation.admin.datos_iniciales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.restaurante.domain.model.admin.DatosBorrados
import com.equipo.sanmarkfood.restaurante.domain.model.admin.DatosCargados
import com.equipo.sanmarkfood.restaurante.domain.model.admin.ErrorDatosIniciales
import com.equipo.sanmarkfood.restaurante.domain.usecase.admin.BorrarDatosInicialesUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.admin.CargarDatosInicialesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AccionDatos { CARGAR, BORRAR }

data class DatosInicialesUiState(
    val confirmando: AccionDatos? = null,
    val trabajando: AccionDatos? = null,
    val cargados: DatosCargados? = null,
    val borrados: DatosBorrados? = null,
    val error: ErrorDatosIniciales? = null,
)

@HiltViewModel
class DatosInicialesViewModel @Inject constructor(
    private val cargarDatosIniciales: CargarDatosInicialesUseCase,
    private val borrarDatosIniciales: BorrarDatosInicialesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DatosInicialesUiState())
    val uiState: StateFlow<DatosInicialesUiState> = _uiState.asStateFlow()

    fun onPedir(accion: AccionDatos) {
        if (_uiState.value.trabajando != null) return
        _uiState.update { it.copy(confirmando = accion) }
    }

    fun onCancelar() {
        _uiState.update { it.copy(confirmando = null) }
    }

    fun onConfirmar() {
        val accion = _uiState.value.confirmando ?: return
        _uiState.update { it.copy(confirmando = null, trabajando = accion, error = null) }
        viewModelScope.launch {
            try {
                when (accion) {
                    AccionDatos.CARGAR -> {
                        val cargados = cargarDatosIniciales()
                        _uiState.update { it.copy(trabajando = null, cargados = cargados, borrados = null) }
                    }
                    AccionDatos.BORRAR -> {
                        val borrados = borrarDatosIniciales()
                        _uiState.update { it.copy(trabajando = null, borrados = borrados, cargados = null) }
                    }
                }
            } catch (e: ErrorDatosIniciales) {
                _uiState.update { it.copy(trabajando = null, error = e) }
            }
        }
    }
}
