import {
  FieldValue,
  getFirestore,
  type DocumentData,
  type DocumentReference,
  type Transaction,
} from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { ErrorFuncion, funcionHttps, leerEntero, leerOpcion, leerTexto, leerTextoOpcional, type Cuerpo } from "./https.js";
import { notificar, type Aviso } from "./notificar.js";
import { diaLima, fechaLima, momentoLima } from "./tiempo.js";

const ACCIONES = ["crear", "modificar", "cancelar", "confirmar", "rechazar"] as const;
const MOTIVOS = ["sin_mesas", "local_cerrado", "grupo_grande", "otro"] as const;
const TEXTO_DEL_MOTIVO: Record<string, string> = {
  sin_mesas: "Sin mesas disponibles a esa hora",
  local_cerrado: "El local está cerrado ese día",
  grupo_grande: "El grupo es demasiado grande",
};
const CANAL_RESERVAS = "reservas";
const DOS_HORAS = 2 * 3_600_000;
const SIETE_DIAS = 7 * 86_400_000;

const db = () => getFirestore();

export const reservas = funcionHttps(async ({ uid, cuerpo }) => {
  switch (leerOpcion(cuerpo, "accion", ACCIONES)) {
    case "crear":
      return crear(uid, cuerpo);
    case "modificar":
      return modificar(uid, cuerpo);
    case "cancelar":
      return cancelar(uid, cuerpo);
    case "confirmar":
      return confirmar(uid, cuerpo);
    case "rechazar":
      return rechazar(uid, cuerpo);
  }
});

async function crear(uid: string, cuerpo: Cuerpo): Promise<object> {
  const restauranteUid = leerTexto(cuerpo, "restauranteUid", 128);
  const fecha = leerFecha(cuerpo);
  const hora = leerHora(cuerpo);
  const personas = leerEntero(cuerpo, "personas", 1, 10);

  const comensal = await db().collection("usuarios").doc(uid).get();
  if (comensal.get("rol") !== "comensal") throw new ErrorFuncion("no_autorizado", 403);
  const localRef = db().collection("restaurantes").doc(restauranteUid);
  const reservaRef = db().collection("reservas").doc();

  const local = await db().runTransaction(async (transaccion) => {
    const local = await transaccion.get(localRef);
    if (!local.exists || local.get("estado") !== "aprobado") throw new ErrorFuncion("datos_invalidos");
    validarHorario(local.data(), fecha, hora);

    const activas = await transaccion.get(
      db().collection("reservas").where("comensalUid", "==", uid).where("estado", "in", ["pendiente", "confirmada"]),
    );
    if (activas.docs.filter((reserva) => reserva.get("fecha") >= fechaLima()).length >= 2) {
      throw new ErrorFuncion("tope_reservas", 409);
    }
    await validarCupo(transaccion, localRef, local.data(), fecha, hora, personas);

    transaccion.set(reservaRef, {
      comensalUid: uid,
      comensalNombre: String(comensal.get("nombre") ?? "Comensal"),
      restauranteUid,
      restauranteNombre: String(local.get("nombre")),
      fecha,
      hora,
      personas,
      estado: "pendiente",
      creadoEn: FieldValue.serverTimestamp(),
      actualizadoEn: FieldValue.serverTimestamp(),
    });
    sumarAFranja(transaccion, localRef, fecha, hora, personas);
    return local;
  });

  await avisarSinFallar(restauranteUid, {
    titulo: "Reserva nueva",
    cuerpo: `${comensal.get("nombre") ?? "Un comensal"} · ${personas} ${personas === 1 ? "persona" : "personas"} · ${fecha} ${hora}`,
    tipo: "reserva_nueva",
    canal: CANAL_RESERVAS,
    datos: { reservaId: reservaRef.id },
  });
  logger.info("Reserva creada", { reservaId: reservaRef.id, local: local.id });
  return { reservaId: reservaRef.id };
}

