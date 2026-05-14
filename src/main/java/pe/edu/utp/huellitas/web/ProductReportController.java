package pe.edu.utp.huellitas.web;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.*;
import pe.edu.utp.huellitas.model.Product; // ajuste según modelo actual
import pe.edu.utp.huellitas.service.ProductService; // usted puede tener un servicio similar

@RestController
@RequestMapping("/api/products")
public class ProductReportController {

    private final ProductService productService;

    public ProductReportController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/report")
    public ResponseEntity<Map<String,Object>> getProductReport(
            @RequestParam(required = false) String category) {

        List<Product> products = (category == null)
                ? productService.findAll()
                : productService.findByCategory(category);

        // Mapear a DTO simple y calcular totales
        List<Map<String,Object>> items = new ArrayList<>();
        int totalStock = 0;
        double totalValue = 0;
        for (Product p : products) {
            Map<String,Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("name", p.getName());
            m.put("category", p.getCategory());
            m.put("stock", p.getStock());
            m.put("price", p.getPrice());
            items.add(m);
            totalStock += p.getStock();
            totalValue += p.getStock() * p.getPrice();
        }

        Map<String,Object> response = new HashMap<>();
        response.put("items", items);
        response.put("totalStock", totalStock);
        response.put("totalValue", totalValue);
        return ResponseEntity.ok(response);
    }

    // Endpoint para exportar CSV (ejemplo básico)
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv() {
        List<Product> products = productService.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("id,name,category,stock,price\n");
        for (Product p : products) {
            sb.append(p.getId()).append(",")
              .append(p.getName()).append(",")
              .append(p.getCategory()).append(",")
              .append(p.getStock()).append(",")
              .append(p.getPrice()).append("\n");
        }
        byte[] csv = sb.toString().getBytes();
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=products-report.csv")
            .body(csv);
    }
}