import type { DocumentReference } from "firebase-admin/firestore";

const MAX_RESENAS = 30;

export async function leerComentariosRecientes(localRef: DocumentReference): Promise<string[]> {
  const resenas = await localRef.collection("resenas").orderBy("creadoEn", "desc").limit(MAX_RESENAS).get();
  return resenas.docs
    .map((resena) => resena.get("comentario"))
    .filter((comentario): comentario is string => typeof comentario === "string" && comentario.trim() !== "")
    .map((comentario) => comentario.trim());
}
