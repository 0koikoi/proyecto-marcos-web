# Informe 1 — Estado y estructura del proyecto: qué debe tener Huellitas v3

Este documento reúne todo lo que debe existir en el proyecto a partir de ahora: la división de módulos, qué contiene cada clase, las correcciones puntuales sobre el código actual, y la estructura técnica base.

---

## 1. Correcciones puntuales sobre el proyecto actual (aplicar primero, son rápidas)

Estas ya estaban en el código revisado y hay que corregirlas antes de seguir construyendo encima:

| Corrección | Dónde | Qué hacer |
|---|---|---|
| Recepción puede anular ventas | `SecurityConfig` + `VentaController` | Agregar `.requestMatchers("/ventas/*/anular").hasRole("ADMINISTRADOR")` **antes** de la regla general de `/ventas/**`, y un `@PreAuthorize("hasRole('ADMINISTRADOR')")` en el método del controller |
| No se puede cobrar servicios en una venta | `VentaService.registrarVentaMultilinea` | Extender para recibir también `servicioIds`/cantidades, generando líneas de `DetalleVenta` con `servicio_id` |
| Eliminar propietario no valida historia clínica | `PropietarioService.eliminar()` | Antes de `deleteById`, verificar que ninguno de sus pacientes tenga historia clínica/ventas/citas asociadas; si tiene, lanzar `EliminacionNoPermitidaException` |
| Eliminar personal es `DELETE` físico | `PersonalService.eliminar()` | Reemplazar por `desactivar()` → `activo = false`, nunca borrar de la tabla |
| Sin validación de solapamiento de citas | `CitaService.guardar()` | Validar horario del veterinario + ausencia de otra cita superpuesta antes de guardar |
| Stock mutado directamente en 2 servicios distintos | `VentaService`, `SolicitudMaterialService` | Ambos deben llamar a `InventarioService.descontarStock(...)` / `incrementarStock(...)`, nunca tocar `producto.stockActual` directo |
| Ningún listado usa paginación | Todos los repositorios/servicios de listado | Migrar `findAll()` a `Pageable`/`Page<T>` — puede quedar para después de la entrega si el tiempo no alcanza, pero debe quedar anotado como pendiente conocido |
| `maximumSessions(1)` no funciona del todo | `SecurityConfig` | Registrar el bean `HttpSessionEventPublisher` para que el `SessionRegistry` se entere de sesiones cerradas |

---

## 2. División de módulos

El sistema se organiza en 7 módulos, con dependencia en un solo sentido (un módulo de abajo nunca depende de uno de arriba):

```
Núcleo (Personal, Rol, Seguridad)
   └── Clientes y pacientes (Propietario, Paciente)
          └── Agenda (Cita, horarios)
                 └── Atención clínica (Historia, Vacunas, Recetas)
Inventario y compras (Producto, Servicio, Proveedor, Órdenes de compra) ── depende solo de Núcleo
   └── Ventas y facturación ── depende de Clientes, Agenda, Clínico e Inventario
Reportes y dashboard ── lee de todos, ninguno depende de él
```

esta estructura no influencia ni debe cambiar con respecto a la actual estructura del proyecto en cuanto a carpetas, solo es para tomar en consideración sobre el  modo de trabajo.

**Regla de acoplamiento:** un módulo solo llama al `Service` público de otro módulo, nunca a su `Repository` ni edita sus entidades directamente.

## 3. Qué debe contener cada módulo (clase por clase)

### Núcleo
- `Personal`, `Rol` (entidades ya existentes, con `intentosFallidos` y `bloqueadoHasta` agregados).
- `Auditable` (`@MappedSuperclass`) con `creadoEn`, `creadoPor`, `actualizadoEn`, `actualizadoPor` vía `@EnableJpaAuditing` + `AuditorAware<Long>` — todas las demás entidades del proyecto la extienden.
- `SecurityConfig`: RBAC por URL + bean `HttpSessionEventPublisher` + `UserDetailsChecker` para el bloqueo por intentos.
- Listeners de `AuthenticationFailureBadCredentialsEvent` / `AuthenticationSuccessEvent` para el conteo de intentos.
- `NegocioException` (clase base) y `GlobalExceptionHandler` (único punto de manejo de errores — ver Informe 3 para el detalle del patrón).
- Layout base (`layout.html`) + fragments compartidos (modal de confirmación, tabla vacía, menú lateral por rol).
- Dashboard con contenido distinto por rol.

### Clientes y pacientes
- `Propietario`, `Paciente` (con `estado`, `alergias`, `esterilizado`, `pesoReferencia`, `fechaNacimientoEstimada`).
- `PropietarioService` / `PacienteService` con: validación de duplicado blando (advertencia, no bloqueo), y bloqueo real de eliminación si tiene historial asociado.
- DTOs ya existían para estas entidades — mantenerlos.

### Inventario y compras
- `Producto` (con `version` para *optimistic locking*), `Servicio`, `Proveedor` (con `email` agregado).
- `OrdenCompra` / `DetalleOrdenCompra` con flujo `BORRADOR → ENVIADA → RECIBIDA/PARCIAL/CANCELADA`.
- `MovimientoStock` — registro de cada entrada/salida.
- `InventarioService` como único punto de mutación de stock.
- Vista imprimible de la orden de compra.

### Agenda
- `Cita` (con `duracionMinutos`), `HorarioAtencion`, `ExcepcionHorario`.
- `AgendaService.hayDisponibilidad(veterinarioId, fechaHora, duracion)`: valida horario regular + excepciones + solapamiento.
- Transición de estado: no se puede pasar a `COMPLETADA` sin una `HistoriaClinica` asociada.

### Atención clínica
- `HistoriaClinica`, `Vacuna`, `Receta`, `DetalleReceta` (con `productoId` opcional para enlazar al inventario).
- `RecetaService.obtenerMedicamentosVendibles(recetaId)` para que Ventas los precargue.
- Vistas imprimibles de historia clínica y receta.

### Ventas y facturación
- `Venta`, `DetalleVenta` (con soporte real de servicios, no solo productos).
- Anulación restringida a ADMINISTRADOR con doble capa de seguridad.
- Reversión de stock vía `InventarioService`.
- Vista imprimible tipo boleta.

### Reportes y dashboard
- Sin entidades propias. Cada módulo aporta su propio indicador; se integra en el dashboard de Núcleo.

---

## 4. Base técnica que ya debe estar lista antes de que el resto empiece a construir

1. Base de datos nueva y limpia en Neon (`huellitas-v3`), con `V1__esquema_inicial.sql` ya aplicado vía Flyway.
2. `ddl-auto=validate` (no `update`) en `application.properties`.
3. `Auditable` + `AuditorAware` funcionando.
4. `NegocioException` + `GlobalExceptionHandler` como único patrón de manejo de errores en todo el proyecto.
5. Layout base + fragments compartidos, para que cada módulo solo agregue su contenido sin rediseñar estructura.

Esto es responsabilidad de Núcleo y bloquea a los demás — debe quedar listo el primer día.