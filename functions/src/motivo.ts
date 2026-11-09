const MAX_MOTIVO = 60;
const BORDES = /^[\s«"“'¡¿]+|[\s»"”']+$/g;
const PUNTUACION_FINAL = /[\s.,;:!?]+$/;
const CONECTOR_FINAL = /\s+(?:y|e|o|u|de|del|con|que|el|la|los|las|en|por|para|a|al)$/i;

export function limitarMotivo(texto: string): string | undefined {
  const limpio = texto.replace(BORDES, "").replace(PUNTUACION_FINAL, "");
  const corto = limpio.length <= MAX_MOTIVO ? limpio : cortarEnPalabra(limpio);
  if (corto === "") return undefined;
  return corto.charAt(0).toLocaleLowerCase("es-PE") + corto.slice(1);
}

function cortarEnPalabra(texto: string): string {
  const corte = texto.lastIndexOf(" ", MAX_MOTIVO);
  return texto.slice(0, corte > 0 ? corte : MAX_MOTIVO).replace(CONECTOR_FINAL, "").replace(PUNTUACION_FINAL, "");
}
