import type { DocumentData } from "firebase-admin/firestore";
import type { Notification } from "firebase-admin/messaging";

const MOTIVOS: Record<string, string> = {
  datos_incompletos: "Datos incompletos",
  direccion_no_verificable: "No pudimos verificar la dirección",
  local_duplicado: "Local duplicado",
};

export function armarAviso(local: DocumentData): Notification {
  const nombre = typeof local.nombre === "string" && local.nombre.trim() !== "" ? local.nombre.trim() : "Tu local";
  if (local.estado === "aprobado") {
    return {
      title: "¡Tu local fue aprobado!",
      body: `${nombre} ya aparece en el mapa de San Mark Food.`,
    };
  }
  return {
    title: "Revisa el registro de tu local",
    body: `${motivoDelRechazo(local.rechazo)}. Corrige tus datos en la app y vuelve a enviarlos.`,
  };
}

function motivoDelRechazo(rechazo: DocumentData | undefined): string {
  const valores: unknown[] = Array.isArray(rechazo?.motivos)
    ? rechazo.motivos
    : rechazo?.motivo !== undefined
      ? [rechazo.motivo]
      : [];
  const conocidos = valores
    .filter((valor): valor is string => typeof valor === "string" && Object.hasOwn(MOTIVOS, valor))
    .map((valor) => MOTIVOS[valor]);
  if (conocidos.length > 0) return unirConY(conocidos);

  const detalle = typeof rechazo?.detalle === "string" ? rechazo.detalle.trim().replace(/\.+$/, "") : "";
  return detalle !== "" ? detalle : "Hay datos que corregir";
}

function unirConY(partes: string[]): string {
  const enOracion = partes.map((parte, indice) =>
    indice === 0 ? parte : parte.charAt(0).toLocaleLowerCase("es-PE") + parte.slice(1),
  );
  if (enOracion.length === 1) return enOracion[0];
  return `${enOracion.slice(0, -1).join(", ")} y ${enOracion[enOracion.length - 1]}`;
}
