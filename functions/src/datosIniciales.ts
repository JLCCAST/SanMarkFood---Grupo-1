import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions";
import { ErrorFuncion, funcionHttps, leerOpcion } from "./https.js";
import { cargarActividadDePrueba } from "./semilla/actividad.js";
import { borrarDatosDePrueba } from "./semilla/borrar.js";
import { cargarCartasDePrueba } from "./semilla/carta.js";
import {
  COMENSALES_DE_PRUEBA,
  CONTRASENA_DE_PRUEBA,
  LOCALES_DE_PRUEBA,
  crearComensalesDePrueba,
  crearLocalesDePrueba,
} from "./semilla/cuentas.js";

const ACCIONES = ["cargar", "borrar"] as const;

export const datosIniciales = funcionHttps(async ({ uid, cuerpo }) => {
  const usuario = await getFirestore().collection("usuarios").doc(uid).get();
  if (usuario.get("rol") !== "administrador") throw new ErrorFuncion("no_autorizado", 403);

  const accion = leerOpcion(cuerpo, "accion", ACCIONES);
  const borrados = await borrarDatosDePrueba();
  if (accion === "borrar") {
    logger.info("Datos iniciales borrados", borrados);
    return { accion, cuentasBorradas: borrados.cuentas, pedidos: borrados.pedidos, reservas: borrados.reservas };
  }

  const locales = await crearLocalesDePrueba();
  const comensales = await crearComensalesDePrueba();
  const cartas = await cargarCartasDePrueba();
  const actividad = await cargarActividadDePrueba(cartas);
  const resumen = {
    accion,
    locales,
    comensales,
    platos: cartas.reduce((suma, carta) => suma + carta.platos.length, 0),
    menus: cartas.length,
    ...actividad,
    contrasena: CONTRASENA_DE_PRUEBA,
    cuentas: [
      ...LOCALES_DE_PRUEBA.map((local) => ({ rol: "restaurante", nombre: local.nombre, correo: local.correo })),
      ...COMENSALES_DE_PRUEBA.map((comensal) => ({ rol: "comensal", nombre: comensal.nombre, correo: comensal.correo })),
    ],
  };
  logger.info("Datos iniciales cargados", { ...resumen, cuentas: resumen.cuentas.length });
  return resumen;
});
