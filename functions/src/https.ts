import { getAuth } from "firebase-admin/auth";
import { logger } from "firebase-functions";
import { onRequest, type Request } from "firebase-functions/v2/https";

export class ErrorFuncion extends Error {
  constructor(
    readonly codigo: string,
    readonly estado: 400 | 401 | 403 | 404 | 409 = 400,
  ) {
    super(codigo);
  }
}

export type Cuerpo = Record<string, unknown>;

export interface Llamada {
  uid: string;
  cuerpo: Cuerpo;
}

export function funcionHttps(manejar: (llamada: Llamada) => Promise<object>) {
  return onRequest({ region: "us-central1" }, async (solicitud, respuesta) => {
    if (solicitud.method !== "POST") {
      respuesta.status(405).json({ error: "metodo_no_permitido" });
      return;
    }
    try {
      const uid = await uidVerificado(solicitud);
      respuesta.status(200).json(await manejar({ uid, cuerpo: cuerpoDe(solicitud) }));
    } catch (error) {
      if (error instanceof ErrorFuncion) {
        respuesta.status(error.estado).json({ error: error.codigo });
        return;
      }
      logger.error("Error inesperado", error);
      respuesta.status(500).json({ error: "error_interno" });
    }
  });
}

async function uidVerificado(solicitud: Request): Promise<string> {
  const encabezado = solicitud.get("Authorization") ?? "";
  if (!encabezado.startsWith("Bearer ")) throw new ErrorFuncion("no_autenticado", 401);

  const token = await getAuth()
    .verifyIdToken(encabezado.slice("Bearer ".length))
    .catch(() => {
      throw new ErrorFuncion("no_autenticado", 401);
    });
  if (token.email_verified !== true) throw new ErrorFuncion("correo_sin_verificar", 403);
  return token.uid;
}

function cuerpoDe(solicitud: Request): Cuerpo {
  const cuerpo: unknown = solicitud.body;
  return typeof cuerpo === "object" && cuerpo !== null && !Array.isArray(cuerpo) ? (cuerpo as Cuerpo) : {};
}

export function leerTexto(cuerpo: Cuerpo, campo: string, maximo = 300): string {
  const valor = cuerpo[campo];
  if (typeof valor !== "string" || valor.trim() === "" || valor.length > maximo) {
    throw new ErrorFuncion("datos_invalidos");
  }
  return valor.trim();
}

export function leerTextoOpcional(cuerpo: Cuerpo, campo: string, maximo = 300): string | undefined {
  return cuerpo[campo] === undefined || cuerpo[campo] === null ? undefined : leerTexto(cuerpo, campo, maximo);
}

export function leerEntero(cuerpo: Cuerpo, campo: string, minimo: number, maximo: number): number {
  const valor = cuerpo[campo];
  if (typeof valor !== "number" || !Number.isInteger(valor) || valor < minimo || valor > maximo) {
    throw new ErrorFuncion("datos_invalidos");
  }
  return valor;
}

export function leerOpcion<T extends string>(cuerpo: Cuerpo, campo: string, opciones: readonly T[]): T {
  const valor = cuerpo[campo];
  if (typeof valor !== "string" || !opciones.includes(valor as T)) throw new ErrorFuncion("datos_invalidos");
  return valor as T;
}
