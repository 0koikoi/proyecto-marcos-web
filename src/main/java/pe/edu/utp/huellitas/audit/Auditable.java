package pe.edu.utp.huellitas.audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import pe.edu.utp.huellitas.model.Personal;

import java.time.OffsetDateTime;

/**
 * Clase base de auditoría.
 *
 * Entidades que la extiendan obtienen automáticamente:
 *   - creado_en    → fecha de creación (inmutable)
 *   - creado_por   → usuario que creó el registro
 *   - actualizado_en → fecha de última modificación
 *   - actualizado_por → usuario que modificó el registro
 *
 * NOTA: Este patrón es para FUTURAS entidades nuevas.
 * Las entidades existentes (Personal, Propietario, etc.) ya tienen
 * estos campos mapeados directamente por coherencia con el esquema V1.
 *
 * Uso:
 *   @Entity
 *   public class MiEntidad extends Auditable { ... }
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable {

    @CreatedDate
    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por", updatable = false)
    private Personal creadoPor;

    @LastModifiedDate
    @Column(name = "actualizado_en")
    private OffsetDateTime actualizadoEn;

    @LastModifiedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actualizado_por")
    private Personal actualizadoPor;
}
