# Guía Consolidada de Desarrollo - Huellitas v3

Este documento unifica el estado del proyecto, las reglas técnicas globales y las tareas específicas que cada rol debe asumir a partir de ahora. Su objetivo es evitar confusiones y servir como mapa exacto para el sprint.

---

## 1. Estado Actual: ¿Qué YA ESTÁ HECHO? (Núcleo - Persona 1)

El trabajo fundacional y la arquitectura base están **100% terminados**. Nadie debe preocuparse por programar las siguientes funcionalidades, ya que operan automáticamente:

- ✅ **Base de Datos y Flyway:** El esquema V1 ya está aplicado. Hibernate está en `validate`, por lo que el sistema no arranca si se desincroniza. *(Si alguien necesita modificar tablas, debe crear un archivo `V3__modificacion.sql` en Flyway).*
- ✅ **Auditoría JPA Automática:** Todos los modelos ya heredan de `Auditable` o tienen `@EntityListeners`. **NUNCA** seteen `creadoEn` o `creadoPor` a mano en los servicios; Spring Boot lo hace solo con el usuario logueado.
- ✅ **Manejo Global de Excepciones:** No hagan `try/catch` locales en sus servicios. Solo hagan `throw new NegocioException("Mensaje")` y el sistema automáticamente interceptará el error y mostrará una alerta visual roja en la interfaz.
- ✅ **Seguridad y Accesos:** 
  - Rutas protegidas por roles en `SecurityConfig`. (Aun así, agreguen `@PreAuthorize` en sus Controllers por seguridad).
  - Bloqueo de cuentas por 3 intentos fallidos y reseteo automático operativos.
  - Bloqueo de sesiones concurrentes (`maximumSessions(1)`).
  - Generación de contraseñas seguras automáticas de 9 caracteres y validación de 8 al cambiarlas.
- ✅ **Baja Lógica y Bloqueos Críticos:** `PersonalService` y `PropietarioService` ya bloquean eliminaciones destructivas que rompan el historial.

---

## 2. Reglas Técnicas Obligatorias para TODO el Equipo

1. **Acoplamiento de Servicios:** Un módulo solo puede llamar al `Service` de otro módulo, NUNCA usar el `Repository` ajeno ni editar sus entidades directamente.
2. **Impresiones (Reportes):** No usen librerías PDF externas. Construyan una vista normal en Thymeleaf, oculten los botones y menú lateral usando `@media print` en CSS, e invoquen `window.print()` en Javascript.
3. **Controladores:** Todas las operaciones de escritura (crear, editar, eliminar) deben usar `@PostMapping`. Thymeleaf inyectará el token CSRF automáticamente.

---

## 3. Tareas Pendientes: ¿Qué DEBE SEGUIR CADA ROL?

### 👤 Persona 2: Clientes y Pacientes
- **Paciente:** Adaptar los formularios y vistas a los nuevos campos (`estado`, `alergias`, `esterilizado`, `fechaNacimientoEstimada`). 
- **Validaciones Frontales:** 
  - DNI estricto (bloqueo si se duplica).
  - Alerta blanda (warning visual, no bloqueante) si se registra un propietario con nombre/teléfono similar, o pacientes con mismo nombre en el mismo propietario.
- **Eliminación Protegida (Paciente):** Replicar lo que se hizo en `PropietarioService`; impedir el borrado físico de un `Paciente` si este tiene historial clínico o citas, cambiando su estado a `FALLECIDO` o `INACTIVO`.

### 📦 Persona 3: Inventario y Compras (¡Crítico!)
- **InventarioService:** **[TAREA MÁS IMPORTANTE]** Crear esta clase para centralizar todas las mutaciones de stock (`incrementarStock` y `descontarStock`). 
- **Movimiento de Stock:** Cada vez que el stock cambie, registrar obligatoriamente una fila en `movimiento_stock`.
- **CRUD Base:** Adaptar Producto, Servicio y Proveedor al nuevo esquema V1.
- **Concurrencia:** La tabla `producto` tiene *Optimistic Locking* (`version`). Si el `InventarioService` atrapa un `OptimisticLockingFailureException`, relanzar un mensaje amigable indicando que otro usuario modificó el stock simultáneamente.

### 📅 Persona 4: Agenda
- **Validación de Solapamiento:** Ajustar `CitaService.guardar()` para evitar que se agende una cita si el veterinario ya tiene otra en ese mismo bloque de tiempo (considerar `duracionMinutos`).
- **Estados Estrictos:** Asegurar que una cita no pase a estado `COMPLETADA` sin que exista una `HistoriaClinica` atada a ella.

### 🩺 Persona 5: Atención Clínica
- **Ajuste de Modelos:** Integrar Historia Clínica, Vacunas y Recetas al esquema V1 (la mayoría del código previo es reusable).
- **Integración Inventario:** En el Detalle de Receta, enlazar de forma opcional los medicamentos recomendados con IDs de la tabla `Producto` (para facilitar el trabajo a Ventas).
- **Impresión:** Crear las vistas imprimibles para Recetas e Historia Clínica.

### 💰 Persona 6: Ventas y Facturación
- **Dependencia de Inventario:** NUNCA descontar stock directamente; inyectar y usar el `InventarioService` provisto por la Persona 3 (aplica tanto para vender como para anular).
- **Venta Multilínea Mixta:** Refactorizar `VentaService.registrarVentaMultilinea()` para que el `DetalleVenta` acepte tanto `producto_id` como `servicio_id`. (El SQL ya tiene un `CHECK` que fuerza a que una línea sea producto O servicio, nunca ambos).
- **Impresión:** Crear la vista de boleta imprimible.
