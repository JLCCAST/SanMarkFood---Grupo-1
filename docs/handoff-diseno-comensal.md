# Handoff · Diseño de la App Comensal (San Mark Food)

Resumen de las sesiones de diseño del 16 al 21 de setiembre de 2026, para continuar en otro chat. El 21 de setiembre se cerraron **todas las decisiones pendientes** (sección 6).

## 1. Dónde está todo

| Qué | Dónde |
|---|---|
| Prototipo (lienzo de Design en Claude) | https://claude.ai/artifact/HMEjQm4nBAiPRtMHUBFSeF (compartido por enlace) |
| Backlog | Jira `jlchuque.atlassian.net`, proyecto **SCRUM** ("San Mark Food - GRUPO 1"). Cada HU es una Historia y **cada criterio de aceptación es una subtarea**. |
| Planificación | `docs/SanMarkFood-Planificacion.xlsx`, hoja PLANIFICACION (116 filas, sincronizada con Jira al 21 set) |
| App Restaurante y Administración | `docs/handoff-diseno-restaurante.md` y el lienzo https://claude.ai/artifact/7bxRDUe6815yRJrMqG1tDF |
| Caso de estudio | `docs/San-Mark-Food-Caso-de-Estudio.docx` |
| Arquitectura | `docs/arquitectura-proyecto.md` |

**Regla del proyecto:** Claude **nunca** hace commit, push ni PR. Deja los cambios sin confirmar y avisa qué archivos cambiaron.

## 2. Estado del lienzo

El lienzo tiene dos páginas. Todas las pantallas miden 390×844, son interactivas (Play) y están enlazadas entre sí.

- **Página "Descartado":** la versión A (`Main`, `Restaurante`, `Reserva`, `Carrito`, `Seguimiento`). Se conserva como evidencia del proceso (decisión C1).
- **Página "Versión B":** la vigente, filas B a G.

Varias pantallas tienen **Tweaks** (props) para mostrar sus estados; las pantallas de la fila E son envoltorios (`dc-import`) que fijan esos props.

### Fila B: flujo principal
| Pantalla | Archivo | HU |
|---|---|---|
| B1 Explorar en el mapa (mapa + lista deslizable, pines con precio del menú, filtros). Tweak `invitado` | `Inicio.dc.html` | HU06 |
| B2 Menú del día y carta fija. Tweaks `vista` (normal / vaciarPedido / invitado) y `menu` (publicado / agotado / terminado / sinPublicar) | `Local.dc.html` | HU06, HU03, HU04, HU08 |
| B3 Reseñas por pedido y reporte | `Resenas.dc.html` | HU22, HU14, HU25 |
| B4 Asistente por voz y permiso de micrófono | `Asistente.dc.html` | HU11 |
| B5 Reserva de mesa con límites (2 h a 7 días, 1 a 10, tope de 2 activas). Tweak `reservasActivas` | `Reservar.dc.html` | HU10 |
| B6 Pedido tipo ticket, recojo o delivery, pago biométrico (efectivo sin biometría). Tweaks `pago` y `aviso` | `Pedido.dc.html` | HU08, HU19 |
| B7 Seguimiento con recojo, cancelar con confirmación, reseña del pedido (prop `modo`: recojo/delivery) | `EnCurso.dc.html` | HU08, HU09 |
| B8 Actividad e historial, reservas rechazadas con detalle. Tweak `invitado` | `Actividad.dc.html` | HU20, HU10 |

### Fila C: cuenta, perfil y avisos
| Pantalla | Archivo | HU |
|---|---|---|
| C1 Inicio de sesión y «Explorar sin cuenta» | `Acceso.dc.html` | HU05 |
| C2 Registro y verificación de correo | `Registro.dc.html` | HU05 |
| C3 Recuperar contraseña | `Recuperar.dc.html` | HU05 |
| C4 Perfil, direcciones frecuentes y preferencias de avisos. Tweak `invitado` | `Perfil.dc.html` | HU19 |
| C5 Bandeja de notificaciones (con acceso a preferencias) | `Notificaciones.dc.html` | HU09, HU10, HU12, HU21 |
| C6 Modificar reserva: el cambio vuelve a dejarla pendiente, con confirmación | `EditarReserva.dc.html` | HU10 |

