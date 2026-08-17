package kg.dev.pixel_commerce_ai.dto;

import kg.dev.pixel_commerce_ai.entity.ProductCondition;
import kg.dev.pixel_commerce_ai.entity.ProductStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductResponse {

    private Long id;

    private String brand;

    private String model;

    private Integer storage;

    private ProductCondition condition;

    private BigDecimal purchasePrice;

    private String purchaseCurrency;

    private BigDecimal salePrice;

    private ProductStatus status;
}