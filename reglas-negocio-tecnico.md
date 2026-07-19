# Informe 3 — Reglas de negocio e información técnica

---

## 1. Reglas de negocio, por módulo

### Personal y seguridad
- Baja de personal = `activo = false`. Nunca `DELETE` físico (rompería historial de citas, ventas, recetas asociadas).
- Login: máximo 3 intentos fallidos → bloqueo de 10 minutos (`bloqueadoHasta`), reseteo del contador al iniciar sesión con éxito.
- Contraseña: mínimo 8 caracteres, aplicado tanto al crear usuario como al cambiarla (hoy solo se validaba al cambiar).

### Clientes y pacientes
- DNI único por propietario (control duro). Nombre + teléfono similar entre dos propietarios → advertencia blanda, no bloqueo.
- Paciente: mismo nombre + especie bajo el mismo propietario → advertencia blanda, no bloqueo (dos mascotas sí pueden compartir nombre legítimamente).
- Peso: sin rango fijo global — rango de referencia por especie, usado solo como advertencia (perro 0.5–90 kg, gato 0.5–15 kg, ave 0.01–5 kg, roedor 0.02–3 kg, reptil 0.01–20 kg).
- Edad: si no se conoce la fecha de nacimiento exacta (mascota adoptada/encontrada), se marca `fechaNacimientoEstimada = true` en vez de forzar un dato inventado.
- `pesoReferencia` del paciente se actualiza automáticamente desde la última `HistoriaClinica` registrada — nunca se edita a mano en el formulario de paciente.
- Un paciente o propietario con historia clínica, ventas o citas asociadas **no se puede eliminar**; se marca `FALLECIDO`/`INACTIVO` en el caso del paciente.

### Inventario y compras
- El stock nunca puede quedar negativo (`CHECK (stock_actual >= 0)` a nivel de base, y validación en `InventarioService` antes de descontar).
- Toda mutación de stock pasa por `InventarioService.descontarStock()` / `incrementarStock()` — ningún otro service escribe `producto.stockActual` directamente.
- Cada mutación de stock queda registrada en `movimiento_stock` con su origen (venta, solicitud, compra, ajuste manual).
- `Producto.version` (optimistic locking): si dos operaciones intentan modificar el mismo stock al mismo tiempo, la segunda debe fallar de forma controlada, no sobrescribir en silencio.
- Orden de compra: solo se incrementa stock al registrar la recepción real de mercadería, nunca al solo generar o enviar la orden.

### Agenda
- Una cita solo puede agendarse si cae dentro del horario regular del veterinario para ese día, no hay una excepción (vacaciones/licencia/día libre) activa en esa fecha, y no se solapa con otra cita del mismo veterinario o del mismo paciente.
- Transición de estado válida: `PENDIENTE → EN_PROCESO → COMPLETADA` o `→ CANCELADA`. No se puede pasar a `COMPLETADA` sin que exista una `HistoriaClinica` asociada a esa cita.

### Atención clínica
- Toda `Receta` cuelga de una `HistoriaClinica` válida.
- Un medicamento recetado puede (opcionalmente) enlazarse a un `Producto` del inventario — solo esos quedan disponibles para que Ventas los precargue automáticamente.

### Ventas y facturación
- Una venta puede incluir productos y/o servicios en la misma boleta (cada línea de `DetalleVenta` referencia exactamente uno de los dos, nunca ambos ni ninguno — regla forzada con `CHECK` a nivel de base).
- Solo ADMINISTRADOR puede anular una venta (doble capa: `@PreAuthorize` + regla de `SecurityConfig`, no solo una de las dos).
- Anular una venta revierte el stock de los productos vendidos, siempre vía `InventarioService`.
- Recomendado (a definir en equipo si el tiempo alcanza): límite de tiempo para anular una venta (ej. 24–48 horas desde la emisión), para evitar reversos de cobros muy antiguos sin control.

### Auditoría y visibilidad
- Toda entidad relevante extiende `Auditable` (`creadoEn`, `creadoPor`, `actualizadoEn`, `actualizadoPor`), poblado automáticamente vía `AuditorAware`, no a mano por cada developer.
- Definir explícitamente si un veterinario ve todo el historial clínico o solo el de los pacientes que él mismo atendió (queda como decisión de equipo antes de programar el filtro).

---

