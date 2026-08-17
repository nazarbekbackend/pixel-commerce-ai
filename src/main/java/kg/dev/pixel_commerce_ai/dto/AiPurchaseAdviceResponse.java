package kg.dev.pixel_commerce_ai.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class AiPurchaseAdviceResponse {

    private BigDecimal budget;

    private BigDecimal usedBudget;

    private BigDecimal remainingBudget;

    private BigDecimal expectedRevenue;

    private BigDecimal expectedProfit;

    private List<BudgetPurchaseItemResponse> recommendedItems;

    private String advice;
}