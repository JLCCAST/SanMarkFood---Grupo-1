import { FieldValue, Timestamp, getFirestore, type DocumentData } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { onSchedule } from "firebase-functions/v2/scheduler";
import { notificar } from "./notificar.js";

const MINUTOS_PARA_ACEPTAR = 10;
const RECHAZO_AUTOMATICO = { motivo: "sin_respuesta", automatico: true };

export const vencimientos = onSchedule({ schedule: "every 1 minutes", timeZone: "America/Lima" }, async () => {
  await vencerPedidos();
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
