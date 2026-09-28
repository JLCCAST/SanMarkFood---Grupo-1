package com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante

/**
 * R3 y R4 se usan en varios momentos: en el alta del local (pasos 2 y 3); después, desde «Tu negocio»,
 * para editar lo guardado (O7 «Perfil del local» y O8 «Horario»); y R3 también para corregir un
 * registro rechazado y reenviarlo (R7, solo en los datos del local).
 */
enum class ModoFormulario {
    ALTA,
    EDITAR,
    CORREGIR,
}
