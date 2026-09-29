package com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.equipo.sanmarkfood.restaurante.core.navigation.HorarioLocal
import com.equipo.sanmarkfood.restaurante.domain.model.DiaSemana
import com.equipo.sanmarkfood.restaurante.domain.model.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.Hora
import com.equipo.sanmarkfood.restaurante.domain.model.Horario
import com.equipo.sanmarkfood.restaurante.domain.model.HorarioDia
import com.equipo.sanmarkfood.restaurante.domain.usecase.EnviarARevisionUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.GuardarHorarioUseCase
import com.equipo.sanmarkfood.restaurante.domain.usecase.ObtenerRestauranteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HorarioLocalUiState(
    val modo: ModoFormulario = ModoFormulario.ALTA,
    val cargando: Boolean = false,
    val errorCarga: ErrorRestaurante? = null,
    val dias: Map<DiaSemana, HorarioDia> = Horario.PorDefecto.dias,
    val diaEditando: DiaSemana? = null,
    val diasInvalidos: Set<DiaSemana> = emptySet(),
    val guardando: Boolean = false,
    val error: ErrorRestaurante? = null,
    val guardado: Boolean = false,
    val confirmandoDescarte: Boolean = false,
    val salir: Boolean = false,
)

@HiltViewModel
class HorarioLocalViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val obtenerRestaurante: ObtenerRestauranteUseCase,
    private val enviarARevision: EnviarARevisionUseCase,
    private val guardarHorario: GuardarHorarioUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HorarioLocalUiState(modo = savedStateHandle.toRoute<HorarioLocal>().modo)
    )
    val uiState: StateFlow<HorarioLocalUiState> = _uiState.asStateFlow()

    // El horario guardado al abrir O8, para saber si al salir hay cambios que se perderían.
    private var diasGuardados: Map<DiaSemana, HorarioDia>? = null

    init {
        // En el alta el local todavía no tiene horario: se empieza con el del prototipo.
        if (_uiState.value.modo == ModoFormulario.EDITAR) cargarHorario()
    }

    fun onReintentarCarga() = cargarHorario()

    fun onAlternarDia(dia: DiaSemana) = cambiarDia(dia) { it.copy(abierto = !it.abierto) }

    fun onEditarHoras(dia: DiaSemana) = _uiState.update { it.copy(diaEditando = dia) }

    fun onCancelarEdicion() = _uiState.update { it.copy(diaEditando = null) }

    fun onHorasElegidas(abre: Hora, cierra: Hora) {
        val dia = _uiState.value.diaEditando ?: return
        cambiarDia(dia) { it.copy(abre = abre, cierra = cierra) }
        _uiState.update { it.copy(diaEditando = null) }
    }

    // Como en el prototipo, copia solo las horas: cada día conserva si abre o no.
    fun onCopiarLunesATodos() {
        _uiState.update { estado ->
            val lunes = estado.dias.getValue(DiaSemana.LUNES)
            estado.copy(
                dias = estado.dias.mapValues { (_, dia) -> dia.copy(abre = lunes.abre, cierra = lunes.cierra) },
                diasInvalidos = emptySet(),
                error = estado.error.takeUnless { it is ErrorRestaurante.HorasInvalidas },
            )
        }
    }

    // En el alta envía el local a revisión (R4); al editar solo guarda el horario (O8).
    fun onGuardar() {
        val estado = _uiState.value
        if (estado.guardando) return

        _uiState.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            try {
                val horario = Horario(estado.dias)
                when (estado.modo) {
                    ModoFormulario.ALTA -> enviarARevision(horario)
                    // R4 no se abre para corregir (R7 es solo de los datos): el horario se corrige en O8.
                    ModoFormulario.EDITAR, ModoFormulario.CORREGIR -> guardarHorario(horario)
                }
                _uiState.update { it.copy(guardando = false, guardado = true) }
            } catch (e: ErrorRestaurante.HorasInvalidas) {
                _uiState.update { it.copy(guardando = false, diasInvalidos = e.dias, error = e) }
            } catch (e: ErrorRestaurante) {
                _uiState.update { it.copy(guardando = false, error = e) }
            }
        }
    }

    fun onGuardadoAtendido() = _uiState.update { it.copy(guardado = false) }

    // En el alta se vuelve a R3 sin preguntar, como en el prototipo. En O8 se pregunta si hay cambios.
    fun onVolver() {
        val estado = _uiState.value
        val hayCambios = estado.modo == ModoFormulario.EDITAR && !estado.cargando && estado.dias != diasGuardados
        _uiState.update { if (hayCambios) it.copy(confirmandoDescarte = true) else it.copy(salir = true) }
    }

    fun onSeguirEditando() = _uiState.update { it.copy(confirmandoDescarte = false) }

    fun onDescartarCambios() = _uiState.update { it.copy(confirmandoDescarte = false, salir = true) }

    private fun cargarHorario() {
        _uiState.update { it.copy(cargando = true, errorCarga = null) }
        viewModelScope.launch {
            try {
                val dias = (obtenerRestaurante()?.horario ?: Horario.PorDefecto).dias
                diasGuardados = dias
                _uiState.update { it.copy(cargando = false, dias = dias) }
            } catch (e: ErrorRestaurante) {
                _uiState.update { it.copy(cargando = false, errorCarga = e) }
            }
        }
    }

    // Al cambiar un día se le quita la marca de error, y se borra el aviso general si ya no aplica.
    private fun cambiarDia(dia: DiaSemana, cambio: (HorarioDia) -> HorarioDia) {
        _uiState.update { estado ->
            val dias = estado.dias + (dia to cambio(estado.dias.getValue(dia)))
            val invalidos = estado.diasInvalidos - dia
            val error = when (estado.error) {
                ErrorRestaurante.NingunDiaAbierto -> estado.error.takeIf { dias.values.none { it.abierto } }
                is ErrorRestaurante.HorasInvalidas -> estado.error.takeIf { invalidos.isNotEmpty() }
                else -> estado.error
            }
            estado.copy(dias = dias, diasInvalidos = invalidos, error = error)
        }
    }
}
