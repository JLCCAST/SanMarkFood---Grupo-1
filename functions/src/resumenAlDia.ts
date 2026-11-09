import type { DocumentSnapshot } from "firebase-admin/firestore";

export function totalDeResenas(local: DocumentSnapshot): number {
  return Number(local.get("calificacion.total") ?? 0);
}

export function resumenAlDia(local: DocumentSnapshot): boolean {
  return local.get("resumenResenas.totalResenas") === totalDeResenas(local);
}
