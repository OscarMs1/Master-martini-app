<div align="center">

# Master Martini Mobile

**Aprende, descubre y organiza tus ideas gastronómicas.**

Aplicación móvil para explorar recetas, conocer técnicas de preparación, consultar productos y participar en actividades relacionadas con el mundo de la gastronomía.

**Desarrollo de Aplicaciones Móviles · DSY1105**  
**Ingeniería Informática · Duoc UC · Evaluación Parcial 2**

</div>

---

## Acerca del proyecto

**Master Martini Mobile** es una aplicación Android desarrollada a partir del caso semestral de **Master Martini Chile**, una empresa vinculada a la comercialización de ingredientes e insumos para el sector alimentario.

La propuesta reúne en un mismo lugar información que puede ser útil para emprendedores, estudiantes y profesionales de panadería, pastelería, chocolatería y heladería. La idea es que una persona pueda encontrar inspiración para nuevas preparaciones, guardar contenido de interés, consultar productos y organizar solicitudes desde su teléfono.

El proyecto se centra especialmente en ofrecer una experiencia **fácil de entender, cómoda de navegar y visualmente clara**, con secciones diferenciadas, fotografías gastronómicas y opciones de accesibilidad.

### ¿Qué se puede hacer en la aplicación?

| Función | ¿Para qué sirve? |
|---|---|
| **Inicio** | Descubrir un video técnico, dos recetas de temporada y accesos a distintas especialidades. |
| **Aprende** | Consultar recetas, fichas técnicas y cursos, utilizar filtros y guardar favoritos. |
| **Catálogo** | Explorar insumos, revisar sus formatos y añadir productos a una cotización. |
| **Servicios** | Conocer actividades y registrar solicitudes de demostración o reserva. |
| **Mi cuenta** | Editar el perfil, revisar información guardada y cambiar preferencias. |
| **Administración** | Gestionar los productos del catálogo con una cuenta que tenga el permiso correspondiente. |

---

## Vista general del proyecto

<div align="center">

<img src="https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin" />
<img src="https://img.shields.io/badge/Android-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android" />
<img src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" />
<img src="https://img.shields.io/badge/Material%203-6750A4?style=flat-square&logo=materialdesign&logoColor=white" alt="Material 3" />
<img src="https://img.shields.io/badge/Room-1976D2?style=flat-square" alt="Room" />
<img src="https://img.shields.io/badge/Gradle-02303A?style=flat-square&logo=gradle&logoColor=white" alt="Gradle" />
<img src="https://img.shields.io/badge/JUnit-25A162?style=flat-square&logo=junit5&logoColor=white" alt="JUnit" />

</div>

La interfaz utiliza una combinación de **verde oscuro, tonos crema y superficies claras** para mantener una apariencia cálida y una lectura cómoda. Las distintas secciones se conectan mediante una navegación inferior sencilla, con contenido adaptado a la pantalla del teléfono.

El proyecto integra exploración de contenidos gastronómicos, consulta de productos, cotizaciones, gestión de perfil y almacenamiento local de información.

## Funcionalidades principales

### 1. Inicio: un espacio para inspirarse

La pantalla principal presenta un saludo personalizado y una selección de contenido destacado:

- **Video de templado de chocolate:** se reproduce dentro de la aplicación, con opciones para pausar, adelantar y retroceder.
- **Recetas de temporada:** dos tarjetas que muestran una preparación, su fotografía y tiempo estimado.
- **Explora por especialidad:** accesos a categorías como panadería, pastelería, chocolatería y heladería.
- **Accesos a otras actividades:** enlaces internos para seguir explorando el contenido disponible.

El video se encuentra incluido en los recursos de la aplicación, por lo que no necesita abrir YouTube para reproducirse.

### 2. Aprende: recetas, técnicas y cursos

En esta sección es posible consultar distintos tipos de contenido gastronómico.

**Recetas:** incluyen información de la preparación, tiempo aproximado y pasos de elaboración cuando corresponda.  
**Técnicas:** presentan explicaciones de procedimientos utilizados en el rubro gastronómico.  
**Cursos:** permiten revisar módulos y llevar un seguimiento del avance.

Además, la sección cuenta con:

- Búsqueda de contenidos.
- Filtros por tipo y categoría.
- Lista de favoritos.
- Progreso de aprendizaje guardado en el dispositivo.
- Pantallas de detalle con información relacionada con cada contenido.

