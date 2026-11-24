import { strict as assert } from "node:assert";
import { before, test } from "node:test";
import { initializeApp } from "firebase-admin/app";
import { getAuth } from "firebase-admin/auth";
import { getFirestore } from "firebase-admin/firestore";

const FIRESTORE = process.env.FIRESTORE_EMULATOR_HOST;
const AUTH = process.env.FIREBASE_AUTH_EMULATOR_HOST;
const PROYECTO = process.env.GCLOUD_PROJECT ?? "";
if (!FIRESTORE || !AUTH || !PROYECTO.startsWith("demo-")) {
  throw new Error("Esta prueba solo corre en los emuladores: npm run prueba-admin");
}

const FUNCIONES = `http://127.0.0.1:5001/${PROYECTO}/us-central1`;
const DOCUMENTOS = `projects/${PROYECTO}/databases/(default)/documents`;
const CONTRASENA = "prueba-admin-1";
const ROLES = ["comensal", "restaurante", "administrador"] as const;

initializeApp({ projectId: PROYECTO });
const db = getFirestore();
const tokens: Record<string, string> = {};

type Campos = Record<string, { stringValue: string }>;

function texto(valores: Record<string, string>): Campos {
  return Object.fromEntries(Object.entries(valores).map(([campo, valor]) => [campo, { stringValue: valor }]));
}

async function crearCuenta(uid: string): Promise<string> {
  await getAuth().createUser({ uid, email: `${uid}@prueba.test`, password: CONTRASENA, emailVerified: true });
  const respuesta = await fetch(
    `http://${AUTH}/identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=emulador`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email: `${uid}@prueba.test`, password: CONTRASENA, returnSecureToken: true }),
    },
  );
  return ((await respuesta.json()) as { idToken: string }).idToken;
}

async function firestore(token: string, ruta: string, cuerpo?: object): Promise<number> {
  const respuesta = await fetch(`http://${FIRESTORE}/v1/${DOCUMENTOS}${ruta}`, {
    method: cuerpo === undefined ? "GET" : "POST",
    headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
    body: cuerpo === undefined ? undefined : JSON.stringify(cuerpo),
  });
  return respuesta.status;
}

function leer(token: string, ruta: string): Promise<number> {
  return firestore(token, `/${ruta}`);
}

function escribir(token: string, ruta: string, campos: Campos, conRevisadoEn = false): Promise<number> {
  return firestore(token, ":commit", {
    writes: [{
      update: { name: `${DOCUMENTOS}/${ruta}`, fields: campos },
      updateMask: { fieldPaths: Object.keys(campos) },
      updateTransforms: conRevisadoEn ? [{ fieldPath: "revisadoEn", setToServerValue: "REQUEST_TIME" }] : [],
    }],
  });
}

function leerReportes(token: string): Promise<number> {
  return firestore(token, ":runQuery", { structuredQuery: { from: [{ collectionId: "reportes", allDescendants: true }] } });
}

async function datosIniciales(token?: string): Promise<{ estado: number; error?: string }> {
  const respuesta = await fetch(`${FUNCIONES}/datosIniciales`, {
    method: "POST",
    headers: { "Content-Type": "application/json", ...(token ? { Authorization: `Bearer ${token}` } : {}) },
    body: JSON.stringify({ accion: "ninguna" }),
  });
  return { estado: respuesta.status, error: ((await respuesta.json()) as { error?: string }).error };
}

before(async () => {
  for (const rol of ROLES) {
    tokens[rol] = await crearCuenta(rol);
    await db.doc(`usuarios/${rol}`).set({ rol, correo: `${rol}@prueba.test` });
  }
  tokens.nuevo = await crearCuenta("nuevo");
  await db.doc("restaurantes/pendiente").set({ nombre: "Local pendiente", estado: "pendiente" });
  await db.doc("restaurantes/pendiente/reportes/otro").set({ motivo: "ofensivo" });
  await db.doc("pedidos/ajeno").set({ comensalUid: "otro", restauranteUid: "pendiente", estado: "recibido" });
});

test("nadie se registra como administrador", async () => {
  assert.equal(await escribir(tokens.nuevo, "usuarios/nuevo", texto({ rol: "administrador" })), 403);
  assert.equal(await escribir(tokens.nuevo, "usuarios/nuevo", texto({ rol: "comensal" })), 200);
});

test("nadie se cambia el rol a administrador", async () => {
  assert.equal(await escribir(tokens.restaurante, "usuarios/restaurante", texto({ rol: "administrador" })), 403);
  assert.equal(await escribir(tokens.comensal, "usuarios/comensal", texto({ rol: "administrador" })), 403);
});

test("solo el administrador aprueba un local", async () => {
  const aprobar = texto({ estado: "aprobado" });
  assert.equal(await escribir(tokens.restaurante, "restaurantes/pendiente", aprobar, true), 403);
  assert.equal(await escribir(tokens.comensal, "restaurantes/pendiente", aprobar, true), 403);
  assert.equal(await escribir(tokens.administrador, "restaurantes/pendiente", aprobar, true), 200);
});

test("solo el administrador lee la cuenta de otro usuario", async () => {
  assert.equal(await leer(tokens.comensal, "usuarios/restaurante"), 403);
  assert.equal(await leer(tokens.restaurante, "usuarios/comensal"), 403);
  assert.equal(await leer(tokens.administrador, "usuarios/restaurante"), 200);
});

test("solo el administrador lee pedidos ajenos", async () => {
  assert.equal(await leer(tokens.comensal, "pedidos/ajeno"), 403);
  assert.equal(await leer(tokens.restaurante, "pedidos/ajeno"), 403);
  assert.equal(await leer(tokens.administrador, "pedidos/ajeno"), 200);
});

test("solo el administrador lee los reportes de reseñas", async () => {
  assert.equal(await leerReportes(tokens.comensal), 403);
  assert.equal(await leerReportes(tokens.restaurante), 403);
  assert.equal(await leerReportes(tokens.administrador), 200);
});

test("datosIniciales solo atiende al administrador", async () => {
  assert.deepEqual(await datosIniciales(), { estado: 401, error: "no_autenticado" });
  assert.deepEqual(await datosIniciales(tokens.comensal), { estado: 403, error: "no_autorizado" });
  assert.deepEqual(await datosIniciales(tokens.restaurante), { estado: 403, error: "no_autorizado" });
  assert.deepEqual(await datosIniciales(tokens.administrador), { estado: 400, error: "datos_invalidos" });
});
