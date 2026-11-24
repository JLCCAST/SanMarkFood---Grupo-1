# Google Play: ficha y formularios (HU18, SCRUM-145)

Lo que se llena en Play Console para las dos apps, en «Panel de control → Termina de configurar tu aplicación». Los formularios se llenan **una vez por app**. Cuando cambie lo que la app recoge o muestra, hay que actualizar esto y el formulario.

## Datos comunes

| Campo | Valor |
| --- | --- |
| Correo de contacto | sanmarkfood.app@gmail.com |
| Sitio web | https://sanmarket-food.web.app |
| Política de privacidad | https://sanmarket-food.web.app/privacidad |
| Eliminar la cuenta (URL) | https://sanmarket-food.web.app/eliminar-cuenta |

## San Mark (comensal) · `com.equipo.sanmarkfood.comensal`

**Nombre (30):** San Mark: menús y reservas

**Descripción breve (80):** Menús del día, pedidos y reservas en los locales cerca de San Marcos.

**Descripción completa:**

```
San Mark te ayuda a decidir dónde comer cerca de la Ciudad Universitaria de San Marcos.

• Encuentra los locales cercanos en el mapa o en la lista, con su categoría, horario y calificación.
• Mira el menú del día de cada local, con sus entradas y segundos, y la carta completa con precios.
• Pide para recoger o para delivery, sigue tu pedido en tiempo real y recibe un aviso cuando esté listo.
• Reserva una mesa para el día y la hora que quieras, y recibe la confirmación del local.
• Califica tus pedidos y lee el resumen de las reseñas de cada local.
• Guarda tus locales favoritos y tus direcciones frecuentes.

Puedes explorar los locales sin crear una cuenta. Para pedir o reservar, entra con tu correo o con tu cuenta de Google.

San Mark Food es un proyecto académico del curso Desarrollo de Aplicaciones Móviles de la Universidad Nacional Mayor de San Marcos. El pago dentro de la app es una simulación: no se cobra dinero real ni se piden datos de tarjetas.
```

**Categoría:** Comida y bebida.

## SoySMF (restaurante) · `com.equipo.sanmarkfood.restaurante`

**Nombre (30):** SoySMF: para restaurantes

**Descripción breve (80):** Publica tu menú del día y atiende pedidos y reservas de tu restaurante.

**Descripción completa:**

```
SoySMF es la app para los restaurantes que atienden a la comunidad de San Marcos a través de San Mark.

• Registra tu local con sus datos, fotos, ubicación y horario. Un administrador lo revisa antes de publicarlo.
• Arma tu carta con categorías, platos, precios y fotos.
• Publica tu menú del día en segundos: toma una foto de tu pizarra y la inteligencia artificial lee las entradas y los segundos por ti.
• Recibe los pedidos con un aviso que no se te pasa, acéptalos con su tiempo de preparación y entrégalos con un código de recojo.
• Confirma o rechaza las reservas y controla el cupo de cada franja horaria.
• Te avisamos cuando llega una reseña negativa, con el motivo que detecta la inteligencia artificial.
• Mira cómo va tu negocio: pedidos, ventas, reservas, platos más pedidos y la evolución de tu calificación.

Entra con tu correo o con tu cuenta de Google. El teléfono del local se verifica por SMS.

SoySMF es parte de San Mark Food, un proyecto académico del curso Desarrollo de Aplicaciones Móviles de la Universidad Nacional Mayor de San Marcos. Los pagos de los pedidos son simulados.
```

**Categoría:** Empresa.

## Formularios del contenido de la app

### Acceso a la app

Elegir «Toda la funcionalidad o parte de ella está restringida» y agregar estas instrucciones. Mientras Google revise, **no tocar «Borrar datos iniciales»**: estas cuentas son de los datos de ejemplo.

- **San Mark:** «Se puede explorar sin cuenta con "Explorar sin cuenta". Para pedir y reservar: correo `lucia@datos.sanmarkfood.test`, contraseña `SanMark2026!`. El pago es simulado.»
- **SoySMF:** «Local de prueba ya aprobado, con pedidos, reservas y reseñas de ejemplo: correo `criolla@datos.sanmarkfood.test`, contraseña `SanMark2026!`.»

### Anuncios

No contiene anuncios.

### ID de publicidad

**Sí**, la app usa el ID de publicidad. Motivo: **Estadísticas**. Lo agrega Firebase Analytics (permiso `com.google.android.gms.permission.AD_ID` en las dos apps); la app no muestra anuncios.

### Clasificación de contenido