### 3. Catálogo: productos e insumos

El catálogo presenta **nueve productos iniciales de referencia**, organizados por categoría. Cada tarjeta muestra su nombre, fotografía y formatos disponibles.

Los usuarios pueden buscar productos, filtrar categorías, elegir formatos y cantidades, y preparar una **cotización local** que pueden consultar posteriormente. Este proceso representa un flujo académico de solicitud; no realiza pagos ni envía órdenes comerciales a la empresa.

### 4. Servicios y actividades

La aplicación incluye un espacio para consultar actividades gastronómicas y registrar solicitudes relacionadas con demostraciones o reservas. Los formularios incorporan validaciones y mensajes para guiar al usuario durante el ingreso de datos.

### 5. Mi cuenta y personalización

Cada cliente puede registrarse e iniciar sesión para utilizar las funciones asociadas a su perfil:

- Consultar y editar su información de contacto.
- Indicar su tipo de negocio e intereses.
- Cambiar su fotografía desde la cámara o la galería.
- Revisar sus cotizaciones y solicitudes guardadas.
- Utilizar opciones de texto ampliado y alto contraste.
- Cerrar sesión al terminar.

---

## Cómo instalar y ejecutar

### Antes de comenzar

Se necesita:

- **Android Studio** instalado.
- **Android SDK 35** configurado.
- **JDK 17 o 21**.
- Un dispositivo Android o un emulador para ejecutar la aplicación.
- Conexión a internet durante la primera sincronización, si Gradle necesita descargar dependencias.

### Abrir el proyecto en Android Studio

1. Descarga y extrae el ZIP del proyecto.
2. Abre **Android Studio** y selecciona **File → Open**.
3. Elige la carpeta **`MASTER_MARTINI_MOBILE_EP2`**, que contiene `settings.gradle.kts`.
4. Deja que Android Studio termine de sincronizar el proyecto con Gradle.
5. En la parte superior, selecciona un emulador (por ejemplo, Pixel) o un teléfono Android conectado.
6. Asegúrate de que la configuración elegida sea **`app`** y presiona **Run**.

La aplicación se instalará y abrirá en el dispositivo seleccionado si la compilación finaliza correctamente.

### Generar una APK

Si se necesita instalar la aplicación directamente en un teléfono Android:

1. Abre el menú **Build** de Android Studio.
2. Selecciona **Build Bundle(s) / APK(s) → Build APK(s)** (el texto puede variar según la versión).
3. Cuando termine la compilación, busca el archivo:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Es una **APK de pruebas**, adecuada para instalar y revisar el proyecto. El código fuente debe conservarse para su evaluación y modificaciones.

---

## Guía rápida para probar la aplicación

| Paso | Qué revisar | Resultado esperado |
|---|---|---|
| **1** | Crear una cuenta de cliente | El registro valida los datos y permite entrar a la app. |
| **2** | Abrir Inicio y reproducir el video | El video aparece dentro de su tarjeta y se puede controlar. |
| **3** | Entrar a Aprende y usar la búsqueda | Se muestran los contenidos relacionados con la búsqueda y el filtro. |
| **4** | Guardar una receta en favoritos | La selección se conserva al volver a la sección. |
| **5** | Completar un módulo de curso | El progreso se actualiza y queda guardado. |
| **6** | Agregar productos a una cotización | Los productos, cantidades y formatos quedan registrados. |
| **7** | Editar el perfil y escoger una foto | Los cambios se reflejan en la cuenta. |
| **8** | Cerrar y volver a abrir la aplicación | La información guardada localmente se mantiene. |

Para las pruebas de registro, perfil y cotizaciones, utiliza información ficticia.

---

## ¿Cómo está construido?

El proyecto está desarrollado en **Kotlin**, usando herramientas habituales del desarrollo Android.

| Tecnología | Uso dentro de Master Martini Mobile |
|---|---|
| **Kotlin** | Lenguaje principal de la aplicación. |
| **Jetpack Compose** | Construcción de pantallas, tarjetas, formularios y botones. |
| **Material 3** | Estilos y componentes visuales. |
| **Navigation Compose** | Movimiento entre las diferentes pantallas. |
| **ViewModel** | Organización del estado y de la información utilizada por las vistas. |
| **Room** | Almacenamiento local de datos de la aplicación. |
| **DataStore** | Guardado de preferencias y configuraciones. |
| **Media3 ExoPlayer** | Reproducción del video técnico dentro de Inicio. |
| **JUnit** | Pruebas automáticas de parte de la lógica del proyecto. |

