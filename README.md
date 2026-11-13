# 🩺 KinApp: rehabilitación kinésica guiada por visión artificial

> Aplicación Android nativa (Java) que acompaña al paciente en rutinas de rehabilitación en casa: detecta su postura con la cámara, cuenta las repeticiones y corrige la técnica por voz.

![Plataforma](https://img.shields.io/badge/Plataforma-Android-3DDC84?logo=android&logoColor=white)
![Lenguaje](https://img.shields.io/badge/Lenguaje-Java-ED8B00?logo=openjdk&logoColor=white)
![Base de datos](https://img.shields.io/badge/BD-SQLite-003B57?logo=sqlite&logoColor=white)
![Arquitectura](https://img.shields.io/badge/Arquitectura-MVC-blue)
![Visión](https://img.shields.io/badge/Visi%C3%B3n-ML%20Kit%20Pose-orange)
![API mínima](https://img.shields.io/badge/API%20m%C3%ADnima-29-brightgreen)

---

## Tabla de contenidos

1. [Resumen y alcance](#1-resumen-y-alcance)
2. [Arquitectura MVC](#2-arquitectura-mvc)
3. [Visión por computador y sensores](#3-visión-por-computador-y-sensores)
4. [Concurrencia](#4-concurrencia)
5. [Seguridad](#5-seguridad)
6. [Base de datos](#6-base-de-datos)
7. [Manual de usuario](#7-manual-de-usuario)
8. [Documentación visual](#8-documentación-visual)
9. [Especificaciones técnicas](#9-especificaciones-técnicas)
10. [Instalación](#10-instalación)
11. [Control de versiones](#11-control-de-versiones)

---

## 1. Resumen y alcance

**KinApp** es un "kinesiólogo digital" pensado para mejorar la adherencia a terapias de rehabilitación física realizadas en el hogar. El paciente ejecuta la rutina frente a la cámara del teléfono y la aplicación analiza su movimiento en tiempo real, sin enviar imágenes a ningún servidor.

### Funcionalidades implementadas en este avance

- **Registro e inicio de sesión** con persistencia local en SQLite y contraseñas almacenadas con hash y sal.
- **Geolocalización por hardware** (GPS / red + `Geocoder`) para obtener la ciudad del usuario al registrarse.
- **Calibración con acelerómetro**: verifica que el teléfono esté en posición vertical antes de comenzar.
- **Rutina guiada de sentadillas**: CameraX + Google ML Kit Pose Detection, conteo automático de 12 repeticiones, esqueleto 2D superpuesto y feedback por voz (TextToSpeech).
- **Resumen de la rutina con métricas reales**: tiempo, repeticiones y correcciones de profundidad emitidas durante la sesión.
- **Historial y progreso**: sesiones totales, días con actividad (racha) y adherencia mensual.
- **Perfil**: edición de teléfono, cambio de contraseña, notificación local de recordatorio (prueba) y cierre de sesión.

### Alcance y próximas etapas

Quedan fuera de este avance y se abordarán en etapas posteriores: diferenciación funcional por rol (el selector Paciente / Especialista ya existe en el registro), catálogo de ejercicios, mensajería con el especialista, supervisión del especialista, recordatorios programados, ejercicios adicionales a las sentadillas y sincronización remota de datos.

---

## 2. Arquitectura MVC

```text
[ Vista (Activities / XML / PoseOverlayView) ]
               │  eventos del usuario / callbacks
               ▼
[ Controlador (UsuarioController, ProgresoController, SentadillaAnalizador, AppExecutors) ]
               │  consultas parametrizadas
               ▼
[ Modelo (Usuario, Rutina, Contracts) + DatabaseHelper ]  →  SQLite
```

| Capa | Paquete | Responsabilidad |
|---|---|---|
| Modelo | `modelo/` | Entidades de dominio (`Usuario`, `Rutina`) y clases contrato (`UsuarioContract`, `RutinaContract`) con nombres de tablas y columnas. |
| Vista | `vista/` | Activities, layouts XML y `PoseOverlayView`. Capturan eventos y muestran estados; no contienen lógica de negocio. |
| Controlador | `controlador/` | Lógica de negocio y acceso a datos: `UsuarioController`, `ProgresoController`, `DatabaseHelper`, `SentadillaAnalizador`, `SeguridadUtil`, `AppExecutors` y `RutinaAdapter`. |

---

## 3. Visión por computador y sensores

### Rutina guiada (`CalibracionSensorActivity` → `CamaraGuiadaActivity`)

1. **Calibración**: el acelerómetro mide la gravedad en el eje Y; cuando el teléfono está vertical (≈ 9,8 m/s²) se habilita el inicio. Los listeners se registran en `onResume` y se liberan en `onPause`.
2. **Cámara**: CameraX (`Preview` + `ImageAnalysis`) con la cámara frontal. Los frames se analizan en un hilo dedicado y se descartan después del análisis; nunca se guardan.
3. **Detección**: `SentadillaAnalizador` usa ML Kit Pose Detection (modelo preciso, `STREAM_MODE`) y ubica cadera, rodilla y tobillo.
4. **Ángulo de rodilla**: se calcula con `atan2` en el plano 2D. Solo se consideran detecciones con confianza ≥ 0,6.
5. **Repetición válida**: se registra al pasar de ángulo < 110° (abajo) a > 150° (de pie). La meta son 12 repeticiones.
6. **Feedback**: si el usuario permanece en la zona intermedia más de 1,5 s, se emite "Baja un poco más la cadera" por voz (máximo un aviso cada 4 s). Cada aviso cuenta como una corrección en el resumen.
7. **Resumen**: al completar la meta se muestran el tiempo, las repeticiones y la precisión, calculada como `100 % − 10 % por corrección` (mínimo 0 %).

> Limitación conocida: el análisis usa la pierna izquierda, por lo que el usuario debe ubicarse de lado con su costado izquierdo visible para la cámara.

### Sensores y hardware utilizados

| Hardware | Uso | Clase |
|---|---|---|
| Cámara | Análisis de postura en tiempo real | `CamaraGuiadaActivity` |
| Acelerómetro | Calibración de orientación del teléfono | `CalibracionSensorActivity` |
| GPS / red | Ciudad del usuario al registrarse | `RegistroActivity` |

---

## 4. Concurrencia

`AppExecutors` (patrón **Singleton**) evita bloquear el hilo principal:

| Ejecutor | Descripción |
|---|---|
| `diskIO()` | Pool de hilos para consultas y escrituras SQLite y para el cálculo del hash de contraseñas. |
| `mainThread()` | Entrega los resultados al hilo de interfaz (`Looper.getMainLooper()`) mediante callbacks (`OnResultCallback`, `OnIntCallback`). |

---

## 5. Seguridad

| Medida | Implementación |
|---|---|
| **Consultas parametrizadas** | Todas las operaciones SQLite usan `selectionArgs`, `?` y `ContentValues`; los nombres de tablas y columnas provienen de las clases contrato. |
| **Contraseñas con hash** | `SeguridadUtil` aplica PBKDF2WithHmacSHA256 (120.000 iteraciones) con una sal aleatoria de 16 bytes por usuario. En la base solo se guarda `sal:hash` en Base64. |
| **`FLAG_SECURE`** | Activo en `LoginActivity`, `RegistroActivity`, `PerfilActivity`, `CamaraGuiadaActivity` y `ResumenRutinaActivity`: bloquea capturas y grabaciones de pantalla. |
| **Componentes privados** | Todas las Activities tienen `android:exported="false"`, excepto `LoginActivity` (launcher). |
| **Respaldo desactivado** | `android:allowBackup="false"`: la base de datos no se incluye en copias de seguridad. |
| **Permisos mínimos** | Solo cámara, ubicación (fina y aproximada) y notificaciones, todos usados por la app. |
| **Privacidad de imágenes** | Los frames de la cámara se procesan en memoria y se descartan. Como la app no guarda ni comparte fotos ni archivos, no requiere `FileProvider`. |
| **Sesión** | Solo se guarda el correo en `SharedPreferences` privadas (`MODE_PRIVATE`), nunca la contraseña. Se elimina al cerrar sesión. |

### Limitaciones conocidas y mejoras previstas

- La base de datos local no está cifrada y la sesión se restaura solo con el correo (sin token).
- Validaciones de formulario más estrictas (formato de correo, fortaleza de contraseña).
- Verificar la contraseña actual antes de permitir su cambio.

---

## 6. Base de datos

SQLite local (`KinApp.db`) administrada por `DatabaseHelper` y las clases contrato.

**`usuarios`**

| Columna | Tipo | Notas |
|---|---|---|
| `_id` | INTEGER | Clave primaria |
| `correo` | TEXT | UNIQUE |
| `contrasena` | TEXT | Hash PBKDF2 con sal (`sal:hash`) |
| `nombre` | TEXT | |
| `fechaNac` | TEXT | Fecha de nacimiento |
| `telefono` | TEXT | Opcional |
| `ubicacion` | TEXT | Ciudad obtenida con GPS / `Geocoder` |

**`historial_rutinas`**

| Columna | Tipo | Notas |
|---|---|---|
| `_id` | INTEGER | Clave primaria, AUTOINCREMENT |
| `correo` | TEXT | Relación lógica con `usuarios.correo` |
| `fecha` | TEXT | Formato `YYYY-MM-DD` |
| `tipo` | TEXT | Ej.: `Sentadillas` |

Un usuario registra muchas rutinas (relación 1 a N). El diagrama entidad-relación está en [`assets/`](#8-documentación-visual).

---

## 7. Manual de usuario

### Autenticación

- **Inicio de sesión** (`LoginActivity`): si existe una sesión guardada, entra directo al panel; si no, se ingresan correo y contraseña.
- **Registro** (`RegistroActivity`): nombre, fecha de nacimiento (calendario nativo), correo, contraseña y ubicación opcional con el botón de GPS.

| Inicio de sesión | Registro |
|---|---|
| ![Inicio de sesión](assets/capturas/login.png) | ![Registro](assets/capturas/registro.png) |

### Rutina de sentadillas

1. En el **Panel** (`PanelActivity`), pulsar el inicio de la rutina.
2. **Calibración**: sostener el teléfono vertical hasta que se confirme.
3. **Cámara guiada**: tras la cuenta regresiva, hacer 12 sentadillas de lado a la cámara. La app cuenta y corrige por voz.
4. **Resumen** (`ResumenRutinaActivity`): revisar tiempo, repeticiones y precisión, y pulsar **Finalizar y guardar**.

| Panel | Calibración | Cámara guiada | Resumen                                  |
|---|---|---|------------------------------------------|
| ![Panel](assets/capturas/panel.png) | ![Calibración](assets/capturas/calibracion.png) | ![Cámara](assets/capturas/camara.png) | ![Resumen](assets/capturas/resumen.jpeg) |

### Progreso y perfil

- **Progreso** (`ProgresoActivity`): sesiones totales, racha (días distintos con al menos una rutina) y adherencia mensual (días con rutina respecto a los días del mes, con tope del 100 %).
- **Perfil** (`PerfilActivity`): actualizar teléfono, cambiar contraseña, activar o silenciar notificaciones y cerrar sesión.

| Progreso                                   | Perfil |
|--------------------------------------------|---|
| ![Progreso](assets/capturas/progreso.jpeg) | ![Perfil](assets/capturas/perfil.png) |

---

## 8. Documentación visual

| Diagrama | Archivo                                                                             |
|---|-------------------------------------------------------------------------------------|
| Arquitectura MVC | `assets/Diagrama de Arquitectura/Diagrama_de_Arquitectura_MVC.jpg`                  |
| Casos de uso (Paciente y Especialista) | `assets/Diagrama de caso de uso UML/UML_general_Paciente_y_Especialista.jpg`        |
| Flujo de registro y geolocalización | `assets/Diagrama de Flujo_Actividad/Diagrama_de_Flujo_Geolocalizacion_registro.jpg` |
| Flujo de navegación y progreso | `assets/Diagrama de Flujo_Actividad/Diagrama_de_Flujo_navegacion_y_progreso.jpg`    |
| Flujo de rutinas | `assets/Diagrama de Flujo_Actividad/Diagrama_de_Flujo_Rutinas.jpg`                  |
| Modelo entidad-relación | `assets/MER/MER_V0.jpg`                                                             |

![Arquitectura MVC](assets/Diagrama_de_Arquitectura_MVC.jpg)

![Modelo entidad-relación](assets/MER_V0.jpg)

![Flujo de rutinas](assets/Diagrama_de_Flujo_Rutinas.jpg)

La documentación JavaDoc (`@param`, `@return`, `@throws`) está integrada en el código fuente de las clases de modelo, controlador y vista.

---

## 9. Especificaciones técnicas

| Componente | Detalle |
|---|---|
| Lenguaje | Java 11 |
| IDE / build | Android Studio, Gradle (Kotlin DSL) |
| SDK | `minSdk` 29 · `targetSdk` y `compileSdk` 37 |
| Persistencia | SQLite nativo con clases contrato |
| Cámara | CameraX 1.3.0 (`core`, `camera2`, `lifecycle`, `view`) |
| Visión artificial | Google ML Kit Pose Detection 17.0.0 (modelo `accurate`) |
| Interfaz | Material Components, ConstraintLayout, `BottomNavigationView` |
| Control de versiones | Git y GitHub |

---

## 10. Instalación

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/Yeremyyy/Aplicacion-de-Rehabilitaci-n-KINAPP.git
   ```
2. Abrir **Android Studio** y elegir **Open** sobre la carpeta del proyecto.
3. Ejecutar **Sync Project with Gradle Files**.
4. Ejecutar la app en un emulador con **API 29 o superior** (con cámara virtual habilitada) o en un dispositivo físico con depuración USB. Para probar la rutina completa se recomienda un dispositivo físico.

---

## 11. Control de versiones

El proyecto se desarrolló de forma incremental en GitHub, con más de 70 commits siguiendo convenciones semánticas (`feat`, `fix`, `docs`, `refactor`), que reflejan las etapas de configuración, interfaz, base de datos, seguridad, sensores y documentación.
