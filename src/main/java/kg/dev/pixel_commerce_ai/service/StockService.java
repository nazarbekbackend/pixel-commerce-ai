package kg.dev.pixel_commerce_ai.service;

public interface StockService {

    void removeFromStock(
            Long productId,
            Integer quantity
    );
}