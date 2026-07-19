# 🐾 Huellitas — Sistema de Gestión de Clínica Veterinaria

**Stack:** Java 21 · Spring Boot 4.0.7 · Spring Security · Spring Data JPA · Thymeleaf · PostgreSQL (Neon)

> [!IMPORTANT]
> **ATENCIÓN EQUIPO DE DESARROLLO:** La Fase 1 (Arquitectura Base y Núcleo) ha sido **COMPLETADA**. Para ver exactamente qué tareas le corresponden a cada rol (Persona 2 a Persona 6) a partir de este punto, por favor consulten la **[Guía Consolidada de Desarrollo](guia-equipo-consolidada.md)** ubicada en la raíz de este repositorio.

---

## 📋 Tabla de Contenidos
1. [Configuración inicial](#configuración-inicial)
2. [Base de datos](#base-de-datos)
3. [Usuarios iniciales](#usuarios-iniciales)
4. [Estructura del proyecto](#estructura-del-proyecto)
5. [Permisos por rol](#permisos-por-rol)
6. [Módulos pendientes — TODOs del equipo](#módulos-pendientes)
7. [Convenciones y buenas prácticas](#convenciones)

---

## ⚙️ Configuración inicial

### 1. Variables de entorno
Crea un archivo `.env` en la raíz del proyecto (no subir al repositorio — ya está en `.gitignore`):
```
DB_URL=jdbc:postgresql://<host>/<database>?sslmode=require
DB_USER=<usuario>
DB_PASS=<contraseña>
```

### 2. Base de datos
Ejecutar el esquema completo en tu instancia PostgreSQL:
```bash
# Instalación desde cero (entorno de desarrollo)
psql -U <usuario> -d <database> -f schema-v3.sql

# Migración (si ya tienes v2 instalada)
# Ver la sección "MIGRACIÓN v2 → v3" al final del schema-v3.sql
```

> ⚠️ Requiere la extensión `pgcrypto` para el hasheo BCrypt en la semilla de datos.
> Se instala automáticamente con `CREATE EXTENSION IF NOT EXISTS pgcrypto;`

### 3. Ejecutar el proyecto
```bash
mvn spring-boot:run
# Acceder en: http://localhost:8080
```

---

## 🗄️ Base de Datos

**Schema versión:** v3.0 (Aplicado automáticamente vía Flyway)
**Archivos base:** `src/main/resources/db/migration/V1__esquema_inicial.sql`

> **Nota:** Hibernate está configurado en `validate`. Si necesitan alterar el esquema, creen obligatoriamente un nuevo script de migración en Flyway (`V3__...sql`).

---

## 👥 Usuarios iniciales

**Contraseña de todos:** Las contraseñas ahora son auto-generadas. Revisen las credenciales temporales generadas en los logs o usen una de prueba si corrieron el script de la V2. 

| Usuario | Rol | Código |
|---|---|---|
| `admin1` | ADMINISTRADOR | C000001 |
| `admin2` | ADMINISTRADOR | C000002 |
| `recep1` | RECEPCION | C000003 |
| `recep2` | RECEPCION | C000004 |
| `vet1` — `vet8` | VETERINARIO | C000005 — C000012 |

> 🔒 **El equipo debe cambiar las contraseñas al primer acceso.** El sistema bloquea cuentas tras 3 intentos fallidos por 10 minutos.

---

## 🗂️ Estructura del proyecto

```
src/main/java/pe/edu/utp/huellitas/
│
├── config/
│   └── SecurityConfig.java          ✅ Configurado con maximumSessions(1) y permisos RBAC
│
├── exception/
│   ├── GlobalExceptionHandler.java  ✅ Único punto de manejo de errores
│   └── NegocioException.java        ✅ Excepción base a lanzar desde los servicios
│
├── audit/
│   ├── Auditable.java               ✅ Todos los modelos heredan de aquí (o usan @EntityListeners)
│   └── AuditorAwareImpl.java        ✅ Registra automáticamente el usuario logueado
│
├── controller/
│   ├── CitaController.java          ✅ Implementado
│   ├── PacienteController.java      ✅ Implementado
│   ├── PersonalController.java      ✅ Implementado
│   ├── PropietarioController.java   ✅ Implementado
│   ├── ProductoController.java      ✅ Implementado
│   ├── ProveedorController.java     ✅ Implementado
│   ├── ServicioController.java      ✅ Implementado
│   ├── VentaController.java         ✅ Implementado (módulo básico)
│   ├── HistoriaClinicaController.java  🚧 Scaffold listo — pendiente implementar
│   ├── VacunaController.java           🚧 Scaffold listo — pendiente implementar
│   ├── RecetaController.java           🚧 Scaffold listo — pendiente implementar
│   └── SolicitudMaterialController.java 🚧 Scaffold listo — pendiente implementar
│
└── repository/
    └── (todos los repositorios base están listos)
```

---

## 🔐 Permisos por Rol

| Módulo / Acción | ADMIN | RECEPCION | VET |
|---|:---:|:---:|:---:|
| Dashboard | ✅ | ✅ | ✅ |
| Propietarios — ver/crear/editar | ✅ | ✅ | ✅ |
| Propietarios — eliminar | ✅ | ❌ | ❌ |
| Pacientes — ver/crear/editar | ✅ | ✅ | ✅ |
| Pacientes — eliminar | ✅ | ❌ | ❌ |
| Citas — ver | ✅ | ✅ | ✅ |
| Citas — crear/editar | ✅ | ✅ | ❌ |
| Citas — cancelar | ✅ | ✅ | ❌ |
| Citas — eliminar | ✅ | ❌ | ❌ |
| Historia Clínica | ✅ | ❌ | ✅ |
| Vacunas | ✅ | ❌ | ✅ |
| Recetas | ✅ | ❌ | ✅ |
| Solicitudes — crear | ✅ | ❌ | ✅ |
| Solicitudes — aprobar/rechazar/entregar | ✅ | ❌ | ❌ |
| Ventas | ✅ | ✅ | ❌ |
| Inventario — ver | ✅ | ✅ | ✅ |
| Inventario — crear/editar/eliminar | ✅ | ❌ | ❌ |
| Servicios — ver | ✅ | ✅ | ✅ |
| Servicios — crear/editar/eliminar | ✅ | ❌ | ❌ |
| Personal | ✅ | ❌ | ❌ |
| Proveedores | ✅ | ❌ | ❌ |

---

## 🚀 Módulos pendientes (Por Roles)

> **Ver detalles técnicos exhaustivos en `guia-equipo-consolidada.md`**

1. **Persona 2 (Clientes):** Vistas adaptadas a los nuevos campos, validaciones de DNI y eliminación protegida de `Paciente`.
2. **Persona 3 (Inventario):** `InventarioService` (centralización de stock) y registro en `movimiento_stock`.
3. **Persona 4 (Agenda):** Validar cruces/solapamiento de horarios en `CitaService`.
4. **Persona 5 (Médico):** Relación de Recetas con Inventario, e impresiones médicas.
5. **Persona 6 (Ventas):** Multilínea mixta (productos + servicios) y reabastecimiento de stock automático al anular (vía `InventarioService`).

---

## 📐 Convenciones

### Seguridad — REGLAS CRÍTICAS:
1. **Nunca usar GET para modificar datos** — todas las operaciones de guardar/cancelar/eliminar deben ser `POST`.
2. **Incluir CSRF token** en todos los forms, aunque con el form de Thymeleaf por defecto se incluye solo (siempre usar el `th:action`).
3. **Anotar con `@PreAuthorize`** en controllers para módulos restringidos.
4. **Manejo de Errores:** Usen `throw new NegocioException(...)` en sus servicios. NUNCA `try/catch` locales para lógica de negocio.

### Thymeleaf — Uso de roles en templates:
```html
<!-- Mostrar solo al administrador -->
<div sec:authorize="hasRole('ADMINISTRADOR')">...</div>

<!-- Mostrar a administrador y veterinario -->
<div sec:authorize="hasAnyRole('ADMINISTRADOR','VETERINARIO')">...</div>

<!-- Mostrar a cualquier usuario logueado -->
<div sec:authorize="isAuthenticated()">...</div>
```

---

*Si la vida te da limones, métele toda tu plata a Argenfifa y haz cheesecake*