# Handoff · Diseño de la App Restaurante (San Mark Food)

Resumen de la sesión de diseño del 21 de setiembre de 2026, para continuar en otro chat. Incluye la sección de **Administración**, que vive dentro de esta app (caso de estudio). El diseño del comensal está en `docs/handoff-diseno-comensal.md`.

## 1. Dónde está todo

| Qué | Dónde |
|---|---|
| Prototipo (lienzo de Design en Claude) | https://claude.ai/artifact/7bxRDUe6815yRJrMqG1tDF (compartido por enlace) |
| Prototipo del comensal | https://claude.ai/artifact/HMEjQm4nBAiPRtMHUBFSeF |
| Backlog | Jira `jlchuque.atlassian.net`, proyecto **SCRUM**. Cada HU es una Historia y **cada criterio de aceptación es una subtarea**. |
| Planificación | `docs/SanMarkFood-Planificacion.xlsx`, hoja PLANIFICACION (116 filas, sincronizada con Jira al 21 set) |

**Regla del proyecto:** Claude **nunca** hace commit, push ni PR.

## 2. Decisiones de producto (21 set)

| # | Decisión | Afecta |
|---|---|---|
| R1 | **Mientras está pendiente de aprobación, el local puede preparar todo** (perfil, horario, fotos, carta), pero no aparece en el mapa ni recibe pedidos o reservas. | HU02 (SCRUM-65) |
| R2 | **Una cuenta = un local.** Sin roles de personal ni sucursales; si atienden varios, comparten la sesión (Firebase permite varias sesiones a la vez). | HU02 (SCRUM-61) |
| R3 | **El administrador entra por el mismo inicio de sesión.** El rol (asignado en el servidor, no editable) decide si se abre el panel del local o la sección de administración. Las cuentas de administrador se crean fuera de la app. | HU01 (SCRUM-60), HU23 (SCRUM-74) |
| R4 | **Registro rechazado → corregir y reenviar.** El administrador elige un motivo de una lista (datos incompletos, dirección no verificable, local duplicado) u «Otro». El local ve el motivo, corrige y vuelve a «pendiente». El administrador ve los reenvíos marcados, con el motivo anterior. | HU02 (SCRUM-160), HU24 (SCRUM-76, SCRUM-77) |
| R5 | **Menú del día:** se arma desde una foto de la pizarra, copiando el de ayer o desde cero. Vale solo para hoy. Se puede editar después de publicarlo (los pedidos hechos conservan su precio) y terminar antes de la hora de fin. | HU03 (SCRUM-161) |
| R6 | **Aviso fuerte de pedido nuevo:** push de alta prioridad con sonido que se repite hasta abrirlo, y cuenta regresiva de 10 min en la app. Al aceptar, el local elige 10, 15, 20 o 30 min, y con eso se calcula la hora que ve el comensal. El interruptor de pausa está en la cabecera de Pedidos. | HU09 (SCRUM-162, SCRUM-163) |
| R7 | **Entrega con código:** el cajero escribe los 4 dígitos del comensal y la app abre ese pedido. Sin código también se puede entregar, con confirmación. En delivery no hay código. | HU09 (SCRUM-164) |
| R8 | **Cupo de reservas por franja de 30 min**, configurado en el horario (por ejemplo, 12 comensales). Pendientes y confirmadas ocupan cupo; una franja llena se ve tachada para el comensal. Lo valida el backend. | HU12 (SCRUM-165), HU07 |

Otras decisiones de diseño:
- **Dispositivo:** celular (390×844).
- **Estilo:** la misma marca que el comensal, pero más densa.
- **La sección de administración** usa cabecera y barra de pizarra (`#23332C`) con acento ají, para no confundirla nunca con el panel de un local.

## 3. Cambios en el backlog (Jira y Excel)

- **Modificados:**
  - SCRUM-60 (HU01, rol asignado en el servidor)
  - SCRUM-61 (HU02, una cuenta = un local)
  - SCRUM-65 (HU02, qué puede hacer un local pendiente)
  - SCRUM-74 (HU23, mismo inicio de sesión, sin registro de administradores)
  - SCRUM-76 (HU24, reenvíos marcados)
  - SCRUM-77 (HU24, motivo de rechazo de una lista)
- **Nuevos:**
  - SCRUM-160 (HU02, corregir y reenviar)
  - SCRUM-161 (HU03, formas de armar el menú del día)
  - SCRUM-162 (HU09, aviso fuerte con cuenta regresiva)
  - SCRUM-163 (HU09, tiempo estimado al aceptar)
  - SCRUM-164 (HU09, entrega con código)
  - SCRUM-165 (HU12, cupo por franja)

## 4. Estado del lienzo

29 pantallas de 390×844, interactivas (Play) y enlazadas entre sí. Las que reutilizan otra pantalla con `dc-import` y un prop son envoltorios de una línea; los cambios se hacen en la pantalla original.

