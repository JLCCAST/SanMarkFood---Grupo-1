# Arquitectura del proyecto — San Mark Food

MVVM + Clean Architecture por capas, repetida en **dos proyectos de Android Studio completamente independientes** (no un Gradle multi-módulo): cada app tiene su propio `build.gradle.kts`, su propio `gradlew` y su propio ciclo de compilación. Viven como carpetas hermanas dentro del mismo repositorio de Git, pero Android Studio las abre por separado, una ventana por app.

## Estado actual del código (25 de octubre de 2026)

El árbol de la sección siguiente es el **destino**, no lo que hay hoy en disco. Antes de buscar una carpeta, ten esto claro:

**Ya existe:**

- Los dos proyectos creados con la plantilla de Compose.
- El tema Material 3 real en `ui/theme/` de cada app: `Color.kt`, `Type.kt` y `Theme.kt` con la paleta propia (guinda, ají, verde) en modo claro y oscuro, más las tres fuentes en `res/font/`. Sale del diseño ya cerrado, no es la plantilla de Android Studio.
- **App Restaurante — HU01 (SCRUM-30) completa:** registro, verificación de correo, inicio de sesión persistente, recuperación de contraseña y rol de la cuenta, con sus estados de cargando, error y sin conexión. Con ella entraron `core/di/`, `core/navigation/`, `data/firebase/`, `data/repository/`, `domain/` (`model/`, `repository/`, `usecase/`) y `presentation/auth/`, y en su `build.gradle.kts` Hilt, Navigation Compose, Firebase Auth y Firestore. Después se agregó **«Continuar con Google»** en R1, debajo de «Iniciar sesión» (no está en el prototipo): sirve para entrar y para crear la cuenta.
- **App Restaurante — HU02 (SCRUM-31) completa:** alta del local en tres pasos (R3 datos, fotos y mapa; R4 horario), local en revisión con su estado en vivo (R5), rechazo con motivo y reenvío (R6, R7), edición del perfil y del horario (O7, O8) y pausa de pedidos. Con ella entraron:
  - `presentation/gestion_restaurante/`: R3 a R7, O7 y O8. Las pantallas del alta se reutilizan para editar y para corregir con un `ModoFormulario` en la ruta.
  - `presentation/panel/`: el panel del local con la barra inferior (Pedidos · Reservas · Menú · Reseñas · Negocio). Las pestañas que todavía no existen muestran un aviso provisional.
  - `presentation/dashboard/`, solo con la sección «Tu local» de O6 (los indicadores son de HU13), y `presentation/pedidos/`, solo con la cabecera de O1 y su interruptor de pausa (la bandeja es de HU09).
  - `data/local/LectorImagenes.kt`, que achica las fotos antes de subirlas, y en el Gradle Firebase Storage, Coil y Maps Compose.
  - Las primeras pruebas unitarias, en `app/src/test/`: casos de uso probados con un repositorio falso.
- **App Restaurante — carpetas por proceso (acuerdo del 3 oct):** `domain/model/` y `domain/usecase/` se dividieron en carpetas por proceso (`auth/`, `gestion_restaurante/` y, en casos de uso, `pedidos/`), con los mismos nombres que `presentation/`. Las pruebas siguen la misma división.
- **App Restaurante — HU03 (SCRUM-32) completa:**
  - SCRUM-69 (categorías): `domain/model/menu/`, `MenuRepository`, `domain/usecase/menu/` y, en `data/`, `MenuDataSource` y `MenuRepositoryImpl`.
  - SCRUM-67 (alta de plato): `presentation/menu/` con la pestaña Menú del panel (cabecera «Menú de hoy · Carta»), M7 (la carta agrupada por categoría) y M8 (alta de plato, con su foto y el chip «+ Nueva categoría»). La foto del plato se sube a Storage al guardar y, después de cada plato, se recalcula `rangoCarta`. En R5, el ítem «Carta» tiene el botón «Cargar» y su estado real.
  - SCRUM-68 (editar y eliminar): al tocar un plato en M7 se abre M8 en modo edición (la ruta `Plato` lleva su id), con el botón de eliminar y su confirmación. Al cambiar la foto o eliminar el plato se borra la foto vieja de Storage, y `rangoCarta` se recalcula, o se quita si la carta queda vacía.
  - SCRUM-148 (publicar el menú del día): M1 «Menú de hoy» (sin publicar, con «Empezar de cero», y la tarjeta del menú publicado) y M5 para armarlo: precio, entradas y segundos, refresco y postre, y la hora de fin, que por defecto es 15:00.
  - SCRUM-70 (disponible / agotado): interruptor verde en cada plato de M7 y en cada opción del menú publicado, que se aplica al instante, y el bloque «Disponible hoy» en M8, que se guarda con el plato.
  - SCRUM-161, primera parte: «Copiar el de ayer» en M1. Aparece solo si ayer se publicó un menú, y abre M5 con ese menú cargado. Al publicarlo queda con `origen: "ayer"` y con todas sus opciones disponibles.
  - SCRUM-161, segunda parte: en el menú publicado, la hora de fin («Se sirve hasta»), «Editar menú» (abre M5 con lo publicado y conserva las opciones agotadas), «Terminar menú de hoy» con su confirmación y «Reabrir». Cada cambio actualiza también `menuHoy`, así que el comensal ve «Menú agotado» apenas el local lo termina.
  - Queda para HU04: «Foto de la pizarra» en M1 y «Escanear carta» en M7.
- **App Restaurante — HU04 (SCRUM-33) completa:**
  - SCRUM-73, primera parte (el borrador del menú del día): la primera base de datos local, con Room. `data/local/` tiene `SanMarkFoodDatabase`, `entity/` y `dao/`; `core/di/DatabaseModule` la entrega a Hilt, y `BorradorMenuRepository` la usa. Mientras el local arma el menú en M5, cada cambio se guarda en el celular, y al publicar se borra (ver «El borrador del menú del día», más abajo).
  - SCRUM-71 (la cámara): «Foto de la pizarra» en M1, destacada en guinda, abre M2 (`CamaraScreen`, ruta `Camara`). La vista previa y la foto son de CameraX; el permiso de la cámara se pide al entrar y, si se niega, M2 lo explica y deja elegir una foto de la galería. La foto queda en la caché de la app (`pizarra.jpg`). M2 es oscura en los dos temas: se dibuja con el tema oscuro. Por ahora solo fotografía la pizarra: la pestaña «Carta» (M9) llega después.
  - SCRUM-72 (la lectura con IA): al tomar o elegir la foto, M2 muestra «Leyendo tu pizarra…» y Gemini la lee (`ai/digitalizacion_carta/LectorPizarra`, a través de `PizarraRepository` y `LeerPizarraUseCase`). El resultado llena el borrador del menú del día con origen `ia`, y «Revisar resultado» abre M5 con él, con el aviso «Leímos tu pizarra» (es M3). Si falla o la foto no tiene un menú, M2 lo dice y deja tomar otra.
  - SCRUM-73, segunda parte (volver a la lectura): M1 observa el borrador de Room con un `Flow` (`ObservarPizarraLeidaUseCase`). Si hoy hay una lectura sin publicar, «Foto de la pizarra» dice «Ya leímos tu pizarra» y abre M3 con ella. En M3, «Tomar otra foto» vuelve a la cámara, y la lectura nueva reemplaza a la anterior. Ese botón y ese texto no están en el prototipo.
  - SCRUM-73, tercera parte (los datos dudosos): Gemini marca el precio (`precioDudoso`) y cada plato (`dudoso`) que no leyó bien. En M3 se ven en amarillo (fondo `secondaryContainer`, borde `secondary`), y cada plato dudoso dice «Revisa: no se leyó bien». El precio deja de estar marcado al corregirlo; un plato, al quitarlo. Mientras quede algo marcado, la pista de abajo dice «Aún hay datos marcados para revisar», pero se puede publicar igual.
