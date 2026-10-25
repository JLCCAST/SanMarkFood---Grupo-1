import { FieldValue, Timestamp, getFirestore, type DocumentData } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { ErrorFuncion, funcionHttps, leerEntero, leerOpcion, leerTexto, leerTextoOpcional, type Cuerpo } from "./https.js";
import { notificar, type Aviso } from "./notificar.js";
import { horaLima } from "./tiempo.js";

const ACCIONES = ["cancelar", "aceptar", "rechazar", "avanzar", "entregar"] as const;
const MOTIVOS = ["plato_agotado", "cocina_saturada", "local_por_cerrar", "otro"] as const;
const MINUTOS = [10, 15, 20, 30];
const TEXTO_DEL_MOTIVO: Record<string, string> = {
  plato_agotado: "Plato agotado",
  cocina_saturada: "Cocina saturada",
  local_por_cerrar: "El local está por cerrar",
};
const CANAL_PEDIDOS = "pedidos";

type Accion = (typeof ACCIONES)[number];

export const actualizarPedido = funcionHttps(async ({ uid, cuerpo }) => {
  const db = getFirestore();
  const accion = leerOpcion(cuerpo, "accion", ACCIONES);
  const pedidoRef = db.collection("pedidos").doc(leerTexto(cuerpo, "pedidoId", 128));

  const { antes, cambios } = await db.runTransaction(async (transaccion) => {
    const antes = (await transaccion.get(pedidoRef)).data();
    const duenio = accion === "cancelar" ? antes?.comensalUid : antes?.restauranteUid;
    if (antes === undefined || duenio !== uid) throw new ErrorFuncion("no_autorizado", 403);

    const cambios = cambiosDe(accion, antes, cuerpo);
    transaccion.update(pedidoRef, { ...cambios, actualizadoEn: FieldValue.serverTimestamp() });
    return { antes, cambios };
  });

  const despues = { ...antes, ...cambios };
  await avisarSinFallar(accion, pedidoRef.id, despues);
  return { estado: despues.estado };
});

function cambiosDe(accion: Accion, pedido: DocumentData, cuerpo: Cuerpo): DocumentData {
  switch (accion) {
    case "cancelar":
      exigirEstado(pedido, ["recibido"]);
      return { estado: "cancelado", pago: pagoRevertido(pedido) };
    case "aceptar": {
      exigirEstado(pedido, ["recibido"]);
      const minutos = leerEntero(cuerpo, "minutos", 10, 30);
      if (!MINUTOS.includes(minutos)) throw new ErrorFuncion("datos_invalidos");
      return {
        estado: "preparando",
        minutosEstimados: minutos,
        aceptadoEn: FieldValue.serverTimestamp(),
        listoAprox: Timestamp.fromMillis(Date.now() + minutos * 60_000),
      };
    }
    case "rechazar": {
      exigirEstado(pedido, ["recibido"]);
      const motivo = leerOpcion(cuerpo, "motivo", MOTIVOS);
      const detalle = leerTextoOpcional(cuerpo, "detalle", 200);
      const platoAgotado = leerTextoOpcional(cuerpo, "platoAgotado", 200);
      if (motivo === "otro" && (detalle === undefined || detalle.length < 3)) throw new ErrorFuncion("datos_invalidos");
      return {
        estado: "rechazado",
        pago: pagoRevertido(pedido),
        rechazo: {
          motivo,
          ...(detalle !== undefined ? { detalle } : {}),
          ...(platoAgotado !== undefined ? { platoAgotado } : {}),
          automatico: false,
        },
      };
    }
    case "avanzar":
      exigirEstado(pedido, ["preparando"]);
      return { estado: pedido.modalidad === "delivery" ? "en_camino" : "listo" };
    case "entregar": {
      exigirEstado(pedido, ["listo", "en_camino"]);
      if (pedido.modalidad === "recojo" && cuerpo.sinCodigo !== true) {
        if (leerTexto(cuerpo, "codigo", 4) !== pedido.codigoRecojo) throw new ErrorFuncion("codigo_incorrecto", 409);
      }
      return { estado: "entregado", entregadoEn: FieldValue.serverTimestamp() };
    }
  }
}

function exigirEstado(pedido: DocumentData, permitidos: string[]): void {
  if (!permitidos.includes(pedido.estado)) throw new ErrorFuncion("estado_invalido", 409);
}

function pagoRevertido(pedido: DocumentData): string {
  return pedido.pago === "pagado" ? "revertido" : pedido.pago;
}

async function avisarSinFallar(accion: Accion, pedidoId: string, pedido: DocumentData): Promise<void> {
  const numero = `#${String(pedido.numero).padStart(4, "0")}`;
  const aviso = accion === "cancelar" ? avisoAlLocal(numero, pedido) : avisoAlComensal(numero, pedido);
  const destino = accion === "cancelar" ? pedido.restauranteUid : pedido.comensalUid;
  await notificar(destino, { ...aviso, canal: CANAL_PEDIDOS, datos: { pedidoId } }).catch((error) =>
    logger.error("No se pudo avisar del cambio del pedido", { pedidoId, error }),
  );
}

function avisoAlLocal(numero: string, pedido: DocumentData): Omit<Aviso, "canal"> {
  return {
    titulo: `Pedido ${numero} cancelado`,
    cuerpo: `${pedido.comensalNombre} canceló su pedido antes de que lo aceptaras.`,
    tipo: "pedido_cancelado",
  };
}

function avisoAlComensal(numero: string, pedido: DocumentData): Omit<Aviso, "canal"> {
  const local = pedido.restauranteNombre;
  const base = { tipo: `pedido_${pedido.estado}`, guardarEnBandeja: true };
  switch (pedido.estado) {
    case "preparando":
      return {
        ...base,
        titulo: `${local} está preparando tu pedido ${numero}`,
        cuerpo: `Listo aprox. a las ${horaLima((pedido.listoAprox as Timestamp).toDate())}.`,
      };
    case "listo":
      return {
        ...base,
        titulo: `Tu pedido ${numero} está listo`,
        cuerpo: `Acércate a ${local} y dile al cajero tu código ${pedido.codigoRecojo}.`,
      };
    case "en_camino":
      return { ...base, titulo: `Tu pedido ${numero} va en camino`, cuerpo: `${local} ya lo envió.` };
    case "entregado":
      return { ...base, titulo: `Pedido ${numero} entregado`, cuerpo: "¡Buen provecho! Puedes calificarlo en la app." };
    default: {
      const motivo = pedido.rechazo?.detalle ?? TEXTO_DEL_MOTIVO[pedido.rechazo?.motivo] ?? "El local no pudo atenderlo";
      const plato = pedido.rechazo?.platoAgotado !== undefined ? `: ${pedido.rechazo.platoAgotado}` : "";
      const reversion = pedido.pago === "revertido" ? " Te devolvimos el pago." : "";
      return { ...base, titulo: `${local} rechazó tu pedido ${numero}`, cuerpo: `${motivo}${plato}.${reversion}` };
    }
  }
}
