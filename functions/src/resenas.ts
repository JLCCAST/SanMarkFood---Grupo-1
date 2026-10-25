import { FieldValue, getFirestore, type DocumentData } from "firebase-admin/firestore";
import { ErrorFuncion, funcionHttps, leerEntero, leerTexto, leerTextoOpcional, type Cuerpo } from "./https.js";

export const resenas = funcionHttps(async ({ uid, cuerpo }) => {
  const db = getFirestore();
  const pedidoId = leerTexto(cuerpo, "pedidoId", 128);
  const estrellas = leerEntero(cuerpo, "estrellas", 1, 5);
  const comentario = leerTextoOpcional(cuerpo, "comentario", 500);
  const pedidoRef = db.collection("pedidos").doc(pedidoId);

  await db.runTransaction(async (transaccion) => {
    const pedido = (await transaccion.get(pedidoRef)).data();
    if (pedido === undefined || pedido.comensalUid !== uid) throw new ErrorFuncion("no_autorizado", 403);
    if (pedido.estado !== "entregado") throw new ErrorFuncion("estado_invalido", 409);

    const localRef = db.collection("restaurantes").doc(pedido.restauranteUid);
    const resenaRef = localRef.collection("resenas").doc(pedidoId);
    if ((await transaccion.get(resenaRef)).exists) throw new ErrorFuncion("ya_calificado", 409);
    const local = await transaccion.get(localRef);

    const total = Number(local.get("calificacion.total") ?? 0);
    const promedio = Number(local.get("calificacion.promedio") ?? 0);
    transaccion.set(resenaRef, {
      comensalUid: uid,
      comensalNombre: pedido.comensalNombre,
      estrellas,
      ...(comentario !== undefined ? { comentario } : {}),
      platos: leerPlatos(cuerpo.platos, pedido),
      creadoEn: FieldValue.serverTimestamp(),
    });
    transaccion.update(localRef, {
      calificacion: { promedio: (promedio * total + estrellas) / (total + 1), total: total + 1 },
    });
  });

  return { pedidoId };
});

function leerPlatos(valor: unknown, pedido: DocumentData): Record<string, boolean> {
  if (valor === undefined || valor === null) return {};
  if (typeof valor !== "object" || Array.isArray(valor)) throw new ErrorFuncion("datos_invalidos");

  const delPedido = new Set(
    (Array.isArray(pedido.items) ? pedido.items : [])
      .map((item: DocumentData) => item.platoId)
      .filter((platoId: unknown): platoId is string => typeof platoId === "string"),
  );
  const platos = valor as Cuerpo;
  for (const [platoId, gusto] of Object.entries(platos)) {
    if (!delPedido.has(platoId) || typeof gusto !== "boolean") throw new ErrorFuncion("datos_invalidos");
  }
  return platos as Record<string, boolean>;
}
