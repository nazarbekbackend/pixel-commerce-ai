package kg.dev.pixel_commerce_ai.controller;


import jakarta.validation.Valid;
import kg.dev.pixel_commerce_ai.dto.ProductRequest;
import kg.dev.pixel_commerce_ai.dto.ProductResponse;
import kg.dev.pixel_commerce_ai.exception.ProductNotFoundException;
import kg.dev.pixel_commerce_ai.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor

public class ProductController {

    private final ProductService productService;


    @PostMapping
    public ProductResponse creat(@Valid @RequestBody ProductRequest request){

        return productService.create(request);

    }

    @GetMapping
    public List<ProductResponse> findAll(){
        return productService.findAll();
    }

    @GetMapping("/{id}")
    public ProductResponse findById(@PathVariable Long id){
        return productService.findById(id).orElseThrow(()-> new ProductNotFoundException("Product not found"));
    }

    @PutMapping("/{id}")
    public ProductResponse update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request){

        return productService.update(id, request);
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){
        productService.delete(id);
    }
}
