package kg.dev.pixel_commerce_ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class AnalyticsDashboardResponse {

    private BigDecimal revenue;

    private BigDecimal cost;

    private BigDecimal grossProfit;

    private BigDecimal expenses;

    private BigDecimal netProfit;

    private Integer productsSold;

    private Integer productsInStock;
}