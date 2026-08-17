package kg.dev.pixel_commerce_ai.mapper;

import kg.dev.pixel_commerce_ai.dto.PurchaseItemResponse;
import kg.dev.pixel_commerce_ai.dto.PurchaseResponse;
import kg.dev.pixel_commerce_ai.entity.Purchase;
import kg.dev.pixel_commerce_ai.entity.PurchaseItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PurchaseMapper {

    public PurchaseResponse toResponse(
            Purchase purchase,
            List<PurchaseItem> items) {

        PurchaseResponse response = new PurchaseResponse();

        response.setId(purchase.getId());
        response.setPurchaseDate(purchase.getPurchaseDate());

        response.setSupplierId(purchase.getSupplier().getId());
        response.setSupplierName(purchase.getSupplier().getName());

        response.setTotalAmount(purchase.getTotalAmount());
        response.setCurrency(purchase.getCurrency());
        response.setShippingCost(purchase.getShippingCost());

        List<PurchaseItemResponse> itemResponses = items.stream()
                .map(this::toItemResponse)
                .toList();

        response.setItems(itemResponses);

        return response;
    }

    private PurchaseItemResponse toItemResponse(PurchaseItem item) {

        PurchaseItemResponse response = new PurchaseItemResponse();

        response.setId(item.getId());
        response.setProductId(item.getProduct().getId());
        response.setBrand(item.getProduct().getBrand());
        response.setModel(item.getProduct().getModel());
        response.setStorage(item.getProduct().getStorage());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());

        return response;
    }
}