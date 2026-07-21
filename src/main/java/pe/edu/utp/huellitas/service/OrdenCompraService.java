package pe.edu.utp.huellitas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.huellitas.exception.NegocioException;
import pe.edu.utp.huellitas.model.*;
import pe.edu.utp.huellitas.repository.OrdenCompraRepository;
import pe.edu.utp.huellitas.repository.DetalleOrdenCompraRepository;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdenCompraService {

    private final OrdenCompraRepository ordenCompraRepository;
    private final DetalleOrdenCompraRepository detalleOrdenCompraRepository;
    private final InventarioService inventarioService;

    public List<OrdenCompra> listarOrdenes() {
        return ordenCompraRepository.findAll();
    }

    public OrdenCompra obtenerPorId(Long id) {
        return ordenCompraRepository.findById(id)
                .orElseThrow(() -> new NegocioException("Orden de compra no encontrada"));
    }

    @Transactional
    public OrdenCompra guardar(OrdenCompra ordenCompra) {
        if (ordenCompra.getEstado() == null) {
            ordenCompra.setEstado(EstadoOrdenCompra.BORRADOR);
        }
        return ordenCompraRepository.save(ordenCompra);
    }

    @Transactional
    public void recibirOrdenCompra(Long id) {
        OrdenCompra ordenCompra = obtenerPorId(id);

        if (ordenCompra.getEstado() == EstadoOrdenCompra.RECIBIDA) {
            throw new NegocioException("La orden de compra ya ha sido recibida.");
        }
        
        if (ordenCompra.getEstado() == EstadoOrdenCompra.CANCELADA) {
            throw new NegocioException("No se puede recibir una orden de compra cancelada.");
        }

        // Ingresar el stock de cada detalle
        for (DetalleOrdenCompra detalle : ordenCompra.getDetalles()) {
            Integer cantidadARecibir = detalle.getCantidadSolicitada(); // Asumimos recepción completa por defecto
            
            if (detalle.getCantidadRecibida() != null && detalle.getCantidadRecibida() > 0) {
                 cantidadARecibir = detalle.getCantidadRecibida();
            } else {
                 detalle.setCantidadRecibida(cantidadARecibir);
            }

            inventarioService.incrementarStock(
                    detalle.getProducto().getId(), 
                    cantidadARecibir, 
                    OrigenMovimiento.COMPRA, 
                    ordenCompra.getId()
            );
        }

        ordenCompra.setEstado(EstadoOrdenCompra.RECIBIDA);
        ordenCompra.setFechaRecepcion(OffsetDateTime.now());
        ordenCompraRepository.save(ordenCompra);
    }
}
