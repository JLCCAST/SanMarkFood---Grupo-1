import { getFirestore } from "firebase-admin/firestore";
import { getMessaging } from "firebase-admin/messaging";
import { logger } from "firebase-functions";
import { onDocumentUpdated } from "firebase-functions/v2/firestore";
import { armarAviso } from "./avisos.js";

const CANAL_ESTADO_LOCAL = "estado_local";

const ERRORES_DE_TOKEN = new Set([
  "messaging/registration-token-not-registered",
  "messaging/invalid-registration-token",
  "messaging/invalid-argument",
]);

export const revisarLocal = onDocumentUpdated(
  { document: "restaurantes/{uid}", region: "us-central1" },
  async (evento) => {
    const antes = evento.data?.before.data();
    const despues = evento.data?.after.data();
    if (!antes || !despues) return;
    if (antes.estado !== "pendiente") return;
    if (despues.estado !== "aprobado" && despues.estado !== "rechazado") return;

    const uid = evento.params.uid;
    const dispositivos = await getFirestore().collection("usuarios").doc(uid).collection("dispositivos").get();
    if (dispositivos.empty) {
      logger.info("El local no tiene celulares registrados", { uid, estado: despues.estado });
      return;
    }

    const respuesta = await getMessaging().sendEachForMulticast({
      tokens: dispositivos.docs.map((dispositivo) => dispositivo.id),
      notification: armarAviso(despues),
      data: { tipo: "revision", estado: despues.estado },
      android: {
        priority: "high",
        notification: { channelId: CANAL_ESTADO_LOCAL },
      },
    });

    const vencidos = dispositivos.docs.filter((_, indice) => {
      const error = respuesta.responses[indice]?.error;
      return error !== undefined && ERRORES_DE_TOKEN.has(error.code);
    });
    await Promise.all(vencidos.map((dispositivo) => dispositivo.ref.delete()));

    logger.info("Aviso de revisión enviado", {
      uid,
      estado: despues.estado,
      enviados: respuesta.successCount,
      fallidos: respuesta.failureCount,
      borrados: vencidos.length,
    });
  },
);