- **App Restaurante — HU23 (SCRUM-34) completa:** el administrador entra por el mismo inicio de sesión que los restaurantes; en la app no hay registro de administradores. Con el `rol` de `usuarios/{uid}`, la app abre `presentation/admin/PanelAdministradorScreen`: la sección de administración, con la cabecera pizarra («ADMINISTRACIÓN» y «Salir») y la barra Solicitudes · Reportes · Métricas.
- **App Restaurante — HU24 (SCRUM-35) completa:**
  - SCRUM-76 (solicitudes pendientes): `domain/model/admin/`, `SolicitudesRepository`, `domain/usecase/admin/` y, en `data/`, `SolicitudesDataSource` y `SolicitudesRepositoryImpl`. A1 vive en `presentation/admin/aprobacion/`: escucha en vivo los locales en `pendiente`, de la solicitud más reciente a la más antigua, con la etiqueta «Nuevo» o «Reenviado», el rechazo anterior y la cantidad de platos (un `count()` al servidor por local). La pestaña Solicitudes muestra cuántas hay; Reportes y Métricas muestran un aviso provisional hasta HU25 y HU26.
  - SCRUM-77 (aprobar o rechazar): A2 (`RevisarSolicitudScreen`, ruta `RevisarSolicitud(uid)`) se abre al tocar una tarjeta de A1. Muestra el local en vivo (portada, logo, correo de la cuenta, el rechazo anterior con los campos corregidos, dirección, teléfono, horario, platos y categorías, y el mapa), con «Rechazar» y «Aprobar» mientras está `pendiente`. Rechazar abre la hoja «Rechazar el registro»: uno o más motivos con casillas y un detalle, obligatorio si se marca «Otro». La decisión la escriben `RevisionRepository` y `RevisionDataSource` en una transacción que solo aplica si el local sigue `pendiente` (si no, «Esta solicitud ya fue revisada»). A1 tiene los filtros «Pendientes (n)», «Aprobados» y «Rechazados»; los revisados van de la última decisión a la primera, con «aprobado el …» o «rechazado el …».
  - SCRUM-78 (aviso al local con el resultado): el celular del local guarda su token de FCM en `usuarios/{uid}/dispositivos/{token}` al iniciar sesión o abrir la app (`RegistrarDispositivoUseCase`, solo cuentas de restaurante) y lo borra del celular al cerrar sesión. `avisos/AvisosService` recibe los tokens nuevos y muestra el aviso con la app abierta, en el canal «Estado del local»; el panel pide el permiso de notificaciones (Android 13+) mientras el local espera la revisión. La función `revisarLocal` de `functions/` envía el aviso cuando el `estado` pasa de `pendiente` a `aprobado` o `rechazado`, y borra los tokens vencidos. A2 lo confirma con «…y le enviamos una notificación».
  - Re-evaluación por cambios sensibles (regla agregada a la historia): si un local `aprobado` cambia la dirección, la ubicación, la portada o el logo, vuelve a `pendiente`; si cambia solo el nombre, la categoría o el teléfono, sigue aprobado. Antes de guardar, O7 avisa con «¿Enviar tu local a revisión?» (`RequiereNuevaRevisionUseCase`), y el panel pasa solo a la pestaña Pedidos para mostrar «En revisión». El administrador lo ve en A1 con la etiqueta «Actualizado» y en A2 con el aviso «Actualización de datos» y los campos que cambiaron.
  - Las reglas de Firestore ya viven en el repositorio, en `firestore.rules` (ver «Reglas de seguridad», más abajo).
- **App Comensal — HU05 (SCRUM-36) completa:** inicio de sesión, registro, verificación de correo, recuperación de contraseña y exploración sin cuenta, con rol de la cuenta: documento `usuarios/{uid}` con `rol: comensal`, creado al registrarse, y rechazo de cuentas de otro rol al iniciar sesión (cuentas antiguas sin documento se crean como comensal). Ya con Hilt, Firestore y capa `domain/`, alineada con esta arquitectura.
- **App Comensal — HU19 (SCRUM-37) completa:** gestión de perfil — nombre, teléfono y foto (SCRUM-83, en Firestore + Storage), direcciones frecuentes (SCRUM-84), switches de notificaciones de reservas y reseñas (SCRUM-158), tema claro/oscuro (SCRUM-159) y listener en tiempo real del perfil (SCRUM-85). Con ella entraron `domain/model/perfil/`, `domain/usecase/perfil/` y `presentation/perfil/`.
- **App Comensal — carpetas por proceso (acuerdo del 3 oct):** aplicado en `auth/` y `perfil/`, igual que en el restaurante.
- **App Comensal — `domain/` sin Android (SCRUM-86):** los errores de auth se devuelven como `ErrorAuth` (sealed class en `domain/model/auth/`), con la traducción de las excepciones de Firebase hecha en `data/repository/AuthRepositoryImpl.kt`, siguiendo el mismo patrón que ya usa `app-restaurante`.
- **App Comensal — HU06 (SCRUM-38) completa:** en `presentation/descubrimiento/`, la lista de restaurantes aprobados ordenada por distancia (SCRUM-86), los filtros con «Con menú hoy» y «Abierto ahora» y el mapa con un pin por local (SCRUM-87), el detalle del local con favoritos y la sincronización entre tarjeta y mapa (SCRUM-88) y la barra superior del mapa (SCRUM-149).
- **App Comensal — misma estructura que el restaurante (refactor previo a HU08):** Navigation Compose con rutas tipadas en `core/navigation/` (`Rutas.kt` y `ComensalNavGraph`), Hilt en `core/di/` (`FirebaseModule` y `RepositoryModule`) y la barra inferior en `presentation/panel/PanelComensalScreen.kt`. El `GestorSesion` sigue siendo la fuente única de la sesión: el NavGraph lo observa y lleva a la pantalla que corresponde (inicio de sesión, verificación, invitado o panel). El detalle del local es su propia ruta y comparte el `DescubrimientoViewModel` del panel. Todavía sin `data/firebase/` (los `RepositoryImpl` usan Firestore directo).
- Todo el diseño de pantallas, decidido y revisado: 46 pantallas del comensal y 29 del restaurante. Los prototipos son material interno del equipo, fuera del repositorio.

**Todavía no existe:**

- En `app-comensal`: `data/firebase/`, Room, `workers/`, `ai/` y los procesos `pedidos/`, `reservas/`, `resenas/`, `historial/` y `notificaciones/`.
- En `app-restaurante`: `core/util/`, `data/remote/`, `workers/`, `ai/alerta_resenas/`, los paquetes `reservas/` y `resenas/` de `presentation/` y, dentro de `presentation/admin/`, las carpetas `moderacion/` y `dashboard_global/`.
- En `functions/`: las funciones de HU07 (`crearPedido`, `actualizarPedido`, `reservas`, `resenas`, `rankingPlatos`, `vencimientos` y `resumenResenas`). Hoy solo existe `revisarLocal` (SCRUM-78).

