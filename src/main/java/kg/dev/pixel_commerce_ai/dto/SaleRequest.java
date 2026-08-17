package kg.dev.pixel_commerce_ai.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class SaleRequest {

    @NotNull
    private LocalDateTime saleDate;

    @NotNull
    private String currency;

    @NotNull
    private BigDecimal exchangeRate;

    @NotNull
    private BigDecimal expenses;

    @Valid
    @NotNull
    private List<SaleItemRequest> items;
}