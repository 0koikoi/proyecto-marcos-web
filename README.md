# 🐾 Huellitas — Sistema de Gestión de Clínica Veterinaria

**Stack:** Java 21 · Spring Boot 4.0.7 · Spring Security · Spring Data JPA · Thymeleaf · PostgreSQL (Neon)

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

**Schema versión:** v3.0
**Archivo:** `schema-v3.sql`

### Cambios en v3 respecto a v2:
| Tabla | Cambio |
|---|---|
| `personal` | + columna `email VARCHAR(150)` |
| `solicitud_material` | + columna `cantidad_entregada INT` |
| `vacuna` | + FK `historia_clinica_id` (trazabilidad clínica) |
| `personal` (semilla) | + campo email en todos los usuarios |

---

## 👥 Usuarios iniciales

**Contraseña de todos:** `Huellitas2025!`

| Usuario | Rol | Código |
|---|---|---|
| `admin1` | ADMINISTRADOR | C000001 |
| `admin2` | ADMINISTRADOR | C000002 |
| `recep1` | RECEPCION | C000003 |
| `recep2` | RECEPCION | C000004 |
| `vet1` — `vet8` | VETERINARIO | C000005 — C000012 |

> 🔒 **El equipo debe cambiar las contraseñas al primer acceso.**

---

## 🗂️ Estructura del proyecto

```
src/main/java/pe/edu/utp/huellitas/
│
├── config/
│   └── SecurityConfig.java          ← Mapa completo de permisos por rol
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
├── model/
│   ├── EstadoCita.java              ✅ Enum (PENDIENTE, EN_PROCESO, COMPLETADA, CANCELADA)
│   ├── EstadoSolicitud.java         ✅ Enum (PENDIENTE, APROBADA, RECHAZADA, ENTREGADA)
│   ├── EstadoVenta.java             ✅ Enum (PENDIENTE, PAGADA, ANULADA)
│   └── TipoPago.java                ✅ Enum (EFECTIVO, TARJETA, TRANSFERENCIA, MIXTO)
│
├── service/
│   ├── HistoriaClinicaService.java  🚧 Scaffold listo — métodos base implementados
│   ├── VacunaService.java           🚧 Scaffold listo — métodos base implementados
│   ├── RecetaService.java           🚧 Scaffold listo — métodos base implementados
│   └── SolicitudMaterialService.java 🚧 Scaffold listo — flujo completo implementado
│
└── repository/
    └── (todos los repositorios base están listos)

src/main/resources/templates/
├── historia/
│   ├── lista.html       🚧 Scaffold listo
│   ├── formulario.html  🚧 Scaffold listo
│   └── detalle.html     🚧 Scaffold listo
├── vacunas/
│   ├── lista.html       🚧 Scaffold listo
│   └── formulario.html  🚧 Scaffold listo
├── recetas/
│   ├── lista.html       🚧 Scaffold listo
│   ├── formulario.html  🚧 Scaffold listo
│   └── detalle.html     🚧 Scaffold listo (con CSS @media print)
└── solicitudes/
    ├── lista.html       🚧 Scaffold listo
    └── formulario.html  🚧 Scaffold listo
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

## 🚧 Módulos Pendientes (Planificación Equipo de 5)

Cada módulo tiene su **Service completamente implementado** con la lógica de negocio, y un **Controller scaffold** con todas las rutas definidas y documentadas.

### 👤 PERSONA 1 — Módulo de Ventas (Completar)
- **Archivos:** `VentaController.java`, `VentaService.java`, `ventas.html`, `formulario-venta.html`
- **Tareas:** Habilitar detalle de ventas multi-producto en el formulario usando JS y guardándolo correctamente con `VentaService`. Mostrar total en tiempo real y agregar tipo de pago.

### 👤 PERSONA 2 — Solicitudes de Material
- **Archivos:** `SolicitudMaterialController.java`, `solicitudes/lista.html`, `solicitudes/formulario.html`
- **Tareas:** Completar lógica del controller. Modales de aprobación/rechazo/entrega según rol (solo ADMIN aprueba). Usar badges de color para estados.

### 👤 PERSONA 3 — Dashboard Dinámico
- **Archivos:** `WebController.java`, `dashboard.html`
- **Tareas:** Reemplazar actividad reciente y próximas citas hardcodeadas por consultas reales a la BD. Crear tarjetas de estadísticas de ventas (para admin) y diferenciación de vistas por rol.

### 👤 PERSONA 4 — Historia Clínica, Recetas y Vacunas
- **Archivos:** `HistoriaClinicaController.java`, `RecetaController.java`, `VacunaController.java` y vistas asociadas.
- **Tareas:** Buscadores y filtros por paciente/fecha. Funcionalidad para exportar receta a PDF (`@media print`). Alertas visuales para vacunas por vencer (< 7 días).

### 👤 PERSONA 5 — UI/UX, Validaciones y Calidad
- **Archivos:** `style.css`, layout, vistas en general.
- **Tareas:** Integrar `personal.css` en `style.css`. Asegurar validaciones (`@NotBlank`, `@Email`) en todos los formularios con alertas. Agregar confirmaciones de eliminación (patrón de `personal.html`). Estandarizar diseño sin `<style>` tags.

---

## 📐 Convenciones

### Seguridad — REGLAS CRÍTICAS:
1. **Nunca usar GET para modificar datos** — todas las operaciones de guardar/cancelar/eliminar deben ser `POST`.
2. **Incluir CSRF token** en todos los forms:
   ```html
   <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}">
   ```
3. **Anotar con `@PreAuthorize`** en controllers para módulos restringidos.
4. **No inyectar repositorios en controllers** — solo inyectar servicios.

### Thymeleaf — Uso de roles en templates:
```html
<!-- Mostrar solo al administrador -->
<div sec:authorize="hasRole('ADMINISTRADOR')">...</div>

<!-- Mostrar a administrador y veterinario -->
<div sec:authorize="hasAnyRole('ADMINISTRADOR','VETERINARIO')">...</div>

<!-- Mostrar a cualquier usuario logueado -->
<div sec:authorize="isAuthenticated()">...</div>
```

### Obtener usuario autenticado en un Controller:
```java
// Opción 1: vía parámetro
@GetMapping("/mi-ruta")
public String miMetodo(Authentication authentication) {
    Personal usuario = (Personal) authentication.getPrincipal();
    // ...
}

// Opción 2: anotación
@GetMapping("/mi-ruta")
public String miMetodo(@AuthenticationPrincipal Personal usuario) {
    // ...
}
```

### Estructura de un Controller (patrón del proyecto):
```java
@Controller
@RequestMapping("/modulo")
@PreAuthorize("hasAnyRole('ADMINISTRADOR','ROL_NECESARIO')")
public class ModuloController {

    private final ModuloService service;

    public ModuloController(ModuloService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(Model model) { ... }

    @GetMapping("/nuevo")
    public String nuevo(Model model) { ... }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute Entidad entidad,
                          BindingResult result,
                          Model model,
                          RedirectAttributes redirectAttrs) { ... }

    @PostMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttrs) { ... }
}
```

---

*Sistema Huellitas — Equipo de Desarrollo | UTP*
