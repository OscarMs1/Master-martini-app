# Plan de pruebas y verificación · Master Martini Mobile

## Comandos

Abrir el proyecto en Android Studio con SDK 35 y ejecutar la sincronización de Gradle. Luego:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

Un APK de depuración se crea en `app/build/outputs/apk/debug/app-debug.apk` si `assembleDebug` termina correctamente.

## Pruebas unitarias incluidas

- `domain/validation/ValidatorsTest.kt`: validaciones de formularios y reglas de negocio.
- `domain/validation/LearnFiltersTest.kt`: filtros y búsquedas de Aprende.
- `ui/screens/home/HomeContentSelectionTest.kt`: selección de contenidos para Inicio.
- `data/repository/DemoAdminAccountTest.kt`: verificación de credenciales administrativas de demostración.
- `LearningTest.kt`: lógica de aprendizaje.

## Lista de comprobación en emulador

- [ ] Iniciar sesión como cliente. Abrir Inicio, Aprende, Catálogo y Cuenta.
- [ ] Reproducir el MP4; adelantar y retroceder repetidas veces. **Los controles deben permanecer dentro del video**.
- [ ] Pausar, cerrar, reabrir el video y cambiar de pestaña sin sonido de fondo.
- [ ] Bloquear o minimizar la app mientras el video reproduce y comprobar que se pausa.
- [ ] Terminar el video y volver a reproducirlo.
- [ ] Buscar contenidos, aplicar categorías, añadir favoritos y verificar la persistencia al reiniciar.
- [ ] Crear una cotización y comprobar cantidades y formatos.
- [ ] Probar formulario de registro y validaciones visuales con datos ficticios.
- [ ] Abrir cámara o selector de fotografías; verificar guardado y cancelación.
- [ ] Iniciar sesión como `admin@gmail.com`, abrir Panel de administración, modificar imagen y contenido de un producto.
- [ ] Cerrar sesión e iniciar como cliente: comprobar que el editor no se muestra ni permite editar.
- [ ] Comprobar barra de estado, contraste, tamaño grande de texto, navegación atrás y teclado.

## Alcance de las comprobaciones

La inspección del paquete, referencias de recursos y código fuente puede realizarse sin el entorno Android. **La compilación del APK, las pruebas instrumentadas y el funcionamiento de Media3 deben validarse en Android Studio antes de la entrega**, ya que el entorno en el que se preparó el ZIP no incluye Android SDK ni emulador.
