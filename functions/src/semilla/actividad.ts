import { FieldValue, Timestamp, getFirestore, type DocumentData } from "firebase-admin/firestore";
import { fechaLima } from "../tiempo.js";
import type { CartaCargada } from "./carta.js";
import { COMENSALES_DE_PRUEBA, LOCALES_DE_PRUEBA, type ComensalDePrueba } from "./cuentas.js";

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

interface PedidoDePrueba {
  modalidad: "recojo" | "delivery";
  metodoPago: "efectivo" | "billetera" | "tarjeta";
  estado: string;
  items: Item[];
  minutos: number;
  estimado?: number;
  rechazo?: DocumentData;
  resena?: { estrellas: number; comentario: string; gusto: boolean };
}

export interface ActividadCargada {
  pedidos: number;
  reservas: number;
  resenas: number;
}

const DIRECCION_DE_ENTREGA = {
  texto: "Facultad · Av. Germán Amézaga 375, Ciudad Universitaria, Lima",
  latitud: -12.0563,
  longitud: -77.0844,
};

const MINUTOS_POR_DIA = 1_440;

const RESENAS = [
  { estrellas: 5, comentario: "Todo muy rico y llegó a tiempo.", gusto: true },
  { estrellas: 4, comentario: "Buena porción, el postre podría mejorar.", gusto: true },
  { estrellas: 2, comentario: "La comida llegó fría y tardó mucho.", gusto: false },
];

function haceMinutos(minutos: number): Timestamp {
  return Timestamp.fromMillis(Date.now() - minutos * 60_000);
}

function codigoRecojo(): string {
  return String(Math.floor(Math.random() * 10_000)).padStart(4, "0");
}

function itemPlato(carta: CartaCargada, indice: number, cantidad = 1): Item {
  const plato = carta.platos[indice % carta.platos.length];
  return { tipo: "plato", platoId: plato.id, cantidad, nombre: plato.nombre, precio: plato.precio };
}

function itemMenu(carta: CartaCargada, indice = 0, cantidad = 1): Item {
  return {
    tipo: "menu",
    cantidad,
    nombre: "Menú del día",
    precio: carta.menu.precio,
    fecha: carta.menu.fecha,
    entrada: carta.menu.entradas[indice % carta.menu.entradas.length],
    segundo: carta.menu.segundos[indice % carta.menu.segundos.length],
  };
}

function pedidosDe(carta: CartaCargada, indiceLocal: number): PedidoDePrueba[] {
  const pedidos: PedidoDePrueba[] = [
    { modalidad: "recojo", metodoPago: "efectivo", estado: "recibido",
      items: [itemMenu(carta), itemPlato(carta, 0)], minutos: 2 },
    { modalidad: "recojo", metodoPago: "tarjeta", estado: "preparando",
      items: [itemMenu(carta, 1, 2)], minutos: 20, estimado: 15 },
    { modalidad: "recojo", metodoPago: "billetera", estado: "listo",
      items: [itemPlato(carta, 1)], minutos: 35, estimado: 20 },
    { modalidad: "delivery", metodoPago: "efectivo", estado: "en_camino",
      items: [itemMenu(carta), itemPlato(carta, 3)], minutos: 45, estimado: 30 },
    { modalidad: "recojo", metodoPago: "tarjeta", estado: "rechazado",
      items: [itemPlato(carta, 2)], minutos: 60,
      rechazo: { motivo: "plato_agotado", platoAgotado: itemPlato(carta, 2).nombre, automatico: false } },
    { modalidad: "delivery", metodoPago: "billetera", estado: "rechazado",
      items: [itemMenu(carta)], minutos: 90, rechazo: { motivo: "sin_respuesta", automatico: true } },
    { modalidad: "recojo", metodoPago: "efectivo", estado: "cancelado",
      items: [itemPlato(carta, 4)], minutos: 120 },
  ];
  RESENAS.forEach((resena, indice) => {
    const desplazada = RESENAS[(indice + indiceLocal) % RESENAS.length];
    pedidos.push({ modalidad: indice % 2 === 0 ? "recojo" : "delivery", metodoPago: "billetera", estado: "entregado",
      items: [itemPlato(carta, indice), itemMenu(carta, indice)], minutos: (indice + 1) * MINUTOS_POR_DIA,
      estimado: 15, resena: desplazada });
  });
  for (let semana = 1; semana <= 7; semana++) {
    const cantidad = 1 + ((semana + indiceLocal) % 3);
    for (let vez = 0; vez < cantidad; vez++) {
      pedidos.push({ modalidad: "recojo", metodoPago: vez % 2 === 0 ? "efectivo" : "tarjeta", estado: "entregado",
        items: [itemPlato(carta, semana + vez)], minutos: (semana * 7 + vez) * MINUTOS_POR_DIA, estimado: 15 });
    }
  }
  return pedidos;
}

function pagoDe(pedido: PedidoDePrueba): string {
  if (pedido.metodoPago === "efectivo") return "por_cobrar";
  return pedido.estado === "rechazado" || pedido.estado === "cancelado" ? "revertido" : "pagado";
}

function franja(localUid: string, fecha: string, hora: string) {
  return getFirestore().collection("restaurantes").doc(localUid).collection("franjas")
    .doc(`${fecha}_${hora.replace(":", "")}`);
}

