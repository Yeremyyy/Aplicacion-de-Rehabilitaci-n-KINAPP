# 🩺 KinApp: Sistema de Rehabilitación Kinésica y Fisioterapia Móvil

> Documentación técnica oficial y manual operativo

![Plataforma](https://img.shields.io/badge/Plataforma-Android-3DDC84?logo=android&logoColor=white)
![Lenguaje](https://img.shields.io/badge/Lenguaje-Java-ED8B00?logo=openjdk&logoColor=white)
![Base de datos](https://img.shields.io/badge/BD-SQLite-003B57?logo=sqlite&logoColor=white)
![Arquitectura](https://img.shields.io/badge/Arquitectura-MVC-blue)
![API mínima](https://img.shields.io/badge/API%20m%C3%ADnima-24-brightgreen)

---

## 📑 Tabla de contenidos

1. [Resumen ejecutivo y alcance](#1-resumen-ejecutivo-y-alcance-del-proyecto)
2. [Arquitectura y patrón de diseño](#2-arquitectura-del-sistema-y-patrón-de-diseño)
3. [Concurrencia y rendimiento](#3-estrategia-de-concurrencia-y-rendimiento-appexecutors)
4. [Seguridad y protección de datos](#4-estándares-de-seguridad-y-protección-de-datos)
5. [Manual de usuario](#5-manual-de-usuario-y-operación-del-sistema)
6. [Especificaciones técnicas](#6-especificaciones-técnicas-y-entorno-de-desarrollo)
7. [Guía de compilación e instalación](#guía-de-compilación-e-instalación)

---

## 1. Resumen ejecutivo y alcance del proyecto

**KinApp** es una solución de software nativa para dispositivos Android, desarrollada bajo los más altos estándares de la ingeniería de software moderna. Su objetivo principal es optimizar la gestión, el seguimiento y la ejecución de rutinas de rehabilitación kinésica a domicilio, estableciendo un canal de comunicación seguro y estructurado entre pacientes y especialistas en fisioterapia.

El sistema integra de forma nativa:

- 📍 Geolocalización por hardware
- 🔒 Persistencia local segura
- ⚡ Procesamiento concurrente de alto rendimiento

---

## 2. Arquitectura del sistema y patrón de diseño

El sistema implementa estrictamente el patrón arquitectónico **Modelo-Vista-Controlador (MVC)**, garantizando un acoplamiento débil entre la lógica de negocio, la capa de persistencia y la interfaz gráfica de usuario.

```text
[ Capa de Vista (Activities / XML) ]
               │
               ▼ (Invoca métodos asíncronos con callbacks)
[ Capa de Controlador (Lógica de Negocio / AppExecutors) ]
               │
               ▼ (Consultas parametrizadas seguras)
[ Capa de Modelo (Entidades, Contracts, DatabaseHelper) ]
```

### Capa de Modelo (`modelo/`)

- Contiene las entidades de dominio (`Usuario`, `Rutina`, etc.).
- Implementa clases de contrato (`UsuarioContract`, `RutinaContract`) que centralizan los esquemas relacionales, nombres de tablas y constantes de columnas para evitar errores de sintaxis en tiempo de ejecución.
- Administra la conexión y el ciclo de vida de la base de datos relacional mediante `DatabaseHelper`.

### Capa de Vista (`vista/`)

- Compuesta por los componentes visuales de Android (`AppCompatActivity`).
- Se encarga exclusivamente de la captura de eventos del usuario, la representación gráfica de los estados y la invocación de los métodos provistos por los controladores.

### Capa de Controlador (`controlador/`)

- Concentra la lógica de negocio y las operaciones CRUD (`UsuarioController`, `ProgresoController`).
- Gestiona la validación de credenciales, el control de sesiones y el procesamiento de datos sin comprometer el hilo principal de ejecución.

---

## 3. Estrategia de concurrencia y rendimiento (`AppExecutors`)

Para cumplir con los estándares de rendimiento de Android y prevenir excepciones de tipo **ANR** (*Application Not Responding*), KinApp prescinde de operaciones bloqueantes en el hilo principal. Se implementó un componente centralizado denominado `AppExecutors` bajo el patrón **Singleton**:

| Ejecutor | Descripción |
|---|---|
| `diskIO()` | Pool de hilos dedicado exclusivamente a la ejecución en segundo plano de operaciones de lectura, escritura y consultas intensivas sobre la base de datos SQLite. |
| `mainThread()` | Ejecutor sincronizado con el bucle principal del sistema (`Looper.getMainLooper()`) que recibe los resultados procesados en segundo plano mediante interfaces de callback personalizadas (`OnResultCallback`, `OnIntCallback`), actualizando la interfaz de usuario de manera segura y fluida. |

---

## 4. Estándares de seguridad y protección de datos

KinApp incorpora mecanismos de ciberseguridad móvil y protección de la privacidad del paciente:

### 🛡️ Mitigación de inyección SQL

Todas las consultas y modificaciones realizadas en la base de datos SQLite se ejecutan mediante el uso estricto de **consultas parametrizadas** (`selectionArgs` y objetos `ContentValues`), eliminando por completo la concatenación manual de cadenas SQL.

### 📵 Blindaje contra capturas de pantalla (`FLAG_SECURE`)

Las actividades críticas que manejan credenciales o información personal (`LoginActivity`, `RegistroActivity`) incorporan la bandera de seguridad a nivel de ventana:

```java
getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
```

Esto impide que el sistema operativo o aplicaciones de terceros realicen capturas o grabaciones de pantalla de los datos confidenciales del usuario.

### 🔑 Persistencia segura de sesión

Control de tokens de acceso y estados de autenticación gestionados mediante almacenamiento estructurado y cifrado básico en preferencias compartidas (`SharedPreferences`).

---

## 5. Manual de usuario y operación del sistema

### Módulo 1: Autenticación y gestión de sesión

#### Inicio de sesión (`LoginActivity`)

- Al iniciar la aplicación, el sistema verifica automáticamente la existencia de una sesión previa mediante almacenamiento local. Si el registro es válido, redirige de inmediato al panel principal.
- En caso contrario, el usuario debe ingresar su correo electrónico y contraseña registrados.
- **Acción:** pulsar el botón de inicio de sesión. El controlador valida las credenciales de forma asíncrona en segundo plano y devuelve la respuesta a la vista.

> 📸 **Captura de referencia:**
>
> `![Inicio de sesión](docs/img/login.png)`

#### Registro de usuarios (`RegistroActivity`)

- Permite dar de alta a nuevos pacientes o especialistas en el sistema.
- Requiere datos obligatorios como nombre completo, fecha de nacimiento (seleccionada mediante un componente de calendario nativo), correo y contraseña cifrada lógicamente.
- **Geolocalización por hardware:** el usuario puede activar la detección automática de ubicación mediante el botón especializado, el cual consulta los proveedores GPS o de red para obtener las coordenadas y traducirlas a una dirección legible (ciudad y país) a través de `Geocoder`.
- **Acción:** confirmar la aceptación de los términos y condiciones y presionar el botón de registro.

> 📸 **Captura de referencia:**
>
> `![Registro](docs/img/registro.png)`

### Módulo 2: Panel de control y ejecución de rutinas

#### Panel principal (`PanelActivity`)

- Actúa como el nodo central de navegación una vez superada la autenticación, adaptando sus opciones visuales según el rol del usuario (**Paciente** o **Especialista**).

> 📸 **Captura de referencia:**
>
> `![Panel principal](docs/img/panel.png)`

#### Módulo de rutinas y progreso (`ResumenRutinaActivity`)

- Permite consultar el catálogo de ejercicios kinésicos asignados, verificar el estado de avance y registrar métricas de desempeño personalizadas.

> 📸 **Captura de referencia:**
>
> `![Resumen de rutinas](docs/img/resumen-rutinas.png)`

---

## 6. Especificaciones técnicas y entorno de desarrollo

| Componente | Detalle |
|---|---|
| **Lenguaje de programación** | Java (Android SDK) |
| **Entorno de desarrollo (IDE)** | Android Studio |
| **Persistencia** | SQLite nativo estructurado mediante clases de contrato |
| **Control de versiones** | Git y GitHub con trazabilidad de commits semánticos |
| **API mínima** | Android API 24 o superior |

---

## Guía de compilación e instalación

1. **Clonar** el repositorio oficial del proyecto en la estación de trabajo local:
   ```bash
   git clone <https://github.com/Yeremyyy/Aplicacion-de-Rehabilitaci-n-KINAPP.git>
   ```
2. Abrir **Android Studio** y seleccionar la opción **Open** para abrir un proyecto existente.
3. Sincronizar las dependencias seleccionando **Sync Project with Gradle Files**.
4. **Compilar y ejecutar** la aplicación sobre:
    - un dispositivo virtual (emulador Android con **API nivel 24 o superior**), o
    - un dispositivo físico con **depuración USB** activa.