Esas carpetas se crean **una por una, cuando la primera historia de usuario que las necesita entra en desarrollo** — no se arma el esqueleto completo vacío de entrada.

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
│           │   │   ├── api/                 # Interfaces Retrofit (Cloud Functions del equipo + Geocoding API)
│           │   │   └── dto/
│           │   ├── firebase/                # Wrappers de Firebase Auth / Firestore / Storage / FCM
│           │   └── repository/              # Implementaciones de los repos del dominio
│           ├── domain/                      # Kotlin puro, sin imports de Android
│           │   ├── model/<proceso>/         # Una carpeta por proceso, igual que en presentation/
│           │   ├── repository/              # Interfaces (contratos)
│           │   └── usecase/<proceso>/       # Un caso de uso = una acción de negocio
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
│           ├── core/                        # (misma estructura que app-comensal) — di/ y navigation/ ✔
│           ├── data/                        # (misma estructura que app-comensal) — firebase/, repository/ ✔ y local/ (LectorImagenes y Room: entity/, dao/) ✔
│           ├── domain/
│           │   ├── model/                   # admin/, auth/, gestion_restaurante/ y menu/ ✔
│           │   ├── repository/              # AuthRepository, RestauranteRepository, MenuRepository, BorradorMenuRepository, SolicitudesRepository y RevisionRepository ✔
│           │   └── usecase/                 # admin/, auth/, gestion_restaurante/, menu/ y pedidos/ ✔
│           ├── presentation/
│           │   ├── auth/                    # HU01 — registro, verificación, inicio de sesión, recuperar contraseña ✔
│           │   ├── panel/                   # Panel del local con la barra inferior; cada pestaña es de su proceso ✔
│           │   ├── gestion_restaurante/     # Proceso: Gestión de restaurante (perfil, horario y estado del local) ✔ (HU02)
│           │   ├── menu/                    # Proceso: Carta y menú del día (HU03, HU04) — HU03 ✔
│           │   ├── pedidos/                 # Proceso: Pedidos entrantes — cabecera de O1 con la pausa ✔ (HU02)
│           │   ├── reservas/                # Proceso: Reservas entrantes
│           │   ├── resenas/                 # Proceso: Gestión y respuesta a reseñas
│           │   ├── dashboard/               # HU13 — dashboard del restaurante — sección «Tu local» ✔ (HU02)
│           │   └── admin/                   # Proceso: Moderación y administración — acceso del administrador ✔ (HU23)
│           │       ├── aprobacion/          # HU24 — aprobación de restaurantes — solicitudes, filtros y revisión ✔ (SCRUM-76, SCRUM-77)
│           │       ├── moderacion/          # HU25 — moderación de reseñas
│           │       └── dashboard_global/    # HU26 — dashboard agregado de la plataforma
│           ├── avisos/                      # Avisos push (FCM): AvisosService y el canal «Estado del local» ✔ (SCRUM-78)
│           ├── workers/                     # WorkManager: sin uso por ahora (el recordatorio de HU10 vive en el comensal)
│           └── ai/
│               ├── digitalizacion_carta/    # HU04 — digitalización de carta con IA — la pizarra ✔ (LectorPizarra)
│               └── alerta_resenas/          # HU15 — alerta de reseñas negativas
│
├── functions/                               # Cloud Functions (TypeScript, Node 22) — revisarLocal ✔ (SCRUM-78); el resto, HU07
│   └── src/                                 # index.ts, una función por archivo (revisarLocal.ts) y sus ayudas (avisos.ts)
├── firestore.rules                          # Reglas de seguridad de Firestore ✔ (se publican con la CLI)
├── firebase.json, .firebaserc               # Configuración de la CLI de Firebase: proyecto, reglas y funciones ✔
│
└── docs/                                    # Diseño, planificación y este documento
```

✔ = ya está en el repositorio.

`ui/theme/` queda **fuera de `core/`**, donde lo crea Android Studio y donde ya lo referencia `MainActivity`. Moverlo no aporta nada y obliga a tocar imports. `core/` es solo para lo transversal: `di/`, `navigation/` y `util/`.

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
| Inyección de dependencias | **Hilt** (con KSP), en las dos apps. Con AGP 9 hace falta Hilt 2.59 o superior | Es el estándar de Google (guías de arquitectura, codelabs, *Now in Android*). Si falta declarar una dependencia, el proyecto no compila — el error aparece en el IDE y no como un crash en plena demo. |
| Navegación | **Navigation Compose con rutas tipadas**: cada ruta es un `@Serializable` en `core/navigation/Rutas.kt` (plugin `kotlin.serialization`) | Los argumentos (por ejemplo, el correo en `VerificarCorreo`) viajan con su tipo y el compilador avisa si falta uno. Es la forma recomendada desde Navigation 2.8. |
| Rol de la cuenta (SCRUM-60) | Documento `usuarios/{uid}` en Firestore con los campos `rol` (`comensal`, `restaurante` o `administrador`), `correo` y `creadoEn`, protegido por **reglas de Firestore**. Cada app lo crea al registrarse con su rol; si una cuenta verificada no lo tiene (el registro se cortó a medias), la app lo crea al iniciar sesión | Cuando se decidió, el proyecto estaba en el plan **Spark**, que no permite Cloud Functions (desde HU02 está en Blaze; ver «Plan de Firebase»). Las reglas impiden crear una cuenta como `administrador` y cambiar el rol después de creado. El administrador se asigna a mano desde la consola. Con las Cloud Functions de HU07 se puede migrar a *custom claims* sin cambiar el modelo de datos. |
| Inicio de sesión con Google (HU01) | **Credential Manager** de Android (`GetSignInWithGoogleOption`) da el token de la cuenta de Google y con él se entra a Firebase Auth (`GoogleAuthProvider`). Después sigue el mismo camino que el correo: `ObtenerEstadoSesionUseCase` decide a dónde ir | Una cuenta nueva queda con `usuarios/{uid}` de rol `restaurante` (lo crea `estadoSesion()`, como en el registro) y va al alta del local, sin «Confirma tu correo»: Google ya lo verificó. Un correo de comensal se rechaza igual que con contraseña. Para que funcione, el proveedor Google tiene que estar activo en Firebase Auth, la SHA-1 de cada PC de desarrollo registrada en el proyecto y cada integrante con el `google-services.json` que trae el cliente OAuth (sin él, `default_web_client_id` no existe y la app no compila). Ese archivo no se sube al repositorio. |
| Cuentas entre las dos apps | **Un correo = una cuenta = un rol.** Al iniciar sesión, cada app rechaza las cuentas que no son suyas: la del restaurante cierra la sesión de un comensal, y la del comensal debe hacer lo mismo con restaurantes y administradores (HU05) | Las dos apps comparten el proyecto de Firebase: un correo es un solo usuario de Auth con un solo `usuarios/{uid}`, y las reglas congelan el rol. Quien sea comensal y dueño de un local usa dos correos. |
| Arranque y sesión | La app arranca en `Arranque`, que lee la sesión guardada y navega según `EstadoSesion` (sin sesión, sin verificar, activa con su rol). Todo cambio de sesión —iniciar, verificar el correo, cerrar— limpia el historial de navegación | Firebase Auth guarda la sesión y Firestore deja el rol en caché, así que se entra directo, también sin conexión. Con el historial limpio, «atrás» nunca vuelve a una pantalla de antes de iniciar o cerrar sesión. |
| Base de datos local | **Room** 2.8.4 (SQLite), una base por app. En el restaurante, el borrador del menú del día (HU04); en el comensal, el carrito (HU08) | Requisito del curso. Guarda lo que no debe perderse si se cierra la app y que no le sirve a nadie más: por eso no va en Firestore. |
| Servidor y datos (HU07, acuerdo del 3 oct) | **Todos los datos en Firestore.** Lo que no puede vivir en el celular va en **Cloud Functions**: funciones HTTPS, que las apps llaman con Retrofit, y funciones programadas. Sin Ktor ni PostgreSQL | Hay tareas que una app no puede hacer de forma confiable: los avisos push a la otra app, los rechazos automáticos (pedido a los 10 min, reserva 60 min antes), el cupo por franja y validar precio y menú al crear un pedido. Con una sola base, la otra app se entera en tiempo real sin programar nada, hay un solo esquema y no hay servidor que mantener encendido. PostgreSQL no es requisito del curso: la base relacional que pide es SQLite (Room). |
| Retrofit (acuerdo del 3 oct; Geocoding movido a HU08 el 25 oct) | Para las **funciones HTTPS** del equipo y para **Geocoding API**, que convierte en coordenadas la dirección de delivery del pedido (HU08, SCRUM-167) | Requisito del sílabo (Corrutinas + Retrofit). Las funciones se escriben como HTTPS normales y no como «callable», porque esas se llaman con el SDK de Firebase y no usan Retrofit. Geocoding usa una clave propia, restringida solo a esa API y con tope diario. El mapa (Maps SDK) y la IA tienen su propio SDK y no pasan por Retrofit. |
| Dashboards | Consultas de agregación de Firestore (`count()`, `sum()`, `average()`); el ranking de platos más pedidos lo calcula una función | Firestore suma y cuenta en el servidor, así que no hacen falta colecciones de estadísticas ni otra base. |
| Plan de Firebase (HU02) | **Blaze**, con la prueba gratuita de Google Cloud. El bucket de Storage está en `us-east1` | Desde febrero de 2026, Cloud Storage para Firebase exige Blaze. En `us-east1` el uso de Storage entra en la capa gratuita de Google Cloud. La prueba dura 90 días: al terminar hay que activar la cuenta de facturación o el proyecto vuelve a Spark y Storage deja de funcionar. Conviene tener una alerta de presupuesto. |
| Local del restaurante (HU02) | Documento `restaurantes/{uid}`, con el mismo `uid` de la cuenta. Estados: `borrador` → `pendiente` → `aprobado` o `rechazado` (y de `rechazado` otra vez a `pendiente` al reenviar) | Una cuenta = un local: el `uid` como id lo garantiza sin consultas. Campos, estados y reglas en «El documento del local», más abajo. |
| Estado del local en pantalla (HU02) | El panel escucha `restaurantes/{uid}` en vivo (*snapshot listener*) | Si el administrador aprueba o rechaza el local, la pantalla del restaurante cambia sola, sin reiniciar la app. |
| Escrituras que no pueden quedar en espera (HU02) | Antes de guardar, una lectura al servidor (`Source.SERVER`); el reenvío de R7 usa una transacción | Sin conexión, Firestore deja las escrituras en espera sin fallar. Así la app muestra «Sin conexión» al instante, y una pausa de pedidos nunca aparenta haberse guardado. |
| Fotos del local (HU02) | **Firebase Storage** + **Coil**. Se achican en el celular (portada de 1600 px, logo de 512 px, en JPEG). Cada subida tiene un nombre nuevo (`portada-<hora>.jpg`) y, al guardar, se borran las que ya no se usan | Cuidar la capa gratuita. Al sobrescribir un archivo, Storage cambia su URL de descarga y la URL guardada en Firestore dejaría de funcionar. Se eligen con el selector de fotos de Android, que no pide permisos. |
| IA de la pizarra (HU04, SCRUM-72) | **Firebase AI Logic** con la **Agent Platform Gemini API** (antes Vertex AI: `GenerativeBackend.agentPlatform()`, en la ubicación `global`) y el modelo `gemini-3.5-flash`. Gemini recibe la foto (achicada a 1600 px) y responde un JSON con forma fija (`responseSchema`): precio, entradas, segundos, refresco y postre | Gemini es multimodal: lee el texto de la foto (OCR) y lo ordena (LLM) en un solo paso, así que no hace falta un OCR aparte, que además lee mal la letra a mano. El SDK de Firebase no necesita clave en la app ni servidor propio. `gemini-3.5-flash` es estable al menos hasta mayo de 2027. No se usa la Gemini Developer API: con el proyecto en Blaze, exige comprar créditos prepagados en AI Studio y no acepta el crédito de prueba de Google Cloud. Agent Platform se cobra a la facturación del proyecto, así que la cubre ese crédito mientras dure la prueba. |
| App Check (HU04) | **Aplicado en Firebase AI Logic**: el servidor solo acepta pedidos de la app original. En las versiones debug, el proveedor de depuración (`src/debug/…/ProveedorAppCheck.kt`); en release, Play Integrity (`src/release/…`). `RestauranteApp` lo instala al abrir la app | La configuración de Firebase va dentro del APK y cualquiera podría usarla para llamar a Gemini a costa del proyecto. Play Integrity solo certifica apps instaladas desde Play Store, por eso en desarrollo cada celular usa un token de depuración (ver «App Check», más abajo). El proveedor de depuración no entra en el APK de release. Firestore y Storage no lo exigen todavía. |
| Chatbot (HU11, acuerdo del 25 oct) | **Gemini con Firebase AI Logic**, como la pizarra. El prompt lleva los datos del local (horario, menú de hoy, carta) y el pedido o la reserva activa del comensal, leídos de Firestore. La voz entra con `SpeechRecognizer` | Ya está configurado en el proyecto (crédito de Google Cloud, App Check) y no necesita clave ni servidor. No pasa por Retrofit: ese requisito ya lo cubren las funciones de HU07 y Geocoding. La conversación no se guarda. |
| Cámara (HU04, SCRUM-71) | **CameraX** 1.6.2 con `LifecycleCameraController` y `PreviewView` (`camera-view`) | El controlador junta la vista previa, la foto y el ciclo de vida: se enciende al abrir M2 y se apaga al salir, sin código propio. La galería usa el mismo selector de fotos de HU02, que no pide permisos. |
| Mapa (HU02) | **Maps Compose** (`maps-compose`). La clave va en `local.properties` como `MAPS_API_KEY` y el Gradle la pasa al manifiesto | La clave no se sube al repositorio. Cada integrante agrega la suya; sin ella la app compila y abre, pero el mapa sale en blanco. |
| Versiones fijadas (HU02) | Coil 3.4.0 y maps-compose 8.3.1 | Las versiones más nuevas traen `kotlin-stdlib` 2.4 y el proyecto compila con Kotlin 2.2.10, que no puede leerla. Para subirlas hay que actualizar Kotlin en todo el proyecto. |

Las reglas de Firestore viven en la consola de Firebase (Firestore → Reglas), y las de Storage en Storage → Reglas. **Cada colección o carpeta nueva necesita su regla**: sin regla queda inaccesible, y con una regla floja queda abierta a cualquiera.

**App Check: registrar tu celular (una vez por integrante).** Sin esto, la lectura de la pizarra falla con «Algo salió mal» (el servidor responde «Firebase App Check token is invalid»):

1. Instala la app desde Android Studio en tu celular y ábrela.
2. En Logcat, filtra por `DebugAppCheckProvider`. Aparece tu token (un código como `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`) en el mensaje «Failed to exchange debug token (…)» o «Enter this debug secret…».
3. En Firebase → **App Check** → **Apps** → **Restaurantes** → ⋮ → **Administrar tokens de depuración**, agrégalo con tu nombre.

El token vive en los datos de la app: si la desinstalas o borras sus datos, cambia y hay que registrarlo otra vez. Para la versión de Play Store (Sprint 3) hay que registrar en App Check la huella SHA-256 de la clave de firma de Play Console y enlazar la Play Integrity API.

Las colecciones con datos reales deben exigir además `request.auth.token.email_verified == true`, como ya lo hace `restaurantes` desde HU02. La app no deja pasar a una cuenta sin verificar, pero es la regla la que lo garantiza en el servidor. Ojo: el token guardado no se entera de la verificación hasta que se refresca. Por eso, después de «Ya lo confirmé» la app pide uno nuevo con `getIdToken(true)`, en `AuthDataSource.correoVerificado()`.

## Modelo de datos (acuerdo del 3 de octubre)

Todos los datos viven en Firestore; las fotos, en Storage; el carrito del comensal y el borrador del menú del día, en Room. Esta es la vista completa. El detalle campo por campo de cada colección se escribe en su propia sección cuando su HU la implementa, como «El documento del local».

**Reglas del esquema:**

- El `uid` es el id cuando hay uno por cuenta (`usuarios`, `restaurantes`).
- El id del documento asegura «uno solo»: un menú por fecha, una reseña por pedido, un reporte por comensal, una franja por hora.
- Lo que es de un local vive dentro del local (subcolecciones); lo que comparten dos actores va arriba (`pedidos`, `reservas`).
- Copias solo donde hacen falta: para mostrar algo en una sola consulta (el menú de hoy dentro del local, el nombre del local dentro del pedido) o para que un dato no cambie después (el precio dentro del pedido).
- El dinero en céntimos, como entero: `1400` es S/ 14.00. Firestore guarda los decimales como *double*, y una suma con decimales puede dar S/ 37.999999.
- Los momentos, con la hora del servidor (`creadoEn`); los días y horas del negocio, en texto y en hora de Lima (`"2026-10-03"`, `"15:00"`), como el horario de HU02.
- Los valores fijos, en minúsculas y sin tildes (`publicado`, `en_camino`); `data/` los traduce a `enum`.
- Lo que tiene consecuencias —un aviso push, IA, un cupo, un contador o un plazo— lo escriben las funciones. Las apps escriben directo solo sus propios datos: el perfil, el local, la carta y el menú.

| Colección | Qué guarda | La escribe | HU |
| --- | --- | --- | --- |
| `usuarios/{uid}` | Rol y correo; del comensal, además nombre, teléfono, foto, direcciones frecuentes (lista `direcciones`, cada una con `etiqueta`, `direccionTexto`, `latitud`, `longitud`) y preferencias de avisos | la app (el rol, al registrarse); el resto del perfil, el comensal | HU01, HU05, HU19 |
| `usuarios/{uid}/dispositivos/{token}` | El token de avisos de cada celular | cada app | HU09, HU12, HU15, HU21 |
| `usuarios/{uid}/notificaciones/{id}` | La bandeja de avisos (C5) | las funciones | HU09, HU10, HU12, HU21 |
| `restaurantes/{uid}` | El local (ver «El documento del local») | el local; el administrador aprueba o rechaza | HU02, HU24 |
| `restaurantes/{uid}/categorias/{id}` | Las categorías de la carta y su orden | el local | HU03 |
| `restaurantes/{uid}/platos/{id}` | La carta | el local | HU03, HU04 |
| `restaurantes/{uid}/menus/{AAAA-MM-DD}` | El menú del día | el local | HU03, HU04 |
| `restaurantes/{uid}/franjas/{AAAA-MM-DD_HHmm}` | Los comensales que ocupan cada franja (el cupo) | las funciones | HU10, HU12 |
| `restaurantes/{uid}/resenas/{pedidoId}` | Una reseña por pedido, con el «me gustó» por plato, el sentimiento (IA) y la respuesta del local | las funciones | HU08, HU15, HU21, HU22 |
| `…/resenas/{pedidoId}/reportes/{comensalUid}` | Un reporte por comensal | las funciones | HU22, HU25 |
| `pedidos/{id}` | El pedido, con sus ítems y precios copiados | las funciones | HU08, HU09, HU20 |
| `reservas/{id}` | La reserva | las funciones | HU10, HU12, HU20 |
| `moderaciones/{id}` | El historial de moderación | las funciones | HU25 |

**Estados:**

- **Pedido:** `recibido` → `preparando` → `listo` (recojo) o `en_camino` (delivery) → `entregado`. Además, `rechazado` (por el local, o solo a los 10 minutos sin respuesta) y `cancelado` (por el comensal, antes de que lo acepten).
- **Reserva:** `pendiente` → `confirmada`. Además, `rechazada` (por el local, o sola 60 minutos antes) y `cancelada`. Si el comensal la modifica, vuelve a `pendiente`.
- **Menú del día:** no hay documento hasta que se publica; después, `publicado` o `terminado` («Terminar menú de hoy» y «Reabrir»).

**Las funciones (HU07):** `crearPedido`, `actualizarPedido`, `reservas`, `resenas` y `rankingPlatos`, que son HTTPS y las apps llaman con Retrofit; `vencimientos` (cada minuto) y `resumenResenas` (diaria), que son programadas; y `revisarLocal` (HU24), que se dispara sola cuando cambia el `estado` de un local. El detalle de las de pedidos y reservas está en «Pedidos y reservas», más abajo.

**Los ítems del pedido (acuerdo del 8 oct, para HU07, HU08 y HU13).** Cada ítem de `pedidos/{id}` guarda `tipo` (`plato` o `menu`), `cantidad`, y el `nombre` y el `precio` copiados al momento de pedir. Un plato de la carta guarda además su `platoId`; el menú del día, la `fecha` del menú y los nombres de la `entrada` y el `segundo` elegidos. El ranking de platos más pedidos de O6 (SCRUM-131, función `rankingPlatos`) cuenta los platos por `platoId` y no por nombre, porque el local puede renombrarlos, y junta todos los ítems `menu` en una sola fila, «Menú del día», como en el diseño. Las opciones del menú no se rankean: son texto libre que cambia cada día, y a veces lo escribe la IA. Por la misma razón, el dashboard del administrador (HU26) no compara platos entre locales; puede agrupar por la `categoria` del local, que es una lista fija.

**Fuera de Firestore:** el carrito del comensal (HU08) y el borrador del menú del día (HU04) van en Room. No se guardan la conversación del chatbot (HU11) ni la foto de la carta o de la pizarra después de leerla (HU04). El recordatorio de una reserva lo programa el celular con WorkManager (HU10).

## Pedidos y reservas (acuerdo del 25 de octubre)

Es el contrato entre HU07, HU09 y HU12 (Jose: funciones y restaurante), HU08 y HU10 (Camila: comensal) y HU14 y HU15 (Rodrigo: reseñas con IA). Lo acordaron Jose y Claude; el resto del equipo se adapta a lo que vaya avanzando. Si alguien necesita cambiar un nombre o un campo, lo cambia aquí primero y avisa.

**Quién escribe qué función** (todas en `functions/`):

- **Jose (HU07, HU09 y HU12):** la base que usan todas (verificar el token, responder errores, enviar avisos), `crearPedido`, `actualizarPedido`, `reservas` (con la validación del cupo), `resenas` (SCRUM-170), `vencimientos`, los datos de ejemplo (SCRUM-168), los avisos en la app del comensal (SCRUM-169), las reglas, los índices y el despliegue. También el campo `cupoPorFranja` en O8 y su regla (SCRUM-165).
- **Rodrigo (HU14 y HU15):** `resumenResenas` (programada) y la función que se dispara con cada reseña nueva, las dos con Gemini, usando la base de Jose para los avisos.
- **Camila (HU08 y HU10):** solo la app del comensal; no escribe funciones.

### Orden del Sprint 2 (hasta el 9 de noviembre)

Para que nadie espere a nadie, lo primero de Jose es lo que usan los otros dos, y cada uno empieza por lo que no necesita servidor.

| | Semana 1 (26 oct – 1 nov) | Semana 2 (2 – 9 nov) |
| --- | --- | --- |
| **Jose** | Días 1 y 2: la base de las funciones, los datos de ejemplo (SCRUM-168), las reglas y los índices. Después: `crearPedido`, `reservas`, los avisos del comensal (SCRUM-169) y la bandeja O1 con los datos de ejemplo (SCRUM-99) | `actualizarPedido` con aceptar, avanzar, rechazar y entregar (SCRUM-163, 100, 152, 164), el cupo en O8 (SCRUM-165), las reservas en O4 (SCRUM-106, 107), el aviso de pedido nuevo (SCRUM-162), `vencimientos` (SCRUM-153, 155), los avisos al comensal (SCRUM-101, 108), `resenas` (SCRUM-170) y el despliegue final |
| **Rodrigo** | Con las reseñas de ejemplo: la clasificación del sentimiento y el motivo con Gemini (SCRUM-121, 122) y el resumen (SCRUM-116) | El resumen en caché (SCRUM-117), el aviso al local por reseña negativa (SCRUM-123) y probarlo con reseñas reales cuando exista `resenas` |
| **Camila** | Lo que no necesita servidor: el carrito con Room (SCRUM-93), el menú (SCRUM-150), modalidad y pago (SCRUM-94), Geocoding (SCRUM-167), la biometría (SCRUM-95), el formulario de reserva (SCRUM-102) y el recordatorio con WorkManager (SCRUM-103) | Conectar con `crearPedido` y `reservas`, el seguimiento (SCRUM-96), el rechazo (SCRUM-151), el menú vencido (SCRUM-156), cancelar (SCRUM-98, 104), modificar (SCRUM-105) y la calificación (SCRUM-97) |

Las únicas esperas que quedan: Camila necesita `crearPedido` y `reservas` al empezar la semana 2, y Rodrigo necesita los datos de ejemplo desde el día 2. Por eso Jose los entrega primero. Mientras tanto, todos prueban con los datos de ejemplo.

Si el sprint se aprieta, estos criterios pueden pasar al Sprint 3 sin perder ningún requisito del curso: modificar la reserva (SCRUM-105), la entrega con código (SCRUM-164), el sonido que se repite (SCRUM-162) y la calificación (SCRUM-97).

### Cómo llaman las apps a las funciones

- Con **Retrofit**, a `https://us-central1-<id-del-proyecto>.cloudfunctions.net/<función>`, con `POST` y cuerpo JSON. La región es `us-central1`, la misma de `revisarLocal`.
- Cada app tiene `data/remote/` con la interfaz de Retrofit (`FuncionesApi`) y un interceptor de OkHttp que agrega `Authorization: Bearer <token>`, con el token de Firebase Auth de la sesión (`getIdToken(false)`). La función lo verifica con `verifyIdToken` y saca de ahí el `uid`: nunca se manda el `uid` en el cuerpo.
- Si sale bien, responde `200` con un JSON. Si no, responde `400`, `403` o `409` con `{ "error": "<codigo>" }`, y `data/` traduce cada código a un error del dominio (como `ErrorAuth`). Los códigos de cada función están abajo. Todas pueden responder además `401` `no_autenticado` (sin token o vencido), `403` `correo_sin_verificar` y `500` `error_interno`.
- En `functions/src/`, la base es `https.ts`: `funcionHttps(...)` arma cada función (solo `POST`, verifica el token y traduce los errores), `ErrorFuncion(codigo, estado)` corta con un error del contrato, y `leerTexto`, `leerEntero` y `leerOpcion` validan el cuerpo (si algo no cuadra, `datos_invalidos`). Los avisos salen con `notificar(uid, aviso)` de `notificar.ts`, que envía a todos los celulares de la cuenta, borra los tokens vencidos y, si se le pide, guarda el aviso en la bandeja: `usuarios/{uid}/notificaciones/{id}` con `{ titulo, cuerpo, tipo, datos, leida, creadoEn }`.

