package kg.dev.pixel_commerce_ai.service;

import kg.dev.pixel_commerce_ai.dto.SaleItemRequest;
import kg.dev.pixel_commerce_ai.dto.SaleRequest;
import kg.dev.pixel_commerce_ai.entity.Product;
import kg.dev.pixel_commerce_ai.entity.Sale;
import kg.dev.pixel_commerce_ai.entity.SaleItem;
import kg.dev.pixel_commerce_ai.entity.Stock;
import kg.dev.pixel_commerce_ai.entity.StockBatch;
import kg.dev.pixel_commerce_ai.repository.ProductRepository;
import kg.dev.pixel_commerce_ai.repository.SaleItemRepository;
import kg.dev.pixel_commerce_ai.repository.SaleRepository;
import kg.dev.pixel_commerce_ai.repository.StockBatchRepository;
import kg.dev.pixel_commerce_ai.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final StockBatchRepository stockBatchRepository;

    @Override
    @Transactional
    public void create(SaleRequest request) {

        Sale sale = new Sale();

        sale.setSaleDate(request.getSaleDate());
        sale.setCurrency(request.getCurrency());
        sale.setExchangeRate(request.getExchangeRate());
        sale.setExpenses(request.getExpenses());

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;

        for (SaleItemRequest itemRequest : request.getItems()) {

            Product product = productRepository.findById(
                    itemRequest.getProductId()
            ).orElseThrow(() ->
                    new RuntimeException("Product not found"));

            Stock stock = stockRepository.findByProductId(
                    product.getId()
            ).orElseThrow(() ->
                    new RuntimeException("Stock not found"));

            if (stock.getQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException(
                        "Not enough stock for " + product.getModel()
                );
            }

            int remainingQuantity = itemRequest.getQuantity();

            List<StockBatch> batches =
                    stockBatchRepository
                            .findByProductIdAndQuantityGreaterThanOrderByPurchaseDateAsc(
                                    product.getId(),
                                    0
                            );

            if (batches.isEmpty()) {
                throw new RuntimeException(
                        "No stock batches found for "
                                + product.getModel()
                );
            }

            BigDecimal totalCostKgs = BigDecimal.ZERO;

            for (StockBatch batch : batches) {

                if (remainingQuantity <= 0) {
                    break;
                }

                int batchQuantity = batch.getQuantity();

                int quantityFromBatch =
                        Math.min(
                                remainingQuantity,
                                batchQuantity
                        );

                BigDecimal batchCostKgs =
                        batch.getUnitCost()
                                .multiply(
                                        request.getExchangeRate()
                                );

                BigDecimal batchTotalCost =
                        batchCostKgs.multiply(
                                BigDecimal.valueOf(
                                        quantityFromBatch
                                )
                        );

                totalCostKgs =
                        totalCostKgs.add(batchTotalCost);

                batch.setQuantity(
                        batchQuantity - quantityFromBatch
                );

                stockBatchRepository.save(batch);

                remainingQuantity -= quantityFromBatch;
            }

            if (remainingQuantity > 0) {
                throw new RuntimeException(
                        "Not enough stock batches for "
                                + product.getModel()
                );
            }

            BigDecimal saleTotal =
                    itemRequest.getUnitPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            itemRequest.getQuantity()
                                    )
                            );

            BigDecimal profit =
                    saleTotal.subtract(totalCostKgs);

            totalAmount =
                    totalAmount.add(saleTotal);

            totalProfit =
                    totalProfit.add(profit);

            stock.setQuantity(
                    stock.getQuantity()
                            - itemRequest.getQuantity()
            );

            stockRepository.save(stock);

            SaleItem saleItem = new SaleItem();

            saleItem.setSale(sale);
            saleItem.setProduct(product);
            saleItem.setQuantity(
                    itemRequest.getQuantity()
            );
            saleItem.setUnitPrice(
                    itemRequest.getUnitPrice()
            );

            /*
             * Средняя себестоимость одной проданной единицы
             * с учётом FIFO.
             */
            BigDecimal averageCostPrice =
                    totalCostKgs.divide(
                            BigDecimal.valueOf(
                                    itemRequest.getQuantity()
                            ),
                            2,
                            java.math.RoundingMode.HALF_UP
                    );

            saleItem.setCostPrice(
                    averageCostPrice
            );

            saleItem.setProfit(profit);

            sale.getItems().add(saleItem);
        }

        BigDecimal netProfit =
                totalProfit.subtract(
                        request.getExpenses()
                );

        sale.setTotalAmount(totalAmount);
        sale.setNetProfit(netProfit);

        Sale savedSale =
                saleRepository.save(sale);

        for (SaleItem saleItem : sale.getItems()) {

            saleItem.setSale(savedSale);

            saleItemRepository.save(saleItem);
        }
    }
}