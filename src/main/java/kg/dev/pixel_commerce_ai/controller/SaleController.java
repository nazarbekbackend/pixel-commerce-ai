package kg.dev.pixel_commerce_ai.controller;

import kg.dev.pixel_commerce_ai.dto.SaleRequest;
import kg.dev.pixel_commerce_ai.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@Valid @RequestBody SaleRequest request) {
        saleService.create(request);
    }
}