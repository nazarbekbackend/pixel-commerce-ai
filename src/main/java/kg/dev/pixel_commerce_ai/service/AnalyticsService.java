package kg.dev.pixel_commerce_ai.service;

import kg.dev.pixel_commerce_ai.dto.*;

import java.math.BigDecimal;
import java.util.List;

public interface AnalyticsService {

    DashboardResponse getDashboard();

    List<ProductAnalyticsResponse> getProductsAnalytics();

    List<RecommendationResponse> getRecommendations();

    List<PurchasePlanResponse> getPurchasePlan(BigDecimal exchangeRate);

    BudgetPurchasePlanResponse getBudgetPurchasePlan(
            BigDecimal exchangeRate,
            BigDecimal budget
    );
    List<ProductAnalyticsResponse> getProductsInStock();
}