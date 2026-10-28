import { Type, type Schema } from "@google/genai";
import { pedirJson } from "./gemini.js";

export const SENTIMIENTOS = ["positiva", "neutra", "negativa"] as const;

export type Sentimiento = (typeof SENTIMIENTOS)[number];

export interface Clasificacion {
  sentimiento: Sentimiento;
}

const OPCIONES_GEMINI = ["neutra", "negativa"] as const;

const INSTRUCCIONES =
  "Clasificas reseñas de restaurantes de comida peruana. " +
  "Cada reseña tiene 3 estrellas o menos y un comentario del comensal. " +
  "Responde negativa si el comensal se queja de algo que el local debería corregir, y neutra si no hay una queja clara.";

const ESQUEMA: Schema = {
  type: Type.OBJECT,
  properties: {
    sentimiento: { type: Type.STRING, enum: [...OPCIONES_GEMINI] },
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
  const { sentimiento } = await pedirJson<{ sentimiento: unknown }>(
    INSTRUCCIONES,
    `Estrellas: ${estrellas} de 5\nComentario: ${comentario}`,
    ESQUEMA,
  );
  const opcion = OPCIONES_GEMINI.find((valor) => valor === sentimiento);
  if (opcion === undefined) throw new Error("Gemini devolvió un sentimiento desconocido");
  return { sentimiento: opcion };
}
