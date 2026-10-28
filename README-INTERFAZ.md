# Interfaz HOTEL EBEN-EZER

Esta versión aplica una identidad visual uniforme al sistema Java Swing, usando el nombre y el logo proporcionados y la paleta:

- Azul marino: `#0A1931`
- Azul claro: `#B3CFE5`
- Malva: `#A47FA7`
- Azul: `#1A3D63`
- Fondo claro: `#F6FAFD`

## Cambios incluidos

- Inicio de sesión rediseñado, sin fotografía de fondo y con el logo del hotel.
- Menú principal rediseñado con navegación lateral, tarjetas de resumen, disponibilidad de habitaciones, reservas recientes, actividad y accesos rápidos.
- El resumen intenta leer los datos existentes desde los DAO de clientes, habitaciones y reservas; no utiliza cifras ficticias para los contadores.
- Se aplicó una apariencia coherente a las ventanas existentes de clientes, habitaciones y reservas.
- Se agregó `EstiloHotel.java` para centralizar los colores y estilos.
- Se agregó el logo como recurso en `src/main/resources/com/hotel/vista/logo-eben-ezer.png`.

## Ejecutar en IntelliJ IDEA

1. Extrae el ZIP.
2. Abre la carpeta `AppHotel Project` que contiene `pom.xml`.
3. Configura un JDK compatible con el valor de `maven.compiler.release` de `pom.xml` (actualmente 25 en el proyecto original).
4. Espera a que Maven cargue las dependencias.
5. Ejecuta `com.mycompany.apphotel.AppHotel`.

La aplicación necesita una conexión válida a la base de datos para autenticar usuarios y mostrar los datos reales. Los apartados Usuarios, Reportes y Configuración aparecen en el menú como accesos informativos, pero sus ventanas todavía no existen en el proyecto.
