import { getAuth } from "firebase-admin/auth";
import { FieldValue, GeoPoint, getFirestore } from "firebase-admin/firestore";

export const CONTRASENA_DE_PRUEBA = "SanMark2026!";

export type Categoria = "criolla" | "chifa" | "polleria" | "marina" | "vegetariana" | "otra";

export interface LocalDePrueba {
  uid: string;
  correo: string;
  nombre: string;
  categoria: Categoria;
  direccion: string;
  telefono: string;
  latitud: number;
  longitud: number;
}

export interface ComensalDePrueba {
  uid: string;
  correo: string;
  nombre: string;
  telefono: string;
}

export const LOCALES_DE_PRUEBA: LocalDePrueba[] = [
  {
    uid: "semilla-local-criolla",
    correo: "criolla@datos.sanmarkfood.test",
    nombre: "La Sazón de Doña Carmen",
    categoria: "criolla",
    direccion: "Av. Universitaria 1820, San Miguel",
    telefono: "912345601",
    latitud: -12.0598,
    longitud: -77.0822,
  },
  {
    uid: "semilla-local-chifa",
    correo: "chifa@datos.sanmarkfood.test",
    nombre: "Chifa Dragón de la U",
    categoria: "chifa",
    direccion: "Av. Venezuela 3456, Lima",
    telefono: "912345602",
    latitud: -12.0558,
    longitud: -77.0790,
  },
  {
    uid: "semilla-local-polleria",
    correo: "polleria@datos.sanmarkfood.test",
    nombre: "Pollería El Cholo Dorado",
    categoria: "polleria",
    direccion: "Av. Amézaga 412, Lima",
    telefono: "912345603",
    latitud: -12.0520,
    longitud: -77.0870,
  },
  {
    uid: "semilla-local-marina",
    correo: "marina@datos.sanmarkfood.test",
    nombre: "Cevichería Puerto San Marcos",
    categoria: "marina",
    direccion: "Av. Mariano Cornejo 2210, Lima",
    telefono: "912345604",
    latitud: -12.0640,
    longitud: -77.0760,
  },
  {
    uid: "semilla-local-vegetariana",
    correo: "vegetariana@datos.sanmarkfood.test",
    nombre: "Verde Campus",
    categoria: "vegetariana",
    direccion: "Jr. Tingo María 980, Breña",
    telefono: "912345605",
    latitud: -12.0590,
    longitud: -77.0720,
  },
  {
    uid: "semilla-local-otra",
    correo: "sangucheria@datos.sanmarkfood.test",
    nombre: "Sanguchería La Esquina",
    categoria: "otra",
    direccion: "Av. Colonial 3100, Cercado de Lima",
    telefono: "912345606",
    latitud: -12.0490,
    longitud: -77.0800,
  },
];

export const COMENSALES_DE_PRUEBA: ComensalDePrueba[] = [
  { uid: "semilla-comensal-1", correo: "lucia@datos.sanmarkfood.test", nombre: "Lucía Ramos", telefono: "987654301" },
  { uid: "semilla-comensal-2", correo: "diego@datos.sanmarkfood.test", nombre: "Diego Quispe", telefono: "987654302" },
  { uid: "semilla-comensal-3", correo: "valeria@datos.sanmarkfood.test", nombre: "Valeria Torres", telefono: "987654303" },
];

export const UIDS_DE_LOCALES = LOCALES_DE_PRUEBA.map((local) => local.uid);
export const UIDS_DE_COMENSALES = COMENSALES_DE_PRUEBA.map((comensal) => comensal.uid);

const DIAS = ["lun", "mar", "mie", "jue", "vie", "sab", "dom"];

const HORARIO_TODO_EL_DIA = Object.fromEntries(
  DIAS.map((dia) => [dia, { abierto: true, abre: "00:00", cierra: "23:30" }]),
);

const DIRECCION_FRECUENTE = {
  etiqueta: "Facultad",
  direccionTexto: "Av. Germán Amézaga 375, Ciudad Universitaria, Lima",
  latitud: -12.0563,
  longitud: -77.0844,
};

async function crearCuenta(uid: string, correo: string, nombre: string): Promise<void> {
  await getAuth().createUser({
    uid,
    email: correo,
    emailVerified: true,
    password: CONTRASENA_DE_PRUEBA,
    displayName: nombre,
  });
}

export async function crearLocalesDePrueba(): Promise<number> {
  const db = getFirestore();
  for (const local of LOCALES_DE_PRUEBA) {
    await crearCuenta(local.uid, local.correo, local.nombre);
    const ahora = FieldValue.serverTimestamp();
    const lote = db.batch();
    lote.set(db.collection("usuarios").doc(local.uid), {
      rol: "restaurante",
      correo: local.correo,
      creadoEn: ahora,
    });
    lote.set(db.collection("restaurantes").doc(local.uid), {
      nombre: local.nombre,
      direccion: local.direccion,
      categoria: local.categoria,
      telefono: local.telefono,
      ubicacion: new GeoPoint(local.latitud, local.longitud),
      portadaUrl: "",
      logoUrl: "",
      horario: HORARIO_TODO_EL_DIA,
      aceptaReservas: true,
      cupoPorFranja: 12,
      estado: "aprobado",
      creadoEn: ahora,
      enviadoEn: ahora,
      revisadoEn: ahora,
      actualizadoEn: ahora,
    });
    await lote.commit();
  }
  return LOCALES_DE_PRUEBA.length;
}

export async function crearComensalesDePrueba(): Promise<number> {
  const db = getFirestore();
  for (const comensal of COMENSALES_DE_PRUEBA) {
    await crearCuenta(comensal.uid, comensal.correo, comensal.nombre);
    await db.collection("usuarios").doc(comensal.uid).set({
      rol: "comensal",
      correo: comensal.correo,
      nombre: comensal.nombre,
      telefono: comensal.telefono,
      fotoUrl: "",
      direcciones: [DIRECCION_FRECUENTE],
      notificarReservas: true,
      notificarResenas: true,
      creadoEn: FieldValue.serverTimestamp(),
    });
  }
  return COMENSALES_DE_PRUEBA.length;
}