### Fila D: casos alternativos
| Pantalla | Archivo |
|---|---|
| D1 Permiso de ubicación (ya no tiene punto manual). Tweak `estado` | `Ubicacion.dc.html` |
| D2 Búsqueda: resultados y sin resultados | `Busqueda.dc.html` |
| D3 Huella no reconocida, bloqueo y PIN | `PagoError.dc.html` |
| D4 Celular sin huella ni rostro | `PagoSinBiometria.dc.html` |
| D5 Seguimiento con delivery (reutiliza `EnCurso` con `modo="delivery"`) | `EnCursoDelivery.dc.html` |
| D6 Pedido rechazado por el restaurante | `PedidoRechazado.dc.html` |
| D7 Ubicación denegada: activar en ajustes | `UbicacionDenegada.dc.html` |

### Fila E: estados de las decisiones del 21 set
| Pantalla | Archivo | Decisión |
|---|---|---|
| E1 Pedido de otro local: «¿Empezar un pedido nuevo?» | `LocalVaciar.dc.html` | A1 |
| E2 Explorar sin cuenta (botón «Ingresar») | `InicioInvitado.dc.html` | A2 |
| E3 Local sin cuenta: pedir, reservar o preguntar abre «Inicia sesión para continuar» | `LocalInvitado.dc.html` | A2 |
| E4 Actividad sin cuenta | `ActividadInvitado.dc.html` | A2 |
| E5 Perfil sin cuenta | `PerfilInvitado.dc.html` | A2 |
| E6 Pago en efectivo, sin biometría | `PedidoEfectivo.dc.html` | A3 |
| E7 Detalle de reserva rechazada con motivo (prop `motivo`) | `ReservaRechazada.dc.html` | A5 |
| E8 Reserva sin respuesta: rechazo automático | `ReservaSinRespuesta.dc.html` | A4 |
| E9 Tope de 2 reservas activas | `ReservarLimite.dc.html` | A6 |
| E10 Menú del día terminado | `LocalMenuTerminado.dc.html` | A7 |
| E11 El menú venció con el pedido armado | `PedidoMenuVencido.dc.html` | A7 |
| E12 Menú de hoy aún no publicado | `LocalSinMenu.dc.html` | C2 |
| E13 Local en pausa: se ve la carta y se puede reservar, pero no pedir (tweak `local` de B2) | `LocalPausado.dc.html` | HU02 (pausa del restaurante) |

### Fila F: modo oscuro (C5)
`InicioOscuro`, `LocalOscuro`, `PedidoOscuro` y `EnCursoOscuro` (`.dc.html`). Se generaron desde las pantallas claras cambiando la paleta (sección 3). **Si se edita una pantalla clara, hay que regenerar su versión oscura.** Sus enlaces internos apuntan a las otras pantallas oscuras.

### Fila G: estados vacíos, carga y sin conexión
| Pantalla | Archivo | Tweak de la pantalla original |
|---|---|---|
| G1 Actividad sin pedidos ni reservas | `ActividadVacia.dc.html` | `Actividad` · `vacio` |
| G2 Notificaciones vacía (con acceso a preferencias) | `NotificacionesVacia.dc.html` | `Notificaciones` · `vacio` |
| G3 Local sin reseñas (sin resumen IA) | `ResenasVacia.dc.html` | `Resenas` · `vacio` |
| G4 Explorar cargando (esqueletos en la lista) | `InicioCargando.dc.html` | `Inicio` · `estado` |
| G5 Explorar sin conexión (datos guardados + Reintentar) | `InicioSinConexion.dc.html` | `Inicio` · `estado` |
| G6 Pedido sin conexión al pagar (no se cobra, el pedido se conserva) | `PedidoSinConexion.dc.html` | `Pedido` · `aviso` |
| G7 Asistente sin conexión | `AsistenteSinConexion.dc.html` | `Asistente` · `conexion` |
| G8 Detalle de reserva pendiente (plazo del rechazo automático, cancelar o modificar) | `ReservaPendiente.dc.html` | pantalla propia |

