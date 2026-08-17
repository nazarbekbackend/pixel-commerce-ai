package kg.dev.pixel_commerce_ai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brand;
    private String model;
    private Integer storage;

    @Enumerated(EnumType.STRING)
    private ProductCondition condition;
    private BigDecimal purchasePrice;
    private String purchaseCurrency;
    private BigDecimal salePrice;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;
}
