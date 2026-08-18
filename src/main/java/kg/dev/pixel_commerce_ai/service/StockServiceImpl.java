package kg.dev.pixel_commerce_ai.service;

import kg.dev.pixel_commerce_ai.entity.Stock;
import kg.dev.pixel_commerce_ai.entity.StockBatch;
import kg.dev.pixel_commerce_ai.repository.StockBatchRepository;
import kg.dev.pixel_commerce_ai.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final StockBatchRepository stockBatchRepository;

    @Override
    @Transactional
    public void removeFromStock(
            Long productId,
            Integer quantity
    ) {

        if (quantity == null || quantity <= 0) {
            throw new RuntimeException(
                    "Quantity must be greater than 0"
            );
        }

        Stock stock =
                stockRepository.findByProductId(productId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Stock not found"
                                )
                        );

        if (stock.getQuantity() < quantity) {
            throw new RuntimeException(
                    "Not enough stock"
            );
        }

        int remainingQuantity = quantity;

        /*
         * Берём партии с самой свежей закупки.
         *
         * Это именно списание со склада,
         * а не продажа, поэтому FIFO из SaleService
         * здесь не используется.
         */
        List<StockBatch> batches =
                stockBatchRepository
                        .findByProductIdAndQuantityGreaterThanOrderByPurchaseDateAsc(
                                productId,
                                0
                        );

        for (StockBatch batch : batches) {

            if (remainingQuantity <= 0) {
                break;
            }

            int batchQuantity =
                    batch.getQuantity();

            int quantityToRemove =
                    Math.min(
                            remainingQuantity,
                            batchQuantity
                    );

            batch.setQuantity(
                    batchQuantity - quantityToRemove
            );

            stockBatchRepository.save(batch);

            remainingQuantity -= quantityToRemove;
        }

        if (remainingQuantity > 0) {

            throw new RuntimeException(
                    "Not enough stock batches"
            );
        }

        stock.setQuantity(
                stock.getQuantity() - quantity
        );

        stockRepository.save(stock);
    }
}