**Revisado en el navegador el 22 set:** las 46 pantallas de «Versión B» cargan y se ven bien, incluidas D5 y las filas E y G (reutilizan otra pantalla con `dc-import`) y la fila F (modo oscuro). Correcciones que salieron de esa revisión:
- La tarjeta del menú del día de B2 desaparecía. Tenía `overflow: hidden` dentro de una columna flex con scroll y el navegador la encogía hasta 0 de alto. Ahora todas las pantallas tienen `.no-scrollbar > * { flex-shrink: 0; }`.
- Algunas pantallas mostraban barras de scroll dentro del marco. Ahora `html, body { overflow: hidden; }` está en todas.
- Al abrir, el lienzo mostraba la página «Descartado» y «Versión B» no mostraba los títulos de fila. Ahora abre en «Versión B» y todo su contenido está arriba.
- El asistente (B4) decía «listo a la 1:00 p. m.». Ahora dice 12:58, igual que el seguimiento.
- Ajustes menores: el botón de C6 ya no se parte en dos líneas, tampoco las etiquetas de E10 y E12, y los controles nativos de la fila F usan esquema oscuro.

## 3. Sistema visual de la versión B ("Mapa · Menú del día · Ticket")

Estructura Material Design 3 (barra de navegación, paneles inferiores, diálogos, botones segmentados, interruptores) con identidad propia.

- **Colores (tema claro):**
  - Fondo papel `#F7F0E4` · superficie `#FFFBF4` · contenedor `#EFE5D3` · línea `#D9CCB6` · contorno `#8A7E6C`
  - Tinta `#1E1B16` · tinta secundaria `#5C5347`
  - Primario guinda `#8C1D2F` · contenedor primario `#F6D9DC`
  - Acento ají `#F2B53A` · contenedor `#FCE7B8`
  - Verde de "abierto" `#3E6B3A` · contenedor `#D6EBCF`
  - Error `#B3261E` · contenedor `#F9DEDC`
  - Pizarra `#23332C` (tarjeta de estado del seguimiento)
- **Colores (tema oscuro, C5):**
  - Fondo `#17130F` · superficie `#211C17` · contenedor `#2B251F` · línea `#4A4238` · contorno `#9C907E`
  - Tinta `#EDE3D3` · tinta secundaria `#CBBFAD`
  - Primario `#FFB2B9` con texto sobre primario `#5F1121`
  - Verde `#A6D39C` · error `#FFB4AB`
  - El acento ají, los contenedores de estado (amarillo, verde, rojo, rosado) y la pizarra se mantienen.
  - Superficies que siguen oscuras en ambos temas (cabecera del menú del día, aviso de pedido en curso): `#0F0C09`.
- **Equivalencia con los roles de Material 3** (para Figma y para `Theme.kt`):

| Nuestro color | Rol M3 |
|---|---|
| Papel `#F7F0E4` | `surface` / `background` |
| Superficie `#FFFBF4` | `surfaceContainerLowest` |
| Contenedor `#EFE5D3` · `#E6DCCB` | `surfaceContainer` · `surfaceContainerHigh` |
| Línea `#D9CCB6` · contorno `#8A7E6C` | `outlineVariant` · `outline` |
| Tinta `#1E1B16` · tinta secundaria `#5C5347` | `onSurface` · `onSurfaceVariant` |
| Guinda `#8C1D2F` + blanco | `primary` + `onPrimary` |
| Rosado `#F6D9DC` + `#3B0711` | `primaryContainer` + `onPrimaryContainer` |
| Ají `#F2B53A` · `#FCE7B8` + `#2E2000` | `secondary` · `secondaryContainer` + `onSecondaryContainer` |
| Verde `#3E6B3A` · `#D6EBCF` · `#1F3D1C` | `tertiary` · `tertiaryContainer` · `onTertiaryContainer` |
| Error `#B3261E` · `#F9DEDC` · `#410E0B` | `error` · `errorContainer` · `onErrorContainer` |
| Pizarra `#23332C` | sin rol: color extendido |

  Nuestra paleta define unos 12 colores y M3 usa ~26 roles por modo. Los que faltan se generan con Material Theme Builder (`m3.material.io/theme-builder`) a partir de esos tres colores base.
