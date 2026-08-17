package kg.dev.pixel_commerce_ai.service;

import kg.dev.pixel_commerce_ai.dto.*;
import kg.dev.pixel_commerce_ai.entity.Product;
import kg.dev.pixel_commerce_ai.entity.Sale;
import kg.dev.pixel_commerce_ai.entity.SaleItem;
import kg.dev.pixel_commerce_ai.entity.Stock;
import kg.dev.pixel_commerce_ai.entity.StockBatch;
import kg.dev.pixel_commerce_ai.repository.ProductRepository;
import kg.dev.pixel_commerce_ai.repository.SaleRepository;
import kg.dev.pixel_commerce_ai.repository.StockBatchRepository;
import kg.dev.pixel_commerce_ai.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final int ANALYTICS_DAYS = 7;
    private static final int PLANNING_DAYS = 14;

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final StockBatchRepository stockBatchRepository;


    // =========================================================
    // DASHBOARD
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {

        List<Sale> sales =
                saleRepository.findAll();

        List<Stock> stocks =
                stockRepository.findAll();

        BigDecimal revenue =
                BigDecimal.ZERO;

        BigDecimal cost =
                BigDecimal.ZERO;

        BigDecimal expenses =
                BigDecimal.ZERO;

        int soldItems = 0;

        int stockItems = 0;


        for (Sale sale : sales) {

            if (sale.getExpenses() != null) {

                expenses =
                        expenses.add(
                                sale.getExpenses()
                        );
            }


            for (SaleItem item : sale.getItems()) {

                BigDecimal itemRevenue =
                        item.getUnitPrice()
                                .multiply(
                                        BigDecimal.valueOf(
                                                item.getQuantity()
                                        )
                                );

                BigDecimal itemCost =
                        BigDecimal.ZERO;


                if (item.getCostPrice() != null) {

                    itemCost =
                            item.getCostPrice()
                                    .multiply(
                                            BigDecimal.valueOf(
                                                    item.getQuantity()
                                            )
                                    );
                }


                revenue =
                        revenue.add(
                                itemRevenue
                        );

                cost =
                        cost.add(
                                itemCost
                        );

                soldItems +=
                        item.getQuantity();
            }
        }


        for (Stock stock : stocks) {

            if (stock.getQuantity() != null) {

                stockItems +=
                        stock.getQuantity();
            }
        }


        BigDecimal netProfit =
                revenue
                        .subtract(cost)
                        .subtract(expenses);


        DashboardResponse response =
                new DashboardResponse();

        response.setRevenue(
                revenue
        );

        response.setCost(
                cost
        );

        response.setExpenses(
                expenses
        );

        response.setNetProfit(
                netProfit
        );

        response.setStockItems(
                stockItems
        );

        response.setSoldItems(
                soldItems
        );


        return response;
    }


    // =========================================================
    // PRODUCT ANALYTICS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<ProductAnalyticsResponse> getProductsAnalytics() {

        List<Product> products =
                productRepository.findAll();

        List<Sale> sales =
                saleRepository.findAll();


        Map<Long, Integer> soldMap =
                new HashMap<>();

        Map<Long, BigDecimal> revenueMap =
                new HashMap<>();

        Map<Long, BigDecimal> profitMap =
                new HashMap<>();

        Map<Long, Integer> recentSoldMap =
                new HashMap<>();


        LocalDateTime sevenDaysAgo =
                LocalDateTime.now()
                        .minusDays(
                                ANALYTICS_DAYS
                        );


        for (Sale sale : sales) {

            boolean recent =
                    sale.getSaleDate() != null
                            && sale.getSaleDate()
                            .isAfter(
                                    sevenDaysAgo
                            );


            for (SaleItem item : sale.getItems()) {

                Long productId =
                        item.getProduct()
                                .getId();


                // -------------------------
                // TOTAL SOLD
                // -------------------------

                int sold =
                        soldMap.getOrDefault(
                                productId,
                                0
                        );


                soldMap.put(
                        productId,
                        sold + item.getQuantity()
                );


                // -------------------------
                // REVENUE
                // -------------------------

                BigDecimal itemRevenue =
                        item.getUnitPrice()
                                .multiply(
                                        BigDecimal.valueOf(
                                                item.getQuantity()
                                        )
                                );


                BigDecimal currentRevenue =
                        revenueMap.getOrDefault(
                                productId,
                                BigDecimal.ZERO
                        );


                revenueMap.put(
                        productId,
                        currentRevenue.add(
                                itemRevenue
                        )
                );


                // -------------------------
                // PROFIT
                // -------------------------

                BigDecimal itemProfit =
                        item.getProfit() != null
                                ? item.getProfit()
                                : BigDecimal.ZERO;


                BigDecimal currentProfit =
                        profitMap.getOrDefault(
                                productId,
                                BigDecimal.ZERO
                        );


                profitMap.put(
                        productId,
                        currentProfit.add(
                                itemProfit
                        )
                );


                // -------------------------
                // LAST 7 DAYS
                // -------------------------

                if (recent) {

                    int recentSold =
                            recentSoldMap.getOrDefault(
                                    productId,
                                    0
                            );


                    recentSoldMap.put(
                            productId,
                            recentSold
                                    + item.getQuantity()
                    );
                }
            }
        }


        // =====================================================
        // STOCK
        // =====================================================

        Map<Long, Integer> stockMap =
                new HashMap<>();


        for (Stock stock : stockRepository.findAll()) {

            if (stock.getProduct() != null) {

                stockMap.put(
                        stock.getProduct().getId(),
                        stock.getQuantity()
                );
            }
        }


        // =====================================================
        // BUILD RESPONSE
        // =====================================================

        return products.stream()
                .map(product -> {

                    Long productId =
                            product.getId();


                    int stock =
                            stockMap.getOrDefault(
                                    productId,
                                    0
                            );


                    int sold =
                            soldMap.getOrDefault(
                                    productId,
                                    0
                            );


                    int recentSold =
                            recentSoldMap.getOrDefault(
                                    productId,
                                    0
                            );


                    // -------------------------
                    // SALES PER DAY
                    // -------------------------

                    BigDecimal salesPerDay =
                            BigDecimal.valueOf(
                                            recentSold
                                    )
                                    .divide(
                                            BigDecimal.valueOf(
                                                    ANALYTICS_DAYS
                                            ),
                                            2,
                                            RoundingMode.HALF_UP
                                    );


                    // -------------------------
                    // DAYS OF STOCK
                    // -------------------------

                    int daysOfStock = 0;


                    if (salesPerDay.compareTo(
                            BigDecimal.ZERO
                    ) > 0) {

                        daysOfStock =
                                BigDecimal.valueOf(
                                                stock
                                        )
                                        .divide(
                                                salesPerDay,
                                                0,
                                                RoundingMode.FLOOR
                                        )
                                        .intValue();
                    }


                    // -------------------------
                    // RECOMMENDATION
                    // -------------------------

                    String recommendation;


                    if (recentSold == 0) {

                        recommendation =
                                "WAIT";

                    } else if (stock == 0) {

                        recommendation =
                                "URGENT_ORDER";

                    } else if (daysOfStock <= 3) {

                        recommendation =
                                "ORDER";

                    } else {

                        recommendation =
                                "MONITOR";
                    }


                    ProductAnalyticsResponse response =
                            new ProductAnalyticsResponse();


                    response.setProductId(
                            productId
                    );

                    response.setBrand(
                            product.getBrand()
                    );

                    response.setModel(
                            product.getModel()
                    );

                    response.setStorage(
                            product.getStorage()
                    );

                    response.setStock(
                            stock
                    );

                    response.setSold(
                            sold
                    );

                    response.setRevenue(
                            revenueMap.getOrDefault(
                                    productId,
                                    BigDecimal.ZERO
                            )
                    );

                    response.setProfit(
                            profitMap.getOrDefault(
                                    productId,
                                    BigDecimal.ZERO
                            )
                    );

                    response.setSalesPerDay(
                            salesPerDay
                    );

                    response.setDaysOfStock(
                            daysOfStock
                    );

                    response.setRecommendation(
                            recommendation
                    );


                    return response;

                })
                .toList();
    }


    // =========================================================
    // RECOMMENDATIONS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<RecommendationResponse> getRecommendations() {

        List<ProductAnalyticsResponse> analytics =
                getProductsAnalytics();


        return analytics.stream()
                .map(product -> {

                    RecommendationResponse response =
                            new RecommendationResponse();


                    response.setProductId(
                            product.getProductId()
                    );

                    response.setBrand(
                            product.getBrand()
                    );

                    response.setModel(
                            product.getModel()
                    );

                    response.setStock(
                            product.getStock()
                    );

                    response.setSold(
                            product.getSold()
                    );

                    response.setProfit(
                            product.getProfit()
                    );


                    // -------------------------
                    // REQUIRED STOCK
                    // -------------------------

                    BigDecimal requiredStock =
                            product.getSalesPerDay()
                                    .multiply(
                                            BigDecimal.valueOf(
                                                    PLANNING_DAYS
                                            )
                                    );


                    // -------------------------
                    // ROUND UP
                    // -------------------------

                    int requiredQuantity =
                            requiredStock
                                    .setScale(
                                            0,
                                            RoundingMode.CEILING
                                    )
                                    .intValue();


                    // -------------------------
                    // SUBTRACT CURRENT STOCK
                    // -------------------------

                    int suggestedQuantity =
                            requiredQuantity
                                    - product.getStock();


                    suggestedQuantity =
                            Math.max(
                                    suggestedQuantity,
                                    0
                            );


                    String recommendation =
                            product.getRecommendation();


                    // -------------------------
                    // NO SALES
                    // -------------------------

                    if (product.getSold() == 0) {

                        recommendation =
                                "WAIT";

                        suggestedQuantity =
                                0;
                    }


                    // -------------------------
                    // NO STOCK
                    // -------------------------

                    else if (product.getStock() == 0) {

                        recommendation =
                                "URGENT_ORDER";
                    }


                    // -------------------------
                    // LOW STOCK
                    // -------------------------

                    else if (
                            product.getDaysOfStock() <= 3
                    ) {

                        recommendation =
                                "ORDER";
                    }


                    // -------------------------
                    // NORMAL
                    // -------------------------

                    else {

                        recommendation =
                                "MONITOR";
                    }


                    response.setRecommendation(
                            recommendation
                    );

                    response.setSuggestedQuantity(
                            suggestedQuantity
                    );


                    return response;

                })
                .toList();
    }


    // =========================================================
    // PURCHASE PLAN
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<PurchasePlanResponse> getPurchasePlan(
            BigDecimal exchangeRate
    ) {

        if (exchangeRate == null
                || exchangeRate.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            throw new IllegalArgumentException(
                    "Exchange rate must be positive"
            );
        }


        List<ProductAnalyticsResponse> analytics =
                getProductsAnalytics();


        return analytics.stream()

                .filter(product ->
                        product.getRecommendation()
                                .equals("ORDER")
                                || product.getRecommendation()
                                .equals("URGENT_ORDER")
                )

                .map(product -> {

                    PurchasePlanResponse response =
                            new PurchasePlanResponse();


                    // -------------------------
                    // QUANTITY
                    // -------------------------

                    int recommendedQuantity =
                            calculateRecommendedQuantity(
                                    product
                            );


                    // -------------------------
                    // LAST PURCHASE PRICE
                    // -------------------------

                    BigDecimal estimatedUnitCost =
                            getLatestUnitCost(
                                    product.getProductId()
                            );


                    // -------------------------
                    // CNY → KGS
                    // -------------------------

                    BigDecimal unitCostKgs =
                            estimatedUnitCost
                                    .multiply(
                                            exchangeRate
                                    );


                    // -------------------------
                    // REQUIRED BUDGET
                    // -------------------------

                    BigDecimal requiredBudget =
                            unitCostKgs
                                    .multiply(
                                            BigDecimal.valueOf(
                                                    recommendedQuantity
                                            )
                                    );


                    // -------------------------
                    // EXPECTED REVENUE
                    // -------------------------

                    BigDecimal expectedRevenue =
                            getExpectedRevenue(
                                    product,
                                    recommendedQuantity
                            );


                    // -------------------------
                    // EXPECTED PROFIT
                    // -------------------------

                    BigDecimal expectedProfit =
                            expectedRevenue
                                    .subtract(
                                            requiredBudget
                                    );


                    // -------------------------
                    // RESPONSE
                    // -------------------------

                    response.setProductId(
                            product.getProductId()
                    );

                    response.setBrand(
                            product.getBrand()
                    );

                    response.setModel(
                            product.getModel()
                    );

                    response.setStorage(
                            product.getStorage()
                    );

                    response.setRecommendedQuantity(
                            recommendedQuantity
                    );

                    response.setEstimatedUnitCost(
                            unitCostKgs
                    );

                    response.setRequiredBudget(
                            requiredBudget
                    );

                    response.setExpectedRevenue(
                            expectedRevenue
                    );

                    response.setExpectedProfit(
                            expectedProfit
                    );


                    return response;

                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetPurchasePlanResponse getBudgetPurchasePlan(
            BigDecimal exchangeRate,
            BigDecimal budget
    ) {

        if (exchangeRate == null
                || exchangeRate.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Exchange rate must be positive"
            );
        }

        if (budget == null
                || budget.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Budget must be positive"
            );
        }

        List<ProductAnalyticsResponse> analytics =
                getProductsAnalytics();

        List<BudgetPurchaseItemResponse> items =
                new java.util.ArrayList<>();

        BigDecimal remainingBudget = budget;

        BigDecimal usedBudget =
                BigDecimal.ZERO;

        BigDecimal expectedRevenue =
                BigDecimal.ZERO;

        BigDecimal expectedProfit =
                BigDecimal.ZERO;

        /*
         * Сначала самые интересные товары:
         * чем выше прибыль на одну единицу,
         * тем выше приоритет закупки.
         */
        List<ProductAnalyticsResponse> products =
                analytics.stream()
                        .filter(product ->
                                product.getRecommendation()
                                        .equals("ORDER")
                                        || product.getRecommendation()
                                        .equals("URGENT_ORDER")
                        )
                        .sorted(
                                (a, b) ->
                                        b.getProfit()
                                                .compareTo(
                                                        a.getProfit()
                                                )
                        )
                        .toList();

        for (ProductAnalyticsResponse product : products) {

            if (remainingBudget.compareTo(
                    BigDecimal.ZERO
            ) <= 0) {
                break;
            }

            int recommendedQuantity =
                    calculateRecommendedQuantity(
                            product
                    );

            if (recommendedQuantity <= 0) {
                continue;
            }

            BigDecimal unitCostCny =
                    getLatestUnitCost(
                            product.getProductId()
                    );

            BigDecimal unitCostKgs =
                    unitCostCny.multiply(
                            exchangeRate
                    );

            if (unitCostKgs.compareTo(
                    BigDecimal.ZERO
            ) <= 0) {
                continue;
            }

            /*
             * Сколько товара реально можем
             * купить на оставшийся бюджет.
             */
            int affordableQuantity =
                    remainingBudget
                            .divide(
                                    unitCostKgs,
                                    0,
                                    RoundingMode.FLOOR
                            )
                            .intValue();

            int quantity =
                    Math.min(
                            recommendedQuantity,
                            affordableQuantity
                    );

            if (quantity <= 0) {
                continue;
            }

            BigDecimal totalCost =
                    unitCostKgs.multiply(
                            BigDecimal.valueOf(
                                    quantity
                            )
                    );

            BigDecimal averageSalePrice =
                    BigDecimal.ZERO;

            if (product.getSold() > 0) {

                averageSalePrice =
                        product.getRevenue()
                                .divide(
                                        BigDecimal.valueOf(
                                                product.getSold()
                                        ),
                                        2,
                                        RoundingMode.HALF_UP
                                );
            }

            BigDecimal itemExpectedRevenue =
                    averageSalePrice.multiply(
                            BigDecimal.valueOf(
                                    quantity
                            )
                    );

            BigDecimal itemExpectedProfit =
                    itemExpectedRevenue.subtract(
                            totalCost
                    );

            BudgetPurchaseItemResponse item =
                    new BudgetPurchaseItemResponse();

            item.setProductId(
                    product.getProductId()
            );

            item.setBrand(
                    product.getBrand()
            );

            item.setModel(
                    product.getModel()
            );

            item.setStorage(
                    product.getStorage()
            );

            item.setQuantity(
                    quantity
            );

            item.setUnitCost(
                    unitCostKgs
            );

            item.setTotalCost(
                    totalCost
            );

            item.setExpectedRevenue(
                    itemExpectedRevenue
            );

            item.setExpectedProfit(
                    itemExpectedProfit
            );

            items.add(item);

            usedBudget =
                    usedBudget.add(
                            totalCost
                    );

            remainingBudget =
                    remainingBudget.subtract(
                            totalCost
                    );

            expectedRevenue =
                    expectedRevenue.add(
                            itemExpectedRevenue
                    );

            expectedProfit =
                    expectedProfit.add(
                            itemExpectedProfit
                    );
        }

        BudgetPurchasePlanResponse response =
                new BudgetPurchasePlanResponse();

        response.setAvailableBudget(
                budget
        );

        response.setUsedBudget(
                usedBudget
        );

        response.setRemainingBudget(
                remainingBudget
        );

        response.setExpectedRevenue(
                expectedRevenue
        );

        response.setExpectedProfit(
                expectedProfit
        );

        response.setItems(items);

        return response;
    }


    // =========================================================
    // CALCULATE RECOMMENDED QUANTITY
    // =========================================================

    private int calculateRecommendedQuantity(
            ProductAnalyticsResponse product
    ) {

        BigDecimal requiredStock =
                product.getSalesPerDay()
                        .multiply(
                                BigDecimal.valueOf(
                                        PLANNING_DAYS
                                )
                        );


        int requiredQuantity =
                requiredStock
                        .setScale(
                                0,
                                RoundingMode.CEILING
                        )
                        .intValue();


        return Math.max(
                requiredQuantity
                        - product.getStock(),
                0
        );
    }


    // =========================================================
    // GET LATEST PURCHASE PRICE
    // =========================================================

    private BigDecimal getLatestUnitCost(
            Long productId
    ) {

        StockBatch batch =
                stockBatchRepository
                        .findTopByProductIdOrderByPurchaseDateDesc(
                                productId
                        );


        if (batch == null
                || batch.getUnitCost() == null) {

            return BigDecimal.ZERO;
        }


        return batch.getUnitCost();
    }


    // =========================================================
    // EXPECTED REVENUE
    // =========================================================

    private BigDecimal getExpectedRevenue(
            ProductAnalyticsResponse product,
            int quantity
    ) {

        if (product.getSold() == 0) {

            return BigDecimal.ZERO;
        }


        BigDecimal averageSalePrice =
                product.getRevenue()
                        .divide(
                                BigDecimal.valueOf(
                                        product.getSold()
                                ),
                                2,
                                RoundingMode.HALF_UP
                        );


        return averageSalePrice.multiply(
                BigDecimal.valueOf(
                        quantity
                )
        );
    }
}