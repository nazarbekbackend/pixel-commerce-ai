package kg.dev.pixel_commerce_ai.repository;

import kg.dev.pixel_commerce_ai.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    @Query("""
            select coalesce(sum(s.totalAmount), 0)
            from Sale s
            """)
    BigDecimal getTotalRevenue();

    @Query("""
            select coalesce(sum(s.expenses), 0)
            from Sale s
            """)
    BigDecimal getTotalExpenses();

    @Query("""
            select coalesce(sum(si.costPrice * si.quantity), 0)
            from SaleItem si
            """)
    BigDecimal getTotalCost();

    @Query("""
            select coalesce(sum(si.quantity), 0)
            from SaleItem si
            """)
    Integer getSoldItems();
}