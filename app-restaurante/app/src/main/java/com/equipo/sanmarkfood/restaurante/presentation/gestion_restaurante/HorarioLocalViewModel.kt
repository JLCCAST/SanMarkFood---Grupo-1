package com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.equipo.sanmarkfood.restaurante.domain.model.DiaSemana
import com.equipo.sanmarkfood.restaurante.domain.model.ErrorRestaurante
import com.equipo.sanmarkfood.restaurante.domain.model.Hora
import com.equipo.sanmarkfood.restaurante.domain.model.Horario
import com.equipo.sanmarkfood.restaurante.domain.model.HorarioDia
import com.equipo.sanmarkfood.restaurante.domain.usecase.EnviarARevisionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HorarioLocalUiState(
    val dias: Map<DiaSemana, HorarioDia> = Horario.PorDefecto.dias,
    val diaEditando: DiaSemana? = null,
    val diasInvalidos: Set<DiaSemana> = emptySet(),
    val enviando: Boolean = false,
    val error: ErrorRestaurante? = null,
    val enviado: Boolean = false,
)

@HiltViewModel
class HorarioLocalViewModel @Inject constructor(
    private val enviarARevision: EnviarARevisionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HorarioLocalUiState())
    val uiState: StateFlow<HorarioLocalUiState> = _uiState.asStateFlow()

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

    fun onEnviarARevision() {
        val estado = _uiState.value
        if (estado.enviando) return

        _uiState.update { it.copy(enviando = true, error = null) }
        viewModelScope.launch {
            try {
                enviarARevision(Horario(estado.dias))
                _uiState.update { it.copy(enviando = false, enviado = true) }
            } catch (e: ErrorRestaurante.HorasInvalidas) {
                _uiState.update { it.copy(enviando = false, diasInvalidos = e.dias, error = e) }
            } catch (e: ErrorRestaurante) {
                _uiState.update { it.copy(enviando = false, error = e) }
            }
        }
    }

    fun onEnvioAtendido() = _uiState.update { it.copy(enviado = false) }

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
