package com.equipo.sanmarkfood.restaurante.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import com.equipo.sanmarkfood.restaurante.domain.model.gestion_restaurante.ErrorRestaurante
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.roundToInt

/** Lee una foto elegida en el celular y la achica antes de subirla, para cuidar la cuota de Storage. */
class LectorImagenes @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    /** Devuelve la foto en JPEG, derecha y con su lado mayor de [ladoMaximo] px como mucho. */
    suspend fun leerComoJpeg(imagen: String, ladoMaximo: Int): ByteArray = withContext(Dispatchers.IO) {
        val uri = Uri.parse(imagen)
        try {
            // Primero solo las medidas, para decodificar ya reducida y no cargar en memoria una foto de 12 MP.
            val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            abrir(uri) { BitmapFactory.decodeStream(it, null, medidas) }
            if (medidas.outWidth <= 0 || medidas.outHeight <= 0) throw ErrorRestaurante.ImagenIlegible

            var muestreo = 1
            while (max(medidas.outWidth, medidas.outHeight) / (muestreo * 2) >= ladoMaximo) muestreo *= 2
            val opciones = BitmapFactory.Options().apply { inSampleSize = muestreo }
            val leida = abrir(uri) { BitmapFactory.decodeStream(it, null, opciones) } ?: throw ErrorRestaurante.ImagenIlegible

            val final = enderezar(leida, uri).ajustar(ladoMaximo)
            ByteArrayOutputStream().use { salida ->
                final.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, salida)
                salida.toByteArray()
            }
        } catch (e: IOException) {
            throw ErrorRestaurante.ImagenIlegible
        } catch (e: SecurityException) {
            throw ErrorRestaurante.ImagenIlegible
        }
    }

    // Solo falla si no se puede abrir el archivo: lo que devuelva [leer] (que puede ser null) pasa tal cual.
    private fun <T> abrir(uri: Uri, leer: (InputStream) -> T): T {
        val entrada = context.contentResolver.openInputStream(uri) ?: throw ErrorRestaurante.ImagenIlegible
        return entrada.use(leer)
    }

    // Las fotos de la cámara guardan su giro en el EXIF; BitmapFactory lo ignora y quedarían de costado.
    // Si el formato no trae EXIF (PNG, por ejemplo) o no se puede leer, la foto se usa tal cual.
    private fun enderezar(bitmap: Bitmap, uri: Uri): Bitmap {
        val orientacion = runCatching {
            abrir(uri) {
                ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            }
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
        val grados = when (orientacion) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> return bitmap
        }
        val giro = Matrix().apply { postRotate(grados) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, giro, true)
    }

    private fun Bitmap.ajustar(ladoMaximo: Int): Bitmap {
        val escala = ladoMaximo.toFloat() / max(width, height)
        if (escala >= 1f) return this
        return Bitmap.createScaledBitmap(this, (width * escala).roundToInt(), (height * escala).roundToInt(), true)
    }

    private companion object {
        const val CALIDAD_JPEG = 85
    }
}