### El pedido (`pedidos/{id}`)

| Campo | Tipo | Notas |
| --- | --- | --- |
| `numero` | entero | Correlativo del local, con `ultimoNumeroPedido` del local en una transacción. Se muestra como «#0132». |
| `comensalUid`, `comensalNombre` | texto | El nombre se copia del perfil al pedir. |
| `restauranteUid`, `restauranteNombre` | texto | El nombre se copia del local al pedir. |
| `modalidad` | texto | `recojo` o `delivery`. |
| `direccion` | mapa | Solo en delivery: `{ texto, latitud, longitud }`. Las coordenadas salen de Geocoding (SCRUM-167). |
| `metodoPago` | texto | `efectivo`, `billetera` o `tarjeta`. |
| `pago` | texto | **Simulado**: `pagado` (billetera o tarjeta, después de la huella), `por_cobrar` (efectivo) o `revertido` (al rechazar o cancelar un pedido `pagado`). |
| `items` | lista | Como en el acuerdo del 8 oct: `{ tipo, cantidad, nombre, precio }`, más `platoId` en un plato, o `fecha`, `entrada` y `segundo` en el menú. |
| `total` | entero | En céntimos. Lo calcula la función. |
| `estado` | texto | `recibido` → `preparando` → `listo` (recojo) o `en_camino` (delivery) → `entregado`; o `rechazado` o `cancelado`. |
| `codigoRecojo` | texto | 4 dígitos, solo en recojo. Lo genera la función. |
| `minutosEstimados`, `listoAprox` | entero, fecha | Al aceptar: 10, 15, 20 o 30, y la hora que ve el comensal. |
| `rechazo` | mapa | `{ motivo, detalle, platoAgotado, automatico }`. `motivo`: `plato_agotado`, `cocina_saturada`, `local_por_cerrar`, `otro` o `sin_respuesta` (el automático). `detalle` es obligatorio con `otro`; `platoAgotado` es el nombre del plato. |
| `creadoEn`, `aceptadoEn`, `entregadoEn`, `actualizadoEn` | fecha | Hora del servidor. |

