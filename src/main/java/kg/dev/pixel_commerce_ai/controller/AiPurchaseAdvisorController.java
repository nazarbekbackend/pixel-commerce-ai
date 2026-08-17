package kg.dev.pixel_commerce_ai.controller;

import jakarta.validation.Valid;
import kg.dev.pixel_commerce_ai.dto.AiPurchaseAdviceRequest;
import kg.dev.pixel_commerce_ai.dto.AiPurchaseAdviceResponse;
import kg.dev.pixel_commerce_ai.service.AiPurchaseAdvisorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiPurchaseAdvisorController {

    private final AiPurchaseAdvisorService aiPurchaseAdvisorService;

    @PostMapping("/purchase-advice")
    public AiPurchaseAdviceResponse getPurchaseAdvice(
            @Valid @RequestBody AiPurchaseAdviceRequest request
    ) {

        return aiPurchaseAdvisorService.getAdvice(
                request
        );
    }
}