### Fila R: cuenta y alta del local
| Pantalla | Archivo | HU |
|---|---|---|
| R1 Inicio de sesión (restaurante o administrador, recuperar contraseña en panel) | `Main.dc.html` | HU01, HU23 |
| R2 Registro y verificación de correo (paso 1 de 3) | `Registro.dc.html` | HU01 |
| R3 Datos del local y fotos (paso 2 de 3). Tweak `modo`: alta / corregir / editar | `AltaLocal.dc.html` | HU02 |
| R4 Horario por día y cupo de reservas (paso 3 de 3). Tweak `modo`: alta / editar | `Horario.dc.html` | HU02, HU12 |
| R5 Local en revisión con lista de preparación. Tweak `estado`: pendiente / rechazado | `EnRevision.dc.html` | HU02 |
| R6 Registro rechazado con motivo | `EnRevisionRechazado.dc.html` (envoltorio) | HU02, HU24 |
| R7 Corregir y reenviar | `AltaLocalCorregir.dc.html` (envoltorio) | HU02 |

### Fila M: menú del día, carta e IA
| Pantalla | Archivo | HU |
|---|---|---|
| M1 Menú de hoy sin publicar: foto de la pizarra / copiar el de ayer / de cero. Tweak `estado` | `MenuHoy.dc.html` | HU03 |
| M2 Foto de la pizarra | `CamaraPizarra.dc.html` (envoltorio) | HU04 |
| M3 Revisar el menú leído por la IA (datos dudosos en amarillo) | `ArmarMenuIA.dc.html` (envoltorio) | HU04 |
| M4 Copiar el menú de ayer. Tweak `origen`: ayer / cero / ia | `ArmarMenu.dc.html` | HU03 |
| M5 Menú desde cero | `ArmarMenuCero.dc.html` (envoltorio) | HU03 |
| M6 Menú publicado: agotar opciones, hora de fin, editar, terminar | `MenuHoyPublicado.dc.html` (envoltorio) | HU03 |
| M7 Carta por categoría con disponibilidad. Tweak `vacia` | `Carta.dc.html` | HU03 |
| M8 Alta o edición de plato, con eliminar. Tweak `modo` | `Plato.dc.html` | HU03 |
| M9 Foto de la carta (lectura con IA simulada). Tweak `modo`: carta / pizarra | `Camara.dc.html` | HU04 |
| M10 Revisar la carta leída por la IA antes de agregarla | `RevisionIA.dc.html` | HU04 |

### Fila O: operación diaria
| Pantalla | Archivo | HU |
|---|---|---|
| O1 Pedidos: nuevo con cuenta regresiva real, aceptar con tiempo estimado, rechazar con motivo, pausa, secciones por estado | `Pedidos.dc.html` | HU09, HU02 |
| O2 Detalle y estados del pedido; entregar sin código con confirmación. Tweak `modo`: recojo / delivery (efectivo: cobrar al entregar) | `PedidoDetalle.dc.html` | HU09 |
| O3 Entregar con código (teclado numérico). **En el prototipo, el código válido es 4821** | `EntregarCodigo.dc.html` | HU09 |
| O4 Reservas: por responder (con plazo del rechazo automático), confirmar o rechazar con motivo, ocupación por franja | `Reservas.dc.html` | HU12 |
| O5 Reseñas: filtros, negativas con motivo detectado por IA, responder en público | `Resenas.dc.html` | HU21, HU15 |
| O6 Dashboard del negocio (pedidos, ventas, platos más pedidos, calificación) y ajustes | `Negocio.dc.html` | HU13 |
| O7 Perfil del local | `PerfilLocal.dc.html` (envoltorio) | HU02 |
| O8 Horario y cupo | `HorarioEditar.dc.html` (envoltorio) | HU02, HU12 |

### Fila A: administración
| Pantalla | Archivo | HU |
|---|---|---|
| A1 Solicitudes de locales (pendientes, aprobados, rechazados; reenvíos marcados) | `AdminSolicitudes.dc.html` | HU24 |
| A2 Revisar solicitud: aprobar o rechazar con motivo | `AdminSolicitud.dc.html` | HU24 |
| A3 Reseñas reportadas: mantener o eliminar, e historial de moderación | `AdminResenas.dc.html` | HU25 |
| A4 Métricas de la plataforma | `AdminMetricas.dc.html` | HU26 |

**Navegación:**
- **Restaurante:** barra inferior con Pedidos · Reservas · Menú · Reseñas · Negocio.
- **Administración:** Solicitudes · Reportes · Métricas.
- **En el inicio de sesión del prototipo**, un correo que contenga «admin» abre la sección de administración. En la app real lo decide el rol que asigna el servidor.

