package kg.dev.pixel_commerce_ai.service;
import kg.dev.pixel_commerce_ai.dto.PurchaseResponse;
import kg.dev.pixel_commerce_ai.entity.*;
import kg.dev.pixel_commerce_ai.dto.PurchaseItemRequest;
import kg.dev.pixel_commerce_ai.dto.PurchaseRequest;
import kg.dev.pixel_commerce_ai.exception.ProductNotFoundException;
import kg.dev.pixel_commerce_ai.mapper.PurchaseMapper;
import kg.dev.pixel_commerce_ai.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseMapper purchaseMapper;
    private final StockRepository stockRepository;
    private final StockBatchRepository stockBatchRepository;

    @Override
    @Transactional
    public void create(PurchaseRequest request) {

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(()-> new RuntimeException("Supplier not found"));

        Purchase purchase = new Purchase();

        purchase.setPurchaseDate(request.getPurchaseDate());
        purchase.setSupplier(supplier);
        purchase.setCurrency(request.getCurrency());
        purchase.setShippingCost(request.getShippingCost());

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (PurchaseItemRequest itemRequest : request.getItems()) {

            BigDecimal itemTotal = itemRequest.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            totalAmount = totalAmount.add(itemTotal);
        }

        purchase.setTotalAmount(totalAmount);

        Purchase savedPurchase = purchaseRepository.save(purchase);

        for (PurchaseItemRequest itemRequest : request.getItems()) {

            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() ->
                            new ProductNotFoundException("Product not found"));

            PurchaseItem purchaseItem = new PurchaseItem();

            purchaseItem.setPurchase(savedPurchase);
            purchaseItem.setProduct(product);
            purchaseItem.setQuantity(itemRequest.getQuantity());
            purchaseItem.setUnitPrice(itemRequest.getUnitPrice());

            purchaseItemRepository.save(purchaseItem);

            StockBatch stockBatch = new StockBatch();

            stockBatch.setProduct(product);
            stockBatch.setQuantity(itemRequest.getQuantity());
            stockBatch.setUnitCost(itemRequest.getUnitPrice());
            stockBatch.setPurchaseDate(savedPurchase.getPurchaseDate());
            stockBatch.setPurchase(savedPurchase);

            stockBatchRepository.save(stockBatch);

            Stock stock = stockRepository.findByProductId(product.getId())
                    .orElseGet(() -> {

                        Stock newStock = new Stock();

                        newStock.setProduct(product);
                        newStock.setQuantity(0);

                        return newStock;
                    });

            stock.setQuantity(
                    stock.getQuantity() + itemRequest.getQuantity()
            );

            stockRepository.save(stock);
        }

    }

    @Override
    public List<PurchaseResponse> findAll() {
        return purchaseRepository.findAll()
                .stream()
                .map(purchase -> {
                    List<PurchaseItem> items =
                            purchaseItemRepository.findByPurchaseId(
                                    purchase.getId()
                            );
                    return purchaseMapper.toResponse(purchase,items);
                })
                .toList();
    }
}