async function modificar(uid: string, cuerpo: Cuerpo): Promise<object> {
  const reservaRef = referenciaDeReserva(cuerpo);
  const fecha = leerFecha(cuerpo);
  const hora = leerHora(cuerpo);

  const reserva = await db().runTransaction(async (transaccion) => {
    const reserva = await leerReserva(transaccion, reservaRef, uid, "comensalUid");
    if (reserva.estado !== "pendiente" && reserva.estado !== "confirmada") throw new ErrorFuncion("estado_invalido", 409);
    if (reserva.fecha === fecha && reserva.hora === hora) throw new ErrorFuncion("datos_invalidos");

    const localRef = db().collection("restaurantes").doc(reserva.restauranteUid);
    const local = await transaccion.get(localRef);
    validarHorario(local.data(), fecha, hora);
    await validarCupo(transaccion, localRef, local.data(), fecha, hora, reserva.personas);

    transaccion.update(reservaRef, {
      fecha,
      hora,
      estado: "pendiente",
      horarioAnterior: { fecha: reserva.fecha, hora: reserva.hora },
      actualizadoEn: FieldValue.serverTimestamp(),
    });
    sumarAFranja(transaccion, localRef, reserva.fecha, reserva.hora, -reserva.personas);
    sumarAFranja(transaccion, localRef, fecha, hora, reserva.personas);
    return reserva;
  });

  await avisarSinFallar(reserva.restauranteUid, {
    titulo: "Reserva modificada",
    cuerpo: `${reserva.comensalNombre} cambió su reserva a ${fecha} ${hora}`,
    tipo: "reserva_modificada",
    canal: CANAL_RESERVAS,
    datos: { reservaId: reservaRef.id },
  });
  return { estado: "pendiente" };
}

async function cancelar(uid: string, cuerpo: Cuerpo): Promise<object> {
  const reservaRef = referenciaDeReserva(cuerpo);
  const reserva = await db().runTransaction(async (transaccion) => {
    const reserva = await leerReserva(transaccion, reservaRef, uid, "comensalUid");
    if (reserva.estado !== "pendiente" && reserva.estado !== "confirmada") throw new ErrorFuncion("estado_invalido", 409);
    transaccion.update(reservaRef, { estado: "cancelada", actualizadoEn: FieldValue.serverTimestamp() });
    liberarFranja(transaccion, reserva);
    return reserva;
  });

  await avisarSinFallar(reserva.restauranteUid, {
    titulo: "Reserva cancelada",
    cuerpo: `${reserva.comensalNombre} canceló su reserva del ${reserva.fecha} ${reserva.hora}`,
    tipo: "reserva_cancelada",
    canal: CANAL_RESERVAS,
    datos: { reservaId: reservaRef.id },
  });
  return { estado: "cancelada" };
}

async function confirmar(uid: string, cuerpo: Cuerpo): Promise<object> {
  const reservaRef = referenciaDeReserva(cuerpo);
  const reserva = await db().runTransaction(async (transaccion) => {
    const reserva = await leerReserva(transaccion, reservaRef, uid, "restauranteUid");
    if (reserva.estado !== "pendiente") throw new ErrorFuncion("estado_invalido", 409);
    transaccion.update(reservaRef, {
      estado: "confirmada",
      respondidaEn: FieldValue.serverTimestamp(),
      actualizadoEn: FieldValue.serverTimestamp(),
    });
    return reserva;
  });

  await avisarAlComensal(reserva, reservaRef.id, {
    titulo: "Reserva confirmada",
    cuerpo: `${reserva.restauranteNombre} te espera el ${reserva.fecha} a las ${reserva.hora}.`,
    tipo: "reserva_confirmada",
  });
  return { estado: "confirmada" };
}

async function rechazar(uid: string, cuerpo: Cuerpo): Promise<object> {
  const reservaRef = referenciaDeReserva(cuerpo);
  const motivo = leerOpcion(cuerpo, "motivo", MOTIVOS);
  const detalle = leerTextoOpcional(cuerpo, "detalle", 200);
  if (motivo === "otro" && (detalle === undefined || detalle.length < 3)) throw new ErrorFuncion("datos_invalidos");

  const reserva = await db().runTransaction(async (transaccion) => {
    const reserva = await leerReserva(transaccion, reservaRef, uid, "restauranteUid");
    if (reserva.estado !== "pendiente") throw new ErrorFuncion("estado_invalido", 409);
    transaccion.update(reservaRef, {
      estado: "rechazada",
      rechazo: { motivo, ...(detalle !== undefined ? { detalle } : {}), automatico: false },
      respondidaEn: FieldValue.serverTimestamp(),
      actualizadoEn: FieldValue.serverTimestamp(),
    });
    liberarFranja(transaccion, reserva);
    return reserva;
  });

  await avisarAlComensal(reserva, reservaRef.id, {
    titulo: "Tu reserva fue rechazada",
    cuerpo: `${reserva.restauranteNombre}: ${detalle ?? TEXTO_DEL_MOTIVO[motivo]}.`,
    tipo: "reserva_rechazada",
  });
  return { estado: "rechazada" };
}

