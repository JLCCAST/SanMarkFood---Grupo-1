import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { clasificarResena } from "./sentimiento.js";

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
  },
);
