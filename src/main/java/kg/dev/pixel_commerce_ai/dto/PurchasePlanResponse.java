package kg.dev.pixel_commerce_ai.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PurchasePlanResponse {

    private Long productId;

    private String brand;

    private String model;

    private Integer storage;

    private Integer recommendedQuantity;

    private BigDecimal estimatedUnitCost;

    private BigDecimal requiredBudget;

    private BigDecimal expectedRevenue;

    private BigDecimal expectedProfit;
}