- **Tipografía:** Bricolage Grotesque (títulos, precios) · Figtree (texto) · Caveat (solo detalles "escritos a mano").
- **Motivos:** ticket con línea perforada (pedido, código de recojo, reserva), precios alineados con puntos (carta), pines con precio.
- **Datos:** los locales, platos y precios son de ejemplo. Lo desconocido va entre corchetes: `[Dirección del local]`, `[#N.º]`, `[0000]`, `[Yape / Plin]`, `[Teléfono del local]`.

## 4. Decisiones tomadas

### Del 16 al 20 de setiembre
- **Reseñas por pedido:** estrellas y comentario para el local, más "me gustó / no me gustó" opcional por plato. Pedir dos veces lo mismo da dos opiniones distintas. Los platos fijos de la carta muestran "% le gustó".
- **Pantalla principal:** mapa arriba y lista deslizable abajo, sincronizados. Los pines muestran el precio del menú de hoy (o "Carta" / "Cerrado").
- **Menú del día dinámico:** separado de la carta fija. Se elige 1 entrada y 1 segundo; **refresco y postre incluidos**. Se publica a diario, idealmente con foto de la pizarra y la IA de HU04. Las opciones pueden agotarse.
- **Delivery:** lo coordina el restaurante (sin repartidores ni ruteo). En el seguimiento, el paso 3 es "En camino".
- **Recordatorio de reserva:** el comensal elige 30 min, 1 h o 2 h antes.
- **Rechazo de pedidos:** el restaurante puede rechazar con motivo obligatorio. **Si no acepta en 10 minutos, el pedido se rechaza automáticamente.** El pago simulado se revierte.
- **Repetir pedido con menú:** como el menú cambia cada día, se eligen entrada y segundo del menú de hoy.

### Del 21 de setiembre (antes sección 6)
| # | Decisión |
|---|---|
| A1 | **Un pedido = un local.** Un solo carrito. Al agregar algo de otro local se pregunta «¿Empezar un pedido nuevo?» (*Mantener el actual* / *Vaciar y agregar*). |
| A2 | **Se puede explorar sin cuenta:** mapa, menús, carta y reseñas. Pedir, reservar y usar el asistente piden iniciar sesión con un panel inferior. Al iniciar sesión se vuelve a la pantalla de origen y el carrito se conserva (vive en Room). Actividad y Perfil muestran un estado de invitado. C1 tiene el enlace «Explorar sin cuenta». |
| A3 | **El efectivo no pide biometría.** Solo billetera y tarjeta piden huella o rostro. Con efectivo, el botón dice «Confirmar pedido». |
| A4 | **Reserva sin respuesta:** queda pendiente hasta que **falten 60 min** para la hora. Entonces se rechaza automáticamente y se avisa al comensal. |
| A5 | **Rechazar una reserva exige motivo**, elegido de una lista (sin mesas a esa hora, local cerrado ese día, grupo demasiado grande) u «Otro» con texto. El motivo va en el aviso push. |
| A6 | **Límites de reserva:** entre **2 h y 7 días** de anticipación; horarios **cada 30 min** dentro del horario del local; de **1 a 10** comensales (más de 10: llamar al local); **máximo 2 reservas activas** por comensal. |
| A7 | **Horario del menú del día:** el restaurante publica la hora de fin (por defecto 15:00). El menú deja de pedirse a esa hora **o** cuando se agotan sus entradas o segundos. Si vence con el menú en el carrito, se avisa al confirmar y se quita del pedido. |
| B1 | **Reportar reseñas desde el comensal entra al backlog** (HU22). Alimenta la cola de moderación de HU25. |
| B2 | **Se descarta el punto de partida manual.** Sin GPS no se puede explorar: D1 pide el permiso y, si se niega, muestra «Activa tu ubicación» con *Abrir ajustes* (D7). |
| B3 | **Modificar una reserva es un cambio directo:** libera el horario anterior y la reserva vuelve a quedar pendiente. Si el restaurante la rechaza, no se recupera el horario anterior. Antes de guardar se pide confirmación. |
| C1 | La versión A se movió a la página «Descartado». |
| C2 | Local sin menú publicado hoy: pin «Carta», etiqueta «MENÚ POR PUBLICAR» en la lista y aviso en B2. Es la misma tarjeta que «menú agotado» y «menú terminado». |
| C3 | Cancelar un pedido pide confirmación con un diálogo. |
| C4 | Preferencias de avisos en Perfil: interruptores para **reservas** y **respuestas a reseñas**. El estado de un pedido en curso es fijo (no se puede apagar). |
| C5 | **Modo oscuro diseñado** para Explorar, Local, Pedido y Seguimiento (fila F). |

