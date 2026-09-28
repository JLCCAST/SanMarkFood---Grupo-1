package com.equipo.sanmarkfood.restaurante.presentation.gestion_restaurante

/**
 * R3 y R4 se usan en dos momentos: en el alta del local (pasos 2 y 3) y después, desde «Tu negocio»,
 * para editar lo guardado (O7 «Perfil del local» y O8 «Horario»). SCRUM-160 agregará «corregir».
 */
enum class ModoFormulario {
    ALTA,
    EDITAR,
}
