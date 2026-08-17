package kg.dev.pixel_commerce_ai.controller;

import kg.dev.pixel_commerce_ai.entity.Supplier;
import kg.dev.pixel_commerce_ai.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    public Supplier create(@RequestBody Supplier supplier) {
        return supplierService.create(supplier);
    }

    @GetMapping
    public List<Supplier> findAll() {
        return supplierService.findAll();
    }
}