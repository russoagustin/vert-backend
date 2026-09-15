# Vert - Backend Catálogo

Backend para el sistema de catálogo de **Vert**, desarrollado con **Java 21**, **Spring Boot 4**, **Spring Security con JWT**, **MySQL** y almacenamiento de imágenes en la nube con **Cloudflare R2** (compatible con AWS S3 API). 

Combina una arquitectura en capas basada en Spring Data JPA con procedimientos almacenados (Stored Procedures) con bloqueos concurrentes y transacciones atómicas para la gestión segura de ordenamientos jerárquicos.

---

## 🛠️ Tecnologías y Dependencias

- **Lenguaje**: Java 21 LTS
- **Framework Principal**: Spring Boot 4.1.0
  - `spring-boot-starter-webmvc`: Controladores REST y validación de peticiones.
  - `spring-boot-starter-data-jpa`: Capa de persistencia y mapeo objeto-relacional.
  - `spring-boot-starter-security`: Seguridad, filtros y autenticación sin estado.
  - `spring-boot-starter-validation`: Validaciones de beans (`@Valid`, `@NotNull`, `@NotBlank`, etc.).
- **Base de Datos**: MySQL 8.0+
- **ORM**: Hibernate ORM 7.x
- **Almacenamiento de Medios**: Cloudflare R2 Object Storage (mediante AWS Java SDK v2 S3 `software.amazon.awssdk:s3:2.54.17`).
- **Autenticación**: Auth0 Java JWT (`com.auth0:java-jwt:4.5.1`) con tokens firmados vía HMAC256 y soporte para Cookies `HttpOnly`.
- **Utilidades**: Lombok
- **Testing**: JUnit 5, Mockito, Spring Boot Starter Test
- **Colección de API**: Postman v2.1.0 incluida

---

## 📁 Estructura del Proyecto

```text
vert-backend/
├── src/
│   ├── main/
│   │   ├── java/com/vert/catalogo/
│   │   │   ├── CatalogoApplication.java
│   │   │   ├── config/                     # Configuraciones de seguridad y almacenamiento
│   │   │   │   ├── filters/
│   │   │   │   │   └── JwtFilter.java      # Filtro de autenticación JWT para peticiones
│   │   │   │   ├── R2Config.java           # Configuración del cliente S3 para Cloudflare R2
│   │   │   │   └── SecurityConfig.java     # Configuración de Spring Security y Beans
│   │   │   ├── controller/                 # Endpoints REST de la API
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── CategoriaController.java
│   │   │   │   ├── ProductoController.java
│   │   │   │   └── SubCategoriaController.java
│   │   │   ├── dto/                        # DTOs y Records para entrada/salida y validación
│   │   │   │   ├── CategoriaDto.java
│   │   │   │   ├── CategoriaOrdenDto.java
│   │   │   │   ├── ErrorResponseDto.java
│   │   │   │   ├── LoginRequestDto.java
│   │   │   │   ├── ProductoDto.java
│   │   │   │   ├── SubCategoriaDto.java
│   │   │   │   └── SubCategoriaOrdenDto.java
│   │   │   ├── entities/                   # Entidades JPA (Categoría, SubCategoría, Producto, Usuario)
│   │   │   │   ├── Categoria.java
│   │   │   │   ├── Producto.java
│   │   │   │   ├── SubCategoria.java
│   │   │   │   └── Usuario.java
│   │   │   ├── exceptions/                 # Manejo centralizado de excepciones y códigos de error
│   │   │   │   ├── ErrorCode.java
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── NotFoundException.java
│   │   │   │   └── ValidationException.java
│   │   │   ├── repositories/               # Repositorios JPA y llamadas a Stored Procedures
│   │   │   └── services/                   # Lógica de negocio (interfaces e implementaciones)
│   │   │       ├── impl/
│   │   │       │   ├── AppDetailsService.java
│   │   │       │   ├── CloudflareStorageService.java
│   │   │       │   ├── DefaultCategoriaService.java
│   │   │       │   ├── DefaultProductoService.java
│   │   │       │   ├── DefaultSubCategoriaService.java
│   │   │       │   └── JwtService.java
│   │   │       └── interfaces/
│   │   │           ├── CategoriaService.java
│   │   │           ├── ProductoService.java
│   │   │           ├── StorageService.java
│   │   │           └── SubCategoriaService.java
│   │   └── resources/
│   │       ├── application.properties      # Configuración de base de datos y multipart
│   │       ├── bucket.properties           # Variables de conexión a Cloudflare R2
│   │       ├── jwt.properties              # Configuración de tokens JWT y cookies
│   │       ├── schema.sql                  # DDL de tablas (Categorias, SubCategorias, Productos, Usuarios)
│   │       ├── script.sql                  # DDL alternativo
│   │       └── storedProcedures.sql        # Procedimientos almacenados para alta y reordenamiento
│   └── test/                               # Tests unitarios y de integración
├── Vert_Catalogo.postman_collection.json   # Colección completa de Postman lista para importar
├── pom.xml
└── README.md
```

