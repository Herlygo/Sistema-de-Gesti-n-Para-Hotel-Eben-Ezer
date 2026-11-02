# Sistema de Gestión para Hotel Eben-Ezer

Aplicación de escritorio en Java para gestionar huéspedes, habitaciones y reservas, con base de datos en Supabase (PostgreSQL).

## Video promocional (máx. 2 min)
[Ver video](https://drive.google.com/file/d/1IZf4WbVDdatlS97rJRpgPthoUorJm9ZE/view?usp=sharing)

## Integrantes
- Herly González - 0903-25-14485
- Luis Fuentes   - 0903-25-24281
- Cesia López    - 0903-25-6106
- Arnold Barrios - 0903-24-19181

## Contenido del repositorio
- `/src` – código fuente (Java + Maven)
- `/database/script_completo.sql` – script completo de la base de datos
- [Guía de la interfaz](README-INTERFAZ.md)

## Requisitos
- Windows 10/11
- JDK 25 y Maven solo si se ejecuta desde el código
- Conexión a internet (base de datos en Supabase)

## Opción A: instalar y usar
1. [Descargar el instalador](https://github.com/Herlygo/Sistema-de-Gesti-n-Para-Hotel-Eben-Ezer/releases/download/v1.0/Hotel.Eben-Ezer-1.0.exe) y ejecutarlo. Si Windows muestra una advertencia, elegir *Más información → Ejecutar de todas formas*.
2. Abrir "Hotel Eben-Ezer" desde el menú Inicio.

## Opción B: ejecutar desde el código
1. `git clone https://github.com/Herlygo/Sistema-de-Gesti-n-Para-Hotel-Eben-Ezer.git`
2. Crear un proyecto en Supabase y ejecutar `database/script_completo.sql` en el *SQL Editor*.
3. Configurar la conexión en [`Conexion.java`](src/main/java/com/hotel/bd/Conexion.java) con la URL, el usuario y la contraseña del proyecto de Supabase (*Project Settings → Database → Connection string*).
4. `mvn clean package` y ejecutar la clase principal `com.mycompany.apphotel.AppHotel`.

5.  Ejecutar: `java -jar target/AppHotel-1.0-SNAPSHOT.jar` (confirma el nombre exacto del archivo en la carpeta `target/`).

## Usuario de prueba
Usuario: Admin / Contraseña: admin123
