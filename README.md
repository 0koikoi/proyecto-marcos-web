# 🐾 Huellitas — Sistema de Gestión Veterinaria

## ⚡ Guía rápida para el equipo

### Requisitos previos (instalar una sola vez)
- [Java 21+](https://adoptium.net/) — verificar con `java -version`
- [Git](https://git-scm.com/)

---

### 1️⃣ Clonar el repositorio

```bash
git clone https://github.com/0koikoi/proyecto-marcos-web.git
cd proyecto-marcos-web
```

---

### 2️⃣ Configurar la base de datos

1. Copia el archivo de ejemplo:
   ```bash
   copy .env.example .env
   ```
2. Abre el archivo `.env` con cualquier editor de texto (Notepad, VS Code, etc.)
3. Cambia **solo** la última línea con la contraseña que te dio el líder del equipo:
   ```
   DB_PASS=PEGAR_CONTRASEÑA_AQUI
   ```
4. Guarda el archivo

> ⚠️ El archivo `.env` es **personal y privado**. Nunca lo subas a GitHub.

---

### 3️⃣ Ejecutar la aplicación

**Opción A — Doble clic (más fácil):**
> Haz doble clic en el archivo `iniciar.bat` en la raíz del proyecto

**Opción B — Terminal:**
```powershell
# Cargar variables de entorno (una vez por sesión)
Get-Content .env | ForEach-Object {
    if ($_ -match '^([^#].+?)=(.+)$') {
        [System.Environment]::SetEnvironmentVariable($matches[1], $matches[2], 'Process')
    }
}

# Arrancar la app
./mvnw spring-boot:run
```

---

### 4️⃣ Abrir en el navegador

Una vez que veas este mensaje en la terminal:
```
Started HuellitasApplication in X.XXX seconds
```

Abre tu navegador en: **http://localhost:8080**

---

### 🔄 Flujo de trabajo diario

```
1. git pull              ← traer últimos cambios del equipo
2. doble clic iniciar.bat ← arrancar la app
3. Trabajar...
4. git add .
5. git commit -m "mi cambio"
6. git push              ← subir tus cambios
```

---

### 🗄️ Base de datos

La base de datos está en la nube (**Neon PostgreSQL**). Todos comparten la misma BD automáticamente. No necesitas instalar PostgreSQL localmente.

| Dato | Valor |
|------|-------|
| Host | `ep-polished-mouse-acdkdc5n.sa-east-1.aws.neon.tech` |
| Base de datos | `neondb` |
| Usuario | `neondb_owner` |
| Contraseña | _Pedir al líder del equipo_ |

---

### ❓ Problemas comunes

| Error | Solución |
|-------|----------|
| `DB_URL no encontrado` | El archivo `.env` no existe o está mal ubicado |
| `Connection refused` | Verifica la contraseña en `.env` |
| `java: command not found` | Instala Java 21 y agrega al PATH |
| Puerto 8080 ocupado | Cierra otra instancia de la app o cambia el puerto |