export function liberarFranja(transaccion: Transaction, reserva: DocumentData): void {
  const localRef = db().collection("restaurantes").doc(reserva.restauranteUid);
  sumarAFranja(transaccion, localRef, reserva.fecha, reserva.hora, -reserva.personas);
}

export async function avisarAlComensal(
  reserva: DocumentData,
  reservaId: string,
  aviso: Pick<Aviso, "titulo" | "cuerpo" | "tipo">,
): Promise<void> {
  const comensal = await db().collection("usuarios").doc(reserva.comensalUid).get();
  if (comensal.get("notificarReservas") === false) return;
  await avisarSinFallar(reserva.comensalUid, {
    ...aviso,
    canal: CANAL_RESERVAS,
    datos: { reservaId },
    guardarEnBandeja: true,
  });
}

function sumarAFranja(transaccion: Transaction, localRef: DocumentReference, fecha: string, hora: string, personas: number) {
  transaccion.set(franja(localRef, fecha, hora), { ocupados: FieldValue.increment(personas) }, { merge: true });
}

function franja(localRef: DocumentReference, fecha: string, hora: string): DocumentReference {
  return localRef.collection("franjas").doc(`${fecha}_${hora.replace(":", "")}`);
}

async function validarCupo(
  transaccion: Transaction,
  localRef: DocumentReference,
  local: DocumentData | undefined,
  fecha: string,
  hora: string,
  personas: number,
): Promise<void> {
  const cupo = local?.cupoPorFranja;
  if (typeof cupo !== "number") return;
  const ocupados = Number((await transaccion.get(franja(localRef, fecha, hora))).get("ocupados") ?? 0);
  if (ocupados + personas > cupo) throw new ErrorFuncion("franja_llena", 409);
}

function validarHorario(local: DocumentData | undefined, fecha: string, hora: string): void {
  const momento = momentoLima(fecha, hora);
  if (momento < Date.now() + DOS_HORAS || momento > Date.now() + SIETE_DIAS) {
    throw new ErrorFuncion("fuera_de_plazo", 409);
  }
  const dia = local?.horario?.[diaLima(fecha)];
  if (dia?.abierto !== true || hora < dia.abre || hora >= dia.cierra) {
    throw new ErrorFuncion("fuera_de_horario", 409);
  }
}

async function leerReserva(
  transaccion: Transaction,
  reservaRef: DocumentReference,
  uid: string,
  campoDelDuenio: "comensalUid" | "restauranteUid",
): Promise<DocumentData> {
  const reserva = (await transaccion.get(reservaRef)).data();
  if (reserva === undefined || reserva[campoDelDuenio] !== uid) throw new ErrorFuncion("no_autorizado", 403);
  return reserva;
}

function referenciaDeReserva(cuerpo: Cuerpo): DocumentReference {
  return db().collection("reservas").doc(leerTexto(cuerpo, "reservaId", 128));
}

function leerFecha(cuerpo: Cuerpo): string {
  const fecha = leerTexto(cuerpo, "fecha", 10);
  if (!/^\d{4}-\d{2}-\d{2}$/.test(fecha)) throw new ErrorFuncion("datos_invalidos");
  return fecha;
}

function leerHora(cuerpo: Cuerpo): string {
  const hora = leerTexto(cuerpo, "hora", 5);
  if (!/^([01]\d|2[0-3]):(00|30)$/.test(hora)) throw new ErrorFuncion("datos_invalidos");
  return hora;
}

async function avisarSinFallar(uid: string, aviso: Aviso): Promise<void> {
  await notificar(uid, aviso).catch((error) => logger.error("No se pudo enviar el aviso", { uid, tipo: aviso.tipo, error }));
}
