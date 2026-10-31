import { FieldValue, getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { onSchedule } from "firebase-functions/v2/scheduler";
import { leerComentariosRecientes } from "./resenasRecientes.js";
import { generarResumen } from "./resumen.js";

const MIN_COMENTARIOS = 3;

export const resumenResenas = onSchedule(
  { schedule: "0 3 * * *", timeZone: "America/Lima", region: "us-central1", timeoutSeconds: 540 },
  async () => {
    const locales = await getFirestore().collection("restaurantes").where("estado", "==", "aprobado").get();

    for (const local of locales.docs) {
      try {
        const comentarios = await leerComentariosRecientes(local.ref);
        if (comentarios.length < MIN_COMENTARIOS) continue;
        const texto = await generarResumen(comentarios);
        await local.ref.update({ resumenResenas: { texto, generadoEn: FieldValue.serverTimestamp() } });
      } catch (error) {
        logger.error("No se pudo generar el resumen de reseñas", { uid: local.id, error });
      }
    }
  },
);