---

## ⚙️ Configuración y Variables de Entorno

### 1. Variables de Entorno (`.env`)
El proyecto lee automáticamente variables de entorno desde un archivo `.env` en la raíz del proyecto (o configuradas en el sistema operativo / contenedor).

Crea un archivo `.env` en la raíz con la siguiente estructura:

```properties
# Base de Datos MySQL
DB_URL=jdbc:mysql://localhost:3306/Vert?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_USERNAME=root
DB_PASSWORD=tu_password_mysql

# Cloudflare R2 Object Storage
CLOUDFLARE_R2_ACCESS_KEY=tu_r2_access_key
CLOUDFLARE_R2_SECRET_KEY=tu_r2_secret_key
CLOUDFLARE_R2_BUCKET=vert
CLOUDFLARE_R2_ACCOUNT_ID=tu_cloudflare_account_id
CLOUDFLARE_R2_PUBLIC_URL=https://media.accesoriosvert.online
```

### 2. Configuración de Seguridad y JWT (`jwt.properties`)
Ubicado en `src/main/resources/jwt.properties`:

```properties
jwt.private-key=tu-clave-secreta-para-firmar-jwt
jwt.issuer=accesoriosvert.online
jwt.expiration-milis=1800000
jwt.refresh-expiration-minutes=10080
jwt.refresh-token-cookie-name=refresh_token
jwt.access-token-cookie-name=access_token
jwt.cookie-secure=false # Cambiar a true en producción (HTTPS)
```

### 3. Inicialización de la Base de Datos
Ejecuta los scripts en tu motor MySQL en este orden:
1. `src/main/resources/schema.sql`: Crea el esquema `Vert` y las tablas `Categorias`, `SubCategorias`, `Productos` y `Usuarios`.
2. `src/main/resources/storedProcedures.sql`: Registra las rutinas y procedimientos almacenados (`sp_CrearCategoria`, `sp_ModificarOrdenCategoria`, `sp_ModificarOrdenCategorias`, etc.).

---

## 🚀 Ejecución

### Iniciar la aplicación en modo desarrollo
```powershell
# Windows (PowerShell)
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

El servidor iniciará en: `http://localhost:8080`.

### Ejecutar Pruebas
```powershell
.\mvnw.cmd test
```

---

## 📖 Referencia de la API

> 📘 **Documentación para Frontend:** Puedes consultar la especificación técnica completa con modelos de datos, TypeScript interfaces, paginación y snippets de integración en [`API_FRONTEND_SPECIFICATION.md`](./API_FRONTEND_SPECIFICATION.md).

A continuación se detallan los endpoints disponibles divididos por módulo, con la estructura exacta de los **Body de entrada (Request Body)**, validaciones de campos y ejemplos de respuesta.

> [!IMPORTANT]
> **Política de Seguridad y Autenticación:**
> - 🔓 **Endpoints Públicos:** Exclusivamente endpoints de consulta y listado (método `GET`) y el inicio de sesión (`POST /api/auth/login`).
> - 🔒 **Endpoints Protegidos (Gestión):** Todas las operaciones de **alta / creación** (`POST`), **modificación / reordenamiento** (`PUT`, `PATCH`) y **borrado** (`DELETE`) de recursos requieren autenticación.
> - **Autenticación requerida:** Enviar el token JWT en la cabecera `Authorization: Bearer <token>` o mediante la cookie HttpOnly `access_token` generada tras el login. Las peticiones no autenticadas a endpoints protegidos responderán con `401 Unauthorized` o `403 Forbidden`.

