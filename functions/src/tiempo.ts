const ZONA = "America/Lima";
const DIAS = ["dom", "lun", "mar", "mie", "jue", "vie", "sab"];
const DIAS_EN_INGLES = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

export function fechaLima(dias = 0): string {
  return new Date(Date.now() + dias * 86_400_000).toLocaleDateString("en-CA", { timeZone: ZONA });
}

export function horaLima(fecha = new Date()): string {
  return fecha.toLocaleTimeString("en-GB", { timeZone: ZONA, hour: "2-digit", minute: "2-digit", hourCycle: "h23" });
}

export function diaLima(fecha = fechaLima()): string {
  const dia = new Date(`${fecha}T12:00:00-05:00`).toLocaleDateString("en-US", { timeZone: ZONA, weekday: "short" });
  return DIAS[DIAS_EN_INGLES.indexOf(dia)];
}

export function momentoLima(fecha: string, hora: string): number {
  return Date.parse(`${fecha}T${hora}:00-05:00`);
}