## 5. Cambios aplicados al backlog (Jira y Excel)

### Del 16 al 20 de setiembre
- **Modificados:** SCRUM-70 (HU03, disponibilidad de plato u opción del menú), SCRUM-71 y SCRUM-72 (HU04, foto de la pizarra e IA para el menú del día), SCRUM-97 (HU08, calificación por pedido), SCRUM-103 (HU10, recordatorio de 30 min, 1 h o 2 h), SCRUM-111 (HU20, repetir pedido con menú del día).
- **Nuevos:** SCRUM-148 (HU03, publicación diaria del menú del día), SCRUM-149 (HU06, mapa y lista), SCRUM-150 (HU08, armado del menú), SCRUM-151 (HU08, estado "Rechazado"), SCRUM-152 (HU09, rechazo con motivo), SCRUM-153 (HU09, rechazo automático a los 10 min).

### Del 21 de setiembre
- **Modificados:**
  - SCRUM-93 (HU08, carrito de un solo local): A1
  - SCRUM-95 (HU08, biometría solo para billetera y tarjeta): A3
  - SCRUM-107 (HU12, rechazo de reserva con motivo de lista): A5
  - SCRUM-108 (HU12, el push incluye el motivo): A5
  - SCRUM-102 (HU10, límites de reserva): A6
  - SCRUM-105 (HU10, modificar vuelve a pendiente): B3
  - SCRUM-148 (HU03, hora de fin del menú): A7
- **Nuevos:**
  - SCRUM-154 (HU05, exploración sin cuenta): A2
  - SCRUM-155 (HU12, rechazo automático 60 min antes): A4
  - SCRUM-156 (HU08, el menú se puede pedir hasta la hora de fin o hasta agotarse): A7
  - SCRUM-157 (HU22, reportar reseñas): B1
  - SCRUM-158 (HU19, preferencias de notificaciones): C4
  - SCRUM-159 (HU19, la app sigue el modo claro u oscuro del sistema con la paleta de marca, incluido el mapa): C5
- **Corrección:** la versión anterior de este documento asociaba A5 a SCRUM-58, pero SCRUM-58 es "Inicio de sesión persistente" (HU05). El criterio correcto es SCRUM-107.

## 6. Decisiones pendientes

**La App Comensal está completa** (decisiones y pantallas) y revisada en el navegador (sección 2).

El reporte de reseñas (B3) ya cumple SCRUM-157: después de reportar, la reseña muestra «Reportaste esta reseña · en revisión».

## 7. Pendiente fuera de la App Comensal
- **App Restaurante y sección Administrador:** diseñadas el 21 set. Ver `docs/handoff-diseno-restaurante.md`.
- **Backend (HU07):** tareas programadas para los dos rechazos automáticos (pedido a los 10 min y reserva 60 min antes), validar los límites de reserva de A6 y la hora de fin del menú de A7 al confirmar un pedido.
- **Llevar la paleta y la tipografía al tema de Compose:** `app-comensal/.../ui/theme/Color.kt`, `Type.kt` y `Theme.kt`, con el esquema M3 claro y oscuro de la sección 3 (SCRUM-159). Además de definir los dos `ColorScheme`:
  - Desactivar el color dinámico (`dynamicColor = false`) para que Android 12+ no reemplace la paleta de marca.
  - Usar siempre `MaterialTheme.colorScheme` en las pantallas, nunca colores fijos.
  - Los colores que M3 no trae (ají, verde de "abierto", pizarra) van como colores extendidos con valor claro y oscuro.
  - El mapa necesita su propio estilo oscuro (`MapStyleOptions`) según `isSystemInDarkTheme()`.

