import { GoogleGenAI, type Schema } from "@google/genai";

const MODELO = "gemini-3.5-flash";

let cliente: GoogleGenAI | undefined;

function gemini(): GoogleGenAI {
  cliente ??= new GoogleGenAI({ vertexai: true, project: process.env.GCLOUD_PROJECT, location: "global" });
  return cliente;
}

export async function pedirJson<T>(instrucciones: string, texto: string, esquema: Schema): Promise<T> {
  const respuesta = await gemini().models.generateContent({
    model: MODELO,
    contents: texto,
    config: {
      systemInstruction: instrucciones,
      responseMimeType: "application/json",
      responseSchema: esquema,
      temperature: 0,
    },
  });
  const json = respuesta.text;
  if (json === undefined) throw new Error("Gemini no devolvió una respuesta");
  return JSON.parse(json) as T;
}