**`crearPedido`** (el comensal). Recibe `{ restauranteUid, modalidad, direccion?, metodoPago, items }`, donde cada ítem manda solo `{ tipo, cantidad }` más `platoId`, o `entrada` y `segundo`. Devuelve `{ pedidoId, numero }`. **La función vuelve a leer la carta y el menú y calcula los precios y el total: no confía en lo que manda la app.** Errores: `local_no_disponible` (no aprobado o pausado), `local_cerrado` (fuera de horario), `plato_agotado`, `opcion_agotada`, `menu_vencido` (pasó `horaFin` o el menú está `terminado`), `datos_invalidos`.

**`actualizarPedido`**. Recibe `{ pedidoId, accion, ... }` y devuelve `{ estado }`:

| Acción | Quién | Desde | Hacia | Datos |
| --- | --- | --- | --- | --- |
| `cancelar` | comensal | `recibido` | `cancelado` | — |
| `aceptar` | local | `recibido` | `preparando` | `minutos`: 10, 15, 20 o 30 |
| `rechazar` | local | `recibido` | `rechazado` | `motivo`, `detalle?`, `platoAgotado?` |
| `avanzar` | local | `preparando` | `listo` o `en_camino`, según la modalidad | — |
| `entregar` | local | `listo` o `en_camino` | `entregado` | `codigo` (recojo) o `sinCodigo: true` |

Errores: `no_autorizado` (no es su pedido), `estado_invalido` (la acción no vale en el estado actual; por ejemplo, el comensal cancela justo cuando el local acepta), `codigo_incorrecto`, `datos_invalidos`. Con «Plato agotado», la app del local además marca el plato como agotado con el caso de uso de HU03, como en el prototipo.