## 8. Notas para trabajar el lienzo en otro chat

- Para editar el lienzo existente, pásale a Claude su URL. Antes de publicar, siempre hay que leer `project/canvas.json` y los archivos que se van a cambiar, porque el editor puede guardar cambios desde la página.
- Cada pantalla es un archivo `project/<Nombre>.dc.html`. Los enlaces entre pantallas usan `<a href="Otra.dc.html">`.
- Las pantallas de la fila E son envoltorios de una línea (`<dc-import name="Local" vista="vaciarPedido">`). Los cambios se hacen en la pantalla original.
- Las pantallas `*Oscuro` son copias generadas: si cambias una pantalla clara, regenera la oscura con el mismo mapeo de colores (sección 3).
- Reglas que evitan errores de dibujo (ya están en todas las pantallas; conservarlas en las nuevas):
  - `html, body { overflow: hidden; }`
  - `.no-scrollbar > * { flex-shrink: 0; }` en las áreas con scroll.
  - Si un texto mezcla palabras fijas con un valor dinámico (`Agregar {{n}} a la carta`) dentro de un contenedor flex, se pierden los espacios. Hay que armar el texto completo en `renderVals()`.
- Idioma de la interfaz: español (Perú). Moneda: S/.

## 9. Mensaje para iniciar la próxima sesión

El diseño de las dos apps está cerrado y revisado. Hay dos caminos posibles.

**A) Programar el Sprint 1:**

> Lee `docs/handoff-diseno-comensal.md` y `docs/handoff-diseno-restaurante.md`. Quiero empezar a programar el Sprint 1: configurar Firebase en las dos apps, llevar el tema de marca a Compose (sección 7 del handoff del comensal) y armar la navegación base. No hagas commits: esos los hago yo.

**B) Pasar el diseño a Figma** (hay que autorizar antes el conector de Figma en la configuración de conectores de claude.ai):

> Lee `docs/handoff-diseno-comensal.md` y `docs/handoff-diseno-restaurante.md`. Quiero pasar el diseño de San Mark Food a Figma con el MCP de Figma, un archivo por app, y que quede alineado con Material 3 para Jetpack Compose.
>
> Prototipos: comensal https://claude.ai/artifact/HMEjQm4nBAiPRtMHUBFSeF (solo la página «Versión B») y restaurante https://claude.ai/artifact/7bxRDUe6815yRJrMqG1tDF.
>
> Orden, mostrándome el avance al terminar cada punto:
> 1. Genera el esquema de color con Material Theme Builder: primario guinda `#8C1D2F`, secundario ají `#F2B53A`, terciario verde `#3E6B3A`, en modo claro y oscuro. Usa la tabla de equivalencias de la sección 3 del handoff del comensal.
> 2. Crea las variables de Figma con los nombres de los roles de Material 3 (`primary`, `onPrimary`, `surface`, `outline`, `error`…), con los dos modos. Lo que no tiene rol (pizarra `#23332C`) va como color extendido.
> 3. Estilos de texto con Bricolage Grotesque (títulos y precios), Figtree (texto) y Caveat (detalles a mano), con los tamaños del prototipo.
> 4. Componentes tomados del Material 3 Design Kit de Figma (barra de navegación, paneles inferiores, diálogos, campos, interruptores, chips). Solo se dibujan a mano los motivos propios: ticket con línea perforada, pines con precio y tarjeta pizarra.
> 5. Recién después, las pantallas de 390×844 armadas con esos componentes y variables, nunca con colores sueltos. Una página de Figma por fila del lienzo (comensal B, C, D, E, F, G; restaurante R, M, O, A) y cada frame con el mismo nombre que en el lienzo, por ejemplo «B2 · Menú del día».
> 6. Exporta el tema como `Color.kt` y `Theme.kt` y déjalos en `docs/`.
>
> Todo en español (Perú), moneda S/. Los textos entre corchetes se quedan igual y las fotos y el mapa son marcadores con su rótulo. Empieza por la App Comensal, fila B. No hagas commits: esos los hago yo.
