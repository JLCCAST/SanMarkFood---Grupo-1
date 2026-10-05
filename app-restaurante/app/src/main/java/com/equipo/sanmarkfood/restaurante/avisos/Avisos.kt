package com.equipo.sanmarkfood.restaurante.avisos

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
import com.equipo.sanmarkfood.restaurante.MainActivity
import com.equipo.sanmarkfood.restaurante.R

private const val ID_AVISO_ESTADO_LOCAL = 1

fun crearCanalesDeAvisos(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val canal = NotificationChannel(
        context.getString(R.string.canal_estado_local_id),
        context.getString(R.string.canal_estado_local),
        NotificationManager.IMPORTANCE_DEFAULT,
    ).apply { description = context.getString(R.string.canal_estado_local_descripcion) }
    context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
}

fun puedeMostrarAvisos(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

fun mostrarAvisoEstadoLocal(context: Context, titulo: String?, texto: String?) {
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
    val aviso = NotificationCompat.Builder(context, context.getString(R.string.canal_estado_local_id))
        .setSmallIcon(R.drawable.ic_aviso)
        .setContentTitle(titulo)
        .setContentText(texto)
        .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
        .setContentIntent(abrirApp)
        .setAutoCancel(true)
        .build()
    NotificationManagerCompat.from(context).notify(ID_AVISO_ESTADO_LOCAL, aviso)
}
