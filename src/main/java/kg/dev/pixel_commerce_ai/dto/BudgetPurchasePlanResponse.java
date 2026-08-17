package kg.dev.pixel_commerce_ai.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class BudgetPurchasePlanResponse {

    private BigDecimal availableBudget;

    private BigDecimal usedBudget;

    private BigDecimal remainingBudget;

    private BigDecimal expectedRevenue;

    private BigDecimal expectedProfit;

    private List<BudgetPurchaseItemResponse> items;
}