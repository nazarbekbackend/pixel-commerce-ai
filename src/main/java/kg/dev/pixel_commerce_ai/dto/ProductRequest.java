package kg.dev.pixel_commerce_ai.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kg.dev.pixel_commerce_ai.entity.ProductCondition;
import kg.dev.pixel_commerce_ai.entity.ProductStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequest {

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Model is required")
    private String model;

    @NotNull(message = "Storage is required")
    @Positive(message = "Storage must be positive")
    private Integer storage;

    @NotNull(message = "Condition is required")
    private ProductCondition condition;

    @NotNull(message = "Purchase price is required")
    @Positive(message = "Purchase price must be positive")
    private BigDecimal purchasePrice;

    @NotBlank(message = "Purchase currency is required")
    private String purchaseCurrency;

    @NotNull(message = "Sale price is required")
    @Positive(message = "Sale price must be positive")
    private BigDecimal salePrice;

    @NotNull(message = "Status is required")
    private ProductStatus status;
}
