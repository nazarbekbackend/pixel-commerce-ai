package kg.dev.pixel_commerce_ai.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DashboardResponse {

    private BigDecimal revenue;

    private BigDecimal cost;

    private BigDecimal expenses;

    private BigDecimal netProfit;

    private Integer stockItems;

    private Integer soldItems;
}
