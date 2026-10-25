import { initializeApp } from "firebase-admin/app";
import { FieldValue, Timestamp, getFirestore, type DocumentData } from "firebase-admin/firestore";
import { fechaLima } from "../tiempo.js";

initializeApp();
const db = getFirestore();

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

interface Carta {
  platos: { id: string; nombre: string; precio: number }[];
  menu: { fecha: string; precio: number; entrada: string; segundo: string };
}

const DIRECCION_EJEMPLO = {
  texto: "Av. Universitaria 1801, San Martín de Porres",
  latitud: -12.0561,
  longitud: -77.0844,
};

function argumento(nombre: string): string | undefined {
  const indice = process.argv.indexOf(`--${nombre}`);
  return indice >= 0 ? process.argv[indice + 1] : undefined;
}

function haceMinutos(minutos: number): Timestamp {
  return Timestamp.fromMillis(Date.now() - minutos * 60_000);
}

function codigoRecojo(): string {
  return String(Math.floor(Math.random() * 10_000)).padStart(4, "0");
}

async function leerCarta(localUid: string): Promise<Carta> {
  const local = db.collection("restaurantes").doc(localUid);
  const platos = await local.collection("platos").limit(3).get();
  const menu = (await local.collection("menus").doc(fechaLima()).get()).data();
  const entradas: unknown[] = Array.isArray(menu?.entradas) ? menu.entradas : [];
  const segundos: unknown[] = Array.isArray(menu?.segundos) ? menu.segundos : [];
  return {
    platos: platos.empty
      ? [
          { id: "ejemplo-lomo", nombre: "Lomo saltado", precio: 2400 },
          { id: "ejemplo-aji", nombre: "Ají de gallina", precio: 1800 },
        ]
      : platos.docs.map((plato) => ({
          id: plato.id,
          nombre: String(plato.get("nombre")),
          precio: Number(plato.get("precio")),
        })),
    menu: {
      fecha: fechaLima(),
      precio: typeof menu?.precio === "number" ? menu.precio : 1400,
      entrada: nombreDeOpcion(entradas[0]) ?? "Papa a la huancaína",
      segundo: nombreDeOpcion(segundos[0]) ?? "Arroz con pollo",
    },
  };
}

function nombreDeOpcion(opcion: unknown): string | undefined {
  if (typeof opcion === "string") return opcion;
  if (typeof opcion === "object" && opcion !== null && "nombre" in opcion) return String(opcion.nombre);
  return undefined;
}

function itemPlato(carta: Carta, indice: number, cantidad = 1): Item {
  const plato = carta.platos[indice % carta.platos.length];
  return { tipo: "plato", platoId: plato.id, cantidad, nombre: plato.nombre, precio: plato.precio };
}

function itemMenu(carta: Carta, cantidad = 1): Item {
  return {
    tipo: "menu",
    cantidad,
    nombre: "Menú del día",
    precio: carta.menu.precio,
    fecha: carta.menu.fecha,
    entrada: carta.menu.entrada,
    segundo: carta.menu.segundo,
  };
}

