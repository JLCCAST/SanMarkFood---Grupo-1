import { onDocumentUpdated } from "firebase-functions/v2/firestore";
import { armarAviso } from "./avisos.js";
import { notificar } from "./notificar.js";

const CANAL_ESTADO_LOCAL = "estado_local";

export const revisarLocal = onDocumentUpdated(
  { document: "restaurantes/{uid}", region: "us-central1" },
  async (evento) => {
    const antes = evento.data?.before.data();
    const despues = evento.data?.after.data();
    if (!antes || !despues) return;
    if (antes.estado !== "pendiente") return;
    if (despues.estado !== "aprobado" && despues.estado !== "rechazado") return;

    const aviso = armarAviso(despues);
    await notificar(evento.params.uid, {
      titulo: aviso.title ?? "",
      cuerpo: aviso.body ?? "",
      tipo: "revision",
      canal: CANAL_ESTADO_LOCAL,
      datos: { estado: despues.estado },
    });
  },
);