---

### 🔐 1. Módulo de Autenticación (`/api/auth`)

Maneja el inicio de sesión del personal administrativo. Al autenticar correctamente, emite un token JWT configurado en una cookie `HttpOnly` (`access_token`) para máxima seguridad contra ataques XSS, además de permitir autenticación vía cabecera `Authorization: Bearer <token>`.

#### 📌 Iniciar Sesión (Login) `[🔓 Público]`
- **Método**: `POST`
- **Ruta**: `/api/auth/login`
- **Acceso**: 🔓 Público
- **Headers**:
  - `Content-Type: application/json`

##### Body de la Petición:
```json
{
  "username": "admin",
  "password": "miPasswordSeguro123"
}
```

| Campo | Tipo | Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `username` | `String` | **Sí** | Nombre de usuario registrado en la tabla `Usuarios`. |
| `password` | `String` | **Sí** | Contraseña en texto plano (se verifica contra el hash BCrypt almacenado). |

##### Respuestas:
- **`200 OK`**:
  - **Headers de Respuesta**:
    ```http
    Set-Cookie: access_token=eyJhbGciOi...; Path=/; Max-Age=1800; HttpOnly; SameSite=Strict
    ```
- **`401 UNAUTHORIZED`**: Credenciales inválidas.

---

### 🏷️ 2. Módulo de Categorías (`/api/categorias`)

Administra las categorías de primer nivel del catálogo. Las altas y reordenamientos se delegan a procedimientos almacenados para garantizar órdenes consecutivos sin huecos ni duplicados.

#### Resumen de Endpoints:
| Método | Ruta | Acceso | Descripción | Body |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/categorias` | 🔓 Público | Lista todas las categorías ordenadas por `orden` ASC | — |
| `GET` | `/api/categorias/{id}` | 🔓 Público | Busca una categoría por ID | — |
| `GET` | `/api/categorias/buscar?nombre={nombre}` | 🔓 Público | Busca una categoría por nombre exacto | — |
| `POST` | `/api/categorias` | 🔒 Protegido | Crea una nueva categoría | JSON `CategoriaDto` |
| `PUT` | `/api/categorias/{id}` | 🔒 Protegido | Modifica el nombre de una categoría | JSON `CategoriaDto` |
| `PATCH` | `/api/categorias/{id}/orden` | 🔒 Protegido | Cambia el orden de una sola categoría | JSON `CategoriaDto` |
| `PATCH` | `/api/categorias/orden` | 🔒 Protegido | Reordena en lote todas las categorías | Array JSON `[CategoriaOrdenDto]` |
| `DELETE` | `/api/categorias/{id}` | 🔒 Protegido | Elimina una categoría | — |

---

#### 📌 Detalle de Peticiones con Body:

#### ➤ Crear Categoría `[🔒 Protegido]`
- **Método**: `POST`
- **Ruta**: `/api/categorias`
- **Acceso**: 🔒 Protegido (Requiere JWT)
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer <token>` *(o cookie `access_token`)*

##### Body de la Petición:
```json
{
  "nombre": "Accesorios de Pelo"
}
```

| Campo | Tipo | Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `nombre` | `String` | **Sí** | Nombre de la categoría. No puede estar vacío ni repetirse (índice único). |

##### Respuestas:
- **`201 Created`**: Retorna cabecera `Location: /api/categorias/{id}` con el ID asignado. El campo `orden` se calcula automáticamente como el último valor disponible.
- **`400 Bad Request`**: Nombre duplicado o inválido.
- **`401 Unauthorized`**: Token JWT no provisto o inválido.

---

