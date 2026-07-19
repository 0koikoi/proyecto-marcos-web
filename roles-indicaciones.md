# Informe 2 — Roles e indicaciones específicas (sprint de 2 días)

Con dos días y 6 personas, el plan original de 6 semanas no aplica tal cual. Aquí va la versión comprimida: qué hace cada quien, en qué orden, y qué se corta si el tiempo no alcanza.

---

## 1. Roles

| # | Persona | Módulo | Depende de |
|---|---|---|---|
| 1 | **Tú (Persona 1)** | Núcleo + Dashboard + guía visual | Nadie — bloquea a todos |
| 2 | Persona 2 | Clientes y pacientes | Núcleo |
| 3 | Persona 3 | Inventario y compras | Núcleo |
| 4 | Persona 4 | Agenda | Núcleo, Clientes |
| 5 | Persona 5 | Atención clínica | Núcleo, Clientes, Agenda |
| 6 | Persona 6 | Ventas y facturación | Todos los anteriores |

---

## 2. Plan por horas (48 horas)

### Día 1 — mañana (bloqueante, todo el equipo espera esto)
**Tú, sola (Persona 1):**
- Base de datos nueva en Neon + `V1__esquema_inicial.sql` aplicado con Flyway.
- `Auditable` + `AuditorAware`.
- `NegocioException` + `GlobalExceptionHandler`.
- Login con bloqueo de intentos.
- Layout base + fragments compartidos (modal de confirmación, tabla vacía, menú por rol).

En paralelo, el resto del equipo revisa el código ya existente de su módulo (mucho ya está construido) y prepara qué le falta, sin comitear todavía contra el esquema nuevo.

### Día 1 — tarde (todos en paralelo, ya con la base lista)
- **Persona 2:** ajustes de `Paciente`/`Propietario` a los campos nuevos (`estado`, `alergias`, `esterilizado`) + validación de duplicado + bloqueo de eliminación.
- **Persona 3:** CRUD de producto/servicio/proveedor sobre el esquema nuevo + `InventarioService` unificado (el punto más importante: que Ventas y Solicitudes dejen de tocar stock por su cuenta).
- **Persona 4:** `Cita` con `duracionMinutos` + validación de solapamiento (la funcionalidad, no necesariamente el CRUD completo de horarios si el tiempo aprieta — ver prioridades abajo).
- **Persona 5:** ajustes menores de Historia/Vacuna/Receta al esquema nuevo (la mayoría del código ya existe y aplica casi igual).
- **Persona 6:** espera — su módulo depende de todos, empieza recién el Día 2.

### Día 2 — mañana
- **Persona 6:** Ventas con soporte de servicios + corrección del bug de permisos de anulación + reversión de stock vía `InventarioService`.
- **Persona 3:** si el flujo de Orden de Compra completo no alcanza, dejar al menos el registro manual de entrada de stock (`InventarioService.incrementarStock`) funcionando, aunque sea sin la pantalla de "enviar a proveedor".
- Todos: vista imprimible de su documento principal (boleta, receta, historia, orden de compra) — son rápidas, un `@media print` + botón.

### Día 2 — tarde
- Integración: correr el ciclo completo de punta a punta (propietario nuevo → paciente → cita → atención → receta → venta) entre todos, arreglando lo que rompa en la unión.
- Dashboard con el indicador de cada módulo (tú armas el layout, cada quien te pasa su consulta).
- Pulido visual con la paleta de colores de estado unificada.

---

## 3. Prioridades si el tiempo no alcanza para todo

**No se negocian (son las que corrigen bugs reales ya detectados):**
- Corrección del permiso de anulación de ventas.
- Bloqueo de eliminación de propietario/paciente con historial.
- Personal: desactivar, no borrar.
- `InventarioService` como único punto de mutación de stock.

**Se simplifican si falta tiempo (versión mínima, no la ideal):**
- Agenda: la validación de solapamiento importa más que la pantalla de horarios/excepciones completa — si hay que elegir, se prioriza que no se pueda doble-agendar, aunque el horario regular del veterinario quede como un dato fijo simple por ahora.
- Orden de compra: el estado `BORRADOR → ENVIADA → RECIBIDA` completo puede quedar como un botón simple de "registrar entrada de stock" si no alcanza el flujo completo con proveedor.
- Paginación de listados: queda documentada como pendiente conocido, no bloquea la entrega.

**Se cortan directamente en esta entrega (no se intentan):**
- Notificaciones automáticas.
- Facturación electrónica.
- PDF con librería dedicada (usar impresión del navegador).
- Ramas de base de datos individuales por persona.
- Rediseño responsive completo.
- Multi-sede.
- ArchUnit y Testcontainers (son buena práctica, pero no aportan a la entrega en 2 días).

---

## 4. Qué debe entregar cada quien como mínimo (checklist corto)

Todos:
- [ ] `@PreAuthorize` explícito en su controller (no solo la regla de `SecurityConfig`).
- [ ] Excepciones de negocio propias, nunca `try/catch` local — todo pasa por `GlobalExceptionHandler`.
- [ ] Su entidad extiende `Auditable` si aplica.
- [ ] Vista imprimible si su módulo genera un documento.

Persona 2 — Clientes/pacientes: campos nuevos aplicados + validación de duplicado + bloqueo de eliminación.
Persona 3 — Inventario: `InventarioService` unificado + al menos entrada manual de stock.
Persona 4 — Agenda: validación de solapamiento funcionando (es lo crítico).
Persona 5 — Clínico: enlace opcional de receta a producto (para que Ventas lo aproveche).
Persona 6 — Ventas: servicios cobrables + bug de anulación corregido.

---

## 5. Coordinación durante el sprint

- Todos comitean contra `develop`, con revisión de al menos otra persona antes de mergear — con solo 2 días, un merge roto bloquea a todo el equipo, así que la revisión rápida es más importante que nunca, no menos. Ignorar esto, es para que lo lea el equipo
- Cualquiera que necesite un cambio de esquema no contemplado en `V1__esquema_inicial.sql` escribe una migración nueva (`V2__...sql`) y avisa al resto en el canal del equipo antes de mergear, para que todos hagan `git pull` y levanten con el esquema actualizado antes de seguir probando. Ignorar esto, es para que lo lea el equipo.