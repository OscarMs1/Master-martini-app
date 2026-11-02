# Matriz de evidencias · Evaluación Parcial 2 (DSY1105)

La tabla relaciona los indicadores de la pauta con componentes implementados. Para evaluar el proyecto se debe ejecutar la aplicación en un emulador o dispositivo y presentar las evidencias de colaboración del equipo.

| Indicador | Ponderación | Implementación disponible | Verificación necesaria |
| --- | ---: | --- | --- |
| **IE 2.1.1 · Interfaz y navegación** | 15 % | `ui/navigation`, Inicio, Aprende, Catálogo, Servicios y Cuenta | Navegación completa, recortes, accesibilidad y distintas resoluciones |
| **IE 2.1.2 · Formularios validados** | 15 % | Registro, login, perfil, solicitudes y cotizaciones | Errores por campo y teclado en dispositivo |
| **IE 2.2.1 · Lógica desacoplada** | 10 % | `domain/validation`, repositorios y ViewModels | Pruebas de reglas y flujo de estado |
| **IE 2.2.2 · Animaciones** | 10 % | Componentes de interfaz con transiciones y retroalimentación visual | Evaluar fluidez real en el emulador |
| **IE 2.3.1 · Modularidad y persistencia** | 15 % | MVVM, Room, DataStore y almacenamiento de fotografías | Reinicio, cambio de cuenta y permanencia de datos |
| **IE 2.3.2 · GitHub y Trello** | 20 % | Código y README preparados para repositorio | Repositorio, commits auténticos y tablero del equipo |
| **IE 2.4.1 · Recursos nativos** | 15 % | Photo Picker, cámara, notificaciones, compartir y marcador | Accionar dos o más recursos y probar permisos/cancelaciones |

## Funciones a demostrar

- **Inicio:** MP4 local con controles integrados mediante Media3 ExoPlayer (reproducción, pausa, avance y retroceso sin superposición sobre el encabezado).
- **Aprende:** recetas, fichas, cursos, filtros dinámicos, favoritos y progreso persistente.
- **Catálogo:** nueve productos de referencia, búsqueda, imágenes, formatos y cotizaciones locales.
- **Administrador:** acceso desde el login común; edición del catálogo disponible solo para el rol administrativo de demostración.
- **Cuenta:** datos de perfil, fotografía de cámara o galería, preferencias y cierre de sesión.

**Estado:** el código contiene las funciones indicadas. La nueva integración de Media3 requiere compilación y prueba visual en Android Studio. El cumplimiento total de cada indicador no se presume sin esa verificación.
