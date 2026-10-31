import { FieldValue, getFirestore, type DocumentData, type DocumentReference } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { ErrorFuncion, funcionHttps, leerEntero, leerOpcion, leerTexto, type Cuerpo } from "./https.js";
import { notificar } from "./notificar.js";
import { diaLima, fechaLima, horaLima } from "./tiempo.js";

const MODALIDADES = ["recojo", "delivery"] as const;
const METODOS_DE_PAGO = ["efectivo", "billetera", "tarjeta"] as const;
const CANAL_PEDIDOS_NUEVOS = "pedidos_nuevos";

interface Item {
  tipo: "plato" | "menu";
  cantidad: number;
  nombre: string;
  precio: number;
  platoId?: string;
  fecha?: string;
  entrada?: string;
  segundo?: string;
}

export const crearPedido = funcionHttps(async ({ uid, cuerpo }) => {
  const db = getFirestore();
  const restauranteUid = leerTexto(cuerpo, "restauranteUid", 128);
  const modalidad = leerOpcion(cuerpo, "modalidad", MODALIDADES);
  const metodoPago = leerOpcion(cuerpo, "metodoPago", METODOS_DE_PAGO);
  const direccion = modalidad === "delivery" ? leerDireccion(cuerpo.direccion) : undefined;

  const comensal = await db.collection("usuarios").doc(uid).get();
  if (comensal.get("rol") !== "comensal") throw new ErrorFuncion("no_autorizado", 403);

  const localRef = db.collection("restaurantes").doc(restauranteUid);
  const local = await localRef.get();
  if (!local.exists || local.get("estado") !== "aprobado" || local.get("pausado") === true) {
    throw new ErrorFuncion("local_no_disponible", 409);
  }
  if (!estaAbierto(local.get("horario"))) throw new ErrorFuncion("local_cerrado", 409);

  const items = await armarItems(localRef, cuerpo.items);
  const total = items.reduce((suma, item) => suma + item.precio * item.cantidad, 0);
  const comensalNombre = String(comensal.get("nombre") ?? "Comensal");
  const pedidoRef = db.collection("pedidos").doc();

  const numero = await db.runTransaction(async (transaccion) => {
    const numero = ((await transaccion.get(localRef)).get("ultimoNumeroPedido") ?? 0) + 1;
    transaccion.update(localRef, { ultimoNumeroPedido: numero });
    transaccion.set(pedidoRef, {
      numero,
      comensalUid: uid,
      comensalNombre,
      restauranteUid,
      restauranteNombre: String(local.get("nombre")),
      modalidad,
      ...(direccion !== undefined ? { direccion } : { codigoRecojo: codigoRecojo() }),
      metodoPago,
      pago: metodoPago === "efectivo" ? "por_cobrar" : "pagado",
      items,
      total,
      estado: "recibido",
      creadoEn: FieldValue.serverTimestamp(),
      actualizadoEn: FieldValue.serverTimestamp(),
    });
    return numero;
  });

  await notificar(restauranteUid, {
    titulo: `Pedido nuevo #${String(numero).padStart(4, "0")}`,
    cuerpo: `${comensalNombre} · ${resumen(items)} · S/ ${(total / 100).toFixed(2)}`,
    tipo: "pedido_nuevo",
    canal: CANAL_PEDIDOS_NUEVOS,
    datos: { pedidoId: pedidoRef.id },
  }).catch((error) => logger.error("No se pudo avisar del pedido nuevo", { pedidoId: pedidoRef.id, error }));

  return { pedidoId: pedidoRef.id, numero };
});

function leerDireccion(valor: unknown): { texto: string; latitud: number; longitud: number } {
  if (typeof valor !== "object" || valor === null || Array.isArray(valor)) throw new ErrorFuncion("datos_invalidos");
  const direccion = valor as Cuerpo;
  const { latitud, longitud } = direccion;
  if (typeof latitud !== "number" || latitud < -90 || latitud > 90) throw new ErrorFuncion("datos_invalidos");
  if (typeof longitud !== "number" || longitud < -180 || longitud > 180) throw new ErrorFuncion("datos_invalidos");
  return { texto: leerTexto(direccion, "texto", 200), latitud, longitud };
}

function estaAbierto(horario: unknown): boolean {
  const dia = (horario as Record<string, DocumentData | undefined> | undefined)?.[diaLima()];
  const ahora = horaLima();
  return dia?.abierto === true && dia.abre <= ahora && ahora < dia.cierra;
}

async function armarItems(localRef: DocumentReference, valor: unknown): Promise<Item[]> {
  if (!Array.isArray(valor) || valor.length === 0 || valor.length > 20) throw new ErrorFuncion("datos_invalidos");
  const hoy = fechaLima();
  let menu: DocumentData | undefined;
  const items: Item[] = [];

  for (const crudo of valor) {
    if (typeof crudo !== "object" || crudo === null || Array.isArray(crudo)) throw new ErrorFuncion("datos_invalidos");
    const pedido = crudo as Cuerpo;
    const tipo = leerOpcion(pedido, "tipo", ["plato", "menu"] as const);
    const cantidad = leerEntero(pedido, "cantidad", 1, 20);

    if (tipo === "plato") {
      const platoId = leerTexto(pedido, "platoId", 128);
      const plato = await localRef.collection("platos").doc(platoId).get();
      if (!plato.exists) throw new ErrorFuncion("datos_invalidos");
      if (plato.get("agotadoEl") === hoy) throw new ErrorFuncion("plato_agotado", 409);
      items.push({ tipo, platoId, cantidad, nombre: String(plato.get("nombre")), precio: Number(plato.get("precio")) });
    } else {
      menu ??= await menuDeHoy(localRef, hoy);
      items.push({
        tipo,
        cantidad,
        nombre: "Menú del día",
        precio: Number(menu.precio),
        fecha: hoy,
        entrada: opcionDisponible(menu.entradas, leerTexto(pedido, "entrada", 80)),
        segundo: opcionDisponible(menu.segundos, leerTexto(pedido, "segundo", 80)),
      });
    }
  }
  return items;
}

async function menuDeHoy(localRef: DocumentReference, hoy: string): Promise<DocumentData> {
  const menu = (await localRef.collection("menus").doc(hoy).get()).data();
  if (menu === undefined || menu.estado !== "publicado" || horaLima() >= menu.horaFin) {
    throw new ErrorFuncion("menu_vencido", 409);
  }
  return menu;
}

function opcionDisponible(opciones: unknown, nombre: string): string {
  const opcion = (Array.isArray(opciones) ? opciones : []).find(
    (candidata): candidata is DocumentData => candidata?.nombre === nombre,
  );
  if (opcion === undefined) throw new ErrorFuncion("datos_invalidos");
  if (opcion.agotado === true) throw new ErrorFuncion("opcion_agotada", 409);
  return nombre;
}

function codigoRecojo(): string {
  return String(Math.floor(Math.random() * 10_000)).padStart(4, "0");
}

function resumen(items: Item[]): string {
  return items.map((item) => (item.cantidad > 1 ? `${item.cantidad}× ${item.nombre}` : item.nombre)).join(" + ");
}