**Lo que escucha cada app:** el local, `pedidos` con `restauranteUid == uid` y `creadoEn` desde el inicio del día, ordenados por `creadoEn` (O1); el código de O3 se busca entre esos pedidos. El comensal, su pedido en curso por id (B7) y su historial con `comensalUid == uid` (HU20). Las dos consultas necesitan un índice compuesto en `firestore.indexes.json`.

### La reserva (`reservas/{id}`)

| Campo | Tipo | Notas |
| --- | --- | --- |
| `comensalUid`, `comensalNombre`, `restauranteUid`, `restauranteNombre` | texto | Como en el pedido. |
| `fecha`, `hora` | texto | `"AAAA-MM-DD"` y `"HH:mm"` en hora de Lima; la hora siempre en `:00` o `:30`. |
| `personas` | entero | De 1 a 10. |
| `estado` | texto | `pendiente` → `confirmada`; o `rechazada` o `cancelada`. Al modificarla vuelve a `pendiente`. |
| `rechazo` | mapa | `{ motivo, detalle, automatico }`. `motivo`: `sin_mesas`, `local_cerrado`, `grupo_grande`, `otro` o `sin_respuesta` (el automático). |
| `horarioAnterior` | mapa | `{ fecha, hora }`, si se modificó. El local la ve como pendiente con el horario anterior (decisión B3). |
| `creadoEn`, `respondidaEn`, `actualizadoEn` | fecha | Hora del servidor. |

**`reservas`**. Recibe `{ accion, ... }`:

| Acción | Quién | Datos | Devuelve |
| --- | --- | --- | --- |
| `crear` | comensal | `restauranteUid`, `fecha`, `hora`, `personas` | `{ reservaId }` |
| `modificar` | comensal | `reservaId`, `fecha`, `hora` | `{ estado }` |
| `cancelar` | comensal | `reservaId` | `{ estado }` |
| `confirmar` | local | `reservaId` | `{ estado }` |
| `rechazar` | local | `reservaId`, `motivo`, `detalle?` | `{ estado }` |

`crear` y `modificar` validan lo de la decisión A6: entre 2 h y 7 días de anticipación, dentro del horario del local, de 1 a 10 personas y **máximo 2 reservas activas** (`pendiente` o `confirmada`) por comensal. Errores: `fuera_de_plazo`, `fuera_de_horario`, `franja_llena`, `tope_reservas`, `estado_invalido`, `no_autorizado`, `datos_invalidos`.

**El cupo (SCRUM-165).** Cada reserva ocupa **solo su franja de 30 minutos**. El conteo vive en `restaurantes/{uid}/franjas/{AAAA-MM-DD_HHmm}` como `{ ocupados }` (número de personas) y cambia en la misma transacción que la reserva: suma al crear, se mueve al modificar y resta al cancelar o rechazar. Una franja está llena si `ocupados + personas > cupoPorFranja`. Mientras el local no configure `cupoPorFranja` (SCRUM-165), sus franjas no tienen límite. El comensal lee las franjas del día para tachar las llenas en B5.

**El recordatorio (SCRUM-103)** no pasa por el servidor: al crear o modificar la reserva, la app del comensal programa un trabajo de WorkManager para 30 min, 1 h o 2 h antes, y lo cancela si la reserva se cancela o se rechaza.

### Lo que hace el servidor solo

- **`vencimientos`** (cada minuto): un pedido que sigue `recibido` 10 minutos después de `creadoEn` pasa a `rechazado` con `sin_respuesta` y `automatico: true`, y su `pago` a `revertido` si estaba `pagado`. Una reserva `pendiente` a la que le faltan 60 minutos o menos pasa a `rechazada` igual y libera su franja.
- **Avisos push**: cada función que cambia un estado avisa al otro lado, con los tokens de `usuarios/{uid}/dispositivos`, y guarda el aviso en `usuarios/{uid}/notificaciones` (C5). Al local: pedido nuevo (**alta prioridad**, para que suene aunque el celular esté en reposo, SCRUM-162) y reserva nueva o modificada. Al comensal: cada cambio de estado del pedido (fijo, no se puede apagar, decisión C4) y la respuesta a la reserva (solo si tiene activado el aviso de reservas en su perfil).
- **En la app del comensal (SCRUM-169):** guardar su token de avisos en `usuarios/{uid}/dispositivos/{token}` con `app: "comensal"`, como ya lo hace el restaurante (`RegistrarDispositivoUseCase`). Sin eso no le llega ningún aviso.
- **`resenas` (SCRUM-170):** recibe `{ pedidoId, estrellas, comentario, platos }` (`platos`: el «me gustó» o «no me gustó» por `platoId`, opcional), solo de un pedido `entregado` y una sola vez. Guarda `restaurantes/{uid}/resenas/{pedidoId}` y recalcula `calificacion { promedio, total }` del local. Errores: `estado_invalido`, `ya_calificado`, `no_autorizado`, `datos_invalidos`.

### Datos de ejemplo (SCRUM-168)

Para programar O1, O4, B7, el historial o las reseñas sin esperar a las funciones, el script `functions/src/scripts/datosEjemplo.ts` carga en Firestore, para un local y un comensal reales de prueba:

- **12 pedidos:** 2 `recibido` (recojo en efectivo y delivery con billetera), `preparando`, `listo` con código, `en_camino`, 2 `rechazado` (plato agotado y el automático), `cancelado` y 4 `entregado` de días anteriores. Usan los platos y el menú de hoy del local, o nombres de ejemplo si no tiene carta.
- **5 reservas:** 2 `pendiente` (una modificada, con `horarioAnterior`), `confirmada`, `rechazada` por grupo grande y `cancelada`, con su cupo sumado en `franjas`.
- **4 reseñas** de los pedidos entregados (de 1 a 5 estrellas, dos negativas para HU15), y la `calificacion` del local recalculada.

Todo lleva `ejemplo: true`, así que se borra sin tocar datos reales. Cómo usarlo:

1. Una vez: en Firebase → Configuración del proyecto → **Cuentas de servicio** → «Generar nueva clave privada». Guarda el JSON **fuera del repositorio** (por si acaso, `.gitignore` ya ignora `*-firebase-adminsdk-*.json`). Esa clave da acceso total al proyecto: no se comparte ni se sube.
2. En la terminal, dentro de `functions/` (con `npm ci` hecho), apunta a la clave: `$env:GOOGLE_APPLICATION_CREDENTIALS="C:\ruta\a\la\clave.json"` en PowerShell, o `export GOOGLE_APPLICATION_CREDENTIALS=...` en Git Bash.
3. Cargar: `npm run datos-ejemplo -- --local <uid del local> --comensal <uid del comensal>`. Los `uid` se ven en Firebase → Authentication. Se puede correr varias veces: cada vez agrega otro juego.
4. Borrar: `npm run datos-ejemplo -- --local <uid del local> --borrar`.

**La reseña (`restaurantes/{uid}/resenas/{pedidoId}`):** `{ comensalUid, comensalNombre, estrellas, comentario, platos, creadoEn }`, con `platos` como mapa `{ <platoId>: true | false }` («me gustó» o «no me gustó»). HU15 le agrega `sentimiento` y `motivo`, y HU21 la `respuesta` del local.

### Reglas de Firestore e índices (SCRUM-91)

Ya están en `firestore.rules`:

- `pedidos/{id}` y `reservas/{id}`: los leen solo su comensal (`comensalUid`) y su local (`restauranteUid`), con el correo verificado; los pedidos, también el administrador (para HU26). Nadie los escribe desde una app. Por eso cada consulta de las apps tiene que filtrar por `comensalUid` o `restauranteUid` igual al `uid` de la sesión, o Firestore la rechaza.
- `restaurantes/{uid}/franjas/{id}`: la lee cualquier cuenta con sesión; nadie la escribe desde una app.
- `usuarios/{uid}/notificaciones/{id}`: la lee su dueño, que solo puede marcarla como leída (`leida: true`).
- **Pendiente (SCRUM-165):** que el cambio de horario del local acepte `cupoPorFranja`.

Los índices compuestos están en `firestore.indexes.json` (registrado en `firebase.json`): pedidos por local y por comensal ordenados por `creadoEn`, reservas por local ordenadas por `fecha` y `hora`, reservas por comensal por `fecha`, y los de `vencimientos` (pedidos por `estado` y `creadoEn`; reservas por `estado` y `fecha`). Se publican junto con las reglas: `firebase deploy --only firestore`.

## El documento del local (`restaurantes/{uid}`)

Lo crea y lo mantiene la app del restaurante (HU02). Lo leen también la sección de administración (HU24) y la app del comensal (HU05 en adelante), así que estos nombres y valores son el contrato entre las tres partes.

