import { FieldValue, getFirestore } from "firebase-admin/firestore";
import { getMessaging } from "firebase-admin/messaging";
import { logger } from "firebase-functions";

export interface Aviso {
  titulo: string;
  cuerpo: string;
  tipo: string;
  canal: string;
  datos?: Record<string, string>;
  guardarEnBandeja?: boolean;
}

const ERRORES_DE_TOKEN = new Set([
  "messaging/registration-token-not-registered",
  "messaging/invalid-registration-token",
  "messaging/invalid-argument",
]);

export async function notificar(uid: string, aviso: Aviso): Promise<void> {
  const usuario = getFirestore().collection("usuarios").doc(uid);

  if (aviso.guardarEnBandeja === true) {
    await usuario.collection("notificaciones").add({
      titulo: aviso.titulo,
      cuerpo: aviso.cuerpo,
      tipo: aviso.tipo,
      datos: aviso.datos ?? {},
      leida: false,
      creadoEn: FieldValue.serverTimestamp(),
    });
  }

  const dispositivos = await usuario.collection("dispositivos").get();
  if (dispositivos.empty) {
    logger.info("El usuario no tiene celulares registrados", { uid, tipo: aviso.tipo });
    return;
  }

  const respuesta = await getMessaging().sendEachForMulticast({
    tokens: dispositivos.docs.map((dispositivo) => dispositivo.id),
    notification: { title: aviso.titulo, body: aviso.cuerpo },
    data: { tipo: aviso.tipo, ...aviso.datos },
    android: {
      priority: "high",
      notification: { channelId: aviso.canal },
    },
  });

  const vencidos = dispositivos.docs.filter((_, indice) => {
    const error = respuesta.responses[indice]?.error;
    return error !== undefined && ERRORES_DE_TOKEN.has(error.code);
  });
  await Promise.all(vencidos.map((dispositivo) => dispositivo.ref.delete()));

  logger.info("Aviso enviado", {
    uid,
    tipo: aviso.tipo,
    enviados: respuesta.successCount,
    fallidos: respuesta.failureCount,
    borrados: vencidos.length,
  });
}
