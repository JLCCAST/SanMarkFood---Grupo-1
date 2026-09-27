# Guía de contribución del equipo

El curso exige **evidenciar la participación individual de cada integrante** mediante el control de versiones. Esta guía existe para que eso quede reflejado automáticamente en el historial del repo, sin esfuerzo extra.

## 1. Estrategia de ramas

```
main            → siempre refleja el último estado presentable del proyecto
 └─ develop      → trabajo integrado del equipo; crece sin parar durante todo el semestre
     └─ feature/SCRUM-XX-descripcion-corta   → una rama por HISTORIA DE USUARIO
     └─ fix/SCRUM-XX-descripcion-corta       → correcciones de bugs
     └─ chore/descripcion-corta              → mantenimiento del repo, sin tarjeta (ver 2.1)
```

- **Nunca** se hace push directo a `main` ni a `develop`.
- Cada **historia de usuario** se trabaja en su propia rama `feature/SCRUM-XX-...`: se abre cuando empiezas la HU, se mergea a `develop` cuando termina. Esto pasa continuamente, sin relación con sprints ni fechas del sílabo. Son ~26 ramas en todo el semestre, no una por criterio (ver 1.1).
- `develop` se actualiza tarea por tarea, PR por PR, a lo largo de todo el semestre — no se acumula trabajo para mergear en bloque.
- `main` se actualiza (`develop` → `main`) cuando hay que **mostrar** el proyecto (sustentaciones del sílabo). Es solo una foto del estado actual para el profesor — no significa que el equipo se detenga ahí. El trabajo en `develop` sigue exactamente igual antes y después de cada sustentación.
- Si la tarea toca un solo módulo, puedes incluirlo en el nombre de rama para mayor claridad, ej. `feature/SCRUM-14-comensal-login`.

### 1.1 La rama es la HU, el commit es el criterio

En Jira, **cada HU es una historia y cada criterio de aceptación es una subtarea**. Los dos niveles se usan, pero en sitios distintos:

| Nivel | Qué clave lleva | Cuántos hay |
| --- | --- | --- |
| Rama y Pull Request | la de la **HU** (la historia) | ~26 en todo el semestre |
| Commit | la de la **subtarea** (el criterio) | los que haga falta |

Ejemplo — HU19 (`SCRUM-37`) con sus criterios `SCRUM-157`, `SCRUM-158`, `SCRUM-159`:

```
feature/SCRUM-37-gestion-perfil-comensal
 ├─ [SCRUM-157] feat: permite editar nombre y foto del perfil
 ├─ [SCRUM-158] feat: agrega preferencias de notificación
 └─ [SCRUM-159] feat: aplica el tema claro/oscuro del sistema
```

Un solo PR, una sola revisión, un solo merge — pero la historia queda enlazada a la rama y al PR, y **cada criterio queda enlazado a su commit**. Una rama por criterio daría más de cien ramas y ninguna sería revisable por separado.

Tres reglas que salen de esto:

- **La clave de la HU va en el nombre de la rama.** Una historia no hereda los commits de sus subtareas: si su clave no está en la rama o en el PR, su tarjeta se queda vacía.
- **Si una HU es muy grande, pártela en varias ramas, pero todas con la clave de la HU** (`feature/SCRUM-33-ocr-camara`, `feature/SCRUM-33-revision-ia`). El criterio para partir: ¿esto se revisa y se mergea de una sentada?
- **En el cuerpo del PR, lista las subtareas que cierra.** Así el revisor sabe qué criterios tiene que verificar.

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

### 2.1 Excepción: mantenimiento del repositorio

Hay trabajo que **no pertenece a ninguna tarjeta** porque no implementa ninguna historia de usuario: documentación, `.gitignore`, plantillas, renombrar una carpeta, configuración del repo. Esos commits van **sin clave**, solo con el tipo:

```
docs: agrega convenciones de código y estado actual de la arquitectura
chore: renombra apprestaurante a app-restaurante
```

Dos condiciones para usar la excepción:

1. **No cambia el comportamiento de la app.** Si el commit toca código que ejecuta el usuario, pertenece a una HU y lleva su clave — aunque sea un cambio pequeño.
2. **Va en su propio commit**, no mezclado con trabajo de una tarjeta. Si un mismo cambio de rama incluye las dos cosas, se parten en dos commits.

Estos commits no aparecen en Jira (Jira solo ve lo que lleva `SCRUM-XX`), y está bien: la trazabilidad se exige sobre el trabajo planificado, no sobre el mantenimiento del repo.

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