## 2. Información técnica

### Stack
| Capa | Herramienta |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot |
| Seguridad | Spring Security 6 |
| Persistencia | Spring Data JPA / Hibernate |
| Migraciones | Flyway |
| Base de datos | PostgreSQL en Neon (rama `huellitas-v3`) |
| Vistas | Thymeleaf + dialecto de seguridad + Java Time |
| UI | Bootstrap 5, FontAwesome, CSS corporativo propio |
| Utilidades | Lombok |
| Build | Maven |

### Configuración clave
```properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.jpa.hibernate.ddl-auto=validate
```
`validate` (no `update`): Hibernate solo compara las entidades contra lo que Flyway ya aplicó — si falta una migración para un campo nuevo, el proyecto no arranca. Evita que el esquema se desincronice entre los 6 ambientes de trabajo.

### Patrón de manejo de errores (único en todo el proyecto)
```java
@MappedSuperclass
public abstract class NegocioException extends RuntimeException { ... }
// Subtipos: StockInsuficienteException, SolapamientoCitaException,
// CuentaBloqueadaException, EliminacionNoPermitidaException, etc.

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NegocioException.class)
    public String manejarNegocio(NegocioException ex, RedirectAttributes ra, HttpServletRequest req) {
        ra.addFlashAttribute("error", ex.getMessage());
        return "redirect:" + (req.getHeader("Referer") != null ? req.getHeader("Referer") : "/dashboard");
    }

    @ExceptionHandler(Exception.class)
    public String manejarInesperado(Exception ex, Model model) {
        model.addAttribute("mensaje", "Ocurrió un error inesperado. Contacta al administrador.");
        return "error-generico";
    }
}
```
Los servicios solo **lanzan** la excepción que corresponda; nunca hacen `try/catch` propio. Esto reemplaza la mezcla actual de dos patrones distintos por uno solo.

### Seguridad — bloqueo de intentos de acceso
- `personal.intentos_fallidos` (int) y `personal.bloqueado_hasta` (timestamp).
- `UserDetailsChecker` en el `DaoAuthenticationProvider`: lanza `LockedException` si `bloqueadoHasta` está en el futuro.
- Listener de `AuthenticationFailureBadCredentialsEvent`: incrementa el contador; al llegar a 3, fija `bloqueadoHasta = now() + 10 min`.
- Listener de `AuthenticationSuccessEvent`: resetea el contador y limpia `bloqueadoHasta`.

### Reportes imprimibles
Sin librería de PDF por ahora: vista Thymeleaf dedicada por documento (`/ventas/{id}/imprimir`, `/recetas/{id}/imprimir`, etc.) con `@media print` que oculta menú/botones, y `window.print()` — el navegador ya permite "Guardar como PDF" si el usuario lo necesita.

### Matriz de permisos (referencia)

| Módulo | ADMINISTRADOR | RECEPCION | VETERINARIO |
|---|:---:|:---:|:---:|
| Dashboard | ✅ | ✅ | ✅ |
| Clientes/Pacientes (lectura) | ✅ | ✅ | ✅ |
| Clientes/Pacientes (crear/editar) | ✅ | ✅ | ✅ |
| Citas (lectura) | ✅ | ✅ | ✅ |
| Citas (crear/editar/cancelar) | ✅ | ✅ | ❌ |
| Historia/Vacunas/Recetas | ✅ | ❌ | ✅ |
| Ventas (registrar) | ✅ | ✅ | ❌ |
| Ventas (anular) | ✅ | ❌ | ❌ |
| Inventario (lectura) | ✅ | ✅ | ✅ |
| Inventario (crear/editar) | ✅ | ❌ | ❌ |
| Compras/Proveedores | ✅ | ❌ | ❌ |
| Personal | ✅ | ❌ | ❌ |
| Cualquier eliminación | ✅ | ❌ | ❌ |

### Esquema de base de datos
El script completo (`V1__esquema_inicial.sql`) ya fue entregado en el mensaje anterior, listo para aplicarse con Flyway contra la rama nueva de Neon.

Este archivo es una referencia de lo que se debe cumplir en cuanto a reglas de negocio y patrones de arquitectura, no es un archivo que se debe ejecutar. Su fin es servir de guia para cualquier desarrollador que quiera entender mejor la logica del proyecto y evitar posibles errores.