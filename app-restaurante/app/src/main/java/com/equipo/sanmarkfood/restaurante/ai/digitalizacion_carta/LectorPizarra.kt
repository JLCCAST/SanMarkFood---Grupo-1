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
data class MenuLeidoDto(
    val precio: Double? = null,
    val entradas: List<String> = emptyList(),
    val segundos: List<String> = emptyList(),
    val refresco: String? = null,
    val postre: String? = null,
)

class LectorPizarra @Inject constructor() {

    private val modelo = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
        modelName = "gemini-3.5-flash",
        generationConfig = generationConfig {
            responseMimeType = "application/json"
            responseSchema = esquemaMenu
            temperature = 0f
        },
    )

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun leer(jpeg: ByteArray): MenuLeidoDto {
        val respuesta = modelo.generateContent(
            content {
                inlineData(jpeg, "image/jpeg")
                text(INSTRUCCIONES)
            }
        )
        val texto = respuesta.text ?: return MenuLeidoDto()
        return json.decodeFromString<MenuLeidoDto>(texto)
    }
}

private val esquemaMenu = Schema.obj(
    mapOf(
        "precio" to Schema.double("Precio del menú completo en soles, solo el número"),
        "entradas" to Schema.array(Schema.string()),
        "segundos" to Schema.array(Schema.string()),
        "refresco" to Schema.string(),
        "postre" to Schema.string(),
    ),
    optionalProperties = listOf("precio", "refresco", "postre"),
)

private const val INSTRUCCIONES =
    "Esta foto es la pizarra del menú del día de un restaurante peruano. " +
        "Lee el precio del menú, las entradas, los segundos y, si los incluye, el refresco y el postre. " +
        "Copia cada plato completo, como está escrito, con mayúscula solo al inicio. " +
        "No inventes platos ni precios: si algo no aparece en la pizarra, déjalo fuera."
