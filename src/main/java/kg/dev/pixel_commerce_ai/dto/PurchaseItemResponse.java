package kg.dev.pixel_commerce_ai.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PurchaseItemResponse {

    private Long id;

    private Long productId;

    private String brand;

    private String model;

    private Integer storage;

    private Integer quantity;

    private BigDecimal unitPrice;
}