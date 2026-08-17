package kg.dev.pixel_commerce_ai.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PurchaseRequest {

    @NotNull(message = "Purchase date is required")
    private LocalDateTime purchaseDate;

    @NotNull(message = "Supplier id is required")
    private Long supplierId;

    @NotNull(message = "Currency is required")
    private String currency;

    @Positive(message = "Shipping cost must be positive")
    private BigDecimal shippingCost;

    @NotNull(message = "Items are required")
    @Valid
    private List<PurchaseItemRequest> items;
}