async function sembrar(localUid: string, comensalUid: string): Promise<void> {
  const localRef = db.collection("restaurantes").doc(localUid);
  const local = await localRef.get();
  if (!local.exists) throw new Error(`No existe restaurantes/${localUid}`);
  const comensal = await db.collection("usuarios").doc(comensalUid).get();
  if (!comensal.exists) throw new Error(`No existe usuarios/${comensalUid}`);
  if (comensal.get("rol") !== "comensal") console.warn(`Ojo: usuarios/${comensalUid} no tiene rol comensal`);

  const carta = await leerCarta(localUid);
  const base = {
    comensalUid,
    comensalNombre: String(comensal.get("nombre") ?? "Comensal de prueba"),
    restauranteUid: localUid,
    restauranteNombre: String(local.get("nombre")),
    ejemplo: true,
  };

  const pedidos: DocumentData[] = [
    { modalidad: "recojo", metodoPago: "efectivo", pago: "por_cobrar", estado: "recibido",
      items: [itemMenu(carta), itemPlato(carta, 0)], minutos: 2 },
    { modalidad: "delivery", metodoPago: "billetera", pago: "pagado", estado: "recibido",
      items: [itemPlato(carta, 1, 2)], minutos: 6 },
    { modalidad: "recojo", metodoPago: "tarjeta", pago: "pagado", estado: "preparando",
      items: [itemMenu(carta, 2)], minutos: 20, aceptado: 18, estimado: 15 },
    { modalidad: "recojo", metodoPago: "billetera", pago: "pagado", estado: "listo",
      items: [itemPlato(carta, 2)], minutos: 35, aceptado: 33, estimado: 20 },
    { modalidad: "delivery", metodoPago: "efectivo", pago: "por_cobrar", estado: "en_camino",
      items: [itemMenu(carta), itemPlato(carta, 1)], minutos: 45, aceptado: 43, estimado: 30 },
    { modalidad: "recojo", metodoPago: "tarjeta", pago: "pagado", estado: "rechazado",
      items: [itemPlato(carta, 0)], minutos: 60,
      rechazo: { motivo: "plato_agotado", platoAgotado: carta.platos[0].nombre, automatico: false } },
    { modalidad: "recojo", metodoPago: "billetera", pago: "pagado", estado: "rechazado",
      items: [itemMenu(carta)], minutos: 90, rechazo: { motivo: "sin_respuesta", automatico: true } },
    { modalidad: "delivery", metodoPago: "efectivo", pago: "por_cobrar", estado: "cancelado",
      items: [itemPlato(carta, 2)], minutos: 120 },
  ];
  const resenas = [
    { estrellas: 5, comentario: "Todo muy rico y llegó a tiempo.", dias: 1, gusto: true },
    { estrellas: 4, comentario: "Buen menú, el postre podría mejorar.", dias: 2, gusto: true },
    { estrellas: 2, comentario: "La comida llegó fría y tardó mucho.", dias: 3, gusto: false },
    { estrellas: 1, comentario: "Me cobraron de más y el plato vino incompleto.", dias: 4, gusto: false },
  ];
  for (const resena of resenas) {
    pedidos.push({ modalidad: "recojo", metodoPago: "efectivo", pago: "por_cobrar", estado: "entregado",
      items: [itemPlato(carta, resena.dias)], minutos: resena.dias * 1440, aceptado: resena.dias * 1440 - 2,
      estimado: 15, resena });
  }

  for (const pedido of pedidos) {
    const ref = db.collection("pedidos").doc();
    const items = pedido.items as Item[];
    const creadoEn = haceMinutos(pedido.minutos);
    await db.runTransaction(async (transaccion) => {
      const numero = ((await transaccion.get(localRef)).get("ultimoNumeroPedido") ?? 0) + 1;
      transaccion.update(localRef, { ultimoNumeroPedido: numero });
      transaccion.set(ref, {
        ...base,
        numero,
        modalidad: pedido.modalidad,
        ...(pedido.modalidad === "delivery" ? { direccion: DIRECCION_EJEMPLO } : { codigoRecojo: codigoRecojo() }),
        metodoPago: pedido.metodoPago,
        pago: pedido.estado === "rechazado" || pedido.estado === "cancelado"
          ? (pedido.pago === "pagado" ? "revertido" : pedido.pago)
          : pedido.pago,
        items,
        total: items.reduce((suma, item) => suma + item.precio * item.cantidad, 0),
        estado: pedido.estado,
        ...(pedido.estimado !== undefined
          ? {
              minutosEstimados: pedido.estimado,
              aceptadoEn: haceMinutos(pedido.aceptado),
              listoAprox: haceMinutos(pedido.aceptado - pedido.estimado),
            }
          : {}),
        ...(pedido.rechazo !== undefined ? { rechazo: pedido.rechazo } : {}),
        ...(pedido.estado === "entregado" ? { entregadoEn: haceMinutos(pedido.aceptado - pedido.estimado) } : {}),
        creadoEn,
        actualizadoEn: creadoEn,
      });
    });

    if (pedido.resena !== undefined) {
      const platos = Object.fromEntries(
        items.filter((item) => item.platoId !== undefined).map((item) => [item.platoId, pedido.resena.gusto]),
      );
      await localRef.collection("resenas").doc(ref.id).set({
        comensalUid,
        comensalNombre: base.comensalNombre,
        estrellas: pedido.resena.estrellas,
        comentario: pedido.resena.comentario,
        platos,
        ejemplo: true,
        creadoEn: haceMinutos(pedido.minutos - 60),
      });
    }
  }

  const reservas = [
    { fecha: fechaLima(1), hora: "13:00", personas: 4, estado: "pendiente" },
    { fecha: fechaLima(2), hora: "12:30", personas: 2, estado: "confirmada", respondida: true },
    { fecha: fechaLima(1), hora: "14:00", personas: 3, estado: "pendiente",
      horarioAnterior: { fecha: fechaLima(1), hora: "12:00" } },
    { fecha: fechaLima(3), hora: "13:30", personas: 8, estado: "rechazada", respondida: true,
      rechazo: { motivo: "grupo_grande", automatico: false } },
    { fecha: fechaLima(2), hora: "15:00", personas: 2, estado: "cancelada" },
  ];
  for (const reserva of reservas) {
    const ahora = FieldValue.serverTimestamp();
    await db.collection("reservas").add({
      ...base,
      fecha: reserva.fecha,
      hora: reserva.hora,
      personas: reserva.personas,
      estado: reserva.estado,
      ...(reserva.rechazo !== undefined ? { rechazo: reserva.rechazo } : {}),
      ...(reserva.horarioAnterior !== undefined ? { horarioAnterior: reserva.horarioAnterior } : {}),
      ...(reserva.respondida === true ? { respondidaEn: ahora } : {}),
      creadoEn: ahora,
      actualizadoEn: ahora,
    });
    if (reserva.estado === "pendiente" || reserva.estado === "confirmada") {
      await cambiarFranja(localUid, reserva.fecha, reserva.hora, reserva.personas);
    }
  }

  await recalcularCalificacion(localUid);
  console.log(`Listo: ${pedidos.length} pedidos, ${reservas.length} reservas y ${resenas.length} reseñas de ejemplo.`);
}

