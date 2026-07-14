package pe.edu.utp.huellitas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActividadDTO {

    private OffsetDateTime fecha;

    // PACIENTE, CITA o VENTA
    private String tipo;

    // Texto que verá el usuario
    private String descripcion;

    // Icono FontAwesome
    private String icono;

}