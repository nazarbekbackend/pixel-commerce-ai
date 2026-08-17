package kg.dev.pixel_commerce_ai.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PurchaseResponse {

    private Long id;

    private LocalDateTime purchaseDate;

    private Long supplierId;

    private String supplierName;

    private BigDecimal totalAmount;

    private String currency;

    private BigDecimal shippingCost;

    private List<PurchaseItemResponse> items;
}