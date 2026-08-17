package kg.dev.pixel_commerce_ai.repository;

import kg.dev.pixel_commerce_ai.entity.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseItemRepository
        extends JpaRepository<PurchaseItem, Long> {

    List<PurchaseItem> findByPurchaseId(Long purchaseId);

    PurchaseItem findTopByProductIdOrderByPurchasePurchaseDateDesc(
            Long productId
    );
}