> Proyecto del curso — SAN MARK FOOD — UNMSM, VIII ciclo, 2026-II


## Descripción del proyecto

San Mark Food es un ecosistema de dos aplicaciones móviles independientes que conectan a los restaurantes cercanos a San Marcos con su público, sostenidas por un backend propio compartido:
●	App Comensal: para estudiantes y público en general que buscan restaurantes, reservan mesa y hacen pedidos.
●	App Restaurante: para que cada negocio gestione su propio perfil, menú, reservas y pedidos entrantes, además aca también vivira el perfil de administrador de ambos sistemas.

Ambas aplicaciones comparten la misma fuente de datos (a través de un backend propio), de modo que una acción en una app se refleja en tiempo real en la otra: un pedido hecho desde la app del comensal aparece de inmediato en la app del restaurante correspondiente.

## Requisitos técnicos del curso (checklist)

Estos son los mínimos obligatorios según el sílabo. Marcar conforme se van cubriendo:

- [✓] Autenticación (Firebase Auth u otro)
- [✓] Mínimo 3 procesos de negocio
- [✓] Uso de herramientas Firebase (Auth, Firestore/Realtime DB, Analytics, ML Kit, App Distribution, Test Lab, según aplique)
- [✓] Material Design con paleta de colores bien formada
- [✓] Arquitectura MVVM + Clean Code
- [✓] Corrutinas + Retrofit para consumo de APIs externas
- [✓] WorkManager
- [✓] SQLite (Room)
- [✓] Dashboards (gráficos/estadísticas)
- [✓] Mínimo 3 recursos del móvil (sensores, cámara, audio, GPS, etc.)
- [✓] Mínimo 3 funcionalidades con IA (recomendaciones, chat, deep learning, LLM, procesamiento de imágenes)
- [✓] Despliegue en Google Play Store

## Stack tecnológico

- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose + Material Design 3
- **Arquitectura:** MVVM + Clean Architecture (capas `data` / `domain` / `presentation`)
- **Networking:** Retrofit + OkHttp + Corrutinas
- **Persistencia local:** Room (SQLite)
- **Backend as a service:** Firebase
- **Tareas en background:** WorkManager
- **Inyección de dependencias:** Hilt (o Koin)
- **Backend propio**

## Planificación (Jira)

La planificación del proyecto (épicas, historias de usuario, sprints):

https://jlchuque.atlassian.net/jira/software/projects/SCRUM/boards/1/backlog?selectedIssue=SCRUM-6


## Cómo levantar el proyecto localmente

```bash
git clone https://github.com/JLCCAST/SanMarkFood---Grupo-1.git
cd SanMarkFood--Grupo1
# Abrir en Android Studio (Hedgehog o superior recomendado)
# Copiar google-services.json en app/ (pedirlo al equipo, NO subirlo al repo)
# Sync Gradle y ejecutar
```

## Convenciones

Ver [`CONTRIBUTING.md`](CONTRIBUTING.md) para estrategia de ramas, formato de commits y flujo de Pull Requests.
