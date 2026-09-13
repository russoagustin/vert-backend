# Vert - Backend Catálogo

Backend para el sistema de catálogo de **Vert**, desarrollado con **Java 21**, **Spring Boot 4** y **MySQL**. Utiliza una arquitectura en capas combinando Spring Data JPA y procedimientos almacenados (Stored Procedures) con bloqueos concurrentes y transacciones seguras para la gestión de ordenamientos y catálogos jerárquicos.

---

## 🛠️ Tecnologías Utilizadas

- **Java**: 21
- **Framework**: Spring Boot 4.1.0
  - Spring Web MVC
  - Spring Data JPA
- **Base de Datos**: MySQL 8.0+
- **ORM**: Hibernate ORM 7.x
- **Librerías**: Lombok
- **Pruebas**: JUnit 5, Mockito
- **Herramientas de Construcción**: Maven (con Maven Wrapper)
- **Testing de API**: Colección de Postman v2.1.0 incluida

---

## 📁 Estructura del Proyecto

```text
vert-backend/
├── src/
│   ├── main/
│   │   ├── java/com/vert/catalogo/
│   │   │   ├── controller/         # Controladores REST
│   │   │   │   ├── CategoriaController.java
│   │   │   │   └── SubCategoriaController.java
│   │   │   ├── dto/                # Data Transfer Objects y Records
│   │   │   │   ├── CategoriaDto.java
│   │   │   │   ├── CategoriaOrdenDto.java
│   │   │   │   ├── SubCategoriaDto.java
│   │   │   │   ├── SubCategoriaOrdenDto.java
│   │   │   │   └── ErrorResponseDto.java
│   │   │   ├── entities/           # Entidades JPA (Categoría, SubCategoría, Producto, Usuario)
│   │   │   ├── exceptions/         # Manejo global de errores y excepciones personalizadas
│   │   │   │   ├── ErrorCode.java
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── NotFoundException.java
│   │   │   │   └── ValidationException.java
│   │   │   ├── repositories/       # Repositorios JPA y llamadas a Stored Procedures
│   │   │   └── services/           # Interfaces e implementaciones de la lógica de negocio
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── script.sql           # DDL de la base de datos MySQL
│   │       └── storedProcedures.sql # Procedimientos almacenados
│   └── test/                       # Pruebas unitarias y de contexto Spring
├── Vert_Catalogo.postman_collection.json # Colección de Postman lista para importar
└── pom.xml
```

---

## ⚙️ Configuración y Requisitos

### Requisitos Previos
- **Java JDK 21** o superior instalado.
- **MySQL Server 8.0** o superior en ejecución.

### Configuración de la Base de Datos
El proyecto carga las variables de conexión desde el archivo `.env` o variables de entorno del sistema:

Crea un archivo `.env` en la raíz del proyecto (o utiliza el existente):
```properties
DB_URL=jdbc:mysql://localhost:3306/Vert?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_USERNAME=root
DB_PASSWORD=tu_contraseña
```

### Inicialización de Tablas y Stored Procedures
Ejecuta en tu cliente MySQL (Workbench, CLI, DBeaver, etc.) en el siguiente orden:
1. `src/main/resources/script.sql`: Crea el esquema `Vert` y las tablas `Categorias`, `SubCategorias`, `Productos`, etc.
2. `src/main/resources/storedProcedures.sql`: Registra los procedimientos almacenados para altas, modificaciones y reordenamientos seguros.

---

## 🚀 Ejecución del Proyecto

### Iniciar la aplicación
Desde la terminal en el directorio raíz del proyecto:

```powershell
# En Windows (PowerShell)
.\mvnw.cmd spring-boot:run

# En Linux/macOS
./mvnw spring-boot:run
```

La API quedará escuchando en `http://localhost:8080`.

### Ejecutar Pruebas
Para ejecutar la suite completa de pruebas unitarias y de integración:

```powershell
.\mvnw.cmd test
```

---

## 📖 Referencia de Endpoints API

### 🏷️ Categorías (`/api/categorias`)

| Método | Endpoint | Descripción | Body Ejemplo |
| :--- | :--- | :--- | :--- |
| **GET** | `/api/categorias` | Lista todas las categorías ordenadas por `orden` ASC. | — |
| **GET** | `/api/categorias/{id}` | Obtiene una categoría por su ID. | — |
| **GET** | `/api/categorias/buscar?nombre={nombre}` | Busca una categoría por nombre exacto. | — |
| **POST** | `/api/categorias` | Crea una categoría (calcula orden automáticamente). Retorna `201 Created` + `Location`. | `{"nombre": "Bebidas"}` |
| **PUT** | `/api/categorias/{id}` | Modifica el nombre de una categoría. Retorna `204 No Content`. | `{"nombre": "Bebidas Frías"}` |
| **PATCH** | `/api/categorias/{id}/orden` | Cambia el orden de una sola categoría desplazando al resto. | `{"orden": 2}` |
| **PATCH** | `/api/categorias/orden` | Reordena en lote todas las categorías registradas. | `[{"id": 1, "orden": 2}, {"id": 2, "orden": 1}]` |
| **DELETE** | `/api/categorias/{id}` | Elimina una categoría por su ID. | — |

