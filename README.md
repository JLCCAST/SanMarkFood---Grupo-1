# Falta definir tema

> Proyecto del curso **Desarrollo de Sistemas Móviles (DSM)** — UNMSM, VIII ciclo, 2026-II
> Estado: 🟡 Tema en definición


## Descripción del proyecto

[Pendiente]

## Requisitos técnicos del curso (checklist)

Estos son los mínimos obligatorios según el sílabo. Marcar conforme se van cubriendo:

- [ ] Autenticación (Firebase Auth u otro)
- [ ] Mínimo 3 procesos de negocio
- [ ] Uso de herramientas Firebase (Auth, Firestore/Realtime DB, Analytics, ML Kit, App Distribution, Test Lab, según aplique)
- [ ] Material Design con paleta de colores bien formada
- [ ] Arquitectura MVVM + Clean Code
- [ ] Corrutinas + Retrofit para consumo de APIs externas
- [ ] WorkManager
- [ ] SQLite (Room)
- [ ] Dashboards (gráficos/estadísticas)
- [ ] Mínimo 3 recursos del móvil (sensores, cámara, audio, GPS, etc.)
- [ ] Mínimo 3 funcionalidades con IA (recomendaciones, chat, deep learning, LLM, procesamiento de imágenes)
- [ ] Despliegue en Google Play Store

## Stack tecnológico

- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose + Material Design 3
- **Arquitectura:** MVVM + Clean Architecture (capas `data` / `domain` / `presentation`)
- **Networking:** Retrofit + OkHttp + Corrutinas
- **Persistencia local:** Room (SQLite)
- **Backend as a service:** Firebase
- **Tareas en background:** WorkManager
- **Inyección de dependencias:** Hilt (o Koin)
- **Backend propio (si aplica):** [Ktor / Spring Boot / FastAPI / Node.js — a definir en Unidad 3]

## Planificación (Jira)

La planificación del proyecto (épicas, historias de usuario, sprints):

[Añadir enlace Jira]

## Cómo levantar el proyecto localmente

```bash
git clone https://github.com/[org-o-usuario]/[nombre-repo].git
cd [nombre-repo]
# Abrir en Android Studio (Hedgehog o superior recomendado)
# Copiar google-services.json en app/ (pedirlo al equipo, NO subirlo al repo)
# Sync Gradle y ejecutar
```

## Convenciones

Ver [`CONTRIBUTING.md`](CONTRIBUTING.md) para estrategia de ramas, formato de commits y flujo de Pull Requests.
