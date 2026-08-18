package kg.dev.pixel_commerce_ai.mapper;

import kg.dev.pixel_commerce_ai.dto.ProductRequest;
import kg.dev.pixel_commerce_ai.dto.ProductResponse;
import kg.dev.pixel_commerce_ai.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequest request){

        Product product = new Product();

        product.setBrand(request.getBrand());
        product.setCondition(request.getCondition());
        product.setModel(request.getModel());
        product.setStorage(request.getStorage());
        product.setStatus(request.getStatus());
        product.setPurchasePrice(request.getPurchasePrice());
        product.setSalePrice(request.getSalePrice());

        return product;



    }

    public ProductResponse toResponse(Product product){

        ProductResponse response = new ProductResponse();

        response.setId(product.getId());
        response.setBrand(product.getBrand());
        response.setCondition(product.getCondition());
        response.setModel(product.getModel());
        response.setStatus(product.getStatus());
        response.setStorage(product.getStorage());
        response.setSalePrice(product.getSalePrice());

        return response;
    }




}