---

### 📂 SubCategorías (`/api/subcategorias`)

| Método | Endpoint | Descripción | Body Ejemplo |
| :--- | :--- | :--- | :--- |
| **GET** | `/api/subcategorias` | Lista todas las subcategorías (o filtra con `?idCategoria=...`). | — |
| **GET** | `/api/subcategorias/categoria/{idCategoria}` | Lista subcategorías pertenecientes a una categoría. | — |
| **GET** | `/api/subcategorias/{id}` | Obtiene una subcategoría por ID. | — |
| **GET** | `/api/subcategorias/categoria/{idCategoria}/{id}` | Obtiene una subcategoría validando su categoría padre. | — |
| **GET** | `/api/subcategorias/buscar?idCategoria={id}&nombre={nombre}` | Busca una subcategoría por nombre dentro de una categoría. | — |
| **POST** | `/api/subcategorias` | Crea una subcategoría vinculada a una categoría existente. Retorna `201 Created` + `Location`. | `{"idCategoria": 1, "nombre": "Gaseosas"}` |
| **PUT** | `/api/subcategorias/{id}` | Modifica el nombre de una subcategoría existente. | `{"idCategoria": 1, "nombre": "Gaseosas Zero"}` |
| **PATCH** | `/api/subcategorias/{id}/orden` | Cambia el orden de una subcategoría dentro de su categoría. | `{"idCategoria": 1, "orden": 2}` |
| **PATCH** | `/api/subcategorias/orden?idCategoria={id}` | Reordena en lote **todas las subcategorías de la categoría dada**. | `[{"id": 1, "orden": 2}, {"id": 2, "orden": 1}]` |
| **PATCH** | `/api/subcategorias/categoria/{idCategoria}/orden` | Ruta alternativa para reordenamiento en lote por categoría. | `[{"id": 1, "orden": 2}, {"id": 2, "orden": 1}]` |
| **DELETE** | `/api/subcategorias/{id}` | Elimina una subcategoría por su ID. | — |
| **DELETE** | `/api/subcategorias/categoria/{idCategoria}/{id}` | Elimina una subcategoría validando su categoría padre. | — |

---

### 📦 Productos (`/api/productos`)

| Método | Endpoint | Descripción | Body / Partes |
| :--- | :--- | :--- | :--- |
| **GET** | `/api/productos` | Lista todos los productos (soporta `?idCategoria=...` e `?idSubCategoria=...`). | — |
| **GET** | `/api/productos/categoria/{idCategoria}` | Lista productos pertenecientes a una categoría. | — |
| **GET** | `/api/productos/categoria/{idCategoria}/subcategoria/{idSubCategoria}` | Lista productos pertenecientes a una subcategoría específica. | — |
| **GET** | `/api/productos/{id}` | Obtiene un producto por su ID. | — |
| **GET** | `/api/productos/buscar?nombre={nombre}` | Busca un producto por nombre exacto. | — |
| **POST** | `/api/productos` | Crea un producto. **Obligatorio enviar imagen** como `MultipartFile`. Retorna `201 Created` + `Location`. | `multipart/form-data`:<br>• `producto` (JSON): `{"idCategoria": 1, "idSubCategoria": 1, "nombre": "Coca Cola 1.5L", "precio": 1500.00, "precioDescuento": 1200.00, "descripcion": "Gaseosa", "cantidad": 50}`<br>• `imagen` (Archivo binario) |
| **PUT** | `/api/productos/{id}` | Modifica un producto existente. La imagen es opcional: si se envía se actualiza en Cloudflare R2; si no se envía se preserva la anterior. | `multipart/form-data`:<br>• `producto` (JSON)<br>• `imagen` (Archivo opcional)<br>*O `application/json` si no hay cambios de imagen.* |
| **DELETE** | `/api/productos/{id}` | Elimina un producto por su ID. Retorna `204 No Content`. | — |

---

## 🛡️ Manejo de Errores

El backend cuenta con un manejador global de excepciones (`GlobalExceptionHandler`) que estandariza todas las respuestas de error en formato JSON:

```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Error: El nombre de la categoría ya se encuentra en uso."
}
```

- **404 NOT FOUND**: Cuando el recurso solicitado no existe (`NotFoundException`).
- **400 BAD REQUEST**: Validaciones de negocio (`ValidationException`) y errores de base de datos capturados desde los Stored Procedures con `SQLSTATE '45000'` (nombres duplicados, valores nulos, etc.).
- **500 INTERNAL SERVER ERROR**: Errores no controlados.

---

## 📮 Pruebas con Postman

El archivo [`Vert_Catalogo.postman_collection.json`](./Vert_Catalogo.postman_collection.json) contiene la colección completa lista para importar en **Postman**:

1. Abre Postman y haz clic en **Import**.
2. Selecciona el archivo `Vert_Catalogo.postman_collection.json`.
3. Utiliza las variables de entorno preconfiguradas:
   - `base_url`: `http://localhost:8080`
   - `categoria_id`: `1`
   - `subcategoria_id`: `1`
