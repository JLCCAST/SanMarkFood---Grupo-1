# Arquitectura del proyecto — San Mark Food

MVVM + Clean Architecture por capas, repetida en **dos proyectos de Android Studio completamente independientes** (no un Gradle multi-módulo): cada app tiene su propio `build.gradle.kts`, su propio `gradlew` y su propio ciclo de compilación. Viven como carpetas hermanas dentro del mismo repositorio de Git, pero Android Studio las abre por separado, una ventana por app.

## Estado actual del código (26 de setiembre de 2026)

El árbol de la sección siguiente es el **destino**, no lo que hay hoy en disco. Antes de buscar una carpeta, ten esto claro:

**Ya existe:**

- Los dos proyectos creados con la plantilla de Compose (`MainActivity` de ejemplo, Gradle configurado).
- El tema Material 3 real en `ui/theme/` de cada app: `Color.kt`, `Type.kt` y `Theme.kt` con la paleta propia (guinda, ají, verde) en modo claro y oscuro, más las tres fuentes en `res/font/`. Sale del diseño ya cerrado, no es la plantilla de Android Studio.
- Todo el diseño de pantallas, decidido y revisado: 46 pantallas del comensal y 29 del restaurante. Los prototipos son material interno del equipo, fuera del repositorio.

**Todavía no existe:** `core/`, `data/`, `domain/`, `presentation/`, `workers/`, `ai/` en ninguna de las dos apps, ni el proyecto `backend/`. Esas carpetas se crean **una por una, cuando la primera historia de usuario que las necesita entra en desarrollo** — no se arma el esqueleto completo vacío de entrada.

## Árbol de carpetas (destino)

```
SanMarkFood---Grupo-1/
│
├── app-comensal/                            # Proyecto Android independiente
│   └── app/src/main/
│       ├── res/font/                        # bricolage_grotesque.ttf, figtree.ttf, caveat.ttf ✔
│       └── java/com/equipo/sanmarkfood/comensal/
│           ├── ui/theme/                    # Color.kt, Type.kt, Theme.kt — Material Design 3 ✔
│           ├── core/
│           │   ├── di/                      # Módulos de Hilt
│           │   ├── navigation/              # NavGraph de Compose
│           │   └── util/                    # Extensiones, helpers, Result wrapper
│           ├── data/
│           │   ├── local/
│           │   │   ├── entity/              # Entidades Room (ej. carrito local)
│           │   │   └── dao/
│           │   ├── remote/
│           │   │   ├── api/                 # Interfaces Retrofit (backend propio + APIs externas)
│           │   │   └── dto/
│           │   ├── firebase/                # Wrappers de Firebase Auth / Firestore / Storage / FCM
│           │   └── repository/              # Implementaciones de los repos del dominio
│           ├── domain/                      # Kotlin puro, sin imports de Android
│           │   ├── model/
│           │   ├── repository/              # Interfaces (contratos)
│           │   └── usecase/                 # Un caso de uso = una acción de negocio
│           ├── presentation/
│           │   ├── auth/
│           │   ├── descubrimiento/          # Proceso: Descubrimiento de restaurantes
│           │   ├── reservas/                # Proceso: Reservas de mesa (lado comensal)
│           │   ├── pedidos/                 # Proceso: Pedidos (lado comensal)
│           │   ├── resenas/                 # Proceso: Reseñas (calificar, ver detalle)
│           │   ├── historial/               # Historial de pedidos y reservas (HU20)
│           │   ├── notificaciones/          # Bandeja de notificaciones
│           │   └── perfil/                  # Perfil, tema claro/oscuro y preferencias de aviso
│           ├── workers/                     # WorkManager (recordatorio de reserva, HU10)
│           └── ai/
│               ├── chatbot/                 # HU11 — chatbot con entrada por voz
│               └── resumen_resenas/         # HU14 — resumen de reseñas con IA
│
├── app-restaurante/                         # Proyecto Android independiente
│   └── app/src/main/
│       ├── res/font/                        # las mismas tres fuentes ✔
│       └── java/com/equipo/sanmarkfood/restaurante/
│           ├── ui/theme/                    # mismo tema que el comensal ✔
│           ├── core/                        # (misma estructura que app-comensal)
│           ├── data/                        # (misma estructura que app-comensal)
│           ├── domain/                      # (misma estructura que app-comensal)
│           ├── presentation/
│           │   ├── auth/
│           │   ├── gestion_restaurante/     # Proceso: Gestión de restaurante (perfil + carta + menú del día)
│           │   ├── pedidos/                 # Proceso: Pedidos entrantes
│           │   ├── reservas/                # Proceso: Reservas entrantes
│           │   ├── resenas/                 # Proceso: Gestión y respuesta a reseñas
│           │   ├── dashboard/               # HU13 — dashboard del restaurante
│           │   └── admin/                   # Proceso: Moderación y administración
│           │       ├── aprobacion/          # HU24 — aprobación de restaurantes
│           │       ├── moderacion/          # HU25 — moderación de reseñas
│           │       └── dashboard_global/    # HU26 — dashboard agregado de la plataforma
│           ├── workers/                     # WorkManager (sync de pedidos entrantes, HU09)
│           └── ai/
│               ├── digitalizacion_carta/    # HU04 — digitalización de carta con IA
│               └── alerta_resenas/          # HU15 — alerta de reseñas negativas
│
├── backend/                                 # Proyecto independiente (Kotlin + Ktor), Sprint 2 — HU07
│   └── src/main/kotlin/com/equipo/sanmarkfood/backend/
│       ├── routes/                          # Endpoints de pedidos, reservas, dashboard agregado
│       ├── models/
│       └── repository/                      # Exposed ORM sobre PostgreSQL/H2
│
└── docs/                                    # Diseño, planificación y este documento
```

