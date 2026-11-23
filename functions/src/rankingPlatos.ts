import { Timestamp, getFirestore, type DocumentData, type DocumentReference } from "firebase-admin/firestore";
import { ErrorFuncion, funcionHttps, leerEntero } from "./https.js";

const PLATOS_DEL_RANKING = 5;
const RANGO_MAXIMO = 35 * 86_400_000;
const MENU_DEL_DIA = "Menú del día";

interface Conteo {
  nombre: string;
  cantidad: number;
}

export const rankingPlatos = funcionHttps(async ({ uid, cuerpo }) => {
  const desde = leerEntero(cuerpo, "desde", 0, Number.MAX_SAFE_INTEGER);
  const hasta = leerEntero(cuerpo, "hasta", 0, Number.MAX_SAFE_INTEGER);
  if (hasta <= desde || hasta - desde > RANGO_MAXIMO) throw new ErrorFuncion("datos_invalidos");

  const db = getFirestore();
  const local = db.collection("restaurantes").doc(uid);
  if (!(await local.get()).exists) throw new ErrorFuncion("no_autorizado", 403);

  const pedidos = await db
    .collection("pedidos")
    .where("restauranteUid", "==", uid)
    .where("estado", "==", "entregado")
    .where("creadoEn", ">=", Timestamp.fromMillis(desde))
    .where("creadoEn", "<", Timestamp.fromMillis(hasta))
    .select("items")
    .get();

  const conteos = new Map<string, Conteo>();
  for (const pedido of pedidos.docs) {
    const items: unknown = pedido.get("items");
    for (const item of Array.isArray(items) ? (items as DocumentData[]) : []) {
      const clave = claveDe(item);
      const nombre = item.tipo === "menu" ? MENU_DEL_DIA : String(item.nombre ?? "");
      const cantidad = (conteos.get(clave)?.cantidad ?? 0) + Number(item.cantidad ?? 0);
      conteos.set(clave, { nombre, cantidad });
    }
  }

  const ranking = [...conteos.entries()]
    .sort(([, a], [, b]) => b.cantidad - a.cantidad || a.nombre.localeCompare(b.nombre, "es"))
    .slice(0, PLATOS_DEL_RANKING);
  const nombres = await nombresActuales(local, ranking.map(([clave]) => clave));

  return {
    platos: ranking.map(([clave, conteo]) => ({ nombre: nombres.get(clave) ?? conteo.nombre, cantidad: conteo.cantidad })),
  };
});

function claveDe(item: DocumentData): string {
  if (item.tipo === "menu") return "menu";
  return typeof item.platoId === "string" ? `plato:${item.platoId}` : `nombre:${item.nombre}`;
}

async function nombresActuales(local: DocumentReference, claves: string[]): Promise<Map<string, string>> {
  const platos = claves.filter((clave) => clave.startsWith("plato:")).map((clave) => local.collection("platos").doc(clave.slice(6)));
  if (platos.length === 0) return new Map();
  const fotos = await getFirestore().getAll(...platos);
  return new Map(fotos.filter((foto) => foto.exists).map((foto) => [`plato:${foto.id}`, String(foto.get("nombre"))]));
}
