# 🎮 FakeSteam

Tienda de juegos estilo Steam: una aplicación **full-stack** con backend **Spring Boot + PostgreSQL (Neon)** y frontend **React + Vite**. Permite registrar usuarios, iniciar sesión y administrar (crear, editar, eliminar, buscar) juegos **con imagen guardada en la misma base de datos**.

**Desplegada en Render** (backend) + Render Static Site (frontend).

---

## 📁 Estructura del proyecto

```
FakeSteam/
├── .gitignore                  # Ignora .env (credenciales), builds e IDEs
├── BackEnd/                    # API REST — Spring Boot 3.4.4 (Java 17+)
│   ├── .env                    # TU base de datos (NO se sube a Git)
│   ├── mvnw / mvnw.cmd         # Maven Wrapper (no requiere Maven instalado)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/fakesteam/
│       │   ├── FakeSteamApplication.java      # Entrada + lee .env + formatea URL JDBC
│       │   ├── config/
│       │   │   └── GlobalExceptionHandler.java # Manejo global de errores
│       │   ├── controllers/
│       │   │   ├── UsuarioController.java     # Registro y login
│       │   │   └── JuegoController.java       # CRUD de juegos + imágenes
│       │   ├── entity/
│       │   │   ├── Usuario.java               # Tabla "usuarios"
│       │   │   └── Juego.java                 # Tabla "juegos" (img = bytea)
│       │   └── repository/
│       │       ├── UsuarioRepository.java
│       │       └── JuegoRepository.java
│       └── resources/application.properties   # Puerto y conexión a la BD
└── FrontEnd/                   # SPA — React 19 + Vite 6
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── main.jsx            # Punto de entrada de React
        ├── App.jsx             # Muestra Login o Juegos según sesión
        ├── AuthContext.jsx     # Contexto de autenticación (localStorage)
        ├── Login.jsx/.css      # Login + modal de registro
        ├── Juegos.jsx/.css     # Tienda: CRUD, búsqueda y borrado
        └── config.js           # URL del backend por entorno
```

---

## ✅ Prerrequisitos

| Requisito | Versión mínima | Versión probada |
|-----------|----------------|-----------------|
| Java (JDK) | 17 | 21 (Temurin) |
| Node.js | 18 | 24 |
| npm | 9 | 11 |

