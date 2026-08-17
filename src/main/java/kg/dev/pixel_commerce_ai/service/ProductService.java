package kg.dev.pixel_commerce_ai.service;

import kg.dev.pixel_commerce_ai.dto.ProductRequest;
import kg.dev.pixel_commerce_ai.dto.ProductResponse;
import kg.dev.pixel_commerce_ai.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    ProductResponse create(ProductRequest request);
    List<ProductResponse> findAll();
    Optional<ProductResponse> findById(Long id);
    ProductResponse update(Long id, ProductRequest request);
    void delete (Long id);
}
