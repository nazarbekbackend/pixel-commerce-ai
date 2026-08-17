package kg.dev.pixel_commerce_ai.controller;

import kg.dev.pixel_commerce_ai.dto.*;
import kg.dev.pixel_commerce_ai.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public DashboardResponse getDashboard() {

        return analyticsService.getDashboard();
    }

    @GetMapping("/products")
    public List<ProductAnalyticsResponse> getProductsAnalytics() {

        return analyticsService.getProductsAnalytics();
    }

    @GetMapping("/recommendations")
    public List<RecommendationResponse> getRecommendations() {

        return analyticsService.getRecommendations();
    }

    @GetMapping("/purchase-plan")
    public List<PurchasePlanResponse> getPurchasePlan(
            @RequestParam BigDecimal exchangeRate
    ) {

        return analyticsService.getPurchasePlan(
                exchangeRate
        );
    }
    @GetMapping("/budget-purchase-plan")
    public BudgetPurchasePlanResponse getBudgetPurchasePlan(
            @RequestParam BigDecimal exchangeRate,
            @RequestParam BigDecimal budget
    ) {

        return analyticsService.getBudgetPurchasePlan(
                exchangeRate,
                budget
        );
    }
}