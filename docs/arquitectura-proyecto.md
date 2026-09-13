# Arquitectura del proyecto — San Mark Food

MVVM + Clean Architecture por capas, repetida en **dos proyectos de Android Studio completamente independientes** (no un Gradle multi-módulo): cada app tiene su propio `build.gradle.kts`, su propio `gradlew` y su propio ciclo de compilación. Viven como carpetas hermanas dentro del mismo repositorio de Git, pero Android Studio las abre por separado, una ventana por app.

```
SanMarkFood---Grupo-1/
│
├── app-comensal/                        # Proyecto Android independiente
│   └── app/src/main/java/com/equipo/sanmarkfood/comensal/
│       ├── core/
│       │   ├── di/                     # Módulos de Hilt/Koin
│       │   ├── navigation/             # NavGraph de Compose
│       │   ├── ui/theme/               # Colores, tipografía, Material Design 3
│       │   └── util/                   # Extensiones, helpers, Result wrapper
│       ├── data/
│       │   ├── local/
│       │   │   ├── entity/             # Entidades Room (ej. carrito local)
│       │   │   └── dao/
│       │   ├── remote/
│       │   │   ├── api/                # Interfaces Retrofit (backend propio + APIs externas)
│       │   │   └── dto/
│       │   ├── firebase/               # Wrappers de Firebase Auth / Firestore / Storage / FCM
│       │   └── repository/             # Implementaciones de los repos del dominio
│       ├── domain/                     # Kotlin puro, sin imports de Android
│       │   ├── model/
│       │   ├── repository/             # Interfaces (contratos)
│       │   └── usecase/                # Un caso de uso = una acción de negocio
│       ├── presentation/
│       │   ├── auth/
│       │   ├── descubrimiento/         # Proceso: Descubrimiento de restaurantes
│       │   ├── reservas/                # Proceso: Reservas de mesa (lado comensal)
│       │   ├── pedidos/                 # Proceso: Pedidos (lado comensal)
│       │   ├── resenas/                  # Proceso: Reseñas (calificar, ver detalle)
│       │   └── historial/               # Historial de pedidos y reservas (HU20)
│       ├── workers/                     # WorkManager (recordatorio de reserva, HU10)
│       └── ai/
│           ├── chatbot/                 # HU11 — chatbot con entrada por voz
│           └── resumen_resenas/         # HU14 — resumen de reseñas con IA
│
├── app-restaurante/                     # Proyecto Android independiente
│   └── app/src/main/java/com/equipo/sanmarkfood/restaurante/
│       ├── core/                        # (misma estructura que app-comensal)
│       ├── data/                        # (misma estructura que app-comensal)
│       ├── domain/                      # (misma estructura que app-comensal)
│       ├── presentation/
│       │   ├── auth/
│       │   ├── gestion_restaurante/     # Proceso: Gestión de restaurante (perfil + menú)
│       │   ├── pedidos/                  # Proceso: Pedidos entrantes
│       │   ├── reservas/                 # Proceso: Reservas entrantes
│       │   ├── resenas/                   # Proceso: Gestión y respuesta a reseñas
│       │   ├── dashboard/                 # HU13 — dashboard del restaurante
│       │   └── admin/                     # Proceso: Moderación y administración
│       │       ├── aprobacion/            # HU24 — aprobación de restaurantes
│       │       ├── moderacion/            # HU25 — moderación de reseñas
│       │       └── dashboard_global/      # HU26 — dashboard agregado de la plataforma
│       ├── workers/                       # WorkManager (sync de pedidos entrantes, HU09)
│       └── ai/
│           ├── digitalizacion_carta/      # HU04 — digitalización de carta con IA
│           └── alerta_resenas/            # HU15 — alerta de reseñas negativas
│
├── backend/                               # Proyecto independiente (Kotlin + Ktor), Sprint 2 — HU07
│   └── src/main/kotlin/com/equipo/sanmarkfood/backend/
│       ├── routes/                        # Endpoints de pedidos, reservas, dashboard agregado
│       ├── models/
│       └── repository/                    # Exposed ORM sobre PostgreSQL/H2
│
└── docs/
    └── arquitectura-proyecto.md
```

## Por qué proyectos independientes y no un Gradle multi-módulo

El caso de estudio define San Mark Food como **dos aplicaciones móviles independientes**. Un multi-módulo Gradle (un solo `settings.gradle.kts` con `include(...)`) es técnicamente más "elegante" para compartir código, pero para un equipo de 3 personas en un curso, agrega riesgo de configuración (nombres de módulo, un solo Gradle sync que si falla bloquea a los tres) sin un beneficio que compense. Con proyectos independientes:

- Cada integrante abre solo la carpeta de la app en la que trabaja (`app-comensal` o `app-restaurante`) con `File > Open`.
- Un problema de Gradle en una app no afecta a la otra.
- El costo: si hay un modelo que se repite en ambas (ej. la clase `Restaurante`), se duplica en cada `domain/model/` en vez de vivir en un módulo compartido. Para el tamaño de este proyecto, es un costo aceptable.

## Regla de capas (lo que hace que MVVM sea real y no solo nominal)

- `domain/` es Kotlin puro: modelos, interfaces de repositorio, casos de uso. Cero imports de Android.
- `data/` implementa esas interfaces (Room, Retrofit, Firebase). Nunca se accede a `data/` directamente desde `presentation/`.
- `presentation/` solo tiene `ViewModel` (`StateFlow`) y `Composable`. Ningún ViewModel llama directo a Firestore/Retrofit — siempre pasa por un `usecase`.
- `ai/` y `workers/` son transversales a las tres capas, pero su lógica pesada (llamar al LLM, procesar OCR) debe pasar por `domain/usecase/`, no vivir suelta dentro de esas carpetas.

## Mapeo de requisitos del curso → dónde viven en el código

| Requisito del curso | Dónde va |
| --- | --- |
| Autenticación | `data/firebase/` + `presentation/auth/` en cada proyecto |
| Procesos de negocio (mín. 3, el proyecto cubre 6) | Un paquete por proceso dentro de `presentation/` de la app que corresponde + su `usecase` en `domain/` |
| Firebase | `data/firebase/` en ambas apps (mismo proyecto Firebase, dos apps Android registradas) |
| Material Design | `core/ui/theme/` de cada app — definir paleta y tipografía **antes** de construir pantallas |
| MVVM + Clean Code | Estructura de 3 capas repetida en `app-comensal` y `app-restaurante` |
| Corrutinas + Retrofit | `data/remote/` + `domain/usecase/` (`suspend fun`) — consumen tanto `backend/` (Ktor) como APIs externas (LLM, mapas) |
| WorkManager | `workers/` en cada app |
| SQLite | `data/local/` (Room) — mínimo en `app-comensal` (carrito, HU08) |
| Dashboards | `app-restaurante/presentation/dashboard/` y `admin/dashboard_global/` (Vico o MPAndroidChart) |
| Recursos del móvil (mín. 3, el proyecto cubre 4) | Cámara → `app-restaurante` (carta, HU04); GPS, biometría, micrófono → `app-comensal` |
| Funcionalidades de IA (mín. 3, el proyecto cubre 4) | `ai/` en ambas apps |
| Despliegue en Play Store | No es código — checklist aparte en Sprint 3 (firma de cada AAB, ficha de Play Console por app) |