> **No necesitas instalar Maven**: el proyecto trae el Maven Wrapper (`mvnw`). Necesitas una cuenta en [Neon](https://neon.tech) (PostgreSQL en la nube, gratis) y una en [Render](https://render.com) para desplegar.

---

## 🚀 Cómo ejecutar el proyecto en local

Necesitas **dos terminales**: primero el backend, luego el frontend.

### 1. Backend (puerto 5000)

La conexión a la base de datos está en **`BackEnd/.env`** (ya configurada, y **no se sube a Git**):

```
DB_URL=postgresql://usuario:clave@host/neondb?sslmode=require&channel_binding=require
```

El backend lee ese archivo solo. Arranca:

```powershell
cd BackEnd
.\mvnw.cmd spring-boot:run        # Windows
```

```bash
cd BackEnd
./mvnw spring-boot:run            # Mac / Linux
```

Verás `Started FakeSteamApplication` y `Tomcat started on port 5000`. Las tablas se crean solas en la primera arrancada (`ddl-auto=update`).

### 2. Frontend (puerto 5173)

```powershell
cd FrontEnd
npm install       # solo la primera vez
npm run dev
```

Abre **http://localhost:5173**.

### 3. Detener

`Ctrl + C` en cada terminal.

---

## 🖼️ Sobre las imágenes

Las imágenes **se almacenan en la misma base de datos** (PostgreSQL), en la columna `img` de la tabla `juegos` de tipo `bytea` (binario). No necesitas S3 ni disco externo.

- `POST /juegos/crear` y `PUT /juegos/editar/{id}` aceptan un archivo (`multipart/form-data`).
- El backend las guarda como binario; al listar, JSON la trae en **base64** (texto).
- `GET /juegos/imagen/{id}` devuelve la imagen en crudo para mostrarla en el `<img>`.

> ⚠️ Límite: un registro de PostgreSQL pesa como máximo 16 MB, así que sirve para carátulas/juegos pequeños, no para archivos gigantes.

---

## 🌐 Despliegue en Render

El proyecto ya está preparado para Render tal cual está (acepta variables de entorno y el puerto `$PORT` que Render asigna).

### Paso 1 — Sube el proyecto a GitHub

```powershell
git add .
git commit -m "FakeSteam listo para Render"
git push
```

> Verás que `.env` **no** aparece en `git status`: las credenciales quedan protegidas por `.gitignore`.

### Paso 2 — Backend (Render Web Service)

1. En [Render](https://dashboard.render.com) → **New +** → **Web Service** → conecta tu repo de GitHub.
2. Configura:

| Campo | Valor |
|-------|-------|
| Name | `fakesteam-api` (o el que quieras) |
| Root Directory | `BackEnd` |
| Runtime | `Java` |
| Build Command | `mvn clean package` |
| Start Command | `java -jar target/FakeSteam-0.0.1-SNAPSHOT.jar` |
| Instance Type | **Free** |

3. En **Environment → Key/Value Secrets** agrega:

| Key | Value |
|-----|-------|
| `DB_URL` | Tu misma URL de Neon (la de `BackEnd/.env`, tal cual `postgresql://...`) |

4. **Create Web Service**. El primer deploy tarda unos minutos. Al final tendrás una URL tipo `https://fakesteam-api.onrender.com`.

> ⚠️ **Free tier**: el servicio se duerme tras ~15 min sin uso; el primer request tarda ~50 s en despertarlo. Es normal.

### Paso 3 — Frontend (Render Static Site)

1. **New +** → **Static Site** → mismo repo.
2. Configura:

| Campo | Valor |
|-------|-------|
| Name | `fakesteam-web` |
| Root Directory | `FrontEnd` |
| Build Command | `npm install && npm run build` |
| Publish Directory | `dist` |

3. En **Environment** agrega la URL del backend:

| Key | Value |
|-----|-------|
| `VITE_BACKEND_URL` | `https://fakesteam-api.onrender.com` (tu URL real del paso 2) |

4. **Create Static Site**. Queda en `https://fakesteam-web.onrender.com` — comparte esa URL y cualquiera podrá registrarse, entrar y ver los juegos.

> El frontend usa `VITE_BACKEND_URL` en producción; si no existe, usa la URL por defecto de AWS. En local siempre usa `http://localhost:5000`.

---

## ⚙️ Configuración

### Backend — `BackEnd/src/main/resources/application.properties`

| Propiedad | Descripción |
|-----------|-------------|
| `server.port` | `${PORT:5000}` — Render asigna `$PORT`; en local usa 5000 |
| `spring.datasource.url` | `${DB_URL}` — de `.env` o del entorno (Render) |
| `spring.datasource.username/password` | Se extraen solos del propio URL de Neon |
| `spring.jpa.hibernate.ddl-auto` | `update` — crea/actualiza las tablas automáticamente |

`FakeSteamApplication` convierte el formato de Neon (`postgresql://user:clave@host/db`) al formato JDBC (`jdbc:postgresql://host/db` + usuario/clave aparte) automáticamente, así que puedes pegar la URL de Neon tal cual donde se pida `DB_URL`.

### Frontend — `FrontEnd/src/config.js`

```js
const BACKEND_URLS = {
  development: "http://localhost:5000",   // npm run dev
  production:  import.meta.env.VITE_BACKEND_URL || "https://c9my416xy0.execute-api.us-east-1.amazonaws.com",
};
```

---

## 📦 Build de producción (manual)

```powershell
# Frontend → carpeta dist/
cd FrontEnd
npm run build
npm run preview

# Backend → JAR ejecutable
cd BackEnd
.\mvnw.cmd clean package
java -jar target\FakeSteam-0.0.1-SNAPSHOT.jar
```

---

## 🔌 API REST

Base local: `http://localhost:5000` — CORS abierto a `*`.

### Usuarios

| Método | Ruta | Descripción | Body |
|--------|------|-------------|------|
| `POST` | `/usuarios` | Registrar usuario | JSON `{ nombre, correo, contraseña }` |
| `POST` | `/usuarios/login` | Iniciar sesión (devuelve el usuario) | JSON `{ correo, contraseña }` |
| `PUT` | `/usuarios/{id}` | Actualizar usuario | JSON completo con `id` |

### Juegos

| Método | Ruta | Descripción | Body |
|--------|------|-------------|------|
| `GET` | `/juegos` | Listar juegos (con imagen en base64) | — |
| `POST` | `/juegos/crear` | Crear juego | `multipart/form-data`: `nombre`, `precio`, `categoria`, `img` (opcional) |
| `PUT` | `/juegos/editar/{id}` | Editar juego | `multipart/form-data` (sin `img` = conserva la anterior) |
| `DELETE` | `/juegos/eliminar/{id}` | Eliminar juego (204) | — |
| `GET` | `/juegos/imagen/{id}` | Descargar la imagen en crudo | — |

### Ejemplos con curl (funciona bien con acentos/ñ)

```powershell
# Registrar usuario
$body = '{"nombre":"Josue","correo":"josue@test.com","contrase\u00f1a":"123"}'
$body | Out-File -Encoding ascii $env:TEMP\reg.json
curl.exe -X POST http://localhost:5000/usuarios -H "Content-Type: application/json" --data-binary "@$env:TEMP\reg.json"

# Login
$login = '{"correo":"josue@test.com","contrase\u00f1a":"123"}'
$login | Out-File -Encoding ascii $env:TEMP\login.json
curl.exe -X POST http://localhost:5000/usuarios/login -H "Content-Type: application/json" --data-binary "@$env:TEMP\login.json"

# Crear juego con imagen
curl.exe -X POST http://localhost:5000/juegos/crear -F "nombre=Hollow Knight" -F "precio=14.99" -F "categoria=Metroidvania" -F "img=@C:\ruta\caratula.png"

# Listar / borrar
curl.exe http://localhost:5000/juegos
curl.exe -X DELETE http://localhost:5000/juegos/eliminar/1
```

> 💡 En **PowerShell 5.1** los acentos y la ñ en `-Body` se envían mal codificados (causan error 404/500). Usa `curl.exe` con `\u00f1` como arriba, o PowerShell 7+. En el navegador y con el frontend no hay problema.

---

## 🗄️ Modelo de datos (PostgreSQL en Neon)

Las tablas las crea Hibernate solo al arrancar. En el panel de Neon (SQL Editor) puedes verlas:

### Tabla `usuarios`

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | bigserial | Autogenerado |
| `nombre` | text | Nombre del usuario |
| `correo` | text | Correo (usado para login) |
| `contraseña` | text | Contraseña |

### Tabla `juegos`

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | bigserial | Autogenerado |
| `nombre` | text | Nombre del juego |
| `precio` | double precision | Precio |
| `categoria` | text | Categoría |
| `img` | bytea | **Imagen guardada en la BD** |

---

## 🖥️ Cómo funciona el frontend

- **`AuthContext.jsx`**: la sesión se guarda en `localStorage` (persiste al recargar; no hay tokens).
- **`App.jsx`**: hay sesión → `<Juegos />`, no hay sesión → `<Login />`.
- **`Login.jsx`**: login + modal de registro vía `POST /usuarios`.
- **`Juegos.jsx`**: lista con búsqueda por nombre/categoría, crear/editar con imagen, borrado con confirmación y cerrar sesión.

---

## 🐛 Problemas conocidos

| Problema | Causa | Solución |
|----------|-------|----------|
| `Port 5000 was already in use` | Ya hay un backend corriendo (quizás una terminal tuya abierta) | Ciérrala con `Ctrl + C`, o termina el proceso: `taskkill /F /PID <pid>` (PowerShell como Administrador) |
| Registro/login devuelven **404/500** usando PowerShell 5.1 | La "ñ" y los acentos viajan mal codificados en `-Body` | Usa `curl.exe` con `\u00f1` (ver ejemplos) o PowerShell 7 |
| La app en Render tarda ~50 s en responder | Free tier dormido | Es normal; el primer request la despierta |
| `Cannot find module` en el frontend | Faltan dependencias | `npm install` en `FrontEnd/` |
| El frontend no conecta con el backend | Falta `VITE_BACKEND_URL` en Render o estás en local sin backend | Ver sección de despliegue / arranca el backend |
| Error de conexión a la BD en local | `BackEnd/.env` mal o sin conexión a internet | Verifica que la URL de Neon esté completa |

---

## 🔒 Notas de seguridad (mejoras pendientes)

Proyecto académico/demo; antes de producción real:

- **Contraseñas en texto plano** → usar BCrypt (Spring Security).
- **Sin JWT** → la sesión solo vive en el navegador.
- **CORS `origins = "*"`** → restringir al dominio del frontend.
- **Imágenes en la BD** → bien para carátulas; para archivos grandes usar GridFS/S3.
- **`.env` con credenciales** → ya está en `.gitignore`; no lo subas nunca ni lo compartas.

---

## 🧪 Verificación realizada

Flujo probado de punta a punta contra la base de datos Neon (18/10/2026):

- ✅ Registro de usuario → guardado en `usuarios`
- ✅ Login → devuelve el usuario
- ✅ Crear juego con imagen → imagen guardada en la BD y devuelta en base64
- ✅ Listar juegos
- ✅ Descargar imagen → byte a byte idéntica al archivo original
- ✅ Eliminar juego → 204

> Existe un usuario de prueba en tu base de datos: `prueba-jpa@test.com` / `123` (puedes ignorarlo o borrarlo desde el SQL Editor de Neon).

---

## 🛠️ Comandos rápidos de referencia

```powershell
# Arrancar todo (2 terminales)
cd BackEnd; .\mvnw.cmd spring-boot:run
cd FrontEnd; npm run dev

# URLs
# Frontend:  http://localhost:5173
# Backend:   http://localhost:5000/juegos

# Build
cd FrontEnd; npm run build
cd BackEnd; .\mvnw.cmd clean package
```
