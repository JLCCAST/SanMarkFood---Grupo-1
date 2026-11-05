package com.equipo.sanmarkfood.restaurante.avisos

import android.Manifest
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.equipo.sanmarkfood.restaurante.MainActivity
import com.equipo.sanmarkfood.restaurante.R

private const val ID_AVISO_ESTADO_LOCAL = 1
private const val ID_AVISO_PEDIDO_NUEVO = 2

fun crearCanalesDeAvisos(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val canales = listOf(
        canal(
            context,
            R.string.canal_estado_local_id,
            R.string.canal_estado_local,
            R.string.canal_estado_local_descripcion,
            NotificationManager.IMPORTANCE_DEFAULT,
        ),
        canal(
            context,
            R.string.canal_pedidos_nuevos_id,
            R.string.canal_pedidos_nuevos,
            R.string.canal_pedidos_nuevos_descripcion,
            NotificationManager.IMPORTANCE_HIGH,
        ),
        canal(
            context,
            R.string.canal_pedidos_id,
            R.string.canal_pedidos,
            R.string.canal_pedidos_descripcion,
            NotificationManager.IMPORTANCE_DEFAULT,
        ),
        canal(
            context,
            R.string.canal_reservas_id,
            R.string.canal_reservas,
            R.string.canal_reservas_descripcion,
            NotificationManager.IMPORTANCE_DEFAULT,
        ),
    )
    context.getSystemService(NotificationManager::class.java).createNotificationChannels(canales)
}

private fun canal(
    context: Context,
    @StringRes id: Int,
    @StringRes nombre: Int,
    @StringRes descripcion: Int,
    importancia: Int,
): NotificationChannel =
    NotificationChannel(context.getString(id), context.getString(nombre), importancia)
        .apply { description = context.getString(descripcion) }

fun puedeMostrarAvisos(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

fun mostrarAvisoEstadoLocal(context: Context, titulo: String?, texto: String?) =
    mostrarAviso(context, context.getString(R.string.canal_estado_local_id), ID_AVISO_ESTADO_LOCAL, titulo, texto)

fun mostrarAvisoPedidoNuevo(context: Context, titulo: String?, texto: String?) = mostrarAviso(
    context = context,
    canal = context.getString(R.string.canal_pedidos_nuevos_id),
    id = ID_AVISO_PEDIDO_NUEVO,
    titulo = titulo,
    texto = texto,
    insistente = !appAlFrente(),
)

private fun appAlFrente(): Boolean =
    ActivityManager.RunningAppProcessInfo()
        .also { ActivityManager.getMyMemoryState(it) }
        .importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND

fun cancelarAvisoPedidoNuevo(context: Context) = NotificationManagerCompat.from(context).cancel(ID_AVISO_PEDIDO_NUEVO)

fun mostrarAviso(
    context: Context,
    canal: String,
    id: Int,
    titulo: String?,
    texto: String?,
    insistente: Boolean = false,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED
    ) {
        return
    }
    val abrirApp = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
    val aviso = NotificationCompat.Builder(context, canal)
        .setSmallIcon(R.drawable.ic_aviso)
        .setContentTitle(titulo)
        .setContentText(texto)
        .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
        .setContentIntent(abrirApp)
        .setAutoCancel(true)
        .setPriority(if (insistente) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_DEFAULT)
        .build()
    if (insistente) aviso.flags = aviso.flags or Notification.FLAG_INSISTENT
    NotificationManagerCompat.from(context).notify(id, aviso)
}
