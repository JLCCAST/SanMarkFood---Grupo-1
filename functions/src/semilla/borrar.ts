import { getAuth } from "firebase-admin/auth";
import { FieldValue, getFirestore, type QueryDocumentSnapshot } from "firebase-admin/firestore";
import { UIDS_DE_COMENSALES, UIDS_DE_LOCALES } from "./cuentas.js";

export interface DatosBorrados {
  cuentas: number;
  pedidos: number;
  reservas: number;
}

async function documentosDe(coleccion: string, campo: string, uids: string[]): Promise<QueryDocumentSnapshot[]> {
  const respuesta = await getFirestore().collection(coleccion).where(campo, "in", uids).get();
  return respuesta.docs;
}

function sinRepetir(documentos: QueryDocumentSnapshot[]): QueryDocumentSnapshot[] {
  return [...new Map(documentos.map((documento) => [documento.id, documento])).values()];
}

async function liberarFranjasDeLocalesReales(reservas: QueryDocumentSnapshot[]): Promise<void> {
  const db = getFirestore();
  const activasEnLocalesReales = reservas.filter((reserva) =>
    !UIDS_DE_LOCALES.includes(reserva.get("restauranteUid")) &&
    (reserva.get("estado") === "pendiente" || reserva.get("estado") === "confirmada"),
  );
  for (const reserva of activasEnLocalesReales) {
    const hora = String(reserva.get("hora")).replace(":", "");
    await db.collection("restaurantes").doc(reserva.get("restauranteUid"))
      .collection("franjas").doc(`${reserva.get("fecha")}_${hora}`)
      .set({ ocupados: FieldValue.increment(-Number(reserva.get("personas"))) }, { merge: true });
  }
}

async function borrarEnLotes(documentos: QueryDocumentSnapshot[]): Promise<void> {
  const db = getFirestore();
  for (let inicio = 0; inicio < documentos.length; inicio += 400) {
    const lote = db.batch();
    documentos.slice(inicio, inicio + 400).forEach((documento) => lote.delete(documento.ref));
    await lote.commit();
  }
}

export async function borrarDatosDePrueba(): Promise<DatosBorrados> {
  const db = getFirestore();

  const pedidos = sinRepetir([
    ...await documentosDe("pedidos", "restauranteUid", UIDS_DE_LOCALES),
    ...await documentosDe("pedidos", "comensalUid", UIDS_DE_COMENSALES),
  ]);
  const reservas = sinRepetir([
    ...await documentosDe("reservas", "restauranteUid", UIDS_DE_LOCALES),
    ...await documentosDe("reservas", "comensalUid", UIDS_DE_COMENSALES),
  ]);
  await liberarFranjasDeLocalesReales(reservas);
  await borrarEnLotes([...pedidos, ...reservas]);

  for (const uid of UIDS_DE_LOCALES) {
    await db.recursiveDelete(db.collection("restaurantes").doc(uid));
  }
  const uids = [...UIDS_DE_LOCALES, ...UIDS_DE_COMENSALES];
  for (const uid of uids) {
    await db.recursiveDelete(db.collection("usuarios").doc(uid));
  }
  const existentes = await getAuth().getUsers(uids.map((uid) => ({ uid })));
  await getAuth().deleteUsers(existentes.users.map((usuario) => usuario.uid));

  return {
    cuentas: existentes.users.length,
    pedidos: pedidos.length,
    reservas: reservas.length,
  };
}
