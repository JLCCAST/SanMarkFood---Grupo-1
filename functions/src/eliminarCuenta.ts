import { getAuth } from "firebase-admin/auth";
import {
  FieldValue,
  getFirestore,
  type DocumentData,
  type DocumentReference,
  type Transaction,
} from "firebase-admin/firestore";
import { getStorage } from "firebase-admin/storage";
import { logger } from "firebase-functions";
import { ErrorFuncion, funcionHttps } from "./https.js";
import { notificar, type Aviso } from "./notificar.js";
import { avisarAlComensal, liberarFranja } from "./reservas.js";
import { momentoLima } from "./tiempo.js";

const NOMBRE_BORRADO = "Usuario eliminado";
const PEDIDO_TERMINADO = ["entregado", "rechazado", "cancelado"];
const RECHAZO_POR_CIERRE = { motivo: "otro", detalle: "El local cerró su cuenta", automatico: true };

const db = () => getFirestore();

export const eliminarCuenta = funcionHttps(async ({ uid }) => {
  const usuario = db().collection("usuarios").doc(uid);
  const rol = (await usuario.get()).get("rol");
  if (rol === "administrador") throw new ErrorFuncion("no_autorizado", 403);

  if (rol === "restaurante") await borrarLocal(uid);
  else await borrarComensal(uid);

  await db().recursiveDelete(usuario);
  await getAuth().deleteUser(uid);
  logger.info("Cuenta eliminada", { uid, rol });
  return { eliminada: true };
});

async function borrarComensal(uid: string): Promise<void> {
  const pedidos = await db().collection("pedidos").where("comensalUid", "==", uid).get();
  for (const { ref } of pedidos.docs) {
    const cancelado = await cambiarSi(
      ref,
      (pedido) => pedido.estado === "recibido",
      (pedido) => ({ estado: "cancelado", pago: pagoRevertido(pedido) }),
    );
    if (cancelado === undefined) continue;
    await avisar(cancelado.restauranteUid, {
      titulo: `Pedido ${numeroDe(cancelado)} cancelado`,
      cuerpo: "El comensal eliminó su cuenta antes de que lo aceptaras.",
      tipo: "pedido_cancelado",
      canal: "pedidos",
      datos: { pedidoId: ref.id },
    });
  }

  const reservas = await db().collection("reservas").where("comensalUid", "==", uid).get();
  for (const { ref } of reservas.docs) {
    const cancelada = await cambiarSi(ref, reservaActiva, () => ({ estado: "cancelada" }), liberarFranja);
    if (cancelada === undefined) continue;
    await avisar(cancelada.restauranteUid, {
      titulo: "Reserva cancelada",
      cuerpo: `El comensal eliminó su cuenta. Se liberó su reserva del ${cancelada.fecha} ${cancelada.hora}.`,
      tipo: "reserva_cancelada",
      canal: "reservas",
      datos: { reservaId: ref.id },
    });
  }

  const resenas = pedidos.docs
    .filter((pedido) => pedido.get("estado") === "entregado")
    .map((pedido) => db().collection("restaurantes").doc(pedido.get("restauranteUid")).collection("resenas").doc(pedido.id));
  const conResena = resenas.length === 0 ? [] : (await db().getAll(...resenas)).filter((resena) => resena.exists);

  const escritor = db().bulkWriter();
  for (const { ref } of [...pedidos.docs, ...reservas.docs, ...conResena]) {
    escritor.update(ref, { comensalNombre: NOMBRE_BORRADO });
  }
  await escritor.close();

  await getStorage().bucket().deleteFiles({ prefix: `comensales/${uid}/` });
}

async function borrarLocal(uid: string): Promise<void> {
  const pedidos = await db().collection("pedidos").where("restauranteUid", "==", uid).get();
  for (const { ref } of pedidos.docs) {
    const rechazado = await cambiarSi(
      ref,
      (pedido) => !PEDIDO_TERMINADO.includes(pedido.estado),
      (pedido) => ({ estado: "rechazado", pago: pagoRevertido(pedido), rechazo: RECHAZO_POR_CIERRE }),
    );
    if (rechazado === undefined) continue;
    const reversion = rechazado.pago === "revertido" ? " Te devolvimos el pago." : "";
    await avisar(rechazado.comensalUid, {
      titulo: `${rechazado.restauranteNombre} rechazó tu pedido ${numeroDe(rechazado)}`,
      cuerpo: `El local cerró su cuenta.${reversion}`,
      tipo: "pedido_rechazado",
      canal: "pedidos",
      datos: { pedidoId: ref.id },
      guardarEnBandeja: true,
    });
  }

  const reservas = await db().collection("reservas").where("restauranteUid", "==", uid).get();
  for (const { ref } of reservas.docs) {
    const rechazada = await cambiarSi(ref, reservaActiva, () => ({ estado: "rechazada", rechazo: RECHAZO_POR_CIERRE }));
    if (rechazada === undefined) continue;
    await avisarAlComensal(rechazada, ref.id, {
      titulo: "Tu reserva fue rechazada",
      cuerpo: `${rechazada.restauranteNombre} cerró su cuenta. Busca otra mesa para el ${rechazada.fecha} a las ${rechazada.hora}.`,
      tipo: "reserva_rechazada",
    });
  }

  await getStorage().bucket().deleteFiles({ prefix: `restaurantes/${uid}/` });
  await db().recursiveDelete(db().collection("restaurantes").doc(uid));
}

async function cambiarSi(
  ref: DocumentReference,
  debeCambiar: (antes: DocumentData) => boolean,
  cambios: (antes: DocumentData) => DocumentData,
  ademas?: (transaccion: Transaction, antes: DocumentData) => void,
): Promise<DocumentData | undefined> {
  return db().runTransaction(async (transaccion) => {
    const antes = (await transaccion.get(ref)).data();
    if (antes === undefined || !debeCambiar(antes)) return undefined;
    const nuevos = cambios(antes);
    transaccion.update(ref, { ...nuevos, actualizadoEn: FieldValue.serverTimestamp() });
    ademas?.(transaccion, antes);
    return { ...antes, ...nuevos };
  });
}

function reservaActiva(reserva: DocumentData): boolean {
  return (reserva.estado === "pendiente" || reserva.estado === "confirmada") && momentoLima(reserva.fecha, reserva.hora) > Date.now();
}

function pagoRevertido(pedido: DocumentData): string {
  return pedido.pago === "pagado" ? "revertido" : pedido.pago;
}

function numeroDe(pedido: DocumentData): string {
  return `#${String(pedido.numero).padStart(4, "0")}`;
}

async function avisar(uid: string, aviso: Aviso): Promise<void> {
  await notificar(uid, aviso).catch((error) => logger.error("No se pudo enviar el aviso", { uid, tipo: aviso.tipo, error }));
}