#### ➤ Modificar Categoría `[🔒 Protegido]`
- **Método**: `PUT`
- **Ruta**: `/api/categorias/{id}`
- **Acceso**: 🔒 Protegido (Requiere JWT)
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer <token>` *(o cookie `access_token`)*

##### Body de la Petición:
```json
{
  "nombre": "Accesorios y Moda"
}
```

| Campo | Tipo | Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `nombre` | `String` | **Sí** | Nuevo nombre para la categoría existente. |

##### Respuestas:
- **`204 No Content`**: Modificación exitosa.
- **`400 Bad Request`**: Nombre vacío o inválido.
- **`401 Unauthorized`**: Token JWT no provisto o inválido.
- **`404 Not Found`**: La categoría con ese ID no existe.

---

#### ➤ Cambiar Orden de una Categoría (Individual) `[🔒 Protegido]`
Desplaza las demás categorías automáticamente hacia arriba o abajo según la nueva posición deseada.
- **Método**: `PATCH`
- **Ruta**: `/api/categorias/{id}/orden`
- **Acceso**: 🔒 Protegido (Requiere JWT)
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer <token>` *(o cookie `access_token`)*

##### Body de la Petición:
```json
{
  "orden": 2
}
```

| Campo | Tipo | Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `orden` | `Integer` | **Sí** | Nueva posición en la lista (número entero positivo mayor a 0). |

##### Respuestas:
- **`204 No Content`**: Orden actualizado correctamente.
- **`401 Unauthorized`**: Token JWT no provisto o inválido.

---

#### ➤ Reordenar Categorías en Lote `[🔒 Protegido]`
Permite reordenar simultáneamente todas las categorías registradas. **Requiere enviar la totalidad de las categorías existentes** para evitar inconsistencias.
- **Método**: `PATCH`
- **Ruta**: `/api/categorias/orden`
- **Acceso**: 🔒 Protegido (Requiere JWT)
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer <token>` *(o cookie `access_token`)*

##### Body de la Petición:
```json
[
  {
    "id": 1,
    "orden": 2
  },
  {
    "id": 2,
    "orden": 1
  },
  {
    "id": 3,
    "orden": 3
  }
]
```

| Elemento | Tipo | Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `id` | `Integer` | **Sí** | ID de la categoría. |
| `orden` | `Integer` | **Sí** | Posición deseada (entero mayor a 0 sin repetirse). |

##### Respuestas:
- **`204 No Content`**: Reordenamiento en lote completado.
- **`400 Bad Request`**: La lista no coincide con el total de categorías o los números de orden son inválidos/duplicados.
- **`401 Unauthorized`**: Token JWT no provisto o inválido.

---

#### ➤ Eliminar Categoría `[🔒 Protegido]`
- **Método**: `DELETE`
- **Ruta**: `/api/categorias/{id}`
- **Acceso**: 🔒 Protegido (Requiere JWT)
- **Headers**:
  - `Authorization: Bearer <token>` *(o cookie `access_token`)*

##### Respuestas:
- **`204 No Content`**: Categoría eliminada exitosamente.
- **`401 Unauthorized`**: Token JWT no provisto o inválido.
- **`404 Not Found`**: La categoría con ese ID no existe.

---

### 📂 3. Módulo de SubCategorías (`/api/subcategorias`)

Cada subcategoría depende obligatoriamente de una categoría padre (`idCategoria`).

#### Resumen de Endpoints:
| Método | Ruta | Acceso | Descripción | Body |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/subcategorias` | 🔓 Público | Lista todas las subcategorías (o filtra con `?idCategoria={id}`) | — |
| `GET` | `/api/subcategorias/categoria/{idCategoria}` | 🔓 Público | Lista subcategorías pertenecientes a una categoría | — |
| `GET` | `/api/subcategorias/{id}` | 🔓 Público | Busca una subcategoría por ID | — |
| `GET` | `/api/subcategorias/categoria/{idCat}/{id}` | 🔓 Público | Busca una subcategoría verificando su categoría padre | — |
| `GET` | `/api/subcategorias/buscar?idCategoria={id}&nombre={nombre}` | 🔓 Público | Busca por nombre exacto dentro de una categoría | — |
| `POST` | `/api/subcategorias` | 🔒 Protegido | Crea una subcategoría vinculada a una categoría | JSON `SubCategoriaDto` |
| `PUT` | `/api/subcategorias/{id}` | 🔒 Protegido | Modifica el nombre de una subcategoría | JSON `SubCategoriaDto` |
| `PATCH` | `/api/subcategorias/{id}/orden` | 🔒 Protegido | Cambia el orden individual de una subcategoría | JSON `SubCategoriaDto` |
| `PATCH` | `/api/subcategorias/orden?idCategoria={id}` | 🔒 Protegido | Reordena en lote las subcategorías de una categoría | Array JSON `[SubCategoriaOrdenDto]` |
| `PATCH` | `/api/subcategorias/categoria/{idCategoria}/orden` | 🔒 Protegido | Ruta alternativa de reordenamiento en lote | Array JSON `[SubCategoriaOrdenDto]` |
| `DELETE` | `/api/subcategorias/{id}` | 🔒 Protegido | Elimina una subcategoría por ID | — |
| `DELETE` | `/api/subcategorias/categoria/{idCat}/{id}` | 🔒 Protegido | Elimina una subcategoría validando su categoría padre | — |

