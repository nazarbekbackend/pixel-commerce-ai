package kg.dev.pixel_commerce_ai.repository;

import kg.dev.pixel_commerce_ai.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
}
