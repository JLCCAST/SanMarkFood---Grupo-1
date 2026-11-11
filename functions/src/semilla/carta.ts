import { FieldValue, getFirestore } from "firebase-admin/firestore";
import { fechaLima } from "../tiempo.js";
import { LOCALES_DE_PRUEBA, type Categoria } from "./cuentas.js";

interface PlatoDePrueba {
  nombre: string;
  descripcion: string;
  precio: number;
}

interface CategoriaDePrueba {
  nombre: string;
  platos: PlatoDePrueba[];
}

interface MenuDePrueba {
  precio: number;
  entradas: string[];
  segundos: string[];
  refresco: string;
  postre: string;
}

interface CartaDePrueba {
  categorias: CategoriaDePrueba[];
  menu: MenuDePrueba;
}

export interface PlatoCargado {
  id: string;
  nombre: string;
  precio: number;
}

export interface CartaCargada {
  localUid: string;
  platos: PlatoCargado[];
  menu: { fecha: string; precio: number; entradas: string[]; segundos: string[] };
}

export const HORA_FIN_DEL_MENU = "23:00";

const CARTAS: Record<Categoria, CartaDePrueba> = {
  criolla: {
    categorias: [
      {
        nombre: "Platos de fondo",
        platos: [
          { nombre: "Lomo saltado", descripcion: "Con papas fritas y arroz", precio: 2400 },
          { nombre: "Ají de gallina", descripcion: "Con arroz, papa y aceituna", precio: 1800 },
          { nombre: "Seco de res con frejoles", descripcion: "Al estilo norteño", precio: 2200 },
        ],
      },
      {
        nombre: "Bebidas",
        platos: [
          { nombre: "Chicha morada", descripcion: "Jarra de 1 litro", precio: 800 },
          { nombre: "Limonada", descripcion: "Vaso grande", precio: 500 },
        ],
      },
    ],
    menu: {
      precio: 1400,
      entradas: ["Papa a la huancaína", "Causa limeña", "Sopa de casa"],
      segundos: ["Ají de gallina", "Arroz con pollo", "Tallarines verdes"],
      refresco: "Chicha morada",
      postre: "Mazamorra morada",
    },
  },
  chifa: {
    categorias: [
      {
        nombre: "Arroces y tallarines",
        platos: [
          { nombre: "Arroz chaufa de pollo", descripcion: "Con tortilla y cebollita china", precio: 1600 },
          { nombre: "Tallarín saltado", descripcion: "Con verduras y carne", precio: 1900 },
          { nombre: "Aeropuerto", descripcion: "Chaufa y tallarín con todo", precio: 2100 },
        ],
      },
      {
        nombre: "Especialidades",
        platos: [
          { nombre: "Pollo chi jau kay", descripcion: "Con salsa de ostión", precio: 2300 },
          { nombre: "Wantán frito", descripcion: "Seis unidades con salsa tamarindo", precio: 1000 },
        ],
      },
    ],
    menu: {
      precio: 1300,
      entradas: ["Sopa wantán", "Wantán frito"],
      segundos: ["Chaufa de pollo", "Tipakay", "Pollo enrollado"],
      refresco: "Té helado",
      postre: "Gelatina",
    },
  },
  polleria: {
    categorias: [
      {
        nombre: "Pollos a la brasa",
        platos: [
          { nombre: "1/4 de pollo", descripcion: "Con papas y ensalada", precio: 1700 },
          { nombre: "1/2 pollo", descripcion: "Con papas y ensalada", precio: 3200 },
          { nombre: "Pollo entero", descripcion: "Con papas familiares y ensalada", precio: 6000 },
        ],
      },
      {
        nombre: "Parrillas",
        platos: [
          { nombre: "Anticuchos", descripcion: "Dos palitos con papa dorada", precio: 1500 },
          { nombre: "Mollejitas a la parrilla", descripcion: "Con papas fritas", precio: 1400 },
        ],
      },
    ],
    menu: {
      precio: 1500,
      entradas: ["Ensalada fresca", "Sopa de pollo"],
      segundos: ["1/8 de pollo a la brasa", "Pollo broaster", "Mollejitas saltadas"],
      refresco: "Maracuyá",
      postre: "Arroz con leche",
    },
  },
  marina: {
    categorias: [
      {
        nombre: "Ceviches",
        platos: [
          { nombre: "Ceviche de pescado", descripcion: "Con camote y choclo", precio: 2800 },
          { nombre: "Ceviche mixto", descripcion: "Pescado y mariscos", precio: 3200 },
          { nombre: "Leche de tigre", descripcion: "Con chicharrón de pota", precio: 1500 },
        ],
      },
      {
        nombre: "Calientes",
        platos: [
          { nombre: "Arroz con mariscos", descripcion: "Con salsa criolla", precio: 2900 },
          { nombre: "Chicharrón de pescado", descripcion: "Con yuca frita", precio: 2600 },
        ],
      },
    ],
    menu: {
      precio: 1800,
      entradas: ["Ceviche de pota", "Causa de atún"],
      segundos: ["Arroz con mariscos", "Pescado frito", "Sudado de pescado"],
      refresco: "Limonada",
      postre: "Mazamorra de piña",
    },
  },
  vegetariana: {
    categorias: [
      {
        nombre: "Platos",
        platos: [
          { nombre: "Tacu tacu con champiñones", descripcion: "Con salsa criolla", precio: 1800 },
          { nombre: "Quinotto de verduras", descripcion: "Con queso fresco", precio: 2000 },
          { nombre: "Hamburguesa de lentejas", descripcion: "Con papas al horno", precio: 1700 },
        ],
      },
      {
        nombre: "Jugos",
        platos: [
          { nombre: "Jugo verde", descripcion: "Espinaca, piña y apio", precio: 900 },
          { nombre: "Jugo de papaya", descripcion: "Vaso grande", precio: 700 },
        ],
      },
    ],
    menu: {
      precio: 1300,
      entradas: ["Crema de zapallo", "Ensalada de quinua"],
      segundos: ["Tacu tacu con champiñones", "Tortilla de verduras", "Guiso de lentejas"],
      refresco: "Agua de cebada",
      postre: "Fruta picada",
    },
  },
  otra: {
    categorias: [
      {
        nombre: "Sánguches",
        platos: [
          { nombre: "Butifarra", descripcion: "Jamón del país con salsa criolla", precio: 1200 },
          { nombre: "Pan con chicharrón", descripcion: "Con camote frito", precio: 1400 },
          { nombre: "Triple", descripcion: "Palta, huevo y tomate", precio: 900 },
        ],
      },
      {
        nombre: "Para acompañar",
        platos: [
          { nombre: "Papas fritas", descripcion: "Porción mediana", precio: 600 },
          { nombre: "Café pasado", descripcion: "Taza grande", precio: 500 },
        ],
      },
    ],
    menu: {
      precio: 1200,
      entradas: ["Sopa criolla", "Ensalada rusa"],
      segundos: ["Pan con chicharrón", "Butifarra con papas", "Tortilla de verduras"],
      refresco: "Emoliente",
      postre: "Queque de naranja",
    },
  },
};