**Revisado en el navegador el 22 set:** las 29 pantallas cargan y se ven bien, incluidos los envoltorios (R6, R7, M2, M3, M5, M6, O7, O8) y la cuenta regresiva de O1. En Play se probó el rechazo de O1 con «Plato agotado», que pide marcar el plato y avisa a Jose C. De esa revisión salieron tres correcciones de texto:
- M10 decía «Agregar8 platosa la carta». En un contenedor flex, un texto mezclado con un valor dinámico pierde los espacios; ahora el texto se arma completo en la lógica.
- Por la misma causa, se corrigieron el plazo de O4 («Se rechaza sola a las…») y la etiqueta de reseña negativa de O5.

**Datos de ejemplo:** los locales, platos, precios, cifras de los dashboards y nombres de comensales son de ejemplo. Lo desconocido va entre corchetes: `[Dirección del local]`, `[999 999 999]`, `[correo del local]`.

## 5. Coherencia entre las dos apps

Los dos prototipos cuentan la misma historia con los mismos datos de ejemplo. El local es «La Sazón de Doña Carmen» y el comensal es Jose C.

| Qué hace el restaurante | Qué ve el comensal |
|---|---|
| Recibe el pedido #0132 de Jose C. (12:41, menú + lomo saltado, S/ 38, billetera) | Es el pedido que arma en B6 y sigue en B7 |
| Lo acepta a las 12:43 con 15 min | «Preparando» a las 12:43 · «Listo aprox. a las 12:58» |
| Lo rechaza con «Plato agotado» y marca el plato | D6: «Plato agotado: lomo saltado», con el pago revertido |
| No responde en 10 min | Rechazo automático (D6 y avisos) |
| Escribe el código de recojo | B7: «Díselo al cajero para recoger» |
| Pausa «Recibiendo pedidos» | Pin «Cerrado» y E13 «Cerrado por ahora · no recibe pedidos» |
| Publica el menú (S/ 14, hasta las 15:00, arroz con pollo agotado) | B2 con las mismas opciones, precio y hora |
| Termina el menú antes / pasa la hora de fin / no lo publica | E10–E12: «Menú agotado», «El menú de hoy terminó», «El menú de hoy aún no se publica» |
| Carta: 5 platos (tallarines verdes agotados) | B2: la misma carta con los mismos precios |
| Horario lun–vie 11:30–16:00, sáb 12:00–15:00, dom cerrado | B2 y B5 (domingo cerrado) |
| Confirma o rechaza la reserva con motivo de la lista | E7 y avisos: «Sin mesas disponibles a esa hora» |
| No responde la reserva hasta 60 min antes | E8 y G8: rechazo automático |
| Ve la reserva modificada como pendiente, con el horario anterior liberado | C6: el cambio vuelve a pendiente |
| Cupo por franja; una franja llena | B5: la franja aparece tachada |
| Responde reseñas (ve estrellas, comentario y «me gustó» por plato) | B3: las mismas 6 reseñas, con las respuestas a Luis M. y Kevin R. |
| El administrador modera las reseñas reportadas | B3: reporte con motivo (incluido «Otro»), una vez por reseña |

## 6. Sistema visual

Es el mismo de la App Comensal (sección 3 de `docs/handoff-diseno-comensal.md`): papel `#F7F0E4`, guinda `#8C1D2F`, ají `#F2B53A`, pizarra `#23332C`, Bricolage Grotesque, Figtree y Caveat. Diferencias:
- **Más densa:** filas de 56 a 64 px, más información por tarjeta.
- **Interruptores en verde** (`#3E6B3A`) para disponible/agotado y para recibir pedidos. En la guinda quedan solo las acciones principales.
- **Pedido nuevo** en una tarjeta con borde y banda ají.

## 7. Pendiente
- **Estados que faltan del restaurante:** sin conexión (sobre todo Pedidos: el local debe saber que no está recibiendo), permiso de cámara, notificación del sistema del pedido nuevo (con la app cerrada) y modo oscuro (no se pidió).
- **Backend (HU07):**
  - Los dos rechazos automáticos (pedido a los 10 min y reserva 60 min antes).
  - El cupo por franja, dentro de una transacción.
  - El rol del usuario (custom claims o documento protegido).
  - Validar la hora de fin del menú al confirmar un pedido.
- ~~**Nombre de carpeta:**~~ resuelto el 26 set: la carpeta se renombró a `app-restaurante/`, igual que `rootProject.name` y que los documentos.

## 8. Pasar el diseño a Figma

El mensaje listo para copiar está en la sección 9 del handoff del comensal (opción B), junto con la equivalencia entre nuestra paleta y los roles de Material 3. Vale para las dos apps: este lienzo se pasa después del comensal, con las mismas variables, estilos y componentes.

## 9. Notas para trabajar el lienzo en otro chat

- Para editar el lienzo, pásale a Claude su URL. Antes de publicar, hay que leer `project/canvas.json` y los archivos que se van a cambiar.
- Cada pantalla es un archivo `project/<Nombre>.dc.html`. Esta versión del tipo Design usa `<script type="text/x-dc" data-dc-script>` y un `<title>` por pantalla.
- Idioma de la interfaz: español (Perú). Moneda: S/.
