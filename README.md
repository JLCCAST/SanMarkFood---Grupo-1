> Proyecto del curso — SAN MARK FOOD — UNMSM, VIII ciclo, 2026-II

## Descripción del proyecto

San Mark Food es un ecosistema de dos aplicaciones móviles independientes que conectan a los restaurantes cercanos a San Marcos con su público, sostenidas por un backend propio compartido:

- **App Comensal**: para estudiantes y público en general que buscan restaurantes, reservan mesa y hacen pedidos.
- **App Restaurante**: para que cada negocio gestione su propio perfil, menú, reservas y pedidos entrantes. También aloja, en una sección dedicada, el acceso del rol Administrador.

Ambas aplicaciones comparten la misma fuente de datos (Firebase + backend propio), de modo que una acción en una app se refleja en tiempo real en la otra: un pedido hecho desde la app del comensal aparece de inmediato en la app del restaurante correspondiente.

## Estructura del repositorio

El repo contiene **tres proyectos independientes** como carpetas hermanas (no un Gradle multi-módulo — cada uno tiene su propio `build.gradle.kts` y se abre por separado en Android Studio):

```
SanMarkFood---Grupo-1/
├── app-comensal/       # Proyecto Android independiente — App Comensal
├── app-restaurante/    # Proyecto Android independiente — App Restaurante + sección Administrador
├── backend/            # Proyecto Kotlin/Ktor independiente — backend propio (Sprint 2)
└── docs/
    └── arquitectura-proyecto.md   # Detalle de capas MVVM por app
```

## Equipo

Jose Chuque · Camila Bada · Rodrigo Puente

## Requisitos técnicos del curso (checklist)

Mínimos obligatorios según el sílabo. Se marcan conforme se implementan de verdad (el estado detallado por historia de usuario vive en Jira, no aquí):

- [ ] Autenticación (Firebase Auth) — ambas apps + rol administrador
- [ ] Mínimo 3 procesos de negocio (el proyecto cubre 6: gestión de restaurante, descubrimiento, reservas, pedidos, reseñas, moderación/admin)
- [ ] Uso de herramientas Firebase (Auth, Firestore, Storage, Cloud Messaging, Cloud Functions, Test Lab)
- [ ] Material Design 3 con paleta de colores propia
- [ ] Arquitectura MVVM + Clean Code (`data` / `domain` / `presentation` en cada app)
- [ ] Corrutinas + Retrofit (consumo del backend propio y de APIs externas)
- [ ] WorkManager
- [ ] SQLite (Room)
- [ ] Dashboards (restaurante y administrador)
- [ ] Mínimo 3 recursos del móvil (cámara, GPS, biometría, micrófono — el proyecto cubre los 4)
- [ ] Mínimo 3 funcionalidades de IA (el proyecto cubre 4: digitalización de carta, chatbot con voz, resumen de reseñas, alerta de reseñas negativas)
- [ ] Despliegue en Google Play Store (2 apps)

## Stack tecnológico

- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose + Material Design 3
- **Arquitectura:** MVVM + Clean Architecture (`data` / `domain` / `presentation`)
- **Networking:** Retrofit + OkHttp + Corrutinas
- **Persistencia local:** Room (SQLite)
- **Backend as a service:** Firebase (Auth, Firestore, Storage, Cloud Messaging, Cloud Functions)
- **Backend propio:** Ktor + Exposed (ORM) + PostgreSQL/H2
- **Tareas en background:** WorkManager
- **Inyección de dependencias:** Hilt (o Koin)
- **Dashboards:** Vico o MPAndroidChart

## Planificación (Jira)

Épicas, historias de usuario y sprints:

https://jlchuque.atlassian.net/jira/software/projects/SCRUM/boards/1/backlog?selectedIssue=SCRUM-6

## Cómo levantar el proyecto localmente

```bash
git clone https://github.com/JLCCAST/SanMarkFood---Grupo-1.git
cd SanMarkFood---Grupo-1
```

Este repo contiene **dos apps independientes**, así que se abren por separado (no la carpeta raíz):

- Para trabajar en la app de comensal: Android Studio → `File > Open` → seleccionar la carpeta `app-comensal/`.
- Para trabajar en la app de restaurante: otra ventana de Android Studio → `File > Open` → seleccionar la carpeta `app-restaurante/`.

Antes de compilar, pide al equipo el `google-services.json` correspondiente (nunca se sube al repo, ya está en `.gitignore`) y colócalo en:

```
app-comensal/app/google-services.json
app-restaurante/app/google-services.json
```

Luego Sync Gradle y ejecutar.

## Convenciones

Ver [`CONTRIBUTING.md`](CONTRIBUTING.md) para estrategia de ramas, formato de commits y flujo de Pull Requests.
