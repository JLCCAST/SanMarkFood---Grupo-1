# Contexto para sesiones de IA — San Mark Food

Proyecto del curso de Desarrollo de Aplicaciones Móviles (UNMSM, VIII ciclo, 2026-II). Equipo de 3: Jose Chuque, Camila Bada, Rodrigo Puente.

## Reglas del proyecto

1. **Nunca hagas `git commit`, `git push` ni abras un Pull Request.** Los commits los hace el equipo a mano, porque el curso evalúa la participación individual en el historial. Deja los cambios en el working tree y avisa qué tocaste.
2. **Todo en español (Perú)**, código y textos de UI. Moneda `S/`. Los nombres del dominio van en español (`Reserva`, `MenuDelDia`); en inglés solo lo que impone el framework (`ViewModel`, `UseCase`, `Screen`).
3. **Son dos proyectos Gradle independientes**, no un multi-módulo. No crees un `settings.gradle.kts` en la raíz ni intentes compilar desde ahí: se trabaja dentro de `app-comensal/` o de `app-restaurante/`.
4. **El diseño ya está cerrado.** Antes de programar una pantalla, lee su fila en el handoff correspondiente y mírala en el prototipo. Si algo del diseño no te cuadra, dilo antes de cambiarlo por tu cuenta.
5. **Jira es la fuente de verdad de la planificación**: proyecto `SCRUM` en `jlchuque.atlassian.net`, donde **cada criterio de aceptación es una subtarea**. Si cambias criterios en Jira, replícalos en `docs/SanMarkFood-Planificacion.xlsx` (hoja PLANIFICACION), y al revés.

## Antes de escribir código, lee

| Para qué | Archivo |
| --- | --- |
| Estructura de carpetas, capas, convenciones de código y qué existe hoy | `docs/arquitectura-proyecto.md` |
| Diseño del comensal: 46 pantallas, decisiones, paleta y roles de Material 3 | `docs/handoff-diseno-comensal.md` |
| Diseño del restaurante y del Administrador: 29 pantallas | `docs/handoff-diseno-restaurante.md` |
| Ramas, formato de commits (`[SCRUM-XX] tipo: ...`) y PRs | `CONTRIBUTING.md` |

Prototipos navegables: comensal https://claude.ai/artifact/HMEjQm4nBAiPRtMHUBFSeF (solo la página «Versión B»; la página «Descartado» es historia, no se implementa) y restaurante https://claude.ai/artifact/7bxRDUe6815yRJrMqG1tDF.

## Detalles que se olvidan seguido

- El tema Material 3 ya está implementado en `ui/theme/` de cada app (`Color.kt`, `Type.kt`, `Theme.kt`) con modo claro y oscuro, y las fuentes en `res/font/`. **En las pantallas no se escriben colores ni tamaños literales**: solo `MaterialTheme.colorScheme` y `MaterialTheme.typography`.
- `ui/theme/` está al lado de `core/`, no dentro. Es donde lo dejó Android Studio; no lo muevas.
- Los dos temas son copias a mano. Si cambias un color o un estilo en una app, aplícalo también en la otra.
- Las pantallas diseñadas incluyen estados de vacío, cargando, sin conexión, invitado y error. Implementarlos es parte de la historia, no un extra.
- `app-restaurante/` contiene también la sección **Administrador** (aprobación de locales, moderación de reseñas, dashboard global): es una sección de esa app, no una tercera app.
- `docs/Color.kt` y `docs/Theme.kt` son la exportación del tema desde Figma, ya instalada en las dos apps. No son código del proyecto; no las edites esperando que afecten a la app.