✔ = ya está en el repositorio.

`ui/theme/` queda **fuera de `core/`**, donde lo crea Android Studio y donde ya lo referencia `MainActivity`. Moverlo no aporta nada y obliga a tocar imports. `core/` es solo para lo transversal que todavía no existe: `di/`, `navigation/` y `util/`.

## Por qué proyectos independientes y no un Gradle multi-módulo

El caso de estudio define San Mark Food como **dos aplicaciones móviles independientes**. Un multi-módulo Gradle (un solo `settings.gradle.kts` con `include(...)`) es técnicamente más "elegante" para compartir código, pero para un equipo de 3 personas en un curso, agrega riesgo de configuración (nombres de módulo, un solo Gradle sync que si falla bloquea a los tres) sin un beneficio que compense. Con proyectos independientes:

- Cada integrante abre solo la carpeta de la app en la que trabaja (`app-comensal` o `app-restaurante`) con `File > Open`.
- Un problema de Gradle en una app no afecta a la otra.
- El costo: si hay un modelo que se repite en ambas (ej. la clase `Restaurante`), se duplica en cada `domain/model/` en vez de vivir en un módulo compartido. Para el tamaño de este proyecto, es un costo aceptable.

Lo que sí se mantiene idéntico a mano en las dos apps es el tema (`ui/theme/` y `res/font/`): si se cambia un color o un tamaño de texto en una, hay que replicarlo en la otra. Es el único código duplicado que importa cuidar.

## Regla de capas (lo que hace que MVVM sea real y no solo nominal)

- `domain/` es Kotlin puro: modelos, interfaces de repositorio, casos de uso. Cero imports de Android.
- `data/` implementa esas interfaces (Room, Retrofit, Firebase). Nunca se accede a `data/` directamente desde `presentation/`.
- `presentation/` solo tiene `ViewModel` (`StateFlow`) y `Composable`. Ningún ViewModel llama directo a Firestore/Retrofit — siempre pasa por un `usecase`.
- `ai/` y `workers/` son transversales a las tres capas, pero su lógica pesada (llamar al LLM, procesar OCR) debe pasar por `domain/usecase/`, no vivir suelta dentro de esas carpetas.

## Decisiones técnicas

| Tema | Decisión | Por qué |
| --- | --- | --- |
| Inyección de dependencias | **Hilt** (con KSP), en las dos apps | Es el estándar de Google (guías de arquitectura, codelabs, *Now in Android*). Si falta declarar una dependencia, el proyecto no compila — el error aparece en el IDE y no como un crash en plena demo. |
| Rol de la cuenta (SCRUM-60) | Documento `usuarios/{uid}` en Firestore con el campo `rol` (`comensal`, `restaurante` o `administrador`), protegido por **reglas de Firestore** | El proyecto está en el plan **Spark**, que no permite Cloud Functions. Las reglas impiden crear una cuenta como `administrador` y cambiar el rol después de creado. El administrador se asigna a mano desde la consola. Cuando exista el backend (HU07) se puede migrar a *custom claims* sin cambiar el modelo de datos. |
| Base de datos local | **Room** (SQLite) | Requisito del curso. Se usa para lo que debe funcionar sin conexión, empezando por el carrito del comensal (HU08). |
| Lógica programada del lado servidor | Pendiente de decidir antes del Sprint 2 | Los rechazos automáticos de HU07 (pedido a los 10 min, reserva 60 min antes) no se pueden hacer con Cloud Functions en Spark. Los resolverá el backend de Ktor o habrá que replantearlos. |

Las reglas de Firestore viven en la consola de Firebase (Firestore → Reglas). **Cada colección nueva necesita su regla**: sin regla queda inaccesible, y con una regla floja queda abierta a cualquiera.

## Convenciones de código

Para que el código de los tres integrantes se lea como si lo hubiera escrito una sola persona:

- **Paquetes:** minúsculas, sin guiones ni tildes (`gestion_restaurante`, no `gestión-restaurante`).
- **Una pantalla = tres archivos en el mismo paquete:** `MenuDelDiaScreen.kt`, `MenuDelDiaViewModel.kt` y el `data class MenuDelDiaUiState` (puede vivir en el archivo del ViewModel).
- **Estado:** un solo `StateFlow<XxxUiState>` por pantalla. Los eventos de la UI son funciones del ViewModel (`onAgregarPlato()`), no `LiveData` ni callbacks sueltos.
- **Casos de uso:** verbo + sustantivo + `UseCase` (`PublicarMenuDelDiaUseCase`), con una sola función pública (`operator fun invoke(...)`).
- **Repositorios:** interfaz `MenuRepository` en `domain/repository/`, implementación `MenuRepositoryImpl` en `data/repository/`.
- **Tres representaciones de un dato, nunca mezcladas:** `MenuDto` (red), `MenuEntity` (Room), `Menu` (dominio). El mapeo vive en `data/`; un DTO nunca llega a `presentation/`.
- **Colores y tipografía solo desde el tema:** `MaterialTheme.colorScheme.primary`, `MaterialTheme.typography.titleLarge`. Ningún `Color(0xFF...)` ni `fontSize` suelto dentro de una pantalla — si falta un color, se agrega al esquema, no a la pantalla.
- **Textos:** todos en español (Perú), moneda `S/`. Nada de strings escritos dentro de un Composable → `res/values/strings.xml`.
- **Nombres en español, del dominio** (`Comensal`, `Reserva`, `MenuDelDia`); en inglés solo lo que impone el framework (`ViewModel`, `UseCase`, `Repository`, `Screen`).

## Cómo se agrega una pantalla nueva

1. Tarjeta en Jira (`SCRUM-XX`) y rama `feature/SCRUM-XX-...` — ver [`CONTRIBUTING.md`](../CONTRIBUTING.md).
2. Abrir la pantalla en el prototipo del equipo. **Ahí están los estados obligatorios** (vacío, cargando, sin conexión, error, invitado) que ya se diseñaron: implementarlos todos, no solo el caso feliz.
3. `domain/`: modelo, interfaz de repositorio y caso de uso.
4. `data/`: implementación del repositorio (Firestore / Retrofit / Room) y sus mappers.
5. `presentation/<proceso>/`: `UiState`, `ViewModel`, `Screen`.
6. Registrar la ruta en `core/navigation/` y el binding en `core/di/`.

Los criterios de aceptación de la HU son subtareas en Jira: la pantalla está lista cuando todas pasan, no cuando compila.

## Mapeo de requisitos del curso → dónde viven en el código

| Requisito del curso | Dónde va |
| --- | --- |
| Autenticación | `data/firebase/` + `presentation/auth/` en cada proyecto |
| Procesos de negocio (mín. 3, el proyecto cubre 6) | Un paquete por proceso dentro de `presentation/` de la app que corresponde + su `usecase` en `domain/` |
| Firebase | `data/firebase/` en ambas apps (mismo proyecto Firebase, dos apps Android registradas) |
| Material Design | `ui/theme/` + `res/font/` de cada app — **ya hecho**, con paleta propia y modo oscuro |
| MVVM + Clean Code | Estructura de 3 capas repetida en `app-comensal` y `app-restaurante` |
| Corrutinas + Retrofit | `data/remote/` + `domain/usecase/` (`suspend fun`) — consumen tanto `backend/` (Ktor) como APIs externas (LLM, mapas) |
| WorkManager | `workers/` en cada app |
| SQLite | `data/local/` (Room) — mínimo en `app-comensal` (carrito, HU08) |
| Dashboards | `app-restaurante/presentation/dashboard/` y `admin/dashboard_global/` (Vico o MPAndroidChart) |
| Recursos del móvil (mín. 3, el proyecto cubre 4) | Cámara → `app-restaurante` (carta, HU04); GPS, biometría, micrófono → `app-comensal` |
| Funcionalidades de IA (mín. 3, el proyecto cubre 4) | `ai/` en ambas apps |
| Despliegue en Play Store | No es código — checklist aparte en Sprint 3 (firma de cada AAB, ficha de Play Console por app) |

## Documentos relacionados

| Qué | Dónde |
| --- | --- |
| Diseño de las dos apps: prototipos navegables, decisiones de producto, paleta y equivalencia con los roles de Material 3 | Material interno del equipo, fuera del repositorio — pedírselo a Jose |
| Historias de usuario y criterios de aceptación | Jira `jlchuque.atlassian.net`, proyecto **SCRUM** (cada criterio es una subtarea) + `docs/SanMarkFood-Planificacion.xlsx` |
| Ramas, formato de commits y Pull Requests | [`CONTRIBUTING.md`](../CONTRIBUTING.md) |