---

#### 📌 Detalle de Peticiones con Body:

#### ➤ Crear SubCategoría `[🔒 Protegido]`
- **Método**: `POST`
- **Ruta**: `/api/subcategorias`
- **Acceso**: 🔒 Protegido (Requiere JWT)
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer <token>` *(o cookie `access_token`)*

##### Body de la Petición:
```json
{
  "idCategoria": 1,
  "nombre": "Hebillas y Broches"
}
```

| Campo | Tipo | Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `idCategoria` | `Integer` | **Sí** | ID de la categoría padre a la que pertenecerá. |
| `nombre` | `String` | **Sí** | Nombre de la subcategoría (único dentro de esa categoría). |

##### Respuestas:
- **`201 Created`**: Header `Location: /api/subcategorias/{id}`.
- **`400 Bad Request`**: Datos inválidos.
- **`401 Unauthorized`**: Token JWT no provisto o inválido.

---

#### ➤ Modificar SubCategoría `[🔒 Protegido]`
- **Método**: `PUT`
- **Ruta**: `/api/subcategorias/{id}`
- **Acceso**: 🔒 Protegido (Requiere JWT)
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer <token>` *(o cookie `access_token`)*

##### Body de la Petición:
```json
{
  "idCategoria": 1,
  "nombre": "Hebillas, Clips y Broches"
}
```

| Campo | Tipo | Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `idCategoria` | `Integer` | **Sí** | ID de la categoría a la que pertenece la subcategoría. |
| `nombre` | `String` | **Sí** | Nuevo nombre para la subcategoría. |

##### Respuestas:
- **`204 No Content`**: Actualización exitosa.
- **`400 Bad Request`**: Datos inválidos.
- **`401 Unauthorized`**: Token JWT no provisto o inválido.
- **`404 Not Found`**: Subcategoría o categoría no encontrada.

---

#### ➤ Cambiar Orden de una SubCategoría (Individual) `[🔒 Protegido]`
- **Método**: `PATCH`
- **Ruta**: `/api/subcategorias/{id}/orden` *(opcionalmente con `?idCategoria={id}`)*
- **Acceso**: 🔒 Protegido (Requiere JWT)
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer <token>` *(o cookie `access_token`)*

##### Body de la Petición:
```json
{
  "idCategoria": 1,
  "orden": 2
}
```

| Campo | Tipo | Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `idCategoria` | `Integer` | **Sí** *(o por query param)* | ID de la categoría contenedora. |
| `orden` | `Integer` | **Sí** | Nueva posición deseada dentro de esa categoría. |

##### Respuestas:
- **`204 No Content`**: Reordenamiento aplicado.
- **`401 Unauthorized`**: Token JWT no provisto o inválido.

---

#### ➤ Reordenar SubCategorías en Lote `[🔒 Protegido]`
Reordena todas las subcategorías de una categoría específica. Se debe enviar la lista completa de subcategorías pertenecientes a dicha categoría.
- **Método**: `PATCH`
- **Ruta**: `/api/subcategorias/orden?idCategoria=1` o `/api/subcategorias/categoria/1/orden`
- **Acceso**: 🔒 Protegido (Requiere JWT)
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer <token>` *(o cookie `access_token`)*

