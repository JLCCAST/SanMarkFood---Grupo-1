import { Type, type Schema } from "@google/genai";
import { pedirJson } from "./gemini.js";

export const SENTIMIENTOS = ["positiva", "neutra", "negativa"] as const;

export type Sentimiento = (typeof SENTIMIENTOS)[number];

const INSTRUCCIONES =
  "Clasificas reseñas de restaurantes de comida peruana. " +
  "Lee las estrellas y el comentario del comensal y responde si la reseña es positiva, neutra o negativa. " +
  "Decide por lo que cuenta el comentario; las estrellas ayudan cuando el comentario no es claro.";

const ESQUEMA: Schema = {
  type: Type.OBJECT,
  properties: {
    sentimiento: { type: Type.STRING, enum: [...SENTIMIENTOS] },
  },
  required: ["sentimiento"],
};

export async function clasificarSentimiento(estrellas: number, comentario: string): Promise<Sentimiento> {
  const { sentimiento } = await pedirJson<{ sentimiento: unknown }>(
    INSTRUCCIONES,
    `Estrellas: ${estrellas} de 5\nComentario: ${comentario}`,
    ESQUEMA,
  );
  if (!SENTIMIENTOS.includes(sentimiento as Sentimiento)) throw new Error("Gemini devolvió un sentimiento desconocido");
  return sentimiento as Sentimiento;
}
