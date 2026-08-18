package kg.dev.pixel_commerce_ai.repository;

import kg.dev.pixel_commerce_ai.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StockRepository
        extends JpaRepository<Stock, Long> {

    Optional<Stock> findByProductId(Long productId);

    List<Stock> findByQuantityGreaterThan(Integer quantity);

    @Query("""
            select coalesce(sum(s.quantity), 0)
            from Stock s
            """)
    Integer getTotalStock();
}