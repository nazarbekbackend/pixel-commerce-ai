package kg.dev.pixel_commerce_ai.controller;

import kg.dev.pixel_commerce_ai.dto.PurchaseRequest;
import kg.dev.pixel_commerce_ai.dto.PurchaseResponse;
import kg.dev.pixel_commerce_ai.entity.Purchase;
import kg.dev.pixel_commerce_ai.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@Valid @RequestBody PurchaseRequest request) {
        purchaseService.create(request);
    }

    @GetMapping
    public List<PurchaseResponse> findAll(){
        return purchaseService.findAll();
    }
}