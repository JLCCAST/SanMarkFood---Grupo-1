import { FieldValue, Timestamp, getFirestore, type DocumentData } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { onSchedule } from "firebase-functions/v2/scheduler";
import { notificar } from "./notificar.js";
import { avisarAlComensal, liberarFranja } from "./reservas.js";
import { fechaLima, momentoLima } from "./tiempo.js";

const MINUTOS_PARA_ACEPTAR = 10;
const MINUTOS_ANTES_DE_LA_RESERVA = 60;
const RECHAZO_AUTOMATICO = { motivo: "sin_respuesta", automatico: true };

export const vencimientos = onSchedule({ schedule: "every 1 minutes", timeZone: "America/Lima" }, async () => {
  await Promise.all([vencerPedidos(), vencerReservas()]);
});

async function vencerPedidos(): Promise<void> {
  const db = getFirestore();
  const limite = Timestamp.fromMillis(Date.now() - MINUTOS_PARA_ACEPTAR * 60_000);
  const vencidos = await db.collection("pedidos").where("estado", "==", "recibido").where("creadoEn", "<=", limite).get();

  for (const documento of vencidos.docs) {
    const pedido: DocumentData | undefined = await db.runTransaction(async (transaccion) => {
      const antes = (await transaccion.get(documento.ref)).data();
      if (antes?.estado !== "recibido") return undefined;
      const cambios = {
        estado: "rechazado",
        pago: antes.pago === "pagado" ? "revertido" : antes.pago,
        rechazo: RECHAZO_AUTOMATICO,
      };
      transaccion.update(documento.ref, { ...cambios, actualizadoEn: FieldValue.serverTimestamp() });
      return { ...antes, ...cambios };
    });
    if (pedido === undefined) continue;

    const numero = `#${String(pedido.numero).padStart(4, "0")}`;
    const reversion = pedido.pago === "revertido" ? " Te devolvimos el pago." : "";
    await notificar(pedido.comensalUid, {
      titulo: `${pedido.restauranteNombre} no respondió a tiempo`,
      cuerpo: `Tu pedido ${numero} se rechazó solo a los ${MINUTOS_PARA_ACEPTAR} minutos.${reversion}`,
      tipo: "pedido_rechazado",
      canal: "pedidos",
      datos: { pedidoId: documento.id },
      guardarEnBandeja: true,
    }).catch((error) => logger.error("No se pudo avisar del rechazo automático", { pedidoId: documento.id, error }));
  }
  if (!vencidos.empty) logger.info("Pedidos vencidos", { total: vencidos.size });
}

async function vencerReservas(): Promise<void> {
  const db = getFirestore();
  const limite = Date.now() + MINUTOS_ANTES_DE_LA_RESERVA * 60_000;
  const pendientes = await db.collection("reservas").where("estado", "==", "pendiente").where("fecha", "<=", fechaLima(1)).get();
  const vencidas = pendientes.docs.filter((reserva) => momentoLima(reserva.get("fecha"), reserva.get("hora")) <= limite);

  for (const documento of vencidas) {
    const reserva = await db.runTransaction(async (transaccion) => {
      const antes = (await transaccion.get(documento.ref)).data();
      if (antes?.estado !== "pendiente") return undefined;
      transaccion.update(documento.ref, {
        estado: "rechazada",
        rechazo: RECHAZO_AUTOMATICO,
        actualizadoEn: FieldValue.serverTimestamp(),
      });
      liberarFranja(transaccion, antes);
      return antes;
    });
    if (reserva === undefined) continue;

    await avisarAlComensal(reserva, documento.id, {
      titulo: "Tu reserva fue rechazada",
      cuerpo: `${reserva.restauranteNombre} no respondió a tiempo. Busca otra mesa para el ${reserva.fecha} a las ${reserva.hora}.`,
      tipo: "reserva_rechazada",
    });
  }
  if (vencidas.length > 0) logger.info("Reservas vencidas", { total: vencidas.length });
}
