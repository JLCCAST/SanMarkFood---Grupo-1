# Guía de contribución del equipo

El curso exige **evidenciar la participación individual de cada integrante** mediante el control de versiones. Esta guía existe para que eso quede reflejado automáticamente en el historial del repo, sin esfuerzo extra.

## 1. Estrategia de ramas

```
main            → siempre refleja el último estado presentable del proyecto
 └─ develop      → trabajo integrado del equipo; crece sin parar durante todo el semestre
     └─ feature/SCRUM-XX-descripcion-corta   → una rama por tarjeta de Jira
     └─ fix/SCRUM-XX-descripcion-corta       → correcciones de bugs
```

- **Nunca** se hace push directo a `main` ni a `develop`.
- Cada tarjeta de Jira (historia de usuario, tarea técnica) se trabaja en su propia rama `feature/SCRUM-XX-...`: se abre cuando empiezas la tarea, se mergea a `develop` cuando termina. Esto pasa continuamente, sin relación con sprints ni fechas del sílabo.
- `develop` se actualiza tarea por tarea, PR por PR, a lo largo de todo el semestre — no se acumula trabajo para mergear en bloque.
- `main` se actualiza (`develop` → `main`) cuando hay que **mostrar** el proyecto (sustentaciones del sílabo). Es solo una foto del estado actual para el profesor — no significa que el equipo se detenga ahí. El trabajo en `develop` sigue exactamente igual antes y después de cada sustentación.
- Si la tarea toca un solo módulo, puedes incluirlo en el nombre de rama para mayor claridad, ej. `feature/SCRUM-14-comensal-login`.

## 2. Formato de commits

```
[SCRUM-XX] tipo: descripción corta en imperativo

tipo: feat | fix | refactor | docs | test | chore | style
```

Ejemplos:
```
[SCRUM-12] feat: agrega login con Firebase Authentication
[SCRUM-15] fix: corrige crash al rotar pantalla en detalle de restaurante
[SCRUM-20] docs: agrega diagrama de arquitectura MVVM
```

El código `SCRUM-XX` es el ID de la tarjeta en Jira (board del proyecto: `SCRUM`) — así se puede rastrear cada commit hasta la planificación (y Jira, si está integrado con GitHub, enlaza automáticamente los commits a la tarjeta). Las historias de usuario del backlog (HU01, HU02, ...) deben cargarse como issues en ese board antes de nombrar ramas o commits, para que tengan su número `SCRUM-XX` real.

**Regla de oro:** cada integrante hace commit de su propio trabajo con su propia cuenta de GitHub. No se suben cambios de otra persona bajo tu usuario, ni se hacen commits masivos de "trabajo de todo el sprint" al final.

## 3. Pull Requests

- Toda rama `feature/*` o `fix/*` se integra a `develop` vía Pull Request (nunca merge directo).
- Cada PR necesita **al menos 1 aprobación** de otro integrante del equipo (no auto-mergear).
- Usar la plantilla de PR (se aplica automáticamente al abrir un PR).
- El PR debe enlazar la tarjeta de Jira correspondiente.

Esto genera automáticamente evidencia de: quién programó (autor del commit/branch), quién revisó (aprobador del PR) y qué tarea de planificación cubre (link a Jira).

## 4. Issues en GitHub vs. Jira

- **Jira** = planificación (épicas, historias de usuario, sprints, estimaciones). Es la fuente de verdad para la nota de planificación del curso.
- **GitHub Issues** = opcional, solo para bugs técnicos puntuales detectados durante el desarrollo (plantilla `bug_report.md`).

## 5. Antes de una sustentación (no es un cierre de ciclo, solo una foto)

1. Mergear `develop` → `main` con lo que esté listo hasta ese momento.
2. Opcional: etiquetar ese punto para tener el estado guardado (`git tag entrega-parcial -m "Estado para sustentación"` y `git push --tags`).
3. Revisar que el historial de commits muestre aportes de los 3 integrantes.
4. Terminada la sustentación, el equipo sigue trabajando en `develop` con total normalidad — no hay "sprint 2" que empiece de cero, es la misma rama de siempre.
