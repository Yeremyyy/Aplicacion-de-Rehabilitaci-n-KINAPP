# KinApp - Asistente Kinesiológico Móvil

KinApp es una aplicación móvil nativa para Android diseñada para asistir a pacientes en sus rutinas de rehabilitación física. Utiliza Inteligencia Artificial y visión computacional para evaluar la postura del usuario en tiempo real, garantizando la correcta ejecución de ejercicios.
## Características Principales

* **Análisis Biomecánico con IA:** Integración de Google ML Kit (Pose Detection) para el rastreo del esqueleto en tiempo real, cálculo de ángulos articulares y validación de repeticiones.
* **Feedback Auditivo y Visual:** Sistema de instrucciones guiadas por voz (TextToSpeech) y renderizado del cuerpo en un lienzo interactivo para corregir la postura del paciente ("Baja un poco más la cadera").
* **Gestión de Progreso y Adherencia:** Base de datos relacional nativa (SQLite) que almacena el historial de sesiones, calcula días de racha consecutivos y mide el porcentaje de adherencia mensual del paciente.
* **Geolocalización Automática:** Uso de hardware GPS (LocationManager) y Reverse Geocoding (Geocoder) para detectar y registrar automáticamente la ciudad de origen del paciente al momento de crear su cuenta.
* **Arquitectura Limpia:** Separación lógica basada en el patrón MVC (Modelo-Vista-Controlador).

## Stack Tecnológico

* **Lenguaje:** Java (Android SDK)
* **Entorno de Desarrollo:** Android Studio
* **Machine Learning:** Google ML Kit (Pose Detection API)
* **Hardware de Cámara:** Android CameraX API
* **Base de Datos:** SQLite (SQLiteOpenHelper)
* **Servicios de Ubicación:** LocationManager & Geocoder API
* **Accesibilidad:** TextToSpeech (TTS)

## Requisitos del Sistema

* Dispositivo Android con versión de SO mínima recomendada (API Nivel 24+).
* Permisos habilitados para uso de Cámara (`CAMERA`).
* Permisos habilitados para uso de GPS (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`).

## Autor

**Jordan Yeremy Araya Mena**  
*Ingeniería en Informática - Universidad Santo Tomás*
