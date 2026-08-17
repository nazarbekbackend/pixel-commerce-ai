package kg.dev.pixel_commerce_ai.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductAnalyticsResponse {

    private Long productId;

    private String brand;

    private String model;

    private Integer storage;

    private Integer stock;

    private Integer sold;

    private BigDecimal revenue;

    private BigDecimal profit;

    private BigDecimal salesPerDay;

    private Integer daysOfStock;

    private String recommendation;
}