##### Body de la Petición:
```json
[
  {
    "id": 1,
    "orden": 2
  },
  {
    "id": 2,
    "orden": 1
  }
]
```

| Elemento | Tipo | Requerido | Descripción |
| :--- | :--- | :--- | :--- |
| `id` | `Integer` | **Sí** | ID de la subcategoría. |
| `orden` | `Integer` | **Sí** | Nueva posición dentro de la categoría. |

##### Respuestas:
- **`204 No Content`**: Subcategorías reordenadas exitosamente.
- **`400 Bad Request`**: Datos inconsistentes o duplicados.
- **`401 Unauthorized`**: Token JWT no provisto o inválido.

---

#### ➤ Eliminar SubCategoría `[🔒 Protegido]`
- **Método**: `DELETE`
- **Ruta**: `/api/subcategorias/{id}` o `/api/subcategorias/categoria/{idCategoria}/{id}`
- **Acceso**: 🔒 Protegido (Requiere JWT)
- **Headers**:
  - `Authorization: Bearer <token>` *(o cookie `access_token`)*

##### Respuestas:
- **`204 No Content`**: Subcategoría eliminada exitosamente.
- **`401 Unauthorized`**: Token JWT no provisto o inválido.
- **`404 Not Found`**: Subcategoría no encontrada.

---

### 📦 4. Módulo de Productos (`/api/productos`)

Permite la administración de productos con paginación, filtros combinados y almacenamiento de imágenes en **Cloudflare R2**.

