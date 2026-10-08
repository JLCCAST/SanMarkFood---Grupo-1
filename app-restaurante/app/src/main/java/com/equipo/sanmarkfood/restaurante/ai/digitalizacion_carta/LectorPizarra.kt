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
    val precioDudoso: Boolean = false,
    val entradas: List<OpcionLeidaDto> = emptyList(),
    val segundos: List<OpcionLeidaDto> = emptyList(),
    val refresco: String? = null,
    val postre: String? = null,
)

@Serializable
data class OpcionLeidaDto(
    val nombre: String,
    val dudoso: Boolean = false,
)

class LectorPizarra @Inject constructor() {

    private val modelo = Firebase.ai(backend = GenerativeBackend.agentPlatform()).generativeModel(
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

private val esquemaOpcion = Schema.obj(
    mapOf(
        "nombre" to Schema.string(),
        "dudoso" to Schema.boolean(),
    ),
)

private val esquemaMenu = Schema.obj(
    mapOf(
        "precio" to Schema.double("Precio del menú completo en soles, solo el número"),
        "precioDudoso" to Schema.boolean(),
        "entradas" to Schema.array(esquemaOpcion),
        "segundos" to Schema.array(esquemaOpcion),
        "refresco" to Schema.string(),
        "postre" to Schema.string(),
    ),
    optionalProperties = listOf("precio", "precioDudoso", "refresco", "postre"),
)

private const val INSTRUCCIONES =
    "Esta foto es la pizarra del menú del día de un restaurante peruano. " +
        "Lee el precio del menú, las entradas, los segundos y, si los incluye, el refresco y el postre. " +
        "Copia cada plato completo, como está escrito, con mayúscula solo al inicio. " +
        "No inventes platos ni precios: si algo no aparece en la pizarra, déjalo fuera. " +
        "Marca dudoso un plato, y precioDudoso el precio, si la letra está borrosa, cortada o tapada, " +
        "o si no estás seguro de haberlo leído bien."
