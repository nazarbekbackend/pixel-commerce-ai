package kg.dev.pixel_commerce_ai.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AiPurchaseAdviceRequest {

    @NotNull(message = "Exchange rate is required")
    @Positive(message = "Exchange rate must be positive")
    private BigDecimal exchangeRate;

    @NotNull(message = "Budget is required")
    @Positive(message = "Budget must be positive")
    private BigDecimal budget;
}