async function cambiarFranja(localUid: string, fecha: string, hora: string, personas: number): Promise<void> {
  await db.collection("restaurantes").doc(localUid).collection("franjas")
    .doc(`${fecha}_${hora.replace(":", "")}`)
    .set({ ocupados: FieldValue.increment(personas) }, { merge: true });
}

async function recalcularCalificacion(localUid: string): Promise<void> {
  const localRef = db.collection("restaurantes").doc(localUid);
  const resenas = await localRef.collection("resenas").get();
  if (resenas.empty) {
    await localRef.update({ calificacion: FieldValue.delete() });
    return;
  }
  const suma = resenas.docs.reduce((total, resena) => total + Number(resena.get("estrellas")), 0);
  await localRef.update({
    calificacion: { promedio: Math.round((suma / resenas.size) * 10) / 10, total: resenas.size },
  });
}

async function borrar(localUid: string): Promise<void> {
  const localRef = db.collection("restaurantes").doc(localUid);
  const pedidos = await db.collection("pedidos")
    .where("restauranteUid", "==", localUid).where("ejemplo", "==", true).get();
  const reservas = await db.collection("reservas")
    .where("restauranteUid", "==", localUid).where("ejemplo", "==", true).get();
  const resenas = await localRef.collection("resenas").where("ejemplo", "==", true).get();

  for (const reserva of reservas.docs) {
    if (reserva.get("estado") === "pendiente" || reserva.get("estado") === "confirmada") {
      await cambiarFranja(localUid, reserva.get("fecha"), reserva.get("hora"), -reserva.get("personas"));
    }
  }
  await Promise.all([...pedidos.docs, ...reservas.docs, ...resenas.docs].map((documento) => documento.ref.delete()));
  await recalcularCalificacion(localUid);
  console.log(`Borrados: ${pedidos.size} pedidos, ${reservas.size} reservas y ${resenas.size} reseñas de ejemplo.`);
}

async function principal(): Promise<void> {
  const local = argumento("local");
  const comensal = argumento("comensal");
  if (local === undefined) {
    console.error("Uso: npm run datos-ejemplo -- --local <uid del local> --comensal <uid del comensal>");
    console.error("     npm run datos-ejemplo -- --local <uid del local> --borrar");
    process.exit(1);
  }
  if (process.argv.includes("--borrar")) {
    await borrar(local);
  } else if (comensal !== undefined) {
    await sembrar(local, comensal);
  } else {
    console.error("Falta --comensal <uid del comensal>");
    process.exit(1);
  }
}

principal().catch((error) => {
  console.error(error instanceof Error ? error.message : error);
  process.exit(1);
});
