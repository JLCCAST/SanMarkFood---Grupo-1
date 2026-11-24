package com.equipo.sanmarkfood.restaurante.ai.digitalizacion_carta

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

@Serializable
data class CartaLeidaDto(
    val platos: List<PlatoLeidoDto> = emptyList(),
)

@Serializable
data class PlatoLeidoDto(
    val nombre: String,
    val precio: Double? = null,
    val categoria: String = "",
    val nombreDudoso: Boolean = false,
    val precioDudoso: Boolean = false,
)

class LectorCarta @Inject constructor() {

    private val modelo = Firebase.ai(backend = GenerativeBackend.agentPlatform()).generativeModel(
        modelName = "gemini-3.5-flash",
        generationConfig = generationConfig {
            responseMimeType = "application/json"
            responseSchema = esquemaCarta
            temperature = 0f
        },
    )

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun leer(jpeg: ByteArray): CartaLeidaDto {
        val respuesta = modelo.generateContent(
            content {
                inlineData(jpeg, "image/jpeg")
                text(INSTRUCCIONES)
            }
        )
        val texto = respuesta.text ?: return CartaLeidaDto()
        return json.decodeFromString<CartaLeidaDto>(texto)
    }
}

private val esquemaPlato = Schema.obj(
    mapOf(
        "nombre" to Schema.string(),
        "precio" to Schema.double("Precio del plato en soles, solo el número"),
        "categoria" to Schema.string("Sección de la carta donde aparece el plato"),
        "nombreDudoso" to Schema.boolean(),
        "precioDudoso" to Schema.boolean(),
    ),
    optionalProperties = listOf("precio", "nombreDudoso", "precioDudoso"),
)

private val esquemaCarta = Schema.obj(
    mapOf("platos" to Schema.array(esquemaPlato)),
)

private const val INSTRUCCIONES =
    "Esta foto es la carta impresa de un restaurante peruano. " +
        "Lee cada plato o bebida con su precio en soles y la sección de la carta donde aparece, " +
        "por ejemplo Entradas, Platos de fondo, Bebidas o Postres. Si la carta no tiene secciones, usa Platos. " +
        "Copia cada nombre como está escrito, con mayúscula solo al inicio, sin el precio ni la descripción. " +
        "No inventes platos ni precios: si algo no aparece en la carta, déjalo fuera. " +
        "Si un plato tiene varios tamaños o precios, usa el primero. " +
        "Marca nombreDudoso o precioDudoso si la letra está borrosa, cortada o tapada, " +
        "o si no estás seguro de haberlo leído bien."
