package com.equipo.sanmarkfood.comensal.domain.model.auth

/** La cuenta existe, pero su rol en usuarios/{uid} no es "comensal" (D5). */
class CuentaDeOtroRolException : Exception("La cuenta no es de comensal")