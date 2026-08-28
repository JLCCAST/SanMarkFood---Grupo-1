# Arquitectura del proyecto

MVVM + Clean Architecture por capas. Esta estructura es independiente del tema del proyecto — sirve como esqueleto desde el día 1, y cada feature (proceso de negocio) se agrega como su propio paquete dentro de `presentation/`.

```
app/src/main/java/com/equipo/[nombreproyecto]/
│
├── core/                       # Código transversal
│   ├── di/                     # Módulos de Hilt/Koin
│   ├── navigation/             # NavGraph de Compose
│   ├── ui/theme/                # Colores, tipografía, Material Design 3
│   └── util/                   # Extensiones, helpers, Result wrapper
│
├── data/                       # Capa de datos
│   ├── local/
│   │   ├── entity/             # Entidades Room (SQLite)
│   │   └── dao/
│   ├── remote/
│   │   ├── api/                # Interfaces Retrofit
│   │   └── dto/
│   ├── firebase/                # Wrappers de Firebase Auth / Firestore / etc.
│   └── repository/             # Implementaciones de los repos del dominio
│
├── domain/                     # Capa de dominio (pura Kotlin, sin Android)
│   ├── model/
│   ├── repository/             # Interfaces (contratos)
│   └── usecase/                # Un caso de uso = una acción de negocio
│
├── presentation/                # Capa de UI — una carpeta por proceso de negocio
│   ├── auth/
│   │   ├── AuthViewModel.kt
│   │   └── LoginScreen.kt
│   ├── [proceso_negocio_1]/
│   ├── [proceso_negocio_2]/
│   ├── [proceso_negocio_3]/
│   └── dashboard/               # Gráficos/estadísticas
│
├── workers/                     # WorkManager (sync en background, notificaciones, etc.)
│
└── ai/                          # Funcionalidades de IA (recomendaciones, chat, procesamiento de imágenes, etc.)
```

## Mapeo de requisitos del curso → dónde viven en el código

| Requisito del curso | Dónde va |
| --- | --- |
| Autenticación | `data/firebase/` + `presentation/auth/` |
| Procesos de negocio (mín. 3) | Un paquete por proceso dentro de `presentation/` + su `usecase` en `domain/` |
| Firebase | `data/firebase/` (Auth, Firestore/Realtime DB, Analytics, ML Kit, App Distribution) |
| Material Design | `core/ui/theme/` — definir paleta y tipografía **antes** de construir pantallas |
| MVVM + Clean Code | Estructura general de las 3 capas |
| Corrutinas + Retrofit | `data/remote/` + `domain/usecase/` (usan `suspend fun`) |
| WorkManager | `workers/` |
| SQLite | `data/local/` (Room) |
| Dashboards | `presentation/dashboard/` (ej. librería Vico o MPAndroidChart) |
| Recursos del móvil (mín. 3) | Módulos específicos según el recurso (ej. `core/util/CameraHelper.kt`, `SensorManager`, `AudioRecorder`) |
| Funcionalidades de IA (mín. 3) | `ai/` — puede consumir un LLM externo vía Retrofit, ML Kit local, o modelos TensorFlow Lite |
| Despliegue en Play Store | No es código — checklist aparte en Sprint 3 (firma de app, ficha de Play Console) |

## Por qué empezar así, sin tener el tema definido

Esta estructura de carpetas y el `core/` (tema visual, DI, navegación) se pueden crear y commitear el día 1 — no dependen del tema. Cuando el equipo defina el tema (idealmente antes de la Semana 3-4, según el sílabo), solo se agregan las carpetas de los procesos de negocio concretos dentro de `presentation/` y `domain/usecase/`.
