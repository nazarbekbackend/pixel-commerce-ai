package kg.dev.pixel_commerce_ai.service;

import kg.dev.pixel_commerce_ai.dto.ProductRequest;
import kg.dev.pixel_commerce_ai.dto.ProductResponse;
import kg.dev.pixel_commerce_ai.entity.Product;
import kg.dev.pixel_commerce_ai.exception.ProductNotFoundException;
import kg.dev.pixel_commerce_ai.mapper.ProductMapper;
import kg.dev.pixel_commerce_ai.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor

public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;


    @Override
    public ProductResponse create(ProductRequest request) {

        Product product =productMapper.toEntity(request);

        Product savedProduct = productRepository.save(product);
        return productMapper.toResponse(savedProduct);
    }

    @Override
    public List<ProductResponse> findAll() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    public Optional<ProductResponse> findById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toResponse);
    }

    @Override
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(()-> new ProductNotFoundException("Product not found"));

        product.setBrand(request.getBrand());
        product.setModel(request.getModel());
        product.setStorage(request.getStorage());
        product.setSalePrice(request.getSalePrice());
        product.setPurchasePrice(request.getPurchasePrice());
        product.setPurchaseCurrency(request.getPurchaseCurrency());
        product.setCondition(request.getCondition());
        product.setStatus(request.getStatus());

        Product updatedProduct = productRepository.save(product);

        return productMapper.toResponse(updatedProduct);
    }

    @Override
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(()-> new ProductNotFoundException("Product not found"));

        productRepository.delete(product);

    }
}