#### Resumen de Endpoints:
| Método | Ruta | Acceso | Descripción | Formato / Body |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/productos` | 🔓 Público | Listado paginado (filtro opcional: `?idCategoria=..&idSubCategoria=..`) | Parámetros de paginación |
| `GET` | `/api/productos/categoria/{idCategoria}` | 🔓 Público | Listado paginado de una categoría | Parámetros de paginación |
| `GET` | `/api/productos/categoria/{idCat}/subcategoria/{idSubCat}` | 🔓 Público | Listado paginado de categoría y subcategoría | Parámetros de paginación |
| `GET` | `/api/productos/{id}` | 🔓 Público | Obtiene un producto por su ID | — |
| `GET` | `/api/productos/buscar?nombre={nombre}` | 🔓 Público | Busca un producto por nombre exacto | — |
| `POST` | `/api/productos` | 🔒 Protegido | Crea un producto nuevo (**Imagen obligatoria**) | `multipart/form-data` |
| `PUT` | `/api/productos/{id}` | 🔒 Protegido | Modifica producto reemplazando la imagen | `multipart/form-data` |
| `PUT` | `/api/productos/{id}` | 🔒 Protegido | Modifica producto conservando la imagen actual | `application/json` |
| `DELETE` | `/api/productos/{id}` | 🔒 Protegido | Elimina un producto por su ID | — |

---

#### 📌 Paginación en Listados (`GET`)
Todos los endpoints de listado retornan una página de Spring Data con tamaño predeterminado de **14 productos** ordenados por `id ASC`:
- **Query Params disponibles**:
  - `page`: Número de página (comienza en `0`, valor por defecto: `0`).
  - `size`: Cantidad de elementos por página (por defecto: `14`).
  - `sort`: Campo de ordenamiento (ej: `sort=precio,asc` o `sort=nombre,desc`).

##### Ejemplo de Respuesta Paginada (`Page<ProductoDto>`):
```json
{
  "content": [
    {
      "id": 10,
      "idCategoria": 1,
      "idSubCategoria": 2,
      "nombre": "Colero Scrunchie Seda",
      "precio": 2500.00,
      "precioDescuento": 1999.99,
      "descripcion": "Colero scrunchie de seda suave para el pelo",
      "imgUrl": "https://media.accesoriosvert.online/8f5e1823-colero-seda.jpg",
      "cantidad": 30
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 14,
    "sort": {
      "sorted": true,
      "empty": false
    }
  },
  "totalPages": 1,
  "totalElements": 1,
  "size": 14,
  "number": 0,
  "first": true,
  "last": true,
  "empty": false
}
```

---

#### 📌 Detalle de Peticiones de Creación y Edición:

#### ➤ Crear Producto (Imagen Obligatoria) `[🔒 Protegido]`
- **Método**: `POST`
- **Ruta**: `/api/productos`
- **Acceso**: 🔒 Protegido (Requiere JWT)
- **Headers**:
  - `Content-Type: multipart/form-data`
  - `Authorization: Bearer <token>` *(o cookie `access_token`)*

##### Estructura del `multipart/form-data`:

1. **Parte `producto`** (`Content-Type: application/json`):
```json
{
  "idCategoria": 1,
  "idSubCategoria": 2,
  "nombre": "Vincha Trenzada Rosa",
  "precio": 3200.50,
  "precioDescuento": 2800.00,
  "descripcion": "Vincha acolchada trenzada de raso color rosa pastel",
  "cantidad": 15
}
```

2. **Parte `imagen`** (Archivo binario):
- Archivo de imagen válido (`image/jpeg`, `image/png`, `image/webp`, etc.).
- El servidor sube el archivo a Cloudflare R2 con un identificador UUID único y asigna la URL pública al campo `imgUrl` en la base de datos.

##### Tabla de Campos del JSON `producto`:
| Campo | Tipo | Requerido | Validaciones y Reglas |
| :--- | :--- | :--- | :--- |
| `idCategoria` | `Integer` | **Sí** | `@NotNull`. Debe corresponder a una categoría existente. |
| `idSubCategoria` | `Integer` | **Sí** | `@NotNull`. Debe pertenecer a la categoría indicada. |
| `nombre` | `String` | **Sí** | `@NotBlank`. Nombre comercial del producto. |
| `precio` | `BigDecimal` | **Sí** | `@NotNull`, `@PositiveOrZero`. Precio regular del producto. |
| `precioDescuento` | `BigDecimal` | No | Opcional. Si se define, debe ser `>= 0` y **menor o igual al precio regular** (`precioDescuento <= precio`). |
| `descripcion` | `String` | No | Descripción extendida del producto. |
| `cantidad` | `Integer` | No | `@PositiveOrZero`. Stock disponible en unidades (mayor o igual a 0). |

##### Respuestas:
- **`201 Created`**: Header `Location: /api/productos/{id}` con la URL del nuevo recurso.
- **`400 Bad Request`**: Datos inválidos, `precioDescuento > precio`, o imagen no enviada.
- **`401 Unauthorized`**: Token JWT no provisto o inválido.
- **`404 Not Found`**: La categoría o subcategoría indicada no existe.

---

#### ➤ Modificar Producto con Nueva Imagen (`multipart/form-data`) `[🔒 Protegido]`
Utiliza esta variante cuando el usuario sube un archivo nuevo para sustituir la foto actual.
- **Método**: `PUT`
- **Ruta**: `/api/productos/{id}`
- **Acceso**: 🔒 Protegido (Requiere JWT)
- **Headers**:
  - `Content-Type: multipart/form-data`
  - `Authorization: Bearer <token>` *(o cookie `access_token`)*

##### Partes del Formulario:
1. **Parte `producto`** (`Content-Type: application/json`):
```json
{
  "idCategoria": 1,
  "idSubCategoria": 2,
  "nombre": "Vincha Trenzada Rosa Vintage",
  "precio": 3500.00,
  "precioDescuento": null,
  "descripcion": "Vincha acolchada modelo vintage renovado",
  "cantidad": 20
}
```
2. **Parte `imagen`** (Archivo binario, opcional en `PUT`):
- Archivo con la nueva imagen a sustituir en Cloudflare R2.

##### Respuestas:
- **`204 No Content`**: Producto e imagen actualizados.
- **`400 Bad Request`**: Error en los datos o imagen.
- **`401 Unauthorized`**: Token JWT no provisto o inválido.
- **`404 Not Found`**: Producto no encontrado.

---

#### ➤ Modificar Producto sin Modificar Imagen (`application/json`) `[🔒 Protegido]`
Utiliza esta variante cuando solo se modifican datos textuales (nombre, precio, stock, descripción, categorías), preservando la imagen que ya estaba asociada.
- **Método**: `PUT`
- **Ruta**: `/api/productos/{id}`
- **Acceso**: 🔒 Protegido (Requiere JWT)
- **Headers**:
  - `Content-Type: application/json`
  - `Authorization: Bearer <token>` *(o cookie `access_token`)*

##### Body de la Petición:
```json
{
  "idCategoria": 1,
  "idSubCategoria": 2,
  "nombre": "Vincha Trenzada Rosa Vintage",
  "precio": 3500.00,
  "precioDescuento": 2990.00,
  "descripcion": "Descripción actualizada sin tocar la imagen",
  "cantidad": 25
}
```

##### Respuestas:
- **`204 No Content`**: Modificación realizada con éxito.
- **`400 Bad Request`**: Error en los campos validados.
- **`401 Unauthorized`**: Token JWT no provisto o inválido.
- **`404 Not Found`**: Producto no encontrado.

---

#### ➤ Eliminar Producto `[🔒 Protegido]`
- **Método**: `DELETE`
- **Ruta**: `/api/productos/{id}`
- **Acceso**: 🔒 Protegido (Requiere JWT)
- **Headers**:
  - `Authorization: Bearer <token>` *(o cookie `access_token`)*

##### Respuestas:
- **`204 No Content`**: Eliminado exitosamente.
- **`401 Unauthorized`**: Token JWT no provisto o inválido.
- **`404 Not Found`**: Producto no encontrado.

---

## 🛡️ Formato Estandarizado de Respuestas de Error

Todas las excepciones no controladas o fallas de validación son capturadas por `GlobalExceptionHandler` y retornadas con el siguiente esquema JSON unificado:

```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Error: El nombre de la categoría ya se encuentra en uso."
}
```

### Casos de Error Comunes:

#### 1. Validaciones de Atributos (`MethodArgumentNotValidException` - `400 Bad Request`)
Cuando uno o más campos no cumplen con las anotaciones de validación (`@NotNull`, `@NotBlank`, etc.), `message` contiene un diccionario con el desglose por campo:
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": {
    "nombre": "El nombre de la categoría no puede ser nulo ni estar vacío.",
    "precio": "El precio del producto debe ser mayor o igual a 0."
  }
}
```

