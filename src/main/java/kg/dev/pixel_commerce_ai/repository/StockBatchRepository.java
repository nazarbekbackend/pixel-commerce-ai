package kg.dev.pixel_commerce_ai.repository;

import kg.dev.pixel_commerce_ai.entity.StockBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockBatchRepository
        extends JpaRepository<StockBatch, Long> {

    List<StockBatch> findByProductIdAndQuantityGreaterThanOrderByPurchaseDateAsc(
            Long productId,
            Integer quantity
    );

    StockBatch findTopByProductIdOrderByPurchaseDateDesc(
            Long productId
    );
}