export async function cargarActividadDePrueba(cartas: CartaCargada[]): Promise<ActividadCargada> {
  const db = getFirestore();
  const total: ActividadCargada = { pedidos: 0, reservas: 0, resenas: 0 };

  for (const [indiceLocal, local] of LOCALES_DE_PRUEBA.entries()) {
    const carta = cartas.find((cargada) => cargada.localUid === local.uid);
    if (carta === undefined) continue;
    const localRef = db.collection("restaurantes").doc(local.uid);
    const lote = db.batch();
    const comensalDe = (indice: number): ComensalDePrueba =>
      COMENSALES_DE_PRUEBA[(indice + indiceLocal) % COMENSALES_DE_PRUEBA.length];

    const pedidos = pedidosDe(carta, indiceLocal);
    const estrellas: number[] = [];
    pedidos.forEach((pedido, indice) => {
      const comensal = comensalDe(indice);
      const ref = db.collection("pedidos").doc();
      const creadoEn = haceMinutos(pedido.minutos);
      const aceptado = pedido.minutos - 2;
      lote.set(ref, {
        numero: indice + 1,
        comensalUid: comensal.uid,
        comensalNombre: comensal.nombre,
        restauranteUid: local.uid,
        restauranteNombre: local.nombre,
        modalidad: pedido.modalidad,
        ...(pedido.modalidad === "delivery" ? { direccion: DIRECCION_DE_ENTREGA } : { codigoRecojo: codigoRecojo() }),
        metodoPago: pedido.metodoPago,
        pago: pagoDe(pedido),
        items: pedido.items,
        total: pedido.items.reduce((suma, item) => suma + item.precio * item.cantidad, 0),
        estado: pedido.estado,
        ...(pedido.estimado !== undefined
          ? {
              minutosEstimados: pedido.estimado,
              aceptadoEn: haceMinutos(aceptado),
              listoAprox: haceMinutos(aceptado - pedido.estimado),
            }
          : {}),
        ...(pedido.rechazo !== undefined ? { rechazo: pedido.rechazo } : {}),
        ...(pedido.estado === "entregado" ? { entregadoEn: haceMinutos(aceptado - (pedido.estimado ?? 15)) } : {}),
        semilla: true,
        creadoEn,
        actualizadoEn: creadoEn,
      });

      if (pedido.resena !== undefined) {
        const platos = Object.fromEntries(
          pedido.items.filter((item) => item.platoId !== undefined).map((item) => [item.platoId, pedido.resena?.gusto]),
        );
        lote.set(localRef.collection("resenas").doc(ref.id), {
          comensalUid: comensal.uid,
          comensalNombre: comensal.nombre,
          estrellas: pedido.resena.estrellas,
          comentario: pedido.resena.comentario,
          platos,
          semilla: true,
          creadoEn: haceMinutos(pedido.minutos - 60),
        });
        estrellas.push(pedido.resena.estrellas);
      }
    });

    const reservas = [
      { fecha: fechaLima(1), hora: "13:00", personas: 4, estado: "pendiente" },
      { fecha: fechaLima(2), hora: "12:30", personas: 2, estado: "confirmada", respondida: true },
      { fecha: fechaLima(1), hora: "14:00", personas: 3, estado: "pendiente",
        horarioAnterior: { fecha: fechaLima(1), hora: "12:00" } },
      { fecha: fechaLima(3), hora: "13:30", personas: 8, estado: "rechazada", respondida: true,
        rechazo: { motivo: "grupo_grande", automatico: false } },
      { fecha: fechaLima(2), hora: "15:00", personas: 2, estado: "cancelada" },
    ];
    reservas.forEach((reserva, indice) => {
      const comensal = comensalDe(indice);
      const ahora = FieldValue.serverTimestamp();
      lote.set(db.collection("reservas").doc(), {
        comensalUid: comensal.uid,
        comensalNombre: comensal.nombre,
        restauranteUid: local.uid,
        restauranteNombre: local.nombre,
        fecha: reserva.fecha,
        hora: reserva.hora,
        personas: reserva.personas,
        estado: reserva.estado,
        ...(reserva.rechazo !== undefined ? { rechazo: reserva.rechazo } : {}),
        ...(reserva.horarioAnterior !== undefined ? { horarioAnterior: reserva.horarioAnterior } : {}),
        ...(reserva.respondida === true ? { respondidaEn: ahora } : {}),
        semilla: true,
        creadoEn: ahora,
        actualizadoEn: ahora,
      });
      if (reserva.estado === "pendiente" || reserva.estado === "confirmada") {
        lote.set(franja(local.uid, reserva.fecha, reserva.hora),
          { ocupados: FieldValue.increment(reserva.personas) }, { merge: true });
      }
    });

    const suma = estrellas.reduce((acumulado, valor) => acumulado + valor, 0);
    lote.update(localRef, {
      ultimoNumeroPedido: pedidos.length,
      calificacion: { promedio: suma / estrellas.length, total: estrellas.length },
    });
    await lote.commit();

    total.pedidos += pedidos.length;
    total.reservas += reservas.length;
    total.resenas += estrellas.length;
  }
  return total;
}