| Campo | Tipo | Lo escribe | Notas |
| --- | --- | --- | --- |
| `nombre`, `direccion` | texto | el local (R3, O7, R7) | Hasta 80 y 200 caracteres. |
| `categoria` | texto | el local | `criolla`, `chifa`, `polleria`, `marina`, `vegetariana` u `otra`. |
| `telefono` | texto | el local | Solo dígitos: celular de 9 que empieza con 9, fijo de Lima de 7, o fijo con código de ciudad (`01…`, `044…`). |
| `ubicacion` | geopoint | el local | El punto elegido en el mapa. Empieza en la Ciudad Universitaria. |
| `portadaUrl`, `logoUrl` | texto | el local | URL de descarga de Storage. La portada es obligatoria y el logo opcional: sin logo se muestra la inicial del nombre. |
| `horario` | mapa | el local (R4, O8) | Claves `lun` a `dom`, cada una con `{ abierto, abre: "HH:mm", cierra: "HH:mm" }`. Un día cerrado conserva sus horas. |
| `estado` | texto | el local: `borrador` → `pendiente` (R4) y `rechazado` → `pendiente` (R7). El administrador: `aprobado` o `rechazado` (HU24) | Un `borrador` todavía no es una solicitud: el administrador no lo ve. |
| `rechazo` | mapa | el administrador (HU24) | `{ motivo, detalle }`, con `motivo` igual a `datos_incompletos`, `direccion_no_verificable`, `local_duplicado` u `otro`. El detalle es opcional, salvo con `otro`. |
| `rechazoAnterior`, `reenviado`, `camposCorregidos` | mapa, booleano, lista | el local al reenviar (R7) | Para A1 y A2: el reenvío marcado, el motivo anterior (copia exacta de `rechazo`) y los campos que cambiaron, con los mismos nombres de esta tabla. |
| `pausado` | booleano | el local (cabecera de O1) | Sin el campo, el local recibe pedidos. Si es `true`, el comensal lo ve «Cerrado por ahora». Rechazar en el servidor los pedidos a un local pausado es trabajo de quien cree los pedidos (HU07, HU08). |
| `creadoEn`, `enviadoEn`, `actualizadoEn` | fecha | el local | Siempre la hora del servidor. `enviadoEn` es el último envío a revisión. |
| `menuHoy` | mapa | el local (HU03) | `{ fecha, precio, horaFin, estado }`: copia del menú del día, que se escribe junto con él. Los pines del mapa del comensal muestran ese precio, o «Carta» si no hay menú hoy. |
| `rangoCarta` | mapa | el local (HU03) | `{ min, max }` en céntimos: el plato más barato y el más caro. Se recalcula al guardar o eliminar un plato. Es el segundo filtro de precio del comensal; el primero es `menuHoy.precio` (HU06). |
| `cupoPorFranja` | entero | el local (R4, O8) | Comensales por franja de 30 minutos (SCRUM-165). |
| `calificacion`, `resumenResenas`, `ultimoNumeroPedido` | mapa, mapa, entero | las funciones | `{ promedio, total }` de las reseñas; `{ texto, generadoEn }`, el resumen de la IA (SCRUM-117); el último número de pedido del local («#0132»). |
| `revisadoEn` | fecha | el administrador (HU24) | Cuándo se aprobó o rechazó. |

Qué garantizan las reglas de Firestore:
- El dueño lee su local en cualquier estado; cualquier otro, solo si `estado == 'aprobado'`. Por eso las consultas del comensal tienen que filtrar con `where("estado", "==", "aprobado")`, o Firestore las rechaza.
- El dueño solo puede crear su local en `borrador`. Después, cada cambio tiene que ser uno de estos seis:
  - cambiar los datos y las fotos;
  - enviar a revisión (de `borrador` a `pendiente`, con el horario);
  - cambiar el horario;
  - pausar o reanudar los pedidos (solo si está `aprobado`);
  - reenviar después de un rechazo (de `rechazado` a `pendiente`);
  - cambiar la carta (HU03): tocar solo `menuHoy` (`{ fecha, precio, horaFin, estado }`), `rangoCarta` (`{ min, max }`) y `actualizadoEn`, en cualquier estado del local (uno pendiente también puede preparar su carta).
- Ninguna de esas escrituras le permite al local aprobarse solo ni inventar el motivo del rechazo.
- **Pendiente (Rodrigo):** que el cambio de horario acepte `cupoPorFranja`.
- Las reglas del administrador (leer todas las solicitudes, aprobar y rechazar) están pendientes de HU24.

En Storage, las fotos van en `restaurantes/{uid}/portada-<hora>.jpg` y `logo-<hora>.jpg`. Solo las sube el dueño, si su cuenta es de rol `restaurante`: la regla consulta `usuarios/{uid}` en Firestore, para lo cual se le dio permiso a Storage al publicarla. Solo se aceptan JPEG de menos de 2 MB. Los demás ven las fotos con la URL de descarga guardada en el documento, que no pasa por estas reglas.

## La carta y el menú del día (HU03)

Los escribe la app del restaurante (HU03) y los lee también la del comensal (HU06 en adelante), así que estos nombres son el contrato entre las dos. Viven dentro del local, en `restaurantes/{uid}/categorias`, `platos` y `menus`.

**`categorias/{id}`** (id automático)

| Campo | Tipo | Notas |
| --- | --- | --- |
| `nombre` | texto | De 1 a 40 caracteres. La app no deja crear dos con el mismo nombre, sin importar mayúsculas. |
| `orden` | entero | Posición en la carta. La categoría nueva va al final. |

**`platos/{id}`** (id automático)

| Campo | Tipo | Notas |
| --- | --- | --- |
| `nombre` | texto | De 1 a 80 caracteres. |
| `descripcion` | texto | Opcional, hasta 300 caracteres. |
| `precio` | entero | En céntimos, de 1 a 100000 (de S/ 0.01 a S/ 1000.00). |
| `categoriaId` | texto | El id de una categoría del mismo local. |
| `fotoUrl` | texto | Opcional. URL de descarga de la foto en Storage. |
| `agotadoEl` | texto | Opcional, `"AAAA-MM-DD"` en hora de Lima: el día en que se marcó agotado. El plato está agotado solo si esa fecha es hoy, así que al día siguiente vuelve solo a estar disponible. Si el local lo vuelve a marcar disponible el mismo día, el campo se quita. |
| `creadoEn`, `actualizadoEn` | fecha | Hora del servidor. |

**`menus/{AAAA-MM-DD}`** (el id es la fecha: un menú por día)

| Campo | Tipo | Notas |
| --- | --- | --- |
| `precio` | entero | En céntimos. |
| `entradas`, `segundos` | listas | Las opciones del día, cada una `{ nombre, agotado }`. El comensal elige una de cada lista. `agotado` empieza en `false` y lo cambia SCRUM-70. |
| `refresco`, `postre` | texto | Opcionales: lo que incluye el menú («Chicha morada», «Mazamorra»). |
| `horaFin` | texto | `"HH:mm"`, hasta qué hora se sirve. Por defecto `"15:00"`. |
| `estado` | texto | `publicado` o `terminado` («Terminar menú de hoy» y «Reabrir»). |
| `origen` | texto | `ayer`, `cero` o `ia`: cómo se armó. |
| `publicadoEn`, `actualizadoEn` | fecha | Hora del servidor. |

Qué garantizan las reglas de Firestore:
- Leer la carta y los menús: el dueño siempre; cualquier otro, solo si el local está `aprobado`. Eso incluye al comensal invitado sin cuenta.
- Escribir: solo el dueño con la cuenta verificada, de rol `restaurante` y con su local ya creado.
- `categorias` acepta solo `nombre` y `orden`.
- Los menús no se borran, porque «copiar el de ayer» los necesita.
- `menuHoy` y `rangoCarta` del local son copias que la app actualiza junto con el menú y los platos (ver «El documento del local»).

En Storage, las fotos de los platos van en `restaurantes/{uid}/platos/<platoId>-<hora>.jpg`. Solo las sube el dueño, de rol `restaurante`; se aceptan JPEG de menos de 2 MB y el dueño puede borrarlas (al cambiar la foto o eliminar el plato). La app las achica a 1024 px y las sube recién al guardar el plato, así que un alta abandonada no deja fotos sueltas. El id del plato se genera antes de subir la foto, porque va en su nombre.

El menú del día se publica en una transacción, que falla sin conexión en vez de quedar en espera. La transacción guarda `menus/{fecha}` y su copia `menuHoy` en el local, juntas, y no pisa un menú que ya exista para esa fecha. La fecha es la de hoy en hora de Lima. La app del comensal tiene que comparar `menuHoy.fecha` con su propia fecha de hoy: el campo no se borra solo al cambiar el día.

`rangoCarta` se recalcula después de crear, editar o eliminar un plato, leyendo los precios del servidor; si la carta queda sin platos, el campo se quita (la regla `cambiaCarta()` lo permite). Si ese paso falla, el plato igual queda guardado y el rango se corrige en el siguiente cambio. Lo mismo pasa al borrar la foto vieja de un plato: si falla, queda suelta en Storage, sin afectar la carta.

### El borrador del menú del día (Room, HU04)

Mientras el local arma el menú en M5, cada cambio se guarda en la base `sanmarkfood.db` del celular. Si cierra la app, o sale con la X, al volver a M5 por el mismo botón («Empezar de cero» o «Copiar el de ayer») encuentra el menú como lo dejó. La lectura de la pizarra (SCRUM-72) escribe en este mismo borrador, con origen `IA`, y M2 se lo pasa a M5. M1 observa en vivo el origen del borrador (`BorradorMenuDao.observarOrigen`), y si es `IA` vuelve a M3 en vez de abrir la cámara. Al publicar, `PublicarMenuDelDiaUseCase` lo borra. Hay un solo borrador, y solo vale el de hoy. Editar un menú ya publicado no usa borrador.

| Tabla | Columnas | Notas |
| --- | --- | --- |
| `borrador_menu` | `fecha` (clave), `origen`, `precio`, `refresco`, `postre`, `horaFin`, `precioPorRevisar` | Una sola fila. El precio se guarda como lo escribió el local («14.»), porque todavía puede estar a medias. `precioPorRevisar` es el precio dudoso de la IA. |
| `opcion_borrador` | `id`, `fecha`, `tipo`, `nombre`, `porRevisar` | Una fila por entrada o segundo, en el orden en que se agregaron. `fecha` es clave foránea con borrado en cascada: al borrar el borrador, SQLite borra sus opciones. `porRevisar` es el plato dudoso de la IA. |

`origen` y `tipo` se guardan con el nombre del `enum` (`CERO`, `ENTRADA`): esta base es solo del celular, no es un contrato con otra app. Guardar reemplaza todo en una transacción (`BorradorMenuDao.reemplazar`). La base va en la versión 2 (la 1 no tenía las columnas `porRevisar`). Al cambiar de versión se borra el borrador en vez de migrarlo (`fallbackToDestructiveMigration`): es temporal y no hace falta conservarlo.

## Convenciones de código

Para que el código de los tres integrantes se lea como si lo hubiera escrito una sola persona:

- **Paquetes:** minúsculas, sin guiones ni tildes (`gestion_restaurante`, no `gestión-restaurante`).
- **Carpetas por proceso (acuerdo del 3 oct):** `domain/model/`, `domain/usecase/` y `presentation/` tienen una carpeta por proceso, con el mismo nombre en las tres. En el restaurante son `auth`, `gestion_restaurante`, `menu`, `pedidos`, `reservas`, `resenas`, `dashboard` y `admin`; en el comensal, `auth`, `descubrimiento`, `pedidos`, `reservas`, `resenas`, `historial`, `notificaciones` y `perfil`. Cada HU nueva entra en la carpeta de su proceso, así que no hace falta reorganizar cuando la app crece. `data/` y `domain/repository/` quedan planas. La excepción es `presentation/panel/`: el contenedor con la barra inferior, que muestra las pantallas de cada proceso.
- **Una pantalla = tres archivos en el mismo paquete:** `MenuDelDiaScreen.kt`, `MenuDelDiaViewModel.kt` y el `data class MenuDelDiaUiState` (puede vivir en el archivo del ViewModel).
- **Estado:** un solo `StateFlow<XxxUiState>` por pantalla. Los eventos de la UI son funciones del ViewModel (`onAgregarPlato()`), no `LiveData` ni callbacks sueltos.
- **Casos de uso:** verbo + sustantivo + `UseCase` (`PublicarMenuDelDiaUseCase`), con una sola función pública (`operator fun invoke(...)`).
- **Repositorios:** interfaz `MenuRepository` en `domain/repository/`, implementación `MenuRepositoryImpl` en `data/repository/`.
- **Tres representaciones de un dato, nunca mezcladas:** `MenuDto` (red), `MenuEntity` (Room), `Menu` (dominio). El mapeo vive en `data/`; un DTO nunca llega a `presentation/`.
- **Errores:** los casos de uso lanzan errores del dominio, una `sealed class` en `domain/model/<proceso>/` (por ejemplo `ErrorAuth`, en `auth/`). `data/` traduce las excepciones de Firebase a esos errores (`data/firebase/ErroresFirebase.kt`) y `presentation/` los convierte en textos de `strings.xml`. Ninguna excepción de Firebase llega a un ViewModel.
- **Navegación desde una pantalla:** la pantalla recibe lambdas (`onVolver`, `onSesionIniciada`) y no conoce las rutas; `core/navigation/NavGraph.kt` decide a dónde ir.
- **Una pantalla, varios momentos:** si el diseño reutiliza una pantalla con un «modo» (alta, editar, corregir), se programa una sola, con el modo como argumento de la ruta (`DatosLocal(ModoFormulario.EDITAR)`), no copias. Así lo hacen R3/O7/R7 y R4/O8.
- **Componentes compartidos:** lo que usan varias pantallas de un proceso va en un archivo del paquete, como `ComponentesAuth.kt` o `ComponentesGestion.kt`, no copiado en cada pantalla.
- **Pruebas:** las reglas de los casos de uso se prueban en `app/src/test/` con un repositorio falso escrito en la misma prueba. No hace falta emulador ni Firebase.
- **Colores y tipografía solo desde el tema:** `MaterialTheme.colorScheme.primary`, `MaterialTheme.typography.titleLarge`. Ningún `Color(0xFF...)` ni `fontSize` suelto dentro de una pantalla — si falta un color, se agrega al esquema, no a la pantalla.
- **Textos:** todos en español (Perú), moneda `S/`. Nada de strings escritos dentro de un Composable → `res/values/strings.xml`.
- **Nombres en español, del dominio** (`Comensal`, `Reserva`, `MenuDelDia`); en inglés solo lo que impone el framework (`ViewModel`, `UseCase`, `Repository`, `Screen`).

## Cómo se agrega una pantalla nueva

1. Tarjeta en Jira (`SCRUM-XX`) y rama `feature/SCRUM-XX-...` — ver [`CONTRIBUTING.md`](../CONTRIBUTING.md).
2. Abrir la pantalla en el prototipo del equipo. **Ahí están los estados obligatorios** (vacío, cargando, sin conexión, error, invitado) que ya se diseñaron: implementarlos todos, no solo el caso feliz.
3. `domain/`: el modelo en `model/<proceso>/`, la interfaz del repositorio en `repository/` y el caso de uso en `usecase/<proceso>/`.
4. `data/`: implementación del repositorio (Firestore / Retrofit / Room) y sus mappers.
5. `presentation/<proceso>/`: `UiState`, `ViewModel`, `Screen`.
6. Registrar la ruta en `core/navigation/Rutas.kt` y `NavGraph.kt`, y el binding del repositorio en `core/di/`.

Los criterios de aceptación de la HU son subtareas en Jira: la pantalla está lista cuando todas pasan, no cuando compila.

## Mapeo de requisitos del curso → dónde viven en el código

| Requisito del curso | Dónde va |
| --- | --- |
| Autenticación | `data/firebase/` + `presentation/auth/` en cada proyecto — **hecha en las dos apps** (HU01 y HU05) |
| Procesos de negocio (mín. 3, el proyecto cubre 6) | Una carpeta por proceso en `presentation/`, `domain/model/` y `domain/usecase/` de la app que corresponde |
| Firebase | `data/firebase/` en ambas apps (mismo proyecto Firebase, dos apps Android registradas) — en `app-restaurante` ya se usan Auth, Firestore y Storage; Cloud Functions (`functions/`, HU07) y Cloud Messaging para los avisos |
| Material Design | `ui/theme/` + `res/font/` de cada app — **ya hecho**, con paleta propia y modo oscuro |
| MVVM + Clean Code | Estructura de 3 capas repetida en `app-comensal` y `app-restaurante` — en uso en las dos apps |
| Corrutinas + Retrofit | `data/remote/` + `domain/usecase/` (`suspend fun`) — llaman a las Cloud Functions de pedidos y reservas (HU07, HU08, HU09, HU10) y a Geocoding API (HU08, SCRUM-167) |
| WorkManager | `app-comensal/workers/`: el recordatorio de la reserva (HU10, SCRUM-103) |
| SQLite | `data/local/` (Room) — **en `app-restaurante`, el borrador del menú del día** (HU04); en `app-comensal`, el carrito (HU08) |
| Dashboards | `app-restaurante/presentation/dashboard/` y `admin/dashboard_global/` (Vico o MPAndroidChart), con consultas de agregación de Firestore |
| Recursos del móvil (mín. 3, el proyecto cubre 4) | Cámara → `app-restaurante` (pizarra y carta, HU04) — **la pizarra ya usa CameraX**; GPS, biometría, micrófono → `app-comensal` |
| Funcionalidades de IA (mín. 3, el proyecto cubre 4) | `ai/` en ambas apps — **en `app-restaurante`, la lectura de la pizarra con Gemini** (HU04); el chatbot (HU11, Gemini en `app-comensal`); el resumen y el sentimiento de las reseñas (HU14 y HU15, Gemini en las funciones) |
| Despliegue en Play Store | No es código — checklist aparte en Sprint 3 (firma de cada AAB, ficha de Play Console por app) |

## Documentos relacionados

| Qué | Dónde |
| --- | --- |
| Diseño de las dos apps: prototipos navegables, decisiones de producto, paleta y equivalencia con los roles de Material 3 | Material interno del equipo, fuera del repositorio — pedírselo a Jose |
| Historias de usuario y criterios de aceptación | Jira `jlchuque.atlassian.net`, proyecto **SCRUM** (cada criterio es una subtarea) + `docs/SanMarkFood-Planificacion.xlsx` |
| Ramas, formato de commits y Pull Requests | [`CONTRIBUTING.md`](../CONTRIBUTING.md) |