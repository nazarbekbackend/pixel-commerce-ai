package kg.dev.pixel_commerce_ai.repository;

import kg.dev.pixel_commerce_ai.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {

    @Query("""
            select si.product.id,
                   coalesce(sum(si.quantity), 0),
                   coalesce(sum(si.unitPrice * si.quantity), 0),
                   coalesce(sum(si.profit), 0)
            from SaleItem si
            group by si.product.id
            """)
    List<Object[]> getProductSalesAnalytics();
}