### Organización de los archivos

El código está distribuido por funciones, para que sea más fácil ubicar las pantallas y mantener el proyecto:

```text
MASTER_MARTINI_MOBILE_EP2/
│
├── app/
│   ├── src/main/
│   │   ├── java/cl/duoc/mastermartini/
│   │   │   ├── data/          Modelos, base de datos y repositorios
│   │   │   ├── device/        Cámara, imágenes y otras funciones del celular
│   │   │   ├── domain/        Validaciones y reglas de la aplicación
│   │   │   └── ui/            Pantallas, navegación y diseño
│   │   └── res/              Fotografías, íconos, textos y video
│   └── src/test/             Pruebas del proyecto
│
├── docs/                     Material complementario
├── gradle/                   Configuración de Gradle
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

El proyecto sigue una organización **MVVM**: las pantallas muestran la información, los ViewModels coordinan sus estados y los repositorios se encargan de acceder a los datos.

---

## Acceso de administración

Además de las cuentas normales, el proyecto incluye **una cuenta administrativa de pruebas** para gestionar el catálogo. Se utiliza el **mismo inicio de sesión** que los clientes.

| Dato | Credencial |
|---|---|
| **Correo electrónico** | `admin@gmail.com` |
| **Contraseña** | `admin123` |

**Para entrar al panel:** inicia sesión con estos datos, abre **Mi cuenta** y selecciona **Panel de administración**.

Desde ahí se pueden **agregar, editar, eliminar o restaurar productos**, así como seleccionar una fotografía desde el dispositivo. El botón de administración no aparece en las cuentas de cliente.

> Esta cuenta está incluida únicamente para probar el proyecto en clases. Su acceso funciona de manera local; no debe reutilizarse como sistema de seguridad para una aplicación pública.

---

## Pruebas y aspectos a revisar

El proyecto contiene pruebas de validaciones, filtros, contenidos de Inicio y reglas asociadas al acceso administrativo. También se incluyen documentos de apoyo para revisar las funcionalidades durante la evaluación.

Si quieres ejecutar las pruebas desde la terminal de Android Studio, utiliza:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

**Antes de entregar:** es importante comprobar en un emulador o teléfono que la aplicación compile, que las pantallas funcionen, que el video permita adelantar y retroceder sin mover los controles y que los cambios del catálogo se guarden correctamente.

Documentación complementaria:

- [`docs/PRUEBAS.md`](docs/PRUEBAS.md) — pruebas y comprobaciones recomendadas.
- [`docs/EVIDENCIA_RUBRICA.md`](docs/EVIDENCIA_RUBRICA.md) — relación entre funcionalidades y criterios de evaluación.
- [`docs/FUENTES_RECURSOS.md`](docs/FUENTES_RECURSOS.md) — referencias de imágenes y otros recursos utilizados.

---

## Equipo y entrega

| Información | Detalle |
|---|---|
| **Carrera** | Ingeniería Informática |
| **Asignatura** | Desarrollo de Aplicaciones Móviles |
| **Código** | DSY1105 |
| **Institución** | Duoc UC |
| **Evaluación** | Parcial 2 |
| **Caso semestral** | Master Martini Chile |
| **Integrantes** | Felipe Zapata, Oscar Muñoz y Angel Vergara |

La entrega académica considera el proyecto fuente, su documentación y las evidencias reales de trabajo colaborativo del equipo en GitHub y Trello, conforme a las instrucciones del docente.

### Alcance de esta versión

Master Martini Mobile es un **proyecto académico funcional**, desarrollado para demostrar lo aprendido durante el semestre. Los usuarios, cotizaciones y solicitudes se manejan de forma local y no están conectados con los sistemas comerciales de Master Martini Chile.

Las imágenes de productos son referenciales. El uso y distribución de fotografías y videos debe respetar los permisos correspondientes; sus referencias se encuentran en el documento de recursos.

---

<div align="center">

**Master Martini Mobile**  
*Una forma más cercana de descubrir, aprender y crear en gastronomía.*

</div>
