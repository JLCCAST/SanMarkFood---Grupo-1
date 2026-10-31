package com.equipo.sanmarkfood.comensal.avisos

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.equipo.sanmarkfood.comensal.MainActivity
import com.equipo.sanmarkfood.comensal.R

fun crearCanalesDeAvisos(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val pedidos = NotificationChannel(
        context.getString(R.string.canal_pedidos_id),
        context.getString(R.string.canal_pedidos),
        NotificationManager.IMPORTANCE_HIGH
    ).apply { description = context.getString(R.string.canal_pedidos_descripcion) }
    val reservas = NotificationChannel(
        context.getString(R.string.canal_reservas_id),
        context.getString(R.string.canal_reservas),
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply { description = context.getString(R.string.canal_reservas_descripcion) }
    context.getSystemService(NotificationManager::class.java)
        .createNotificationChannels(listOf(pedidos, reservas))
}

fun puedeMostrarAvisos(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

fun mostrarAviso(context: Context, canal: String, id: Int, titulo: String?, texto: String?) {
    if (!puedeMostrarAvisos(context)) return
    val abrirApp = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    val aviso = NotificationCompat.Builder(context, canal)
        .setSmallIcon(R.drawable.ic_aviso)
        .setContentTitle(titulo)
        .setContentText(texto)
        .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
        .setContentIntent(abrirApp)
        .setAutoCancel(true)
        .build()
    NotificationManagerCompat.from(context).notify(id, aviso)
}
