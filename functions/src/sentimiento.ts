import { Type, type Schema } from "@google/genai";
import { pedirJson } from "./gemini.js";
import { limitarMotivo } from "./motivo.js";

export const SENTIMIENTOS = ["positiva", "neutra", "negativa"] as const;

export type Sentimiento = (typeof SENTIMIENTOS)[number];

export interface Clasificacion {
  sentimiento: Sentimiento;
  motivo?: string;
}

const OPCIONES_GEMINI = ["neutra", "negativa"] as const;

const INSTRUCCIONES =
  "Clasificas reseñas de restaurantes de comida peruana. " +
  "Cada reseña tiene 3 estrellas o menos y un comentario del comensal. " +
  "Responde negativa si el comensal se queja de algo que el local debería corregir, y neutra si no hay una queja clara. " +
  "Si es negativa, escribe en motivo la queja principal como una frase corta de hasta seis palabras, " +
  "en minúsculas y sin punto final, por ejemplo: llegó frío y faltó la bebida. " +
  "Si es neutra, deja motivo vacío.";

const ESQUEMA: Schema = {
  type: Type.OBJECT,
  properties: {
    sentimiento: { type: Type.STRING, enum: [...OPCIONES_GEMINI] },
    motivo: { type: Type.STRING },
  },
  required: ["sentimiento"],
};

export async function clasificarResena(estrellas: number, comentario: string): Promise<Clasificacion> {
  const texto = comentario.trim();
  if (estrellas >= 4 || texto === "") return { sentimiento: sentimientoPorEstrellas(estrellas) };
  return clasificarConGemini(estrellas, texto);
}

function sentimientoPorEstrellas(estrellas: number): Sentimiento {
  if (estrellas >= 4) return "positiva";
  return estrellas === 3 ? "neutra" : "negativa";
}

async function clasificarConGemini(estrellas: number, comentario: string): Promise<Clasificacion> {
  const respuesta = await pedirJson<{ sentimiento: unknown; motivo?: unknown }>(
    INSTRUCCIONES,
    `Estrellas: ${estrellas} de 5\nComentario: ${comentario}`,
    ESQUEMA,
  );
  const opcion = OPCIONES_GEMINI.find((valor) => valor === respuesta.sentimiento);
  if (opcion === undefined) throw new Error("Gemini devolvió un sentimiento desconocido");
  const motivo =
    opcion === "negativa" && typeof respuesta.motivo === "string" ? limitarMotivo(respuesta.motivo) : undefined;
  return { sentimiento: opcion, motivo };
}
