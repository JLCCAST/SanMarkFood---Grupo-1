import { logger } from "firebase-functions";
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { notificar } from "./notificar.js";
import { clasificarResena } from "./sentimiento.js";

const CANAL_RESENAS = "resenas";

export const analizarResena = onDocumentCreated(
  { document: "restaurantes/{uid}/resenas/{pedidoId}", region: "us-central1" },
  async (evento) => {
    const resena = evento.data;
    if (!resena) return;

    const estrellas = resena.get("estrellas");
    if (!Number.isInteger(estrellas) || estrellas < 1 || estrellas > 5) return;
    const comentario = resena.get("comentario");

    const { sentimiento, motivo } = await clasificarResena(estrellas, typeof comentario === "string" ? comentario : "");
    await resena.ref.update({ sentimiento, ...(motivo !== undefined ? { motivo } : {}) });
    if (sentimiento !== "negativa") return;

    const { uid, pedidoId } = evento.params;
    const nombre = resena.get("comensalNombre");
    const comensal = typeof nombre === "string" && nombre.trim() !== "" ? nombre.trim() : "un comensal";
    const calificacion = estrellas === 1 ? "1 estrella" : `${estrellas} estrellas`;
    await notificar(uid, {
      titulo: `Reseña negativa de ${comensal}`,
      cuerpo: motivo !== undefined ? `${calificacion} · motivo detectado: ${motivo}` : calificacion,
      tipo: "resena_negativa",
      canal: CANAL_RESENAS,
      datos: { pedidoId },
    }).catch((error) => logger.error("No se pudo avisar de la reseña negativa", { uid, pedidoId, error }));
  },
);
