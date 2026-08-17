package kg.dev.pixel_commerce_ai.repository;

import kg.dev.pixel_commerce_ai.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
