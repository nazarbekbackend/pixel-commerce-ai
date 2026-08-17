package kg.dev.pixel_commerce_ai.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RecommendationResponse {

    private Long productId;

    private String brand;

    private String model;

    private Integer stock;

    private Integer sold;

    private BigDecimal profit;

    private String recommendation;

    private Integer suggestedQuantity;
}