Correo: sanmarkfood.app@gmail.com. Categoría: **Todas las demás apps**. Responder **No** a violencia, sexualidad, lenguaje, sustancias, apuestas y compras digitales. Responder **Sí** a «los usuarios pueden interactuar o intercambiar contenido», por las reseñas públicas. La ubicación no se comparte con otros usuarios.

### Público objetivo

**18 años o más.** No está diseñada para niños.

### Otros

| Formulario | Respuesta |
| --- | --- |
| App de noticias | No |
| Apps de COVID-19 | No |
| Apps gubernamentales | No |
| Funciones financieras | La app no ofrece funciones financieras (el pago es simulado) |
| Salud | No |

## Seguridad de los datos

En las dos apps:

- **¿Recopila o comparte datos de usuario?** Sí.
- **¿Se encriptan en tránsito?** Sí (HTTPS/TLS).
- **Métodos de creación de cuentas:** «Nombre de usuario y contraseña» y «OAuth» (inicio de sesión con Google). La verificación del teléfono por SMS del restaurante no es un método de inicio de sesión.
- **Enlace para pedir la eliminación de la cuenta:** https://sanmarket-food.web.app/eliminar-cuenta. A «¿pueden pedir que se borren sus datos sin eliminar la cuenta?» se responde **No**.
- **Compartidos con terceros:** ninguno. Firebase y Google Maps Platform procesan los datos por cuenta nuestra y, según Play, eso no cuenta como «compartir».

### San Mark (comensal): datos recopilados

| Tipo de dato (Play) | Obligatorio u opcional | Fines |
| --- | --- | --- |
| Información personal → Nombre | Obligatorio | Funcionalidad de la app, Gestión de la cuenta |
| Información personal → Dirección de correo | Obligatorio | Funcionalidad de la app, Gestión de la cuenta |
| Información personal → Número de teléfono | Opcional | Funcionalidad de la app |
| Información personal → Dirección | Opcional | Funcionalidad de la app (delivery y direcciones frecuentes) |
| Información financiera → Historial de compras | Obligatorio | Funcionalidad de la app (pedidos) |
| Fotos y videos → Fotos | Opcional | Funcionalidad de la app (foto de perfil) |
| Actividad en la app → Interacciones con la app | Obligatorio | Estadísticas |
| Actividad en la app → Otro contenido generado por el usuario | Opcional | Funcionalidad de la app (calificaciones y reseñas) |
| Actividad en la app → Otras acciones | Opcional | Funcionalidad de la app (reservas y favoritos) |
| ID del dispositivo u otros ID | Obligatorio | Funcionalidad de la app (avisos), Estadísticas |

**No se recopila:** la ubicación (la distancia a los locales se calcula en el celular y no se envía) ni la huella o el rostro (la verificación la hace Android).

### SoySMF (restaurante): datos recopilados

| Tipo de dato (Play) | Obligatorio u opcional | Fines |
| --- | --- | --- |
| Información personal → Dirección de correo | Obligatorio | Funcionalidad de la app, Gestión de la cuenta |
| Información personal → Número de teléfono | Obligatorio | Funcionalidad de la app, Prevención de fraudes, seguridad y cumplimiento (verificación por SMS) |
| Información personal → Dirección | Obligatorio | Funcionalidad de la app (dirección y ubicación del local) |
| Fotos y videos → Fotos | Obligatorio | Funcionalidad de la app (portada, logo y platos; la foto de la pizarra o la carta se envía a Gemini para leerla y no se guarda) |
| Actividad en la app → Otro contenido generado por el usuario | Obligatorio | Funcionalidad de la app (carta y menú del día) |
| Actividad en la app → Interacciones con la app | Obligatorio | Estadísticas |
| ID del dispositivo u otros ID | Obligatorio | Funcionalidad de la app (avisos), Estadísticas, Prevención de fraudes, seguridad y cumplimiento (App Check) |

**No se recopila:** la ubicación del celular (la ubicación del local se marca a mano en el mapa).

## Recursos gráficos

Están en `docs/google-play/`. El ícono y el gráfico salen de la ilustración del ícono de cada app (`mipmap-xxxhdpi/ic_launcher_foreground.png`), con los colores y las fuentes de la marca.

| Recurso | Medida | San Mark | SoySMF |
| --- | --- | --- | --- |
| Ícono | 512 × 512 PNG | `san-mark-icono-512.png` | `soysmf-icono-512.png` |
| Gráfico de funciones | 1024 × 500 PNG | `san-mark-grafico-1024x500.png` | `soysmf-grafico-1024x500.png` |
| Capturas de teléfono | 2 a 8, entre 320 y 3840 px | Pendiente: se sacan del celular con adb | Pendiente |