#### 2. Reglas de Negocio en Procedimientos Almacenados (`SQLSTATE '45000'` - `400 Bad Request`)
Los Stored Procedures lanzan señales personalizadas que son interceptadas limpiamente:
```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Error: La cantidad de categorías enviadas no coincide con el total registrado."
}
```

#### 3. Recursos Inexistentes (`NotFoundException` - `404 Not Found`)
```json
{
  "errorCode": "NOT_FOUND",
  "message": "Producto no encontrado con id: 99"
}
```

#### 4. Tipos de `ErrorCode` soportados:
- `VALIDATION_ERROR` (HTTP 400)
- `NOT_FOUND` (HTTP 404)
- `UNAUTHENTICATED` (HTTP 401)
- `FORBIDDEN` (HTTP 403)
- `SERVER_ERROR` (HTTP 500)

---

## 📮 Colección de Postman

El repositorio incluye el archivo [`Vert_Catalogo.postman_collection.json`](./Vert_Catalogo.postman_collection.json) con todas las peticiones configuradas:

1. Abre **Postman** y presiona **Import**.
2. Arrastra o selecciona `Vert_Catalogo.postman_collection.json`.
3. La colección incluye scripts automáticos para:
   - Extraer la cookie `access_token` generada en `POST /api/auth/login` y guardarla automáticamente en la variable de colección `{{token}}`.
4. Variables preconfiguradas:
   - `base_url`: `http://localhost:8080`
   - `categoria_id`: `1`
   - `subcategoria_id`: `1`
   - `producto_id`: `1`
   - `token`: *(asignado automáticamente tras el login)*