export async function cargarCartasDePrueba(): Promise<CartaCargada[]> {
  const db = getFirestore();
  const hoy = fechaLima();
  const ayer = fechaLima(-1);
  const cargadas: CartaCargada[] = [];

  for (const [indiceLocal, local] of LOCALES_DE_PRUEBA.entries()) {
    const carta = CARTAS[local.categoria];
    const localRef = db.collection("restaurantes").doc(local.uid);
    const lote = db.batch();
    const ahora = FieldValue.serverTimestamp();
    const platos: PlatoCargado[] = [];

    carta.categorias.forEach((categoria, orden) => {
      const categoriaRef = localRef.collection("categorias").doc();
      lote.set(categoriaRef, { nombre: categoria.nombre, orden });
      categoria.platos.forEach((plato, indicePlato) => {
        const platoRef = localRef.collection("platos").doc();
        const agotado = orden === 0 && indicePlato === categoria.platos.length - 1 && indiceLocal % 2 === 0;
        lote.set(platoRef, {
          nombre: plato.nombre,
          descripcion: plato.descripcion,
          precio: plato.precio,
          categoriaId: categoriaRef.id,
          ...(agotado ? { agotadoEl: hoy } : {}),
          creadoEn: ahora,
          actualizadoEn: ahora,
        });
        platos.push({ id: platoRef.id, nombre: plato.nombre, precio: plato.precio });
      });
    });

    const opciones = (nombres: string[], agotarUltima: boolean) =>
      nombres.map((nombre, indice) => ({ nombre, agotado: agotarUltima && indice === nombres.length - 1 }));
    const menu = {
      precio: carta.menu.precio,
      entradas: opciones(carta.menu.entradas, false),
      segundos: opciones(carta.menu.segundos, true),
      refresco: carta.menu.refresco,
      postre: carta.menu.postre,
      horaFin: HORA_FIN_DEL_MENU,
      origen: "cero",
      publicadoEn: ahora,
      actualizadoEn: ahora,
    };
    lote.set(localRef.collection("menus").doc(ayer), { ...menu, estado: "terminado" });
    lote.set(localRef.collection("menus").doc(hoy), { ...menu, estado: "publicado" });

    const precios = platos.map((plato) => plato.precio);
    lote.update(localRef, {
      menuHoy: { fecha: hoy, precio: carta.menu.precio, horaFin: HORA_FIN_DEL_MENU, estado: "publicado" },
      rangoCarta: { min: Math.min(...precios), max: Math.max(...precios) },
    });
    await lote.commit();

    cargadas.push({
      localUid: local.uid,
      platos,
      menu: {
        fecha: hoy,
        precio: carta.menu.precio,
        entradas: carta.menu.entradas,
        segundos: carta.menu.segundos.slice(0, -1),
      },
    });
  }
  return cargadas;
}
